package com.chua.starter.signin.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
* 签到模块自动配置
*
* <p>签到模块的自动配置入口，通过 {@code spring.factories} / {@code *.imports} 机制注册。
* 各 服务 使用 {@code @Service} 注解自动扫描，无需在此显式声明 Bean。</p>
*
* @since 4.0.0.42
* @author CH
 */
@Slf4j
@Configuration
public class SignInAutoConfiguration {

    /**
    * 无参构造，日志记录模块初始化。
     */
    public SignInAutoConfiguration() {
        log.info("签到模块自动配置已加载");
    }
}