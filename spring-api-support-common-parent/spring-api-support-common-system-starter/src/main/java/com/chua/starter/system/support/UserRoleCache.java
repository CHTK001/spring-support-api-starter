package com.chua.starter.system.support;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 登录角色编码缓存
 *
 * <p>登录时读取角色编码需要一次联表查询，这里做 60 秒进程内缓存。
 * 角色授权由管理端写入后必须调用 {@link #evict} 失效，否则用户重新登录仍会拿到旧角色。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@Component
public class UserRoleCache {

    /**
     * 缓存有效期（毫秒）
     */
    private static final long TTL_MILLIS = 60_000L;

    /**
     * 缓存条目
     */
    private final Map<Integer, Entry> entries = new ConcurrentHashMap<>();

    /**
     * 读取角色编码，未命中或已过期时回源。
     *
     * @param userId 用户主键，为 空 时直接回源
     * @param loader 回源加载器
     * @return 角色编码集合
     */
    public Set<String> resolve(Integer userId, Supplier<Set<String>> loader) {
        if (userId == null) {
            return loader.get();
        }
        Entry entry = entries.get(userId);
        if (entry != null && entry.expireAt() > System.currentTimeMillis()) {
            return entry.roleCodes();
        }
        Set<String> roleCodes = new HashSet<>(loader.get());
        entries.put(userId, new Entry(roleCodes, System.currentTimeMillis() + TTL_MILLIS));
        return roleCodes;
    }

    /**
     * 失效指定用户的角色缓存。
     *
     * @param userIds 用户主键集合，允许为 空
     */
    public void evict(Collection<Integer> userIds) {
        if (userIds == null) {
            return;
        }
        userIds.stream()
                .filter(Objects::nonNull)
                .forEach(entries::remove);
    }

    /**
     * 失效指定用户的角色缓存。
     *
     * @param userId 用户主键，允许为 空
     */
    public void evict(Integer userId) {
        if (userId != null) {
            entries.remove(userId);
        }
    }

    /**
     * 缓存条目
     *
     * @param roleCodes 角色编码集合
     * @param expireAt  过期时间戳
     */
    private record Entry(Set<String> roleCodes, long expireAt) {
        /**
         * 规范构造器：对角色编码集合做防御性拷贝。
         *
         * <p>value class 前置条件——集合组件必须深不可变；回源集合可能携带 空 元素，
         * 故采用可空安全包装而非 {@code Set.copyOf}。</p>
         *
         * @param roleCodes 角色编码集合
         * @param expireAt  过期时间戳
         */
        private Entry {
            roleCodes = Collections.unmodifiableSet(
                    new HashSet<>(Objects.requireNonNull(roleCodes, "roleCodes 不能为 null")));
        }
    }
}
