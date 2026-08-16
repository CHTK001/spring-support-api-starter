package com.chua.starter.scheduler.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 调度平台配置。
 *
 * <p>平台基于 job-starter 新架构 (SCHEDULER 角色) 提供统一任务管理能力,
 * 任务统一存储在 {@code sys_job / sys_job_log} 单表中, 不再维护多命名空间
 * 物理表与远程 HTTP 派发。调度能力由 job-starter 的 Leader 循环 + 长链 Sync 承担。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Data
@ConfigurationProperties(prefix = "plugin.scheduler.job-platform")
public class SchedulerJobPlatformProperties {

    /**
     * 是否启用任务平台接口。
     */
    private boolean enabled = true;

    /**
     * 控制台访问配置。
     * <p>
     * 这里使用独立的轻量账号密码和 Session 校验，仅保护模块内置的
     * {@code /job-console/**} 页面资源，不影响现有平台 API 的对接方。
     * </p>
     */
    private Console console = new Console();

    /**
     * 控制台访问配置类。
     *
     * @author CH
     * @since 4.0.0.42
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
         * </p>
         */
        private String password = "admin123456";
    }
}
