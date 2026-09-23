package org.aibles.intellihiregrading.mapper;

import org.aibles.intellihiregrading.dto.*;
import org.aibles.intellihiregrading.entity.GradingResult;
import org.aibles.intellihiregrading.entity.GradingSession;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GradingMapper {

    public GradingSession toEntity(GradeRequest request) {
        if (request == null) return null;
        return GradingSession.builder()
                .sessionId(request.getSessionId())
                .userId(request.getUserId())
                .topic(request.getTopic())
                .totalQuestions(request.getAnswers().size())
                .build();
    }

    public GradingResponse toGradingResponse(GradingSession session, List<GradingResult> results) {
        if (session == null) return null;

        double totalScore = results.stream()
                .mapToDouble(r -> r.getScore() != null ? r.getScore() : 0.0).sum();
        double maxScore = results.stream()
                .mapToDouble(r -> r.getMaxScore() != null ? r.getMaxScore() : 0.0).sum();

        return GradingResponse.builder()
                .id(session.getId())
                .sessionId(session.getSessionId())
                .userId(session.getUserId())
                .topic(session.getTopic())
                .status(session.getStatus().name())
                .totalQuestions(session.getTotalQuestions())
                .totalScore(totalScore)
                .maxScore(maxScore)
                .results(results.stream().map(this::toResultResponse).collect(Collectors.toList()))
                .createdAt(session.getCreatedAt())
                .build();
    }

    public GradingResultResponse toResultResponse(GradingResult result) {
        if (result == null) return null;
        return GradingResultResponse.builder()
                .id(result.getId())
                .questionId(result.getQuestionId())
                .questionText(result.getQuestionText())
                .answerText(result.getAnswerText())
                .skillName(result.getSkillName())
                .score(result.getScore())
                .maxScore(result.getMaxScore())
                .feedback(result.getFeedback())
                .modelUsed(result.getModelUsed())
                .latencyMs(result.getLatencyMs())
                .createdAt(result.getCreatedAt())
                .build();
    }

    public GradingSummaryResponse toSummaryResponse(GradingSession session) {
        if (session == null) return null;
        return GradingSummaryResponse.builder()
                .id(session.getId())
                .sessionId(session.getSessionId())
                .userId(session.getUserId())
                .topic(session.getTopic())
                .status(session.getStatus().name())
                .totalQuestions(session.getTotalQuestions())
                .createdAt(session.getCreatedAt())
                .build();
    }
}
