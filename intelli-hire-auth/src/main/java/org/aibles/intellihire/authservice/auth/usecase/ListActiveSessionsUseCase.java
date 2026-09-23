package org.aibles.intellihire.authservice.auth.usecase;

import org.aibles.intellihire.authservice.auth.domain.RefreshTokenEntity;

import java.util.List;

public interface ListActiveSessionsUseCase {
    List<RefreshTokenEntity> execute(Long accountId);
}
