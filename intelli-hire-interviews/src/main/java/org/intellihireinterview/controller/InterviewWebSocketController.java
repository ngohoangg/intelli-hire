package org.aibles.intellihireinterview.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihireinterview.dto.AnswerMessage;
import org.aibles.intellihireinterview.dto.QuestionMessage;
import org.aibles.intellihireinterview.dto.SubmitAnswerRequest;
import org.aibles.intellihireinterview.repository.QuestionLogRepository;
import org.aibles.intellihireinterview.service.AnswerService;
import org.aibles.intellihireinterview.service.InterviewSessionService;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class InterviewWebSocketController {

    private final AnswerService answerService;
    private final InterviewSessionService sessionService;
    private final QuestionLogRepository questionLogRepo;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Client sends to: /app/interview/{sessionId}/answer
     * Service saves answer, publishes Kafka event, then broadcasts next question.
     * Next question sent to: /topic/interview/{sessionId}
     */
    @MessageMapping("/interview/{sessionId}/answer")
    public void handleAnswer(@DestinationVariable Long sessionId,
                             @Payload AnswerMessage answerMessage,
                             @Header(value = "X-User-Id", required = false) String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isBlank()) {
            log.warn("Rejected WebSocket answer for session {} — missing X-User-Id header", sessionId);
            messagingTemplate.convertAndSend("/topic/interview/" + sessionId,
                QuestionMessage.builder().type("ERROR").build());
            return;
        }

        var request = new SubmitAnswerRequest();
        request.setQuestionId(answerMessage.getQuestionId());
        request.setAnswerText(answerMessage.getAnswerText());

        try {
            answerService.submitAnswer(sessionId, userIdHeader, request);
        } catch (Exception e) {
            log.warn("Error submitting answer for session {}: {}", sessionId, e.getMessage());
            messagingTemplate.convertAndSend("/topic/interview/" + sessionId,
                QuestionMessage.builder().type("ERROR").build());
            return;
        }

        var logs = questionLogRepo.findBySessionIdOrderByQuestionOrder(sessionId);
        int nextOrder = logs.stream()
            .filter(ql -> ql.getQuestionId().equals(answerMessage.getQuestionId()))
            .findFirst()
            .map(ql -> ql.getQuestionOrder() + 1)
            .orElse(Integer.MAX_VALUE);

        var nextQuestion = logs.stream()
            .filter(ql -> ql.getQuestionOrder() == nextOrder)
            .findFirst();

        QuestionMessage message;
        if (nextQuestion.isPresent()) {
            var ql = nextQuestion.get();
            message = QuestionMessage.builder()
                .type("QUESTION")
                .questionId(ql.getQuestionId())
                .questionText(ql.getQuestionText())
                .questionOrder(ql.getQuestionOrder())
                .totalQuestions(logs.size())
                .build();
        } else {
            try {
                sessionService.endSession(sessionId, userIdHeader);
            } catch (Exception e) {
                log.warn("Could not auto-complete session {}: {}", sessionId, e.getMessage());
            }
            message = QuestionMessage.builder()
                .type("SESSION_END")
                .totalQuestions(logs.size())
                .build();
        }

        messagingTemplate.convertAndSend("/topic/interview/" + sessionId, message);
    }

    /**
     * Client sends to: /app/interview/{sessionId}/start
     * Service pushes the first question to: /topic/interview/{sessionId}
     */
    @MessageMapping("/interview/{sessionId}/start")
    public void handleStart(@DestinationVariable Long sessionId,
                            @Header(value = "X-User-Id", required = false) String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isBlank()) {
            log.warn("Rejected WebSocket start for session {} — missing X-User-Id header", sessionId);
            messagingTemplate.convertAndSend("/topic/interview/" + sessionId,
                QuestionMessage.builder().type("ERROR").build());
            return;
        }

        var logs = questionLogRepo.findBySessionIdOrderByQuestionOrder(sessionId);
        if (logs.isEmpty()) {
            messagingTemplate.convertAndSend("/topic/interview/" + sessionId,
                QuestionMessage.builder().type("SESSION_END").totalQuestions(0).build());
            return;
        }

        var first = logs.get(0);
        messagingTemplate.convertAndSend("/topic/interview/" + sessionId,
            QuestionMessage.builder()
                .type("QUESTION")
                .questionId(first.getQuestionId())
                .questionText(first.getQuestionText())
                .questionOrder(first.getQuestionOrder())
                .totalQuestions(logs.size())
                .build());
    }
}
