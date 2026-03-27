package com.chua.starter.scheduler;

import com.chua.starter.scheduler.properties.SchedulerJobPlatformProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 定时任务启动器
 *
 * @author CH
 */
@EnableCaching
@EnableScheduling
@EnableConfigurationProperties(SchedulerJobPlatformProperties.class)
@MapperScan({"com.chua.starter.**.mapper"})
@SpringBootApplication(scanBasePackages = {
        "com.chua.starter.system",
        "com.chua.starter.tenant",
        "com.chua.starter.scheduler",
        "com.chua.starter.service.impl",
        "com.chua.starter.service.task",
        "com.chua.starter.service.configuration"
})
public class SchedulerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchedulerApplication.class, args);
    }
}
