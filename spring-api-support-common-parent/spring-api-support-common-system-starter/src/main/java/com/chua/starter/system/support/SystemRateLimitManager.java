package com.chua.starter.system.support;

import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 系统端全局限流管理器（令牌桶算法）。
 *
 * <p>system-starter 不依赖 oauth-starter，故在 common-system-starter 内独立实现，
 * 供 {@code /v2/policy/ratelimit/global} 接口读写。支持运行时热更新。</p>
 *
 * @since 4.0.0.51
 * @author CH
 */
@Component
@Slf4j
public class SystemRateLimitManager {

    /**
     * 当前配置快照
     */
    private volatile State state = new State(false, 100, 200, "global", RateLimiter.create(100));

    /**
     * 尝试获取令牌（非阻塞）
     *
     * @return true=允许通过，false=被限流
     */
    public boolean tryAcquire() {
        State snapshot = state;
        if (!snapshot.isActive()) {
            return true;
        }
        return snapshot.rateLimiter().tryAcquire();
    }

    /**
     * 热更新限流配置
     *
     * @param enabled   是否启用
     * @param qps       QPS
     * @param burstSize 突发容量
     * @param dimension 维度
     */
    public void update(boolean enabled, int qps, int burstSize, String dimension) {
        RateLimiter rateLimiter = qps > 0 ? RateLimiter.create(qps) : null;
        state = new State(enabled, qps, burstSize, dimension, rateLimiter);
        log.info("[SystemRateLimit] 配置已更新: enabled={}, qps={}, burstSize={}, dimension={}",
                enabled, qps, burstSize, dimension);
    }

    /**
     * 获取当前限流状态
     *
     * @return 状态
     */
    public Map<String, Object> getStatus() {
        State snapshot = state;
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("enabled", snapshot.enabled());
        status.put("qps", snapshot.qps());
        status.put("burstSize", snapshot.burstSize());
        status.put("dimension", snapshot.dimension());
        return status;
    }

    /**
     * 是否已启用
     *
     * @return 是否已启用
     */
    public boolean isEnabled() {
        return state.isActive();
    }

    private record State(boolean enabled, int qps, int burstSize, String dimension, RateLimiter rateLimiter) {
        private boolean isActive() {
            return enabled && qps > 0;
        }
    }
}
