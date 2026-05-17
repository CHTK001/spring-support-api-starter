package com.chua.starter.project.test;

import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.oauth.client.support.user.UserResume;
import jakarta.annotation.Nonnull;
import org.aopalliance.intercept.MethodInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Set;

/**
 * 当 OAuth 禁用时（plugin.oauth.enable=false），
 * 提供退化实现：@UserValue 参数返回默认值，@Permission 放行所有请求。
 */
@Configuration
@ConditionalOnProperty(prefix = "plugin.oauth", name = "enable", havingValue = "false")
public class ProjectTestAuthConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ProjectTestAuthConfiguration.class);

    @Bean
    public WebMvcConfigurer userValueArgumentResolverConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addArgumentResolvers(@Nonnull List<HandlerMethodArgumentResolver> resolvers) {
                resolvers.add(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.hasParameterAnnotation(UserValue.class);
                    }

                    @Override
                    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                        Class<?> paramType = parameter.getParameterType();
                        if (Set.class.isAssignableFrom(paramType)) {
                            return Set.of();
                        }
                        if (UserResume.class.isAssignableFrom(paramType)) {
                            UserResume resume = new UserResume();
                            resume.setRoles(Set.of("ADMIN"));
                            return resume;
                        }
                        return null;
                    }
                });
                log.info("[ProjectTestAuth] 已注册 @UserValue 退化参数解析器（OAuth 禁用）");
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    @Order(1)
    public StaticMethodMatcherPointcutAdvisor permissionFallbackAdvisor() {
        StaticMethodMatcherPointcutAdvisor advisor = new StaticMethodMatcherPointcutAdvisor() {
            @Override
            public boolean matches(Method method, Class<?> targetClass) {
                if (Proxy.isProxyClass(targetClass)) {
                    return false;
                }
                return AnnotatedElementUtils.hasAnnotation(method, Permission.class) ||
                        AnnotatedElementUtils.hasAnnotation(targetClass, Permission.class);
            }
        };
        advisor.setAdvice((MethodInterceptor) invocation -> {
            log.debug("[ProjectTestAuth] @Permission 退化检查通过（OAuth 禁用）");
            return invocation.proceed();
        });
        advisor.setOrder(Ordered.HIGHEST_PRECEDENCE);
        log.info("[ProjectTestAuth] 已注册 @Permission 退化拦截器（OAuth 禁用）");
        return advisor;
    }
}
