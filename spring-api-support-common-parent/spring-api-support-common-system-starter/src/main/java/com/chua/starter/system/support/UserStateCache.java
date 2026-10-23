package com.chua.starter.system.support;

import com.chua.starter.system.properties.SystemSecurityProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 用户账号状态缓存
 *
 * <p>请求拦截时需要判断账号当前是否被禁用。为避免每个请求都查库，这里做两级缓存：</p>
 * <ul>
 *   <li><b>L1（进程内）</b>：默认 5 秒，用于把同一实例内的 Redis 访问压到最低；</li>
 *   <li><b>L2（Redis）</b>：默认 30 秒，多实例共享，键为 {@code system:user:state:{userId}}。</li>
 * </ul>
 *
 * <p>管理员执行禁用、启用等操作后必须调用 {@link #evict} 同时失效两级缓存：
 * L2 被删除后其它实例在下一次 L1 过期（≤5 秒）即读到最新状态，而执行操作的本实例
 * 因为同步清掉 L1，下一次请求立即生效。</p>
 *
 * <p>Redis 不可用时自动降级为仅进程内缓存（20 秒），保证鉴权链路不会因为缓存组件故障
 * 变成「每个请求一次数据库查询」。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@Slf4j
@Component
public class UserStateCache {

    /**
     * Redis 键前缀
     */
    private static final String KEY_PREFIX = "system:user:state:";

    /**
     * 用户不存在时写入的哨兵值，避免「查不到」被当成未命中而反复回源
     */
    private static final String NOT_FOUND = "-1";

    /**
     * Redis 不可用时的进程内兜底有效期（毫秒）
     */
    private static final long FALLBACK_LOCAL_TTL_MILLIS = 20_000L;

    /**
     * Redis 模板，允许为空（单元测试 / 未配置 Redis 的场景）
     */
    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;

    /**
     * 系统安全配置，允许为空
     */
    private final ObjectProvider<SystemSecurityProperties> propertiesProvider;

    /**
     * L1 缓存条目
     */
    private final Map<Integer, Entry> entries = new ConcurrentHashMap<>();

    /**
     * 创建 用户账号状态缓存 实例
     *
     * @param redisTemplateProvider Redis 模板提供者
     * @param propertiesProvider    系统安全配置提供者
     */
    public UserStateCache(ObjectProvider<StringRedisTemplate> redisTemplateProvider,
                          ObjectProvider<SystemSecurityProperties> propertiesProvider) {
        this.redisTemplateProvider = redisTemplateProvider;
        this.propertiesProvider = propertiesProvider;
    }

    /**
     * 读取账号状态，未命中或已过期时回源。
     *
     * <p>回源顺序：L1 → L2(Redis) → {@code loader}；回源结果会按配置写回两级缓存。</p>
     *
     * @param userId 用户主键，为空时直接回源
     * @param loader 回源加载器
     * @return 账号状态快照
     */
    public Snapshot resolve(Integer userId, Supplier<Snapshot> loader) {
        if (userId == null) {
            return loader.get();
        }
        long now = System.currentTimeMillis();
        Entry entry = entries.get(userId);
        if (entry != null && entry.expireAt() > now) {
            return entry.snapshot();
        }

        StringRedisTemplate redis = redis();
        Snapshot snapshot = readRedis(redis, userId);
        if (snapshot == null) {
            snapshot = loader.get();
            writeRedis(redis, userId, snapshot);
        }

        long ttl = localTtlMillis(redis != null);
        if (ttl > 0) {
            entries.put(userId, new Entry(snapshot, now + ttl));
        }
        return snapshot;
    }

    /**
     * 失效指定用户的状态缓存（两级同时失效）。
     *
     * @param userIds 用户主键集合，允许为空
     */
    public void evict(Collection<Integer> userIds) {
        if (userIds == null) {
            return;
        }
        userIds.stream().filter(Objects::nonNull).forEach(this::evict);
    }

    /**
     * 失效指定用户的状态缓存（两级同时失效）。
     *
     * @param userId 用户主键，允许为空
     */
    public void evict(Integer userId) {
        if (userId == null) {
            return;
        }
        entries.remove(userId);
        StringRedisTemplate redis = redis();
        if (redis == null) {
            return;
        }
        try {
            redis.delete(key(userId));
        } catch (Exception e) {
            log.warn("[账号状态缓存] Redis 失效失败 - userId: {}", userId, e);
        }
    }

    /**
     * 账号状态快照
     *
     * @param status 账号启用状态：1 启用 / 0 禁用
     */
    public record Snapshot(Integer status) {
    }

    /**
     * 缓存条目
     *
     * @param snapshot 状态快照
     * @param expireAt 过期时间戳
     */
    private record Entry(Snapshot snapshot, long expireAt) {
    }

    /**
     * 获取 Redis 模板。
     *
     * @return Redis 模板，不可用时返回 {@code null}
     */
    private StringRedisTemplate redis() {
        try {
            return redisTemplateProvider.getIfAvailable();
        } catch (Exception e) {
            log.debug("[账号状态缓存] 获取 Redis 模板失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 读取 L2 缓存。
     *
     * @param redis  Redis 模板，允许为空
     * @param userId 用户主键
     * @return 状态快照，未命中或异常时返回 {@code null}
     */
    private Snapshot readRedis(StringRedisTemplate redis, Integer userId) {
        if (redis == null) {
            return null;
        }
        try {
            String value = redis.opsForValue().get(key(userId));
            if (value == null) {
                return null;
            }
            return new Snapshot(NOT_FOUND.equals(value) ? null : Integer.valueOf(value));
        } catch (Exception e) {
            log.debug("[账号状态缓存] Redis 读取失败，降级为回源 - userId: {}, error: {}", userId, e.getMessage());
            return null;
        }
    }

    /**
     * 写入 L2 缓存。
     *
     * @param redis   Redis 模板，允许为空
     * @param userId  用户主键
     * @param snapshot 状态快照
     */
    private void writeRedis(StringRedisTemplate redis, Integer userId, Snapshot snapshot) {
        if (redis == null) {
            return;
        }
        try {
            String value = snapshot.status() == null ? NOT_FOUND : String.valueOf(snapshot.status());
            redis.opsForValue().set(key(userId), value, Duration.ofSeconds(redisTtlSeconds()));
        } catch (Exception e) {
            log.debug("[账号状态缓存] Redis 写入失败 - userId: {}, error: {}", userId, e.getMessage());
        }
    }

    /**
     * L1 缓存有效期。
     *
     * @param redisAvailable Redis 是否可用
     * @return 有效期（毫秒）
     */
    private long localTtlMillis(boolean redisAvailable) {
        if (!redisAvailable) {
            return FALLBACK_LOCAL_TTL_MILLIS;
        }
        SystemSecurityProperties properties = properties();
        int seconds = properties == null ? 5 : properties.getUserStateLocalTtlSeconds();
        return Math.max(0, seconds) * 1000L;
    }

    /**
     * L2 缓存有效期。
     *
     * @return 有效期（秒）
     */
    private long redisTtlSeconds() {
        SystemSecurityProperties properties = properties();
        int seconds = properties == null ? 30 : properties.getUserStateRedisTtlSeconds();
        return Math.max(1, seconds);
    }

    /**
     * 获取系统安全配置。
     *
     * @return 配置对象，不可用时返回 {@code null}
     */
    private SystemSecurityProperties properties() {
        try {
            return propertiesProvider.getIfAvailable();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 构建 Redis 键。
     *
     * @param userId 用户主键
     * @return Redis 键
     */
    private String key(Integer userId) {
        return KEY_PREFIX + userId;
    }
}
