package com.chua.starter.monitor.service;

import com.chua.common.support.json.Json;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.entity.SystemSoftContainer;
import com.chua.starter.monitor.message.ServerWebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
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

    /**
     * WebSocket主题常量
     */
    private static final String TOPIC_CONTAINER_STATUS = "container/status";
    private static final String TOPIC_CONTAINER_LOGS = "container/logs";
    private static final String TOPIC_CONTAINER_STATISTICS = "container/statistics";
    private static final String TOPIC_CONTAINER_EVENTS = "container/events";
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
                    .data(statusData)
                    .timestamp(System.currentTimeMillis())
                    .build();

            socketSessionTemplate.send(TOPIC_CONTAINER_STATUS, Json.toJson(message));
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
                    .data(logData)
                    .timestamp(System.currentTimeMillis())
                    .build();

            socketSessionTemplate.send(TOPIC_CONTAINER_LOGS, Json.toJson(message));
            log.debug("推送容器日志: containerId={}, logType={}, contentLength={}",
                    containerId, logType, logContent.length());

        } catch (Exception e) {
            log.error("推送容器日志失败: containerId={}, logType={}", containerId, logType, e);
        }
    }

    /**
     * 推送容器统计信息
     *
     * @param serverId 服务器ID
     */
    public void pushContainerStatistics(Integer serverId) {
        try {
            // 获取服务器下的容器统计信息
            Map<String, Object> queryParams = new HashMap<>();
            queryParams.put("serverId", serverId);
            List<SystemSoftContainer> containers = containerService.listByCondition(queryParams);

            Map<String, Object> statisticsData = new HashMap<>();
            statisticsData.put("serverId", serverId);
            statisticsData.put("totalContainers", containers.size());

            // 按状态统计
            Map<String, Long> statusCount = new HashMap<>();
            containers.forEach(container -> {
                String status = container.getSystemSoftContainerStatus();
                statusCount.put(status, statusCount.getOrDefault(status, 0L) + 1);
            });
            statisticsData.put("statusCount", statusCount);

            // 按软件统计
            Map<String, Long> softCount = new HashMap<>();
            containers.forEach(container -> {
                String softId = String.valueOf(container.getSystemSoftId());
                softCount.put(softId, softCount.getOrDefault(softId, 0L) + 1);
            });
            statisticsData.put("softCount", softCount);

            statisticsData.put("updateTime", System.currentTimeMillis());

            ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                    .messageType("container_statistics")
                    .serverId(serverId)
                    .data(statisticsData)
                    .timestamp(System.currentTimeMillis())
                    .build();

            socketSessionTemplate.send(TOPIC_CONTAINER_STATISTICS, Json.toJson(message));
            log.debug("推送容器统计信息: serverId={}, totalContainers={}", serverId, containers.size());

        } catch (Exception e) {
            log.error("推送容器统计信息失败: serverId={}", serverId, e);
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
                    .data(eventData)
                    .timestamp(System.currentTimeMillis())
                    .build();

            socketSessionTemplate.send(TOPIC_CONTAINER_EVENTS, Json.toJson(message));
            log.debug("推送容器事件: containerId={}, eventType={}, success={}",
                    containerId, eventType, success);

        } catch (Exception e) {
            log.error("推送容器事件失败: containerId={}, eventType={}", containerId, eventType, e);
        }
    }

    /**
     * 推送批量容器状态
     *
     * @param serverId 服务器ID
     */
    public void pushBatchContainerStatus(Integer serverId) {
        try {
            Map<String, Object> queryParams = new HashMap<>();
            queryParams.put("serverId", serverId);
            List<SystemSoftContainer> containers = containerService.listByCondition(queryParams);

            Map<String, Object> batchData = new HashMap<>();
            batchData.put("serverId", serverId);
            batchData.put("containers", containers);
            batchData.put("updateTime", System.currentTimeMillis());

            ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                    .messageType("batch_container_status")
                    .serverId(serverId)
                    .data(batchData)
                    .timestamp(System.currentTimeMillis())
                    .build();

            socketSessionTemplate.send(TOPIC_CONTAINER_STATUS, Json.toJson(message));
            log.debug("推送批量容器状态: serverId={}, containerCount={}", serverId, containers.size());

        } catch (Exception e) {
            log.error("推送批量容器状态失败: serverId={}", serverId, e);
        }
    }

    /**
     * 获取WebSocket主题列表
     *
     * @return 主题列表
     */
    public Map<String, String> getTopics() {
        Map<String, String> topics = new HashMap<>();
        topics.put("containerStatus", TOPIC_CONTAINER_STATUS);
        topics.put("containerLogs", TOPIC_CONTAINER_LOGS);
        topics.put("containerStatistics", TOPIC_CONTAINER_STATISTICS);
        topics.put("containerEvents", TOPIC_CONTAINER_EVENTS);
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