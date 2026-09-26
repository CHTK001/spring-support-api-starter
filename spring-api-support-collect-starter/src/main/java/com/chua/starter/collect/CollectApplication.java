package com.chua.starter.collect;

import com.chua.starter.collect.properties.CollectProperties;
import com.chua.starter.common.support.api.properties.ApiProperties;
import com.chua.starter.job.support.JobAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 数据采集平台启动器。
 *
 * <p>平台以 SINGLE 角色运行：定时能力由 job-starter 的调度循环驱动
 * {@code collect.scheduler.tick} 任务，采集能力由 utils-support-crawler-starter 提供，
 * 流程执行引擎与结果存储由本模块承担。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@EnableCaching
@EnableScheduling
@EnableConfigurationProperties({CollectProperties.class, ApiProperties.class})
@MapperScan({"com.chua.starter.collect.mapper", JobAutoConfiguration.MAPPER_PACKAGE})
@SpringBootApplication(scanBasePackages = {
        "com.chua.starter.collect",
        "com.chua.starter.system",
        "com.chua.starter.tenant",
        "com.chua.starter.service.impl",
        "com.chua.starter.service.configuration"
})
public class CollectApplication {

    /**
     * 应用启动入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(CollectApplication.class, args);
    }
}
