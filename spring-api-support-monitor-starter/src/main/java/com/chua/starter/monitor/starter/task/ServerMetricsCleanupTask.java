package com.chua.starter.monitor.starter.task;

import com.chua.common.support.json.Json;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerMetrics;
import com.chua.starter.monitor.starter.entity.ServerWebSocketMessage;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerMetricsService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerService;
import com.chua.starter.monitor.starter.service.ServerConnectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务器指标清理定时任务
 *
 * @author CH
 * @since 2024/12/18
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ServerMetricsCleanupTask {

    private final MonitorSysGenServerService serverService;
    private final MonitorSysGenServerMetricsService metricsService;
    private final List<ServerConnectionService> connectionServices;
    private final SocketSessionTemplate socketSessionTemplate;

    /**
     * 清理过期指标数据
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredMetrics() {
        log.info("开始清理过期服务器指标数据...");
        
        try {
            // 获取所有启用的服务器
            List<MonitorSysGenServer> servers = serverService.list();
            
            int totalCleaned = 0;
            int serverCount = 0;
            
            for (MonitorSysGenServer server : servers) {
                if (server.getMonitorSysGenServerMetricsRetentionDays() != null && 
                    server.getMonitorSysGenServerMetricsRetentionDays() > 0) {
                    
                    try {
                        int cleaned = metricsService.deleteExpiredMetrics(
                                server.getMonitorSysGenServerId(), 
                                server.getMonitorSysGenServerMetricsRetentionDays()).getData();
                        
                        totalCleaned += cleaned;
                        serverCount++;
                        
                        log.info("服务器 {} 清理了 {} 条过期指标数据", 
                                server.getMonitorSysGenServerName(), cleaned);
                        
                    } catch (Exception e) {
                        log.error("清理服务器 {} 的过期指标数据失败: {}", 
                                server.getMonitorSysGenServerName(), e.getMessage(), e);
                    }
                }
            }
            
            // 发送清理完成消息
            sendCleanupMessage(serverCount, totalCleaned, true, null);
            
            log.info("过期指标数据清理完成，共处理 {} 台服务器，清理 {} 条记录", serverCount, totalCleaned);
            
        } catch (Exception e) {
            log.error("清理过期指标数据失败: {}", e.getMessage(), e);
            sendCleanupMessage(0, 0, false, e.getMessage());
        }
    }

    /**
     * 收集服务器指标数据
     * 每分钟执行一次
     */
    @Scheduled(fixedRate = 60000)
    public void collectServerMetrics() {
        try {
            // 获取所有启用监控的服务器
            List<MonitorSysGenServer> servers = serverService.list().stream()
                    .filter(server -> server.getMonitorSysGenServerMonitorEnabled() != null && 
                                    server.getMonitorSysGenServerMonitorEnabled() == 1)
                    .toList();
            
            for (MonitorSysGenServer server : servers) {
                try {
                    // 检查监控间隔
                    if (shouldCollectMetrics(server)) {
                        collectMetricsForServer(server);
                    }
                } catch (Exception e) {
                    log.error("收集服务器 {} 指标数据失败: {}", 
                            server.getMonitorSysGenServerName(), e.getMessage(), e);
                }
            }
            
        } catch (Exception e) {
            log.error("收集服务器指标数据任务失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 检查服务器连接状态
     * 每5分钟执行一次
     */
    @Scheduled(fixedRate = 300000)
    public void checkServerConnections() {
        log.debug("开始检查服务器连接状态...");
        
        try {
            List<MonitorSysGenServer> servers = serverService.list();
            
            for (MonitorSysGenServer server : servers) {
                try {
                    checkServerConnection(server);
                } catch (Exception e) {
                    log.error("检查服务器 {} 连接状态失败: {}", 
                            server.getMonitorSysGenServerName(), e.getMessage(), e);
                }
            }
            
        } catch (Exception e) {
            log.error("检查服务器连接状态任务失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送服务器健康报告
     * 每天上午9点执行
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendDailyHealthReport() {
        log.info("开始生成每日服务器健康报告...");
        
        try {
            Map<String, Object> report = generateHealthReport();
            
            // 发送健康报告
            ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                    .messageType("daily_health_report")
                    .data(report)
                    .timestamp(System.currentTimeMillis())
                    .build();

            String topic = "gen/server";
            socketSessionTemplate.send(topic, Json.toJson(message));
            
            log.info("每日服务器健康报告发送完成");
            
        } catch (Exception e) {
            log.error("生成每日健康报告失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 判断是否应该收集指标
     */
    private boolean shouldCollectMetrics(MonitorSysGenServer server) {
        // 这里可以根据服务器的监控间隔来判断
        // 简化实现：每分钟都收集
        return true;
    }

    /**
     * 为指定服务器收集指标
     */
    private void collectMetricsForServer(MonitorSysGenServer server) {
        try {
            ServerConnectionService connectionService = getConnectionService(server.getMonitorSysGenServerProtocol());
            if (connectionService == null) {
                log.warn("不支持的协议类型: {}", server.getMonitorSysGenServerProtocol());
                return;
            }

            MonitorSysGenServerMetrics metrics = connectionService.collectMetrics(server);
            if (metrics != null) {
                metricsService.saveMetrics(metrics);
                
                // 发送实时指标数据
                sendMetricsMessage(server, metrics);
                
                log.debug("收集服务器 {} 指标数据成功", server.getMonitorSysGenServerName());
            }
            
        } catch (Exception e) {
            log.error("收集服务器 {} 指标数据失败: {}", 
                    server.getMonitorSysGenServerName(), e.getMessage(), e);
        }
    }

    /**
     * 检查服务器连接状态
     */
    private void checkServerConnection(MonitorSysGenServer server) {
        try {
            ServerConnectionService connectionService = getConnectionService(server.getMonitorSysGenServerProtocol());
            if (connectionService == null) {
                return;
            }

            boolean connected = connectionService.isConnected(server.getMonitorSysGenServerId());

            // 发送连接状态消息
            Map<String, Object> checkData = new HashMap<>();
            checkData.put("serverId", server.getMonitorSysGenServerId());
            checkData.put("serverName", server.getMonitorSysGenServerName());
            checkData.put("connected", connected);

            ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                    .messageType("connection_check")
                    .serverId(server.getMonitorSysGenServerId())
                    .serverName(server.getMonitorSysGenServerName())
                    .data(checkData)
                    .timestamp(System.currentTimeMillis())
                    .build();

            socketSessionTemplate.send("gen/server", Json.toJson(message));
            
        } catch (Exception e) {
            log.error("检查服务器 {} 连接状态失败: {}", 
                    server.getMonitorSysGenServerName(), e.getMessage(), e);
        }
    }

    /**
     * 生成健康报告
     */
    private Map<String, Object> generateHealthReport() {
        Map<String, Object> report = new HashMap<>();
        
        try {
            // 获取服务器统计信息
            Map<String, Object> statistics = serverService.getServerStatistics().getData();
            report.put("statistics", statistics);
            
            // 获取健康状态汇总
            List<MonitorSysGenServer> servers = serverService.list();
            int healthyCount = 0;
            int warningCount = 0;
            int criticalCount = 0;
            
            for (MonitorSysGenServer server : servers) {
                try {
                    Map<String, Object> health = metricsService.getServerHealthStatus(server.getMonitorSysGenServerId()).getData();
                    String status = (String) health.get("status");
                    
                    switch (status) {
                        case "healthy":
                            healthyCount++;
                            break;
                        case "warning":
                            warningCount++;
                            break;
                        case "critical":
                            criticalCount++;
                            break;
                    }
                } catch (Exception e) {
                    criticalCount++; // 无法获取状态的服务器视为异常
                }
            }
            
            Map<String, Object> healthSummary = new HashMap<>();
            healthSummary.put("healthy", healthyCount);
            healthSummary.put("warning", warningCount);
            healthSummary.put("critical", criticalCount);
            healthSummary.put("total", servers.size());
            
            report.put("healthSummary", healthSummary);
            report.put("reportTime", LocalDateTime.now());
            report.put("type", "daily_health_report");
            
        } catch (Exception e) {
            log.error("生成健康报告失败: {}", e.getMessage(), e);
            report.put("error", e.getMessage());
        }
        
        return report;
    }

    /**
     * 获取连接服务
     */
    private ServerConnectionService getConnectionService(String protocol) {
        return connectionServices.stream()
                .filter(service -> service.getProtocol().equalsIgnoreCase(protocol))
                .findFirst()
                .orElse(null);
    }

    /**
     * 发送清理消息
     */
    private void sendCleanupMessage(int serverCount, int totalCleaned, boolean success, String error) {
        try {
            Map<String, Object> cleanupData = new HashMap<>();
            cleanupData.put("serverCount", serverCount);
            cleanupData.put("totalCleaned", totalCleaned);
            cleanupData.put("success", success);

            if (error != null) {
                cleanupData.put("error", error);
            }

            ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                    .messageType("cleanup_result")
                    .data(cleanupData)
                    .timestamp(System.currentTimeMillis())
                    .build();

            socketSessionTemplate.send("gen/server", Json.toJson(message));
        } catch (Exception e) {
            log.error("发送清理消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送指标消息
     */
    private void sendMetricsMessage(MonitorSysGenServer server, MonitorSysGenServerMetrics metrics) {
        try {
            ServerWebSocketMessage message = ServerWebSocketMessage.createServerMetricsMessage(
                    server.getMonitorSysGenServerId(),
                    server.getMonitorSysGenServerName(),
                    metrics
            );

            String topic = "gen/server";
            socketSessionTemplate.send(topic, Json.toJson(message));
        } catch (Exception e) {
            log.error("发送指标消息失败: {}", e.getMessage(), e);
        }
    }
}
