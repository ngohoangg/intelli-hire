package org.aibles.intellihirequestion.mapper;

import org.aibles.intellihirequestion.dto.DifficultyLevelRequest;
import org.aibles.intellihirequestion.dto.DifficultyLevelResponse;
import org.aibles.intellihirequestion.entity.DifficultyLevel;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Mapper class for converting DifficultyLevel entities to DifficultyLevelResponse DTOs.
 */
@Component
public class DifficultyLevelMapper {

    /**
     * Maps a DifficultyLevelRequest DTO to DifficultyLevel entity.
     *
     * @param request the DifficultyLevelRequest DTO
     * @return the corresponding DifficultyLevel entity
     */
    public DifficultyLevel toEntity(DifficultyLevelRequest request) {
        if (request == null) {
            return null;
        }

        return DifficultyLevel.builder()
                .name(request.getName())
                .weight(request.getWeight())
                .description(request.getDescription())
                .build();
    }

    /**
     * Maps a DifficultyLevel entity to a DifficultyLevelResponse DTO.
     *
     * @param difficultyLevel the DifficultyLevel entity
     * @return the corresponding DifficultyLevelResponse DTO
     */
    public DifficultyLevelResponse toResponse(DifficultyLevel difficultyLevel) {

        if (difficultyLevel == null) {
            return null;
        }

        return DifficultyLevelResponse.builder()
                .id(difficultyLevel.getId())
                .name(difficultyLevel.getName())
                .weight(difficultyLevel.getWeight())
                .description(difficultyLevel.getDescription())
                .build();
    }

    /**
     * Maps a list of DifficultyLevel entities to a list of DifficultyLevelResponse DTOs.
     *
     * @param difficultyLevels the list of DifficultyLevel entities
     * @return the corresponding list of DifficultyLevelResponse DTOs
     */
    public List<DifficultyLevelResponse> toResponseList(List<DifficultyLevel> difficultyLevels) {

        if (difficultyLevels.isEmpty()) {
            return  List.of();
        }

        return difficultyLevels.stream()
                .map(this::toResponse)
                .toList();
    }
}
