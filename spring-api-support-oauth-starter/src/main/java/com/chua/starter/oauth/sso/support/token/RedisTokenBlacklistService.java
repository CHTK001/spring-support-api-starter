package com.chua.starter.oauth.sso.support.token;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 基于 Redis 的 令牌 黑名单服务实现
 * <p>使用 Redis 存储黑名单，利用 TTL 自动清理过期记录</p>
 *
 * @since 2025/03/18
 * @author CH
 */
@Slf4j
@Service
@Primary
public class RedisTokenBlacklistService implements TokenBlacklistService {

    /**
     * 令牌 黑名单 Redis 键 前缀
     */
    private static final String BLACKLIST_TOKEN_PREFIX = "oauth:blacklist:token:";

    /**
     * 用户黑名单 Redis 键 前缀
     */
    private static final String BLACKLIST_USER_PREFIX = "oauth:blacklist:user:";

    /**
     * 默认黑名单过期时间（秒），24小时
     */
    private static final long DEFAULT_EXPIRE_SECONDS = 86400L;

    @Autowired(required = false)
    private RedisTemplate<String, Object> stringRedisTemplate; // 字符串redistemplate

    @Override
    /**
     * 将 令牌 加入黑名单，使用默认过期时间（24小时）。
     *
     * @param token  待加入黑名单的 令牌
     * @param reason 加入黑名单的原因
     */
    public void addToBlacklist(String token, BlacklistReason reason) {
        addToBlacklist(token, reason, DEFAULT_EXPIRE_SECONDS, null, null);
    }

    @Override
    /**
     * 将 令牌 加入黑名单，支持指定过期时间、用户标识和用户名。
     *
     * @param token       待加入黑名单的 令牌
     * @param reason      加入黑名单的原因
     * @param expireTime  过期时间（秒），<=0 时不设置过期
     * @param userId      用户标识（可选）
     * @param username    用户名（可选）
     */
    public void addToBlacklist(String token, BlacklistReason reason, long expireTime, String userId, String username) {
        if (token == null || token.isEmpty()) {
            return;
        }

        try {
            String key = BLACKLIST_TOKEN_PREFIX + token;
            BlacklistEntry entry = new BlacklistEntry();
            entry.setToken(token);
            entry.setReason(reason.name());
            entry.setUserId(userId);
            entry.setUsername(username);
            entry.setTimestamp(System.currentTimeMillis());

            stringRedisTemplate.opsForValue().set(key, entry);

 // 设置过期时间（与原 令牌 的过期时间一致）
            if (expireTime > 0) {
                stringRedisTemplate.expire(key, Duration.ofSeconds(expireTime));
            }

            log.debug("[BLACKLIST] Token 已加入黑名单: reason={}, userId={}, token={}...",
                    reason, userId, maskToken(token));
        } catch (Exception e) {
            log.error("[BLACKLIST] 添加 Token 到黑名单失败: token={}..., error={}",
                    maskToken(token), e.getMessage());
        }
    }

    @Override
    /**
     * 检查 令牌 是否在黑名单中。
     *
     * @param token 待检查的 令牌
     * @return true 表示已被拉黑，false 表示未找到
     */
    public boolean isBlacklisted(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        try {
            String key = BLACKLIST_TOKEN_PREFIX + token;
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("[BLACKLIST] 检查 Token 黑名单失败: token={}..., error={}",
                    maskToken(token), e.getMessage());
            // 安全优先：黑名单校验失败时拒绝放行，避免黑名单穿透
            return true;
        }
    }

