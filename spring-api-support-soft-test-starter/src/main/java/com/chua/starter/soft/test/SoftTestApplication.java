package com.chua.starter.soft.test;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 独立 Soft 测试后端。
 */
@EnableCaching
@EnableScheduling
@MapperScan({"com.chua.starter.soft.support.mapper", "com.chua.starter.server.support.mapper"})
@ConditionalOnProperty(prefix = "plugin.soft.server", name = "enable", havingValue = "true", matchIfMissing = true)
@SpringBootApplication(scanBasePackages = {
        "com.chua.starter.soft.support",
        "com.chua.starter.server.support",
        "com.chua.starter.soft.test"
})
public class SoftTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(SoftTestApplication.class, args);
    }
}
