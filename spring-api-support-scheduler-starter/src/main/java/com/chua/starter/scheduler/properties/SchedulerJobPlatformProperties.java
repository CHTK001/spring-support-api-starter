package com.chua.starter.scheduler.properties;

import com.chua.starter.job.support.JobProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 调度平台多命名空间配置。
 * <p>
 * 每个 namespace 都映射到一组独立的 Job 物理表，字段结构保持与
 * {@code sys_job/sys_job_log/sys_job_log_detail/sys_job_log_backup} 一致，
 * 但可以通过显式表名或前缀派生实现物理隔离。
 * </p>
 */
@Data
@ConfigurationProperties(prefix = "plugin.scheduler.job-platform")
public class SchedulerJobPlatformProperties {

    private static final String DEFAULT_DISPATCH_PATH = "/v1/job-executor/dispatch";

    /**
     * 是否启用任务平台接口。
     */
    private boolean enabled = true;

    /**
     * 是否启用中心侧远程分发轮询。
     * <p>
     * 开启后，调度中心会像 XXL-Job admin 一样轮询配置表，并把命中的任务
     * 推送到命名空间下配置的远程执行器。
     * </p>
     */
    private boolean remoteDispatchEnabled = true;

    /**
     * 中心侧轮询远程命名空间任务的间隔，单位毫秒。
     */
    private long dispatchIntervalMs = 1000L;

    /**
     * 每轮最多拉取多少条待分发任务。
     */
    private int dispatchBatchSize = 200;

    /**
     * 命名空间表映射。
     * <p>
     * key 为业务命名空间，例如 {@code payment}；value 为该命名空间对应的
     * Job 表配置。可以显式指定完整物理表名，也可以只配置 {@code prefix} 让平台
     * 自动派生出 {@code payment_sys_job / payment_sys_job_log ...} 这类表名。
     * </p>
     */
    private Map<String, Namespace> namespaces = new LinkedHashMap<>();

    /**
     * 控制台访问配置。
     * <p>
     * 这里使用独立的轻量账号密码和 Session 校验，仅保护模块内置的
     * {@code /job-console/**} 页面资源，不影响现有平台 API 的对接方。
     * </p>
     */
    private Console console = new Console();

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Namespace extends JobProperties.Table {

        /**
         * 当前命名空间可用的远程执行器列表。
         * <p>
         * 未配置时，平台只负责管理表，不参与执行；配置后，平台会把命中的任务
         * 推送到这些执行器，并按顺序失败转移。
         * </p>
         */
        private List<Executor> executors = new ArrayList<>();
    }

    @Data
    public static class Executor {

        /**
         * 执行器名称，仅用于日志与排障。
         */
        private String name;

        /**
         * 执行器基础地址，例如 {@code http://payment-service:18081/payment/api}。
         */
        private String baseUrl;

        /**
         * 平台调用执行器时使用的访问口令。
         */
        private String accessToken;

        /**
         * 远程执行器下发路径。
         */
        private String dispatchPath = DEFAULT_DISPATCH_PATH;

        /**
         * 连接超时，单位毫秒。
         */
        private long connectTimeoutMs = 3000L;

        /**
         * 读取超时，单位毫秒。
         */
        private long readTimeoutMs = 10000L;
    }

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
