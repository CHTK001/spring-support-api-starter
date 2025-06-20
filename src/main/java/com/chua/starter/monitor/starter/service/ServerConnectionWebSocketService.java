package com.chua.starter.monitor.starter.service;

import com.chua.starter.monitor.starter.entity.ServerConnectionMessage;

/**
 * 服务器连接状态WebSocket服务接口
 *
 * @author CH
 * @since 2024/12/20
 */
public interface ServerConnectionWebSocketService {

    /**
     * 发送连接状态变化消息
     *
     * @param message 连接状态消息
     */
    void sendConnectionStatusChange(ServerConnectionMessage message);

    /**
     * 发送连接测试结果消息
     *
     * @param message 连接测试结果消息
     */
    void sendConnectionTestResult(ServerConnectionMessage message);

    /**
     * 发送服务器上线消息
     *
     * @param serverId 服务器ID
     * @param serverName 服务器名称
     */
    void sendServerOnline(Integer serverId, String serverName);

    /**
     * 发送服务器离线消息
     *
     * @param serverId 服务器ID
     * @param serverName 服务器名称
     * @param errorMessage 错误信息
     */
    void sendServerOffline(Integer serverId, String serverName, String errorMessage);

    /**
     * 广播所有服务器连接状态
     */
    void broadcastAllServerStatus();
}
