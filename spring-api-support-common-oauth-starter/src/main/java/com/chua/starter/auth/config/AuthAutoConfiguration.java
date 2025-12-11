package com.chua.starter.auth.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 认证模块自动配置
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/11
 */
@Slf4j
@Configuration
@ComponentScan(basePackages = "com.chua.starter.auth")
public class AuthAutoConfiguration {

    public AuthAutoConfiguration() {
        log.info("统一认证模块已启用");
    }
}
