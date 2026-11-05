// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
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

    /**
     * 是否开启消息头拦截的第一方豁免。
     *
     * <p>默认拦截名单里含 curl / axios / okhttp 等通用 HTTP 客户端（用 UA 分不出
     * 「脚本」与「爬虫」），全量启用后自家前端与运维脚本也会被 403。第一方靠双字段
     * 标记豁免，但<b>是否允许这个豁免</b>应该在策略管理页面上可控，而不是只能改
     * 配置文件重启。</p>
     *
     * <p>在「策略管理 → 热更新配置」里改这一项即时生效，无需重启。
     * 不接受任何后门时置 false，规则对所有请求一视同仁。</p>
     *
     * <p>与配置文件里的 {@code plugin.system.header-policy.first-party-enabled} 是
     * 「与」关系：配置文件是硬闸（关了谁都开不了），这里是运行期操作杆。</p>
     *
     * @return 是否开启第一方豁免
     */
    @HotReloadConfig("policy.header.first-party-enabled")
    public boolean headerFirstPartyEnabled() {
        return true;
    }
}