package org.aibles.intellihireinterview.kafka;

import org.aibles.intellihireinterview.dto.AnswerSubmittedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnswerEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.answers-submitted}")
    private String answersSubmittedTopic;

    public void publishAnswerSubmitted(AnswerSubmittedEvent event) {
        String key = String.valueOf(event.getSessionId());
        kafkaTemplate.send(answersSubmittedTopic, key, event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish answer event for session {}: {}",
                              event.getSessionId(), ex.getMessage());
                } else {
                    log.debug("Published answer event for session {}, question {}",
                              event.getSessionId(), event.getQuestionId());
                }
            });
    }
}
