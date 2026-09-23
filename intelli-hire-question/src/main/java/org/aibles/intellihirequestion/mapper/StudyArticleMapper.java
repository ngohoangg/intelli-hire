package org.aibles.intellihirequestion.mapper;

import org.aibles.intellihirequestion.dto.StudyArticleRequest;
import org.aibles.intellihirequestion.dto.StudyArticleResponse;
import org.aibles.intellihirequestion.entity.StudyArticle;
import org.aibles.intellihirequestion.projection.StudyArticleDetailProjection;
import org.springframework.stereotype.Component;

@Component
public class StudyArticleMapper {

    /**
     * Converts a study article detail projection to a response DTO.
     *
     * @param projection the projection containing article details and joined names
     * @return the corresponding StudyArticleResponse DTO
     */
    public StudyArticleResponse toResponse(StudyArticleDetailProjection projection) {
        if (projection == null) {
            return null;
        }

        return StudyArticleResponse.builder()
                .id(projection.getId())
                .topicId(projection.getTopicId())
                .topicName(projection.getTopicName())
                .difficultyId(projection.getDifficultyId())
                .difficultyName(projection.getDifficultyName())
                .title(projection.getTitle())
                .url(projection.getUrl())
                .description(projection.getDescription())
                .source(projection.getSource())
                .isActive(projection.getIsActive())
                .build();
    }

    /**
     * Converts a study article entity to a response DTO, using provided topic and difficulty names.
     *
     * @param studyArticle   the StudyArticle entity to convert
     * @param topicName      the name of the topic associated with the article
     * @param difficultyName the name of the difficulty level associated with the article
     * @return the corresponding StudyArticleResponse DTO
     */
    public StudyArticleResponse toResponse(StudyArticle studyArticle, String topicName, String difficultyName) {
        if (studyArticle == null) {
            return null;
        }

        return StudyArticleResponse.builder()
                .id(studyArticle.getId())
                .topicId(studyArticle.getTopicId())
                .topicName(topicName)
                .difficultyId(studyArticle.getDifficultyId())
                .difficultyName(difficultyName)
                .title(studyArticle.getTitle())
                .url(studyArticle.getUrl())
                .description(studyArticle.getDescription())
                .source(studyArticle.getSource())
                .isActive(studyArticle.getIsActive())
                .build();
    }

    /**
     * Converts a study article request DTO to an entity.
     *
     * @param request the request DTO containing the details of the study article to be created or updated
     * @return the corresponding StudyArticle entity
     */
    public StudyArticle toEntity(StudyArticleRequest request) {
        if (request == null) {
            return null;
        }

        return StudyArticle.builder()
                .topicId(request.getTopicId())
                .difficultyId(request.getDifficultyId())
                .title(request.getTitle())
                .url(request.getUrl())
                .description(request.getDescription())
                .source(request.getSource())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
    }

    /**
     * Updates an StudyArticle entity with values from request.
     * Keeps entity identity and audit fields managed by JPA/BaseEntity.
     */
    public StudyArticle updateEntity(StudyArticle studyArticle, StudyArticleRequest request) {
        if (studyArticle == null) {
            return null;
        }
        if (request == null) {
            throw new IllegalArgumentException("StudyArticleRequest must not be null");
        }

        studyArticle.setTopicId(request.getTopicId());
        studyArticle.setDifficultyId(request.getDifficultyId());
        studyArticle.setTitle(request.getTitle());
        studyArticle.setUrl(request.getUrl());
        studyArticle.setDescription(request.getDescription());
        studyArticle.setSource(request.getSource());

        if (request.getIsActive() != null) {
            studyArticle.setIsActive(request.getIsActive());
        }

        return studyArticle;
    }
}
