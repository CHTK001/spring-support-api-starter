package com.chua.starter.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * 启动器
 *
 * @author CH
 */
@EnableCaching
@SpringBootApplication(scanBasePackages = {"com.chua.starter.admin", "com.chua.starter.service"})
public class AdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }
}
