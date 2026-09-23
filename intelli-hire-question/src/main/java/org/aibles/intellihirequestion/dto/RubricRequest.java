package org.aibles.intellihirequestion.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RubricRequest {

    @NotBlank(message = "Criteria must not be blank")
    @Size(max = 5000, message = "Criteria must not exceed 5000 characters")
    private String criteria;

    private List<String> keywords;

    @Size(max = 10000, message = "Example answer must not exceed 10000 characters")
    private String exampleAnswer;

    private List<String> commonMistakes;

    private List<String> hints;

    @NotNull(message = "Max score must not be null")
    @Min(value = 1, message = "Max score must be at least 1")
    @Max(value = 100, message = "Max score must not exceed 100")
    private Integer maxScore = 10;
}
