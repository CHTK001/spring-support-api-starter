package com.chua.starter.protocol.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 协议管理联调应用入口：仅负责启动 Spring Boot；REST、Service、Mapper 均由
 * {@code spring-support-protocol-starter} 在 {@code plugin.protocol.enable=true} 时提供。
 *
 * @author CH
 */
@SpringBootApplication(scanBasePackages = "com.chua.starter.protocol.test")
public class ProtocolTestApplication {

    /**
     * 启动内嵌 Web 容器并加载协议管理相关自动配置。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(ProtocolTestApplication.class, args);
    }
}
