package com.chua.starter.monitor.arthas.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Arthas 代理配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "monitor.arthas.proxy")
public class ArthasProxyProperties {

    /** 是否启用 Arthas 代理（默认启用） */
    private boolean enabled = true;

    /** 控制台代理前缀（仅用于文档/拼装参考） */
    private String proxyPrefix = "/v1/arthas/console";

    /** 允许的 WebSocket 跨域来源（默认 *） */
    private List<String> allowedOrigins = new ArrayList<>(List.of("*"));

    /** 上游 WebSocket 连接超时（秒） */
    private int connectTimeoutSeconds = 10;
}

