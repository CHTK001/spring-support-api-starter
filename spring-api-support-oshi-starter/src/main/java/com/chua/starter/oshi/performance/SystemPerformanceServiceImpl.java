package com.chua.starter.oshi.performance;

import com.chua.common.support.text.json.Json;
import com.chua.socket.support.session.SocketSessionTemplate;
import com.chua.starter.entity.service.SysSetting;
import com.chua.starter.oshi.utils.LocalSystemDataCollector;
import com.chua.starter.service.SysSettingService;
import com.chua.starter.service.pojo.setting.SysSettingUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统性能监控服务实现。
 *
 * @author CH
 * @since 2026/3/28
 */
@Slf4j
@RequiredArgsConstructor
public class SystemPerformanceServiceImpl implements SystemPerformanceService {

    private final SysSettingService sysSettingService;
    private final LocalSystemDataCollector localSystemDataCollector;
    private final ObjectProvider<SocketSessionTemplate> socketSessionTemplateProvider;

    @Override
    public SystemPerformanceConfig getConfig() {
        Map<String, String> settingValues = loadSettingValues();
        SystemPerformanceConfig config = new SystemPerformanceConfig();
        config.setEnabled(readBoolean(settingValues, SystemPerformanceSettings.MONITOR_OPEN));
        config.setSocketPushEnabled(readBoolean(settingValues, SystemPerformanceSettings.SOCKET_PUSH_OPEN));
        config.setRefreshIntervalSeconds(sanitizeRefreshInterval(readInt(settingValues, SystemPerformanceSettings.REFRESH_INTERVAL_SECONDS)));
        config.setWebsocketTopic(SystemPerformanceSettings.SOCKET_TOPIC);

        List<SystemPerformanceMetricToggle> toggles = new ArrayList<>();
        for (SystemPerformanceSettings.MetricDefinition metric : SystemPerformanceSettings.metrics()) {
            SystemPerformanceMetricToggle toggle = new SystemPerformanceMetricToggle();
            toggle.setKey(metric.key());
            toggle.setLabel(metric.label());
            toggle.setDescription(metric.description());
            toggle.setSettingName(metric.settingName());
            toggle.setSort(metric.sort());
            toggle.setEnabled(readBoolean(settingValues, metric.settingName()));
            toggles.add(toggle);
        }
        config.setIndicators(toggles);
        return config;
    }

    @Override
    public SystemPerformanceSnapshot getSnapshot(boolean broadcast) {
        return collectSnapshot(broadcast, false);
    }

    @Override
    public SystemPerformanceSnapshot broadcastSnapshot() {
        return collectSnapshot(true, true);
    }

    private SystemPerformanceSnapshot collectSnapshot(boolean broadcast, boolean forceBroadcast) {
        SystemPerformanceConfig config = getConfig();
        SystemPerformanceSnapshot snapshot = new SystemPerformanceSnapshot();
        snapshot.setEnabled(config.getEnabled());
        snapshot.setSocketPushEnabled(config.getSocketPushEnabled());
        snapshot.setRefreshIntervalSeconds(config.getRefreshIntervalSeconds());
        snapshot.setWebsocketTopic(config.getWebsocketTopic());
        snapshot.setTimestamp(System.currentTimeMillis());
        snapshot.setActiveIndicators(resolveActiveIndicators(config));

        if (!Boolean.TRUE.equals(config.getEnabled())) {
            snapshot.setHealthStatus("disabled");
            snapshot.setMessage("系统性能监控未开启");
            if (broadcast) {
                pushSnapshot(snapshot, forceBroadcast);
            }
            return snapshot;
        }

        Map<String, Object> rawData = localSystemDataCollector.collectSystemData();
        String healthStatus = resolveHealthStatus(rawData);
        snapshot.setHealthStatus(healthStatus);

        Map<String, Object> sections = new LinkedHashMap<>();
        sections.put("overview", buildOverview(rawData, snapshot.getActiveIndicators(), healthStatus));

        for (SystemPerformanceSettings.MetricDefinition metric : SystemPerformanceSettings.metrics()) {
            if (!Boolean.TRUE.equals(isMetricEnabled(config, metric.key()))) {
                continue;
            }
            Object section = buildSection(metric.key(), rawData);
            if (section != null) {
                sections.put(metric.key(), section);
            }
        }

        snapshot.setSections(sections);
        if (broadcast) {
            pushSnapshot(snapshot, forceBroadcast);
        }
        return snapshot;
    }

    private Boolean isMetricEnabled(SystemPerformanceConfig config, String key) {
        return config.getIndicators().stream()
                .filter(item -> key.equals(item.getKey()))
                .map(SystemPerformanceMetricToggle::getEnabled)
                .findFirst()
                .orElse(Boolean.FALSE);
    }

