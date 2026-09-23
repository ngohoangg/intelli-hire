package org.aibles.intellihirereport.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserStatsResponse {
    private Long userId;
    private Integer totalSessions;
    private Double averageScore;
    private Double highestScore;
    private Double lowestScore;
    private List<SkillSummary> topSkills;
    private List<SkillSummary> weakSkills;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class SkillSummary {
        private String skillName;
        private Double averageScore;
        private Integer totalAttempts;
    }
}
