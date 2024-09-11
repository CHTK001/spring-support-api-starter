package com.chua.starter.tenant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * 启动器
 *
 * @author CH
 */
@EnableCaching
@SpringBootApplication(scanBasePackages = {"com.chua.starter.system", "com.chua.starter.service"})
public class TenantApplication {
    public static void main(String[] args) {
        SpringApplication.run(TenantApplication.class, args);
    }
}
