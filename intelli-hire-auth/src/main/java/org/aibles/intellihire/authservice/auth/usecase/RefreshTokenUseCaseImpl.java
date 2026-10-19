package org.aibles.intellihire.authservice.auth.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.account.adapter.repository.AccountRepository;
import org.aibles.intellihire.authservice.account.domain.AccountEntity;
import org.aibles.intellihire.authservice.account.domain.AccountNotFoundException;
import org.aibles.intellihire.authservice.auth.adapter.repository.RefreshTokenRepository;
import org.aibles.intellihire.authservice.auth.domain.RefreshTokenEntity;
import org.aibles.intellihire.authservice.auth.token.JwtTokenBundle;
import org.aibles.intellihire.authservice.auth.token.JwtTokenService;
import org.aibles.intellihire.authservice.auth.token.RefreshTokenClaims;
import org.aibles.intellihire.authservice.auth.usecase.command.RefreshTokenCommand;
import org.aibles.intellihire.authservice.rbac.adapter.repository.AccountAuthorizationRepository;
import org.aibles.intellihire.authservice.shared.exception.BaseException;
import org.aibles.intellihire.authservice.shared.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenUseCaseImpl implements RefreshTokenUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AccountRepository accountRepository;
    private final AccountAuthorizationRepository accountAuthorizationRepository;
    private final JwtTokenService jwtTokenService;

    @Override
    @Transactional
    public LoginUseCase.LoginResponse execute(RefreshTokenCommand command) {
        log.debug("Processing refresh token request");

        // Parse and validate JWT
        RefreshTokenClaims claims = jwtTokenService.parseAndValidateRefreshToken(command.refreshToken());

        // Find refresh token in DB with lock for concurrency
        RefreshTokenEntity storedToken = refreshTokenRepository.findByTokenIdWithLock(claims.tokenId())
                .orElseThrow(() -> new BaseException(ErrorCode.REFRESH_TOKEN_NOT_FOUND) {});

        // Validate token
        if (storedToken.isRevoked()) {
            log.warn("Attempt to use revoked refresh token: {}", claims.tokenId());
            throw new BaseException(ErrorCode.REFRESH_TOKEN_REVOKED) {};
        }

        if (storedToken.isExpired()) {
            log.warn("Attempt to use expired refresh token: {}", claims.tokenId());
            throw new BaseException(ErrorCode.REFRESH_TOKEN_EXPIRED) {};
        }

        // Validate account association
        if (!storedToken.getAccountId().equals(claims.accountId())) {
            log.warn("Token account mismatch: stored={}, claimed={}",
                    storedToken.getAccountId(), claims.accountId());
            throw new BaseException(ErrorCode.INVALID_TOKEN) {};
        }

        // Get account
        AccountEntity account = accountRepository.findById(claims.accountId())
                .orElseThrow(AccountNotFoundException::new);

        // Check account is active
        if (!account.isActive()) {
            throw new BaseException(ErrorCode.ACCOUNT_INACTIVE) {};
        }

        // Get roles and permissions
        List<String> roles = accountAuthorizationRepository.findRolesByAccountId(account.getId());
        List<String> permissions = accountAuthorizationRepository.findPermissionsByAccountId(account.getId());

        // Issue new token pair
        JwtTokenBundle newTokenBundle = jwtTokenService.issueTokenPair(
                account.getId(),
                account.getEmail(),
                account.getUsername(),
                roles,
                permissions
        );

        // Revoke old token and save new one
        storedToken.revoke("rotation");
        refreshTokenRepository.save(storedToken);

        RefreshTokenEntity newRefreshToken = new RefreshTokenEntity();
        newRefreshToken.setTokenId(newTokenBundle.refreshTokenId());
        newRefreshToken.setAccountId(account.getId());
        newRefreshToken.setIpAddress(storedToken.getIpAddress());
        newRefreshToken.setDeviceInfo(storedToken.getDeviceInfo());
        newRefreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(newTokenBundle.refreshTokenExpiresIn()));
        refreshTokenRepository.save(newRefreshToken);

        log.info("Token refreshed for account: {}", account.getId());

        return new LoginUseCase.LoginResponse(
                newTokenBundle.accessToken(),
                newTokenBundle.refreshToken(),
                LoginUseCase.LoginResponse.TOKEN_TYPE_BEARER,
                newTokenBundle.accessTokenExpiresIn(),
                newTokenBundle.refreshTokenExpiresIn(),
                account.getId(),
                account.getEmail(),
                account.getUsername(),
                roles
        );
    }
}
