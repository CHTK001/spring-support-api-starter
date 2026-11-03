package com.chua.starter.tenant.server.support;

import com.chua.starter.sync.support.pojo.ClientInfo;
import com.chua.starter.sync.support.server.SyncServer;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Map;
import java.util.Set;

/**
 * 基于同步服务端的租户端实例目录。
 *
 * <p>租户端应用连上平台后，{@code SyncClientWrapper} 会自动发一条 {@code sync/client/register}，
 * 把 IP、端口、应用名和 {@code TenantSyncClientInfoCustomizer} 塞进去的 {@code tenantId} 一起注册上来。
 * 平台这边因此不需要额外的注册中心，就能知道「哪个租户的哪个实例在哪」。</p>
 *
 * <p>同步服务端未启用时 {@link ObjectProvider} 取不到实例，两个方法都返回空集合，
 * 解析器自动退化到 {@code sys_tenant_home_url}。</p>
 *
 * @since 4.0.0.52
 * @author CH
 */
public class SyncServerTenantClientDirectory implements TenantClientDirectory {

    /**
     * 同步服务端；未启用时为空
     */
    private final ObjectProvider<SyncServer> syncServerProvider;

    /**
     * 创建 a 新 同步服务端租户端实例目录 实例。
     *
     * @param syncServerProvider 同步服务端提供者
     */
    public SyncServerTenantClientDirectory(ObjectProvider<SyncServer> syncServerProvider) {
        this.syncServerProvider = syncServerProvider;
    }

    @Override
    public Map<String, ClientInfo> registeredClients() {
        SyncServer server = syncServerProvider.getIfAvailable();
        return server == null ? Map.of() : server.getAllClientsRaw();
    }

    @Override
    public Set<String> connectedSessions() {
        SyncServer server = syncServerProvider.getIfAvailable();
        return server == null ? Set.of() : server.getConnectedSessions();
    }
}
