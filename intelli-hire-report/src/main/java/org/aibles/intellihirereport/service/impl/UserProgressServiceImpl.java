package org.aibles.intellihirereport.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihirereport.dto.UserProgressResponse;
import org.aibles.intellihirereport.entity.UserProgress;
import org.aibles.intellihirereport.exception.ErrorCode;
import org.aibles.intellihirereport.exception.NotFoundException;
import org.aibles.intellihirereport.mapper.ReportMapper;
import org.aibles.intellihirereport.repository.UserProgressRepository;
import org.aibles.intellihirereport.service.UserProgressService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProgressServiceImpl implements UserProgressService {

    private final UserProgressRepository userProgressRepository;
    private final ReportMapper reportMapper;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "user_progress", key = "#userId")
    public List<UserProgressResponse> getByUserId(Long userId) {
        log.debug("Getting progress for userId: {}", userId);

        List<UserProgress> progressList = userProgressRepository
                .findByUserIdOrderBySkillNameAsc(userId);

        return progressList.stream()
                .map(reportMapper::toUserProgressResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "user_progress_skill", key = "#userId + ':' + #skillName")
    public UserProgressResponse getByUserIdAndSkillName(Long userId, String skillName) {
        log.debug("Getting progress for userId: {}, skill: {}", userId, skillName);

        UserProgress progress = userProgressRepository
                .findByUserIdAndSkillName(userId, skillName)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRG_001));

        return reportMapper.toUserProgressResponse(progress);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "user_progress", key = "#userId"),
            @CacheEvict(value = "user_progress_skill", key = "#userId + ':' + #skillName")
    })
    public void updateProgress(Long userId, String skillName, Double score) {
        log.debug("Updating progress for userId: {}, skill: {}, score: {}", userId, skillName, score);

        Optional<UserProgress> existing = userProgressRepository
                .findByUserIdAndSkillNameForUpdate(userId, skillName);

        if (existing.isPresent()) {
            UserProgress progress = existing.get();
            int newAttempts = progress.getTotalAttempts() + 1;
            double newAverage = ((progress.getAverageScore() * progress.getTotalAttempts()) + score) / newAttempts;

            progress.setLatestScore(score);
            progress.setAverageScore(newAverage);
            progress.setTotalAttempts(newAttempts);

            // Update score trend (append new score, cap at 50 entries)
            List<Double> trend = progress.getScoreTrend() != null
                    ? new ArrayList<>(progress.getScoreTrend())
                    : new ArrayList<>();
            trend.add(score);
            if (trend.size() > 50) {
                trend = new ArrayList<>(trend.subList(trend.size() - 50, trend.size()));
            }
            progress.setScoreTrend(trend);

            userProgressRepository.save(progress);
            log.debug("Updated existing progress for skill: {}", skillName);
        } else {
            List<Double> trend = new ArrayList<>();
            trend.add(score);

            UserProgress newProgress = UserProgress.builder()
                    .userId(userId)
                    .skillName(skillName)
                    .latestScore(score)
                    .averageScore(score)
                    .totalAttempts(1)
                    .scoreTrend(trend)
                    .build();

            userProgressRepository.save(newProgress);
            log.debug("Created new progress for skill: {}", skillName);
        }
    }

}
