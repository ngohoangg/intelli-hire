package org.aibles.intellihirequestion.repository;

import org.aibles.intellihirequestion.entity.QuestionTag;
import org.aibles.intellihirequestion.entity.key.QuestionTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for QuestionTag entity
 */
@Repository
public interface QuestionTagRepository extends JpaRepository<QuestionTag, QuestionTagId> {

    /**
     * Check if a tag is being used by any question
     * @param tagId the tag ID to check
     * @return true if the tag is in use, false otherwise
     */
    boolean existsByTagId(String tagId);

    /**
     * Find all question tags by question ID
     * @param questionId the question ID
     * @return a list of question tags
     */
    List<QuestionTag> findByQuestionId(String questionId);

    /**
     * Delete all question tags by question ID
     * @param questionId the question ID
     */
    void deleteByQuestionId(String questionId);

    /**
     * Find all question tags by multiple question IDs (batch fetch)
     * @param questionIds list of question IDs
     * @return a list of question tags
     */
    List<QuestionTag> findByQuestionIdIn(List<String> questionIds);
}
