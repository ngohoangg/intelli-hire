package org.aibles.intellihire.authservice.auth.usecase;

import org.aibles.intellihire.authservice.account.adapter.repository.AccountRepository;
import org.aibles.intellihire.authservice.account.domain.AccountEntity;
import org.aibles.intellihire.authservice.account.domain.AccountStatus;
import org.aibles.intellihire.authservice.auth.adapter.repository.RefreshTokenRepository;
import org.aibles.intellihire.authservice.auth.domain.RefreshTokenEntity;
import org.aibles.intellihire.authservice.auth.token.JwtTokenBundle;
import org.aibles.intellihire.authservice.auth.token.JwtTokenService;
import org.aibles.intellihire.authservice.auth.token.RefreshTokenClaims;
import org.aibles.intellihire.authservice.auth.usecase.command.RefreshTokenCommand;
import org.aibles.intellihire.authservice.rbac.adapter.repository.AccountAuthorizationRepository;
import org.aibles.intellihire.authservice.shared.exception.BaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenUseCaseImplTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private AccountAuthorizationRepository accountAuthorizationRepository;
    @Mock private JwtTokenService jwtTokenService;

    @InjectMocks
    private RefreshTokenUseCaseImpl refreshTokenUseCase;

    private RefreshTokenCommand command;
    private RefreshTokenEntity storedToken;
    private AccountEntity account;

    @BeforeEach
    void setUp() {
        command = new RefreshTokenCommand("jwt-refresh-token");

        storedToken = new RefreshTokenEntity();
        storedToken.setTokenId("token-id");
        storedToken.setAccountId(1L);
        storedToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        storedToken.setIpAddress("127.0.0.1");
        storedToken.setDeviceInfo("Test-Agent");

        account = new AccountEntity();
        account.setId(1L);
        account.setEmail("test@example.com");
        account.setUsername("testuser");
        account.setStatus(AccountStatus.ACTIVE);
    }

    @Test
    void execute_success() {
        when(jwtTokenService.parseAndValidateRefreshToken(anyString()))
                .thenReturn(new RefreshTokenClaims("token-id", 1L, "test@example.com"));
        when(refreshTokenRepository.findByTokenIdWithLock("token-id")).thenReturn(Optional.of(storedToken));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountAuthorizationRepository.findRolesByAccountId(1L)).thenReturn(List.of("USER"));
        when(accountAuthorizationRepository.findPermissionsByAccountId(1L)).thenReturn(List.of());
        when(jwtTokenService.issueTokenPair(any(), any(), any(), any(), any()))
                .thenReturn(new JwtTokenBundle("new-access", "new-refresh", "new-tid", 900, 604800));

        LoginUseCase.LoginResponse response = refreshTokenUseCase.execute(command);

        assertThat(response.accessToken()).isEqualTo("new-access");
        verify(refreshTokenRepository, times(2)).save(any()); // revoke old + save new
    }

    @Test
    void execute_expiredToken_throwsException() {
        storedToken.setExpiresAt(LocalDateTime.now().minusDays(1));

        when(jwtTokenService.parseAndValidateRefreshToken(anyString()))
                .thenReturn(new RefreshTokenClaims("token-id", 1L, "test@example.com"));
        when(refreshTokenRepository.findByTokenIdWithLock("token-id")).thenReturn(Optional.of(storedToken));

        assertThatThrownBy(() -> refreshTokenUseCase.execute(command))
                .isInstanceOf(BaseException.class);
    }

    @Test
    void execute_revokedToken_throwsException() {
        storedToken.revoke("test");

        when(jwtTokenService.parseAndValidateRefreshToken(anyString()))
                .thenReturn(new RefreshTokenClaims("token-id", 1L, "test@example.com"));
        when(refreshTokenRepository.findByTokenIdWithLock("token-id")).thenReturn(Optional.of(storedToken));

        assertThatThrownBy(() -> refreshTokenUseCase.execute(command))
                .isInstanceOf(BaseException.class);
    }
}
