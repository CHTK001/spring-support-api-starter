package com.chua.starter.payment.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 独立 Payment 测试后端。
 */
@EnableCaching
@EnableScheduling
@ConditionalOnProperty(prefix = "plugin.payment.server", name = "enable", havingValue = "true", matchIfMissing = true)
@SpringBootApplication
public class PaymentTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentTestApplication.class, args);
    }
}
