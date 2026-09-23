package org.aibles.intellihire.authservice.auth.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.account.adapter.repository.AccountRepository;
import org.aibles.intellihire.authservice.account.domain.AccountEntity;
import org.aibles.intellihire.authservice.auth.adapter.repository.RefreshTokenRepository;
import org.aibles.intellihire.authservice.auth.domain.AccountLockedException;
import org.aibles.intellihire.authservice.auth.domain.InvalidCredentialsException;
import org.aibles.intellihire.authservice.auth.domain.RefreshTokenEntity;
import org.aibles.intellihire.authservice.auth.token.JwtTokenBundle;
import org.aibles.intellihire.authservice.auth.token.JwtTokenService;
import org.aibles.intellihire.authservice.auth.usecase.command.LoginCommand;
import org.aibles.intellihire.authservice.rbac.adapter.repository.AccountAuthorizationRepository;
import org.aibles.intellihire.authservice.shared.config.properties.PasswordProperties;
import org.aibles.intellihire.authservice.shared.exception.BaseException;
import org.aibles.intellihire.authservice.shared.exception.ErrorCode;
import org.aibles.intellihire.authservice.shared.service.SessionActivityService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginUseCaseImpl implements LoginUseCase {

    private final AccountRepository accountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AccountAuthorizationRepository accountAuthorizationRepository;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordProperties passwordProperties;
    private final SessionActivityService sessionActivityService;

    @Override
    @Transactional
    public LoginResponse execute(LoginCommand command) {
        log.debug("Processing login for identifier: {}", command.identifier());

        // Find account by email or username
        String identifier = command.identifier().toLowerCase().trim();
        AccountEntity account = accountRepository.findByEmailOrUsername(identifier)
                .orElseThrow(InvalidCredentialsException::new);

        // Check account status
        if (account.isLocked()) {
            log.warn("Login attempt on locked account: {}", account.getId());
            throw new AccountLockedException(account.getLockedReason());
        }

        if (account.isInactive()) {
            log.warn("Login attempt on inactive account: {}", account.getId());
            throw new BaseException(ErrorCode.ACCOUNT_INACTIVE) {};
        }

        // Validate password
        if (account.getPassword() == null || !passwordEncoder.matches(command.password(), account.getPassword())) {
            handleFailedLogin(account);
            throw new InvalidCredentialsException();
        }

        // Successful login
        account.recordSuccessfulLogin();
        accountRepository.save(account);

        // Get roles and permissions
        List<String> roles = accountAuthorizationRepository.findRolesByAccountId(account.getId());
        List<String> permissions = accountAuthorizationRepository.findPermissionsByAccountId(account.getId());

        // Issue tokens
        JwtTokenBundle tokenBundle = jwtTokenService.issueTokenPair(
                account.getId(),
                account.getEmail(),
                account.getUsername(),
                roles,
                permissions
        );

        // Save refresh token
        saveRefreshToken(account.getId(), tokenBundle, command.ipAddress(), command.userAgent());

        sessionActivityService.create(account.getId());

        log.info("Successful login for account: {}", account.getId());

        return new LoginResponse(
                tokenBundle.accessToken(),
                tokenBundle.refreshToken(),
                LoginResponse.TOKEN_TYPE_BEARER,
                tokenBundle.accessTokenExpiresIn(),
                tokenBundle.refreshTokenExpiresIn(),
                account.getId(),
                account.getEmail(),
                account.getUsername(),
                roles
        );
    }

    private void handleFailedLogin(AccountEntity account) {
        account.incrementFailedLoginAttempts();

        if (account.getFailedLoginAttempts() >= passwordProperties.getMaxFailedAttempts()) {
            account.lock("Too many failed login attempts");
            log.warn("Account locked due to failed attempts: {}", account.getId());
        }

        accountRepository.save(account);
    }

    private void saveRefreshToken(Long accountId, JwtTokenBundle tokenBundle, String ipAddress, String userAgent) {
        RefreshTokenEntity refreshToken = new RefreshTokenEntity();
        refreshToken.setTokenId(tokenBundle.refreshTokenId());
        refreshToken.setAccountId(accountId);
        refreshToken.setIpAddress(ipAddress);
        refreshToken.setDeviceInfo(userAgent);
        refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(tokenBundle.refreshTokenExpiresIn()));
        refreshTokenRepository.save(refreshToken);
    }
}
