package org.aibles.intellihiregrading.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihiregrading.dto.interview.InterviewBaseResponse;
import org.aibles.intellihiregrading.dto.interview.InterviewSessionDetail;
import org.aibles.intellihiregrading.service.InterviewClientService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
public class InterviewClientServiceImpl implements InterviewClientService {

    private final WebClient webClient;

    public InterviewClientServiceImpl(
            @Value("${interview-service.base-url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public Optional<InterviewSessionDetail> getSessionDetail(Long sessionId, String userId) {
        try {
            log.info("Fetching session detail from Interview Service: sessionId={}, userId={}", sessionId, userId);

            InterviewBaseResponse response = webClient.get()
                    .uri("/api/v1/interviews/sessions/{sessionId}", sessionId)
                    .header("X-User-Id", userId)
                    .retrieve()
                    .bodyToMono(InterviewBaseResponse.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (response != null && response.isSuccess() && response.getData() != null) {
                log.info("Fetched session detail: sessionId={}, questions={}, answers={}",
                        sessionId,
                        response.getData().getQuestions() != null ? response.getData().getQuestions().size() : 0,
                        response.getData().getAnswers() != null ? response.getData().getAnswers().size() : 0);
                return Optional.of(response.getData());
            }

            log.warn("Interview Service returned unsuccessful response for sessionId={}", sessionId);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to fetch session detail from Interview Service: sessionId={}, error={}",
                    sessionId, e.getMessage());
            return Optional.empty();
        }
    }
}
