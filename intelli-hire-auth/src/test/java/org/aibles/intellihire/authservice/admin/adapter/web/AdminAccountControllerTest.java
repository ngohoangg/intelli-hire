package org.aibles.intellihire.authservice.admin.adapter.web;

import org.aibles.intellihire.authservice.account.domain.AccountEntity;
import org.aibles.intellihire.authservice.account.domain.AccountStatus;
import org.aibles.intellihire.authservice.admin.adapter.web.dto.*;
import org.aibles.intellihire.authservice.admin.usecase.*;
import org.aibles.intellihire.authservice.admin.usecase.command.*;
import org.aibles.intellihire.authservice.rbac.adapter.repository.AccountRoleRepository;
import org.aibles.intellihire.authservice.rbac.domain.AccountRoleEntity;
import org.aibles.intellihire.authservice.rbac.domain.RoleEntity;
import org.aibles.intellihire.authservice.shared.dto.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminAccountControllerTest {

    @Mock
    private AdminListAccountsUseCase adminListAccountsUseCase;

    @Mock
    private AdminGetAccountUseCase adminGetAccountUseCase;

    @Mock
    private AdminUpdateAccountStatusUseCase adminUpdateAccountStatusUseCase;

    @Mock
    private AdminGetAccountRolesUseCase adminGetAccountRolesUseCase;

    @Mock
    private AdminAssignRoleUseCase adminAssignRoleUseCase;

    @Mock
    private AdminRemoveRoleUseCase adminRemoveRoleUseCase;

    @Mock
    private AccountRoleRepository accountRoleRepository;

    @InjectMocks
    private AdminAccountController adminAccountController;

    @Test
    @DisplayName("Should return paginated accounts with isSuperAdmin mapped")
    void listAccounts_success() {
        AccountEntity account = new AccountEntity();
        account.setId(1L);
        account.setEmail("admin@example.com");
        account.setUsername("admin");
        account.setStatus(AccountStatus.ACTIVE);

        when(adminListAccountsUseCase.execute(any(ListAccountsCommand.class)))
                .thenReturn(new PageImpl<>(List.of(account)));
        when(accountRoleRepository.findAccountIdsWithSuperAdminRole()).thenReturn(Set.of(1L));

        ResponseEntity<ApiResponse<Page<AccountResponseDTO>>> response =
                adminAccountController.listAccounts(0, 20, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        Page<AccountResponseDTO> page = response.getBody().getData();
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).isSuperAdmin()).isTrue();
    }

    @Test
    @DisplayName("Should return account detail")
    void getAccount_success() {
        AccountEntity account = new AccountEntity();
        account.setId(2L);
        account.setEmail("user@example.com");
        account.setUsername("user");
        account.setStatus(AccountStatus.ACTIVE);

        when(adminGetAccountUseCase.execute(2L)).thenReturn(account);

        ResponseEntity<ApiResponse<AccountResponseDTO>> response = adminAccountController.getAccount(2L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Should update account status")
    void updateAccountStatus_success() {
        AccountEntity account = new AccountEntity();
        account.setId(2L);
        account.setStatus(AccountStatus.INACTIVE);

        when(adminUpdateAccountStatusUseCase.execute(any(UpdateAccountStatusCommand.class)))
                .thenReturn(account);

        UpdateAccountStatusRequestDTO request = new UpdateAccountStatusRequestDTO();
        request.setStatus(AccountStatus.INACTIVE);
        request.setReason("Violation of rules");

        ResponseEntity<ApiResponse<AccountResponseDTO>> response =
                adminAccountController.updateAccountStatus(2L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getStatus()).isEqualTo(AccountStatus.INACTIVE);
    }

    @Test
    @DisplayName("Should get account roles")
    void getAccountRoles_success() {
        RoleEntity role = new RoleEntity();
        role.setId(5L);
        role.setName("ROLE_ADMIN");

        AccountRoleEntity accountRole = new AccountRoleEntity();
        accountRole.setRoleId(5L);
        accountRole.setRole(role);

        when(adminGetAccountRolesUseCase.execute(1L)).thenReturn(List.of(accountRole));

        ResponseEntity<ApiResponse<List<AccountRoleResponseDTO>>> response =
                adminAccountController.getAccountRoles(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        List<AccountRoleResponseDTO> roles = response.getBody().getData();
        assertThat(roles).hasSize(1);
        assertThat(roles.get(0).getRoleName()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("Should assign role to account")
    void assignRole_success() {
        AssignRoleRequestDTO request = new AssignRoleRequestDTO();
        request.setRoleId(3L);

        ResponseEntity<ApiResponse<Void>> response = adminAccountController.assignRole(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(adminAssignRoleUseCase).execute(any(AssignRoleCommand.class));
    }

    @Test
    @DisplayName("Should remove role from account and return 204 No Content")
    void removeRole_success() {
        ResponseEntity<Void> response = adminAccountController.removeRole(1L, 3L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(adminRemoveRoleUseCase).execute(1L, 3L);
    }
}
