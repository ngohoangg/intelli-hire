package org.aibles.intellihirequestion.controller;

import org.aibles.intellihirequestion.dto.GenerateQuestionsFromResumeRequest;
import org.aibles.intellihirequestion.dto.GenerateQuestionsFromResumeResponse;
import org.aibles.intellihirequestion.dto.GeneratedQuestionItemResponse;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = QuestionController.class)
@ActiveProfiles("test")
class QuestionGenerateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuestionService questionService;

    @MockBean
    private RubricService rubricService;

    @Test
    @DisplayName("POST /v1/questions/generate - should return generated questions")
    void shouldGenerateQuestionsFromResume() throws Exception {
        GenerateQuestionsFromResumeResponse response = GenerateQuestionsFromResumeResponse.builder()
                .resumeId("resume-1")
                .candidateSummary("Backend Java developer with Spring Boot experience.")
                .totalQuestions(2)
                .generatedAt(LocalDateTime.now())
                .questions(List.of(
                        GeneratedQuestionItemResponse.builder()
                                .questionText("Explain dependency injection in Spring Boot.")
                                .questionType("TECHNICAL")
                                .topicId("topic-1")
                                .topicName("Spring Boot")
                                .difficultyId("dif-2")
                                .difficultyName("Mid")
                                .rationale("Candidate has recent Spring Boot backend experience.")
                                .build(),
                        GeneratedQuestionItemResponse.builder()
                                .questionText("Describe a production issue you resolved recently.")
                                .questionType("BEHAVIORAL")
                                .topicId("topic-2")
                                .topicName("Problem Solving")
                                .difficultyId("dif-2")
                                .difficultyName("Mid")
                                .rationale("Resume shows hands-on delivery experience.")
                                .build()
                ))
                .build();

        when(questionService.generateFromResume(eq("user-1"), any(GenerateQuestionsFromResumeRequest.class)))
                .thenReturn(response);

        String body = """
                {
                  "resume_id": "resume-1",
                  "count": 2,
                  "language": "en"
                }
                """;

        mockMvc.perform(post("/v1/questions/generate")
                        .header("X-User-Id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error_code").value(nullValue()))
                .andExpect(jsonPath("$.data.resume_id").value("resume-1"))
                .andExpect(jsonPath("$.data.total_questions").value(2))
                .andExpect(jsonPath("$.data.questions[0].question_text").value("Explain dependency injection in Spring Boot."))
                .andExpect(jsonPath("$.data.questions[0].question_type").value("TECHNICAL"))
                .andExpect(jsonPath("$.data.questions[0].topic_name").value("Spring Boot"));
    }

    @Test
    @DisplayName("POST /v1/questions/generate - should return 400 when resume_id is blank")
    void shouldReturn400WhenResumeIdBlank() throws Exception {
        String body = """
                {
                  "resume_id": "",
                  "count": 2
                }
                """;

        mockMvc.perform(post("/v1/questions/generate")
                        .header("X-User-Id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value("COM_001"));
    }

    // ========================================================================
    // POST /v1/questions/generate - Count validation
    // ========================================================================
    @Nested
    @DisplayName("POST /v1/questions/generate - Count boundary (400)")
    class GenerateCountValidationTests {

        @Test
        @DisplayName("Should return 400 when count is 0")
        void shouldReturn400_whenCountIsZero() throws Exception {
            mockMvc.perform(post("/v1/questions/generate")
                            .header("X-User-Id", "user-1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "resume_id": "resume-1", "count": 0 }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("COM_001"))
                    .andExpect(jsonPath("$.errors").isArray());
        }

        @Test
        @DisplayName("Should return 400 when count exceeds 20")
        void shouldReturn400_whenCountExceeds20() throws Exception {
            mockMvc.perform(post("/v1/questions/generate")
                            .header("X-User-Id", "user-1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "resume_id": "resume-1", "count": 21 }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("COM_001"));
        }

        @Test
        @DisplayName("Should return 400 when count is negative")
        void shouldReturn400_whenCountIsNegative() throws Exception {
            mockMvc.perform(post("/v1/questions/generate")
                            .header("X-User-Id", "user-1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "resume_id": "resume-1", "count": -1 }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("COM_001"));
        }
    }

    // ========================================================================
    // POST /v1/questions/generate - Optional fields
    // ========================================================================
    @Nested
    @DisplayName("POST /v1/questions/generate - Optional fields (200)")
    class GenerateOptionalFieldsTests {

        @Test
        @DisplayName("Should return 200 with all optional fields provided")
        void shouldReturn200_withAllOptionalFields() throws Exception {
            GenerateQuestionsFromResumeResponse response = GenerateQuestionsFromResumeResponse.builder()
                    .resumeId("resume-1")
                    .candidateSummary("Senior Java developer.")
                    .totalQuestions(1)
                    .generatedAt(LocalDateTime.now())
                    .questions(List.of(
                            GeneratedQuestionItemResponse.builder()
                                    .questionText("How do you design a distributed system?")
                                    .questionType("BEHAVIORAL")
                                    .topicName("Architecture")
                                    .difficultyName("Senior")
                                    .rationale("Strong leadership and design background.")
                                    .build()
                    ))
                    .build();

            when(questionService.generateFromResume(eq("user-1"), any(GenerateQuestionsFromResumeRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/v1/questions/generate")
                            .header("X-User-Id", "user-1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "resume_id": "resume-1",
                                      "count": 1,
                                      "question_type": "BEHAVIORAL",
                                      "difficulty_name": "Senior",
                                      "language": "Vietnamese",
                                      "focus_areas": ["leadership", "system-design"]
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.resume_id").value("resume-1"))
                    .andExpect(jsonPath("$.data.total_questions").value(1))
                    .andExpect(jsonPath("$.data.questions[0].question_type").value("BEHAVIORAL"))
                    .andExpect(jsonPath("$.data.questions[0].difficulty_name").value("Senior"))
                    .andExpect(jsonPath("$.data.questions[0].rationale").value("Strong leadership and design background."));
        }

        @Test
        @DisplayName("Should return 200 with default count when count is omitted")
        void shouldReturn200_whenCountOmitted() throws Exception {
            GenerateQuestionsFromResumeResponse response = GenerateQuestionsFromResumeResponse.builder()
                    .resumeId("resume-1")
                    .candidateSummary("Mid Java developer.")
                    .totalQuestions(0)
                    .generatedAt(LocalDateTime.now())
                    .questions(List.of())
                    .build();

            when(questionService.generateFromResume(eq("user-1"), any(GenerateQuestionsFromResumeRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/v1/questions/generate")
                            .header("X-User-Id", "user-1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "resume_id": "resume-1" }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.resume_id").value("resume-1"));
        }
    }

    // ========================================================================
    // POST /v1/questions/generate - Business errors
    // ========================================================================
    @Nested
    @DisplayName("POST /v1/questions/generate - Business errors (400/500)")
    class GenerateBusinessErrorTests {

        @Test
        @DisplayName("Should return 400 with QUE_003 when question_type is an invalid enum")
        void shouldReturn400_whenQuestionTypeInvalid() throws Exception {
            when(questionService.generateFromResume(eq("user-1"), any(GenerateQuestionsFromResumeRequest.class)))
                    .thenThrow(new org.aibles.intellihirequestion.exception.BadRequestException(
                            org.aibles.intellihirequestion.exception.ErrorCode.QUE_003));

            mockMvc.perform(post("/v1/questions/generate")
                            .header("X-User-Id", "user-1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "resume_id": "resume-1",
                                      "count": 2,
                                      "question_type": "INVALID_TYPE"
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("QUE_003"));
        }
    }
}
