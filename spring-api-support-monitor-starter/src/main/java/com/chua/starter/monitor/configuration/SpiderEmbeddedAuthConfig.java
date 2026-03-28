package com.chua.starter.monitor.configuration;

import com.chua.starter.monitor.filter.SpiderAuthenticationFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spider 轻页面嵌入式认证配置。
 */
@Configuration
@ConditionalOnProperty(prefix = "plugin.monitor.spider.web-auth", name = "mode", havingValue = "embedded")
public class SpiderEmbeddedAuthConfig {

    @Bean
    public FilterRegistrationBean<SpiderAuthenticationFilter> spiderAuthFilter() {
        FilterRegistrationBean<SpiderAuthenticationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new SpiderAuthenticationFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }
}
