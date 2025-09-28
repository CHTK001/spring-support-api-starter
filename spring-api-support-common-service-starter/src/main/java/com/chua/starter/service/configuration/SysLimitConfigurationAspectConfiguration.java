package com.chua.starter.service.configuration;

import com.chua.starter.service.aspect.SysLimitConfigurationAspect;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 限流配置切面配置类
 * 
 * 注册基于数据库配置的限流切面
 * 
 * @author CH
 * @since 2025/9/28
 */
@Configuration
public class SysLimitConfigurationAspectConfiguration {

    @Autowired
    private RateLimiterRegistry rateLimiterRegistry;

    /**
     * 注册基于数据库配置的限流切面
     * 
     * @return 限流切面实例
     */
    @Bean
    public SysLimitConfigurationAspect sysLimitConfigurationAspect() {
        return new SysLimitConfigurationAspect(rateLimiterRegistry);
    }
}