package org.aibles.intellihirequestion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aibles.intellihirequestion.dto.TopicRequest;
import org.aibles.intellihirequestion.entity.Question;
import org.aibles.intellihirequestion.entity.Topic;
import org.aibles.intellihirequestion.entity.enums.QuestionType;
import org.aibles.intellihirequestion.exception.ErrorCode;
import org.aibles.intellihirequestion.config.RedisTestConfig;
import org.aibles.intellihirequestion.repository.QuestionRepository;
import org.aibles.intellihirequestion.repository.TopicRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.annotation.Import;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(RedisTestConfig.class)
class TopicControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @BeforeEach
    void setUp() {
        questionRepository.deleteAll();
        topicRepository.deleteAll();
    }

    @Nested
    @DisplayName("POST /v1/topics - Success (201)")
    class CreateTopicSuccessTests {

        @Test
        @DisplayName("Should create root topic and return 201")
        void shouldCreateRootTopic_andReturn201() throws Exception {
            // Given
            TopicRequest request = TopicRequest.builder()
                    .name("Java")
                    .description("Java programming language")
                    .order(1)
                    .isActive(true)
                    .build();

            // When & Then
            mockMvc.perform(post("/v1/topics")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").isNotEmpty())
                    .andExpect(jsonPath("$.data.name").value("Java"))
                    .andExpect(jsonPath("$.data.description").value("Java programming language"))
                    .andExpect(jsonPath("$.data.parent_id").isEmpty())
                    .andExpect(jsonPath("$.data.order").value(1))
                    .andExpect(jsonPath("$.data.is_active").value(true));
        }

        @Test
        @DisplayName("Should create child topic and return 201")
        void shouldCreateChildTopic_andReturn201() throws Exception {
            // Given - Create parent first
            Topic parent = new Topic();
            parent.setId("parent-uuid");
            parent.setName("Programming");
            parent.setIsActive(true);
            parent.setOrder(1);
            topicRepository.save(parent);

            TopicRequest request = TopicRequest.builder()
                    .name("Java")
                    .description("Java programming")
                    .parentId("parent-uuid")
                    .order(1)
                    .build();

            // When & Then
            mockMvc.perform(post("/v1/topics")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("Java"))
                    .andExpect(jsonPath("$.data.parent_id").value("parent-uuid"));
        }
    }

    @Nested
    @DisplayName("POST /v1/topics - 400 Validation Error")
    class CreateTopicValidationErrorTests {

        @Test
        @DisplayName("Should return 400 when name is blank")
        void shouldReturn400_whenNameIsBlank() throws Exception {
            // Given
            TopicRequest request = TopicRequest.builder()
                    .name("")
                    .description("Some description")
                    .build();

            // When & Then
            mockMvc.perform(post("/v1/topics")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("COM_001"));
        }

        @Test
        @DisplayName("Should return 400 when name is null")
        void shouldReturn400_whenNameIsNull() throws Exception {
            // Given
            TopicRequest request = TopicRequest.builder()
                    .name(null)
                    .description("Some description")
                    .build();

            // When & Then
            mockMvc.perform(post("/v1/topics")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("COM_001"));
        }

        @Test
        @DisplayName("Should return 400 when name exceeds 255 characters")
        void shouldReturn400_whenNameExceeds255Characters() throws Exception {
            // Given
            String longName = "A".repeat(256);
            TopicRequest request = TopicRequest.builder()
                    .name(longName)
                    .build();

            // When & Then
            mockMvc.perform(post("/v1/topics")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("COM_001"));
        }
    }

    @Nested
    @DisplayName("POST /v1/topics - 404 Parent Not Found")
    class CreateTopicParentNotFoundTests {

        @Test
        @DisplayName("Should return 404 when parent not found")
        void shouldReturn404_whenParentNotFound() throws Exception {
            // Given
            TopicRequest request = TopicRequest.builder()
                    .name("Java")
                    .parentId("non-existent-uuid")
                    .build();

            // When & Then
            mockMvc.perform(post("/v1/topics")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("TOP_001"));
        }

        @Test
        @DisplayName("Should return 404 when parent is inactive")
        void shouldReturn404_whenParentIsInactive() throws Exception {
            // Given - Create inactive parent
            Topic inactiveParent = new Topic();
            inactiveParent.setId("inactive-parent-uuid");
            inactiveParent.setName("Inactive Topic");
            inactiveParent.setIsActive(false);
            inactiveParent.setOrder(1);
            topicRepository.save(inactiveParent);

            TopicRequest request = TopicRequest.builder()
                    .name("Java")
                    .parentId("inactive-parent-uuid")
                    .build();

            // When & Then
            mockMvc.perform(post("/v1/topics")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("TOP_001"));
        }
    }

    @Nested
    @DisplayName("POST /v1/topics - 409 Duplicate")
    class CreateTopicDuplicateTests {

        @Test
        @DisplayName("Should return 409 when root topic name already exists")
        void shouldReturn409_whenRootTopicNameExists() throws Exception {
            // Given - Create existing topic
            Topic existing = new Topic();
            existing.setId("existing-uuid");
            existing.setName("Java");
            existing.setIsActive(true);
            existing.setOrder(1);
            topicRepository.save(existing);

            TopicRequest request = TopicRequest.builder()
                    .name("Java")
                    .build();

            // When & Then
            mockMvc.perform(post("/v1/topics")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("TOP_003"));
        }

        @Test
        @DisplayName("Should return 409 when child topic name exists under same parent")
        void shouldReturn409_whenChildTopicNameExistsUnderSameParent() throws Exception {
            // Given - Create parent and existing child
            Topic parent = new Topic();
            parent.setId("parent-uuid");
            parent.setName("Programming");
            parent.setIsActive(true);
            parent.setOrder(1);
            topicRepository.save(parent);

            Topic existingChild = new Topic();
            existingChild.setId("child-uuid");
            existingChild.setName("Java");
            existingChild.setParentId("parent-uuid");
            existingChild.setIsActive(true);
            existingChild.setOrder(1);
            topicRepository.save(existingChild);

            TopicRequest request = TopicRequest.builder()
                    .name("Java")
                    .parentId("parent-uuid")
                    .build();

            // When & Then
            mockMvc.perform(post("/v1/topics")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("TOP_003"));
        }

        @Test
        @DisplayName("Should allow same name under different parents")
        void shouldAllowSameNameUnderDifferentParents() throws Exception {
            // Given - Create two parents
            Topic parent1 = new Topic();
            parent1.setId("parent-1-uuid");
            parent1.setName("Programming");
            parent1.setIsActive(true);
            parent1.setOrder(1);
            topicRepository.save(parent1);

            Topic parent2 = new Topic();
            parent2.setId("parent-2-uuid");
            parent2.setName("Frameworks");
            parent2.setIsActive(true);
            parent2.setOrder(2);
            topicRepository.save(parent2);

            // Create "Basics" under parent1
            Topic existingChild = new Topic();
            existingChild.setId("child-uuid");
            existingChild.setName("Basics");
            existingChild.setParentId("parent-1-uuid");
            existingChild.setIsActive(true);
            existingChild.setOrder(1);
            topicRepository.save(existingChild);

            // Try to create "Basics" under parent2 - should succeed
            TopicRequest request = TopicRequest.builder()
                    .name("Basics")
                    .parentId("parent-2-uuid")
                    .build();

            // When & Then
            mockMvc.perform(post("/v1/topics")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("Basics"))
                    .andExpect(jsonPath("$.data.parent_id").value("parent-2-uuid"));
        }
    }

    /**
     * Tests for DELETE /v1/topics/{id}
     */
    @Nested
    @DisplayName("DELETE /v1/topics/{id}")
    class DeleteTopicTests {

        @Test
        @DisplayName("Should delete topic successfully and return 200")
        void shouldDeleteTopicSuccessfully() throws Exception {
            String targetId = "deletable-uuid";
            Topic topic = new Topic();
            topic.setId(targetId);
            topic.setName("Java Core");
            topic.setIsActive(true);
            topicRepository.save(topic);

            mockMvc.perform(delete("/v1/topics/" + targetId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            Topic updated = topicRepository.findById(targetId).orElseThrow();
            assertThat(updated.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should return 404 when topic not found or already inactive")
        void shouldReturn404_whenNotFound() throws Exception {
            mockMvc.perform(delete("/v1/topics/non-existent-id"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.TOP_001.getCode()));
        }

        @Test
        @DisplayName("Should return 400 when has active children (TOP_004)")
        void shouldReturn400_whenHasChildren() throws Exception {
            Topic parent = new Topic();
            parent.setId("p-1");
            parent.setName("Programming");
            parent.setIsActive(true);
            topicRepository.save(parent);

            Topic child = new Topic();
            child.setId("c-1");
            child.setParentId("p-1");
            child.setName("Java");
            child.setIsActive(true);
            topicRepository.save(child);

            mockMvc.perform(delete("/v1/topics/p-1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.TOP_004.getCode()));
        }

        @Test
        @DisplayName("Should return 400 when has active questions (TOP_005)")
        void shouldReturn400_whenHasQuestions() throws Exception {
            // 1. Tạo Topic
            String topicId = "topic-with-questions-id";
            Topic topic = new Topic();
            topic.setId(topicId);
            topic.setName("Spring Boot");
            topic.setIsActive(true);
            topicRepository.save(topic);

            // 2. Tạo Question - Cung cấp đầy đủ các trường NOT NULL
            Question question = new Question();
            question.setId("q-1");
            question.setTopicId(topicId);
            question.setQuestionText("What is Spring?");
            question.setQuestionType(QuestionType.TECHNICAL);
            question.setIsActive(true);
            question.setDifficultyId("easy-difficulty-id");
            questionRepository.save(question);

            // 3. Thực thi request
            mockMvc.perform(delete("/v1/topics/" + topicId))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.TOP_005.getCode()));
        }
    }

    @Nested
    @DisplayName("GET /v1/topics/{id}/questions")
    class GetQuestionsByTopicIdTests {

        @Test
        @DisplayName("Should return questions for existing topic")
        void shouldReturnQuestionsForTopic() throws Exception {
            // Given
            Topic topic = new Topic();
            topic.setId("topic-q-1");
            topic.setName("Java");
            topic.setIsActive(true);
            topic.setOrder(1);
            topicRepository.save(topic);

            Question q1 = new Question();
            q1.setId("q-1");
            q1.setTopicId("topic-q-1");
            q1.setQuestionText("What is JVM?");
            q1.setQuestionType(QuestionType.TECHNICAL);
            q1.setIsActive(true);
            q1.setDifficultyId("diff-1");
            questionRepository.save(q1);

            Question q2 = new Question();
            q2.setId("q-2");
            q2.setTopicId("topic-q-1");
            q2.setQuestionText("What is JDK?");
            q2.setQuestionType(QuestionType.TECHNICAL);
            q2.setIsActive(true);
            q2.setDifficultyId("diff-1");
            questionRepository.save(q2);

            // When & Then
            mockMvc.perform(get("/v1/topics/topic-q-1/questions")
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(2))
                    .andExpect(jsonPath("$.data.total_elements").value(2))
                    .andExpect(jsonPath("$.data.page").value(0));
        }

        @Test
        @DisplayName("Should return 404 when topic not found")
        void shouldReturn404WhenTopicNotFound() throws Exception {
            mockMvc.perform(get("/v1/topics/non-existent-id/questions"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.TOP_001.getCode()));
        }

        @Test
        @DisplayName("Should return empty result for topic with no questions")
        void shouldReturnEmptyForTopicWithNoQuestions() throws Exception {
            // Given
            Topic topic = new Topic();
            topic.setId("empty-topic");
            topic.setName("Empty Topic");
            topic.setIsActive(true);
            topic.setOrder(1);
            topicRepository.save(topic);

            // When & Then
            mockMvc.perform(get("/v1/topics/empty-topic/questions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(0))
                    .andExpect(jsonPath("$.data.total_elements").value(0));
        }

        @Test
        @DisplayName("Should return 423 when topic is inactive")
        void shouldReturn423WhenTopicInactive() throws Exception {
            // Given
            Topic inactiveTopic = new Topic();
            inactiveTopic.setId("inactive-topic");
            inactiveTopic.setName("Inactive Topic");
            inactiveTopic.setIsActive(false);
            inactiveTopic.setOrder(1);
            topicRepository.save(inactiveTopic);

            // When & Then
            mockMvc.perform(get("/v1/topics/inactive-topic/questions"))
                    .andExpect(status().isLocked())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.TOP_004.getCode()));
        }

        @Test
        @DisplayName("Should only return active questions")
        void shouldOnlyReturnActiveQuestions() throws Exception {
            // Given
            Topic topic = new Topic();
            topic.setId("topic-active");
            topic.setName("Spring");
            topic.setIsActive(true);
            topic.setOrder(1);
            topicRepository.save(topic);

            Question active = new Question();
            active.setId("q-active");
            active.setTopicId("topic-active");
            active.setQuestionText("What is Spring?");
            active.setQuestionType(QuestionType.TECHNICAL);
            active.setIsActive(true);
            active.setDifficultyId("diff-1");
            questionRepository.save(active);

            Question inactive = new Question();
            inactive.setId("q-inactive");
            inactive.setTopicId("topic-active");
            inactive.setQuestionText("What is Spring Boot?");
            inactive.setQuestionType(QuestionType.TECHNICAL);
            inactive.setIsActive(false);
            inactive.setDifficultyId("diff-1");
            questionRepository.save(inactive);

            // When & Then
            mockMvc.perform(get("/v1/topics/topic-active/questions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.data.total_elements").value(1));
        }
    }
}
