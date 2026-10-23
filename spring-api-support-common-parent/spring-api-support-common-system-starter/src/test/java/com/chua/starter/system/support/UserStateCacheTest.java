package com.chua.starter.system.support;

import com.chua.starter.system.properties.SystemSecurityProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 账号状态缓存单元测试
 *
 * <p>覆盖 Redis 不可用时的降级路径：L1 进程内缓存必须生效（避免每个请求查库），
 * 且 {@code evict} 必须让下一次读取重新回源。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@DisplayName("UserStateCache: 降级缓存与失效")
class UserStateCacheTest {

    /**
     * 固定返回某个实例的 ObjectProvider 桩，避免引入 Mockito。
     *
     * @param <T> 依赖类型
     */
    private static final class StubProvider<T> implements ObjectProvider<T> {

        /**
         * 固定返回值
         */
        private final T value;

        /**
         * 创建桩
         *
         * @param value 固定返回值，允许为 {@code null}
         */
        private StubProvider(T value) {
            this.value = value;
        }

        @Override
        public T getObject() {
            return value;
        }

        @Override
        public T getObject(Object... args) {
            return value;
        }

        @Override
        public T getIfAvailable() {
            return value;
        }

        @Override
        public T getIfUnique() {
            return value;
        }
    }

    /**
     * 创建不带 Redis 的缓存实例。
     *
     * @return 缓存实例
     */
    private UserStateCache cacheWithoutRedis() {
        return new UserStateCache(
                new StubProvider<StringRedisTemplate>(null),
                new StubProvider<>(new SystemSecurityProperties()));
    }

    /**
     * 同一用户连续读取只回源一次。
     */
    @Test
    @DisplayName("L1 命中: 连续读取不重复回源")
    void shouldCacheWithinLocalTtl() {
        UserStateCache cache = cacheWithoutRedis();
        AtomicInteger loads = new AtomicInteger();
        Supplier<UserStateCache.Snapshot> loader =
                () -> new UserStateCache.Snapshot(loads.incrementAndGet() == 1 ? 1 : 0);

        assertEquals(1, cache.resolve(7, loader).status());
        assertEquals(1, cache.resolve(7, loader).status());
        assertEquals(1, loads.get(), "同一用户在同一 TTL 内只应回源一次");
    }

    /**
     * 失效后必须重新回源并拿到最新状态。
     */
    @Test
    @DisplayName("evict: 失效后立即回源")
    void shouldReloadAfterEvict() {
        UserStateCache cache = cacheWithoutRedis();
        AtomicInteger loads = new AtomicInteger();
        Supplier<UserStateCache.Snapshot> loader =
                () -> new UserStateCache.Snapshot(loads.incrementAndGet() == 1 ? 1 : 0);

        assertEquals(1, cache.resolve(7, loader).status());
        cache.evict(7);
        assertEquals(0, cache.resolve(7, loader).status());
        assertEquals(2, loads.get(), "evict 之后必须重新回源");
    }

    /**
     * 用户不存在（状态为 null）也要能被缓存，避免反复查库。
     */
    @Test
    @DisplayName("null 状态: 可缓存且可失效")
    void shouldCacheNullStatus() {
        UserStateCache cache = cacheWithoutRedis();
        AtomicInteger loads = new AtomicInteger();
        Supplier<UserStateCache.Snapshot> loader = () -> {
            loads.incrementAndGet();
            return new UserStateCache.Snapshot(null);
        };

        assertNull(cache.resolve(9, loader).status());
        assertNull(cache.resolve(9, loader).status());
        assertEquals(1, loads.get());

        cache.evict(9);
        assertNull(cache.resolve(9, loader).status());
        assertEquals(2, loads.get());
    }

    /**
     * 用户主键为空时不做缓存，直接调用加载器。
     */
    @Test
    @DisplayName("userId 为空: 不回源缓存")
    void shouldNotCacheNullUserId() {
        UserStateCache cache = cacheWithoutRedis();
        AtomicInteger loads = new AtomicInteger();
        Supplier<UserStateCache.Snapshot> loader = () -> {
            loads.incrementAndGet();
            return new UserStateCache.Snapshot(1);
        };

        cache.resolve(null, loader);
        cache.resolve(null, loader);
        assertEquals(2, loads.get());
    }
}
