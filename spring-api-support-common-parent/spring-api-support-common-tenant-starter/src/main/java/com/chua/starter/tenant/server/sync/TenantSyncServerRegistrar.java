package com.chua.starter.tenant.server.sync;

import com.chua.starter.sync.support.server.SyncServer;
import com.chua.starter.tenant.properties.TenantProperties;
import com.chua.starter.tenant.sync.TenantSyncMessageSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
* 自动向 同步服务端 注册租户同步服务端处理器。
*
* @since 2026/03/25
* @author CH
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(SyncServer.class)
@ConditionalOnProperty(prefix = TenantProperties.PRE + ".server", name = "enable", havingValue = "true")
public class TenantSyncServerRegistrar implements InitializingBean {

    /** Tenant属性 */
    private final TenantProperties tenantProperties;
    /** 同步服务器 */
    private final SyncServer syncServer;
    /** Tenant服务器同步请求处理器 */
    private final TenantServerSyncRequestHandler tenantServerSyncRequestHandler;
    /** Tenant服务器同步ACK处理器 */
    private final TenantServerSyncAckHandler tenantServerSyncAckHandler;

    @Override
    /** 之后属性设置 */
    public void afterPropertiesSet() {
        if (!tenantProperties.isServerMode() || !tenantProperties.getServer().isSyncEnable()) {
            return;
        }

        syncServer.registerHandler(
                TenantSyncMessageSupport.TOPIC_TENANT_SYNC_REQUEST,
                tenantServerSyncRequestHandler
        );
        log.info("[租户服务端] 已注册同步请求处理器: topic={}", TenantSyncMessageSupport.TOPIC_TENANT_SYNC_REQUEST);

        syncServer.registerHandler(
                TenantSyncMessageSupport.TOPIC_TENANT_SYNC_ACK,
                tenantServerSyncAckHandler
        );
        log.info("[租户服务端] 已注册同步 ACK 处理器: topic={}", TenantSyncMessageSupport.TOPIC_TENANT_SYNC_ACK);
    }
}
