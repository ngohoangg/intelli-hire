package org.aibles.intellihire.authservice.auth.token;

import org.aibles.intellihire.authservice.auth.domain.InvalidTokenException;
import org.aibles.intellihire.authservice.shared.config.properties.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenServiceTest {

    private JwtProperties jwtProperties;
    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret("this-is-a-very-long-test-secret-key-that-exceeds-256-bits-for-testing-12345");
        jwtProperties.setIssuer("intellihire-auth");
        jwtProperties.setAccessTokenAudience("intellihire-api");
        jwtProperties.setRefreshTokenAudience("intellihire-refresh");
        jwtProperties.setAccessTokenExpirationSeconds(900);
        jwtProperties.setRefreshTokenExpirationSeconds(604800);

        jwtTokenService = new JwtTokenService(jwtProperties);
        jwtTokenService.init();
    }

    @Test
    @DisplayName("Should issue valid token pair and parse access token correctly")
    void shouldIssueAndParseTokenPair() {
        JwtTokenBundle bundle = jwtTokenService.issueTokenPair(
                100L,
                "candidate@intellihire.com",
                "candidate_user",
                List.of("ROLE_USER", "ROLE_CANDIDATE"),
                List.of("resume:read", "interview:participate")
        );

        assertThat(bundle).isNotNull();
        assertThat(bundle.accessToken()).isNotBlank();
        assertThat(bundle.refreshToken()).isNotBlank();
        assertThat(bundle.refreshTokenId()).isNotBlank();
        assertThat(bundle.accessTokenExpiresIn()).isEqualTo(900);
        assertThat(bundle.refreshTokenExpiresIn()).isEqualTo(604800);

        AccessTokenClaims accessClaims = jwtTokenService.parseAndValidateAccessToken(bundle.accessToken());
        assertThat(accessClaims.accountId()).isEqualTo(100L);
        assertThat(accessClaims.email()).isEqualTo("candidate@intellihire.com");
        assertThat(accessClaims.username()).isEqualTo("candidate_user");
        assertThat(accessClaims.roles()).containsExactlyInAnyOrder("ROLE_USER", "ROLE_CANDIDATE");
        assertThat(accessClaims.permissions()).containsExactlyInAnyOrder("resume:read", "interview:participate");

        RefreshTokenClaims refreshClaims = jwtTokenService.parseAndValidateRefreshToken(bundle.refreshToken());
        assertThat(refreshClaims.accountId()).isEqualTo(100L);
        assertThat(refreshClaims.tokenId()).isEqualTo(bundle.refreshTokenId());
        assertThat(refreshClaims.email()).isEqualTo("candidate@intellihire.com");
    }

    @Test
    @DisplayName("Should reject access token when given a refresh token")
    void shouldRejectAccessTokenWhenRefreshTokenGiven() {
        JwtTokenBundle bundle = jwtTokenService.issueTokenPair(
                100L, "candidate@intellihire.com", "candidate_user",
                List.of("ROLE_USER"), List.of()
        );

        assertThatThrownBy(() -> jwtTokenService.parseAndValidateAccessToken(bundle.refreshToken()))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    @DisplayName("Should reject refresh token when given an access token")
    void shouldRejectRefreshTokenWhenAccessTokenGiven() {
        JwtTokenBundle bundle = jwtTokenService.issueTokenPair(
                100L, "candidate@intellihire.com", "candidate_user",
                List.of("ROLE_USER"), List.of()
        );

        assertThatThrownBy(() -> jwtTokenService.parseAndValidateRefreshToken(bundle.accessToken()))
                .isInstanceOf(InvalidTokenException.class);
    }
}
