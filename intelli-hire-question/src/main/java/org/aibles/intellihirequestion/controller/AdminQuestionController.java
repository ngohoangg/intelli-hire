package org.aibles.intellihirequestion.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihirequestion.dto.AdminQuestionSearchRequest;
import org.aibles.intellihirequestion.dto.BaseResponse;
import org.aibles.intellihirequestion.dto.ModerationStatusRequest;
import org.aibles.intellihirequestion.dto.PageResponse;
import org.aibles.intellihirequestion.dto.QuestionRequest;
import org.aibles.intellihirequestion.dto.QuestionResponse;
import org.aibles.intellihirequestion.service.QuestionService;
import org.aibles.intellihirequestion.utils.PaginationUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/admin/questions")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AdminQuestionController {

    private final QuestionService questionService;

    @GetMapping
    public ResponseEntity<BaseResponse<PageResponse<QuestionResponse>>> listAll(
            AdminQuestionSearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        log.info("GET /v1/admin/questions - status: {}, page: {}, size: {}", request.getStatus(), page, size);
        Pageable pageable = PaginationUtils.buildPageable(page, size, sort, PaginationUtils.QUESTION_SORTABLE_FIELDS);
        return ResponseEntity.ok(BaseResponse.success(questionService.adminListQuestions(request, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<QuestionResponse>> getById(@PathVariable String id) {
        log.info("GET /v1/admin/questions/{}", id);
        return ResponseEntity.ok(BaseResponse.success(questionService.adminGetById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<QuestionResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody QuestionRequest request) {
        log.info("PUT /v1/admin/questions/{}", id);
        return ResponseEntity.ok(BaseResponse.success(questionService.update(id, request, null)));
    }

    @PatchMapping("/{id}/moderation-status")
    public ResponseEntity<BaseResponse<QuestionResponse>> patchModerationStatus(
            @PathVariable String id,
            @Valid @RequestBody ModerationStatusRequest request) {
        log.info("PATCH /v1/admin/questions/{}/moderation-status - status: {}", id, request.getStatus());
        return ResponseEntity.ok(BaseResponse.success(
                questionService.adminPatchModerationStatus(id, request.getStatus(), request.getRejectionReason())));
    }
}
