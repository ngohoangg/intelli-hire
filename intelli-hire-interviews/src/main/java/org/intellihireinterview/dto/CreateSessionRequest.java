package org.aibles.intellihireinterview.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateSessionRequest {
    @NotBlank(message = "topic is required")
    private String topic;

    private String difficulty;

    @Min(1) @Max(20)
    private Integer questionCount = 5;
}
