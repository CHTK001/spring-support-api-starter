package com.chua.starter.monitor.configuration;

import com.chua.starter.monitor.agent.SystemMetricProperties;
import com.chua.starter.monitor.agent.SystemMetricRegistry;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 系统指标自动装配：注册 {@code system.metric.*} 配置与数据源路由。
 *
 * <p>{@link SystemMetricRegistry} 在这里显式声明为 Bean（自身不带
 * {@code @Component}，避免同名冲突），并启用 {@code system.metric.*} 属性。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Configuration
@EnableConfigurationProperties(SystemMetricProperties.class)
public class SystemMetricConfiguration {

    /**
     * 数据源路由。
     *
     * @param properties 系统指标配置
     * @param applicationContext Spring 上下文（SPI 收集的兜底来源）
     * @return 路由实例
     */
    @Bean
    public SystemMetricRegistry systemMetricRegistry(SystemMetricProperties properties,
                                                     org.springframework.context.ApplicationContext applicationContext) {
        return new SystemMetricRegistry(properties, applicationContext);
    }
}