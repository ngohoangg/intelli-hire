package org.aibles.intellihiregrading.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihiregrading.dto.*;
import org.aibles.intellihiregrading.exception.ErrorCode;
import org.aibles.intellihiregrading.exception.ForbiddenException;
import org.aibles.intellihiregrading.service.GradingService;
import org.aibles.intellihiregrading.utils.PaginationUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/gradings")
@RequiredArgsConstructor
@Validated
public class GradingController {

    private final GradingService gradingService;

    @PostMapping
    public ResponseEntity<BaseResponse<GradingResponse>> grade(
            @Valid @RequestBody GradeRequest request) {
        log.info("Grading request for sessionId: {}", request.getSessionId());
        GradingResponse response = gradingService.grade(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<GradingResponse>> getById(@PathVariable String id) {
        log.info("Getting grading by id: {}", id);
        GradingResponse response = gradingService.getById(id);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<BaseResponse<GradingResponse>> getBySessionId(@PathVariable String sessionId) {
        log.info("Getting grading for sessionId: {}", sessionId);
        GradingResponse response = gradingService.getBySessionId(sessionId);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<BaseResponse<PageResponse<GradingSummaryResponse>>> getHistory(
            @PathVariable String userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        log.info("Getting grading history for userId: {}", userId);
        Pageable pageable = PaginationUtils.buildPageable(
                page, size, sort, PaginationUtils.GRADING_SORTABLE_FIELDS);
        PageResponse<GradingSummaryResponse> response = gradingService.getHistoryByUserId(userId, status, pageable);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @GetMapping("/admin/all")
    public ResponseEntity<BaseResponse<PageResponse<GradingSummaryResponse>>> getAllGradings(
            @RequestHeader(value = "X-User-Role", defaultValue = "") String userRole,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        log.info("Admin getting all gradings, role: {}", userRole);
        if (!"ROLE_ADMIN".equalsIgnoreCase(userRole) && !"ADMIN".equalsIgnoreCase(userRole)) {
            throw new ForbiddenException(ErrorCode.GRD_005);
        }
        Pageable pageable = PaginationUtils.buildPageable(
                page, size, sort, PaginationUtils.GRADING_SORTABLE_FIELDS);
        PageResponse<GradingSummaryResponse> response = gradingService.getAllGradings(pageable);
        return ResponseEntity.ok(BaseResponse.success(response));
    }
}
