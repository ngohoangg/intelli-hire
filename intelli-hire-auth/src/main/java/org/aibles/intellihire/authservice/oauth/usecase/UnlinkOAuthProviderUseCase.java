package org.aibles.intellihire.authservice.oauth.usecase;

public interface UnlinkOAuthProviderUseCase {
    void execute(Long accountId, String providerCode);
}
