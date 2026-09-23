package org.aibles.intellihireinterview.scheduler;

import org.aibles.intellihireinterview.entity.InterviewSession;
import org.aibles.intellihireinterview.entity.enums.SessionStatus;
import org.aibles.intellihireinterview.kafka.SessionEventProducer;
import org.aibles.intellihireinterview.repository.InterviewSessionRepository;
import org.aibles.intellihireinterview.repository.QuestionLogRepository;
import org.aibles.intellihireinterview.repository.UserAnswerRawRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionExpirySchedulerTest {

    @Mock InterviewSessionRepository sessionRepo;
    @Mock QuestionLogRepository questionLogRepo;
    @Mock UserAnswerRawRepository answerRepo;
    @Mock SessionEventProducer sessionEventProducer;
    @InjectMocks SessionExpiryScheduler scheduler;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(scheduler, "expiryMinutes", 60L);
    }

    @Test
    void expireStaleActiveSessions_setsStatusExpiredAndPublishesEvent() {
        var session = InterviewSession.builder()
            .id(5L).userId("99").topic("Python")
            .status(SessionStatus.ACTIVE)
            .startedAt(LocalDateTime.now().minusHours(2))
            .build();

        when(sessionRepo.findByStatusAndStartedAtBefore(eq(SessionStatus.ACTIVE), any()))
            .thenReturn(List.of(session));
        when(sessionRepo.saveAll(anyList())).thenReturn(List.of(session));
        when(questionLogRepo.findBySessionIdOrderByQuestionOrder(5L)).thenReturn(List.of());
        when(answerRepo.findBySessionIdOrderBySubmittedAt(5L)).thenReturn(List.of());

        scheduler.expireStaleActiveSessions();

        assertThat(session.getStatus()).isEqualTo(SessionStatus.EXPIRED);
        assertThat(session.getEndedAt()).isNotNull();
        verify(sessionEventProducer).publishSessionCompleted(argThat(e ->
            "EXPIRED".equals(e.getFinalStatus()) && e.getSessionId().equals(5L)
        ));
    }

    @Test
    void expireStaleActiveSessions_noOp_whenNoStaleSessions() {
        when(sessionRepo.findByStatusAndStartedAtBefore(any(), any())).thenReturn(List.of());

        scheduler.expireStaleActiveSessions();

        verify(sessionRepo, never()).saveAll(any());
        verify(sessionEventProducer, never()).publishSessionCompleted(any());
    }
}
