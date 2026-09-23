package org.aibles.intellihire.authservice.oauth.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.oauth.adapter.repository.OAuthProviderRepository;
import org.aibles.intellihire.authservice.oauth.client.OAuthClient;
import org.aibles.intellihire.authservice.oauth.client.OAuthClientFactory;
import org.aibles.intellihire.authservice.oauth.domain.OAuthProviderDisabledException;
import org.aibles.intellihire.authservice.oauth.domain.OAuthProviderEntity;
import org.aibles.intellihire.authservice.oauth.domain.OAuthProviderNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetOAuthAuthorizationUrlUseCaseImpl implements GetOAuthAuthorizationUrlUseCase {

    private final OAuthProviderRepository oAuthProviderRepository;
    private final OAuthClientFactory oAuthClientFactory;

    @Override
    @Transactional(readOnly = true)
    public String execute(String providerCode, String state) {
        log.debug("Getting authorization URL for provider: {}", providerCode);

        OAuthProviderEntity provider = oAuthProviderRepository.findByCode(providerCode.toLowerCase())
                .orElseThrow(OAuthProviderNotFoundException::new);

        if (!provider.getIsActive()) {
            throw new OAuthProviderDisabledException();
        }

        OAuthClient client = oAuthClientFactory.getClient(providerCode);
        String authorizationUrl = client.getAuthorizationUrl(state);

        log.debug("Generated authorization URL for provider: {}", providerCode);
        return authorizationUrl;
    }
}
