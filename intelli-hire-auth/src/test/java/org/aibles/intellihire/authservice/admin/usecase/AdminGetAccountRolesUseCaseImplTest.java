package org.aibles.intellihire.authservice.admin.usecase;

import org.aibles.intellihire.authservice.account.adapter.repository.AccountRepository;
import org.aibles.intellihire.authservice.account.domain.AccountNotFoundException;
import org.aibles.intellihire.authservice.rbac.adapter.repository.AccountRoleRepository;
import org.aibles.intellihire.authservice.rbac.domain.AccountRoleEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminGetAccountRolesUseCaseImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountRoleRepository accountRoleRepository;

    @InjectMocks
    private AdminGetAccountRolesUseCaseImpl adminGetAccountRolesUseCase;

    @Test
    @DisplayName("Should return account roles when account exists")
    void execute_success() {
        when(accountRepository.existsById(1L)).thenReturn(true);

        AccountRoleEntity role = new AccountRoleEntity();
        role.setId(10L);
        role.setAccountId(1L);
        role.setRoleId(2L);

        when(accountRoleRepository.findByAccountIdWithRole(1L)).thenReturn(List.of(role));

        List<AccountRoleEntity> result = adminGetAccountRolesUseCase.execute(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRoleId()).isEqualTo(2L);
        verify(accountRepository).existsById(1L);
        verify(accountRoleRepository).findByAccountIdWithRole(1L);
    }

    @Test
    @DisplayName("Should throw AccountNotFoundException when account does not exist")
    void execute_accountNotFound() {
        when(accountRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> adminGetAccountRolesUseCase.execute(999L))
                .isInstanceOf(AccountNotFoundException.class);
    }
}
