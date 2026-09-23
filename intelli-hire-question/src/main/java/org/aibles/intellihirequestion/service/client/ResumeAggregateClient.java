package org.aibles.intellihirequestion.service.client;

import org.aibles.intellihirequestion.config.ResumeClientProperties;
import org.aibles.intellihirequestion.dto.resume.ResumeAggregateData;
import org.aibles.intellihirequestion.dto.resume.ResumeApiResponse;
import org.aibles.intellihirequestion.exception.NotFoundException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Question Service should call a single aggregate endpoint from Resume Service.
 * Resume Service is responsible for assembling resume, contact, experiences,
 * educations, skills, and skill profile into one question-context payload.
 */
public class ResumeAggregateClient {

    private final RestClient restClient;
    private final ResumeClientProperties properties;

    public ResumeAggregateClient(ResumeClientProperties properties) {
        this.properties = properties;
        int timeoutMs = properties.getTimeoutSeconds() * 1000;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);

        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(factory)
                .build();
    }

    public ResumeAggregateData getAggregate(String userId, String resumeId) {
        ResumeApiResponse<ResumeAggregateData> response = restClient.get()
                .uri(properties.getQuestionContextPath(), resumeId)
                .header("X-User-Id", userId)
                .retrieve()
                .onStatus(status -> status.value() == 404, (req, res) -> {
                    throw new NotFoundException("RSM_001", "Resume question context not found");
                })
                .body(new ParameterizedTypeReference<ResumeApiResponse<ResumeAggregateData>>() {});

        if (response == null || !response.isSuccess() || response.getData() == null) {
            throw new NotFoundException("RSM_001", "Resume question context not found");
        }

        return response.getData();
    }
}
