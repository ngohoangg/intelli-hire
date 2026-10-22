package org.aibles.intellihire.authservice.admin.usecase;

import org.aibles.intellihire.authservice.rbac.adapter.repository.AccountRoleRepository;
import org.aibles.intellihire.authservice.rbac.domain.RoleNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminRemoveRoleUseCaseImplTest {

    @Mock
    private AccountRoleRepository accountRoleRepository;

    @InjectMocks
    private AdminRemoveRoleUseCaseImpl adminRemoveRoleUseCase;

    @Test
    @DisplayName("Should remove role from account when assigned")
    void execute_success() {
        when(accountRoleRepository.existsByAccountIdAndRoleId(1L, 2L)).thenReturn(true);

        adminRemoveRoleUseCase.execute(1L, 2L);

        verify(accountRoleRepository).deleteByAccountIdAndRoleId(1L, 2L);
    }

    @Test
    @DisplayName("Should throw RoleNotFoundException when assignment does not exist")
    void execute_notFound() {
        when(accountRoleRepository.existsByAccountIdAndRoleId(1L, 999L)).thenReturn(false);

        assertThatThrownBy(() -> adminRemoveRoleUseCase.execute(1L, 999L))
                .isInstanceOf(RoleNotFoundException.class);

        verify(accountRoleRepository, never()).deleteByAccountIdAndRoleId(anyLong(), anyLong());
    }
}
