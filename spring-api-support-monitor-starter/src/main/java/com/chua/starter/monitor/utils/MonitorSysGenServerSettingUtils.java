package com.chua.starter.monitor.utils;

import com.chua.common.support.json.Json;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.starter.pojo.FileManagementApiConfig;
import com.chua.starter.monitor.starter.enums.DataReportMethod;
import com.chua.starter.monitor.starter.enums.DockerConnectionType;
import com.chua.starter.monitor.starter.enums.ProxyType;
import com.chua.starter.monitor.starter.enums.AlertNotificationMethod;
import com.chua.starter.monitor.starter.enums.FileManagementMode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 服务器配置设置工具类 用于处理 MonitorSysGenServerSetting 实体的计算逻辑
 *
 * @author CH
 * @since 2024/12/26
 */
public class MonitorSysGenServerSettingUtils {

    /**
     * 判断是否启用监控
     */
    public static boolean isMonitorEnabled(MonitorSysGenServerSetting setting) {
        return setting != null && setting.getMonitorSysGenServerSettingMonitorEnabled() != null
                && setting.getMonitorSysGenServerSettingMonitorEnabled() == 1;
    }

    /**
     * 判断是否需要上报
     */
    public static boolean isReportEnabled(MonitorSysGenServerSetting setting) {
        return setting != null && setting.getMonitorSysGenServerSettingReportEnabled() != null
                && setting.getMonitorSysGenServerSettingReportEnabled() == 1;
    }

    /**
     * 判断是否启用性能优化建议
     */
    public static boolean isPerformanceSuggestionEnabled(MonitorSysGenServerSetting setting) {
        return setting != null && setting.getMonitorSysGenServerSettingPerformanceSuggestionEnabled() != null
                && setting.getMonitorSysGenServerSettingPerformanceSuggestionEnabled() == 1;
    }

    /**
     * 判断是否启用自动恢复通知
     */
    public static boolean isAutoRecoveryNotificationEnabled(MonitorSysGenServerSetting setting) {
        return setting != null && setting.getMonitorSysGenServerSettingAutoRecoveryNotificationEnabled() != null
                && setting.getMonitorSysGenServerSettingAutoRecoveryNotificationEnabled() == 1;
    }

    /**
     * 获取数据上报方式描述
     */
    public static String getDataReportMethodDesc(MonitorSysGenServerSetting setting) {
        if (setting == null || setting.getMonitorSysGenServerSettingDataReportMethod() == null) {
            return DataReportMethod.NONE.getDesc();
        }
        return DataReportMethod.fromCode(setting.getMonitorSysGenServerSettingDataReportMethod()).getDesc();
    }

    /**
     * 获取告警通知方式描述
     */
    public static String getAlertNotificationMethodDesc(MonitorSysGenServerSetting setting) {
        if (setting == null || setting.getMonitorSysGenServerSettingAlertNotificationMethod() == null) {
            return AlertNotificationMethod.EMAIL.getDesc();
        }
        return AlertNotificationMethod.fromCode(setting.getMonitorSysGenServerSettingAlertNotificationMethod())
                .getDesc();
    }

    /**
     * 判断是否启用Docker
     */
    public static boolean isDockerEnabled(MonitorSysGenServerSetting setting) {
        return setting != null && setting.getMonitorSysGenServerSettingDockerEnabled() != null
                && setting.getMonitorSysGenServerSettingDockerEnabled() == 1;
    }

    /**
     * 判断是否启用告警
     */
    public static boolean isAlertEnabled(MonitorSysGenServerSetting setting) {
        return setting != null && setting.getMonitorSysGenServerSettingAlertEnabled() != null
                && setting.getMonitorSysGenServerSettingAlertEnabled() == 1;
    }

    /**
     * 判断是否启用进程监控
     */
    public static boolean isProcessMonitorEnabled(MonitorSysGenServerSetting setting) {
        return setting != null && setting.getMonitorSysGenServerSettingProcessMonitorEnabled() != null
                && setting.getMonitorSysGenServerSettingProcessMonitorEnabled() == 1;
    }

    /**
     * 判断是否启用服务监控
     */
    public static boolean isServiceMonitorEnabled(MonitorSysGenServerSetting setting) {
        return setting != null && setting.getMonitorSysGenServerSettingServiceMonitorEnabled() != null
                && setting.getMonitorSysGenServerSettingServiceMonitorEnabled() == 1;
    }

