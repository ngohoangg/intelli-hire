package org.aibles.intellihirequestion.controller;

import org.aibles.intellihirequestion.entity.Tag;
import org.aibles.intellihirequestion.entity.QuestionTag;
import org.aibles.intellihirequestion.repository.TagRepository;
import org.aibles.intellihirequestion.repository.QuestionTagRepository;
import org.aibles.intellihirequestion.config.RedisTestConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.Nested;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(RedisTestConfig.class)
class TagControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private QuestionTagRepository questionTagRepository;

    @Autowired(required = false)
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        clearCache();
        questionTagRepository.deleteAll();
        tagRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        questionTagRepository.deleteAll();
        tagRepository.deleteAll();
        clearCache();
    }

    private void clearCache() {
        if (cacheManager != null) {
            cacheManager.getCacheNames().forEach(name -> {
                var cache = cacheManager.getCache(name);
                if (cache != null) {
                    cache.clear();
                }
            });
        }
    }

    private Tag createTag(String id, String name) {
        Tag tag = new Tag();
        tag.setId(id);
        tag.setName(name);
        return tagRepository.save(tag);
    }

    @Test
    @DisplayName("GET /v1/tags - Should return list of tags and return 200")
    void shouldReturnListOfTags_andReturn200() throws Exception {
        // Given
        createTag("1", "algorithm");
        createTag("2", "database");
        createTag("3", "design-pattern");

        // When & Then
        mockMvc.perform(get("/v1/tags")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error_code").isEmpty())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].name").value("algorithm"))
                .andExpect(jsonPath("$.data[1].name").value("database"))
                .andExpect(jsonPath("$.data[2].name").value("design-pattern"))
                .andExpect(jsonPath("$.errors").isEmpty())
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    @DisplayName("GET /v1/tags - Should return empty list when no tags exist")
    void shouldReturnEmptyList_whenNoTagsExist() throws Exception {
        // When & Then
        mockMvc.perform(get("/v1/tags")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error_code").isEmpty())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    @DisplayName("GET /v1/tags - Should return tags ordered by name ASC")
    void shouldReturnTagsOrderedByNameAsc() throws Exception {
        // Given - Insert in random order
        createTag("3", "spring-boot");
        createTag("1", "algorithm");
        createTag("2", "java");

        // When & Then - Should be sorted alphabetically
        mockMvc.perform(get("/v1/tags")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].name").value("algorithm"))
                .andExpect(jsonPath("$.data[1].name").value("java"))
                .andExpect(jsonPath("$.data[2].name").value("spring-boot"));
    }

    @Test
    @DisplayName("GET /v1/tags - Should return correct response structure")
    void shouldReturnCorrectResponseStructure() throws Exception {
        // Given
        createTag("test-uuid-001", "testing");

        // When & Then
        mockMvc.perform(get("/v1/tags")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error_code").isEmpty())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value("test-uuid-001"))
                .andExpect(jsonPath("$.data[0].name").value("testing"))
                .andExpect(jsonPath("$.errors").isEmpty())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ==================== POST /v1/tags Tests ====================

    @Nested
    @DisplayName("POST /v1/tags - Success (201)")
    class CreateTagSuccessTests {

        @Test
        @DisplayName("Should create tag and return 201")
        void shouldCreateTag_andReturn201() throws Exception {
            // Given
            String requestBody = """
                    {"name": "design-pattern"}
                    """;

            // When & Then
            mockMvc.perform(post("/v1/tags")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.error_code").isEmpty())
                    .andExpect(jsonPath("$.data.id").isNotEmpty())
                    .andExpect(jsonPath("$.data.name").value("design-pattern"))
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());
        }

        @Test
        @DisplayName("Should normalize name to lowercase and return 201")
        void shouldNormalizeNameToLowercase_andReturn201() throws Exception {
            // Given
            String requestBody = """
                    {"name": "Design-Pattern"}
                    """;

            // When & Then
            mockMvc.perform(post("/v1/tags")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("design-pattern"));
        }

        @Test
        @DisplayName("Should trim whitespace from name and return 201")
        void shouldTrimWhitespaceFromName_andReturn201() throws Exception {
            // Given
            String requestBody = """
                    {"name": "  java-core  "}
                    """;

            // When & Then
            mockMvc.perform(post("/v1/tags")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("java-core"));
        }

        @Test
        @DisplayName("Should invalidate cache after creating tag")
        void shouldInvalidateCache_afterCreatingTag() throws Exception {
            // Given - Create initial tag and call getAll to populate cache
            createTag("1", "algorithm");
            mockMvc.perform(get("/v1/tags")).andExpect(status().isOk());

            // When - Create new tag
            String requestBody = """
                    {"name": "new-tag"}
                    """;
            mockMvc.perform(post("/v1/tags")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated());

            // Then - getAll should return updated list (cache invalidated)
            mockMvc.perform(get("/v1/tags")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(2));
        }
    }

    @Nested
    @DisplayName("POST /v1/tags - Validation Error (400)")
    class CreateTagValidationErrorTests {

        @Test
        @DisplayName("Should return 400 when name is blank")
        void shouldReturn400_whenNameIsBlank() throws Exception {
            // Given
            String requestBody = """
                    {"name": ""}
                    """;

            // When & Then
            mockMvc.perform(post("/v1/tags")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("COM_001"));
        }

        @Test
        @DisplayName("Should return 400 when name is null")
        void shouldReturn400_whenNameIsNull() throws Exception {
            // Given
            String requestBody = """
                    {"name": null}
                    """;

            // When & Then
            mockMvc.perform(post("/v1/tags")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("COM_001"));
        }

        @Test
        @DisplayName("Should return 400 when name exceeds 100 characters")
        void shouldReturn400_whenNameExceeds100Characters() throws Exception {
            // Given
            String longName = "a".repeat(101);
            String requestBody = String.format("{\"name\": \"%s\"}", longName);

            // When & Then
            mockMvc.perform(post("/v1/tags")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("COM_001"));
        }
    }

    @Nested
    @DisplayName("POST /v1/tags - Duplicate Error (409)")
    class CreateTagDuplicateTests {

        @Test
        @DisplayName("Should return 409 when tag name already exists")
        void shouldReturn409_whenTagNameExists() throws Exception {
            // Given - Create existing tag
            createTag("existing-uuid", "java");

            String requestBody = """
                    {"name": "java"}
                    """;

            // When & Then
            mockMvc.perform(post("/v1/tags")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("TAG_002"));
        }

        @Test
        @DisplayName("Should return 409 for case-insensitive duplicate")
        void shouldReturn409_forCaseInsensitiveDuplicate() throws Exception {
            // Given - Create existing tag with lowercase
            createTag("existing-uuid", "java");

            String requestBody = """
                    {"name": "JAVA"}
                    """;

            // When & Then
            mockMvc.perform(post("/v1/tags")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("TAG_002"));
        }
    }

    // ==================== DELETE /v1/tags/{id} Tests ====================

    @Nested
    @DisplayName("DELETE /v1/tags/{id} - Success (200)")
    class DeleteTagSuccessTests {

        @Test
        @DisplayName("Should delete tag and return 200")
        void shouldDeleteTag_andReturn200() throws Exception {
            // Given
            Tag tag = createTag("tag-uuid-001", "java");

            // When & Then
            mockMvc.perform(delete("/v1/tags/{id}", tag.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.error_code").isEmpty())
                    .andExpect(jsonPath("$.data").isEmpty())
                    .andExpect(jsonPath("$.errors").isEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());

            // Verify tag is deleted
            assertThat(tagRepository.findById(tag.getId())).isEmpty();
        }

        @Test
        @DisplayName("Should invalidate cache after deleting tag")
        void shouldInvalidateCache_afterDeletingTag() throws Exception {
            // Given - Create tags and call getAll to populate cache
            Tag tag1 = createTag("1", "algorithm");
            Tag tag2 = createTag("2", "java");
            mockMvc.perform(get("/v1/tags")).andExpect(status().isOk());

            // When - Delete one tag
            mockMvc.perform(delete("/v1/tags/{id}", tag2.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Then - getAll should return updated list (cache invalidated)
            mockMvc.perform(get("/v1/tags")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].name").value("algorithm"));
        }
    }

    @Nested
    @DisplayName("DELETE /v1/tags/{id} - Not Found (404)")
    class DeleteTagNotFoundTests {

        @Test
        @DisplayName("Should return 404 when tag does not exist")
        void shouldReturn404_whenTagNotFound() throws Exception {
            // Given
            String nonExistentId = "non-existent-uuid";

            // When & Then
            mockMvc.perform(delete("/v1/tags/{id}", nonExistentId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("TAG_001"));
        }
    }

    @Nested
    @DisplayName("DELETE /v1/tags/{id} - Tag In Use (400)")
    class DeleteTagInUseTests {

        @Test
        @DisplayName("Should return 400 when tag is in use by questions")
        void shouldReturn400_whenTagInUse() throws Exception {
            // Given - Create tag and associate with a question
            Tag tag = createTag("tag-uuid-001", "java");
            QuestionTag questionTag = new QuestionTag("question-uuid-001", tag.getId());
            questionTagRepository.save(questionTag);

            // When & Then
            mockMvc.perform(delete("/v1/tags/{id}", tag.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("TAG_003"));

            // Verify tag is NOT deleted
            assertThat(tagRepository.findById(tag.getId())).isPresent();
        }
    }

    // ==================== PUT /v1/tags/{id} Tests ====================
    @Nested
    @DisplayName("PUT /v1/tags/{id} - Success (200)")
    class UpdateTagSuccessTests {

        @Test
        @DisplayName("Should update tag and return 200")
        void shouldUpdateTag_andReturn200() throws Exception {
            // Given
            createTag("1", "java");

            String requestBody = """
                    {"name": "spring"}
                    """;

            // When & Then
            mockMvc.perform(put("/v1/tags/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.error_code").isEmpty())
                    .andExpect(jsonPath("$.data.id").value("1"))
                    .andExpect(jsonPath("$.data.name").value("spring"))
                    .andExpect(jsonPath("$.errors").isEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());
        }

        @Test
        @DisplayName("Should normalize name to lowercase and return 200")
        void shouldNormalizeNameToLowercase_andReturn200() throws Exception {
            // Given
            createTag("1", "java");

            String requestBody = """
                    {"name": "Spring-Boot"}
                    """;

            // When & Then
            mockMvc.perform(put("/v1/tags/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("spring-boot"));
        }

        @Test
        @DisplayName("Should trim whitespace from name and return 200")
        void shouldTrimWhitespaceFromName_andReturn200() throws Exception {
            // Given
            createTag("1", "java");

            String requestBody = """
                    {"name": "  database  "}
                    """;

            // When & Then
            mockMvc.perform(put("/v1/tags/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("database"));
        }

        @Test
        @DisplayName("Should invalidate cache after updating tag")
        void shouldInvalidateCache_afterUpdatingTag() throws Exception {
            // Given - create tags and call getAll to populate cache
            createTag("1", "algorithm");
            createTag("2", "java");
            mockMvc.perform(get("/v1/tags"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(2));

            // When - update tag "java" -> "spring"
            String requestBody = """
                    {"name": "spring"}
                    """;
            mockMvc.perform(put("/v1/tags/2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk());

            // Then - getAll should reflect updated name (cache invalidated)
            mockMvc.perform(get("/v1/tags")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].name").value("algorithm"))
                    .andExpect(jsonPath("$.data[1].name").value("spring"));
        }
    }

    @Nested
    @DisplayName("PUT /v1/tags/{id} - Not found (404)")
    class UpdateTagNotFoundTests {

        @Test
        @DisplayName("Should return 404 when tag does not exist")
        void shouldReturn404_whenTagNotFound() throws Exception {
            // Given
            String requestBody = """
                    {"name": "spring"}
                    """;

            // When & Then
            mockMvc.perform(put("/v1/tags/not-exist")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("TAG_001"));
        }
    }

    @Nested
    @DisplayName("PUT /v1/tags/{id} - Duplicate (409)")
    class UpdateTagDuplicateTests {

        @Test
        @DisplayName("Should return 409 when update name already exists")
        void shouldReturn409_whenNameDuplicateOnUpdate() throws Exception {
            // Given
            createTag("1", "java");
            createTag("2", "spring");

            String requestBody = """
                    {"name": "spring"}
                    """;

            // When & Then
            mockMvc.perform(put("/v1/tags/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("TAG_002"));
        }

        @Test
        @DisplayName("Should return 409 for case-insensitive duplicate update")
        void shouldReturn409_forCaseInsensitiveDuplicateOnUpdate() throws Exception {
            // Given
            createTag("1", "java");
            createTag("2", "spring");

            String requestBody = """
                    {"name": "SPRING"}
                    """;

            // When & Then
            mockMvc.perform(put("/v1/tags/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("TAG_002"));
        }
    }

    @Nested
    @DisplayName("PUT /v1/tags/{id} - Validation Error (400)")
    class UpdateTagValidationErrorTests {

        @Test
        @DisplayName("Should return 400 when name is blank")
        void shouldReturn400_whenNameIsBlank() throws Exception {
            // Given
            createTag("1", "java");

            String requestBody = """
                    {"name": ""}
                    """;

            // When & Then
            mockMvc.perform(put("/v1/tags/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("COM_001"));
        }

        @Test
        @DisplayName("Should return 400 when name is null")
        void shouldReturn400_whenNameIsNull() throws Exception {
            // Given
            createTag("1", "java");

            String requestBody = """
                    {"name": null}
                    """;

            // When & Then
            mockMvc.perform(put("/v1/tags/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("COM_001"));
        }

        @Test
        @DisplayName("Should return 400 when name exceeds 100 characters")
        void shouldReturn400_whenNameExceeds100Characters() throws Exception {
            // Given
            createTag("1", "java");

            String longName = "a".repeat(101);
            String requestBody = String.format("{\"name\": \"%s\"}", longName);

            // When & Then
            mockMvc.perform(put("/v1/tags/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value("COM_001"));
        }
    }
}
