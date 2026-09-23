package org.aibles.intellihire.authservice.oauth.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.oauth.adapter.repository.AccountOAuthLinkRepository;
import org.aibles.intellihire.authservice.oauth.adapter.repository.OAuthProviderRepository;
import org.aibles.intellihire.authservice.oauth.client.OAuthClient;
import org.aibles.intellihire.authservice.oauth.client.OAuthClientFactory;
import org.aibles.intellihire.authservice.oauth.domain.*;
import org.aibles.intellihire.authservice.oauth.usecase.command.LinkOAuthProviderCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkOAuthProviderUseCaseImpl implements LinkOAuthProviderUseCase {

    private final OAuthProviderRepository oAuthProviderRepository;
    private final AccountOAuthLinkRepository accountOAuthLinkRepository;
    private final OAuthClientFactory oAuthClientFactory;

    @Override
    @Transactional
    public void execute(LinkOAuthProviderCommand command) {
        log.debug("Linking OAuth provider {} to account {}", command.providerCode(), command.accountId());

        // 1. Get provider and validate
        OAuthProviderEntity provider = oAuthProviderRepository.findByCode(command.providerCode().toLowerCase())
                .orElseThrow(OAuthProviderNotFoundException::new);

        if (!provider.getIsActive()) {
            throw new OAuthProviderDisabledException();
        }

        // 2. Check if already linked
        if (accountOAuthLinkRepository.existsByAccountIdAndProviderId(command.accountId(), provider.getId())) {
            throw new OAuthLinkAlreadyExistsException();
        }

        // 3. Exchange code for user info
        OAuthClient client = oAuthClientFactory.getClient(command.providerCode());
        OAuthUserInfo userInfo = client.exchangeCodeForUserInfo(command.code());

        // 4. Check if this OAuth account is already linked to another account
        if (accountOAuthLinkRepository.findByProviderIdAndProviderUserId(provider.getId(), userInfo.providerUserId()).isPresent()) {
            log.warn("OAuth account already linked to another user: provider={}, providerUserId={}",
                    command.providerCode(), userInfo.providerUserId());
            throw new OAuthLinkAlreadyExistsException();
        }

        // 5. Create link
        AccountOAuthLinkEntity link = new AccountOAuthLinkEntity();
        link.setAccountId(command.accountId());
        link.setProviderId(provider.getId());
        link.setProviderUserId(userInfo.providerUserId());
        link.setProviderEmail(userInfo.email());

        accountOAuthLinkRepository.save(link);

        log.info("Linked OAuth provider {} to account {}", command.providerCode(), command.accountId());
    }
}
