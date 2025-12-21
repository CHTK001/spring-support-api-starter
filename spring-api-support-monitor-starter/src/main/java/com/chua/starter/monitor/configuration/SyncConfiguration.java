package com.chua.starter.monitor.configuration;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.MapUtils;
import com.chua.starter.monitor.entity.DeviceMetrics;
import com.chua.starter.monitor.entity.MonitorJobLog;
import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.service.agent.AgentDataStorageService;
import com.chua.starter.monitor.service.job.MonitorJobLogService;
import com.chua.starter.monitor.service.server.MonitorSysGenServerDeviceReportService;
import com.chua.starter.monitor.service.server.MonitorSysGenServerService;
import com.chua.starter.monitor.service.server.MonitorSysGenServerSettingService;
import com.chua.starter.monitor.service.alert.UnifiedAlertService;
import com.chua.sync.support.pojo.ClientInfo;
import com.chua.sync.support.server.SyncServer;
import com.chua.sync.support.spi.SyncMessageHandler;

import java.util.function.BiConsumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
@AutoConfiguration(after = com.chua.sync.support.configuration.SyncAutoConfiguration.class)
@ConditionalOnClass(SyncServer.class)
@ConditionalOnBean(SyncServer.class)
public class SyncConfiguration {

    private static final String TOPIC_JOB_RESULT = "monitor/job/result";
    private static final String TOPIC_DEVICE_METRICS = "monitor/device/metrics";
    private static final String TOPIC_DEVICE_INFO = "monitor/device/info";
    private static final String TOPIC_APP_REPORT = "monitor/app/report";
    
    // Agent 数据上报 Topic
    private static final String TOPIC_AGENT_TRACE = "agent/trace";
    private static final String TOPIC_AGENT_SERVER = "agent/server";
    private static final String TOPIC_AGENT_FD = "agent/fd";
    

    @Autowired
    private SyncServer syncServer;

    @Autowired
    private MonitorJobLogService jobLogService;

    @Autowired
    private MonitorSysGenServerDeviceReportService deviceReportService;

    @Autowired
    private MonitorSysGenServerService serverService;

    @Autowired
    private MonitorSysGenServerSettingService serverSettingService;

    @Autowired
    private UnifiedAlertService unifiedAlertService;

    @Autowired(required = false)
    private AgentDataStorageService agentDataStorageService;


    @PostConstruct
    public void init() {
        log.info("[SyncConfig] 注册消息处理器");

        // 注册 Job 结果处理器
        syncServer.registerHandler(TOPIC_JOB_RESULT, createHandler(TOPIC_JOB_RESULT, this::handleJobResult));

        // 注册设备指标处理器
        syncServer.registerHandler(TOPIC_DEVICE_METRICS, createHandler(TOPIC_DEVICE_METRICS, this::handleDeviceMetrics));

        // 注册设备信息处理器
        syncServer.registerHandler(TOPIC_DEVICE_INFO, createHandler(TOPIC_DEVICE_INFO, this::handleDeviceInfo));

        // 注册应用信息上报处理器
        syncServer.registerHandler(TOPIC_APP_REPORT, createHandler(TOPIC_APP_REPORT, this::handleAppReport));

        // 注册 Agent 数据处理器（需要 AgentDataStorageService）
        if (agentDataStorageService != null) {
            syncServer.registerHandler(TOPIC_AGENT_TRACE, createHandler(TOPIC_AGENT_TRACE, this::handleAgentTrace));
            syncServer.registerHandler(TOPIC_AGENT_SERVER, createHandler(TOPIC_AGENT_SERVER, this::handleAgentServer));
            syncServer.registerHandler(TOPIC_AGENT_FD, createHandler(TOPIC_AGENT_FD, this::handleAgentFileHandle));
            log.info("[SyncConfig] Agent 数据处理器注册完成 (Trace, Server, FileHandle)");
        } else {
            log.warn("[SyncConfig] AgentDataStorageService 未配置，跳过 Agent 数据处理器注册");
        }


        log.info("[SyncConfig] 消息处理器注册完成 (JobResult, DeviceMetrics, DeviceInfo, AppReport)");
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
        return MapUtils.getString(data, key);

    }

    private int getInt(Map<String, Object> data, String key) {
        return MapUtils.getIntValue(data, key);
    }

    private long getLong(Map<String, Object> data, String key) {
        return MapUtils.getLong(data, key);
    }

