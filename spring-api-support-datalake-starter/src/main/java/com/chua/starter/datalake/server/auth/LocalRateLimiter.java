package com.chua.starter.datalake.server.auth;

import com.chua.common.support.core.annotation.Spi;
import com.chua.common.support.core.annotation.SpiDescribe;
import com.chua.datalake.support.spi.auth.RateLimiter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 本地内存限流器 — 基于令牌桶算法
 * <p>每个 key 独立计数，默认速率 100/秒。适用于单机部署场景。</p>
 *
 * @author CH
 */
@Spi("local-rate-limiter")
@SpiDescribe(value = "local-rate-limiter", desc = "本地内存限流器 - 令牌桶算法")
public class LocalRateLimiter implements RateLimiter {

    /** 默认每秒许可数 */
    private static final int DEFAULT_PERMITS_PER_SECOND = 100;

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    @Override
    public boolean tryAcquire(String key) {
        return tryAcquire(key, 1);
    }

    @Override
    public boolean tryAcquire(String key, int permits) {
        if (key == null) return false;
        TokenBucket bucket = buckets.computeIfAbsent(key, k -> new TokenBucket(DEFAULT_PERMITS_PER_SECOND));
        return bucket.tryAcquire(permits);
    }

    @Override
    public void setRate(String key, int permitsPerSecond) {
        TokenBucket bucket = buckets.computeIfAbsent(key, k -> new TokenBucket(permitsPerSecond));
        bucket.setRate(permitsPerSecond);
    }

    /**
     * 令牌桶
     */
    static class TokenBucket {
        /** 每秒许可数（速率） */
        private volatile long permitsPerSecond;
        /** 当前可用许可数 */
        private final AtomicLong availablePermits;
        /** 上次补充时间（纳秒） */
        private volatile long lastRefillNanos;

        TokenBucket(long permitsPerSecond) {
            this.permitsPerSecond = permitsPerSecond;
            this.availablePermits = new AtomicLong(permitsPerSecond);
            this.lastRefillNanos = System.nanoTime();
        }

        boolean tryAcquire(int permits) {
            refill();
            while (true) {
                long current = availablePermits.get();
                if (current < permits) return false;
                if (availablePermits.compareAndSet(current, current - permits)) return true;
            }
        }

        void setRate(int permitsPerSecond) {
            this.permitsPerSecond = permitsPerSecond;
        }

        private void refill() {
            long now = System.nanoTime();
            long elapsed = now - lastRefillNanos;
            if (elapsed < TimeUnit.MILLISECONDS.toNanos(100)) return; // 至少 100ms 才补充
            long tokensToAdd = (long) (permitsPerSecond * ((double) elapsed / TimeUnit.SECONDS.toNanos(1)));
            if (tokensToAdd > 0) {
                lastRefillNanos = now;
                while (true) {
                    long current = availablePermits.get();
                    long newValue = Math.min(permitsPerSecond, current + tokensToAdd);
                    if (availablePermits.compareAndSet(current, newValue)) return;
                }
            }
        }
    }
}
