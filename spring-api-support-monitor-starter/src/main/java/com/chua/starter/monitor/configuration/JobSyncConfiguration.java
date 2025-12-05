package com.chua.starter.monitor.configuration;

import com.chua.starter.monitor.entity.DeviceMetrics;
import com.chua.starter.monitor.entity.MonitorJobLog;
import com.chua.starter.monitor.service.MonitorJobLogService;
import com.chua.starter.monitor.service.MonitorSysGenServerDeviceReportService;
import com.chua.sync.support.server.SyncServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

/**
 * Sync 配置
 * <p>
 * 依赖 spring-support-sync-starter 提供的 SyncServer
 * 处理 Job 结果、设备指标上报等
 * </p>
 *
 * @author CH
 * @since 2024/12/05
 */
@Slf4j
@Configuration
@ConditionalOnClass(SyncServer.class)
@ConditionalOnBean(SyncServer.class)
public class JobSyncConfiguration {

    private static final String TOPIC_JOB_RESULT = "monitor/job/result";
    private static final String TOPIC_DEVICE_METRICS = "monitor/device/metrics";
    private static final String TOPIC_DEVICE_INFO = "monitor/device/info";

    @Autowired
    private SyncServer syncServer;

    @Autowired
    private MonitorJobLogService jobLogService;

    @Autowired
    private MonitorSysGenServerDeviceReportService deviceReportService;

    @PostConstruct
    public void init() {
        log.info("[SyncConfig] 注册消息处理器");

        // 注册 Job 结果处理器
        syncServer.registerHandler(TOPIC_JOB_RESULT, (topic, sessionId, data) -> {
            handleJobResult(sessionId, data);
            return null;
        });

        // 注册设备指标处理器
        syncServer.registerHandler(TOPIC_DEVICE_METRICS, (topic, sessionId, data) -> {
            handleDeviceMetrics(sessionId, data);
            return null;
        });

        // 注册设备信息处理器
        syncServer.registerHandler(TOPIC_DEVICE_INFO, (topic, sessionId, data) -> {
            handleDeviceInfo(sessionId, data);
            return null;
        });

        log.info("[SyncConfig] 消息处理器注册完成 (JobResult, DeviceMetrics, DeviceInfo)");
    }

    /**
     * 处理任务结果
     */
    private void handleJobResult(String sessionId, Map<String, Object> data) {
        log.info("[JobSync] 收到任务结果: sessionId={}, data={}", sessionId, data);

        try {
            int jobId = getInt(data, "jobId");
            long logId = getLong(data, "logId");
            String status = getString(data, "status");
            String message = getString(data, "message");
            long timestamp = getLong(data, "timestamp");

            // 更新任务日志
            MonitorJobLog jobLog = jobLogService.getById(logId);
            if (jobLog != null) {
                jobLog.setJobLogExecuteCode(status);
                jobLog.setJobLogTriggerMsg(message);
                if (timestamp > 0 && jobLog.getJobLogTriggerTime() != null) {
                    long cost = timestamp - jobLog.getJobLogTriggerTime().getTime();
                    jobLog.setJobLogCost(new BigDecimal(cost));
                }
                jobLogService.updateById(jobLog);
                log.info("[JobSync] 任务日志已更新: logId={}, status={}", logId, status);
            } else {
                log.warn("[JobSync] 未找到任务日志: logId={}", logId);
            }
        } catch (Exception e) {
            log.error("[JobSync] 处理任务结果失败", e);
        }
    }

    private String getString(Map<String, Object> data, String key) {
        Object v = data.get(key);
        return v != null ? v.toString() : null;
    }

    private int getInt(Map<String, Object> data, String key) {
        Object v = data.get(key);
        if (v instanceof Number) return ((Number) v).intValue();
        if (v != null) return Integer.parseInt(v.toString());
        return 0;
    }

    private long getLong(Map<String, Object> data, String key) {
        Object v = data.get(key);
        if (v instanceof Number) return ((Number) v).longValue();
        if (v != null) return Long.parseLong(v.toString());
        return 0L;
    }

