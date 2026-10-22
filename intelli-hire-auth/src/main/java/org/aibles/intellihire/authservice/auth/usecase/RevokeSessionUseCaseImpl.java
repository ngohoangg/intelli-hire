package org.aibles.intellihire.authservice.auth.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.auth.adapter.repository.RefreshTokenRepository;
import org.aibles.intellihire.authservice.auth.domain.InvalidTokenException;
import org.aibles.intellihire.authservice.auth.domain.RefreshTokenEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RevokeSessionUseCaseImpl implements RevokeSessionUseCase {

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional
    public void execute(Long accountId, Long sessionId) {
        log.debug("Revoking session {} for account: {}", sessionId, accountId);

        RefreshTokenEntity token = refreshTokenRepository.findById(sessionId)
                .orElseThrow(() -> new InvalidTokenException("Không tìm thấy phiên"));

        if (!token.getAccountId().equals(accountId)) {
            throw new InvalidTokenException("Phiên không thuộc về tài khoản này");
        }

        if (!token.isRevoked()) {
            token.revoke("session_revoked");
            refreshTokenRepository.save(token);
            log.info("Session {} revoked for account: {}", sessionId, accountId);
        }
    }
}
