package org.aibles.intellihirequestion.service;

import org.aibles.intellihirequestion.dto.TagRequest;
import org.aibles.intellihirequestion.dto.TagResponse;
import java.util.List;

/**
 * TagService interface for tag-related operations.
 */
public interface TagService {

    /**
     * Get all tags ordered by name ascending.
     *
     * @return list of TagResponse
     */
    List<TagResponse> getAll();

    /**
     * Create a new tag.
     *
     * @param request the tag request
     * @return the created tag response
     */
    TagResponse create(TagRequest request);

    /**
     * Delete a tag by ID.
     * Validates that tag is not being used by any questions before deletion.
     *
     * @param id the tag ID to delete
     */
    void delete(String id);

    /**
     * Update an existing tag by ID.
     *
     * @param id      the tag ID
     * @param request the tag request
     * @return the updated tag response
     */
    TagResponse update(String id, TagRequest request);
}
