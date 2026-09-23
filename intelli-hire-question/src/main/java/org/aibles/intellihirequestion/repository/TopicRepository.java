package org.aibles.intellihirequestion.repository;


import org.aibles.intellihirequestion.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Topic entity
 */
@Repository
public interface TopicRepository extends JpaRepository<Topic, String> {

    /**
     * Find topics by parent ID
     * @param parentId the parent ID of the topics
     * @return a list of topics
     */
    List<Topic> findByParentId(String parentId);

    /**
     * Find topics with no parent (root topics)
     * @return a list of root topics
     */
    List<Topic> findByParentIdIsNull();

    /**
     * Find topic by name ignoring case
     * @param name the name of the topic
     * @return an optional of topic
     */
    Optional<Topic> findByNameIgnoreCase(String name);

    /**
     * Check if any topic exists with the given parent ID
     * @param parentId the parent ID to check
     * @return true if any topic exists with the given parent ID, false otherwise
     */
    boolean existsByParentId(String parentId);

    /**
     * Find all active topics sorted by order and name.
     * @return a list of active topics sorted by order ASC, then name ASC
     */
    List<Topic> findByIsActiveTrueOrderByOrderAscNameAsc();

    /**
     * Check if topic name exists under the same parent (for non-root topics)
     * @param name the topic name
     * @param parentId the parent ID
     * @return true if name exists under the same parent
     */
    boolean existsByNameIgnoreCaseAndParentId(String name, String parentId);

    /**
     * Check if topic name exists at root level (parentId is null)
     * @param name the topic name
     * @return true if name exists at root level
     */
    boolean existsByNameIgnoreCaseAndParentIdIsNull(String name);

    /**
     * Check if topic exists with name under same parent, excluding specific topic
     * @param name the name to check
     * @param parentId the parent ID
     * @param id the topic ID to exclude
     * @return true if duplicate exists
     */
    boolean existsByNameAndParentIdAndIdNot(String name, String parentId, String id);

    /**
     * Check if topic exists with name at root level (null parent), excluding specific topic
     * @param name the name to check
     * @param id the topic ID to exclude
     * @return true if duplicate exists at root level
     */
    boolean existsByNameAndParentIdIsNullAndIdNot(String name, String id);

    /**
     * Check if any active topic exists with the given parent ID
     * @param parentId the parent ID to check
     * @return true if any active topic exists with the given parent ID, false otherwise
     */
    boolean existsByParentIdAndIsActiveTrue(String parentId);
}
