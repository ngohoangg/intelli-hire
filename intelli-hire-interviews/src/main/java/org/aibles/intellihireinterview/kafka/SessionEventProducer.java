package org.aibles.intellihireinterview.kafka;

import org.aibles.intellihireinterview.dto.event.SessionCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.session-completed}")
    private String sessionCompletedTopic;

    public void publishSessionCompleted(SessionCompletedEvent event) {
        String key = String.valueOf(event.getSessionId());
        kafkaTemplate.send(sessionCompletedTopic, key, event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish session-completed event for session {}: {}",
                              event.getSessionId(), ex.getMessage());
                } else {
                    log.debug("Published session-completed event for session {}, status {}",
                              event.getSessionId(), event.getFinalStatus());
                }
            });
    }
}
