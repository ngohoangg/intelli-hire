package org.aibles.intellihire.authservice.admin.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.account.adapter.repository.AccountRepository;
import org.aibles.intellihire.authservice.account.domain.AccountNotFoundException;
import org.aibles.intellihire.authservice.admin.usecase.command.AssignRoleCommand;
import org.aibles.intellihire.authservice.rbac.adapter.repository.AccountRoleRepository;
import org.aibles.intellihire.authservice.rbac.adapter.repository.RoleRepository;
import org.aibles.intellihire.authservice.rbac.domain.AccountRoleEntity;
import org.aibles.intellihire.authservice.rbac.domain.RoleAlreadyAssignedException;
import org.aibles.intellihire.authservice.rbac.domain.RoleNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAssignRoleUseCaseImpl implements AdminAssignRoleUseCase {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final AccountRoleRepository accountRoleRepository;

    @Override
    @Transactional
    public void execute(AssignRoleCommand command) {
        log.debug("Admin assigning role {} to account {}", command.roleId(), command.accountId());

        // Validate account exists
        if (!accountRepository.existsById(command.accountId())) {
            throw new AccountNotFoundException();
        }

        // Validate role exists
        if (!roleRepository.existsById(command.roleId())) {
            throw new RoleNotFoundException();
        }

        // Check if already assigned
        if (accountRoleRepository.existsByAccountIdAndRoleId(command.accountId(), command.roleId())) {
            throw new RoleAlreadyAssignedException();
        }

        // Create assignment
        AccountRoleEntity accountRole = new AccountRoleEntity();
        accountRole.setAccountId(command.accountId());
        accountRole.setRoleId(command.roleId());
        accountRole.setExpiresAt(command.expiresAt());

        accountRoleRepository.save(accountRole);

        log.info("Admin assigned role {} to account {}", command.roleId(), command.accountId());
    }
}
