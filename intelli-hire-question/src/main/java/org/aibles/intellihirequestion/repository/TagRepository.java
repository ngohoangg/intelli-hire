package org.aibles.intellihirequestion.repository;

import org.aibles.intellihirequestion.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Tag entity
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, String> {

    /**
     * Find tag by name
     * @param name the name of the tag
     * @return an optional of tag
     */
    Optional<Tag> findByName(String name);

    /**
     * Find tags by a list of names
     * @param names the list of tag names
     * @return a list of tags
     */
    List<Tag> findByNameIn(List<String> names);

    /**
     * Check if a tag exists by name
     * @param name the name of the tag
     * @return true if a tag exists with the given name, false otherwise
     */
    boolean existsByName(String name);

    /**
     * Find all tags ordered by name ascending
     * @return a list of tags ordered by name ASC
     */
    List<Tag> findAllByOrderByNameAsc();

    /**
     * Count tags by a list of IDs
     * @param ids the list of tag IDs
     * @return the count of tags matching the given IDs
     */
    long countByIdIn(List<String> ids);

    /**
     * Find tags by a list of IDs
     * @param ids the list of tag IDs
     * @return a list of tags
     */
    List<Tag> findByIdIn(List<String> ids);

    /**
     * Check if a tag exists by name excluding a specific id
     * @param name the name of the tag
     * @param id the id to exclude
     * @return true if a tag exists with the given name excluding the specified id, false otherwise
     */
    boolean existsByNameAndIdNot(String name, String id);

    /**
     * Find tags by question ID
     * @param questionId the question ID
     * @return a list of tags for the question
     */
    @Query("SELECT t FROM Tag t " +
            "JOIN QuestionTag qt ON t.id = qt.tagId " +
            "WHERE qt.questionId = :questionId")
    List<Tag> findByQuestionId(@Param("questionId") String questionId);


}
