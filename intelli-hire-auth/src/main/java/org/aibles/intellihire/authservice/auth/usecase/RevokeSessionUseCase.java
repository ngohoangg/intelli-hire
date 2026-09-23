package org.aibles.intellihire.authservice.auth.usecase;

public interface RevokeSessionUseCase {
    void execute(Long accountId, Long sessionId);
}
