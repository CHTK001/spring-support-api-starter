package com.chua.starter.monitor.configuration;

import com.chua.starter.monitor.entity.DeviceMetrics;
import com.chua.starter.monitor.entity.MonitorJobLog;
import com.chua.starter.monitor.service.MonitorJobLogService;
import com.chua.starter.monitor.service.MonitorSysGenServerDeviceReportService;
import com.chua.sync.support.pojo.ClientInfo;
import com.chua.sync.support.server.SyncServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private static final String TOPIC_APP_REGISTER = "monitor/app/register";
    private static final String TOPIC_APP_HEARTBEAT = "monitor/app/heartbeat";
    private static final String TOPIC_APP_UNREGISTER = "monitor/app/unregister";

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

        // 注册应用注册处理器
        syncServer.registerHandler(TOPIC_APP_REGISTER, (topic, sessionId, data) -> {
            handleAppRegister(sessionId, data);
            return null;
        });

        // 注册应用心跳处理器
        syncServer.registerHandler(TOPIC_APP_HEARTBEAT, (topic, sessionId, data) -> {
            handleAppHeartbeat(sessionId, data);
            return null;
        });

        // 注册应用下线处理器
        syncServer.registerHandler(TOPIC_APP_UNREGISTER, (topic, sessionId, data) -> {
            handleAppUnregister(sessionId, data);
            return null;
        });

        log.info("[SyncConfig] 消息处理器注册完成 (JobResult, DeviceMetrics, DeviceInfo, AppRegister)");
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

        // IP 地址（匹配服务器用）
        metrics.setIpAddress(getString(data, "ipAddress"));

        // 操作系统信息（更新服务器配置用）
        metrics.setOsName(getString(data, "osName"));
        metrics.setOsVersion(getString(data, "osVersion"));
        metrics.setOsArch(getString(data, "osArch"));

        // 指标数据
        metrics.setCpuUsage(getDouble(data, "cpuUsage"));
        metrics.setMemoryUsage(getDouble(data, "memoryUsage"));
        metrics.setDiskUsage(getDouble(data, "diskUsage"));
        metrics.setNetworkInBytes(getLong(data, "networkInBytes"));
        metrics.setNetworkOutBytes(getLong(data, "networkOutBytes"));

        metrics.setCollectTime(LocalDateTime.now());
        return metrics;
    }

    /**
     * 处理应用注册
     */
    private void handleAppRegister(String sessionId, Map<String, Object> data) {
        String appName = getString(data, "applicationName");
        String ipAddress = getString(data, "ipAddress");
        int serverPort = getInt(data, "serverPort");
        log.info("[AppRegister] 收到应用注册: sessionId={}, app={}, address={}:{}",
                sessionId, appName, ipAddress, serverPort);

        try {
            // 获取客户端信息并更新
            ClientInfo clientInfo = syncServer.getAllClients().get(sessionId);
            if (clientInfo != null) {
                updateClientInfo(clientInfo, data);
                log.info("[AppRegister] 应用注册成功: app={}, address={}:{}, profiles={}",
                        appName, ipAddress, serverPort, clientInfo.getActiveProfiles());
            } else {
                log.warn("[AppRegister] 未找到客户端会话: sessionId={}", sessionId);
            }
        } catch (Exception e) {
            log.error("[AppRegister] 处理应用注册失败: sessionId={}", sessionId, e);
        }
    }

    /**
     * 处理应用心跳
     */
    private void handleAppHeartbeat(String sessionId, Map<String, Object> data) {
        try {
            ClientInfo clientInfo = syncServer.getAllClients().get(sessionId);
            if (clientInfo != null) {
                clientInfo.setLastHeartbeatTime(System.currentTimeMillis());
                clientInfo.setOnline(true);
                // 更新运行时信息
                if (data.containsKey("heapUsed")) {
                    clientInfo.getMetadata().put("heapUsed", getLong(data, "heapUsed"));
                }
                if (data.containsKey("heapMax")) {
                    clientInfo.getMetadata().put("heapMax", getLong(data, "heapMax"));
                }
                if (data.containsKey("threadCount")) {
                    clientInfo.getMetadata().put("threadCount", getInt(data, "threadCount"));
                }
                if (data.containsKey("uptime")) {
                    clientInfo.getMetadata().put("uptime", getLong(data, "uptime"));
                }
                log.debug("[AppHeartbeat] 心跳更新: sessionId={}, app={}", sessionId, clientInfo.getAppName());
            }
        } catch (Exception e) {
            log.error("[AppHeartbeat] 处理心跳失败: sessionId={}", sessionId, e);
        }
    }

    /**
     * 处理应用下线
     */
    private void handleAppUnregister(String sessionId, Map<String, Object> data) {
        String appName = getString(data, "applicationName");
        log.info("[AppUnregister] 收到应用下线通知: sessionId={}, app={}", sessionId, appName);

        try {
            ClientInfo clientInfo = syncServer.getAllClients().get(sessionId);
            if (clientInfo != null) {
                clientInfo.setOnline(false);
            }
        } catch (Exception e) {
            log.error("[AppUnregister] 处理应用下线失败: sessionId={}", sessionId, e);
        }
    }

    /**
     * 更新客户端信息
     */
    private void updateClientInfo(ClientInfo clientInfo, Map<String, Object> data) {
        // Spring 应用信息
        clientInfo.setAppName(getString(data, "applicationName"));
        clientInfo.setActiveProfiles(getString(data, "activeProfiles"));
        clientInfo.setContextPath(getString(data, "contextPath"));
        clientInfo.setServerPort(getInt(data, "serverPort"));
        clientInfo.setServiceUrl(getString(data, "serviceUrl"));

        // 网络信息
        clientInfo.setIpAddress(getString(data, "ipAddress"));
        clientInfo.setHostname(getString(data, "hostname"));

        // JVM 信息
        clientInfo.setJavaVersion(getString(data, "javaVersion"));
        clientInfo.setPid(getLong(data, "pid"));

        // 操作系统
        clientInfo.setOsName(getString(data, "osName"));
        clientInfo.setOsVersion(getString(data, "osVersion"));

        // 时间信息
        clientInfo.setStartTime(getLong(data, "startTime"));
        clientInfo.setRegisterTime(System.currentTimeMillis());
        clientInfo.setLastHeartbeatTime(System.currentTimeMillis());
        clientInfo.setOnline(true);

        // 初始化 metadata
        if (clientInfo.getMetadata() == null) {
            clientInfo.setMetadata(new java.util.HashMap<>());
        }
        clientInfo.getMetadata().put("jvmName", getString(data, "jvmName"));
        clientInfo.getMetadata().put("jvmVersion", getString(data, "jvmVersion"));
        clientInfo.getMetadata().put("osArch", getString(data, "osArch"));
    }
}
