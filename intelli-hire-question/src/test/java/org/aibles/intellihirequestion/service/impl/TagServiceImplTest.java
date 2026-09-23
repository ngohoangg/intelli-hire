package org.aibles.intellihirequestion.service.impl;

import org.aibles.intellihirequestion.dto.TagRequest;
import org.aibles.intellihirequestion.dto.TagResponse;
import org.aibles.intellihirequestion.entity.Tag;
import org.aibles.intellihirequestion.exception.BadRequestException;
import org.aibles.intellihirequestion.exception.DuplicateException;
import org.aibles.intellihirequestion.exception.NotFoundException;
import org.aibles.intellihirequestion.mapper.TagMapper;
import org.aibles.intellihirequestion.repository.QuestionTagRepository;
import org.aibles.intellihirequestion.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceImplTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private QuestionTagRepository questionTagRepository;

    private final TagMapper tagMapper = new TagMapper();

    private TagServiceImpl tagService;

    @BeforeEach
    void setUp() {
        tagService = new TagServiceImpl(tagRepository, tagMapper, questionTagRepository);
    }

    private Tag createTag(String id, String name) {
        Tag tag = new Tag();
        tag.setId(id);
        tag.setName(name);
        return tag;
    }

    // ==================== getAll() Tests ====================

    @Nested
    @DisplayName("getAll() tests")
    class GetAllTests {

        @Test
        @DisplayName("Should return list of TagResponse")
        void shouldReturnTagResponseList() {
            // Given
            Tag algorithm = createTag("1", "algorithm");
            Tag database = createTag("2", "database");

            when(tagRepository.findAllByOrderByNameAsc())
                    .thenReturn(Arrays.asList(algorithm, database));

            // When
            List<TagResponse> result = tagService.getAll();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("algorithm");
            assertThat(result.get(1).getName()).isEqualTo("database");

            verify(tagRepository, times(1)).findAllByOrderByNameAsc();
        }

        @Test
        @DisplayName("Should return empty list when no tags exist")
        void shouldReturnEmptyListWhenNoTags() {
            // Given
            when(tagRepository.findAllByOrderByNameAsc())
                    .thenReturn(Collections.emptyList());

            // When
            List<TagResponse> result = tagService.getAll();

            // Then
            assertThat(result).isEmpty();

            verify(tagRepository, times(1)).findAllByOrderByNameAsc();
        }

        @Test
        @DisplayName("Should return tags ordered by name ASC")
        void shouldReturnTagsOrderedByNameAsc() {
            // Given
            Tag algorithm = createTag("1", "algorithm");
            Tag database = createTag("2", "database");
            Tag designPattern = createTag("3", "design-pattern");

            when(tagRepository.findAllByOrderByNameAsc())
                    .thenReturn(Arrays.asList(algorithm, database, designPattern));

            // When
            List<TagResponse> result = tagService.getAll();

            // Then
            assertThat(result).extracting(TagResponse::getName)
                    .containsExactly("algorithm", "database", "design-pattern");

            verify(tagRepository).findAllByOrderByNameAsc();
        }

        @Test
        @DisplayName("Should return TagResponse with correct id and name")
        void shouldReturnTagResponseWithCorrectIdAndName() {
            // Given
            Tag tag = createTag("550e8400-e29b-41d4-a716-446655440001", "java");

            when(tagRepository.findAllByOrderByNameAsc())
                    .thenReturn(Collections.singletonList(tag));

            // When
            List<TagResponse> result = tagService.getAll();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo("550e8400-e29b-41d4-a716-446655440001");
            assertThat(result.get(0).getName()).isEqualTo("java");

            verify(tagRepository).findAllByOrderByNameAsc();
        }
    }

    // ==================== create() Tests ====================

    @Nested
    @DisplayName("create() - Success cases")
    class CreateSuccessTests {

        @Test
        @DisplayName("Should create tag successfully")
        void shouldCreateTagSuccessfully() {
            // Given
            TagRequest request = TagRequest.builder()
                    .name("design-pattern")
                    .build();

            Tag savedTag = createTag("uuid-1", "design-pattern");

            when(tagRepository.existsByName("design-pattern")).thenReturn(false);
            when(tagRepository.save(any(Tag.class))).thenReturn(savedTag);

            // When
            TagResponse result = tagService.create(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("uuid-1");
            assertThat(result.getName()).isEqualTo("design-pattern");

            verify(tagRepository).existsByName("design-pattern");
            verify(tagRepository).save(any(Tag.class));
        }

        @Test
        @DisplayName("Should normalize name to lowercase")
        void shouldNormalizeNameToLowercase() {
            // Given
            TagRequest request = TagRequest.builder()
                    .name("Design-Pattern")
                    .build();

            Tag savedTag = createTag("uuid-2", "design-pattern");

            when(tagRepository.existsByName("design-pattern")).thenReturn(false);
            when(tagRepository.save(any(Tag.class))).thenReturn(savedTag);

            // When
            TagResponse result = tagService.create(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("design-pattern");

            verify(tagRepository).existsByName("design-pattern");
            verify(tagRepository).save(any(Tag.class));
        }

        @Test
        @DisplayName("Should trim whitespace from name")
        void shouldTrimWhitespaceFromName() {
            // Given
            TagRequest request = TagRequest.builder()
                    .name("  java-core  ")
                    .build();

            Tag savedTag = createTag("uuid-3", "java-core");

            when(tagRepository.existsByName("java-core")).thenReturn(false);
            when(tagRepository.save(any(Tag.class))).thenReturn(savedTag);

            // When
            TagResponse result = tagService.create(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("java-core");

            verify(tagRepository).existsByName("java-core");
            verify(tagRepository).save(any(Tag.class));
        }
    }

    @Nested
    @DisplayName("create() - Name duplicate")
    class CreateNameDuplicateTests {

        @Test
        @DisplayName("Should throw DuplicateException when tag name already exists")
        void shouldThrowDuplicateException_whenTagNameExists() {
            // Given
            TagRequest request = TagRequest.builder()
                    .name("java")
                    .build();

            when(tagRepository.existsByName("java")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> tagService.create(request))
                    .isInstanceOf(DuplicateException.class);

            verify(tagRepository).existsByName("java");
            verify(tagRepository, never()).save(any(Tag.class));
        }

        @Test
        @DisplayName("Should throw DuplicateException for case-insensitive duplicate")
        void shouldThrowDuplicateException_forCaseInsensitiveDuplicate() {
            // Given
            TagRequest request = TagRequest.builder()
                    .name("JAVA")
                    .build();

            when(tagRepository.existsByName("java")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> tagService.create(request))
                    .isInstanceOf(DuplicateException.class);

            verify(tagRepository).existsByName("java");
            verify(tagRepository, never()).save(any(Tag.class));
        }
    }

    // ==================== delete() Tests ====================

    @Nested
    @DisplayName("delete() - Success cases")
    class DeleteSuccessTests {

        @Test
        @DisplayName("Should delete tag successfully when tag exists and not in use")
        void shouldDeleteTagSuccessfully() {
            // Given
            String tagId = "uuid-1";
            Tag tag = createTag(tagId, "java");

            when(tagRepository.findById(tagId)).thenReturn(Optional.of(tag));
            when(questionTagRepository.existsByTagId(tagId)).thenReturn(false);

            // When
            tagService.delete(tagId);

            // Then
            verify(tagRepository).findById(tagId);
            verify(questionTagRepository).existsByTagId(tagId);
            verify(tagRepository).delete(tag);
        }
    }

    @Nested
    @DisplayName("delete() - Tag not found")
    class DeleteNotFoundTests {

        @Test
        @DisplayName("Should throw NotFoundException when tag does not exist")
        void shouldThrowNotFoundException_whenTagNotFound() {
            // Given
            String tagId = "non-existent-uuid";

            when(tagRepository.findById(tagId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> tagService.delete(tagId))
                    .isInstanceOf(NotFoundException.class);

            verify(tagRepository).findById(tagId);
            verify(questionTagRepository, never()).existsByTagId(anyString());
            verify(tagRepository, never()).delete(any(Tag.class));
        }
    }

    @Nested
    @DisplayName("delete() - Tag in use")
    class DeleteTagInUseTests {

        @Test
        @DisplayName("Should throw BadRequestException when tag is in use by questions")
        void shouldThrowBadRequestException_whenTagInUse() {
            // Given
            String tagId = "uuid-1";
            Tag tag = createTag(tagId, "java");

            when(tagRepository.findById(tagId)).thenReturn(Optional.of(tag));
            when(questionTagRepository.existsByTagId(tagId)).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> tagService.delete(tagId))
                    .isInstanceOf(BadRequestException.class);

            verify(tagRepository).findById(tagId);
            verify(questionTagRepository).existsByTagId(tagId);
            verify(tagRepository, never()).delete(any(Tag.class));
        }
    }

    @Nested
    @DisplayName("update() - Success cases")
    class UpdateSuccessTests {

        @Test
        @DisplayName("Should update tag successfully when name changed and not duplicated")
        void shouldUpdateTagSuccessfully() {
            // Given
            String id = "uuid-1";
            TagRequest request = TagRequest.builder()
                    .name("design-pattern")
                    .build();

            Tag existing = createTag(id, "java");
            Tag saved = createTag(id, "design-pattern");

            when(tagRepository.findById(id)).thenReturn(Optional.of(existing));
            when(tagRepository.existsByNameAndIdNot("design-pattern", id)).thenReturn(false);
            when(tagRepository.save(any(Tag.class))).thenReturn(saved);

            // When
            TagResponse result = tagService.update(id, request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(id);
            assertThat(result.getName()).isEqualTo("design-pattern");

            verify(tagRepository).findById(id);
            verify(tagRepository).existsByNameAndIdNot("design-pattern", id);
            verify(tagRepository).save(any(Tag.class));
        }

        @Test
        @DisplayName("Should normalize name to lowercase before update")
        void shouldNormalizeNameToLowercaseBeforeUpdate() {
            // Given
            String id = "uuid-2";
            TagRequest request = TagRequest.builder()
                    .name("Design-Pattern")
                    .build();

            Tag existing = createTag(id, "java");
            Tag saved = createTag(id, "design-pattern");

            when(tagRepository.findById(id)).thenReturn(Optional.of(existing));
            when(tagRepository.existsByNameAndIdNot("design-pattern", id)).thenReturn(false);
            when(tagRepository.save(any(Tag.class))).thenReturn(saved);

            // When
            TagResponse result = tagService.update(id, request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("design-pattern");

            verify(tagRepository).findById(id);
            verify(tagRepository).existsByNameAndIdNot("design-pattern", id);
            verify(tagRepository).save(any(Tag.class));
        }

        @Test
        @DisplayName("Should trim whitespace from name before update")
        void shouldTrimWhitespaceFromNameBeforeUpdate() {
            // Given
            String id = "uuid-3";
            TagRequest request = TagRequest.builder()
                    .name("  java-core  ")
                    .build();

            Tag existing = createTag(id, "java");
            Tag saved = createTag(id, "java-core");

            when(tagRepository.findById(id)).thenReturn(Optional.of(existing));
            when(tagRepository.existsByNameAndIdNot("java-core", id)).thenReturn(false);
            when(tagRepository.save(any(Tag.class))).thenReturn(saved);

            // When
            TagResponse result = tagService.update(id, request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("java-core");

            verify(tagRepository).findById(id);
            verify(tagRepository).existsByNameAndIdNot("java-core", id);
            verify(tagRepository).save(any(Tag.class));
        }

        @Test
        @DisplayName("Should not check duplicate when name is unchanged (case-insensitive)")
        void shouldNotCheckDuplicateWhenNameUnchanged() {
            // Given
            String id = "uuid-4";
            TagRequest request = TagRequest.builder()
                    .name("JAVA")
                    .build();

            Tag existing = createTag(id, "java");
            Tag saved = createTag(id, "java");

            when(tagRepository.findById(id)).thenReturn(Optional.of(existing));
            when(tagRepository.save(any(Tag.class))).thenReturn(saved);

            // When
            TagResponse result = tagService.update(id, request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("java");

            verify(tagRepository).findById(id);
            verify(tagRepository, never()).existsByNameAndIdNot(any(), any());
            verify(tagRepository).save(any(Tag.class));
        }
    }

    @Nested
    @DisplayName("update() - Tag not found")
    class UpdateNotFoundTests {

        @Test
        @DisplayName("Should throw NotFoundException when tag id does not exist")
        void shouldThrowNotFoundException_whenTagNotFound() {
            // Given
            String id = "missing-id";
            TagRequest request = TagRequest.builder()
                    .name("java")
                    .build();

            when(tagRepository.findById(id)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> tagService.update(id, request))
                    .isInstanceOf(NotFoundException.class);

            verify(tagRepository).findById(id);
            verify(tagRepository, never()).save(any(Tag.class));
            verify(tagRepository, never()).existsByNameAndIdNot(any(), any());
        }
    }

    @Nested
    @DisplayName("update() - Name duplicate")
    class UpdateDuplicateTests {

        @Test
        @DisplayName("Should throw DuplicateException when new name already exists (exclude current tag)")
        void shouldThrowDuplicateException_whenNameExists() {
            // Given
            String id = "uuid-5";
            TagRequest request = TagRequest.builder()
                    .name("spring")
                    .build();

            Tag existing = createTag(id, "java");

            when(tagRepository.findById(id)).thenReturn(Optional.of(existing));
            when(tagRepository.existsByNameAndIdNot("spring", id)).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> tagService.update(id, request))
                    .isInstanceOf(DuplicateException.class);

            verify(tagRepository).findById(id);
            verify(tagRepository).existsByNameAndIdNot("spring", id);
            verify(tagRepository, never()).save(any(Tag.class));
        }

        @Test
        @DisplayName("Should throw DuplicateException for case-insensitive duplicate update")
        void shouldThrowDuplicateException_forCaseInsensitiveDuplicate() {
            // Given
            String id = "uuid-6";
            TagRequest request = TagRequest.builder()
                    .name("SPRING")
                    .build();

            Tag existing = createTag(id, "java");

            when(tagRepository.findById(id)).thenReturn(Optional.of(existing));
            when(tagRepository.existsByNameAndIdNot("spring", id)).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> tagService.update(id, request))
                    .isInstanceOf(DuplicateException.class);

            verify(tagRepository).findById(id);
            verify(tagRepository).existsByNameAndIdNot("spring", id);
            verify(tagRepository, never()).save(any(Tag.class));
        }
    }
}
