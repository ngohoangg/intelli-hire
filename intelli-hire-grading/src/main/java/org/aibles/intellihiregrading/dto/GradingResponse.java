package org.aibles.intellihiregrading.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class GradingResponse {
    private String id;
    private String sessionId;
    private String userId;
    private String topic;
    private String status;
    private Integer totalQuestions;
    private Double totalScore;
    private Double maxScore;
    private List<GradingResultResponse> results;
    private LocalDateTime createdAt;
}
