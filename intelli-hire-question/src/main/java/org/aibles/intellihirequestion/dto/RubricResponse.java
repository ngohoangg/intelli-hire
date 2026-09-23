package org.aibles.intellihirequestion.dto;

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
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RubricResponse {
    private String id;
    private String questionId;
    private String criteria;
    private List<String> keywords;
    private String exampleAnswer;
    private List<String> commonMistakes;
    private List<String> hints;
    private Integer maxScore;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;
}
