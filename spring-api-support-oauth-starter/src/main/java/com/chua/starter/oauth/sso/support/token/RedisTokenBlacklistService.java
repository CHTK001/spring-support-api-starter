package com.chua.starter.oauth.sso.support.token;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

/**
 * 基于 Redis 的 Token 黑名单服务实现
 * <p>使用 Redis 存储黑名单，利用 TTL 自动清理过期记录</p>
 *
 * @author CH
 * @since 2025/03/18
 */
@Slf4j
@Service
@Primary
public class RedisTokenBlacklistService implements TokenBlacklistService {

    /**
     * Token 黑名单 Redis Key 前缀
     */
    private static final String BLACKLIST_TOKEN_PREFIX = "oauth:blacklist:token:";

    /**
     * 用户黑名单 Redis Key 前缀
     */
    private static final String BLACKLIST_USER_PREFIX = "oauth:blacklist:user:";

    /**
     * 默认黑名单过期时间（秒），24小时
     */
    private static final long DEFAULT_EXPIRE_SECONDS = 86400L;

    @Autowired(required = false)
    private RedisTemplate<String, Object> stringRedisTemplate;

    @Override
    public void addToBlacklist(String token, BlacklistReason reason) {
        addToBlacklist(token, reason, DEFAULT_EXPIRE_SECONDS, null, null);
    }

    @Override
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

            // 设置过期时间（与原 Token 的过期时间一致）
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
    public void clearAll() {
        try {
            // 删除所有 Token 黑名单
            Set<String> tokenKeys = stringRedisTemplate.keys(BLACKLIST_TOKEN_PREFIX + "*");
            if (tokenKeys != null && !tokenKeys.isEmpty()) {
                stringRedisTemplate.delete(tokenKeys);
            }

            // 删除所有用户黑名单
            Set<String> userKeys = stringRedisTemplate.keys(BLACKLIST_USER_PREFIX + "*");
            if (userKeys != null && !userKeys.isEmpty()) {
                stringRedisTemplate.delete(userKeys);
            }

            log.warn("[BLACKLIST] 所有黑名单记录已清空");
        } catch (Exception e) {
            log.error("[BLACKLIST] 清空黑名单失败: error={}", e.getMessage());
        }
    }

    /**
     * 脱敏 Token
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 16) {
            return "***";
        }
        return token.substring(0, 8) + "..." + token.substring(token.length() - 8);
    }

    /**
     * 黑名单条目
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class BlacklistEntry implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        
        private String token;
        private String reason;
        private String userId;
        private String username;
        private Long timestamp;
    }

    /**
     * 用户黑名单条目
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class UserBlacklistEntry implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        
        private String userId;
        private String reason;
        private Long timestamp;
    }
}
