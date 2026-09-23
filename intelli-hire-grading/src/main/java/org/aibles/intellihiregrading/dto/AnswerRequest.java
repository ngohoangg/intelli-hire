package org.aibles.intellihiregrading.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AnswerRequest {

    @NotBlank(message = "Question ID is required")
    private String questionId;

    private String questionText;

    private String answerText;

    private String skillName;

    @Min(value = 1, message = "Max score must be >= 1")
    @Max(value = 100, message = "Max score must be <= 100")
    @Builder.Default
    private Double maxScore = 10.0;
}
