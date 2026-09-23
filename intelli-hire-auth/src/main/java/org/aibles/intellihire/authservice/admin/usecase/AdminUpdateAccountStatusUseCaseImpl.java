package org.aibles.intellihire.authservice.admin.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.account.adapter.repository.AccountRepository;
import org.aibles.intellihire.authservice.account.domain.AccountEntity;
import org.aibles.intellihire.authservice.account.domain.AccountNotFoundException;
import org.aibles.intellihire.authservice.account.domain.AccountStatus;
import org.aibles.intellihire.authservice.admin.usecase.command.UpdateAccountStatusCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUpdateAccountStatusUseCaseImpl implements AdminUpdateAccountStatusUseCase {

    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public AccountEntity execute(UpdateAccountStatusCommand command) {
        log.debug("Admin updating account status: accountId={}, status={}",
                command.accountId(), command.status());

        AccountEntity account = accountRepository.findById(command.accountId())
                .orElseThrow(AccountNotFoundException::new);

        AccountStatus previousStatus = account.getStatus();
        account.setStatus(command.status());

        if (command.status() == AccountStatus.LOCKED) {
            account.lock(command.reason());
        } else if (previousStatus == AccountStatus.LOCKED && command.status() == AccountStatus.ACTIVE) {
            account.unlock();
        }

        AccountEntity saved = accountRepository.save(account);

        log.info("Admin updated account {} status from {} to {}",
                command.accountId(), previousStatus, command.status());

        return saved;
    }
}
