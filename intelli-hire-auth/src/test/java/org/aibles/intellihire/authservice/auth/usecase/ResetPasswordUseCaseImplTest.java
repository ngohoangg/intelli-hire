package org.aibles.intellihire.authservice.auth.usecase;

import org.aibles.intellihire.authservice.account.adapter.repository.AccountRepository;
import org.aibles.intellihire.authservice.account.domain.AccountEntity;
import org.aibles.intellihire.authservice.auth.adapter.repository.PasswordResetTokenRepository;
import org.aibles.intellihire.authservice.auth.adapter.repository.RefreshTokenRepository;
import org.aibles.intellihire.authservice.auth.domain.PasswordResetTokenEntity;
import org.aibles.intellihire.authservice.auth.usecase.command.ResetPasswordCommand;
import org.aibles.intellihire.authservice.shared.exception.BaseException;
import org.aibles.intellihire.authservice.shared.util.SecureTokenGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResetPasswordUseCaseImplTest {

    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ResetPasswordUseCaseImpl resetPasswordUseCase;

    private String rawToken;
    private String tokenHash;

    @BeforeEach
    void setUp() {
        rawToken = "valid-reset-token";
        tokenHash = SecureTokenGenerator.hashToken(rawToken);
    }

    @Test
    void execute_success() {
        PasswordResetTokenEntity token = new PasswordResetTokenEntity();
        token.setId(10L);
        token.setAccountId(1L);
        token.setTokenHash(tokenHash);
        token.setExpiresAt(LocalDateTime.now().plusHours(1));

        AccountEntity account = new AccountEntity();
        account.setId(1L);
        account.setPassword("oldEncoded");

        when(passwordResetTokenRepository.findValidToken(eq(tokenHash), any(LocalDateTime.class)))
                .thenReturn(Optional.of(token));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(passwordEncoder.encode("NewPassword1")).thenReturn("newEncoded");

        ResetPasswordUseCase.ResetPasswordResponse response =
                resetPasswordUseCase.execute(new ResetPasswordCommand(rawToken, "NewPassword1"));

        assertThat(response.message()).contains("Password has been reset successfully");
        assertThat(token.isUsed()).isTrue();
        assertThat(account.getPassword()).isEqualTo("newEncoded");
        verify(accountRepository).save(account);
        verify(passwordResetTokenRepository).save(token);
        verify(refreshTokenRepository).revokeAllByAccountId(eq(1L), any(LocalDateTime.class), eq("password_reset"));
    }

    @Test
    void execute_invalidToken_throwsException() {
        when(passwordResetTokenRepository.findValidToken(eq(tokenHash), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> resetPasswordUseCase.execute(new ResetPasswordCommand(rawToken, "NewPassword1")))
                .isInstanceOf(BaseException.class);
    }
}
