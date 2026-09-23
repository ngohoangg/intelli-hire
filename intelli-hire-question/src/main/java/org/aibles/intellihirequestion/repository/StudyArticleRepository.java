package org.aibles.intellihirequestion.repository;

import org.aibles.intellihirequestion.entity.StudyArticle;
import org.aibles.intellihirequestion.projection.StudyArticleDetailProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for StudyArticle entity
 */
@Repository
public interface StudyArticleRepository extends JpaRepository<StudyArticle, String>, JpaSpecificationExecutor<StudyArticle> {

    /**
     * Find study articles by topic ID
     * @param topicId the topic ID of the study articles
     * @return a list of study articles
     */
    List<StudyArticle> findByTopicId(String topicId);

    /**
     * Find study articles by difficulty ID
     * @param difficultyId the difficulty ID of the study articles
     * @return a list of study articles
     */
    List<StudyArticle> findByTopicIdAndDifficultyId(String topicId, String difficultyId);

    /**
     * Find all active study articles
     * @return a list of active study articles
     */
    List<StudyArticle> findByIsActiveTrue();

    /**
     * Find study articles by source
     * @param source the source of the study articles
     * @return a list of study articles
     */
    List<StudyArticle> findBySource(String source);

    /**
     * Find study article by ID with topic and difficulty details using projection.
     * @param id the study article ID
     * @return an optional projection containing article details
     */
    @Query("SELECT sa.id as id, " +
            "sa.topicId as topicId, " +
            "t.name as topicName, " +
            "sa.difficultyId as difficultyId, " +
            "d.name as difficultyName, " +
            "sa.title as title, " +
            "sa.url as url, " +
            "sa.description as description, " +
            "sa.source as source, " +
            "sa.isActive as isActive " +
            "FROM StudyArticle sa " +
            "LEFT JOIN Topic t ON sa.topicId = t.id " +
            "LEFT JOIN DifficultyLevel d ON sa.difficultyId = d.id " +
            "WHERE sa.id = :id " +
            "AND sa.isActive = true")
    Optional<StudyArticleDetailProjection> findByIdProjection(String id);

    /**
     * Check if any study article exists with the given difficulty ID.
     *
     * @param difficultyId the difficulty ID to check
     * @return true if any study article exists with the given difficulty ID, false otherwise
     */
    boolean existsByDifficultyId(String difficultyId);
}
