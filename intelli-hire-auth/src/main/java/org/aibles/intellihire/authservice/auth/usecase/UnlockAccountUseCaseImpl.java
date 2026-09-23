package org.aibles.intellihire.authservice.auth.usecase;

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
public class UnlockAccountUseCaseImpl implements UnlockAccountUseCase {

    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public UnlockAccountResponse execute(Long accountId) {
        log.debug("Processing account unlock for: {}", accountId);

        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(AccountNotFoundException::new);

        if (!account.isLocked()) {
            return new UnlockAccountResponse(accountId, "Account is not locked.");
        }

        account.unlock();
        accountRepository.save(account);

        log.info("Account unlocked: {}", accountId);

        return new UnlockAccountResponse(accountId, "Account has been unlocked successfully.");
    }
}
