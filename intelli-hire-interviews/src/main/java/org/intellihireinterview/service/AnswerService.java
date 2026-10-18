package org.aibles.intellihireinterview.service;

import org.aibles.intellihireinterview.dto.SubmitAnswerRequest;

public interface AnswerService {
    void submitAnswer(Long sessionId, String userId, SubmitAnswerRequest request);
}
