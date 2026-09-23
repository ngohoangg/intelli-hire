package org.aibles.intellihirequestion.controller;

import org.aibles.intellihirequestion.config.RedisTestConfig;
import org.aibles.intellihirequestion.entity.DifficultyLevel;
import org.aibles.intellihirequestion.entity.Question;
import org.aibles.intellihirequestion.entity.StudyArticle;
import org.aibles.intellihirequestion.entity.enums.QuestionType;
import org.aibles.intellihirequestion.exception.ErrorCode;
import org.aibles.intellihirequestion.repository.DifficultyLevelRepository;
import org.aibles.intellihirequestion.repository.QuestionRepository;
import org.aibles.intellihirequestion.repository.StudyArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(RedisTestConfig.class)
class DifficultyControllerTest {

    private static final String DIFFICULTIES_CACHE = "difficulties";
    private static final String DIFFICULTIES_ALL_KEY = "all";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DifficultyLevelRepository difficultyLevelRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private StudyArticleRepository studyArticleRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        clearCache();
        questionRepository.deleteAll();
        studyArticleRepository.deleteAll();
        difficultyLevelRepository.deleteAll();
    }

    private void clearCache() {
        Cache difficultiesCache = cacheManager.getCache(DIFFICULTIES_CACHE);
        if (difficultiesCache != null) {
            difficultiesCache.clear();
        }
    }

    @Nested
    @DisplayName("GET /v1/difficulties - Success (200)")
    class GetAllDifficultyLevelsSuccessTests {

        @Test
        @DisplayName("Should return all difficulty levels ordered by weight ASC")
        void shouldReturnAllDifficultyLevels_orderedByWeightAsc() throws Exception {
            // Given
            DifficultyLevel junior = createAndSaveDifficultyLevel("Junior", 1, "Entry level questions");
            DifficultyLevel mid = createAndSaveDifficultyLevel("Mid", 2, "Intermediate level questions");
            DifficultyLevel senior = createAndSaveDifficultyLevel("Senior", 3, "Advanced level questions");

            clearCache(); // đảm bảo không lấy cache cũ

            // When & Then
            mockMvc.perform(get("/v1/difficulties"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data", hasSize(3)))
                    .andExpect(jsonPath("$.data[0].id").value(junior.getId()))
                    .andExpect(jsonPath("$.data[0].name").value("Junior"))
                    .andExpect(jsonPath("$.data[0].weight").value(1))
                    .andExpect(jsonPath("$.data[0].description").value("Entry level questions"))
                    .andExpect(jsonPath("$.data[1].id").value(mid.getId()))
                    .andExpect(jsonPath("$.data[1].name").value("Mid"))
                    .andExpect(jsonPath("$.data[1].weight").value(2))
                    .andExpect(jsonPath("$.data[1].description").value("Intermediate level questions"))
                    .andExpect(jsonPath("$.data[2].id").value(senior.getId()))
                    .andExpect(jsonPath("$.data[2].name").value("Senior"))
                    .andExpect(jsonPath("$.data[2].weight").value(3))
                    .andExpect(jsonPath("$.data[2].description").value("Advanced level questions"));
        }

        @Test
        @DisplayName("Should return empty list when no difficulty levels exist")
        void shouldReturnEmptyList_whenNoDifficultyLevels() throws Exception {
            // When & Then
            mockMvc.perform(get("/v1/difficulties"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }

        @Test
        @DisplayName("Should use cache on second request")
        void shouldUseCacheOnSecondRequest() throws Exception {
            // Given
            createAndSaveDifficultyLevel("Junior", 1, "Entry level");
            createAndSaveDifficultyLevel("Mid", 2, "Mid level");

            // First request - should hit database and cache result
            mockMvc.perform(get("/v1/difficulties"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data", hasSize(2)));

            // (Optional) assert cache has value - tùy cache provider, thường OK với ConcurrentMapCache
            Cache cache = cacheManager.getCache(DIFFICULTIES_CACHE);
            assertNotNull(cache, "Cache '" + DIFFICULTIES_CACHE + "' not found");
            assertNotNull(cache.get(DIFFICULTIES_ALL_KEY), "Cache entry for key 'all' was not created");

            // Add new difficulty level after first request
            createAndSaveDifficultyLevel("Senior", 3, "Senior level");

            // Second request - should return cached data (still 2)
            mockMvc.perform(get("/v1/difficulties"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data", hasSize(2)));
        }

        @Test
        @DisplayName("Should return correct order when weights are different")
        void shouldReturnCorrectOrder_whenWeightsAreDifferent() throws Exception {
            // Given - insert in random order
            createAndSaveDifficultyLevel("Senior", 10, "Desc 3");
            createAndSaveDifficultyLevel("Junior", 1, "Desc 1");
            createAndSaveDifficultyLevel("Mid", 5, "Desc 2");

            clearCache();

            // When & Then
            mockMvc.perform(get("/v1/difficulties"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data", hasSize(3)))
                    .andExpect(jsonPath("$.data[0].name").value("Junior"))
                    .andExpect(jsonPath("$.data[0].weight").value(1))
                    .andExpect(jsonPath("$.data[1].name").value("Mid"))
                    .andExpect(jsonPath("$.data[1].weight").value(5))
                    .andExpect(jsonPath("$.data[2].name").value("Senior"))
                    .andExpect(jsonPath("$.data[2].weight").value(10));
        }

        @Test
        @DisplayName("Should return response with correct structure")
        void shouldReturnResponseWithCorrectStructure() throws Exception {
            // Given
            createAndSaveDifficultyLevel("Junior", 1, "Entry level");

            clearCache();

            // When & Then
            mockMvc.perform(get("/v1/difficulties"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").exists())
                    .andExpect(jsonPath("$.success").isBoolean())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.error_code").doesNotExist())
                    .andExpect(jsonPath("$.errors").doesNotExist());
        }

        @Test
        @DisplayName("Should handle multiple difficulty levels with same weight")
        void shouldHandleMultipleLevelsWithSameWeight() throws Exception {
            // Given
            createAndSaveDifficultyLevel("Level A", 1, "Description A");
            createAndSaveDifficultyLevel("Level B", 1, "Description B");
            createAndSaveDifficultyLevel("Level C", 2, "Description C");

            clearCache();

            // When & Then
            mockMvc.perform(get("/v1/difficulties"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data", hasSize(3)))
                    .andExpect(jsonPath("$.data[0].weight").value(1))
                    .andExpect(jsonPath("$.data[1].weight").value(1))
                    .andExpect(jsonPath("$.data[2].weight").value(2));
        }
    }

    @Nested
    @DisplayName("GET /v1/difficulties/{id} - Success and Not Found")
    class GetDifficultyLevelByIdTests {

        @Test
        @DisplayName("Should return difficulty level details when id exists")
        void shouldReturnDifficultyLevelDetails_whenIdExists() throws Exception {
            DifficultyLevel difficultyLevel = createAndSaveDifficultyLevel("Junior", 1, "Entry level questions");

            mockMvc.perform(get("/v1/difficulties/{id}", difficultyLevel.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(difficultyLevel.getId()))
                    .andExpect(jsonPath("$.data.name").value("Junior"))
                    .andExpect(jsonPath("$.data.weight").value(1))
                    .andExpect(jsonPath("$.data.description").value("Entry level questions"));
        }

        @Test
        @DisplayName("Should return 404 with DIF_001 when id does not exist")
        void shouldReturn404_whenDifficultyLevelNotFound() throws Exception {
            mockMvc.perform(get("/v1/difficulties/{id}", "missing-id"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.DIF_001.getCode()))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }
    }

    @Nested
    @DisplayName("POST /v1/difficulties - Create")
    class CreateDifficultyLevelTests {

        @Test
        @DisplayName("Should create difficulty level and return 201")
        void shouldCreateDifficultyLevel_andReturn201() throws Exception {
            String body = """
                    {
                      "name": "Senior",
                      "weight": 3,
                      "description": "Requires 5+ years experience"
                    }
                    """;

            mockMvc.perform(post("/v1/difficulties")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.error_code").value(nullValue()))
                    .andExpect(jsonPath("$.data.id").isNotEmpty())
                    .andExpect(jsonPath("$.data.name").value("Senior"))
                    .andExpect(jsonPath("$.data.weight").value(3))
                    .andExpect(jsonPath("$.data.description").value("Requires 5+ years experience"));
        }

        @Test
        @DisplayName("Should trim name before persisting and returning response")
        void shouldTrimNameBeforePersisting() throws Exception {
            String body = """
                    {
                      "name": " Senior ",
                      "weight": 3,
                      "description": "Requires 5+ years experience"
                    }
                    """;

            mockMvc.perform(post("/v1/difficulties")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("Senior"));
        }

        @Test
        @DisplayName("Should return 409 when difficulty level name already exists")
        void shouldReturn409WhenNameExists() throws Exception {
            createAndSaveDifficultyLevel("Senior", 1, "Already exists");

            String body = """
                    {
                      "name": "Senior",
                      "weight": 3,
                      "description": "Requires 5+ years experience"
                    }
                    """;

            mockMvc.perform(post("/v1/difficulties")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.DIF_002.getCode()))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("Should return 409 when difficulty level name already exists with different casing")
        void shouldReturn409WhenNameExistsIgnoringCase() throws Exception {
            createAndSaveDifficultyLevel("Senior", 1, "Already exists");

            String body = """
                    {
                      "name": "senior",
                      "weight": 3,
                      "description": "Requires 5+ years experience"
                    }
                    """;

            mockMvc.perform(post("/v1/difficulties")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.DIF_002.getCode()))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("Should return 400 when name is blank")
        void shouldReturn400WhenNameIsBlank() throws Exception {
            String body = """
                    {
                      "name": "   ",
                      "weight": 3,
                      "description": "Requires 5+ years experience"
                    }
                    """;

            mockMvc.perform(post("/v1/difficulties")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.COM_001.getCode()))
                    .andExpect(jsonPath("$.errors[0].field").value("name"))
                    .andExpect(jsonPath("$.errors[0].message").value("Name is required"))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("Should return 400 when name exceeds max length")
        void shouldReturn400WhenNameExceedsMaxLength() throws Exception {
            String body = """
                    {
                      "name": "%s",
                      "weight": 3,
                      "description": "Requires 5+ years experience"
                    }
                    """.formatted("S".repeat(51));

            mockMvc.perform(post("/v1/difficulties")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.COM_001.getCode()))
                    .andExpect(jsonPath("$.errors[0].field").value("name"))
                    .andExpect(jsonPath("$.errors[0].message").value("Name must not exceed 50 characters"))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("Should return 400 when weight is missing")
        void shouldReturn400WhenWeightIsMissing() throws Exception {
            String body = """
                    {
                      "name": "Senior",
                      "description": "Requires 5+ years experience"
                    }
                    """;

            mockMvc.perform(post("/v1/difficulties")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.COM_001.getCode()))
                    .andExpect(jsonPath("$.errors[0].field").value("weight"))
                    .andExpect(jsonPath("$.errors[0].message").value("Weight is required"))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("Should return 400 when weight is below minimum")
        void shouldReturn400WhenWeightIsBelowMinimum() throws Exception {
            String body = """
                    {
                      "name": "Senior",
                      "weight": 0,
                      "description": "Requires 5+ years experience"
                    }
                    """;

            mockMvc.perform(post("/v1/difficulties")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.COM_001.getCode()))
                    .andExpect(jsonPath("$.errors[0].field").value("weight"))
                    .andExpect(jsonPath("$.errors[0].message").value("Weight must be greater than or equal to 1"))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("Should return 400 when weight exceeds maximum")
        void shouldReturn400WhenWeightExceedsMaximum() throws Exception {
            String body = """
                    {
                      "name": "Senior",
                      "weight": 101,
                      "description": "Requires 5+ years experience"
                    }
                    """;

            mockMvc.perform(post("/v1/difficulties")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.COM_001.getCode()))
                    .andExpect(jsonPath("$.errors[0].field").value("weight"))
                    .andExpect(jsonPath("$.errors[0].message").value("Weight must be less than or equal to 100"))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("Should return 400 when description exceeds max length")
        void shouldReturn400WhenDescriptionExceedsMaxLength() throws Exception {
            String body = """
                    {
                      "name": "Senior",
                      "weight": 3,
                      "description": "%s"
                    }
                    """.formatted("D".repeat(256));

            mockMvc.perform(post("/v1/difficulties")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.COM_001.getCode()))
                    .andExpect(jsonPath("$.errors[0].field").value("description"))
                    .andExpect(jsonPath("$.errors[0].message").value("Description must not exceed 255 characters"))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("Should return 400 when request body is malformed JSON")
        void shouldReturn400WhenRequestBodyIsMalformedJson() throws Exception {
            String body = """
                    {
                      "name": "Senior",
                      "weight": "abc",
                      "description": "Requires 5+ years experience"
                    }
                    """;

            mockMvc.perform(post("/v1/difficulties")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.COM_003.getCode()))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("Should invalidate difficulty cache after create")
        void shouldInvalidateDifficultyCacheAfterCreate() throws Exception {
            createAndSaveDifficultyLevel("Junior", 1, "Entry level");
            createAndSaveDifficultyLevel("Mid", 2, "Mid level");

            mockMvc.perform(get("/v1/difficulties"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(2)));

            String body = """
                    {
                      "name": "Senior",
                      "weight": 3,
                      "description": "Senior level"
                    }
                    """;

            mockMvc.perform(post("/v1/difficulties")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated());

            mockMvc.perform(get("/v1/difficulties"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(3)));
        }
    }

    @Nested
    @DisplayName("PUT /v1/difficulties/{id} - Update")
    class UpdateDifficultyLevelTests {

        @Test
        @DisplayName("Should update difficulty level and return 200")
        void shouldUpdateDifficultyLevelAndReturn200() throws Exception {
            DifficultyLevel existing = createAndSaveDifficultyLevel("Junior", 1, "Entry level");

            String body = """
                    {
                      "name": "Senior",
                      "weight": 3,
                      "description": "Advanced level"
                    }
                    """;

            mockMvc.perform(put("/v1/difficulties/{id}", existing.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.error_code").value(nullValue()))
                    .andExpect(jsonPath("$.data.id").value(existing.getId()))
                    .andExpect(jsonPath("$.data.name").value("Senior"))
                    .andExpect(jsonPath("$.data.weight").value(3))
                    .andExpect(jsonPath("$.data.description").value("Advanced level"));
        }

        @Test
        @DisplayName("Should trim name before updating")
        void shouldTrimNameBeforeUpdating() throws Exception {
            DifficultyLevel existing = createAndSaveDifficultyLevel("Junior", 1, "Entry level");

            String body = """
                    {
                      "name": " Senior ",
                      "weight": 3,
                      "description": "Advanced level"
                    }
                    """;

            mockMvc.perform(put("/v1/difficulties/{id}", existing.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(existing.getId()))
                    .andExpect(jsonPath("$.data.name").value("Senior"));
        }

        @Test
        @DisplayName("Should allow update when name is unchanged")
        void shouldAllowUpdateWhenNameIsUnchanged() throws Exception {
            DifficultyLevel existing = createAndSaveDifficultyLevel("Senior", 1, "Entry level");

            String body = """
                    {
                      "name": "Senior",
                      "weight": 5,
                      "description": "Updated description"
                    }
                    """;

            mockMvc.perform(put("/v1/difficulties/{id}", existing.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(existing.getId()))
                    .andExpect(jsonPath("$.data.name").value("Senior"))
                    .andExpect(jsonPath("$.data.weight").value(5))
                    .andExpect(jsonPath("$.data.description").value("Updated description"));
        }

        @Test
        @DisplayName("Should allow update when name only changes casing")
        void shouldAllowUpdateWhenNameOnlyChangesCasing() throws Exception {
            DifficultyLevel existing = createAndSaveDifficultyLevel("Senior", 1, "Entry level");

            String body = """
                    {
                      "name": "senior",
                      "weight": 5,
                      "description": "Updated description"
                    }
                    """;

            mockMvc.perform(put("/v1/difficulties/{id}", existing.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(existing.getId()))
                    .andExpect(jsonPath("$.data.name").value("senior"));
        }

        @Test
        @DisplayName("Should return 404 when difficulty level does not exist")
        void shouldReturn404WhenDifficultyLevelDoesNotExist() throws Exception {
            String body = """
                    {
                      "name": "Senior",
                      "weight": 3,
                      "description": "Advanced level"
                    }
                    """;

            mockMvc.perform(put("/v1/difficulties/{id}", "missing-id")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.DIF_001.getCode()))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("Should return 409 when updated name already exists")
        void shouldReturn409WhenUpdatedNameAlreadyExists() throws Exception {
            createAndSaveDifficultyLevel("Junior", 1, "Entry level");
            DifficultyLevel existing = createAndSaveDifficultyLevel("Senior", 3, "Advanced level");

            String body = """
                    {
                      "name": "Junior",
                      "weight": 4,
                      "description": "Updated description"
                    }
                    """;

            mockMvc.perform(put("/v1/difficulties/{id}", existing.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.DIF_002.getCode()))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("Should return 409 when updated name already exists ignoring case")
        void shouldReturn409WhenUpdatedNameAlreadyExistsIgnoringCase() throws Exception {
            createAndSaveDifficultyLevel("Junior", 1, "Entry level");
            DifficultyLevel existing = createAndSaveDifficultyLevel("Senior", 3, "Advanced level");

            String body = """
                    {
                      "name": "junior",
                      "weight": 4,
                      "description": "Updated description"
                    }
                    """;

            mockMvc.perform(put("/v1/difficulties/{id}", existing.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.DIF_002.getCode()))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("Should return 400 when name is blank")
        void shouldReturn400WhenUpdateNameIsBlank() throws Exception {
            DifficultyLevel existing = createAndSaveDifficultyLevel("Senior", 1, "Entry level");

            String body = """
                    {
                      "name": "   ",
                      "weight": 3,
                      "description": "Advanced level"
                    }
                    """;

            mockMvc.perform(put("/v1/difficulties/{id}", existing.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.COM_001.getCode()))
                    .andExpect(jsonPath("$.errors[0].field").value("name"))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("Should return 400 when name exceeds max length")
        void shouldReturn400WhenUpdateNameExceedsMaxLength() throws Exception {
            DifficultyLevel existing = createAndSaveDifficultyLevel("Senior", 1, "Entry level");

            String body = """
                    {
                      "name": "%s",
                      "weight": 3,
                      "description": "Advanced level"
                    }
                    """.formatted("S".repeat(51));

            mockMvc.perform(put("/v1/difficulties/{id}", existing.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.COM_001.getCode()))
                    .andExpect(jsonPath("$.errors[0].field").value("name"))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("Should return 400 when weight is missing")
        void shouldReturn400WhenUpdateWeightIsMissing() throws Exception {
            DifficultyLevel existing = createAndSaveDifficultyLevel("Senior", 1, "Entry level");

            String body = """
                    {
                      "name": "Senior",
                      "description": "Advanced level"
                    }
                    """;

            mockMvc.perform(put("/v1/difficulties/{id}", existing.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.COM_001.getCode()))
                    .andExpect(jsonPath("$.errors[0].field").value("weight"))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("Should return 400 when weight is below minimum")
        void shouldReturn400WhenUpdateWeightIsBelowMinimum() throws Exception {
            DifficultyLevel existing = createAndSaveDifficultyLevel("Senior", 1, "Entry level");

            String body = """
                    {
                      "name": "Senior",
                      "weight": 0,
                      "description": "Advanced level"
                    }
                    """;

            mockMvc.perform(put("/v1/difficulties/{id}", existing.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.COM_001.getCode()))
                    .andExpect(jsonPath("$.errors[0].field").value("weight"))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("Should return 400 when weight exceeds maximum")
        void shouldReturn400WhenUpdateWeightExceedsMaximum() throws Exception {
            DifficultyLevel existing = createAndSaveDifficultyLevel("Senior", 1, "Entry level");

            String body = """
                    {
                      "name": "Senior",
                      "weight": 101,
                      "description": "Advanced level"
                    }
                    """;

            mockMvc.perform(put("/v1/difficulties/{id}", existing.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.COM_001.getCode()))
                    .andExpect(jsonPath("$.errors[0].field").value("weight"))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("Should return 400 when description exceeds max length")
        void shouldReturn400WhenUpdateDescriptionExceedsMaxLength() throws Exception {
            DifficultyLevel existing = createAndSaveDifficultyLevel("Senior", 1, "Entry level");

            String body = """
                    {
                      "name": "Senior",
                      "weight": 3,
                      "description": "%s"
                    }
                    """.formatted("D".repeat(256));

            mockMvc.perform(put("/v1/difficulties/{id}", existing.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.COM_001.getCode()))
                    .andExpect(jsonPath("$.errors[0].field").value("description"))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("Should return 400 when update request body is malformed JSON")
        void shouldReturn400WhenUpdateRequestBodyIsMalformedJson() throws Exception {
            DifficultyLevel existing = createAndSaveDifficultyLevel("Senior", 1, "Entry level");

            String body = """
                    {
                      "name": "Senior",
                      "weight": "abc",
                      "description": "Advanced level"
                    }
                    """;

            mockMvc.perform(put("/v1/difficulties/{id}", existing.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.COM_003.getCode()))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("Should invalidate all difficulties cache after update")
        void shouldInvalidateAllDifficultiesCacheAfterUpdate() throws Exception {
            DifficultyLevel existing = createAndSaveDifficultyLevel("Junior", 1, "Entry level");
            createAndSaveDifficultyLevel("Mid", 2, "Mid level");

            mockMvc.perform(get("/v1/difficulties"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(2)));

            String body = """
                    {
                      "name": "Senior",
                      "weight": 3,
                      "description": "Advanced level"
                    }
                    """;

            mockMvc.perform(put("/v1/difficulties/{id}", existing.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(existing.getId()))
                    .andExpect(jsonPath("$.data.name").value("Senior"));

            mockMvc.perform(get("/v1/difficulties"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(2)))
                    .andExpect(jsonPath("$.data[0].name").value("Mid"))
                    .andExpect(jsonPath("$.data[1].id").value(existing.getId()))
                    .andExpect(jsonPath("$.data[1].name").value("Senior"));
        }

    }

    @Nested
    @DisplayName("DELETE /v1/difficulties/{id} - Delete")
    class DeleteDifficultyLevelTests {

        @Test
        @DisplayName("Should delete difficulty level and return 200")
        void shouldDeleteDifficultyLevelAndReturn200() throws Exception {
            DifficultyLevel existing = createAndSaveDifficultyLevel("Senior", 3, "Advanced level");

            mockMvc.perform(delete("/v1/difficulties/{id}", existing.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.error_code").value(nullValue()))
                    .andExpect(jsonPath("$.data").isEmpty());

            org.assertj.core.api.Assertions.assertThat(difficultyLevelRepository.findById(existing.getId())).isEmpty();
        }

        @Test
        @DisplayName("Should return 404 when difficulty level does not exist")
        void shouldReturn404WhenDeletingMissingDifficultyLevel() throws Exception {
            mockMvc.perform(delete("/v1/difficulties/{id}", "missing-id"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.DIF_001.getCode()))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("Should return 400 when difficulty level is used by questions")
        void shouldReturn400WhenDifficultyLevelUsedByQuestions() throws Exception {
            DifficultyLevel existing = createAndSaveDifficultyLevel("Senior", 3, "Advanced level");
            createAndSaveQuestion(existing.getId());

            mockMvc.perform(delete("/v1/difficulties/{id}", existing.getId()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.DIF_003.getCode()))
                    .andExpect(jsonPath("$.data").doesNotExist());

            org.assertj.core.api.Assertions.assertThat(difficultyLevelRepository.findById(existing.getId())).isPresent();
        }

        @Test
        @DisplayName("Should return 400 when difficulty level is used by study articles")
        void shouldReturn400WhenDifficultyLevelUsedByStudyArticles() throws Exception {
            DifficultyLevel existing = createAndSaveDifficultyLevel("Senior", 3, "Advanced level");
            createAndSaveStudyArticle(existing.getId());

            mockMvc.perform(delete("/v1/difficulties/{id}", existing.getId()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error_code").value(ErrorCode.DIF_003.getCode()))
                    .andExpect(jsonPath("$.data").doesNotExist());

            org.assertj.core.api.Assertions.assertThat(difficultyLevelRepository.findById(existing.getId())).isPresent();
        }

        @Test
        @DisplayName("Should invalidate difficulty cache after delete")
        void shouldInvalidateDifficultyCacheAfterDelete() throws Exception {
            DifficultyLevel junior = createAndSaveDifficultyLevel("Junior", 1, "Entry level");
            DifficultyLevel senior = createAndSaveDifficultyLevel("Senior", 3, "Advanced level");

            mockMvc.perform(get("/v1/difficulties"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(2)));

            mockMvc.perform(delete("/v1/difficulties/{id}", senior.getId()))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/v1/difficulties"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].id").value(junior.getId()))
                    .andExpect(jsonPath("$.data[0].name").value("Junior"));
        }
    }

    private DifficultyLevel createAndSaveDifficultyLevel(String name, Integer weight, String description) {
        DifficultyLevel level = new DifficultyLevel();
        level.setName(name);
        level.setWeight(weight);
        level.setDescription(description);
        return difficultyLevelRepository.save(level);
    }

    private Question createAndSaveQuestion(String difficultyId) {
        Question question = new Question();
        question.setTopicId("topic-1");
        question.setDifficultyId(difficultyId);
        question.setQuestionText("What is polymorphism?");
        question.setQuestionType(QuestionType.TECHNICAL);
        question.setIsActive(true);
        return questionRepository.save(question);
    }

    private StudyArticle createAndSaveStudyArticle(String difficultyId) {
        StudyArticle studyArticle = new StudyArticle();
        studyArticle.setTopicId("topic-1");
        studyArticle.setDifficultyId(difficultyId);
        studyArticle.setTitle("Senior Java Roadmap");
        studyArticle.setUrl("https://example.com/senior-java-roadmap");
        studyArticle.setDescription("Advanced content");
        studyArticle.setSource("internal");
        studyArticle.setIsActive(true);
        return studyArticleRepository.save(studyArticle);
    }
}
