package com.chua.starter.monitor.support;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * 配置中心服务
 * @author CH
 */
@SpringBootApplication
@EnableCaching
public class MonitorServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonitorServerApplication.class);
    }
}
