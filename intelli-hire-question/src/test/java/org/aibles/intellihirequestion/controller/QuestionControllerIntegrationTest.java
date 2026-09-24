package org.aibles.intellihirequestion.controller;

import org.aibles.intellihirequestion.entity.*;
import org.aibles.intellihirequestion.entity.enums.QuestionType;
import org.aibles.intellihirequestion.repository.DifficultyLevelRepository;
import org.aibles.intellihirequestion.repository.QuestionRepository;
import org.aibles.intellihirequestion.repository.RubricRepository;
import org.aibles.intellihirequestion.repository.TopicRepository;
import org.aibles.intellihirequestion.repository.TagRepository;
import org.aibles.intellihirequestion.repository.QuestionTagRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.aibles.intellihirequestion.config.RedisTestConfig;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.cache.type=none",
        "spring.main.allow-bean-definition-overriding=true"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(RedisTestConfig.class)
class QuestionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private RubricRepository rubricRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private DifficultyLevelRepository difficultyLevelRepository;

    private static final String TOPIC_ID = "topic-uuid-1";
    private static final String DIFFICULTY_ID = "difficulty-uuid-1";
    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private QuestionTagRepository questionTagRepository;

    @BeforeEach
    void setUp() {
        rubricRepository.deleteAll();
        questionTagRepository.deleteAll();
        questionRepository.deleteAll();
        topicRepository.deleteAll();
        tagRepository.deleteAll();
        difficultyLevelRepository.deleteAll();

        // Create required entities
        createTopic(TOPIC_ID, "Test Topic");
        createDifficultyLevel(DIFFICULTY_ID, "Medium", 3);
    }

    @AfterEach
    void tearDown() {
        rubricRepository.deleteAll();
        questionRepository.deleteAll();
        topicRepository.deleteAll();
        difficultyLevelRepository.deleteAll();
    }

    private Topic createTopic(String id, String name) {
        Topic topic = new Topic();
        topic.setId(id);
        topic.setName(name);
        topic.setIsActive(true);
        return topicRepository.save(topic);
    }

    private DifficultyLevel createDifficultyLevel(String id, String name, int weight) {
        DifficultyLevel difficulty = new DifficultyLevel();
        difficulty.setId(id);
        difficulty.setName(name);
        difficulty.setWeight(weight);
        return difficultyLevelRepository.save(difficulty);
    }

    private Question createQuestion(String id) {
        Question question = new Question();
        question.setId(id);
        question.setTopicId(TOPIC_ID);
        question.setDifficultyId(DIFFICULTY_ID);
        question.setQuestionText("Test question");
        question.setQuestionType(QuestionType.TECHNICAL);
        question.setIsActive(true);
        return questionRepository.save(question);
    }

    private Rubric createRubric(String id, String questionId) {
        Rubric rubric = new Rubric();
        rubric.setId(id);
        rubric.setQuestionId(questionId);
        rubric.setCriteria("Test criteria");
        rubric.setIsActive(true);
        return rubricRepository.save(rubric);
    }

    // ==================== DELETE /v1/questions/{id} Tests ====================

    @Nested
    @DisplayName("DELETE api/v1/questions/{id} - Success (200)")
    class DeleteQuestionSuccessTests {

        @Test
        @DisplayName("Should soft delete question and return 200")
        void shouldSoftDeleteQuestion_andReturn200() throws Exception {
            // Given
            Question question = createQuestion("question-uuid-001");

            // When & Then
            mockMvc.perform(delete("/v1/questions/{id}", question.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.error_code").isEmpty())
                    .andExpect(jsonPath("$.data").isEmpty())
                    .andExpect(jsonPath("$.errors").isEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            // Verify question is soft deleted (isActive = false)
            Question deletedQuestion = questionRepository.findById(question.getId()).orElseThrow();
            assertThat(deletedQuestion.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should soft delete question and rubric when rubric exists")
        void shouldSoftDeleteQuestionAndRubric() throws Exception {
            // Given
            Question question = createQuestion("question-uuid-002");
            Rubric rubric = createRubric("rubric-uuid-001", question.getId());

            // When & Then
            mockMvc.perform(delete("/v1/questions/{id}", question.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            // Verify question and rubric are soft deleted
            Question deletedQuestion = questionRepository.findById(question.getId()).orElseThrow();
            Rubric deletedRubric = rubricRepository.findById(rubric.getId()).orElseThrow();
            assertThat(deletedQuestion.getIsActive()).isFalse();
            assertThat(deletedRubric.getIsActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("DELETE api/v1/questions/{id} - Not Found (404)")
    class DeleteQuestionNotFoundTests {

        @Test
        @DisplayName("Should return 404 when question does not exist")
        void shouldReturn404_whenQuestionNotFound() throws Exception {
            // Given
            String nonExistentId = "non-existent-uuid";

            // When & Then
            mockMvc.perform(delete("/v1/questions/{id}", nonExistentId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("QUE_001"));
        }
    }

    @Nested
    @DisplayName("GET /v1/questions/{id}")
    class GetByIdTests {

        @Test
        @DisplayName("Should return question with all nested data when found")
        void shouldReturnQuestion_whenFound() throws Exception {
            // Given
            Topic topic = createAndSaveTopic("t-123", "Java Core");
            DifficultyLevel difficulty = createAndSaveDifficulty("d-123", "Mid", 2);
            Tag tag = createAndSaveTag("tag-123", "collections");
            Question question = createAndSaveQuestion("q-123", topic.getId(), difficulty.getId());
            linkQuestionWithTags(question.getId(), List.of(tag));

            // When & Then
            mockMvc.perform(get("/v1/questions/{id}", question.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(question.getId()))
                    .andExpect(jsonPath("$.data.topic_id").value(topic.getId()))
                    .andExpect(jsonPath("$.data.topic_name").value("Java Core"))
                    .andExpect(jsonPath("$.data.difficulty_id").value(difficulty.getId()))
                    .andExpect(jsonPath("$.data.difficulty_name").value("Mid"))
                    .andExpect(jsonPath("$.data.question_text").value("Test question text"))
                    .andExpect(jsonPath("$.data.question_type").value("TECHNICAL"))
                    .andExpect(jsonPath("$.data.tags").isArray())
                    .andExpect(jsonPath("$.data.tags[0].name").value("collections"))
                    .andExpect(jsonPath("$.data.is_active").value(true));
        }

        @Test
        @DisplayName("Should return question with empty tags when no tags")
        void shouldReturnQuestion_withEmptyTags() throws Exception {
            // Given
            Topic topic = createAndSaveTopic("t-123", "Java Core");
            DifficultyLevel difficulty = createAndSaveDifficulty("d-123", "Mid", 2);
            Question question = createAndSaveQuestion("q-123", topic.getId(), difficulty.getId());

            // When & Then
            mockMvc.perform(get("/v1/questions/{id}", question.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.tags").isArray())
                    .andExpect(jsonPath("$.data.tags").isEmpty());
        }

        @Test
        @DisplayName("Should return inactive question (for admin viewing)")
        void shouldReturnInactiveQuestion() throws Exception {
            // Given
            Topic topic = createAndSaveTopic("t-123", "Java Core");
            DifficultyLevel difficulty = createAndSaveDifficulty("d-123", "Mid", 2);
            Question question = createAndSaveQuestion("q-123", topic.getId(), difficulty.getId());
            question.setIsActive(false);
            questionRepository.save(question);

            // When & Then
            mockMvc.perform(get("/v1/questions/{id}", question.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.is_active").value(false));
        }

        @Test
        @DisplayName("Should return 404 when question not found")
        void shouldReturn404_whenQuestionNotFound() throws Exception {
            // When & Then
            mockMvc.perform(get("/v1/questions/{id}", "non-existent-id"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("QUE_001"));
        }
    }

    private Topic createAndSaveTopic(String id, String name) {
        Topic topic = new Topic();
        topic.setId(id);
        topic.setName(name);
        topic.setIsActive(true);
        topic.setOrder(1);
        return topicRepository.save(topic);
    }

    private DifficultyLevel createAndSaveDifficulty(String id, String name, int weight) {
        DifficultyLevel difficulty = new DifficultyLevel();
        difficulty.setId(id);
        difficulty.setName(name);
        difficulty.setWeight(weight);
        return difficultyLevelRepository.save(difficulty);
    }

    private Tag createAndSaveTag(String id, String name) {
        Tag tag = new Tag();
        tag.setId(id);
        tag.setName(name);
        return tagRepository.save(tag);
    }

    private Question createAndSaveQuestion(String id, String topicId, String difficultyId) {
        Question question = new Question();
        question.setId(id);
        question.setTopicId(topicId);
        question.setDifficultyId(difficultyId);
        question.setQuestionText("Test question text");
        question.setQuestionType(QuestionType.TECHNICAL);
        question.setIsActive(true);
        return questionRepository.save(question);
    }

    private void linkQuestionWithTags(String questionId, List<Tag> tags) {
        for (Tag tag : tags) {
            QuestionTag questionTag = new QuestionTag(questionId, tag.getId());
            questionTagRepository.save(questionTag);
        }
    }

    // ========== Random Question Helpers ==========

    private void createMultipleQuestions(String topicId, String difficultyId, int count) {
        for (int i = 1; i <= count; i++) {
            createAndSaveQuestion("random-q-" + i, topicId, difficultyId);
        }
    }

    // ==================== GET /v1/questions/random Tests ====================

    @Nested
    @DisplayName("GET /v1/questions/random - Success (200)")
    class GetRandomQuestionsSuccessTests {

        @Test
        @DisplayName("Should return random questions with count param")
        void shouldReturn200_withRandomQuestions() throws Exception {
            // Given
            createMultipleQuestions(TOPIC_ID, DIFFICULTY_ID, 5);

            // When & Then
            mockMvc.perform(get("/v1/questions/random")
                            .param("count", "3"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data", hasSize(3)));
        }

        @Test
        @DisplayName("Should return random questions filtered by topicId")
        void shouldReturn200_withTopicFilter() throws Exception {
            // Given
            Topic topic2 = createAndSaveTopic("topic-2", "Another Topic");
            createMultipleQuestions(TOPIC_ID, DIFFICULTY_ID, 5);
            createAndSaveQuestion("other-topic-q", topic2.getId(), DIFFICULTY_ID);

            // When & Then
            mockMvc.perform(get("/v1/questions/random")
                            .param("count", "3")
                            .param("topicId", TOPIC_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data", hasSize(3)))
                    .andExpect(jsonPath("$.data[0].topic_id").value(TOPIC_ID))
                    .andExpect(jsonPath("$.data[1].topic_id").value(TOPIC_ID))
                    .andExpect(jsonPath("$.data[2].topic_id").value(TOPIC_ID));
        }

        @Test
        @DisplayName("Should return random questions filtered by difficultyId")
        void shouldReturn200_withDifficultyFilter() throws Exception {
            // Given
            DifficultyLevel hard = createAndSaveDifficulty("diff-hard", "Hard", 5);
            createMultipleQuestions(TOPIC_ID, DIFFICULTY_ID, 3);
            createAndSaveQuestion("hard-q-1", TOPIC_ID, hard.getId());

            // When & Then
            mockMvc.perform(get("/v1/questions/random")
                            .param("count", "3")
                            .param("difficultyId", DIFFICULTY_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data", hasSize(3)))
                    .andExpect(jsonPath("$.data[0].difficulty_id").value(DIFFICULTY_ID));
        }

        @Test
        @DisplayName("Should exclude specified question IDs")
        void shouldReturn200_withExcludeIds() throws Exception {
            // Given
            createMultipleQuestions(TOPIC_ID, DIFFICULTY_ID, 5);

            // When & Then
            mockMvc.perform(get("/v1/questions/random")
                            .param("count", "2")
                            .param("excludeIds", "random-q-1")
                            .param("excludeIds", "random-q-2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data", hasSize(2)))
                    .andExpect(jsonPath("$.data[*].id", not(hasItem("random-q-1"))))
                    .andExpect(jsonPath("$.data[*].id", not(hasItem("random-q-2"))));
        }

        @Test
        @DisplayName("Should return questions filtered by tagIds")
        void shouldReturn200_withTagFilter() throws Exception {
            // Given
            Tag tag1 = createAndSaveTag("tag-java", "java");

            Question q1 = createAndSaveQuestion("tagged-q-1", TOPIC_ID, DIFFICULTY_ID);
            Question q2 = createAndSaveQuestion("tagged-q-2", TOPIC_ID, DIFFICULTY_ID);
            createAndSaveQuestion("untagged-q-3", TOPIC_ID, DIFFICULTY_ID);

            linkQuestionWithTags(q1.getId(), List.of(tag1));
            linkQuestionWithTags(q2.getId(), List.of(tag1));

            // When & Then
            mockMvc.perform(get("/v1/questions/random")
                            .param("count", "2")
                            .param("tagIds", tag1.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data", hasSize(2)))
                    .andExpect(jsonPath("$.data[*].id", not(hasItem("untagged-q-3"))));
        }
    }

    @Nested
    @DisplayName("GET /v1/questions/random - Error cases")
    class GetRandomQuestionsErrorTests {

        @Test
        @DisplayName("Should return 404 when topicId not found")
        void shouldReturn404_whenTopicNotFound() throws Exception {
            mockMvc.perform(get("/v1/questions/random")
                            .param("count", "3")
                            .param("topicId", "non-existent-topic"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("TOP_001"));
        }

        @Test
        @DisplayName("Should return 404 when difficultyId not found")
        void shouldReturn404_whenDifficultyNotFound() throws Exception {
            mockMvc.perform(get("/v1/questions/random")
                            .param("count", "3")
                            .param("difficultyId", "non-existent-diff"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("DIF_001"));
        }

        @Test
        @DisplayName("Should return 404 when not enough questions available")
        void shouldReturn404_whenNotEnoughQuestions() throws Exception {
            // Given - only 2 questions but requesting 5
            createMultipleQuestions(TOPIC_ID, DIFFICULTY_ID, 2);

            mockMvc.perform(get("/v1/questions/random")
                            .param("count", "5"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("QUE_005"));
        }

        @Test
        @DisplayName("Should return 400 when count is missing")
        void shouldReturn400_whenCountMissing() throws Exception {
            mockMvc.perform(get("/v1/questions/random")
                            .param("topicId", TOPIC_ID))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should return 400 when count is 0")
        void shouldReturn400_whenCountIsZero() throws Exception {
            mockMvc.perform(get("/v1/questions/random")
                            .param("count", "0"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should return 400 when type is invalid")
        void shouldReturn400_whenTypeInvalid() throws Exception {
            createMultipleQuestions(TOPIC_ID, DIFFICULTY_ID, 5);

            mockMvc.perform(get("/v1/questions/random")
                            .param("count", "3")
                            .param("type", "INVALID_TYPE"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("QUE_003"));
        }
    }

    // ==================== POST /v1/questions/bulk Tests ====================

    @Nested
    @DisplayName("POST /v1/questions/bulk - All Valid (200)")
    class BulkImportAllValidTests {

        @Test
        @DisplayName("Should create all questions and return correct counts")
        void shouldCreateAllQuestions() throws Exception {
            // Given
            Tag tag = createAndSaveTag("tag-bulk-1", "java");

            String body = String.format("""
                    {
                      "questions": [
                        {
                          "topic_id": "%s",
                          "difficulty_id": "%s",
                          "question_text": "What is Spring Boot?",
                          "question_type": "TECHNICAL",
                          "tag_ids": ["%s"]
                        },
                        {
                          "topic_id": "%s",
                          "difficulty_id": "%s",
                          "question_text": "What is dependency injection?",
                          "question_type": "TECHNICAL",
                          "tag_ids": ["%s"]
                        }
                      ]
                    }
                    """, TOPIC_ID, DIFFICULTY_ID, tag.getId(),
                    TOPIC_ID, DIFFICULTY_ID, tag.getId());

            // When & Then
            mockMvc.perform(post("/v1/questions/bulk")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.total").value(2))
                    .andExpect(jsonPath("$.data.created").value(2))
                    .andExpect(jsonPath("$.data.failed").value(0))
                    .andExpect(jsonPath("$.data.errors").isArray())
                    .andExpect(jsonPath("$.data.errors", hasSize(0)));

            // Verify questions are saved in DB
            assertThat(questionRepository.count()).isGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("Should create questions without tags")
        void shouldCreateQuestionsWithoutTags() throws Exception {
            String body = String.format("""
                    {
                      "questions": [
                        {
                          "topic_id": "%s",
                          "difficulty_id": "%s",
                          "question_text": "No tags question",
                          "question_type": "TECHNICAL"
                        }
                      ]
                    }
                    """, TOPIC_ID, DIFFICULTY_ID);

            mockMvc.perform(post("/v1/questions/bulk")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.total").value(1))
                    .andExpect(jsonPath("$.data.created").value(1))
                    .andExpect(jsonPath("$.data.failed").value(0));
        }
    }

    @Nested
    @DisplayName("POST /v1/questions/bulk - Partial Success (200)")
    class BulkImportPartialSuccessTests {

        @Test
        @DisplayName("Should create valid questions and report invalid ones")
        void shouldCreateValidAndReportInvalid() throws Exception {
            String body = String.format("""
                    {
                      "questions": [
                        {
                          "topic_id": "%s",
                          "difficulty_id": "%s",
                          "question_text": "Valid question here",
                          "question_type": "TECHNICAL"
                        },
                        {
                          "topic_id": "non-existent-topic",
                          "difficulty_id": "%s",
                          "question_text": "Invalid topic question",
                          "question_type": "TECHNICAL"
                        },
                        {
                          "topic_id": "%s",
                          "difficulty_id": "non-existent-diff",
                          "question_text": "Invalid difficulty question",
                          "question_type": "TECHNICAL"
                        }
                      ]
                    }
                    """, TOPIC_ID, DIFFICULTY_ID, DIFFICULTY_ID, TOPIC_ID);

            mockMvc.perform(post("/v1/questions/bulk")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.total").value(3))
                    .andExpect(jsonPath("$.data.created").value(1))
                    .andExpect(jsonPath("$.data.failed").value(2))
                    .andExpect(jsonPath("$.data.errors", hasSize(2)))
                    .andExpect(jsonPath("$.data.errors[0].index").value(1))
                    .andExpect(jsonPath("$.data.errors[0].error").value("Không tìm thấy chủ đề: non-existent-topic"))
                    .andExpect(jsonPath("$.data.errors[1].index").value(2))
                    .andExpect(jsonPath("$.data.errors[1].error").value("Không tìm thấy độ khó: non-existent-diff"));
        }

        @Test
        @DisplayName("Should report error when tag not found")
        void shouldReportError_whenTagNotFound() throws Exception {
            String body = String.format("""
                    {
                      "questions": [
                        {
                          "topic_id": "%s",
                          "difficulty_id": "%s",
                          "question_text": "Question with bad tag",
                          "question_type": "TECHNICAL",
                          "tag_ids": ["non-existent-tag"]
                        }
                      ]
                    }
                    """, TOPIC_ID, DIFFICULTY_ID);

            mockMvc.perform(post("/v1/questions/bulk")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.created").value(0))
                    .andExpect(jsonPath("$.data.failed").value(1))
                    .andExpect(jsonPath("$.data.errors[0].error").value("Không tìm thấy nhãn: non-existent-tag"));
        }

        @Test
        @DisplayName("Should report error when topic is inactive")
        void shouldReportError_whenTopicInactive() throws Exception {
            Topic inactiveTopic = createAndSaveTopic("inactive-topic", "Inactive");
            inactiveTopic.setIsActive(false);
            topicRepository.save(inactiveTopic);

            String body = String.format("""
                    {
                      "questions": [
                        {
                          "topic_id": "inactive-topic",
                          "difficulty_id": "%s",
                          "question_text": "Question with inactive topic",
                          "question_type": "TECHNICAL"
                        }
                      ]
                    }
                    """, DIFFICULTY_ID);

            mockMvc.perform(post("/v1/questions/bulk")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.created").value(0))
                    .andExpect(jsonPath("$.data.failed").value(1))
                    .andExpect(jsonPath("$.data.errors[0].error").value("Chủ đề đang không hoạt động: inactive-topic"));
        }
    }

    @Nested
    @DisplayName("POST /v1/questions/bulk - All Invalid (200)")
    class BulkImportAllInvalidTests {

        @Test
        @DisplayName("Should create none when all questions are invalid")
        void shouldCreateNone_whenAllInvalid() throws Exception {
            String body = """
                    {
                      "questions": [
                        {
                          "topic_id": "bad-topic-1",
                          "difficulty_id": "bad-diff-1",
                          "question_text": "Question 1",
                          "question_type": "TECHNICAL"
                        },
                        {
                          "topic_id": "bad-topic-2",
                          "difficulty_id": "bad-diff-2",
                          "question_text": "Question 2",
                          "question_type": "TECHNICAL"
                        }
                      ]
                    }
                    """;

            long countBefore = questionRepository.count();

            mockMvc.perform(post("/v1/questions/bulk")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.total").value(2))
                    .andExpect(jsonPath("$.data.created").value(0))
                    .andExpect(jsonPath("$.data.failed").value(2))
                    .andExpect(jsonPath("$.data.errors", hasSize(2)));

            // Verify no new questions in DB
            assertThat(questionRepository.count()).isEqualTo(countBefore);
        }
    }

    @Nested
    @DisplayName("POST /v1/questions/bulk - Duplicate Text in Batch (200)")
    class BulkImportDuplicateTextTests {

        @Test
        @DisplayName("Should fail second duplicate and keep first")
        void shouldFailDuplicate_keepFirst() throws Exception {
            String body = String.format("""
                    {
                      "questions": [
                        {
                          "topic_id": "%s",
                          "difficulty_id": "%s",
                          "question_text": "Same question text",
                          "question_type": "TECHNICAL"
                        },
                        {
                          "topic_id": "%s",
                          "difficulty_id": "%s",
                          "question_text": "Same question text",
                          "question_type": "TECHNICAL"
                        },
                        {
                          "topic_id": "%s",
                          "difficulty_id": "%s",
                          "question_text": "Unique question text",
                          "question_type": "TECHNICAL"
                        }
                      ]
                    }
                    """, TOPIC_ID, DIFFICULTY_ID,
                    TOPIC_ID, DIFFICULTY_ID,
                    TOPIC_ID, DIFFICULTY_ID);

            mockMvc.perform(post("/v1/questions/bulk")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.total").value(3))
                    .andExpect(jsonPath("$.data.created").value(2))
                    .andExpect(jsonPath("$.data.failed").value(1))
                    .andExpect(jsonPath("$.data.errors", hasSize(1)))
                    .andExpect(jsonPath("$.data.errors[0].index").value(1))
                    .andExpect(jsonPath("$.data.errors[0].error").value("Trùng lặp nội dung câu hỏi trong lô nhập"));
        }

        @Test
        @DisplayName("Should detect duplicate case-insensitively")
        void shouldDetectDuplicate_caseInsensitive() throws Exception {
            String body = String.format("""
                    {
                      "questions": [
                        {
                          "topic_id": "%s",
                          "difficulty_id": "%s",
                          "question_text": "What is Java?",
                          "question_type": "TECHNICAL"
                        },
                        {
                          "topic_id": "%s",
                          "difficulty_id": "%s",
                          "question_text": "what is java?",
                          "question_type": "TECHNICAL"
                        }
                      ]
                    }
                    """, TOPIC_ID, DIFFICULTY_ID,
                    TOPIC_ID, DIFFICULTY_ID);

            mockMvc.perform(post("/v1/questions/bulk")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.created").value(1))
                    .andExpect(jsonPath("$.data.failed").value(1))
                    .andExpect(jsonPath("$.data.errors[0].error").value("Trùng lặp nội dung câu hỏi trong lô nhập"));
        }
    }

    @Nested
    @DisplayName("POST /v1/questions/bulk - Validation Error (400)")
    class BulkImportValidationErrorTests {

        @Test
        @DisplayName("Should return 400 when questions list is empty")
        void shouldReturn400_whenEmpty() throws Exception {
            mockMvc.perform(post("/v1/questions/bulk")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"questions\": []}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should return 400 when questions field is missing")
        void shouldReturn400_whenMissing() throws Exception {
            mockMvc.perform(post("/v1/questions/bulk")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should return 400 when exceeds 100 questions")
        void shouldReturn400_whenExceeds100() throws Exception {
            StringBuilder sb = new StringBuilder("{\"questions\": [");
            for (int i = 0; i < 101; i++) {
                if (i > 0) sb.append(",");
                sb.append(String.format("""
                        {
                          "topic_id": "%s",
                          "difficulty_id": "%s",
                          "question_text": "Question %d",
                          "question_type": "TECHNICAL"
                        }
                        """, TOPIC_ID, DIFFICULTY_ID, i));
            }
            sb.append("]}");

            mockMvc.perform(post("/v1/questions/bulk")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(sb.toString()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should return 400 when question_text is blank in one item")
        void shouldReturn400_whenItemInvalid() throws Exception {
            String body = String.format("""
                    {
                      "questions": [
                        {
                          "topic_id": "%s",
                          "difficulty_id": "%s",
                          "question_text": "",
                          "question_type": "TECHNICAL"
                        }
                      ]
                    }
                    """, TOPIC_ID, DIFFICULTY_ID);

            mockMvc.perform(post("/v1/questions/bulk")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("POST /v1/questions/bulk - Verify DB state")
    class BulkImportDbVerificationTests {

        @Test
        @DisplayName("Should save question_tags correctly for valid questions")
        void shouldSaveQuestionTags() throws Exception {
            Tag tag1 = createAndSaveTag("tag-b1", "spring");
            Tag tag2 = createAndSaveTag("tag-b2", "jpa");

            String body = String.format("""
                    {
                      "questions": [
                        {
                          "topic_id": "%s",
                          "difficulty_id": "%s",
                          "question_text": "Question with multiple tags",
                          "question_type": "TECHNICAL",
                          "tag_ids": ["%s", "%s"]
                        }
                      ]
                    }
                    """, TOPIC_ID, DIFFICULTY_ID, tag1.getId(), tag2.getId());

            mockMvc.perform(post("/v1/questions/bulk")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.created").value(1));

            // Verify question_tags are saved
            List<Question> questions = questionRepository.findAll();
            Question savedQuestion = questions.stream()
                    .filter(q -> q.getQuestionText().equals("Question with multiple tags"))
                    .findFirst()
                    .orElseThrow();

            List<QuestionTag> questionTags = questionTagRepository.findByQuestionIdIn(
                    List.of(savedQuestion.getId()));
            assertThat(questionTags).hasSize(2);
        }
    }

    // ==================== POST /v1/questions/{id}/rubric Tests ====================

    @Nested
    @DisplayName("POST /v1/questions/{id}/rubric")
    class CreateRubricTests {

        @Test
        @DisplayName("Should create rubric successfully and return 201")
        void shouldCreateRubricSuccessfully() throws Exception {
            Question question = createQuestion("q-rubric-create");

            String body = """
                    {
                      "criteria": "Explain OOP principles clearly",
                      "keywords": ["encapsulation", "inheritance"],
                      "example_answer": "OOP has 4 pillars...",
                      "common_mistakes": ["Confusing abstraction with encapsulation"],
                      "hints": ["Think of real-world examples"],
                      "max_score": 10
                    }
                    """;

            mockMvc.perform(post("/v1/questions/{id}/rubric", question.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").isNotEmpty())
                    .andExpect(jsonPath("$.data.question_id").value(question.getId()))
                    .andExpect(jsonPath("$.data.criteria").value("Explain OOP principles clearly"))
                    .andExpect(jsonPath("$.data.keywords", hasSize(2)))
                    .andExpect(jsonPath("$.data.max_score").value(10))
                    .andExpect(jsonPath("$.data.is_active").value(true));
        }

        @Test
        @DisplayName("Should return 404 when question not found")
        void shouldReturn404WhenQuestionNotFound() throws Exception {
            String body = """
                    {
                      "criteria": "Some criteria",
                      "keywords": ["keyword1"],
                      "max_score": 10
                    }
                    """;

            mockMvc.perform(post("/v1/questions/{id}/rubric", "non-existent-id")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("QUE_001"));
        }

        @Test
        @DisplayName("Should return 409 when rubric already exists")
        void shouldReturn409WhenRubricAlreadyExists() throws Exception {
            Question question = createQuestion("q-rubric-dup");
            createRubric("rubric-dup", question.getId());

            String body = """
                    {
                      "criteria": "Another criteria",
                      "keywords": ["keyword1"],
                      "max_score": 5
                    }
                    """;

            mockMvc.perform(post("/v1/questions/{id}/rubric", question.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("RUB_002"));
        }

        @Test
        @DisplayName("Should return 400 when criteria is blank")
        void shouldReturn400WhenCriteriaBlank() throws Exception {
            Question question = createQuestion("q-rubric-val");

            String body = """
                    {
                      "criteria": "",
                      "max_score": 0
                    }
                    """;

            mockMvc.perform(post("/v1/questions/{id}/rubric", question.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    // ==================== GET /v1/questions/{id}/rubric Tests ====================

    @Nested
    @DisplayName("GET /v1/questions/{id}/rubric")
    class GetRubricTests {

        @Test
        @DisplayName("Should return rubric successfully")
        void shouldReturnRubricSuccessfully() throws Exception {
            Question question = createQuestion("q-rubric-get");
            Rubric rubric = createRubric("rubric-get", question.getId());

            mockMvc.perform(get("/v1/questions/{id}/rubric", question.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(rubric.getId()))
                    .andExpect(jsonPath("$.data.question_id").value(question.getId()))
                    .andExpect(jsonPath("$.data.criteria").value("Test criteria"))
                    .andExpect(jsonPath("$.data.is_active").value(true));
        }

        @Test
        @DisplayName("Should return 404 when question not found")
        void shouldReturn404WhenQuestionNotFound() throws Exception {
            mockMvc.perform(get("/v1/questions/{id}/rubric", "non-existent-id"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("QUE_001"));
        }

        @Test
        @DisplayName("Should return 404 when rubric not found for question")
        void shouldReturn404WhenRubricNotFound() throws Exception {
            Question question = createQuestion("q-no-rubric");

            mockMvc.perform(get("/v1/questions/{id}/rubric", question.getId()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("RUB_001"));
        }
    }

    // ==================== PUT /v1/questions/{id}/rubric Tests ====================

    @Nested
    @DisplayName("PUT /v1/questions/{id}/rubric")
    class UpdateRubricTests {

        @Test
        @DisplayName("Should update rubric successfully")
        void shouldUpdateRubricSuccessfully() throws Exception {
            Question question = createQuestion("q-rubric-update");
            createRubric("rubric-update", question.getId());

            String body = """
                    {
                      "criteria": "Updated criteria with more detail",
                      "keywords": ["SOLID", "design patterns"],
                      "example_answer": "Updated example answer",
                      "common_mistakes": ["New mistake 1", "New mistake 2"],
                      "hints": ["New hint"],
                      "max_score": 20
                    }
                    """;

            mockMvc.perform(put("/v1/questions/{id}/rubric", question.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.question_id").value(question.getId()))
                    .andExpect(jsonPath("$.data.criteria").value("Updated criteria with more detail"))
                    .andExpect(jsonPath("$.data.keywords", hasSize(2)))
                    .andExpect(jsonPath("$.data.max_score").value(20))
                    .andExpect(jsonPath("$.data.is_active").value(true));

            // Verify DB
            Rubric updated = rubricRepository.findByQuestionId(question.getId()).orElseThrow();
            assertThat(updated.getCriteria()).isEqualTo("Updated criteria with more detail");
            assertThat(updated.getMaxScore()).isEqualTo(20);
        }

        @Test
        @DisplayName("Should return 404 when question not found")
        void shouldReturn404WhenQuestionNotFound() throws Exception {
            String body = """
                    {
                      "criteria": "Some criteria",
                      "keywords": ["keyword1"],
                      "max_score": 10
                    }
                    """;

            mockMvc.perform(put("/v1/questions/{id}/rubric", "non-existent-id")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("QUE_001"));
        }

        @Test
        @DisplayName("Should return 404 when rubric not found for question")
        void shouldReturn404WhenRubricNotFound() throws Exception {
            Question question = createQuestion("q-no-rubric-update");

            String body = """
                    {
                      "criteria": "Some criteria",
                      "keywords": ["keyword1"],
                      "max_score": 10
                    }
                    """;

            mockMvc.perform(put("/v1/questions/{id}/rubric", question.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("RUB_001"));
        }
    }
}
