package com.chua.starter.tenant.client;

import com.chua.common.support.network.net.NetUtils;
import com.chua.starter.sync.support.pojo.ClientInfo;
import com.chua.starter.sync.support.spi.SyncClientInfoCustomizer;
import com.chua.starter.tenant.properties.TenantProperties;
import com.chua.starter.tenant.sync.TenantSyncMessageSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * 租户端客户端信息增强器。
 *
 * <p>除租户标识与同步能力外，这里还负责上报「其它服务该用什么地址访问我」：
 * 平台侧解析租户服务器地址时用它，不必假设同步通道的端口就是业务 HTTP 端口。</p>
 *
 * @since 2026/03/24
 * @author CH
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = TenantProperties.PRE + ".client", name = "enable", havingValue = "true")
public class TenantSyncClientInfoCustomizer implements SyncClientInfoCustomizer {

    /**
     * server.port 未配置时的默认端口
     */
    private static final int DEFAULT_SERVER_PORT = 8080;

    /**
     * 端口配置键
     */
    private static final String PROPERTY_SERVER_PORT = "server.port";

    /**
     * 上下文路径配置键
     */
    private static final String PROPERTY_CONTEXT_PATH = "server.servlet.context-path";

    /**
     * Tenant属性
     */
    private final TenantProperties tenantProperties;

    /**
     * 运行环境，仅在未显式配置基地址时用于推导
     */
    private final Environment environment;

    @Override
    /**
     * 获取订单
    */
    public int getOrder() {
        return -100;
    }

    @Override
    /**
     * Customize
    */
    public void customize(ClientInfo clientInfo) {
        if (clientInfo == null) {
            return;
        }

        Map<String, Object> metadata = clientInfo.getClientMetadata();
        if (metadata != null) {
            metadata.put(TenantSyncMessageSupport.METADATA_TENANT_ID, tenantProperties.getClient().getTenantId());
            metadata.put(TenantSyncMessageSupport.METADATA_SYNC_ROLE, TenantSyncMessageSupport.ROLE_TENANT_CLIENT);
            // 平台按这个地址把请求打到租户自己的服务器：显式配置优先，未配置时按运行时端口推导
            metadata.put(TenantSyncMessageSupport.METADATA_TENANT_BASE_URL, resolveBaseUrl());
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

    /**
     * 解析租户端对外基地址。
     *
     * <p>显式配置的 {@code tenant-base-url} 直接采用；否则按「首选非回环 IPv4 + server.port +
     * context-path」推导。推导值在容器 / 反向代理后面可能是内网地址或错误端口，
     * 所以只作默认值，生产环境应显式配置。</p>
     *
     * @return 基地址，不含末尾斜杠
     */
    private String resolveBaseUrl() {
        String configured = tenantProperties.getClient().getTenantBaseUrl();
        if (configured != null && !configured.isBlank()) {
            return trimTrailingSlash(configured.trim());
        }

        String ip = NetUtils.getPreferredLocalIp();
        Integer port = environment.getProperty(PROPERTY_SERVER_PORT, Integer.class);
        String contextPath = environment.getProperty(PROPERTY_CONTEXT_PATH, "");
        String path = contextPath == null || contextPath.isBlank() || "/".equals(contextPath)
                ? ""
                : (contextPath.startsWith("/") ? contextPath : "/" + contextPath);
        return "http://" + ip + ":" + (port == null || port <= 0 ? DEFAULT_SERVER_PORT : port) + path;
    }

    /**
     * 去掉末尾斜杠，避免拼接路径时出现双斜杠。
     *
     * @param url 地址
     * @return 规范化后的地址
     */
    private String trimTrailingSlash(String url) {
        String value = url;
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }
}