    private Double getDouble(Map<String, Object> data, String key) {
        return MapUtils.getDouble(data, key);
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
     * <p>
     * 支持客户端指定上报的指标类型
     * 会检查服务器是否启用节点上报模式
     * </p>
     *
     * @param sessionId 会话ID
     * @param data      上报数据
     * @author CH
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    private void handleDeviceMetrics(String sessionId, Map<String, Object> data) {
        String ipAddress = getString(data, "ipAddress");
        String deviceId = getString(data, "deviceId");

        // 获取客户端上报的指标类型列表
        List<String> reportedMetrics = (List<String>) data.get("reportedMetrics");
        long reportTime = getLong(data, "reportTime");

        log.debug("[DeviceMetrics] 收到设备指标: sessionId={}, ip={}, metrics={}, reportTime={}",
                sessionId, ipAddress, reportedMetrics, reportTime);

        try {
            if (ipAddress == null || ipAddress.isEmpty()) {
                log.warn("[DeviceMetrics] 设备IP地址不能为空");
                return;
            }

            // 根据 IP 查找服务器配置
            MonitorSysGenServer server = serverService.findByIpAddress(ipAddress);
            if (server == null) {
                log.debug("[DeviceMetrics] 未找到服务器配置，跳过上报模式检查: ip={}", ipAddress);
            } else {
                // 检查服务器是否启用节点上报模式
                try {
                    ReturnResult<MonitorSysGenServerSetting> settingResult =
                            serverSettingService.getOrCreateSetting(server.getMonitorSysGenServerId());

                    if (settingResult.isOk() && settingResult.getData() != null) {
                        MonitorSysGenServerSetting setting = settingResult.getData();
                        String reportMethod = setting.getMonitorSysGenServerSettingDataReportMethod();
                        // 只允许 NODE 模式的节点上报
                        if (!"NODE".equals(reportMethod)) {
                            log.warn("[DeviceMetrics] 服务器未配置为节点上报模式，拒绝上报: ip={}, serverId={}, reportMethod={}",
                                    ipAddress, server.getMonitorSysGenServerId(), reportMethod);
                            return;
                        }
                    }
                } catch (Exception e) {
                    log.error("[DeviceMetrics] 获取服务器配置失败: ip={}", ipAddress, e);
                }
            }

            // 转换为 DeviceMetrics 对象（仅转换上报的指标）
            DeviceMetrics metrics = convertToDeviceMetrics(data, reportedMetrics);

            // 使用 deviceReportService 处理
            // 内部会：1. 更新统计 2. 根据IP查找服务器 3. 保存到Redis 4. WebSocket推送
            var result = deviceReportService.handleDeviceMetrics(metrics);

            if (result.isOk()) {
                log.debug("[DeviceMetrics] 处理成功: ip={}, metrics={}", ipAddress, reportedMetrics);
                
                // 如果数据处理成功且找到了服务器，进行告警检测
                if (server != null) {
                    try {
                        // 使用统一告警服务检测和推送节点上报数据的告警
                        ReturnResult<Boolean> alertResult = 
                            unifiedAlertService.checkAndPushApiReportAlerts(server.getMonitorSysGenServerId(), data);
                        
                        if (alertResult.isOk() && alertResult.getData() != null && alertResult.getData()) {
                            log.info("[DeviceMetrics] 节点上报数据检测到告警并已推送: ip={}, serverId={}", 
                                    ipAddress, server.getMonitorSysGenServerId());
                        }
                    } catch (Exception alertEx) {
                        // 告警检测失败不影响数据上报的成功
                        log.error("[DeviceMetrics] 节点上报数据告警检测失败: ip={}, serverId={}", 
                                ipAddress, server.getMonitorSysGenServerId(), alertEx);
                    }
                }
            } else {
                log.warn("[DeviceMetrics] 处理失败: ip={}, error={}", ipAddress, result.getMsg());
            }

        } catch (Exception e) {
            log.error("[DeviceMetrics] 处理设备指标失败: deviceId={}, ip={}", deviceId, ipAddress, e);
        }
    }

    /**
     * 转换为 DeviceMetrics 对象
     *
     * @param data 上报数据
     * @return DeviceMetrics
     * @author CH
     * @since 1.0.0
     */
    private DeviceMetrics convertToDeviceMetrics(Map<String, Object> data) {
        return convertToDeviceMetrics(data, null);
    }

    /**
     * 转换为 DeviceMetrics 对象（仅转换指定的指标类型）
     * <p>
     * 支持新的数据结构：
     * - CPU: cpuUsage, cpuUser, cpuSystem, cpuWait, cpuTime
     * - 内存: memory.memoryTotal, memory.memoryUsed, memory.memoryUsage, memory.memoryTime
     * - 磁盘: disks[].name, total, used, free, usage, type, time
     * - 网卡: networks[].name, receiveBytes, transmitBytes, time
     * </p>
     *
     * @param data            上报数据
     * @param reportedMetrics 上报的指标类型列表（null 表示全部）
     * @return DeviceMetrics
     * @author CH
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    private DeviceMetrics convertToDeviceMetrics(Map<String, Object> data, List<String> reportedMetrics) {
        DeviceMetrics metrics = new DeviceMetrics();

        // IP 地址（始终设置）
        metrics.setIpAddress(getString(data, "ipAddress"));

        // 操作系统信息（更新服务器配置用）
        metrics.setOsName(getString(data, "osName"));
        metrics.setOsVersion(getString(data, "osVersion"));
        metrics.setOsArch(getString(data, "osArch"));

        // 根据上报的指标类型设置数据
        boolean hasAll = reportedMetrics == null || reportedMetrics.isEmpty();

        // CPU（从顶层字段读取）
        if (hasAll || reportedMetrics.contains("cpu")) {
            metrics.setCpuUsage(getDouble(data, "cpuUsage"));
        }

        // 内存（从嵌套对象读取）
        if (hasAll || reportedMetrics.contains("memory")) {
            Object memoryObj = data.get("memory");
            if (memoryObj instanceof Map) {
                Map<String, Object> memoryData = (Map<String, Object>) memoryObj;
                metrics.setTotalMemory(MapUtils.getLong(memoryData, "memoryTotal"));
                metrics.setUsedMemory(MapUtils.getLong(memoryData, "memoryUsed"));
                metrics.setAvailableMemory(MapUtils.getLong(memoryData, "memoryFree"));
                metrics.setMemoryUsage(MapUtils.getDouble(memoryData, "memoryUsage"));
            } else {
                // 兼容旧格式
                metrics.setMemoryUsage(getDouble(data, "memoryUsage"));
            }
        }

        // 磁盘（优先使用汇总字段，其次从列表计算）
        if (hasAll || reportedMetrics.contains("disk")) {
            // 优先使用客户端计算的汇总数据
            Long diskTotal = getLong(data, "diskTotal");
            Long diskUsed = getLong(data, "diskUsed");
            Long diskFree = getLong(data, "diskFree");

            if (diskTotal != null && diskTotal > 0) {
                metrics.setTotalDisk(diskTotal);
                metrics.setUsedDisk(diskUsed);
                metrics.setAvailableDisk(diskFree);
                metrics.setDiskUsage(Math.round((double) diskUsed / diskTotal * 100 * 100.0) / 100.0);
            } else {
                // 从列表计算
                Object disksObj = data.get("disks");
                if (disksObj instanceof List) {
                    List<Map<String, Object>> diskList = (List<Map<String, Object>>) disksObj;
                    long totalDisk = 0;
                    long usedDisk = 0;
                    long freeDisk = 0;
                    for (Map<String, Object> disk : diskList) {
                        totalDisk += MapUtils.getLong(disk, "total", 0L);
                        usedDisk += MapUtils.getLong(disk, "used", 0L);
                        freeDisk += MapUtils.getLong(disk, "free", 0L);
                    }
                    metrics.setTotalDisk(totalDisk);
                    metrics.setUsedDisk(usedDisk);
                    metrics.setAvailableDisk(freeDisk);
                    if (totalDisk > 0) {
                        metrics.setDiskUsage(Math.round((double) usedDisk / totalDisk * 100 * 100.0) / 100.0);
                    }
                } else {
                    // 兼容旧格式
                    metrics.setDiskUsage(getDouble(data, "diskUsage"));
                }
            }
        }

        // 网卡（从列表读取，计算总量）
        if (hasAll || reportedMetrics.contains("network")) {
            Object networksObj = data.get("networks");
            if (networksObj instanceof List) {
                List<Map<String, Object>> networkList = (List<Map<String, Object>>) networksObj;
                long totalIn = 0;
                long totalOut = 0;
                for (Map<String, Object> net : networkList) {
                    totalIn += MapUtils.getLong(net, "receiveBytes", 0L);
                    totalOut += MapUtils.getLong(net, "transmitBytes", 0L);
                }
                metrics.setNetworkInBytes(totalIn);
                metrics.setNetworkOutBytes(totalOut);
            } else {
                // 兼容旧格式
                metrics.setNetworkInBytes(getLong(data, "networkInBytes"));
                metrics.setNetworkOutBytes(getLong(data, "networkOutBytes"));
            }
        }

        metrics.setCollectTime(LocalDateTime.now());
        return metrics;
    }

    /**
     * 处理应用信息上报（定时覆盖更新）
     */
    private void handleAppReport(String sessionId, Map<String, Object> data) {
        try {
            ClientInfo clientInfo = syncServer.getAllClientsRaw().get(sessionId);
            if (clientInfo == null) {
                log.warn("[AppReport] 未找到客户端会话: sessionId={}", sessionId);
                return;
            }

            // 覆盖更新客户端信息
            // Spring 应用信息
            clientInfo.setClientApplicationName(getString(data, "applicationName"));
            clientInfo.setClientApplicationActive(getString(data, "applicationActive"));
            clientInfo.setClientContextPath(getString(data, "contextPath"));
            clientInfo.setClientPort(getInt(data, "serverPort"));
            clientInfo.setClientUrl(getString(data, "serviceUrl"));

            // 网络信息
            clientInfo.setClientIpAddress(getString(data, "ipAddress"));
            clientInfo.setClientHostname(getString(data, "hostname"));

            // 系统信息（核心）
            clientInfo.setClientOsName(getString(data, "osName"));
            clientInfo.setClientJavaVersion(getString(data, "javaVersion"));
            clientInfo.setClientPid(getLong(data, "pid"));

            // 时间信息
            long startTime = getLong(data, "startTime");
            clientInfo.setClientStartTime(startTime);
            clientInfo.setClientUptime(System.currentTimeMillis() - startTime);
            clientInfo.setClientLastHeartbeatTime(System.currentTimeMillis());
            clientInfo.setClientOnline(true);

            // 详细信息存到 metadata
            if (clientInfo.getClientMetadata() == null) {
                clientInfo.setClientMetadata(new java.util.HashMap<>());
            }
            Map<String, Object> metadata = clientInfo.getClientMetadata();
            metadata.put("activeProfiles", getString(data, "activeProfiles"));
            metadata.put("osVersion", getString(data, "osVersion"));
            metadata.put("osArch", getString(data, "osArch"));
            metadata.put("jvmName", getString(data, "jvmName"));
            metadata.put("jvmVersion", getString(data, "jvmVersion"));
            metadata.put("heapUsed", getLong(data, "heapUsed"));
            metadata.put("heapMax", getLong(data, "heapMax"));
            metadata.put("threadCount", getInt(data, "threadCount"));

            log.debug("[AppReport] 更新应用信息: app={}, address={}:{}",
                    clientInfo.getClientApplicationName(), clientInfo.getClientIpAddress(), clientInfo.getClientPort());

        } catch (Exception e) {
            log.error("[AppReport] 处理应用信息上报失败: sessionId={}", sessionId, e);
        }
    }

    /**
     * 创建 SyncMessageHandler 实例
     *
     * @param name    处理器名称
     * @param handler 消息处理函数
     * @return SyncMessageHandler 实例
     */
    private SyncMessageHandler createHandler(String name, BiConsumer<String, Map<String, Object>> handler) {
        return new SyncMessageHandler() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public Object handle(String topic, String sessionId, Map<String, Object> data) {
                handler.accept(sessionId, data);
                return null;
            }
        };
    }