    /**
     * 判断是否启用端口监控
     */
    public static boolean isPortMonitorEnabled(MonitorSysGenServerSetting setting) {
        return setting != null && setting.getMonitorSysGenServerSettingPortMonitorEnabled() != null
                && setting.getMonitorSysGenServerSettingPortMonitorEnabled() == 1;
    }

    /**
     * 判断是否启用日志监控
     */
    public static boolean isLogMonitorEnabled(MonitorSysGenServerSetting setting) {
        return setting != null && setting.getMonitorSysGenServerSettingLogMonitorEnabled() != null
                && setting.getMonitorSysGenServerSettingLogMonitorEnabled() == 1;
    }

    /**
     * 判断是否使用代理
     */
    public static boolean isProxyEnabled(MonitorSysGenServerSetting setting) {
        return setting != null && setting.getMonitorSysGenServerSettingProxyEnabled() != null
                && setting.getMonitorSysGenServerSettingProxyEnabled() == 1;
    }

    /**
     * 判断是否启用Docker TLS
     */
    public static boolean isDockerTlsEnabled(MonitorSysGenServerSetting setting) {
        return setting != null && setting.getMonitorSysGenServerSettingDockerTlsEnabled() != null
                && setting.getMonitorSysGenServerSettingDockerTlsEnabled() == 1;
    }

