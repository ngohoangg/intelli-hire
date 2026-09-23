package org.aibles.intellihire.authservice.oauth.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.oauth.adapter.repository.AccountOAuthLinkRepository;
import org.aibles.intellihire.authservice.oauth.domain.AccountOAuthLinkEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetLinkedOAuthProvidersUseCaseImpl implements GetLinkedOAuthProvidersUseCase {

    private final AccountOAuthLinkRepository accountOAuthLinkRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AccountOAuthLinkEntity> execute(Long accountId) {
        log.debug("Getting linked OAuth providers for account: {}", accountId);
        return accountOAuthLinkRepository.findByAccountIdWithProvider(accountId);
    }
}
