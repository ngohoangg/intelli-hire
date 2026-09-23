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
public class UserProgressResponse {
    private String id;
    private Long userId;
    private String skillName;
    private Double latestScore;
    private Double averageScore;
    private Integer totalAttempts;
    private java.util.List<Double> scoreTrend;
    private LocalDateTime lastUpdatedAt;
}
