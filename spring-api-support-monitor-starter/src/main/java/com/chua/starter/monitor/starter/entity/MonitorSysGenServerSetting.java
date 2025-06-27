package com.chua.starter.monitor.starter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 服务器配置设置实体类
 *
 * @author CH
 * @since 2024/12/26
 */
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "服务器配置设置")
@Schema(description = "服务器配置设置")
@Data
@TableName(value = "monitor_sys_gen_server_setting")
public class MonitorSysGenServerSetting extends SysBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "monitor_sys_gen_server_setting_id", type = IdType.AUTO)
    @ApiModelProperty(value = "服务器配置设置ID")
    @Schema(description = "服务器配置设置ID")
    @NotNull(message = "配置ID不能为空", groups = {UpdateGroup.class})
    private Integer monitorSysGenServerSettingId;

    /**
     * 服务器ID (关联MonitorSysGenServer表)
     */
    @TableField(value = "monitor_sys_gen_server_id")
    @ApiModelProperty(value = "服务器ID")
    @Schema(description = "服务器ID")
    @NotNull(message = "服务器ID不能为空", groups = {AddGroup.class, UpdateGroup.class})
    private Integer monitorSysGenServerId;

    /**
     * 是否需要上报 0:否 1:是 (默认上报)
     */
    @TableField(value = "monitor_sys_gen_server_setting_report_enabled")
    @ApiModelProperty(value = "是否需要上报")
    @Schema(description = "是否需要上报")
    private Integer monitorSysGenServerSettingReportEnabled;

    /**
     * 数据上报方式 (NONE:不支持上报, LOCAL:本地上报, API:接口上报, PROMETHEUS:prometheus)
     */
    @TableField(value = "monitor_sys_gen_server_setting_data_report_method")
    @ApiModelProperty(value = "数据上报方式")
    @Schema(description = "数据上报方式")
    @Size(max = 20, message = "数据上报方式最大长度要小于 20")
    private String monitorSysGenServerSettingDataReportMethod;

    /**
     * Prometheus服务器地址
     */
    @TableField(value = "monitor_sys_gen_server_setting_prometheus_host")
    @ApiModelProperty(value = "Prometheus服务器地址")
    @Schema(description = "Prometheus服务器地址")
    @Size(max = 255, message = "Prometheus服务器地址最大长度要小于 255")
    private String monitorSysGenServerSettingPrometheusHost;

    /**
     * Prometheus服务器端口
     */
    @TableField(value = "monitor_sys_gen_server_setting_prometheus_port")
    @ApiModelProperty(value = "Prometheus服务器端口")
    @Schema(description = "Prometheus服务器端口")
    private Integer monitorSysGenServerSettingPrometheusPort;

    /**
     * 指标存储时间(天)
     */
    @TableField(value = "monitor_sys_gen_server_setting_metrics_retention_days")
    @ApiModelProperty(value = "指标存储时间(天)")
    @Schema(description = "指标存储时间(天)")
    private Integer monitorSysGenServerSettingMetricsRetentionDays;

    /**
     * CPU指标存储时间(天)
     */
    @TableField(value = "monitor_sys_gen_server_setting_cpu_retention_days")
    @ApiModelProperty(value = "CPU指标存储时间(天)")
    @Schema(description = "CPU指标存储时间(天)")
    private Integer monitorSysGenServerSettingCpuRetentionDays;

    /**
     * 内存指标存储时间(天)
     */
    @TableField(value = "monitor_sys_gen_server_setting_memory_retention_days")
    @ApiModelProperty(value = "内存指标存储时间(天)")
    @Schema(description = "内存指标存储时间(天)")
    private Integer monitorSysGenServerSettingMemoryRetentionDays;

    /**
     * 磁盘指标存储时间(天)
     */
    @TableField(value = "monitor_sys_gen_server_setting_disk_retention_days")
    @ApiModelProperty(value = "磁盘指标存储时间(天)")
    @Schema(description = "磁盘指标存储时间(天)")
    private Integer monitorSysGenServerSettingDiskRetentionDays;

    /**
     * 网络指标存储时间(天)
     */
    @TableField(value = "monitor_sys_gen_server_setting_network_retention_days")
    @ApiModelProperty(value = "网络指标存储时间(天)")
    @Schema(description = "网络指标存储时间(天)")
    private Integer monitorSysGenServerSettingNetworkRetentionDays;

    /**
     * 监控间隔(秒)
     */
    @TableField(value = "monitor_sys_gen_server_setting_monitor_interval")
    @ApiModelProperty(value = "监控间隔(秒)")
    @Schema(description = "监控间隔(秒)")
    private Integer monitorSysGenServerSettingMonitorInterval;

    /**
     * 是否启用监控 0:否 1:是
     */
    @TableField(value = "monitor_sys_gen_server_setting_monitor_enabled")
    @ApiModelProperty(value = "是否启用监控")
    @Schema(description = "是否启用监控")
    private Integer monitorSysGenServerSettingMonitorEnabled;

    /**
     * CPU告警阈值 (百分比)
     */
    @TableField(value = "monitor_sys_gen_server_setting_cpu_alert_threshold")
    @ApiModelProperty(value = "CPU告警阈值")
    @Schema(description = "CPU告警阈值 (百分比)")
    private BigDecimal monitorSysGenServerSettingCpuAlertThreshold;

    /**
     * 内存告警阈值 (百分比)
     */
    @TableField(value = "monitor_sys_gen_server_setting_memory_alert_threshold")
    @ApiModelProperty(value = "内存告警阈值")
    @Schema(description = "内存告警阈值 (百分比)")
    private BigDecimal monitorSysGenServerSettingMemoryAlertThreshold;

    /**
     * 磁盘告警阈值 (百分比)
     */
    @TableField(value = "monitor_sys_gen_server_setting_disk_alert_threshold")
    @ApiModelProperty(value = "磁盘告警阈值")
    @Schema(description = "磁盘告警阈值 (百分比)")
    private BigDecimal monitorSysGenServerSettingDiskAlertThreshold;

    /**
     * 网络告警阈值 (Mbps)
     */
    @TableField(value = "monitor_sys_gen_server_setting_network_alert_threshold")
    @ApiModelProperty(value = "网络告警阈值")
    @Schema(description = "网络告警阈值 (Mbps)")
    private BigDecimal monitorSysGenServerSettingNetworkAlertThreshold;

    /**
     * 响应时间告警阈值 (毫秒)
     */
    @TableField(value = "monitor_sys_gen_server_setting_response_time_alert_threshold")
    @ApiModelProperty(value = "响应时间告警阈值")
    @Schema(description = "响应时间告警阈值 (毫秒)")
    private Long monitorSysGenServerSettingResponseTimeAlertThreshold;

    /**
     * 是否启用告警 0:否 1:是
     */
    @TableField(value = "monitor_sys_gen_server_setting_alert_enabled")
    @ApiModelProperty(value = "是否启用告警")
    @Schema(description = "是否启用告警")
    private Integer monitorSysGenServerSettingAlertEnabled;

    /**
     * 告警通知方式 (EMAIL:邮件, SMS:短信, WEBHOOK:Webhook)
     */
    @TableField(value = "monitor_sys_gen_server_setting_alert_notification_method")
    @ApiModelProperty(value = "告警通知方式")
    @Schema(description = "告警通知方式")
    @Size(max = 100, message = "告警通知方式最大长度要小于 100")
    private String monitorSysGenServerSettingAlertNotificationMethod;

    /**
     * 告警通知地址 (邮箱地址、手机号、Webhook URL等)
     */
    @TableField(value = "monitor_sys_gen_server_setting_alert_notification_address")
    @ApiModelProperty(value = "告警通知地址")
    @Schema(description = "告警通知地址")
    @Size(max = 500, message = "告警通知地址最大长度要小于 500")
    private String monitorSysGenServerSettingAlertNotificationAddress;

    /**
     * 告警静默时间 (分钟) - 同一类型告警在此时间内不重复发送
     */
    @TableField(value = "monitor_sys_gen_server_setting_alert_silence_duration")
    @ApiModelProperty(value = "告警静默时间")
    @Schema(description = "告警静默时间 (分钟)")
    private Integer monitorSysGenServerSettingAlertSilenceDuration;

    /**
     * 是否启用自动恢复通知 0:否 1:是
     */
    @TableField(value = "monitor_sys_gen_server_setting_auto_recovery_enabled")
    @ApiModelProperty(value = "是否启用自动恢复通知")
    @Schema(description = "是否启用自动恢复通知")
    private Integer monitorSysGenServerSettingAutoRecoveryNotificationEnabled;

    /**
     * 数据收集频率 (秒) - 多久收集一次指标数据
     */
    @TableField(value = "monitor_sys_gen_server_setting_data_collection_frequency")
    @ApiModelProperty(value = "数据收集频率")
    @Schema(description = "数据收集频率 (秒)")
    private Integer monitorSysGenServerSettingDataCollectionFrequency;

    /**
     * 是否启用性能优化建议 0:否 1:是
     */
    @TableField(value = "monitor_sys_gen_server_setting_performance_suggestion_enabled")
    @ApiModelProperty(value = "是否启用性能优化建议")
    @Schema(description = "是否启用性能优化建议")
    private Integer monitorSysGenServerSettingPerformanceSuggestionEnabled;

    /**
     * 自定义标签 (JSON格式)
     */
    @TableField(value = "monitor_sys_gen_server_setting_custom_tags")
    @ApiModelProperty(value = "自定义标签")
    @Schema(description = "自定义标签 (JSON格式)")
    @Size(max = 1000, message = "自定义标签最大长度要小于 1000")
    private String monitorSysGenServerSettingCustomTags;

    /**
     * 配置描述
     */
    @TableField(value = "monitor_sys_gen_server_setting_description")
    @ApiModelProperty(value = "配置描述")
    @Schema(description = "配置描述")
    @Size(max = 500, message = "配置描述最大长度要小于 500")
    private String monitorSysGenServerSettingDescription;

    /**
     * 配置状态 0:停用 1:启用
     */
    @TableField(value = "monitor_sys_gen_server_setting_status")
    @ApiModelProperty(value = "配置状态")
    @Schema(description = "配置状态")
    private Integer monitorSysGenServerSettingStatus;

    // ==================== Docker 相关配置 ====================

    /**
     * 是否支持Docker 0:否 1:是
     */
    @TableField(value = "monitor_sys_gen_server_setting_docker_enabled")
    @ApiModelProperty(value = "是否支持Docker")
    @Schema(description = "是否支持Docker")
    private Integer monitorSysGenServerSettingDockerEnabled;

    /**
     * Docker连接方式 (SHELL:命令行, TCP:TCP连接, SOCKET:Unix Socket)
     */
    @TableField(value = "monitor_sys_gen_server_setting_docker_connection_type")
    @ApiModelProperty(value = "Docker连接方式")
    @Schema(description = "Docker连接方式")
    @Size(max = 20, message = "Docker连接方式最大长度要小于 20")
    private String monitorSysGenServerSettingDockerConnectionType;

    /**
     * Docker TCP连接地址
     */
    @TableField(value = "monitor_sys_gen_server_setting_docker_host")
    @ApiModelProperty(value = "Docker TCP连接地址")
    @Schema(description = "Docker TCP连接地址")
    @Size(max = 255, message = "Docker TCP连接地址最大长度要小于 255")
    private String monitorSysGenServerSettingDockerHost;

    /**
     * Docker TCP连接端口
     */
    @TableField(value = "monitor_sys_gen_server_setting_docker_port")
    @ApiModelProperty(value = "Docker TCP连接端口")
    @Schema(description = "Docker TCP连接端口")
    private Integer monitorSysGenServerSettingDockerPort;

    /**
     * Docker Socket路径 (Unix Socket连接使用)
     */
    @TableField(value = "monitor_sys_gen_server_setting_docker_socket_path")
    @ApiModelProperty(value = "Docker Socket路径")
    @Schema(description = "Docker Socket路径")
    @Size(max = 500, message = "Docker Socket路径最大长度要小于 500")
    private String monitorSysGenServerSettingDockerSocketPath;

    /**
     * Docker TLS验证 0:否 1:是
     */
    @TableField(value = "monitor_sys_gen_server_setting_docker_tls_enabled")
    @ApiModelProperty(value = "Docker TLS验证")
    @Schema(description = "Docker TLS验证")
    private Integer monitorSysGenServerSettingDockerTlsEnabled;

    /**
     * Docker证书路径
     */
    @TableField(value = "monitor_sys_gen_server_setting_docker_cert_path")
    @ApiModelProperty(value = "Docker证书路径")
    @Schema(description = "Docker证书路径")
    @Size(max = 500, message = "Docker证书路径最大长度要小于 500")
    private String monitorSysGenServerSettingDockerCertPath;

    // ==================== 代理相关配置 ====================

    /**
     * 是否使用代理 0:否 1:是
     */
    @TableField(value = "monitor_sys_gen_server_setting_proxy_enabled")
    @ApiModelProperty(value = "是否使用代理")
    @Schema(description = "是否使用代理")
    private Integer monitorSysGenServerSettingProxyEnabled;

    /**
     * 代理类型 (HTTP, SOCKS4, SOCKS5)
     */
    @TableField(value = "monitor_sys_gen_server_setting_proxy_type")
    @ApiModelProperty(value = "代理类型")
    @Schema(description = "代理类型")
    @Size(max = 20, message = "代理类型最大长度要小于 20")
    private String monitorSysGenServerSettingProxyType;

    /**
     * 代理服务器地址
     */
    @TableField(value = "monitor_sys_gen_server_setting_proxy_host")
    @ApiModelProperty(value = "代理服务器地址")
    @Schema(description = "代理服务器地址")
    @Size(max = 255, message = "代理服务器地址最大长度要小于 255")
    private String monitorSysGenServerSettingProxyHost;

    /**
     * 代理服务器端口
     */
    @TableField(value = "monitor_sys_gen_server_setting_proxy_port")
    @ApiModelProperty(value = "代理服务器端口")
    @Schema(description = "代理服务器端口")
    private Integer monitorSysGenServerSettingProxyPort;

    /**
     * 代理用户名
     */
    @TableField(value = "monitor_sys_gen_server_setting_proxy_username")
    @ApiModelProperty(value = "代理用户名")
    @Schema(description = "代理用户名")
    @Size(max = 255, message = "代理用户名最大长度要小于 255")
    private String monitorSysGenServerSettingProxyUsername;

    /**
     * 代理密码
     */
    @TableField(value = "monitor_sys_gen_server_setting_proxy_password")
    @ApiModelProperty(value = "代理密码")
    @Schema(description = "代理密码")
    @Size(max = 255, message = "代理密码最大长度要小于 255")
    private String monitorSysGenServerSettingProxyPassword;

    // ==================== 高级监控配置 ====================

    /**
     * 是否启用进程监控 0:否 1:是
     */
    @TableField(value = "monitor_sys_gen_server_setting_process_monitor_enabled")
    @ApiModelProperty(value = "是否启用进程监控")
    @Schema(description = "是否启用进程监控")
    private Integer monitorSysGenServerSettingProcessMonitorEnabled;

    /**
     * 是否启用服务监控 0:否 1:是
     */
    @TableField(value = "monitor_sys_gen_server_setting_service_monitor_enabled")
    @ApiModelProperty(value = "是否启用服务监控")
    @Schema(description = "是否启用服务监控")
    private Integer monitorSysGenServerSettingServiceMonitorEnabled;

    /**
     * 是否启用端口监控 0:否 1:是
     */
    @TableField(value = "monitor_sys_gen_server_setting_port_monitor_enabled")
    @ApiModelProperty(value = "是否启用端口监控")
    @Schema(description = "是否启用端口监控")
    private Integer monitorSysGenServerSettingPortMonitorEnabled;

    /**
     * 监控的端口列表 (逗号分隔)
     */
    @TableField(value = "monitor_sys_gen_server_setting_monitor_ports")
    @ApiModelProperty(value = "监控的端口列表")
    @Schema(description = "监控的端口列表 (逗号分隔)")
    @Size(max = 1000, message = "监控端口列表最大长度要小于 1000")
    private String monitorSysGenServerSettingMonitorPorts;

    /**
     * 是否启用日志监控 0:否 1:是
     */
    @TableField(value = "monitor_sys_gen_server_setting_log_monitor_enabled")
    @ApiModelProperty(value = "是否启用日志监控")
    @Schema(description = "是否启用日志监控")
    private Integer monitorSysGenServerSettingLogMonitorEnabled;

    /**
     * 日志文件路径列表 (JSON格式)
     */
    @TableField(value = "monitor_sys_gen_server_setting_log_file_paths")
    @ApiModelProperty(value = "日志文件路径列表")
    @Schema(description = "日志文件路径列表 (JSON格式)")
    @Size(max = 2000, message = "日志文件路径列表最大长度要小于 2000")
    private String monitorSysGenServerSettingLogFilePaths;

    /**
     * 连接超时时间 (秒)
     */
    @TableField(value = "monitor_sys_gen_server_setting_connection_timeout")
    @ApiModelProperty(value = "连接超时时间")
    @Schema(description = "连接超时时间 (秒)")
    private Integer monitorSysGenServerSettingConnectionTimeout;

    /**
     * 读取超时时间 (秒)
     */
    @TableField(value = "monitor_sys_gen_server_setting_read_timeout")
    @ApiModelProperty(value = "读取超时时间")
    @Schema(description = "读取超时时间 (秒)")
    private Integer monitorSysGenServerSettingReadTimeout;

    /**
     * 数据上报方式枚举
     */
    public enum DataReportMethod {
        NONE("NONE", "不支持上报"),
        LOCAL("LOCAL", "本地上报"),
        API("API", "接口上报"),
        PROMETHEUS("PROMETHEUS", "Prometheus");

        private final String code;
        private final String desc;

        DataReportMethod(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static DataReportMethod fromCode(String code) {
            for (DataReportMethod method : values()) {
                if (method.code.equals(code)) {
                    return method;
                }
            }
            return NONE;
        }
    }

    /**
     * Docker连接方式枚举
     */
    public enum DockerConnectionType {
        SHELL("SHELL", "命令行"),
        TCP("TCP", "TCP连接"),
        SOCKET("SOCKET", "Unix Socket");

        private final String code;
        private final String desc;

        DockerConnectionType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static DockerConnectionType fromCode(String code) {
            for (DockerConnectionType type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            return SHELL;
        }
    }

    /**
     * 代理类型枚举
     */
    public enum ProxyType {
        HTTP("HTTP", "HTTP代理"),
        SOCKS4("SOCKS4", "SOCKS4代理"),
        SOCKS5("SOCKS5", "SOCKS5代理"),
        SSH_TUNNEL("SSH_TUNNEL", "SSH隧道"),
        GUACAMOLE("GUACAMOLE", "Guacamole代理");

        private final String code;
        private final String desc;

        ProxyType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static ProxyType fromCode(String code) {
            for (ProxyType type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            return HTTP;
        }
    }

    /**
     * 告警通知方式枚举
     */
    public enum AlertNotificationMethod {
        EMAIL("EMAIL", "邮件"),
        SMS("SMS", "短信"),
        WEBHOOK("WEBHOOK", "Webhook"),
        DINGTALK("DINGTALK", "钉钉"),
        WECHAT("WECHAT", "微信");

        private final String code;
        private final String desc;

        AlertNotificationMethod(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static AlertNotificationMethod fromCode(String code) {
            for (AlertNotificationMethod method : values()) {
                if (method.code.equals(code)) {
                    return method;
                }
            }
            return EMAIL;
        }
    }

    /**
     * 获取数据上报方式描述
     */
    public String getDataReportMethodDesc() {
        if (this.monitorSysGenServerSettingDataReportMethod == null) {
            return DataReportMethod.NONE.getDesc();
        }
        return DataReportMethod.fromCode(this.monitorSysGenServerSettingDataReportMethod).getDesc();
    }

    /**
     * 获取告警通知方式描述
     */
    public String getAlertNotificationMethodDesc() {
        if (this.monitorSysGenServerSettingAlertNotificationMethod == null) {
            return AlertNotificationMethod.EMAIL.getDesc();
        }
        return AlertNotificationMethod.fromCode(this.monitorSysGenServerSettingAlertNotificationMethod).getDesc();
    }

    /**
     * 判断是否启用监控
     */
    public boolean isMonitorEnabled() {
        return this.monitorSysGenServerSettingMonitorEnabled != null && this.monitorSysGenServerSettingMonitorEnabled == 1;
    }

    /**
     * 判断是否启用告警
     */
    public boolean isAlertEnabled() {
        return this.monitorSysGenServerSettingAlertEnabled != null && this.monitorSysGenServerSettingAlertEnabled == 1;
    }

    /**
     * 判断是否需要上报
     */
    public boolean isReportEnabled() {
        return this.monitorSysGenServerSettingReportEnabled != null && this.monitorSysGenServerSettingReportEnabled == 1;
    }

    /**
     * 判断是否启用性能优化建议
     */
    public boolean isPerformanceSuggestionEnabled() {
        return this.monitorSysGenServerSettingPerformanceSuggestionEnabled != null && this.monitorSysGenServerSettingPerformanceSuggestionEnabled == 1;
    }

    /**
     * 判断是否启用自动恢复通知
     */
    public boolean isAutoRecoveryNotificationEnabled() {
        return this.monitorSysGenServerSettingAutoRecoveryNotificationEnabled != null && this.monitorSysGenServerSettingAutoRecoveryNotificationEnabled == 1;
    }

    /**
     * 判断是否启用Docker
     */
    public boolean isDockerEnabled() {
        return this.monitorSysGenServerSettingDockerEnabled != null && this.monitorSysGenServerSettingDockerEnabled == 1;
    }

    /**
     * 判断是否使用代理
     */
    public boolean isProxyEnabled() {
        return this.monitorSysGenServerSettingProxyEnabled != null && this.monitorSysGenServerSettingProxyEnabled == 1;
    }

    /**
     * 判断是否启用进程监控
     */
    public boolean isProcessMonitorEnabled() {
        return this.monitorSysGenServerSettingProcessMonitorEnabled != null && this.monitorSysGenServerSettingProcessMonitorEnabled == 1;
    }

    /**
     * 判断是否启用服务监控
     */
    public boolean isServiceMonitorEnabled() {
        return this.monitorSysGenServerSettingServiceMonitorEnabled != null && this.monitorSysGenServerSettingServiceMonitorEnabled == 1;
    }

    /**
     * 判断是否启用端口监控
     */
    public boolean isPortMonitorEnabled() {
        return this.monitorSysGenServerSettingPortMonitorEnabled != null && this.monitorSysGenServerSettingPortMonitorEnabled == 1;
    }

    /**
     * 判断是否启用日志监控
     */
    public boolean isLogMonitorEnabled() {
        return this.monitorSysGenServerSettingLogMonitorEnabled != null && this.monitorSysGenServerSettingLogMonitorEnabled == 1;
    }

    /**
     * 判断是否启用Docker TLS
     */
    public boolean isDockerTlsEnabled() {
        return this.monitorSysGenServerSettingDockerTlsEnabled != null && this.monitorSysGenServerSettingDockerTlsEnabled == 1;
    }

    /**
     * 获取Docker连接方式描述
     */
    public String getDockerConnectionTypeDesc() {
        if (this.monitorSysGenServerSettingDockerConnectionType == null) {
            return DockerConnectionType.SHELL.getDesc();
        }
        return DockerConnectionType.fromCode(this.monitorSysGenServerSettingDockerConnectionType).getDesc();
    }

    /**
     * 获取代理类型描述
     */
    public String getProxyTypeDesc() {
        if (this.monitorSysGenServerSettingProxyType == null) {
            return ProxyType.HTTP.getDesc();
        }
        return ProxyType.fromCode(this.monitorSysGenServerSettingProxyType).getDesc();
    }

    /**
     * 获取监控端口列表
     */
    public java.util.List<Integer> getMonitorPortsList() {
        if (this.monitorSysGenServerSettingMonitorPorts == null || this.monitorSysGenServerSettingMonitorPorts.trim().isEmpty()) {
            return new java.util.ArrayList<>();
        }

        java.util.List<Integer> ports = new java.util.ArrayList<>();
        String[] portStrings = this.monitorSysGenServerSettingMonitorPorts.split(",");
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
    public void setMonitorPortsList(java.util.List<Integer> ports) {
        if (ports == null || ports.isEmpty()) {
            this.monitorSysGenServerSettingMonitorPorts = null;
        } else {
            this.monitorSysGenServerSettingMonitorPorts = ports.stream()
                .map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(","));
        }
    }

    /**
     * 获取默认配置
     */
    public static MonitorSysGenServerSetting getDefaultSetting(Integer serverId) {
        MonitorSysGenServerSetting setting = new MonitorSysGenServerSetting();
        setting.setMonitorSysGenServerId(serverId);

        // 基础监控配置
        setting.setMonitorSysGenServerSettingMonitorEnabled(1);
        setting.setMonitorSysGenServerSettingReportEnabled(1);
        setting.setMonitorSysGenServerSettingDataReportMethod(DataReportMethod.API.getCode());
        setting.setMonitorSysGenServerSettingMonitorInterval(60);
        setting.setMonitorSysGenServerSettingDataCollectionFrequency(30);
        setting.setMonitorSysGenServerSettingMetricsRetentionDays(30);

        // 各类指标存储时间配置（默认30天）
        setting.setMonitorSysGenServerSettingCpuRetentionDays(30);
        setting.setMonitorSysGenServerSettingMemoryRetentionDays(30);
        setting.setMonitorSysGenServerSettingDiskRetentionDays(30);
        setting.setMonitorSysGenServerSettingNetworkRetentionDays(30);

        // 告警配置
        setting.setMonitorSysGenServerSettingAlertEnabled(1);
        setting.setMonitorSysGenServerSettingCpuAlertThreshold(new java.math.BigDecimal("80"));
        setting.setMonitorSysGenServerSettingMemoryAlertThreshold(new java.math.BigDecimal("85"));
        setting.setMonitorSysGenServerSettingDiskAlertThreshold(new java.math.BigDecimal("90"));
        setting.setMonitorSysGenServerSettingNetworkAlertThreshold(new java.math.BigDecimal("100"));
        setting.setMonitorSysGenServerSettingResponseTimeAlertThreshold(5000L);
        setting.setMonitorSysGenServerSettingAlertNotificationMethod(AlertNotificationMethod.EMAIL.getCode());
        setting.setMonitorSysGenServerSettingAlertSilenceDuration(30);
        setting.setMonitorSysGenServerSettingAutoRecoveryNotificationEnabled(1);

        // Docker配置
        setting.setMonitorSysGenServerSettingDockerEnabled(0);
        setting.setMonitorSysGenServerSettingDockerConnectionType(DockerConnectionType.SHELL.getCode());
        setting.setMonitorSysGenServerSettingDockerTlsEnabled(0);

        // 代理配置
        setting.setMonitorSysGenServerSettingProxyEnabled(0);
        setting.setMonitorSysGenServerSettingProxyType(ProxyType.HTTP.getCode());

        // 高级监控配置
        setting.setMonitorSysGenServerSettingProcessMonitorEnabled(0);
        setting.setMonitorSysGenServerSettingServiceMonitorEnabled(0);
        setting.setMonitorSysGenServerSettingPortMonitorEnabled(0);
        setting.setMonitorSysGenServerSettingLogMonitorEnabled(0);

        // 超时配置
        setting.setMonitorSysGenServerSettingConnectionTimeout(30);
        setting.setMonitorSysGenServerSettingReadTimeout(60);

        // 其他配置
        setting.setMonitorSysGenServerSettingPerformanceSuggestionEnabled(1);
        setting.setMonitorSysGenServerSettingStatus(1);

        return setting;
    }
}
