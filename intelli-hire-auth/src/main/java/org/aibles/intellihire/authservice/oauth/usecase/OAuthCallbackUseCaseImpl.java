package org.aibles.intellihire.authservice.oauth.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.account.adapter.repository.AccountRepository;
import org.aibles.intellihire.authservice.account.domain.AccountEntity;
import org.aibles.intellihire.authservice.account.domain.AccountStatus;
import org.aibles.intellihire.authservice.auth.adapter.repository.RefreshTokenRepository;
import org.aibles.intellihire.authservice.auth.domain.AccountInactiveException;
import org.aibles.intellihire.authservice.auth.domain.AccountLockedException;
import org.aibles.intellihire.authservice.auth.domain.RefreshTokenEntity;
import org.aibles.intellihire.authservice.auth.token.JwtTokenBundle;
import org.aibles.intellihire.authservice.auth.token.JwtTokenService;
import org.aibles.intellihire.authservice.auth.usecase.LoginUseCase;
import org.aibles.intellihire.authservice.oauth.adapter.repository.AccountOAuthLinkRepository;
import org.aibles.intellihire.authservice.oauth.adapter.repository.OAuthProviderRepository;
import org.aibles.intellihire.authservice.oauth.client.OAuthClient;
import org.aibles.intellihire.authservice.oauth.client.OAuthClientFactory;
import org.aibles.intellihire.authservice.oauth.domain.*;
import org.aibles.intellihire.authservice.oauth.usecase.command.OAuthCallbackCommand;
import org.aibles.intellihire.authservice.profile.adapter.repository.UserProfileRepository;
import org.aibles.intellihire.authservice.profile.domain.UserProfileEntity;
import org.aibles.intellihire.authservice.rbac.adapter.repository.AccountAuthorizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthCallbackUseCaseImpl implements OAuthCallbackUseCase {

    private final OAuthProviderRepository oAuthProviderRepository;
    private final AccountOAuthLinkRepository accountOAuthLinkRepository;
    private final AccountRepository accountRepository;
    private final UserProfileRepository userProfileRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AccountAuthorizationRepository accountAuthorizationRepository;
    private final OAuthClientFactory oAuthClientFactory;
    private final JwtTokenService jwtTokenService;

    @Override
    @Transactional
    public LoginUseCase.LoginResponse execute(OAuthCallbackCommand command) {
        log.debug("Processing OAuth callback for provider: {}", command.providerCode());

        // 1. Get provider and validate
        OAuthProviderEntity provider = oAuthProviderRepository.findByCode(command.providerCode().toLowerCase())
                .orElseThrow(OAuthProviderNotFoundException::new);

        if (!provider.getIsActive()) {
            throw new OAuthProviderDisabledException();
        }

        // 2. Exchange code for user info
        OAuthClient client = oAuthClientFactory.getClient(command.providerCode());
        OAuthUserInfo userInfo = client.exchangeCodeForUserInfo(command.code());

        log.debug("Retrieved OAuth user info: providerUserId={}, email={}", userInfo.providerUserId(), userInfo.email());

        // 3. Check if OAuth link exists
        Optional<AccountOAuthLinkEntity> existingLink = accountOAuthLinkRepository
                .findByProviderIdAndProviderUserId(provider.getId(), userInfo.providerUserId());

        AccountEntity account;
        if (existingLink.isPresent()) {
            // Existing OAuth user - login
            account = handleExistingOAuthUser(existingLink.get());
        } else {
            // New OAuth user - check email and possibly register
            account = handleNewOAuthUser(provider, userInfo);
        }

        // 4. Validate account status
        validateAccountStatus(account);

        // 5. Record successful login
        account.recordSuccessfulLogin();
        accountRepository.save(account);

        // 6. Get roles and permissions
        List<String> roles = accountAuthorizationRepository.findRolesByAccountId(account.getId());
        List<String> permissions = accountAuthorizationRepository.findPermissionsByAccountId(account.getId());

        // 7. Issue tokens
        JwtTokenBundle tokenBundle = jwtTokenService.issueTokenPair(
                account.getId(),
                account.getEmail(),
                account.getUsername(),
                roles,
                permissions
        );

        // 8. Save refresh token
        saveRefreshToken(account.getId(), tokenBundle, command.deviceInfo(), command.ipAddress());

        log.info("OAuth login successful for account: {} via provider: {}", account.getId(), command.providerCode());

        return new LoginUseCase.LoginResponse(
                tokenBundle.accessToken(),
                tokenBundle.refreshToken(),
                LoginUseCase.LoginResponse.TOKEN_TYPE_BEARER,
                tokenBundle.accessTokenExpiresIn(),
                tokenBundle.refreshTokenExpiresIn(),
                account.getId(),
                account.getEmail(),
                account.getUsername(),
                roles
        );
    }

    private AccountEntity handleExistingOAuthUser(AccountOAuthLinkEntity link) {
        log.debug("Found existing OAuth link for account: {}", link.getAccountId());

        // Update last used timestamp
        link.updateLastUsedAt();
        accountOAuthLinkRepository.save(link);

        return accountRepository.findById(link.getAccountId())
                .orElseThrow(() -> {
                    log.error("Account not found for OAuth link: {}", link.getId());
                    return new OAuthCallbackException("Account not found");
                });
    }

    private AccountEntity handleNewOAuthUser(OAuthProviderEntity provider, OAuthUserInfo userInfo) {
        log.debug("New OAuth user, checking email: {}", userInfo.email());

        // Check if email already exists in the system
        Optional<AccountEntity> existingAccount = accountRepository.findByEmail(userInfo.email());

        if (existingAccount.isPresent()) {
            // Email exists - link OAuth to existing account
            AccountEntity account = existingAccount.get();
            log.info("Linking OAuth provider {} to existing account: {}", provider.getCode(), account.getId());

            createOAuthLink(account.getId(), provider.getId(), userInfo);
            return account;
        }

        // Create new account
        log.info("Creating new account via OAuth provider: {}", provider.getCode());
        return createNewOAuthAccount(provider, userInfo);
    }

    private AccountEntity createNewOAuthAccount(OAuthProviderEntity provider, OAuthUserInfo userInfo) {
        // Generate unique username from email or name
        String baseUsername = generateUsername(userInfo);
        String username = ensureUniqueUsername(baseUsername);

        // Create account (no password for OAuth-only accounts)
        AccountEntity account = new AccountEntity();
        account.setEmail(userInfo.email());
        account.setUsername(username);
        account.setPassword(null); // OAuth accounts may not have password
        account.setStatus(AccountStatus.ACTIVE);

        AccountEntity savedAccount = accountRepository.save(account);

        // Create profile
        createUserProfile(savedAccount.getId(), userInfo);

        // Assign default role
        accountAuthorizationRepository.assignDefaultRole(savedAccount.getId());

        // Create OAuth link
        createOAuthLink(savedAccount.getId(), provider.getId(), userInfo);

        log.info("Created new account {} via OAuth provider {}", savedAccount.getId(), provider.getCode());

        return savedAccount;
    }

    private void createUserProfile(Long accountId, OAuthUserInfo userInfo) {
        UserProfileEntity profile = new UserProfileEntity();
        profile.setAccountId(accountId);
        profile.setAvatarUrl(userInfo.avatarUrl());

        // Parse name if available
        if (userInfo.name() != null && !userInfo.name().isBlank()) {
            String[] nameParts = userInfo.name().trim().split("\\s+");
            if (nameParts.length >= 1) {
                profile.setFirstName(nameParts[0]);
            }
            if (nameParts.length >= 2) {
                profile.setLastName(nameParts[nameParts.length - 1]);
            }
            if (nameParts.length >= 3) {
                // Middle name(s)
                StringBuilder middleName = new StringBuilder();
                for (int i = 1; i < nameParts.length - 1; i++) {
                    if (i > 1) middleName.append(" ");
                    middleName.append(nameParts[i]);
                }
                profile.setMiddleName(middleName.toString());
            }
        }

        userProfileRepository.save(profile);
    }

    private void createOAuthLink(Long accountId, Long providerId, OAuthUserInfo userInfo) {
        AccountOAuthLinkEntity link = new AccountOAuthLinkEntity();
        link.setAccountId(accountId);
        link.setProviderId(providerId);
        link.setProviderUserId(userInfo.providerUserId());
        link.setProviderEmail(userInfo.email());

        accountOAuthLinkRepository.save(link);
    }

    private String generateUsername(OAuthUserInfo userInfo) {
        if (userInfo.email() != null) {
            return userInfo.email().split("@")[0];
        }
        if (userInfo.name() != null) {
            return userInfo.name().toLowerCase().replaceAll("\\s+", ".");
        }
        return "user" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String ensureUniqueUsername(String baseUsername) {
        String username = baseUsername;
        int suffix = 1;

        while (accountRepository.existsByUsername(username)) {
            username = baseUsername + suffix;
            suffix++;
        }

        return username;
    }

    private void validateAccountStatus(AccountEntity account) {
        if (account.isLocked()) {
            log.warn("Login attempt for locked account: {}", account.getId());
            throw new AccountLockedException();
        }
        if (account.isInactive()) {
            log.warn("Login attempt for inactive account: {}", account.getId());
            throw new AccountInactiveException();
        }
    }

    private void saveRefreshToken(Long accountId, JwtTokenBundle tokenBundle, String deviceInfo, String ipAddress) {
        RefreshTokenEntity refreshToken = new RefreshTokenEntity();
        refreshToken.setTokenId(tokenBundle.refreshTokenId());
        refreshToken.setAccountId(accountId);
        refreshToken.setDeviceInfo(deviceInfo);
        refreshToken.setIpAddress(ipAddress);
        refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(tokenBundle.refreshTokenExpiresIn()));

        refreshTokenRepository.save(refreshToken);
    }
}
