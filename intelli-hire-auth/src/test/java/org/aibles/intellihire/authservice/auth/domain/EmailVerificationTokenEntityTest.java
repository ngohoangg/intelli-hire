package org.aibles.intellihire.authservice.auth.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EmailVerificationTokenEntityTest {

    @Test
    @DisplayName("Should detect valid token state correctly")
    void shouldDetectValidState() {
        EmailVerificationTokenEntity token = new EmailVerificationTokenEntity();
        token.setTokenHash("hash-123");
        token.setAccountId(1L);
        token.setEmail("test@intellihire.com");
        token.setExpiresAt(LocalDateTime.now().plusHours(24));

        assertThat(token.isExpired()).isFalse();
        assertThat(token.isVerified()).isFalse();
        assertThat(token.isValid()).isTrue();
    }

    @Test
    @DisplayName("Should mark token as verified")
    void shouldMarkAsVerified() {
        EmailVerificationTokenEntity token = new EmailVerificationTokenEntity();
        token.setTokenHash("hash-123");
        token.setAccountId(1L);
        token.setEmail("test@intellihire.com");
        token.setExpiresAt(LocalDateTime.now().plusHours(24));

        token.markAsVerified();

        assertThat(token.isVerified()).isTrue();
        assertThat(token.getVerifiedAt()).isNotNull();
        assertThat(token.isValid()).isFalse();
    }

    @Test
    @DisplayName("Should detect expired token")
    void shouldDetectExpiredToken() {
        EmailVerificationTokenEntity token = new EmailVerificationTokenEntity();
        token.setTokenHash("hash-123");
        token.setAccountId(1L);
        token.setEmail("test@intellihire.com");
        token.setExpiresAt(LocalDateTime.now().minusHours(1));

        assertThat(token.isExpired()).isTrue();
        assertThat(token.isValid()).isFalse();
    }
}
