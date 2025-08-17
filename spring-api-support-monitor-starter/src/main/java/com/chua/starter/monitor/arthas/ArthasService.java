package com.chua.starter.monitor.arthas;

import java.util.Map;

/**
 * Arthas 管理服务
 */
public interface ArthasService {

    /**
     * 查询节点客户端的 Arthas 配置
     * 
     * @param nodeId 节点ID
     */
    Map<String, Object> getNodeArthasConfig(String nodeId);

    /**
     * 获取服务器 Arthas Tunnel 配置（包含地址与账号密码）
     */
    ArthasTunnelConfig getServerTunnelConfig(String serverId);

    /**
     * 设置服务器 Arthas Tunnel 配置（包含地址与账号密码）
     */
    boolean setServerTunnelConfig(String serverId, ArthasTunnelConfig config);
}
