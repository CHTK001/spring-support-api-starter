package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.mapper.MonitorSysGenServerMapper;
import com.chua.starter.monitor.service.DelayedUpdateService;
import com.chua.starter.monitor.service.MonitorSysGenServerMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 延迟更新实现：
 * - 以 serverId 为 key，每种类型只保留最后一条
 * - 周期性批量刷新到数据库
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DelayedUpdateServiceImpl implements DelayedUpdateService {

    private final MonitorSysGenServerMapper serverMapper;
    private final MonitorSysGenServerMetricsService metricsService;

    // 在线状态缓冲: serverId -> isOnline
    private final Map<Integer, Boolean> onlineStatusBuffer = new ConcurrentHashMap<>();
    // 健康状态缓冲: serverId -> isHealthy
    private final Map<Integer, Boolean> healthStatusBuffer = new ConcurrentHashMap<>();
    // 连接状态缓冲: serverId -> status+error
    private final Map<Integer, ConnEntry> connectionStatusBuffer = new ConcurrentHashMap<>();
    // 指标清理缓冲：serverId -> expireTime（多个 serverId 仅保留全局最小的 expireTime，最终按“最早”时间执行一次全局清理）
    private final Map<Integer, LocalDateTime> metricsCleanupBuffer = new ConcurrentHashMap<>();
    private final AtomicReference<LocalDateTime> minCleanupExpireTime = new AtomicReference<>(null);

    @Value("${server.delay-update.flush-interval-ms:1000}")
    private long flushIntervalMs;

    @Override
    public void submitOnlineStatus(Integer serverId, boolean online) {
        if (serverId == null) return;
        onlineStatusBuffer.put(serverId, online);
    }

    @Override
    public void submitHealthStatus(Integer serverId, boolean healthy) {
        if (serverId == null) return;
        healthStatusBuffer.put(serverId, healthy);
    }

    @Override
    public void submitConnectionStatus(Integer serverId, Integer connectionStatus, String errorMessage) {
        if (serverId == null) return;
        connectionStatusBuffer.put(serverId, new ConnEntry(connectionStatus, errorMessage));
    }

    @Override
    public void submitMetricsCleanup(Integer serverId, LocalDateTime expireTime) {
        if (expireTime == null) return;
        metricsCleanupBuffer.put(serverId, expireTime);
        // 维护全局最小的过期时间
        minCleanupExpireTime.updateAndGet(prev -> prev == null || expireTime.isBefore(prev) ? expireTime : prev);
    }

    /**
     * 定时刷新：合并后批量更新数据库
     */
    @Scheduled(fixedDelayString = "${server.delay-update.flush-interval-ms:1000}")
    public void flush() {
        flushOnline();
        flushHealth();
        flushConnection();
        flushMetricsCleanup();
    }

    private void flushOnline() {
        if (onlineStatusBuffer.isEmpty()) return;
        // 逐条更新：保留简单、避免批量 SQL 复杂性（可按需优化批量）
        onlineStatusBuffer.forEach((serverId, online) -> {
            try {
                LambdaUpdateChainWrapper<MonitorSysGenServer> uw = new LambdaUpdateChainWrapper<>(serverMapper);
                uw.eq(MonitorSysGenServer::getMonitorSysGenServerId, serverId)
                  .set(MonitorSysGenServer::getMonitorSysGenServerConnectionStatus, online ? 1 : 0)
                  .set(MonitorSysGenServer::getMonitorSysGenServerLastConnectTime, LocalDateTime.now())
                  .update();
            } catch (Exception e) {
                log.warn("延迟刷新在线状态失败 serverId={} online={}", serverId, online, e);
            }
        });
        onlineStatusBuffer.clear();
    }

    private void flushHealth() {
        if (healthStatusBuffer.isEmpty()) return;
        // 目前 MonitorSysGenServer 无独立健康字段，按原有逻辑：不健康则置连接状态为离线
        healthStatusBuffer.forEach((serverId, healthy) -> {
            try {
                if (!healthy) {
                    LambdaUpdateChainWrapper<MonitorSysGenServer> uw = new LambdaUpdateChainWrapper<>(serverMapper);
                    uw.eq(MonitorSysGenServer::getMonitorSysGenServerId, serverId)
                      .set(MonitorSysGenServer::getMonitorSysGenServerConnectionStatus, 0)
                      .set(MonitorSysGenServer::getMonitorSysGenServerLastConnectTime, LocalDateTime.now())
                      .update();
                }
            } catch (Exception e) {
                log.warn("延迟刷新健康状态失败 serverId={} healthy={}", serverId, healthy, e);
            }
        });
        healthStatusBuffer.clear();
    }

    private void flushConnection() {
        if (connectionStatusBuffer.isEmpty()) return;
        connectionStatusBuffer.forEach((serverId, entry) -> {
            try {
                LambdaUpdateChainWrapper<MonitorSysGenServer> uw = new LambdaUpdateChainWrapper<>(serverMapper);
                uw.eq(MonitorSysGenServer::getMonitorSysGenServerId, serverId)
                  .set(MonitorSysGenServer::getMonitorSysGenServerConnectionStatus, entry.status)
                  .set(MonitorSysGenServer::getMonitorSysGenServerLastConnectTime, LocalDateTime.now())
                  .set(entry.error != null, MonitorSysGenServer::getMonitorSysGenServerConnectionError, entry.error)
                  .update();
            } catch (Exception e) {
                log.warn("延迟刷新连接状态失败 serverId={} status={} err={}", serverId, entry.status, entry.error, e);
            }
        });
        connectionStatusBuffer.clear();
    }

    private void flushMetricsCleanup() {
        LocalDateTime cutoff = minCleanupExpireTime.getAndSet(null);
        if (cutoff == null) return;
        try {
            // 采用最小过期时间，进行一次全局清理
            // 这里假设 metricsService 或 mapper 提供清理方法；当前 LogCleanupStep 通过 metricsService.cleanupExpiredMetrics(expireTime)
            // 我们在此处调用一次等效的清理逻辑
            // 可通过事件或直接注入服务进行调用，这里直接注入 metricsService 更简洁
            metricsService.cleanupExpiredMetrics(cutoff);
        } catch (Exception e) {
            log.warn("延迟刷新指标清理失败 cutoff={}", cutoff, e);
        } finally {
            metricsCleanupBuffer.clear();
        }
    }

    @lombok.AllArgsConstructor
    private static class ConnEntry {
        Integer status;
        String error;
    }
}

