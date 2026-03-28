package com.chua.starter.monitor.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 爬虫模块配置
 *
 * @author CH
 * @since 2026/03/27
 */
@Data
@ConfigurationProperties(prefix = SpiderProperties.PRE)
public class SpiderProperties {

    public static final String PRE = "plugin.monitor.spider";

    /**
     * 是否启用爬虫模块
     */
    private boolean enabled = true;

    /**
     * 启动时自动初始化数据库表
     */
    private boolean autoCreateTable = false;

    /**
     * 预览/解析页面时的请求超时时间（秒）
     */
    private int requestTimeoutSeconds = 30;

    /**
     * 可视化 DOM 解析最大深度
     */
    private int previewMaxDepth = 10;

    /**
     * 每层最多保留的子节点数量
     */
    private int previewMaxChildren = 50;

    /**
     * 文本预览最大长度
     */
    private int previewTextLimit = 200;

    /**
     * spider 轻页面认证配置
     */
    private WebAuthConfig webAuth = new WebAuthConfig();

    @Data
    public static class WebAuthConfig {

        /**
         * 认证模式：embedded / none
         */
        private String mode = "embedded";

        /**
         * embedded 模式用户名
         */
        private String username = "admin";

        /**
         * embedded 模式密码
         */
        private String password = "admin123";

        /**
         * Session 超时时间（秒）
         */
        private Integer sessionTimeout = 3600;
    }
}
