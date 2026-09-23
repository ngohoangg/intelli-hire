package org.aibles.intellihirereport.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihirereport.dto.*;
import org.aibles.intellihirereport.entity.InterviewReport;
import org.aibles.intellihirereport.entity.QuestionScore;
import org.aibles.intellihirereport.entity.SkillScore;
import org.aibles.intellihirereport.entity.enums.ReportStatus;
import org.aibles.intellihirereport.exception.BadRequestException;
import org.aibles.intellihirereport.exception.DuplicateException;
import org.aibles.intellihirereport.exception.ErrorCode;
import org.aibles.intellihirereport.exception.NotFoundException;
import org.aibles.intellihirereport.mapper.ReportMapper;
import org.aibles.intellihirereport.repository.InterviewReportRepository;
import org.aibles.intellihirereport.repository.QuestionScoreRepository;
import org.aibles.intellihirereport.repository.SkillScoreRepository;
import org.aibles.intellihirereport.service.ReportService;
import org.aibles.intellihirereport.service.UserProgressService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final InterviewReportRepository reportRepository;
    private final SkillScoreRepository skillScoreRepository;
    private final QuestionScoreRepository questionScoreRepository;
    private final UserProgressService userProgressService;
    private final ReportMapper reportMapper;

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "user_stats", key = "#request.userId"),
            @CacheEvict(value = "user_progress", key = "#request.userId"),
            @CacheEvict(value = "user_progress_skill", allEntries = true)
    })
    public ReportResponse create(CreateReportRequest request) {
        log.debug("Creating report for sessionId: {}", request.getSessionId());

        // Check duplicate session
        if (reportRepository.existsBySessionId(request.getSessionId())) {
            throw new DuplicateException(ErrorCode.RPT_002);
        }

        // Create report entity
        InterviewReport report = reportMapper.toEntity(request);
        report.setStatus(ReportStatus.COMPLETED);
        report.setTotalQuestions(request.getQuestionScores().size());

        // Save report first to get ID
        report = reportRepository.save(report);
        String reportId = report.getId();

        // Save question scores
        List<QuestionScore> questionScores = request.getQuestionScores().stream()
                .map(qs -> reportMapper.toQuestionScoreEntity(qs, reportId))
                .collect(Collectors.toList());
        questionScores = questionScoreRepository.saveAll(questionScores);

        // Calculate and save skill scores (aggregate by skill_name)
        List<SkillScore> skillScores = calculateSkillScores(questionScores, reportId);
        skillScores = skillScoreRepository.saveAll(skillScores);

        // Calculate total score (null-safe)
        double totalScore = questionScores.stream()
                .mapToDouble(qs -> qs.getScore() != null ? qs.getScore() : 0.0)
                .sum();
        double maxScore = questionScores.stream()
                .mapToDouble(qs -> qs.getMaxScore() != null ? qs.getMaxScore() : 0.0)
                .sum();

        report.setTotalScore(totalScore);
        report.setMaxScore(maxScore);
        report = reportRepository.save(report);

        // Update user progress for each skill
        for (SkillScore skillScore : skillScores) {
            double avgSkillScore = skillScore.getQuestionCount() > 0
                    ? skillScore.getScore() / skillScore.getQuestionCount()
                    : 0;
            userProgressService.updateProgress(
                    request.getUserId(),
                    skillScore.getSkillName(),
                    avgSkillScore
            );
        }

        log.info("Report created successfully for sessionId: {}, reportId: {}",
                request.getSessionId(), reportId);

        return reportMapper.toReportResponse(report, skillScores, questionScores);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "report_session", key = "#sessionId")
    public ReportResponse getBySessionId(String sessionId) {
        log.debug("Getting report for sessionId: {}", sessionId);

        InterviewReport report = reportRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RPT_001));

        List<SkillScore> skillScores = skillScoreRepository.findByReportId(report.getId());
        List<QuestionScore> questionScores = questionScoreRepository.findByReportId(report.getId());

        return reportMapper.toReportResponse(report, skillScores, questionScores);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "report", key = "#id")
    public ReportResponse getById(String id) {
        log.debug("Getting report by id: {}", id);

        InterviewReport report = reportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RPT_001));

        List<SkillScore> skillScores = skillScoreRepository.findByReportId(id);
        List<QuestionScore> questionScores = questionScoreRepository.findByReportId(id);

        return reportMapper.toReportResponse(report, skillScores, questionScores);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReportSummaryResponse> getHistoryByUserId(Long userId, String status, Pageable pageable) {
        log.debug("Getting report history for userId: {}, status: {}", userId, status);

        Page<InterviewReport> reportPage;

        if (status != null && !status.isBlank()) {
            ReportStatus reportStatus;
            try {
                reportStatus = ReportStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException(ErrorCode.RPT_004);
            }
            reportPage = reportRepository.findByUserIdAndStatus(userId, reportStatus, pageable);
        } else {
            reportPage = reportRepository.findByUserId(userId, pageable);
        }

        Page<ReportSummaryResponse> responsePage = reportPage
                .map(reportMapper::toReportSummaryResponse);

        return PageResponse.from(responsePage);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "report", key = "#id"),
            @CacheEvict(value = "report_session", allEntries = true)
    })
    public ReportResponse update(String id, UpdateReportRequest request) {
        log.debug("Updating report id: {}", id);

        InterviewReport report = reportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RPT_001));

        if (request.getSummary() != null) {
            report.setSummary(request.getSummary());
        }
        if (request.getRoadmapJson() != null) {
            report.setRoadmapJson(reportMapper.toJsonString(request.getRoadmapJson()));
        }

        report = reportRepository.save(report);

        List<SkillScore> skillScores = skillScoreRepository.findByReportId(id);
        List<QuestionScore> questionScores = questionScoreRepository.findByReportId(id);

        log.info("Report updated: {}", id);
        return reportMapper.toReportResponse(report, skillScores, questionScores);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "user_stats", key = "#userId")
    public UserStatsResponse getStatsByUserId(Long userId) {
        log.debug("Getting stats for userId: {}", userId);

        int totalSessions = reportRepository.countByUserId(userId);
        Double avgScorePercent = reportRepository.getAverageScorePercentByUserId(userId);
        Double highestScore = reportRepository.getMaxScorePercentByUserId(userId);
        Double lowestScore = reportRepository.getMinScorePercentByUserId(userId);

        // Get skill progress for top/weak skills
        List<UserProgressResponse> progressList = userProgressService.getByUserId(userId);

        List<UserStatsResponse.SkillSummary> allSkills = progressList.stream()
                .map(p -> UserStatsResponse.SkillSummary.builder()
                        .skillName(p.getSkillName())
                        .averageScore(p.getAverageScore())
                        .totalAttempts(p.getTotalAttempts())
                        .build())
                .sorted((a, b) -> Double.compare(b.getAverageScore(), a.getAverageScore()))
                .collect(Collectors.toList());

        List<UserStatsResponse.SkillSummary> topSkills = allSkills.stream()
                .limit(3)
                .collect(Collectors.toList());

        List<UserStatsResponse.SkillSummary> weakSkills = allSkills.stream()
                .sorted((a, b) -> Double.compare(a.getAverageScore(), b.getAverageScore()))
                .limit(3)
                .collect(Collectors.toList());

        return UserStatsResponse.builder()
                .userId(userId)
                .totalSessions(totalSessions)
                .averageScore(avgScorePercent != null ? Math.round(avgScorePercent * 100.0) / 100.0 : 0.0)
                .highestScore(highestScore != null ? Math.round(highestScore * 100.0) / 100.0 : 0.0)
                .lowestScore(lowestScore != null ? Math.round(lowestScore * 100.0) / 100.0 : 0.0)
                .topSkills(topSkills)
                .weakSkills(weakSkills)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReportSummaryResponse> getAllReports(Pageable pageable) {
        log.debug("Getting all reports (admin)");

        Page<InterviewReport> reportPage = reportRepository.findAll(pageable);

        Page<ReportSummaryResponse> responsePage = reportPage
                .map(reportMapper::toReportSummaryResponse);

        return PageResponse.from(responsePage);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "report", key = "#id"),
            @CacheEvict(value = "report_session", allEntries = true)
    })
    public ReportResponse regenerateRoadmap(String id, Object roadmapJson) {
        log.debug("Regenerating roadmap for report id: {}", id);

        InterviewReport report = reportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RPT_001));

        report.setRoadmapJson(reportMapper.toJsonString(roadmapJson));
        report = reportRepository.save(report);

        List<SkillScore> skillScores = skillScoreRepository.findByReportId(id);
        List<QuestionScore> questionScores = questionScoreRepository.findByReportId(id);

        log.info("Roadmap regenerated for report: {}", id);
        return reportMapper.toReportResponse(report, skillScores, questionScores);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "report", allEntries = true),
            @CacheEvict(value = "report_session", key = "#sessionId"),
            @CacheEvict(value = "user_stats", allEntries = true),
            @CacheEvict(value = "user_progress", allEntries = true),
            @CacheEvict(value = "user_progress_skill", allEntries = true)
    })
    public void deleteBySessionId(String sessionId) {
        log.debug("Deleting report for sessionId: {}", sessionId);

        InterviewReport report = reportRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RPT_001));

        // Cascade delete handled by DB foreign key ON DELETE CASCADE
        reportRepository.delete(report);

        log.info("Report deleted for sessionId: {}", sessionId);
    }

    private List<SkillScore> calculateSkillScores(List<QuestionScore> questionScores, String reportId) {
        Map<String, List<QuestionScore>> bySkill = questionScores.stream()
                .filter(qs -> qs.getSkillName() != null && !qs.getSkillName().isBlank())
                .collect(Collectors.groupingBy(QuestionScore::getSkillName));

        return bySkill.entrySet().stream()
                .map(entry -> {
                    String skillName = entry.getKey();
                    List<QuestionScore> scores = entry.getValue();

                    double totalScore = scores.stream()
                            .mapToDouble(qs -> qs.getScore() != null ? qs.getScore() : 0.0).sum();
                    double totalMaxScore = scores.stream()
                            .mapToDouble(qs -> qs.getMaxScore() != null ? qs.getMaxScore() : 0.0).sum();

                    return SkillScore.builder()
                            .reportId(reportId)
                            .skillName(skillName)
                            .score(totalScore)
                            .maxScore(totalMaxScore)
                            .questionCount(scores.size())
                            .build();
                })
                .collect(Collectors.toList());
    }
}
