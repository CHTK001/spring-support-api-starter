package com.chua.starter.monitor.agent;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 系统指标后端发现配置。
 *
 * <p>前缀 {@code system.metric}，控制「系统指标」页面连接哪一种观测后端：</p>
 * <ul>
 *   <li>{@code auto}（缺省）：按 hotsport → skywalker → none 的顺序探测，
 *       第一个可达的作为当前数据源，并缓存一段时间；</li>
 *   <li>{@code none}：不启用任何观测后端，接口返回降级空数据；</li>
 *   <li>{@code hotsport}：直连 hotsport 观测后端（反代同一套
 *       {@code /api/apm/*} 契约）；</li>
 *   <li>{@code skywalker}：直连 SkyWalking OAP GraphQL，由 SPI 实现负责把
 *       OAP 数据翻译成前端契约。</li>
 * </ul>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Data
@ConfigurationProperties(prefix = "system.metric")
public class SystemMetricProperties {

    /**
     * 是否启用系统指标接口（{@code /api/apm/**}）。
     *
     * <p>缺省为 {@code true}：引入监控模块即生效。设为 {@code false} 可整体关闭接口，
     * 与 {@link #provider} 不同——后者只切换数据源，接口始终存在。</p>
     */
    private boolean enable = true;

    /**
     * 数据源模式：auto / none / hotsport / skywalker。
     */
    private String provider = "auto";

    /**
     * 单个后端的探测超时（毫秒）。
     */
    private int probeTimeoutMs = 800;

    /**
     * 自动探测结果缓存时长（秒），避免每次请求都去碰后端。
     */
    private int cacheSeconds = 30;

    /**
     * 数据转发超时（毫秒）。
     */
    private int forwardTimeoutMs = 10000;

    /**
     * hotsport 后端配置。
     */
    private Hotsport hotsport = new Hotsport();

    /**
     * SkyWalking OAP 配置。
     */
    private Skywalking skywalking = new Skywalking();

    /**
     * hotsport 观测后端。
     */
    @Data
    public static class Hotsport {

        /**
         * 后端源地址，例如 {@code http://127.0.0.1:8582}。
         * 若以 {@code /api/apm} 结尾则视为已含统一前缀，否则转发时自动拼接。
         */
        private String baseUrl = "";
    }

    /**
     * SkyWalking OAP。
     */
    @Data
    public static class Skywalking {

        /**
         * OAP HTTP 地址，例如 {@code http://127.0.0.1:12800}。
         */
        private String oapUrl = "";
    }
}