package com.chua.starter.monitor.starter.service.impl;

import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerLatencyDetectionService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerService;
import com.chua.starter.monitor.starter.pojo.*;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.json.Json;
import com.chua.starter.monitor.starter.message.ServerWebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 服务器延迟检测服务实现
 *
 * @author CH
 * @since 2024/12/25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorSysGenServerLatencyDetectionServiceImpl implements MonitorSysGenServerLatencyDetectionService {

    private final MonitorSysGenServerService serverService;
    private final SocketSessionTemplate socketSessionTemplate;
    
    // 延迟检测缓存，避免频繁检测同一服务器
    private final Map<Integer, Long> lastDetectTime = new ConcurrentHashMap<>();

    // 检测间隔时间（毫秒）
    private static final long DETECT_INTERVAL = 30000; // 30秒

    @Override
    public ReturnResult<LatencyDetectionResult> detectLatency(MonitorSysGenServer server) {
        try {
            log.debug("开始检测服务器延迟: serverId={}, host={}", 
                    server.getMonitorSysGenServerId(), server.getMonitorSysGenServerHost());
            
            if (!StringUtils.hasText(server.getMonitorSysGenServerHost())) {
                return ReturnResult.error("服务器地址不能为空");
            }
            
            long startTime = System.currentTimeMillis();
            boolean success = false;
            long latency = 0;
            String errorMessage = null;
            
            try {
                // 使用TCP连接检测延迟
                latency = detectTcpLatency(server.getMonitorSysGenServerHost(), 
                        server.getMonitorSysGenServerPort() != null ? server.getMonitorSysGenServerPort() : 22);
                success = true;
                
            } catch (Exception e) {
                errorMessage = e.getMessage();
                log.warn("TCP延迟检测失败，尝试ICMP检测: serverId={}, error={}", 
                        server.getMonitorSysGenServerId(), e.getMessage());
                
                try {
                    // 如果TCP检测失败，尝试ICMP检测
                    latency = detectIcmpLatency(server.getMonitorSysGenServerHost());
                    success = true;
                    errorMessage = null;
                    
                } catch (Exception icmpException) {
                    errorMessage = "TCP和ICMP检测都失败: " + icmpException.getMessage();
                    log.error("延迟检测完全失败: serverId={}", server.getMonitorSysGenServerId(), icmpException);
                }
            }
            
            // 更新延迟信息
            updateServerLatencyInternal(server.getMonitorSysGenServerId(), success ? latency : null, success);

            LatencyDetectionResult result;
            if (success) {
                result = LatencyDetectionResult.success(
                        server.getMonitorSysGenServerId(),
                        server.getMonitorSysGenServerName(),
                        server.getMonitorSysGenServerHost(),
                        server.getMonitorSysGenServerPort(),
                        latency,
                        latency <= 100 ? "TCP" : "ICMP"
                );
            } else {
                result = LatencyDetectionResult.failure(
                        server.getMonitorSysGenServerId(),
                        server.getMonitorSysGenServerName(),
                        server.getMonitorSysGenServerHost(),
                        server.getMonitorSysGenServerPort(),
                        errorMessage
                );
            }

            log.debug("延迟检测完成: serverId={}, latency={}ms, success={}",
                    server.getMonitorSysGenServerId(), latency, success);

            return ReturnResult.success(result);
            
        } catch (Exception e) {
            log.error("延迟检测失败: serverId={}", server.getMonitorSysGenServerId(), e);
            return ReturnResult.error("延迟检测失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<LatencyBatchDetectionResult> batchDetectLatency() {
        try {
            long startTime = System.currentTimeMillis();
            log.info("开始批量延迟检测...");

            // 获取所有在线的服务器
            List<MonitorSysGenServer> onlineServers = serverService.list().stream()
                    .filter(server -> server.getMonitorSysGenServerStatus() != null &&
                                    server.getMonitorSysGenServerStatus() == 1 &&
                                    server.getMonitorSysGenServerConnectionStatus() != null &&
                                    server.getMonitorSysGenServerConnectionStatus() == 1)
                    .collect(Collectors.toList());

            if (onlineServers.isEmpty()) {
                log.info("没有在线的服务器需要检测延迟");
                LatencyBatchDetectionResult emptyResult = LatencyBatchDetectionResult.create(
                        Collections.emptyList(), System.currentTimeMillis() - startTime);
                return ReturnResult.success(emptyResult);
            }

            log.info("开始检测 {} 台在线服务器的延迟", onlineServers.size());

            // 异步检测所有服务器延迟
            List<CompletableFuture<LatencyDetectionResult>> futures = onlineServers.stream()
                    .map(server -> CompletableFuture.supplyAsync(() -> {
                        ReturnResult<LatencyDetectionResult> result = detectLatency(server);
                        return result.getData() != null ? result.getData() :
                                LatencyDetectionResult.failure(server.getMonitorSysGenServerId(),
                                        server.getMonitorSysGenServerName(),
                                        server.getMonitorSysGenServerHost(),
                                        server.getMonitorSysGenServerPort(),
                                        "检测失败");
                    }))
                    .collect(Collectors.toList());
            
            // 等待所有检测完成
            CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            List<LatencyDetectionResult> results = allOf.thenApply(v ->
                    futures.stream()
                            .map(CompletableFuture::join)
                            .collect(Collectors.toList())
            ).get();

            // 创建批量检测结果
            long duration = System.currentTimeMillis() - startTime;
            LatencyBatchDetectionResult batchResult = LatencyBatchDetectionResult.create(results, duration);

            log.info("批量延迟检测完成: 总数={}, 成功={}, 失败={}",
                    batchResult.getTotalCount(), batchResult.getSuccessCount(), batchResult.getFailCount());

            return ReturnResult.success(batchResult);
            
        } catch (Exception e) {
            log.error("批量延迟检测失败", e);
            return ReturnResult.error("批量延迟检测失败: " + e.getMessage());
        }
    }

    @Override
    @Async
    public void detectLatencyAsync(MonitorSysGenServer server) {
        try {
            ReturnResult<LatencyDetectionResult> result = detectLatency(server);
            if (result.isOk() && result.getData() != null) {
                // 通过WebSocket推送延迟信息到前端
                broadcastServerLatency(server.getMonitorSysGenServerId(), result.getData());
            }
        } catch (Exception e) {
            log.error("异步延迟检测失败: serverId={}", server.getMonitorSysGenServerId(), e);
        }
    }



    @Override
    public ReturnResult<LatencyStatistics> getLatencyStatistics(Integer serverId) {
        try {
            MonitorSysGenServer server = serverService.getById(serverId);
            if (server == null) {
                return ReturnResult.error("服务器不存在");
            }

            // 计算成功次数和成功率
            Long totalCount = server.getMonitorSysGenServerLatencyTotalCount() != null ?
                    server.getMonitorSysGenServerLatencyTotalCount().longValue() : null;
            Long failCount = server.getMonitorSysGenServerLatencyFailCount() != null ?
                    server.getMonitorSysGenServerLatencyFailCount().longValue() : null;
            Long successCount = totalCount != null && failCount != null ? totalCount - failCount : null;
            Double successRate = totalCount != null && totalCount > 0 ?
                    (double) (successCount != null ? successCount : 0) / totalCount * 100 : null;

            LatencyStatistics statistics = LatencyStatistics.builder()
                    .serverId(serverId)
                    .serverName(server.getMonitorSysGenServerName())
                    .currentLatency(server.getMonitorSysGenServerLatency())
                    .avgLatency(server.getMonitorSysGenServerAvgLatency())
                    .maxLatency(server.getMonitorSysGenServerMaxLatency())
                    .minLatency(server.getMonitorSysGenServerMinLatency())
                    .latencyStatus(server.getMonitorSysGenServerLatencyStatus())
                    .latencyStatusDesc(server.getLatencyStatusDesc())
                    .totalCount(totalCount)
                    .failCount(failCount)
                    .successCount(successCount)
                    .successRate(successRate)
                    .lastCheckTime(server.getMonitorSysGenServerLastLatencyCheckTime())
                    .statisticsTime(LocalDateTime.now())
                    .build();

            return ReturnResult.success(statistics);

        } catch (Exception e) {
            log.error("获取延迟统计信息失败: serverId={}", serverId, e);
            return ReturnResult.error("获取延迟统计失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<LatencyOverview> getLatencyOverview() {
        try {
            List<MonitorSysGenServer> servers = serverService.list();

            // 统计各种延迟状态的服务器数量
            Map<String, Long> statusCount = servers.stream()
                    .filter(server -> server.getMonitorSysGenServerLatencyStatus() != null)
                    .collect(Collectors.groupingBy(
                            server -> MonitorSysGenServer.LatencyStatus.fromCode(
                                    server.getMonitorSysGenServerLatencyStatus()).getDesc(),
                            Collectors.counting()
                    ));

            // 计算平均延迟
            OptionalDouble avgLatencyOpt = servers.stream()
                    .filter(server -> server.getMonitorSysGenServerLatency() != null)
                    .mapToLong(MonitorSysGenServer::getMonitorSysGenServerLatency)
                    .average();

            // 计算最大最小延迟
            OptionalLong maxLatencyOpt = servers.stream()
                    .filter(server -> server.getMonitorSysGenServerLatency() != null)
                    .mapToLong(MonitorSysGenServer::getMonitorSysGenServerLatency)
                    .max();

            OptionalLong minLatencyOpt = servers.stream()
                    .filter(server -> server.getMonitorSysGenServerLatency() != null)
                    .mapToLong(MonitorSysGenServer::getMonitorSysGenServerLatency)
                    .min();

            // 统计各状态服务器数量
            long normalServers = statusCount.getOrDefault("正常", 0L);
            long highLatencyServers = statusCount.getOrDefault("延迟较高", 0L);
            long failedServers = statusCount.getOrDefault("检测失败", 0L);
            long uncheckedServers = statusCount.getOrDefault("未检测", 0L);

            // 统计总数
            int totalServers = servers.size();
            long checkedServers = servers.stream()
                    .mapToLong(server -> server.getMonitorSysGenServerLatencyTotalCount() != null &&
                                       server.getMonitorSysGenServerLatencyTotalCount() > 0 ? 1 : 0)
                    .sum();

            // 计算健康率
            double healthRate = checkedServers > 0 ? (double) normalServers / checkedServers * 100 : 0.0;

            LatencyOverview overview = LatencyOverview.builder()
                    .statusCount(statusCount)
                    .avgLatency(avgLatencyOpt.isPresent() ? avgLatencyOpt.getAsDouble() : null)
                    .maxLatency(maxLatencyOpt.isPresent() ? maxLatencyOpt.getAsLong() : null)
                    .minLatency(minLatencyOpt.isPresent() ? minLatencyOpt.getAsLong() : null)
                    .totalServers(totalServers)
                    .checkedServers(checkedServers)
                    .normalServers(normalServers)
                    .highLatencyServers(highLatencyServers)
                    .failedServers(failedServers)
                    .uncheckedServers(uncheckedServers)
                    .healthRate(healthRate)
                    .updateTime(LocalDateTime.now())
                    .build();

            return ReturnResult.success(overview);

        } catch (Exception e) {
            log.error("获取延迟概览失败", e);
            return ReturnResult.error("获取延迟概览失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> resetLatencyStatistics(Integer serverId) {
        try {
            MonitorSysGenServer server = serverService.getById(serverId);
            if (server == null) {
                return ReturnResult.error("服务器不存在");
            }

            // 重置延迟统计信息
            server.setMonitorSysGenServerLatency(null);
            server.setMonitorSysGenServerAvgLatency(null);
            server.setMonitorSysGenServerMaxLatency(null);
            server.setMonitorSysGenServerMinLatency(null);
            server.setMonitorSysGenServerLatencyStatus(MonitorSysGenServer.LatencyStatus.NOT_CHECKED.getCode());
            server.setMonitorSysGenServerLatencyTotalCount(0);
            server.setMonitorSysGenServerLatencyFailCount(0);
            server.setMonitorSysGenServerLastLatencyCheckTime(null);

            // 清除历史记录
            latencyHistory.remove(serverId);

            boolean result = serverService.updateById(server);

            if (result) {
                log.info("服务器延迟统计已重置: serverId={}", serverId);
            }

            return ReturnResult.of(result, result ? "延迟统计重置成功" : "延迟统计重置失败");

        } catch (Exception e) {
            log.error("重置延迟统计失败: serverId={}", serverId, e);
            return ReturnResult.error("重置延迟统计失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<LatencyAlerts> checkLatencyAlerts() {
        try {
            List<MonitorSysGenServer> servers = serverService.list();

            // 查找延迟异常的服务器
            List<LatencyAlerts.LatencyAlert> highLatencyServers = servers.stream()
                    .filter(server -> server.getMonitorSysGenServerLatency() != null &&
                                    server.getMonitorSysGenServerLatency() >= 200)
                    .map(server -> LatencyAlerts.LatencyAlert.builder()
                            .serverId(server.getMonitorSysGenServerId())
                            .serverName(server.getMonitorSysGenServerName())
                            .serverHost(server.getMonitorSysGenServerHost())
                            .latency(server.getMonitorSysGenServerLatency())
                            .status(server.getLatencyStatusDesc())
                            .alertLevel(server.getMonitorSysGenServerLatency() >= 500 ? "严重" : "警告")
                            .lastCheckTime(server.getMonitorSysGenServerLastLatencyCheckTime())
                            .alertMessage("服务器延迟异常: " + server.getMonitorSysGenServerLatency() + "ms")
                            .build())
                    .collect(Collectors.toList());

            // 查找检测失败的服务器
            List<LatencyAlerts.LatencyAlert> failedServers = servers.stream()
                    .filter(server -> server.getMonitorSysGenServerLatencyStatus() != null &&
                                    server.getMonitorSysGenServerLatencyStatus() ==
                                    MonitorSysGenServer.LatencyStatus.CHECK_FAILED.getCode())
                    .map(server -> LatencyAlerts.LatencyAlert.builder()
                            .serverId(server.getMonitorSysGenServerId())
                            .serverName(server.getMonitorSysGenServerName())
                            .serverHost(server.getMonitorSysGenServerHost())
                            .status("检测失败")
                            .alertLevel("错误")
                            .lastCheckTime(server.getMonitorSysGenServerLastLatencyCheckTime())
                            .alertMessage("延迟检测失败")
                            .build())
                    .collect(Collectors.toList());

            LatencyAlerts alerts = LatencyAlerts.builder()
                    .highLatencyServers(highLatencyServers)
                    .failedServers(failedServers)
                    .totalAlerts(highLatencyServers.size() + failedServers.size())
                    .highLatencyCount(highLatencyServers.size())
                    .failedCount(failedServers.size())
                    .checkTime(LocalDateTime.now())
                    .build();

            return ReturnResult.success(alerts);

        } catch (Exception e) {
            log.error("检查延迟告警失败", e);
            return ReturnResult.error("检查延迟告警失败: " + e.getMessage());
        }
    }



    /**
     * TCP延迟检测
     */
    private long detectTcpLatency(String host, int port) throws IOException {
        long startTime = System.currentTimeMillis();

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 5000); // 5秒超时
            return System.currentTimeMillis() - startTime;
        }
    }

    /**
     * ICMP延迟检测
     */
    private long detectIcmpLatency(String host) throws IOException {
        long startTime = System.currentTimeMillis();

        InetAddress address = InetAddress.getByName(host);
        boolean reachable = address.isReachable(5000); // 5秒超时

        if (!reachable) {
            throw new IOException("主机不可达: " + host);
        }

        return System.currentTimeMillis() - startTime;
    }



    /**
     * 更新服务器延迟信息（内部方法）
     */
    private void updateServerLatencyInternal(Integer serverId, Long latency, boolean success) {
        try {
            MonitorSysGenServer server = serverService.getById(serverId);
            if (server == null) {
                return;
            }

            // 更新延迟信息
            server.setMonitorSysGenServerLatency(latency);
            server.setMonitorSysGenServerLastLatencyCheckTime(LocalDateTime.now());

            // 更新统计信息
            Integer totalCount = server.getMonitorSysGenServerLatencyTotalCount();
            if (totalCount == null) {
                totalCount = 0;
            }
            server.setMonitorSysGenServerLatencyTotalCount(totalCount + 1);

            if (!success) {
                Integer failCount = server.getMonitorSysGenServerLatencyFailCount();
                if (failCount == null) {
                    failCount = 0;
                }
                server.setMonitorSysGenServerLatencyFailCount(failCount + 1);
            }

            // 更新延迟状态
            if (success && latency != null) {
                if (latency <= 100) {
                    server.setMonitorSysGenServerLatencyStatus(MonitorSysGenServer.LatencyStatus.NORMAL.getCode());
                } else {
                    server.setMonitorSysGenServerLatencyStatus(MonitorSysGenServer.LatencyStatus.HIGH_LATENCY.getCode());
                }

                // 更新最大最小延迟
                if (server.getMonitorSysGenServerMaxLatency() == null || latency > server.getMonitorSysGenServerMaxLatency()) {
                    server.setMonitorSysGenServerMaxLatency(latency);
                }
                if (server.getMonitorSysGenServerMinLatency() == null || latency < server.getMonitorSysGenServerMinLatency()) {
                    server.setMonitorSysGenServerMinLatency(latency);
                }

                // 更新平均延迟
                Long avgLatency = server.getMonitorSysGenServerAvgLatency();
                if (avgLatency == null) {
                    server.setMonitorSysGenServerAvgLatency(latency);
                } else {
                    // 简单的移动平均
                    server.setMonitorSysGenServerAvgLatency((avgLatency + latency) / 2);
                }
            } else {
                server.setMonitorSysGenServerLatencyStatus(MonitorSysGenServer.LatencyStatus.CHECK_FAILED.getCode());
            }

            serverService.updateById(server);

        } catch (Exception e) {
            log.error("更新服务器延迟信息失败: serverId={}", serverId, e);
        }
    }

    /**
     * 广播服务器延迟信息到前端
     */
    private void broadcastServerLatency(Integer serverId, LatencyDetectionResult latencyResult) {
        try {
            ServerWebSocketMessage message = ServerWebSocketMessage.createServerMetricsMessage(
                    serverId, latencyResult.getServerName(), latencyResult);
            String messageJson = Json.toJson(message);
            socketSessionTemplate.send("gen/server", messageJson);
            log.debug("服务器延迟信息已广播: serverId={}", serverId);
        } catch (Exception e) {
            log.error("广播服务器延迟信息失败: serverId={}", serverId, e);
        }
    }
}
