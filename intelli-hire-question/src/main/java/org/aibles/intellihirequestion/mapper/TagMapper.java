package org.aibles.intellihirequestion.mapper;

import org.aibles.intellihirequestion.dto.TagRequest;
import org.aibles.intellihirequestion.dto.TagResponse;
import org.aibles.intellihirequestion.entity.Tag;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Mapper class for converting Tag entity to TagResponse DTO
 */
@Component
public class TagMapper {

    /**
     * Converts a Tag entity to a TagResponse DTO.
     *
     * @param tag the Tag entity to convert
     * @return the corresponding TagResponse DTO
     */
    public static TagResponse toResponse(Tag tag) {
        if (tag == null) {
            return null;
        }

        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .build();
    }

    /**
     * Convert list of Tag entities to list of TagResponse DTOs
     * @param tags List of Tag entities
     * @return List of TagResponse DTOs
     */
    public List<TagResponse> toResponseList(List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        return tags.stream()
                .map(TagMapper::toResponse)
                .toList();
    }

    /**
     * Convert TagRequest DTO to Tag entity.
     * Normalizes name to lowercase and trimmed.
     * @param request TagRequest DTO
     * @return Tag entity
     */
    public Tag toEntity(TagRequest request) {
        if (request == null) {
            return null;
        }

        return Tag.builder()
                .name(request.getName().trim().toLowerCase())
                .build();
    }
}
