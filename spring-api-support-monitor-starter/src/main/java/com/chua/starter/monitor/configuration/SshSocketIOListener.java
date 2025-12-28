package com.chua.starter.monitor.configuration;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.chua.common.support.json.Json;
import com.chua.common.support.json.JsonObject;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.socket.support.SocketListener;
import com.chua.socket.support.annotations.OnEvent;
import com.chua.socket.support.session.SocketSession;
import com.chua.socket.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.constant.MonitorSocketTopics;
import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.message.ServerWebSocketMessage;
import com.chua.starter.monitor.service.server.MonitorSysGenServerService;
import com.chua.starter.monitor.service.server.ServerConnectionService;
import com.chua.starter.monitor.service.server.SshCommandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSH Socket IO 监听器
 *
 * @author CH
 * @since 2024/12/18
 */
@Component
@Slf4j
public class SshSocketIOListener implements SocketListener {

    private final SshCommandService sshCommandService;

    @Autowired
    @Lazy
    private SocketSessionTemplate socketSessionTemplate;

    @Autowired
    @Lazy
    private MonitorSysGenServerService serverService;

    @Autowired
    @Lazy
    private List<ServerConnectionService> serverConnectionServices;

    public SshSocketIOListener(SshCommandService sshCommandService) {
        this.sshCommandService = sshCommandService;
    }

    // 存储会话与服务器的映射关系
    private final Map<String, Integer> sessionServerMap = new ConcurrentHashMap<>();
    private final Map<Integer, SocketSession> serverSessionMap = new ConcurrentHashMap<>();

    /**
     * 获取SSH连接服务
     */
    private ServerConnectionService getSshConnectionService() {
        return serverConnectionServices.stream()
                .filter(service -> "SSH".equals(service.getProtocol()))
                .findFirst()
                .orElse(null);
    }

    /**
     * SSH连接事件 (remote:ssh:connect)
     */
    @OnEvent(MonitorSocketTopics.Remote.Ssh.CONNECT)
    public void onRemoteSshConnect(SocketSession session, String rawData) {
        try {
            JsonObject data = Json.getJsonObject(rawData);
            Integer serverId = data.getInteger("serverId");
            if (serverId == null) {
                session.send(MonitorSocketTopics.Remote.Ssh.ERROR, ReturnResult.illegal("服务器ID不能为空"));
                return;
            }

            MonitorSysGenServer server = serverService.getById(String.valueOf(serverId));
            if (server == null) {
                session.send(MonitorSocketTopics.Remote.Ssh.ERROR, ReturnResult.illegal("服务器配置不存在"));
                return;
            }

            boolean connected = sshCommandService.connectSSH(server);
            if (connected) {
                sessionServerMap.put(session.getId(), serverId);
                serverSessionMap.put(serverId, session);
                
                ServerWebSocketMessage response = ServerWebSocketMessage.createSSHConnectMessage(
                        serverId,
                        server.getMonitorSysGenServerName(),
                        server.getMonitorSysGenServerHost(),
                        server.getMonitorSysGenServerPort()
                );
                socketSessionTemplate.send(MonitorSocketTopics.Remote.Ssh.CONNECT, Json.toJson(response));
                log.info("SSH连接成功: serverId={}", serverId);
            } else {
                session.send(MonitorSocketTopics.Remote.Ssh.ERROR, ReturnResult.illegal("SSH连接失败"));
            }
        } catch (Exception e) {
            log.error("SSH连接失败: {}", e.getMessage(), e);
            session.send(MonitorSocketTopics.Remote.Ssh.ERROR, ReturnResult.illegal("SSH连接失败: " + e.getMessage()));
        }
    }

    /**
     * SSH输入事件 (remote:ssh:input)
     */
    @OnEvent(MonitorSocketTopics.Remote.Ssh.INPUT)
    public void onRemoteSshInput(SocketSession session, String rawData) {
        try {
            JsonObject data = Json.getJsonObject(rawData);
            Integer serverId = data.getInteger("serverId");
            String inputData = data.getString("data");

            if (serverId == null) {
                session.send(MonitorSocketTopics.Remote.Ssh.ERROR, ReturnResult.illegal("服务器ID不能为空"));
                return;
            }

            if (!sshCommandService.isSSHConnected(serverId)) {
                session.send(MonitorSocketTopics.Remote.Ssh.DISCONNECT, ReturnResult.illegal("SSH未连接"));
                return;
            }

            sshCommandService.sendSSHInput(serverId, inputData != null ? inputData : "");
        } catch (Exception e) {
            log.error("SSH输入失败: {}", e.getMessage(), e);
            session.send(MonitorSocketTopics.Remote.Ssh.ERROR, ReturnResult.illegal("SSH输入失败: " + e.getMessage()));
        }
    }

