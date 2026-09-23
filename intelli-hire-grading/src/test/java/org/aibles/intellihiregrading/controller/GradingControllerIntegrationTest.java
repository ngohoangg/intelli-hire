package org.aibles.intellihiregrading.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aibles.intellihiregrading.dto.AnswerRequest;
import org.aibles.intellihiregrading.dto.GradeRequest;
import org.aibles.intellihiregrading.dto.openrouter.LlmGradingResult;
import org.aibles.intellihiregrading.kafka.GradingCompletedProducer;
import org.aibles.intellihiregrading.service.InterviewClientService;
import org.aibles.intellihiregrading.service.LlmService;
import org.springframework.cache.CacheManager;
import org.springframework.kafka.core.KafkaTemplate;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GradingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LlmService llmService;

    @MockBean
    private GradingCompletedProducer gradingCompletedProducer;

    @MockBean
    private InterviewClientService interviewClientService;

    @MockBean
    private KafkaTemplate<?, ?> kafkaTemplate;

    @MockBean
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        when(llmService.grade(anyString())).thenReturn(
                LlmGradingResult.builder()
                        .score(8.0)
                        .feedback("Good answer")
                        .model("test-model")
                        .build()
        );
    }

    @Nested
    class GradeEndpoint {

        @Test
        void grade_validRequest_returns201() throws Exception {
            GradeRequest request = GradeRequest.builder()
                    .sessionId("test-session-1")
                    .userId("1")
                    .topic("Java")
                    .answers(List.of(
                            AnswerRequest.builder()
                                    .questionId("q1")
                                    .questionText("What is Java?")
                                    .answerText("A programming language")
                                    .skillName("java-core")
                                    .maxScore(10.0)
                                    .build()
                    ))
                    .build();

            mockMvc.perform(post("/v1/gradings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.session_id").value("test-session-1"))
                    .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.data.results[0].score").value(8.0));
        }

        @Test
        void grade_missingSessionId_returns400() throws Exception {
            GradeRequest request = GradeRequest.builder()
                    .userId("1")
                    .answers(List.of(
                            AnswerRequest.builder().questionId("q1").maxScore(10.0).build()
                    ))
                    .build();

            mockMvc.perform(post("/v1/gradings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void grade_emptyAnswers_returns400() throws Exception {
            GradeRequest request = GradeRequest.builder()
                    .sessionId("test-session-empty")
                    .userId("1")
                    .answers(List.of())
                    .build();

            mockMvc.perform(post("/v1/gradings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void grade_duplicateSession_returns409() throws Exception {
            GradeRequest request = GradeRequest.builder()
                    .sessionId("test-session-dup")
                    .userId("1")
                    .topic("Java")
                    .answers(List.of(
                            AnswerRequest.builder().questionId("q1").questionText("Q?")
                                    .answerText("A").skillName("java").maxScore(10.0).build()
                    ))
                    .build();

            mockMvc.perform(post("/v1/gradings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/v1/gradings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    class GetEndpoints {

        @Test
        void getById_notFound_returns404() throws Exception {
            mockMvc.perform(get("/v1/gradings/nonexistent-id"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void getBySessionId_notFound_returns404() throws Exception {
            mockMvc.perform(get("/v1/gradings/session/nonexistent"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void getHistory_returnsPage() throws Exception {
            mockMvc.perform(get("/v1/gradings/history/1")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray());
        }
    }

    @Nested
    class AdminEndpoint {

        @Test
        void adminGetAll_withoutRole_returns403() throws Exception {
            mockMvc.perform(get("/v1/gradings/admin/all"))
                    .andExpect(status().isForbidden());
        }

        @Test
        void adminGetAll_withAdminRole_returns200() throws Exception {
            mockMvc.perform(get("/v1/gradings/admin/all")
                            .header("X-User-Role", "ADMIN"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray());
        }
    }
}
