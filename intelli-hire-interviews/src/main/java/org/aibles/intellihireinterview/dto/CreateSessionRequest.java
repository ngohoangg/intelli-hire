package org.aibles.intellihireinterview.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateSessionRequest {
    @NotBlank(message = "Chủ đề là bắt buộc")
    private String topic;

    private String difficulty;

    @Min(1) @Max(20)
    private Integer questionCount = 5;
}
