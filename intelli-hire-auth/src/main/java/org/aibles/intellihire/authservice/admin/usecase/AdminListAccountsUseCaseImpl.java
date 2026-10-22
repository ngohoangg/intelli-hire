package org.aibles.intellihire.authservice.admin.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.account.adapter.repository.AccountRepository;
import org.aibles.intellihire.authservice.account.domain.AccountEntity;
import org.aibles.intellihire.authservice.admin.usecase.command.ListAccountsCommand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminListAccountsUseCaseImpl implements AdminListAccountsUseCase {

    private final AccountRepository accountRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AccountEntity> execute(ListAccountsCommand command) {
        log.debug("Admin listing accounts: page={}, size={}, status={}",
                command.page(), command.size(), command.status());

        Pageable pageable = PageRequest.of(
                command.page(),
                command.size(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return accountRepository.findAll(pageable);
    }
}
