package com.chua.starter.service.hotreload;

import com.chua.starter.service.annotation.HotReloadConfig;
import org.springframework.stereotype.Component;

/**
 * 热重载配置读取器（业务侧注入入口）。
 *
 * <p>方法体只提供兜底默认值，实际调用会被 {@link HotReloadConfigAspect} 拦截，
 * 从 @Cacheable 缓存中读取最新配置值。配置保存/重载后无需重启即可生效。</p>
 *
 * @since 4.0.0.43
 * @author CH
 */
@Component
public class HotReloadConfigReader {

    /**
     * 全局限流默认 QPS。
     *
     * @return QPS
     */
    @HotReloadConfig("policy.rate-limit.global-qps")
    public int globalRateLimitQps() {
        return 100;
    }

    /**
     * 策略规则默认突发参数。
     *
     * @return 突发参数
     */
    @HotReloadConfig("policy.rate-limit.default-burst")
    public int defaultBurstSize() {
        return 200;
    }

    /**
     * 是否开启 IP 白名单豁免。
     *
     * @return 是否开启
     */
    @HotReloadConfig("policy.access.ip-whitelist-enabled")
    public boolean ipWhitelistEnabled() {
        return true;
    }

    /**
     * 名单规则分页大小。
     *
     * @return 分页大小
     */
    @HotReloadConfig("policy.access.page-size")
    public int accessPageSize() {
        return 20;
    }
}