    /**
     * SSH断开事件 (remote:ssh:disconnect)
     */
    @OnEvent(MonitorSocketTopics.Remote.Ssh.DISCONNECT)
    public void onRemoteSshDisconnect(SocketSession session, String rawData) {
        try {
            JsonObject data = Json.getJsonObject(rawData);
            Integer serverId = data.getInteger("serverId");
            if (serverId == null) {
                serverId = sessionServerMap.get(session.getId());
            }

            if (serverId != null) {
                sshCommandService.disconnectSSH(serverId);
                sessionServerMap.remove(session.getId());
                serverSessionMap.remove(serverId);
                
                ServerWebSocketMessage response = ServerWebSocketMessage.createSSHDisconnectMessage(
                        serverId, null, "用户主动断开连接");
                socketSessionTemplate.send(MonitorSocketTopics.Remote.Ssh.DISCONNECT, Json.toJson(response));
                log.info("SSH连接已断开: serverId={}", serverId);
            }
        } catch (Exception e) {
            log.error("SSH断开连接失败: {}", e.getMessage(), e);
        }
    }

    /**
     * SSH终端大小调整事件 (remote:ssh:resize)
     */
    @OnEvent(MonitorSocketTopics.Remote.Ssh.RESIZE)
    public void onRemoteSshResize(SocketSession session, String rawData) {
        try {
            JsonObject data = Json.getJsonObject(rawData);
            Integer serverId = data.getInteger("serverId");
            Integer cols = data.getInteger("cols");
            Integer rows = data.getInteger("rows");

            if (serverId == null || cols == null || rows == null) {
                session.send(MonitorSocketTopics.Remote.Ssh.ERROR, ReturnResult.illegal("参数不完整"));
                return;
            }

            sshCommandService.resizeSSHTerminal(serverId, cols, rows);
        } catch (Exception e) {
            log.error("SSH终端大小调整失败: {}", e.getMessage(), e);
            session.send(MonitorSocketTopics.Remote.Ssh.ERROR, ReturnResult.illegal("SSH终端大小调整失败: " + e.getMessage()));
        }
    }

