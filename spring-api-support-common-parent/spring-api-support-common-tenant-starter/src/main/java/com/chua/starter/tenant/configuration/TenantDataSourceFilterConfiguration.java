package com.chua.starter.tenant.configuration;

import com.chua.starter.datasource.support.DynamicDataSource;
import com.chua.starter.common.support.oauth.AuthService;
import com.chua.starter.tenant.properties.TenantProperties;
import com.chua.starter.tenant.server.mapper.SysTenantMapper;
import com.chua.starter.tenant.server.support.TenantDataSourceFilter;
import com.chua.starter.tenant.server.support.TenantDataSourceRegistry;
import com.chua.starter.tenant.server.support.TenantDataSourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * 租户数据源路由配置。
 *
 * <p>只在 {@code plugin.mybatis-plus.tenant.server.enable=true} 且
 * {@code plugin.mybatis-plus.tenant.server.datasource-routing-enable=true} 时装配，
 * 默认不装配，存量共享库行为完全不变。</p>
 *
 * <p><b>为什么用 {@link FilterRegistrationBean} 而不是把过滤器标 {@code @Component}</b>：
 * 需要显式设置注册顺序，保证在任何业务逻辑拿到连接之前完成切换；
 * 同时避免 {@code @Component} 让过滤器在开关关闭时也被容器注册。</p>
 *
 * <p><b>启动期自检</b>：路由依赖 {@link DynamicDataSource} 这个 {@code @Primary} 数据源。
 * 如果应用没有装 datasource starter（或另配了别的 {@code @Primary} DataSource），
 * 过滤器设的线程变量没人消费，表现为「开了开关却不生效且无报错」。这里提前 WARN 一次，
 * 把根因直接写在启动日志里。</p>
 *
 * @since 4.0.0.51
 * @author CH
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(
        prefix = TenantProperties.PRE + ".server",
        name = {"enable", "datasource-routing-enable"},
        havingValue = "true"
)
public class TenantDataSourceFilterConfiguration {

    /**
     * 日志
     */
    private static final Logger log = LoggerFactory.getLogger(TenantDataSourceFilterConfiguration.class);

    /**
     * 注册租户数据源过滤器。
     *
     * @param tenantProperties         租户配置
     * @param sysTenantMapper          租户查询
     * @param tenantDataSourceRegistry 租户数据源注册器
     * @param tenantDataSourceResolver 连接信息解析器（与建池同源判定「是否登记独立库」）
     * @param authServiceProvider      认证服务（用于从会话解析租户）
     * @param dynamicDataSource        动态数据源（用于启动期自检，可为空）
     * @return 过滤器注册对象
     */
    @Bean
    @ConditionalOnMissingBean(name = "tenantDataSourceFilterRegistration")
    public FilterRegistrationBean<TenantDataSourceFilter> tenantDataSourceFilterRegistration(
            TenantProperties tenantProperties,
            SysTenantMapper sysTenantMapper,
            TenantDataSourceRegistry tenantDataSourceRegistry,
            TenantDataSourceResolver tenantDataSourceResolver,
            ObjectProvider<AuthService> authServiceProvider,
            ObjectProvider<DynamicDataSource> dynamicDataSource) {

        if (dynamicDataSource.getIfAvailable() == null) {
            log.warn("[租户数据源] 已开启 datasource-routing-enable，但容器中没有 DynamicDataSource。"
                    + "按租户切库不会生效——请确认引入了 spring-support-datasource-starter，"
                    + "且主数据源没有被其他 @Primary DataSource 覆盖。");
        } else {
            String source = tenantProperties.getServer().getDatasourceRoutingTenantSource();
            boolean failClosed = tenantProperties.getServer().isDatasourceRoutingFailClosed();
            log.info("[租户数据源] 已启用按租户切库: 租户来源={}, 注册前缀={}, 失败关闭={}",
                    source, tenantProperties.getServer().getDatasourceRoutingPrefix(), failClosed);
            if (source != null && source.toLowerCase().startsWith("header")) {
                log.warn("[租户数据源] 租户来源包含请求头（{}）。请求头可被客户端伪造，"
                        + "请确保相关接口自行校验租户归属，否则可用改头的方式读到其它租户库。", source);
            }
            if (!failClosed) {
                log.warn("[租户数据源] 已关闭失败关闭（datasource-routing-fail-closed=false）。"
                        + "租户库不可用时会回退主库，租户用户将读到平台共享库的数据——"
                        + "仅可用于「部分租户已切库」的迁移期，切库完成后请改回 true。");
            }
        }

        TenantDataSourceFilter filter = new TenantDataSourceFilter(
                tenantProperties, sysTenantMapper, tenantDataSourceRegistry, tenantDataSourceResolver,
                authServiceProvider);

        FilterRegistrationBean<TenantDataSourceFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setName("tenantDataSourceFilter");
        registration.addUrlPatterns("/*");
        registration.setOrder(filter.getOrder());
        return registration;
    }
}
