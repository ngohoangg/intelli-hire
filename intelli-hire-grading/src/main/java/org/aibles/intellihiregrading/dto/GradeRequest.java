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

    @NotBlank(message = "Mã phiên là bắt buộc")
    private String sessionId;

    @NotBlank(message = "Mã người dùng là bắt buộc")
    private String userId;

    private String topic;

    @NotEmpty(message = "Danh sách câu trả lời không được để trống")
    @jakarta.validation.constraints.Size(max = 50, message = "Tối đa 50 câu trả lời mỗi yêu cầu")
    @Valid
    private List<AnswerRequest> answers;
}
