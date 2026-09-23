package org.aibles.intellihirereport.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ReportResponse {
    private String id;
    private String sessionId;
    private Long userId;
    private String topic;
    private Double totalScore;
    private Double maxScore;
    private String summary;
    private Object roadmapJson;
    private Integer totalQuestions;
    private String status;
    private List<SkillScoreResponse> skillScores;
    private List<QuestionScoreResponse> questionScores;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;
}