    // ==================== Agent 数据处理方法 ====================

    /**
     * 处理 Agent 链路追踪上报
     * <p>
     * 支持新的 ReportData 结构，包含 ip、port、profile 等服务信息
     * </p>
     *
     * @param sessionId 会话ID
     * @param data      链路追踪数据（ReportData 结构）
     */
    @SuppressWarnings("unchecked")
    private void handleAgentTrace(String sessionId, Map<String, Object> data) {
        try {
            // 提取 ReportData 中的服务信息
            String ip = MapUtils.getString(data, "ip");
            Integer port = MapUtils.getInteger(data, "port");
            String profile = MapUtils.getString(data, "profile");
            String applicationName = MapUtils.getString(data, "applicationName");
            
            // 提取实际的链路数据
            Object actualData = data.get("data");
            Map<String, Object> traceData;
            if (actualData instanceof Map) {
                traceData = (Map<String, Object>) actualData;
            } else {
                // 兼容旧版格式，直接使用整个 data
                traceData = data;
            }
            
            // 将服务信息添加到链路数据中
            if (ip != null) {
                traceData.put("sourceIp", ip);
            }
            if (port != null) {
                traceData.put("sourcePort", port);
            }
            if (profile != null) {
                traceData.put("profile", profile);
            }
            if (applicationName != null) {
                traceData.put("applicationName", applicationName);
            }
            
            log.debug("[AgentTrace] 收到链路追踪: sessionId={}, app={}, ip={}:{}, linkId={}", 
                    sessionId, applicationName, ip, port, MapUtils.getString(traceData, "linkId"));

            var result = agentDataStorageService.storeTraceForAgent(sessionId, traceData);
            if (result.isOk()) {
                log.debug("[AgentTrace] 链路追踪存储成功: sessionId={}, app={}", sessionId, applicationName);
            } else {
                log.warn("[AgentTrace] 链路追踪存储失败: sessionId={}, error={}", sessionId, result.getMsg());
            }
        } catch (Exception e) {
            log.error("[AgentTrace] 处理链路追踪失败: sessionId={}", sessionId, e);
        }
    }

