package org.aibles.intellihire.authservice.shared.ratelimit;

import org.aibles.intellihire.authservice.shared.config.RateLimitProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryRateLimiterTest {

    private RateLimitProperties properties;
    private InMemoryRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        properties = new RateLimitProperties();
        properties.setDefaultCapacity(2);
        properties.setDefaultRefillTokens(2);
        properties.setDefaultRefillSeconds(60);
        rateLimiter = new InMemoryRateLimiter(properties);
    }

    @Test
    @DisplayName("Should allow requests up to capacity and reject when tokens exhausted")
    void shouldAllowUpToCapacityAndReject() {
        String key = "test-client-ip";

        assertThat(rateLimiter.tryConsume(key)).isTrue();
        assertThat(rateLimiter.tryConsume(key)).isTrue();
        assertThat(rateLimiter.tryConsume(key)).isFalse();
    }

    @Test
    @DisplayName("Should return configured retry-after seconds")
    void shouldReturnRetryAfterSeconds() {
        String key = "test-client-ip";
        rateLimiter.tryConsume(key);

        assertThat(rateLimiter.getRetryAfterSeconds(key)).isEqualTo(60);
    }
}
