package org.aibles.intellihire.authservice.auth.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.auth.adapter.repository.RefreshTokenRepository;
import org.aibles.intellihire.authservice.auth.domain.RefreshTokenEntity;
import org.aibles.intellihire.authservice.auth.token.JwtTokenService;
import org.aibles.intellihire.authservice.auth.token.RefreshTokenClaims;
import org.aibles.intellihire.authservice.auth.usecase.command.LogoutCommand;
import org.aibles.intellihire.authservice.shared.service.SessionActivityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutUseCaseImpl implements LogoutUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenService jwtTokenService;
    private final SessionActivityService sessionActivityService;

    @Override
    @Transactional
    public void execute(LogoutCommand command) {
        log.debug("Processing logout request");

        try {
            RefreshTokenClaims claims = jwtTokenService.parseAndValidateRefreshToken(command.refreshToken());

            Optional<RefreshTokenEntity> tokenOpt = refreshTokenRepository.findByTokenId(claims.tokenId());
            if (tokenOpt.isPresent()) {
                RefreshTokenEntity token = tokenOpt.get();
                token.revoke("logout");
                refreshTokenRepository.save(token);
                sessionActivityService.delete(claims.accountId());
                log.info("Refresh token revoked for account: {}", claims.accountId());
            }
        } catch (Exception e) {
            // Token might be invalid or already revoked - that's fine for logout
            log.debug("Logout with invalid token: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public void logoutAll(Long accountId) {
        log.debug("Processing logout all for account: {}", accountId);

        int revokedCount = refreshTokenRepository.revokeAllByAccountId(
                accountId,
                LocalDateTime.now(),
                "logout_all"
        );

        sessionActivityService.delete(accountId);
        log.info("Revoked {} refresh tokens for account: {}", revokedCount, accountId);
    }
}