    /**
     * 处理 Agent 服务实例上报
     * <p>
     * 支持新的 ReportData 结构，包含 ip、port、profile 等服务信息
     * </p>
     *
     * @param sessionId    会话ID
     * @param data         服务实例数据（ReportData 结构）
     */
    @SuppressWarnings("unchecked")
    private void handleAgentServer(String sessionId, Map<String, Object> data) {
        try {
            // 提取 ReportData 中的服务信息
            String ip = MapUtils.getString(data, "ip");
            Integer port = MapUtils.getInteger(data, "port");
            String applicationName = MapUtils.getString(data, "applicationName");
            
            // 提取实际的服务实例数据
            Object actualData = data.get("data");
            Map<String, Object> instanceData;
            if (actualData instanceof Map) {
                instanceData = (Map<String, Object>) actualData;
            } else {
                // 兼容旧版格式
                instanceData = data;
            }
            
            // 将服务信息添加到实例数据中
            if (ip != null && !instanceData.containsKey("sourceHost")) {
                instanceData.put("sourceHost", ip);
            }
            if (port != null && !instanceData.containsKey("sourcePort")) {
                instanceData.put("sourcePort", port);
            }
            if (applicationName != null && !instanceData.containsKey("sourceName")) {
                instanceData.put("sourceName", applicationName);
            }
            
            String sourceHost = MapUtils.getString(instanceData, "sourceHost");
            String targetHost = MapUtils.getString(instanceData, "targetHost");
            log.debug("[AgentServer] 收到服务实例: sessionId={}, app={}, {} -> {}", 
                    sessionId, applicationName, sourceHost, targetHost);

            var result = agentDataStorageService.storeServiceInstanceForAgent(sessionId, instanceData);
            if (result.isOk()) {
                log.debug("[AgentServer] 服务实例存储成功: sessionId={}, app={}", sessionId, applicationName);
            } else {
                log.warn("[AgentServer] 服务实例存储失败: sessionId={}, error={}", sessionId, result.getMsg());
            }
        } catch (Exception e) {
            log.error("[AgentServer] 处理服务实例失败: sessionId={}", sessionId, e);
        }
    }

