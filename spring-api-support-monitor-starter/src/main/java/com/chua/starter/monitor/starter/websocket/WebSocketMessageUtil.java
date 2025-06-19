package com.chua.starter.monitor.starter.websocket;

import com.chua.common.support.json.Json;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;

/**
 * WebSocket消息工具类
 *
 * @author CH
 * @since 2024/12/18
 */
@Slf4j
public class WebSocketMessageUtil {

    /**
     * 发送成功消息
     */
    public static void sendSuccess(WebSocketSession session, String type, Object data) {
        sendMessage(session, type, Map.of(
            "success", true,
            "data", data,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * 发送错误消息
     */
    public static void sendError(WebSocketSession session, String type, String error) {
        sendMessage(session, type, Map.of(
            "success", false,
            "error", error,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * 发送通用消息
     */
    public static void sendMessage(WebSocketSession session, String type, Object data) {
        try {
            if (session.isOpen()) {
                Map<String, Object> message = Map.of(
                    "type", type,
                    "data", data,
                    "timestamp", System.currentTimeMillis()
                );
                session.sendMessage(new TextMessage(Json.toJson(message)));
            }
        } catch (IOException e) {
            log.error("发送WebSocket消息失败: sessionId={}, type={}, error={}", 
                    session.getId(), type, e.getMessage(), e);
        }
    }

    /**
     * 发送二进制数据消息
     */
    public static void sendBinaryData(WebSocketSession session, String type, byte[] data) {
        try {
            if (session.isOpen()) {
                Map<String, Object> message = Map.of(
                    "type", type,
                    "dataType", "binary",
                    "dataLength", data.length,
                    "timestamp", System.currentTimeMillis()
                );
                // 先发送元数据
                session.sendMessage(new TextMessage(Json.toJson(message)));
                // 再发送二进制数据
                session.sendMessage(new org.springframework.web.socket.BinaryMessage(data));
            }
        } catch (IOException e) {
            log.error("发送WebSocket二进制消息失败: sessionId={}, type={}, error={}", 
                    session.getId(), type, e.getMessage(), e);
        }
    }

    /**
     * 发送心跳消息
     */
    public static void sendHeartbeat(WebSocketSession session) {
        sendMessage(session, "heartbeat", Map.of(
            "status", "alive",
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * 发送连接状态消息
     */
    public static void sendConnectionStatus(WebSocketSession session, Integer serverId, boolean connected, String message) {
        sendMessage(session, "connection-status", Map.of(
            "serverId", serverId,
            "connected", connected,
            "message", message,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * 发送服务器信息
     */
    public static void sendServerInfo(WebSocketSession session, Integer serverId, Map<String, Object> serverInfo) {
        sendMessage(session, "server-info", Map.of(
            "serverId", serverId,
            "info", serverInfo,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * 发送屏幕数据
     */
    public static void sendScreenData(WebSocketSession session, Integer serverId, String imageData) {
        sendMessage(session, "screen-data", Map.of(
            "serverId", serverId,
            "imageData", imageData,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * 发送屏幕更新
     */
    public static void sendScreenUpdate(WebSocketSession session, Integer serverId, 
                                       int x, int y, int width, int height, String imageData) {
        sendMessage(session, "screen-update", Map.of(
            "serverId", serverId,
            "x", x,
            "y", y,
            "width", width,
            "height", height,
            "imageData", imageData,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * 发送鼠标事件响应
     */
    public static void sendMouseEventResponse(WebSocketSession session, boolean success, String message) {
        sendMessage(session, "mouse-event-response", Map.of(
            "success", success,
            "message", message,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * 发送键盘事件响应
     */
    public static void sendKeyboardEventResponse(WebSocketSession session, boolean success, String message) {
        sendMessage(session, "keyboard-event-response", Map.of(
            "success", success,
            "message", message,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * 发送剪贴板数据
     */
    public static void sendClipboardData(WebSocketSession session, String clipboardData) {
        sendMessage(session, "clipboard-data", Map.of(
            "data", clipboardData,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * 发送文件传输进度
     */
    public static void sendFileTransferProgress(WebSocketSession session, String transferId, 
                                              long transferred, long total, boolean completed) {
        sendMessage(session, "file-transfer-progress", Map.of(
            "transferId", transferId,
            "transferred", transferred,
            "total", total,
            "progress", total > 0 ? (double) transferred / total * 100 : 0,
            "completed", completed,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * 检查会话是否有效
     */
    public static boolean isSessionValid(WebSocketSession session) {
        return session != null && session.isOpen();
    }

    /**
     * 安全关闭会话
     */
    public static void closeSession(WebSocketSession session, String reason) {
        try {
            if (session != null && session.isOpen()) {
                // 发送关闭消息
                sendMessage(session, "session-closing", Map.of(
                    "reason", reason,
                    "timestamp", System.currentTimeMillis()
                ));
                // 关闭会话
                session.close();
            }
        } catch (IOException e) {
            log.error("关闭WebSocket会话失败: sessionId={}, reason={}, error={}", 
                    session.getId(), reason, e.getMessage(), e);
        }
    }
}
