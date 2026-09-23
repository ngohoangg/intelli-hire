package org.aibles.intellihirequestion.service;

import org.aibles.intellihirequestion.dto.DifficultyLevelRequest;
import org.aibles.intellihirequestion.dto.DifficultyLevelResponse;
import java.util.List;

/**
 * Service interface for managing difficulty levels.
 */
public interface DifficultyLevelService {
    /**
     * Retrieves all difficulty levels.
     *
     * @return a list of DifficultyLevelResponse objects
     */
    List<DifficultyLevelResponse> getAll();

    /**
     * Creates a new difficulty level.
     *
     * @param request the difficulty level request payload
     * @return the created difficulty level response
     */
    DifficultyLevelResponse create(DifficultyLevelRequest request);

    /**
     * Retrieves a difficulty level by ID.
     *
     * @param id the difficulty level ID
     * @return the corresponding DifficultyLevelResponse
     */
    DifficultyLevelResponse getById(String id);

    /**
     * Updates an existing difficulty level.
     *
     * @param id the difficulty level ID
     * @param request the updated difficulty level request payload
     * @return the updated DifficultyLevelResponse
     */
    DifficultyLevelResponse update(String id, DifficultyLevelRequest request);

    /**
     * Deletes a difficulty level by ID.
     *
     * @param id the difficulty level ID to delete
     */
    void delete(String id);
}
