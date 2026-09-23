package org.aibles.intellihiregrading.dto.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
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
