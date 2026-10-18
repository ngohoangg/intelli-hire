package org.aibles.intellihireinterview.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SessionResponse {
    private Long id;
    private String topic;
    private String difficulty;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private LocalDateTime createdAt;
}
