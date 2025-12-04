package com.chua.starter.monitor.service;

import com.chua.common.support.json.Json;
import com.chua.socketio.support.constant.SocketTopics;
import com.chua.socket.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.entity.SystemSoftContainer;
import com.chua.starter.monitor.message.ServerWebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 系统软件容器WebSocket推送服务
 * 提供容器状态、日志等实时推送功能
 *
 * @author CH
 * @version 4.0.0
 * @since 2024/12/30
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SystemSoftContainerWebSocketService {

    private final SocketSessionTemplate socketSessionTemplate;
    @Lazy
    private final SystemSoftContainerService containerService;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);

    /**
     * 启动WebSocket服务
     */
    public void start() {
        log.info("启动系统软件容器WebSocket推送服务");
        // WebSocket服务启动，定时任务由task包中的类负责
    }

    /**
     * 停止WebSocket服务
     */
    public void stop() {
        log.info("停止系统软件容器WebSocket推送服务");
        scheduledExecutorService.shutdown();
    }

    /**
     * 推送容器状态变更
     *
     * @param containerId 容器ID
     * @param status      容器状态
     */
    public void pushContainerStatus(Integer containerId, String status) {
        try {
            SystemSoftContainer container = containerService.getById(containerId);
            if (container == null) {
                log.warn("容器不存在: containerId={}", containerId);
                return;
            }

            Map<String, Object> statusData = new HashMap<>();
            statusData.put("containerId", containerId);
            statusData.put("containerName", container.getSystemSoftContainerName());
            statusData.put("status", status);
            statusData.put("softId", container.getSystemSoftId());
            statusData.put("serverId", container.getSystemServerId());
            statusData.put("updateTime", System.currentTimeMillis());

            ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                    .messageType("container_status")
                    .serverId(container.getSystemServerId())
                    .dataId(String.valueOf(containerId))
                    .data(statusData)
                    .timestamp(System.currentTimeMillis())
                    .build();

            socketSessionTemplate.send(SocketTopics.Monitor.Docker.CONTAINER_STATUS, Json.toJson(message));
            log.debug("推送容器状态变更: containerId={}, status={}", containerId, status);

        } catch (Exception e) {
            log.error("推送容器状态失败: containerId={}, status={}", containerId, status, e);
        }
    }

    /**
     * 推送容器日志
     *
     * @param containerId 容器ID
     * @param logContent  日志内容
     * @param logType     日志类型 (stdout/stderr)
     */
    public void pushContainerLogs(String containerId, String logContent, String logType) {
        try {
            SystemSoftContainer container = containerService.getById(containerId);
            if (container == null) {
                log.warn("容器不存在: containerId={}", containerId);
                return;
            }

            Map<String, Object> logData = new HashMap<>();
            logData.put("containerId", containerId);
            logData.put("containerName", container.getSystemSoftContainerName());
            logData.put("logContent", logContent);
            logData.put("logType", logType);
            logData.put("timestamp", System.currentTimeMillis());
            logData.put("softId", container.getSystemSoftId());
            logData.put("serverId", container.getSystemServerId());

            ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                    .messageType("container_logs")
                    .serverId(container.getSystemServerId())
                    .dataId(containerId)
                    .data(logData)
                    .timestamp(System.currentTimeMillis())
                    .build();

            socketSessionTemplate.send(SocketTopics.Monitor.Docker.CONTAINER_LOG, Json.toJson(message));
            log.debug("推送容器日志: containerId={}, logType={}, contentLength={}",
                    containerId, logType, logContent.length());

        } catch (Exception e) {
            log.error("推送容器日志失败: containerId={}, logType={}", containerId, logType, e);
        }
    }


    /**
     * 推送容器事件
     *
     * @param containerId  容器ID
     * @param eventType    事件类型 (create/start/stop/restart/delete)
     * @param eventMessage 事件消息
     * @param success      是否成功
     */
    public void pushContainerEvent(Integer containerId, String eventType, String eventMessage, boolean success) {
        try {
            SystemSoftContainer container = containerService.getById(containerId);
            if (container == null) {
                log.warn("容器不存在: containerId={}", containerId);
                return;
            }

            Map<String, Object> eventData = new HashMap<>();
            eventData.put("containerId", containerId);
            eventData.put("containerName", container.getSystemSoftContainerName());
            eventData.put("eventType", eventType);
            eventData.put("eventMessage", eventMessage);
            eventData.put("success", success);
            eventData.put("timestamp", System.currentTimeMillis());
            eventData.put("softId", container.getSystemSoftId());
            eventData.put("serverId", container.getSystemServerId());

            ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                    .messageType("container_event")
                    .serverId(container.getSystemServerId())
                    .dataId(String.valueOf(containerId))
                    .data(eventData)
                    .timestamp(System.currentTimeMillis())
                    .build();

            socketSessionTemplate.send(SocketTopics.Monitor.Docker.CONTAINER_EVENTS, Json.toJson(message));
            log.debug("推送容器事件: containerId={}, eventType={}, success={}",
                    containerId, eventType, success);

        } catch (Exception e) {
            log.error("推送容器事件失败: containerId={}, eventType={}", containerId, eventType, e);
        }
    }


    /**
     * 获取WebSocket主题列表
     *
     * @return 主题列表
     */
    public Map<String, String> getTopics() {
        Map<String, String> topics = new HashMap<>();
        topics.put("containerStatus", SocketTopics.Monitor.Docker.CONTAINER_STATUS);
        topics.put("containerLogs", SocketTopics.Monitor.Docker.CONTAINER_LOG);
        topics.put("containerStatistics", SocketTopics.Monitor.Docker.CONTAINER_STATISTICS);
        topics.put("containerEvents", SocketTopics.Monitor.Docker.CONTAINER_EVENTS);
        return topics;
    }

    /**
     * 检查WebSocket连接状态
     *
     * @return 连接状态信息
     */
    public Map<String, Object> getConnectionStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "SystemSoftContainerWebSocketService");
        status.put("status", "running");
        status.put("topics", getTopics());
        status.put("timestamp", System.currentTimeMillis());
        return status;
    }
}
