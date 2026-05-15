package com.chua.starter.system.test;

import com.chua.starter.job.support.JobProperties;
import com.chua.starter.system.properties.SystemSecurityProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.chua.starter.system.annotations",
        "com.chua.starter.system.configuration",
        "com.chua.starter.system.constant",
        "com.chua.starter.system.controller",
        "com.chua.starter.system.entity",
        "com.chua.starter.system.enums",
        "com.chua.starter.system.handler",
        "com.chua.starter.system.interceptor",
        "com.chua.starter.system.login",
        "com.chua.starter.system.mapper",
        "com.chua.starter.system.model",
        "com.chua.starter.system.pojo",
        "com.chua.starter.system.properties",
        "com.chua.starter.system.service",
        "com.chua.starter.soft",
        "com.chua.starter.service",
        "com.chua.starter.scheduler",
        "com.chua.starter.job",
        "com.chua.starter.monitor.controller.file"
}, excludeFilters = {
        @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.chua\\.starter\\.service\\.configuration\\..*"
        )
})
@MapperScan(basePackages = "com.chua.starter.system.mapper")
@EnableConfigurationProperties({JobProperties.class, SystemSecurityProperties.class})
public class SystemTestApplication {

    public static void main(String[] args) {
        if (System.getProperty("mybatis-plus.encryptor.mode") == null) {
            System.setProperty("mybatis-plus.encryptor.mode", "PLAIN");
        }
        if (System.getProperty("mybatis-plus.encryptor.password") == null) {
            System.setProperty("mybatis-plus.encryptor.password", "system-test");
        }
        SpringApplication.run(SystemTestApplication.class, args);
    }
}