    /**
     * 获取监控端口列表
     */
    public static List<Integer> getMonitorPortsList(MonitorSysGenServerSetting setting) {
        if (setting == null || setting.getMonitorSysGenServerSettingMonitorPorts() == null
                || setting.getMonitorSysGenServerSettingMonitorPorts().trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<Integer> ports = new ArrayList<>();
        String[] portStrings = setting.getMonitorSysGenServerSettingMonitorPorts().split(",");
        for (String portString : portStrings) {
            try {
                ports.add(Integer.parseInt(portString.trim()));
            } catch (NumberFormatException e) {
                // 忽略无效的端口号
            }
        }
        return ports;
    }

    /**
     * 设置监控端口列表
     */
    public static void setMonitorPortsList(MonitorSysGenServerSetting setting, List<Integer> ports) {
        if (setting == null) {
            return;
        }
        if (ports == null || ports.isEmpty()) {
            setting.setMonitorSysGenServerSettingMonitorPorts(null);
        } else {
            setting.setMonitorSysGenServerSettingMonitorPorts(
                    ports.stream().map(String::valueOf).collect(Collectors.joining(",")));
        }
    }

    /**
     * 是否启用Docker监控
     */
    public static boolean isDockerMonitorEnabled(MonitorSysGenServerSetting setting) {
        return setting != null && setting.getMonitorSysGenServerSettingDockerMonitorEnabled() != null
                && setting.getMonitorSysGenServerSettingDockerMonitorEnabled() == 1;
    }

    /**
     * 判断是否启用文件管理
     */
    public static boolean isFileManagementEnabled(MonitorSysGenServerSetting setting) {
        return setting != null && setting.getMonitorSysGenServerSettingFileManagementEnabled() != null
                && setting.getMonitorSysGenServerSettingFileManagementEnabled() == 1;
    }

    /**
     * 获取Docker连接方式描述
     */
    public static String getDockerConnectionTypeDesc(MonitorSysGenServerSetting setting) {
        if (setting == null || setting.getMonitorSysGenServerSettingDockerConnectionType() == null) {
            return DockerConnectionType.SHELL.getDesc();
        }
        return DockerConnectionType.fromCode(setting.getMonitorSysGenServerSettingDockerConnectionType()).getDesc();
    }

    /**
     * 获取代理类型描述
     */
    public static String getProxyTypeDesc(MonitorSysGenServerSetting setting) {
        if (setting == null || setting.getMonitorSysGenServerSettingProxyType() == null) {
            return ProxyType.HTTP.getDesc();
        }
        return ProxyType.fromCode(setting.getMonitorSysGenServerSettingProxyType()).getDesc();
    }

    /**
     * 获取文件管理模式
     */
    public static String getFileManagementMode(MonitorSysGenServerSetting setting) {
        if (setting == null) {
            return FileManagementMode.NONE.getCode();
        }
        return setting.getMonitorSysGenServerSettingFileManagementMode() != null
                ? setting.getMonitorSysGenServerSettingFileManagementMode()
                : FileManagementMode.NONE.getCode();
    }

    /**
     * 获取文件管理模式枚举
     */
    public static FileManagementMode getFileManagementModeEnum(MonitorSysGenServerSetting setting) {
        if (setting == null) {
            return FileManagementMode.NONE;
        }
        return FileManagementMode.fromCode(setting.getMonitorSysGenServerSettingFileManagementMode());
    }

    /**
     * 获取文件管理API配置对象
     */
    public static FileManagementApiConfig getFileManagementApiConfigObject(MonitorSysGenServerSetting setting) {
        if (setting == null || setting.getMonitorSysGenServerSettingFileManagementApiConfig() == null
                || setting.getMonitorSysGenServerSettingFileManagementApiConfig().trim().isEmpty()) {
            return FileManagementApiConfig.createDefault();
        }

        try {
            return Json.fromJson(setting.getMonitorSysGenServerSettingFileManagementApiConfig(),
                    FileManagementApiConfig.class);
        } catch (Exception e) {
            return FileManagementApiConfig.createDefault();
        }
    }

    /**
     * 判断是否支持文件管理功能 需要启用文件管理且文件管理模式不为NONE
     */
    public static boolean supportsFileManagement(MonitorSysGenServerSetting setting) {
        return isFileManagementEnabled(setting) && getFileManagementModeEnum(setting).supportsFileManagement();
    }

    /**
     * 获取客户端健康状态检查间隔（秒） 如果未设置则返回默认值30秒
     */
    public static int getClientHealthCheckIntervalOrDefault(MonitorSysGenServerSetting setting) {
        if (setting == null) {
            return 30;
        }
        return setting.getMonitorSysGenServerSettingClientHealthCheckInterval() != null
                ? setting.getMonitorSysGenServerSettingClientHealthCheckInterval()
                : 30;
    }

    /**
     * 获取客户端健康状态超时时间（秒） 如果未设置则返回默认值10秒
     */
    public static int getClientHealthTimeoutOrDefault(MonitorSysGenServerSetting setting) {
        if (setting == null) {
            return 10;
        }
        return setting.getMonitorSysGenServerSettingClientHealthTimeout() != null
                ? setting.getMonitorSysGenServerSettingClientHealthTimeout()
                : 10;
    }

    /**
     * 设置文件管理模式
     */
    public static void setFileManagementMode(MonitorSysGenServerSetting setting, String mode) {
        if (setting != null) {
            setting.setMonitorSysGenServerSettingFileManagementMode(mode);
        }
    }

    /**
     * 获取文件管理操作超时时间（秒） 如果未设置则返回默认值60秒
     */
    public static int getFileManagementTimeoutOrDefault(MonitorSysGenServerSetting setting) {
        if (setting == null) {
            return 60;
        }
        return setting.getMonitorSysGenServerSettingFileManagementTimeout() != null
                ? setting.getMonitorSysGenServerSettingFileManagementTimeout()
                : 60;
    }

    /**
     * 设置文件管理API配置对象
     */
    public static void setFileManagementApiConfigObject(MonitorSysGenServerSetting setting,
                                                        FileManagementApiConfig config) {
        if (setting == null) {
            return;
        }
        if (config == null) {
            setting.setMonitorSysGenServerSettingFileManagementApiConfig(null);
        } else {
            try {
                setting.setMonitorSysGenServerSettingFileManagementApiConfig(Json.toJson(config));
            } catch (Exception e) {
                setting.setMonitorSysGenServerSettingFileManagementApiConfig(null);
            }
        }
    }

    /**
     * 获取文件管理最大重试次数 如果未设置则返回默认值3次
     */
    public static int getFileManagementMaxRetriesOrDefault(MonitorSysGenServerSetting setting) {
        if (setting == null) {
            return 3;
        }
        return setting.getMonitorSysGenServerSettingFileManagementMaxRetries() != null
                ? setting.getMonitorSysGenServerSettingFileManagementMaxRetries()
                : 3;
    }

    /**
     * 判断是否需要客户端健康状态验证
     */
    public static boolean requiresClientHealthCheck(MonitorSysGenServerSetting setting) {
        FileManagementMode mode = getFileManagementModeEnum(setting);
        return mode.requiresClientOnline() || mode.requiresApiConnection();
    }
}
