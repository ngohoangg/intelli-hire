package org.aibles.intellihirequestion.service;

import org.aibles.intellihirequestion.dto.TopicRequest;
import org.aibles.intellihirequestion.dto.TopicResponse;
import org.aibles.intellihirequestion.dto.TopicTreeResponse;
import java.util.List;
import org.aibles.intellihirequestion.dto.TopicRequest;
import org.aibles.intellihirequestion.dto.TopicResponse;

/**
 * TopicService interface for topic-related operations.
 */
public interface TopicService {

    /**
     * Get all topics.
     *
     * @return list of TopicResponse
     */
    List<TopicResponse> getAll();

    /**
     * Get all topics as tree structure.
     *
     * @return list of TopicTreeResponse
     */
    List<TopicTreeResponse> getTree();

    /**
     * Retrieves a TopicResponse by its ID.
     *
     * @param id the ID of the topic
     * @return the TopicResponse corresponding to the given ID
     */
    TopicResponse getById(String id);

    /**
     * Create a new topic.
     *
     * @param request the topic request
     * @return the created topic response
     */
    TopicResponse create(TopicRequest request);
    TopicResponse update(String id, TopicRequest request);

    /**
     * Delete a topic by setting isActive to false (soft delete).
     *
     * @param id the ID of the topic to delete
     */
    void delete(String id);
}
