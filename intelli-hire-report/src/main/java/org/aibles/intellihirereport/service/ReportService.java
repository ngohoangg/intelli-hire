package org.aibles.intellihirereport.service;

import org.aibles.intellihirereport.dto.*;
import org.springframework.data.domain.Pageable;

public interface ReportService {

    ReportResponse create(CreateReportRequest request);

    ReportResponse getBySessionId(String sessionId);

    ReportResponse getById(String id);

    PageResponse<ReportSummaryResponse> getHistoryByUserId(Long userId, String status, Pageable pageable);

    ReportResponse update(String id, UpdateReportRequest request);

    UserStatsResponse getStatsByUserId(Long userId);

    PageResponse<ReportSummaryResponse> getAllReports(Pageable pageable);

    ReportResponse regenerateRoadmap(String id, Object roadmapJson);

    void deleteBySessionId(String sessionId);
}
