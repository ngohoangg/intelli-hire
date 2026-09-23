package org.aibles.intellihirequestion.service.impl;

import org.aibles.intellihirequestion.dto.RubricRequest;
import org.aibles.intellihirequestion.dto.RubricResponse;
import org.aibles.intellihirequestion.entity.Question;
import org.aibles.intellihirequestion.entity.Rubric;
import org.aibles.intellihirequestion.entity.enums.QuestionType;
import org.aibles.intellihirequestion.exception.BaseException;
import org.aibles.intellihirequestion.exception.DuplicateException;
import org.aibles.intellihirequestion.exception.ErrorCode;
import org.aibles.intellihirequestion.exception.NotFoundException;
import org.aibles.intellihirequestion.mapper.RubricMapper;
import org.aibles.intellihirequestion.repository.QuestionRepository;
import org.aibles.intellihirequestion.repository.RubricRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RubricServiceImplTest {

    @Mock private QuestionRepository questionRepository;
    @Mock private RubricRepository rubricRepository;
    @Mock private RubricMapper rubricMapper;

    @InjectMocks
    private RubricServiceImpl rubricService;

    private static final String QUESTION_ID = "question-uuid-1";
    private static final String RUBRIC_ID = "rubric-uuid-1";

    private RubricRequest buildRubricRequest() {
        RubricRequest request = new RubricRequest();
        request.setCriteria("Explain OOP principles clearly with examples");
        request.setKeywords(List.of("encapsulation", "inheritance", "polymorphism", "abstraction"));
        request.setExampleAnswer("OOP has 4 pillars...");
        request.setCommonMistakes(List.of("Confusing abstraction with encapsulation"));
        request.setHints(List.of("Think of real-world examples"));
        request.setMaxScore(10);
        return request;
    }

    private Question buildQuestion() {
        Question question = new Question();
        question.setId(QUESTION_ID);
        question.setTopicId("topic-uuid-1");
        question.setDifficultyId("difficulty-uuid-1");
        question.setQuestionText("Explain OOP");
        question.setQuestionType(QuestionType.TECHNICAL);
        question.setIsActive(true);
        return question;
    }

    private Rubric buildRubric() {
        Rubric rubric = new Rubric();
        rubric.setId(RUBRIC_ID);
        rubric.setQuestionId(QUESTION_ID);
        rubric.setCriteria("Explain OOP principles clearly with examples");
        rubric.setMaxScore(10);
        rubric.setIsActive(true);
        rubric.setCreatedAt(LocalDateTime.now());
        return rubric;
    }

    private RubricResponse buildRubricResponse() {
        return RubricResponse.builder()
                .id(RUBRIC_ID)
                .questionId(QUESTION_ID)
                .criteria("Explain OOP principles clearly with examples")
                .keywords(List.of("encapsulation", "inheritance", "polymorphism", "abstraction"))
                .exampleAnswer("OOP has 4 pillars...")
                .commonMistakes(List.of("Confusing abstraction with encapsulation"))
                .hints(List.of("Think of real-world examples"))
                .maxScore(10)
                .isActive(true)
                .build();
    }

    @Nested
    @DisplayName("createRubric()")
    class CreateRubricTests {

        @Test
        @DisplayName("Should create rubric successfully")
        void shouldCreateRubricSuccessfully() {
            // Given
            RubricRequest request = buildRubricRequest();
            Question question = buildQuestion();
            Rubric rubric = buildRubric();
            RubricResponse expectedResponse = buildRubricResponse();

            when(questionRepository.existsById(QUESTION_ID)).thenReturn(true);
            when(rubricRepository.existsByQuestionId(QUESTION_ID)).thenReturn(false);
            when(rubricMapper.toEntity(eq(QUESTION_ID), any(RubricRequest.class))).thenReturn(rubric);
            when(rubricRepository.save(any(Rubric.class))).thenReturn(rubric);
            when(rubricMapper.toResponse(rubric)).thenReturn(expectedResponse);

            // When
            RubricResponse result = rubricService.createRubric(QUESTION_ID, request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(RUBRIC_ID);
            assertThat(result.getQuestionId()).isEqualTo(QUESTION_ID);
            assertThat(result.getCriteria()).isEqualTo("Explain OOP principles clearly with examples");
            assertThat(result.getMaxScore()).isEqualTo(10);
            assertThat(result.getKeywords()).containsExactly("encapsulation", "inheritance", "polymorphism", "abstraction");

            verify(questionRepository).existsById(QUESTION_ID);
            verify(rubricRepository).existsByQuestionId(QUESTION_ID);
            verify(rubricRepository).save(any(Rubric.class));
        }

        @Test
        @DisplayName("Should throw 404 when question not found")
        void shouldThrow404WhenQuestionNotFound() {
            // Given
            RubricRequest request = buildRubricRequest();
            when(questionRepository.existsById(QUESTION_ID)).thenReturn(false);

            // When & Then
            BaseException ex = catchThrowableOfType(
                    () -> rubricService.createRubric(QUESTION_ID, request),
                    NotFoundException.class);
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.QUE_001.getCode());

            verify(questionRepository).existsById(QUESTION_ID);
            verify(rubricRepository, never()).existsByQuestionId(anyString());
            verify(rubricRepository, never()).save(any(Rubric.class));
        }

        @Test
        @DisplayName("Should throw 409 when rubric already exists for question")
        void shouldThrow409WhenRubricAlreadyExists() {
            // Given
            RubricRequest request = buildRubricRequest();

            when(questionRepository.existsById(QUESTION_ID)).thenReturn(true);
            when(rubricRepository.existsByQuestionId(QUESTION_ID)).thenReturn(true);

            // When & Then
            BaseException ex = catchThrowableOfType(
                    () -> rubricService.createRubric(QUESTION_ID, request),
                    DuplicateException.class);
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.RUB_002.getCode());

            verify(questionRepository).existsById(QUESTION_ID);
            verify(rubricRepository).existsByQuestionId(QUESTION_ID);
            verify(rubricRepository, never()).save(any(Rubric.class));
        }
    }

    @Nested
    @DisplayName("getRubric()")
    class GetRubricTests {

        @Test
        @DisplayName("Should return rubric successfully")
        void shouldReturnRubricSuccessfully() {
            // Given
            Rubric rubric = buildRubric();
            RubricResponse expectedResponse = buildRubricResponse();

            when(questionRepository.existsById(QUESTION_ID)).thenReturn(true);
            when(rubricRepository.findByQuestionId(QUESTION_ID)).thenReturn(Optional.of(rubric));
            when(rubricMapper.toResponse(rubric)).thenReturn(expectedResponse);

            // When
            RubricResponse result = rubricService.getRubric(QUESTION_ID);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(RUBRIC_ID);
            assertThat(result.getQuestionId()).isEqualTo(QUESTION_ID);
            assertThat(result.getCriteria()).isEqualTo("Explain OOP principles clearly with examples");
            assertThat(result.getMaxScore()).isEqualTo(10);

            verify(questionRepository).existsById(QUESTION_ID);
            verify(rubricRepository).findByQuestionId(QUESTION_ID);
        }

        @Test
        @DisplayName("Should throw 404 when question not found")
        void shouldThrow404WhenQuestionNotFound() {
            // Given
            when(questionRepository.existsById(QUESTION_ID)).thenReturn(false);

            // When & Then
            BaseException ex = catchThrowableOfType(
                    () -> rubricService.getRubric(QUESTION_ID),
                    NotFoundException.class);
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.QUE_001.getCode());

            verify(questionRepository).existsById(QUESTION_ID);
            verify(rubricRepository, never()).findByQuestionId(anyString());
        }

        @Test
        @DisplayName("Should throw 404 when rubric not found for question")
        void shouldThrow404WhenRubricNotFound() {
            // Given
            when(questionRepository.existsById(QUESTION_ID)).thenReturn(true);
            when(rubricRepository.findByQuestionId(QUESTION_ID)).thenReturn(Optional.empty());

            // When & Then
            BaseException ex = catchThrowableOfType(
                    () -> rubricService.getRubric(QUESTION_ID),
                    NotFoundException.class);
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.RUB_001.getCode());

            verify(questionRepository).existsById(QUESTION_ID);
            verify(rubricRepository).findByQuestionId(QUESTION_ID);
        }
    }

    @Nested
    @DisplayName("updateRubric()")
    class UpdateRubricTests {

        @Test
        @DisplayName("Should update rubric successfully")
        void shouldUpdateRubricSuccessfully() {
            // Given
            RubricRequest request = buildRubricRequest();
            request.setCriteria("Updated criteria");
            request.setMaxScore(20);

            Rubric existingRubric = buildRubric();
            Rubric savedRubric = buildRubric();
            savedRubric.setCriteria("Updated criteria");
            savedRubric.setMaxScore(20);

            RubricResponse expectedResponse = RubricResponse.builder()
                    .id(RUBRIC_ID)
                    .questionId(QUESTION_ID)
                    .criteria("Updated criteria")
                    .keywords(List.of("encapsulation", "inheritance", "polymorphism", "abstraction"))
                    .maxScore(20)
                    .isActive(true)
                    .build();

            when(questionRepository.existsById(QUESTION_ID)).thenReturn(true);
            when(rubricRepository.findByQuestionId(QUESTION_ID)).thenReturn(Optional.of(existingRubric));
            when(rubricRepository.save(any(Rubric.class))).thenReturn(savedRubric);
            when(rubricMapper.toResponse(savedRubric)).thenReturn(expectedResponse);

            // When
            RubricResponse result = rubricService.updateRubric(QUESTION_ID, request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCriteria()).isEqualTo("Updated criteria");
            assertThat(result.getMaxScore()).isEqualTo(20);

            verify(questionRepository).existsById(QUESTION_ID);
            verify(rubricRepository).findByQuestionId(QUESTION_ID);
            verify(rubricMapper).updateEntity(existingRubric, request);
            verify(rubricRepository).save(existingRubric);
        }

        @Test
        @DisplayName("Should throw 404 when question not found")
        void shouldThrow404WhenQuestionNotFound() {
            // Given
            RubricRequest request = buildRubricRequest();
            when(questionRepository.existsById(QUESTION_ID)).thenReturn(false);

            // When & Then
            BaseException ex = catchThrowableOfType(
                    () -> rubricService.updateRubric(QUESTION_ID, request),
                    NotFoundException.class);
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.QUE_001.getCode());

            verify(questionRepository).existsById(QUESTION_ID);
            verify(rubricRepository, never()).findByQuestionId(anyString());
            verify(rubricRepository, never()).save(any(Rubric.class));
        }

        @Test
        @DisplayName("Should throw 404 when rubric not found for question")
        void shouldThrow404WhenRubricNotFound() {
            // Given
            RubricRequest request = buildRubricRequest();
            when(questionRepository.existsById(QUESTION_ID)).thenReturn(true);
            when(rubricRepository.findByQuestionId(QUESTION_ID)).thenReturn(Optional.empty());

            // When & Then
            BaseException ex = catchThrowableOfType(
                    () -> rubricService.updateRubric(QUESTION_ID, request),
                    NotFoundException.class);
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.RUB_001.getCode());

            verify(questionRepository).existsById(QUESTION_ID);
            verify(rubricRepository).findByQuestionId(QUESTION_ID);
            verify(rubricRepository, never()).save(any(Rubric.class));
        }
    }
}
