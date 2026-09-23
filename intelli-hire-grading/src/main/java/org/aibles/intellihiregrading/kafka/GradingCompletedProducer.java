package org.aibles.intellihiregrading.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihiregrading.dto.kafka.GradingCompletedEvent;
import org.aibles.intellihiregrading.entity.GradingResult;
import org.aibles.intellihiregrading.entity.GradingSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class GradingCompletedProducer {

    private final KafkaTemplate<String, GradingCompletedEvent> kafkaTemplate;

    @Value("${kafka.topics.grading-completed}")
    private String topic;

    public void publish(GradingSession session, List<GradingResult> results) {
        GradingCompletedEvent event = GradingCompletedEvent.builder()
                .sessionId(session.getSessionId())
                .userId(session.getUserId())
                .topic(session.getTopic())
                .questionScores(results.stream()
                        .map(r -> GradingCompletedEvent.QuestionScoreEvent.builder()
                                .questionId(r.getQuestionId())
                                .questionText(r.getQuestionText())
                                .answerText(r.getAnswerText())
                                .score(r.getScore())
                                .maxScore(r.getMaxScore())
                                .feedback(r.getFeedback())
                                .skillName(r.getSkillName())
                                .build())
                        .collect(Collectors.toList()))
                .build();

        kafkaTemplate.send(topic, session.getSessionId(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish grading_completed event: sessionId={}, error={}",
                                session.getSessionId(), ex.getMessage(), ex);
                    } else {
                        log.info("Published grading_completed event: sessionId={}, offset={}",
                                session.getSessionId(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
