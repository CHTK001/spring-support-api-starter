package com.chua.starter.monitor.socket.listener.connection;

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
import com.chua.starter.monitor.service.impl.RemoteDesktopServerConnectionServiceImpl;
import com.chua.starter.monitor.service.server.MonitorSysGenServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 远程桌面 Socket 监听器
 * <p>
 * 处理基于图片传输的远程桌面连接，支持：
 * - 自动检测服务器模式（桌面/SSH）
 * - 屏幕图片实时传输（二进制）
 * - 鼠标和键盘事件传输
 * - 画质模式和压缩器配置
 *
 * @author CH
 * @since 2024/12/28
 */
@Component
@Slf4j
public class RemoteDesktopSocketListener implements SocketListener {

    @Autowired
    @Lazy
    private RemoteDesktopServerConnectionServiceImpl remoteDesktopService;

    @Autowired
    @Lazy
    private SocketSessionTemplate socketSessionTemplate;

    @Autowired
    @Lazy
    private MonitorSysGenServerService serverService;

    // 存储会话与服务器的映射关系
    private final Map<String, Integer> sessionServerMap = new ConcurrentHashMap<>();
    private final Map<Integer, SocketSession> serverSessionMap = new ConcurrentHashMap<>();

    /**
     * 远程桌面连接事件
     */
    @OnEvent(MonitorSocketTopics.Remote.Desktop.CONNECT)
    public void onConnect(SocketSession session, String rawData) {
        try {
            JsonObject data = Json.getJsonObject(rawData);
            Integer serverId = data.getInteger("serverId");
            
            if (serverId == null) {
                sendError(session, "服务器ID不能为空");
                return;
            }

            MonitorSysGenServer server = serverService.getById(String.valueOf(serverId));
            if (server == null) {
                sendError(session, "服务器配置不存在");
                return;
            }

            // 建立远程桌面连接
            boolean connected = remoteDesktopService.connect(server);
            if (connected) {
                sessionServerMap.put(session.getId(), serverId);
                serverSessionMap.put(serverId, session);

                String mode = remoteDesktopService.getConnectionMode(serverId);

                // 设置数据回调
                setupDataCallback(serverId, session);

                // 发送连接成功消息
                ServerWebSocketMessage response = ServerWebSocketMessage.builder()
                        .messageType("remote_desktop_connect")
                        .serverId(serverId)
                        .serverName(server.getMonitorSysGenServerName())
                        .serverHost(server.getMonitorSysGenServerHost())
                        .serverPort(server.getMonitorSysGenServerPort())
                        .serverProtocol("REMOTE")
                        .data(Map.of("mode", mode, "success", true))
                        .timestamp(System.currentTimeMillis())
                        .build();

                socketSessionTemplate.send(MonitorSocketTopics.Remote.Desktop.CONNECTED, Json.toJson(response));
                log.info("远程桌面连接成功: serverId={}, mode={}", serverId, mode);

                // 如果是 SSH 模式，通知前端
                if ("SSH".equals(mode)) {
                    ServerWebSocketMessage modeMsg = ServerWebSocketMessage.builder()
                            .messageType("mode_detected")
                            .serverId(serverId)
                            .data(Map.of("mode", "SSH"))
                            .timestamp(System.currentTimeMillis())
                            .build();
                    session.send(MonitorSocketTopics.Remote.Desktop.MODE_DETECTED, Json.toJson(modeMsg));
                }
            } else {
                sendError(session, "远程桌面连接失败");
            }
        } catch (Exception e) {
            log.error("远程桌面连接失败: {}", e.getMessage(), e);
            sendError(session, "连接失败: " + e.getMessage());
        }
    }

