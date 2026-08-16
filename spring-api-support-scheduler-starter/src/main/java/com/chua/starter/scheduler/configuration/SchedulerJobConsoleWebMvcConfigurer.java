package com.chua.starter.scheduler.configuration;

import com.chua.starter.scheduler.properties.SchedulerJobPlatformProperties;
import com.chua.starter.scheduler.support.SchedulerJobConsoleAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 控制台页面 MVC 配置。
 *
 * <p>负责将控制台登录拦截器注册到 {@code /job-console/**} 路径,
 * 并排除登录页与静态资源。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "plugin.scheduler.job-platform", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SchedulerJobConsoleWebMvcConfigurer implements WebMvcConfigurer {

    /**
     * 调度平台配置
     */
    private final SchedulerJobPlatformProperties properties;

    /**
     * 注册控制台登录拦截器。
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SchedulerJobConsoleAuthInterceptor(properties))
                .addPathPatterns("/job-console", "/job-console/", "/job-console/**")
                .excludePathPatterns(
                        "/job-console/login",
                        "/job-console/login/",
                        "/job-console/login.html",
                        "/job-console/login.css",
                        "/job-console/login.js",
                        "/job-console/assets/**",
                        "/job-console/static/**",
                        "/job-console/auth/**"
                );
    }
}
