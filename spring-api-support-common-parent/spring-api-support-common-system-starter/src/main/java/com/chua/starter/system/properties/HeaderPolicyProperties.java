package com.chua.starter.system.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 消息头拦截（{@code HeaderPolicyFilter}）的部署级配置。
 *
 * <p>分两层：</p>
 * <ul>
 *   <li><b>硬闸（这里）</b>：{@code firstPartyEnabled} 是配置项，改它要重启。
 *       作用是「无论运行期开关怎么设，都不给出后门」的最终保证。</li>
 *   <li><b>运行期操作杆</b>：热重载编码 {@code policy.header.first-party-enabled}，
 *       在「策略管理 → 热更新配置」里改即时生效。两个开关取「与」。</li>
 * </ul>
 *
 * <p>头名与标记放在这里而不是运行期：它们是协议的一部分（前端要按同样的规则发），
 * 改动需要前后端同步，走配置重启比走页面更合适。</p>
 *
 * @since 4.0.0.43
 * @author CH
 */
@Data
@ConfigurationProperties(prefix = HeaderPolicyProperties.PRE)
public class HeaderPolicyProperties {

    /**
     * PRE
     */
    public static final String PRE = "plugin.system.header-policy";

    /**
     * 第一方豁免硬闸
     *
     * <p>关掉后即使运行期开关是开的，也不再有任何请求能跳过消息头规则。
     * 与热重载编码 {@code policy.header.first-party-enabled} 取「与」。</p>
     */
    private boolean firstPartyEnabled = true;

    /**
     * 第一方判定字段一：承载请求头名
     *
     * <p>选它是因为本系统本来就有 {@code X-Trace-Id} 链路追踪（MDC 过滤器在读它），
     * 属于「本来就会出现、不显眼」的头。取值只要以 {@link #firstPartyMarker} 结尾即通过。</p>
     */
    private String firstPartyHeaderA = "X-Trace-Id";

    /**
     * 第一方判定字段二：承载请求头名
     *
     * <p>选它是因为 {@code X-Requested-With} 是 AJAX 的事实标准头（jQuery/axios 都发），
     * 一个没有它的请求本身就可疑，所以把标记藏在这里不显眼。
     * 取值同样只要以 {@link #firstPartyMarker} 结尾即通过。</p>
     */
    private String firstPartyHeaderB = "X-Requested-With";

    /**
     * 第一方标记
     *
     * <p>两个字段的取值都要以它结尾。<b>两个都满足</b>才算第一方：
     * 只满足一个不足以豁免，避免扫描器只要猜中一个就能整体绕过。</p>
     */
    private String firstPartyMarker = "-7f3a";
}
