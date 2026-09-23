package org.aibles.intellihire.authservice.oauth.usecase.command;

public record LinkOAuthProviderCommand(
        Long accountId,
        String providerCode,
        String code
) {
}
