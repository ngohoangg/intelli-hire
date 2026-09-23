package org.aibles.intellihireinterview.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class AnswerSubmittedEvent {
    private Long sessionId;
    private String questionId;
    private String userId;
    private String answerText;
    private String questionText;
    private LocalDateTime submittedAt;
}
