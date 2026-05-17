package com.chua.starter.rustserver.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Rust 服务器配置属性。
 *
 * <pre>{@code
 * plugin:
 *   server:
 *     rust:
 *       enabled: true
 *       port: 8080
 *       response-buffer: 524288
 * }</pre>
 */
@Data
@ConfigurationProperties(prefix = RustServerProperties.PREFIX)
public class RustServerProperties {

    public static final String PREFIX = "plugin.server.rust";

    /** 是否启用 Rust 服务器 {@code defaultValue=true} */
    private boolean enabled = false;

    /** 监听端口，默认 8080 */
    private int port = 8080;

    /** 响应缓冲区大小（字节），默认 64KB */
    private int responseBuffer = 64 * 1024;

    /** tokio worker 线程数，≤0 时自动 = CPU核心数 × 2 */
    private int workerThreads = 0;

    /** Session 超时时间（秒），默认 1800（30 分钟） */
    private int sessionTimeout = 1800;

    /** Session Cookie 名称 */
    private String sessionCookieName = "JSESSIONID";

    /** 是否启用访问日志 */
    private boolean accessLogEnabled = true;

    /** 优雅关闭超时（秒），默认 30 */
    private int shutdownTimeout = 30;

    /** SSL 配置 */
    private Ssl ssl = new Ssl();

    @Data
    public static class Ssl {
        /** 是否启用 HTTPS */
        private boolean enabled = false;

        /** 证书路径（classpath: 或 file:） */
        private String certPath;

        /** 私钥路径（classpath: 或 file:） */
        private String keyPath;
    }
}
