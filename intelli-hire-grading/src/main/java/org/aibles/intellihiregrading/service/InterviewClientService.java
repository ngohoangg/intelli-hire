package org.aibles.intellihiregrading.service;

import org.aibles.intellihiregrading.dto.interview.InterviewSessionDetail;

import java.util.Optional;

public interface InterviewClientService {

    Optional<InterviewSessionDetail> getSessionDetail(Long sessionId, String userId);
}
