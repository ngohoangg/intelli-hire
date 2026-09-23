package org.aibles.intellihirequestion.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihirequestion.dto.BaseResponse;
import org.aibles.intellihirequestion.dto.BulkImportRequest;
import org.aibles.intellihirequestion.dto.BulkImportResponse;
import org.aibles.intellihirequestion.dto.GenerateQuestionsFromResumeRequest;
import org.aibles.intellihirequestion.dto.GenerateQuestionsFromResumeResponse;
import org.aibles.intellihirequestion.dto.ModerationStatusRequest;
import org.aibles.intellihirequestion.dto.PageResponse;
import org.aibles.intellihirequestion.dto.QuestionRequest;
import org.aibles.intellihirequestion.dto.QuestionResponse;
import org.aibles.intellihirequestion.dto.QuestionSearchRequest;
import org.aibles.intellihirequestion.dto.RandomQuestionRequest;
import org.aibles.intellihirequestion.dto.RubricRequest;
import org.aibles.intellihirequestion.dto.RubricResponse;
import org.aibles.intellihirequestion.service.QuestionService;
import org.aibles.intellihirequestion.service.RubricService;
import org.aibles.intellihirequestion.utils.PaginationUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/questions")
@RequiredArgsConstructor
@Validated
@Slf4j
public class QuestionController {

    private final QuestionService questionService;
    private final RubricService rubricService;

    @PostMapping("/bulk")
    public ResponseEntity<BaseResponse<BulkImportResponse>> bulkImport(@RequestBody @Valid BulkImportRequest request) {
        log.info("POST /v1/questions/bulk - importing {} questions", request.getQuestions().size());
        BulkImportResponse response = questionService.bulkImport(request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<BaseResponse<QuestionResponse>> create(
            @RequestBody @Valid QuestionRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Name", required = false) String username) {
        log.info("Received request to Create Question : {}", request);
        QuestionResponse response = questionService.create(request, userId, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(response));
    }

    @PostMapping("/generate")
    public ResponseEntity<BaseResponse<GenerateQuestionsFromResumeResponse>> generateFromResume(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody @Valid GenerateQuestionsFromResumeRequest request) {
        log.info("POST /v1/questions/generate - resumeId: {}, count: {}", request.getResumeId(), request.getCount());
        GenerateQuestionsFromResumeResponse response = questionService.generateFromResume(userId, request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable String id) {
        log.info("DELETE api/v1/questions/{}", id);
        questionService.delete(id);
        return ResponseEntity.ok(BaseResponse.success(null));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<QuestionResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody QuestionRequest questionRequest,
            @RequestHeader(value = "X-User-Name", required = false) String username) {
        log.info("Received request to update question with id: {}", id);
        QuestionResponse updatedQuestion = questionService.update(id, questionRequest, username);
        return ResponseEntity.ok(BaseResponse.success(updatedQuestion));
    }

    @GetMapping("/submitted")
    public ResponseEntity<BaseResponse<PageResponse<QuestionResponse>>> getSubmitted(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        log.info("GET /v1/questions/submitted - userId: {}", userId);
        Long userIdLong = null;
        if (userId != null) {
            try { userIdLong = Long.parseLong(userId); } catch (NumberFormatException ignored) {}
        }
        Pageable pageable = PaginationUtils.buildPageable(page, size, sort, PaginationUtils.QUESTION_SORTABLE_FIELDS);
        return ResponseEntity.ok(BaseResponse.success(questionService.getSubmittedQuestions(userIdLong, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<QuestionResponse>> getById(@PathVariable String id) {
        log.info("GET api/v1/questions/{}", id);
        QuestionResponse response = questionService.getById(id);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @GetMapping("/random")
    public ResponseEntity<BaseResponse<List<QuestionResponse>>> getRandomQuestions(
            @Valid RandomQuestionRequest request) {
        log.info("GET api/v1/questions/random - count: {}, topicId: {}, difficultyId: {}",
                request.getCount(), request.getTopicId(), request.getDifficultyId());
        List<QuestionResponse> responses = questionService.getRandomQuestions(request);
        return ResponseEntity.ok(BaseResponse.success(responses));
    }

    @GetMapping("/count")
    public ResponseEntity<BaseResponse<Long>> count(
            @RequestParam(required = false) String topicId,
            @RequestParam(required = false) String difficultyId) {
        log.info("GET /v1/questions/count - topicId: {}, difficultyId: {}", topicId, difficultyId);
        return ResponseEntity.ok(BaseResponse.success(questionService.countQuestions(topicId, difficultyId)));
    }

    @GetMapping("/search")
    public ResponseEntity<BaseResponse<PageResponse<QuestionResponse>>> search(
            @Valid QuestionSearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        log.info("GET /v1/questions/search - keyword: {}, page: {}, size: {}",
                request.getKeyword(), page, size);
        Pageable pageable = PaginationUtils.buildPageable(page, size, sort, PaginationUtils.QUESTION_SORTABLE_FIELDS);
        PageResponse<QuestionResponse> response = questionService.searchQuestion(request, pageable);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @PatchMapping("/{id}/moderation-status")
    public ResponseEntity<BaseResponse<QuestionResponse>> resubmit(
            @PathVariable String id,
            @Valid @RequestBody ModerationStatusRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        log.info("PATCH /v1/questions/{}/moderation-status - status: {}", id, request.getStatus());
        Long userIdLong = null;
        if (userId != null) {
            try { userIdLong = Long.parseLong(userId); } catch (NumberFormatException ignored) {}
        }
        return ResponseEntity.ok(BaseResponse.success(questionService.resubmit(id, userIdLong)));
    }

    @PostMapping("/{id}/rubric")
    public ResponseEntity<BaseResponse<RubricResponse>> createRubric(
            @PathVariable String id,
            @Valid @RequestBody RubricRequest request) {
        log.info("POST /v1/questions/{}/rubric", id);
        RubricResponse response = rubricService.createRubric(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(response));
    }

    @GetMapping("/{id}/rubric")
    public ResponseEntity<BaseResponse<RubricResponse>> getRubric(@PathVariable String id) {
        log.info("GET /v1/questions/{}/rubric", id);
        RubricResponse response = rubricService.getRubric(id);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @PutMapping("/{id}/rubric")
    public ResponseEntity<BaseResponse<RubricResponse>> updateRubric(
            @PathVariable String id,
            @Valid @RequestBody RubricRequest request) {
        log.info("PUT /v1/questions/{}/rubric", id);
        RubricResponse response = rubricService.updateRubric(id, request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }
}
