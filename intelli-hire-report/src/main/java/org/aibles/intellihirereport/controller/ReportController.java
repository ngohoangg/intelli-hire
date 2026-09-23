package org.aibles.intellihirereport.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihirereport.dto.*;
import org.aibles.intellihirereport.exception.ErrorCode;
import org.aibles.intellihirereport.exception.ForbiddenException;
import org.aibles.intellihirereport.service.ReportService;
import org.aibles.intellihirereport.service.UserProgressService;
import org.aibles.intellihirereport.utils.PaginationUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

@Slf4j
@RestController
@RequestMapping("/v1/reports")
@RequiredArgsConstructor
@Validated
public class ReportController {

    private final ReportService reportService;
    private final UserProgressService userProgressService;
    @PostMapping
    public ResponseEntity<BaseResponse<ReportResponse>> create(
            @Valid @RequestBody CreateReportRequest request) {
        log.info("Creating report for sessionId: {}", request.getSessionId());
        ReportResponse response = reportService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<ReportResponse>> update(
            @PathVariable String id,
            @RequestBody UpdateReportRequest request) {
        log.info("Updating report id: {}", id);
        ReportResponse response = reportService.update(id, request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<BaseResponse<ReportResponse>> getBySessionId(
            @PathVariable String sessionId) {
        log.info("Getting report for sessionId: {}", sessionId);
        ReportResponse response = reportService.getBySessionId(sessionId);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<ReportResponse>> getById(
            @PathVariable String id) {
        log.info("Getting report by id: {}", id);
        ReportResponse response = reportService.getById(id);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<BaseResponse<Void>> deleteBySessionId(
            @PathVariable String sessionId) {
        log.info("Deleting report for sessionId: {}", sessionId);
        reportService.deleteBySessionId(sessionId);
        return ResponseEntity.ok(BaseResponse.success(null));
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<BaseResponse<PageResponse<ReportSummaryResponse>>> getHistory(
            @PathVariable Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        log.info("Getting report history for userId: {}, status: {}", userId, status);
        Pageable pageable = PaginationUtils.buildPageable(
                page, size, sort, PaginationUtils.REPORT_SORTABLE_FIELDS);
        PageResponse<ReportSummaryResponse> response = reportService.getHistoryByUserId(userId, status, pageable);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @GetMapping("/stats/{userId}")
    public ResponseEntity<BaseResponse<UserStatsResponse>> getStats(
            @PathVariable Long userId) {
        log.info("Getting stats for userId: {}", userId);
        UserStatsResponse response = reportService.getStatsByUserId(userId);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @GetMapping("/progress/{userId}")
    public ResponseEntity<BaseResponse<List<UserProgressResponse>>> getProgress(
            @PathVariable Long userId) {
        log.info("Getting progress for userId: {}", userId);
        List<UserProgressResponse> response = userProgressService.getByUserId(userId);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @GetMapping("/progress/{userId}/{skillName}")
    public ResponseEntity<BaseResponse<UserProgressResponse>> getProgressBySkill(
            @PathVariable Long userId,
            @PathVariable String skillName) {
        log.info("Getting progress for userId: {}, skill: {}", userId, skillName);
        UserProgressResponse response = userProgressService.getByUserIdAndSkillName(userId, skillName);
        return ResponseEntity.ok(BaseResponse.success(response));
    }


    @PostMapping("/{id}/regenerate")
    public ResponseEntity<BaseResponse<ReportResponse>> regenerateRoadmap(
            @PathVariable String id,
            @Valid @RequestBody RegenerateRoadmapRequest request) {
        log.info("Regenerating roadmap for report id: {}", id);
        ReportResponse response = reportService.regenerateRoadmap(id, request.getRoadmapJson());
        return ResponseEntity.ok(BaseResponse.success(response));
    }


    @GetMapping("/admin/all")
    public ResponseEntity<BaseResponse<PageResponse<ReportSummaryResponse>>> getAllReports(
            @RequestHeader(value = "X-User-Role", defaultValue = "") String userRole,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        log.info("Admin getting all reports, role: {}", userRole);

        if (!"ROLE_ADMIN".equalsIgnoreCase(userRole) && !"ADMIN".equalsIgnoreCase(userRole)) {
            throw new ForbiddenException(ErrorCode.RPT_005);
        }

        Pageable pageable = PaginationUtils.buildPageable(
                page, size, sort, PaginationUtils.REPORT_SORTABLE_FIELDS);
        PageResponse<ReportSummaryResponse> response = reportService.getAllReports(pageable);
        return ResponseEntity.ok(BaseResponse.success(response));
    }
}
