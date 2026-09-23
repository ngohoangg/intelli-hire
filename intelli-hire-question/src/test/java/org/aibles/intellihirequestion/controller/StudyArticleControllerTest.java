package org.aibles.intellihirequestion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aibles.intellihirequestion.dto.PageResponse;
import org.aibles.intellihirequestion.dto.StudyArticleRequest;
import org.aibles.intellihirequestion.dto.StudyArticleResponse;
import org.aibles.intellihirequestion.exception.ErrorCode;
import org.aibles.intellihirequestion.exception.InactiveResourceException;
import org.aibles.intellihirequestion.exception.NotFoundException;
import org.aibles.intellihirequestion.service.StudyArticleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import java.util.LinkedHashMap;
import java.util.Map;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

@WebMvcTest(controllers = StudyArticleController.class)
@ActiveProfiles("test")
class StudyArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudyArticleService studyArticleService;

    @Nested
    @DisplayName("GET /v1/study-articles")
    class GetAllTests {

        @Test
        @DisplayName("Should return 200 with paginated study articles when filters are provided")
        void shouldReturn200_whenGetAllWithFilters() throws Exception {
            StudyArticleResponse article = StudyArticleResponse.builder()
                    .id("article-1")
                    .topicId("topic-1")
                    .topicName("Backend")
                    .difficultyId("difficulty-1")
                    .difficultyName("Senior")
                    .title("System Design Roadmap")
                    .url("https://example.com/system-design")
                    .description("A curated roadmap")
                    .source("Tech Blog")
                    .isActive(true)
                    .build();

            PageResponse<StudyArticleResponse> response = PageResponse.<StudyArticleResponse>builder()
                    .content(java.util.List.of(article))
                    .page(0)
                    .size(20)
                    .totalElements(1)
                    .totalPages(1)
                    .isFirst(true)
                    .isLast(true)
                    .build();

            when(studyArticleService.getAll(org.mockito.ArgumentMatchers.eq("topic-1"), org.mockito.ArgumentMatchers.eq("difficulty-1"), any(Pageable.class)))
                    .thenReturn(response);

            mockMvc.perform(get("/v1/study-articles")
                            .param("topicId", "topic-1")
                            .param("difficultyId", "difficulty-1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.error_code").value(nullValue()))
                    .andExpect(jsonPath("$.errors").value(nullValue()))
                    .andExpect(jsonPath("$.data.page").value(0))
                    .andExpect(jsonPath("$.data.size").value(20))
                    .andExpect(jsonPath("$.data.total_elements").value(1))
                    .andExpect(jsonPath("$.data.total_pages").value(1))
                    .andExpect(jsonPath("$.data.first").value(true))
                    .andExpect(jsonPath("$.data.last").value(true))
                    .andExpect(jsonPath("$.data.content[0].id").value("article-1"))
                    .andExpect(jsonPath("$.data.content[0].topic_id").value("topic-1"))
                    .andExpect(jsonPath("$.data.content[0].difficulty_id").value("difficulty-1"))
                    .andExpect(jsonPath("$.data.content[0].is_active").value(true));

            verify(studyArticleService).getAll(
                    org.mockito.ArgumentMatchers.eq("topic-1"),
                    org.mockito.ArgumentMatchers.eq("difficulty-1"),
                    argThat(pageable -> pageable.getPageNumber() == 0
                            && pageable.getPageSize() == 20
                            && pageable.getSort().getOrderFor("createdAt") != null
                            && pageable.getSort().getOrderFor("createdAt").isDescending())
            );
        }

        @Test
        @DisplayName("Should normalize invalid pagination values when listing study articles")
        void shouldNormalizeInvalidPaginationValues_whenGetAll() throws Exception {
            PageResponse<StudyArticleResponse> response = PageResponse.<StudyArticleResponse>builder()
                    .content(java.util.List.of())
                    .page(0)
                    .size(100)
                    .totalElements(0)
                    .totalPages(0)
                    .isFirst(true)
                    .isLast(true)
                    .build();

            when(studyArticleService.getAll(org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.isNull(), any(Pageable.class)))
                    .thenReturn(response);

            mockMvc.perform(get("/v1/study-articles")
                            .param("page", "-5")
                            .param("size", "1000"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.size").value(100));

            verify(studyArticleService).getAll(
                    org.mockito.ArgumentMatchers.isNull(),
                    org.mockito.ArgumentMatchers.isNull(),
                    argThat(pageable -> pageable.getPageNumber() == 0 && pageable.getPageSize() == 100)
            );
        }
    }

    @Nested
    @DisplayName("GET /v1/study-articles/{id}")
    class GetByIdTests {

        @Test
        @DisplayName("Should return 200 with study article details when id exists")
        void shouldReturn200_whenIdExists() throws Exception {
            StudyArticleResponse response = StudyArticleResponse.builder()
                    .id("article-1")
                    .topicId("topic-1")
                    .topicName("Backend")
                    .difficultyId("difficulty-1")
                    .difficultyName("Senior")
                    .title("System Design Roadmap")
                    .url("https://example.com/system-design")
                    .description("A curated roadmap for system design interviews")
                    .source("Tech Blog")
                    .isActive(true)
                    .build();

            when(studyArticleService.getById("article-1")).thenReturn(response);

            mockMvc.perform(get("/v1/study-articles/{id}", "article-1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.error_code").value(nullValue()))
                    .andExpect(jsonPath("$.errors").value(nullValue()))
                    .andExpect(jsonPath("$.timestamp").isNotEmpty())
                    .andExpect(jsonPath("$.data.id").value("article-1"))
                    .andExpect(jsonPath("$.data.topic_id").value("topic-1"))
                    .andExpect(jsonPath("$.data.topic_name").value("Backend"))
                    .andExpect(jsonPath("$.data.difficulty_id").value("difficulty-1"))
                    .andExpect(jsonPath("$.data.difficulty_name").value("Senior"))
                    .andExpect(jsonPath("$.data.title").value("System Design Roadmap"))
                    .andExpect(jsonPath("$.data.url").value("https://example.com/system-design"))
                    .andExpect(jsonPath("$.data.description").value("A curated roadmap for system design interviews"))
                    .andExpect(jsonPath("$.data.source").value("Tech Blog"))
                    .andExpect(jsonPath("$.data.is_active").value(true));
        }

        @Test
        @DisplayName("Should return 404 with SA_001 when study article does not exist")
        void shouldReturn404_whenStudyArticleDoesNotExist() throws Exception {
            when(studyArticleService.getById("missing-id"))
                    .thenThrow(new NotFoundException(ErrorCode.SA_001));

            mockMvc.perform(get("/v1/study-articles/{id}", "missing-id"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.SA_001.getCode()))
                    .andExpect(jsonPath("$.data").doesNotExist())
                    .andExpect(jsonPath("$.errors").value(nullValue()));
        }

        @Test
        @DisplayName("Should return 404 with SA_001 when study article is inactive")
        void shouldReturn404_whenStudyArticleIsInactive() throws Exception {
            when(studyArticleService.getById("inactive-id"))
                    .thenThrow(new NotFoundException(ErrorCode.SA_001));

            mockMvc.perform(get("/v1/study-articles/{id}", "inactive-id"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.SA_001.getCode()))
                    .andExpect(jsonPath("$.data").doesNotExist())
                    .andExpect(jsonPath("$.errors").value(nullValue()));
        }

        @Test
        @DisplayName("Should return 500 with COM_002 when unexpected error occurs")
        void shouldReturn500_whenUnexpectedErrorOccurs() throws Exception {
            when(studyArticleService.getById("article-1"))
                    .thenThrow(new RuntimeException("unexpected"));

            mockMvc.perform(get("/v1/study-articles/{id}", "article-1"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.COM_002.getCode()))
                    .andExpect(jsonPath("$.data").doesNotExist())
                    .andExpect(jsonPath("$.errors").value(nullValue()));
        }
    }

    @Nested
    @DisplayName("POST /v1/study-articles")
    class CreateTests {

        @Test
        @DisplayName("Should return 201 and default isActive to true when request omits the field")
        void shouldReturn201AndDefaultActive_whenRequestOmitsIsActive() throws Exception {
            StudyArticleResponse response = StudyArticleResponse.builder()
                    .id("article-1")
                    .topicId("topic-1")
                    .topicName("Backend")
                    .difficultyId("difficulty-1")
                    .difficultyName("Senior")
                    .title("System Design Roadmap")
                    .url("https://example.com/system-design")
                    .description("A curated roadmap for system design interviews")
                    .source("Tech Blog")
                    .isActive(true)
                    .build();

            when(studyArticleService.create(any(StudyArticleRequest.class))).thenReturn(response);

            performCreate(createRequestJson(
                    "topic-1",
                    "difficulty-1",
                    "System Design Roadmap",
                    "https://example.com/system-design",
                    "A curated roadmap for system design interviews",
                    "Tech Blog",
                    null
            ))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.error_code").value(nullValue()))
                    .andExpect(jsonPath("$.data.id").value("article-1"))
                    .andExpect(jsonPath("$.data.is_active").value(true));

            verify(studyArticleService).create(argThat(request ->
                    "topic-1".equals(request.getTopicId())
                            && "difficulty-1".equals(request.getDifficultyId())
                            && "System Design Roadmap".equals(request.getTitle())
                            && Boolean.TRUE.equals(request.getIsActive())
            ));
        }

        @Test
        @DisplayName("Should return 201 and preserve explicit inactive status")
        void shouldReturn201AndPreserveExplicitInactiveStatus() throws Exception {
            StudyArticleResponse response = StudyArticleResponse.builder()
                    .id("article-2")
                    .topicId("topic-1")
                    .topicName("Backend")
                    .difficultyId("difficulty-1")
                    .difficultyName("Senior")
                    .title("Spring Boot Checklist")
                    .url("https://example.com/spring-checklist")
                    .description("Checklist for advanced interview prep")
                    .source("Engineering Blog")
                    .isActive(false)
                    .build();

            when(studyArticleService.create(any(StudyArticleRequest.class))).thenReturn(response);

            performCreate(createRequestJson(
                    "topic-1",
                    "difficulty-1",
                    "Spring Boot Checklist",
                    "https://example.com/spring-checklist",
                    "Checklist for advanced interview prep",
                    "Engineering Blog",
                    false
            ))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value("article-2"))
                    .andExpect(jsonPath("$.data.is_active").value(false));

            verify(studyArticleService).create(argThat(request -> Boolean.FALSE.equals(request.getIsActive())));
        }

        @Test
        @DisplayName("Should return 400 when topicId is blank")
        void shouldReturn400_whenTopicIdIsBlank() throws Exception {
            performCreate(createRequestJson(
                    " ",
                    "difficulty-1",
                    "System Design Roadmap",
                    "https://example.com/system-design",
                    "desc",
                    "Tech Blog",
                    true
            ))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.COM_001.getCode()))
                    .andExpect(jsonPath("$.errors[0].field").value("topicId"))
                    .andExpect(jsonPath("$.errors[0].message").value("Topic ID must not be blank"));
        }

        @Test
        @DisplayName("Should return 400 when difficultyId is blank")
        void shouldReturn400_whenDifficultyIdIsBlank() throws Exception {
            performCreate(createRequestJson(
                    "topic-1",
                    " ",
                    "System Design Roadmap",
                    "https://example.com/system-design",
                    "desc",
                    "Tech Blog",
                    true
            ))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.COM_001.getCode()))
                    .andExpect(jsonPath("$.errors[0].field").value("difficultyId"))
                    .andExpect(jsonPath("$.errors[0].message").value("Difficulty ID must not be blank"));
        }

        @Test
        @DisplayName("Should return 400 when title is blank")
        void shouldReturn400_whenTitleIsBlank() throws Exception {
            performCreate(createRequestJson(
                    "topic-1",
                    "difficulty-1",
                    " ",
                    "https://example.com/system-design",
                    "desc",
                    "Tech Blog",
                    true
            ))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.COM_001.getCode()))
                    .andExpect(jsonPath("$.errors[0].field").value("title"))
                    .andExpect(jsonPath("$.errors[0].message").value("Title must not be blank"));
        }

        @Test
        @DisplayName("Should return 400 when url is blank")
        void shouldReturn400_whenUrlIsBlank() throws Exception {
            performCreate(createRequestJson(
                    "topic-1",
                    "difficulty-1",
                    "System Design Roadmap",
                    " ",
                    "desc",
                    "Tech Blog",
                    true
            ))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.COM_001.getCode()))
                    .andExpect(jsonPath("$.errors[0].field").value("url"))
                    .andExpect(jsonPath("$.errors[0].message").value("URL must not be blank"));
        }

        @Test
        @DisplayName("Should return 400 when title exceeds max length")
        void shouldReturn400_whenTitleExceedsMaxLength() throws Exception {
            performCreate(createRequestJson(
                    "topic-1",
                    "difficulty-1",
                    "a".repeat(256),
                    "https://example.com/system-design",
                    "desc",
                    "Tech Blog",
                    true
            ))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.COM_001.getCode()))
                    .andExpect(jsonPath("$.errors[0].field").value("title"))
                    .andExpect(jsonPath("$.errors[0].message").value("Title must not exceed 255 characters"));
        }

        @Test
        @DisplayName("Should return 400 when url exceeds max length")
        void shouldReturn400_whenUrlExceedsMaxLength() throws Exception {
            performCreate(createRequestJson(
                    "topic-1",
                    "difficulty-1",
                    "System Design Roadmap",
                    "h".repeat(501),
                    "desc",
                    "Tech Blog",
                    true
            ))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.COM_001.getCode()))
                    .andExpect(jsonPath("$.errors[0].field").value("url"))
                    .andExpect(jsonPath("$.errors[0].message").value("URL must not exceed 500 characters"));
        }

        @Test
        @DisplayName("Should return 400 when source exceeds max length")
        void shouldReturn400_whenSourceExceedsMaxLength() throws Exception {
            performCreate(createRequestJson(
                    "topic-1",
                    "difficulty-1",
                    "System Design Roadmap",
                    "https://example.com/system-design",
                    "desc",
                    "s".repeat(101),
                    true
            ))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.COM_001.getCode()))
                    .andExpect(jsonPath("$.errors[0].field").value("source"))
                    .andExpect(jsonPath("$.errors[0].message").value("Source must not exceed 100 characters"));
        }

        @Test
        @DisplayName("Should return 404 with TOP_001 when topic does not exist")
        void shouldReturn404_whenTopicDoesNotExist() throws Exception {
            when(studyArticleService.create(any(StudyArticleRequest.class)))
                    .thenThrow(new NotFoundException(ErrorCode.TOP_001));

            performCreate(createRequestJson(
                    "missing-topic",
                    "difficulty-1",
                    "System Design Roadmap",
                    "https://example.com/system-design",
                    "desc",
                    "Tech Blog",
                    true
            ))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.TOP_001.getCode()))
                    .andExpect(jsonPath("$.errors").value(nullValue()));
        }

        @Test
        @DisplayName("Should return 423 with TOP_004 when topic is inactive")
        void shouldReturn423_whenTopicIsInactive() throws Exception {
            when(studyArticleService.create(any(StudyArticleRequest.class)))
                    .thenThrow(new InactiveResourceException(ErrorCode.TOP_004));

            performCreate(createRequestJson(
                    "topic-1",
                    "difficulty-1",
                    "System Design Roadmap",
                    "https://example.com/system-design",
                    "desc",
                    "Tech Blog",
                    true
            ))
                    .andExpect(status().isLocked())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.TOP_004.getCode()))
                    .andExpect(jsonPath("$.errors").value(nullValue()));
        }

        @Test
        @DisplayName("Should return 404 with DIF_001 when difficulty level does not exist")
        void shouldReturn404_whenDifficultyLevelDoesNotExist() throws Exception {
            when(studyArticleService.create(any(StudyArticleRequest.class)))
                    .thenThrow(new NotFoundException(ErrorCode.DIF_001));

            performCreate(createRequestJson(
                    "topic-1",
                    "missing-difficulty",
                    "System Design Roadmap",
                    "https://example.com/system-design",
                    "desc",
                    "Tech Blog",
                    true
            ))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.DIF_001.getCode()))
                    .andExpect(jsonPath("$.errors").value(nullValue()));
        }

        @Test
        @DisplayName("Should return 500 with COM_002 when unexpected error occurs")
        void shouldReturn500_whenUnexpectedErrorOccurs() throws Exception {
            when(studyArticleService.create(any(StudyArticleRequest.class)))
                    .thenThrow(new RuntimeException("unexpected"));

            performCreate(createRequestJson(
                    "topic-1",
                    "difficulty-1",
                    "System Design Roadmap",
                    "https://example.com/system-design",
                    "desc",
                    "Tech Blog",
                    true
            ))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.COM_002.getCode()))
                    .andExpect(jsonPath("$.errors").value(nullValue()));
        }
    }

    @Nested
    @DisplayName("PUT /v1/study-articles/{id}")
    class UpdateTests {

        @Test
        @DisplayName("Should return 200 when request is valid")
        void shouldReturn200_whenRequestIsValid() throws Exception {
            StudyArticleResponse response = StudyArticleResponse.builder()
                    .id("article-1")
                    .topicId("topic-2")
                    .topicName("System Design")
                    .difficultyId("difficulty-2")
                    .difficultyName("Mid")
                    .title("Updated Roadmap")
                    .url("https://example.com/updated-roadmap")
                    .description("Updated description")
                    .source("Updated Blog")
                    .isActive(true)
                    .build();

            when(studyArticleService.update(org.mockito.ArgumentMatchers.eq("article-1"), any(StudyArticleRequest.class)))
                    .thenReturn(response);

            performUpdate("article-1", createRequestJson(
                    "topic-2",
                    "difficulty-2",
                    "Updated Roadmap",
                    "https://example.com/updated-roadmap",
                    "Updated description",
                    "Updated Blog",
                    true
            ))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.error_code").value(nullValue()))
                    .andExpect(jsonPath("$.errors").value(nullValue()))
                    .andExpect(jsonPath("$.timestamp").isNotEmpty())
                    .andExpect(jsonPath("$.data.id").value("article-1"))
                    .andExpect(jsonPath("$.data.topic_id").value("topic-2"))
                    .andExpect(jsonPath("$.data.topic_name").value("System Design"))
                    .andExpect(jsonPath("$.data.difficulty_id").value("difficulty-2"))
                    .andExpect(jsonPath("$.data.difficulty_name").value("Mid"))
                    .andExpect(jsonPath("$.data.title").value("Updated Roadmap"))
                    .andExpect(jsonPath("$.data.url").value("https://example.com/updated-roadmap"))
                    .andExpect(jsonPath("$.data.description").value("Updated description"))
                    .andExpect(jsonPath("$.data.source").value("Updated Blog"))
                    .andExpect(jsonPath("$.data.is_active").value(true));

            verify(studyArticleService).update(
                    org.mockito.ArgumentMatchers.eq("article-1"),
                    argThat(request ->
                            "topic-2".equals(request.getTopicId())
                                    && "difficulty-2".equals(request.getDifficultyId())
                                    && "Updated Roadmap".equals(request.getTitle())
                                    && "https://example.com/updated-roadmap".equals(request.getUrl())
                                    && "Updated description".equals(request.getDescription())
                                    && "Updated Blog".equals(request.getSource())
                                    && Boolean.TRUE.equals(request.getIsActive())
                    )
            );
        }

        @Test
        @DisplayName("Should return 400 when topicId is blank")
        void shouldReturn400_whenTopicIdIsBlank() throws Exception {
            performUpdate("article-1", createRequestJson(
                    " ",
                    "difficulty-1",
                    "Updated Roadmap",
                    "https://example.com/updated-roadmap",
                    "desc",
                    "Tech Blog",
                    true
            ))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.COM_001.getCode()))
                    .andExpect(jsonPath("$.errors[0].field").value("topicId"))
                    .andExpect(jsonPath("$.errors[0].message").value("Topic ID must not be blank"));
        }

        @Test
        @DisplayName("Should return 400 when difficultyId is blank")
        void shouldReturn400_whenDifficultyIdIsBlank() throws Exception {
            performUpdate("article-1", createRequestJson(
                    "topic-1",
                    " ",
                    "Updated Roadmap",
                    "https://example.com/updated-roadmap",
                    "desc",
                    "Tech Blog",
                    true
            ))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.COM_001.getCode()))
                    .andExpect(jsonPath("$.errors[0].field").value("difficultyId"))
                    .andExpect(jsonPath("$.errors[0].message").value("Difficulty ID must not be blank"));
        }

        @Test
        @DisplayName("Should return 400 when title is blank")
        void shouldReturn400_whenTitleIsBlank() throws Exception {
            performUpdate("article-1", createRequestJson(
                    "topic-1",
                    "difficulty-1",
                    " ",
                    "https://example.com/updated-roadmap",
                    "desc",
                    "Tech Blog",
                    true
            ))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.COM_001.getCode()))
                    .andExpect(jsonPath("$.errors[0].field").value("title"))
                    .andExpect(jsonPath("$.errors[0].message").value("Title must not be blank"));
        }

        @Test
        @DisplayName("Should return 400 when url is blank")
        void shouldReturn400_whenUrlIsBlank() throws Exception {
            performUpdate("article-1", createRequestJson(
                    "topic-1",
                    "difficulty-1",
                    "Updated Roadmap",
                    " ",
                    "desc",
                    "Tech Blog",
                    true
            ))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.COM_001.getCode()))
                    .andExpect(jsonPath("$.errors[0].field").value("url"))
                    .andExpect(jsonPath("$.errors[0].message").value("URL must not be blank"));
        }

        @Test
        @DisplayName("Should return 400 when title exceeds max length")
        void shouldReturn400_whenTitleExceedsMaxLength() throws Exception {
            performUpdate("article-1", createRequestJson(
                    "topic-1",
                    "difficulty-1",
                    "a".repeat(256),
                    "https://example.com/updated-roadmap",
                    "desc",
                    "Tech Blog",
                    true
            ))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.COM_001.getCode()))
                    .andExpect(jsonPath("$.errors[0].field").value("title"))
                    .andExpect(jsonPath("$.errors[0].message").value("Title must not exceed 255 characters"));
        }

        @Test
        @DisplayName("Should return 400 when url exceeds max length")
        void shouldReturn400_whenUrlExceedsMaxLength() throws Exception {
            performUpdate("article-1", createRequestJson(
                    "topic-1",
                    "difficulty-1",
                    "Updated Roadmap",
                    "h".repeat(501),
                    "desc",
                    "Tech Blog",
                    true
            ))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.COM_001.getCode()))
                    .andExpect(jsonPath("$.errors[0].field").value("url"))
                    .andExpect(jsonPath("$.errors[0].message").value("URL must not exceed 500 characters"));
        }

        @Test
        @DisplayName("Should return 400 when source exceeds max length")
        void shouldReturn400_whenSourceExceedsMaxLength() throws Exception {
            performUpdate("article-1", createRequestJson(
                    "topic-1",
                    "difficulty-1",
                    "Updated Roadmap",
                    "https://example.com/updated-roadmap",
                    "desc",
                    "s".repeat(101),
                    true
            ))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.COM_001.getCode()))
                    .andExpect(jsonPath("$.errors[0].field").value("source"))
                    .andExpect(jsonPath("$.errors[0].message").value("Source must not exceed 100 characters"));
        }

        @Test
        @DisplayName("Should return 404 with SA_001 when study article does not exist")
        void shouldReturn404_whenStudyArticleDoesNotExist() throws Exception {
            when(studyArticleService.update(org.mockito.ArgumentMatchers.eq("missing-id"), any(StudyArticleRequest.class)))
                    .thenThrow(new NotFoundException(ErrorCode.SA_001));

            performUpdate("missing-id", createRequestJson(
                    "topic-1",
                    "difficulty-1",
                    "Updated Roadmap",
                    "https://example.com/updated-roadmap",
                    "desc",
                    "Tech Blog",
                    true
            ))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.SA_001.getCode()))
                    .andExpect(jsonPath("$.errors").value(nullValue()));
        }

        @Test
        @DisplayName("Should return 404 with TOP_001 when topic does not exist")
        void shouldReturn404_whenTopicDoesNotExist() throws Exception {
            when(studyArticleService.update(org.mockito.ArgumentMatchers.eq("article-1"), any(StudyArticleRequest.class)))
                    .thenThrow(new NotFoundException(ErrorCode.TOP_001));

            performUpdate("article-1", createRequestJson(
                    "missing-topic",
                    "difficulty-1",
                    "Updated Roadmap",
                    "https://example.com/updated-roadmap",
                    "desc",
                    "Tech Blog",
                    true
            ))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.TOP_001.getCode()))
                    .andExpect(jsonPath("$.errors").value(nullValue()));
        }

        @Test
        @DisplayName("Should return 423 with TOP_004 when topic is inactive")
        void shouldReturn423_whenTopicIsInactive() throws Exception {
            when(studyArticleService.update(org.mockito.ArgumentMatchers.eq("article-1"), any(StudyArticleRequest.class)))
                    .thenThrow(new InactiveResourceException(ErrorCode.TOP_004));

            performUpdate("article-1", createRequestJson(
                    "topic-1",
                    "difficulty-1",
                    "Updated Roadmap",
                    "https://example.com/updated-roadmap",
                    "desc",
                    "Tech Blog",
                    true
            ))
                    .andExpect(status().isLocked())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.TOP_004.getCode()))
                    .andExpect(jsonPath("$.errors").value(nullValue()));
        }

        @Test
        @DisplayName("Should return 404 with DIF_001 when difficulty level does not exist")
        void shouldReturn404_whenDifficultyLevelDoesNotExist() throws Exception {
            when(studyArticleService.update(org.mockito.ArgumentMatchers.eq("article-1"), any(StudyArticleRequest.class)))
                    .thenThrow(new NotFoundException(ErrorCode.DIF_001));

            performUpdate("article-1", createRequestJson(
                    "topic-1",
                    "missing-difficulty",
                    "Updated Roadmap",
                    "https://example.com/updated-roadmap",
                    "desc",
                    "Tech Blog",
                    true
            ))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.DIF_001.getCode()))
                    .andExpect(jsonPath("$.errors").value(nullValue()));
        }

        @Test
        @DisplayName("Should return 500 with COM_002 when unexpected error occurs")
        void shouldReturn500_whenUnexpectedErrorOccurs() throws Exception {
            when(studyArticleService.update(org.mockito.ArgumentMatchers.eq("article-1"), any(StudyArticleRequest.class)))
                    .thenThrow(new RuntimeException("unexpected"));

            performUpdate("article-1", createRequestJson(
                    "topic-1",
                    "difficulty-1",
                    "Updated Roadmap",
                    "https://example.com/updated-roadmap",
                    "desc",
                    "Tech Blog",
                    true
            ))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.COM_002.getCode()))
                    .andExpect(jsonPath("$.errors").value(nullValue()));
        }
    }

    @Nested
    @DisplayName("DELETE /v1/study-articles/{id}")
    class DeleteTests {

        @Test
        @DisplayName("Should return 204 when study article is deleted successfully")
        void shouldReturn204_whenDeleteSuccessfully() throws Exception {
            mockMvc.perform(delete("/v1/study-articles/{id}", "article-1"))
                    .andExpect(status().isNoContent());

            verify(studyArticleService).delete("article-1");
        }

        @Test
        @DisplayName("Should return 404 with SA_001 when study article does not exist")
        void shouldReturn404_whenStudyArticleDoesNotExist() throws Exception {
            org.mockito.Mockito.doThrow(new NotFoundException(ErrorCode.SA_001))
                    .when(studyArticleService).delete("missing-id");

            mockMvc.perform(delete("/v1/study-articles/{id}", "missing-id"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.SA_001.getCode()))
                    .andExpect(jsonPath("$.data").doesNotExist())
                    .andExpect(jsonPath("$.errors").value(nullValue()));

            verify(studyArticleService).delete("missing-id");
        }

        @Test
        @DisplayName("Should return 500 with COM_002 when unexpected error occurs")
        void shouldReturn500_whenUnexpectedErrorOccurs() throws Exception {
            org.mockito.Mockito.doThrow(new RuntimeException("unexpected"))
                    .when(studyArticleService).delete("article-1");

            mockMvc.perform(delete("/v1/study-articles/{id}", "article-1"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.COM_002.getCode()))
                    .andExpect(jsonPath("$.data").doesNotExist())
                    .andExpect(jsonPath("$.errors").value(nullValue()));

            verify(studyArticleService).delete("article-1");
        }
    }

    private ResultActions performCreate(String requestBody) throws Exception {
        return mockMvc.perform(post("/v1/study-articles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));
    }

    private String createRequestJson(
            String topicId,
            String difficultyId,
            String title,
            String url,
            String description,
            String source,
            Boolean isActive
    ) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("topic_id", topicId);
        payload.put("difficulty_id", difficultyId);
        payload.put("title", title);
        payload.put("url", url);
        payload.put("description", description);
        payload.put("source", source);

        if (isActive != null) {
            payload.put("is_active", isActive);
        }

        return objectMapper.writeValueAsString(payload);
    }

    private ResultActions performUpdate(String id, String requestBody) throws Exception {
        return mockMvc.perform(put("/v1/study-articles/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));
    }
}
