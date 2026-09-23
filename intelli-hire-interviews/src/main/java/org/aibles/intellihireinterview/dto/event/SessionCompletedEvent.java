package org.aibles.intellihireinterview.dto.event;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SessionCompletedEvent {
    private Long sessionId;
    private String userId;
    private String topic;
    private String difficulty;
    private String finalStatus;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Integer totalQuestions;
    private Integer answeredQuestions;
}
