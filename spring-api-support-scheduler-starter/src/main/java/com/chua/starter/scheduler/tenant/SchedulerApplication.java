package com.chua.starter.scheduler.tenant;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 定时任务启动器
 *
 * @author CH
 */
@EnableCaching
@EnableScheduling
@MapperScan({"com.chua.starter.**.mapper"})
@SpringBootApplication(scanBasePackages = {"com.chua.starter.system", "com.chua.starter.tenant", "com.chua.starter.service.**"})
public class SchedulerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchedulerApplication.class, args);
    }
}
