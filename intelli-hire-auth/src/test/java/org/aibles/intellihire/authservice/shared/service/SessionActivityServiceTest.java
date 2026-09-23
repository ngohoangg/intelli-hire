package org.aibles.intellihire.authservice.shared.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionActivityServiceTest {

    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOps;

    private SessionActivityService service;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        service = new SessionActivityService(redisTemplate);
    }

    @Test
    void create_setsKeyWithTtl() {
        service.create(42L);
        verify(valueOps).set("session:activity:42", "1", Duration.ofMinutes(30));
    }

    @Test
    void refresh_updatesExpiry() {
        service.refresh(42L);
        verify(redisTemplate).expire("session:activity:42", Duration.ofMinutes(30));
    }

    @Test
    void delete_removesKey() {
        service.delete(42L);
        verify(redisTemplate).delete("session:activity:42");
    }

    @Test
    void isActive_returnsTrueWhenKeyExists() {
        when(redisTemplate.hasKey("session:activity:42")).thenReturn(Boolean.TRUE);
        assertThat(service.isActive(42L)).isTrue();
    }

    @Test
    void isActive_returnsFalseWhenKeyMissing() {
        when(redisTemplate.hasKey("session:activity:42")).thenReturn(Boolean.FALSE);
        assertThat(service.isActive(42L)).isFalse();
    }

    @Test
    void isActive_returnsFalseWhenRedisReturnsNull() {
        when(redisTemplate.hasKey("session:activity:42")).thenReturn(null);
        assertThat(service.isActive(42L)).isFalse();
    }
}
