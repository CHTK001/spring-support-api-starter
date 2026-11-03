package com.chua.starter.tenant.server.support;

import com.chua.starter.sync.support.pojo.ClientInfo;

import java.util.Map;
import java.util.Set;

/**
 * 租户端在线实例目录。
 *
 * <p>抽成接口而不是直接依赖 {@code SyncServer}，是为了让地址解析逻辑可单测：
 * {@code SyncServer} 的客户端表是私有的，没有注入入口，测试里造不出「已注册的租户端」。
 * 生产实现 {@link SyncServerTenantClientDirectory} 只是把 {@code SyncServer} 的两张表读出来。</p>
 *
 * @since 4.0.0.52
 * @author CH
 */
public interface TenantClientDirectory {

    /**
     * 已注册的客户端。
     *
     * @return 会话标识 → 客户端信息；没有任何注册时返回空映射
     */
    Map<String, ClientInfo> registeredClients();

    /**
     * 当前仍在连接的会话。
     *
     * @return 会话标识集合；同步服务端未启用时返回空集合
     */
    Set<String> connectedSessions();
}
