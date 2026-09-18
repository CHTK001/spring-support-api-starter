package com.chua.starter.scheduler.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 调度平台配置。
 *
 * <p>平台基于 job-starter 新架构 (SCHEDULER 角色) 提供统一任务管理能力,
 * 任务统一存储在 {@code sys_job / sys_job_log} 单表中。命名空间任务管理与
 * 远程执行器分发通过 {@link #namespaces} 配置。调度能力由 作业-启动 的
 * 铅 循环 + 长链 同步 承担。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@Data
@ConfigurationProperties(prefix = "plugin.scheduler.job-platform")
public class SchedulerJobPlatformProperties {

    /**
     * 是否启用任务平台接口。
     */
    private boolean enabled = true;

    /**
     * 是否启用远程分发。
     * <p>
     * 启用后调度中心才会扫描命名空间任务表并把到期任务推送到远程执行器。
     * </p>
     */
    private boolean remoteDispatchEnabled = false;

    /**
     * 分发轮询间隔（毫秒），默认 1000。
     */
    private long dispatchIntervalMs = 1000L;

    /**
     * 单轮分发批量大小，默认 100。
     */
    private int dispatchBatchSize = 100;

    /**
     * 控制台访问配置。
     * <p>
     * 这里使用独立的轻量账号密码和 会话 校验，仅保护模块内置的
     * {@code /job-console/**} 页面资源，不影响现有平台 API 的对接方。
     * </p>
     */
    private Console console = new Console();

    /**
     * 命名空间配置，按命名空间 key 配置任务表前缀与远程执行器。
     */
    private Map<String, Namespace> namespaces = new LinkedHashMap<>();

    /**
     * 控制台访问配置类。
     *
     * @since 4.0.0.42
     * @author CH
     */
    @Data
    public static class Console {

        /**
         * 是否启用控制台账号密码校验。
         */
        private boolean authEnabled = true;

        /**
         * 控制台登录用户名。
         */
        private String username = "admin";

        /**
         * 控制台登录密码。
         * <p>
         * 这是轻量页面登录，不走统一用户中心，建议部署时显式覆盖。
         * 由配置文件 plugin.调度器.作业-platform.控制台.密码 设置。
         * </p>
         */
        private String password = "";
    }

    /**
     * 命名空间配置。
     *
     * @since 4.0.0
     * @author CH
     */
    @Data
    public static class Namespace {

        /**
         * 运行模式 (SINGLE / CLUSTER)。
         */
        private String mode = "SINGLE";

        /**
         * 任务分组。
         */
        private String group;

        /**
         * 远程执行器列表。
         */
        private java.util.List<Executor> executors = new java.util.ArrayList<>();

        /**
         * 发现服务配置。
         */
        private Discovery discovery = new Discovery();
    }

    /**
     * 远程执行器配置。
     *
     * @since 4.0.0
     * @author CH
     */
    @Data
    public static class Executor {

        /**
         * 执行器名称。
         */
        private String name;

        /**
         * 执行器基础 URL。
         */
        private String baseUrl;

        /**
         * 访问令牌。
         */
        private String accessToken;

        /**
         * 分发路径。
         */
        private String dispatchPath;

        /**
         * 连接超时（毫秒）。
         */
        private long connectTimeoutMs = 3000L;

        /**
         * 读取超时（毫秒）。
         */
        private long readTimeoutMs = 10000L;
    }

    /**
     * 发现服务配置。
     *
     * @since 4.0.0
     * @author CH
     */
    @Data
    public static class Discovery {

        /**
         * 是否启用发现服务任务。
         */
        private boolean enabled = false;

        /**
         * 发现协议（如 nacos / eureka）。
         */
        private String protocol;

        /**
         * 发现命名空间路径。
         */
        private String namespace;

        /**
         * 基础路径。
         */
        private String basePath;

        /**
         * 分发路径。
         */
        private String dispatchPath;

        /**
         * 访问令牌。
         */
        private String accessToken;

        /**
         * 连接超时（毫秒）。
         */
        private long connectTimeoutMs = 3000L;

        /**
         * 读取超时（毫秒）。
         */
        private long readTimeoutMs = 10000L;
    }
}
