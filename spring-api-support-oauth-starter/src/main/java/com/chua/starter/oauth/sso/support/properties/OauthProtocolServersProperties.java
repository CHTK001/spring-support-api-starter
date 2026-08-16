package com.chua.starter.oauth.sso.support.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * OAuth 认证服务多协议 Server 配置
 *
 * <p>通过 {@code plugin.oauth.protocol-server.protocols[]} 配置启动多个协议 Server,
 * 基于 common-starter 的 {@code network.Server} 承载 OAuth 认证协议入口
 * (HTTP/KCP/WS/UDP 等,可插拔扩展)。每个协议一个 Server 实例。</p>
 *
 * <p>配置示例:</p>
 * <pre>{@code
 * plugin:
 *   oauth:
 *     protocol-server:
 *       protocols:
 *         - name: http
 *           type: jdk
 *           protocol: http
 *           host: 0.0.0.0
 *           port: 19182
 *           enable: true
 *           context-path: /
 *           auth-routes:
 *             - /login
 *             - /verify
 *         - name: kcp
 *           type: kcp
 *           protocol: kcp
 *           port: 19183
 *           enable: false
 * }</pre>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Data
@Component
@ConfigurationProperties(prefix = OauthProtocolServersProperties.PRE, ignoreUnknownFields = true)
public class OauthProtocolServersProperties {

    /**
     * 配置前缀
     */
    public static final String PRE = "plugin.oauth.protocol-server";

    /**
     * 协议 Server 配置列表
     */
    private List<ProtocolServer> protocols = new ArrayList<>();

    /**
     * 单个协议 Server 配置
     *
     * @author CH
     * @since 4.0.0.42
     */
    @Data
    public static class ProtocolServer {

        /**
         * Server 名称(唯一标识,用于日志/管理)
         */
        private String name = "http";

        /**
         * Server 实现类型(对应 network.Server 的 SPI 键,如 jdk / nio)
         */
        private String type = "jdk";

        /**
         * 协议类型(对应 network.ProtocolType: http / tcp / udp / kcp / ws 等)
         */
        private String protocol = "http";

        /**
         * 监听主机
         */
        private String host = "0.0.0.0";

        /**
         * 监听端口
         */
        private int port = 19180;

        /**
         * 是否启用
         */
        private boolean enable = true;

        /**
         * 上下文路径(HTTP 协议有效,默认 /)
         */
        private String contextPath = "/";

        /**
         * 认证路由路径列表(为空时使用默认路径 /login /verify 等)
         */
        private List<String> authRoutes = new ArrayList<>();
    }
}
