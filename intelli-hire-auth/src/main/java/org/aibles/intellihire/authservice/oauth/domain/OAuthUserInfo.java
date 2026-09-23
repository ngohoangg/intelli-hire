package org.aibles.intellihire.authservice.oauth.domain;

public record OAuthUserInfo(
        String providerUserId,
        String email,
        String name,
        String avatarUrl
) {
}
