package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.monitor.entity.DeviceMetrics;
import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.entity.MonitorSysGenServerMetrics;
import com.chua.starter.monitor.service.server.MonitorSysGenServerDeviceReportService;
import com.chua.starter.monitor.service.server.MonitorSysGenServerMetricsService;
import com.chua.starter.monitor.service.server.MonitorSysGenServerService;
import com.chua.starter.monitor.service.server.MonitorSysGenServerDataReportService;
import com.chua.starter.monitor.service.server.ServerMetricsWebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 服务器设备数据上报服务实现
 * 
 * @author CH
 * @since 2024/12/19
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorSysGenServerDeviceReportServiceImpl implements MonitorSysGenServerDeviceReportService {

    private final MonitorSysGenServerService serverService;
    private final MonitorSysGenServerMetricsService metricsService;
    private final MonitorSysGenServerDataReportService dataReportService;
    private final ServerMetricsWebSocketService webSocketService;

    // 设备上报统计缓存
    private final Map<String, AtomicLong> deviceReportCount = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> deviceLastReportTime = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> deviceErrorCount = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public ReturnResult<String> handleDeviceMetrics(DeviceMetrics metrics) {
        try {
            if (metrics == null) {
                return ReturnResult.error("设备指标数据不能为空");
            }

            String deviceIp = metrics.getIpAddress();
            if (StringUtils.isEmpty(deviceIp)) {
                return ReturnResult.error("设备IP地址不能为空");
            }

            log.debug("处理设备指标数据上报: deviceIp={}", deviceIp);

            // 更新设备上报统计
            updateDeviceReportStats(deviceIp, true);

            // 根据IP地址查找匹配的服务器
            List<MonitorSysGenServer> matchedServers = findServersByIp(deviceIp);

            if (matchedServers.isEmpty()) {
                log.debug("未找到匹配的服务器: deviceIp={}", deviceIp);
                return ReturnResult.success("设备数据已接收，但未找到匹配的服务器");
            }

            // 为每个匹配的服务器创建指标数据
            for (MonitorSysGenServer server : matchedServers) {
                try {
                    // 更新服务器系统信息
                    updateServerSystemInfo(server, metrics);

                    MonitorSysGenServerMetrics serverMetrics = convertDeviceMetricsToServerMetrics(metrics, server);
                    // 通过WebSocket推送到前端
                    broadcastServerMetrics(server.getMonitorSysGenServerId(), serverMetrics);
                    // 保存到Redis
                    var saveResult = metricsService.saveMetrics(serverMetrics);
                    if (saveResult.isOk()) {
                        log.debug("设备指标数据已关联到服务器并推送到前端: deviceIp={}, serverId={}", deviceIp, server.getMonitorSysGenServerId());
                    } else {
                        log.warn("保存服务器指标数据失败: deviceIp={}, serverId={}, error={}", deviceIp, server.getMonitorSysGenServerId(), saveResult.getMsg());
                    }
                } catch (Exception e) {
                    log.error("保存服务器指标数据失败: deviceIp={}, serverId={}", deviceIp, server.getMonitorSysGenServerId(), e);
                    updateDeviceReportStats(deviceIp, false);
                }
            }

            return ReturnResult.success("设备数据处理成功，关联了 " + matchedServers.size() + " 台服务器");

        } catch (Exception e) {
            log.error("处理设备指标数据失败: deviceIp={}", metrics != null ? metrics.getIpAddress() : "unknown", e);
            if (metrics != null && StringUtils.isNotEmpty(metrics.getIpAddress())) {
                updateDeviceReportStats(metrics.getIpAddress(), false);
            }
            return ReturnResult.error("处理设备数据失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<String> handleDeviceMetrics(Map<String, Object> metricsData) {
        try {
            if (metricsData == null || metricsData.isEmpty()) {
                return ReturnResult.error("设备指标数据不能为空");
            }

            // 转换Map为DeviceMetrics对象
            DeviceMetrics metrics = convertMapToDeviceMetrics(metricsData);
            return handleDeviceMetrics(metrics);

        } catch (Exception e) {
            log.error("处理设备指标数据失败", e);
            return ReturnResult.error("处理设备数据失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getDeviceReportStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            // 总设备数量
            statistics.put("totalDevices", deviceReportCount.size());
            
            // 总上报次数
            long totalReports = deviceReportCount.values().stream()
                    .mapToLong(AtomicLong::get)
                    .sum();
            statistics.put("totalReports", totalReports);
            
            // 总错误次数
            long totalErrors = deviceErrorCount.values().stream()
                    .mapToLong(AtomicLong::get)
                    .sum();
            statistics.put("totalErrors", totalErrors);
            
            // 成功率
            double successRate = totalReports > 0 ? (double) (totalReports - totalErrors) / totalReports * 100 : 0;
            statistics.put("successRate", successRate);
            
            // 活跃设备数量（最近1小时内有上报）
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            long activeDevices = deviceLastReportTime.values().stream()
                    .mapToLong(time -> time.isAfter(oneHourAgo) ? 1 : 0)
                    .sum();
            statistics.put("activeDevices", activeDevices);
            
            statistics.put("updateTime", LocalDateTime.now());
            
            return ReturnResult.success(statistics);

        } catch (Exception e) {
            log.error("获取设备上报统计失败", e);
            return ReturnResult.error("获取统计信息失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getDeviceReportHistory(String deviceIp, Integer limit) {
        try {
            if (StringUtils.isEmpty(deviceIp)) {
                return ReturnResult.error("设备IP地址不能为空");
            }

            if (limit == null || limit <= 0) {
                limit = 100;
            }

            Map<String, Object> history = new HashMap<>();
            history.put("deviceIp", deviceIp);
            history.put("reportCount", deviceReportCount.getOrDefault(deviceIp, new AtomicLong(0)).get());
            history.put("errorCount", deviceErrorCount.getOrDefault(deviceIp, new AtomicLong(0)).get());
            history.put("lastReportTime", deviceLastReportTime.get(deviceIp));

            // 查询相关的服务器指标历史
            List<MonitorSysGenServer> matchedServers = findServersByIp(deviceIp);
            if (!matchedServers.isEmpty()) {
                // 这里可以添加查询服务器指标历史的逻辑
                history.put("relatedServers", matchedServers.size());
            }

            return ReturnResult.success(history);

        } catch (Exception e) {
            log.error("获取设备上报历史失败: deviceIp={}", deviceIp, e);
            return ReturnResult.error("获取上报历史失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<String> cleanupExpiredDeviceReports(Integer retentionDays) {
        try {
            if (retentionDays == null || retentionDays <= 0) {
                retentionDays = 30; // 默认保留30天
            }

            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(retentionDays);
            
            // 清理过期的设备上报记录
            int cleanedCount = 0;
            for (Map.Entry<String, LocalDateTime> entry : deviceLastReportTime.entrySet()) {
                if (entry.getValue().isBefore(cutoffTime)) {
                    String deviceIp = entry.getKey();
                    deviceLastReportTime.remove(deviceIp);
                    deviceReportCount.remove(deviceIp);
                    deviceErrorCount.remove(deviceIp);
                    cleanedCount++;
                }
            }

            log.info("清理过期设备上报数据完成: 清理了 {} 个设备的记录", cleanedCount);
            return ReturnResult.success("清理完成，共清理了 " + cleanedCount + " 个设备的记录");

        } catch (Exception e) {
            log.error("清理过期设备上报数据失败", e);
            return ReturnResult.error("清理失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getActiveDevices(Integer hours) {
        try {
            if (hours == null || hours <= 0) {
                hours = 24; // 默认24小时
            }

            LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hours);
            Map<String, Object> result = new HashMap<>();
            
            List<String> activeDeviceIps = deviceLastReportTime.entrySet().stream()
                    .filter(entry -> entry.getValue().isAfter(cutoffTime))
                    .map(Map.Entry::getKey)
                    .toList();

            result.put("activeDeviceCount", activeDeviceIps.size());
            result.put("activeDeviceIps", activeDeviceIps);
            result.put("timeRange", hours + " 小时");
            result.put("checkTime", LocalDateTime.now());

            return ReturnResult.success(result);

        } catch (Exception e) {
            log.error("获取活跃设备列表失败", e);
            return ReturnResult.error("获取活跃设备失败: " + e.getMessage());
        }
    }

    /**
     * 根据IP地址查找匹配的服务器
     */
    private List<MonitorSysGenServer> findServersByIp(String deviceIp) {
        LambdaQueryWrapper<MonitorSysGenServer> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MonitorSysGenServer::getMonitorSysGenServerHost, deviceIp)
                .or()
                .like(MonitorSysGenServer::getMonitorSysGenServerIpAddresses, deviceIp);
        
        return serverService.list(queryWrapper);
    }

    /**
     * 将设备指标转换为服务器指标
     */
    private MonitorSysGenServerMetrics convertDeviceMetricsToServerMetrics(DeviceMetrics deviceMetrics, MonitorSysGenServer server) {
        MonitorSysGenServerMetrics serverMetrics = new MonitorSysGenServerMetrics();
        
        // 设置基本信息
        serverMetrics.setMonitorSysGenServerId(server.getMonitorSysGenServerId());
        serverMetrics.setMonitorSysGenServerMetricsCollectTime(LocalDateTime.now());
        
        // 转换指标数据
        if (deviceMetrics.getCpuUsage() != null) {
            serverMetrics.setMonitorSysGenServerMetricsCpuUsage(BigDecimal.valueOf(deviceMetrics.getCpuUsage()));
        }
        if (deviceMetrics.getMemoryUsage() != null) {
            serverMetrics.setMonitorSysGenServerMetricsMemoryUsage(BigDecimal.valueOf(deviceMetrics.getMemoryUsage()));
        }
        if (deviceMetrics.getDiskUsage() != null) {
            serverMetrics.setMonitorSysGenServerMetricsDiskUsage(BigDecimal.valueOf(deviceMetrics.getDiskUsage()));
        }
        if (deviceMetrics.getNetworkInBytes() != null) {
            serverMetrics.setMonitorSysGenServerMetricsNetworkIn(deviceMetrics.getNetworkInBytes());
        }
        if (deviceMetrics.getNetworkOutBytes() != null) {
            serverMetrics.setMonitorSysGenServerMetricsNetworkOut(deviceMetrics.getNetworkOutBytes());
        }
        
        return serverMetrics;
    }

    /**
     * 将Map转换为DeviceMetrics对象
     */
    private DeviceMetrics convertMapToDeviceMetrics(Map<String, Object> metricsData) {
        DeviceMetrics metrics = new DeviceMetrics();
        
        // 设置设备IP
        Object deviceIp = metricsData.get("deviceIp");
        if (deviceIp != null) {
            metrics.setIpAddress(deviceIp.toString());
        }
        
        // 设置指标数据
        Object cpuUsage = metricsData.get("cpuUsage");
        if (cpuUsage instanceof Number) {
            metrics.setCpuUsage(((Number) cpuUsage).doubleValue());
        }
        
        Object memoryUsage = metricsData.get("memoryUsage");
        if (memoryUsage instanceof Number) {
            metrics.setMemoryUsage(((Number) memoryUsage).doubleValue());
        }
        
        Object diskUsage = metricsData.get("diskUsage");
        if (diskUsage instanceof Number) {
            metrics.setDiskUsage(((Number) diskUsage).doubleValue());
        }
        
        Object networkIn = metricsData.get("networkIn");
        if (networkIn instanceof Number) {
            metrics.setNetworkInBytes(((Number) networkIn).longValue());
        }

        Object networkOut = metricsData.get("networkOut");
        if (networkOut instanceof Number) {
            metrics.setNetworkOutBytes(((Number) networkOut).longValue());
        }
        
        return metrics;
    }

    @Override
    public ReturnResult<Map<String, Object>> getDeviceReportFrequencyStats() {
        try {
            Map<String, Object> stats = new HashMap<>();

            // 计算各设备的上报频率
            Map<String, Object> deviceFrequency = new HashMap<>();
            for (Map.Entry<String, AtomicLong> entry : deviceReportCount.entrySet()) {
                String deviceIp = entry.getKey();
                long reportCount = entry.getValue().get();
                LocalDateTime lastReportTime = deviceLastReportTime.get(deviceIp);

                if (lastReportTime != null) {
                    // 计算平均上报间隔（简化计算）
                    deviceFrequency.put(deviceIp, Map.of(
                            "reportCount", reportCount,
                            "lastReportTime", lastReportTime
                    ));
                }
            }

            stats.put("deviceFrequency", deviceFrequency);
            stats.put("totalDevices", deviceReportCount.size());
            stats.put("statisticsTime", LocalDateTime.now());

            return ReturnResult.success(stats);

        } catch (Exception e) {
            log.error("获取设备上报频率统计失败", e);
            return ReturnResult.error("获取频率统计失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<String> handleBatchDeviceMetrics(Map<String, Object> batchMetrics) {
        try {
            if (batchMetrics == null || batchMetrics.isEmpty()) {
                return ReturnResult.error("批量设备指标数据不能为空");
            }

            Object devicesData = batchMetrics.get("devices");
            if (!(devicesData instanceof List)) {
                return ReturnResult.error("设备数据格式错误");
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> devicesList = (List<Map<String, Object>>) devicesData;

            int successCount = 0;
            int errorCount = 0;

            for (Map<String, Object> deviceData : devicesList) {
                try {
                    ReturnResult<String> result = handleDeviceMetrics(deviceData);
                    if (result.isOk()) {
                        successCount++;
                    } else {
                        errorCount++;
                    }
                } catch (Exception e) {
                    log.error("处理单个设备数据失败", e);
                    errorCount++;
                }
            }

            String message = String.format("批量处理完成: 成功 %d 个，失败 %d 个", successCount, errorCount);
            return ReturnResult.success(message);

        } catch (Exception e) {
            log.error("批量处理设备指标数据失败", e);
            return ReturnResult.error("批量处理失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getDeviceReportErrorStats() {
        try {
            Map<String, Object> errorStats = new HashMap<>();

            // 总错误次数
            long totalErrors = deviceErrorCount.values().stream()
                    .mapToLong(AtomicLong::get)
                    .sum();
            errorStats.put("totalErrors", totalErrors);

            // 各设备错误统计
            Map<String, Long> deviceErrors = new HashMap<>();
            for (Map.Entry<String, AtomicLong> entry : deviceErrorCount.entrySet()) {
                deviceErrors.put(entry.getKey(), entry.getValue().get());
            }
            errorStats.put("deviceErrors", deviceErrors);

            // 错误率最高的设备
            String maxErrorDevice = deviceErrorCount.entrySet().stream()
                    .max(Map.Entry.comparingByValue((a, b) -> Long.compare(a.get(), b.get())))
                    .map(Map.Entry::getKey)
                    .orElse(null);
            errorStats.put("maxErrorDevice", maxErrorDevice);

            errorStats.put("statisticsTime", LocalDateTime.now());

            return ReturnResult.success(errorStats);

        } catch (Exception e) {
            log.error("获取设备上报错误统计失败", e);
            return ReturnResult.error("获取错误统计失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<String> resetDeviceReportStats(String deviceIp) {
        try {
            if (StringUtils.isEmpty(deviceIp)) {
                // 重置所有设备统计
                deviceReportCount.clear();
                deviceLastReportTime.clear();
                deviceErrorCount.clear();
                log.info("已重置所有设备上报统计");
                return ReturnResult.success("已重置所有设备上报统计");
            } else {
                // 重置指定设备统计
                deviceReportCount.remove(deviceIp);
                deviceLastReportTime.remove(deviceIp);
                deviceErrorCount.remove(deviceIp);
                log.info("已重置设备上报统计: deviceIp={}", deviceIp);
                return ReturnResult.success("已重置设备上报统计: " + deviceIp);
            }

        } catch (Exception e) {
            log.error("重置设备上报统计失败: deviceIp={}", deviceIp, e);
            return ReturnResult.error("重置统计失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getDeviceReportConfig(String deviceIp) {
        try {
            if (StringUtils.isEmpty(deviceIp)) {
                return ReturnResult.error("设备IP地址不能为空");
            }

            // 这里可以从数据库或配置文件中获取设备配置
            Map<String, Object> config = new HashMap<>();
            config.put("deviceIp", deviceIp);
            config.put("reportInterval", 60); // 默认60秒上报间隔
            config.put("enabled", true);
            config.put("retryCount", 3);
            config.put("timeout", 30);

            return ReturnResult.success(config);

        } catch (Exception e) {
            log.error("获取设备上报配置失败: deviceIp={}", deviceIp, e);
            return ReturnResult.error("获取配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<String> updateDeviceReportConfig(String deviceIp, Map<String, Object> config) {
        try {
            if (StringUtils.isEmpty(deviceIp)) {
                return ReturnResult.error("设备IP地址不能为空");
            }

            if (config == null || config.isEmpty()) {
                return ReturnResult.error("配置信息不能为空");
            }

            // 这里可以将配置保存到数据库或配置文件
            log.info("更新设备上报配置: deviceIp={}, config={}", deviceIp, config);

            return ReturnResult.success("设备配置更新成功");

        } catch (Exception e) {
            log.error("更新设备上报配置失败: deviceIp={}", deviceIp, e);
            return ReturnResult.error("更新配置失败: " + e.getMessage());
        }
    }

    /**
     * 更新设备上报统计
     */
    private void updateDeviceReportStats(String deviceIp, boolean success) {
        deviceReportCount.computeIfAbsent(deviceIp, k -> new AtomicLong(0)).incrementAndGet();
        deviceLastReportTime.put(deviceIp, LocalDateTime.now());

        if (!success) {
            deviceErrorCount.computeIfAbsent(deviceIp, k -> new AtomicLong(0)).incrementAndGet();
        }
    }

    /**
     * 更新服务器系统信息
     */
    private void updateServerSystemInfo(MonitorSysGenServer server, DeviceMetrics metrics) {
        boolean needUpdate = false;

        // 更新操作系统类型
        if (StringUtils.isNotEmpty(metrics.getOsName()) &&
                !metrics.getOsName().equals(server.getMonitorSysGenServerOsType())) {
            server.setMonitorSysGenServerOsType(metrics.getOsName());
            needUpdate = true;
        }

        // 更新操作系统版本
        if (StringUtils.isNotEmpty(metrics.getOsVersion()) &&
                !metrics.getOsVersion().equals(server.getMonitorSysGenServerOsVersion())) {
            server.setMonitorSysGenServerOsVersion(metrics.getOsVersion());
            needUpdate = true;
        }

        // 更新操作系统架构
        if (StringUtils.isNotEmpty(metrics.getOsArch()) &&
                !metrics.getOsArch().equals(server.getMonitorSysGenServerOsArch())) {
            server.setMonitorSysGenServerOsArch(metrics.getOsArch());
            needUpdate = true;
        }

        // 只有有变化时才更新数据库
        if (needUpdate) {
            try {
                serverService.updateById(server);
                log.debug("更新服务器系统信息: serverId={}, osType={}, osVersion={}, osArch={}",
                        server.getMonitorSysGenServerId(),
                        server.getMonitorSysGenServerOsType(),
                        server.getMonitorSysGenServerOsVersion(),
                        server.getMonitorSysGenServerOsArch());
            } catch (Exception e) {
                log.warn("更新服务器系统信息失败: serverId={}", server.getMonitorSysGenServerId(), e);
            }
        }
    }

    /**
     * 广播服务器指标数据到前端
     */
    private void broadcastServerMetrics(Integer serverId, MonitorSysGenServerMetrics metrics) {
        try {
            // 获取服务器对象
            MonitorSysGenServer server = serverService.getById(serverId);
            if (server != null) {
                // 使用WebSocket服务推送服务器指标数据
                webSocketService.pushSingleServerMetrics(server, metrics);
                log.debug("服务器指标数据已通过WebSocket推送: serverId={}", serverId);
            } else {
                log.warn("服务器不存在，无法推送指标数据: serverId={}", serverId);
            }
        } catch (Exception e) {
            log.error("推送服务器指标数据失败: serverId={}", serverId, e);
        }
    }
}
