package com.chua.starter.system.support;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 用户账号状态缓存
 *
 * <p>请求拦截时需要判断账号当前是否被禁用，这里做 20 秒进程内缓存，避免每个请求查库。
 * 管理员执行禁用、启用等操作后必须调用 {@link #evict} 失效，使踢下线立即生效。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@Component
public class UserStateCache {

    /**
     * 缓存有效期（毫秒）
     */
    private static final long TTL_MILLIS = 20_000L;

    /**
     * 缓存条目
     */
    private final Map<Integer, Entry> entries = new ConcurrentHashMap<>();

    /**
     * 读取账号状态，未命中或已过期时回源。
     *
     * @param userId 用户主键，为空时直接回源
     * @param loader 回源加载器
     * @return 账号状态快照
     */
    public Snapshot resolve(Integer userId, Supplier<Snapshot> loader) {
        if (userId == null) {
            return loader.get();
        }
        Entry entry = entries.get(userId);
        if (entry != null && entry.expireAt() > System.currentTimeMillis()) {
            return entry.snapshot();
        }
        Snapshot snapshot = loader.get();
        entries.put(userId, new Entry(snapshot, System.currentTimeMillis() + TTL_MILLIS));
        return snapshot;
    }

    /**
     * 失效指定用户的状态缓存。
     *
     * @param userIds 用户主键集合，允许为空
     */
    public void evict(Collection<Integer> userIds) {
        if (userIds == null) {
            return;
        }
        userIds.stream().filter(Objects::nonNull).forEach(entries::remove);
    }

    /**
     * 失效指定用户的状态缓存。
     *
     * @param userId 用户主键，允许为空
     */
    public void evict(Integer userId) {
        if (userId != null) {
            entries.remove(userId);
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
}
