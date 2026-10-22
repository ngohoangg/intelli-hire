package org.aibles.intellihire.authservice.shared.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecureTokenGeneratorTest {

    @Test
    @DisplayName("Should generate random base64url token of valid length")
    void shouldGenerateRandomToken() {
        String token1 = SecureTokenGenerator.generateToken();
        String token2 = SecureTokenGenerator.generateToken();

        assertThat(token1).isNotBlank();
        assertThat(token2).isNotBlank();
        assertThat(token1).isNotEqualTo(token2);
        assertThat(token1.length()).isGreaterThanOrEqualTo(40);
    }

    @Test
    @DisplayName("Should hash token using SHA-256 to 64-char hex string deterministically")
    void shouldHashTokenDeterministically() {
        String token = "my-secret-test-token-123";
        String hash1 = SecureTokenGenerator.hashToken(token);
        String hash2 = SecureTokenGenerator.hashToken(token);

        assertThat(hash1).isNotNull();
        assertThat(hash1).hasSize(64);
        assertThat(hash1).isEqualTo(hash2);
    }
}
