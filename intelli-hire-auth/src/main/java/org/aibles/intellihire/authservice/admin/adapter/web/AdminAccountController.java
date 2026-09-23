package org.aibles.intellihire.authservice.admin.adapter.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.account.domain.AccountEntity;
import org.aibles.intellihire.authservice.account.domain.AccountStatus;
import org.aibles.intellihire.authservice.admin.adapter.web.dto.*;
import org.aibles.intellihire.authservice.admin.usecase.*;
import org.aibles.intellihire.authservice.admin.usecase.command.*;
import org.aibles.intellihire.authservice.rbac.adapter.repository.AccountRoleRepository;
import org.aibles.intellihire.authservice.rbac.domain.AccountRoleEntity;
import org.aibles.intellihire.authservice.shared.dto.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/v1/admin/accounts")
@RequiredArgsConstructor
@Tag(name = "Admin - Accounts", description = "Admin account management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AdminAccountController {

    private final AdminListAccountsUseCase adminListAccountsUseCase;
    private final AdminGetAccountUseCase adminGetAccountUseCase;
    private final AdminUpdateAccountStatusUseCase adminUpdateAccountStatusUseCase;
    private final AdminGetAccountRolesUseCase adminGetAccountRolesUseCase;
    private final AdminAssignRoleUseCase adminAssignRoleUseCase;
    private final AdminRemoveRoleUseCase adminRemoveRoleUseCase;
    private final AccountRoleRepository accountRoleRepository;

    @GetMapping
    @Operation(summary = "List accounts", description = "Returns a paginated list of all accounts")
    public ResponseEntity<ApiResponse<Page<AccountResponseDTO>>> listAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) AccountStatus status,
            @RequestParam(required = false) String search
    ) {
        log.debug("Admin listing accounts: page={}, size={}", page, size);

        ListAccountsCommand command = new ListAccountsCommand(page, size, status, search);
        Page<AccountEntity> accounts = adminListAccountsUseCase.execute(command);
        Set<Long> superAdminIds = accountRoleRepository.findAccountIdsWithSuperAdminRole();

        return ResponseEntity.ok(ApiResponse.success(accounts.map(a -> toAccountResponseDTO(a, superAdminIds))));
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Get account", description = "Returns details of a specific account")
    public ResponseEntity<ApiResponse<AccountResponseDTO>> getAccount(@PathVariable Long accountId) {
        log.debug("Admin getting account: {}", accountId);

        AccountEntity account = adminGetAccountUseCase.execute(accountId);

        return ResponseEntity.ok(ApiResponse.success(toAccountResponseDTO(account)));
    }

    @PatchMapping("/{accountId}/status")
    @Operation(summary = "Update account status", description = "Updates the status of an account (lock/unlock/deactivate)")
    public ResponseEntity<ApiResponse<AccountResponseDTO>> updateAccountStatus(
            @PathVariable Long accountId,
            @Valid @RequestBody UpdateAccountStatusRequestDTO requestDTO
    ) {
        log.info("Admin updating account {} status to {}", accountId, requestDTO.getStatus());

        UpdateAccountStatusCommand command = new UpdateAccountStatusCommand(
                accountId,
                requestDTO.getStatus(),
                requestDTO.getReason()
        );

        AccountEntity account = adminUpdateAccountStatusUseCase.execute(command);

        return ResponseEntity.ok(ApiResponse.success(toAccountResponseDTO(account)));
    }

    @GetMapping("/{accountId}/roles")
    @Operation(summary = "Get account roles", description = "Returns roles assigned to an account")
    public ResponseEntity<ApiResponse<List<AccountRoleResponseDTO>>> getAccountRoles(@PathVariable Long accountId) {
        log.debug("Admin getting roles for account: {}", accountId);

        List<AccountRoleEntity> roles = adminGetAccountRolesUseCase.execute(accountId);

        List<AccountRoleResponseDTO> response = roles.stream()
                .map(this::toAccountRoleResponseDTO)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{accountId}/roles")
    @Operation(summary = "Assign role to account", description = "Assigns a role to an account")
    public ResponseEntity<ApiResponse<Void>> assignRole(
            @PathVariable Long accountId,
            @Valid @RequestBody AssignRoleRequestDTO requestDTO
    ) {
        log.info("Admin assigning role {} to account {}", requestDTO.getRoleId(), accountId);

        AssignRoleCommand command = new AssignRoleCommand(
                accountId,
                requestDTO.getRoleId(),
                requestDTO.getExpiresAt()
        );

        adminAssignRoleUseCase.execute(command);

        return ResponseEntity.ok(ApiResponse.success("Role assigned successfully."));
    }

    @DeleteMapping("/{accountId}/roles/{roleId}")
    @Operation(summary = "Remove role from account", description = "Removes a role from an account")
    public ResponseEntity<Void> removeRole(
            @PathVariable Long accountId,
            @PathVariable Long roleId
    ) {
        log.info("Admin removing role {} from account {}", roleId, accountId);

        adminRemoveRoleUseCase.execute(accountId, roleId);

        return ResponseEntity.noContent().build();
    }

    private AccountResponseDTO toAccountResponseDTO(AccountEntity account) {
        return toAccountResponseDTO(account, Set.of());
    }

    private AccountResponseDTO toAccountResponseDTO(AccountEntity account, Set<Long> superAdminIds) {
        return AccountResponseDTO.builder()
                .id(account.getId())
                .email(account.getEmail())
                .username(account.getUsername())
                .status(account.getStatus())
                .failedLoginAttempts(account.getFailedLoginAttempts())
                .lastLoginAt(account.getLastLoginAt())
                .lockedAt(account.getLockedAt())
                .lockedReason(account.getLockedReason())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .isSuperAdmin(superAdminIds.contains(account.getId()))
                .build();
    }

    private AccountRoleResponseDTO toAccountRoleResponseDTO(AccountRoleEntity accountRole) {
        return AccountRoleResponseDTO.builder()
                .roleId(accountRole.getRoleId())
                .roleName(accountRole.getRole() != null ? accountRole.getRole().getName() : null)
                .assignedAt(accountRole.getAssignedAt())
                .expiresAt(accountRole.getExpiresAt())
                .build();
    }
}
