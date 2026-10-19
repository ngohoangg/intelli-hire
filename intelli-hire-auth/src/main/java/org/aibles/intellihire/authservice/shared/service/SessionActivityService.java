package org.aibles.intellihire.authservice.shared.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class SessionActivityService {

    private static final String KEY_PREFIX = "session:activity:";
    private static final Duration TTL = Duration.ofMinutes(30);

    private final StringRedisTemplate redisTemplate;

    public void create(Long accountId) {
        redisTemplate.opsForValue().set(key(accountId), "1", TTL);
    }

    public void refresh(Long accountId) {
        redisTemplate.expire(key(accountId), TTL);
    }

    public void delete(Long accountId) {
        redisTemplate.delete(key(accountId));
    }

    public boolean isActive(Long accountId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key(accountId)));
    }

    private String key(Long accountId) {
        return KEY_PREFIX + accountId;
    }
}
