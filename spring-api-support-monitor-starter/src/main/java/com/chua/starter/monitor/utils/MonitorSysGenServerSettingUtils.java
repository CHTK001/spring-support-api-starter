package com.chua.starter.monitor.utils;

import com.chua.common.support.json.Json;
import com.chua.starter.monitor.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.pojo.FileManagementApiConfig;
import com.chua.starter.monitor.enums.DataReportMethod;
import com.chua.starter.monitor.enums.DockerConnectionType;
import com.chua.starter.monitor.enums.ProxyType;
import com.chua.starter.monitor.enums.AlertNotificationMethod;
import com.chua.starter.monitor.enums.FileManagementMode;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 服务器配置设置工具类 用于处理 MonitorSysGenServerSetting 实体的计算逻辑
 *
 * @author CH
 * @since 2024/12/26
 */
@Slf4j
public class MonitorSysGenServerSettingUtils {

    /**
     * 判断是否启用监控
     *
     * @param setting 服务器配置设置对象 {@link MonitorSysGenServerSetting}
     * @return true: 启用监控, false: 禁用监控
     * @example setting.getMonitorSysGenServerSettingMonitorEnabled() == 1 返回 true
     */
    public static boolean isMonitorEnabled(MonitorSysGenServerSetting setting) {
        return setting != null && setting.getMonitorSysGenServerSettingMonitorEnabled() != null
                && setting.getMonitorSysGenServerSettingMonitorEnabled() == 1;
    }

    /**
     * 判断是否需要上报
     *
     * @param setting 服务器配置设置对象 {@link MonitorSysGenServerSetting}
     * @return true: 需要上报, false: 不需要上报
     * @example setting.getMonitorSysGenServerSettingReportEnabled() == 1 返回 true
     */
    public static boolean isReportEnabled(MonitorSysGenServerSetting setting) {
        return setting != null && setting.getMonitorSysGenServerSettingReportEnabled() != null
                && setting.getMonitorSysGenServerSettingReportEnabled() == 1;
    }

    /**
     * 判断是否启用性能优化建议
     *
     * @param setting 服务器配置设置对象 {@link MonitorSysGenServerSetting}
     * @return true: 启用性能优化建议, false: 禁用性能优化建议
     * @example setting.getMonitorSysGenServerSettingPerformanceSuggestionEnabled() == 1 返回 true
     */
    public static boolean isPerformanceSuggestionEnabled(MonitorSysGenServerSetting setting) {
        return setting != null && setting.getMonitorSysGenServerSettingPerformanceSuggestionEnabled() != null
                && setting.getMonitorSysGenServerSettingPerformanceSuggestionEnabled() == 1;
    }

    /**
     * 判断是否启用自动恢复通知
     *
     * @param setting 服务器配置设置对象 {@link MonitorSysGenServerSetting}
     * @return true: 启用自动恢复通知, false: 禁用自动恢复通知
     * @example setting.getMonitorSysGenServerSettingAutoRecoveryNotificationEnabled() == 1 返回 true
     */
    public static boolean isAutoRecoveryNotificationEnabled(MonitorSysGenServerSetting setting) {
        return setting != null && setting.getMonitorSysGenServerSettingAutoRecoveryNotificationEnabled() != null
                && setting.getMonitorSysGenServerSettingAutoRecoveryNotificationEnabled() == 1;
    }

    /**
     * 获取数据上报方式描述
     *
     * @param setting 服务器配置设置对象 {@link MonitorSysGenServerSetting}
     * @return 数据上报方式描述文本
     * @example setting.getMonitorSysGenServerSettingDataReportMethod() = "NODE" 返回 "节点上报"
     */
    public static String getDataReportMethodDesc(MonitorSysGenServerSetting setting) {
        if (setting == null || setting.getMonitorSysGenServerSettingDataReportMethod() == null) {
            return DataReportMethod.NONE.getDesc();
        }
        return DataReportMethod.fromCode(setting.getMonitorSysGenServerSettingDataReportMethod()).getDesc();
    }

