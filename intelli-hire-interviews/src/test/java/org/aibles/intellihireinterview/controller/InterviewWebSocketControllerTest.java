package org.aibles.intellihireinterview.controller;

import org.aibles.intellihireinterview.dto.AnswerMessage;
import org.aibles.intellihireinterview.dto.QuestionMessage;
import org.aibles.intellihireinterview.entity.InterviewSession;
import org.aibles.intellihireinterview.entity.QuestionLog;
import org.aibles.intellihireinterview.entity.enums.SessionStatus;
import org.aibles.intellihireinterview.repository.QuestionLogRepository;
import org.aibles.intellihireinterview.service.AnswerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterviewWebSocketControllerTest {

    @Mock AnswerService answerService;
    @Mock QuestionLogRepository questionLogRepo;
    @Mock SimpMessagingTemplate messagingTemplate;
    @InjectMocks InterviewWebSocketController controller;

    @Test
    void handleAnswer_sendsNextQuestion() {
        var session = InterviewSession.builder().id(1L).userId("1").status(SessionStatus.ACTIVE).topic("Java").build();
        var q1 = QuestionLog.builder().questionId("q-001").questionText("Q1").questionOrder(1).session(session).build();
        var q2 = QuestionLog.builder().questionId("q-002").questionText("Q2").questionOrder(2).session(session).build();

        when(questionLogRepo.findBySessionIdOrderByQuestionOrder(1L)).thenReturn(List.of(q1, q2));
        doNothing().when(answerService).submitAnswer(anyLong(), anyString(), any());

        var msg = new AnswerMessage();
        msg.setQuestionId("q-001");
        msg.setAnswerText("My answer");

        controller.handleAnswer(1L, msg, "1");

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<String> destCaptor = ArgumentCaptor.forClass(String.class);
        verify(messagingTemplate).convertAndSend(destCaptor.capture(), payloadCaptor.capture());

        assertThat(destCaptor.getValue()).isEqualTo("/topic/interview/1");
        QuestionMessage sent = (QuestionMessage) payloadCaptor.getValue();
        assertThat(sent.getType()).isEqualTo("QUESTION");
        assertThat(sent.getQuestionId()).isEqualTo("q-002");
    }

    @Test
    void handleAnswer_lastQuestion_sendsSessionEnd() {
        var session = InterviewSession.builder().id(1L).userId("1").status(SessionStatus.ACTIVE).topic("Java").build();
        var q1 = QuestionLog.builder().questionId("q-001").questionText("Q1").questionOrder(1).session(session).build();

        when(questionLogRepo.findBySessionIdOrderByQuestionOrder(1L)).thenReturn(List.of(q1));
        doNothing().when(answerService).submitAnswer(anyLong(), anyString(), any());

        var msg = new AnswerMessage();
        msg.setQuestionId("q-001");
        msg.setAnswerText("My answer");

        controller.handleAnswer(1L, msg, "1");

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSend(any(), payloadCaptor.capture());
        QuestionMessage sent = (QuestionMessage) payloadCaptor.getValue();
        assertThat(sent.getType()).isEqualTo("SESSION_END");
    }

    @Test
    void handleAnswer_missingUserId_sendsError() {
        var msg = new AnswerMessage();
        msg.setQuestionId("q-010");
        msg.setAnswerText("My answer");

        controller.handleAnswer(1L, msg, null);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSend(any(), payloadCaptor.capture());
        QuestionMessage sent = (QuestionMessage) payloadCaptor.getValue();
        assertThat(sent.getType()).isEqualTo("ERROR");
        verifyNoInteractions(answerService);
    }

    @Test
    void handleStart_sendsFirstQuestion() {
        var session = InterviewSession.builder().id(1L).build();
        var q1 = QuestionLog.builder().questionId("q-001").questionText("What is JVM?").questionOrder(1).session(session).build();
        var q2 = QuestionLog.builder().questionId("q-002").questionText("What is GC?").questionOrder(2).session(session).build();

        when(questionLogRepo.findBySessionIdOrderByQuestionOrder(1L)).thenReturn(List.of(q1, q2));

        controller.handleStart(1L, "42");

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<String> destCaptor = ArgumentCaptor.forClass(String.class);
        verify(messagingTemplate).convertAndSend(destCaptor.capture(), payloadCaptor.capture());

        assertThat(destCaptor.getValue()).isEqualTo("/topic/interview/1");
        QuestionMessage sent = (QuestionMessage) payloadCaptor.getValue();
        assertThat(sent.getType()).isEqualTo("QUESTION");
        assertThat(sent.getQuestionId()).isEqualTo("q-001");
        assertThat(sent.getQuestionOrder()).isEqualTo(1);
        assertThat(sent.getTotalQuestions()).isEqualTo(2);
    }

    @Test
    void handleStart_sendsError_whenUserIdMissing() {
        controller.handleStart(1L, null);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSend(any(), payloadCaptor.capture());
        assertThat(((QuestionMessage) payloadCaptor.getValue()).getType()).isEqualTo("ERROR");
    }

    @Test
    void handleStart_sendsSessionEnd_whenNoQuestions() {
        when(questionLogRepo.findBySessionIdOrderByQuestionOrder(1L)).thenReturn(List.of());

        controller.handleStart(1L, "42");

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSend(any(), payloadCaptor.capture());
        assertThat(((QuestionMessage) payloadCaptor.getValue()).getType()).isEqualTo("SESSION_END");
    }
}
