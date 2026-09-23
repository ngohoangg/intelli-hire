package org.aibles.intellihirequestion.service;

import org.aibles.intellihirequestion.dto.StudyArticleRequest;
import org.aibles.intellihirequestion.dto.StudyArticleResponse;
import org.springframework.data.domain.Pageable;
import org.aibles.intellihirequestion.dto.PageResponse;

/**
 * StudyArticleService is an interface that defines the contract for the service layer responsible for handling business logic related to study articles. It may include methods for creating, retrieving, updating, and deleting study articles, as well as any other operations relevant to managing study articles in the application.
 */
public interface StudyArticleService {

    /**
     * Retrieves active study articles with optional topic and difficulty filters.
     *
     * @param topicId       optional topic identifier
     * @param difficultyId  optional difficulty identifier
     * @param pageable      pagination parameters
     * @return paginated study article responses
     */
    PageResponse<StudyArticleResponse> getAll(String topicId, String difficultyId, Pageable pageable);

    /**
     * Retrieves a study article by its unique identifier.
     *
     * @param id the unique identifier of the study article
     * @return a StudyArticleResponse containing the details of the study article
     */
    StudyArticleResponse getById(String id);

    /**
     * Creates a new study article based on the provided request data.
     *
     * @param studyArticleRequest the request object containing the details of the study article to be created
     * @return a StudyArticleResponse containing the details of the newly created study article
     */
    StudyArticleResponse create(StudyArticleRequest studyArticleRequest);

    /**
     * Updates an existing study article identified by its unique identifier with the provided request data.
     *
     * @param id the unique identifier of the study article to be updated
     * @param studyArticleRequest the request object containing the updated details of the study article
     * @return a StudyArticleResponse containing the details of the updated study article
     */
    StudyArticleResponse update(String id, StudyArticleRequest studyArticleRequest);

    /**
     * Deletes a study article identified by its unique identifier.
     *
     * @param id the unique identifier of the study article to be deleted
     */
    void delete(String id);
}
