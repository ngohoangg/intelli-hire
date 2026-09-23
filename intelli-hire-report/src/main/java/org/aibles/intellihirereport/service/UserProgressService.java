package org.aibles.intellihirereport.service;

import org.aibles.intellihirereport.dto.UserProgressResponse;

import java.util.List;

public interface UserProgressService {

    List<UserProgressResponse> getByUserId(Long userId);

    UserProgressResponse getByUserIdAndSkillName(Long userId, String skillName);

    void updateProgress(Long userId, String skillName, Double score);
}
