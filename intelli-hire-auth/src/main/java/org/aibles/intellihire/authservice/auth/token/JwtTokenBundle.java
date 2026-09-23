package org.aibles.intellihire.authservice.auth.token;

public record JwtTokenBundle(
        String accessToken,
        String refreshToken,
        String refreshTokenId,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn
) {
}
