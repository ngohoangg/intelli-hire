package org.aibles.intellihirequestion.service.impl;

import org.aibles.intellihirequestion.dto.TopicResponse;
import org.aibles.intellihirequestion.entity.Topic;
import org.aibles.intellihirequestion.mapper.TopicMapper;
import org.aibles.intellihirequestion.repository.TopicRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TopicServiceImplGetAllTest {

    @Mock
    private TopicRepository topicRepository;

    private final TopicMapper topicMapper = new TopicMapper();

    private TopicServiceImpl topicService;

    @BeforeEach
    void setUp() {
        topicService = new TopicServiceImpl(topicRepository, null, topicMapper);
    }

    private Topic createTopic(String id, String name, Integer order) {
        Topic topic = new Topic();
        topic.setId(id);
        topic.setName(name);
        topic.setOrder(order);
        topic.setIsActive(true);
        return topic;
    }

    @Test
    @DisplayName("Should return list of TopicResponse")
    void shouldReturnTopicResponseList() {
        // Given
        Topic java = createTopic("1", "Java", 1);
        Topic python = createTopic("2", "Python", 2);

        when(topicRepository.findByIsActiveTrueOrderByOrderAscNameAsc())
                .thenReturn(Arrays.asList(java, python));

        // When
        List<TopicResponse> result = topicService.getAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Java");
        assertThat(result.get(1).getName()).isEqualTo("Python");

        verify(topicRepository, times(1)).findByIsActiveTrueOrderByOrderAscNameAsc();
    }

    @Test
    @DisplayName("Should return empty list when no active topics")
    void shouldReturnEmptyListWhenNoTopics() {
        // Given
        when(topicRepository.findByIsActiveTrueOrderByOrderAscNameAsc())
                .thenReturn(Collections.emptyList());

        // When
        List<TopicResponse> result = topicService.getAll();

        // Then
        assertThat(result).isEmpty();

        verify(topicRepository, times(1)).findByIsActiveTrueOrderByOrderAscNameAsc();
    }

    @Test
    @DisplayName("Should keep sorted order from repository")
    void shouldKeepSortedOrder() {
        // Given
        Topic csharp = createTopic("1", "C#", 1);
        Topic java = createTopic("2", "Java", 1);
        Topic python = createTopic("3", "Python", 2);

        when(topicRepository.findByIsActiveTrueOrderByOrderAscNameAsc())
                .thenReturn(Arrays.asList(csharp, java, python));

        // When
        List<TopicResponse> result = topicService.getAll();

        // Then
        assertThat(result).extracting(TopicResponse::getName)
                .containsExactly("C#", "Java", "Python");

        verify(topicRepository).findByIsActiveTrueOrderByOrderAscNameAsc();
    }
}
