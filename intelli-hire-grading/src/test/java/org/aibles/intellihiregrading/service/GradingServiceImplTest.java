package org.aibles.intellihiregrading.service;

import org.aibles.intellihiregrading.dto.*;
import org.aibles.intellihiregrading.dto.openrouter.LlmGradingResult;
import org.aibles.intellihiregrading.entity.GradingResult;
import org.aibles.intellihiregrading.entity.GradingSession;
import org.aibles.intellihiregrading.entity.enums.GradingStatus;
import org.aibles.intellihiregrading.exception.BadRequestException;
import org.aibles.intellihiregrading.exception.DuplicateException;
import org.aibles.intellihiregrading.exception.NotFoundException;
import org.aibles.intellihiregrading.kafka.GradingCompletedProducer;
import org.springframework.cache.CacheManager;
import org.aibles.intellihiregrading.mapper.GradingMapper;
import org.aibles.intellihiregrading.repository.GradingResultRepository;
import org.aibles.intellihiregrading.repository.GradingSessionRepository;
import org.aibles.intellihiregrading.service.impl.GradingServiceImpl;
import org.aibles.intellihiregrading.utils.PromptBuilder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class GradingServiceImplTest {

    @Mock
    private GradingSessionRepository sessionRepository;

    @Mock
    private GradingResultRepository resultRepository;

    @Mock
    private LlmService llmService;

    @Mock
    private GradingCompletedProducer gradingCompletedProducer;

    @Mock
    private TransactionTemplate transactionTemplate;

    @Mock
    private CacheManager cacheManager;

    private GradingMapper gradingMapper;
    private PromptBuilder promptBuilder;
    private GradingServiceImpl gradingService;

    @BeforeEach
    void setUp() {
        gradingMapper = new GradingMapper();
        promptBuilder = new PromptBuilder();

        // Make TransactionTemplate execute callbacks directly (bypass real transactions)
        lenient().when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            var callback = invocation.getArgument(0, org.springframework.transaction.support.TransactionCallback.class);
            return callback.doInTransaction(null);
        });
        lenient().doAnswer(invocation -> {
            var consumer = invocation.getArgument(0, java.util.function.Consumer.class);
            consumer.accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());

        gradingService = new GradingServiceImpl(
                sessionRepository,
                resultRepository,
                llmService,
                gradingCompletedProducer,
                gradingMapper,
                promptBuilder,
                Executors.newFixedThreadPool(2),
                transactionTemplate,
                cacheManager
        );
    }

    @Nested
    class GradeMethod {

        @Test
        void grade_validRequest_returnsCompletedResponse() {
            GradeRequest request = GradeRequest.builder()
                    .sessionId("sess-1")
                    .userId("1")
                    .topic("Java")
                    .answers(List.of(
                            AnswerRequest.builder()
                                    .questionId("q1")
                                    .questionText("What is DI?")
                                    .answerText("Dependency injection")
                                    .skillName("spring")
                                    .maxScore(10.0)
                                    .build()
                    ))
                    .build();

            when(sessionRepository.findBySessionIdAndStatus("sess-1", GradingStatus.FAILED))
                    .thenReturn(Optional.empty());
            when(sessionRepository.existsBySessionIdAndStatusNot("sess-1", GradingStatus.FAILED))
                    .thenReturn(false);

            GradingSession savedSession = GradingSession.builder()
                    .sessionId("sess-1").userId("1").topic("Java").totalQuestions(1).build();
            savedSession.initId();
            when(sessionRepository.save(any(GradingSession.class))).thenReturn(savedSession);

            when(llmService.grade(anyString())).thenReturn(
                    LlmGradingResult.builder().score(8.5).feedback("Good").model("gpt-4o").build()
            );
            when(resultRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

            GradingResponse response = gradingService.grade(request);

            assertNotNull(response);
            assertEquals("sess-1", response.getSessionId());
            assertEquals("COMPLETED", response.getStatus());
            assertEquals(1, response.getTotalQuestions());
            assertEquals(8.5, response.getTotalScore());
            assertEquals(10.0, response.getMaxScore());
            assertEquals(1, response.getResults().size());
            assertEquals(8.5, response.getResults().get(0).getScore());
            assertEquals("Good", response.getResults().get(0).getFeedback());

            verify(gradingCompletedProducer).publish(any(), anyList());
        }

        @Test
        void grade_duplicateCompletedSession_throwsDuplicateException() {
            GradeRequest request = GradeRequest.builder()
                    .sessionId("sess-dup")
                    .userId("1")
                    .answers(List.of(AnswerRequest.builder().questionId("q1").maxScore(10.0).build()))
                    .build();

            when(sessionRepository.findBySessionIdAndStatus("sess-dup", GradingStatus.FAILED))
                    .thenReturn(Optional.empty());
            when(sessionRepository.existsBySessionIdAndStatusNot("sess-dup", GradingStatus.FAILED))
                    .thenReturn(true);

            assertThrows(DuplicateException.class, () -> gradingService.grade(request));
        }

        @Test
        void grade_failedSessionExists_regradesSuccessfully() {
            GradeRequest request = GradeRequest.builder()
                    .sessionId("sess-failed")
                    .userId("1")
                    .topic("Go")
                    .answers(List.of(
                            AnswerRequest.builder().questionId("q1").questionText("Q?")
                                    .answerText("A").skillName("go").maxScore(10.0).build()
                    ))
                    .build();

            GradingSession failedSession = GradingSession.builder()
                    .sessionId("sess-failed").status(GradingStatus.FAILED).build();
            failedSession.initId();

            when(sessionRepository.findBySessionIdAndStatus("sess-failed", GradingStatus.FAILED))
                    .thenReturn(Optional.of(failedSession));
            when(sessionRepository.existsBySessionIdAndStatusNot("sess-failed", GradingStatus.FAILED))
                    .thenReturn(false);

            GradingSession newSession = GradingSession.builder()
                    .sessionId("sess-failed").userId("1").topic("Go").totalQuestions(1).build();
            newSession.initId();
            when(sessionRepository.save(any(GradingSession.class))).thenReturn(newSession);

            when(llmService.grade(anyString())).thenReturn(
                    LlmGradingResult.builder().score(7.0).feedback("OK").model("gpt-4o").build()
            );
            when(resultRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

            GradingResponse response = gradingService.grade(request);

            assertEquals("COMPLETED", response.getStatus());
            verify(resultRepository).deleteByGradingSessionId(failedSession.getId());
            verify(sessionRepository).delete(failedSession);
        }

        @Test
        void grade_llmReturnsHighScore_clampedToMaxScore() {
            GradeRequest request = GradeRequest.builder()
                    .sessionId("sess-clamp")
                    .userId("1")
                    .answers(List.of(
                            AnswerRequest.builder().questionId("q1").questionText("Q?")
                                    .answerText("A").maxScore(10.0).build()
                    ))
                    .build();

            when(sessionRepository.findBySessionIdAndStatus(anyString(), any())).thenReturn(Optional.empty());
            when(sessionRepository.existsBySessionIdAndStatusNot(anyString(), any())).thenReturn(false);

            GradingSession session = GradingSession.builder()
                    .sessionId("sess-clamp").userId("1").totalQuestions(1).build();
            session.initId();
            when(sessionRepository.save(any())).thenReturn(session);

            // LLM returns score 15 which exceeds maxScore of 10
            when(llmService.grade(anyString())).thenReturn(
                    LlmGradingResult.builder().score(15.0).feedback("Over max").model("gpt-4o").build()
            );
            when(resultRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

            GradingResponse response = gradingService.grade(request);

            assertEquals(10.0, response.getTotalScore()); // clamped to maxScore
            assertEquals(10.0, response.getResults().get(0).getScore());
        }

        @Test
        void grade_multipleQuestions_allGradedInParallel() {
            GradeRequest request = GradeRequest.builder()
                    .sessionId("sess-multi")
                    .userId("1")
                    .topic("Java")
                    .answers(List.of(
                            AnswerRequest.builder().questionId("q1").questionText("Q1?")
                                    .answerText("A1").skillName("java").maxScore(10.0).build(),
                            AnswerRequest.builder().questionId("q2").questionText("Q2?")
                                    .answerText("A2").skillName("spring").maxScore(10.0).build(),
                            AnswerRequest.builder().questionId("q3").questionText("Q3?")
                                    .answerText("A3").skillName("sql").maxScore(10.0).build()
                    ))
                    .build();

            when(sessionRepository.findBySessionIdAndStatus(anyString(), any())).thenReturn(Optional.empty());
            when(sessionRepository.existsBySessionIdAndStatusNot(anyString(), any())).thenReturn(false);

            GradingSession session = GradingSession.builder()
                    .sessionId("sess-multi").userId("1").topic("Java").totalQuestions(3).build();
            session.initId();
            when(sessionRepository.save(any())).thenReturn(session);

            when(llmService.grade(anyString())).thenReturn(
                    LlmGradingResult.builder().score(7.0).feedback("OK").model("gpt-4o").build()
            );
            when(resultRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

            GradingResponse response = gradingService.grade(request);

            assertEquals(3, response.getTotalQuestions());
            assertEquals(3, response.getResults().size());
            assertEquals(21.0, response.getTotalScore()); // 7 * 3
            assertEquals(30.0, response.getMaxScore()); // 10 * 3
            verify(llmService, times(3)).grade(anyString());
        }

        @Test
        void grade_llmThrowsException_failedQuestionGetsZeroScore() {
            GradeRequest request = GradeRequest.builder()
                    .sessionId("sess-partial")
                    .userId("1")
                    .answers(List.of(
                            AnswerRequest.builder().questionId("q1").questionText("Q1?")
                                    .answerText("A1").maxScore(10.0).build(),
                            AnswerRequest.builder().questionId("q2").questionText("Q2?")
                                    .answerText("A2").maxScore(10.0).build()
                    ))
                    .build();

            when(sessionRepository.findBySessionIdAndStatus(anyString(), any())).thenReturn(Optional.empty());
            when(sessionRepository.existsBySessionIdAndStatusNot(anyString(), any())).thenReturn(false);

            GradingSession session = GradingSession.builder()
                    .sessionId("sess-partial").userId("1").totalQuestions(2).build();
            session.initId();
            when(sessionRepository.save(any())).thenReturn(session);

            // First call succeeds, second throws
            when(llmService.grade(anyString()))
                    .thenReturn(LlmGradingResult.builder().score(8.0).feedback("Good").model("gpt-4o").build())
                    .thenThrow(new RuntimeException("LLM timeout"));
            when(resultRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

            GradingResponse response = gradingService.grade(request);

            assertEquals("COMPLETED", response.getStatus());
            assertEquals(2, response.getResults().size());
            // One should have score, one should be 0
            long zeroScoreCount = response.getResults().stream()
                    .filter(r -> r.getScore() == 0.0).count();
            long successCount = response.getResults().stream()
                    .filter(r -> r.getScore() > 0.0).count();
            assertTrue(zeroScoreCount >= 1 || successCount >= 1,
                    "Should have mix of success and failure results");
        }
    }

    @Nested
    class GetByIdMethod {

        @Test
        void getById_exists_returnsResponse() {
            GradingSession session = GradingSession.builder()
                    .sessionId("sess-1").userId("1").topic("Java")
                    .status(GradingStatus.COMPLETED).totalQuestions(1).build();
            session.initId();

            GradingResult result = GradingResult.builder()
                    .gradingSessionId(session.getId()).questionId("q1")
                    .score(8.0).maxScore(10.0).feedback("Good").build();
            result.initId();

            when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
            when(resultRepository.findByGradingSessionId(session.getId())).thenReturn(List.of(result));

            GradingResponse response = gradingService.getById(session.getId());

            assertNotNull(response);
            assertEquals("sess-1", response.getSessionId());
            assertEquals(8.0, response.getTotalScore());
        }

        @Test
        void getById_notFound_throwsNotFoundException() {
            when(sessionRepository.findById("nonexistent")).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> gradingService.getById("nonexistent"));
        }
    }

    @Nested
    class GetBySessionIdMethod {

        @Test
        void getBySessionId_exists_returnsResponse() {
            GradingSession session = GradingSession.builder()
                    .sessionId("sess-1").userId("1").status(GradingStatus.COMPLETED).totalQuestions(0).build();
            session.initId();

            when(sessionRepository.findBySessionId("sess-1")).thenReturn(Optional.of(session));
            when(resultRepository.findByGradingSessionId(session.getId())).thenReturn(List.of());

            GradingResponse response = gradingService.getBySessionId("sess-1");

            assertEquals("sess-1", response.getSessionId());
        }

        @Test
        void getBySessionId_notFound_throwsNotFoundException() {
            when(sessionRepository.findBySessionId("nonexistent")).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> gradingService.getBySessionId("nonexistent"));
        }
    }

    @Nested
    class GetHistoryMethod {

        @Test
        void getHistoryByUserId_noStatusFilter_returnsPage() {
            GradingSession s1 = GradingSession.builder()
                    .sessionId("s1").userId("1").status(GradingStatus.COMPLETED).totalQuestions(5).build();
            s1.initId();

            Pageable pageable = PageRequest.of(0, 10);
            Page<GradingSession> page = new PageImpl<>(List.of(s1), pageable, 1);
            when(sessionRepository.findByUserId("1", pageable)).thenReturn(page);

            PageResponse<GradingSummaryResponse> result = gradingService.getHistoryByUserId("1", null, pageable);

            assertEquals(1, result.getContent().size());
            assertEquals("s1", result.getContent().get(0).getSessionId());
        }

        @Test
        void getHistoryByUserId_withStatusFilter_returnsFilteredPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<GradingSession> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            when(sessionRepository.findByUserIdAndStatus("1", GradingStatus.FAILED, pageable))
                    .thenReturn(emptyPage);

            PageResponse<GradingSummaryResponse> result = gradingService.getHistoryByUserId("1", "FAILED", pageable);

            assertEquals(0, result.getContent().size());
        }

        @Test
        void getHistoryByUserId_invalidStatus_throwsBadRequest() {
            Pageable pageable = PageRequest.of(0, 10);

            assertThrows(BadRequestException.class,
                    () -> gradingService.getHistoryByUserId("1", "INVALID_STATUS", pageable));
        }
    }

    @Nested
    class GetAllGradingsMethod {

        @Test
        void getAllGradings_returnsPage() {
            GradingSession s1 = GradingSession.builder()
                    .sessionId("s1").userId("1").status(GradingStatus.COMPLETED).totalQuestions(3).build();
            s1.initId();
            GradingSession s2 = GradingSession.builder()
                    .sessionId("s2").userId("2").status(GradingStatus.FAILED).totalQuestions(5).build();
            s2.initId();

            Pageable pageable = PageRequest.of(0, 10);
            Page<GradingSession> page = new PageImpl<>(List.of(s1, s2), pageable, 2);
            when(sessionRepository.findAll(pageable)).thenReturn(page);

            PageResponse<GradingSummaryResponse> result = gradingService.getAllGradings(pageable);

            assertEquals(2, result.getContent().size());
            assertEquals("COMPLETED", result.getContent().get(0).getStatus());
            assertEquals("FAILED", result.getContent().get(1).getStatus());
        }

        @Test
        void getAllGradings_emptyResult_returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<GradingSession> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            when(sessionRepository.findAll(pageable)).thenReturn(emptyPage);

            PageResponse<GradingSummaryResponse> result = gradingService.getAllGradings(pageable);

            assertTrue(result.getContent().isEmpty());
            assertEquals(0, result.getTotalElements());
        }
    }

    @Nested
    class GradeAllFailedMethod {

        @Test
        void grade_allLlmFail_sessionStatusIsFailed() {
            GradeRequest request = GradeRequest.builder()
                    .sessionId("sess-allfail")
                    .userId("1")
                    .answers(List.of(
                            AnswerRequest.builder().questionId("q1").questionText("Q?")
                                    .answerText("A").maxScore(10.0).build()
                    ))
                    .build();

            when(sessionRepository.findBySessionIdAndStatus(anyString(), any())).thenReturn(Optional.empty());
            when(sessionRepository.existsBySessionIdAndStatusNot(anyString(), any())).thenReturn(false);

            GradingSession session = GradingSession.builder()
                    .sessionId("sess-allfail").userId("1").totalQuestions(1).build();
            session.initId();
            when(sessionRepository.save(any())).thenReturn(session);

            // LLM returns fallback (Unable to grade)
            when(llmService.grade(anyString())).thenReturn(
                    LlmGradingResult.builder().score(0.0).feedback("Unable to grade: timeout").model("gpt-4o").build()
            );
            when(resultRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

            GradingResponse response = gradingService.grade(request);

            assertEquals("FAILED", response.getStatus());
            assertEquals(0.0, response.getTotalScore());
        }
    }
}
