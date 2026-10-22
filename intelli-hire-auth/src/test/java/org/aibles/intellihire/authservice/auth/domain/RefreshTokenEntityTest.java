package org.aibles.intellihire.authservice.auth.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenEntityTest {

    @Test
    @DisplayName("Should detect valid token state correctly")
    void shouldDetectValidState() {
        RefreshTokenEntity token = new RefreshTokenEntity();
        token.setTokenId("token-123");
        token.setAccountId(1L);
        token.setExpiresAt(LocalDateTime.now().plusDays(7));

        assertThat(token.isRevoked()).isFalse();
        assertThat(token.isExpired()).isFalse();
        assertThat(token.isValid()).isTrue();
    }

    @Test
    @DisplayName("Should revoke token and mark as invalid")
    void shouldRevokeToken() {
        RefreshTokenEntity token = new RefreshTokenEntity();
        token.setTokenId("token-123");
        token.setAccountId(1L);
        token.setExpiresAt(LocalDateTime.now().plusDays(7));

        token.revoke("logout");

        assertThat(token.isRevoked()).isTrue();
        assertThat(token.getRevokedReason()).isEqualTo("logout");
        assertThat(token.isValid()).isFalse();
    }

    @Test
    @DisplayName("Should detect expired token")
    void shouldDetectExpired() {
        RefreshTokenEntity token = new RefreshTokenEntity();
        token.setTokenId("token-123");
        token.setAccountId(1L);
        token.setExpiresAt(LocalDateTime.now().minusDays(1));

        assertThat(token.isExpired()).isTrue();
        assertThat(token.isValid()).isFalse();
    }
}
