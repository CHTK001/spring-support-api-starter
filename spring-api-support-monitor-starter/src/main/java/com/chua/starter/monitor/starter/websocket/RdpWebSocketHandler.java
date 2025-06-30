package com.chua.starter.monitor.starter.websocket;

import com.chua.common.support.json.Json;
import com.chua.common.support.utils.MapUtils;
import com.chua.guacamole.support.client.GuacamoleClient;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.message.ServerWebSocketMessage;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerSettingService;
import com.chua.starter.monitor.starter.service.impl.RdpServerConnectionServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.io.GuacamoleReader;
import org.apache.guacamole.io.GuacamoleWriter;
import org.apache.guacamole.net.SimpleGuacamoleTunnel;
import org.apache.guacamole.protocol.GuacamoleInstruction;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
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
public class RdpWebSocketHandler implements WebSocketHandler {

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
    private final Map<Integer, SimpleGuacamoleTunnel> serverTunnelMap = new ConcurrentHashMap<>();

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
                    return;
                }
            } catch (NumberFormatException e) {
                log.warn("无效的服务器ID: {}", serverId);
                sendErrorMessage(session, "无效的服务器ID");
                return;
            }
        }

        // 发送连接成功消息
        sendMessage(session, "connection", Map.of(
            "type", "connected",
            "sessionId", sessionId,
            "success", true,
            "timestamp", System.currentTimeMillis()
        ));
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage textMessage) {
            handleTextMessage(session, textMessage);
        } else if (message instanceof BinaryMessage binaryMessage) {
            handleBinaryMessage(session, binaryMessage);
        }
    }

    /**
     * 处理文本消息
     */
    private void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String payload = message.getPayload();
            @SuppressWarnings("unchecked")
            Map<String, Object> data = Json.fromJson(payload, Map.class);
            
            String action = (String) data.get("action");
            if (action == null) {
                sendErrorMessage(session, "action不能为空");
                return;
            }

            switch (action) {
                case "connect" -> handleConnect(session, data);
                case "disconnect" -> handleDisconnect(session, data);
                case "mouse" -> handleMouseEvent(session, data);
                case "keyboard" -> handleKeyboardEvent(session, data);
                case "resize" -> handleScreenResize(session, data);
                case "clipboard" -> handleClipboard(session, data);
                case "screenshot" -> handleScreenshot(session, data);
                default -> sendErrorMessage(session, "未知的操作类型: " + action);
            }
        } catch (Exception e) {
            log.error("处理RDP WebSocket消息失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "消息处理失败: " + e.getMessage());
        }
    }

    /**
     * 处理二进制消息
     */
    private void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        try {
            String sessionId = session.getId();
            Integer serverId = sessionServerMap.get(sessionId);

            if (serverId == null) {
                log.warn("会话未关联服务器: sessionId={}", sessionId);
                return;
            }

            SimpleGuacamoleTunnel tunnel = serverTunnelMap.get(serverId);
            if (tunnel != null) {
                try {
                    // 将二进制数据发送到 Guacamole 隧道
                    byte[] payload = message.getPayload().array();
                    GuacamoleWriter writer = tunnel.acquireWriter();
                    writer.write(new String(payload).toCharArray());
                } catch (GuacamoleException e) {
                    log.error("发送二进制数据到 Guacamole 隧道失败: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("处理RDP二进制消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理连接请求
     */
    private void handleConnect(WebSocketSession session, Map<String, Object> data) {
        try {
            Integer serverId = MapUtils.getInteger(data, "serverId");
            if (serverId == null) {
                sendErrorMessage(session, "服务器ID不能为空");
                return;
            }

            // 检查是否已经存在连接
            if (sessionServerMap.containsKey(session.getId())) {
                Integer existingServerId = sessionServerMap.get(session.getId());
                log.warn("会话已存在连接: sessionId={}, existingServerId={}, newServerId={}",
                    session.getId(), existingServerId, serverId);
                sendErrorMessage(session, "会话已存在连接，请先断开现有连接");
                return;
            }

            // 检查服务器是否已被其他会话连接
            if (serverSessionMap.containsKey(serverId)) {
                WebSocketSession existingSession = serverSessionMap.get(serverId);
                if (existingSession != null && existingSession.isOpen() && !existingSession.getId().equals(session.getId())) {
                    log.warn("服务器已被其他会话连接: serverId={}, existingSessionId={}, newSessionId={}",
                        serverId, existingSession.getId(), session.getId());
                    sendErrorMessage(session, "服务器已被其他会话连接");
                    return;
                }
            }

            MonitorSysGenServer server = serverService.getById(serverId);
            if (server == null) {
                sendErrorMessage(session, "服务器不存在");
                return;
            }

            // 创建 GuacamoleClient
            GuacamoleClient guacamoleClient = RdpServerConnectionServiceImpl.createGuacamoleClient(server, serverSettingService);
            if (guacamoleClient == null) {
                sendErrorMessage(session, "创建 Guacamole 客户端失败");
                return;
            }

            // 直接获取 SimpleGuacamoleTunnel
            SimpleGuacamoleTunnel guacamoleTunnel = guacamoleClient.createSession().getTunnel();
            if (guacamoleTunnel != null) {
                // 保存映射关系
                sessionServerMap.put(session.getId(), serverId);
                serverSessionMap.put(serverId, session);
                serverTunnelMap.put(serverId, guacamoleTunnel);

                // 设置数据监听
                setupGuacamoleDataListener(serverId, session, guacamoleTunnel);

                // 发送WebSocket消息
                sendMessage(session, "connect-result", Map.of(
                    "success", true,
                    "serverId", serverId,
                    "message", "RDP连接成功"
                ));

                // 通过Socket.IO广播RDP连接状态
                broadcastRdpConnectionStatus(serverId, server.getMonitorSysGenServerName(), "connected");

                log.info("RDP Guacamole 连接建立成功: serverId={}, sessionId={}", serverId, session.getId());

            } else {
                sendMessage(session, "connect-result", Map.of(
                    "success", false,
                    "serverId", serverId,
                    "message", "RDP连接失败"
                ));

                // 广播连接失败状态
                broadcastRdpConnectionStatus(serverId, server.getMonitorSysGenServerName(), "failed");
            }
        } catch (Exception e) {
            log.error("处理RDP连接请求失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "连接失败: " + e.getMessage());
        }
    }

    /**
     * 处理断开连接请求
     */
    private void handleDisconnect(WebSocketSession session, Map<String, Object> data) {
        try {
            Integer serverId = sessionServerMap.get(session.getId());
            if (serverId == null) {
                sendErrorMessage(session, "会话未关联服务器");
                return;
            }

            // 关闭 Guacamole 隧道
            SimpleGuacamoleTunnel tunnel = serverTunnelMap.get(serverId);
            if (tunnel != null) {
                try {
                    tunnel.close();
                } catch (GuacamoleException e) {
                    log.warn("关闭 Guacamole 隧道失败: {}", e.getMessage());
                }
            }

            // 清理映射关系
            sessionServerMap.remove(session.getId());
            serverSessionMap.remove(serverId);
            serverTunnelMap.remove(serverId);

            sendMessage(session, "disconnect-result", Map.of(
                "success", true,
                "serverId", serverId,
                "message", "RDP连接已断开"
            ));

            // 广播断开连接状态
            MonitorSysGenServer server = serverService.getById(serverId);
            String serverName = server != null ? server.getMonitorSysGenServerName() : "Server-" + serverId;
            broadcastRdpConnectionStatus(serverId, serverName, "disconnected");

        } catch (Exception e) {
            log.error("处理RDP断开连接失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "断开连接失败: " + e.getMessage());
        }
    }

    /**
     * 处理鼠标事件
     */
    private void handleMouseEvent(WebSocketSession session, Map<String, Object> data) {
        try {
            Integer serverId = sessionServerMap.get(session.getId());
            if (serverId == null) {
                sendErrorMessage(session, "会话未关联服务器");
                return;
            }

            SimpleGuacamoleTunnel tunnel = serverTunnelMap.get(serverId);
            if (tunnel == null) {
                sendErrorMessage(session, "Guacamole 隧道不存在");
                return;
            }

            // 构建鼠标事件指令
            Integer x = (Integer) data.get("x");
            Integer y = (Integer) data.get("y");
            Integer button = (Integer) data.get("button");

            if (x != null && y != null) {
                String instruction = String.format("5.mouse,%d,%d,%d;", x, y, button != null ? button : 0);
                try {
                    GuacamoleWriter writer = tunnel.acquireWriter();
                    writer.write(instruction.toCharArray());
                    sendMessage(session, "mouse-result", Map.of("success", true));
                } catch (GuacamoleException e) {
                    log.error("发送鼠标事件失败: {}", e.getMessage());
                    sendErrorMessage(session, "发送鼠标事件失败");
                }
            } else {
                sendErrorMessage(session, "无效的鼠标事件数据");
            }

        } catch (Exception e) {
            log.error("处理RDP鼠标事件失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "鼠标事件处理失败: " + e.getMessage());
        }
    }

    /**
     * 处理键盘事件
     */
    private void handleKeyboardEvent(WebSocketSession session, Map<String, Object> data) {
        try {
            Integer serverId = sessionServerMap.get(session.getId());
            if (serverId == null) {
                sendErrorMessage(session, "会话未关联服务器");
                return;
            }

            SimpleGuacamoleTunnel tunnel = serverTunnelMap.get(serverId);
            if (tunnel == null) {
                sendErrorMessage(session, "Guacamole 隧道不存在");
                return;
            }

            // 构建键盘事件指令
            Integer keysym = (Integer) data.get("keysym");
            Boolean pressed = (Boolean) data.get("pressed");

            if (keysym != null && pressed != null) {
                String instruction = String.format("3.key,%d,%d;", keysym, pressed ? 1 : 0);
                try {
                    GuacamoleWriter writer = tunnel.acquireWriter();
                    writer.write(instruction.toCharArray());
                    sendMessage(session, "keyboard-result", Map.of("success", true));
                } catch (GuacamoleException e) {
                    log.error("发送键盘事件失败: {}", e.getMessage());
                    sendErrorMessage(session, "发送键盘事件失败");
                }
            } else {
                sendErrorMessage(session, "无效的键盘事件数据");
            }

        } catch (Exception e) {
            log.error("处理RDP键盘事件失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "键盘事件处理失败: " + e.getMessage());
        }
    }

    /**
     * 处理屏幕调整
     */
    private void handleScreenResize(WebSocketSession session, Map<String, Object> data) {
        try {
            Integer serverId = sessionServerMap.get(session.getId());
            if (serverId == null) {
                sendErrorMessage(session, "会话未关联服务器");
                return;
            }

            Integer width = (Integer) data.get("width");
            Integer height = (Integer) data.get("height");

            if (width == null || height == null) {
                sendErrorMessage(session, "屏幕尺寸参数不能为空");
                return;
            }

            SimpleGuacamoleTunnel tunnel = serverTunnelMap.get(serverId);
            if (tunnel != null) {
                try {
                    // 发送屏幕尺寸调整指令
                    String instruction = String.format("4.size,%d,%d;", width, height);
                    GuacamoleWriter writer = tunnel.acquireWriter();
                    writer.write(instruction.toCharArray());
                } catch (GuacamoleException e) {
                    log.error("发送屏幕调整指令失败: {}", e.getMessage());
                }
            }

            sendMessage(session, "resize-result", Map.of(
                "success", true,
                "width", width,
                "height", height
            ));
        } catch (Exception e) {
            log.error("处理RDP屏幕调整失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "屏幕调整失败: " + e.getMessage());
        }
    }

    /**
     * 处理剪贴板操作
     */
    private void handleClipboard(WebSocketSession session, Map<String, Object> data) {
        try {
            Integer serverId = sessionServerMap.get(session.getId());
            if (serverId == null) {
                sendErrorMessage(session, "会话未关联服务器");
                return;
            }

            String clipboardData = (String) data.get("data");
            if (clipboardData == null) {
                sendErrorMessage(session, "剪贴板数据不能为空");
                return;
            }

            SimpleGuacamoleTunnel tunnel = serverTunnelMap.get(serverId);
            if (tunnel != null) {
                try {
                    // 发送剪贴板数据指令
                    String instruction = String.format("9.clipboard,%s;", clipboardData);
                    GuacamoleWriter writer = tunnel.acquireWriter();
                    writer.write(instruction.toCharArray());
                } catch (GuacamoleException e) {
                    log.error("发送剪贴板数据失败: {}", e.getMessage());
                }
            }

            sendMessage(session, "clipboard-result", Map.of("success", true));
        } catch (Exception e) {
            log.error("处理RDP剪贴板操作失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "剪贴板操作失败: " + e.getMessage());
        }
    }

    /**
     * 处理屏幕截图
     */
    private void handleScreenshot(WebSocketSession session, Map<String, Object> data) {
        try {
            Integer serverId = sessionServerMap.get(session.getId());
            if (serverId == null) {
                sendErrorMessage(session, "会话未关联服务器");
                return;
            }

            // 简单返回成功，实际截图功能需要更复杂的实现
            sendMessage(session, "screenshot-result", Map.of(
                "success", true,
                "message", "屏幕截图功能暂未实现"
            ));
        } catch (Exception e) {
            log.error("处理RDP屏幕截图失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "屏幕截图失败: " + e.getMessage());
        }
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
        sessions.remove(sessionId);
        Integer serverId = sessionServerMap.remove(sessionId);

        // 断开服务器连接
        if (serverId != null) {
            // 关闭 Guacamole 隧道
            SimpleGuacamoleTunnel tunnel = serverTunnelMap.remove(serverId);
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
    private void sendMessage(WebSocketSession session, String type, Object data) {
        try {
            Map<String, Object> message = Map.of(
                "type", type,
                "data", data,
                "success", true,
                "timestamp", System.currentTimeMillis()
            );
            session.sendMessage(new TextMessage(Json.toJson(message)));
        } catch (IOException e) {
            log.error("发送WebSocket消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送错误消息
     */
    private void sendErrorMessage(WebSocketSession session, String error) {
        sendMessage(session, "error", Map.of("message", error));
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
            GuacamoleClient guacamoleClient = RdpServerConnectionServiceImpl.createGuacamoleClient(server, serverSettingService);
            if (guacamoleClient == null) {
                log.error("创建GuacamoleClient失败: serverId={}", serverId);
                sendErrorMessage(session, "创建Guacamole客户端失败");
                return;
            }

            // 连接到服务器
            guacamoleClient.connect();

            // 创建SimpleGuacamoleTunnel
            SimpleGuacamoleTunnel tunnel = guacamoleClient.createSession().getTunnel();
            if (tunnel != null) {
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
                sendMessage(session, "rdp-data", Map.of("data", data));
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

                    while (tunnel.isOpen()) {
                        // 读取 Guacamole 指令
                        GuacamoleInstruction instruction = reader.readInstruction();
                        if (instruction != null) {
                            String instructionStr = instruction.toString();

                            // 发送数据到WebSocket客户端
                            sendMessage(session, "rdp-data", Map.of("data", instructionStr));

                            // 通过Socket.IO广播RDP数据
                            MonitorSysGenServer server = serverService.getById(serverId);
                            String serverName = server != null ? server.getMonitorSysGenServerName() : "Server-" + serverId;

                            ServerWebSocketMessage message = ServerWebSocketMessage.createRdpDataMessage(
                                serverId, serverName, Map.of("instruction", instructionStr, "type", "guacamole_data")
                            );
                            socketSessionTemplate.send("gen/server", Json.toJson(message));
                        }
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
}
