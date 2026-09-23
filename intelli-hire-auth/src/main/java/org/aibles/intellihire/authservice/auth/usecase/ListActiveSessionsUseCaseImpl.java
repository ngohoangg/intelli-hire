package org.aibles.intellihire.authservice.auth.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.auth.adapter.repository.RefreshTokenRepository;
import org.aibles.intellihire.authservice.auth.domain.RefreshTokenEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListActiveSessionsUseCaseImpl implements ListActiveSessionsUseCase {

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RefreshTokenEntity> execute(Long accountId) {
        log.debug("Listing active sessions for account: {}", accountId);
        return refreshTokenRepository.findActiveByAccountId(accountId, LocalDateTime.now());
    }
}
