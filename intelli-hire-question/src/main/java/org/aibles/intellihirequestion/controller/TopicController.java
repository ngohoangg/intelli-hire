package org.aibles.intellihirequestion.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihirequestion.dto.BaseResponse;
import org.aibles.intellihirequestion.dto.PageResponse;
import org.aibles.intellihirequestion.dto.QuestionResponse;
import org.aibles.intellihirequestion.dto.TopicRequest;
import org.aibles.intellihirequestion.dto.TopicResponse;
import org.aibles.intellihirequestion.dto.TopicTreeResponse;
import org.aibles.intellihirequestion.service.QuestionService;
import org.aibles.intellihirequestion.service.TopicService;
import org.aibles.intellihirequestion.utils.PaginationUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/v1/topics")
@RequiredArgsConstructor
@Slf4j
public class TopicController {

    private final TopicService topicService;
    private final QuestionService questionService;

    @GetMapping("/tree")
    public ResponseEntity<BaseResponse<List<TopicTreeResponse>>> getTree() {
        List<TopicTreeResponse> tree = topicService.getTree();
        return ResponseEntity.ok(BaseResponse.success(tree));
    }

    /**
     * Get all topics
     *
     * @return list of topics
     */
    @GetMapping
    public ResponseEntity<BaseResponse<List<TopicResponse>>> getAllTopics() {
        log.info("Request to get all topics");
        List<TopicResponse> responses = topicService.getAll();
        return ResponseEntity.ok(BaseResponse.success(responses));
    }

    /**
     * Retrieves a topic by its ID.
     *
     * @param id the ID of the topic
     * @return ResponseEntity containing BaseResponse with TopicResponse
     */
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<TopicResponse>> getTopicById(@PathVariable String id) {
        log.info("GET /v1/topics/{}", id);
        TopicResponse response = topicService.getById(id);
        BaseResponse<TopicResponse> result = BaseResponse.success(response);
        return ResponseEntity.ok(result);
    }

    /**
     * Create a new topic
     *
     * @param request the topic request
     * @return the created topic
     */
    @PostMapping
    public ResponseEntity<BaseResponse<TopicResponse>> createTopic(
            @Valid @RequestBody TopicRequest request) {
        log.info("Request to create topic with name: {}", request.getName());
        TopicResponse response = topicService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(response));
    }


    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<TopicResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody TopicRequest request) {
        TopicResponse response = topicService.update(id, request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    /**
     * Delete a topic by its ID
     *
     * @param id the ID of the topic to delete
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteTopic(@PathVariable String id) {
        log.info("Request to delete topic with id: {}", id);
        topicService.delete(id);
        return ResponseEntity.ok(BaseResponse.success(null));
    }

    /**
     * Get questions by topic ID with pagination.
     *
     * @param id   the topic ID
     * @param page page number (default 0)
     * @param size page size (default 20, max 100)
     * @param sort sort field and direction (default createdAt,desc)
     * @return paginated list of active questions for the given topic
     */
    @GetMapping("/{id}/questions")
    public ResponseEntity<BaseResponse<PageResponse<QuestionResponse>>> getQuestionsByTopicId(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        log.info("GET /v1/topics/{}/questions - page: {}, size: {}", id, page, size);

        Pageable pageable = PaginationUtils.buildPageable(page, size, sort,
                PaginationUtils.QUESTION_SORTABLE_FIELDS);
        PageResponse<QuestionResponse> response = questionService.getByTopicId(id, pageable);
        return ResponseEntity.ok(BaseResponse.success(response));
    }
}
