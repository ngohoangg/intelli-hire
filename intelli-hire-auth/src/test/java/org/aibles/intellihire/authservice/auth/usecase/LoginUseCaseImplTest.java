package org.aibles.intellihire.authservice.auth.usecase;

import org.aibles.intellihire.authservice.account.adapter.repository.AccountRepository;
import org.aibles.intellihire.authservice.account.domain.AccountEntity;
import org.aibles.intellihire.authservice.account.domain.AccountStatus;
import org.aibles.intellihire.authservice.auth.adapter.repository.RefreshTokenRepository;
import org.aibles.intellihire.authservice.auth.domain.AccountLockedException;
import org.aibles.intellihire.authservice.auth.domain.InvalidCredentialsException;
import org.aibles.intellihire.authservice.auth.token.JwtTokenBundle;
import org.aibles.intellihire.authservice.auth.token.JwtTokenService;
import org.aibles.intellihire.authservice.auth.usecase.command.LoginCommand;
import org.aibles.intellihire.authservice.rbac.adapter.repository.AccountAuthorizationRepository;
import org.aibles.intellihire.authservice.shared.config.properties.PasswordProperties;
import org.aibles.intellihire.authservice.shared.service.SessionActivityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseImplTest {

    @Mock private AccountRepository accountRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private AccountAuthorizationRepository accountAuthorizationRepository;
    @Mock private JwtTokenService jwtTokenService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private PasswordProperties passwordProperties;
    @Mock private SessionActivityService sessionActivityService;

    @InjectMocks
    private LoginUseCaseImpl loginUseCase;

    private LoginCommand command;
    private AccountEntity account;

    @BeforeEach
    void setUp() {
        command = new LoginCommand("test@example.com", "Password1", "127.0.0.1", "Test-Agent");
        account = new AccountEntity();
        account.setId(1L);
        account.setEmail("test@example.com");
        account.setUsername("testuser");
        account.setPassword("encoded");
        account.setStatus(AccountStatus.ACTIVE);
    }

    @Test
    void execute_success() {
        when(accountRepository.findByEmailOrUsername(anyString())).thenReturn(Optional.of(account));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(accountAuthorizationRepository.findRolesByAccountId(1L)).thenReturn(List.of("USER"));
        when(accountAuthorizationRepository.findPermissionsByAccountId(1L)).thenReturn(List.of());
        when(jwtTokenService.issueTokenPair(any(), any(), any(), any(), any()))
                .thenReturn(new JwtTokenBundle("access", "refresh", "tid", 900, 604800));

        LoginUseCase.LoginResponse response = loginUseCase.execute(command);

        assertThat(response.accessToken()).isEqualTo("access");
        assertThat(response.accountId()).isEqualTo(1L);
        verify(refreshTokenRepository).save(any());
    }

    @Test
    void execute_success_createsRedisSession() {
        when(accountRepository.findByEmailOrUsername(anyString())).thenReturn(Optional.of(account));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(accountAuthorizationRepository.findRolesByAccountId(1L)).thenReturn(List.of("USER"));
        when(accountAuthorizationRepository.findPermissionsByAccountId(1L)).thenReturn(List.of());
        when(jwtTokenService.issueTokenPair(any(), any(), any(), any(), any()))
                .thenReturn(new JwtTokenBundle("access", "refresh", "tid", 900, 604800));

        loginUseCase.execute(command);

        verify(sessionActivityService).create(1L);
    }

    @Test
    void execute_wrongPassword_throwsException() {
        when(accountRepository.findByEmailOrUsername(anyString())).thenReturn(Optional.of(account));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        when(passwordProperties.getMaxFailedAttempts()).thenReturn(5);

        assertThatThrownBy(() -> loginUseCase.execute(command))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void execute_lockedAccount_throwsException() {
        account.setStatus(AccountStatus.LOCKED);
        when(accountRepository.findByEmailOrUsername(anyString())).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> loginUseCase.execute(command))
                .isInstanceOf(AccountLockedException.class);
    }

    @Test
    void execute_accountNotFound_throwsException() {
        when(accountRepository.findByEmailOrUsername(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginUseCase.execute(command))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
