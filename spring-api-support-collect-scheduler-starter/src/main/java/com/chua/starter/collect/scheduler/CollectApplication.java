package com.chua.starter.collect.scheduler;

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
 * <p>扫描范围只保留本应用自己的包：采集侧能力（service/mapper/controller/engine 等）
 * 已由 {@code spring-support-collect-starter} 的 CollectAutoConfiguration 自动装配，
 * 消费方加依赖即可生效，这里不再重复扫描，也就避免了两处扫描同一批类。</p>
 *
 * <p>Mapper 只显式声明 job-starter 的包：job 的自动装配约定是由消费方自行
 * {@code @MapperScan}（见 JobAutoConfiguration.MAPPER_PACKAGE）；采集的 mapper 归
 * CollectAutoConfiguration 负责。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@EnableCaching
@EnableScheduling
@EnableConfigurationProperties({CollectProperties.class, ApiProperties.class})
@MapperScan(JobAutoConfiguration.MAPPER_PACKAGE)
@SpringBootApplication(scanBasePackages = {
        "com.chua.starter.collect.scheduler",
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
