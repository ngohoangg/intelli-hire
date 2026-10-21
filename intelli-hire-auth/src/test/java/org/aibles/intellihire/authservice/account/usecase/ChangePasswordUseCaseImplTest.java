package org.aibles.intellihire.authservice.account.usecase;

import org.aibles.intellihire.authservice.account.adapter.repository.AccountRepository;
import org.aibles.intellihire.authservice.account.domain.AccountEntity;
import org.aibles.intellihire.authservice.account.domain.AccountNotFoundException;
import org.aibles.intellihire.authservice.account.usecase.command.ChangePasswordCommand;
import org.aibles.intellihire.authservice.auth.domain.InvalidCredentialsException;
import org.aibles.intellihire.authservice.shared.exception.BaseException;
import org.aibles.intellihire.authservice.shared.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangePasswordUseCaseImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ChangePasswordUseCaseImpl changePasswordUseCase;

    @Test
    @DisplayName("Should change password successfully when inputs are valid")
    void changePassword_success() {
        Long accountId = 1L;
        String currentPass = "OldPassword123!";
        String newPass = "NewPassword123!";
        String encodedNewPass = "encoded-new-pass";

        AccountEntity account = new AccountEntity();
        account.setId(accountId);
        account.setPassword("encoded-old-pass");

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches(currentPass, "encoded-old-pass")).thenReturn(true);
        when(passwordEncoder.matches(newPass, "encoded-old-pass")).thenReturn(false);
        when(passwordEncoder.encode(newPass)).thenReturn(encodedNewPass);
        when(accountRepository.save(any(AccountEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChangePasswordCommand command = new ChangePasswordCommand(accountId, currentPass, newPass, newPass);

        changePasswordUseCase.execute(command);

        assertThat(account.getPassword()).isEqualTo(encodedNewPass);
        verify(accountRepository).save(account);
    }

    @Test
    @DisplayName("Should throw BaseException with PASSWORD_MISMATCH when new password and confirm do not match")
    void changePassword_mismatch() {
        ChangePasswordCommand command = new ChangePasswordCommand(1L, "Old123!", "New123!", "Different123!");

        assertThatThrownBy(() -> changePasswordUseCase.execute(command))
                .isInstanceOf(BaseException.class)
                .satisfies(ex -> assertThat(((BaseException) ex).getErrorCode()).isEqualTo(ErrorCode.PASSWORD_MISMATCH));

        verifyNoInteractions(accountRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Should throw AccountNotFoundException when account does not exist")
    void changePassword_accountNotFound() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        ChangePasswordCommand command = new ChangePasswordCommand(999L, "Old123!", "New123!", "New123!");

        assertThatThrownBy(() -> changePasswordUseCase.execute(command))
                .isInstanceOf(AccountNotFoundException.class);

        verify(accountRepository).findById(999L);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when current password is wrong")
    void changePassword_wrongCurrentPassword() {
        Long accountId = 1L;
        AccountEntity account = new AccountEntity();
        account.setId(accountId);
        account.setPassword("encoded-pass");

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("WrongPass123!", "encoded-pass")).thenReturn(false);

        ChangePasswordCommand command = new ChangePasswordCommand(accountId, "WrongPass123!", "New123!", "New123!");

        assertThatThrownBy(() -> changePasswordUseCase.execute(command))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BaseException with SAME_PASSWORD when new password matches current password")
    void changePassword_samePassword() {
        Long accountId = 1L;
        String currentPass = "Password123!";
        AccountEntity account = new AccountEntity();
        account.setId(accountId);
        account.setPassword("encoded-pass");

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches(currentPass, "encoded-pass")).thenReturn(true);

        ChangePasswordCommand command = new ChangePasswordCommand(accountId, currentPass, currentPass, currentPass);

        assertThatThrownBy(() -> changePasswordUseCase.execute(command))
                .isInstanceOf(BaseException.class)
                .satisfies(ex -> assertThat(((BaseException) ex).getErrorCode()).isEqualTo(ErrorCode.SAME_PASSWORD));

        verify(accountRepository, never()).save(any());
    }
}
