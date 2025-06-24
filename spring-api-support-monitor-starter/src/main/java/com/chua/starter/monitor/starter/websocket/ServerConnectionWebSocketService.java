package com.chua.starter.monitor.starter.websocket;

import com.chua.common.support.json.Json;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerConnectionStatus;
import com.chua.starter.monitor.starter.entity.ServerWebSocketMessage;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerConnectionStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 服务器连接状态WebSocket服务
 *
 * @author CH
 * @since 2024/12/20
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ServerConnectionWebSocketService {

    private final SocketSessionTemplate socketSessionTemplate;
    // WebSocket主题常量 - 统一使用gen/server主题
    private static final String TOPIC_CONNECTION_STATUS = "gen/server";
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);
    private static final String TOPIC_CONNECTION_STATISTICS = "gen/server";
    private static final String TOPIC_CONNECTION_HEALTH = "gen/server";
    private static final String TOPIC_CONNECTION_ALERT = "gen/server";
    @Lazy
    private final MonitorSysGenServerConnectionStatusService connectionStatusService;

    /**
     * 启动WebSocket服务
     */
    public void start() {
        log.info("启动服务器连接状态WebSocket服务");
        
        // 定时推送连接状态统计信息 (每30秒)
        scheduledExecutorService.scheduleAtFixedRate(this::broadcastConnectionStatistics, 0, 30, TimeUnit.SECONDS);
        
        // 定时检查连接健康状态 (每5分钟)
        scheduledExecutorService.scheduleAtFixedRate(this::broadcastHealthStatus, 60, 300, TimeUnit.SECONDS);
        
        // 定时检查连接告警 (每分钟)
        scheduledExecutorService.scheduleAtFixedRate(this::checkConnectionAlerts, 30, 60, TimeUnit.SECONDS);
    }

    /**
     * 停止WebSocket服务
     */
    public void stop() {
        log.info("停止服务器连接状态WebSocket服务");
        scheduledExecutorService.shutdown();
    }

    /**
     * 广播单个服务器连接状态变化
     *
     * @param serverId         服务器ID
     * @param connectionStatus 连接状态
     * @param message          状态消息
     * @param responseTime     响应时间
     */
    public void broadcastServerConnectionStatus(Integer serverId, Integer connectionStatus, String message, Long responseTime) {
        try {
            // 获取服务器信息
            String serverName = "Server-" + serverId; // 简化处理，实际应该从数据库获取
            String statusDesc = getConnectionStatusDesc(connectionStatus);

            ServerWebSocketMessage wsMessage = ServerWebSocketMessage.createConnectionStatusMessage(
                    serverId,
                    serverName,
                    null, // host
                    null, // port
                    null, // protocol
                    connectionStatus,
                    statusDesc,
                    message,
                    responseTime
            );

            // 统一发送到gen/server主题
            String topic = "gen/server";
            socketSessionTemplate.send(topic, Json.toJson(wsMessage));

            log.debug("广播服务器连接状态: serverId={}, status={}", serverId, connectionStatus);

        } catch (Exception e) {
            log.error("广播服务器连接状态失败: serverId={}", serverId, e);
        }
    }

    /**
     * 广播批量连接测试结果
     *
     * @param results 测试结果列表
     */
    public void broadcastBatchTestResults(List<MonitorSysGenServerConnectionStatus> results) {
        try {
            ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                    .messageType("batch_test_results")
                    .data(results)
                    .timestamp(System.currentTimeMillis())
                    .build();

            socketSessionTemplate.send("gen/server", Json.toJson(message));

            log.debug("广播批量连接测试结果: count={}", results.size());

        } catch (Exception e) {
            log.error("广播批量连接测试结果失败", e);
        }
    }

    /**
     * 广播连接状态统计信息
     */
    public void broadcastConnectionStatistics() {
        try {
            var statisticsResult = connectionStatusService.getConnectionStatusStatistics();
            if (statisticsResult != null && statisticsResult.getData() != null) {
                ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                        .messageType("connection_statistics")
                        .data(statisticsResult.getData())
                        .timestamp(System.currentTimeMillis())
                        .build();

                socketSessionTemplate.send("gen/server", Json.toJson(message));

                log.debug("广播连接状态统计信息");
            }
        } catch (Exception e) {
            log.error("广播连接状态统计信息失败", e);
        }
    }

    /**
     * 广播健康状态报告
     */
    public void broadcastHealthStatus() {
        try {
            var healthResult = connectionStatusService.getServerConnectionHealthReport();
            if (healthResult != null && healthResult.getData() != null) {
                ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                        .messageType("health_report")
                        .data(healthResult.getData())
                        .timestamp(System.currentTimeMillis())
                        .build();

                socketSessionTemplate.send("gen/server", Json.toJson(message));

                log.debug("广播健康状态报告");
            }
        } catch (Exception e) {
            log.error("广播健康状态报告失败", e);
        }
    }

    /**
     * 检查连接告警
     */
    public void checkConnectionAlerts() {
        try {
            // 检查长时间未连接的服务器
            var longTimeNoConnectResult = connectionStatusService.getLongTimeNoConnectServers(30);
            if (longTimeNoConnectResult != null && longTimeNoConnectResult.getData() != null && !longTimeNoConnectResult.getData().isEmpty()) {
                Map<String, Object> alertData = new HashMap<>();
                alertData.put("type", "long_time_no_connect");
                alertData.put("level", "warning");
                alertData.put("message", "发现长时间未连接的服务器");
                alertData.put("servers", longTimeNoConnectResult.getData());

                ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                        .messageType("connection_alert")
                        .data(alertData)
                        .timestamp(System.currentTimeMillis())
                        .build();

                socketSessionTemplate.send("gen/server", Json.toJson(message));

                log.warn("发现长时间未连接的服务器: count={}", longTimeNoConnectResult.getData().size());
            }

            // 检查连接失败的服务器
            var failedServersResult = connectionStatusService.getServersByConnectionStatus(
                    MonitorSysGenServerConnectionStatus.ConnectionStatus.FAILED.getCode());
            if (failedServersResult != null && failedServersResult.getData() != null && !failedServersResult.getData().isEmpty()) {
                Map<String, Object> alertData = new HashMap<>();
                alertData.put("type", "connection_failed");
                alertData.put("level", "error");
                alertData.put("message", "发现连接失败的服务器");
                alertData.put("servers", failedServersResult.getData());

                ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                        .messageType("connection_alert")
                        .data(alertData)
                        .timestamp(System.currentTimeMillis())
                        .build();

                socketSessionTemplate.send("gen/server", Json.toJson(message));

                log.warn("发现连接失败的服务器: count={}", failedServersResult.getData().size());
            }

        } catch (Exception e) {
            log.error("检查连接告警失败", e);
        }
    }

    /**
     * 获取连接状态描述
     */
    private String getConnectionStatusDesc(Integer status) {
        if (status == null) {
            return "未知";
        }
        return MonitorSysGenServerConnectionStatus.ConnectionStatus.fromCode(status).getDesc();
    }

    /**
     * 广播连接状态变化事件
     *
     * @param serverId    服务器ID
     * @param oldStatus   旧状态
     * @param newStatus   新状态
     * @param changeTime  变化时间
     */
    public void broadcastConnectionStatusChange(Integer serverId, Integer oldStatus, Integer newStatus, LocalDateTime changeTime) {
        try {
            Map<String, Object> changeData = new HashMap<>();
            changeData.put("serverId", serverId);
            changeData.put("oldStatus", oldStatus);
            changeData.put("newStatus", newStatus);
            changeData.put("changeTime", changeTime);

            ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                    .messageType("connection_status_change")
                    .serverId(serverId)
                    .data(changeData)
                    .timestamp(System.currentTimeMillis())
                    .build();

            socketSessionTemplate.send("gen/server", Json.toJson(message));

            log.debug("广播连接状态变化: serverId={}, {} -> {}", serverId, oldStatus, newStatus);

        } catch (Exception e) {
            log.error("广播连接状态变化失败: serverId={}", serverId, e);
        }
    }

    /**
     * 发送连接测试进度
     *
     * @param taskId      任务ID
     * @param progress    进度百分比
     * @param currentItem 当前处理项
     * @param totalItems  总项数
     */
    public void sendTestProgress(String taskId, int progress, int currentItem, int totalItems) {
        try {
            Map<String, Object> progressData = new HashMap<>();
            progressData.put("taskId", taskId);
            progressData.put("progress", progress);
            progressData.put("currentItem", currentItem);
            progressData.put("totalItems", totalItems);

            ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                    .messageType("test_progress")
                    .data(progressData)
                    .timestamp(System.currentTimeMillis())
                    .build();

            socketSessionTemplate.send("gen/server", Json.toJson(message));

            log.debug("发送连接测试进度: taskId={}, progress={}%", taskId, progress);

        } catch (Exception e) {
            log.error("发送连接测试进度失败: taskId={}", taskId, e);
        }
    }

    /**
     * 获取WebSocket主题列表
     *
     * @return 主题列表
     */
    public Map<String, String> getTopics() {
        Map<String, String> topics = new HashMap<>();
        topics.put("connectionStatus", TOPIC_CONNECTION_STATUS);
        topics.put("connectionStatistics", TOPIC_CONNECTION_STATISTICS);
        topics.put("connectionHealth", TOPIC_CONNECTION_HEALTH);
        topics.put("connectionAlert", TOPIC_CONNECTION_ALERT);
        return topics;
    }
}
