package org.aibles.intellihirequestion.service.impl;

import org.aibles.intellihirequestion.dto.TopicRequest;
import org.aibles.intellihirequestion.dto.TopicResponse;
import org.aibles.intellihirequestion.dto.TopicTreeResponse;
import org.aibles.intellihirequestion.entity.Topic;
import org.aibles.intellihirequestion.exception.BadRequestException;
import org.aibles.intellihirequestion.exception.DuplicateException;
import org.aibles.intellihirequestion.exception.NotFoundException;
import org.aibles.intellihirequestion.exception.ErrorCode;
import org.aibles.intellihirequestion.mapper.TopicMapper;
import org.aibles.intellihirequestion.repository.QuestionRepository;
import org.aibles.intellihirequestion.repository.TopicRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TopicServiceImplTest {

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private QuestionRepository questionRepository;

    private final TopicMapper topicMapper = new TopicMapper();

    private TopicServiceImpl topicService;

    @BeforeEach
    void setUp() {
        topicService = new TopicServiceImpl(topicRepository, questionRepository, topicMapper);
    }

    @Nested
    @DisplayName("getTree()")
    class GetTreeTests {

        @Test
        @DisplayName("Should build tree from DB")
        void shouldBuildTreeFromDB() {
            // Given
            Topic java = createTopic("1", "Java", null, 1);
            Topic javaCore = createTopic("2", "Java Core", "1", 1);
            Topic collections = createTopic("3", "Collections", "2", 1);

            when(topicRepository.findByIsActiveTrueOrderByOrderAscNameAsc())
                    .thenReturn(Arrays.asList(java, javaCore, collections));

            // When
            List<TopicTreeResponse> result = topicService.getTree();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Java");
            assertThat(result.get(0).getChildren()).hasSize(1);
            assertThat(result.get(0).getChildren().get(0).getName()).isEqualTo("Java Core");
            assertThat(result.get(0).getChildren().get(0).getChildren()).hasSize(1);
            assertThat(result.get(0).getChildren().get(0).getChildren().get(0).getName()).isEqualTo("Collections");

            verify(topicRepository).findByIsActiveTrueOrderByOrderAscNameAsc();
        }

        @Test
        @DisplayName("Should return empty list when no topics")
        void shouldReturnEmptyList_whenNoTopics() {
            // Given
            when(topicRepository.findByIsActiveTrueOrderByOrderAscNameAsc())
                    .thenReturn(Collections.emptyList());

            // When
            List<TopicTreeResponse> result = topicService.getTree();

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should sort by order ASC then name ASC")
        void shouldSortByOrderThenName() {
            // Given
            Topic java = createTopic("1", "Java", null, 2);
            Topic python = createTopic("2", "Python", null, 1);
            Topic csharp = createTopic("3", "C#", null, 1);

            when(topicRepository.findByIsActiveTrueOrderByOrderAscNameAsc())
                    .thenReturn(Arrays.asList(java, python, csharp));

            // When
            List<TopicTreeResponse> result = topicService.getTree();

            // Then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getName()).isEqualTo("C#");      // order=1, name="C#"
            assertThat(result.get(1).getName()).isEqualTo("Python");  // order=1, name="Python"
            assertThat(result.get(2).getName()).isEqualTo("Java");    // order=2
        }

        @Test
        @DisplayName("Should handle all root topics (flat list with empty children)")
        void shouldHandleAllRootTopics() {
            // Given
            Topic java = createTopic("1", "Java", null, 1);
            Topic python = createTopic("2", "Python", null, 2);

            when(topicRepository.findByIsActiveTrueOrderByOrderAscNameAsc())
                    .thenReturn(Arrays.asList(java, python));

            // When
            List<TopicTreeResponse> result = topicService.getTree();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getChildren()).isEmpty();
            assertThat(result.get(1).getChildren()).isEmpty();
        }

        @Test
        @DisplayName("Should handle deep nesting (3+ levels)")
        void shouldHandleDeepNesting() {
            // Given
            Topic level1 = createTopic("1", "Level 1", null, 1);
            Topic level2 = createTopic("2", "Level 2", "1", 1);
            Topic level3 = createTopic("3", "Level 3", "2", 1);
            Topic level4 = createTopic("4", "Level 4", "3", 1);

            when(topicRepository.findByIsActiveTrueOrderByOrderAscNameAsc())
                    .thenReturn(Arrays.asList(level1, level2, level3, level4));

            // When
            List<TopicTreeResponse> result = topicService.getTree();

            // Then
            assertThat(result).hasSize(1);

            TopicTreeResponse l1 = result.get(0);
            assertThat(l1.getName()).isEqualTo("Level 1");
            assertThat(l1.getChildren()).hasSize(1);

            TopicTreeResponse l2 = l1.getChildren().get(0);
            assertThat(l2.getName()).isEqualTo("Level 2");
            assertThat(l2.getChildren()).hasSize(1);

            TopicTreeResponse l3 = l2.getChildren().get(0);
            assertThat(l3.getName()).isEqualTo("Level 3");
            assertThat(l3.getChildren()).hasSize(1);

            TopicTreeResponse l4 = l3.getChildren().get(0);
            assertThat(l4.getName()).isEqualTo("Level 4");
            assertThat(l4.getChildren()).isEmpty();
        }

        @Test
        @DisplayName("Should handle multiple root topics with children")
        void shouldHandleMultipleRootsWithChildren() {
            // Given
            Topic java = createTopic("1", "Java", null, 1);
            Topic javaCore = createTopic("2", "Java Core", "1", 1);
            Topic python = createTopic("3", "Python", null, 2);
            Topic pythonBasics = createTopic("4", "Python Basics", "3", 1);

            when(topicRepository.findByIsActiveTrueOrderByOrderAscNameAsc())
                    .thenReturn(Arrays.asList(java, javaCore, python, pythonBasics));

            // When
            List<TopicTreeResponse> result = topicService.getTree();

            // Then
            assertThat(result).hasSize(2);

            assertThat(result.get(0).getName()).isEqualTo("Java");
            assertThat(result.get(0).getChildren()).hasSize(1);
            assertThat(result.get(0).getChildren().get(0).getName()).isEqualTo("Java Core");

            assertThat(result.get(1).getName()).isEqualTo("Python");
            assertThat(result.get(1).getChildren()).hasSize(1);
            assertThat(result.get(1).getChildren().get(0).getName()).isEqualTo("Python Basics");
        }

        @Test
        @DisplayName("Should sort children within each parent")
        void shouldSortChildrenWithinParent() {
            // Given
            Topic java = createTopic("1", "Java", null, 1);
            Topic streams = createTopic("2", "Streams", "1", 3);
            Topic collections = createTopic("3", "Collections", "1", 1);
            Topic concurrency = createTopic("4", "Concurrency", "1", 2);

            when(topicRepository.findByIsActiveTrueOrderByOrderAscNameAsc())
                    .thenReturn(Arrays.asList(java, streams, collections, concurrency));

            // When
            List<TopicTreeResponse> result = topicService.getTree();

            // Then
            assertThat(result).hasSize(1);
            List<TopicTreeResponse> children = result.get(0).getChildren();
            assertThat(children).hasSize(3);
            assertThat(children.get(0).getName()).isEqualTo("Collections");  // order=1
            assertThat(children.get(1).getName()).isEqualTo("Concurrency");  // order=2
            assertThat(children.get(2).getName()).isEqualTo("Streams");      // order=3
        }

        @Test
        @DisplayName("Should handle null order values")
        void shouldHandleNullOrderValues() {
            // Given
            Topic java = createTopic("1", "Java", null, null);
            Topic python = createTopic("2", "Python", null, 1);

            when(topicRepository.findByIsActiveTrueOrderByOrderAscNameAsc())
                    .thenReturn(Arrays.asList(java, python));

            // When
            List<TopicTreeResponse> result = topicService.getTree();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("Python");  // order=1
            assertThat(result.get(1).getName()).isEqualTo("Java");    // order=null (last)
        }
    }

    @Nested
    @DisplayName("update()")
    class UpdateTests {

        @Test
        @DisplayName("Should update topic successfully")
        void shouldUpdateTopicSuccessfully() {
            // Given
            String topicId = "topic-1";
            Topic existingTopic = createTopic(topicId, "Java", null, 1);

            TopicRequest request = TopicRequest.builder()
                    .name("Java Updated")
                    .description("Updated description")
                    .parentId(null)
                    .order(2)
                    .isActive(true)
                    .build();

            Topic savedTopic = createTopic(topicId, "Java Updated", null, 2);
            savedTopic.setDescription("Updated description");

            when(topicRepository.findById(topicId)).thenReturn(Optional.of(existingTopic));
            when(topicRepository.existsByNameAndParentIdIsNullAndIdNot("Java Updated", topicId))
                    .thenReturn(false);
            when(topicRepository.save(any(Topic.class))).thenReturn(savedTopic);

            // When
            TopicResponse result = topicService.update(topicId, request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Java Updated");
            assertThat(result.getDescription()).isEqualTo("Updated description");
            assertThat(result.getOrder()).isEqualTo(2);

            verify(topicRepository).findById(topicId);
            verify(topicRepository).existsByNameAndParentIdIsNullAndIdNot("Java Updated", topicId);
            verify(topicRepository).save(any(Topic.class));
        }

        @Test
        @DisplayName("Should update topic with new parent successfully")
        void shouldUpdateTopicWithNewParentSuccessfully() {
            // Given
            String topicId = "topic-1";
            String newParentId = "parent-1";

            Topic existingTopic = createTopic(topicId, "Java Core", null, 1);
            Topic parentTopic = createTopic(newParentId, "Java", null, 1);

            TopicRequest request = TopicRequest.builder()
                    .name("Java Core")
                    .description("Core concepts")
                    .parentId(newParentId)
                    .order(1)
                    .isActive(true)
                    .build();

            Topic savedTopic = createTopic(topicId, "Java Core", newParentId, 1);
            savedTopic.setDescription("Core concepts");

            when(topicRepository.findById(topicId)).thenReturn(Optional.of(existingTopic));
            when(topicRepository.findById(newParentId)).thenReturn(Optional.of(parentTopic));
            when(topicRepository.existsByNameAndParentIdAndIdNot("Java Core", newParentId, topicId))
                    .thenReturn(false);
            when(topicRepository.save(any(Topic.class))).thenReturn(savedTopic);

            // When
            TopicResponse result = topicService.update(topicId, request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getParentId()).isEqualTo(newParentId);

            // 3 findById calls: topic, parent validation, circular reference check
            verify(topicRepository, times(3)).findById(anyString());
            verify(topicRepository).save(any(Topic.class));
        }

        @Test
        @DisplayName("Should throw NotFoundException when topic not found")
        void shouldThrowNotFoundException_whenTopicNotFound() {
            // Given
            String topicId = "non-existent-id";
            TopicRequest request = TopicRequest.builder()
                    .name("Java Updated")
                    .build();

            when(topicRepository.findById(topicId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> topicService.update(topicId, request))
                    .isInstanceOf(NotFoundException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "TOP_001");

            verify(topicRepository).findById(topicId);
            verify(topicRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw NotFoundException when parent topic not found")
        void shouldThrowNotFoundException_whenParentNotFound() {
            // Given
            String topicId = "topic-1";
            String parentId = "non-existent-parent";

            Topic existingTopic = createTopic(topicId, "Java Core", null, 1);

            TopicRequest request = TopicRequest.builder()
                    .name("Java Core")
                    .parentId(parentId)
                    .build();

            when(topicRepository.findById(topicId)).thenReturn(Optional.of(existingTopic));
            when(topicRepository.findById(parentId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> topicService.update(topicId, request))
                    .isInstanceOf(NotFoundException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "TOP_001");

            verify(topicRepository).findById(topicId);
            verify(topicRepository).findById(parentId);
            verify(topicRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw NotFoundException when parent topic is inactive")
        void shouldThrowNotFoundException_whenParentInactive() {
            // Given
            String topicId = "topic-1";
            String parentId = "parent-1";

            Topic existingTopic = createTopic(topicId, "Java Core", null, 1);
            Topic inactiveParent = createTopic(parentId, "Java", null, 1);
            inactiveParent.setIsActive(false);

            TopicRequest request = TopicRequest.builder()
                    .name("Java Core")
                    .parentId(parentId)
                    .build();

            when(topicRepository.findById(topicId)).thenReturn(Optional.of(existingTopic));
            when(topicRepository.findById(parentId)).thenReturn(Optional.of(inactiveParent));

            // When & Then
            assertThatThrownBy(() -> topicService.update(topicId, request))
                    .isInstanceOf(NotFoundException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "TOP_001");

            verify(topicRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw BadRequestException when circular reference - self reference")
        void shouldThrowBadRequestException_whenSelfReference() {
            // Given
            String topicId = "topic-1";

            Topic existingTopic = createTopic(topicId, "Java", null, 1);

            TopicRequest request = TopicRequest.builder()
                    .name("Java")
                    .parentId(topicId)  // Setting parent to itself
                    .build();

            when(topicRepository.findById(topicId)).thenReturn(Optional.of(existingTopic));

            // When & Then
            assertThatThrownBy(() -> topicService.update(topicId, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "TOP_002");

            verify(topicRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw BadRequestException when circular reference - ancestor chain")
        void shouldThrowBadRequestException_whenCircularReferenceInAncestorChain() {
            // Given
            // Hierarchy: Java (id=1) -> Java Core (id=2) -> Collections (id=3)
            // Try to set Java's parent to Collections (its grandchild)
            String javaId = "topic-1";
            String javaCoreId = "topic-2";
            String collectionsId = "topic-3";

            Topic java = createTopic(javaId, "Java", null, 1);
            Topic javaCore = createTopic(javaCoreId, "Java Core", javaId, 1);
            Topic collections = createTopic(collectionsId, "Collections", javaCoreId, 1);

            TopicRequest request = TopicRequest.builder()
                    .name("Java")
                    .parentId(collectionsId)  // Try to set grandchild as parent
                    .build();

            when(topicRepository.findById(javaId)).thenReturn(Optional.of(java));
            when(topicRepository.findById(collectionsId)).thenReturn(Optional.of(collections));
            when(topicRepository.findById(javaCoreId)).thenReturn(Optional.of(javaCore));

            // When & Then
            assertThatThrownBy(() -> topicService.update(javaId, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "TOP_002");

            verify(topicRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw BadRequestException when circular reference - direct child as parent")
        void shouldThrowBadRequestException_whenDirectChildAsParent() {
            // Given
            // Hierarchy: Java (id=1) -> Java Core (id=2)
            // Try to set Java's parent to Java Core (its child)
            String javaId = "topic-1";
            String javaCoreId = "topic-2";

            Topic java = createTopic(javaId, "Java", null, 1);
            Topic javaCore = createTopic(javaCoreId, "Java Core", javaId, 1);

            TopicRequest request = TopicRequest.builder()
                    .name("Java")
                    .parentId(javaCoreId)  // Try to set child as parent
                    .build();

            when(topicRepository.findById(javaId)).thenReturn(Optional.of(java));
            when(topicRepository.findById(javaCoreId)).thenReturn(Optional.of(javaCore));

            // When & Then
            assertThatThrownBy(() -> topicService.update(javaId, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "TOP_002");

            verify(topicRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw DuplicateException when name already exists under same parent")
        void shouldThrowDuplicateException_whenNameDuplicateUnderSameParent() {
            // Given
            String topicId = "topic-1";

            Topic existingTopic = createTopic(topicId, "Java", null, 1);

            TopicRequest request = TopicRequest.builder()
                    .name("Python")  // Name already exists under same parent (null)
                    .parentId(null)
                    .build();

            when(topicRepository.findById(topicId)).thenReturn(Optional.of(existingTopic));
            when(topicRepository.existsByNameAndParentIdIsNullAndIdNot("Python", topicId))
                    .thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> topicService.update(topicId, request))
                    .isInstanceOf(DuplicateException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "TOP_003");

            verify(topicRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should allow same name under different parent")
        void shouldAllowSameNameUnderDifferentParent() {
            // Given
            String topicId = "topic-1";
            String parentId = "parent-1";

            Topic existingTopic = createTopic(topicId, "Basics", null, 1);
            Topic parentTopic = createTopic(parentId, "Python", null, 1);

            TopicRequest request = TopicRequest.builder()
                    .name("Basics")  // Same name but under different parent
                    .parentId(parentId)
                    .build();

            Topic savedTopic = createTopic(topicId, "Basics", parentId, 1);

            when(topicRepository.findById(topicId)).thenReturn(Optional.of(existingTopic));
            when(topicRepository.findById(parentId)).thenReturn(Optional.of(parentTopic));
            when(topicRepository.existsByNameAndParentIdAndIdNot("Basics", parentId, topicId))
                    .thenReturn(false);
            when(topicRepository.save(any(Topic.class))).thenReturn(savedTopic);

            // When
            TopicResponse result = topicService.update(topicId, request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Basics");
            assertThat(result.getParentId()).isEqualTo(parentId);

            verify(topicRepository).save(any(Topic.class));
        }

        @Test
        @DisplayName("Should not check circular reference when parentId unchanged")
        void shouldNotCheckCircularReference_whenParentIdUnchanged() {
            // Given
            String topicId = "topic-1";
            String parentId = "parent-1";

            Topic existingTopic = createTopic(topicId, "Java Core", parentId, 1);
            Topic parentTopic = createTopic(parentId, "Java", null, 1);

            TopicRequest request = TopicRequest.builder()
                    .name("Java Core Updated")
                    .parentId(parentId)  // Same parent as before
                    .order(2)
                    .build();

            Topic savedTopic = createTopic(topicId, "Java Core Updated", parentId, 2);

            when(topicRepository.findById(topicId)).thenReturn(Optional.of(existingTopic));
            when(topicRepository.findById(parentId)).thenReturn(Optional.of(parentTopic));
            when(topicRepository.existsByNameAndParentIdAndIdNot("Java Core Updated", parentId, topicId))
                    .thenReturn(false);
            when(topicRepository.save(any(Topic.class))).thenReturn(savedTopic);

            // When
            TopicResponse result = topicService.update(topicId, request);

            // Then
            assertThat(result).isNotNull();
            // Only 2 findById calls: one for topic, one for parent validation
            // No extra calls for circular reference check
            verify(topicRepository, times(2)).findById(anyString());
            verify(topicRepository).save(any(Topic.class));
        }
    }

    private Topic createTopic(String id, String name, String parentId, Integer order) {
        Topic topic = new Topic();
        topic.setId(id);
        topic.setName(name);
        topic.setParentId(parentId);
        topic.setOrder(order);
        topic.setIsActive(true);
        return topic;
    }

    /**
     * Delete Topic (Soft Delete)
     */
    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("Should soft delete topic successfully when all conditions met")
        void shouldDeleteSuccessfully() {
            String id = "topic-1";
            Topic topic = new Topic();
            topic.setId(id);
            topic.setIsActive(true);

            when(topicRepository.findById(id)).thenReturn(Optional.of(topic));
            when(topicRepository.existsByParentIdAndIsActiveTrue(id)).thenReturn(false);
            when(questionRepository.existsByTopicIdAndIsActiveTrue(id)).thenReturn(false);

            topicService.delete(id);

            assertThat(topic.getIsActive()).isFalse();

            InOrder inOrder = inOrder(topicRepository, questionRepository);
            inOrder.verify(topicRepository).findById(id);
            inOrder.verify(topicRepository).existsByParentIdAndIsActiveTrue(id);
            inOrder.verify(questionRepository).existsByTopicIdAndIsActiveTrue(id);
            inOrder.verify(topicRepository).save(topic);
        }

        @Test
        @DisplayName("Should throw NotFoundException when topic not found or already inactive")
        void shouldThrowNotFound_whenTopicNotFoundOrInactive() {
            // Case 1: ID không tồn tại
            when(topicRepository.findById("invalid")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> topicService.delete("invalid"))
                    .isInstanceOf(NotFoundException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TOP_001.getCode());

            // Case 2: ID tồn tại nhưng isActive = false
            Topic inactiveTopic = new Topic();
            inactiveTopic.setIsActive(false);
            when(topicRepository.findById("inactive")).thenReturn(Optional.of(inactiveTopic));

            assertThatThrownBy(() -> topicService.delete("inactive"))
                    .isInstanceOf(NotFoundException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TOP_001.getCode());
        }

        @Test
        @DisplayName("Should throw BadRequestException TOP_004 when has active children")
        void shouldThrowBadRequest_whenHasChildren() {
            Topic topic = new Topic();
            topic.setId("parent");
            topic.setIsActive(true);

            when(topicRepository.findById("parent")).thenReturn(Optional.of(topic));
            when(topicRepository.existsByParentIdAndIsActiveTrue("parent")).thenReturn(true);

            assertThatThrownBy(() -> topicService.delete("parent"))
                    .isInstanceOf(BadRequestException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TOP_004.getCode());

            verify(topicRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw BadRequestException TOP_005 when has active questions")
        void shouldThrowBadRequest_whenHasQuestions() {
            Topic topic = new Topic();
            topic.setId("topic-1");
            topic.setIsActive(true);

            when(topicRepository.findById("topic-1")).thenReturn(Optional.of(topic));
            when(topicRepository.existsByParentIdAndIsActiveTrue("topic-1")).thenReturn(false);
            when(questionRepository.existsByTopicIdAndIsActiveTrue("topic-1")).thenReturn(true);

            assertThatThrownBy(() -> topicService.delete("topic-1"))
                    .isInstanceOf(BadRequestException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TOP_005.getCode());

            verify(topicRepository, never()).save(any());
        }
    }
}

