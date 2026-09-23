package org.aibles.intellihire.authservice.oauth.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.oauth.adapter.repository.AccountOAuthLinkRepository;
import org.aibles.intellihire.authservice.oauth.adapter.repository.OAuthProviderRepository;
import org.aibles.intellihire.authservice.oauth.domain.OAuthLinkNotFoundException;
import org.aibles.intellihire.authservice.oauth.domain.OAuthProviderEntity;
import org.aibles.intellihire.authservice.oauth.domain.OAuthProviderNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UnlinkOAuthProviderUseCaseImpl implements UnlinkOAuthProviderUseCase {

    private final OAuthProviderRepository oAuthProviderRepository;
    private final AccountOAuthLinkRepository accountOAuthLinkRepository;

    @Override
    @Transactional
    public void execute(Long accountId, String providerCode) {
        log.debug("Unlinking OAuth provider {} from account {}", providerCode, accountId);

        // 1. Get provider
        OAuthProviderEntity provider = oAuthProviderRepository.findByCode(providerCode.toLowerCase())
                .orElseThrow(OAuthProviderNotFoundException::new);

        // 2. Check if link exists
        if (!accountOAuthLinkRepository.existsByAccountIdAndProviderId(accountId, provider.getId())) {
            throw new OAuthLinkNotFoundException();
        }

        // 3. Delete link
        accountOAuthLinkRepository.deleteByAccountIdAndProviderId(accountId, provider.getId());

        log.info("Unlinked OAuth provider {} from account {}", providerCode, accountId);
    }
}