    /**
     * 鼠标移动事件
     */
    @OnEvent(MonitorSocketTopics.Remote.Desktop.MOUSE_MOVE)
    public void onMouseMove(SocketSession session, String rawData) {
        try {
            Integer serverId = sessionServerMap.get(session.getId());
            if (serverId == null) return;

            JsonObject data = Json.getJsonObject(rawData);
            int x = data.getIntValue("x", 0);
            int y = data.getIntValue("y", 0);
            remoteDesktopService.sendMouseMove(serverId, x, y);
        } catch (Exception e) {
            log.error("处理鼠标移动失败: {}", e.getMessage());
        }
    }

    /**
     * 鼠标点击事件
     */
    @OnEvent(MonitorSocketTopics.Remote.Desktop.MOUSE_CLICK)
    public void onMouseClick(SocketSession session, String rawData) {
        try {
            Integer serverId = sessionServerMap.get(session.getId());
            if (serverId == null) return;

            JsonObject data = Json.getJsonObject(rawData);
            int x = data.getIntValue("x", 0);
            int y = data.getIntValue("y", 0);
            int button = data.getIntValue("button", 1);
            boolean pressed = data.getBooleanValue("pressed", false);
            remoteDesktopService.sendMouseClick(serverId, x, y, button, pressed);
        } catch (Exception e) {
            log.error("处理鼠标点击失败: {}", e.getMessage());
        }
    }

    /**
     * 鼠标滚轮事件
     */
    @OnEvent(MonitorSocketTopics.Remote.Desktop.MOUSE_WHEEL)
    public void onMouseWheel(SocketSession session, String rawData) {
        try {
            Integer serverId = sessionServerMap.get(session.getId());
            if (serverId == null) return;

            JsonObject data = Json.getJsonObject(rawData);
            int x = data.getIntValue("x", 0);
            int y = data.getIntValue("y", 0);
            int amount = data.getIntValue("amount", 0);
            remoteDesktopService.sendMouseWheel(serverId, x, y, amount);
        } catch (Exception e) {
            log.error("处理鼠标滚轮失败: {}", e.getMessage());
        }
    }

    /**
     * 键盘按下事件
     */
    @OnEvent(MonitorSocketTopics.Remote.Desktop.KEY_PRESS)
    public void onKeyPress(SocketSession session, String rawData) {
        try {
            Integer serverId = sessionServerMap.get(session.getId());
            if (serverId == null) return;

            JsonObject data = Json.getJsonObject(rawData);
            int keyCode = data.getIntValue("keyCode", 0);
            String keyCharStr = data.getString("keyChar");
            char keyChar = (keyCharStr != null && !keyCharStr.isEmpty()) ? keyCharStr.charAt(0) : '\0';
            int modifiers = data.getIntValue("modifiers", 0);
            remoteDesktopService.sendKeyPress(serverId, keyCode, keyChar, modifiers);
        } catch (Exception e) {
            log.error("处理键盘按下失败: {}", e.getMessage());
        }
    }

    /**
     * 键盘释放事件
     */
    @OnEvent(MonitorSocketTopics.Remote.Desktop.KEY_RELEASE)
    public void onKeyRelease(SocketSession session, String rawData) {
        try {
            Integer serverId = sessionServerMap.get(session.getId());
            if (serverId == null) return;

            JsonObject data = Json.getJsonObject(rawData);
            int keyCode = data.getIntValue("keyCode", 0);
            String keyCharStr = data.getString("keyChar");
            char keyChar = (keyCharStr != null && !keyCharStr.isEmpty()) ? keyCharStr.charAt(0) : '\0';
            int modifiers = data.getIntValue("modifiers", 0);
            remoteDesktopService.sendKeyRelease(serverId, keyCode, keyChar, modifiers);
        } catch (Exception e) {
            log.error("处理键盘释放失败: {}", e.getMessage());
        }
    }

    /**
     * 设置画质模式
     */
    @OnEvent(MonitorSocketTopics.Remote.Desktop.SET_QUALITY)
    public void onSetQuality(SocketSession session, String rawData) {
        try {
            Integer serverId = sessionServerMap.get(session.getId());
            if (serverId == null) return;

            JsonObject data = Json.getJsonObject(rawData);
            String mode = data.getString("mode");
            if (mode != null) {
                remoteDesktopService.setQualityMode(serverId, mode);
                log.info("设置画质模式: serverId={}, mode={}", serverId, mode);
            }
        } catch (Exception e) {
            log.error("设置画质模式失败: {}", e.getMessage());
        }
    }

