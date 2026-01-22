package com.chua.starter.oauth.sso.support.security;

import com.chua.common.support.core.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 登录尝试服务
 * <p>
 * 用于跟踪用户登录尝试，防止暴力破解攻击。
 * 当用户连续登录失败超过阈值时，账户将被临时锁定。
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2025-12-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    /**
     * Redis 键前缀 - 登录失败次数
     */
    private static final String LOGIN_ATTEMPT_PREFIX = "oauth:login:attempt:";

    /**
     * Redis 键前缀 - 账户锁定
     */
    private static final String ACCOUNT_LOCKED_PREFIX = "oauth:login:locked:";

    /**
     * 默认最大失败次数
     */
    private static final int DEFAULT_MAX_ATTEMPTS = 5;

    /**
     * 默认锁定时间（分钟）
     */
    private static final int DEFAULT_LOCK_DURATION_MINUTES = 15;

    /**
     * 失败记录过期时间（分钟）
     */
    private static final int ATTEMPT_EXPIRE_MINUTES = 30;

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 记录登录失败
     *
     * @param username 用户名
     * @return 剩余尝试次数，0表示已锁定
     */
    public int recordFailedAttempt(String username) {
        return recordFailedAttempt(username, DEFAULT_MAX_ATTEMPTS, DEFAULT_LOCK_DURATION_MINUTES);
    }

    /**
     * 记录登录失败
     *
     * @param username            用户名
     * @param maxAttempts         最大尝试次数
     * @param lockDurationMinutes 锁定时长（分钟）
     * @return 剩余尝试次数，0表示已锁定
     */
    public int recordFailedAttempt(String username, int maxAttempts, int lockDurationMinutes) {
        if (StringUtils.isBlank(username)) {
            return maxAttempts;
        }

        String attemptKey = LOGIN_ATTEMPT_PREFIX + username;
        String lockKey = ACCOUNT_LOCKED_PREFIX + username;

        try {
            // 增加失败次数
            Long attempts = stringRedisTemplate.opsForValue().increment(attemptKey);
            if (attempts == null) {
                attempts = 1L;
            }

            // 设置过期时间
            stringRedisTemplate.expire(attemptKey, Duration.ofMinutes(ATTEMPT_EXPIRE_MINUTES));

            int remaining = maxAttempts - attempts.intValue();
            log.debug("登录失败记录: username={}, attempts={}, remaining={}", username, attempts, remaining);

            // 检查是否需要锁定账户
            if (attempts >= maxAttempts) {
                // 锁定账户
                stringRedisTemplate.opsForValue().set(lockKey, String.valueOf(System.currentTimeMillis()),
                        Duration.ofMinutes(lockDurationMinutes));
                log.warn("账户已锁定: username={}, lockDuration={}分钟", username, lockDurationMinutes);
                return 0;
            }

            return Math.max(0, remaining);
        } catch (Exception e) {
            log.error("记录登录失败异常: username={}, error={}", username, e.getMessage());
            return maxAttempts;
        }
    }

    /**
     * 检查账户是否被锁定
     *
     * @param username 用户名
     * @return true-已锁定，false-未锁定
     */
    public boolean isAccountLocked(String username) {
        if (StringUtils.isBlank(username)) {
            return false;
        }

        String lockKey = ACCOUNT_LOCKED_PREFIX + username;
        try {
            Boolean exists = stringRedisTemplate.hasKey(lockKey);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("检查账户锁定状态异常: username={}, error={}", username, e.getMessage());
            return false;
        }
    }

    /**
     * 获取账户锁定剩余时间（秒）
     *
     * @param username 用户名
     * @return 剩余锁定时间（秒），-1表示未锁定
     */
    public long getLockRemainingSeconds(String username) {
        if (StringUtils.isBlank(username)) {
            return -1;
        }

        String lockKey = ACCOUNT_LOCKED_PREFIX + username;
        try {
            Long ttl = stringRedisTemplate.getExpire(lockKey, TimeUnit.SECONDS);
            return ttl != null && ttl > 0 ? ttl : -1;
        } catch (Exception e) {
            log.error("获取锁定剩余时间异常: username={}, error={}", username, e.getMessage());
            return -1;
        }
    }

    /**
     * 登录成功后清除失败记录
     *
     * @param username 用户名
     */
    public void clearFailedAttempts(String username) {
        if (StringUtils.isBlank(username)) {
            return;
        }

        String attemptKey = LOGIN_ATTEMPT_PREFIX + username;
        try {
            stringRedisTemplate.delete(attemptKey);
            log.debug("清除登录失败记录: username={}", username);
        } catch (Exception e) {
            log.error("清除登录失败记录异常: username={}, error={}", username, e.getMessage());
        }
    }

    /**
     * 手动解锁账户
     *
     * @param username 用户名
     * @return 是否解锁成功
     */
    public boolean unlockAccount(String username) {
        if (StringUtils.isBlank(username)) {
            return false;
        }

        String lockKey = ACCOUNT_LOCKED_PREFIX + username;
        String attemptKey = LOGIN_ATTEMPT_PREFIX + username;
        try {
            stringRedisTemplate.delete(lockKey);
            stringRedisTemplate.delete(attemptKey);
            log.info("账户已解锁: username={}", username);
            return true;
        } catch (Exception e) {
            log.error("解锁账户异常: username={}, error={}", username, e.getMessage());
            return false;
        }
    }

    /**
     * 获取当前失败次数
     *
     * @param username 用户名
     * @return 失败次数
     */
    public int getFailedAttempts(String username) {
        if (StringUtils.isBlank(username)) {
            return 0;
        }

        String attemptKey = LOGIN_ATTEMPT_PREFIX + username;
        try {
            String value = stringRedisTemplate.opsForValue().get(attemptKey);
            return value != null ? Integer.parseInt(value) : 0;
        } catch (Exception e) {
            log.error("获取失败次数异常: username={}, error={}", username, e.getMessage());
            return 0;
        }
    }

    /**
     * 获取锁定信息
     *
     * @param username 用户名
     * @return 锁定信息
     */
    public LockInfo getLockInfo(String username) {
        boolean locked = isAccountLocked(username);
        long remainingSeconds = locked ? getLockRemainingSeconds(username) : -1;
        int failedAttempts = getFailedAttempts(username);
        return new LockInfo(locked, remainingSeconds, failedAttempts, DEFAULT_MAX_ATTEMPTS);
    }

    /**
     * 锁定信息
     */
    public record LockInfo(
            boolean locked,
            long remainingSeconds,
            int failedAttempts,
            int maxAttempts
    ) {
        /**
         * 获取剩余尝试次数
         */
        public int remainingAttempts() {
            return Math.max(0, maxAttempts - failedAttempts);
        }

        /**
         * 获取格式化的锁定剩余时间
         */
        public String formattedRemainingTime() {
            if (remainingSeconds <= 0) {
                return "";
            }
            long minutes = remainingSeconds / 60;
            long seconds = remainingSeconds % 60;
            if (minutes > 0) {
                return minutes + "分" + seconds + "秒";
            }
            return seconds + "秒";
        }
    }
}
