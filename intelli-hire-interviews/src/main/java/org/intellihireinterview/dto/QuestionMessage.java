package org.aibles.intellihireinterview.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuestionMessage {
    private String type;          // "QUESTION" | "SESSION_END" | "ERROR"
    private String questionId;
    private String questionText;
    private Integer questionOrder;
    private Integer totalQuestions;
}
