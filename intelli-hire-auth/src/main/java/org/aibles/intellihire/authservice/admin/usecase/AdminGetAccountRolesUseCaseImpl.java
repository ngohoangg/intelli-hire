package org.aibles.intellihire.authservice.admin.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.account.adapter.repository.AccountRepository;
import org.aibles.intellihire.authservice.account.domain.AccountNotFoundException;
import org.aibles.intellihire.authservice.rbac.adapter.repository.AccountRoleRepository;
import org.aibles.intellihire.authservice.rbac.domain.AccountRoleEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminGetAccountRolesUseCaseImpl implements AdminGetAccountRolesUseCase {

    private final AccountRepository accountRepository;
    private final AccountRoleRepository accountRoleRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AccountRoleEntity> execute(Long accountId) {
        log.debug("Admin getting roles for account: {}", accountId);

        if (!accountRepository.existsById(accountId)) {
            throw new AccountNotFoundException();
        }

        return accountRoleRepository.findByAccountIdWithRole(accountId);
    }
}
