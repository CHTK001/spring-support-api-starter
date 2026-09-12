package com.chua.starter.system.configuration;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.chua.starter.common.support.oauth.AuthService;
import com.chua.starter.common.support.serviceinfo.ServiceInfoContributor;
import com.chua.starter.oauth.client.support.properties.AuthClientProperties;
import com.chua.starter.system.properties.SystemSecurityProperties;
import com.chua.starter.system.interceptor.PrivilegedVisibilityDataPermissionHandler;
import com.chua.starter.system.interceptor.PrivilegedVisibilityInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.function.Supplier;

/**
 * 系统统一认证自动配置
 * <p>自动扫描认证相关的组件和Mapper</p>
 *
   * @版本 1.0.0
 * @since 2024/12/11
 * @author CH
 */
@Slf4j
@Configuration
@ComponentScan(basePackages = {
        "com.chua.starter.system"
})
@MapperScan(basePackages = "com.chua.starter.system.mapper")
@EnableConfigurationProperties(SystemSecurityProperties.class)
public class SystemAutoConfiguration {

    /**
     * 构造方法
     * @param environment 环境
     /**
      * 系统auto配置。
      */
     * @param value 值
     * @return resolve的结果
     */
    public SystemAutoConfiguration() {
        log.info("系统统一认证模块已启用");
    }

    @Bean
    public InnerInterceptor privilegedVisibilityInnerInterceptor(
            @Qualifier("authService") ObjectProvider<AuthService> authServiceProvider,
            @Qualifier("defaultAuthService") ObjectProvider<AuthService> defaultAuthServiceProvider) {
        Supplier<AuthService> authServiceSupplier = () -> {
            AuthService authService = authServiceProvider.getIfAvailable();
            return null != authService ? authService : defaultAuthServiceProvider.getIfAvailable();
        };
        return new PrivilegedVisibilityInterceptor(
                new PrivilegedVisibilityDataPermissionHandler(authServiceSupplier),
                authServiceSupplier);
    }

    @Bean
    public ApplicationRunner authClientPropertiesBridge(
            ApplicationContext applicationContext,
            Environment environment,
            ObjectProvider<AuthClientProperties> authClientPropertiesProvider) {
        return args -> {
            AuthClientProperties authClientProperties = authClientPropertiesProvider.getIfAvailable();
            if (authClientProperties == null) {
                log.warn("[OAuth配置桥接]未找到 AuthClientProperties Bean，跳过桥接");
                return;
            }

            String address = firstNonBlank(
                    authClientProperties.getAddress(),
                    environment.getProperty("plugin.oauth.address"),
                    environment.getProperty("plugin.oauth.auth-address"));
            if (StringUtils.hasText(address) && !StringUtils.hasText(authClientProperties.getAddress())) {
                authClientProperties.setAddress(address);
            }

            String loginAddress = firstNonBlank(
                    resolve(environment, authClientProperties.getLoginAddress()),
                    environment.getProperty("plugin.oauth.login-address"),
                    environment.getProperty("plugin.oauth.auth-address"),
                    address);
            if (StringUtils.hasText(loginAddress) && !StringUtils.hasText(authClientProperties.getLoginAddress())) {
                authClientProperties.setLoginAddress(loginAddress);
            }

            log.info("[OAuth配置桥接] address={}, loginAddress={}, protocol={}",
                    authClientProperties.getAddress(),
                    authClientProperties.getLoginAddress(),
                    authClientProperties.getProtocol());
        /**
         * 第一个nonblank。
         * @param values 值
         * @return 第一个nonblank的结果
         */
        };
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private static String resolve(Environment environment, String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return environment.resolvePlaceholders(value);
    }

    /**
     * 注册系统认证模块服务信息。
     *
     * @return 服务信息贡献器
     */
    @Bean
    public ServiceInfoContributor systemServiceInfoContributor() {
        return registry -> {
            registry.registerEndpoint("认证授权", "登录认证", "/v2/user/**", "登录、刷新、注销、用户信息");
            registry.registerFlag("system", "系统认证", Boolean.TRUE, "系统统一认证模块", null);
        };
    }
}
