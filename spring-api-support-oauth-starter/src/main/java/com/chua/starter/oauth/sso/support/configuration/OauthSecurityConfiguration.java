// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.oauth.sso.support.configuration;

import com.chua.starter.oauth.sso.support.properties.OauthSecurityProperties;
import com.chua.starter.oauth.sso.support.admin.RateLimitManager;
import com.chua.starter.oauth.sso.support.security.OauthAdminAuthFilter;
import com.chua.starter.oauth.sso.support.security.OauthCsrfFilter;
import com.chua.starter.oauth.sso.support.security.OauthRateLimitFilter;
import com.chua.starter.oauth.sso.support.security.OauthSecurityHeadersFilter;
import com.chua.starter.oauth.sso.support.security.OauthSessionFixationFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

/**
 * OAuth认证服务器安全配置
 *
 * <p>注册安全响应头、IP 限流、CSRF、Session Fixation 等过滤器。</p>
 * <p>配置前缀：{@code plugin.oauth.server.security}</p>
 *
 * @since 2025/01/01
 * @see OauthSecurityProperties
 * @author CH
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(OauthSecurityProperties.class)
@ConditionalOnProperty(prefix = OauthSecurityProperties.PRE, name = "enabled", havingValue = "true", matchIfMissing = true)
public class OauthSecurityConfiguration {

    /**
     * 注册全局限流管理器。
     *
     * <p>RateLimitManager 位于 {@code com.chua.starter.oauth.sso.support.admin} 包，
     * 平台端组件扫描范围不覆盖该包，故在此显式声明 Bean，确保限流过滤器与
     * 策略任务接口均可注入/按名获取；若独立 OAuth 服务端已通过组件扫描注册，
     * 则 {@link ConditionalOnMissingBean} 避免重复定义。</p>
     *
     * @return 全局限流管理器
     */
    @Bean
    @ConditionalOnMissingBean
    public RateLimitManager rateLimitManager() {
        return new RateLimitManager();
    }

    /**
     * 注册安全响应头过滤器
     *
     * @param securityProperties 安全配置
     * @return 过滤器注册 Bean
     */
    @Bean
    @ConditionalOnProperty(prefix = OauthSecurityProperties.PRE + ".headers", name = "enabled",
            havingValue = "true", matchIfMissing = true)
    /**
     * oauth安全性头部过滤Registration
     * @param securityProperties 安全性属性
     */
    public FilterRegistrationBean<OauthSecurityHeadersFilter> oauthSecurityHeadersFilterRegistration(
            OauthSecurityProperties securityProperties) {
        FilterRegistrationBean<OauthSecurityHeadersFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new OauthSecurityHeadersFilter(securityProperties));
        registration.addUrlPatterns("/*");
        registration.setName("oauthSecurityHeadersFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        log.info("已注册 OAuth 安全响应头过滤器");
        return registration;
    }

    /**
     * 注册 IP 级限流过滤器
     *
     * @param securityProperties   安全配置
     * @param stringRedisTemplate  Redis 模板
     * @return 过滤器注册 Bean
     */
    @Bean
    @ConditionalOnProperty(prefix = OauthSecurityProperties.PRE + ".rate-limit", name = "enabled",
            havingValue = "true", matchIfMissing = true)
    /**
     * oauthrate限制过滤Registration
     * @param securityProperties 安全性属性
     * @param stringRedisTemplate 字符串redistemplate
     */
    public FilterRegistrationBean<OauthRateLimitFilter> oauthRateLimitFilterRegistration(
            OauthSecurityProperties securityProperties,
            StringRedisTemplate stringRedisTemplate,
            RateLimitManager rateLimitManager) {
        FilterRegistrationBean<OauthRateLimitFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new OauthRateLimitFilter(securityProperties, stringRedisTemplate, rateLimitManager));
        registration.addUrlPatterns("/*");
        registration.setName("oauthRateLimitFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 5);
        log.info("已注册 OAuth IP 限流过滤器: maxRequests={}, windowSeconds={}",
                securityProperties.getRateLimit().getMaxRequests(),
                securityProperties.getRateLimit().getWindowSeconds());
        return registration;
    }

    /**
     * 注册管理控制台鉴权过滤器
     *
     * <p><b>修复</b>：{@code /admin/**} 此前完全无鉴权，未授权请求即可关闭全局签名校验、
     * 把令牌有效期改成 {@code Long.MAX_VALUE}、解除单点登录限制。</p>
     * <p>顺序排在限流(+5)之后、CSRF(+15)之前：管理面同样吃 IP 限流以抗令牌爆破。</p>
     *
     * @param securityProperties 安全配置
     * @return 过滤器注册 Bean
     */
    @Bean
    @ConditionalOnProperty(prefix = OauthSecurityProperties.PRE + ".admin", name = "enabled",
            havingValue = "true", matchIfMissing = true)
    /**
     * oauth管理鉴权过滤Registration
     * @param securityProperties 安全性属性
     */
    public FilterRegistrationBean<OauthAdminAuthFilter> oauthAdminAuthFilterRegistration(
            OauthSecurityProperties securityProperties) {
        FilterRegistrationBean<OauthAdminAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new OauthAdminAuthFilter(securityProperties));
        registration.addUrlPatterns("/*");
        registration.setName("oauthAdminAuthFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        if (!StringUtils.hasText(securityProperties.getAdmin().getToken())) {
            log.warn("已注册 OAuth 管理鉴权过滤器, 但未配置 plugin.oauth.security.admin.token —— "
                    + "/oauth/admin/** 将拒绝一切访问, 请配置后使用");
        } else {
            log.info("已注册 OAuth 管理鉴权过滤器: 管理令牌已配置");
        }
        return registration;
    }

    /**
     * 注册 CSRF 防护过滤器
     *
     * @param securityProperties   安全配置
     * @param stringRedisTemplate  Redis 模板
     * @return 过滤器注册 Bean
     */
    @Bean
    @ConditionalOnProperty(prefix = OauthSecurityProperties.PRE + ".csrf", name = "enabled",
            havingValue = "true", matchIfMissing = true)
    /**
     * oauthcsrf过滤Registration
     * @param securityProperties 安全性属性
     * @param stringRedisTemplate 字符串redistemplate
     */
    public FilterRegistrationBean<OauthCsrfFilter> oauthCsrfFilterRegistration(
            OauthSecurityProperties securityProperties,
            StringRedisTemplate stringRedisTemplate) {
        FilterRegistrationBean<OauthCsrfFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new OauthCsrfFilter(securityProperties, stringRedisTemplate));
        registration.addUrlPatterns("/*");
        registration.setName("oauthCsrfFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 15);
        log.info("已注册 OAuth CSRF 防护过滤器");
        return registration;
    }

    /**
     * 注册 会话 Fixation 防护过滤器
     *
     * @param securityProperties 安全配置
     * @return 过滤器注册 Bean
     */
    @Bean
    @ConditionalOnProperty(prefix = OauthSecurityProperties.PRE + ".session-fixation", name = "enabled",
            havingValue = "true", matchIfMissing = true)
    /**
     * oauth会话fixation过滤Registration
     * @param securityProperties 安全性属性
     */
    public FilterRegistrationBean<OauthSessionFixationFilter> oauthSessionFixationFilterRegistration(
            OauthSecurityProperties securityProperties) {
        FilterRegistrationBean<OauthSessionFixationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new OauthSessionFixationFilter(securityProperties));
        registration.addUrlPatterns("/*");
        registration.setName("oauthSessionFixationFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
        log.info("已注册 OAuth Session Fixation 防护过滤器");
        return registration;
    }
}