package org.aibles.intellihire.authservice.account.usecase;

import org.aibles.intellihire.authservice.account.adapter.repository.AccountRepository;
import org.aibles.intellihire.authservice.account.domain.AccountEntity;
import org.aibles.intellihire.authservice.account.domain.AccountNotFoundException;
import org.aibles.intellihire.authservice.account.usecase.command.DeleteAccountCommand;
import org.aibles.intellihire.authservice.auth.adapter.repository.RefreshTokenRepository;
import org.aibles.intellihire.authservice.auth.domain.InvalidCredentialsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteAccountUseCaseImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DeleteAccountUseCaseImpl deleteAccountUseCase;

    @Test
    @DisplayName("Should successfully delete account and revoke all refresh tokens when password is correct")
    void deleteAccount_success() {
        Long accountId = 1L;
        String password = "SecretPassword123!";

        AccountEntity account = new AccountEntity();
        account.setId(accountId);
        account.setPassword("encoded-pass");

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches(password, "encoded-pass")).thenReturn(true);

        DeleteAccountCommand command = new DeleteAccountCommand(accountId, password);

        deleteAccountUseCase.execute(command);

        verify(refreshTokenRepository).revokeAllByAccountId(eq(accountId), any(LocalDateTime.class), eq("account_deleted"));
        verify(accountRepository).delete(account);
    }

    @Test
    @DisplayName("Should throw AccountNotFoundException when account does not exist")
    void deleteAccount_notFound() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        DeleteAccountCommand command = new DeleteAccountCommand(999L, "anyPassword");

        assertThatThrownBy(() -> deleteAccountUseCase.execute(command))
                .isInstanceOf(AccountNotFoundException.class);

        verify(accountRepository).findById(999L);
        verifyNoInteractions(passwordEncoder, refreshTokenRepository);
        verify(accountRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when password does not match")
    void deleteAccount_wrongPassword() {
        Long accountId = 1L;
        AccountEntity account = new AccountEntity();
        account.setId(accountId);
        account.setPassword("encoded-pass");

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("wrong-pass", "encoded-pass")).thenReturn(false);

        DeleteAccountCommand command = new DeleteAccountCommand(accountId, "wrong-pass");

        assertThatThrownBy(() -> deleteAccountUseCase.execute(command))
                .isInstanceOf(InvalidCredentialsException.class);

        verifyNoInteractions(refreshTokenRepository);
        verify(accountRepository, never()).delete(any());
    }
}
