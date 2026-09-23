package org.aibles.intellihirereport.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.aibles.intellihirereport.dto.*;
import org.aibles.intellihirereport.entity.*;
import org.aibles.intellihirereport.exception.BadRequestException;
import org.aibles.intellihirereport.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReportMapper {

    private final ObjectMapper objectMapper;

    public InterviewReport toEntity(CreateReportRequest request) {
        if (request == null) return null;

        return InterviewReport.builder()
                .sessionId(request.getSessionId())
                .userId(request.getUserId())
                .topic(request.getTopic())
                .summary(request.getSummary())
                .roadmapJson(toJsonString(request.getRoadmapJson()))
                .build();
    }

    public QuestionScore toQuestionScoreEntity(QuestionScoreRequest request, String reportId) {
        if (request == null) return null;

        return QuestionScore.builder()
                .reportId(reportId)
                .questionId(request.getQuestionId())
                .questionText(request.getQuestionText())
                .answerText(request.getAnswerText())
                .score(request.getScore())
                .maxScore(request.getMaxScore())
                .feedback(request.getFeedback())
                .skillName(request.getSkillName())
                .build();
    }

    public ReportResponse toReportResponse(InterviewReport report,
                                           List<SkillScore> skillScores,
                                           List<QuestionScore> questionScores) {
        if (report == null) return null;

        return ReportResponse.builder()
                .id(report.getId())
                .sessionId(report.getSessionId())
                .userId(report.getUserId())
                .topic(report.getTopic())
                .totalScore(report.getTotalScore())
                .maxScore(report.getMaxScore())
                .summary(report.getSummary())
                .roadmapJson(fromJsonString(report.getRoadmapJson()))
                .totalQuestions(report.getTotalQuestions())
                .status(report.getStatus().name())
                .skillScores(skillScores != null
                        ? skillScores.stream().map(this::toSkillScoreResponse).collect(Collectors.toList())
                        : null)
                .questionScores(questionScores != null
                        ? questionScores.stream().map(this::toQuestionScoreResponse).collect(Collectors.toList())
                        : null)
                .createdAt(report.getCreatedAt())
                .lastUpdatedAt(report.getLastUpdatedAt())
                .build();
    }

    public ReportSummaryResponse toReportSummaryResponse(InterviewReport report) {
        if (report == null) return null;

        return ReportSummaryResponse.builder()
                .id(report.getId())
                .sessionId(report.getSessionId())
                .userId(report.getUserId())
                .topic(report.getTopic())
                .totalScore(report.getTotalScore())
                .maxScore(report.getMaxScore())
                .totalQuestions(report.getTotalQuestions())
                .status(report.getStatus().name())
                .createdAt(report.getCreatedAt())
                .build();
    }

    public SkillScoreResponse toSkillScoreResponse(SkillScore skillScore) {
        if (skillScore == null) return null;

        return SkillScoreResponse.builder()
                .id(skillScore.getId())
                .skillName(skillScore.getSkillName())
                .score(skillScore.getScore())
                .maxScore(skillScore.getMaxScore())
                .questionCount(skillScore.getQuestionCount())
                .feedback(skillScore.getFeedback())
                .build();
    }

    public QuestionScoreResponse toQuestionScoreResponse(QuestionScore questionScore) {
        if (questionScore == null) return null;

        return QuestionScoreResponse.builder()
                .id(questionScore.getId())
                .questionId(questionScore.getQuestionId())
                .questionText(questionScore.getQuestionText())
                .answerText(questionScore.getAnswerText())
                .score(questionScore.getScore())
                .maxScore(questionScore.getMaxScore())
                .feedback(questionScore.getFeedback())
                .skillName(questionScore.getSkillName())
                .createdAt(questionScore.getCreatedAt())
                .build();
    }

    public UserProgressResponse toUserProgressResponse(UserProgress progress) {
        if (progress == null) return null;

        return UserProgressResponse.builder()
                .id(progress.getId())
                .userId(progress.getUserId())
                .skillName(progress.getSkillName())
                .latestScore(progress.getLatestScore())
                .averageScore(progress.getAverageScore())
                .totalAttempts(progress.getTotalAttempts())
                .scoreTrend(progress.getScoreTrend())
                .lastUpdatedAt(progress.getLastUpdatedAt())
                .build();
    }

    public String toJsonString(Object obj) {
        if (obj == null) return null;
        if (obj instanceof String) return (String) obj;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new BadRequestException(ErrorCode.RPT_006);
        }
    }

    private Object fromJsonString(String json) {
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (JsonProcessingException e) {
            throw new BadRequestException(ErrorCode.RPT_006);
        }
    }
}
