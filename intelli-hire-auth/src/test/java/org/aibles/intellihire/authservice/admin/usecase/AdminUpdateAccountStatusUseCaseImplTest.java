package org.aibles.intellihire.authservice.admin.usecase;

import org.aibles.intellihire.authservice.account.adapter.repository.AccountRepository;
import org.aibles.intellihire.authservice.account.domain.AccountEntity;
import org.aibles.intellihire.authservice.account.domain.AccountNotFoundException;
import org.aibles.intellihire.authservice.account.domain.AccountStatus;
import org.aibles.intellihire.authservice.admin.usecase.command.UpdateAccountStatusCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUpdateAccountStatusUseCaseImplTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AdminUpdateAccountStatusUseCaseImpl adminUpdateAccountStatusUseCase;

    @Test
    @DisplayName("Should lock account when status is LOCKED")
    void execute_lockAccount() {
        AccountEntity account = new AccountEntity();
        account.setId(1L);
        account.setStatus(AccountStatus.ACTIVE);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(AccountEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateAccountStatusCommand command = new UpdateAccountStatusCommand(1L, AccountStatus.LOCKED, "Suspicious activity");
        AccountEntity updated = adminUpdateAccountStatusUseCase.execute(command);

        assertThat(updated.getStatus()).isEqualTo(AccountStatus.LOCKED);
        assertThat(updated.isLocked()).isTrue();
        assertThat(updated.getLockedReason()).isEqualTo("Suspicious activity");
        verify(accountRepository).save(account);
    }

    @Test
    @DisplayName("Should unlock account when status changes from LOCKED to ACTIVE")
    void execute_unlockAccount() {
        AccountEntity account = new AccountEntity();
        account.setId(1L);
        account.setStatus(AccountStatus.LOCKED);
        account.lock("Previous reason");

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(AccountEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateAccountStatusCommand command = new UpdateAccountStatusCommand(1L, AccountStatus.ACTIVE, null);
        AccountEntity updated = adminUpdateAccountStatusUseCase.execute(command);

        assertThat(updated.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(updated.isLocked()).isFalse();
        assertThat(updated.getLockedAt()).isNull();
        verify(accountRepository).save(account);
    }

    @Test
    @DisplayName("Should throw AccountNotFoundException when account does not exist")
    void execute_notFound() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        UpdateAccountStatusCommand command = new UpdateAccountStatusCommand(999L, AccountStatus.INACTIVE, null);

        assertThatThrownBy(() -> adminUpdateAccountStatusUseCase.execute(command))
                .isInstanceOf(AccountNotFoundException.class);
    }
}
