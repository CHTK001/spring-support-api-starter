package com.chua.starter.system.support;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 账号状态守卫配置
 *
 * <p>注册 {@link UserStateGuardInterceptor}，拦截所有请求；拦截器内部仅对已登录会话做状态校验，
 * 未登录 / 公开接口直接放行。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@Configuration
public class UserStateGuardConfiguration implements WebMvcConfigurer {

    /**
     * 账号状态守卫拦截器
     */
    @Resource
    private UserStateGuardInterceptor userStateGuardInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userStateGuardInterceptor).addPathPatterns("/**");
    }
}
