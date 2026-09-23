package org.aibles.intellihirequestion.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DifficultyLevelRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 50, message = "Name must not exceed 50 characters")
    private String name;

    @Min(value = 1, message = "Weight must be greater than or equal to 1")
    @Max(value = 100, message = "Weight must be less than or equal to 100")
    @NotNull(message = "Weight is required")
    private Integer weight;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
}
