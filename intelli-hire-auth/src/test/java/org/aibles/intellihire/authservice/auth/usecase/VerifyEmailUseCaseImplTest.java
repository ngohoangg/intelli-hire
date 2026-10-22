package org.aibles.intellihire.authservice.auth.usecase;

import org.aibles.intellihire.authservice.account.adapter.repository.AccountRepository;
import org.aibles.intellihire.authservice.account.domain.AccountEntity;
import org.aibles.intellihire.authservice.account.domain.AccountNotFoundException;
import org.aibles.intellihire.authservice.auth.adapter.repository.EmailVerificationTokenRepository;
import org.aibles.intellihire.authservice.auth.domain.EmailVerificationTokenEntity;
import org.aibles.intellihire.authservice.auth.usecase.command.VerifyEmailCommand;
import org.aibles.intellihire.authservice.shared.exception.BaseException;
import org.aibles.intellihire.authservice.shared.util.SecureTokenGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerifyEmailUseCaseImplTest {

    @Mock
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private VerifyEmailUseCaseImpl verifyEmailUseCase;

    private String rawToken;
    private String tokenHash;

    @BeforeEach
    void setUp() {
        rawToken = "valid-token-string";
        tokenHash = SecureTokenGenerator.hashToken(rawToken);
    }

    @Test
    void execute_success() {
        EmailVerificationTokenEntity tokenEntity = new EmailVerificationTokenEntity();
        tokenEntity.setId(10L);
        tokenEntity.setAccountId(1L);
        tokenEntity.setTokenHash(tokenHash);
        tokenEntity.setEmail("test@example.com");
        tokenEntity.setExpiresAt(LocalDateTime.now().plusHours(12));

        AccountEntity account = new AccountEntity();
        account.setId(1L);
        account.setEmail("test@example.com");

        when(emailVerificationTokenRepository.findValidToken(eq(tokenHash), any(LocalDateTime.class)))
                .thenReturn(Optional.of(tokenEntity));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        VerifyEmailUseCase.VerifyEmailResponse response = verifyEmailUseCase.execute(new VerifyEmailCommand(rawToken));

        assertThat(response.message()).isEqualTo("Email verified successfully.");
        assertThat(tokenEntity.isVerified()).isTrue();
        verify(emailVerificationTokenRepository).save(tokenEntity);
    }

    @Test
    void execute_invalidOrExpiredToken_throwsException() {
        when(emailVerificationTokenRepository.findValidToken(eq(tokenHash), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> verifyEmailUseCase.execute(new VerifyEmailCommand(rawToken)))
                .isInstanceOf(BaseException.class);
    }

    @Test
    void execute_accountNotFound_throwsException() {
        EmailVerificationTokenEntity tokenEntity = new EmailVerificationTokenEntity();
        tokenEntity.setId(10L);
        tokenEntity.setAccountId(999L);
        tokenEntity.setTokenHash(tokenHash);
        tokenEntity.setEmail("notfound@example.com");
        tokenEntity.setExpiresAt(LocalDateTime.now().plusHours(12));

        when(emailVerificationTokenRepository.findValidToken(eq(tokenHash), any(LocalDateTime.class)))
                .thenReturn(Optional.of(tokenEntity));
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> verifyEmailUseCase.execute(new VerifyEmailCommand(rawToken)))
                .isInstanceOf(AccountNotFoundException.class);
    }
}
