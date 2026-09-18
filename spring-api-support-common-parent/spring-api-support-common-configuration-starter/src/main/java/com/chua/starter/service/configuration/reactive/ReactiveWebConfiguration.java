package com.chua.starter.service.configuration.reactive;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import reactor.core.publisher.Mono;

/**
 * Reactor 响应式返回值自动配置 — Spring MVC 下支持 控制器 返回 Mono/Flux。
 *
 * <p>classpath 存在 reactor-core 时 Spring MVC 经 {@code ReactiveTypeHandler}
 * 自动处理响应式返回值（无需 WebFlux）；本配置仅补充异步超时等保护性默认值。</p>
 *
 * <p>配置项：</p>
 * <ul>
 *   <li>{@code spring.reactive.async-timeout} — 异步请求超时（毫秒），默认 30000</li>
 * </ul>
 *
 * @author CH
 * @since 4.0.0.42
 */
@AutoConfiguration
@ConditionalOnClass(Mono.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(ReactiveWebConfiguration.ReactiveProperties.class)
public class ReactiveWebConfiguration {

    /**
     * 默认异步超时时间（毫秒）
     */
    private static final long DEFAULT_ASYNC_TIMEOUT_MILLIS = 30_000L;

    /**
     * 注册 MVC 定制器：为响应式返回值设置异步超时保护。
     *
     * @param properties 响应式配置属性
     * @return MVC 定制器实例
     */
    @Bean
    public WebMvcConfigurer reactiveWebMvcConfigurer(ReactiveProperties properties) {
        return new WebMvcConfigurer() {
            @Override
            public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
                configurer.setDefaultTimeout(properties.getAsyncTimeout());
            }
        };
    }

    /**
     * 响应式配置属性。
     *
     * @author CH
     * @since 4.0.0.42
     */
    @ConfigurationProperties(prefix = "spring.reactive")
    public static class ReactiveProperties {

        /**
         * 异步请求超时时间（毫秒）
         */
        private long asyncTimeout = DEFAULT_ASYNC_TIMEOUT_MILLIS;

        /**
         * 获取异步超时时间。
         *
         * @return 异步超时（毫秒）
         */
        public long getAsyncTimeout() {
            return asyncTimeout;
        }

        /**
         * 设置异步超时时间。
         *
         * @param asyncTimeout 异步超时（毫秒）
         */
        public void setAsyncTimeout(long asyncTimeout) {
            this.asyncTimeout = asyncTimeout;
        }
    }
}