    private List<String> resolveActiveIndicators(SystemPerformanceConfig config) {
        return config.getIndicators().stream()
                .filter(item -> Boolean.TRUE.equals(item.getEnabled()))
                .sorted((left, right) -> Integer.compare(left.getSort(), right.getSort()))
                .map(SystemPerformanceMetricToggle::getKey)
                .toList();
    }

    private Object buildSection(String key, Map<String, Object> rawData) {
        return switch (key) {
            case "system" -> buildSystem(rawData);
            case "cpu" -> buildCpu(rawData);
            case "memory" -> buildMemory(rawData);
            case "disk" -> buildDisk(rawData);
            case "network" -> buildNetwork(rawData);
            case "jvm" -> buildJvm(rawData);
            case "process" -> buildProcess(rawData);
            default -> null;
        };
    }

    private Map<String, Object> buildOverview(Map<String, Object> rawData, List<String> activeIndicators, String healthStatus) {
        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("healthStatus", healthStatus);
        overview.put("healthy", !"critical".equals(healthStatus));
        overview.put("activeIndicators", activeIndicators);
        overview.put("cpuUsage", asDouble(rawData.get("cpuUsage")));
        overview.put("memoryUsagePercent", asDouble(rawData.get("memoryUsagePercent")));
        overview.put("diskUsagePercent", resolveMaxDiskUsage(rawData));
        overview.put("processCount", asInteger(rawData.get("processCount")));
        overview.put("threadCount", asInteger(rawData.get("threadCount")));
        overview.put("systemUptime", asLong(rawData.get("systemUptime")));
        overview.put("timestamp", System.currentTimeMillis());
        return overview;
    }

    private Map<String, Object> buildSystem(Map<String, Object> rawData) {
        Map<String, Object> system = new LinkedHashMap<>();
        system.put("osName", rawData.get("osName"));
        system.put("osVersion", rawData.get("osVersion"));
        system.put("osArch", rawData.get("osArch"));
        system.put("osManufacturer", rawData.get("osManufacturer"));
        system.put("osFamily", rawData.get("osFamily"));
        system.put("systemBootTime", rawData.get("systemBootTime"));
        system.put("systemUptime", rawData.get("systemUptime"));
        system.put("computerManufacturer", rawData.get("computerManufacturer"));
        system.put("computerModel", rawData.get("computerModel"));
        system.put("computerSerialNumber", rawData.get("computerSerialNumber"));
        return system;
    }

    private Map<String, Object> buildCpu(Map<String, Object> rawData) {
        Map<String, Object> cpu = new LinkedHashMap<>();
        cpu.put("name", rawData.get("cpuName"));
        cpu.put("family", rawData.get("cpuFamily"));
        cpu.put("model", rawData.get("cpuModel"));
        cpu.put("vendor", rawData.get("cpuVendor"));
        cpu.put("logicalProcessors", rawData.get("cpuLogicalProcessors"));
        cpu.put("physicalProcessors", rawData.get("cpuPhysicalProcessors"));
        cpu.put("maxFreq", rawData.get("cpuMaxFreq"));
        cpu.put("usage", rawData.get("cpuUsage"));
        cpu.put("load1min", rawData.get("systemLoad1min"));
        cpu.put("load5min", rawData.get("systemLoad5min"));
        cpu.put("load15min", rawData.get("systemLoad15min"));
        return cpu;
    }

    private Map<String, Object> buildMemory(Map<String, Object> rawData) {
        Map<String, Object> memory = new LinkedHashMap<>();
        memory.put("total", rawData.get("totalMemory"));
        memory.put("used", rawData.get("usedMemory"));
        memory.put("available", rawData.get("availableMemory"));
        memory.put("usagePercent", rawData.get("memoryUsagePercent"));
        memory.put("swapTotal", rawData.get("swapTotal"));
        memory.put("swapUsed", rawData.get("swapUsed"));
        memory.put("swapUsagePercent", rawData.get("swapUsagePercent"));
        return memory;
    }

    private Map<String, Object> buildDisk(Map<String, Object> rawData) {
        Map<String, Object> disk = new LinkedHashMap<>();
        disk.put("maxUsagePercent", resolveMaxDiskUsage(rawData));
        disk.put("disks", rawData.get("disks"));
        disk.put("fileSystems", rawData.get("fileSystems"));
        disk.put("partitions", rawData.get("diskPartitions"));
        return disk;
    }

    private Map<String, Object> buildNetwork(Map<String, Object> rawData) {
        Map<String, Object> network = new LinkedHashMap<>();
        List<Map<String, Object>> networks = castList(rawData.get("networks"));
        network.put("interfaceCount", networks.size());
        network.put("interfaces", networks);
        return network;
    }

