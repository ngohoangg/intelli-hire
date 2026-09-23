package org.aibles.intellihireinterview.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class QuestionServiceClientTest {

    static MockWebServer mockServer;
    QuestionServiceClient client;
    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    static void startServer() throws Exception {
        mockServer = new MockWebServer();
        mockServer.start();
    }

    @AfterAll
    static void stopServer() throws Exception { mockServer.shutdown(); }

    @BeforeEach
    void setup() {
        var webClient = WebClient.builder()
            .baseUrl(mockServer.url("/").toString()).build();
        client = new QuestionServiceClient(webClient, objectMapper);
    }

    @Test
    void getRandomQuestions_returnsParsedList() throws Exception {
        var question = Map.of(
            "id", "q-001",
            "question_text", "What is JVM?",
            "topic_id", "topic-java",
            "difficulty_id", "diff-mid"
        );

        var responseBody = objectMapper.writeValueAsString(
            Map.of("success", true, "data", List.of(question))
        );

        mockServer.enqueue(new MockResponse()
            .setBody(responseBody)
            .addHeader("Content-Type", "application/json"));

        var result = client.getRandomQuestions("topic-java", "diff-mid", 1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("q-001");
        assertThat(result.get(0).getQuestionText()).isEqualTo("What is JVM?");
    }

    @Test
    void getRandomQuestions_returnsEmptyListOn503() {
        mockServer.enqueue(new MockResponse().setResponseCode(503));

        var result = client.getRandomQuestions("topic-java", "diff-mid", 5);

        assertThat(result).isEmpty();
    }

    @Test
    void getRandomQuestions_returnsEmptyListOn500() {
        mockServer.enqueue(new MockResponse().setResponseCode(500).setBody("Internal Server Error"));

        var result = client.getRandomQuestions("topic-java", "diff-mid", 5);

        assertThat(result).isEmpty();
    }

    @Test
    void getRandomQuestions_returnsEmptyListWhenSuccessFalse() throws Exception {
        var responseBody = objectMapper.writeValueAsString(
            Map.of("success", false, "error_code", "NOT_FOUND")
        );

        mockServer.enqueue(new MockResponse()
            .setBody(responseBody)
            .addHeader("Content-Type", "application/json"));

        var result = client.getRandomQuestions("unknown-topic", null, 5);

        assertThat(result).isEmpty();
    }
}
