package org.aibles.intellihirequestion.service.impl;

import org.aibles.intellihirequestion.dto.DifficultyLevelRequest;
import org.aibles.intellihirequestion.dto.DifficultyLevelResponse;
import org.aibles.intellihirequestion.entity.DifficultyLevel;
import org.aibles.intellihirequestion.exception.BadRequestException;
import org.aibles.intellihirequestion.exception.DuplicateException;
import org.aibles.intellihirequestion.exception.ErrorCode;
import org.aibles.intellihirequestion.exception.NotFoundException;
import org.aibles.intellihirequestion.mapper.DifficultyLevelMapper;
import org.aibles.intellihirequestion.repository.DifficultyLevelRepository;
import org.aibles.intellihirequestion.repository.QuestionRepository;
import org.aibles.intellihirequestion.repository.StudyArticleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DifficultyLevelServiceImpl")
class DifficultyLevelServiceImplTest {

    @Mock
    private DifficultyLevelRepository difficultyLevelRepository;

    @Mock
    private DifficultyLevelMapper difficultyLevelMapper;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private StudyArticleRepository studyArticleRepository;

    @InjectMocks
    private DifficultyLevelServiceImpl difficultyLevelService;

    @Nested
    @DisplayName("getAll()")
    class GetAllTests {

        @Test
        @DisplayName("Should return all difficulty levels ordered by weight ASC")
        void shouldReturnAllDifficultyLevels_orderedByWeightAsc() {
            // Given
            DifficultyLevel junior = createDifficultyLevel("1", "Junior", 1, "Entry level");
            DifficultyLevel mid = createDifficultyLevel("2", "Mid", 2, "Mid level");
            DifficultyLevel senior = createDifficultyLevel("3", "Senior", 3, "Senior level");

            List<DifficultyLevel> entities = Arrays.asList(junior, mid, senior);
            List<DifficultyLevelResponse> responses = Arrays.asList(
                    createResponse("1", "Junior", 1, "Entry level"),
                    createResponse("2", "Mid", 2, "Mid level"),
                    createResponse("3", "Senior", 3, "Senior level")
            );

            when(difficultyLevelRepository.findAllByOrderByWeightAsc()).thenReturn(entities);
            when(difficultyLevelMapper.toResponseList(entities)).thenReturn(responses);

            // When
            List<DifficultyLevelResponse> result = difficultyLevelService.getAll();

            // Then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getName()).isEqualTo("Junior");
            assertThat(result.get(0).getWeight()).isEqualTo(1);
            assertThat(result.get(1).getName()).isEqualTo("Mid");
            assertThat(result.get(1).getWeight()).isEqualTo(2);
            assertThat(result.get(2).getName()).isEqualTo("Senior");
            assertThat(result.get(2).getWeight()).isEqualTo(3);

            verify(difficultyLevelRepository).findAllByOrderByWeightAsc();
            verify(difficultyLevelMapper).toResponseList(entities);
        }

        @Test
        @DisplayName("Should return empty list when no difficulty levels exist")
        void shouldReturnEmptyList_whenNoDifficultyLevels() {
            // Given
            when(difficultyLevelRepository.findAllByOrderByWeightAsc())
                    .thenReturn(Collections.emptyList());
            when(difficultyLevelMapper.toResponseList(Collections.emptyList()))
                    .thenReturn(Collections.emptyList());

            // When
            List<DifficultyLevelResponse> result = difficultyLevelService.getAll();

            // Then
            assertThat(result).isEmpty();
            verify(difficultyLevelRepository).findAllByOrderByWeightAsc();
        }

        @Test
        @DisplayName("Should map entity fields correctly to response")
        void shouldMapEntityFieldsCorrectly() {
            // Given
            DifficultyLevel difficulty = createDifficultyLevel(
                    "test-id",
                    "Test Level",
                    5,
                    "Test description"
            );

            List<DifficultyLevel> entities = Collections.singletonList(difficulty);
            DifficultyLevelResponse response = createResponse(
                    "test-id",
                    "Test Level",
                    5,
                    "Test description"
            );

            when(difficultyLevelRepository.findAllByOrderByWeightAsc()).thenReturn(entities);
            when(difficultyLevelMapper.toResponseList(entities))
                    .thenReturn(Collections.singletonList(response));

            // When
            List<DifficultyLevelResponse> result = difficultyLevelService.getAll();

            // Then
            assertThat(result).hasSize(1);
            DifficultyLevelResponse resultResponse = result.get(0);
            assertThat(resultResponse.getId()).isEqualTo("test-id");
            assertThat(resultResponse.getName()).isEqualTo("Test Level");
            assertThat(resultResponse.getWeight()).isEqualTo(5);
            assertThat(resultResponse.getDescription()).isEqualTo("Test description");
        }

