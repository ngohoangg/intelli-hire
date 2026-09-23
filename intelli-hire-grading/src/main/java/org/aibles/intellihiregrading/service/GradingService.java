package org.aibles.intellihiregrading.service;

import org.aibles.intellihiregrading.dto.*;
import org.springframework.data.domain.Pageable;

public interface GradingService {

    GradingResponse grade(GradeRequest request);

    GradingResponse getById(String id);

    GradingResponse getBySessionId(String sessionId);

    PageResponse<GradingSummaryResponse> getHistoryByUserId(String userId, String status, Pageable pageable);

    PageResponse<GradingSummaryResponse> getAllGradings(Pageable pageable);
}
