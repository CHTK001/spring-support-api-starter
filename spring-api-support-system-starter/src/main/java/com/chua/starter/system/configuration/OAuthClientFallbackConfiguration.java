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
 * 回退OAuth客户端配置，当认证客户端配置未加载时提供默认Bean
 *
 * <p>在缺少统一 OAuth 客户端配置时, 将运行环境注入
 * {@link OauthClientRuntimeContext} 并提供默认的静态协议配置。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@Configuration
public class OAuthClientFallbackConfiguration {

    /**
     * 静态协议标识
     */
    private static final String PROTOCOL_STATIC = "static";

    /**
     * Spring 环境
     */
    private final Environment environment;

    /**
     * 构造器, 注入 Spring 环境。
     *
     * @param environment Spring 环境
     */
    public OAuthClientFallbackConfiguration(Environment environment) {
        this.environment = environment;
    }

    /**
     * 初始化, 将环境注入 OAuth 客户端运行时上下文。
     */
    @PostConstruct
    public void init() {
        OauthClientRuntimeContext.setEnvironment(environment);
    }

    /**
     * 提供默认的静态协议客户端配置。
     *
     * @return 回退的客户端配置
     */
    @Bean
    @ConditionalOnMissingBean
    @Primary
    public AuthClientProperties authClientProperties() {
        AuthClientProperties props = new AuthClientProperties();
        props.setProtocol(PROTOCOL_STATIC);
        return props;
    }
}
