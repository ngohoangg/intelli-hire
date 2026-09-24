package org.aibles.intellihirequestion.controller;

import org.aibles.intellihirequestion.dto.BulkImportError;
import org.aibles.intellihirequestion.dto.BulkImportRequest;
import org.aibles.intellihirequestion.dto.BulkImportResponse;
import org.aibles.intellihirequestion.dto.QuestionRequest;
import org.aibles.intellihirequestion.dto.QuestionResponse;
import org.aibles.intellihirequestion.dto.RandomQuestionRequest;
import org.aibles.intellihirequestion.dto.TagResponse;
import org.aibles.intellihirequestion.entity.enums.QuestionType;
import org.aibles.intellihirequestion.exception.ErrorCode;
import org.aibles.intellihirequestion.exception.NotFoundException;
import org.aibles.intellihirequestion.service.QuestionService;
import org.aibles.intellihirequestion.service.RubricService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = QuestionController.class)
@ActiveProfiles("test")
class QuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuestionService questionService;

    @MockBean
    private RubricService rubricService;

    // ================== Test data helpers ==================
    private String buildValidCreateRequestBody() {
        return """
                {
                  "topic_id": "t_backend",
                  "difficulty_id": "d3",
                  "question_text": "Question text",
                  "question_type": "TECHNICAL",
                  "tag_ids": ["tag_oop", "tag_algo"],
                  "is_active": true
                }
                """;
    }

    private String buildValidUpdateRequestBody() {
        return """
                {
                  "topic_id": "t_backend",
                  "difficulty_id": "d3",
                  "question_text": "Updated question text",
                  "question_type": "TECHNICAL",
                  "tag_ids": ["tag_oop", "tag_algo"],
                  "is_active": true
                }
                """;
    }

    private QuestionResponse buildCreatedQuestionResponse() {
        TagResponse tag1 = TagResponse.builder()
                .id("tag_oop")
                .name("oop")
                .build();

        TagResponse tag2 = TagResponse.builder()
                .id("tag_algo")
                .name("algorithm")
                .build();

        return QuestionResponse.builder()
                .id("q_1")
                .topicId("t_backend")
                .topicName("Backend Development")
                .difficultyId("d3")
                .difficultyName("Medium")
                .questionText("Question text") // <-- cho case create
                .questionType(QuestionType.TECHNICAL.name())
                .tags(List.of(tag1, tag2))
                .isActive(true)
                .build();
    }

    private QuestionResponse buildUpdatedQuestionResponse() {
        TagResponse tag1 = TagResponse.builder()
                .id("tag_oop")
                .name("oop")
                .build();

        TagResponse tag2 = TagResponse.builder()
                .id("tag_algo")
                .name("algorithm")
                .build();

        return QuestionResponse.builder()
                .id("q_1")
                .topicId("t_backend")
                .topicName("Backend Development")
                .difficultyId("d3")
                .difficultyName("Medium")
                .questionText("Updated question text") // <-- cho case update
                .questionType(QuestionType.TECHNICAL.name())
                .tags(List.of(tag1, tag2))
                .isActive(true)
                .build();
    }

    // ========================================================================
    // CREATE - POST /v1/questions
    // ========================================================================
    @Nested
    @DisplayName("POST /v1/questions - Success (201)")
    class CreateQuestionSuccessTests {

        @Test
        @DisplayName("Should create question and return 201 with correct response structure")
        void shouldCreateQuestion_andReturn201() throws Exception {
            // Given
            QuestionResponse response = buildCreatedQuestionResponse();

            when(questionService.create(any(QuestionRequest.class), nullable(String.class), nullable(String.class)))
                    .thenReturn(response);

            // When & Then
            mockMvc.perform(post("/v1/questions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(buildValidCreateRequestBody()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.error_code").value(nullValue()))
                    .andExpect(jsonPath("$.errors").value(nullValue()))
                    .andExpect(jsonPath("$.timestamp").isNotEmpty())

                    // data (snake_case)
                    .andExpect(jsonPath("$.data.id").value("q_1"))
                    .andExpect(jsonPath("$.data.topic_id").value("t_backend"))
                    .andExpect(jsonPath("$.data.topic_name").value("Backend Development"))
                    .andExpect(jsonPath("$.data.difficulty_id").value("d3"))
                    .andExpect(jsonPath("$.data.difficulty_name").value("Medium"))
                    .andExpect(jsonPath("$.data.question_text").value("Question text"))
                    .andExpect(jsonPath("$.data.question_type").value("TECHNICAL"))
                    .andExpect(jsonPath("$.data.is_active").value(true))
                    .andExpect(jsonPath("$.data.tags").isArray())
                    .andExpect(jsonPath("$.data.tags.length()").value(2))
                    .andExpect(jsonPath("$.data.tags[0].id").value("tag_oop"))
                    .andExpect(jsonPath("$.data.tags[0].name").value("oop"))
                    .andExpect(jsonPath("$.data.tags[1].id").value("tag_algo"))
                    .andExpect(jsonPath("$.data.tags[1].name").value("algorithm"));
        }
    }

    @Nested
    @DisplayName("POST /v1/questions - Validation Error (400)")
    class CreateQuestionValidationErrorTests {

        @Test
        @DisplayName("Should return 400 when required fields are invalid (blank questionText)")
        void shouldReturn400_whenCreateRequestInvalid() throws Exception {
            String invalidBody = """
                    {
                      "topic_id": "t_backend",
                      "difficulty_id": "d3",
                      "question_text": "",
                      "question_type": "TECHNICAL",
                      "tag_ids": ["tag_oop"],
                      "is_active": true
                    }
                    """;

            mockMvc.perform(post("/v1/questions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("COM_001"))
                    .andExpect(jsonPath("$.data").doesNotExist())
                    .andExpect(jsonPath("$.errors").isArray());
        }

        @Test
        @DisplayName("Should return 400 when questionType is invalid enum value")
        void shouldReturn400_whenCreateQuestionTypeInvalid() throws Exception {
            String invalidBody = """
                    {
                      "topic_id": "t_backend",
                      "difficulty_id": "d3",
                      "question_text": "Some text",
                      "question_type": "INVALID_TYPE",
                      "tag_ids": ["tag_oop"],
                      "is_active": true
                    }
                    """;

            mockMvc.perform(post("/v1/questions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("COM_003"));
        }
    }

    @Nested
    @DisplayName("POST /v1/questions - NotFound / Business Error (404)")
    class CreateQuestionBusinessErrorTests {

        @Test
        @DisplayName("Should return 404 with TOP_001 when topic not found")
        void shouldReturn404_whenCreateTopicNotFound() throws Exception {
            when(questionService.create(any(QuestionRequest.class), nullable(String.class), nullable(String.class)))
                    .thenThrow(new NotFoundException(ErrorCode.TOP_001));

            mockMvc.perform(post("/v1/questions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(buildValidCreateRequestBody()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("TOP_001"));
        }

        @Test
        @DisplayName("Should return 404 with DIF_001 when difficulty not found")
        void shouldReturn404_whenCreateDifficultyNotFound() throws Exception {
            when(questionService.create(any(QuestionRequest.class), nullable(String.class), nullable(String.class)))
                    .thenThrow(new NotFoundException(ErrorCode.DIF_001));

            mockMvc.perform(post("/v1/questions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(buildValidCreateRequestBody()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("DIF_001"));
        }

        @Test
        @DisplayName("Should return 404 with TAG_001 when some tags not found")
        void shouldReturn404_whenCreateTagsNotFound() throws Exception {
            when(questionService.create(any(QuestionRequest.class), nullable(String.class), nullable(String.class)))
                    .thenThrow(new NotFoundException(ErrorCode.TAG_001));

            mockMvc.perform(post("/v1/questions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(buildValidCreateRequestBody()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("TAG_001"));
        }
    }

    // ========================================================================
    // UPDATE - PUT /v1/questions/{id}
    // ========================================================================
    @Nested
    @DisplayName("PUT /v1/questions/{id} - Success (200)")
    class UpdateQuestionSuccessTests {

        @Test
        @DisplayName("Should update question and return 200 with correct response structure")
        void shouldUpdateQuestion_andReturn200() throws Exception {
            // Given
            String questionId = "q_1";
            QuestionResponse response = buildUpdatedQuestionResponse();
            when(questionService.update(eq(questionId), any(QuestionRequest.class), nullable(String.class)))
                    .thenReturn(response);

            // When & Then
            mockMvc.perform(put("/v1/questions/{id}", questionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(buildValidUpdateRequestBody()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.error_code").value(nullValue()))
                    .andExpect(jsonPath("$.errors").value(nullValue()))
                    .andExpect(jsonPath("$.timestamp").isNotEmpty())

                    // data (snake_case)
                    .andExpect(jsonPath("$.data.id").value("q_1"))
                    .andExpect(jsonPath("$.data.topic_id").value("t_backend"))
                    .andExpect(jsonPath("$.data.topic_name").value("Backend Development"))
                    .andExpect(jsonPath("$.data.difficulty_id").value("d3"))
                    .andExpect(jsonPath("$.data.difficulty_name").value("Medium"))
                    .andExpect(jsonPath("$.data.question_text").value("Updated question text"))
                    .andExpect(jsonPath("$.data.question_type").value("TECHNICAL"))
                    .andExpect(jsonPath("$.data.is_active").value(true))
                    .andExpect(jsonPath("$.data.tags").isArray())
                    .andExpect(jsonPath("$.data.tags.length()").value(2))
                    .andExpect(jsonPath("$.data.tags[0].id").value("tag_oop"))
                    .andExpect(jsonPath("$.data.tags[0].name").value("oop"))
                    .andExpect(jsonPath("$.data.tags[1].id").value("tag_algo"))
                    .andExpect(jsonPath("$.data.tags[1].name").value("algorithm"));
        }
    }

    @Nested
    @DisplayName("PUT /v1/questions/{id} - Validation Error (400)")
    class UpdateQuestionValidationErrorTests {

        @Test
        @DisplayName("Should return 400 when required fields are invalid (e.g. blank questionText)")
        void shouldReturn400_whenRequestInvalid() throws Exception {
            String invalidBody = """
                    {
                      "topic_id": "t_backend",
                      "difficulty_id": "d3",
                      "question_text": "",
                      "question_type": "TECHNICAL",
                      "tag_ids": ["tag_oop"],
                      "is_active": true
                    }
                    """;

            mockMvc.perform(put("/v1/questions/{id}", "q_1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("COM_001"))
                    .andExpect(jsonPath("$.data").doesNotExist())
                    .andExpect(jsonPath("$.errors").isArray());
        }

        @Test
        @DisplayName("Should return 400 when questionType is invalid enum value")
        void shouldReturn400_whenQuestionTypeInvalid() throws Exception {
            String invalidBody = """
                    {
                      "topic_id": "t_backend",
                      "difficulty_id": "d3",
                      "question_text": "Some text",
                      "question_type": "INVALID_TYPE",
                      "tag_ids": ["tag_oop"],
                      "is_active": true
                    }
                    """;

            mockMvc.perform(put("/v1/questions/{id}", "q_1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("COM_003"));
        }
    }

    @Nested
    @DisplayName("PUT /v1/questions/{id} - NotFound / Business Error")
    class UpdateQuestionBusinessErrorTests {

        @Test
        @DisplayName("Should return 404 with QUE_001 when question not found")
        void shouldReturn404_whenQuestionNotFound() throws Exception {
            String questionId = "q_missing";

            when(questionService.update(eq(questionId), any(QuestionRequest.class), nullable(String.class)))
                    .thenThrow(new NotFoundException(ErrorCode.QUE_001));

            mockMvc.perform(put("/v1/questions/{id}", questionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(buildValidUpdateRequestBody()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("QUE_001"))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("Should return 404 with TOP_001 when topic not found")
        void shouldReturn404_whenTopicNotFound() throws Exception {
            when(questionService.update(eq("q_1"), any(QuestionRequest.class), nullable(String.class)))
                    .thenThrow(new NotFoundException(ErrorCode.TOP_001));

            mockMvc.perform(put("/v1/questions/{id}", "q_1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(buildValidUpdateRequestBody()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("TOP_001"));
        }

        @Test
        @DisplayName("Should return 404 with TOP_003 when topic is inactive")
        void shouldReturn404_whenTopicInactive() throws Exception {
            when(questionService.update(eq("q_1"), any(QuestionRequest.class), nullable(String.class)))
                    .thenThrow(new NotFoundException(ErrorCode.TOP_003));

            mockMvc.perform(put("/v1/questions/{id}", "q_1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(buildValidUpdateRequestBody()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("TOP_003"));
        }

        @Test
        @DisplayName("Should return 404 with DIF_001 when difficulty not found")
        void shouldReturn404_whenDifficultyNotFound() throws Exception {
            when(questionService.update(eq("q_1"), any(QuestionRequest.class), nullable(String.class)))
                    .thenThrow(new NotFoundException(ErrorCode.DIF_001));

            mockMvc.perform(put("/v1/questions/{id}", "q_1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(buildValidUpdateRequestBody()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("DIF_001"));
        }

        @Test
        @DisplayName("Should return 404 with TAG_001 when some tags not found")
        void shouldReturn404_whenTagsNotFound() throws Exception {
            when(questionService.update(eq("q_1"), any(QuestionRequest.class), nullable(String.class)))
                    .thenThrow(new NotFoundException(ErrorCode.TAG_001));

            mockMvc.perform(put("/v1/questions/{id}", "q_1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(buildValidUpdateRequestBody()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("TAG_001"));
        }
    }

    // ========================================================================
    // GET RANDOM - GET /v1/questions/random
    // ========================================================================

    private List<QuestionResponse> buildRandomQuestionResponses(int count) {
        List<QuestionResponse> responses = new java.util.ArrayList<>();
        for (int i = 1; i <= count; i++) {
            responses.add(QuestionResponse.builder()
                    .id("q_" + i)
                    .topicId("t_backend")
                    .topicName("Backend Development")
                    .difficultyId("d3")
                    .difficultyName("Medium")
                    .questionText("Random question " + i)
                    .questionType(QuestionType.TECHNICAL.name())
                    .tags(List.of())
                    .isActive(true)
                    .build());
        }
        return responses;
    }

    @Nested
    @DisplayName("GET /v1/questions/random - Success (200)")
    class GetRandomQuestionsSuccessTests {

        @Test
        @DisplayName("Should return 200 with random questions when valid params")
        void shouldReturn200_withRandomQuestions() throws Exception {
            // Given
            List<QuestionResponse> responses = buildRandomQuestionResponses(3);
            when(questionService.getRandomQuestions(any(RandomQuestionRequest.class)))
                    .thenReturn(responses);

            // When & Then
            mockMvc.perform(get("/v1/questions/random")
                            .param("count", "3")
                            .param("topicId", "t_backend")
                            .param("difficultyId", "d3"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.error_code").value(nullValue()))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(3))
                    .andExpect(jsonPath("$.data[0].id").value("q_1"))
                    .andExpect(jsonPath("$.data[0].topic_id").value("t_backend"))
                    .andExpect(jsonPath("$.data[0].question_text").value("Random question 1"));
        }

        @Test
        @DisplayName("Should return 200 with only count param (no filters)")
        void shouldReturn200_withOnlyCount() throws Exception {
            // Given
            List<QuestionResponse> responses = buildRandomQuestionResponses(2);
            when(questionService.getRandomQuestions(any(RandomQuestionRequest.class)))
                    .thenReturn(responses);

            // When & Then
            mockMvc.perform(get("/v1/questions/random")
                            .param("count", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2));
        }
    }

    @Nested
    @DisplayName("GET /v1/questions/random - Validation Error (400)")
    class GetRandomQuestionsValidationTests {

        @Test
        @DisplayName("Should return 400 when count is missing")
        void shouldReturn400_whenCountMissing() throws Exception {
            mockMvc.perform(get("/v1/questions/random")
                            .param("topicId", "t_backend"))
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
        @DisplayName("Should return 400 when count exceeds 50")
        void shouldReturn400_whenCountExceeds50() throws Exception {
            mockMvc.perform(get("/v1/questions/random")
                            .param("count", "51"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("GET /v1/questions/random - NotFound (404)")
    class GetRandomQuestionsNotFoundTests {

        @Test
        @DisplayName("Should return 404 with TOP_001 when topic not found")
        void shouldReturn404_whenTopicNotFound() throws Exception {
            when(questionService.getRandomQuestions(any(RandomQuestionRequest.class)))
                    .thenThrow(new NotFoundException(ErrorCode.TOP_001));

            mockMvc.perform(get("/v1/questions/random")
                            .param("count", "3")
                            .param("topicId", "non-existent"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("TOP_001"));
        }

        @Test
        @DisplayName("Should return 404 with QUE_005 when not enough questions")
        void shouldReturn404_whenNotEnoughQuestions() throws Exception {
            when(questionService.getRandomQuestions(any(RandomQuestionRequest.class)))
                    .thenThrow(new NotFoundException("QUE_005", "Not enough questions available"));

            mockMvc.perform(get("/v1/questions/random")
                            .param("count", "10"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("QUE_005"));
        }
    }

    // ========================================================================
    // BULK IMPORT - POST /v1/questions/bulk
    // ========================================================================

    private String buildValidBulkImportBody() {
        return """
                {
                  "questions": [
                    {
                      "topic_id": "t_backend",
                      "difficulty_id": "d3",
                      "question_text": "What is dependency injection?",
                      "question_type": "TECHNICAL",
                      "tag_ids": ["tag_oop"]
                    },
                    {
                      "topic_id": "t_backend",
                      "difficulty_id": "d3",
                      "question_text": "Explain OOP principles",
                      "question_type": "TECHNICAL",
                      "tag_ids": ["tag_oop"]
                    }
                  ]
                }
                """;
    }

    @Nested
    @DisplayName("POST /v1/questions/bulk - All Valid (200)")
    class BulkImportAllValidTests {

        @Test
        @DisplayName("Should return 200 with all questions created successfully")
        void shouldReturn200_whenAllQuestionsValid() throws Exception {
            BulkImportResponse response = BulkImportResponse.builder()
                    .total(2)
                    .created(2)
                    .failed(0)
                    .errors(List.of())
                    .build();

            when(questionService.bulkImport(any(BulkImportRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/v1/questions/bulk")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(buildValidBulkImportBody()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.error_code").value(nullValue()))
                    .andExpect(jsonPath("$.data.total").value(2))
                    .andExpect(jsonPath("$.data.created").value(2))
                    .andExpect(jsonPath("$.data.failed").value(0))
                    .andExpect(jsonPath("$.data.errors").isArray())
                    .andExpect(jsonPath("$.data.errors.length()").value(0));
        }
    }

    @Nested
    @DisplayName("POST /v1/questions/bulk - Partial Success (200)")
    class BulkImportPartialSuccessTests {

        @Test
        @DisplayName("Should return 200 with partial success report")
        void shouldReturn200_withPartialSuccess() throws Exception {
            BulkImportResponse response = BulkImportResponse.builder()
                    .total(3)
                    .created(2)
                    .failed(1)
                    .errors(List.of(new BulkImportError(1, "Topic not found: bad-topic")))
                    .build();

            when(questionService.bulkImport(any(BulkImportRequest.class)))
                    .thenReturn(response);

            String body = """
                    {
                      "questions": [
                        {
                          "topic_id": "t_backend",
                          "difficulty_id": "d3",
                          "question_text": "Valid question 1",
                          "question_type": "TECHNICAL",
                          "tag_ids": ["tag_oop"]
                        },
                        {
                          "topic_id": "bad-topic",
                          "difficulty_id": "d3",
                          "question_text": "Invalid question",
                          "question_type": "TECHNICAL",
                          "tag_ids": ["tag_oop"]
                        },
                        {
                          "topic_id": "t_backend",
                          "difficulty_id": "d3",
                          "question_text": "Valid question 2",
                          "question_type": "TECHNICAL",
                          "tag_ids": ["tag_oop"]
                        }
                      ]
                    }
                    """;

            mockMvc.perform(post("/v1/questions/bulk")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.total").value(3))
                    .andExpect(jsonPath("$.data.created").value(2))
                    .andExpect(jsonPath("$.data.failed").value(1))
                    .andExpect(jsonPath("$.data.errors.length()").value(1))
                    .andExpect(jsonPath("$.data.errors[0].index").value(1))
                    .andExpect(jsonPath("$.data.errors[0].error").value("Topic not found: bad-topic"));
        }
    }

    @Nested
    @DisplayName("POST /v1/questions/bulk - All Invalid (200)")
    class BulkImportAllInvalidTests {

        @Test
        @DisplayName("Should return 200 with all questions failed")
        void shouldReturn200_whenAllQuestionsInvalid() throws Exception {
            BulkImportResponse response = BulkImportResponse.builder()
                    .total(2)
                    .created(0)
                    .failed(2)
                    .errors(List.of(
                            new BulkImportError(0, "Topic not found: bad-topic"),
                            new BulkImportError(1, "Difficulty level not found: bad-diff")
                    ))
                    .build();

            when(questionService.bulkImport(any(BulkImportRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/v1/questions/bulk")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(buildValidBulkImportBody()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.total").value(2))
                    .andExpect(jsonPath("$.data.created").value(0))
                    .andExpect(jsonPath("$.data.failed").value(2))
                    .andExpect(jsonPath("$.data.errors.length()").value(2));
        }
    }

    @Nested
    @DisplayName("POST /v1/questions/bulk - Duplicate Text (200)")
    class BulkImportDuplicateTextTests {

        @Test
        @DisplayName("Should return 200 with duplicate text error")
        void shouldReturn200_withDuplicateTextError() throws Exception {
            BulkImportResponse response = BulkImportResponse.builder()
                    .total(2)
                    .created(1)
                    .failed(1)
                    .errors(List.of(new BulkImportError(1, "Duplicate question text in batch")))
                    .build();

            when(questionService.bulkImport(any(BulkImportRequest.class)))
                    .thenReturn(response);

            String body = """
                    {
                      "questions": [
                        {
                          "topic_id": "t_backend",
                          "difficulty_id": "d3",
                          "question_text": "Same question",
                          "question_type": "TECHNICAL",
                          "tag_ids": ["tag_oop"]
                        },
                        {
                          "topic_id": "t_backend",
                          "difficulty_id": "d3",
                          "question_text": "Same question",
                          "question_type": "TECHNICAL",
                          "tag_ids": ["tag_oop"]
                        }
                      ]
                    }
                    """;

            mockMvc.perform(post("/v1/questions/bulk")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.created").value(1))
                    .andExpect(jsonPath("$.data.failed").value(1))
                    .andExpect(jsonPath("$.data.errors[0].index").value(1))
                    .andExpect(jsonPath("$.data.errors[0].error").value("Duplicate question text in batch"));
        }
    }

    @Nested
    @DisplayName("POST /v1/questions/bulk - Validation Error (400)")
    class BulkImportValidationErrorTests {

        @Test
        @DisplayName("Should return 400 when questions list is empty")
        void shouldReturn400_whenQuestionsListEmpty() throws Exception {
            String body = """
                    {
                      "questions": []
                    }
                    """;

            mockMvc.perform(post("/v1/questions/bulk")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should return 400 when questions field is missing")
        void shouldReturn400_whenQuestionsFieldMissing() throws Exception {
            String body = "{}";

            mockMvc.perform(post("/v1/questions/bulk")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should return 400 when a question in list has blank question_text")
        void shouldReturn400_whenQuestionInListInvalid() throws Exception {
            String body = """
                    {
                      "questions": [
                        {
                          "topic_id": "t_backend",
                          "difficulty_id": "d3",
                          "question_text": "",
                          "question_type": "TECHNICAL"
                        }
                      ]
                    }
                    """;

            mockMvc.perform(post("/v1/questions/bulk")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should return 400 when exceeds 100 questions")
        void shouldReturn400_whenExceeds100Questions() throws Exception {
            StringBuilder sb = new StringBuilder("{\"questions\": [");
            for (int i = 0; i < 101; i++) {
                if (i > 0) sb.append(",");
                sb.append(String.format("""
                        {
                          "topic_id": "t_backend",
                          "difficulty_id": "d3",
                          "question_text": "Question %d",
                          "question_type": "TECHNICAL"
                        }
                        """, i));
            }
            sb.append("]}");

            mockMvc.perform(post("/v1/questions/bulk")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(sb.toString()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}
