package org.aibles.intellihirequestion.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class GenerateQuestionsFromResumeRequest {

    @NotBlank(message = "Resume ID must not be blank")
    private String resumeId;

    @Min(value = 1, message = "Count must be between 1 and 20")
    @Max(value = 20, message = "Count must be between 1 and 20")
    private Integer count = 8;

    private String questionType;
    private String difficultyName;
    private String language = "en";
    private List<String> focusAreas;
}
