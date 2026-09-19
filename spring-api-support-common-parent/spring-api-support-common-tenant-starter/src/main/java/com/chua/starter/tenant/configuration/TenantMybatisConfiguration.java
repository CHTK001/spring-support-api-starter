package com.chua.starter.tenant.configuration;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.chua.starter.common.support.serviceinfo.ServiceInfoRegistry;
import com.chua.starter.tenant.properties.TenantProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 租户 MyBatis 配置
 * <p>
 * 配置租户拦截器，实现多租户数据隔离
 * </p>
 *
 * @since 2024/12/06
 * @author CH
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class TenantMybatisConfiguration {

    /** Tenant属性 */
    private final TenantProperties tenantProperties;

    /**
     * 配置 MyBatis-Plus 租户拦截器（客户端模式）
     * <p>
     * 租户端采用单租户独立数据库，数据已按租户物理隔离，默认【不启用】行级拦截器，
     * 避免给无 sys_tenant_标识 列的表追加租户条件。
     * 仅当配置 plugin.MyBatis-plus.tenant.客户端.tenant-线-enable=true 时启用。
     * </p>
     *
     * @return MybatisPlusInterceptor
     */
    @Bean
    @ConditionalOnProperty(prefix = TenantProperties.PRE + ".client", name = "tenant-line-enable", havingValue = "true")
    public MybatisPlusInterceptor tenantClientMybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        TenantLineInnerInterceptor tenantInterceptor = new TenantLineInnerInterceptor();
        tenantInterceptor.setTenantLineHandler(new TenantLineHandlerImpl(tenantProperties, true));
        interceptor.addInnerInterceptor(tenantInterceptor);

        ServiceInfoRegistry.getInstance().registerFlag(
                "tenant-client", "租户拦截",
                true, "客户端租户拦截器", "[字段: " + tenantProperties.getEffectiveTenantIdColumn() + "]");
        log.info("[租户配置] 客户端租户拦截器已启用，租户字段: {}", tenantProperties.getEffectiveTenantIdColumn());
        return interceptor;
    }

    /**
     * 配置 MyBatis-Plus 租户拦截器（服务端模式）
     * <p>
     * 平台端（服务端）是全局管理视角，管理所有租户数据，默认【不启用】租户行级隔离，
     * 避免给现有无 sys_tenant_标识 列的表追加租户条件而破坏功能。
     * 仅当配置 plugin.MyBatis-plus.tenant.服务端.tenant-线-enable=true 时启用。
     * </p>
     *
     * @return MybatisPlusInterceptor
     */
    @Bean
    @ConditionalOnProperty(prefix = TenantProperties.PRE + ".server", name = "tenant-line-enable", havingValue = "true")
    public MybatisPlusInterceptor tenantServerMybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        TenantLineInnerInterceptor tenantInterceptor = new TenantLineInnerInterceptor();
        tenantInterceptor.setTenantLineHandler(new TenantLineHandlerImpl(tenantProperties, false));
        interceptor.addInnerInterceptor(tenantInterceptor);

        ServiceInfoRegistry.getInstance().registerFlag(
                "tenant-server", "租户拦截",
                true, "服务端租户拦截器", "[字段: " + tenantProperties.getEffectiveTenantIdColumn() + "]");
        log.info("[租户配置] 服务端租户拦截器已启用，租户字段: {}", tenantProperties.getEffectiveTenantIdColumn());
        return interceptor;
    }
}
