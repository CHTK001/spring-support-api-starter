package com.chua.starter.monitor.configuration.apm;

import com.chua.starter.monitor.agent.SystemMetricProvider;
import com.chua.starter.monitor.agent.SystemMetricProperties;
import com.chua.starter.monitor.agent.SystemMetricRegistry;
import com.chua.starter.monitor.agent.impl.HotsportSystemMetricProvider;
import com.chua.starter.monitor.agent.impl.NoneSystemMetricProvider;
import com.chua.starter.monitor.agent.impl.SkywalkingSystemMetricProvider;
import com.chua.starter.monitor.controller.apm.SystemMetricController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * 系统指标（APM）自动装配。
 *
 * <p>登记于 {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}，
 * 目标是「加依赖即生效」：任何引入了监控模块 jar 的 Servlet 应用（监控服务自身、
 * 系统服务 {@code spring-api-support-system-starter} 等）都会自动获得一套
 * {@code /api/apm/*} 接口，<b>不需要</b>在启动类
 * {@code @SpringBootApplication(scanBasePackages = ...)} 里手工补扫
 * {@code controller.apm} / {@code configuration.apm}。</p>
 *
 * <p>这里刻意<b>不</b>对 {@code com.chua.starter.monitor} 做整体 {@code @ComponentScan}：
 * 同一个 jar 里还有 {@code MonitorServerController}、ACME 证书服务等依赖监控服务全量环境
 * （数据库、Redis、Socket 协议 SPI）的类，宿主应用未必具备这些条件。APM 只需要自己的
 * 控制器与数据源路由，所以按类型精确 {@link Import}。</p>
 *
 * <p>本类<b>不</b>声明 {@code @MapperScan}：APM 不需要任何 Mapper，而多声明一个
 * {@code MapperScannerConfigurer} 会让 MyBatis 的「未显式声明则自动扫描」逻辑失效，
 * 反而可能让宿主自己的 Mapper 扫不到。</p>
 *
 * <p>装配内容：</p>
 * <ul>
 *   <li>{@link SystemMetricController}：统一入口 {@code /api/apm/**} 与
 *       {@code /api/apm/provider}，仅在 Servlet Web 应用中装配。经
 *       {@link SystemMetricControllerRegistrar} 按需注册：监控服务自身用组件扫描
 *       已经注册过同一个类时跳过，避免同名 Bean 冲突；</li>
 *   <li>{@link SystemMetricRegistry}：数据源路由，自身不带 {@code @Component}，
 *       由 {@link #systemMetricRegistry(SystemMetricProperties, ApplicationContext)} 声明；</li>
 *   <li>{@code none} / {@code hotsport} / {@code skywalker} 三个 {@link SystemMetricProvider}：
 *       无状态单例，注册为 Bean 后路由优先取容器实例，取不到再回退 SPI
 *       {@code ServiceProvider} 发现。全部带 {@code @ConditionalOnMissingBean}，
 *       宿主声明自己的同类型 Bean 即可覆盖。</li>
 * </ul>
 *
 * <p>关闭方式：{@code system.metric.enable=false} 或
 * {@code spring.autoconfigure.exclude=com.chua.starter.monitor.configuration.apm.SystemMetricAutoConfiguration}。
 * 注意 {@code system.metric.provider} 只选择数据源（{@code auto} / {@code none} /
 * {@code hotsport} / {@code skywalker}），<b>不会</b>关闭接口——前端页面依赖这些接口渲染空态。</p>
 *
 * @author CH
 * @since 4.0.0.42
 * @see SystemMetricProperties
 * @see SystemMetricRegistry
 */
@Slf4j
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({SystemMetricRegistry.class, DispatcherServlet.class})
@ConditionalOnProperty(prefix = "system.metric", name = "enable", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SystemMetricProperties.class)
@Import(SystemMetricControllerRegistrar.class)
public class SystemMetricAutoConfiguration {

    /**
     * 自动装配构造方法，只用于输出装配日志。
     */
    public SystemMetricAutoConfiguration() {
        log.info("[system-metric] 系统指标自动装配已启用（/api/apm/**），数据源模式见 system.metric.provider");
    }

    /**
     * 数据源路由。
     *
     * @param properties 系统指标配置（{@code system.metric.*}）
     * @param applicationContext Spring 上下文（SPI 收集的兜底来源）
     * @return 路由实例
     */
    @Bean
    @ConditionalOnMissingBean
    public SystemMetricRegistry systemMetricRegistry(SystemMetricProperties properties,
                                                     ApplicationContext applicationContext) {
        return new SystemMetricRegistry(properties, applicationContext);
    }

    /**
     * 降级数据源：无观测后端时返回形状正确的空数据，恒可用。
     *
     * @return 数据源实现
     */
    @Bean
    @ConditionalOnMissingBean(NoneSystemMetricProvider.class)
    public SystemMetricProvider noneSystemMetricProvider() {
        return new NoneSystemMetricProvider();
    }

    /**
     * hotsport 观测后端：整体透传 {@code /api/apm/*} 契约（含 SSE）。
     *
     * @return 数据源实现
     */
    @Bean
    @ConditionalOnMissingBean(HotsportSystemMetricProvider.class)
    public SystemMetricProvider hotsportSystemMetricProvider() {
        return new HotsportSystemMetricProvider();
    }

    /**
     * SkyWalking OAP 数据源：把 OAP GraphQL 结果翻译成前端契约。
     *
     * @return 数据源实现
     */
    @Bean
    @ConditionalOnMissingBean(SkywalkingSystemMetricProvider.class)
    public SystemMetricProvider skywalkingSystemMetricProvider() {
        return new SkywalkingSystemMetricProvider();
    }
}
