package org.aibles.intellihirequestion.mapper;

import org.aibles.intellihirequestion.dto.TopicRequest;
import org.aibles.intellihirequestion.dto.TopicResponse;
import org.aibles.intellihirequestion.dto.TopicTreeResponse;
import org.aibles.intellihirequestion.entity.Topic;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Mapper class for converting Topic entity to TopicResponse DTO
 */
@Component
public class TopicMapper {

    /**
     * Converts a Topic entity to a TopicResponse DTO.
     *
     * @param topic the Topic entity to convert
     * @return the corresponding TopicResponse DTO
     */
    public static TopicResponse toResponse(Topic topic) {
        if (topic == null) {
            return null;
        }

        return TopicResponse.builder()
                .id(topic.getId())
                .name(topic.getName())
                .description(topic.getDescription())
                .parentId(topic.getParentId())
                .order(topic.getOrder())
                .isActive(topic.getIsActive())
                .createdAt(topic.getCreatedAt())
                .lastUpdatedAt(topic.getLastUpdatedAt())
                .build();
    }

    /**
     * Convert list of Topic entities to list of TopicResponse DTOs
     * @param topics List of Topic entities
     * @return List of TopicResponse DTOs
     */
    public List<TopicResponse> topicResponseList(List<Topic> topics) {
        return topics.stream()
                .map(TopicMapper::toResponse)
                .toList();
    }

    public TopicTreeResponse toTreeResponse(Topic entity, List<TopicTreeResponse> children) {
        return TopicTreeResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .order(entity.getOrder())
                .children(children)
                .build();
    }

    /**
     * Convert TopicRequest DTO to Topic entity
     * @param request TopicRequest DTO
     * @return Topic entity
     */
    public Topic toEntity(TopicRequest request) {
        if (request == null) {
            return null;
        }

        return Topic.builder()
                .name(request.getName())
                .description(request.getDescription())
                .parentId(request.getParentId())
                .order(request.getOrder() != null ? request.getOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
    }
}
