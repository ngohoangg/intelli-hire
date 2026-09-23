package org.aibles.intellihiregrading.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class GradeRequest {

    @NotBlank(message = "Session ID is required")
    private String sessionId;

    @NotBlank(message = "User ID is required")
    private String userId;

    private String topic;

    @NotEmpty(message = "Answers must not be empty")
    @jakarta.validation.constraints.Size(max = 50, message = "Maximum 50 answers per request")
    @Valid
    private List<AnswerRequest> answers;
}
