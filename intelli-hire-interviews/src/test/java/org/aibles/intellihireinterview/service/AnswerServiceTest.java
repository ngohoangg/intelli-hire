package org.aibles.intellihireinterview.service;

import org.aibles.intellihireinterview.entity.InterviewSession;
import org.aibles.intellihireinterview.entity.QuestionLog;
import org.aibles.intellihireinterview.entity.enums.SessionStatus;
import org.aibles.intellihireinterview.dto.SubmitAnswerRequest;
import org.aibles.intellihireinterview.exception.SessionNotActiveException;
import org.aibles.intellihireinterview.exception.SessionNotFoundException;
import org.aibles.intellihireinterview.kafka.AnswerEventProducer;
import org.aibles.intellihireinterview.repository.InterviewSessionRepository;
import org.aibles.intellihireinterview.repository.QuestionLogRepository;
import org.aibles.intellihireinterview.repository.UserAnswerRawRepository;
import org.aibles.intellihireinterview.service.impl.AnswerServiceImpl;
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
class AnswerServiceTest {

    @Mock InterviewSessionRepository sessionRepo;
    @Mock UserAnswerRawRepository answerRepo;
    @Mock QuestionLogRepository questionLogRepo;
    @Mock AnswerEventProducer eventProducer;
    @InjectMocks AnswerServiceImpl service;

    @Test
    void submitAnswer_savesAnswerAndPublishesEvent() {
        var session = InterviewSession.builder()
            .id(1L).userId("1").status(SessionStatus.ACTIVE).topic("Java").build();
        var questionLog = QuestionLog.builder()
            .questionId("q-001").questionText("What is JVM?").questionOrder(1).session(session).build();

        when(sessionRepo.findByIdAndUserId(1L, "1")).thenReturn(Optional.of(session));
        when(questionLogRepo.findBySessionIdOrderByQuestionOrder(1L)).thenReturn(List.of(questionLog));
        when(answerRepo.existsBySessionIdAndQuestionId(1L, "q-001")).thenReturn(false);
        when(answerRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var request = new SubmitAnswerRequest();
        request.setQuestionId("q-001");
        request.setAnswerText("JVM is the Java Virtual Machine...");

        service.submitAnswer(1L, "1", request);

        verify(answerRepo).save(any());
        verify(eventProducer).publishAnswerSubmitted(argThat(event ->
            event.getSessionId().equals(1L) &&
            event.getQuestionId().equals("q-001") &&
            event.getAnswerText().equals("JVM is the Java Virtual Machine...") &&
            event.getQuestionText().equals("What is JVM?")
        ));
    }

    @Test
    void submitAnswer_throwsIfSessionNotFound() {
        when(sessionRepo.findByIdAndUserId(anyLong(), anyString())).thenReturn(Optional.empty());

        var request = new SubmitAnswerRequest();
        request.setQuestionId("q-001");
        request.setAnswerText("answer");

        assertThatThrownBy(() -> service.submitAnswer(99L, "1", request))
            .isInstanceOf(SessionNotFoundException.class);
    }

    @Test
    void submitAnswer_throwsIfSessionNotActive() {
        var session = InterviewSession.builder()
            .id(1L).userId("1").status(SessionStatus.COMPLETED).topic("Java").build();
        when(sessionRepo.findByIdAndUserId(1L, "1")).thenReturn(Optional.of(session));

        var request = new SubmitAnswerRequest();
        request.setQuestionId("q-001");
        request.setAnswerText("answer");

        assertThatThrownBy(() -> service.submitAnswer(1L, "1", request))
            .isInstanceOf(SessionNotActiveException.class);
    }

    @Test
    void submitAnswer_throwsIfDuplicateAnswer() {
        var session = InterviewSession.builder()
            .id(1L).userId("1").status(SessionStatus.ACTIVE).topic("Java").build();
        when(sessionRepo.findByIdAndUserId(1L, "1")).thenReturn(Optional.of(session));
        when(answerRepo.existsBySessionIdAndQuestionId(1L, "q-001")).thenReturn(true);

        var request = new SubmitAnswerRequest();
        request.setQuestionId("q-001");
        request.setAnswerText("answer");

        assertThatThrownBy(() -> service.submitAnswer(1L, "1", request))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("q-001");
    }
}
