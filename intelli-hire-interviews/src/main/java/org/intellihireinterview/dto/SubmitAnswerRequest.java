package org.aibles.intellihireinterview.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SubmitAnswerRequest {
    @NotBlank
    private String questionId;

    @NotBlank(message = "answer cannot be blank")
    @Size(max = 10000)
    private String answerText;
}
