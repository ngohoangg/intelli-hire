package org.aibles.intellihireinterview.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SubmitAnswerRequest {
    @NotBlank
    private String questionId;

    @NotBlank(message = "Câu trả lời không được để trống")
    @Size(max = 10000)
    private String answerText;
}
