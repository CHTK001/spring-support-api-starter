package com.chua.starter.school;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 学校服务
 *
 * @author CH
 */
@EnableCaching
@EnableScheduling
@MapperScan({"com.chua.starter.**.mapper"})
@SpringBootApplication(scanBasePackages = {"com.chua.starter.school", "com.chua.starter.service"})
public class SchoolServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchoolServerApplication.class);
    }
}