package com.chua.starter.payment.test;

import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.client.support.wrapper.OAuthHttpServletRequestWrapper;
import jakarta.annotation.Nonnull;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import org.aopalliance.intercept.MethodInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
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
 * AuthClientConfiguration 不会被加载，导致 @UserValue 参数解析器和 @Permission 拦截器缺失。
 * 本配置提供退化实现：
 * - @UserValue 参数返回空默认值（null / 空Set）
 * - @Permission 注解放行所有请求
 */
@Configuration
@ConditionalOnProperty(prefix = "plugin.oauth", name = "enable", havingValue = "false")
public class PaymentTestAuthConfiguration {

    private static final Logger log = LoggerFactory.getLogger(PaymentTestAuthConfiguration.class);

    /**
     * 注册 @UserValue 参数解析器，当 OAuth 禁用时返回默认值。
     * 这避免了 Spring MVC 因找不到参数解析器而抛出 IllegalStateException。
     */
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
                log.info("[PaymentTestAuth] 已注册 @UserValue 退化参数解析器（OAuth 禁用）");
            }
        };
    }

    /**
     * 注册 @Permission 拦截器退化实现，当 OAuth 禁用时放行所有请求。
     */
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
            log.debug("[PaymentTestAuth] @Permission 退化检查通过（OAuth 禁用）");
            return invocation.proceed();
        });
        advisor.setOrder(Ordered.HIGHEST_PRECEDENCE);
        log.info("[PaymentTestAuth] 已注册 @Permission 退化拦截器（OAuth 禁用）");
        return advisor;
    }

    /**
     * 注入模拟 OAuthPrincipal 过滤器，使 PaymentAccountPrincipalResolver 能获取到 ADMIN 用户。
     * 当 OAuth 禁用时，所有请求都会携带一个测试管理员身份。
     */
    @Bean
    public FilterRegistrationBean<Filter> mockOAuthPrincipalFilter() {
        FilterRegistrationBean<Filter> reg = new FilterRegistrationBean<>();
        reg.setFilter((request, response, chain) -> {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            UserResume resume = new UserResume();
            resume.setUserId("1");
            resume.setUsername("admin");
            resume.setNickName("测试管理员");
            resume.setRoles(Set.of("ADMIN"));
            OAuthHttpServletRequestWrapper wrapper = OAuthHttpServletRequestWrapper.authenticated(httpRequest, resume, "OAUTH_HTTP");
            chain.doFilter(wrapper, response);
        });
        reg.addUrlPatterns("/*");
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        log.info("[PaymentTestAuth] 已注册模拟 OAuthPrincipal 过滤器（OAuth 禁用）");
        return reg;
    }
}
