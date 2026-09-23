package org.aibles.intellihire.authservice.admin.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.rbac.adapter.repository.AccountRoleRepository;
import org.aibles.intellihire.authservice.rbac.domain.RoleNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminRemoveRoleUseCaseImpl implements AdminRemoveRoleUseCase {

    private final AccountRoleRepository accountRoleRepository;

    @Override
    @Transactional
    public void execute(Long accountId, Long roleId) {
        log.debug("Admin removing role {} from account {}", roleId, accountId);

        if (!accountRoleRepository.existsByAccountIdAndRoleId(accountId, roleId)) {
            throw new RoleNotFoundException();
        }

        accountRoleRepository.deleteByAccountIdAndRoleId(accountId, roleId);

        log.info("Admin removed role {} from account {}", roleId, accountId);
    }
}
