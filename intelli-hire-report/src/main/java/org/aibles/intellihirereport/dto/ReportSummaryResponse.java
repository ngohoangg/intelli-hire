package org.aibles.intellihirereport.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ReportSummaryResponse {
    private String id;
    private String sessionId;
    private Long userId;
    private String topic;
    private Double totalScore;
    private Double maxScore;
    private Integer totalQuestions;
    private String status;
    private LocalDateTime createdAt;
}
