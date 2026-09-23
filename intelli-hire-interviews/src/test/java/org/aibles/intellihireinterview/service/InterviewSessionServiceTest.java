package org.aibles.intellihireinterview.service;

import org.aibles.intellihireinterview.client.QuestionServiceClient;
import org.aibles.intellihireinterview.client.dto.QuestionDto;
import org.aibles.intellihireinterview.dto.event.SessionCompletedEvent;
import org.aibles.intellihireinterview.entity.InterviewSession;
import org.aibles.intellihireinterview.entity.enums.SessionStatus;
import org.aibles.intellihireinterview.dto.CreateSessionRequest;
import org.aibles.intellihireinterview.exception.SessionNotActiveException;
import org.aibles.intellihireinterview.exception.SessionNotFoundException;
import org.aibles.intellihireinterview.kafka.SessionEventProducer;
import org.aibles.intellihireinterview.mapper.SessionMapper;
import org.aibles.intellihireinterview.repository.InterviewSessionRepository;
import org.aibles.intellihireinterview.repository.QuestionLogRepository;
import org.aibles.intellihireinterview.repository.UserAnswerRawRepository;
import org.aibles.intellihireinterview.service.impl.InterviewSessionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterviewSessionServiceTest {

    @Mock InterviewSessionRepository sessionRepo;
    @Mock QuestionLogRepository questionLogRepo;
    @Mock UserAnswerRawRepository answerRepo;
    @Mock QuestionServiceClient questionClient;
    @Mock SessionMapper sessionMapper;
    @Mock SessionEventProducer sessionEventProducer;
    @InjectMocks InterviewSessionServiceImpl service;

    @Test
    void createSession_savesSessionWithActiveStatus() {
        var request = new CreateSessionRequest();
        request.setTopic("Java");
        request.setDifficulty("mid");
        request.setQuestionCount(2);

        var questionDto = new QuestionDto();
        questionDto.setId("q-001");
        questionDto.setQuestionText("What is JVM?");

        when(questionClient.getRandomQuestions(any(), any(), anyInt()))
            .thenReturn(List.of(questionDto));
        when(sessionRepo.save(any())).thenAnswer(inv -> {
            var s = (InterviewSession) inv.getArgument(0);
            s.setId(1L);
            return s;
        });

        var result = service.createSession("1", request);

        assertThat(result.getTopic()).isEqualTo("Java");
        assertThat(result.getStatus()).isEqualTo(SessionStatus.ACTIVE);
        verify(questionLogRepo, times(1)).saveAll(anyList());
    }

    @Test
    void createSession_withNoQuestionsReturned_throwsNoQuestionsAvailableException() {
        var request = new CreateSessionRequest();
        request.setTopic("Java");
        request.setDifficulty("mid");
        request.setQuestionCount(5);

        when(questionClient.getRandomQuestions(any(), any(), anyInt()))
            .thenReturn(List.of());
        when(sessionRepo.save(any())).thenAnswer(inv -> {
            var s = (InterviewSession) inv.getArgument(0);
            s.setId(1L);
            return s;
        });

        assertThatThrownBy(() -> service.createSession("1", request))
            .isInstanceOf(org.aibles.intellihireinterview.exception.NoQuestionsAvailableException.class);
    }

    @Test
    void endSession_setsStatusCompleted() {
        var session = InterviewSession.builder()
            .id(1L).userId("1").status(SessionStatus.ACTIVE).topic("Java").build();
        when(sessionRepo.findByIdAndUserId(1L, "1")).thenReturn(Optional.of(session));
        when(sessionRepo.save(any())).thenReturn(session);

        service.endSession(1L, "1");

        assertThat(session.getStatus()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(session.getEndedAt()).isNotNull();
    }

    @Test
    void endSession_throwsIfNotFound() {
        when(sessionRepo.findByIdAndUserId(anyLong(), anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.endSession(99L, "1"))
            .isInstanceOf(SessionNotFoundException.class);
    }

    @Test
    void listSessions_delegatesToRepository() {
        when(sessionRepo.findByUserIdOrderByCreatedAtDesc("42")).thenReturn(List.of());

        var result = service.listSessions("42");

        assertThat(result).isEmpty();
        verify(sessionRepo).findByUserIdOrderByCreatedAtDesc("42");
    }

    @Test
    void endSession_throwsIfAlreadyCompleted() {
        var session = InterviewSession.builder()
            .id(1L).userId("1").status(SessionStatus.COMPLETED).topic("Java").build();
        when(sessionRepo.findByIdAndUserId(1L, "1")).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> service.endSession(1L, "1"))
            .isInstanceOf(SessionNotActiveException.class);
    }

    @Test
    void endSession_publishesSessionCompletedEvent() {
        var session = InterviewSession.builder()
            .id(1L).userId("1").status(SessionStatus.ACTIVE).topic("Java")
            .difficulty("mid").build();
        when(sessionRepo.findByIdAndUserId(1L, "1")).thenReturn(Optional.of(session));
        when(sessionRepo.save(any())).thenReturn(session);
        when(questionLogRepo.findBySessionIdOrderByQuestionOrder(1L)).thenReturn(List.of());
        when(answerRepo.findBySessionIdOrderBySubmittedAt(1L)).thenReturn(List.of());

        service.endSession(1L, "1");

        verify(sessionEventProducer).publishSessionCompleted(argThat(event ->
            event.getSessionId().equals(1L) &&
            "COMPLETED".equals(event.getFinalStatus()) &&
            event.getUserId().equals("1")
        ));
    }
}
