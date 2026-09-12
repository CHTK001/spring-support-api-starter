package com.chua.starter.tenant.client;

import com.chua.starter.sync.support.pojo.ClientInfo;
import com.chua.starter.sync.support.spi.SyncClientInfoCustomizer;
import com.chua.starter.tenant.properties.TenantProperties;
import com.chua.starter.tenant.sync.TenantSyncMessageSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * 租户端客户端信息增强器。
 *
 * @since 2026/03/24
 * @author CH
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = TenantProperties.PRE + ".client", name = "enable", havingValue = "true")
public class TenantSyncClientInfoCustomizer implements SyncClientInfoCustomizer {

    /** Tenant属性 */
    private final TenantProperties tenantProperties;

    @Override
    /** 获取订单 */
    public int getOrder() {
        return -100;
    }

    @Override
    /** Customize */
    public void customize(ClientInfo clientInfo) {
        if (clientInfo == null) {
            return;
        }

        Map<String, Object> metadata = clientInfo.getClientMetadata();
        if (metadata != null) {
            metadata.put(TenantSyncMessageSupport.METADATA_TENANT_ID, tenantProperties.getClient().getTenantId());
            metadata.put(TenantSyncMessageSupport.METADATA_SYNC_ROLE, TenantSyncMessageSupport.ROLE_TENANT_CLIENT);
            metadata.put("tenantClientMode", true);
            metadata.put("tenantSyncEnabled", tenantProperties.getClient().isSyncEnable());
            metadata.put("tenantSyncSignEnabled", tenantProperties.isClientSyncSignEnabled());
            metadata.put("tenantSyncAckEnabled", tenantProperties.isClientSyncAckEnabled());
        }

        LinkedHashSet<String> capabilities = new LinkedHashSet<>();
        if (clientInfo.getClientCapabilities() != null) {
            capabilities.addAll(Arrays.asList(clientInfo.getClientCapabilities()));
        }
        capabilities.add("tenant-sync");
        if (tenantProperties.isClientSyncSignEnabled()) {
            capabilities.add("tenant-sync-signed");
        }
        if (tenantProperties.isClientSyncAckEnabled()) {
            capabilities.add("tenant-sync-ack");
        }
        clientInfo.setClientCapabilities(capabilities.toArray(String[]::new));
    }
}
