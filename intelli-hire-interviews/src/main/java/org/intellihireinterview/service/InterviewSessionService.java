package org.aibles.intellihireinterview.service;

import org.aibles.intellihireinterview.entity.InterviewSession;
import org.aibles.intellihireinterview.dto.CreateSessionRequest;
import org.aibles.intellihireinterview.dto.SessionDetailResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InterviewSessionService {
    InterviewSession createSession(String userId, CreateSessionRequest request);
    InterviewSession getSession(Long sessionId, String userId);
    List<InterviewSession> listSessions(String userId);
    Page<InterviewSession> listSessions(String userId, Pageable pageable);
    void endSession(Long sessionId, String userId);
    void deleteSession(Long sessionId, String userId);
    SessionDetailResponse getSessionDetail(Long sessionId, String userId);
}
