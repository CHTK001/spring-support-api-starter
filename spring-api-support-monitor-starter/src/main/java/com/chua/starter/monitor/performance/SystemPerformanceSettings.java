package com.chua.starter.monitor.performance;

import com.chua.common.support.core.utils.StringUtils;
import com.chua.starter.common.support.application.GlobalSettingFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统性能监控设置定义。
 *
 * @author CH
 * @since 2026/3/28
 */
public final class SystemPerformanceSettings {

    private SystemPerformanceSettings() {
    }

    public static final String SETTING_GROUP = "default";
    public static final String SETTING_CONFIG = "performance-monitor";
    public static final String SOCKET_TOPIC = "system:performance:metrics";

    public static final String MONITOR_OPEN = "PerformanceMonitorOpen";
    public static final String SOCKET_PUSH_OPEN = "PerformanceMonitorSocketPushOpen";
    public static final String REFRESH_INTERVAL_SECONDS = "PerformanceMonitorRefreshIntervalSeconds";

    private static final List<MetricDefinition> METRICS = List.of(
            new MetricDefinition("system", "PerformanceMonitorSystemOpen", "系统信息", "操作系统、硬件与启动信息", 23),
            new MetricDefinition("cpu", "PerformanceMonitorCpuOpen", "CPU指标", "CPU型号、核心数、使用率和负载", 24),
            new MetricDefinition("memory", "PerformanceMonitorMemoryOpen", "内存指标", "物理内存和交换分区使用情况", 25),
            new MetricDefinition("disk", "PerformanceMonitorDiskOpen", "磁盘指标", "磁盘、分区和文件系统容量情况", 26),
            new MetricDefinition("network", "PerformanceMonitorNetworkOpen", "网络指标", "网卡、吞吐和地址信息", 27),
            new MetricDefinition("jvm", "PerformanceMonitorJvmOpen", "JVM指标", "JVM版本、堆与非堆内存", 28),
            new MetricDefinition("process", "PerformanceMonitorProcessOpen", "进程指标", "进程数、线程数和热点进程", 29)
    );

    private static final List<SettingDefinition> DEFINITIONS;
    private static final Map<String, SettingDefinition> DEFINITION_MAP;

    static {
        List<SettingDefinition> definitions = new ArrayList<>();
        definitions.add(new SettingDefinition(MONITOR_OPEN, "false", "Boolean", 20, "系统性能监控总开关", SETTING_CONFIG));
        definitions.add(new SettingDefinition(SOCKET_PUSH_OPEN, "false", "Boolean", 21, "性能监控Socket推送开关", SETTING_CONFIG));
        definitions.add(new SettingDefinition(REFRESH_INTERVAL_SECONDS, "5", "Number", 22, "性能监控刷新间隔(秒)", SETTING_CONFIG));
        for (MetricDefinition metric : METRICS) {
            definitions.add(new SettingDefinition(
                    metric.settingName(),
                    "true",
                    "Boolean",
                    metric.sort(),
                    metric.label() + "开关",
                    SETTING_CONFIG + ":" + metric.key()
            ));
        }
        DEFINITIONS = List.copyOf(definitions);

        Map<String, SettingDefinition> definitionMap = new LinkedHashMap<>();
        for (SettingDefinition definition : DEFINITIONS) {
            definitionMap.put(definition.name(), definition);
        }
        DEFINITION_MAP = Map.copyOf(definitionMap);
    }

    public static List<SettingDefinition> definitions() {
        return DEFINITIONS;
    }

    public static List<MetricDefinition> metrics() {
        return METRICS;
    }

    public static SettingDefinition definition(String name) {
        return DEFINITION_MAP.get(name);
    }

    public static boolean readBoolean(String name) {
        return Boolean.parseBoolean(readString(name));
    }

    public static int readInt(String name) {
        String value = readString(name);
        if (StringUtils.isBlank(value)) {
            SettingDefinition definition = definition(name);
            return definition == null ? 0 : Integer.parseInt(definition.defaultValue());
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            SettingDefinition definition = definition(name);
            return definition == null ? 0 : Integer.parseInt(definition.defaultValue());
        }
    }

    public static String readString(String name) {
        SettingDefinition definition = definition(name);
        if (definition == null) {
            return null;
        }
        String value = GlobalSettingFactory.getInstance().get(SETTING_GROUP, name);
        if (StringUtils.isBlank(value)) {
            return definition.defaultValue();
        }
        return value;
    }

    public record SettingDefinition(
            String name,
            String defaultValue,
            String valueType,
            int sort,
            String remark,
            String config
    ) {
    }

    public record MetricDefinition(
            String key,
            String settingName,
            String label,
            String description,
            int sort
    ) {
    }
}
