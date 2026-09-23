package org.aibles.intellihirereport.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class CreateReportRequest {

    @NotBlank(message = "Session ID is required")
    private String sessionId;

    @NotNull(message = "User ID is required")
    private Long userId;

    private String topic;

    private String summary;

    private Object roadmapJson;

    @NotEmpty(message = "Question scores must not be empty")
    @Valid
    private List<QuestionScoreRequest> questionScores;
}
