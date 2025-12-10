package com.chua.starter.monitor.service.impl;

import com.chua.common.support.json.Json;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.monitor.service.file.FileSystemSseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 文件系统SSE服务实现
 * 提供文件系统相关的Server-Sent Events服务
 *
 * @author CH
 * @since 2024/12/13
 */
@Slf4j
@Service
public class FileSystemSseServiceImpl implements FileSystemSseService {

    /**
     * SSE连接超时时间（毫秒）
     */
    private static final long SSE_TIMEOUT = 30 * 60 * 1000L; // 30分钟

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");
    /**
     * 心跳检测间隔（秒）
     */
    private static final long HEARTBEAT_INTERVAL = 30;

    /**
     * 客户端连接信息
     */
    private static class ClientConnection {
        private final SseEmitter emitter;
        private final Set<String> subscribedEvents;
        private LocalDateTime lastHeartbeat;
        private final LocalDateTime createTime;

        public ClientConnection(SseEmitter emitter) {
            this.emitter = emitter;
            this.subscribedEvents = ConcurrentHashMap.newKeySet();
            this.lastHeartbeat = LocalDateTime.now();
            this.createTime = LocalDateTime.now();
        }

        // Getters and setters
        public SseEmitter getEmitter() { return emitter; }
        public Set<String> getSubscribedEvents() { return subscribedEvents; }
        public LocalDateTime getLastHeartbeat() { return lastHeartbeat; }
        public void setLastHeartbeat(LocalDateTime lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; }
        public LocalDateTime getCreateTime() { return createTime; }
    }

    /**
     * 客户端连接映射
     */
    private final Map<String, ClientConnection> connections = new ConcurrentHashMap<>();

    /**
     * 定时任务执行器
     */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    /**
     * 文件系统事件类型常量
     */
    public static class EventType {
        public static final String UPLOAD_PROGRESS = "upload_progress";
        public static final String UPLOAD_STATUS = "upload_status";
        public static final String UPLOAD_COMPLETED = "upload_completed";
        public static final String UPLOAD_FAILED = "upload_failed";
        public static final String MERGE_PROGRESS = "merge_progress";
        public static final String MERGE_COMPLETED = "merge_completed";
        public static final String MERGE_FAILED = "merge_failed";
        public static final String SYSTEM_STATUS = "system_status";
        public static final String QUEUE_STATUS = "queue_status";
        public static final String STATISTICS_UPDATE = "statistics_update";
        public static final String FILE_CREATED = "file_created";
        public static final String FILE_DELETED = "file_deleted";
        public static final String FILE_UPDATED = "file_updated";
        public static final String ERROR = "error";
        public static final String HEARTBEAT = "heartbeat";
        public static final String TEST = "test";
    }

    public FileSystemSseServiceImpl() {
        // 启动心跳检测任务
        startHeartbeatTask();
        // 启动连接清理任务
        startConnectionCleanupTask();
    }

    @Override
    public SseEmitter createConnection(String clientId) {
        // 如果clientId为空，生成一个随机ID
        if (StringUtils.isEmpty(clientId)) {
            clientId = "client_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
        }

        // 如果已存在连接，先关闭旧连接
        if (connections.containsKey(clientId)) {
            disconnect(clientId);
        }

        // 创建新的SSE连接
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        ClientConnection connection = new ClientConnection(emitter);

        // 设置连接事件处理
        String finalClientId = clientId;
        emitter.onCompletion(() -> {
            log.info("文件系统SSE连接完成: clientId={}", finalClientId);
            connections.remove(finalClientId);
        });

        emitter.onTimeout(() -> {
            log.info("文件系统SSE连接超时: clientId={}", finalClientId);
            connections.remove(finalClientId);
        });

        emitter.onError((throwable) -> {
            log.error("文件系统SSE连接错误: clientId={}", finalClientId, throwable);
            connections.remove(finalClientId);
        });

        // 保存连接
        connections.put(clientId, connection);

        // 发送连接成功消息
        try {
            Map<String, Object> connectMessage = new HashMap<>();
            connectMessage.put("clientId", clientId);
            connectMessage.put("timestamp", System.currentTimeMillis());
            connectMessage.put("message", "连接成功");

            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(Json.toJson(connectMessage)));

            log.info("文件系统SSE连接创建成功: clientId={}", clientId);
        } catch (IOException e) {
            log.error("发送连接成功消息失败: clientId={}", clientId, e);
            connections.remove(clientId);
            throw new RuntimeException("发送连接消息失败", e);
        }

