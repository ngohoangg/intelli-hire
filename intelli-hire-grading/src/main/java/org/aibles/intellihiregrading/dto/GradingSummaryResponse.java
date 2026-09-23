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
public class GradingSummaryResponse {
    private String id;
    private String sessionId;
    private String userId;
    private String topic;
    private String status;
    private Integer totalQuestions;
    private LocalDateTime createdAt;
}
