package com.chua.starter.oauth.sso.support.configuration;

import com.chua.starter.oauth.sso.support.service.OAuthTraceLogService;
import com.chua.starter.oauth.sso.support.trace.OAuthTraceFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * OAuth追踪配置
 * 配置追踪过滤器和异步支持
 *
 * @author CH
 * @since 2025/01/01
 */
@Configuration
@EnableAsync
@ConditionalOnProperty(name = "plugin.auth.server.trace.enabled", havingValue = "true", matchIfMissing = true)
public class OAuthTraceConfiguration {

    /**
     * 注册OAuth追踪过滤器
     */
    @Bean
    public FilterRegistrationBean<OAuthTraceFilter> oauthTraceFilterRegistration(OAuthTraceLogService traceLogService) {
        FilterRegistrationBean<OAuthTraceFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new OAuthTraceFilter(traceLogService));
        registration.addUrlPatterns("/*");
        registration.setName("oauthTraceFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return registration;
    }
}
