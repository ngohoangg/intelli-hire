package org.aibles.intellihirequestion.service.impl;

import org.aibles.intellihirequestion.dto.TopicResponse;
import org.aibles.intellihirequestion.entity.Topic;
import org.aibles.intellihirequestion.exception.NotFoundException;
import org.aibles.intellihirequestion.repository.TopicRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GetTopicByIdTest {

    @Mock
    private TopicRepository topicRepository;

    @InjectMocks
    private TopicServiceImpl topicService;

    @Nested
    @DisplayName("getById()")
    class GetByIdTests {

        @Test
        @DisplayName("Should return topic response when topic found and active")
        void shouldReturnTopicResponse_whenTopicFoundAndActive() {
            // Given
            String topicId = "123";
            Topic topic = createTopic(topicId, "Java", true);
            when(topicRepository.findById(topicId)).thenReturn(Optional.of(topic));

            // When
            TopicResponse response = topicService.getById(topicId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(topicId);
            assertThat(response.getName()).isEqualTo("Java");
            assertThat(response.getIsActive()).isTrue();
            verify(topicRepository).findById(topicId);
        }

        @Test
        @DisplayName("Should throw NotFoundException when topic not found in database")
        void shouldThrowNotFoundException_whenTopicNotFoundInDatabase() {
            // Given
            String topicId = "999";
            when(topicRepository.findById(topicId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> topicService.getById(topicId))
                    .isInstanceOf(NotFoundException.class);
            verify(topicRepository).findById(topicId);
        }

        @Test
        @DisplayName("Should throw NotFoundException when topic is inactive")
        void shouldThrowNotFoundException_whenTopicIsInactive() {
            // Given
            String topicId = "123";
            Topic topic = createTopic(topicId, "Java", false);
            when(topicRepository.findById(topicId)).thenReturn(Optional.of(topic));

            // When & Then
            assertThatThrownBy(() -> topicService.getById(topicId))
                    .isInstanceOf(NotFoundException.class);
            verify(topicRepository).findById(topicId);
        }

        @Test
        @DisplayName("Should throw NotFoundException when topic isActive is null")
        void shouldThrowNotFoundException_whenTopicIsActiveIsNull() {
            // Given
            String topicId = "123";
            Topic topic = createTopic(topicId, "Java", null);
            when(topicRepository.findById(topicId)).thenReturn(Optional.of(topic));

            // When & Then
            assertThatThrownBy(() -> topicService.getById(topicId))
                    .isInstanceOf(NotFoundException.class);
            verify(topicRepository).findById(topicId);
        }
    }

    private Topic createTopic(String id, String name, Boolean isActive) {
        Topic topic = new Topic();
        topic.setId(id);
        topic.setName(name);
        topic.setDescription("Test description");
        topic.setIsActive(isActive);
        topic.setCreatedAt(LocalDateTime.now());
        topic.setLastUpdatedAt(LocalDateTime.now());
        return topic;
    }
}