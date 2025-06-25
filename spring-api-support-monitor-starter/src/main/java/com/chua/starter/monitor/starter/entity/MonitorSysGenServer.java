package com.chua.starter.monitor.starter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 服务器配置实体类
 *
 * @author CH
 * @since 2024/12/18
 */
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "服务器配置")
@Schema(description = "服务器配置")
@Data
@TableName(value = "monitor_sys_gen_server")
public class MonitorSysGenServer extends SysBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "monitor_sys_gen_server_id", type = IdType.AUTO)
    @ApiModelProperty(value = "服务器配置ID")
    @Schema(description = "服务器配置ID")
    @NotNull(message = "不能为null")
    private Integer monitorSysGenServerId;

    /**
     * 服务器名称
     */
    @TableField(value = "monitor_sys_gen_server_name")
    @ApiModelProperty(value = "服务器名称")
    @Schema(description = "服务器名称")
    @Size(max = 255, message = "服务器名称最大长度要小于 255")
    private String monitorSysGenServerName;

    /**
     * 服务器主机地址
     */
    @TableField(value = "monitor_sys_gen_server_host")
    @ApiModelProperty(value = "服务器主机地址")
    @Schema(description = "服务器主机地址")
    @Size(max = 255, message = "服务器主机地址最大长度要小于 255")
    private String monitorSysGenServerHost;

    /**
     * 服务器端口
     */
    @TableField(value = "monitor_sys_gen_server_port")
    @ApiModelProperty(value = "服务器端口")
    @Schema(description = "服务器端口")
    private Integer monitorSysGenServerPort;

    /**
     * 连接协议 (SSH, RDP, TELNET, VNC等)
     */
    @TableField(value = "monitor_sys_gen_server_protocol")
    @ApiModelProperty(value = "连接协议")
    @Schema(description = "连接协议")
    @Size(max = 50, message = "连接协议最大长度要小于 50")
    private String monitorSysGenServerProtocol;

    /**
     * 用户名
     */
    @TableField(value = "monitor_sys_gen_server_username")
    @ApiModelProperty(value = "用户名")
    @Schema(description = "用户名")
    @Size(max = 255, message = "用户名最大长度要小于 255")
    private String monitorSysGenServerUsername;

    /**
     * 密码
     */
    @TableField(value = "monitor_sys_gen_server_password")
    @ApiModelProperty(value = "密码")
    @Schema(description = "密码")
    @Size(max = 255, message = "密码最大长度要小于 255")
    private String monitorSysGenServerPassword;

    /**
     * 私钥文件路径 (SSH协议使用)
     */
    @TableField(value = "monitor_sys_gen_server_private_key")
    @ApiModelProperty(value = "私钥文件路径")
    @Schema(description = "私钥文件路径")
    @Size(max = 500, message = "私钥文件路径最大长度要小于 500")
    private String monitorSysGenServerPrivateKey;

    /**
     * 指标存储时间(天)
     */
    @TableField(value = "monitor_sys_gen_server_metrics_retention_days")
    @ApiModelProperty(value = "指标存储时间(天)")
    @Schema(description = "指标存储时间(天)")
    private Integer monitorSysGenServerMetricsRetentionDays;

    /**
     * 监控间隔(秒)
     */
    @TableField(value = "monitor_sys_gen_server_monitor_interval")
    @ApiModelProperty(value = "监控间隔(秒)")
    @Schema(description = "监控间隔(秒)")
    private Integer monitorSysGenServerMonitorInterval;

    /**
     * 连接超时时间(毫秒)
     */
    @TableField(value = "monitor_sys_gen_server_timeout")
    @ApiModelProperty(value = "连接超时时间(毫秒)")
    @Schema(description = "连接超时时间(毫秒)")
    private Integer monitorSysGenServerTimeout;

    /**
     * 服务器描述
     */
    @TableField(value = "monitor_sys_gen_server_desc")
    @ApiModelProperty(value = "服务器描述")
    @Schema(description = "服务器描述")
    @Size(max = 500, message = "服务器描述最大长度要小于 500")
    private String monitorSysGenServerDesc;

    /**
     * 服务器状态 0:停用 1:启用
     */
    @TableField(value = "monitor_sys_gen_server_status")
    @ApiModelProperty(value = "服务器状态")
    @Schema(description = "服务器状态")
    private Integer monitorSysGenServerStatus;

    /**
     * 是否启用监控 0:否 1:是
     */
    @TableField(value = "monitor_sys_gen_server_monitor_enabled")
    @ApiModelProperty(value = "是否启用监控")
    @Schema(description = "是否启用监控")
    private Integer monitorSysGenServerMonitorEnabled;

    /**
     * 服务器标签 (用于分组)
     */
    @TableField(value = "monitor_sys_gen_server_tags")
    @ApiModelProperty(value = "服务器标签")
    @Schema(description = "服务器标签")
    @Size(max = 500, message = "服务器标签最大长度要小于 500")
    private String monitorSysGenServerTags;

    /**
     * 连接状态 0:离线 1:在线 2:连接中 3:连接失败
     */
    @TableField(value = "monitor_sys_gen_server_connection_status")
    @ApiModelProperty(value = "连接状态")
    @Schema(description = "连接状态")
    private Integer monitorSysGenServerConnectionStatus;

    /**
     * 最后连接时间
     */
    @TableField(value = "monitor_sys_gen_server_last_connect_time")
    @ApiModelProperty(value = "最后连接时间")
    @Schema(description = "最后连接时间")
    private LocalDateTime monitorSysGenServerLastConnectTime;

    /**
     * 连接失败原因
     */
    @TableField(value = "monitor_sys_gen_server_connection_error")
    @ApiModelProperty(value = "连接失败原因")
    @Schema(description = "连接失败原因")
    @Size(max = 1000, message = "连接失败原因最大长度要小于 1000")
    private String monitorSysGenServerConnectionError;





    /**
     * 是否需要上报 0:否 1:是 (默认上报)
     */
    @TableField(value = "monitor_sys_gen_server_report_enabled")
    @ApiModelProperty(value = "是否需要上报")
    @Schema(description = "是否需要上报")
    private Integer monitorSysGenServerReportEnabled;



    /**
     * 数据上报方式 (NONE:不支持上报, LOCAL:本地上报, API:接口上报, PROMETHEUS:prometheus)
     */
    @TableField(value = "monitor_sys_gen_server_data_report_method")
    @ApiModelProperty(value = "数据上报方式")
    @Schema(description = "数据上报方式")
    @Size(max = 20, message = "数据上报方式最大长度要小于 20")
    private String monitorSysGenServerDataReportMethod;

    /**
     * Prometheus服务器地址
     */
    @TableField(value = "monitor_sys_gen_server_prometheus_host")
    @ApiModelProperty(value = "Prometheus服务器地址")
    @Schema(description = "Prometheus服务器地址")
    @Size(max = 255, message = "Prometheus服务器地址最大长度要小于 255")
    private String monitorSysGenServerPrometheusHost;

    /**
     * Prometheus服务器端口
     */
    @TableField(value = "monitor_sys_gen_server_prometheus_port")
    @ApiModelProperty(value = "Prometheus服务器端口")
    @Schema(description = "Prometheus服务器端口")
    private Integer monitorSysGenServerPrometheusPort;

    /**
     * 代理ID (关联MonitorProxy表)
     */
    @TableField(value = "monitor_sys_gen_server_proxy_id")
    @ApiModelProperty(value = "代理ID")
    @Schema(description = "代理ID")
    private Integer monitorSysGenServerProxyId;

    /**
     * 是否本地服务器 0:否 1:是 (自动检测，不允许修改)
     */
    @TableField(value = "monitor_sys_gen_server_is_local")
    @ApiModelProperty(value = "是否本地服务器")
    @Schema(description = "是否本地服务器")
    private Integer monitorSysGenServerIsLocal;

    /**
     * 服务器IP地址列表 (JSON格式，支持多个IP)
     */
    @TableField(value = "monitor_sys_gen_server_ip_addresses")
    @ApiModelProperty(value = "服务器IP地址列表")
    @Schema(description = "服务器IP地址列表")
    @Size(max = 1000, message = "服务器IP地址列表最大长度要小于 1000")
    private String monitorSysGenServerIpAddresses;

    /**
     * 是否支持Docker 0:否 1:是
     */
    @TableField(value = "monitor_sys_gen_server_docker_enabled")
    @ApiModelProperty(value = "是否支持Docker")
    @Schema(description = "是否支持Docker")
    private Integer monitorSysGenServerDockerEnabled;

    /**
     * Docker连接方式 (SHELL:命令行, TCP:TCP连接)
     */
    @TableField(value = "monitor_sys_gen_server_docker_connection_type")
    @ApiModelProperty(value = "Docker连接方式")
    @Schema(description = "Docker连接方式")
    @Size(max = 20, message = "Docker连接方式最大长度要小于 20")
    private String monitorSysGenServerDockerConnectionType;

    /**
     * Docker TCP连接地址
     */
    @TableField(value = "monitor_sys_gen_server_docker_host")
    @ApiModelProperty(value = "Docker TCP连接地址")
    @Schema(description = "Docker TCP连接地址")
    @Size(max = 255, message = "Docker TCP连接地址最大长度要小于 255")
    private String monitorSysGenServerDockerHost;

    /**
     * Docker TCP连接端口
     */
    @TableField(value = "monitor_sys_gen_server_docker_port")
    @ApiModelProperty(value = "Docker TCP连接端口")
    @Schema(description = "Docker TCP连接端口")
    private Integer monitorSysGenServerDockerPort;

    /**
     * 操作系统类型 (自动检测)
     */
    @TableField(value = "monitor_sys_gen_server_os_type")
    @ApiModelProperty(value = "操作系统类型")
    @Schema(description = "操作系统类型")
    @Size(max = 50, message = "操作系统类型最大长度要小于 50")
    private String monitorSysGenServerOsType;

    /**
     * 操作系统版本 (自动检测)
     */
    @TableField(value = "monitor_sys_gen_server_os_version")
    @ApiModelProperty(value = "操作系统版本")
    @Schema(description = "操作系统版本")
    @Size(max = 100, message = "操作系统版本最大长度要小于 100")
    private String monitorSysGenServerOsVersion;

    /**
     * 操作系统架构 (自动检测)
     */
    @TableField(value = "monitor_sys_gen_server_os_arch")
    @ApiModelProperty(value = "操作系统架构")
    @Schema(description = "操作系统架构")
    @Size(max = 50, message = "操作系统架构最大长度要小于 50")
    private String monitorSysGenServerOsArch;

    /**
     * 网络延迟(毫秒)
     */
    @TableField(value = "monitor_sys_gen_server_latency")
    @ApiModelProperty(value = "网络延迟(毫秒)")
    @Schema(description = "网络延迟(毫秒)")
    private Long monitorSysGenServerLatency;

    /**
     * 延迟检测状态 0:未检测 1:正常 2:延迟较高 3:检测失败
     */
    @TableField(value = "monitor_sys_gen_server_latency_status")
    @ApiModelProperty(value = "延迟检测状态")
    @Schema(description = "延迟检测状态 0:未检测 1:正常 2:延迟较高 3:检测失败")
    private Integer monitorSysGenServerLatencyStatus;

    /**
     * 最后延迟检测时间
     */
    @TableField(value = "monitor_sys_gen_server_last_latency_check_time")
    @ApiModelProperty(value = "最后延迟检测时间")
    @Schema(description = "最后延迟检测时间")
    private LocalDateTime monitorSysGenServerLastLatencyCheckTime;

    /**
     * 平均延迟(毫秒) - 最近10次检测的平均值
     */
    @TableField(value = "monitor_sys_gen_server_avg_latency")
    @ApiModelProperty(value = "平均延迟(毫秒)")
    @Schema(description = "平均延迟(毫秒) - 最近10次检测的平均值")
    private Long monitorSysGenServerAvgLatency;

    /**
     * 最大延迟(毫秒) - 最近10次检测的最大值
     */
    @TableField(value = "monitor_sys_gen_server_max_latency")
    @ApiModelProperty(value = "最大延迟(毫秒)")
    @Schema(description = "最大延迟(毫秒) - 最近10次检测的最大值")
    private Long monitorSysGenServerMaxLatency;

    /**
     * 最小延迟(毫秒) - 最近10次检测的最小值
     */
    @TableField(value = "monitor_sys_gen_server_min_latency")
    @ApiModelProperty(value = "最小延迟(毫秒)")
    @Schema(description = "最小延迟(毫秒) - 最近10次检测的最小值")
    private Long monitorSysGenServerMinLatency;

    /**
     * 延迟检测失败次数
     */
    @TableField(value = "monitor_sys_gen_server_latency_fail_count")
    @ApiModelProperty(value = "延迟检测失败次数")
    @Schema(description = "延迟检测失败次数")
    private Integer monitorSysGenServerLatencyFailCount;

    /**
     * 延迟检测总次数
     */
    @TableField(value = "monitor_sys_gen_server_latency_total_count")
    @ApiModelProperty(value = "延迟检测总次数")
    @Schema(description = "延迟检测总次数")
    private Integer monitorSysGenServerLatencyTotalCount;

    /**
     * 延迟状态枚举
     */
    public enum LatencyStatus {
        NOT_CHECKED(0, "未检测"),
        NORMAL(1, "正常"),
        HIGH_LATENCY(2, "延迟较高"),
        CHECK_FAILED(3, "检测失败");

        private final int code;
        private final String desc;

        LatencyStatus(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public int getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static LatencyStatus fromCode(int code) {
            for (LatencyStatus status : values()) {
                if (status.code == code) {
                    return status;
                }
            }
            return NOT_CHECKED;
        }
    }

    /**
     * 获取延迟状态描述
     */
    public String getLatencyStatusDesc() {
        if (this.monitorSysGenServerLatencyStatus == null) {
            return LatencyStatus.NOT_CHECKED.getDesc();
        }
        return LatencyStatus.fromCode(this.monitorSysGenServerLatencyStatus).getDesc();
    }

    /**
     * 判断延迟是否正常
     * 延迟小于100ms认为正常
     */
    public boolean isLatencyNormal() {
        return this.monitorSysGenServerLatency != null && this.monitorSysGenServerLatency < 100;
    }

    /**
     * 判断延迟是否较高
     * 延迟大于等于100ms且小于500ms认为较高
     */
    public boolean isLatencyHigh() {
        return this.monitorSysGenServerLatency != null
                && this.monitorSysGenServerLatency >= 100
                && this.monitorSysGenServerLatency < 500;
    }

    /**
     * 判断延迟是否异常
     * 延迟大于等于500ms认为异常
     */
    public boolean isLatencyAbnormal() {
        return this.monitorSysGenServerLatency != null && this.monitorSysGenServerLatency >= 500;
    }

    /**
     * 获取延迟成功率
     */
    public double getLatencySuccessRate() {
        if (this.monitorSysGenServerLatencyTotalCount == null || this.monitorSysGenServerLatencyTotalCount == 0) {
            return 0.0;
        }
        int successCount = this.monitorSysGenServerLatencyTotalCount -
                (this.monitorSysGenServerLatencyFailCount != null ? this.monitorSysGenServerLatencyFailCount : 0);
        return (double) successCount / this.monitorSysGenServerLatencyTotalCount * 100;
    }
}