    /**
     * 获取告警通知方式描述
     *
     * @param setting 服务器配置设置对象 {@link MonitorSysGenServerSetting}
     * @return 告警通知方式描述文本
     * @example setting.getMonitorSysGenServerSettingAlertNotificationMethod() = "EMAIL" 返回 "邮件"
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
     *
     * @param setting 服务器配置设置对象 {@link MonitorSysGenServerSetting}
     * @return true: 启用Docker, false: 禁用Docker
     * @example setting.getMonitorSysGenServerSettingDockerEnabled() == 1 返回 true
     */
    public static boolean isDockerEnabled(MonitorSysGenServerSetting setting) {
        return setting != null && setting.getMonitorSysGenServerSettingDockerEnabled() != null
                && setting.getMonitorSysGenServerSettingDockerEnabled() == 1;
    }

    /**
     * 判断是否启用告警
     *
     * @param setting 服务器配置设置对象 {@link MonitorSysGenServerSetting}
     * @return true: 启用告警, false: 禁用告警
     * @example setting.getMonitorSysGenServerSettingAlertEnabled() == 1 返回 true
     */
    public static boolean isAlertEnabled(MonitorSysGenServerSetting setting) {
        return setting != null && setting.getMonitorSysGenServerSettingAlertEnabled() != null
                && setting.getMonitorSysGenServerSettingAlertEnabled() == 1;
    }

    /**
     * 判断是否启用进程监控
     *
     * @param setting 服务器配置设置对象 {@link MonitorSysGenServerSetting}
     * @return true: 启用进程监控, false: 禁用进程监控
     * @example setting.getMonitorSysGenServerSettingProcessMonitorEnabled() == 1 返回 true
     */
    public static boolean isProcessMonitorEnabled(MonitorSysGenServerSetting setting) {
        return setting != null && setting.getMonitorSysGenServerSettingProcessMonitorEnabled() != null
                && setting.getMonitorSysGenServerSettingProcessMonitorEnabled() == 1;
    }

    /**
     * 判断是否启用服务监控
     *
     * @param setting 服务器配置设置对象 {@link MonitorSysGenServerSetting}
     * @return true: 启用服务监控, false: 禁用服务监控
     * @example setting.getMonitorSysGenServerSettingServiceMonitorEnabled() == 1 返回 true
     */
    public static boolean isServiceMonitorEnabled(MonitorSysGenServerSetting setting) {
        return setting != null && setting.getMonitorSysGenServerSettingServiceMonitorEnabled() != null
                && setting.getMonitorSysGenServerSettingServiceMonitorEnabled() == 1;
    }

    /**
     * 判断是否启用端口监控
     *
     * @param setting 服务器配置设置对象 {@link MonitorSysGenServerSetting}
     * @return true: 启用端口监控, false: 禁用端口监控
     * @example setting.getMonitorSysGenServerSettingPortMonitorEnabled() == 1 返回 true
     */
    public static boolean isPortMonitorEnabled(MonitorSysGenServerSetting setting) {
        return setting != null && setting.getMonitorSysGenServerSettingPortMonitorEnabled() != null
                && setting.getMonitorSysGenServerSettingPortMonitorEnabled() == 1;
    }

    /**
     * 判断是否启用日志监控
     *
     * @param setting 服务器配置设置对象 {@link MonitorSysGenServerSetting}
     * @return true: 启用日志监控, false: 禁用日志监控
     * @example setting.getMonitorSysGenServerSettingLogMonitorEnabled() == 1 返回 true
     */
    public static boolean isLogMonitorEnabled(MonitorSysGenServerSetting setting) {
        return setting != null && setting.getMonitorSysGenServerSettingLogMonitorEnabled() != null
                && setting.getMonitorSysGenServerSettingLogMonitorEnabled() == 1;
    }

    /**
     * 判断是否使用代理
     *
     * @param setting 服务器配置设置对象 {@link MonitorSysGenServerSetting}
     * @return true: 使用代理, false: 不使用代理
     * @example setting.getMonitorSysGenServerSettingProxyEnabled() == 1 返回 true
     */
    public static boolean isProxyEnabled(MonitorSysGenServerSetting setting) {
        return setting != null && setting.getMonitorSysGenServerSettingProxyEnabled() != null
                && setting.getMonitorSysGenServerSettingProxyEnabled() == 1;
    }

