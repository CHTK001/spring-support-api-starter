package com.chua.starter.pay.support;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 启动器
 *
 * @author CH
 */
@EnableCaching
@EnableScheduling
@MapperScan({"com.chua.starter.**.mapper"})
@SpringBootApplication(scanBasePackages = {"com.chua.starter.pay", "com.chua.starter.service"})
public class PayServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(PayServerApplication.class, args);
    }
}
