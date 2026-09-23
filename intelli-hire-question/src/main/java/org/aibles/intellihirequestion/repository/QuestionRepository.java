package org.aibles.intellihirequestion.repository;

import org.aibles.intellihirequestion.entity.Question;
import org.aibles.intellihirequestion.entity.enums.QuestionType;
import org.aibles.intellihirequestion.projection.QuestionDetailProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, String>, JpaSpecificationExecutor<Question> {

    /**
     * Find question by ID with topic and difficulty details using Projection
     * @param id the question ID
     * @return an optional of QuestionDetailProjection
     */
    @Query("SELECT q.id as id, " +
            "q.topicId as topicId, " +
            "t.name as topicName, " +
            "q.difficultyId as difficultyId, " +
            "d.name as difficultyName, " +
            "q.questionText as questionText, " +
            "q.questionType as questionType, " +
            "q.isActive as isActive, " +
            "q.status as status, " +
            "q.submittedBy as submittedBy, " +
            "q.submittedById as submittedById, " +
            "q.rejectionReason as rejectionReason, " +
            "q.createdAt as createdAt, " +
            "q.lastUpdatedAt as lastUpdatedAt " +
            "FROM Question q " +
            "LEFT JOIN Topic t ON q.topicId = t.id " +
            "LEFT JOIN DifficultyLevel d ON q.difficultyId = d.id " +
            "WHERE q.id = :id")
    Optional<QuestionDetailProjection> findByIdProjection(@Param("id") String id);

    /**
     * Find questions by topic ID
     * @param topicId the topic ID of the questions
     * @return a list of questions
     */
    List<Question> findByTopicId(String topicId);

    /**
     * Find questions by difficulty ID
     * @param difficultyId the difficulty ID of the questions
     * @return a list of questions
     */
    List<Question> findByDifficultyId(String difficultyId);

    /**
     * Find all active questions
     * @return a list of active questions
     */
    List<Question> findByIsActiveTrue();

    /**
     * Find active questions by topic ID
     * @param topicId the topic ID of the questions
     * @return a list of active questions
     */
    List<Question> findByTopicIdAndIsActiveTrue(String topicId);

    /**
     * Find active questions by topic ID with pagination
     * @param topicId the topic ID of the questions
     * @param pageable pagination parameters
     * @return a page of active questions
     */
    Page<Question> findByTopicIdAndIsActiveTrue(String topicId, Pageable pageable);

    /**
     * Count questions by topic ID
     * @param topicId the topic ID of the questions
     * @return the count of questions
     */
    long countByTopicId(String topicId);

    /**
     * Count questions by difficulty ID
     * @param difficultyId the difficulty ID of the questions
     * @return the count of questions
     */
    long countByDifficultyId(String difficultyId);

    /**
     * Check if any active question exists with the given topic ID
     * @param topicId the topic ID to check
     * @return true if any active question exists with the given topic ID, false otherwise
     */
    boolean existsByTopicIdAndIsActiveTrue(String topicId);

    /**
     * Find active question IDs by optional filters (topic, difficulty, type)
     * @param topicId the topic ID filter (nullable)
     * @param difficultyId the difficulty ID filter (nullable)
     * @param questionType the question type filter (nullable)
     * @return list of question IDs matching filters
     */
    @Query("SELECT q.id FROM Question q " +
            "WHERE q.isActive = true " +
            "AND (:topicId IS NULL OR q.topicId = :topicId) " +
            "AND (:difficultyId IS NULL OR q.difficultyId = :difficultyId) " +
            "AND (:questionType IS NULL OR q.questionType = :questionType)")
    List<String> findIdsByFilters(@Param("topicId") String topicId,
                                  @Param("difficultyId") String difficultyId,
                                  @Param("questionType") QuestionType questionType);

    /**
     * Find active question IDs by optional filters with tag filtering
     * @param topicId the topic ID filter (nullable)
     * @param difficultyId the difficulty ID filter (nullable)
     * @param questionType the question type filter (nullable)
     * @param tagIds list of tag IDs to filter by
     * @return list of question IDs matching filters and having at least one of the specified tags
     */
    @Query("SELECT DISTINCT q.id FROM Question q " +
            "JOIN QuestionTag qt ON q.id = qt.questionId " +
            "WHERE q.isActive = true " +
            "AND (:topicId IS NULL OR q.topicId = :topicId) " +
            "AND (:difficultyId IS NULL OR q.difficultyId = :difficultyId) " +
            "AND (:questionType IS NULL OR q.questionType = :questionType) " +
            "AND qt.tagId IN :tagIds")
    List<String> findIdsByFiltersWithTags(@Param("topicId") String topicId,
                                          @Param("difficultyId") String difficultyId,
                                          @Param("questionType") QuestionType questionType,
                                          @Param("tagIds") List<String> tagIds);

    /**
     * Check if any question exists with the given difficulty ID.
     *
     * @param difficultyId the difficulty ID to check
     * @return true if any question exists with the given difficulty ID, false otherwise
     */
    boolean existsByDifficultyId(String difficultyId);

    /**
     * Count active questions by optional filters (topic, difficulty).
     * Only counts questions where isActive = true.
     */
    @Query("SELECT COUNT(q.id) FROM Question q " +
            "WHERE q.isActive = true " +
            "AND (:topicId IS NULL OR q.topicId = :topicId) " +
            "AND (:difficultyId IS NULL OR q.difficultyId = :difficultyId)")
    long countByFilters(@Param("topicId") String topicId,
                        @Param("difficultyId") String difficultyId);
}
