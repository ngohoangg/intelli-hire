package org.aibles.intellihireinterview.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.aibles.intellihireinterview.dto.CreateSessionRequest;
import org.aibles.intellihireinterview.dto.SubmitAnswerRequest;
import org.aibles.intellihireinterview.entity.enums.SessionStatus;
import org.aibles.intellihireinterview.repository.InterviewSessionRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@EmbeddedKafka(partitions = 1, topics = {"answers_submitted", "session_completed"})
class InterviewServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    static MockWebServer mockQuestionService;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
        r.add("question-service.base-url",
              () -> "http://localhost:" + mockQuestionService.getPort());
    }

    @BeforeAll
    static void startMockServer() throws IOException {
        mockQuestionService = new MockWebServer();
        mockQuestionService.start();
    }

    @AfterAll
    static void stopMockServer() throws IOException {
        mockQuestionService.shutdown();
    }

    @Autowired MockMvc mockMvc;
    @Autowired InterviewSessionRepository sessionRepo;

    final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    @Test
    void fullFlow_createSession_submitAnswer_endSession() throws Exception {
        String questionsJson = """
            {"success":true,"data":[
              {"id":"q-001","question_text":"What is JVM?","topic_id":"topic-java","difficulty_id":"diff-mid"},
              {"id":"q-002","question_text":"What is GC?","topic_id":"topic-java","difficulty_id":"diff-mid"}
            ]}
            """;
        mockQuestionService.enqueue(new MockResponse()
            .setBody(questionsJson)
            .addHeader("Content-Type", "application/json"));

        // Step 1: Create session
        var createReq = new CreateSessionRequest();
        createReq.setTopic("Java");
        createReq.setDifficulty("mid");
        createReq.setQuestionCount(2);

        var createResult = mockMvc.perform(post("/api/v1/interviews/sessions")
                .header("X-User-Id", "100")
                .requestAttr("userId", "100")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createReq)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.status").value("ACTIVE"))
            .andReturn();

        Long sessionId = mapper.readTree(createResult.getResponse().getContentAsString())
            .get("data").get("id").asLong();

        // Step 2: Submit answer
        var answerReq = new SubmitAnswerRequest();
        answerReq.setQuestionId("q-001");
        answerReq.setAnswerText("JVM is Java Virtual Machine...");

        mockMvc.perform(post("/api/v1/interviews/sessions/{id}/answers", sessionId)
                .header("X-User-Id", "100")
                .requestAttr("userId", "100")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(answerReq)))
            .andExpect(status().isOk());

        // Step 3: End session
        mockMvc.perform(put("/api/v1/interviews/sessions/{id}/end", sessionId)
                .header("X-User-Id", "100")
                .requestAttr("userId", "100"))
            .andExpect(status().isOk());

        // Step 4: Verify DB state
        var session = sessionRepo.findById(sessionId).orElseThrow();
        assertThat(session.getStatus()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(session.getEndedAt()).isNotNull();

        // Step 5: Verify session detail
        mockMvc.perform(get("/api/v1/interviews/sessions/{id}", sessionId)
                .header("X-User-Id", "100")
                .requestAttr("userId", "100"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("COMPLETED"))
            .andExpect(jsonPath("$.data.questions").isArray())
            .andExpect(jsonPath("$.data.answers").isArray());
    }

    @Test
    void createSession_withoutUserId_returns401() throws Exception {
        var req = new CreateSessionRequest();
        req.setTopic("Java");

        mockMvc.perform(post("/api/v1/interviews/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void listSessions_returnsPaginatedResult() throws Exception {
        mockMvc.perform(get("/api/v1/interviews/sessions?page=0&size=10")
                .header("X-User-Id", "999")
                .requestAttr("userId", "999"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.total_elements").exists());
    }
}
