package org.aibles.intellihire.authservice.admin.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.account.adapter.repository.AccountRepository;
import org.aibles.intellihire.authservice.account.domain.AccountEntity;
import org.aibles.intellihire.authservice.account.domain.AccountNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminGetAccountUseCaseImpl implements AdminGetAccountUseCase {

    private final AccountRepository accountRepository;

    @Override
    @Transactional(readOnly = true)
    public AccountEntity execute(Long accountId) {
        log.debug("Admin getting account: {}", accountId);

        return accountRepository.findById(accountId)
                .orElseThrow(AccountNotFoundException::new);
    }
}
