package org.aibles.intellihire.authservice.shared.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecureTokenGeneratorTest {

    @Test
    void generateToken_returnsNonBlankToken() {
        String token = SecureTokenGenerator.generateToken();
        assertThat(token).isNotBlank();
    }

    @Test
    void generateToken_uniqueEachTime() {
        String token1 = SecureTokenGenerator.generateToken();
        String token2 = SecureTokenGenerator.generateToken();
        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    void generateToken_hasMinimumLength() {
        String token = SecureTokenGenerator.generateToken();
        // 32 bytes base64url encoded = ~43 characters
        assertThat(token.length()).isGreaterThanOrEqualTo(40);
    }

    @Test
    void hashToken_deterministic() {
        String token = "test-token";
        String hash1 = SecureTokenGenerator.hashToken(token);
        String hash2 = SecureTokenGenerator.hashToken(token);
        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    void hashToken_differentInputsProduceDifferentHashes() {
        String hash1 = SecureTokenGenerator.hashToken("token1");
        String hash2 = SecureTokenGenerator.hashToken("token2");
        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    void hashToken_returns64CharHex() {
        String hash = SecureTokenGenerator.hashToken("test");
        assertThat(hash).hasSize(64); // SHA-256 = 256 bits = 64 hex chars
        assertThat(hash).matches("[0-9a-f]+");
    }
}
