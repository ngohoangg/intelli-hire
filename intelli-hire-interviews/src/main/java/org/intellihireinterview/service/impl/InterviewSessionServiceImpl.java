package org.aibles.intellihireinterview.service.impl;

import org.aibles.intellihireinterview.client.QuestionServiceClient;
import org.aibles.intellihireinterview.dto.event.SessionCompletedEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.aibles.intellihireinterview.entity.InterviewSession;
import org.aibles.intellihireinterview.entity.QuestionLog;
import org.aibles.intellihireinterview.entity.enums.SessionStatus;
import org.aibles.intellihireinterview.dto.CreateSessionRequest;
import org.aibles.intellihireinterview.dto.SessionDetailResponse;
import org.aibles.intellihireinterview.exception.NoQuestionsAvailableException;
import org.aibles.intellihireinterview.exception.SessionNotActiveException;
import org.aibles.intellihireinterview.exception.SessionNotFoundException;
import org.aibles.intellihireinterview.kafka.SessionEventProducer;
import org.aibles.intellihireinterview.mapper.SessionMapper;
import org.aibles.intellihireinterview.repository.InterviewSessionRepository;
import org.aibles.intellihireinterview.repository.QuestionLogRepository;
import org.aibles.intellihireinterview.repository.UserAnswerRawRepository;
import org.aibles.intellihireinterview.service.InterviewSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewSessionServiceImpl implements InterviewSessionService {

    private final InterviewSessionRepository sessionRepo;
    private final QuestionLogRepository questionLogRepo;
    private final UserAnswerRawRepository answerRepo;
    private final QuestionServiceClient questionClient;
    private final SessionMapper sessionMapper;
    private final SessionEventProducer sessionEventProducer;

    @Override
    @Transactional
    public InterviewSession createSession(String userId, CreateSessionRequest request) {
        var session = InterviewSession.builder()
            .userId(userId)
            .topic(request.getTopic())
            .difficulty(request.getDifficulty())
            .status(SessionStatus.CREATED)
            .build();
        session = sessionRepo.save(session);

        var questions = questionClient.getRandomQuestions(
            null, null, request.getQuestionCount());

        if (questions.isEmpty()) {
            throw new NoQuestionsAvailableException();
        }

        var counter = new AtomicInteger(1);
        var logs = new ArrayList<QuestionLog>();
        for (var q : questions) {
            logs.add(QuestionLog.builder()
                .session(session)
                .questionId(q.getId())
                .questionText(q.getQuestionText())
                .questionOrder(counter.getAndIncrement())
                .build());
        }
        questionLogRepo.saveAll(logs);

        session.setStatus(SessionStatus.ACTIVE);
        session = sessionRepo.save(session);

        log.info("Created interview session {} for user {} with {} questions",
                 session.getId(), userId, logs.size());
        return session;
    }

    @Override
    @Transactional(readOnly = true)
    public InterviewSession getSession(Long sessionId, String userId) {
        return sessionRepo.findByIdAndUserId(sessionId, userId)
            .orElseThrow(() -> new SessionNotFoundException(sessionId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterviewSession> listSessions(String userId) {
        return sessionRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InterviewSession> listSessions(String userId, Pageable pageable) {
        return sessionRepo.findByUserId(userId, pageable);
    }

    @Override
    @Transactional
    public void endSession(Long sessionId, String userId) {
        var session = sessionRepo.findByIdAndUserId(sessionId, userId)
            .orElseThrow(() -> new SessionNotFoundException(sessionId));
        if (session.getStatus().isTerminal()) {
            throw new SessionNotActiveException(sessionId);
        }
        session.setStatus(SessionStatus.COMPLETED);
        session.setEndedAt(LocalDateTime.now());
        sessionRepo.save(session);

        int totalQ = questionLogRepo.findBySessionIdOrderByQuestionOrder(sessionId).size();
        int answered = answerRepo.findBySessionIdOrderBySubmittedAt(sessionId).size();
        try {
            sessionEventProducer.publishSessionCompleted(
                SessionCompletedEvent.builder()
                    .sessionId(sessionId)
                    .userId(userId)
                    .topic(session.getTopic())
                    .difficulty(session.getDifficulty())
                    .finalStatus("COMPLETED")
                    .startedAt(session.getStartedAt())
                    .endedAt(session.getEndedAt())
                    .totalQuestions(totalQ)
                    .answeredQuestions(answered)
                    .build()
            );
        } catch (Exception e) {
            log.warn("Failed to publish session-completed event for session {}: {}", sessionId, e.getMessage());
        }
        log.info("Ended session {} for user {}", sessionId, userId);
    }

    @Override
    @Transactional
    public void deleteSession(Long sessionId, String userId) {
        var session = sessionRepo.findByIdAndUserId(sessionId, userId)
            .orElseThrow(() -> new SessionNotFoundException(sessionId));
        sessionRepo.delete(session);
        log.info("Deleted session {} for user {}", sessionId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public SessionDetailResponse getSessionDetail(Long sessionId, String userId) {
        var session = sessionRepo.findByIdAndUserId(sessionId, userId)
            .orElseThrow(() -> new SessionNotFoundException(sessionId));
        var detail = sessionMapper.toDetailResponse(session);
        var questions = questionLogRepo.findBySessionIdOrderByQuestionOrder(sessionId);
        var answers = answerRepo.findBySessionIdOrderBySubmittedAt(sessionId);
        detail.setQuestions(questions.stream().map(sessionMapper::toQuestionLogDto).toList());
        detail.setAnswers(answers.stream().map(sessionMapper::toAnswerDto).toList());
        return detail;
    }
}
