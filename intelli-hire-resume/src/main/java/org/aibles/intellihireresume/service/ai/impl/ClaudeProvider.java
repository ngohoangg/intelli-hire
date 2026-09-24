package org.aibles.intellihireresume.service.ai.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihireresume.config.AiProperties;
import org.aibles.intellihireresume.dto.ai.ResumeParseResult;
import org.aibles.intellihireresume.exception.AiAuthException;
import org.aibles.intellihireresume.service.ai.AiProvider;
import org.aibles.intellihireresume.service.ai.ResumeParsePromptBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
public class ClaudeProvider implements AiProvider {

    private static final String BASE_URL = "https://api.anthropic.com/v1";
    private static final String ANTHROPIC_VERSION = "2023-06-01";

    private final AiProperties.ClaudeConfig config;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    @Value("${app.ai.output-language:Vietnamese}")
    private String outputLanguage = "Vietnamese";

    public ClaudeProvider(AiProperties.ClaudeConfig config) {
        this.config = config;
        this.objectMapper = new ObjectMapper();

        int timeoutMs = config.getTimeoutSeconds() * 1000;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);

        this.restClient = RestClient.builder()
            .baseUrl(BASE_URL)
            .requestFactory(factory)
            .defaultHeader("x-api-key", config.getApiKey())
            .defaultHeader("anthropic-version", ANTHROPIC_VERSION)
            .defaultHeader("Content-Type", "application/json")
            .build();
    }

    @Override
    public ResumeParseResult parse(String rawText, List<String> skillCatalog) {
        String catalogStr = String.join(", ", skillCatalog);
        String systemPrompt = buildSystemPrompt(catalogStr);

        Map<String, Object> requestBody = Map.of(
            "model", config.getModel(),
            "max_tokens", 4096,
            "system", systemPrompt,
            "messages", List.of(
                Map.of("role", "user", "content", rawText)
            )
        );

        try {
            String requestJson = objectMapper.writeValueAsString(requestBody);
            log.debug("Calling Claude model={}", config.getModel());

            String responseJson = restClient.post()
                .uri("/messages")
                .body(requestJson)
                .retrieve()
                .onStatus(status -> status.value() == 401,
                    (req, resp) -> { throw new AiAuthException(); })
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                    (req, resp) -> { throw new RuntimeException("Claude API error: " + resp.getStatusCode()); })
                .body(String.class);

            // Navigate: content[0].text
            Map<?, ?> response = objectMapper.readValue(responseJson, Map.class);
            List<?> content = (List<?>) response.get("content");
            String text = (String) ((Map<?, ?>) content.get(0)).get("text");

            return objectMapper.readValue(text, ResumeParseResult.class);

        } catch (AiAuthException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Claude API call failed: " + e.getMessage(), e);
        }
    }

    private String buildSystemPrompt(String catalogStr) {
        return ResumeParsePromptBuilder.build(catalogStr, outputLanguage);
    }
}
