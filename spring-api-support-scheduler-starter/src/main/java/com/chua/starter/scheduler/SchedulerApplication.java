package com.chua.starter.scheduler;

import com.chua.starter.common.support.api.properties.ApiProperties;
import com.chua.starter.job.support.JobAutoConfiguration;
import com.chua.starter.scheduler.properties.SchedulerJobPlatformProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 定时任务启动器。
 *
 * <p>平台基于 job-starter 新架构以 SCHEDULER 角色运行, 通过
 * {@link JobAutoConfiguration} 自动装配 Leader 扫描 + 长链 Sync,
 * 业务执行器侧配置 job-starter 的 EXECUTOR 角色接入。</p>
 *
 * @since 4.0.0.42
 */
@EnableCaching
@EnableScheduling
@EnableConfigurationProperties({SchedulerJobPlatformProperties.class, ApiProperties.class})
@MapperScan({"com.chua.starter.**.mapper", JobAutoConfiguration.MAPPER_PACKAGE})
@SpringBootApplication(scanBasePackages = {
        "com.chua.starter.system",
        "com.chua.starter.tenant",
        "com.chua.starter.scheduler",
        "com.chua.starter.service.impl",
        "com.chua.starter.service.task",
        "com.chua.starter.service.configuration"
})
public class SchedulerApplication {

    /**
     * 应用启动入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SchedulerApplication.class, args);
    }
}
