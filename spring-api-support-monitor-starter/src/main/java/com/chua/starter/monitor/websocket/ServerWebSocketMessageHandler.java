package com.chua.starter.monitor.websocket;

import com.chua.common.support.json.Json;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.message.ServerWebSocketMessage;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerComponentService;
import com.chua.starter.monitor.starter.service.ServerConnectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 服务器WebSocket消息处理器
 *
 * @author CH
 * @since 2024/12/23
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ServerWebSocketMessageHandler {

    private final MonitorSysGenServerService serverService;
    private final MonitorSysGenServerComponentService componentService;
    private final List<ServerConnectionService> connectionServices;
    private final SocketSessionTemplate socketSessionTemplate;

    /**
     * 处理WebSocket消息
     */
    public void handleMessage(String topic, String messageData) {
        try {
            ServerWebSocketMessage message = Json.fromJson(messageData, ServerWebSocketMessage.class);
            if (message == null || message.getMessageType() == null) {
                log.warn("无效的WebSocket消息: {}", messageData);
                return;
            }

            log.debug("处理WebSocket消息: type={}, serverId={}", message.getMessageType(), message.getServerId());

            switch (message.getMessageType()) {
                case ServerWebSocketMessage.MessageType.SSH_CONNECT:
                    handleSSHConnect(message);
                    break;
                case ServerWebSocketMessage.MessageType.SSH_INPUT:
                    handleSSHInput(message);
                    break;
                case ServerWebSocketMessage.MessageType.SSH_DISCONNECT:
                    handleSSHDisconnect(message);
                    break;

                default:
                    log.debug("未处理的消息类型: {}", message.getMessageType());
            }
        } catch (Exception e) {
            log.error("处理WebSocket消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理SSH连接请求
     */
    private void handleSSHConnect(ServerWebSocketMessage message) {
        try {
            Integer serverId = message.getServerId();
            if (serverId == null) {
                log.warn("SSH连接请求缺少服务器ID");
                return;
            }

            MonitorSysGenServer server = serverService.getById(String.valueOf(serverId));
            if (server == null) {
                sendSSHError(serverId, "服务器配置不存在");
                return;
            }

            // 获取SSH连接服务
            ServerConnectionService sshService = getConnectionService("SSH");
            if (sshService == null) {
                sendSSHError(serverId, "SSH服务不可用");
                return;
            }

            // 建立SSH连接
            boolean connected = sshService.connect(server);
            if (connected) {
                // 发送连接成功消息
                ServerWebSocketMessage response = ServerWebSocketMessage.createSSHConnectMessage(
                    serverId,
                    server.getMonitorSysGenServerName(),
                    server.getMonitorSysGenServerHost(),
                    server.getMonitorSysGenServerPort()
                );
                broadcastMessage(response);

                log.info("SSH连接成功: serverId={}", serverId);
            } else {
                sendSSHError(serverId, "SSH连接失败");
            }
        } catch (Exception e) {
            log.error("处理SSH连接请求失败: {}", e.getMessage(), e);
            sendSSHError(message.getServerId(), "连接异常: " + e.getMessage());
        }
    }

    /**
     * 处理SSH输入
     */
    private void handleSSHInput(ServerWebSocketMessage message) {
        try {
            Integer serverId = message.getServerId();
            String inputData = (String) message.getData();

            if (serverId == null || inputData == null) {
                log.warn("SSH输入请求参数无效: serverId={}, data={}", serverId, inputData);
                return;
            }

            // 获取SSH连接服务
            ServerConnectionService sshService = getConnectionService("SSH");
            if (sshService == null) {
                sendSSHError(serverId, "SSH服务不可用");
                return;
            }

            // 检查连接状态
            if (!sshService.isConnected(serverId)) {
                sendSSHError(serverId, "SSH连接已断开");
                return;
            }

            // 发送数据到SSH服务器
            sshService.sendData(serverId, inputData);

        } catch (Exception e) {
            log.error("处理SSH输入失败: {}", e.getMessage(), e);
            sendSSHError(message.getServerId(), "输入处理异常: " + e.getMessage());
        }
    }

    /**
     * 处理SSH断开连接
     */
    private void handleSSHDisconnect(ServerWebSocketMessage message) {
        try {
            Integer serverId = message.getServerId();
            if (serverId == null) {
                log.warn("SSH断开连接请求缺少服务器ID");
                return;
            }

            // 获取SSH连接服务
            ServerConnectionService sshService = getConnectionService("SSH");
            if (sshService != null) {
                sshService.disconnect(serverId);
            }

            // 发送断开连接消息
            MonitorSysGenServer server = serverService.getById(String.valueOf(serverId));
            String serverName = server != null ? server.getMonitorSysGenServerName() : "Server-" + serverId;
            
            ServerWebSocketMessage response = ServerWebSocketMessage.createSSHDisconnectMessage(
                serverId,
                serverName,
                "用户主动断开"
            );
            broadcastMessage(response);

            log.info("SSH连接已断开: serverId={}", serverId);
        } catch (Exception e) {
            log.error("处理SSH断开连接失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送SSH错误消息
     */
    private void sendSSHError(Integer serverId, String errorMessage) {
        try {
            MonitorSysGenServer server = serverService.getById(String.valueOf(serverId));
            String serverName = server != null ? server.getMonitorSysGenServerName() : "Server-" + serverId;
            
            ServerWebSocketMessage errorMsg = ServerWebSocketMessage.createSSHErrorMessage(
                serverId,
                serverName,
                errorMessage
            );
            broadcastMessage(errorMsg);
        } catch (Exception e) {
            log.error("发送SSH错误消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 广播消息
     */
    private void broadcastMessage(ServerWebSocketMessage message) {
        try {
            String topic = "gen/server";
            socketSessionTemplate.send(topic, Json.toJson(message));
        } catch (Exception e) {
            log.error("广播消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取连接服务
     */
    private ServerConnectionService getConnectionService(String protocol) {
        return connectionServices.stream()
                .filter(service -> service.getProtocol().equalsIgnoreCase(protocol))
                .findFirst()
                .orElse(null);
    }

    /**
     * 发送SSH数据到前端
     */
    public void sendSSHDataToClient(Integer serverId, String data) {
        try {
            MonitorSysGenServer server = serverService.getById(String.valueOf(serverId));
            String serverName = server != null ? server.getMonitorSysGenServerName() : "Server-" + serverId;

            ServerWebSocketMessage message = ServerWebSocketMessage.createSSHDataMessage(
                serverId,
                serverName,
                data
            );
            broadcastMessage(message);
        } catch (Exception e) {
            log.error("发送SSH数据到前端失败: {}", e.getMessage(), e);
        }
    }




    /**
     * 从数据Map中提取Long类型值
     */
    private Long extractLongFromData(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                log.warn("无法解析Long类型参数: {} = {}", key, value);
                return null;
            }
        }
        return null;
    }


}
