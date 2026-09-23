package org.aibles.intellihire.authservice.oauth.usecase;

import org.aibles.intellihire.authservice.oauth.domain.OAuthProviderEntity;

import java.util.List;

public interface GetOAuthProvidersUseCase {
    List<OAuthProviderEntity> execute();
}
