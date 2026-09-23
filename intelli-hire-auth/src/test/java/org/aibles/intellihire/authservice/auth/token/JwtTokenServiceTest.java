package org.aibles.intellihire.authservice.auth.token;

import org.aibles.intellihire.authservice.auth.domain.InvalidTokenException;
import org.aibles.intellihire.authservice.shared.config.properties.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenServiceTest {

    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("test-secret-key-that-is-at-least-256-bits-long-for-hmac-sha256");
        properties.setIssuer("intellihire-test");
        properties.setAccessTokenAudience("intellihire-api");
        properties.setRefreshTokenAudience("intellihire-auth-refresh");
        properties.setAccessTokenExpirationSeconds(900);
        properties.setRefreshTokenExpirationSeconds(604800);

        jwtTokenService = new JwtTokenService(properties);
    }

    @Test
    void issueTokenPair_returnsValidTokens() {
        JwtTokenBundle bundle = jwtTokenService.issueTokenPair(
                1L, "test@example.com", "testuser",
                List.of("USER"), List.of("READ")
        );

        assertThat(bundle.accessToken()).isNotBlank();
        assertThat(bundle.refreshToken()).isNotBlank();
        assertThat(bundle.refreshTokenId()).isNotBlank();
        assertThat(bundle.accessTokenExpiresIn()).isEqualTo(900);
        assertThat(bundle.refreshTokenExpiresIn()).isEqualTo(604800);
    }

    @Test
    void parseAccessToken_validToken_returnsClaims() {
        JwtTokenBundle bundle = jwtTokenService.issueTokenPair(
                1L, "test@example.com", "testuser",
                List.of("USER"), List.of("READ")
        );

        AccessTokenClaims claims = jwtTokenService.parseAndValidateAccessToken(bundle.accessToken());

        assertThat(claims.accountId()).isEqualTo(1L);
        assertThat(claims.email()).isEqualTo("test@example.com");
        assertThat(claims.username()).isEqualTo("testuser");
        assertThat(claims.roles()).containsExactly("USER");
        assertThat(claims.permissions()).containsExactly("READ");
    }

    @Test
    void parseRefreshToken_validToken_returnsClaims() {
        JwtTokenBundle bundle = jwtTokenService.issueTokenPair(
                1L, "test@example.com", "testuser",
                List.of("USER"), List.of()
        );

        RefreshTokenClaims claims = jwtTokenService.parseAndValidateRefreshToken(bundle.refreshToken());

        assertThat(claims.tokenId()).isEqualTo(bundle.refreshTokenId());
        assertThat(claims.accountId()).isEqualTo(1L);
        assertThat(claims.email()).isEqualTo("test@example.com");
    }

    @Test
    void parseAccessToken_invalidToken_throwsException() {
        assertThatThrownBy(() -> jwtTokenService.parseAndValidateAccessToken("invalid-token"))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void parseAccessToken_refreshTokenUsedAsAccess_throwsException() {
        JwtTokenBundle bundle = jwtTokenService.issueTokenPair(
                1L, "test@example.com", "testuser",
                List.of("USER"), List.of()
        );

        assertThatThrownBy(() -> jwtTokenService.parseAndValidateAccessToken(bundle.refreshToken()))
                .isInstanceOf(InvalidTokenException.class);
    }
}
