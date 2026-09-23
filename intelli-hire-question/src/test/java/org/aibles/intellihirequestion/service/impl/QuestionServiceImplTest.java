package org.aibles.intellihirequestion.service.impl;

import org.aibles.intellihirequestion.dto.AdminQuestionSearchRequest;
import org.aibles.intellihirequestion.dto.BulkImportError;
import org.aibles.intellihirequestion.dto.BulkImportRequest;
import org.aibles.intellihirequestion.dto.BulkImportResponse;
import org.aibles.intellihirequestion.dto.GenerateQuestionsFromResumeRequest;
import org.aibles.intellihirequestion.dto.GenerateQuestionsFromResumeResponse;
import org.aibles.intellihirequestion.dto.GeneratedQuestionItemResponse;
import org.aibles.intellihirequestion.dto.PageResponse;
import org.aibles.intellihirequestion.dto.QuestionRequest;
import org.aibles.intellihirequestion.dto.QuestionResponse;
import org.aibles.intellihirequestion.dto.QuestionSearchRequest;
import org.aibles.intellihirequestion.dto.RandomQuestionRequest;
import org.aibles.intellihirequestion.dto.TagResponse;
import org.aibles.intellihirequestion.dto.ai.AiGeneratedQuestion;
import org.aibles.intellihirequestion.dto.ai.AiGeneratedQuestionResult;
import org.aibles.intellihirequestion.dto.resume.ResumeAggregateData;
import org.aibles.intellihirequestion.entity.*;
import org.aibles.intellihirequestion.entity.enums.QuestionType;
import org.aibles.intellihirequestion.exception.BadRequestException;
import org.aibles.intellihirequestion.exception.ErrorCode;
import org.aibles.intellihirequestion.exception.InactiveResourceException;
import org.aibles.intellihirequestion.exception.NotFoundException;
import org.aibles.intellihirequestion.mapper.QuestionMapper;
import org.aibles.intellihirequestion.mapper.TagMapper;
import org.aibles.intellihirequestion.projection.QuestionDetailProjection;
import org.aibles.intellihirequestion.repository.*;
import org.aibles.intellihirequestion.service.ai.QuestionGenerationAiClient;
import org.aibles.intellihirequestion.service.client.ResumeAggregateClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionServiceImplTest {

    @Mock private QuestionRepository questionRepository;
    @Mock private TopicRepository topicRepository;
    @Mock private DifficultyLevelRepository difficultyLevelRepository;
    @Mock private TagRepository tagRepository;
    @Mock private QuestionTagRepository questionTagRepository;
    @Mock private RubricRepository rubricRepository;
    @Mock private QuestionMapper questionMapper;
    @Mock private TagMapper tagMapper;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ValueOperations<String, Object> valueOperations;
    @Mock private ResumeAggregateClient resumeAggregateClient;
    @Mock private QuestionGenerationAiClient questionGenerationAiClient;

    @InjectMocks
    private QuestionServiceImpl questionService;

    // ========== Test Data Constants ==========
    private static final String TOPIC_ID = "topic-uuid-1";
    private static final String TOPIC_NAME = "Backend Development";
    private static final String DIFFICULTY_ID = "difficulty-uuid-1";
    private static final String DIFFICULTY_NAME = "Medium";
    private static final int DIFFICULTY_WEIGHT = 3;
    private static final String TAG_1_ID = "tag-uuid-1";
    private static final String TAG_1_NAME = "java";
    private static final String TAG_2_ID = "tag-uuid-2";
    private static final String TAG_2_NAME = "data-structures";
    private static final String QUESTION_ID = "question-uuid-1";
    private static final String QUESTION_TEXT = "Explain the difference between ArrayList and LinkedList in Java";
    private static final String UPDATED_QUESTION_TEXT = "Updated question text";
    private static final String USER_ID = "42";
    private static final String USERNAME = "alice";

    // ========== Common Builders ==========

    private QuestionRequest buildCreateRequest() {
        QuestionRequest request = new QuestionRequest();
        request.setTopicId(TOPIC_ID);
        request.setDifficultyId(DIFFICULTY_ID);
        request.setQuestionText(QUESTION_TEXT);
        request.setQuestionType(QuestionType.TECHNICAL);
        request.setTagIds(List.of(TAG_1_ID, TAG_2_ID));
        request.setIsActive(true);
        return request;
    }

    private QuestionRequest buildUpdateRequest(List<String> tagIds) {
        QuestionRequest request = new QuestionRequest();
        request.setTopicId(TOPIC_ID);
        request.setDifficultyId(DIFFICULTY_ID);
        request.setQuestionText(UPDATED_QUESTION_TEXT);
        request.setQuestionType(QuestionType.TECHNICAL);
        request.setTagIds(tagIds);
        request.setIsActive(true);
        return request;
    }

    private Topic buildTopic() {
        Topic topic = new Topic();
        topic.setId(TOPIC_ID);
        topic.setName(TOPIC_NAME);
        topic.setIsActive(true);
        return topic;
    }

    private Topic buildInactiveTopic() {
        Topic topic = buildTopic();
        topic.setIsActive(false);
        return topic;
    }

    private DifficultyLevel buildDifficultyLevel() {
        DifficultyLevel difficulty = new DifficultyLevel();
        difficulty.setId(DIFFICULTY_ID);
        difficulty.setName(DIFFICULTY_NAME);
        difficulty.setWeight(DIFFICULTY_WEIGHT);
        return difficulty;
    }

    private Tag buildTag(String id, String name) {
        Tag tag = new Tag();
        tag.setId(id);
        tag.setName(name);
        return tag;
    }

    private List<Tag> buildTags() {
        return List.of(
                buildTag(TAG_1_ID, TAG_1_NAME),
                buildTag(TAG_2_ID, TAG_2_NAME)
        );
    }

    private Question buildQuestion() {
        Question question = new Question();
        question.setId(QUESTION_ID);
        question.setTopicId(TOPIC_ID);
        question.setDifficultyId(DIFFICULTY_ID);
        question.setQuestionText(QUESTION_TEXT);
        question.setQuestionType(QuestionType.TECHNICAL);
        question.setIsActive(true);
        return question;
    }

    private Question buildSavedQuestion() {
        return buildQuestion();
    }

    private Question buildQuestionWithIdAndText(String id, String text) {
        Question question = buildQuestion();
        question.setId(id);
        question.setQuestionText(text);
        return question;
    }

    private QuestionRequest buildBulkQuestionRequest(String topicId, String difficultyId, String text, List<String> tagIds) {
        QuestionRequest request = new QuestionRequest();
        request.setTopicId(topicId);
        request.setDifficultyId(difficultyId);
        request.setQuestionText(text);
        request.setQuestionType(QuestionType.TECHNICAL);
        request.setTagIds(tagIds);
        request.setIsActive(true);
        return request;
    }

    private BulkImportRequest buildBulkImportRequest(QuestionRequest... requests) {
        BulkImportRequest bulkImportRequest = new BulkImportRequest();
        bulkImportRequest.setQuestions(List.of(requests));
        return bulkImportRequest;
    }

    private Rubric buildRubric() {
        Rubric rubric = new Rubric();
        rubric.setId("rubric-uuid-1");
        rubric.setQuestionId(QUESTION_ID);
        rubric.setIsActive(true);
        return rubric;
    }

    private TagResponse buildTagResponse(String id, String name) {
        return TagResponse.builder()
                .id(id)
                .name(name)
                .build();
    }

    private List<TagResponse> buildTagResponses() {
        return List.of(
                buildTagResponse(TAG_1_ID, TAG_1_NAME),
                buildTagResponse(TAG_2_ID, TAG_2_NAME)
        );
    }

    private QuestionResponse buildQuestionResponse(
            String id,
            String questionText,
            List<TagResponse> tags
    ) {
        return QuestionResponse.builder()
                .id(id)
                .topicId(TOPIC_ID)
                .topicName(TOPIC_NAME)
                .difficultyId(DIFFICULTY_ID)
                .difficultyName(DIFFICULTY_NAME)
                .questionText(questionText)
                .questionType(QuestionType.TECHNICAL.name())
                .tags(tags)
                .isActive(true)
                .build();
    }

    // ========== Common Mock Setup Methods ==========

    private void mockValidTopicAndDifficultyActive() {
        when(topicRepository.findById(TOPIC_ID))
                .thenReturn(Optional.of(buildTopic()));
        when(difficultyLevelRepository.findById(DIFFICULTY_ID))
                .thenReturn(Optional.of(buildDifficultyLevel()));
    }

    private void mockValidTags(List<String> tagIds) {
        when(tagRepository.countByIdIn(tagIds)).thenReturn((long) tagIds.size());
        when(tagRepository.findByIdIn(tagIds)).thenReturn(buildTags());
    }

    // ========== Common Assertion Helpers ==========

    private void assertQuestionTagsMappings(
            List<QuestionTag> mappings,
            String questionId,
            List<String> expectedTagIds
    ) {
        assertThat(mappings).hasSize(expectedTagIds.size());
        assertThat(mappings)
                .extracting(QuestionTag::getQuestionId)
                .containsOnly(questionId);
        assertThat(mappings)
                .extracting(QuestionTag::getTagId)
                .containsExactlyInAnyOrder(expectedTagIds.toArray(new String[0]));
    }

    // =======================================================
    //                  CREATE() TESTS
    // =======================================================
    @Nested
    @DisplayName("create() - Success cases")
    class CreateSuccessTests {

        @Test
        @DisplayName("Should create question successfully with all valid data")
        void shouldCreateQuestionSuccessfully() {
            // Given
            QuestionRequest request = buildCreateRequest();
            Topic topic = buildTopic();
            DifficultyLevel difficulty = buildDifficultyLevel();
            List<Tag> tags = buildTags();
            Question questionEntity = new Question();
            Question savedQuestion = buildSavedQuestion();

            List<TagResponse> tagResponses = buildTagResponses();
            QuestionResponse expectedResponse =
                    buildQuestionResponse(QUESTION_ID, QUESTION_TEXT, tagResponses);

            when(topicRepository.findById(TOPIC_ID)).thenReturn(Optional.of(topic));
            when(difficultyLevelRepository.findById(DIFFICULTY_ID)).thenReturn(Optional.of(difficulty));
            when(tagRepository.countByIdIn(request.getTagIds()))
                    .thenReturn((long) request.getTagIds().size());
            when(tagRepository.findByIdIn(request.getTagIds())).thenReturn(tags);
            when(questionMapper.toEntity(request)).thenReturn(questionEntity);
            when(questionRepository.save(questionEntity)).thenReturn(savedQuestion);
            when(questionMapper.toResponse(
                    eq(savedQuestion),
                    eq(TOPIC_NAME),
                    eq(DIFFICULTY_NAME),
                    anyList()
            )).thenReturn(expectedResponse);

            // When
            QuestionResponse actualResponse = questionService.create(request, USER_ID, USERNAME);

            // Then
            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.getId()).isEqualTo(QUESTION_ID);
            assertThat(actualResponse.getTopicId()).isEqualTo(TOPIC_ID);
            assertThat(actualResponse.getDifficultyId()).isEqualTo(DIFFICULTY_ID);
            assertThat(actualResponse.getQuestionText()).isEqualTo(QUESTION_TEXT);
            assertThat(actualResponse.getQuestionType())
                    .isEqualTo(QuestionType.TECHNICAL.name());
            assertThat(actualResponse.getTags()).hasSize(2);
            assertThat(actualResponse.getIsActive()).isTrue();

            verify(topicRepository).findById(TOPIC_ID);
            verify(difficultyLevelRepository).findById(DIFFICULTY_ID);
            verify(tagRepository).countByIdIn(request.getTagIds());
            verify(tagRepository).findByIdIn(request.getTagIds());
            verify(questionRepository).save(questionEntity);
            assertThat(questionEntity.getStatus()).isEqualTo("PENDING");
            assertThat(questionEntity.getSubmittedById()).isEqualTo(Long.valueOf(USER_ID));
            assertThat(questionEntity.getSubmittedBy()).isEqualTo(USERNAME);
            assertThat(questionEntity.getCreatedAt()).isNotNull();

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<QuestionTag>> captor =
                    (ArgumentCaptor<List<QuestionTag>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(List.class);

            verify(questionTagRepository).saveAll(captor.capture());
            assertQuestionTagsMappings(captor.getValue(), QUESTION_ID, request.getTagIds());
        }

        @Test
        @DisplayName("Should create question without tags and ignore non-numeric userId")
        void shouldCreateQuestionWithoutTags() {
            QuestionRequest request = buildCreateRequest();
            request.setTagIds(List.of());

            Question questionEntity = new Question();
            Question savedQuestion = buildSavedQuestion();
            QuestionResponse expectedResponse = buildQuestionResponse(QUESTION_ID, QUESTION_TEXT, List.of());

            when(topicRepository.findById(TOPIC_ID)).thenReturn(Optional.of(buildTopic()));
            when(difficultyLevelRepository.findById(DIFFICULTY_ID)).thenReturn(Optional.of(buildDifficultyLevel()));
            when(questionMapper.toEntity(request)).thenReturn(questionEntity);
            when(questionRepository.save(questionEntity)).thenReturn(savedQuestion);
            when(questionMapper.toResponse(eq(savedQuestion), eq(TOPIC_NAME), eq(DIFFICULTY_NAME), anyList()))
                    .thenReturn(expectedResponse);

            QuestionResponse actualResponse = questionService.create(request, "not-a-number", null);

            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.getTags()).isEmpty();
            assertThat(questionEntity.getSubmittedById()).isNull();
            assertThat(questionEntity.getSubmittedBy()).isNull();
            verifyNoInteractions(tagRepository);
            verify(questionTagRepository, never()).saveAll(anyList());
        }
    }

    @Nested
    @DisplayName("create() - Failure cases")
    class CreateFailureTests {

        @Test
        @DisplayName("Should throw NotFoundException with TOP_001 when topic not found")
        void shouldThrowExceptionWhenTopicNotFound() {
            // Given
            QuestionRequest request = buildCreateRequest();
            when(topicRepository.findById(TOPIC_ID)).thenReturn(Optional.empty());

            // When
            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> questionService.create(request, USER_ID, USERNAME)
            );

            // Then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.TOP_001.getCode());
            verify(topicRepository).findById(TOPIC_ID);
            verifyNoInteractions(difficultyLevelRepository, tagRepository,
                    questionRepository, questionTagRepository);
        }

        @Test
        @DisplayName("Should throw NotFoundException with DIF_001 when difficulty not found")
        void shouldThrowExceptionWhenDifficultyNotFound() {
            // Given
            QuestionRequest request = buildCreateRequest();
            when(topicRepository.findById(TOPIC_ID)).thenReturn(Optional.of(buildTopic()));
            when(difficultyLevelRepository.findById(DIFFICULTY_ID))
                    .thenReturn(Optional.empty());

            // When
            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> questionService.create(request, USER_ID, USERNAME)
            );

            // Then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DIF_001.getCode());
            verify(topicRepository).findById(TOPIC_ID);
            verify(difficultyLevelRepository).findById(DIFFICULTY_ID);
            verifyNoInteractions(tagRepository, questionRepository, questionTagRepository);
        }

        @Test
        @DisplayName("Should throw InactiveResourceException with TAG_004 when some tags are locked or missing")
        void shouldThrowExceptionWhenTagsNotFound() {
            // Given
            QuestionRequest request = buildCreateRequest();
            when(topicRepository.findById(TOPIC_ID)).thenReturn(Optional.of(buildTopic()));
            when(difficultyLevelRepository.findById(DIFFICULTY_ID))
                    .thenReturn(Optional.of(buildDifficultyLevel()));
            when(tagRepository.countByIdIn(request.getTagIds())).thenReturn(1L);

            // When
            InactiveResourceException exception = assertThrows(
                    InactiveResourceException.class,
                    () -> questionService.create(request, USER_ID, USERNAME)
            );

            // Then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.TAG_004.getCode());
            verify(topicRepository).findById(TOPIC_ID);
            verify(difficultyLevelRepository).findById(DIFFICULTY_ID);
            verify(tagRepository).countByIdIn(request.getTagIds());
            verify(tagRepository, never()).findByIdIn(anyList());
            verifyNoInteractions(questionRepository, questionTagRepository);
        }
    }

    // ========== Test Delete ==========
    @Nested
    @DisplayName("delete() - Success cases")
    class DeleteSuccessTests {

        @Test
        @DisplayName("Should soft delete question successfully")
        void shouldSoftDeleteQuestionSuccessfully() {
            // Given
            Question question = buildQuestion();
            when(questionRepository.findById(QUESTION_ID)).thenReturn(Optional.of(question));
            when(rubricRepository.findByQuestionId(QUESTION_ID)).thenReturn(Optional.empty());

            // When
            questionService.delete(QUESTION_ID);

            // Then
            assertThat(question.getIsActive()).isFalse();
            verify(questionRepository).save(question);
        }

        @Test
        @DisplayName("Should soft delete question and rubric when rubric exists")
        void shouldSoftDeleteQuestionAndRubric() {
            // Given
            Question question = buildQuestion();
            Rubric rubric = buildRubric();
            when(questionRepository.findById(QUESTION_ID)).thenReturn(Optional.of(question));
            when(rubricRepository.findByQuestionId(QUESTION_ID)).thenReturn(Optional.of(rubric));

            // When
            questionService.delete(QUESTION_ID);

            // Then
            assertThat(question.getIsActive()).isFalse();
            assertThat(rubric.getIsActive()).isFalse();
            verify(questionRepository).save(question);
            verify(rubricRepository).save(rubric);
        }
    }

    @Nested
    @DisplayName("delete() - Failure cases")
    class DeleteFailureTests {

        @Test
        @DisplayName("Should throw NotFoundException when question not found")
        void shouldThrowExceptionWhenQuestionNotFound() {
            // Given
            when(questionRepository.findById(QUESTION_ID)).thenReturn(Optional.empty());

            // When
            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> questionService.delete(QUESTION_ID)
            );

            // Then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.QUE_001.getCode());
            verify(questionRepository, never()).save(any());
            verifyNoInteractions(rubricRepository);
        }
    }

        // =======================================================
    //                  UPDATE() TESTS
    // =======================================================
    @Nested
    @DisplayName("update() - Success cases")
    class UpdateSuccessTests {

        @Test
        @DisplayName("Should update question successfully and replace tags")
        void shouldUpdateQuestionSuccessfully() {
            // Given
            List<String> tagIds = List.of(TAG_1_ID, TAG_2_ID);
            QuestionRequest request = buildUpdateRequest(tagIds);

            Question existing = buildQuestion();
            List<TagResponse> tagResponses = buildTagResponses();
            QuestionResponse expectedResponse =
                    buildQuestionResponse(QUESTION_ID, UPDATED_QUESTION_TEXT, tagResponses);

            when(questionRepository.findById(QUESTION_ID))
                    .thenReturn(Optional.of(existing));
            mockValidTopicAndDifficultyActive();
            mockValidTags(tagIds);
            when(questionRepository.save(any(Question.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            when(questionMapper.toResponse(
                    any(Question.class),
                    eq(TOPIC_NAME),
                    eq(DIFFICULTY_NAME),
                    anyList()
            )).thenReturn(expectedResponse);

            // When
            QuestionResponse actual = questionService.update(QUESTION_ID, request);

            // Then
            assertThat(actual).isNotNull();
            assertThat(actual.getId()).isEqualTo(QUESTION_ID);
            assertThat(actual.getQuestionText()).isEqualTo(UPDATED_QUESTION_TEXT);
            assertThat(actual.getTags()).hasSize(2);

            ArgumentCaptor<Question> questionCaptor =
                    ArgumentCaptor.forClass(Question.class);
            verify(questionRepository).save(questionCaptor.capture());

            Question savedEntity = questionCaptor.getValue();
            assertThat(savedEntity.getTopicId()).isEqualTo(TOPIC_ID);
            assertThat(savedEntity.getDifficultyId()).isEqualTo(DIFFICULTY_ID);
            assertThat(savedEntity.getQuestionText()).isEqualTo(UPDATED_QUESTION_TEXT);
            assertThat(savedEntity.getQuestionType()).isEqualTo(QuestionType.TECHNICAL);
            assertThat(savedEntity.getIsActive()).isTrue();
            assertThat(savedEntity.getLastUpdatedAt()).isNotNull();

            verify(questionTagRepository).deleteByQuestionId(QUESTION_ID);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<QuestionTag>> qtCaptor =
                    (ArgumentCaptor<List<QuestionTag>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(List.class);
            verify(questionTagRepository).saveAll(qtCaptor.capture());

            assertQuestionTagsMappings(qtCaptor.getValue(), QUESTION_ID, tagIds);

            verify(questionRepository).findById(QUESTION_ID);
            verify(topicRepository).findById(TOPIC_ID);
            verify(difficultyLevelRepository).findById(DIFFICULTY_ID);
            verify(tagRepository).countByIdIn(tagIds);
            verify(tagRepository).findByIdIn(tagIds);
        }

        @Test
        @DisplayName("Should update question successfully with empty tags (clear all tags)")
        void shouldUpdateQuestionWithEmptyTags() {
            // Given
            QuestionRequest request = buildUpdateRequest(List.of());
            Question existing = buildQuestion();

            QuestionResponse expectedResponse =
                    buildQuestionResponse(QUESTION_ID, UPDATED_QUESTION_TEXT, List.of());

            when(questionRepository.findById(QUESTION_ID))
                    .thenReturn(Optional.of(existing));
            mockValidTopicAndDifficultyActive();
            when(questionRepository.save(any(Question.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            when(questionMapper.toResponse(
                    any(Question.class),
                    eq(TOPIC_NAME),
                    eq(DIFFICULTY_NAME),
                    anyList()
            )).thenReturn(expectedResponse);

            // When
            QuestionResponse actual = questionService.update(QUESTION_ID, request);

            // Then
            assertThat(actual).isNotNull();
            assertThat(actual.getTags()).isEmpty();

            verify(questionTagRepository).deleteByQuestionId(QUESTION_ID);
            verify(questionTagRepository, never()).saveAll(anyList());
            verify(tagRepository, never()).countByIdIn(anyList());
            verify(tagRepository, never()).findByIdIn(anyList());
        }

        @Test
        @DisplayName("Should update successfully when tagIds is null (clear all tags)")
        void shouldUpdateQuestionWithNullTags() {
            // Given
            QuestionRequest request = buildUpdateRequest(null);
            Question existing = buildQuestion();

            QuestionResponse expectedResponse =
                    buildQuestionResponse(QUESTION_ID, UPDATED_QUESTION_TEXT, List.of());

            when(questionRepository.findById(QUESTION_ID))
                    .thenReturn(Optional.of(existing));
            mockValidTopicAndDifficultyActive();
            when(questionRepository.save(any(Question.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            when(questionMapper.toResponse(
                    any(Question.class),
                    eq(TOPIC_NAME),
                    eq(DIFFICULTY_NAME),
                    anyList()
            )).thenReturn(expectedResponse);

            // When
            QuestionResponse actual = questionService.update(QUESTION_ID, request);

            // Then
            assertThat(actual).isNotNull();
            assertThat(actual.getTags()).isEmpty();
            verify(questionTagRepository).deleteByQuestionId(QUESTION_ID);
            verify(questionTagRepository, never()).saveAll(anyList());
            verifyNoInteractions(tagRepository);
        }
    }

    @Nested
    @DisplayName("update() - Failure cases")
    class UpdateFailureTests {

        @Test
        @DisplayName("Should throw NotFoundException with QUE_001 when question not found")
        void shouldThrowWhenQuestionNotFound() {
            // Given
            QuestionRequest request = buildUpdateRequest(List.of(TAG_1_ID));
            when(questionRepository.findById(QUESTION_ID)).thenReturn(Optional.empty());

            // When
            NotFoundException ex = assertThrows(
                    NotFoundException.class,
                    () -> questionService.update(QUESTION_ID, request)
            );

            // Then
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.QUE_001.getCode());
            verify(questionRepository).findById(QUESTION_ID);
            verifyNoInteractions(topicRepository, difficultyLevelRepository,
                    tagRepository, questionTagRepository);
        }

        @Test
        @DisplayName("Should throw NotFoundException with TOP_001 when topic not found")
        void shouldThrowWhenTopicNotFound() {
            // Given
            QuestionRequest request = buildUpdateRequest(List.of(TAG_1_ID));
            when(questionRepository.findById(QUESTION_ID))
                    .thenReturn(Optional.of(buildQuestion()));
            when(topicRepository.findById(TOPIC_ID))
                    .thenReturn(Optional.empty());

            // When
            NotFoundException ex = assertThrows(
                    NotFoundException.class,
                    () -> questionService.update(QUESTION_ID, request)
            );

            // Then
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.TOP_001.getCode());
            verify(topicRepository).findById(TOPIC_ID);
            verifyNoInteractions(difficultyLevelRepository, tagRepository, questionTagRepository);
            verify(questionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw InactiveResourceException with TOP_004 when topic is inactive")
        void shouldThrowWhenTopicInactive() {
            // Given
            QuestionRequest request = buildUpdateRequest(List.of(TAG_1_ID));
            when(questionRepository.findById(QUESTION_ID))
                    .thenReturn(Optional.of(buildQuestion()));
            when(topicRepository.findById(TOPIC_ID))
                    .thenReturn(Optional.of(buildInactiveTopic()));

            // When
            InactiveResourceException ex = assertThrows(
                    InactiveResourceException.class,
                    () -> questionService.update(QUESTION_ID, request)
            );

            // Then
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.TOP_004.getCode());
            verify(topicRepository).findById(TOPIC_ID);
            verifyNoInteractions(difficultyLevelRepository, tagRepository, questionTagRepository);
            verify(questionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw NotFoundException with DIF_001 when difficulty not found")
        void shouldThrowWhenDifficultyNotFound() {
            // Given
            QuestionRequest request = buildUpdateRequest(List.of(TAG_1_ID));
            when(questionRepository.findById(QUESTION_ID))
                    .thenReturn(Optional.of(buildQuestion()));
            when(topicRepository.findById(TOPIC_ID))
                    .thenReturn(Optional.of(buildTopic()));
            when(difficultyLevelRepository.findById(DIFFICULTY_ID))
                    .thenReturn(Optional.empty());

            // When
            NotFoundException ex = assertThrows(
                    NotFoundException.class,
                    () -> questionService.update(QUESTION_ID, request)
            );

            // Then
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.DIF_001.getCode());
            verify(difficultyLevelRepository).findById(DIFFICULTY_ID);
            verifyNoInteractions(tagRepository, questionTagRepository);
            verify(questionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw InactiveResourceException with TAG_004 when some tags are locked or missing")
        void shouldThrowWhenTagNotFound() {
            // Given
            List<String> tagIds = List.of(TAG_1_ID, TAG_2_ID);
            QuestionRequest request = buildUpdateRequest(tagIds);

            when(questionRepository.findById(QUESTION_ID))
                    .thenReturn(Optional.of(buildQuestion()));
            mockValidTopicAndDifficultyActive();
            when(tagRepository.countByIdIn(tagIds)).thenReturn(1L);

            // When
            InactiveResourceException ex = assertThrows(
                    InactiveResourceException.class,
                    () -> questionService.update(QUESTION_ID, request)
            );

            // Then
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.TAG_004.getCode());
            verify(tagRepository).countByIdIn(tagIds);
            verify(tagRepository, never()).findByIdIn(anyList());
            verify(questionRepository, never()).save(any());
            verifyNoInteractions(questionTagRepository);
        }
    }

    @Nested
    @DisplayName("getById()")
    class GetByIdTests {

        @Test
        @DisplayName("Should return question response with all nested data when question found")
        void shouldReturnQuestionResponse_whenQuestionFound() {
            // Given
            String questionId = "q-123";
            QuestionDetailProjection projection = createProjection(questionId, "Java Core", "Mid");
            Tag tag = createTag("tag-123", "collections");

            when(questionRepository.findByIdProjection(questionId)).thenReturn(Optional.of(projection));
            when(tagRepository.findByQuestionId(questionId)).thenReturn(List.of(tag));

            // When
            QuestionResponse response = questionService.getById(questionId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(questionId);
            assertThat(response.getTopicId()).isEqualTo("t-123");
            assertThat(response.getTopicName()).isEqualTo("Java Core");
            assertThat(response.getDifficultyId()).isEqualTo("d-123");
            assertThat(response.getDifficultyName()).isEqualTo("Mid");
            assertThat(response.getQuestionText()).isEqualTo("Test question");
            assertThat(response.getQuestionType()).isEqualTo(QuestionType.TECHNICAL.name());
            assertThat(response.getTags()).hasSize(1);
            assertThat(response.getTags().get(0).getName()).isEqualTo("collections");
            assertThat(response.getIsActive()).isTrue();

            verify(questionRepository).findByIdProjection(questionId);
            verify(tagRepository).findByQuestionId(questionId);
        }

        @Test
        @DisplayName("Should return question response with empty tags when no tags")
        void shouldReturnQuestionResponse_withEmptyTags() {
            // Given
            String questionId = "q-123";
            QuestionDetailProjection projection = createProjection(questionId, "Java Core", "Mid");

            when(questionRepository.findByIdProjection(questionId)).thenReturn(Optional.of(projection));
            when(tagRepository.findByQuestionId(questionId)).thenReturn(List.of());

            // When
            QuestionResponse response = questionService.getById(questionId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getTags()).isEmpty();
        }

        @Test
        @DisplayName("Should return question response with null names when topic/difficulty null")
        void shouldReturnQuestionResponse_withNullNames() {
            // Given
            String questionId = "q-123";
            QuestionDetailProjection projection = createProjection(questionId, null, null);

            when(questionRepository.findByIdProjection(questionId)).thenReturn(Optional.of(projection));
            when(tagRepository.findByQuestionId(questionId)).thenReturn(List.of());

            // When
            QuestionResponse response = questionService.getById(questionId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getTopicName()).isNull();
            assertThat(response.getDifficultyName()).isNull();
        }

        @Test
        @DisplayName("Should throw NotFoundException when question not found")
        void shouldThrowNotFoundException_whenQuestionNotFound() {
            // Given
            String questionId = "non-existent";
            when(questionRepository.findByIdProjection(questionId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> questionService.getById(questionId))
                    .isInstanceOf(NotFoundException.class);

            verify(questionRepository).findByIdProjection(questionId);
            verify(tagRepository, never()).findByQuestionId(anyString());
        }
    }

    private QuestionDetailProjection createProjection(String id, String topicName, String difficultyName) {
        LocalDateTime now = LocalDateTime.now();
        return new QuestionDetailProjection() {
            @Override
            public String getId() { return id; }
            @Override
            public String getTopicId() { return "t-123"; }
            @Override
            public String getTopicName() { return topicName; }
            @Override
            public String getDifficultyId() { return "d-123"; }
            @Override
            public String getDifficultyName() { return difficultyName; }
            @Override
            public String getQuestionText() { return "Test question"; }
            @Override
            public String getQuestionType() { return "TECHNICAL"; }
            @Override
            public Boolean getIsActive() { return true; }
            @Override
            public String getStatus() { return "PENDING"; }
            @Override
            public String getSubmittedBy() { return USERNAME; }
            @Override
            public Long getSubmittedById() { return Long.valueOf(USER_ID); }
            @Override
            public String getRejectionReason() { return null; }
            @Override
            public LocalDateTime getCreatedAt() { return now; }
            @Override
            public LocalDateTime getLastUpdatedAt() { return now; }
        };
    }

    private Tag createTag(String id, String name) {
        Tag tag = new Tag();
        tag.setId(id);
        tag.setName(name);
        return tag;
    }

    // ========== Random Question Helpers ==========

    private RandomQuestionRequest buildRandomRequest(Integer count) {
        return RandomQuestionRequest.builder()
                .topicId(TOPIC_ID)
                .difficultyId(DIFFICULTY_ID)
                .count(count)
                .build();
    }

    private List<String> buildQuestionIds(int count) {
        List<String> ids = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            ids.add("q-" + i);
        }
        return ids;
    }

    private List<Question> buildQuestionsFromIds(List<String> ids) {
        return ids.stream().map(id -> {
            Question q = new Question();
            q.setId(id);
            q.setTopicId(TOPIC_ID);
            q.setDifficultyId(DIFFICULTY_ID);
            q.setQuestionText("Question " + id);
            q.setQuestionType(QuestionType.TECHNICAL);
            q.setIsActive(true);
            return q;
        }).toList();
    }

    private void mockRedisCacheMiss() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
    }

    private void mockRedisCacheHit(List<String> cachedIds) {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(cachedIds);
    }

    private void mockFindAllByIdDynamic() {
        when(questionRepository.findAllById(anyList())).thenAnswer(inv -> {
            List<String> requestedIds = inv.getArgument(0);
            return buildQuestionsFromIds(requestedIds);
        });
    }

    private void mockRandomResponseMapping() {
        when(topicRepository.findAllById(anySet())).thenReturn(List.of(buildTopic()));
        when(difficultyLevelRepository.findAllById(anySet())).thenReturn(List.of(buildDifficultyLevel()));
        when(questionTagRepository.findByQuestionIdIn(anyList())).thenReturn(List.of());
        when(tagRepository.findAllById(anySet())).thenReturn(List.of());
        when(tagMapper.toResponseList(anyList())).thenReturn(List.of());
        when(questionMapper.toResponse(any(Question.class), anyString(), anyString(), anyList()))
                .thenAnswer(inv -> {
                    Question q = inv.getArgument(0);
                    return QuestionResponse.builder()
                            .id(q.getId())
                            .topicId(q.getTopicId())
                            .topicName(inv.getArgument(1))
                            .difficultyId(q.getDifficultyId())
                            .difficultyName(inv.getArgument(2))
                            .questionText(q.getQuestionText())
                            .questionType(QuestionType.TECHNICAL.name())
                            .isActive(true)
                            .tags(List.of())
                            .build();
                });
    }

    // =======================================================
    //        getRandomQuestions() TESTS
    // =======================================================
    @Nested
    @DisplayName("getRandomQuestions() - Success cases")
    class GetRandomQuestionsSuccessTests {

        @Test
        @DisplayName("Should return random questions when all filters provided and cache miss")
        void shouldReturnRandomQuestions_whenAllFiltersProvided() {
            // Given
            List<String> questionIds = buildQuestionIds(5);
            RandomQuestionRequest request = buildRandomRequest(3);

            when(topicRepository.findById(TOPIC_ID)).thenReturn(Optional.of(buildTopic()));
            when(difficultyLevelRepository.findById(DIFFICULTY_ID)).thenReturn(Optional.of(buildDifficultyLevel()));
            mockRedisCacheMiss();
            when(questionRepository.findIdsByFilters(eq(TOPIC_ID), eq(DIFFICULTY_ID), isNull()))
                    .thenReturn(new ArrayList<>(questionIds));
            mockFindAllByIdDynamic();
            mockRandomResponseMapping();

            // When
            List<QuestionResponse> result = questionService.getRandomQuestions(request);

            // Then
            assertThat(result).hasSize(3);
            verify(topicRepository).findById(TOPIC_ID);
            verify(difficultyLevelRepository).findById(DIFFICULTY_ID);
            verify(questionRepository).findIdsByFilters(eq(TOPIC_ID), eq(DIFFICULTY_ID), isNull());
            verify(valueOperations).set(anyString(), anyList(), eq(5L), any());
        }

        @Test
        @DisplayName("Should return random questions when no filters provided (only count)")
        void shouldReturnRandomQuestions_whenNoFiltersProvided() {
            // Given
            RandomQuestionRequest request = RandomQuestionRequest.builder().count(3).build();
            List<String> questionIds = buildQuestionIds(5);

            mockRedisCacheMiss();
            when(questionRepository.findIdsByFilters(isNull(), isNull(), isNull()))
                    .thenReturn(new ArrayList<>(questionIds));
            mockFindAllByIdDynamic();
            mockRandomResponseMapping();

            // When
            List<QuestionResponse> result = questionService.getRandomQuestions(request);

            // Then
            assertThat(result).hasSize(3);
            verify(topicRepository, never()).findById(anyString());
            verify(difficultyLevelRepository, never()).findById(anyString());
        }

        @Test
        @DisplayName("Should return random questions from cache when cache hit")
        void shouldReturnRandomQuestions_whenCacheHit() {
            // Given
            List<String> questionIds = buildQuestionIds(5);
            RandomQuestionRequest request = buildRandomRequest(3);

            when(topicRepository.findById(TOPIC_ID)).thenReturn(Optional.of(buildTopic()));
            when(difficultyLevelRepository.findById(DIFFICULTY_ID)).thenReturn(Optional.of(buildDifficultyLevel()));
            mockRedisCacheHit(new ArrayList<>(questionIds));
            mockFindAllByIdDynamic();
            mockRandomResponseMapping();

            // When
            List<QuestionResponse> result = questionService.getRandomQuestions(request);

            // Then
            assertThat(result).hasSize(3);
            verify(questionRepository, never()).findIdsByFilters(any(), any(), any());
            verify(valueOperations, never()).set(anyString(), anyList(), anyLong(), any());
        }

        @Test
        @DisplayName("Should exclude specified IDs from result")
        void shouldExcludeIds_whenExcludeIdsProvided() {
            // Given
            List<String> allIds = buildQuestionIds(5);
            RandomQuestionRequest request = RandomQuestionRequest.builder()
                    .topicId(TOPIC_ID).difficultyId(DIFFICULTY_ID)
                    .count(2).excludeIds(List.of("q-1", "q-2")).build();

            when(topicRepository.findById(TOPIC_ID)).thenReturn(Optional.of(buildTopic()));
            when(difficultyLevelRepository.findById(DIFFICULTY_ID)).thenReturn(Optional.of(buildDifficultyLevel()));
            mockRedisCacheMiss();
            when(questionRepository.findIdsByFilters(eq(TOPIC_ID), eq(DIFFICULTY_ID), isNull()))
                    .thenReturn(new ArrayList<>(allIds));
            mockFindAllByIdDynamic();
            mockRandomResponseMapping();

            // When
            List<QuestionResponse> result = questionService.getRandomQuestions(request);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(QuestionResponse::getId)
                    .doesNotContain("q-1", "q-2");
        }

        @Test
        @DisplayName("Should use findIdsByFiltersWithTags when tagIds provided")
        void shouldReturnRandomQuestions_withTagFilter() {
            // Given
            List<String> tagIds = List.of(TAG_1_ID, TAG_2_ID);
            RandomQuestionRequest request = RandomQuestionRequest.builder()
                    .topicId(TOPIC_ID).difficultyId(DIFFICULTY_ID)
                    .count(2).tagIds(tagIds).build();
            List<String> questionIds = buildQuestionIds(5);

            when(topicRepository.findById(TOPIC_ID)).thenReturn(Optional.of(buildTopic()));
            when(difficultyLevelRepository.findById(DIFFICULTY_ID)).thenReturn(Optional.of(buildDifficultyLevel()));
            mockRedisCacheMiss();
            when(questionRepository.findIdsByFiltersWithTags(eq(TOPIC_ID), eq(DIFFICULTY_ID), isNull(), eq(tagIds)))
                    .thenReturn(new ArrayList<>(questionIds));
            mockFindAllByIdDynamic();
            mockRandomResponseMapping();

            // When
            List<QuestionResponse> result = questionService.getRandomQuestions(request);

            // Then
            assertThat(result).hasSize(2);
            verify(questionRepository).findIdsByFiltersWithTags(eq(TOPIC_ID), eq(DIFFICULTY_ID), isNull(), eq(tagIds));
            verify(questionRepository, never()).findIdsByFilters(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("getRandomQuestions() - Failure cases")
    class GetRandomQuestionsFailureTests {

        @Test
        @DisplayName("Should throw NotFoundException with TOP_001 when topic not found")
        void shouldThrowNotFoundException_whenTopicNotFound() {
            // Given
            RandomQuestionRequest request = buildRandomRequest(3);
            when(topicRepository.findById(TOPIC_ID)).thenReturn(Optional.empty());

            // When & Then
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> questionService.getRandomQuestions(request));
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.TOP_001.getCode());
        }

        @Test
        @DisplayName("Should throw NotFoundException with DIF_001 when difficulty not found")
        void shouldThrowNotFoundException_whenDifficultyNotFound() {
            // Given
            RandomQuestionRequest request = buildRandomRequest(3);
            when(topicRepository.findById(TOPIC_ID)).thenReturn(Optional.of(buildTopic()));
            when(difficultyLevelRepository.findById(DIFFICULTY_ID)).thenReturn(Optional.empty());

            // When & Then
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> questionService.getRandomQuestions(request));
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.DIF_001.getCode());
        }

        @Test
        @DisplayName("Should throw BadRequestException with QUE_003 when type is invalid")
        void shouldThrowBadRequestException_whenInvalidType() {
            // Given
            RandomQuestionRequest request = RandomQuestionRequest.builder()
                    .count(3).type("INVALID_TYPE").build();

            // When & Then
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> questionService.getRandomQuestions(request));
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.QUE_003.getCode());
        }

        @Test
        @DisplayName("Should throw NotFoundException with QUE_005 when not enough questions")
        void shouldThrowNotFoundException_whenNotEnoughQuestions() {
            // Given
            RandomQuestionRequest request = buildRandomRequest(10);

            when(topicRepository.findById(TOPIC_ID)).thenReturn(Optional.of(buildTopic()));
            when(difficultyLevelRepository.findById(DIFFICULTY_ID)).thenReturn(Optional.of(buildDifficultyLevel()));
            mockRedisCacheMiss();
            when(questionRepository.findIdsByFilters(eq(TOPIC_ID), eq(DIFFICULTY_ID), isNull()))
                    .thenReturn(new ArrayList<>(buildQuestionIds(3)));

            // When & Then
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> questionService.getRandomQuestions(request));
            assertThat(ex.getErrorCode()).isEqualTo("QUE_005");
        }

        @Test
        @DisplayName("Should handle Redis cache failure gracefully and fallback to DB")
        void shouldHandleRedisCacheFailureGracefully() {
            // Given
            List<String> questionIds = buildQuestionIds(5);
            RandomQuestionRequest request = buildRandomRequest(3);

            when(topicRepository.findById(TOPIC_ID)).thenReturn(Optional.of(buildTopic()));
            when(difficultyLevelRepository.findById(DIFFICULTY_ID)).thenReturn(Optional.of(buildDifficultyLevel()));
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(anyString())).thenThrow(new RuntimeException("Redis connection refused"));
            when(questionRepository.findIdsByFilters(eq(TOPIC_ID), eq(DIFFICULTY_ID), isNull()))
                    .thenReturn(new ArrayList<>(questionIds));
            mockFindAllByIdDynamic();
            mockRandomResponseMapping();

            // When
            List<QuestionResponse> result = questionService.getRandomQuestions(request);

            // Then
            assertThat(result).hasSize(3);
            verify(questionRepository).findIdsByFilters(eq(TOPIC_ID), eq(DIFFICULTY_ID), isNull());
        }
    }

    // ========== Search Question Tests ==========
    @Nested
    @DisplayName("searchQuestion()")
    class SearchQuestionTests {

        private QuestionResponse buildQuestionResponse() {
            return QuestionResponse.builder()
                    .id(QUESTION_ID)
                    .topicId(TOPIC_ID)
                    .topicName(TOPIC_NAME)
                    .difficultyId(DIFFICULTY_ID)
                    .difficultyName(DIFFICULTY_NAME)
                    .questionText(QUESTION_TEXT)
                    .questionType("TECHNICAL")
                    .tags(List.of(new TagResponse(TAG_1_ID, TAG_1_NAME)))
                    .isActive(true)
                    .build();
        }

        private void mockSearchDependencies(Page<Question> questionPage) {
            when(questionRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(questionPage);
            when(topicRepository.findAllById(anySet()))
                    .thenReturn(List.of(buildTopic()));
            when(difficultyLevelRepository.findAllById(anySet()))
                    .thenReturn(List.of(buildDifficultyLevel()));
            when(questionTagRepository.findByQuestionIdIn(anyList()))
                    .thenReturn(List.of());
            when(tagRepository.findAllById(anySet()))
                    .thenReturn(List.of());
            when(tagMapper.toResponseList(anyList()))
                    .thenReturn(List.of(new TagResponse(TAG_1_ID, TAG_1_NAME)));
            when(questionMapper.toResponse(any(Question.class), anyString(), anyString(), anyList()))
                    .thenReturn(buildQuestionResponse());
        }

        @Test
        @DisplayName("Should return filtered results when searching with topicId filter")
        void searchQuestion_withTopicIdFilter_shouldReturnFilteredResults() {
            // Given
            QuestionSearchRequest request = new QuestionSearchRequest();
            request.setTopicId(TOPIC_ID);
            Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));

            Question question = buildQuestion();
            Page<Question> questionPage = new PageImpl<>(List.of(question), pageable, 1);
            mockSearchDependencies(questionPage);

            // When
            PageResponse<QuestionResponse> result = questionService.searchQuestion(request, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getTopicId()).isEqualTo(TOPIC_ID);
            verify(questionRepository).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("Should combine multiple filters with AND logic")
        void searchQuestion_withMultipleFilters_shouldCombineWithAnd() {
            // Given
            QuestionSearchRequest request = new QuestionSearchRequest();
            request.setTopicId(TOPIC_ID);
            request.setDifficultyId(DIFFICULTY_ID);
            request.setType("TECHNICAL");
            request.setIsActive(true);
            Pageable pageable = PageRequest.of(0, 10);

            Question question = buildQuestion();
            Page<Question> questionPage = new PageImpl<>(List.of(question), pageable, 1);
            mockSearchDependencies(questionPage);

            // When
            PageResponse<QuestionResponse> result = questionService.searchQuestion(request, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(questionRepository).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("Should return empty results when keyword matches nothing")
        void searchQuestion_withKeywordNoMatch_shouldReturnEmpty() {
            // Given
            QuestionSearchRequest request = new QuestionSearchRequest();
            request.setKeyword("nonexistentkeyword");
            Pageable pageable = PageRequest.of(0, 20);

            Page<Question> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            when(questionRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(emptyPage);

            // When
            PageResponse<QuestionResponse> result = questionService.searchQuestion(request, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getContent()).isEmpty();
            assertThat(result.isFirst()).isTrue();
        }

        @Test
        @DisplayName("Should return correct pagination metadata")
        void searchQuestion_withPagination_shouldReturnCorrectPageInfo() {
            // Given
            QuestionSearchRequest request = new QuestionSearchRequest();
            Pageable pageable = PageRequest.of(2, 10, Sort.by("createdAt").descending());

            Page<Question> emptyPage = new PageImpl<>(List.of(), pageable, 30);
            when(questionRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(emptyPage);

            // When
            PageResponse<QuestionResponse> result = questionService.searchQuestion(request, pageable);

            // Then
            assertThat(result.getPage()).isEqualTo(2);
            assertThat(result.getSize()).isEqualTo(10);
            assertThat(result.getTotalElements()).isEqualTo(30);
            assertThat(result.getTotalPages()).isEqualTo(3);
            assertThat(result.isFirst()).isFalse();
            assertThat(result.isLast()).isTrue();
        }

        @Test
        @DisplayName("Should return all results when no filters provided")
        void searchQuestion_withNoFilters_shouldReturnAll() {
            // Given
            QuestionSearchRequest request = new QuestionSearchRequest();
            Pageable pageable = PageRequest.of(0, 20);

            Question question = buildQuestion();
            Page<Question> questionPage = new PageImpl<>(List.of(question), pageable, 1);
            mockSearchDependencies(questionPage);

            // When
            PageResponse<QuestionResponse> result = questionService.searchQuestion(request, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(questionRepository).findAll(any(Specification.class), eq(pageable));
        }
    }

    // ========== getByTopicId Tests ==========
    @Nested
    @DisplayName("getByTopicId")
    class GetByTopicIdTests {

        @Test
        @DisplayName("Should return questions for existing topic")
        void shouldReturnQuestionsForTopic() {
            // Given
            Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
            Topic topic = buildTopic();
            Question question = buildQuestion();
            Page<Question> questionPage = new PageImpl<>(List.of(question), pageable, 1);

            when(topicRepository.findById(TOPIC_ID)).thenReturn(Optional.of(topic));
            when(questionRepository.findByTopicIdAndIsActiveTrue(TOPIC_ID, pageable)).thenReturn(questionPage);
            when(difficultyLevelRepository.findAllById(anyCollection())).thenReturn(List.of(buildDifficultyLevel()));
            when(questionTagRepository.findByQuestionIdIn(anyList())).thenReturn(List.of());
            when(tagRepository.findAllById(anyCollection())).thenReturn(List.of());
            when(questionMapper.toResponse(any(Question.class), anyString(), anyString(), anyList()))
                    .thenReturn(QuestionResponse.builder()
                            .id(QUESTION_ID)
                            .topicId(TOPIC_ID)
                            .topicName(TOPIC_NAME)
                            .difficultyId(DIFFICULTY_ID)
                            .difficultyName(DIFFICULTY_NAME)
                            .questionText(QUESTION_TEXT)
                            .build());

            // When
            PageResponse<QuestionResponse> result = questionService.getByTopicId(TOPIC_ID, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getTopicId()).isEqualTo(TOPIC_ID);
            verify(topicRepository).findById(TOPIC_ID);
            verify(questionRepository).findByTopicIdAndIsActiveTrue(TOPIC_ID, pageable);
        }

        @Test
        @DisplayName("Should throw 404 when topic not found")
        void shouldThrow404WhenTopicNotFound() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            when(topicRepository.findById(TOPIC_ID)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> questionService.getByTopicId(TOPIC_ID, pageable))
                    .isInstanceOf(NotFoundException.class);

            verify(topicRepository).findById(TOPIC_ID);
            verify(questionRepository, never()).findByTopicIdAndIsActiveTrue(anyString(), any(Pageable.class));
        }

        @Test
        @DisplayName("Should throw 423 when topic is inactive")
        void shouldThrow423WhenTopicInactive() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Topic inactiveTopic = buildInactiveTopic();
            when(topicRepository.findById(TOPIC_ID)).thenReturn(Optional.of(inactiveTopic));

            // When & Then
            assertThatThrownBy(() -> questionService.getByTopicId(TOPIC_ID, pageable))
                    .isInstanceOf(InactiveResourceException.class);

            verify(topicRepository).findById(TOPIC_ID);
            verify(questionRepository, never()).findByTopicIdAndIsActiveTrue(anyString(), any(Pageable.class));
        }

        @Test
        @DisplayName("Should return empty result for topic with no questions")
        void shouldReturnEmptyForTopicWithNoQuestions() {
            // Given
            Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
            Topic topic = buildTopic();
            Page<Question> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(topicRepository.findById(TOPIC_ID)).thenReturn(Optional.of(topic));
            when(questionRepository.findByTopicIdAndIsActiveTrue(TOPIC_ID, pageable)).thenReturn(emptyPage);
            when(difficultyLevelRepository.findAllById(anyCollection())).thenReturn(List.of());
            when(questionTagRepository.findByQuestionIdIn(anyList())).thenReturn(List.of());
            when(tagRepository.findAllById(anyCollection())).thenReturn(List.of());

            // When
            PageResponse<QuestionResponse> result = questionService.getByTopicId(TOPIC_ID, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            verify(topicRepository).findById(TOPIC_ID);
            verify(questionRepository).findByTopicIdAndIsActiveTrue(TOPIC_ID, pageable);
        }
    }

    @Nested
    @DisplayName("generateFromResume()")
    class GenerateFromResumeTests {

        @Test
        @DisplayName("Should generate questions from resume when topic and difficulty catalogs are empty")
        void shouldGenerateQuestionsFromResumeWhenCatalogsAreEmpty() {
            GenerateQuestionsFromResumeRequest request = new GenerateQuestionsFromResumeRequest();
            request.setResumeId("resume-1");
            request.setCount(1);
            request.setLanguage("en");

            ResumeAggregateData resumeData = new ResumeAggregateData();
            AiGeneratedQuestion aiQuestion = new AiGeneratedQuestion(
                    "How would you optimize a Spring Boot API backed by PostgreSQL?",
                    "TECHNICAL",
                    "Spring Boot",
                    "Mid",
                    "The resume shows Java backend and Spring Boot experience."
            );
            AiGeneratedQuestionResult aiResult = new AiGeneratedQuestionResult(
                    "Mid-level Java backend engineer.",
                    List.of(aiQuestion)
            );

            when(resumeAggregateClient.getAggregate("user-1", "resume-1")).thenReturn(resumeData);
            when(topicRepository.findByIsActiveTrueOrderByOrderAscNameAsc()).thenReturn(List.of());
            when(difficultyLevelRepository.findAllByOrderByWeightAsc()).thenReturn(List.of());
            when(questionGenerationAiClient.generateQuestions(eq(resumeData), eq(request), anyList(), anyList()))
                    .thenReturn(aiResult);

            GenerateQuestionsFromResumeResponse response = questionService.generateFromResume("user-1", request);

            assertThat(response).isNotNull();
            assertThat(response.getResumeId()).isEqualTo("resume-1");
            assertThat(response.getCandidateSummary()).isEqualTo("Mid-level Java backend engineer.");
            assertThat(response.getTotalQuestions()).isEqualTo(1);
            assertThat(response.getQuestions()).hasSize(1);

            GeneratedQuestionItemResponse generatedQuestion = response.getQuestions().get(0);
            assertThat(generatedQuestion.getQuestionText()).isEqualTo(aiQuestion.getQuestionText());
            assertThat(generatedQuestion.getQuestionType()).isEqualTo("TECHNICAL");
            assertThat(generatedQuestion.getTopicId()).isNull();
            assertThat(generatedQuestion.getTopicName()).isEqualTo("Spring Boot");
            assertThat(generatedQuestion.getDifficultyId()).isNull();
            assertThat(generatedQuestion.getDifficultyName()).isEqualTo("Mid");
        }

        @Test
        @DisplayName("Should resolve topicId and difficultyId when catalogs match AI output names")
        void shouldResolveCatalogIds_whenNamesMatchCatalog() {
            GenerateQuestionsFromResumeRequest request = new GenerateQuestionsFromResumeRequest();
            request.setResumeId("resume-2");
            request.setCount(1);

            ResumeAggregateData resumeData = new ResumeAggregateData();

            Topic topic = new Topic();
            topic.setId("topic-spring");
            topic.setName("Spring Boot");
            topic.setIsActive(true);

            DifficultyLevel difficulty = new DifficultyLevel();
            difficulty.setId("diff-mid");
            difficulty.setName("Mid");
            difficulty.setWeight(2);

            AiGeneratedQuestion aiQuestion = new AiGeneratedQuestion(
                    "How does Spring dependency injection work?",
                    "TECHNICAL",
                    "Spring Boot",
                    "Mid",
                    "Resume shows Spring Boot project."
            );
            AiGeneratedQuestionResult aiResult = new AiGeneratedQuestionResult(
                    "Spring Boot developer.", List.of(aiQuestion)
            );

            when(resumeAggregateClient.getAggregate("user-1", "resume-2")).thenReturn(resumeData);
            when(topicRepository.findByIsActiveTrueOrderByOrderAscNameAsc()).thenReturn(List.of(topic));
            when(difficultyLevelRepository.findAllByOrderByWeightAsc()).thenReturn(List.of(difficulty));
            when(questionGenerationAiClient.generateQuestions(eq(resumeData), eq(request), anyList(), anyList()))
                    .thenReturn(aiResult);

            GenerateQuestionsFromResumeResponse response = questionService.generateFromResume("user-1", request);

            assertThat(response.getQuestions()).hasSize(1);
            GeneratedQuestionItemResponse item = response.getQuestions().get(0);
            assertThat(item.getTopicId()).isEqualTo("topic-spring");
            assertThat(item.getTopicName()).isEqualTo("Spring Boot");
            assertThat(item.getDifficultyId()).isEqualTo("diff-mid");
            assertThat(item.getDifficultyName()).isEqualTo("Mid");
        }

        @Test
        @DisplayName("Should throw BadRequestException with QUE_003 when questionType is invalid")
        void shouldThrowBadRequest_whenQuestionTypeIsInvalid() {
            GenerateQuestionsFromResumeRequest request = new GenerateQuestionsFromResumeRequest();
            request.setResumeId("resume-1");
            request.setCount(2);
            request.setQuestionType("INVALID_TYPE");

            assertThatThrownBy(() -> questionService.generateFromResume("user-1", request))
                    .isInstanceOf(BadRequestException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.QUE_003.getCode());
        }

        @Test
        @DisplayName("Should return empty questions list when AI returns null questions")
        void shouldReturnEmptyList_whenAiReturnsNullQuestions() {
            GenerateQuestionsFromResumeRequest request = new GenerateQuestionsFromResumeRequest();
            request.setResumeId("resume-1");
            request.setCount(3);

            ResumeAggregateData resumeData = new ResumeAggregateData();
            AiGeneratedQuestionResult aiResult = new AiGeneratedQuestionResult("Summary.", null);

            when(resumeAggregateClient.getAggregate("user-1", "resume-1")).thenReturn(resumeData);
            when(topicRepository.findByIsActiveTrueOrderByOrderAscNameAsc()).thenReturn(List.of());
            when(difficultyLevelRepository.findAllByOrderByWeightAsc()).thenReturn(List.of());
            when(questionGenerationAiClient.generateQuestions(eq(resumeData), eq(request), anyList(), anyList()))
                    .thenReturn(aiResult);

            GenerateQuestionsFromResumeResponse response = questionService.generateFromResume("user-1", request);

            assertThat(response.getQuestions()).isEmpty();
            assertThat(response.getTotalQuestions()).isZero();
            assertThat(response.getCandidateSummary()).isEqualTo("Summary.");
        }

        @Test
        @DisplayName("Should use preferred question type when request provides one")
        void shouldUsePreferredQuestionType_whenRequestProvidesOne() {
            GenerateQuestionsFromResumeRequest request = new GenerateQuestionsFromResumeRequest();
            request.setResumeId("resume-3");
            request.setCount(1);
            request.setQuestionType("behavioral");

            ResumeAggregateData resumeData = new ResumeAggregateData();
            AiGeneratedQuestion aiQuestion = new AiGeneratedQuestion(
                    "Tell me about a time you resolved a production incident.",
                    "INVALID_TYPE",
                    "Incident Response",
                    "Senior",
                    "Candidate handled on-call support."
            );
            AiGeneratedQuestionResult aiResult = new AiGeneratedQuestionResult("Summary.", List.of(aiQuestion));

            when(resumeAggregateClient.getAggregate("user-1", "resume-3")).thenReturn(resumeData);
            when(topicRepository.findByIsActiveTrueOrderByOrderAscNameAsc()).thenReturn(List.of());
            when(difficultyLevelRepository.findAllByOrderByWeightAsc()).thenReturn(List.of());
            when(questionGenerationAiClient.generateQuestions(eq(resumeData), eq(request), anyList(), anyList()))
                    .thenReturn(aiResult);

            GenerateQuestionsFromResumeResponse response = questionService.generateFromResume("user-1", request);

            assertThat(response.getQuestions()).hasSize(1);
            assertThat(response.getQuestions().get(0).getQuestionType()).isEqualTo(QuestionType.BEHAVIORAL.name());
        }
    }

    @Nested
    @DisplayName("adminListQuestions()")
    class AdminListQuestionsTests {

        @Test
        @DisplayName("Should return admin page with mapped topic, difficulty and tags")
        void shouldReturnAdminListQuestionsSuccessfully() {
            AdminQuestionSearchRequest request = new AdminQuestionSearchRequest();
            request.setStatus("PENDING");
            Pageable pageable = PageRequest.of(0, 10);
            Question question = buildQuestion();
            Page<Question> questionPage = new PageImpl<>(List.of(question), pageable, 1);
            QuestionResponse expectedResponse = QuestionResponse.builder()
                    .id(QUESTION_ID)
                    .status("PENDING")
                    .tags(buildTagResponses())
                    .build();

            when(questionRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(questionPage);
            when(topicRepository.findAllById(anySet())).thenReturn(List.of(buildTopic()));
            when(difficultyLevelRepository.findAllById(anySet())).thenReturn(List.of(buildDifficultyLevel()));
            when(questionTagRepository.findByQuestionIdIn(anyList()))
                    .thenReturn(List.of(new QuestionTag(QUESTION_ID, TAG_1_ID), new QuestionTag(QUESTION_ID, TAG_2_ID)));
            when(tagRepository.findAllById(anySet())).thenReturn(buildTags());
            when(tagMapper.toResponseList(anyList())).thenReturn(buildTagResponses());
            when(questionMapper.toResponse(any(Question.class), eq(TOPIC_NAME), eq(DIFFICULTY_NAME), anyList()))
                    .thenReturn(expectedResponse);

            PageResponse<QuestionResponse> response = questionService.adminListQuestions(request, pageable);

            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getId()).isEqualTo(QUESTION_ID);
            verify(questionRepository).findAll(any(Specification.class), eq(pageable));
            verify(questionTagRepository).findByQuestionIdIn(anyList());
        }
    }

    @Nested
    @DisplayName("adminGetById()")
    class AdminGetByIdTests {

        @Test
        @DisplayName("Should return same projection-based response as getById")
        void shouldReturnAdminGetByIdSuccessfully() {
            when(questionRepository.findByIdProjection(QUESTION_ID))
                    .thenReturn(Optional.of(createProjection(QUESTION_ID, TOPIC_NAME, DIFFICULTY_NAME)));
            when(tagRepository.findByQuestionId(QUESTION_ID)).thenReturn(buildTags());

            QuestionResponse response = questionService.adminGetById(QUESTION_ID);

            assertThat(response.getId()).isEqualTo(QUESTION_ID);
            assertThat(response.getTopicName()).isEqualTo(TOPIC_NAME);
            assertThat(response.getDifficultyName()).isEqualTo(DIFFICULTY_NAME);
            assertThat(response.getTags()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("adminPatchModerationStatus()")
    class AdminPatchModerationStatusTests {

        @Test
        @DisplayName("Should set rejection reason when status is REJECTED")
        void shouldSetRejectionReasonWhenRejected() {
            Question question = buildQuestion();
            question.setStatus("PENDING");
            QuestionResponse expectedResponse = QuestionResponse.builder()
                    .id(QUESTION_ID)
                    .status("REJECTED")
                    .rejectionReason("Needs more detail")
                    .build();

            when(questionRepository.findById(QUESTION_ID)).thenReturn(Optional.of(question));
            when(topicRepository.findById(TOPIC_ID)).thenReturn(Optional.of(buildTopic()));
            when(difficultyLevelRepository.findById(DIFFICULTY_ID)).thenReturn(Optional.of(buildDifficultyLevel()));
            when(tagRepository.findByQuestionId(QUESTION_ID)).thenReturn(buildTags());
            when(tagMapper.toResponseList(anyList())).thenReturn(buildTagResponses());
            when(questionMapper.toResponse(any(Question.class), eq(TOPIC_NAME), eq(DIFFICULTY_NAME), anyList()))
                    .thenReturn(expectedResponse);

            QuestionResponse response = questionService.adminPatchModerationStatus(QUESTION_ID, "REJECTED", "Needs more detail");

            assertThat(response.getStatus()).isEqualTo("REJECTED");
            assertThat(question.getStatus()).isEqualTo("REJECTED");
            assertThat(question.getRejectionReason()).isEqualTo("Needs more detail");
            assertThat(question.getLastUpdatedAt()).isNotNull();
            verify(questionRepository).save(question);
        }

        @Test
        @DisplayName("Should clear rejection reason when status is not REJECTED")
        void shouldClearRejectionReasonWhenApproved() {
            Question question = buildQuestion();
            question.setStatus("REJECTED");
            question.setRejectionReason("Old reason");
            QuestionResponse expectedResponse = QuestionResponse.builder()
                    .id(QUESTION_ID)
                    .status("APPROVED")
                    .rejectionReason(null)
                    .build();

            when(questionRepository.findById(QUESTION_ID)).thenReturn(Optional.of(question));
            when(topicRepository.findById(TOPIC_ID)).thenReturn(Optional.of(buildTopic()));
            when(difficultyLevelRepository.findById(DIFFICULTY_ID)).thenReturn(Optional.of(buildDifficultyLevel()));
            when(tagRepository.findByQuestionId(QUESTION_ID)).thenReturn(buildTags());
            when(tagMapper.toResponseList(anyList())).thenReturn(buildTagResponses());
            when(questionMapper.toResponse(any(Question.class), eq(TOPIC_NAME), eq(DIFFICULTY_NAME), anyList()))
                    .thenReturn(expectedResponse);

            QuestionResponse response = questionService.adminPatchModerationStatus(QUESTION_ID, "APPROVED", "ignored");

            assertThat(response.getStatus()).isEqualTo("APPROVED");
            assertThat(question.getStatus()).isEqualTo("APPROVED");
            assertThat(question.getRejectionReason()).isNull();
            verify(questionRepository).save(question);
        }
    }

    @Nested
    @DisplayName("getSubmittedQuestions()")
    class GetSubmittedQuestionsTests {

        @Test
        @DisplayName("Should return submitted questions with mapped tags")
        void shouldReturnSubmittedQuestionsSuccessfully() {
            Pageable pageable = PageRequest.of(0, 10);
            Question question = buildQuestion();
            question.setSubmittedById(Long.valueOf(USER_ID));
            Page<Question> questionPage = new PageImpl<>(List.of(question), pageable, 1);
            QuestionResponse expectedResponse = buildQuestionResponse(QUESTION_ID, QUESTION_TEXT, buildTagResponses());

            when(questionRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(questionPage);
            when(topicRepository.findAllById(anySet())).thenReturn(List.of(buildTopic()));
            when(difficultyLevelRepository.findAllById(anySet())).thenReturn(List.of(buildDifficultyLevel()));
            when(tagRepository.findByQuestionId(QUESTION_ID)).thenReturn(buildTags());
            when(tagMapper.toResponseList(anyList())).thenReturn(buildTagResponses());
            when(questionMapper.toResponse(any(Question.class), eq(TOPIC_NAME), eq(DIFFICULTY_NAME), anyList()))
                    .thenReturn(expectedResponse);

            PageResponse<QuestionResponse> response = questionService.getSubmittedQuestions(Long.valueOf(USER_ID), pageable);

            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getId()).isEqualTo(QUESTION_ID);
            verify(tagRepository).findByQuestionId(QUESTION_ID);
        }
    }

    @Nested
    @DisplayName("resubmit()")
    class ResubmitTests {

        @Test
        @DisplayName("Should resubmit rejected question for owner")
        void shouldResubmitRejectedQuestionSuccessfully() {
            Question question = buildQuestion();
            question.setSubmittedById(Long.valueOf(USER_ID));
            question.setStatus("REJECTED");
            question.setRejectionReason("Old reason");
            QuestionResponse expectedResponse = QuestionResponse.builder()
                    .id(QUESTION_ID)
                    .status("PENDING")
                    .rejectionReason(null)
                    .build();

            when(questionRepository.findById(QUESTION_ID)).thenReturn(Optional.of(question));
            when(topicRepository.findById(TOPIC_ID)).thenReturn(Optional.of(buildTopic()));
            when(difficultyLevelRepository.findById(DIFFICULTY_ID)).thenReturn(Optional.of(buildDifficultyLevel()));
            when(tagRepository.findByQuestionId(QUESTION_ID)).thenReturn(buildTags());
            when(tagMapper.toResponseList(anyList())).thenReturn(buildTagResponses());
            when(questionMapper.toResponse(any(Question.class), eq(TOPIC_NAME), eq(DIFFICULTY_NAME), anyList()))
                    .thenReturn(expectedResponse);

            QuestionResponse response = questionService.resubmit(QUESTION_ID, Long.valueOf(USER_ID));

            assertThat(response.getStatus()).isEqualTo("PENDING");
            assertThat(question.getStatus()).isEqualTo("PENDING");
            assertThat(question.getRejectionReason()).isNull();
            assertThat(question.getLastUpdatedAt()).isNotNull();
            verify(questionRepository).save(question);
        }

        @Test
        @DisplayName("Should reject resubmit when current user is not owner")
        void shouldRejectResubmitWhenNotOwner() {
            Question question = buildQuestion();
            question.setSubmittedById(999L);
            question.setStatus("REJECTED");

            when(questionRepository.findById(QUESTION_ID)).thenReturn(Optional.of(question));

            assertThatThrownBy(() -> questionService.resubmit(QUESTION_ID, Long.valueOf(USER_ID)))
                    .isInstanceOf(BadRequestException.class)
                    .extracting("errorCode")
                    .isEqualTo("QUE_403");

            verify(questionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject resubmit when question is not in REJECTED status")
        void shouldRejectResubmitWhenStatusIsNotRejected() {
            Question question = buildQuestion();
            question.setSubmittedById(Long.valueOf(USER_ID));
            question.setStatus("PENDING");

            when(questionRepository.findById(QUESTION_ID)).thenReturn(Optional.of(question));

            assertThatThrownBy(() -> questionService.resubmit(QUESTION_ID, Long.valueOf(USER_ID)))
                    .isInstanceOf(BadRequestException.class)
                    .extracting("errorCode")
                    .isEqualTo("QUE_409");

            verify(questionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("bulkImport()")
    class BulkImportTests {

        @Test
        @DisplayName("Should bulk import valid questions and invalidate random cache")
        void shouldBulkImportValidQuestionsSuccessfully() {
            QuestionRequest firstRequest = buildBulkQuestionRequest(TOPIC_ID, DIFFICULTY_ID, "Question A", List.of(TAG_1_ID, TAG_2_ID));
            QuestionRequest secondRequest = buildBulkQuestionRequest(TOPIC_ID, DIFFICULTY_ID, "Question B", List.of());
            BulkImportRequest request = buildBulkImportRequest(firstRequest, secondRequest);

            Question firstEntity = new Question();
            Question secondEntity = new Question();
            Question savedFirst = buildQuestionWithIdAndText("question-a", "Question A");
            Question savedSecond = buildQuestionWithIdAndText("question-b", "Question B");

            when(topicRepository.findAllById(anySet())).thenReturn(List.of(buildTopic()));
            when(difficultyLevelRepository.findAllById(anySet())).thenReturn(List.of(buildDifficultyLevel()));
            when(tagRepository.findAllById(anySet())).thenReturn(buildTags());
            when(questionMapper.toEntity(firstRequest)).thenReturn(firstEntity);
            when(questionMapper.toEntity(secondRequest)).thenReturn(secondEntity);
            when(questionRepository.saveAll(anyList())).thenReturn(List.of(savedFirst, savedSecond));
            when(redisTemplate.keys(anyString())).thenReturn(Set.of("questions:random:test"));

            BulkImportResponse response = questionService.bulkImport(request);

            assertThat(response.getTotal()).isEqualTo(2);
            assertThat(response.getCreated()).isEqualTo(2);
            assertThat(response.getFailed()).isZero();
            assertThat(firstEntity.getCreatedBy()).isEqualTo("SYSTEM");
            assertThat(secondEntity.getCreatedBy()).isEqualTo("SYSTEM");
            assertThat(firstEntity.getStatus()).isEqualTo("PENDING");
            assertThat(secondEntity.getStatus()).isEqualTo("PENDING");
            assertThat(firstEntity.getIsActive()).isTrue();
            assertThat(secondEntity.getIsActive()).isTrue();
            assertThat(firstEntity.getCreatedAt()).isNotNull();
            assertThat(secondEntity.getCreatedAt()).isNotNull();

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<QuestionTag>> questionTagCaptor =
                    (ArgumentCaptor<List<QuestionTag>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(List.class);

            verify(questionTagRepository).saveAll(questionTagCaptor.capture());
            assertQuestionTagsMappings(questionTagCaptor.getValue(), "question-a", List.of(TAG_1_ID, TAG_2_ID));
            verify(redisTemplate).delete(Set.of("questions:random:test"));
        }

        @Test
        @DisplayName("Should collect validation errors and only persist valid rows")
        void shouldCollectValidationErrorsAndPersistOnlyValidRows() {
            String inactiveTopicId = "topic-inactive";
            String missingDifficultyId = "difficulty-missing";
            String missingTagId = "tag-missing";

            Topic inactiveTopic = buildInactiveTopic();
            inactiveTopic.setId(inactiveTopicId);

            QuestionRequest validRequest = buildBulkQuestionRequest(TOPIC_ID, DIFFICULTY_ID, "Question A", List.of());
            QuestionRequest inactiveTopicRequest = buildBulkQuestionRequest(inactiveTopicId, DIFFICULTY_ID, "Question B", List.of());
            QuestionRequest missingDifficultyRequest = buildBulkQuestionRequest(TOPIC_ID, missingDifficultyId, "Question C", List.of());
            QuestionRequest missingTagRequest = buildBulkQuestionRequest(TOPIC_ID, DIFFICULTY_ID, "Question D", List.of(missingTagId));
            QuestionRequest duplicateRequest = buildBulkQuestionRequest(TOPIC_ID, DIFFICULTY_ID, "Question A", List.of());
            BulkImportRequest request = buildBulkImportRequest(
                    validRequest,
                    inactiveTopicRequest,
                    missingDifficultyRequest,
                    missingTagRequest,
                    duplicateRequest
            );

            Question validEntity = new Question();
            Question savedValidQuestion = buildQuestionWithIdAndText("question-valid", "Question A");

            when(topicRepository.findAllById(anySet())).thenReturn(List.of(buildTopic(), inactiveTopic));
            when(difficultyLevelRepository.findAllById(anySet())).thenReturn(List.of(buildDifficultyLevel()));
            when(tagRepository.findAllById(anySet())).thenReturn(List.of());
            when(questionMapper.toEntity(validRequest)).thenReturn(validEntity);
            when(questionRepository.saveAll(anyList())).thenReturn(List.of(savedValidQuestion));
            when(redisTemplate.keys(anyString())).thenReturn(Set.of());

            BulkImportResponse response = questionService.bulkImport(request);

            assertThat(response.getTotal()).isEqualTo(5);
            assertThat(response.getCreated()).isEqualTo(1);
            assertThat(response.getFailed()).isEqualTo(4);
            assertThat(response.getErrors()).hasSize(4);
            assertThat(response.getErrors().get(0).getIndex()).isEqualTo(1);
            assertThat(response.getErrors().get(0).getError()).isEqualTo("Topic is inactive: " + inactiveTopicId);
            assertThat(response.getErrors().get(1).getIndex()).isEqualTo(2);
            assertThat(response.getErrors().get(1).getError()).isEqualTo("Difficulty level not found: " + missingDifficultyId);
            assertThat(response.getErrors().get(2).getIndex()).isEqualTo(3);
            assertThat(response.getErrors().get(2).getError()).isEqualTo("Tag not found: " + missingTagId);
            assertThat(response.getErrors().get(3).getIndex()).isEqualTo(4);
            assertThat(response.getErrors().get(3).getError()).isEqualTo("Duplicate question text in batch");

            verify(questionMapper, times(1)).toEntity(any(QuestionRequest.class));
            verify(questionTagRepository, never()).saveAll(anyList());
        }

        @Test
        @DisplayName("Should ignore cache invalidation failure during bulk import")
        void shouldIgnoreCacheInvalidationFailureDuringBulkImport() {
            QuestionRequest validRequest = buildBulkQuestionRequest(TOPIC_ID, DIFFICULTY_ID, "Question A", List.of());
            BulkImportRequest request = buildBulkImportRequest(validRequest);
            Question entity = new Question();
            Question savedQuestion = buildQuestionWithIdAndText("question-a", "Question A");

            when(topicRepository.findAllById(anySet())).thenReturn(List.of(buildTopic()));
            when(difficultyLevelRepository.findAllById(anySet())).thenReturn(List.of(buildDifficultyLevel()));
            when(tagRepository.findAllById(anySet())).thenReturn(List.of());
            when(questionMapper.toEntity(validRequest)).thenReturn(entity);
            when(questionRepository.saveAll(anyList())).thenReturn(List.of(savedQuestion));
            when(redisTemplate.keys(anyString())).thenThrow(new RuntimeException("Redis down"));

            BulkImportResponse response = questionService.bulkImport(request);

            assertThat(response.getCreated()).isEqualTo(1);
            assertThat(response.getFailed()).isZero();
            verify(questionRepository).saveAll(anyList());
        }
    }

    @Nested
    @DisplayName("countQuestions()")
    class CountQuestionsTests {

        @Test
        @DisplayName("Should delegate count query with provided filters")
        void shouldCountQuestionsWithProvidedFilters() {
            when(questionRepository.countByFilters(TOPIC_ID, DIFFICULTY_ID)).thenReturn(7L);

            long result = questionService.countQuestions(TOPIC_ID, DIFFICULTY_ID);

            assertThat(result).isEqualTo(7L);
            verify(questionRepository).countByFilters(TOPIC_ID, DIFFICULTY_ID);
        }
    }
}
