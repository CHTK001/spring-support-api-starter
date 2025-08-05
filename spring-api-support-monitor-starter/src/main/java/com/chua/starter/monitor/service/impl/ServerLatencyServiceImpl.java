package com.chua.starter.monitor.service.impl;

import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.message.ServerWebSocketMessage;
import com.chua.starter.monitor.service.MonitorSysGenServerService;
import com.chua.starter.monitor.service.ServerLatencyService;
import com.chua.starter.redis.support.service.TimeSeriesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务器延迟服务实现类
 *
 * @author CH
 * @since 2024/12/26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServerLatencyServiceImpl implements ServerLatencyService {

    private final TimeSeriesService timeSeriesService;
    private final SocketSessionTemplate socketSessionTemplate;
    private final MonitorSysGenServerService serverService;

    /**
     * Redis键前缀 - 只保留最新数据
     */
    private static final String REDIS_LATENCY_PREFIX = "server:latency:";

    /**
     * WebSocket主题
     */
    private static final String TOPIC_SERVER_METRICS = "gen/server";

    /**
     * 延迟数据缓存，用于记录变化
     */
    private final Map<Integer, Long> latencyCache = new ConcurrentHashMap<>();

    @Override
    public ReturnResult<Boolean> updateServerLatencyToRedis(Integer serverId, Long latency) {
        try {
            if (serverId == null || latency == null) {
                return ReturnResult.error("服务器ID和延迟时间不能为空");
            }

            // 检查是否有变化
            Long cachedLatency = latencyCache.get(serverId);
            if (cachedLatency != null && cachedLatency.equals(latency)) {
                // 延迟没有变化，不需要更新
                return ReturnResult.ok(false, "延迟无变化，跳过更新");
            }

            // 更新缓存
            latencyCache.put(serverId, latency);

            // 只存储最新数据到Redis
            String key = REDIS_LATENCY_PREFIX + serverId;
            long currentTimestamp = System.currentTimeMillis();

            // 逐个字段保存到Redis Hash
            timeSeriesService.hSet(key, "latency", String.valueOf(latency));
            timeSeriesService.hSet(key, "timestamp", String.valueOf(currentTimestamp));
            timeSeriesService.hSet(key, "serverId", String.valueOf(serverId));

            log.debug("服务器 {} 延迟已更新到Redis: {}ms", serverId, latency);
            return ReturnResult.ok(true, "延迟数据已更新");

        } catch (Exception e) {
            log.error("更新服务器延迟到Redis失败: serverId={}, latency={}", serverId, latency, e);
            return ReturnResult.error("更新延迟数据失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Long> getServerLatencyFromRedis(Integer serverId) {
        try {
            if (serverId == null) {
                return ReturnResult.error("服务器ID不能为空");
            }

            String key = REDIS_LATENCY_PREFIX + serverId;
            Map<String, String> latencyData = timeSeriesService.hGet(key);

            if (latencyData == null || !latencyData.containsKey("latency")) {
                return ReturnResult.ok(null, "未找到延迟数据");
            }

            Long latency = Long.valueOf(latencyData.get("latency"));
            return ReturnResult.ok(latency);

        } catch (Exception e) {
            log.error("从Redis获取服务器延迟失败: serverId={}", serverId, e);
            return ReturnResult.error("获取延迟数据失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<Integer, Long>> getBatchServerLatency(List<Integer> serverIds) {
        try {
            if (serverIds == null || serverIds.isEmpty()) {
                return ReturnResult.ok(new HashMap<>());
            }

            Map<Integer, Long> result = new HashMap<>();
            for (Integer serverId : serverIds) {
                ReturnResult<Long> latencyResult = getServerLatencyFromRedis(serverId);
                if (latencyResult.isOk() && latencyResult.getData() != null) {
                    result.put(serverId, latencyResult.getData());
                }
            }

            return ReturnResult.ok(result);

        } catch (Exception e) {
            log.error("批量获取服务器延迟失败", e);
            return ReturnResult.error("批量获取延迟数据失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> pushLatencyToFrontend(Integer serverId, Long latency) {
        try {
            if (serverId == null || latency == null) {
                return ReturnResult.error("服务器ID和延迟时间不能为空");
            }

            // 获取服务器信息
            MonitorSysGenServer server = serverService.getById(serverId);
            if (server == null) {
                return ReturnResult.error("服务器不存在");
            }

            // 构建延迟数据
            Map<String, Object> latencyData = new HashMap<>();
            latencyData.put("serverId", serverId);
            latencyData.put("serverName", server.getMonitorSysGenServerName());
            latencyData.put("latency", latency);
            latencyData.put("timestamp", System.currentTimeMillis());
            latencyData.put("status", getLatencyStatus(latency));

            // 构建WebSocket消息
            ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                    .messageType(ServerWebSocketMessage.MessageType.SERVER_LATENCY)
                    .serverId(serverId)
                    .serverName(server.getMonitorSysGenServerName())
                    .data(latencyData)
                    .timestamp(System.currentTimeMillis())
                    .build();

            // 发送到前端
            socketSessionTemplate.send(TOPIC_SERVER_METRICS, Json.toJson(message));

            log.debug("推送服务器 {} 延迟数据到前端: {}ms", server.getMonitorSysGenServerName(), latency);
            return ReturnResult.ok(true, "延迟数据已推送");

        } catch (Exception e) {
            log.error("推送延迟数据到前端失败: serverId={}, latency={}", serverId, latency, e);
            return ReturnResult.error("推送延迟数据失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> batchPushLatencyToFrontend(Map<Integer, Long> latencyData) {
        try {
            if (latencyData == null || latencyData.isEmpty()) {
                return ReturnResult.ok(true, "无数据需要推送");
            }

            List<Map<String, Object>> batchData = new ArrayList<>();
            for (Map.Entry<Integer, Long> entry : latencyData.entrySet()) {
                Integer serverId = entry.getKey();
                Long latency = entry.getValue();

                MonitorSysGenServer server = serverService.getById(serverId);
                if (server != null) {
                    Map<String, Object> serverLatencyData = new HashMap<>();
                    serverLatencyData.put("serverId", serverId);
                    serverLatencyData.put("serverName", server.getMonitorSysGenServerName());
                    serverLatencyData.put("latency", latency);
                    serverLatencyData.put("timestamp", System.currentTimeMillis());
                    serverLatencyData.put("status", getLatencyStatus(latency));
                    batchData.add(serverLatencyData);
                }
            }

            if (!batchData.isEmpty()) {
                ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                        .messageType(ServerWebSocketMessage.MessageType.BATCH_SERVER_LATENCY)
                        .data(batchData)
                        .timestamp(System.currentTimeMillis())
                        .build();

                socketSessionTemplate.send(TOPIC_SERVER_METRICS, Json.toJson(message));
                log.debug("批量推送 {} 个服务器延迟数据到前端", batchData.size());
            }

            return ReturnResult.ok(true, "批量延迟数据已推送");

        } catch (Exception e) {
            log.error("批量推送延迟数据到前端失败", e);
            return ReturnResult.error("批量推送延迟数据失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Integer> saveLatencyToDatabase() {
        try {
            // 简化实现：只更新当前缓存中的延迟数据到数据库
            int savedCount = 0;

            for (Map.Entry<Integer, Long> entry : latencyCache.entrySet()) {
                Integer serverId = entry.getKey();
                Long latency = entry.getValue();

                // 更新数据库中的延迟字段
                MonitorSysGenServer server = serverService.getById(serverId);
                if (server != null) {
                    server.setMonitorSysGenServerLatency(latency);
                    boolean updated = serverService.updateById(server);
                    if (updated) {
                        savedCount++;
                        log.debug("保存服务器 {} 延迟到数据库: {}ms", serverId, latency);
                    }
                }
            }

            return ReturnResult.ok(savedCount, "已保存 " + savedCount + " 个服务器延迟数据");

        } catch (Exception e) {
            log.error("保存延迟数据到数据库失败", e);
            return ReturnResult.error("保存延迟数据失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<Map<String, Object>>> getServerLatencyHistory(Integer serverId, LocalDateTime startTime, LocalDateTime endTime) {
        // 不再支持历史数据查询，只返回当前最新数据
        try {
            if (serverId == null) {
                return ReturnResult.error("服务器ID不能为空");
            }

            List<Map<String, Object>> historyData = new ArrayList<>();

            // 只返回当前最新的延迟数据
            String currentKey = REDIS_LATENCY_PREFIX + serverId;
            Map<String, String> currentData = timeSeriesService.hGet(currentKey);
            if (currentData != null && currentData.containsKey("latency")) {
                Map<String, Object> data = new HashMap<>();
                data.put("timestamp", Long.valueOf(currentData.getOrDefault("timestamp", "0")));
                data.put("latency", Long.valueOf(currentData.get("latency")));
                data.put("serverId", serverId);
                historyData.add(data);
            }

            return ReturnResult.ok(historyData);

        } catch (Exception e) {
            log.error("获取服务器延迟数据失败: serverId={}", serverId, e);
            return ReturnResult.error("获取延迟数据失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Integer> cleanupExpiredLatencyData(Integer expireDays) {
        try {
            // 由于只保留最新数据，不需要清理历史数据
            log.info("延迟数据清理：只保留最新数据，无需清理历史数据");
            return ReturnResult.ok(0, "无需清理，只保留最新数据");

        } catch (Exception e) {
            log.error("清理延迟数据失败", e);
            return ReturnResult.error("清理失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getLatencyStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            // 统计当前在线服务器数量和延迟分布
            int totalServers = latencyCache.size();
            int normalCount = 0;
            int highCount = 0;
            int abnormalCount = 0;
            
            for (Long latency : latencyCache.values()) {
                if (latency < 100) {
                    normalCount++;
                } else if (latency < 500) {
                    highCount++;
                } else {
                    abnormalCount++;
                }
            }
            
            statistics.put("totalServers", totalServers);
            statistics.put("normalCount", normalCount);
            statistics.put("highCount", highCount);
            statistics.put("abnormalCount", abnormalCount);
            statistics.put("timestamp", System.currentTimeMillis());
            
            return ReturnResult.ok(statistics);

        } catch (Exception e) {
            log.error("获取延迟统计信息失败", e);
            return ReturnResult.error("获取统计信息失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<Map<String, Object>>> checkLatencyAlerts() {
        try {
            List<Map<String, Object>> alerts = new ArrayList<>();
            
            for (Map.Entry<Integer, Long> entry : latencyCache.entrySet()) {
                Integer serverId = entry.getKey();
                Long latency = entry.getValue();
                
                if (latency >= 500) { // 异常延迟
                    MonitorSysGenServer server = serverService.getById(serverId);
                    if (server != null) {
                        Map<String, Object> alert = new HashMap<>();
                        alert.put("serverId", serverId);
                        alert.put("serverName", server.getMonitorSysGenServerName());
                        alert.put("latency", latency);
                        alert.put("level", "error");
                        alert.put("message", "服务器延迟异常");
                        alert.put("timestamp", System.currentTimeMillis());
                        alerts.add(alert);
                    }
                }
            }
            
            return ReturnResult.ok(alerts);

        } catch (Exception e) {
            log.error("检查延迟告警失败", e);
            return ReturnResult.error("检查告警失败: " + e.getMessage());
        }
    }

    /**
     * 获取延迟状态
     */
    private String getLatencyStatus(Long latency) {
        if (latency < 100) {
            return "normal";
        } else if (latency < 500) {
            return "high";
        } else {
            return "abnormal";
        }
    }
}
