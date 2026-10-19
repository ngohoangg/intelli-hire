package org.aibles.intellihire.authservice.shared.ratelimit;

import lombok.RequiredArgsConstructor;
import org.aibles.intellihire.authservice.shared.config.RateLimitProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@RequiredArgsConstructor
public class InMemoryRateLimiter {

    private final RateLimitProperties properties;
    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public boolean tryConsume(String key) {
        TokenBucket bucket = buckets.computeIfAbsent(key, k -> new TokenBucket(
                properties.getDefaultCapacity(),
                properties.getDefaultRefillTokens(),
                properties.getDefaultRefillSeconds()
        ));
        return bucket.tryConsume();
    }

    public long getRetryAfterSeconds(String key) {
        TokenBucket bucket = buckets.get(key);
        if (bucket == null) return 1;
        return bucket.getRefillSeconds();
    }

    @Scheduled(fixedRate = 300_000) // every 5 minutes
    public void cleanup() {
        long now = System.currentTimeMillis();
        buckets.entrySet().removeIf(entry ->
                now - entry.getValue().getLastAccessTime() > 600_000 // 10 minutes idle
        );
    }

    static class TokenBucket {
        private final int capacity;
        private final int refillTokens;
        private final int refillSeconds;
        private final AtomicLong tokens;
        private volatile long lastRefillTime;
        private volatile long lastAccessTime;

        TokenBucket(int capacity, int refillTokens, int refillSeconds) {
            this.capacity = capacity;
            this.refillTokens = refillTokens;
            this.refillSeconds = refillSeconds;
            this.tokens = new AtomicLong(capacity);
            this.lastRefillTime = System.currentTimeMillis();
            this.lastAccessTime = System.currentTimeMillis();
        }

        synchronized boolean tryConsume() {
            refill();
            lastAccessTime = System.currentTimeMillis();
            long current = tokens.get();
            if (current > 0) {
                tokens.decrementAndGet();
                return true;
            }
            return false;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRefillTime;
            long refillIntervalMs = refillSeconds * 1000L;
            if (elapsed >= refillIntervalMs) {
                long periods = elapsed / refillIntervalMs;
                long newTokens = Math.min(capacity, tokens.get() + periods * refillTokens);
                tokens.set(newTokens);
                lastRefillTime = now;
            }
        }

        int getRefillSeconds() {
            return refillSeconds;
        }

        long getLastAccessTime() {
            return lastAccessTime;
        }
    }
}
