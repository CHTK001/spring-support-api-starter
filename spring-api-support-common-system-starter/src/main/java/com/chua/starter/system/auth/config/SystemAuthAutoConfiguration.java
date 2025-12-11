package com.chua.starter.system.auth.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 系统统一认证自动配置
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/11
 */
@Slf4j
@Configuration
@ComponentScan(basePackages = "com.chua.starter.system.auth")
public class SystemAuthAutoConfiguration {

    /**
     * 构造方法
     */
    public SystemAuthAutoConfiguration() {
        log.info("系统统一认证模块已启用");
    }
}