    /**
     * 处理 Agent 文件句柄上报
     * <p>
     * 支持新的 ReportData 结构，包含 ip、port、profile 等服务信息
     * </p>
     *
     * @param sessionId  会话ID
     * @param data       文件句柄数据（ReportData 结构）
     */
    @SuppressWarnings("unchecked")
    private void handleAgentFileHandle(String sessionId, Map<String, Object> data) {
        try {
            // 提取 ReportData 中的服务信息
            String ip = MapUtils.getString(data, "ip");
            Integer port = MapUtils.getInteger(data, "port");
            String applicationName = MapUtils.getString(data, "applicationName");
            
            // 提取实际的文件句柄数据
            Object actualData = data.get("data");
            Map<String, Object> handleData;
            if (actualData instanceof Map) {
                handleData = (Map<String, Object>) actualData;
            } else {
                // 兼容旧版格式
                handleData = data;
            }
            
            // 将服务信息添加到句柄数据中
            if (ip != null) {
                handleData.put("ipAddress", ip);
            }
            if (port != null) {
                handleData.put("port", port);
            }
            if (applicationName != null) {
                handleData.put("applicationName", applicationName);
            }
            
            String filePath = MapUtils.getString(handleData, "filePath");
            int fd = MapUtils.getIntValue(handleData, "fd");
            log.debug("[AgentFD] 收到文件句柄: sessionId={}, app={}, fd={}, path={}", 
                    sessionId, applicationName, fd, filePath);

            var result = agentDataStorageService.storeFileHandleForAgent(sessionId, handleData);
            if (result.isOk()) {
                log.debug("[AgentFD] 文件句柄存储成功: sessionId={}, app={}", sessionId, applicationName);
            } else {
                log.warn("[AgentFD] 文件句柄存储失败: sessionId={}, error={}", sessionId, result.getMsg());
            }
        } catch (Exception e) {
            log.error("[AgentFD] 处理文件句柄失败: sessionId={}", sessionId, e);
        }
    }

}