    /**
     * 判断是否启用Docker TLS
     *
     * @param setting 服务器配置设置对象 {@link MonitorSysGenServerSetting}
     * @return true: 启用Docker TLS, false: 禁用Docker TLS
     * @example setting.getMonitorSysGenServerSettingDockerTlsEnabled() == 1 返回 true
     */
    public static boolean isDockerTlsEnabled(MonitorSysGenServerSetting setting) {
        return setting != null && setting.getMonitorSysGenServerSettingDockerTlsEnabled() != null
                && setting.getMonitorSysGenServerSettingDockerTlsEnabled() == 1;
    }

    /**
     * 获取监控端口列表
     *
     * @param setting 服务器配置设置对象 {@link MonitorSysGenServerSetting}
     * @return 监控端口列表
     * @example setting.getMonitorSysGenServerSettingMonitorPorts() = "8080,8081" 返回 [8080, 8081]
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
                log.warn("解析端口号失败: {}", portString, e);
            }
        }
        return ports;
    }

    /**
     * 设置监控端口列表
     *
     * @param setting 服务器配置设置对象 {@link MonitorSysGenServerSetting}
     * @param ports   端口列表
     * @example ports = [8080, 8081] 会设置为 "8080,8081"
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
     *
     * @param setting 服务器配置设置对象 {@link MonitorSysGenServerSetting}
     * @return true: 启用Docker监控, false: 禁用Docker监控
     * @example setting.getMonitorSysGenServerSettingDockerMonitorEnabled() == 1 返回 true
     */
    public static boolean isDockerMonitorEnabled(MonitorSysGenServerSetting setting) {
        return setting != null && setting.getMonitorSysGenServerSettingDockerMonitorEnabled() != null
                && setting.getMonitorSysGenServerSettingDockerMonitorEnabled() == 1;
    }

    /**
     * 判断是否启用文件管理
     *
     * @param setting 服务器配置设置对象 {@link MonitorSysGenServerSetting}
     * @return true: 启用文件管理, false: 禁用文件管理
     * @example setting.getMonitorSysGenServerSettingFileManagementEnabled() == 1 返回 true
     */
    public static boolean isFileManagementEnabled(MonitorSysGenServerSetting setting) {
        return setting != null && setting.getMonitorSysGenServerSettingFileManagementEnabled() != null
                && setting.getMonitorSysGenServerSettingFileManagementEnabled() == 1;
    }

    /**
     * 获取Docker连接方式描述
     *
     * @param setting 服务器配置设置对象 {@link MonitorSysGenServerSetting}
     * @return Docker连接方式描述文本
     * @example setting.getMonitorSysGenServerSettingDockerConnectionType() = "TCP" 返回 "TCP连接"
     */
    public static String getDockerConnectionTypeDesc(MonitorSysGenServerSetting setting) {
        if (setting == null || setting.getMonitorSysGenServerSettingDockerConnectionType() == null) {
            return DockerConnectionType.SHELL.getDesc();
        }
        return DockerConnectionType.fromCode(setting.getMonitorSysGenServerSettingDockerConnectionType()).getDesc();
    }

    /**
     * 获取代理类型描述
     *
     * @param setting 服务器配置设置对象 {@link MonitorSysGenServerSetting}
     * @return 代理类型描述文本
     * @example setting.getMonitorSysGenServerSettingProxyType() = "SOCKS5" 返回 "SOCKS5代理"
     */
    public static String getProxyTypeDesc(MonitorSysGenServerSetting setting) {
        if (setting == null || setting.getMonitorSysGenServerSettingProxyType() == null) {
            return ProxyType.HTTP.getDesc();
        }
        return ProxyType.fromCode(setting.getMonitorSysGenServerSettingProxyType()).getDesc();
    }

