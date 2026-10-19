package org.aibles.intellihire.authservice.shared.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionActivityServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private SessionActivityService sessionActivityService;

    @BeforeEach
    void setUp() {
        sessionActivityService = new SessionActivityService(redisTemplate);
    }

    @Test
    @DisplayName("Should create session with 30-minute TTL")
    void shouldCreateSession() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        sessionActivityService.create(123L);

        verify(valueOperations).set(eq("session:activity:123"), eq("1"), eq(Duration.ofMinutes(30)));
    }

    @Test
    @DisplayName("Should refresh session TTL")
    void shouldRefreshSession() {
        sessionActivityService.refresh(123L);

        verify(redisTemplate).expire(eq("session:activity:123"), eq(Duration.ofMinutes(30)));
    }

    @Test
    @DisplayName("Should delete session key")
    void shouldDeleteSession() {
        sessionActivityService.delete(123L);

        verify(redisTemplate).delete("session:activity:123");
    }

    @Test
    @DisplayName("Should check if session is active")
    void shouldCheckIfActive() {
        when(redisTemplate.hasKey("session:activity:123")).thenReturn(true);
        when(redisTemplate.hasKey("session:activity:999")).thenReturn(false);

        assertThat(sessionActivityService.isActive(123L)).isTrue();
        assertThat(sessionActivityService.isActive(999L)).isFalse();
    }
}
