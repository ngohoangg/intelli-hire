package org.aibles.intellihirereport;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aibles.intellihirereport.dto.CreateReportRequest;
import org.aibles.intellihirereport.dto.QuestionScoreRequest;
import org.aibles.intellihirereport.dto.RegenerateRoadmapRequest;
import org.aibles.intellihirereport.dto.UpdateReportRequest;
import org.aibles.intellihirereport.entity.InterviewReport;
import org.aibles.intellihirereport.entity.UserProgress;
import org.aibles.intellihirereport.entity.enums.ReportStatus;
import org.aibles.intellihirereport.repository.InterviewReportRepository;
import org.aibles.intellihirereport.repository.QuestionScoreRepository;
import org.aibles.intellihirereport.repository.SkillScoreRepository;
import org.aibles.intellihirereport.repository.UserProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReportControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InterviewReportRepository reportRepository;

    @Autowired
    private QuestionScoreRepository questionScoreRepository;

    @Autowired
    private SkillScoreRepository skillScoreRepository;

    @Autowired
    private UserProgressRepository userProgressRepository;

    @BeforeEach
    void setUp() {
        questionScoreRepository.deleteAll();
        skillScoreRepository.deleteAll();
        reportRepository.deleteAll();
        userProgressRepository.deleteAll();
    }

    private CreateReportRequest buildCreateRequest(String sessionId, Long userId) {
        return CreateReportRequest.builder()
                .sessionId(sessionId)
                .userId(userId)
                .topic("Java Spring Boot")
                .summary("Good interview performance")
                .roadmapJson(Map.of("step1", "Learn Spring", "step2", "Practice"))
                .questionScores(List.of(
                        QuestionScoreRequest.builder()
                                .questionId("q1")
                                .questionText("What is Spring Boot?")
                                .answerText("A framework for Java")
                                .score(8.0)
                                .maxScore(10.0)
                                .feedback("Good answer")
                                .skillName("Spring")
                                .build(),
                        QuestionScoreRequest.builder()
                                .questionId("q2")
                                .questionText("What is JPA?")
                                .answerText("Java Persistence API")
                                .score(7.0)
                                .maxScore(10.0)
                                .feedback("Decent answer")
                                .skillName("Spring")
                                .build(),
                        QuestionScoreRequest.builder()
                                .questionId("q3")
                                .questionText("Explain OOP principles")
                                .answerText("Encapsulation, Inheritance, Polymorphism, Abstraction")
                                .score(9.0)
                                .maxScore(10.0)
                                .feedback("Excellent")
                                .skillName("Java Core")
                                .build()
                ))
                .build();
    }

    private String createReportAndGetId(String sessionId, Long userId) throws Exception {
        CreateReportRequest request = buildCreateRequest(sessionId, userId);
        MvcResult result = mockMvc.perform(post("/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Map<String, Object> response = objectMapper.readValue(
                result.getResponse().getContentAsString(), Map.class);
        Map<String, Object> data = (Map<String, Object>) response.get("data");
        return (String) data.get("id");
    }

    // ========== POST /v1/reports ==========

    @Nested
    @DisplayName("POST /v1/reports - Create Report")
    class CreateReport {

        @Test
        @DisplayName("Should create report successfully")
        void createReport_success() throws Exception {
            CreateReportRequest request = buildCreateRequest("session-001", 1L);

            mockMvc.perform(post("/v1/reports")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.session_id").value("session-001"))
                    .andExpect(jsonPath("$.data.user_id").value(1))
                    .andExpect(jsonPath("$.data.topic").value("Java Spring Boot"))
                    .andExpect(jsonPath("$.data.total_score").value(24.0))
                    .andExpect(jsonPath("$.data.max_score").value(30.0))
                    .andExpect(jsonPath("$.data.total_questions").value(3))
                    .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.data.skill_scores", hasSize(2)))
                    .andExpect(jsonPath("$.data.question_scores", hasSize(3)))
                    .andExpect(jsonPath("$.data.id").isNotEmpty());

            // Verify data persisted in DB
            assertEquals(1, reportRepository.count());
            assertEquals(3, questionScoreRepository.count());
            assertEquals(2, skillScoreRepository.count());
            // Verify user progress created
            List<UserProgress> progressList = userProgressRepository.findByUserIdOrderBySkillNameAsc(1L);
            assertEquals(2, progressList.size());
        }

        @Test
        @DisplayName("Should return 409 for duplicate session")
        void createReport_duplicateSession() throws Exception {
            CreateReportRequest request = buildCreateRequest("session-dup", 1L);

            // First create
            mockMvc.perform(post("/v1/reports")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            // Duplicate create
            mockMvc.perform(post("/v1/reports")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("RPT_002"));
        }

        @Test
        @DisplayName("Should return 400 when sessionId is blank")
        void createReport_missingSessionId() throws Exception {
            CreateReportRequest request = CreateReportRequest.builder()
                    .sessionId("")
                    .userId(1L)
                    .questionScores(List.of(
                            QuestionScoreRequest.builder()
                                    .questionId("q1")
                                    .score(5.0)
                                    .maxScore(10.0)
                                    .build()
                    ))
                    .build();

            mockMvc.perform(post("/v1/reports")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should return 400 when userId is null")
        void createReport_missingUserId() throws Exception {
            CreateReportRequest request = CreateReportRequest.builder()
                    .sessionId("session-no-user")
                    .userId(null)
                    .questionScores(List.of(
                            QuestionScoreRequest.builder()
                                    .questionId("q1")
                                    .score(5.0)
                                    .maxScore(10.0)
                                    .build()
                    ))
                    .build();

            mockMvc.perform(post("/v1/reports")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should return 400 when questionScores is empty")
        void createReport_emptyQuestionScores() throws Exception {
            CreateReportRequest request = CreateReportRequest.builder()
                    .sessionId("session-empty-qs")
                    .userId(1L)
                    .questionScores(List.of())
                    .build();

            mockMvc.perform(post("/v1/reports")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should return 400 when score exceeds max allowed")
        void createReport_invalidScore() throws Exception {
            CreateReportRequest request = CreateReportRequest.builder()
                    .sessionId("session-invalid-score")
                    .userId(1L)
                    .questionScores(List.of(
                            QuestionScoreRequest.builder()
                                    .questionId("q1")
                                    .score(150.0) // exceeds max 100
                                    .maxScore(10.0)
                                    .build()
                    ))
                    .build();

            mockMvc.perform(post("/v1/reports")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    // ========== GET /v1/reports/{id} ==========

    @Nested
    @DisplayName("GET /v1/reports/{id} - Get Report By Id")
    class GetReportById {

        @Test
        @DisplayName("Should get report by id successfully")
        void getById_success() throws Exception {
            String reportId = createReportAndGetId("session-get-id", 1L);

            mockMvc.perform(get("/v1/reports/{id}", reportId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(reportId))
                    .andExpect(jsonPath("$.data.session_id").value("session-get-id"))
                    .andExpect(jsonPath("$.data.skill_scores", hasSize(2)))
                    .andExpect(jsonPath("$.data.question_scores", hasSize(3)));
        }

        @Test
        @DisplayName("Should return 404 when report not found")
        void getById_notFound() throws Exception {
            mockMvc.perform(get("/v1/reports/{id}", "non-existent-id"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("RPT_001"));
        }
    }

    // ========== GET /v1/reports/session/{sessionId} ==========

    @Nested
    @DisplayName("GET /v1/reports/session/{sessionId} - Get Report By Session")
    class GetReportBySessionId {

        @Test
        @DisplayName("Should get report by sessionId successfully")
        void getBySessionId_success() throws Exception {
            createReportAndGetId("session-get-session", 1L);

            mockMvc.perform(get("/v1/reports/session/{sessionId}", "session-get-session"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.session_id").value("session-get-session"))
                    .andExpect(jsonPath("$.data.topic").value("Java Spring Boot"))
                    .andExpect(jsonPath("$.data.skill_scores").isArray())
                    .andExpect(jsonPath("$.data.question_scores").isArray());
        }

        @Test
        @DisplayName("Should return 404 when session not found")
        void getBySessionId_notFound() throws Exception {
            mockMvc.perform(get("/v1/reports/session/{sessionId}", "non-existent-session"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("RPT_001"));
        }
    }

    // ========== PUT /v1/reports/{id} ==========

    @Nested
    @DisplayName("PUT /v1/reports/{id} - Update Report")
    class UpdateReport {

        @Test
        @DisplayName("Should update summary successfully")
        void update_summary() throws Exception {
            String reportId = createReportAndGetId("session-update", 1L);

            UpdateReportRequest updateRequest = UpdateReportRequest.builder()
                    .summary("Updated summary after review")
                    .build();

            mockMvc.perform(put("/v1/reports/{id}", reportId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.summary").value("Updated summary after review"))
                    .andExpect(jsonPath("$.data.id").value(reportId));
        }

        @Test
        @DisplayName("Should update roadmapJson successfully")
        void update_roadmap() throws Exception {
            String reportId = createReportAndGetId("session-update-roadmap", 1L);

            UpdateReportRequest updateRequest = UpdateReportRequest.builder()
                    .roadmapJson(Map.of("newStep", "Advanced topics"))
                    .build();

            mockMvc.perform(put("/v1/reports/{id}", reportId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.roadmap_json.newStep").value("Advanced topics"));
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent report")
        void update_notFound() throws Exception {
            UpdateReportRequest updateRequest = UpdateReportRequest.builder()
                    .summary("Some summary")
                    .build();

            mockMvc.perform(put("/v1/reports/{id}", "non-existent-id")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("RPT_001"));
        }
    }

    // ========== DELETE /v1/reports/session/{sessionId} ==========

    @Nested
    @DisplayName("DELETE /v1/reports/session/{sessionId} - Delete Report")
    class DeleteReport {

        @Test
        @DisplayName("Should delete report by sessionId successfully")
        void delete_success() throws Exception {
            createReportAndGetId("session-delete", 1L);

            mockMvc.perform(delete("/v1/reports/session/{sessionId}", "session-delete"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            // Verify deleted from DB
            assertEquals(0, reportRepository.count());
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent session")
        void delete_notFound() throws Exception {
            mockMvc.perform(delete("/v1/reports/session/{sessionId}", "non-existent"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("RPT_001"));
        }
    }

    // ========== GET /v1/reports/history/{userId} ==========

    @Nested
    @DisplayName("GET /v1/reports/history/{userId} - Get Report History")
    class GetHistory {

        @Test
        @DisplayName("Should get report history with pagination")
        void getHistory_success() throws Exception {
            createReportAndGetId("session-hist-1", 100L);
            createReportAndGetId("session-hist-2", 100L);
            createReportAndGetId("session-hist-3", 200L); // different user

            mockMvc.perform(get("/v1/reports/history/{userId}", 100L)
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content", hasSize(2)))
                    .andExpect(jsonPath("$.data.total_elements").value(2))
                    .andExpect(jsonPath("$.data.page").value(0));
        }

        @Test
        @DisplayName("Should filter history by status")
        void getHistory_filterByStatus() throws Exception {
            createReportAndGetId("session-hist-status-1", 100L);

            mockMvc.perform(get("/v1/reports/history/{userId}", 100L)
                            .param("status", "COMPLETED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content", hasSize(1)));
        }

        @Test
        @DisplayName("Should return 400 for invalid status")
        void getHistory_invalidStatus() throws Exception {
            mockMvc.perform(get("/v1/reports/history/{userId}", 100L)
                            .param("status", "INVALID_STATUS"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("RPT_004"));
        }

        @Test
        @DisplayName("Should return empty page when no reports for user")
        void getHistory_emptyResult() throws Exception {
            mockMvc.perform(get("/v1/reports/history/{userId}", 999L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content", hasSize(0)))
                    .andExpect(jsonPath("$.data.total_elements").value(0));
        }

        @Test
        @DisplayName("Should support pagination with size parameter")
        void getHistory_pagination() throws Exception {
            createReportAndGetId("session-page-1", 50L);
            createReportAndGetId("session-page-2", 50L);
            createReportAndGetId("session-page-3", 50L);

            mockMvc.perform(get("/v1/reports/history/{userId}", 50L)
                            .param("page", "0")
                            .param("size", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content", hasSize(2)))
                    .andExpect(jsonPath("$.data.total_elements").value(3))
                    .andExpect(jsonPath("$.data.total_pages").value(2));
        }
    }

    // ========== GET /v1/reports/stats/{userId} ==========

    @Nested
    @DisplayName("GET /v1/reports/stats/{userId} - Get User Stats")
    class GetStats {

        @Test
        @DisplayName("Should get user stats successfully")
        void getStats_success() throws Exception {
            createReportAndGetId("session-stats-1", 300L);
            createReportAndGetId("session-stats-2", 300L);

            mockMvc.perform(get("/v1/reports/stats/{userId}", 300L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.user_id").value(300))
                    .andExpect(jsonPath("$.data.total_sessions").value(2))
                    .andExpect(jsonPath("$.data.average_score").isNumber())
                    .andExpect(jsonPath("$.data.highest_score").isNumber())
                    .andExpect(jsonPath("$.data.lowest_score").isNumber())
                    .andExpect(jsonPath("$.data.top_skills").isArray())
                    .andExpect(jsonPath("$.data.weak_skills").isArray());
        }

        @Test
        @DisplayName("Should return zero stats for user with no reports")
        void getStats_noReports() throws Exception {
            mockMvc.perform(get("/v1/reports/stats/{userId}", 999L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.user_id").value(999))
                    .andExpect(jsonPath("$.data.total_sessions").value(0))
                    .andExpect(jsonPath("$.data.average_score").value(0.0))
                    .andExpect(jsonPath("$.data.highest_score").value(0.0))
                    .andExpect(jsonPath("$.data.lowest_score").value(0.0));
        }
    }

    // ========== GET /v1/reports/progress/{userId} ==========

    @Nested
    @DisplayName("GET /v1/reports/progress/{userId} - Get User Progress")
    class GetProgress {

        @Test
        @DisplayName("Should get user progress after creating report")
        void getProgress_success() throws Exception {
            createReportAndGetId("session-progress", 400L);

            mockMvc.perform(get("/v1/reports/progress/{userId}", 400L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data", hasSize(2))) // 2 skills: Spring, Java Core
                    .andExpect(jsonPath("$.data[*].skill_name",
                            containsInAnyOrder("Java Core", "Spring")))
                    .andExpect(jsonPath("$.data[0].total_attempts").value(1))
                    .andExpect(jsonPath("$.data[0].score_trend").isArray());
        }

        @Test
        @DisplayName("Should return empty list for user with no progress")
        void getProgress_empty() throws Exception {
            mockMvc.perform(get("/v1/reports/progress/{userId}", 999L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }
    }

    // ========== GET /v1/reports/progress/{userId}/{skillName} ==========

    @Nested
    @DisplayName("GET /v1/reports/progress/{userId}/{skillName} - Get Progress By Skill")
    class GetProgressBySkill {

        @Test
        @DisplayName("Should get progress for specific skill")
        void getProgressBySkill_success() throws Exception {
            createReportAndGetId("session-skill-progress", 500L);

            mockMvc.perform(get("/v1/reports/progress/{userId}/{skillName}", 500L, "Spring"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.skill_name").value("Spring"))
                    .andExpect(jsonPath("$.data.user_id").value(500))
                    .andExpect(jsonPath("$.data.latest_score").isNumber())
                    .andExpect(jsonPath("$.data.average_score").isNumber())
                    .andExpect(jsonPath("$.data.total_attempts").value(1));
        }

        @Test
        @DisplayName("Should return 404 for non-existent skill progress")
        void getProgressBySkill_notFound() throws Exception {
            mockMvc.perform(get("/v1/reports/progress/{userId}/{skillName}", 500L, "NonExistentSkill"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("PRG_001"));
        }
    }

    // ========== POST /v1/reports/{id}/regenerate ==========

    @Nested
    @DisplayName("POST /v1/reports/{id}/regenerate - Regenerate Roadmap")
    class RegenerateRoadmap {

        @Test
        @DisplayName("Should regenerate roadmap successfully")
        void regenerate_success() throws Exception {
            String reportId = createReportAndGetId("session-regen", 1L);

            Map<String, Object> newRoadmap = Map.of(
                    "phase1", "Review fundamentals",
                    "phase2", "Practice coding",
                    "phase3", "Mock interviews"
            );

            RegenerateRoadmapRequest request = RegenerateRoadmapRequest.builder()
                    .roadmapJson(newRoadmap)
                    .build();

            mockMvc.perform(post("/v1/reports/{id}/regenerate", reportId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(reportId))
                    .andExpect(jsonPath("$.data.roadmap_json.phase1").value("Review fundamentals"))
                    .andExpect(jsonPath("$.data.roadmap_json.phase2").value("Practice coding"))
                    .andExpect(jsonPath("$.data.roadmap_json.phase3").value("Mock interviews"));
        }

        @Test
        @DisplayName("Should return 404 when regenerating for non-existent report")
        void regenerate_notFound() throws Exception {
            RegenerateRoadmapRequest request = RegenerateRoadmapRequest.builder()
                    .roadmapJson(Map.of("step", "learn"))
                    .build();

            mockMvc.perform(post("/v1/reports/{id}/regenerate", "non-existent-id")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("RPT_001"));
        }

        @Test
        @DisplayName("Should return 400 when roadmapJson is null")
        void regenerate_nullRoadmap() throws Exception {
            String reportId = createReportAndGetId("session-regen-null", 1L);

            RegenerateRoadmapRequest request = RegenerateRoadmapRequest.builder()
                    .roadmapJson(null)
                    .build();

            mockMvc.perform(post("/v1/reports/{id}/regenerate", reportId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    // ========== GET /v1/reports/admin/all ==========

    @Nested
    @DisplayName("GET /v1/reports/admin/all - Admin Get All Reports")
    class AdminGetAll {

        @Test
        @DisplayName("Should get all reports with ROLE_ADMIN")
        void adminGetAll_withRoleAdmin() throws Exception {
            createReportAndGetId("session-admin-1", 1L);
            createReportAndGetId("session-admin-2", 2L);

            mockMvc.perform(get("/v1/reports/admin/all")
                            .header("X-User-Role", "ROLE_ADMIN")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content", hasSize(2)))
                    .andExpect(jsonPath("$.data.total_elements").value(2));
        }

        @Test
        @DisplayName("Should get all reports with ADMIN role")
        void adminGetAll_withAdmin() throws Exception {
            createReportAndGetId("session-admin-3", 1L);

            mockMvc.perform(get("/v1/reports/admin/all")
                            .header("X-User-Role", "ADMIN"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content", hasSize(1)));
        }

        // TODO: Enable when admin role check is implemented in controller
        // @Test
        // @DisplayName("Should return 403 without admin role")
        // void adminGetAll_forbidden() throws Exception {
        //     mockMvc.perform(get("/v1/reports/admin/all")
        //                     .header("X-User-Role", "USER"))
        //             .andExpect(status().isForbidden())
        //             .andExpect(jsonPath("$.success").value(false))
        //             .andExpect(jsonPath("$.error_code").value("RPT_005"));
        // }

        // @Test
        // @DisplayName("Should return 403 without role header")
        // void adminGetAll_noRoleHeader() throws Exception {
        //     mockMvc.perform(get("/v1/reports/admin/all"))
        //             .andExpect(status().isForbidden())
        //             .andExpect(jsonPath("$.success").value(false))
        //             .andExpect(jsonPath("$.error_code").value("RPT_005"));
        // }
    }

    // ========== Authorization Tests ==========

    @Nested
    @DisplayName("Authorization - X-User-Id ownership validation")
    class Authorization {

        // TODO: Enable when X-User-Id ownership validation is implemented
        // @Test
        // @DisplayName("Should return 403 when user accesses another user's history")
        // void history_forbidden() throws Exception {
        //     createReportAndGetId("session-auth-1", 1L);
        //
        //     mockMvc.perform(get("/v1/reports/history/{userId}", 1L)
        //                     .header("X-User-Id", "999"))
        //             .andExpect(status().isForbidden())
        //             .andExpect(jsonPath("$.error_code").value("RPT_007"));
        // }

        @Test
        @DisplayName("Should allow admin to access another user's history")
        void history_adminAllowed() throws Exception {
            createReportAndGetId("session-auth-2", 1L);

            mockMvc.perform(get("/v1/reports/history/{userId}", 1L)
                            .header("X-User-Id", "999")
                            .header("X-User-Role", "ROLE_ADMIN"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        // TODO: Enable when X-User-Id ownership validation is implemented
        // @Test
        // @DisplayName("Should return 403 when user accesses another user's stats")
        // void stats_forbidden() throws Exception {
        //     mockMvc.perform(get("/v1/reports/stats/{userId}", 1L)
        //                     .header("X-User-Id", "999"))
        //             .andExpect(status().isForbidden())
        //             .andExpect(jsonPath("$.error_code").value("RPT_007"));
        // }

        // TODO: Enable when X-User-Id ownership validation is implemented
        // @Test
        // @DisplayName("Should return 403 when user accesses another user's report by id")
        // void getById_forbidden() throws Exception {
        //     String reportId = createReportAndGetId("session-auth-getid", 1L);
        //
        //     mockMvc.perform(get("/v1/reports/{id}", reportId)
        //                     .header("X-User-Id", "999"))
        //             .andExpect(status().isForbidden())
        //             .andExpect(jsonPath("$.error_code").value("RPT_007"));
        // }

        @Test
        @DisplayName("Should allow owner to access their own report")
        void getById_ownerAllowed() throws Exception {
            String reportId = createReportAndGetId("session-auth-owner", 1L);

            mockMvc.perform(get("/v1/reports/{id}", reportId)
                            .header("X-User-Id", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        // TODO: Enable when X-User-Id ownership validation is implemented
        // @Test
        // @DisplayName("Should return 403 when user deletes another user's report")
        // void delete_forbidden() throws Exception {
        //     createReportAndGetId("session-auth-del", 1L);
        //
        //     mockMvc.perform(delete("/v1/reports/session/{sessionId}", "session-auth-del")
        //                     .header("X-User-Id", "999"))
        //             .andExpect(status().isForbidden())
        //             .andExpect(jsonPath("$.error_code").value("RPT_007"));
        // }
    }

    // ========== Validation Tests ==========

    @Nested
    @DisplayName("Validation - score <= maxScore")
    class ScoreValidation {

        @Test
        @DisplayName("Should return 400 when score > maxScore")
        void createReport_scoreExceedsMaxScore() throws Exception {
            CreateReportRequest request = CreateReportRequest.builder()
                    .sessionId("session-score-validation")
                    .userId(1L)
                    .questionScores(List.of(
                            QuestionScoreRequest.builder()
                                    .questionId("q1")
                                    .score(15.0)
                                    .maxScore(10.0)
                                    .build()
                    ))
                    .build();

            mockMvc.perform(post("/v1/reports")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    // ========== End-to-End Flow Tests ==========

    @Nested
    @DisplayName("End-to-End Flow Tests")
    class EndToEndFlow {

        @Test
        @DisplayName("Full lifecycle: create -> get -> update -> regenerate -> delete")
        void fullLifecycle() throws Exception {
            // 1. Create
            String reportId = createReportAndGetId("session-lifecycle", 1L);
            assertNotNull(reportId);

            // 2. Get by ID
            mockMvc.perform(get("/v1/reports/{id}", reportId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(reportId));

            // 3. Get by session
            mockMvc.perform(get("/v1/reports/session/{sessionId}", "session-lifecycle"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.session_id").value("session-lifecycle"));

            // 4. Update
            UpdateReportRequest updateRequest = UpdateReportRequest.builder()
                    .summary("Updated via lifecycle test")
                    .build();

            mockMvc.perform(put("/v1/reports/{id}", reportId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.summary").value("Updated via lifecycle test"));

            // 5. Regenerate roadmap
            RegenerateRoadmapRequest regenRequest = RegenerateRoadmapRequest.builder()
                    .roadmapJson(Map.of("updated", true))
                    .build();

            mockMvc.perform(post("/v1/reports/{id}/regenerate", reportId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(regenRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.roadmap_json.updated").value(true));

            // 6. Check history
            mockMvc.perform(get("/v1/reports/history/{userId}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content", hasSize(1)));

            // 7. Check stats
            mockMvc.perform(get("/v1/reports/stats/{userId}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.total_sessions").value(1));

            // 8. Check progress
            mockMvc.perform(get("/v1/reports/progress/{userId}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(2)));

            // 9. Delete
            mockMvc.perform(delete("/v1/reports/session/{sessionId}", "session-lifecycle"))
                    .andExpect(status().isOk());

            // 10. Verify deleted
            mockMvc.perform(get("/v1/reports/{id}", reportId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("User progress updates across multiple reports")
        void progressUpdatesAcrossReports() throws Exception {
            // Create first report
            createReportAndGetId("session-prog-1", 600L);

            // Check initial progress for Spring skill
            mockMvc.perform(get("/v1/reports/progress/{userId}/{skillName}", 600L, "Spring"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.total_attempts").value(1));

            // Create second report (same user, same skills)
            createReportAndGetId("session-prog-2", 600L);

            // Progress should now show 2 attempts
            mockMvc.perform(get("/v1/reports/progress/{userId}/{skillName}", 600L, "Spring"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.total_attempts").value(2))
                    .andExpect(jsonPath("$.data.score_trend", hasSize(2)));
        }
    }
}
