package com.chua.starter.soft.test.config;

import com.chua.starter.oauth.client.support.properties.AuthClientProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * soft-test 独立运行时提供最小 OAuth 客户端配置，
 * 避免系统模块在没有完整 OAuth 服务中心时启动失败。
 */
@Configuration
public class SoftTestOAuthFallbackConfiguration {

    /**
     * 提供一个可被环境配置覆盖的基础 Bean。
     */
    @Bean
    @ConfigurationProperties(prefix = AuthClientProperties.PRE)
    public AuthClientProperties authClientProperties() {
        AuthClientProperties properties = new AuthClientProperties();
        properties.setEnable(false);
        properties.setProtocol("static");
        return properties;
    }
}
