package org.aibles.intellihire.authservice.oauth.usecase;

import org.aibles.intellihire.authservice.oauth.domain.AccountOAuthLinkEntity;

import java.util.List;

public interface GetLinkedOAuthProvidersUseCase {
    List<AccountOAuthLinkEntity> execute(Long accountId);
}
