package org.aibles.intellihire.authservice.admin.usecase;

import org.aibles.intellihire.authservice.account.adapter.repository.AccountRepository;
import org.aibles.intellihire.authservice.account.domain.AccountNotFoundException;
import org.aibles.intellihire.authservice.admin.usecase.command.AssignRoleCommand;
import org.aibles.intellihire.authservice.rbac.adapter.repository.AccountRoleRepository;
import org.aibles.intellihire.authservice.rbac.adapter.repository.RoleRepository;
import org.aibles.intellihire.authservice.rbac.domain.AccountRoleEntity;
import org.aibles.intellihire.authservice.rbac.domain.RoleAlreadyAssignedException;
import org.aibles.intellihire.authservice.rbac.domain.RoleNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminAssignRoleUseCaseImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AccountRoleRepository accountRoleRepository;

    @InjectMocks
    private AdminAssignRoleUseCaseImpl adminAssignRoleUseCase;

    @Test
    @DisplayName("Should assign role to account successfully")
    void execute_success() {
        Long accountId = 1L;
        Long roleId = 2L;
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(30);

        when(accountRepository.existsById(accountId)).thenReturn(true);
        when(roleRepository.existsById(roleId)).thenReturn(true);
        when(accountRoleRepository.existsByAccountIdAndRoleId(accountId, roleId)).thenReturn(false);

        AssignRoleCommand command = new AssignRoleCommand(accountId, roleId, expiresAt);
        adminAssignRoleUseCase.execute(command);

        ArgumentCaptor<AccountRoleEntity> captor = ArgumentCaptor.forClass(AccountRoleEntity.class);
        verify(accountRoleRepository).save(captor.capture());

        AccountRoleEntity saved = captor.getValue();
        assertThat(saved.getAccountId()).isEqualTo(accountId);
        assertThat(saved.getRoleId()).isEqualTo(roleId);
        assertThat(saved.getExpiresAt()).isEqualTo(expiresAt);
    }

    @Test
    @DisplayName("Should throw AccountNotFoundException when account does not exist")
    void execute_accountNotFound() {
        when(accountRepository.existsById(999L)).thenReturn(false);

        AssignRoleCommand command = new AssignRoleCommand(999L, 2L, null);

        assertThatThrownBy(() -> adminAssignRoleUseCase.execute(command))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw RoleNotFoundException when role does not exist")
    void execute_roleNotFound() {
        when(accountRepository.existsById(1L)).thenReturn(true);
        when(roleRepository.existsById(888L)).thenReturn(false);

        AssignRoleCommand command = new AssignRoleCommand(1L, 888L, null);

        assertThatThrownBy(() -> adminAssignRoleUseCase.execute(command))
                .isInstanceOf(RoleNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw RoleAlreadyAssignedException when role is already assigned")
    void execute_alreadyAssigned() {
        when(accountRepository.existsById(1L)).thenReturn(true);
        when(roleRepository.existsById(2L)).thenReturn(true);
        when(accountRoleRepository.existsByAccountIdAndRoleId(1L, 2L)).thenReturn(true);

        AssignRoleCommand command = new AssignRoleCommand(1L, 2L, null);

        assertThatThrownBy(() -> adminAssignRoleUseCase.execute(command))
                .isInstanceOf(RoleAlreadyAssignedException.class);

        verify(accountRoleRepository, never()).save(any());
    }
}
