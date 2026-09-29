package com.chua.starter.collect.configuration;

import com.chua.starter.collect.properties.CollectProperties;
import com.chua.starter.collect.support.CollectConsoleAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 采集控制台 MVC 配置。
 *
 * <p>控制台前端为独立的 Vue 应用，本模块只暴露接口：登录拦截器保护采集侧的 {@code /v2/collect/**}，
 * 以及并入本进程的调度接口 {@code /v1/job-platform/**} 与 {@code /job-console/api/**}
 * （调度模块自带的 {@code /job-console/**} 会话拦截器在合并宿主里不挂载，见
 * {@link CollectSchedulerMergeConfiguration}）。登录相关接口走 {@code /collect-console/auth/**}
 * 独立路径，无需放行规则。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "plugin.collect", name = "enable", havingValue = "true", matchIfMissing = true)
public class CollectConsoleWebMvcConfigurer implements WebMvcConfigurer {

    /**
     * 控制台会话要保护的接口路径，含并入本进程的调度接口。
     */
    private static final String[] PROTECTED_PATHS = {
            "/v2/collect/**",
            "/v1/job-platform/**",
            "/job-console/api/**",
    };

    /**
     * 平台配置，含控制台账号。
     */
    private final CollectProperties properties;

    /**
     * 注册控制台登录拦截器。
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CollectConsoleAuthInterceptor(properties))
                .addPathPatterns(PROTECTED_PATHS);
    }
}
