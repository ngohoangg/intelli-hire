package org.aibles.intellihirequestion.service.ai.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihirequestion.config.AiProperties;
import org.aibles.intellihirequestion.dto.GenerateQuestionsFromResumeRequest;
import org.aibles.intellihirequestion.dto.ai.AiGeneratedQuestionResult;
import org.aibles.intellihirequestion.dto.resume.ResumeAggregateData;
import org.aibles.intellihirequestion.entity.DifficultyLevel;
import org.aibles.intellihirequestion.entity.Topic;
import org.aibles.intellihirequestion.service.ai.QuestionGenerationAiClient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class OpenRouterQuestionGenerationClient implements QuestionGenerationAiClient {

    private static final String BASE_URL = "https://openrouter.ai/api/v1";
    private static final String DEFAULT_OUTPUT_LANGUAGE = "Vietnamese";
    private static final Map<String, String> LANGUAGE_CODE_ALIASES = Map.of(
            "vi", "Vietnamese",
            "vi-vn", "Vietnamese",
            "en", "English",
            "en-us", "English"
    );

    private final AiProperties.OpenRouterConfig config;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final String outputLanguage;

    /**
     * @param config       OpenRouter API configuration
     * @param objectMapper Spring-managed ObjectMapper (already configured with JavaTimeModule
     *                     by Spring Boot's JacksonAutoConfiguration)
     */
    public OpenRouterQuestionGenerationClient(AiProperties.OpenRouterConfig config, ObjectMapper objectMapper) {
        this(config, objectMapper, DEFAULT_OUTPUT_LANGUAGE);
    }

    /**
     * @param config         OpenRouter API configuration
     * @param objectMapper   Spring-managed ObjectMapper (already configured with JavaTimeModule
     *                       by Spring Boot's JacksonAutoConfiguration)
     * @param outputLanguage configured default output language (e.g. "ai.output-language"); falls
     *                       back to {@link #DEFAULT_OUTPUT_LANGUAGE} when null/blank
     */
    public OpenRouterQuestionGenerationClient(AiProperties.OpenRouterConfig config, ObjectMapper objectMapper, String outputLanguage) {
        this.config = config;
        this.objectMapper = objectMapper;
        this.outputLanguage = (outputLanguage != null && !outputLanguage.isBlank())
                ? outputLanguage
                : DEFAULT_OUTPUT_LANGUAGE;

        int timeoutMs = config.getTimeoutSeconds() * 1000;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);

        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .requestFactory(factory)
                .defaultHeader("Authorization", "Bearer " + config.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("HTTP-Referer", config.getSiteUrl())
                .defaultHeader("X-Title", config.getSiteName())
                .build();
    }

    @Override
    public AiGeneratedQuestionResult generateQuestions(ResumeAggregateData resumeData, GenerateQuestionsFromResumeRequest request, List<Topic> topics, List<DifficultyLevel> difficulties) {

        try {
            String topicCatalog = topics.stream()
                    .map(topic -> topic.getName() + " - " + safe(topic.getDescription()))
                    .collect(Collectors.joining("\n"));
            String difficultyCatalog = difficulties.stream()
                    .map(difficulty -> difficulty.getName() + " - " + safe(difficulty.getDescription()))
                    .collect(Collectors.joining("\n"));

            String systemPrompt = buildSystemPrompt(request, topicCatalog, difficultyCatalog);
            String userPrompt = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(resumeData);

            Map<String, Object> requestBody = Map.of(
                    "model", config.getModel(),
                    "response_format", Map.of("type", "json_object"),
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)
                    )
            );

            String responseJson = restClient.post()
                    .uri("/chat/completions")
                    .body(objectMapper.writeValueAsString(requestBody))
                    .retrieve()
                    .body(String.class);

            Map<?, ?> response = objectMapper.readValue(responseJson, Map.class);
            List<?> choices = (List<?>) response.get("choices");
            Map<?, ?> message = (Map<?, ?>) ((Map<?, ?>) choices.get(0)).get("message");
            String content = (String) message.get("content");

            return objectMapper.readValue(content, AiGeneratedQuestionResult.class);
        } catch (Exception e) {
            log.error("OpenRouter question generation failed: {}", e.getMessage(), e);
            throw new RuntimeException("OpenRouter question generation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Resolves the effective output language: the request's own language when non-blank,
     * otherwise the service-configured default. Known short codes ("vi", "vi-VN", "en",
     * "en-US") are normalized to their full English name; any other non-blank value is
     * passed through as given.
     */
    String resolveOutputLanguage(String requestLanguage) {
        String raw = (requestLanguage != null && !requestLanguage.isBlank())
                ? requestLanguage.trim()
                : outputLanguage;
        String alias = LANGUAGE_CODE_ALIASES.get(raw.toLowerCase());
        return alias != null ? alias : raw;
    }

    String buildSystemPrompt(
            GenerateQuestionsFromResumeRequest request,
            String topicCatalog,
            String difficultyCatalog) {
        String resolvedLanguage = resolveOutputLanguage(request.getLanguage());
        String topicInstruction = topicCatalog.isBlank()
                ? """
                2. There is no internal topic catalog configured. Infer a concise, resume-based topic name from the candidate's skills, experience, or focus areas.
                """
                : """
                2. Use ONLY topic names from this catalog:
                %s
                """.formatted(topicCatalog);
        String difficultyInstruction = difficultyCatalog.isBlank()
                ? """
                3. There is no internal difficulty catalog configured. Use a sensible difficulty label based on the candidate's seniority and question complexity.
                """
                : """
                3. Use ONLY difficulty names from this catalog:
                %s
                """.formatted(difficultyCatalog);

        return """
                You are an interview question generator for a hiring platform.

                Your task:
                1. Read the candidate resume data.
                2. Generate exactly %d interview questions tailored to the candidate.
                3. Balance questions around the candidate's strongest skills, recent work, and seniority.
                4. If focusAreas is present, prioritize those areas.
                5. If questionType is provided, prefer that type. Allowed types: TECHNICAL, BEHAVIORAL, CODING.
                6. If difficultyName is provided, prefer that difficulty.
                7. Write all human-readable text values (candidateSummary, questionText, rationale) in %s.

                Constraints:
                1. Return ONLY valid JSON. No markdown. No explanation outside JSON.
                2. Keep JSON keys, enum values (questionType), and catalog-matched names (topicName, difficultyName) exactly as specified in English/catalog form — do NOT translate them.
                4. Keep each question concise and interview-ready.
                5. rationale must be a short sentence explaining why the question fits the resume.

                Topic guidance:
                %s

                Difficulty guidance:
                %s

                Required JSON:
                {
                  "candidateSummary": "...",
                  "questions": [
                    {
                      "questionText": "...",
                      "questionType": "TECHNICAL",
                      "topicName": "...",
                      "difficultyName": "...",
                      "rationale": "..."
                    }
                  ]
                }
                """.formatted(
                request.getCount(),
                resolvedLanguage,
                topicInstruction,
                difficultyInstruction);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