    /**
     * 获取文件管理模式
     *
     * @param setting 服务器配置设置对象 {@link MonitorSysGenServerSetting}
     * @return 文件管理模式代码
     * @example setting.getMonitorSysGenServerSettingFileManagementMode() = "SSH" 返回 "SSH"
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
     *
     * @param setting 服务器配置设置对象 {@link MonitorSysGenServerSetting}
     * @return 文件管理模式枚举值 {@link FileManagementMode}
     * @example setting.getMonitorSysGenServerSettingFileManagementMode() = "SSH" 返回 FileManagementMode.SSH
     */
    public static FileManagementMode getFileManagementModeEnum(MonitorSysGenServerSetting setting) {
        if (setting == null) {
            return FileManagementMode.NONE;
        }
        return FileManagementMode.fromCode(setting.getMonitorSysGenServerSettingFileManagementMode());
    }

    /**
     * 获取文件管理API配置对象
     *
     * @param setting 服务器配置设置对象 {@link MonitorSysGenServerSetting}
     * @return 文件管理API配置对象 {@link FileManagementApiConfig}
     * @example setting.getMonitorSysGenServerSettingFileManagementApiConfig() = JSON字符串 返回解析后的对象
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
            log.error("解析文件管理API配置失败", e);
            return FileManagementApiConfig.createDefault();
        }
    }

    /**
     * 判断是否支持文件管理功能 需要启用文件管理且文件管理模式不为NONE
     *
     * @param setting 服务器配置设置对象 {@link MonitorSysGenServerSetting}
     * @return true: 支持文件管理, false: 不支持文件管理
     * @example isFileManagementEnabled=true 且 getFileManagementModeEnum=SSH 返回 true
     */
    public static boolean supportsFileManagement(MonitorSysGenServerSetting setting) {
        return isFileManagementEnabled(setting) && getFileManagementModeEnum(setting).supportsFileManagement();
    }

    /**
     * 获取客户端健康状态检查间隔（秒） 如果未设置则返回默认值30秒
     *
     * @param setting 服务器配置设置对象 {@link MonitorSysGenServerSetting}
     * @return 客户端健康状态检查间隔（秒）
     * @example setting.getMonitorSysGenServerSettingClientHealthCheckInterval() = 60 返回 60
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
     *
     * @param setting 服务器配置设置对象 {@link MonitorSysGenServerSetting}
     * @return 客户端健康状态超时时间（秒）
     * @example setting.getMonitorSysGenServerSettingClientHealthTimeout() = 20 返回 20
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
     *
     * @param setting 服务器配置设置对象 {@link MonitorSysGenServerSetting}
     * @param mode    文件管理模式代码
     * @example mode = "SSH" 会设置文件管理模式为SSH
     */
    public static void setFileManagementMode(MonitorSysGenServerSetting setting, String mode) {
        if (setting != null) {
            setting.setMonitorSysGenServerSettingFileManagementMode(mode);
        }
    }

    /**
     * 获取文件管理操作超时时间（秒） 如果未设置则返回默认值60秒
     *
     * @param setting 服务器配置设置对象 {@link MonitorSysGenServerSetting}
     * @return 文件管理操作超时时间（秒）
     * @example setting.getMonitorSysGenServerSettingFileManagementTimeout() = 120 返回 120
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
     *
     * @param setting 服务器配置设置对象 {@link MonitorSysGenServerSetting}
     * @param config  文件管理API配置对象 {@link FileManagementApiConfig}
     * @example config = FileManagementApiConfig 对象 会序列化为JSON字符串存储
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
                log.error("序列化文件管理API配置失败", e);
                setting.setMonitorSysGenServerSettingFileManagementApiConfig(null);
            }
        }
    }

    /**
     * 获取文件管理最大重试次数 如果未设置则返回默认值3次
     *
     * @param setting 服务器配置设置对象 {@link MonitorSysGenServerSetting}
     * @return 文件管理最大重试次数
     * @example setting.getMonitorSysGenServerSettingFileManagementMaxRetries() = 5 返回 5
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
     *
     * @param setting 服务器配置设置对象 {@link MonitorSysGenServerSetting}
     * @return true: 需要客户端健康状态验证, false: 不需要客户端健康状态验证
     * @example getFileManagementModeEnum=NODE 返回 true (因为NODE模式需要客户端在线)
     */
    public static boolean requiresClientHealthCheck(MonitorSysGenServerSetting setting) {
        FileManagementMode mode = getFileManagementModeEnum(setting);
        return mode.requiresClientOnline() || mode.requiresApiConnection();
    }
}