    @Override
    /**
     * 将用户加入全局黑名单（不设过期时间，需手动移除）。
     *
     * @param userId 用户标识
     * @param reason 加入黑名单的原因
     */
    public void addUserToBlacklist(String userId, String reason) {
        if (userId == null || userId.isEmpty()) {
            return;
        }

        try {
            String key = BLACKLIST_USER_PREFIX + userId;
            UserBlacklistEntry entry = new UserBlacklistEntry();
            entry.setUserId(userId);
            entry.setReason(reason);
            entry.setTimestamp(System.currentTimeMillis());

            // 用户黑名单不设置过期时间（需要手动移除）
            stringRedisTemplate.opsForValue().set(key, entry);

            log.info("[BLACKLIST] 用户已加入全局黑名单: userId={}, reason={}", userId, reason);
        } catch (Exception e) {
            log.error("[BLACKLIST] 添加用户到黑名单失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    @Override
    /**
     * 从全局黑名单中移除用户。
     *
     * @param userId 用户标识
     */
    public void removeUserFromBlacklist(String userId) {
        if (userId == null || userId.isEmpty()) {
            return;
        }

        try {
            String key = BLACKLIST_USER_PREFIX + userId;
            stringRedisTemplate.delete(key);
            log.info("[BLACKLIST] 用户已从全局黑名单移除: userId={}", userId);
        } catch (Exception e) {
            log.error("[BLACKLIST] 从黑名单移除用户失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    @Override
    /**
     * 检查用户是否在全局黑名单中。
     *
     * @param userId 用户标识
     * @return true 表示已被拉黑，false 表示未找到
     */
    public boolean isUserBlacklisted(String userId) {
        if (userId == null || userId.isEmpty()) {
            return false;
        }

        try {
            String key = BLACKLIST_USER_PREFIX + userId;
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("[BLACKLIST] 检查用户黑名单失败: userId={}, error={}", userId, e.getMessage());
            // 安全优先：黑名单校验失败时拒绝放行，避免黑名单穿透
            return true;
        }
    }

    @Override
    /**
     * 从 令牌 黑名单中移除指定 令牌。
     *
     * @param token 待移除的 令牌
     */
    public void removeFromBlacklist(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }

        try {
            String key = BLACKLIST_TOKEN_PREFIX + token;
            stringRedisTemplate.delete(key);
            log.debug("[BLACKLIST] Token 已从黑名单移除: token={}...", maskToken(token));
        } catch (Exception e) {
            log.error("[BLACKLIST] 从黑名单移除 Token 失败: token={}..., error={}",
                    maskToken(token), e.getMessage());
        }
    }

    @Override
    /**
     * 清空所有 令牌 黑名单和用户黑名单记录。
     */
    public void clearAll() {
        try {
            // 不能再用 KEYS：它会阻塞整个 Redis 实例，黑名单量一大就把认证链路拖死
            int deleted = deleteByPrefix(BLACKLIST_TOKEN_PREFIX);
            deleted += deleteByPrefix(BLACKLIST_USER_PREFIX);
            log.warn("[BLACKLIST] 黑名单记录已清空, count={}", deleted);
        } catch (Exception e) {
            log.error("[BLACKLIST] 清空黑名单失败: error={}", e.getMessage());
        }
    }

    /**
     * 按前缀分批删除（SCAN 游标，不阻塞 Redis）。
     *
     * @param prefix 键前缀
     * @return 删除数量
     */
    private int deleteByPrefix(String prefix) {
        ScanOptions options = ScanOptions.scanOptions().match(prefix + "*").count(1000).build();
        List<String> batch = new ArrayList<>(1000);
        int deleted = 0;
        try (Cursor<String> cursor = stringRedisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                batch.add(cursor.next());
                if (batch.size() >= 1000) {
                    deleted += deleteBatch(batch);
                }
            }
        }
        deleted += deleteBatch(batch);
        return deleted;
    }

    /**
     * 删除一批键。
     *
     * @param batch 键集合，删除后清空
     * @return 删除数量
     */
    private int deleteBatch(List<String> batch) {
        if (batch.isEmpty()) {
            return 0;
        }
        Long removed = stringRedisTemplate.delete(batch);
        batch.clear();
        return removed == null ? 0 : removed.intValue();
    }

    /**
     * 脱敏 令牌
     * @param token 令牌
     * @return mask令牌的结果
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 16) {
            return "***";
        }
        return token.substring(0, 8) + "..." + token.substring(token.length() - 8);
    }

    /**
     * 黑名单条目
     * @author CH
     * @since 4.0.0
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class BlacklistEntry implements java.io.Serializable {
        /**
         * 串行版本UID
        */
        private static final long serialVersionUID = 1L;
        
        /**
         * 令牌
        */
        private String token;
        /**
         * ReasonML
        */
        private String reason;
        /**
         * 用户标识
        */
        private String userId;
        /**
         * 用户名
        */
        private String username;
        /**
         * 时间戳
        */
        private Long timestamp;
    }

    /**
     * 用户黑名单条目
     * @author CH
     * @since 4.0.0
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class UserBlacklistEntry implements java.io.Serializable {
        /**
         * 串行版本UID
        */
        private static final long serialVersionUID = 1L;
        
        /**
         * 用户标识
        */
        private String userId;
        /**
         * ReasonML
        */
        private String reason;
        /**
         * 时间戳
        */
        private Long timestamp;
    }
}
