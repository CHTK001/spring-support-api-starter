package com.chua.starter.monitor.starter.service.impl;

import com.chua.common.support.json.Json;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.starter.constant.ServerConnectionStatus;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.entity.ServerConnectionMessage;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerService;
import com.chua.starter.monitor.starter.service.ServerConnectionWebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 服务器连接状态WebSocket服务实现类
 *
 * @author CH
 * @since 2024/12/20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServerConnectionWebSocketServiceImpl implements ServerConnectionWebSocketService {

    private final SocketSessionTemplate socketSessionTemplate;
    private final MonitorSysGenServerService monitorSysGenServerService;

    private static final String TOPIC_SERVER_CONNECTION = "server_connection";

    @Override
    public void sendConnectionStatusChange(ServerConnectionMessage message) {
        try {
            message.setTimestamp(System.currentTimeMillis());
            String jsonMessage = Json.toJson(message);
            
            // 发送到指定主题
            socketSessionTemplate.sendToTopic(TOPIC_SERVER_CONNECTION, jsonMessage);
            
            log.debug("发送连接状态变化消息: serverId={}, status={}", 
                message.getServerId(), message.getConnectionStatus());
        } catch (Exception e) {
            log.error("发送连接状态变化消息失败", e);
        }
    }

    @Override
    public void sendConnectionTestResult(ServerConnectionMessage message) {
        try {
            message.setMessageType(ServerConnectionMessage.MessageType.CONNECTION_TEST_RESULT);
            message.setTimestamp(System.currentTimeMillis());
            String jsonMessage = Json.toJson(message);
            
            socketSessionTemplate.sendToTopic(TOPIC_SERVER_CONNECTION, jsonMessage);
            
            log.debug("发送连接测试结果消息: serverId={}, status={}", 
                message.getServerId(), message.getConnectionStatus());
        } catch (Exception e) {
            log.error("发送连接测试结果消息失败", e);
        }
    }

    @Override
    public void sendServerOnline(Integer serverId, String serverName) {
        ServerConnectionMessage message = ServerConnectionMessage.builder()
                .messageType(ServerConnectionMessage.MessageType.SERVER_ONLINE)
                .serverId(serverId)
                .serverName(serverName)
                .connectionStatus(ServerConnectionStatus.ONLINE)
                .statusDesc(ServerConnectionStatus.getStatusDesc(ServerConnectionStatus.ONLINE))
                .connectTime(LocalDateTime.now())
                .timestamp(System.currentTimeMillis())
                .build();
        
        sendConnectionStatusChange(message);
    }

    @Override
    public void sendServerOffline(Integer serverId, String serverName, String errorMessage) {
        ServerConnectionMessage message = ServerConnectionMessage.builder()
                .messageType(ServerConnectionMessage.MessageType.SERVER_OFFLINE)
                .serverId(serverId)
                .serverName(serverName)
                .connectionStatus(ServerConnectionStatus.OFFLINE)
                .statusDesc(ServerConnectionStatus.getStatusDesc(ServerConnectionStatus.OFFLINE))
                .errorMessage(errorMessage)
                .connectTime(LocalDateTime.now())
                .timestamp(System.currentTimeMillis())
                .build();
        
        sendConnectionStatusChange(message);
    }

    @Override
    public void broadcastAllServerStatus() {
        try {
            List<MonitorSysGenServer> servers = monitorSysGenServerService.list();
            
            for (MonitorSysGenServer server : servers) {
                ServerConnectionMessage message = ServerConnectionMessage.builder()
                        .messageType(ServerConnectionMessage.MessageType.CONNECTION_STATUS_CHANGE)
                        .serverId(server.getMonitorSysGenServerId())
                        .serverName(server.getMonitorSysGenServerName())
                        .connectionStatus(server.getMonitorSysGenServerConnectionStatus())
                        .statusDesc(ServerConnectionStatus.getStatusDesc(server.getMonitorSysGenServerConnectionStatus()))
                        .errorMessage(server.getMonitorSysGenServerConnectionError())
                        .connectTime(server.getMonitorSysGenServerLastConnectTime())
                        .timestamp(System.currentTimeMillis())
                        .build();
                
                sendConnectionStatusChange(message);
            }
            
            log.info("广播所有服务器连接状态完成，共{}台服务器", servers.size());
        } catch (Exception e) {
            log.error("广播所有服务器连接状态失败", e);
        }
    }
}
