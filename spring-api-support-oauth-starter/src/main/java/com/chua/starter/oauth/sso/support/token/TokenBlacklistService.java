package com.chua.starter.oauth.sso.support.token;

import com.chua.common.support.utils.DigestUtils;
import com.chua.common.support.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Token黑名单服务
 * <p>
 * 用于管理已失效的Token，防止已登出的Token被重复使用。
 * 使用Redis存储黑名单，支持自动过期清理。
 * </p>
 *
 * @author CH
 * @since 2024/12/07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * Redis Key前缀
     */
    private static final String BLACKLIST_PREFIX = "oauth:token:blacklist:";

    /**
     * 黑名单原因枚举
     */
    public enum BlacklistReason {
        /**
         * 用户主动登出
         */
        LOGOUT,
        /**
         * 被管理员踢出
         */
        KICKED,
        /**
         * Token过期
         */
        EXPIRED,
        /**
         * Token被撤销
         */
        REVOKED,
        /**
         * 用户密码变更
         */
        PASSWORD_CHANGED,
        /**
         * 安全原因
         */
        SECURITY
    }

    /**
     * 将Token加入黑名单
     *
     * @param token          Token值
     * @param reason         加入黑名单原因
     * @param expireSeconds  过期时间（秒），应与原Token剩余有效期一致
     * @param userId         用户ID（可选）
     * @param username       用户名（可选）
     */
    public void addToBlacklist(String token, BlacklistReason reason, long expireSeconds, 
                               String userId, String username) {
        if (StringUtils.isBlank(token)) {
            log.warn("【Token黑名单】Token为空，跳过加入黑名单");
            return;
        }

        try {
            String tokenHash = hashToken(token);
            String key = BLACKLIST_PREFIX + tokenHash;
            
            // 存储黑名单信息
            String value = String.format("%s|%s|%s|%s", 
                    reason.name(), 
                    userId != null ? userId : "", 
                    username != null ? username : "",
                    LocalDateTime.now().toString());
            
            // 设置过期时间（与原Token剩余有效期一致，避免内存浪费）
            stringRedisTemplate.opsForValue().set(key, value, expireSeconds, TimeUnit.SECONDS);
            
            log.info("【Token黑名单】Token已加入黑名单 - 原因: {}, 用户: {}, 过期时间: {}秒, TokenHash: {}***",
                    reason, username, expireSeconds, tokenHash.substring(0, 8));
        } catch (Exception e) {
            log.error("【Token黑名单】加入黑名单失败 - 原因: {}, 异常: {}", reason, e.getMessage(), e);
        }
    }

    /**
     * 将Token加入黑名单（使用默认过期时间）
     *
     * @param token   Token值
     * @param reason  加入黑名单原因
     */
    public void addToBlacklist(String token, BlacklistReason reason) {
        // 默认保留24小时
        addToBlacklist(token, reason, 86400, null, null);
    }

    /**
     * 检查Token是否在黑名单中
     *
     * @param token Token值
     * @return true-在黑名单中（Token已失效），false-不在黑名单中
     */
    public boolean isBlacklisted(String token) {
        if (StringUtils.isBlank(token)) {
            return false;
        }

        try {
            String tokenHash = hashToken(token);
            String key = BLACKLIST_PREFIX + tokenHash;
            Boolean exists = stringRedisTemplate.hasKey(key);
            
            if (Boolean.TRUE.equals(exists)) {
                log.debug("【Token黑名单】Token已在黑名单中 - TokenHash: {}***", tokenHash.substring(0, 8));
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("【Token黑名单】检查黑名单失败 - 异常: {}", e.getMessage());
            // 检查失败时返回false，避免影响正常业务
            return false;
        }
    }

    /**
     * 从黑名单中移除Token（一般不需要调用）
     *
     * @param token Token值
     */
    public void removeFromBlacklist(String token) {
        if (StringUtils.isBlank(token)) {
            return;
        }

        try {
            String tokenHash = hashToken(token);
            String key = BLACKLIST_PREFIX + tokenHash;
            stringRedisTemplate.delete(key);
            log.info("【Token黑名单】Token已从黑名单移除 - TokenHash: {}***", tokenHash.substring(0, 8));
        } catch (Exception e) {
            log.error("【Token黑名单】移除黑名单失败 - 异常: {}", e.getMessage());
        }
    }

    /**
     * 批量将用户的所有Token加入黑名单
     * <p>
     * 适用于用户修改密码、管理员强制下线等场景
     * </p>
     *
     * @param userId        用户ID
     * @param reason        原因
     * @param expireSeconds 过期时间
     */
    public void blacklistUserTokens(String userId, BlacklistReason reason, long expireSeconds) {
        if (StringUtils.isBlank(userId)) {
            return;
        }

        try {
            String userKey = BLACKLIST_PREFIX + "user:" + userId;
            stringRedisTemplate.opsForValue().set(userKey, reason.name(), expireSeconds, TimeUnit.SECONDS);
            log.info("【Token黑名单】用户所有Token已加入黑名单 - 用户ID: {}, 原因: {}", userId, reason);
        } catch (Exception e) {
            log.error("【Token黑名单】批量加入黑名单失败 - 用户ID: {}, 异常: {}", userId, e.getMessage());
        }
    }

    /**
     * 检查用户是否被全局拉黑
     *
     * @param userId 用户ID
     * @return true-用户被拉黑，false-用户正常
     */
    public boolean isUserBlacklisted(String userId) {
        if (StringUtils.isBlank(userId)) {
            return false;
        }

        try {
            String userKey = BLACKLIST_PREFIX + "user:" + userId;
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(userKey));
        } catch (Exception e) {
            log.error("【Token黑名单】检查用户黑名单失败 - 用户ID: {}, 异常: {}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * 计算Token的哈希值（用于存储和查询）
     *
     * @param token Token原值
     * @return Token的SHA256哈希值
     */
    private String hashToken(String token) {
        return DigestUtils.sha256Hex(token);
    }
}
