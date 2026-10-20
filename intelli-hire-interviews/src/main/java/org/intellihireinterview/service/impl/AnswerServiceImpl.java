package org.aibles.intellihireinterview.service.impl;

import org.aibles.intellihireinterview.entity.QuestionLog;
import org.aibles.intellihireinterview.entity.UserAnswerRaw;
import org.aibles.intellihireinterview.entity.enums.SessionStatus;
import org.aibles.intellihireinterview.dto.AnswerSubmittedEvent;
import org.aibles.intellihireinterview.dto.SubmitAnswerRequest;
import org.aibles.intellihireinterview.exception.SessionNotActiveException;
import org.aibles.intellihireinterview.exception.SessionNotFoundException;
import org.aibles.intellihireinterview.kafka.AnswerEventProducer;
import org.aibles.intellihireinterview.repository.InterviewSessionRepository;
import org.aibles.intellihireinterview.repository.QuestionLogRepository;
import org.aibles.intellihireinterview.repository.UserAnswerRawRepository;
import org.aibles.intellihireinterview.service.AnswerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnswerServiceImpl implements AnswerService {

    private final InterviewSessionRepository sessionRepo;
    private final UserAnswerRawRepository answerRepo;
    private final QuestionLogRepository questionLogRepo;
    private final AnswerEventProducer eventProducer;

    @Override
    @Transactional
    public void submitAnswer(Long sessionId, String userId, SubmitAnswerRequest request) {
        var session = sessionRepo.findByIdAndUserId(sessionId, userId)
            .orElseThrow(() -> new SessionNotFoundException(sessionId));

        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new SessionNotActiveException(sessionId);
        }

        if (answerRepo.existsBySessionIdAndQuestionId(sessionId, request.getQuestionId())) {
            log.warn("Duplicate answer attempt for session {} question {} by user {}",
                     sessionId, request.getQuestionId(), userId);
            throw new IllegalStateException(
                "Answer already submitted for question " + request.getQuestionId() + " in session " + sessionId);
        }

        var questionText = questionLogRepo.findBySessionIdOrderByQuestionOrder(sessionId).stream()
            .filter(ql -> ql.getQuestionId().equals(request.getQuestionId()))
            .findFirst()
            .map(QuestionLog::getQuestionText)
            .orElse("");

        var answer = UserAnswerRaw.builder()
            .session(session)
            .questionId(request.getQuestionId())
            .answerText(request.getAnswerText())
            .build();
        answerRepo.save(answer);

        var event = AnswerSubmittedEvent.builder()
            .sessionId(sessionId)
            .questionId(request.getQuestionId())
            .userId(userId)
            .answerText(request.getAnswerText())
            .questionText(questionText)
            .submittedAt(LocalDateTime.now())
            .build();
        eventProducer.publishAnswerSubmitted(event);
    }
}
