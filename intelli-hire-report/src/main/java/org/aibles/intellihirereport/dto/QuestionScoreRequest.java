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

    @NotBlank(message = "Mã câu hỏi là bắt buộc")
    private String questionId;

    private String questionText;

    private String answerText;

    @NotNull(message = "Điểm số là bắt buộc")
    @Min(value = 0, message = "Điểm số phải >= 0")
    @Max(value = 100, message = "Điểm số phải <= 100")
    private Double score;

    @NotNull(message = "Điểm tối đa là bắt buộc")
    @Min(value = 1, message = "Điểm tối đa phải >= 1")
    @Max(value = 100, message = "Điểm tối đa phải <= 100")
    private Double maxScore;

    private String feedback;

    private String skillName;

    @JsonIgnore
    @AssertTrue(message = "Điểm số phải nhỏ hơn hoặc bằng điểm tối đa")
    public boolean isScoreValid() {
        if (score == null || maxScore == null) return true;
        return score <= maxScore;
    }
}
