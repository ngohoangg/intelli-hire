package org.aibles.intellihiregrading.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihiregrading.dto.*;
import org.aibles.intellihiregrading.dto.openrouter.LlmGradingResult;
import org.aibles.intellihiregrading.entity.GradingResult;
import org.aibles.intellihiregrading.entity.GradingSession;
import org.aibles.intellihiregrading.entity.enums.GradingStatus;
import org.aibles.intellihiregrading.exception.*;
import org.aibles.intellihiregrading.mapper.GradingMapper;
import org.aibles.intellihiregrading.repository.GradingResultRepository;
import org.aibles.intellihiregrading.repository.GradingSessionRepository;
import org.aibles.intellihiregrading.service.GradingService;
import org.aibles.intellihiregrading.service.LlmService;
import org.aibles.intellihiregrading.kafka.GradingCompletedProducer;
import org.aibles.intellihiregrading.utils.PromptBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GradingServiceImpl implements GradingService {

    private final GradingSessionRepository sessionRepository;
    private final GradingResultRepository resultRepository;
    private final LlmService llmService;
    private final GradingCompletedProducer gradingCompletedProducer;
    private final GradingMapper gradingMapper;
    private final PromptBuilder promptBuilder;
    private final Executor gradingTaskExecutor;
    private final TransactionTemplate transactionTemplate;
    private final CacheManager cacheManager;

    public GradingServiceImpl(
            GradingSessionRepository sessionRepository,
            GradingResultRepository resultRepository,
            LlmService llmService,
            GradingCompletedProducer gradingCompletedProducer,
            GradingMapper gradingMapper,
            PromptBuilder promptBuilder,
            @Qualifier("gradingTaskExecutor") Executor gradingTaskExecutor,
            TransactionTemplate transactionTemplate,
            CacheManager cacheManager) {
        this.sessionRepository = sessionRepository;
        this.resultRepository = resultRepository;
        this.llmService = llmService;
        this.gradingCompletedProducer = gradingCompletedProducer;
        this.gradingMapper = gradingMapper;
        this.promptBuilder = promptBuilder;
        this.gradingTaskExecutor = gradingTaskExecutor;
        this.transactionTemplate = transactionTemplate;
        this.cacheManager = cacheManager;
    }

    @Override
    public GradingResponse grade(GradeRequest request) {
        log.info("Starting grading for sessionId: {}", request.getSessionId());

        // 1+2. Check duplicate + save session PROCESSING (single transaction to avoid race condition)
        GradingSession session = transactionTemplate.execute(status -> {
            handleDuplicateSession(request.getSessionId());
            GradingSession s = gradingMapper.toEntity(request);
            return sessionRepository.save(s);
        });

        // 3. Parallel LLM grading (NO transaction — avoid holding DB connections)
        List<GradingResult> results = gradeInParallel(request.getAnswers(), request.getTopic(), session.getId());

        // 4. Save results + update session (in transaction)
        boolean allFailed = results.stream().allMatch(r ->
                r.getFeedback() != null && r.getFeedback().startsWith("Unable to grade"));
        transactionTemplate.executeWithoutResult(status -> {
            resultRepository.saveAll(results);
            session.setGradedCount(results.size());
            session.setStatus(allFailed ? GradingStatus.FAILED : GradingStatus.COMPLETED);
            sessionRepository.save(session);
        });

        // 5. Publish grading_completed event to Kafka (Report Service consumes)
        gradingCompletedProducer.publish(session, results);

        // 6. Return response
        log.info("Grading completed for sessionId: {}, gradedCount: {}",
                request.getSessionId(), results.size());
        return gradingMapper.toGradingResponse(session, results);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "grading", key = "#id")
    public GradingResponse getById(String id) {
        GradingSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.GRD_001));
        List<GradingResult> results = resultRepository.findByGradingSessionId(id);
        return gradingMapper.toGradingResponse(session, results);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "grading_session", key = "#sessionId")
    public GradingResponse getBySessionId(String sessionId) {
        GradingSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.GRD_001));
        List<GradingResult> results = resultRepository.findByGradingSessionId(session.getId());
        return gradingMapper.toGradingResponse(session, results);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<GradingSummaryResponse> getHistoryByUserId(String userId, String status, Pageable pageable) {
        Page<GradingSession> page;
        if (status != null && !status.isBlank()) {
            GradingStatus gradingStatus;
            try {
                gradingStatus = GradingStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException(ErrorCode.GRD_004);
            }
            page = sessionRepository.findByUserIdAndStatus(userId, gradingStatus, pageable);
        } else {
            page = sessionRepository.findByUserId(userId, pageable);
        }
        return PageResponse.from(page.map(gradingMapper::toSummaryResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<GradingSummaryResponse> getAllGradings(Pageable pageable) {
        Page<GradingSession> page = sessionRepository.findAll(pageable);
        return PageResponse.from(page.map(gradingMapper::toSummaryResponse));
    }

    // --- Private helpers ---

    private void handleDuplicateSession(String sessionId) {
        sessionRepository.findBySessionIdAndStatus(sessionId, GradingStatus.FAILED)
                .ifPresent(failedSession -> {
                    log.info("Re-grading FAILED session: {}", sessionId);
                    evictCache("grading", failedSession.getId());
                    evictCache("grading_session", sessionId);
                    resultRepository.deleteByGradingSessionId(failedSession.getId());
                    sessionRepository.delete(failedSession);
                });

        if (sessionRepository.existsBySessionIdAndStatusNot(sessionId, GradingStatus.FAILED)) {
            throw new DuplicateException(ErrorCode.GRD_002);
        }
    }

    private List<GradingResult> gradeInParallel(List<AnswerRequest> answers, String topic, String sessionId) {
        List<CompletableFuture<GradingResult>> futures = answers.stream()
                .map(answer -> CompletableFuture.supplyAsync(
                                () -> gradeOneQuestion(answer, topic, sessionId), gradingTaskExecutor)
                        .handle((result, ex) -> {
                            if (ex != null) {
                                log.error("Failed to grade question {}: {}", answer.getQuestionId(), ex.getMessage());
                                return buildFailedResult(answer, sessionId, ex.getMessage());
                            }
                            return result;
                        }))
                .collect(Collectors.toList());

        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    private GradingResult gradeOneQuestion(AnswerRequest answer, String topic, String sessionId) {
        long start = System.currentTimeMillis();
        String prompt = promptBuilder.build(answer, topic);
        LlmGradingResult llmResult = llmService.grade(prompt);
        long latencyMs = System.currentTimeMillis() - start;

        double clampedScore = Math.max(0, Math.min(llmResult.getScore(), answer.getMaxScore()));

        return GradingResult.builder()
                .gradingSessionId(sessionId)
                .questionId(answer.getQuestionId())
                .questionText(answer.getQuestionText())
                .answerText(answer.getAnswerText())
                .skillName(answer.getSkillName())
                .score(clampedScore)
                .maxScore(answer.getMaxScore())
                .feedback(llmResult.getFeedback())
                .promptUsed(prompt)
                .modelUsed(llmResult.getModel())
                .latencyMs(latencyMs)
                .build();
    }

    private GradingResult buildFailedResult(AnswerRequest answer, String sessionId, String errorMsg) {
        return GradingResult.builder()
                .gradingSessionId(sessionId)
                .questionId(answer.getQuestionId())
                .questionText(answer.getQuestionText())
                .answerText(answer.getAnswerText())
                .skillName(answer.getSkillName())
                .score(0.0)
                .maxScore(answer.getMaxScore())
                .feedback("Unable to grade: " + errorMsg)
                .modelUsed("N/A")
                .latencyMs(0L)
                .build();
    }

    private void evictCache(String cacheName, String key) {
        try {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) cache.evict(key);
        } catch (Exception e) {
            log.warn("Failed to evict cache {}:{} — {}", cacheName, key, e.getMessage());
        }
    }
}
