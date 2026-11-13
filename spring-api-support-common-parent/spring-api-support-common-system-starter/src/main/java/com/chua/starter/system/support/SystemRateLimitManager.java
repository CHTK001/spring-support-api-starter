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
    // [P3C A20 豁免]
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

    /**
     * 限流配置快照。
     *
     * <p>不可变值对象，保存「一次热更新后的全部限流参数 + 对应的令牌桶实例」，
     * 由 {@code volatile} 字段 {@code state} 整体替换，读侧（{@code tryAcquire()} / {@code getStatus()}）
     * 先取快照再判断，因此不需要加锁。</p>
     *
     * <p>生效判定：真正开始限流需同时满足 {@link #enabled()} 为 {@code true} 且 {@link #qps()} 大于 0，
     * 即 {@link #isActive()}；只满足其一都按不限流处理。</p>
     *
     * @param enabled     限流总开关，来自 {@code /v2/policy/ratelimit/global} 接口写入的配置。
     *                    原始 {@code boolean} 不允许为 {@code null}；为 {@code false} 时限流器整体旁路
     * @param qps         限流速率，每秒允许通过的请求数（对应 Guava 的 {@code permitsPerSecond}）。
     *                    原始 {@code int}；小于等于 0 表示速率非法，此时 {@link #rateLimiter()} 为 {@code null}
     *                    且 {@link #isActive()} 返回 {@code false}（按不限流处理，不会抛 NPE）
     * @param burstSize   突发容量，即允许瞬间通过的额外请求数。当前实现只把它保存在快照里并由
     *                    {@code getStatus()} 原样回显给前端，{@link #rateLimiter()} 仍是按 {@link #qps} 单参构造的，
     *                    因此该值尚未真正参与令牌桶容量计算，属预留字段
     * @param dimension   限流维度标识，标记这组参数作用在哪个维度上（初始快照与全局接口固定为
     *                    {@code "global"}，即全系统共用一个令牌桶）。允许为 {@code null}；
     *                    当前实现同样只做快照与回显，不参与令牌桶的选取
     * @param rateLimiter Guava 令牌桶实例，承载真实的限流判定。**允许为 {@code null}**：
     *                    仅当 {@link #qps()} 大于 0 时由 {@code RateLimiter.create(qps)} 创建，否则为 {@code null}；
     *                    {@code tryAcquire()} 只在 {@link #isActive()} 为 {@code true}（此时必然非 {@code null}）时调用它
     */
    private record State(boolean enabled, int qps, int burstSize, String dimension, RateLimiter rateLimiter) {
        private boolean isActive() {
            return enabled && qps > 0;
        }
    }
}
