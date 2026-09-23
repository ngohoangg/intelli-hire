package org.aibles.intellihirequestion.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihirequestion.dto.PageResponse;
import org.aibles.intellihirequestion.dto.StudyArticleRequest;
import org.aibles.intellihirequestion.dto.StudyArticleResponse;
import org.aibles.intellihirequestion.entity.DifficultyLevel;
import org.aibles.intellihirequestion.entity.StudyArticle;
import org.aibles.intellihirequestion.entity.Topic;
import org.aibles.intellihirequestion.exception.ErrorCode;
import org.aibles.intellihirequestion.exception.InactiveResourceException;
import org.aibles.intellihirequestion.exception.NotFoundException;
import org.aibles.intellihirequestion.mapper.StudyArticleMapper;
import org.aibles.intellihirequestion.projection.StudyArticleDetailProjection;
import org.aibles.intellihirequestion.repository.DifficultyLevelRepository;
import org.aibles.intellihirequestion.repository.StudyArticleRepository;
import org.aibles.intellihirequestion.repository.TopicRepository;
import org.aibles.intellihirequestion.service.StudyArticleService;
import org.aibles.intellihirequestion.specification.StudyArticleSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StudyArticleServiceImpl implements StudyArticleService {

    private final StudyArticleRepository studyArticleRepository;
    private final TopicRepository topicRepository;
    private final DifficultyLevelRepository difficultyLevelRepository;
    private final StudyArticleMapper studyArticleMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StudyArticleResponse> getAll(String topicId, String difficultyId, Pageable pageable) {
        log.info("Get study articles - topicId: {}, difficultyId: {}, page: {}, size: {}",
                topicId, difficultyId, pageable.getPageNumber(), pageable.getPageSize());

        Page<StudyArticle> articlePage = studyArticleRepository.findAll(StudyArticleSpecification.withFilters(topicId, difficultyId), pageable);
        Set<String> topicIds = articlePage.getContent().stream()
                .map(StudyArticle::getTopicId)
                .collect(Collectors.toSet());
        Set<String> difficultyIds = articlePage.getContent().stream()
                .map(StudyArticle::getDifficultyId)
                .collect(Collectors.toSet());

        Map<String, String> topicNames = topicRepository.findAllById(topicIds).stream()
                .collect(Collectors.toMap(Topic::getId, Topic::getName));
        Map<String, String> difficultyNames = difficultyLevelRepository.findAllById(difficultyIds).stream()
                .collect(Collectors.toMap(DifficultyLevel::getId, DifficultyLevel::getName));

        Page<StudyArticleResponse> responsePage = articlePage.map(article ->
                studyArticleMapper.toResponse(
                        article,
                        topicNames.get(article.getTopicId()),
                        difficultyNames.get(article.getDifficultyId())
                )
        );

        return PageResponse.from(responsePage);
    }

    /**
     * Retrieves a study article by its unique identifier.
     *
     * @param id the unique identifier of the study article
     * @return a StudyArticleResponse containing the details of the study article
     */
    @Override
    @Transactional(readOnly = true)
    public StudyArticleResponse getById(String id) {
        log.info("Get study article by id: {}", id);

        StudyArticleDetailProjection studyArticle = studyArticleRepository.findByIdProjection(id)
                .orElseThrow(() -> {
                    log.error("Study article not found with id: {}", id);
                    return new NotFoundException(ErrorCode.SA_001);
                });

        if (Boolean.FALSE.equals(studyArticle.getIsActive())) {
            log.error("Study article is inactive with id: {}", id);
            throw new NotFoundException(ErrorCode.SA_001);
        }

        StudyArticleResponse response = studyArticleMapper.toResponse(studyArticle);
        log.info("Successfully retrieved study article with id: {}", id);
        return response;
    }

    /**
     * Creates a new study article based on the provided request data.
     *
     * @param studyArticleRequest the request object containing the details of the study article to be created
     * @return a StudyArticleResponse containing the details of the newly created study article
     */
    @Transactional
    @Override
    public StudyArticleResponse create(StudyArticleRequest studyArticleRequest) {
        log.info("Creating study article with topicId: {}, difficultyId: {}, title: {}",
                studyArticleRequest.getTopicId(), studyArticleRequest.getDifficultyId(), studyArticleRequest.getTitle());

        // Validate topic and difficulty level
        Topic topic = getActiveTopic(studyArticleRequest.getTopicId());
        DifficultyLevel difficultyLevel = getDifficultyLevel(studyArticleRequest.getDifficultyId());

        // Map request to entity and save
        StudyArticle studyArticle = studyArticleMapper.toEntity(studyArticleRequest);
        StudyArticle savedArticle = studyArticleRepository.save(studyArticle);
        log.info("Successfully created study article with id: {}", savedArticle.getId());
        return studyArticleMapper.toResponse(savedArticle, topic.getName(), difficultyLevel.getName());
    }

    /**
     * Updates an existing study article with the provided request data.
     *
     * @param id      the unique identifier of the study article to be updated
     * @param request the request object containing the updated details of the study article
     * @return a StudyArticleResponse containing the details of the updated study article
     */
    @Override
    @Transactional
    public StudyArticleResponse update(String id, StudyArticleRequest request) {
        log.info("Updating study article with id: {}", id);

        StudyArticle studyArticle = getStudyArticle(id);
        Topic topic = getActiveTopic(request.getTopicId());
        DifficultyLevel difficultyLevel = getDifficultyLevel(request.getDifficultyId());

        studyArticleMapper.updateEntity(studyArticle, request);

        log.info("Successfully updated study article with id: {}", id);
        return studyArticleMapper.toResponse(studyArticle, topic.getName(), difficultyLevel.getName());
    }

    /**
     * Deletes a study article identified by its unique identifier.
     *
     * @param id the unique identifier of the study article to be deleted
     */
    @Override
    @Transactional
    public void delete(String id) {
        log.info("Deleting study article with id: {}", id);
        StudyArticle studyArticle = getStudyArticle(id);
        studyArticle.setIsActive(false);
        studyArticleRepository.save(studyArticle);
        log.info("Successfully deleted (deactivated) study article with id: {}", id);
    }

    private StudyArticle getStudyArticle(String id) {
        return studyArticleRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Study article not found with id: {}", id);
                    return new NotFoundException(ErrorCode.SA_001);
                });
    }

    private Topic getActiveTopic(String topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> {
                    log.error("Topic not found with id: {}", topicId);
                    return new NotFoundException(ErrorCode.TOP_001);
                });

        if (Boolean.FALSE.equals(topic.getIsActive())) {
            log.error("Topic is inactive with id: {}", topicId);
            throw new InactiveResourceException(ErrorCode.TOP_004);
        }

        return topic;
    }

    private DifficultyLevel getDifficultyLevel(String difficultyId) {
        return difficultyLevelRepository.findById(difficultyId)
                .orElseThrow(() -> {
                    log.error("Difficulty level not found with id: {}", difficultyId);
                    return new NotFoundException(ErrorCode.DIF_001);
                });
    }
}
