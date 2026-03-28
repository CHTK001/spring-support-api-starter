package com.chua.starter.oshi.performance;

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

    private static final String VALUE_TYPE_BOOL = "BOOL";
    private static final String VALUE_TYPE_NUMBER = "NUMBER";
    private static final String VALUE_TYPE_TEXT = "TEXT";

    private SystemPerformanceSettings() {
    }

    public static final String SETTING_GROUP = "default";
    public static final String SETTING_CONFIG = "performance-monitor";
    public static final String SOCKET_TOPIC = "system:performance:metrics";

    public static final String MONITOR_OPEN = "PerformanceMonitorOpen";
    public static final String SOCKET_PUSH_OPEN = "PerformanceMonitorSocketPushOpen";
    public static final String REFRESH_INTERVAL_SECONDS = "PerformanceMonitorRefreshIntervalSeconds";
    public static final String SHOW_FPS_MONITOR = "ShowFpsMonitor";
    public static final String SHOW_CPU_MONITOR = "ShowCpuMonitor";
    public static final String SHOW_MEMORY_MONITOR = "ShowMemoryMonitor";
    public static final String SHOW_BANDWIDTH_MONITOR = "ShowBandwidthMonitor";
    public static final String SHOW_BATTERY_MONITOR = "ShowBatteryMonitor";
    public static final String SHOW_BLUETOOTH_MONITOR = "ShowBluetoothMonitor";
    public static final String SHOW_SCREEN_MONITOR = "ShowScreenMonitor";
    public static final String MONITOR_POSITION = "PerformanceMonitorPosition";
    public static final String MONITOR_MODE = "PerformanceMonitorMode";
    public static final String MONITOR_LAYOUT = "PerformanceMonitorLayout";
    public static final String MONITOR_DIRECTION = "PerformanceMonitorDirection";

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
        definitions.add(new SettingDefinition(MONITOR_OPEN, "false", VALUE_TYPE_BOOL, 20, "系统性能监控总开关", SETTING_CONFIG));
        definitions.add(new SettingDefinition(SOCKET_PUSH_OPEN, "false", VALUE_TYPE_BOOL, 21, "性能监控Socket推送开关", SETTING_CONFIG));
        definitions.add(new SettingDefinition(REFRESH_INTERVAL_SECONDS, "5", VALUE_TYPE_NUMBER, 22, "性能监控刷新间隔(秒)", SETTING_CONFIG));
        for (MetricDefinition metric : METRICS) {
            definitions.add(new SettingDefinition(
                    metric.settingName(),
                    "true",
                    VALUE_TYPE_BOOL,
                    metric.sort(),
                    metric.label() + "开关",
                    SETTING_CONFIG + ":" + metric.key()
            ));
        }
        definitions.add(new SettingDefinition(SHOW_FPS_MONITOR, "false", VALUE_TYPE_BOOL, 30, "前端FPS监控显示开关", SETTING_CONFIG + ":frontend:fps"));
        definitions.add(new SettingDefinition(SHOW_CPU_MONITOR, "false", VALUE_TYPE_BOOL, 31, "前端CPU监控显示开关", SETTING_CONFIG + ":frontend:cpu"));
        definitions.add(new SettingDefinition(SHOW_MEMORY_MONITOR, "false", VALUE_TYPE_BOOL, 32, "前端内存监控显示开关", SETTING_CONFIG + ":frontend:memory"));
        definitions.add(new SettingDefinition(SHOW_BANDWIDTH_MONITOR, "false", VALUE_TYPE_BOOL, 33, "前端带宽监控显示开关", SETTING_CONFIG + ":frontend:bandwidth"));
        definitions.add(new SettingDefinition(SHOW_BATTERY_MONITOR, "false", VALUE_TYPE_BOOL, 34, "前端电池监控显示开关", SETTING_CONFIG + ":frontend:battery"));
        definitions.add(new SettingDefinition(SHOW_BLUETOOTH_MONITOR, "false", VALUE_TYPE_BOOL, 35, "前端蓝牙监控显示开关", SETTING_CONFIG + ":frontend:bluetooth"));
        definitions.add(new SettingDefinition(SHOW_SCREEN_MONITOR, "false", VALUE_TYPE_BOOL, 36, "前端屏幕监控显示开关", SETTING_CONFIG + ":frontend:screen"));
        definitions.add(new SettingDefinition(MONITOR_POSITION, "top-left", VALUE_TYPE_TEXT, 37, "前端性能监控位置", SETTING_CONFIG + ":frontend:layout"));
        definitions.add(new SettingDefinition(MONITOR_MODE, "minimal", VALUE_TYPE_TEXT, 38, "前端性能监控模式", SETTING_CONFIG + ":frontend:layout"));
        definitions.add(new SettingDefinition(MONITOR_LAYOUT, "merged", VALUE_TYPE_TEXT, 39, "前端性能监控布局", SETTING_CONFIG + ":frontend:layout"));
        definitions.add(new SettingDefinition(MONITOR_DIRECTION, "auto", VALUE_TYPE_TEXT, 40, "前端性能监控方向", SETTING_CONFIG + ":frontend:layout"));
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
