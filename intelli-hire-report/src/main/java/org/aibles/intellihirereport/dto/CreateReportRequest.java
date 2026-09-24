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

    @NotBlank(message = "Mã phiên là bắt buộc")
    private String sessionId;

    @NotNull(message = "Mã người dùng là bắt buộc")
    private Long userId;

    private String topic;

    private String summary;

    private Object roadmapJson;

    @NotEmpty(message = "Danh sách điểm câu hỏi không được để trống")
    @Valid
    private List<QuestionScoreRequest> questionScores;
}
