package com.chua.starter.system.configuration;

import com.chua.starter.oauth.client.support.properties.AuthClientProperties;
import com.chua.starter.oauth.client.support.runtime.OauthClientRuntimeContext;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

/**
 * 回退OAuth客户端配置，当AuthClientConfiguration未加载时提供默认Bean
 */
@Configuration
public class OAuthClientFallbackConfiguration {

    private final Environment environment;

    public OAuthClientFallbackConfiguration(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void init() {
        OauthClientRuntimeContext.setEnvironment(environment);
    }

    @Bean
    @ConditionalOnMissingBean
    @Primary
    public AuthClientProperties authClientProperties() {
        AuthClientProperties props = new AuthClientProperties();
        props.setProtocol("static");
        return props;
    }
}
