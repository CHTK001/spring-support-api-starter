package com.chua.starter.system;

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
@SpringBootApplication(scanBasePackages = {"com.chua.starter.system", "com.chua.starter.tenant", "com.chua.starter.service.**"})
public class SystemApplication {
    public static void main(String[] args) {
        System.out.println("1");
        SpringApplication.run(SystemApplication.class, args);
    }
}
