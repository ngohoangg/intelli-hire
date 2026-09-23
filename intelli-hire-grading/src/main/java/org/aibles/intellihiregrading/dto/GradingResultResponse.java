package org.aibles.intellihiregrading.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class GradingResultResponse {
    private String id;
    private String questionId;
    private String questionText;
    private String answerText;
    private String skillName;
    private Double score;
    private Double maxScore;
    private String feedback;
    private String modelUsed;
    private Long latencyMs;
    private LocalDateTime createdAt;
}