    /**
     * 处理前端发送的 gen/server 事件
     * 这是前端 Socket.IO 客户端发送的统一事件
     */
    @OnEvent("gen/server")
    public void onGenServer(SocketSession session, String messageData) {
        try {
            log.debug("收到 gen/server 消息: {}", messageData);

            // 解析消息
            ServerWebSocketMessage message = Json.fromJson(messageData, ServerWebSocketMessage.class);
            if (message == null || message.getMessageType() == null) {
                log.warn("无效的 gen/server 消息: {}", messageData);
                sendErrorMessage(session, "无效的消息格式");
                return;
            }

            // 根据消息类型处理
            switch (message.getMessageType()) {
                case ServerWebSocketMessage.MessageType.SSH_CONNECT:
                    handleSSHConnect(session, message);
                    break;
                case ServerWebSocketMessage.MessageType.SSH_INPUT:
                    Integer serverId = message.getServerId();
                    if(!sshCommandService.isSSHConnected(serverId)) {
                        handleSSHDisconnect(session, message);
                        break;
                    }
                    handleSSHInput(session, message);
                    break;
                case ServerWebSocketMessage.MessageType.SSH_DISCONNECT:
                    handleSSHDisconnect(session, message);
                    break;
                case ServerWebSocketMessage.MessageType.SSH_RESIZE:
                    handleSSHResize(session, message);
                    break;
                default:
                    log.debug("未处理的消息类型: {}", message.getMessageType());
                    sendErrorMessage(session, "不支持的消息类型: " + message.getMessageType());
            }

        } catch (Exception e) {
            log.error("处理 gen/server 消息失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "消息处理失败: " + e.getMessage());
        }
    }

    /**
     * 处理SSH连接请求
     */
    private void handleSSHConnect(SocketSession session, ServerWebSocketMessage message) {
        try {
            Integer serverId = message.getServerId();
            if (serverId == null) {
                log.warn("SSH连接请求缺少服务器ID");
                sendErrorMessage(session, "服务器ID不能为空");
                return;
            }

            MonitorSysGenServer server = serverService.getById(String.valueOf(serverId));
            if (server == null) {
                sendErrorMessage(session, "服务器配置不存在");
                return;
            }

            ServerConnectionService sshService = getSshConnectionService();
            if (sshService == null) {
                sendErrorMessage(session, "SSH服务不可用");
                return;
            }

            // 使用 SshCommandService 建立SSH连接
            boolean connected = sshCommandService.connectSSH(server);
            if (connected) {
                // 存储会话映射
                sessionServerMap.put(session.getId(), serverId);
                serverSessionMap.put(serverId, session);

                // 发送连接成功消息
                ServerWebSocketMessage response = ServerWebSocketMessage.createSSHConnectMessage(
                        serverId,
                        server.getMonitorSysGenServerName(),
                        server.getMonitorSysGenServerHost(),
                        server.getMonitorSysGenServerPort()
                );

                // 广播到所有订阅了 gen/server 主题的客户端
                broadcastMessage(response);

                // 也发送给当前会话
//                session.send("gen/server", Json.toJson(response));

                log.info("SSH连接成功: serverId={}", serverId);
            } else {
                sendErrorMessage(session, "SSH连接失败");
            }
        } catch (Exception e) {
            log.error("处理SSH连接请求失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "连接异常: " + e.getMessage());
        }
    }

    /**
     * 处理SSH输入
     */
    private void handleSSHInput(SocketSession session, ServerWebSocketMessage message) {
        try {
            Integer serverId = message.getServerId();
            String data = message.getData() != null ? message.getData().toString() : "";

            if (serverId == null) {
                sendErrorMessage(session, "服务器ID不能为空");
                return;
            }

            if (StringUtils.isEmpty(data)) {
                // 空数据也是有效的（比如回车键）
                data = "";
            }

            ServerConnectionService sshService = getSshConnectionService();
            if (sshService == null) {
                sendErrorMessage(session, "SSH服务不可用");
                return;
            }

            // 使用 SshCommandService 发送输入到SSH会话
            boolean success = sshCommandService.sendSSHInput(serverId, data);
            if (!success) {
                sendErrorMessage(session, "发送SSH输入失败");
            }

        } catch (Exception e) {
            log.error("处理SSH输入失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "输入处理失败: " + e.getMessage());
        }
    }

    /**
     * 处理SSH断开连接
     */
    private void handleSSHDisconnect(SocketSession session, ServerWebSocketMessage message) {
        try {
            Integer serverId = message.getServerId();
            if (serverId == null) {
                // 尝试从会话映射中获取服务器ID
                serverId = sessionServerMap.get(session.getId());
            }

            if (serverId == null) {
                log.warn("SSH断开连接请求缺少服务器ID");
                return;
            }

            // 使用 SshCommandService 断开SSH连接
            sshCommandService.disconnectSSH(serverId);

            // 清理会话映射
            sessionServerMap.remove(session.getId());
            serverSessionMap.remove(serverId);

            // 发送断开连接消息
            ServerWebSocketMessage response = ServerWebSocketMessage.createSSHDisconnectMessage(
                    serverId, null, "用户主动断开连接");
            broadcastMessage(response);

            log.info("SSH连接已断开: serverId={}", serverId);

        } catch (Exception e) {
            log.error("处理SSH断开连接失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理SSH终端大小调整
     */
    private void handleSSHResize(SocketSession session, ServerWebSocketMessage message) {
        try {
            Integer serverId = message.getServerId();
            if (serverId == null) {
                sendErrorMessage(session, "服务器ID不能为空");
                return;
            }

            // 从消息中获取终端大小信息
            Object dataObj = message.getData();
            if (dataObj == null) {
                sendErrorMessage(session, "缺少终端大小信息");
                return;
            }

            Map<String, Object> resizeData;
            if (dataObj instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked")
                Map<String, Object> tempMap = (Map<String, Object>) dataObj;
                resizeData = tempMap;
            } else {
                sendErrorMessage(session, "终端大小数据格式错误");
                return;
            }

            Integer cols = (Integer) resizeData.get("cols");
            Integer rows = (Integer) resizeData.get("rows");

            if (cols == null || rows == null) {
                sendErrorMessage(session, "终端大小参数无效");
                return;
            }

            ServerConnectionService sshService = getSshConnectionService();
            if (sshService == null) {
                sendErrorMessage(session, "SSH服务不可用");
                return;
            }

            // 使用 SshCommandService 调整终端大小
            boolean success = sshCommandService.resizeSSHTerminal(serverId, cols, rows);
            if (!success) {
                sendErrorMessage(session, "调整终端大小失败");
            }

        } catch (Exception e) {
            log.error("处理SSH终端大小调整失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "终端大小调整失败: " + e.getMessage());
        }
    }

    /**
     * 发送错误消息给客户端
     */
    private void sendErrorMessage(SocketSession session, String errorMessage) {
        try {
            ServerWebSocketMessage errorResponse = ServerWebSocketMessage.createSSHErrorMessage(
                    null, null, errorMessage);
            session.send("gen/server", Json.toJson(errorResponse));
        } catch (Exception e) {
            log.error("发送错误消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 广播消息到所有订阅了 gen/server 主题的客户端
     */
    private void broadcastMessage(ServerWebSocketMessage message) {
        try {
            String messageJson = Json.toJson(message);
            // 使用 SocketSessionTemplate 广播消息
            socketSessionTemplate.send("gen/server", messageJson);
        } catch (Exception e) {
            log.error("广播消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 会话断开时的清理工作
     */
    public void onSessionDisconnect(SocketSession session) {
        try {
            String sessionId = session.getId();
            Integer serverId = sessionServerMap.get(sessionId);

            if (serverId != null) {
                // 使用 SshCommandService 断开SSH连接
                sshCommandService.disconnectSSH(serverId);

                // 清理映射
                sessionServerMap.remove(sessionId);
                serverSessionMap.remove(serverId);

                log.info("会话断开，已清理SSH连接: sessionId={}, serverId={}", sessionId, serverId);
            }
        } catch (Exception e) {
            log.error("会话断开清理失败: {}", e.getMessage(), e);
        }
    }
}
