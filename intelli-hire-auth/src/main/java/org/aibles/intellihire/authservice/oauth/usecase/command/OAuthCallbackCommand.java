package org.aibles.intellihire.authservice.oauth.usecase.command;

public record OAuthCallbackCommand(
        String providerCode,
        String code,
        String state,
        String deviceInfo,
        String ipAddress
) {
}
