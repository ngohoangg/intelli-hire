package org.aibles.intellihire.authservice.oauth.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihire.authservice.oauth.adapter.repository.OAuthProviderRepository;
import org.aibles.intellihire.authservice.oauth.domain.OAuthProviderEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetOAuthProvidersUseCaseImpl implements GetOAuthProvidersUseCase {

    private final OAuthProviderRepository oAuthProviderRepository;

    @Override
    @Transactional(readOnly = true)
    public List<OAuthProviderEntity> execute() {
        log.debug("Getting all active OAuth providers");
        return oAuthProviderRepository.findByIsActiveTrue();
    }
}
