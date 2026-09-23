package org.aibles.intellihire.authservice.auth.usecase;

import org.aibles.intellihire.authservice.account.adapter.repository.AccountRepository;
import org.aibles.intellihire.authservice.account.domain.AccountEntity;
import org.aibles.intellihire.authservice.auth.adapter.repository.PasswordResetTokenRepository;
import org.aibles.intellihire.authservice.auth.usecase.command.ForgotPasswordCommand;
import org.aibles.intellihire.authservice.shared.email.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ForgotPasswordUseCaseImplTest {

    @Mock private AccountRepository accountRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private EmailService emailService;

    @InjectMocks
    private ForgotPasswordUseCaseImpl forgotPasswordUseCase;

    @Test
    void execute_success_sendsEmail() {
        AccountEntity account = new AccountEntity();
        account.setId(1L);
        account.setEmail("test@example.com");

        when(accountRepository.findByEmail("test@example.com")).thenReturn(Optional.of(account));

        ForgotPasswordCommand command = new ForgotPasswordCommand("test@example.com", "127.0.0.1");
        ForgotPasswordUseCase.ForgotPasswordResponse response = forgotPasswordUseCase.execute(command);

        assertThat(response.message()).contains("password reset link has been sent");
        verify(emailService).sendPasswordResetEmail(eq("test@example.com"), anyString());
        verify(passwordResetTokenRepository).save(any());
    }

    @Test
    void execute_emailNotFound_returnsSuccessWithoutSendingEmail() {
        when(accountRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        ForgotPasswordCommand command = new ForgotPasswordCommand("unknown@example.com", "127.0.0.1");
        ForgotPasswordUseCase.ForgotPasswordResponse response = forgotPasswordUseCase.execute(command);

        assertThat(response.message()).contains("password reset link has been sent");
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }
}
