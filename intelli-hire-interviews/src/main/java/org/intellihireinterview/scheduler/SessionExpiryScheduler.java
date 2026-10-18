package org.aibles.intellihireinterview.scheduler;

import org.aibles.intellihireinterview.dto.event.SessionCompletedEvent;
import org.aibles.intellihireinterview.entity.enums.SessionStatus;
import org.aibles.intellihireinterview.kafka.SessionEventProducer;
import org.aibles.intellihireinterview.repository.InterviewSessionRepository;
import org.aibles.intellihireinterview.repository.QuestionLogRepository;
import org.aibles.intellihireinterview.repository.UserAnswerRawRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionExpiryScheduler {

    private final InterviewSessionRepository sessionRepo;
    private final QuestionLogRepository questionLogRepo;
    private final UserAnswerRawRepository answerRepo;
    private final SessionEventProducer sessionEventProducer;

    @Value("${session.expiry.minutes:60}")
    private long expiryMinutes;

    @Scheduled(fixedDelayString = "${session.expiry.check-interval-ms:300000}")
    @Transactional
    public void expireStaleActiveSessions() {
        var threshold = LocalDateTime.now().minusMinutes(expiryMinutes);
        var stale = sessionRepo.findByStatusAndStartedAtBefore(SessionStatus.ACTIVE, threshold);

        if (stale.isEmpty()) return;

        stale.forEach(session -> {
            session.setStatus(SessionStatus.EXPIRED);
            session.setEndedAt(LocalDateTime.now());
        });
        sessionRepo.saveAll(stale);

        stale.forEach(session -> {
            int totalQ = questionLogRepo.findBySessionIdOrderByQuestionOrder(session.getId()).size();
            int answered = answerRepo.findBySessionIdOrderBySubmittedAt(session.getId()).size();
            sessionEventProducer.publishSessionCompleted(
                SessionCompletedEvent.builder()
                    .sessionId(session.getId())
                    .userId(session.getUserId())
                    .topic(session.getTopic())
                    .difficulty(session.getDifficulty())
                    .finalStatus("EXPIRED")
                    .startedAt(session.getStartedAt())
                    .endedAt(session.getEndedAt())
                    .totalQuestions(totalQ)
                    .answeredQuestions(answered)
                    .build()
            );
        });

        log.info("Expired {} stale ACTIVE sessions (threshold: {} minutes ago)", stale.size(), expiryMinutes);
    }
}
