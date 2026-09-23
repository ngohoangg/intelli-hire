package org.aibles.intellihireinterview.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aibles.intellihireinterview.client.dto.QuestionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuestionServiceClient {

    private final WebClient questionWebClient;
    private final ObjectMapper objectMapper;

    @SuppressWarnings("unchecked")
    public List<QuestionDto> getRandomQuestions(String topicId, String difficultyId, int count) {
        try {
            Map<String, Object> response = questionWebClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder
                        .path("/api/v1/questions/random")
                        .queryParam("count", count);
                    if (topicId != null && !topicId.isBlank()) {
                        builder = builder.queryParam("topic_id", topicId);
                    }
                    if (difficultyId != null && !difficultyId.isBlank()) {
                        builder = builder.queryParam("difficulty_id", difficultyId);
                    }
                    return builder.build();
                })
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(5))
                .block();

            if (response == null || !Boolean.TRUE.equals(response.get("success"))) {
                log.warn("Question Service returned unsuccessful response for topicId={}", topicId);
                return Collections.emptyList();
            }

            List<Map<String, Object>> raw = (List<Map<String, Object>>) response.get("data");
            if (raw == null) {
                log.warn("Question Service returned null data for topicId={}", topicId);
                return Collections.emptyList();
            }

            return raw.stream()
                .map(m -> objectMapper.convertValue(m, QuestionDto.class))
                .toList();

        } catch (WebClientResponseException e) {
            log.warn("Question Service returned HTTP {} for topicId={}: {}",
                     e.getStatusCode(), topicId, e.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("Failed to fetch questions from Question Service for topicId={}: {}",
                     topicId, e.getMessage());
            return Collections.emptyList();
        }
    }
}
