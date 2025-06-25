package com.chua.starter.monitor.starter.configuration;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.chua.common.support.json.Json;
import com.chua.common.support.json.JsonObject;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.socketio.support.SocketIOListener;
import com.chua.socketio.support.annotations.OnEvent;
import com.chua.socketio.support.session.SocketSession;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.message.ServerWebSocketMessage;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerService;
import com.chua.starter.monitor.starter.service.ServerConnectionService;
import com.chua.starter.monitor.starter.service.SshCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
public class SshSocketIOListener implements SocketIOListener {

    private final List<ServerConnectionService> serverConnectionServices;
    private final MonitorSysGenServerService serverService;
    private final SocketSessionTemplate socketSessionTemplate;
    private final SshCommandService sshCommandService;

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
     * SSH连接事件
     */
    @OnEvent("ssh/connect")
    public void onConnect(SocketSession session, JsonObject data) {
        try {
            Integer serverId = data.getInteger("serverId");
            if (serverId == null) {
                session.send("ssh/error", ReturnResult.illegal("服务器ID不能为空"));
                return;
            }

            ServerConnectionService sshService = getSshConnectionService();
            if (sshService == null) {
                session.send("ssh/error", ReturnResult.illegal("SSH服务不可用"));
                return;
            }

            // 这里需要根据serverId获取服务器信息
            // MonitorSysGenServer server = getServerById(serverId);
            // boolean connected = sshService.connect(server);
            
            // 临时返回成功
            session.send("ssh/connected", ReturnResult.success("SSH连接成功"));
            
        } catch (Exception e) {
            log.error("SSH连接失败: {}", e.getMessage(), e);
            session.send("ssh/error", ReturnResult.illegal("SSH连接失败: " + e.getMessage()));
        }
    }

    /**
     * SSH断开连接事件
     */
    @OnEvent("ssh/disconnect")
    public void onDisconnect(SocketSession session, JsonObject data) {
        try {
            Integer serverId = data.getInteger("serverId");
            if (serverId == null) {
                session.send("ssh/error", ReturnResult.illegal("服务器ID不能为空"));
                return;
            }

            ServerConnectionService sshService = getSshConnectionService();
            if (sshService != null) {
                sshService.disconnect(serverId);
            }

            session.send("ssh/disconnected", ReturnResult.success("SSH连接已断开"));
            
        } catch (Exception e) {
            log.error("SSH断开连接失败: {}", e.getMessage(), e);
            session.send("ssh/error", ReturnResult.illegal("SSH断开连接失败: " + e.getMessage()));
        }
    }

    /**
     * SSH命令执行事件
     */
    @OnEvent("ssh/command")
    public void onCommand(SocketSession session, JsonObject data) {
        try {
            Integer serverId = data.getInteger("serverId");
            String command = data.getString("command");
            
            if (serverId == null) {
                session.send("ssh/error", ReturnResult.illegal("服务器ID不能为空"));
                return;
            }
            
            if (StringUtils.isEmpty(command)) {
                session.send("ssh/error", ReturnResult.illegal("命令不能为空"));
                return;
            }

            ServerConnectionService sshService = getSshConnectionService();
            if (sshService == null) {
                session.send("ssh/error", ReturnResult.illegal("SSH服务不可用"));
                return;
            }

            String result = sshService.executeCommand(serverId, command);
            session.send("ssh/command-result", ReturnResult.success(result));
            
        } catch (Exception e) {
            log.error("SSH命令执行失败: {}", e.getMessage(), e);
            session.send("ssh/error", ReturnResult.illegal("SSH命令执行失败: " + e.getMessage()));
        }
    }

    /**
     * SSH数据发送事件
     */
    @OnEvent("ssh/data")
    public void onData(SocketSession session, JsonObject data) {
        try {
            Integer serverId = data.getInteger("serverId");
            String inputData = data.getString("data");
            
            if (serverId == null) {
                session.send("ssh/error", ReturnResult.illegal("服务器ID不能为空"));
                return;
            }

            ServerConnectionService sshService = getSshConnectionService();
            if (sshService != null) {
                sshService.sendData(serverId, inputData);
            }
            
        } catch (Exception e) {
            log.error("SSH数据发送失败: {}", e.getMessage(), e);
            session.send("ssh/error", ReturnResult.illegal("SSH数据发送失败: " + e.getMessage()));
        }
    }

    /**
     * SSH终端大小调整事件
     */
    @OnEvent("ssh/resize")
    public void onResize(SocketSession session, JsonObject data) {
        try {
            Integer serverId = data.getInteger("serverId");
            Integer width = data.getInteger("width");
            Integer height = data.getInteger("height");
            
            if (serverId == null || width == null || height == null) {
                session.send("ssh/error", ReturnResult.illegal("参数不完整"));
                return;
            }

            ServerConnectionService sshService = getSshConnectionService();
            if (sshService != null) {
                sshService.resizeTerminal(serverId, width, height);
            }
            
        } catch (Exception e) {
            log.error("SSH终端大小调整失败: {}", e.getMessage(), e);
            session.send("ssh/error", ReturnResult.illegal("SSH终端大小调整失败: " + e.getMessage()));
        }
    }

    /**
     * SSH文件列表事件
     */
    @OnEvent("ssh/list-files")
    public void onListFiles(SocketSession session, JsonObject data) {
        try {
            Integer serverId = data.getInteger("serverId");
            String path = data.getString("path");

            if (serverId == null) {
                session.send("ssh/error", ReturnResult.illegal("服务器ID不能为空"));
                return;
            }

            ServerConnectionService sshService = getSshConnectionService();
            if (sshService == null) {
                session.send("ssh/error", ReturnResult.illegal("SSH服务不可用"));
                return;
            }

            var result = sshService.listFiles(serverId, path);
            session.send("ssh/file-list", ReturnResult.success(result));

        } catch (Exception e) {
            log.error("SSH文件列表获取失败: {}", e.getMessage(), e);
            session.send("ssh/error", ReturnResult.illegal("SSH文件列表获取失败: " + e.getMessage()));
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
                sessionServerMap.put(session.getSessionId(), serverId);
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
                session.send("gen/server", Json.toJson(response));

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
                serverId = sessionServerMap.get(session.getSessionId());
            }

            if (serverId == null) {
                log.warn("SSH断开连接请求缺少服务器ID");
                return;
            }

            // 使用 SshCommandService 断开SSH连接
            sshCommandService.disconnectSSH(serverId);

            // 清理会话映射
            sessionServerMap.remove(session.getSessionId());
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
            String sessionId = session.getSessionId();
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
