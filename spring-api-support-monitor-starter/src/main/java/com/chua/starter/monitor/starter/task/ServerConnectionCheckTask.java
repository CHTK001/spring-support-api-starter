package com.chua.starter.monitor.starter.task;

import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerConnectionStatus;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerLatencyDetectionService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerConnectionStatusService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerService;
import com.chua.starter.monitor.starter.websocket.ServerConnectionWebSocketService;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.common.support.json.Json;
import com.chua.starter.monitor.starter.message.ServerWebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 服务器连接状态检查定时任务
 *
 * @author CH
 * @since 2024/12/20
 */
@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "monitor.server.connection.check.enabled", havingValue = "true", matchIfMissing = true)
public class ServerConnectionCheckTask {

    private final MonitorSysGenServerService serverService;
    private final MonitorSysGenServerConnectionStatusService connectionStatusService;
    private final ServerConnectionWebSocketService webSocketService;
    private final SocketSessionTemplate socketSessionTemplate;
    private final MonitorSysGenServerLatencyDetectionService latencyDetectionService;
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    /**
     * 定时检查服务器连接状态
     * 每5分钟执行一次
     */
    @Scheduled(fixedRate = 300000) // 5分钟
    public void checkServerConnections() {
        log.debug("开始定时检查服务器连接状态...");
        
        try {
            // 获取所有启用的服务器
            List<MonitorSysGenServer> servers = serverService.list().stream()
                    .filter(server -> server.getMonitorSysGenServerStatus() != null && 
                                    server.getMonitorSysGenServerStatus() == 1)
                    .toList();
            
            if (servers.isEmpty()) {
                log.debug("没有找到启用的服务器，跳过连接检查");
                return;
            }
            
            log.info("开始检查 {} 台服务器的连接状态", servers.size());
            
            // 异步检查所有服务器连接
            List<CompletableFuture<Void>> futures = servers.stream()
                    .map(server -> CompletableFuture.runAsync(() -> checkSingleServerConnection(server), executorService))
                    .toList();
            
            // 等待所有检查完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenRun(() -> {
                        log.info("服务器连接状态检查完成");
                        // 广播统计信息
                        webSocketService.broadcastConnectionStatistics();
                    })
                    .exceptionally(throwable -> {
                        log.error("服务器连接状态检查出现异常", throwable);
                        return null;
                    });
            
        } catch (Exception e) {
            log.error("定时检查服务器连接状态失败", e);
        }
    }

    /**
     * 检查长时间未连接的服务器
     * 每30分钟执行一次
     */
    @Scheduled(fixedRate = 1800000) // 30分钟
    public void checkLongTimeNoConnectServers() {
        log.debug("开始检查长时间未连接的服务器...");
        
        try {
            var result = connectionStatusService.getLongTimeNoConnectServers(60); // 60分钟
            if (result != null && result.getData() != null && !result.getData().isEmpty()) {
                List<MonitorSysGenServerConnectionStatus> longTimeNoConnectServers = result.getData();
                
                log.warn("发现 {} 台服务器长时间未连接", longTimeNoConnectServers.size());
                
                // 尝试重新连接这些服务器
                for (MonitorSysGenServerConnectionStatus server : longTimeNoConnectServers) {
                    CompletableFuture.runAsync(() -> {
                        try {
                            log.info("尝试重新连接长时间未连接的服务器: {}", server.getMonitorSysGenServerName());
                            connectionStatusService.testServerConnection(server.getMonitorSysGenServerId());
                        } catch (Exception e) {
                            log.error("重新连接服务器失败: serverId={}", server.getMonitorSysGenServerId(), e);
                        }
                    }, executorService);
                }
            }
            
        } catch (Exception e) {
            log.error("检查长时间未连接服务器失败", e);
        }
    }

    /**
     * 生成连接状态报告
     * 每天上午9点执行
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void generateDailyConnectionReport() {
        log.info("开始生成每日连接状态报告...");
        
        try {
            var healthReportResult = connectionStatusService.getServerConnectionHealthReport();
            if (healthReportResult != null && healthReportResult.getData() != null) {
                // 广播健康报告
                webSocketService.broadcastHealthStatus();
                
                log.info("每日连接状态报告生成完成");
            }
            
        } catch (Exception e) {
            log.error("生成每日连接状态报告失败", e);
        }
    }

    /**
     * 延迟检测任务
     * 每10分钟执行一次，只检测在线的服务器
     */
    @Scheduled(fixedRate = 600000) // 10分钟
    public void checkServerLatency() {
        log.debug("开始定时检查服务器延迟...");

        try {
            // 获取所有在线的服务器
            List<MonitorSysGenServer> onlineServers = serverService.list().stream()
                    .filter(server -> server.getMonitorSysGenServerStatus() != null &&
                                    server.getMonitorSysGenServerStatus() == 1 &&
                                    server.getMonitorSysGenServerConnectionStatus() != null &&
                                    server.getMonitorSysGenServerConnectionStatus() == 1)
                    .toList();

            if (onlineServers.isEmpty()) {
                log.debug("没有在线的服务器需要检测延迟");
                return;
            }

            log.info("开始检测 {} 台在线服务器的延迟", onlineServers.size());

            // 异步检测所有在线服务器的延迟
            List<CompletableFuture<Void>> futures = onlineServers.stream()
                    .map(server -> CompletableFuture.runAsync(() -> {
                        try {
                            latencyDetectionService.detectLatencyAsync(server);
                        } catch (Exception e) {
                            log.error("检测服务器延迟失败: serverId={}", server.getMonitorSysGenServerId(), e);
                        }
                    }, executorService))
                    .toList();

            // 等待所有检测完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenRun(() -> {
                        log.info("服务器延迟检测完成");
                        // 检查延迟告警
                        checkLatencyAlerts();
                    })
                    .exceptionally(throwable -> {
                        log.error("服务器延迟检测出现异常", throwable);
                        return null;
                    });

        } catch (Exception e) {
            log.error("定时检查服务器延迟失败", e);
        }
    }

    /**
     * 检查延迟告警
     */
    private void checkLatencyAlerts() {
        try {
            var alertResult = latencyDetectionService.checkLatencyAlerts();
            if (alertResult != null && alertResult.isOk() && alertResult.getData() != null) {
                var alerts = alertResult.getData();
                Integer totalAlerts = alerts.getTotalAlerts();

                if (totalAlerts != null && totalAlerts > 0) {
                    log.warn("发现 {} 个延迟告警", totalAlerts);
                    // 通过WebSocket广播延迟告警
                    broadcastLatencyAlerts(alerts);
                }
            }
        } catch (Exception e) {
            log.error("检查延迟告警失败", e);
        }
    }

    /**
     * 清理过期的连接状态数据
     * 每天凌晨3点执行
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupExpiredConnectionData() {
        log.info("开始清理过期的连接状态数据...");

        try {
            // 这里可以添加清理逻辑，比如清理过期的连接日志等
            // 目前连接状态数据存储在主表中，暂不需要清理

            log.info("过期连接状态数据清理完成");

        } catch (Exception e) {
            log.error("清理过期连接状态数据失败", e);
        }
    }

    /**
     * 检查单个服务器连接状态
     */
    private void checkSingleServerConnection(MonitorSysGenServer server) {
        try {
            Integer serverId = server.getMonitorSysGenServerId();
            String serverName = server.getMonitorSysGenServerName();
            
            log.debug("检查服务器连接状态: {} (ID: {})", serverName, serverId);
            
            // 获取当前连接状态
            var currentStatusResult = connectionStatusService.getServerConnectionStatus(serverId);
            Integer oldStatus = null;
            if (currentStatusResult != null && currentStatusResult.getData() != null) {
                oldStatus = currentStatusResult.getData().getMonitorSysGenServerConnectionStatus();
            }
            
            // 测试连接
            var testResult = connectionStatusService.testServerConnection(serverId);
            if (testResult != null && testResult.getData() != null) {
                MonitorSysGenServerConnectionStatus newStatus = testResult.getData();
                Integer newConnectionStatus = newStatus.getMonitorSysGenServerConnectionStatus();
                
                // 如果状态发生变化，广播状态变化事件
                if (oldStatus != null && !oldStatus.equals(newConnectionStatus)) {
                    webSocketService.broadcastConnectionStatusChange(serverId, oldStatus, newConnectionStatus, LocalDateTime.now());
                    
                    log.info("服务器 {} 连接状态发生变化: {} -> {}", 
                            serverName, 
                            MonitorSysGenServerConnectionStatus.ConnectionStatus.fromCode(oldStatus).getDesc(),
                            MonitorSysGenServerConnectionStatus.ConnectionStatus.fromCode(newConnectionStatus).getDesc());
                }
                
                // 广播当前状态
                webSocketService.broadcastServerConnectionStatus(
                        serverId, 
                        newConnectionStatus, 
                        newStatus.getMonitorSysGenServerConnectionError(),
                        newStatus.getMonitorSysGenServerResponseTime()
                );
                
                log.debug("服务器 {} 连接检查完成，状态: {}", 
                        serverName, 
                        MonitorSysGenServerConnectionStatus.ConnectionStatus.fromCode(newConnectionStatus).getDesc());
            }
            
        } catch (Exception e) {
            log.error("检查服务器连接状态失败: serverId={}", server.getMonitorSysGenServerId(), e);
            
            // 更新为连接失败状态
            try {
                connectionStatusService.updateServerConnectionStatus(
                        server.getMonitorSysGenServerId(), 
                        MonitorSysGenServerConnectionStatus.ConnectionStatus.FAILED.getCode(), 
                        "连接检查异常: " + e.getMessage()
                );
                
                // 广播失败状态
                webSocketService.broadcastServerConnectionStatus(
                        server.getMonitorSysGenServerId(), 
                        MonitorSysGenServerConnectionStatus.ConnectionStatus.FAILED.getCode(), 
                        "连接检查异常: " + e.getMessage(),
                        null
                );
                
            } catch (Exception updateException) {
                log.error("更新服务器连接失败状态时出错: serverId={}", server.getMonitorSysGenServerId(), updateException);
            }
        }
    }

    /**
     * 手动触发所有服务器连接检查
     */
    public void triggerManualCheck() {
        log.info("手动触发服务器连接状态检查");
        CompletableFuture.runAsync(this::checkServerConnections, executorService);
    }

    /**
     * 手动触发单个服务器连接检查
     */
    public void triggerManualCheck(Integer serverId) {
        log.info("手动触发服务器连接状态检查: serverId={}", serverId);
        
        CompletableFuture.runAsync(() -> {
            try {
                MonitorSysGenServer server = serverService.getById(serverId);
                if (server != null) {
                    checkSingleServerConnection(server);
                } else {
                    log.warn("服务器不存在: serverId={}", serverId);
                }
            } catch (Exception e) {
                log.error("手动检查服务器连接状态失败: serverId={}", serverId, e);
            }
        }, executorService);
    }

    /**
     * 广播延迟告警信息到前端
     */
    private void broadcastLatencyAlerts(com.chua.starter.monitor.starter.pojo.LatencyAlerts alerts) {
        try {
            ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                    .messageType("latency_alerts")
                    .data(alerts)
                    .timestamp(System.currentTimeMillis())
                    .build();

            socketSessionTemplate.send("gen/server", Json.toJson(message));
            log.debug("延迟告警信息已广播: totalAlerts={}", alerts.getTotalAlerts());
        } catch (Exception e) {
            log.error("广播延迟告警信息失败", e);
        }
    }
}