    private Map<String, Object> buildJvm(Map<String, Object> rawData) {
        Map<String, Object> jvm = new LinkedHashMap<>();
        jvm.put("name", rawData.get("jvmName"));
        jvm.put("version", rawData.get("jvmVersion"));
        jvm.put("vendor", rawData.get("jvmVendor"));
        jvm.put("uptime", rawData.get("jvmUptime"));
        jvm.put("startTime", rawData.get("jvmStartTime"));
        jvm.put("heapUsed", rawData.get("jvmHeapUsed"));
        jvm.put("heapMax", rawData.get("jvmHeapMax"));
        jvm.put("heapCommitted", rawData.get("jvmHeapCommitted"));
        jvm.put("nonHeapUsed", rawData.get("jvmNonHeapUsed"));
        jvm.put("nonHeapMax", rawData.get("jvmNonHeapMax"));
        return jvm;
    }

    private Map<String, Object> buildProcess(Map<String, Object> rawData) {
        Map<String, Object> process = new LinkedHashMap<>();
        process.put("processCount", rawData.get("processCount"));
        process.put("threadCount", rawData.get("threadCount"));
        process.put("topProcesses", rawData.get("topProcesses"));
        return process;
    }

    private String resolveHealthStatus(Map<String, Object> rawData) {
        double cpuUsage = asDouble(rawData.get("cpuUsage"));
        double memoryUsage = asDouble(rawData.get("memoryUsagePercent"));
        double diskUsage = resolveMaxDiskUsage(rawData);
        if (cpuUsage >= 90 || memoryUsage >= 90 || diskUsage >= 95) {
            return "critical";
        }
        if (cpuUsage >= 75 || memoryUsage >= 75 || diskUsage >= 85) {
            return "warning";
        }
        return "healthy";
    }

    private double resolveMaxDiskUsage(Map<String, Object> rawData) {
        double max = 0D;
        for (Map<String, Object> fileSystem : castList(rawData.get("fileSystems"))) {
            max = Math.max(max, asDouble(fileSystem.get("usagePercent")));
        }
        return max;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> castList(Object value) {
        if (value instanceof List<?> list) {
            List<Map<String, Object>> result = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    result.add((Map<String, Object>) map);
                }
            }
            return result;
        }
        return List.of();
    }

    private double asDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value != null) {
            try {
                return Double.parseDouble(value.toString());
            } catch (NumberFormatException ignored) {
                return 0D;
            }
        }
        return 0D;
    }

    private int asInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value != null) {
            try {
                return Integer.parseInt(value.toString());
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return 0;
    }

    private long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value != null) {
            try {
                return Long.parseLong(value.toString());
            } catch (NumberFormatException ignored) {
                return 0L;
            }
        }
        return 0L;
    }

    private int sanitizeRefreshInterval(int refreshInterval) {
        if (refreshInterval < 1) {
            return 5;
        }
        return Math.min(refreshInterval, 3600);
    }

    private void pushSnapshot(SystemPerformanceSnapshot snapshot, boolean forceBroadcast) {
        if (!forceBroadcast && !Boolean.TRUE.equals(snapshot.getSocketPushEnabled())) {
            return;
        }
        SocketSessionTemplate socketSessionTemplate = socketSessionTemplateProvider.getIfAvailable();
        if (socketSessionTemplate == null) {
            log.debug("SocketSessionTemplate 未初始化，跳过性能快照推送");
            return;
        }
        socketSessionTemplate.send(SystemPerformanceSettings.SOCKET_TOPIC, Json.toJson(snapshot));
    }

    private Map<String, String> loadSettingValues() {
        List<SysSetting> settings = sysSettingService.list(
                new SysSettingUpdate(SystemPerformanceSettings.SETTING_GROUP),
                Collections.emptySet()
        );
        Map<String, String> values = new LinkedHashMap<>();
        for (SysSetting setting : settings) {
            String name = setting.getSysSettingName();
            if (SystemPerformanceSettings.definition(name) == null) {
                continue;
            }
            values.put(name, setting.getSysSettingValue());
        }
        return values;
    }

    private boolean readBoolean(Map<String, String> values, String name) {
        return Boolean.parseBoolean(readString(values, name));
    }

    private int readInt(Map<String, String> values, String name) {
        String value = readString(values, name);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return Integer.parseInt(SystemPerformanceSettings.definition(name).defaultValue());
        }
    }

    private String readString(Map<String, String> values, String name) {
        String value = values.get(name);
        if (value != null) {
            return value;
        }
        SystemPerformanceSettings.SettingDefinition definition = SystemPerformanceSettings.definition(name);
        return definition == null ? null : definition.defaultValue();
    }
}
