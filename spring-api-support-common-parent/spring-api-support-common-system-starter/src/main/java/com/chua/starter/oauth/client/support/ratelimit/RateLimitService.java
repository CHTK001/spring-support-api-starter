package com.chua.starter.oauth.client.support.ratelimit;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 登录限流服务
 *
 * <p>提供账号登录失败计数、账号锁定与解锁能力。
 * 作为 SPI 占位实现，运行期可被外部 Starter 覆盖。</p>
 *
 * @author CH
 * @since 2026/8/3
 */
@Slf4j
public class RateLimitService {

    private static final RateLimitService INSTANCE = new RateLimitService();

    private final ConcurrentMap<String, Integer> failureCount = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Long> lockedUntil = new ConcurrentHashMap<>();

    private static final int DEFAULT_FAILURE_LIMIT = 3;
    private static final long DEFAULT_LOCK_MINUTES = 5L;

    protected RateLimitService() {
    }

    public static RateLimitService getInstance() {
        return INSTANCE;
    }

    /**
     * 账号是否已被锁定
     *
     * @param username 用户名
     * @return 是否锁定
     */
    public boolean isAccountLocked(String username) {
        if (username == null) {
            return false;
        }
        Long until = lockedUntil.get(username);
        if (until == null) {
            return false;
        }
        if (System.currentTimeMillis() >= until) {
            lockedUntil.remove(username);
            failureCount.remove(username);
            return false;
        }
        return true;
    }

    /**
     * 记录一次登录失败
     *
     * @param username 用户名
     */
    public void recordLoginFailure(String username) {
        if (username == null) {
            return;
        }
        int count = failureCount.merge(username, 1, Integer::sum);
        if (count >= DEFAULT_FAILURE_LIMIT) {
            long until = System.currentTimeMillis() + DEFAULT_LOCK_MINUTES * 60_000L;
            lockedUntil.put(username, until);
            log.warn("[RateLimit] 账号已锁定: username={}, until={}", username, until);
        }
    }

    /**
     * 重置账号失败计数（登录成功后调用）
     *
     * @param username 用户名
     */
    public void resetAccount(String username) {
        if (username == null) {
            return;
        }
        failureCount.remove(username);
        lockedUntil.remove(username);
    }
}