    private Double getDouble(Map<String, Object> data, String key) {
        Object v = data.get(key);
        if (v instanceof Number) return ((Number) v).doubleValue();
        if (v != null) {
            try {
                return Double.parseDouble(v.toString());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 处理设备信息上报
     */
    private void handleDeviceInfo(String sessionId, Map<String, Object> data) {
        String ipAddress = getString(data, "ipAddress");
        String deviceId = getString(data, "deviceId");
        log.info("[DeviceInfo] 收到设备信息: sessionId={}, deviceId={}, ip={}", sessionId, deviceId, ipAddress);

        try {
            if (ipAddress == null || ipAddress.isEmpty()) {
                log.warn("[DeviceInfo] 设备IP地址不能为空");
                return;
            }

            // 转换为 DeviceMetrics 并处理
            DeviceMetrics metrics = convertToDeviceMetrics(data);
            deviceReportService.handleDeviceMetrics(metrics);

        } catch (Exception e) {
            log.error("[DeviceInfo] 处理设备信息失败: deviceId={}", deviceId, e);
        }
    }

    /**
     * 处理设备指标上报
     * 与 DeviceReportController 逻辑一致
     */
    private void handleDeviceMetrics(String sessionId, Map<String, Object> data) {
        String ipAddress = getString(data, "ipAddress");
        String deviceId = getString(data, "deviceId");
        log.debug("[DeviceMetrics] 收到设备指标: sessionId={}, deviceId={}, ip={}", sessionId, deviceId, ipAddress);

        try {
            if (ipAddress == null || ipAddress.isEmpty()) {
                log.warn("[DeviceMetrics] 设备IP地址不能为空");
                return;
            }

            // 转换为 DeviceMetrics 对象
            DeviceMetrics metrics = convertToDeviceMetrics(data);

            // 使用 deviceReportService 处理（与 DeviceReportController 一致）
            // 内部会：1. 更新统计 2. 根据IP查找服务器 3. 保存到Redis 4. WebSocket推送
            var result = deviceReportService.handleDeviceMetrics(metrics);

            if (result.isOk()) {
                log.debug("[DeviceMetrics] 处理成功: deviceId={}, result={}", deviceId, result.getData());
            } else {
                log.warn("[DeviceMetrics] 处理失败: deviceId={}, error={}", deviceId, result.getMsg());
            }

        } catch (Exception e) {
            log.error("[DeviceMetrics] 处理设备指标失败: deviceId={}, ip={}", deviceId, ipAddress, e);
        }
    }

    /**
     * 转换为 DeviceMetrics 对象
     */
    private DeviceMetrics convertToDeviceMetrics(Map<String, Object> data) {
        DeviceMetrics metrics = new DeviceMetrics();

        // 基本信息
        metrics.setDeviceId(getString(data, "deviceId"));
        metrics.setDeviceName(getString(data, "deviceName"));
        metrics.setIpAddress(getString(data, "ipAddress"));
        metrics.setHostname(getString(data, "hostname"));

        // 操作系统
        metrics.setOsName(getString(data, "osName"));
        metrics.setOsVersion(getString(data, "osVersion"));
        metrics.setOsArch(getString(data, "osArch"));

        // CPU
        metrics.setCpuUsage(getDouble(data, "cpuUsage"));
        metrics.setCpuCores(getInt(data, "cpuCores"));
        metrics.setLoadAverage(getString(data, "loadAverage"));

        // 内存
        metrics.setMemoryUsage(getDouble(data, "memoryUsage"));
        metrics.setTotalMemory(getLong(data, "totalMemory"));
        metrics.setUsedMemory(getLong(data, "usedMemory"));
        metrics.setAvailableMemory(getLong(data, "availableMemory"));

        // 磁盘
        metrics.setDiskUsage(getDouble(data, "diskUsage"));
        metrics.setTotalDisk(getLong(data, "totalDisk"));
        metrics.setUsedDisk(getLong(data, "usedDisk"));
        metrics.setAvailableDisk(getLong(data, "availableDisk"));

        // 网络
        metrics.setNetworkInBytes(getLong(data, "networkInBytes"));
        metrics.setNetworkOutBytes(getLong(data, "networkOutBytes"));

        // 其他
        metrics.setUptime(getLong(data, "uptime"));
        metrics.setThreadCount(getInt(data, "threadCount"));
        metrics.setProcessCount(getInt(data, "processCount"));
        metrics.setOnline(true);

        // 收集时间
        long collectTime = getLong(data, "collectTime");
        if (collectTime > 0) {
            metrics.setCollectTime(LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(collectTime), ZoneId.systemDefault()));
        } else {
            metrics.setCollectTime(LocalDateTime.now());
        }

        return metrics;
    }
}
