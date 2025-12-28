package com.chua.starter.monitor.service.acme;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ACME 验证缓存服务
 * 用于本机部署模式，存储 token -> keyAuthorization 的映射
 *
 * @author CH
 * @since 2025-12-28
 */
@Slf4j
@Service
public class AcmeChallengeService {

    /**
     * 验证信息缓存: token -> keyAuthorization
     */
    private final Map<String, ChallengeInfo> challengeCache = new ConcurrentHashMap<>();

    /**
     * 定时清理过期的验证信息（默认10分钟过期）
     */
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor();

    public AcmeChallengeService() {
        // 每分钟清理过期的验证信息
        cleaner.scheduleAtFixedRate(this::cleanExpired, 1, 1, TimeUnit.MINUTES);
    }

    /**
     * 存储验证信息
     *
     * @param token            验证 token
     * @param keyAuthorization 验证内容
     */
    public void put(String token, String keyAuthorization) {
        challengeCache.put(token, new ChallengeInfo(keyAuthorization, System.currentTimeMillis()));
        log.info("[ACME-CHALLENGE] 存储验证信息: token={}", token);
    }

    /**
     * 获取验证内容
     *
     * @param token 验证 token
     * @return keyAuthorization，不存在返回 null
     */
    public String getKeyAuthorization(String token) {
        ChallengeInfo info = challengeCache.get(token);
        return info != null ? info.keyAuthorization : null;
    }

    /**
     * 移除验证信息
     *
     * @param token 验证 token
     */
    public void remove(String token) {
        challengeCache.remove(token);
        log.info("[ACME-CHALLENGE] 移除验证信息: token={}", token);
    }

    /**
     * 清理过期的验证信息（10分钟过期）
     */
    private void cleanExpired() {
        long now = System.currentTimeMillis();
        long expireTime = 10 * 60 * 1000; // 10分钟
        challengeCache.entrySet().removeIf(entry -> {
            boolean expired = now - entry.getValue().createTime > expireTime;
            if (expired) {
                log.info("[ACME-CHALLENGE] 清理过期验证: token={}", entry.getKey());
            }
            return expired;
        });
    }

    /**
     * 验证信息
     */
    private record ChallengeInfo(String keyAuthorization, long createTime) {
    }
}
