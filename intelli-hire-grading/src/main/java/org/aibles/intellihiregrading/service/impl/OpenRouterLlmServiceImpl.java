package org.aibles.intellihiregrading.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihiregrading.config.OpenRouterConfig;
import org.aibles.intellihiregrading.dto.openrouter.ChatRequest;
import org.aibles.intellihiregrading.dto.openrouter.ChatResponse;
import org.aibles.intellihiregrading.dto.openrouter.LlmGradingResult;
import org.aibles.intellihiregrading.service.LlmService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class OpenRouterLlmServiceImpl implements LlmService {

    private static final Pattern MARKDOWN_FENCE_PATTERN =
            Pattern.compile("(?s)^\\s*```(?:json)?\\s*(.*?)\\s*```\\s*$");

    private final WebClient openRouterWebClient;
    private final OpenRouterConfig openRouterConfig;
    private final ObjectMapper objectMapper;

    public OpenRouterLlmServiceImpl(
            @Qualifier("openRouterWebClient") WebClient openRouterWebClient,
            OpenRouterConfig openRouterConfig,
            ObjectMapper objectMapper) {
        this.openRouterWebClient = openRouterWebClient;
        this.openRouterConfig = openRouterConfig;
        this.objectMapper = objectMapper;
    }

    @Override
    public LlmGradingResult grade(String prompt) {
        ChatRequest request = ChatRequest.builder()
                .model(openRouterConfig.getModel())
                .temperature(openRouterConfig.getTemperature())
                .maxTokens(openRouterConfig.getMaxTokens())
                .messages(List.of(
                        ChatRequest.Message.builder()
                                .role("user")
                                .content(prompt)
                                .build()
                ))
                .build();

        try {
            ChatResponse response = callWithRetry(request);
            return parseResponse(response);
        } catch (Exception e) {
            log.error("LLM grading failed: {}", e.getMessage(), e);
            return LlmGradingResult.builder()
                    .score(0.0)
                    .feedback("Không thể chấm điểm: " + e.getMessage())
                    .model(openRouterConfig.getModel())
                    .build();
        }
    }

    private ChatResponse callWithRetry(ChatRequest request) {
        return openRouterWebClient.post()
                .uri("/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ChatResponse.class)
                .timeout(Duration.ofMillis(openRouterConfig.getTimeout()))
                .retryWhen(reactor.util.retry.Retry.fixedDelay(1, Duration.ofSeconds(2))
                        .filter(this::isRetryable)
                        .doBeforeRetry(signal ->
                                log.warn("LLM call failed, retrying: {}", signal.failure().getMessage())))
                .block();
    }

    private boolean isRetryable(Throwable e) {
        if (e instanceof WebClientResponseException wce) {
            int status = wce.getStatusCode().value();
            return status == 500 || status == 502 || status == 503 || status == 504;
        }
        return e instanceof java.util.concurrent.TimeoutException
                || e.getCause() instanceof java.util.concurrent.TimeoutException;
    }

    private LlmGradingResult parseResponse(ChatResponse response) {
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            return fallbackResult("Empty LLM response");
        }

        String content = response.getChoices().get(0).getMessage().getContent();
        String model = response.getModel() != null ? response.getModel() : openRouterConfig.getModel();

        content = stripMarkdownFences(content).trim();

        try {
            JsonNode node = objectMapper.readTree(content);
            double score = node.has("score") ? node.get("score").asDouble() : 0.0;
            String feedback = node.has("feedback") ? node.get("feedback").asText() : "Không có phản hồi được cung cấp";
            return LlmGradingResult.builder()
                    .score(score)
                    .feedback(feedback)
                    .model(model)
                    .build();
        } catch (Exception e) {
            log.warn("Failed to parse LLM response as JSON: {}", content, e);
            return LlmGradingResult.builder()
                    .score(0.0)
                    .feedback("Lỗi phân tích phản hồi. Phản hồi gốc: " + content)
                    .model(model)
                    .build();
        }
    }

    private String stripMarkdownFences(String content) {
        if (content == null) return "";
        Matcher matcher = MARKDOWN_FENCE_PATTERN.matcher(content);
        if (matcher.matches()) {
            return matcher.group(1).trim();
        }
        return content.trim();
    }

    private LlmGradingResult fallbackResult(String reason) {
        return LlmGradingResult.builder()
                .score(0.0)
                .feedback("Không thể chấm điểm: " + reason)
                .model(openRouterConfig.getModel())
                .build();
    }
}
