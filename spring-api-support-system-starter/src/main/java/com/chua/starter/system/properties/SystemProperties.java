package com.chua.starter.system.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 系统服务配置属性
 *
 * <p>
 * 定义了系统服务的所有配置参数，包括功能开关、缓存配置、日志配置等。
 * </p>
 *
 * <h3>配置前缀：</h3>
 * <p>
 * <code>plugin.system.server</code>
 * </p>
 *
 * <h3>主要配置项：</h3>
 * <ul>
 * <li><strong>功能开关</strong> - 启用/禁用系统服务</li>
 * <li><strong>缓存配置</strong> - 缓存开关、过期时间等</li>
 * <li><strong>日志配置</strong> - 操作日志记录配置</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * 
 * <pre>{@code
 * plugin:
 *   system:
 *     server:
 *       enable: true
 *       cache:
 *         enable: true
 *         expire: 3600
 * }</pre>
 *
 * @author CH
 * @since 2024/12/11
 * @see org.springframework.boot.context.properties.ConfigurationProperties
 */
@Data
@ConfigurationProperties(prefix = SystemProperties.PRE, ignoreUnknownFields = false)
public class SystemProperties {

    /**
     * 配置属性前缀
     */
    public static final String PRE = "plugin.system.server";

    /**
     * 是否启用系统服务
     * <p>
     * 设置为false时，整个系统服务不会启动
     * </p>
     * <p>
     * 默认值：true
     * </p>
     */
    private boolean enable = true;

    /**
     * 缓存配置
     */
    private CacheConfig cache = new CacheConfig();

    /**
     * 日志配置
     */
    private LogConfig log = new LogConfig();

    /**
     * 缓存配置类
     */
    @Data
    public static class CacheConfig {
        /**
         * 是否启用缓存
         * <p>
         * 默认值：true
         * </p>
         */
        private boolean enable = true;

        /**
         * 缓存过期时间（秒）
         * <p>
         * 默认值：3600秒（1小时）
         * </p>
         */
        private long expire = 3600L;
    }

    /**
     * 日志配置类
     */
    @Data
    public static class LogConfig {
        /**
         * 是否启用操作日志
         * <p>
         * 默认值：true
         * </p>
         */
        private boolean enable = true;

        /**
         * 是否异步记录日志
         * <p>
         * 默认值：true
         * </p>
         */
        private boolean async = true;
    }
}