        @Test
        @DisplayName("Should maintain correct order when weights are different")
        void shouldMaintainCorrectOrder_whenWeightsAreDifferent() {
            // Given
            DifficultyLevel level1 = createDifficultyLevel("1", "Level 1", 1, "Desc 1");
            DifficultyLevel level2 = createDifficultyLevel("2", "Level 2", 5, "Desc 2");
            DifficultyLevel level3 = createDifficultyLevel("3", "Level 3", 10, "Desc 3");

            List<DifficultyLevel> entities = Arrays.asList(level1, level2, level3);
            List<DifficultyLevelResponse> responses = Arrays.asList(
                    createResponse("1", "Level 1", 1, "Desc 1"),
                    createResponse("2", "Level 2", 5, "Desc 2"),
                    createResponse("3", "Level 3", 10, "Desc 3")
            );

            when(difficultyLevelRepository.findAllByOrderByWeightAsc()).thenReturn(entities);
            when(difficultyLevelMapper.toResponseList(entities)).thenReturn(responses);

            // When
            List<DifficultyLevelResponse> result = difficultyLevelService.getAll();

            // Then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getWeight()).isEqualTo(1);
            assertThat(result.get(1).getWeight()).isEqualTo(5);
            assertThat(result.get(2).getWeight()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("getById()")
    class GetByIdTests {

        @Test
        @DisplayName("Should return difficulty level when id exists")
        void shouldReturnDifficultyLevel_whenIdExists() {
            DifficultyLevel entity = createDifficultyLevel("diff-1", "Junior", 1, "Entry level");
            DifficultyLevelResponse response = createResponse("diff-1", "Junior", 1, "Entry level");

            when(difficultyLevelRepository.findById("diff-1")).thenReturn(java.util.Optional.of(entity));
            when(difficultyLevelMapper.toResponse(entity)).thenReturn(response);

            DifficultyLevelResponse result = difficultyLevelService.getById("diff-1");

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("diff-1");
            assertThat(result.getName()).isEqualTo("Junior");
            assertThat(result.getWeight()).isEqualTo(1);
            assertThat(result.getDescription()).isEqualTo("Entry level");

            verify(difficultyLevelRepository).findById("diff-1");
            verify(difficultyLevelMapper).toResponse(entity);
        }

        @Test
        @DisplayName("Should throw NotFoundException with DIF_001 when id does not exist")
        void shouldThrowNotFoundException_whenIdDoesNotExist() {
            when(difficultyLevelRepository.findById("missing-id")).thenReturn(java.util.Optional.empty());

            assertThatThrownBy(() -> difficultyLevelService.getById("missing-id"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Không tìm thấy độ khó");

            verify(difficultyLevelRepository).findById("missing-id");
            verify(difficultyLevelMapper, never()).toResponse(any());
        }
    }

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("Should create difficulty level successfully")
        void shouldCreateDifficultyLevelSuccessfully() {
            DifficultyLevelRequest request = createRequest(" Senior ", 3, "Requires 5+ years experience");

            DifficultyLevel entity = createDifficultyLevel(null, " Senior ", 3, "Requires 5+ years experience");
            DifficultyLevel saved = createDifficultyLevel("diff-3", "Senior", 3, "Requires 5+ years experience");
            DifficultyLevelResponse response = createResponse("diff-3", "Senior", 3, "Requires 5+ years experience");

            when(difficultyLevelRepository.existsByNameIgnoreCase("Senior")).thenReturn(false);
            when(difficultyLevelMapper.toEntity(request)).thenReturn(entity);
            when(difficultyLevelRepository.save(entity)).thenReturn(saved);
            when(difficultyLevelMapper.toResponse(saved)).thenReturn(response);

            DifficultyLevelResponse result = difficultyLevelService.create(request);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("diff-3");
            assertThat(result.getName()).isEqualTo("Senior");
            assertThat(result.getWeight()).isEqualTo(3);
            assertThat(entity.getName()).isEqualTo("Senior");
            verify(difficultyLevelRepository).existsByNameIgnoreCase("Senior");
            verify(difficultyLevelRepository).save(entity);
        }

        @Test
        @DisplayName("Should check duplicate after trimming input name")
        void shouldCheckDuplicateAfterTrimmingInputName() {
            DifficultyLevelRequest request = createRequest(" Senior ", 3, "desc");

            when(difficultyLevelRepository.existsByNameIgnoreCase("Senior")).thenReturn(true);

            assertThatThrownBy(() -> difficultyLevelService.create(request))
                    .isInstanceOf(DuplicateException.class)
                    .hasMessage("Độ khó đã tồn tại");

            verify(difficultyLevelRepository).existsByNameIgnoreCase("Senior");
            verify(difficultyLevelMapper, never()).toEntity(any());
            verify(difficultyLevelRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw DuplicateException with DIF_002 when name exists")
        void shouldThrowDuplicateExceptionWhenNameExists() {
            DifficultyLevelRequest request = createRequest("Senior", 3, "desc");

            when(difficultyLevelRepository.existsByNameIgnoreCase("Senior")).thenReturn(true);

            assertThatThrownBy(() -> difficultyLevelService.create(request))
                    .isInstanceOf(DuplicateException.class)
                    .hasMessage("Độ khó đã tồn tại");

            verify(difficultyLevelRepository).existsByNameIgnoreCase("Senior");
            verify(difficultyLevelMapper, never()).toEntity(any());
            verify(difficultyLevelRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw DuplicateException for case-insensitive duplicate")
        void shouldThrowDuplicateExceptionForCaseInsensitiveDuplicate() {
            DifficultyLevelRequest request = createRequest("senior", 3, "desc");

            when(difficultyLevelRepository.existsByNameIgnoreCase("senior")).thenReturn(true);

            assertThatThrownBy(() -> difficultyLevelService.create(request))
                    .isInstanceOf(DuplicateException.class)
                    .hasMessage("Độ khó đã tồn tại");

            verify(difficultyLevelRepository).existsByNameIgnoreCase("senior");
            verify(difficultyLevelMapper, never()).toEntity(any());
            verify(difficultyLevelRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("update()")
    class UpdateTests {

        @Test
        @DisplayName("Should update difficulty level successfully when name changed")
        void shouldUpdateDifficultyLevelSuccessfullyWhenNameChanged() {
            String id = "diff-1";
            DifficultyLevelRequest request = createRequest(" Senior ", 3, "Advanced level");

            DifficultyLevel existing = createDifficultyLevel(id, "Junior", 1, "Entry level");
            DifficultyLevel saved = createDifficultyLevel(id, "Senior", 3, "Advanced level");
            DifficultyLevelResponse response = createResponse(id, "Senior", 3, "Advanced level");

            when(difficultyLevelRepository.findById(id)).thenReturn(java.util.Optional.of(existing));
            when(difficultyLevelRepository.existsByNameIgnoreCaseAndIdNot("Senior", id)).thenReturn(false);
            when(difficultyLevelRepository.save(existing)).thenReturn(saved);
            when(difficultyLevelMapper.toResponse(saved)).thenReturn(response);

            DifficultyLevelResponse result = difficultyLevelService.update(id, request);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(id);
            assertThat(result.getName()).isEqualTo("Senior");
            assertThat(result.getWeight()).isEqualTo(3);
            assertThat(result.getDescription()).isEqualTo("Advanced level");
            assertThat(existing.getName()).isEqualTo("Senior");
            assertThat(existing.getWeight()).isEqualTo(3);
            assertThat(existing.getDescription()).isEqualTo("Advanced level");

            verify(difficultyLevelRepository).findById(id);
            verify(difficultyLevelRepository).existsByNameIgnoreCaseAndIdNot("Senior", id);
            verify(difficultyLevelRepository).save(existing);
            verify(difficultyLevelMapper).toResponse(saved);
            verify(difficultyLevelMapper, never()).toEntity(any());
        }

        @Test
        @DisplayName("Should not check duplicate when name is unchanged")
        void shouldNotCheckDuplicateWhenNameIsUnchanged() {
            String id = "diff-2";
            DifficultyLevelRequest request = createRequest("Senior", 5, "Updated description");

            DifficultyLevel existing = createDifficultyLevel(id, "Senior", 1, "Entry level");
            DifficultyLevel saved = createDifficultyLevel(id, "Senior", 5, "Updated description");
            DifficultyLevelResponse response = createResponse(id, "Senior", 5, "Updated description");

            when(difficultyLevelRepository.findById(id)).thenReturn(java.util.Optional.of(existing));
            when(difficultyLevelRepository.save(existing)).thenReturn(saved);
            when(difficultyLevelMapper.toResponse(saved)).thenReturn(response);

            DifficultyLevelResponse result = difficultyLevelService.update(id, request);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Senior");
            assertThat(result.getWeight()).isEqualTo(5);
            assertThat(result.getDescription()).isEqualTo("Updated description");

            verify(difficultyLevelRepository).findById(id);
            verify(difficultyLevelRepository, never()).existsByNameIgnoreCaseAndIdNot(any(), any());
            verify(difficultyLevelRepository).save(existing);
            verify(difficultyLevelMapper).toResponse(saved);
        }

        @Test
        @DisplayName("Should not check duplicate when name only changes casing")
        void shouldNotCheckDuplicateWhenNameOnlyChangesCasing() {
            String id = "diff-3";
            DifficultyLevelRequest request = createRequest("senior", 5, "Updated description");

            DifficultyLevel existing = createDifficultyLevel(id, "Senior", 1, "Entry level");
            DifficultyLevel saved = createDifficultyLevel(id, "senior", 5, "Updated description");
            DifficultyLevelResponse response = createResponse(id, "senior", 5, "Updated description");

            when(difficultyLevelRepository.findById(id)).thenReturn(java.util.Optional.of(existing));
            when(difficultyLevelRepository.save(existing)).thenReturn(saved);
            when(difficultyLevelMapper.toResponse(saved)).thenReturn(response);

            DifficultyLevelResponse result = difficultyLevelService.update(id, request);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("senior");

            verify(difficultyLevelRepository).findById(id);
            verify(difficultyLevelRepository, never()).existsByNameIgnoreCaseAndIdNot(any(), any());
            verify(difficultyLevelRepository).save(existing);
            verify(difficultyLevelMapper).toResponse(saved);
        }

        @Test
        @DisplayName("Should throw NotFoundException when difficulty level does not exist")
        void shouldThrowNotFoundExceptionWhenDifficultyLevelDoesNotExist() {
            String id = "missing-id";
            DifficultyLevelRequest request = createRequest("Senior", 3, "Advanced level");

            when(difficultyLevelRepository.findById(id)).thenReturn(java.util.Optional.empty());

            assertThatThrownBy(() -> difficultyLevelService.update(id, request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Không tìm thấy độ khó");

            verify(difficultyLevelRepository).findById(id);
            verify(difficultyLevelRepository, never()).existsByNameIgnoreCaseAndIdNot(any(), any());
            verify(difficultyLevelRepository, never()).save(any());
            verify(difficultyLevelMapper, never()).toResponse(any());
        }

        @Test
        @DisplayName("Should throw DuplicateException when updated name already exists")
        void shouldThrowDuplicateExceptionWhenUpdatedNameAlreadyExists() {
            String id = "diff-4";
            DifficultyLevelRequest request = createRequest("Senior", 3, "Advanced level");

            DifficultyLevel existing = createDifficultyLevel(id, "Junior", 1, "Entry level");

            when(difficultyLevelRepository.findById(id)).thenReturn(java.util.Optional.of(existing));
            when(difficultyLevelRepository.existsByNameIgnoreCaseAndIdNot("Senior", id)).thenReturn(true);

            assertThatThrownBy(() -> difficultyLevelService.update(id, request))
                    .isInstanceOf(DuplicateException.class)
                    .hasMessage("Độ khó đã tồn tại");

            verify(difficultyLevelRepository).findById(id);
            verify(difficultyLevelRepository).existsByNameIgnoreCaseAndIdNot("Senior", id);
            verify(difficultyLevelRepository, never()).save(any());
            verify(difficultyLevelMapper, never()).toResponse(any());
        }

        @Test
        @DisplayName("Should throw DuplicateException for case-insensitive duplicate update")
        void shouldThrowDuplicateExceptionForCaseInsensitiveDuplicateUpdate() {
            String id = "diff-5";
            DifficultyLevelRequest request = createRequest(" senior ", 3, "Advanced level");

            DifficultyLevel existing = createDifficultyLevel(id, "Junior", 1, "Entry level");

            when(difficultyLevelRepository.findById(id)).thenReturn(java.util.Optional.of(existing));
            when(difficultyLevelRepository.existsByNameIgnoreCaseAndIdNot("senior", id)).thenReturn(true);

            assertThatThrownBy(() -> difficultyLevelService.update(id, request))
                    .isInstanceOf(DuplicateException.class)
                    .hasMessage("Độ khó đã tồn tại");

            verify(difficultyLevelRepository).findById(id);
            verify(difficultyLevelRepository).existsByNameIgnoreCaseAndIdNot("senior", id);
            verify(difficultyLevelRepository, never()).save(any());
            verify(difficultyLevelMapper, never()).toResponse(any());
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("Should delete difficulty level successfully when not in use")
        void shouldDeleteDifficultyLevelSuccessfullyWhenNotInUse() {
            String id = "diff-1";
            DifficultyLevel existing = createDifficultyLevel(id, "Senior", 3, "Advanced level");

            when(difficultyLevelRepository.findById(id)).thenReturn(java.util.Optional.of(existing));
            when(questionRepository.existsByDifficultyId(id)).thenReturn(false);
            when(studyArticleRepository.existsByDifficultyId(id)).thenReturn(false);

            difficultyLevelService.delete(id);

            verify(difficultyLevelRepository).findById(id);
            verify(questionRepository).existsByDifficultyId(id);
            verify(studyArticleRepository).existsByDifficultyId(id);
            verify(difficultyLevelRepository).delete(existing);
        }

        @Test
        @DisplayName("Should throw NotFoundException when difficulty level does not exist")
        void shouldThrowNotFoundExceptionWhenDeletingMissingDifficultyLevel() {
            String id = "missing-id";
            when(difficultyLevelRepository.findById(id)).thenReturn(java.util.Optional.empty());

            assertThatThrownBy(() -> difficultyLevelService.delete(id))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage(ErrorCode.DIF_001.getMessage());

            verify(difficultyLevelRepository).findById(id);
            verify(questionRepository, never()).existsByDifficultyId(anyString());
            verify(studyArticleRepository, never()).existsByDifficultyId(anyString());
            verify(difficultyLevelRepository, never()).delete(any(DifficultyLevel.class));
        }

        @Test
        @DisplayName("Should throw BadRequestException when difficulty level is used by questions")
        void shouldThrowBadRequestExceptionWhenDifficultyLevelUsedByQuestions() {
            String id = "diff-2";
            DifficultyLevel existing = createDifficultyLevel(id, "Senior", 3, "Advanced level");

            when(difficultyLevelRepository.findById(id)).thenReturn(java.util.Optional.of(existing));
            when(questionRepository.existsByDifficultyId(id)).thenReturn(true);

            assertThatThrownBy(() -> difficultyLevelService.delete(id))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage(ErrorCode.DIF_003.getMessage());

            verify(difficultyLevelRepository).findById(id);
            verify(questionRepository).existsByDifficultyId(id);
            verify(studyArticleRepository, never()).existsByDifficultyId(anyString());
            verify(difficultyLevelRepository, never()).delete(any(DifficultyLevel.class));
        }

        @Test
        @DisplayName("Should throw BadRequestException when difficulty level is used by study articles")
        void shouldThrowBadRequestExceptionWhenDifficultyLevelUsedByStudyArticles() {
            String id = "diff-3";
            DifficultyLevel existing = createDifficultyLevel(id, "Senior", 3, "Advanced level");

            when(difficultyLevelRepository.findById(id)).thenReturn(java.util.Optional.of(existing));
            when(questionRepository.existsByDifficultyId(id)).thenReturn(false);
            when(studyArticleRepository.existsByDifficultyId(id)).thenReturn(true);

            assertThatThrownBy(() -> difficultyLevelService.delete(id))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage(ErrorCode.DIF_003.getMessage());

            verify(difficultyLevelRepository).findById(id);
            verify(questionRepository).existsByDifficultyId(id);
            verify(studyArticleRepository).existsByDifficultyId(id);
            verify(difficultyLevelRepository, never()).delete(any(DifficultyLevel.class));
        }
    }

    private DifficultyLevel createDifficultyLevel(String id, String name, Integer weight, String description) {
        DifficultyLevel level = new DifficultyLevel();
        level.setId(id);
        level.setName(name);
        level.setWeight(weight);
        level.setDescription(description);
        return level;
    }

    private DifficultyLevelRequest createRequest(String name, Integer weight, String description) {
        return new DifficultyLevelRequest(name, weight, description);
    }

    private DifficultyLevelResponse createResponse(String id, String name, Integer weight, String description) {
        return DifficultyLevelResponse.builder()
                .id(id)
                .name(name)
                .weight(weight)
                .description(description)
                .build();
    }
}

