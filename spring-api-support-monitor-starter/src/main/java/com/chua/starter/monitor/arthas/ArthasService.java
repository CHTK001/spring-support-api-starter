package com.chua.starter.monitor.arthas;

import java.util.Map;

/**
 * Arthas 管理服务
 */
public interface ArthasService {

    /**
     * 查询节点客户端的 Arthas 配置
     * @param nodeId 节点ID
     */
    Map<String, Object> getNodeArthasConfig(String nodeId);

    /**
     * 获取服务器的 Tunnel 地址
     */
    String getServerTunnelAddress(Integer serverId);

    /**
     * 设置服务器的 Tunnel 地址
     */
    boolean setServerTunnelAddress(Integer serverId, String tunnelAddress);
}


