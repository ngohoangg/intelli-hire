package org.aibles.intellihirequestion.repository;

import org.aibles.intellihirequestion.entity.Rubric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Rubric entity
 */
@Repository
public interface RubricRepository extends JpaRepository<Rubric, String> {

    /**
     * Find rubric by question ID
     * @param questionId the question ID of the rubric
     * @return an optional of rubric
     */
    Optional<Rubric> findByQuestionId(String questionId);

    /**
     * Check if a rubric exists by question ID
     * @param questionId the question ID to check
     * @return true if a rubric exists with the given question ID, false otherwise
     */
    boolean existsByQuestionId(String questionId);

    /**
     * Delete rubric by question ID
     * @param questionId the question ID of the rubric to delete
     */
    void deleteByQuestionId(String questionId);
}
