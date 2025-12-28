package com.chua.starter.monitor.configuration;

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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义连接 Socket IO 监听器
 * 用于处理非标准协议的服务器连接
 *
 * @author CH
 * @since 2024/12/25
 */
@Component
@Slf4j
public class CustomSocketIOListener implements SocketListener {

    @Autowired
    @Lazy
    private SocketSessionTemplate socketSessionTemplate;

    @Autowired
    @Lazy
    private MonitorSysGenServerService serverService;

    @Autowired
    @Lazy
    private List<ServerConnectionService> serverConnectionServices;

    // 存储会话与服务器的映射关系
    private final Map<String, Integer> sessionServerMap = new ConcurrentHashMap<>();
    private final Map<Integer, SocketSession> serverSessionMap = new ConcurrentHashMap<>();

    /**
     * 获取自定义连接服务
     */
    private ServerConnectionService getCustomConnectionService() {
        return serverConnectionServices.stream()
                .filter(service -> "CUSTOM".equals(service.getProtocol()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据协议类型获取连接服务
     */
    private ServerConnectionService getConnectionServiceByProtocol(String protocol) {
        if (protocol == null) {
            return getCustomConnectionService();
        }
        return serverConnectionServices.stream()
                .filter(service -> protocol.equalsIgnoreCase(service.getProtocol()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 自定义连接事件 (remote:custom:connect)
     */
    @OnEvent(MonitorSocketTopics.Remote.Custom.CONNECT)
    public void onConnect(SocketSession session, JsonObject data) {
        try {
            Integer serverId = data.getInteger("serverId");
            String protocol = data.getString("protocol");

            if (serverId == null) {
                session.send(MonitorSocketTopics.Remote.Custom.ERROR, ReturnResult.illegal("服务器ID不能为空"));
                return;
            }

            ServerConnectionService connectionService = getConnectionServiceByProtocol(protocol);
            if (connectionService == null) {
                session.send(MonitorSocketTopics.Remote.Custom.ERROR, ReturnResult.illegal("连接服务不可用: " + protocol));
                return;
            }

            MonitorSysGenServer server = serverService.getById(String.valueOf(serverId));
            if (server == null) {
                session.send(MonitorSocketTopics.Remote.Custom.ERROR, ReturnResult.illegal("服务器配置不存在"));
                return;
            }

            boolean connected = connectionService.connect(server);
            if (connected) {
                sessionServerMap.put(session.getId(), serverId);
                serverSessionMap.put(serverId, session);
                session.send(MonitorSocketTopics.Remote.Custom.CONNECTED, ReturnResult.success("连接成功"));
            } else {
                session.send(MonitorSocketTopics.Remote.Custom.ERROR, ReturnResult.illegal("连接失败"));
            }

        } catch (Exception e) {
            log.error("自定义连接失败: {}", e.getMessage(), e);
            session.send(MonitorSocketTopics.Remote.Custom.ERROR, ReturnResult.illegal("连接失败: " + e.getMessage()));
        }
    }

    /**
     * 自定义断开连接事件 (remote:custom:disconnect)
     */
    @OnEvent(MonitorSocketTopics.Remote.Custom.DISCONNECT)
    public void onDisconnect(SocketSession session, JsonObject data) {
        try {
            Integer serverId = data.getInteger("serverId");
            String protocol = data.getString("protocol");

            if (serverId == null) {
                serverId = sessionServerMap.get(session.getId());
            }

            if (serverId != null) {
                ServerConnectionService connectionService = getConnectionServiceByProtocol(protocol);
                if (connectionService != null) {
                    connectionService.disconnect(serverId);
                }
                sessionServerMap.remove(session.getId());
                serverSessionMap.remove(serverId);
            }

            session.send(MonitorSocketTopics.Remote.Custom.DISCONNECTED, ReturnResult.success("连接已断开"));

        } catch (Exception e) {
            log.error("自定义断开连接失败: {}", e.getMessage(), e);
            session.send(MonitorSocketTopics.Remote.Custom.ERROR, ReturnResult.illegal("断开连接失败: " + e.getMessage()));
        }
    }

    /**
     * 自定义数据发送事件 (remote:custom:data)
     */
    @OnEvent(MonitorSocketTopics.Remote.Custom.DATA)
    public void onData(SocketSession session, JsonObject data) {
        try {
            Integer serverId = data.getInteger("serverId");
            String inputData = data.getString("data");
            String protocol = data.getString("protocol");

            if (serverId == null) {
                session.send(MonitorSocketTopics.Remote.Custom.ERROR, ReturnResult.illegal("服务器ID不能为空"));
                return;
            }

            ServerConnectionService connectionService = getConnectionServiceByProtocol(protocol);
            if (connectionService != null) {
                connectionService.sendData(serverId, inputData);
            }

        } catch (Exception e) {
            log.error("自定义数据发送失败: {}", e.getMessage(), e);
            session.send(MonitorSocketTopics.Remote.Custom.ERROR, ReturnResult.illegal("数据发送失败: " + e.getMessage()));
        }
    }

    /**
     * 自定义命令执行事件 (remote:custom:command)
     */
    @OnEvent(MonitorSocketTopics.Remote.Custom.COMMAND)
    public void onCommand(SocketSession session, JsonObject data) {
        try {
            Integer serverId = data.getInteger("serverId");
            String command = data.getString("command");
            String protocol = data.getString("protocol");

            if (serverId == null) {
                session.send(MonitorSocketTopics.Remote.Custom.ERROR, ReturnResult.illegal("服务器ID不能为空"));
                return;
            }

            ServerConnectionService connectionService = getConnectionServiceByProtocol(protocol);
            if (connectionService != null) {
                String result = connectionService.executeCommand(serverId, command);
                session.send(MonitorSocketTopics.Remote.Custom.COMMAND_RESULT, ReturnResult.success(result));
            } else {
                session.send(MonitorSocketTopics.Remote.Custom.ERROR, ReturnResult.illegal("连接服务不可用"));
            }

        } catch (Exception e) {
            log.error("自定义命令执行失败: {}", e.getMessage(), e);
            session.send(MonitorSocketTopics.Remote.Custom.ERROR, ReturnResult.illegal("命令执行失败: " + e.getMessage()));
        }
    }

    /**
     * 自定义屏幕/终端大小调整事件 (remote:custom:resize)
     */
    @OnEvent(MonitorSocketTopics.Remote.Custom.RESIZE)
    public void onResize(SocketSession session, JsonObject data) {
        try {
            Integer serverId = data.getInteger("serverId");
            Integer width = data.getInteger("width");
            Integer height = data.getInteger("height");
            String protocol = data.getString("protocol");

            if (serverId == null || width == null || height == null) {
                session.send(MonitorSocketTopics.Remote.Custom.ERROR, ReturnResult.illegal("参数不完整"));
                return;
            }

            ServerConnectionService connectionService = getConnectionServiceByProtocol(protocol);
            if (connectionService != null) {
                connectionService.resizeTerminal(serverId, width, height);
            }

        } catch (Exception e) {
            log.error("自定义屏幕大小调整失败: {}", e.getMessage(), e);
            session.send(MonitorSocketTopics.Remote.Custom.ERROR, ReturnResult.illegal("屏幕大小调整失败: " + e.getMessage()));
        }
    }

    public void onSessionDisconnect(SocketSession session) {
        try {
            String sessionId = session.getId();
            Integer serverId = sessionServerMap.get(sessionId);

            if (serverId != null) {
                MonitorSysGenServer server = serverService.getById(String.valueOf(serverId));
                if (server != null) {
                    String protocol = server.getMonitorSysGenServerProtocol();
                    ServerConnectionService connectionService = getConnectionServiceByProtocol(protocol);
                    if (connectionService != null) {
                        connectionService.disconnect(serverId);
                    }
                }

                sessionServerMap.remove(sessionId);
                serverSessionMap.remove(serverId);

                log.info("会话断开，已清理自定义连接: sessionId={}, serverId={}", sessionId, serverId);
            }
        } catch (Exception e) {
            log.error("会话断开清理失败: {}", e.getMessage(), e);
        }
    }
}
