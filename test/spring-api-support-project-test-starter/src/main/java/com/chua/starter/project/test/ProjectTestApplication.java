package com.chua.starter.project.test;

import com.chua.starter.oauth.client.support.properties.AuthClientProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

/**
 * 项目管理模块联调测试后端。
 */
@SpringBootApplication
@EnableConfigurationProperties(AuthClientProperties.class)
@ComponentScan(basePackages = {
        "com.chua.starter.project.test",
        "com.chua.starter.project",
})
public class ProjectTestApplication {

    public static void main(String[] args) {
        if (System.getProperty("mybatis-plus.encryptor.mode") == null) {
            System.setProperty("mybatis-plus.encryptor.mode", "PLAIN");
        }
        if (System.getProperty("mybatis-plus.encryptor.password") == null) {
            System.setProperty("mybatis-plus.encryptor.password", "project-test");
        }
        SpringApplication.run(ProjectTestApplication.class, args);
    }

}
