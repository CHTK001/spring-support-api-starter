package com.chua.starter.tenant.configuration;

import com.chua.starter.sync.support.server.SyncServer;
import com.chua.starter.tenant.properties.TenantProperties;
import com.chua.starter.tenant.server.support.SyncServerTenantClientDirectory;
import com.chua.starter.tenant.server.support.TenantClientDirectory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 租户服务器地址解析配置。
 *
 * <p>{@code TenantEndpointResolver} 需要「当前有哪些租户端在线」这份数据，来源是同步服务端。
 * 同步服务端是否装配取决于是否引入 {@code spring-support-sync-starter}，
 * 因此这里用 {@link ObjectProvider} 延迟取，未装配时目录返回空集合，
 * 解析器自动退化到 {@code sys_tenant_home_url}。</p>
 *
 * @since 4.0.0.52
 * @author CH
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = TenantProperties.PRE + ".server", name = "enable", havingValue = "true")
public class TenantEndpointConfiguration {

    /**
     * 租户端在线实例目录。
     *
     * @param syncServerProvider 同步服务端提供者
     * @return 目录实例
     */
    @Bean
    @ConditionalOnMissingBean(TenantClientDirectory.class)
    public TenantClientDirectory tenantClientDirectory(ObjectProvider<SyncServer> syncServerProvider) {
        return new SyncServerTenantClientDirectory(syncServerProvider);
    }
}
