package org.aibles.intellihire.authservice.auth.token;

public record RefreshTokenClaims(
        String tokenId,
        Long accountId,
        String email
) {
}
