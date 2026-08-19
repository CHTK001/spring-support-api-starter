package com.chua.starter.oauth.sso.support.admin;

import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 运行时限流管理器（令牌桶算法）
 * <p>
 * 支持热更新：无需重启即可调整限流参数。
 * </p>
 * @author CH
 */
@Component
@Slf4j
public class RateLimitManager {

    /** 是否启用 */
    private final AtomicBoolean enabled = new AtomicBoolean(false);
    /** QPS */
    private final AtomicInteger qps = new AtomicInteger(100);
    /** Burst尺寸 */
    private final AtomicInteger burstSize = new AtomicInteger(200);
    private volatile String dimension = "global";
    private volatile RateLimiter rateLimiter = RateLimiter.create(100);

    /**
     * 尝试获取令牌（非阻塞）
     *
     * @return true=允许通过，false=被限流
     */
    public boolean tryAcquire() {
        if (!enabled.get()) {
            return true;
        }
        return rateLimiter.tryAcquire();
    }

    /**
     * 热更新限流配置
     */
    public void update(RateLimitConfig config) {
        this.enabled.set(config.isEnabled());
        this.qps.set(config.getQps());
        this.burstSize.set(config.getBurstSize());
        this.dimension = config.getDimension();
        // 重建限流器
        this.rateLimiter = RateLimiter.create(config.getQps());
        log.info("[RateLimit] 配置已更新: enabled={}, qps={}, dimension={}",
                config.isEnabled(), config.getQps(), config.getDimension());
    }

    /**
     * 获取当前限流状态
     */
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("enabled", enabled.get());
        status.put("qps", qps.get());
        status.put("burstSize", burstSize.get());
        status.put("dimension", dimension);
        return status;
    }

    /** 是否Enabled */
    public boolean isEnabled() {
        return enabled.get();
    }
}

