package org.aibles.intellihirequestion.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihirequestion.dto.BaseResponse;
import org.aibles.intellihirequestion.dto.PageResponse;
import org.aibles.intellihirequestion.dto.StudyArticleRequest;
import org.aibles.intellihirequestion.dto.StudyArticleResponse;
import org.aibles.intellihirequestion.service.StudyArticleService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing study articles related to questions.
 * This controller provides endpoints for creating, retrieving, updating, and deleting study articles.
 * It also allows associating study articles with specific questions.
 */
@RestController
@RequestMapping("/v1/study-articles")
@RequiredArgsConstructor
@Validated
@Slf4j
public class StudyArticleController {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final StudyArticleService studyArticleService;

    @GetMapping
    public ResponseEntity<BaseResponse<PageResponse<StudyArticleResponse>>> getAll(
            @RequestParam(required = false) String topicId,
            @RequestParam(required = false) String difficultyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int normalizedPage = Math.max(page, DEFAULT_PAGE);
        int normalizedSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(normalizedPage, normalizedSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        log.info("Received request to get study articles - topicId: {}, difficultyId: {}, page: {}, size: {}", topicId, difficultyId, normalizedPage, normalizedSize);
        PageResponse<StudyArticleResponse> response = studyArticleService.getAll(topicId, difficultyId, pageable);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    /**
     * Retrieves a study article by its unique identifier.
     *
     * @param id the unique identifier of the study article
     * @return a StudyArticleResponse containing the details of the study article
     */
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<StudyArticleResponse>> getById(@PathVariable String id) {
        log.info("Received request to get study article by id: {}", id);
        StudyArticleResponse response = studyArticleService.getById(id);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    /**
     * Creates a new study article based on the provided request data.
     *
     * @param request the request object containing the details of the study article to be created
     * @return a StudyArticleResponse containing the details of the newly created study article
     */
    @PostMapping
    public ResponseEntity<BaseResponse<StudyArticleResponse>> create(@RequestBody @Valid StudyArticleRequest request) {
        log.info("Received request to create study article with topicId: {}, difficultyId: {}, title: {}",
                request.getTopicId(), request.getDifficultyId(), request.getTitle());
        StudyArticleResponse response = studyArticleService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(response));
    }

    /**
     * Updates an existing study article identified by its unique identifier with the provided request data.
     *
     * @param id the unique identifier of the study article to be updated
     * @param request the request object containing the updated details of the study article
     * @return a StudyArticleResponse containing the details of the updated study article
     */
    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<StudyArticleResponse>> update(@PathVariable String id, @RequestBody @Valid StudyArticleRequest request) {
        log.info("Received request to update study article with id: {}, topicId: {}, difficultyId: {}, title: {}", id, request.getTopicId(), request.getDifficultyId(), request.getTitle());
        StudyArticleResponse response = studyArticleService.update(id, request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    /**
     * Deletes a study article identified by its unique identifier.
     *
     * @param id the unique identifier of the study article to be deleted
     * @return a ResponseEntity with no content indicating successful deletion
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        log.info("Received request to delete study article with id: {}", id);
        studyArticleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
