package com.chua.starter.monitor.socket.util;

import com.chua.common.support.json.Json;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;

/**
 * WebSocket娑堟伅宸ュ叿绫?
 *
 * @author CH
 * @since 2024/12/18
 */
@Slf4j
public class WebSocketMessageUtil {

    /**
     * 鍙戦€佹垚鍔熸秷鎭?
     */
    public static void sendSuccess(WebSocketSession session, String type, Object data) {
        sendMessage(session, type, Map.of(
            "success", true,
            "data", data,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * 鍙戦€侀敊璇秷鎭?
     */
    public static void sendError(WebSocketSession session, String type, String error) {
        sendMessage(session, type, Map.of(
            "success", false,
            "error", error,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * 鍙戦€侀€氱敤娑堟伅
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
            log.error("鍙戦€乄ebSocket娑堟伅澶辫触: sessionId={}, type={}, error={}", 
                    session.getId(), type, e.getMessage(), e);
        }
    }

    /**
     * 鍙戦€佷簩杩涘埗鏁版嵁娑堟伅
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
                // 鍏堝彂閫佸厓鏁版嵁
                session.sendMessage(new TextMessage(Json.toJson(message)));
                // 鍐嶅彂閫佷簩杩涘埗鏁版嵁
                session.sendMessage(new org.springframework.web.socket.BinaryMessage(data));
            }
        } catch (IOException e) {
            log.error("鍙戦€乄ebSocket浜岃繘鍒舵秷鎭け璐? sessionId={}, type={}, error={}", 
                    session.getId(), type, e.getMessage(), e);
        }
    }

    /**
     * 鍙戦€佸績璺虫秷鎭?
     */
    public static void sendHeartbeat(WebSocketSession session) {
        sendMessage(session, "heartbeat", Map.of(
            "status", "alive",
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * 鍙戦€佽繛鎺ョ姸鎬佹秷鎭?
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
     * 鍙戦€佹湇鍔″櫒淇℃伅
     */
    public static void sendServerInfo(WebSocketSession session, Integer serverId, Map<String, Object> serverInfo) {
        sendMessage(session, "server-info", Map.of(
            "serverId", serverId,
            "info", serverInfo,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * 鍙戦€佸睆骞曟暟鎹?
     */
    public static void sendScreenData(WebSocketSession session, Integer serverId, String imageData) {
        sendMessage(session, "screen-data", Map.of(
            "serverId", serverId,
            "imageData", imageData,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * 鍙戦€佸睆骞曟洿鏂?
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
     * 鍙戦€侀紶鏍囦簨浠跺搷搴?
     */
    public static void sendMouseEventResponse(WebSocketSession session, boolean success, String message) {
        sendMessage(session, "mouse-event-response", Map.of(
            "success", success,
            "message", message,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * 鍙戦€侀敭鐩樹簨浠跺搷搴?
     */
    public static void sendKeyboardEventResponse(WebSocketSession session, boolean success, String message) {
        sendMessage(session, "keyboard-event-response", Map.of(
            "success", success,
            "message", message,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * 鍙戦€佸壀璐存澘鏁版嵁
     */
    public static void sendClipboardData(WebSocketSession session, String clipboardData) {
        sendMessage(session, "clipboard-data", Map.of(
            "data", clipboardData,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * 鍙戦€佹枃浠朵紶杈撹繘搴?
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
     * 妫€鏌ヤ細璇濇槸鍚︽湁鏁?
     */
    public static boolean isSessionValid(WebSocketSession session) {
        return session != null && session.isOpen();
    }

    /**
     * 瀹夊叏鍏抽棴浼氳瘽
     */
    public static void closeSession(WebSocketSession session, String reason) {
        try {
            if (session != null && session.isOpen()) {
                // 鍙戦€佸叧闂秷鎭?
                sendMessage(session, "session-closing", Map.of(
                    "reason", reason,
                    "timestamp", System.currentTimeMillis()
                ));
                // 鍏抽棴浼氳瘽
                session.close();
            }
        } catch (IOException e) {
            log.error("鍏抽棴WebSocket浼氳瘽澶辫触: sessionId={}, reason={}, error={}", 
                    session.getId(), reason, e.getMessage(), e);
        }
    }
}

