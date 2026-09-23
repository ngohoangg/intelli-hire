package org.aibles.intellihiregrading.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.aibles.intellihiregrading.config.OpenRouterConfig;
import org.aibles.intellihiregrading.dto.openrouter.LlmGradingResult;
import org.aibles.intellihiregrading.service.impl.OpenRouterLlmServiceImpl;
import org.junit.jupiter.api.*;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.*;

class OpenRouterLlmServiceImplTest {

    private MockWebServer mockServer;
    private OpenRouterLlmServiceImpl llmService;

    @BeforeEach
    void setUp() throws Exception {
        mockServer = new MockWebServer();
        mockServer.start();

        OpenRouterConfig config = new OpenRouterConfig();
        config.setApiKey("test-key");
        config.setBaseUrl(mockServer.url("/").toString());
        config.setModel("test-model");
        config.setTemperature(0.3);
        config.setMaxTokens(1024);
        config.setTimeout(5000);

        WebClient webClient = WebClient.builder()
                .baseUrl(config.getBaseUrl())
                .build();

        llmService = new OpenRouterLlmServiceImpl(webClient, config, new ObjectMapper());
    }

    @AfterEach
    void tearDown() throws Exception {
        mockServer.shutdown();
    }

    @Test
    void grade_validJsonResponse_parsesCorrectly() {
        String body = """
                {
                  "choices": [{"message": {"role": "assistant", "content": "{\\"score\\": 8.5, \\"feedback\\": \\"Good answer\\"}"}}],
                  "model": "test-model"
                }
                """;
        mockServer.enqueue(new MockResponse()
                .setBody(body)
                .setHeader("Content-Type", "application/json"));

        LlmGradingResult result = llmService.grade("test prompt");

        assertEquals(8.5, result.getScore());
        assertEquals("Good answer", result.getFeedback());
        assertEquals("test-model", result.getModel());
    }

    @Test
    void grade_markdownWrappedResponse_stripsAndParses() {
        String body = """
                {
                  "choices": [{"message": {"role": "assistant", "content": "```json\\n{\\"score\\": 7.0, \\"feedback\\": \\"Decent\\"}\\n```"}}],
                  "model": "test-model"
                }
                """;
        mockServer.enqueue(new MockResponse()
                .setBody(body)
                .setHeader("Content-Type", "application/json"));

        LlmGradingResult result = llmService.grade("test prompt");

        assertEquals(7.0, result.getScore());
        assertEquals("Decent", result.getFeedback());
    }

    @Test
    void grade_invalidJsonFromLlm_returnsFallbackWithZeroScore() {
        String body = """
                {
                  "choices": [{"message": {"role": "assistant", "content": "This is not JSON at all"}}],
                  "model": "test-model"
                }
                """;
        mockServer.enqueue(new MockResponse()
                .setBody(body)
                .setHeader("Content-Type", "application/json"));

        LlmGradingResult result = llmService.grade("test prompt");

        assertEquals(0.0, result.getScore());
        assertTrue(result.getFeedback().contains("Parse error"));
    }

    @Test
    void grade_emptyChoices_returnsFallback() {
        String body = """
                {"choices": [], "model": "test-model"}
                """;
        mockServer.enqueue(new MockResponse()
                .setBody(body)
                .setHeader("Content-Type", "application/json"));

        LlmGradingResult result = llmService.grade("test prompt");

        assertEquals(0.0, result.getScore());
        assertTrue(result.getFeedback().contains("Unable to grade"));
    }

    @Test
    void grade_serverError_retriesAndReturnsFallback() {
        // First call: 500 error
        mockServer.enqueue(new MockResponse().setResponseCode(500));
        // Retry: also 500 error
        mockServer.enqueue(new MockResponse().setResponseCode(500));

        LlmGradingResult result = llmService.grade("test prompt");

        assertEquals(0.0, result.getScore());
        assertTrue(result.getFeedback().contains("Unable to grade"));
        assertEquals(2, mockServer.getRequestCount()); // original + 1 retry
    }

    @Test
    void grade_missingScoreField_defaultsToZero() {
        String body = """
                {
                  "choices": [{"message": {"role": "assistant", "content": "{\\"feedback\\": \\"No score field\\"}"}}],
                  "model": "test-model"
                }
                """;
        mockServer.enqueue(new MockResponse()
                .setBody(body)
                .setHeader("Content-Type", "application/json"));

        LlmGradingResult result = llmService.grade("test prompt");

        assertEquals(0.0, result.getScore());
        assertEquals("No score field", result.getFeedback());
    }

    @Test
    void grade_missingFeedbackField_defaultsToNoFeedback() {
        String body = """
                {
                  "choices": [{"message": {"role": "assistant", "content": "{\\"score\\": 9.0}"}}],
                  "model": "test-model"
                }
                """;
        mockServer.enqueue(new MockResponse()
                .setBody(body)
                .setHeader("Content-Type", "application/json"));

        LlmGradingResult result = llmService.grade("test prompt");

        assertEquals(9.0, result.getScore());
        assertEquals("No feedback provided", result.getFeedback());
    }

    @Test
    void grade_nullModelInResponse_fallsBackToConfigModel() {
        String body = """
                {
                  "choices": [{"message": {"role": "assistant", "content": "{\\"score\\": 5.0, \\"feedback\\": \\"OK\\"}"}}],
                  "model": null
                }
                """;
        mockServer.enqueue(new MockResponse()
                .setBody(body)
                .setHeader("Content-Type", "application/json"));

        LlmGradingResult result = llmService.grade("test prompt");

        assertEquals(5.0, result.getScore());
        assertEquals("test-model", result.getModel());
    }
}
