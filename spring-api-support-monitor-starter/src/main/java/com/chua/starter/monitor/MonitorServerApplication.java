package com.chua.starter.monitor;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 配置中心服务
 * @author CH
 */
@EnableCaching
@EnableScheduling
@MapperScan({"com.chua.starter.**.mapper"})
@SpringBootApplication(scanBasePackages = {"com.chua.starter.monitor", "com.chua.starter.service.**"})
public class MonitorServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonitorServerApplication.class);
    }
}