    /**
     * 设置压缩器
     */
    @OnEvent(MonitorSocketTopics.Remote.Desktop.SET_COMPRESSOR)
    public void onSetCompressor(SocketSession session, String rawData) {
        try {
            Integer serverId = sessionServerMap.get(session.getId());
            if (serverId == null) return;

            JsonObject data = Json.getJsonObject(rawData);
            String compressor = data.getString("compressor");
            if (compressor != null) {
                remoteDesktopService.setCompressor(serverId, compressor);
                log.info("设置压缩器: serverId={}, compressor={}", serverId, compressor);
            }
        } catch (Exception e) {
            log.error("设置压缩器失败: {}", e.getMessage());
        }
    }

    /**
     * 重置捕获（强制发送全屏）
     */
    @OnEvent(MonitorSocketTopics.Remote.Desktop.CAPTURE_RESET)
    public void onCaptureReset(SocketSession session, String rawData) {
        try {
            Integer serverId = sessionServerMap.get(session.getId());
            if (serverId != null) {
                remoteDesktopService.requestCaptureReset(serverId);
                log.info("请求重置捕获: serverId={}", serverId);
            }
        } catch (Exception e) {
            log.error("重置捕获失败: {}", e.getMessage());
        }
    }

    /**
     * 断开连接事件
     */
    @OnEvent(MonitorSocketTopics.Remote.Desktop.DISCONNECT)
    public void onDisconnect(SocketSession session, String rawData) {
        try {
            JsonObject data = Json.getJsonObject(rawData);
            Integer serverId = data != null ? data.getInteger("serverId") : null;
            if (serverId == null) {
                serverId = sessionServerMap.get(session.getId());
            }

            if (serverId != null) {
                remoteDesktopService.disconnect(serverId);
                sessionServerMap.remove(session.getId());
                serverSessionMap.remove(serverId);

                ServerWebSocketMessage response = ServerWebSocketMessage.builder()
                        .messageType("remote_desktop_disconnect")
                        .serverId(serverId)
                        .data(Map.of("reason", "用户主动断开"))
                        .timestamp(System.currentTimeMillis())
                        .build();
                socketSessionTemplate.send(MonitorSocketTopics.Remote.Desktop.DISCONNECTED, Json.toJson(response));

                log.info("远程桌面连接已断开: serverId={}", serverId);
            }
        } catch (Exception e) {
            log.error("断开远程桌面连接失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 设置数据回调（屏幕图片以二进制格式发送）
     */
    private void setupDataCallback(Integer serverId, SocketSession session) {
        // 使用二进制回调，直接发送原始图像字节
        remoteDesktopService.setBinaryDataCallback(serverId, (imageBytes) -> {
            try {
                // 直接发送二进制图像数据
                session.sendBinary(MonitorSocketTopics.Remote.Desktop.SCREEN, imageBytes);
            } catch (Exception e) {
                log.error("发送屏幕数据失败: {}", e.getMessage());
            }
        });
    }

    /**
     * 发送错误消息
     */
    private void sendError(SocketSession session, String message) {
        try {
            session.send(MonitorSocketTopics.Remote.Desktop.ERROR, ReturnResult.illegal(message));
        } catch (Exception e) {
            log.error("发送错误消息失败: {}", e.getMessage());
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
                remoteDesktopService.disconnect(serverId);
                sessionServerMap.remove(sessionId);
                serverSessionMap.remove(serverId);
                log.info("会话断开，已清理远程桌面连接: sessionId={}, serverId={}", sessionId, serverId);
            }
        } catch (Exception e) {
            log.error("会话断开清理失败: {}", e.getMessage(), e);
        }
    }
}
