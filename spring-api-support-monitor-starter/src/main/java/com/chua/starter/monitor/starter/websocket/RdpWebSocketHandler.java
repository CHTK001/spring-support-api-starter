package com.chua.starter.monitor.starter.websocket;

import com.chua.common.support.json.Json;
import com.chua.guacamole.support.client.GuacamoleChannelSession;
import com.chua.guacamole.support.client.GuacamoleClient;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.message.ServerWebSocketMessage;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerSettingService;
import com.chua.starter.monitor.starter.service.impl.RdpServerConnectionServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.guacamole.GuacamoleClientException;
import org.apache.guacamole.GuacamoleConnectionClosedException;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.io.GuacamoleReader;
import org.apache.guacamole.io.GuacamoleWriter;
import org.apache.guacamole.net.GuacamoleTunnel;
import org.apache.guacamole.net.SimpleGuacamoleTunnel;
import org.apache.guacamole.protocol.FilteredGuacamoleWriter;
import org.apache.guacamole.protocol.GuacamoleFilter;
import org.apache.guacamole.protocol.GuacamoleInstruction;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RDP WebSocket处理器
 *
 * @author CH
 * @since 2024/12/18
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RdpWebSocketHandler extends TextWebSocketHandler {
    private static final int BUFFER_SIZE = 8192;
    private static final String PING_OPCODE = "ping";
    private final MonitorSysGenServerService serverService;
    private final MonitorSysGenServerSettingService serverSettingService;
    private final SocketSessionTemplate socketSessionTemplate;

    /**
     * WebSocket会话映射
     */
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    /**
     * 会话与服务器ID映射
     */
    private final Map<String, Integer> sessionServerMap = new ConcurrentHashMap<>();

    /**
     * 服务器与会话映射
     */
    private final Map<Integer, WebSocketSession> serverSessionMap = new ConcurrentHashMap<>();

    /**
     * 服务器与Guacamole隧道映射
     */
    private final Map<Integer, GuacamoleTunnel> serverTunnelMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        log.info("RDP WebSocket连接建立: sessionId={}", sessionId);

        // 从URL参数获取服务器ID
        String serverId = getServerIdFromSession(session);
        if (serverId != null) {
            try {
                Integer serverIdInt = Integer.valueOf(serverId);

                // 获取服务器信息
                MonitorSysGenServer server = serverService.getById(serverIdInt);
                if (server != null) {
                    // 创建Guacamole连接
                    createGuacamoleConnection(session, server);
                } else {
                    log.warn("服务器不存在: serverId={}", serverId);
                    sendErrorMessage(session, "服务器不存在");
                    // 不要立即返回，让连接保持打开状态用于测试
                }
            } catch (NumberFormatException e) {
                log.warn("无效的服务器ID: {}", serverId);
                sendErrorMessage(session, "无效的服务器ID");
                return;
            }
        }

        // 发送连接成功消息
        sendMessage("connection", Map.of(
                "type", "connected",
                "sessionId", sessionId,
                "success", true,
                "timestamp", System.currentTimeMillis()
        ));
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        Integer serverId = sessionServerMap.get(session.getId());
        if (serverId == null) {
            return;
        }
        GuacamoleTunnel tunnel = serverTunnelMap.get(serverId);
        // Ignore inbound messages if there is no associated tunnel
        if (tunnel == null)
            return;

        // Filter received instructions, handling tunnel-internal instructions
        // without passing through to guacd
        GuacamoleWriter writer = new FilteredGuacamoleWriter(tunnel.acquireWriter(), new GuacamoleFilter() {

            @Override
            public GuacamoleInstruction filter(GuacamoleInstruction instruction)
                    throws GuacamoleException {

                // Filter out all tunnel-internal instructions
                if (instruction.getOpcode().equals(GuacamoleTunnel.INTERNAL_DATA_OPCODE)) {

                    List<String> args = instruction.getArgs();
                    if (args.size() >= 2 && args.get(0).equals(PING_OPCODE)) {

                        try {
                            sendInstruction(session, new GuacamoleInstruction(
                                    GuacamoleTunnel.INTERNAL_DATA_OPCODE,
                                    PING_OPCODE, args.get(1)
                            ));
                        } catch (IOException e) {
                            log.debug("Unable to send \"ping\" response for WebSocket tunnel.", e);
                        }

                    }

                    return null;

                }

                // Pass through all non-internal instructions untouched
                return instruction;

            }

        });

        try {
            // Write received message
            writer.write(message.getPayload().toString().toCharArray());
        } catch (GuacamoleConnectionClosedException e) {
            log.debug("Connection to guacd closed.", e);
        } catch (GuacamoleException e) {
            log.debug("WebSocket tunnel write failed.", e);
        }

        tunnel.releaseWriter();
    }


    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("RDP WebSocket传输错误: sessionId={}, error={}", session.getId(), exception.getMessage(), exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String sessionId = session.getId();
        log.info("RDP WebSocket连接关闭: sessionId={}, status={}", sessionId, closeStatus);

        // 清理会话
        try (WebSocketSession webSocketSession = sessions.remove(sessionId)) {
        }
        Integer serverId = sessionServerMap.remove(sessionId);

        // 断开服务器连接
        if (serverId != null) {
            // 关闭 Guacamole 隧道
            GuacamoleTunnel tunnel = serverTunnelMap.remove(serverId);
            if (tunnel != null) {
                try {
                    tunnel.close();
                } catch (GuacamoleException e) {
                    log.warn("关闭 Guacamole 隧道失败: {}", e.getMessage());
                }
            }
            serverSessionMap.remove(serverId);
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 发送消息
     */
    private void sendMessage(String type, Object data) {
        Map<String, Object> message = Map.of(
                "type", type,
                "data", data,
                "success", true,
                "timestamp", System.currentTimeMillis()
        );
        socketSessionTemplate.send("gen/server", Json.toJson(message));
    }

    /**
     * 发送错误消息
     */
    private void sendErrorMessage(WebSocketSession session, String error) {
        sendMessage("error", Map.of("message", error));
    }

    /**
     * 从WebSocket会话中获取服务器ID
     */
    private String getServerIdFromSession(WebSocketSession session) {
        try {
            String query = session.getUri().getQuery();
            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2 && "serverId".equals(keyValue[0])) {
                        return keyValue[1];
                    }
                }
            }
        } catch (Exception e) {
            log.warn("获取服务器ID失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 创建Guacamole连接
     */
    private void createGuacamoleConnection(WebSocketSession session, MonitorSysGenServer server) {
        try {
            Integer serverId = server.getMonitorSysGenServerId();

            // 创建GuacamoleClient
            GuacamoleClient guacamoleClient = RdpServerConnectionServiceImpl.createGuacamoleClient(server, serverSettingService, Collections.emptyMap());
            if (guacamoleClient == null) {
                log.error("创建GuacamoleClient失败: serverId={}", serverId);
                sendErrorMessage(session, "创建Guacamole客户端失败");
                return;
            }

            // 连接到服务器
            guacamoleClient.connect();

            // 创建SimpleGuacamoleTunnel
            GuacamoleChannelSession guacamoleClientSession = (GuacamoleChannelSession) guacamoleClient.createSession("server-" + server.getMonitorSysGenServerId());
            SimpleGuacamoleTunnel tunnel = guacamoleClientSession.getTunnel();
            if (tunnel != null &&  tunnel.isOpen()) {
                // 保存映射关系
                sessionServerMap.put(session.getId(), serverId);
                serverSessionMap.put(serverId, session);
                serverTunnelMap.put(serverId, tunnel);

                // 启动数据监听线程
                setupGuacamoleDataListener(serverId, session, tunnel);

                log.info("RDP Guacamole连接创建成功: serverId={}, sessionId={}", serverId, session.getId());

                // 广播连接状态
                broadcastRdpConnectionStatus(serverId, server.getMonitorSysGenServerName(), "connected");
            } else {
                afterConnectionClosed(session, CloseStatus.SERVER_ERROR);
                log.error("创建SimpleGuacamoleTunnel失败: serverId={}", serverId);
                sendErrorMessage(session, "创建Guacamole隧道失败");
            }
        } catch (Exception e) {
            log.error("创建Guacamole连接异常: serverId={}, error={}", server.getMonitorSysGenServerId(), e.getMessage(), e);
            sendErrorMessage(session, "创建Guacamole连接失败: " + e.getMessage());
        }
    }

    /**
     * 广播RDP连接状态
     */
    private void broadcastRdpConnectionStatus(Integer serverId, String serverName, String status) {
        try {
            ServerWebSocketMessage message = ServerWebSocketMessage.createRdpConnectionMessage(
                    serverId, serverName, status
            );
            socketSessionTemplate.send("gen/server", Json.toJson(message));
            log.debug("广播RDP连接状态: serverId={}, status={}", serverId, status);
        } catch (Exception e) {
            log.error("广播RDP连接状态失败: serverId={}", serverId, e);
        }
    }

    /**
     * 发送RDP数据到前端
     */
    public void sendRdpDataToClient(Integer serverId, Object data) {
        try {
            WebSocketSession session = serverSessionMap.get(serverId);
            if (session != null && session.isOpen()) {
                sendMessage("rdp-data", Map.of("data", data));
            }

            // 同时通过Socket.IO广播
            MonitorSysGenServer server = serverService.getById(serverId);
            String serverName = server != null ? server.getMonitorSysGenServerName() : "Server-" + serverId;

            ServerWebSocketMessage message = ServerWebSocketMessage.createRdpDataMessage(
                    serverId, serverName, data
            );
            socketSessionTemplate.send("gen/server", Json.toJson(message));

        } catch (Exception e) {
            log.error("发送RDP数据到前端失败: serverId={}", serverId, e);
        }
    }

    /**
     * 设置 Guacamole 数据监听
     */
    private void setupGuacamoleDataListener(Integer serverId, WebSocketSession session, SimpleGuacamoleTunnel tunnel) {
        try {
            // 启动读取线程
            new Thread(() -> {
                try {
                    GuacamoleReader reader = tunnel.acquireReader();

                    // 读取 Guacamole 指令
                    try {
                        GuacamoleInstruction instruction = reader.readInstruction();
                        if (instruction != null) {
                            sendInstruction(session,
                                    new GuacamoleInstruction(GuacamoleTunnel.INTERNAL_DATA_OPCODE, tunnel.getUUID().toString())
                            );

                        }
                    } catch (GuacamoleException | IOException ex) {
                        afterConnectionClosed(session, CloseStatus.SERVER_ERROR);
                        throw new RuntimeException(ex);
                    }

                    // 发送数据到WebSocket客户端
                    StringBuilder buffer = new StringBuilder(BUFFER_SIZE);
                    char[] readMessage;
                    try {

                        // Attempt to read
                        while ((readMessage = reader.read()) != null) {
                            // Buffer message
                            buffer.append(readMessage);
                            // Flush if we expect to wait or buffer is getting full
                            if (!reader.available() || buffer.length() >= BUFFER_SIZE) {
                                sendInstruction(session, buffer.toString());
                                buffer.setLength(0);
                            }
                        }

                        // No more data
                        afterConnectionClosed(session, CloseStatus.NORMAL);
                    }

                    // Catch any thrown guacamole exception and attempt
                    // to pass within the WebSocket connection, logging
                    // each error appropriately.
                    catch (GuacamoleClientException e) {
                        log.info("WebSocket connection terminated: {}", e.getMessage());
                        log.debug("WebSocket connection terminated due to client error.", e);
                        afterConnectionClosed(session, CloseStatus.SERVER_ERROR);
                    } catch (GuacamoleConnectionClosedException e) {
                        log.debug("Connection to guacd closed.", e);
                        afterConnectionClosed(session, CloseStatus.SERVER_ERROR);
                    } catch (GuacamoleException e) {
                        log.error("Connection to guacd terminated abnormally: {}", e.getMessage());
                        log.debug("Internal error during connection to guacd.", e);
                        afterConnectionClosed(session, CloseStatus.SERVER_ERROR);
                    }
                } catch (GuacamoleException e) {
                    log.error("读取 Guacamole 数据失败: serverId={}, error={}", serverId, e.getMessage());
                } catch (Exception e) {
                    log.error("处理 Guacamole 数据失败: serverId={}, error={}", serverId, e.getMessage(), e);
                }
            }, "GuacamoleReader-" + serverId).start();

        } catch (Exception e) {
            log.error("设置 Guacamole 数据监听失败: serverId={}, error={}", serverId, e.getMessage(), e);
        }
    }

    private void sendInstruction(WebSocketSession session, GuacamoleInstruction instruction)
            throws IOException {
        sendInstruction(session, instruction.toString());
    }

    private void sendInstruction(WebSocketSession session, String instruction)
            throws IOException {

        // NOTE: Synchronization on the non-final remote field here is
        // intentional. The remote (the outbound websocket connection) is only
        // sensitive to simultaneous attempts to send messages with respect to
        // itself. If the remote changes, then the outbound websocket
        // connection has changed, and synchronization need only be performed
        // in context of the new remote.
        session.sendMessage(new TextMessage(instruction));
        sendMessage("rdp-data", Map.of("data", instruction));
    }
}