        return emitter;
    }

    @Override
    public void subscribe(String clientId, String eventTypes) {
        ClientConnection connection = connections.get(clientId);
        if (connection == null) {
            throw new IllegalArgumentException("客户端连接不存在: " + clientId);
        }

        String[] types = eventTypes.split(",");
        for (String type : types) {
            String trimmedType = type.trim();
            if (StringUtils.isNotEmpty(trimmedType)) {
                connection.getSubscribedEvents().add(trimmedType);
                log.debug("客户端订阅事件: clientId={}, eventType={}", clientId, trimmedType);
            }
        }
    }

    @Override
    public void unsubscribe(String clientId, String eventTypes) {
        ClientConnection connection = connections.get(clientId);
        if (connection == null) {
            throw new IllegalArgumentException("客户端连接不存在: " + clientId);
        }

        String[] types = eventTypes.split(",");
        for (String type : types) {
            String trimmedType = type.trim();
            if (StringUtils.isNotEmpty(trimmedType)) {
                connection.getSubscribedEvents().remove(trimmedType);
                log.debug("客户端取消订阅事件: clientId={}, eventType={}", clientId, trimmedType);
            }
        }
    }

    @Override
    public void disconnect(String clientId) {
        ClientConnection connection = connections.remove(clientId);
        if (connection != null) {
            try {
                connection.getEmitter().complete();
                log.info("文件系统SSE连接断开: clientId={}", clientId);
            } catch (Exception e) {
                log.error("断开文件系统SSE连接失败: clientId={}", clientId, e);
            }
        }
    }

    @Override
    public Map<String, Object> getConnectionStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("totalConnections", connections.size());
        status.put("timestamp", System.currentTimeMillis());

        List<Map<String, Object>> clientList = new ArrayList<>();
        for (Map.Entry<String, ClientConnection> entry : connections.entrySet()) {
            Map<String, Object> clientInfo = new HashMap<>();
            ClientConnection connection = entry.getValue();
            clientInfo.put("clientId", entry.getKey());
            clientInfo.put("subscribedEvents", new ArrayList<>(connection.getSubscribedEvents()));
            clientInfo.put("lastHeartbeat", connection.getLastHeartbeat().toString());
            clientInfo.put("createTime", connection.getCreateTime().toString());
            clientList.add(clientInfo);
        }
        status.put("clients", clientList);

        return status;
    }

    @Override
    public void sendTestMessage(String clientId, Map<String, Object> message) {
        Map<String, Object> testMessage = new HashMap<>();
        testMessage.put("type", "test");
        testMessage.put("timestamp", System.currentTimeMillis());
        testMessage.put("data", message);

        if (StringUtils.isNotEmpty(clientId)) {
            sendMessageToClient(clientId, EventType.TEST, testMessage);
        } else {
            broadcastMessage(EventType.TEST, testMessage);
        }
    }

    @Override
    public void updateHeartbeat(String clientId) {
        ClientConnection connection = connections.get(clientId);
        if (connection != null) {
            connection.setLastHeartbeat(LocalDateTime.now());
            log.debug("更新客户端心跳: clientId={}", clientId);
        }
    }

    @Override
    public void sendUploadProgress(Integer fileId, Double progress, String status) {
        Map<String, Object> data = new HashMap<>();
        data.put("fileId", fileId);
        data.put("progress", DECIMAL_FORMAT.format(progress));
        data.put("status", status);
        data.put("timestamp", System.currentTimeMillis());

        broadcastToSubscribers(EventType.UPLOAD_PROGRESS, data);
    }

    @Override
    public void sendMergeProgress(Integer fileId, Double progress, String status) {
        Map<String, Object> data = new HashMap<>();
        data.put("fileId", fileId);
        data.put("progress", DECIMAL_FORMAT.format(progress));
        data.put("status", status);
        data.put("timestamp", System.currentTimeMillis());

        broadcastToSubscribers(EventType.MERGE_PROGRESS, data);
    }

    @Override
    public void sendFileOperationResult(String operation, Integer fileId, Boolean success, String message) {
        Map<String, Object> data = new HashMap<>();
        data.put("operation", operation);
        data.put("fileId", fileId);
        data.put("success", success);
        data.put("message", message);
        data.put("timestamp", System.currentTimeMillis());

        String eventType = success ? operation + "_completed" : operation + "_failed";
        broadcastToSubscribers(eventType, data);
    }

    @Override
    public void sendSystemStatusUpdate(Map<String, Object> status) {
        Map<String, Object> data = new HashMap<>(status);
        data.put("timestamp", System.currentTimeMillis());
        broadcastToSubscribers(EventType.SYSTEM_STATUS, data);
    }

    @Override
    public void sendQueueStatusUpdate(Map<String, Object> queueStatus) {
        Map<String, Object> data = new HashMap<>(queueStatus);
        data.put("timestamp", System.currentTimeMillis());
        broadcastToSubscribers(EventType.QUEUE_STATUS, data);
    }

    @Override
    public void sendStatisticsUpdate(Map<String, Object> statistics) {
        Map<String, Object> data = new HashMap<>(statistics);
        data.put("timestamp", System.currentTimeMillis());
        broadcastToSubscribers(EventType.STATISTICS_UPDATE, data);
    }

    @Override
    public void sendFileCreated(Map<String, Object> fileInfo) {
        Map<String, Object> data = new HashMap<>(fileInfo);
        data.put("timestamp", System.currentTimeMillis());
        broadcastToSubscribers(EventType.FILE_CREATED, data);
    }

    @Override
    public void sendFileDeleted(Integer fileId, String fileName) {
        Map<String, Object> data = new HashMap<>();
        data.put("fileId", fileId);
        data.put("fileName", fileName);
        data.put("timestamp", System.currentTimeMillis());
        broadcastToSubscribers(EventType.FILE_DELETED, data);
    }

    @Override
    public void sendFileUpdated(Map<String, Object> fileInfo) {
        Map<String, Object> data = new HashMap<>(fileInfo);
        data.put("timestamp", System.currentTimeMillis());
        broadcastToSubscribers(EventType.FILE_UPDATED, data);
    }

    @Override
    public void sendError(String error) {
        Map<String, Object> data = new HashMap<>();
        data.put("error", error);
        data.put("timestamp", System.currentTimeMillis());
        broadcastMessage(EventType.ERROR, data);
    }

    @Override
    public void sendCustomMessage(String eventType, Object data) {
        broadcastMessage(eventType, data);
    }

    @Override
    public void sendMessageToClient(String clientId, String eventType, Object data) {
        ClientConnection connection = connections.get(clientId);
        if (connection == null) {
            log.warn("客户端连接不存在，无法发送消息: clientId={}, eventType={}", clientId, eventType);
            return;
        }

        try {
            connection.getEmitter().send(SseEmitter.event()
                    .name(eventType)
                    .data(Json.toJson(data)));
            log.debug("发送SSE消息到客户端: clientId={}, eventType={}", clientId, eventType);
        } catch (IOException e) {
            log.error("发送SSE消息失败: clientId={}, eventType={}", clientId, eventType, e);
            // 连接异常，移除连接
            connections.remove(clientId);
        }
    }

    @Override
    public void broadcastMessage(String eventType, Object data) {
        List<String> failedClients = new ArrayList<>();

        for (Map.Entry<String, ClientConnection> entry : connections.entrySet()) {
            try {
                entry.getValue().getEmitter().send(SseEmitter.event()
                        .name(eventType)
                        .data(Json.toJson(data)));
            } catch (IOException e) {
                log.error("广播SSE消息失败: clientId={}, eventType={}", entry.getKey(), eventType, e);
                failedClients.add(entry.getKey());
            }
        }

        // 移除失败的连接
        for (String clientId : failedClients) {
            connections.remove(clientId);
        }

        log.debug("广播SSE消息: eventType={}, 成功={}, 失败={}", 
                eventType, connections.size() - failedClients.size(), failedClients.size());
    }

    @Override
    public void broadcastToSubscribers(String eventType, Object data) {
        List<String> failedClients = new ArrayList<>();

        for (Map.Entry<String, ClientConnection> entry : connections.entrySet()) {
            ClientConnection connection = entry.getValue();
            
            // 检查客户端是否订阅了此事件类型
            if (connection.getSubscribedEvents().isEmpty() || 
                connection.getSubscribedEvents().contains(eventType)) {
                
                try {
                    connection.getEmitter().send(SseEmitter.event()
                            .name(eventType)
                            .data(Json.toJson(data)));
                } catch (IOException e) {
                    log.error("发送SSE消息到订阅者失败: clientId={}, eventType={}", entry.getKey(), eventType, e);
                    failedClients.add(entry.getKey());
                }
            }
        }

        // 移除失败的连接
        for (String clientId : failedClients) {
            connections.remove(clientId);
        }

        log.debug("发送SSE消息到订阅者: eventType={}, 订阅者数量={}, 失败={}", 
                eventType, connections.size() - failedClients.size(), failedClients.size());
    }

    /**
     * 启动心跳检测任务
     */
    private void startHeartbeatTask() {
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                Map<String, Object> heartbeatData = new HashMap<>();
                heartbeatData.put("timestamp", System.currentTimeMillis());
                heartbeatData.put("message", "heartbeat");

                broadcastMessage(EventType.HEARTBEAT, heartbeatData);
                log.debug("发送心跳消息到所有客户端");
            } catch (Exception e) {
                log.error("发送心跳消息失败", e);
            }
        }, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
    }

    /**
     * 启动连接清理任务
     */
    private void startConnectionCleanupTask() {
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                LocalDateTime now = LocalDateTime.now();
                List<String> expiredClients = new ArrayList<>();

                for (Map.Entry<String, ClientConnection> entry : connections.entrySet()) {
                    ClientConnection connection = entry.getValue();
                    // 检查心跳超时（5分钟无心跳则认为连接失效）
                    if (connection.getLastHeartbeat().plusMinutes(5).isBefore(now)) {
                        expiredClients.add(entry.getKey());
                    }
                }

                // 清理过期连接
                for (String clientId : expiredClients) {
                    log.info("清理过期的SSE连接: clientId={}", clientId);
                    disconnect(clientId);
                }

                if (!expiredClients.isEmpty()) {
                    log.info("清理了 {} 个过期的SSE连接", expiredClients.size());
                }
            } catch (Exception e) {
                log.error("清理过期连接失败", e);
            }
        }, 60, 60, TimeUnit.SECONDS); // 每分钟检查一次
    }

    @PreDestroy
    public void destroy() {
        log.info("关闭文件系统SSE服务");
        
        // 关闭所有连接
        for (String clientId : new ArrayList<>(connections.keySet())) {
            disconnect(clientId);
        }
        
        // 关闭定时任务
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
