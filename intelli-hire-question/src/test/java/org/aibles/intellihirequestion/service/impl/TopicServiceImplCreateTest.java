package org.aibles.intellihirequestion.service.impl;

import org.aibles.intellihirequestion.dto.TopicRequest;
import org.aibles.intellihirequestion.dto.TopicResponse;
import org.aibles.intellihirequestion.entity.Topic;
import org.aibles.intellihirequestion.exception.DuplicateException;
import org.aibles.intellihirequestion.exception.NotFoundException;
import org.aibles.intellihirequestion.mapper.TopicMapper;
import org.aibles.intellihirequestion.repository.TopicRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TopicServiceImplCreateTest {

    @Mock
    private TopicRepository topicRepository;

    private final TopicMapper topicMapper = new TopicMapper();

    private TopicServiceImpl topicService;

    @BeforeEach
    void setUp() {
        topicService = new TopicServiceImpl(topicRepository, null, topicMapper);
    }

    @Nested
    @DisplayName("create() - Success cases")
    class CreateSuccessTests {

        @Test
        @DisplayName("Should create root topic successfully")
        void shouldCreateRootTopicSuccessfully() {
            // Given
            TopicRequest request = TopicRequest.builder()
                    .name("Java")
                    .description("Java programming")
                    .parentId(null)
                    .order(1)
                    .isActive(true)
                    .build();

            Topic savedTopic = createTopic("uuid-1", "Java", "Java programming", null, 1);

            when(topicRepository.existsByNameIgnoreCaseAndParentIdIsNull("Java"))
                    .thenReturn(false);
            when(topicRepository.save(any(Topic.class))).thenReturn(savedTopic);

            // When
            TopicResponse result = topicService.create(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("uuid-1");
            assertThat(result.getName()).isEqualTo("Java");
            assertThat(result.getDescription()).isEqualTo("Java programming");
            assertThat(result.getParentId()).isNull();
            assertThat(result.getOrder()).isEqualTo(1);

            verify(topicRepository).existsByNameIgnoreCaseAndParentIdIsNull("Java");
            verify(topicRepository).save(any(Topic.class));
            verify(topicRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Should create child topic successfully")
        void shouldCreateChildTopicSuccessfully() {
            // Given
            String parentId = "parent-uuid";
            TopicRequest request = TopicRequest.builder()
                    .name("Spring Boot")
                    .description("Spring Boot framework")
                    .parentId(parentId)
                    .order(1)
                    .isActive(true)
                    .build();

            Topic parentTopic = createTopic(parentId, "Java", null, null, 1);
            Topic savedTopic = createTopic("uuid-2", "Spring Boot", "Spring Boot framework", parentId, 1);

            when(topicRepository.findById(parentId)).thenReturn(Optional.of(parentTopic));
            when(topicRepository.existsByNameIgnoreCaseAndParentId("Spring Boot", parentId))
                    .thenReturn(false);
            when(topicRepository.save(any(Topic.class))).thenReturn(savedTopic);

            // When
            TopicResponse result = topicService.create(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("uuid-2");
            assertThat(result.getName()).isEqualTo("Spring Boot");
            assertThat(result.getParentId()).isEqualTo(parentId);

            verify(topicRepository).findById(parentId);
            verify(topicRepository).existsByNameIgnoreCaseAndParentId("Spring Boot", parentId);
            verify(topicRepository).save(any(Topic.class));
        }

        @Test
        @DisplayName("Should use default values when order and isActive are null")
        void shouldUseDefaultValues() {
            // Given
            TopicRequest request = TopicRequest.builder()
                    .name("Python")
                    .description("Python programming")
                    .parentId(null)
                    .order(null)
                    .isActive(null)
                    .build();

            Topic savedTopic = createTopic("uuid-3", "Python", "Python programming", null, 0);

            when(topicRepository.existsByNameIgnoreCaseAndParentIdIsNull("Python"))
                    .thenReturn(false);
            when(topicRepository.save(any(Topic.class))).thenReturn(savedTopic);

            // When
            TopicResponse result = topicService.create(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Python");

            verify(topicRepository).save(any(Topic.class));
        }
    }

    @Nested
    @DisplayName("create() - Parent not found")
    class CreateParentNotFoundTests {

        @Test
        @DisplayName("Should throw NotFoundException when parent not found")
        void shouldThrowNotFoundException_whenParentNotFound() {
            // Given
            String parentId = "non-existent-uuid";
            TopicRequest request = TopicRequest.builder()
                    .name("Spring Boot")
                    .parentId(parentId)
                    .build();

            when(topicRepository.findById(parentId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> topicService.create(request))
                    .isInstanceOf(NotFoundException.class);

            verify(topicRepository).findById(parentId);
            verify(topicRepository, never()).save(any(Topic.class));
        }

        @Test
        @DisplayName("Should throw NotFoundException when parent is inactive")
        void shouldThrowNotFoundException_whenParentIsInactive() {
            // Given
            String parentId = "inactive-parent-uuid";
            TopicRequest request = TopicRequest.builder()
                    .name("Spring Boot")
                    .parentId(parentId)
                    .build();

            Topic inactiveParent = createTopic(parentId, "Java", null, null, 1);
            inactiveParent.setIsActive(false);

            when(topicRepository.findById(parentId)).thenReturn(Optional.of(inactiveParent));

            // When & Then
            assertThatThrownBy(() -> topicService.create(request))
                    .isInstanceOf(NotFoundException.class);

            verify(topicRepository).findById(parentId);
            verify(topicRepository, never()).save(any(Topic.class));
        }
    }

    @Nested
    @DisplayName("create() - Name duplicate")
    class CreateNameDuplicateTests {

        @Test
        @DisplayName("Should throw DuplicateException when root topic name exists")
        void shouldThrowDuplicateException_whenRootTopicNameExists() {
            // Given
            TopicRequest request = TopicRequest.builder()
                    .name("Java")
                    .parentId(null)
                    .build();

            when(topicRepository.existsByNameIgnoreCaseAndParentIdIsNull("Java"))
                    .thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> topicService.create(request))
                    .isInstanceOf(DuplicateException.class);

            verify(topicRepository).existsByNameIgnoreCaseAndParentIdIsNull("Java");
            verify(topicRepository, never()).save(any(Topic.class));
        }

        @Test
        @DisplayName("Should throw DuplicateException when child topic name exists under same parent")
        void shouldThrowDuplicateException_whenChildTopicNameExistsUnderSameParent() {
            // Given
            String parentId = "parent-uuid";
            TopicRequest request = TopicRequest.builder()
                    .name("Spring Boot")
                    .parentId(parentId)
                    .build();

            Topic parentTopic = createTopic(parentId, "Java", null, null, 1);

            when(topicRepository.findById(parentId)).thenReturn(Optional.of(parentTopic));
            when(topicRepository.existsByNameIgnoreCaseAndParentId("Spring Boot", parentId))
                    .thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> topicService.create(request))
                    .isInstanceOf(DuplicateException.class);

            verify(topicRepository).findById(parentId);
            verify(topicRepository).existsByNameIgnoreCaseAndParentId("Spring Boot", parentId);
            verify(topicRepository, never()).save(any(Topic.class));
        }

        @Test
        @DisplayName("Should allow same name under different parents")
        void shouldAllowSameNameUnderDifferentParents() {
            // Given
            String parentId = "parent-uuid-2";
            TopicRequest request = TopicRequest.builder()
                    .name("Basics")
                    .parentId(parentId)
                    .order(1)
                    .build();

            Topic parentTopic = createTopic(parentId, "Python", null, null, 1);
            Topic savedTopic = createTopic("uuid-new", "Basics", null, parentId, 1);

            when(topicRepository.findById(parentId)).thenReturn(Optional.of(parentTopic));
            when(topicRepository.existsByNameIgnoreCaseAndParentId("Basics", parentId))
                    .thenReturn(false);
            when(topicRepository.save(any(Topic.class))).thenReturn(savedTopic);

            // When
            TopicResponse result = topicService.create(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Basics");

            verify(topicRepository).save(any(Topic.class));
        }
    }

    private Topic createTopic(String id, String name, String description, String parentId, Integer order) {
        Topic topic = new Topic();
        topic.setId(id);
        topic.setName(name);
        topic.setDescription(description);
        topic.setParentId(parentId);
        topic.setOrder(order);
        topic.setIsActive(true);
        return topic;
    }
}
