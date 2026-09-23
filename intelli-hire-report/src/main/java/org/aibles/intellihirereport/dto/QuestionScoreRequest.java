package org.aibles.intellihirereport.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class QuestionScoreRequest {

    @NotBlank(message = "Question ID is required")
    private String questionId;

    private String questionText;

    private String answerText;

    @NotNull(message = "Score is required")
    @Min(value = 0, message = "Score must be >= 0")
    @Max(value = 100, message = "Score must be <= 100")
    private Double score;

    @NotNull(message = "Max score is required")
    @Min(value = 1, message = "Max score must be >= 1")
    @Max(value = 100, message = "Max score must be <= 100")
    private Double maxScore;

    private String feedback;

    private String skillName;

    @JsonIgnore
    @AssertTrue(message = "Score must be less than or equal to max score")
    public boolean isScoreValid() {
        if (score == null || maxScore == null) return true;
        return score <= maxScore;
    }
}
