package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 服务器指标数据实体类
 *
 * @author CH
 * @since 2024/12/18
 */
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "服务器指标数据")
@Schema(description = "服务器指标数据")
@Data
@TableName(value = "monitor_sys_gen_server_metrics")
public class MonitorSysGenServerMetrics extends SysBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "monitor_sys_gen_server_metrics_id", type = IdType.AUTO)
    @ApiModelProperty(value = "指标数据ID")
    @Schema(description = "指标数据ID")
    @NotNull(message = "不能为null")
    private Long monitorSysGenServerMetricsId;

    /**
     * 服务器ID
     */
    @TableField(value = "monitor_sys_gen_server_id")
    @ApiModelProperty(value = "服务器ID")
    @Schema(description = "服务器ID")
    private Integer monitorSysGenServerId;

    /**
     * 抓取时间
     */
    @TableField(value = "monitor_sys_gen_server_metrics_collect_time")
    @ApiModelProperty(value = "抓取时间")
    @Schema(description = "抓取时间")
    private LocalDateTime monitorSysGenServerMetricsCollectTime;

    /**
     * CPU使用率 (百分比)
     */
    @TableField(value = "monitor_sys_gen_server_metrics_cpu_usage")
    @ApiModelProperty(value = "CPU使用率")
    @Schema(description = "CPU使用率")
    private BigDecimal monitorSysGenServerMetricsCpuUsage;

    /**
     * CPU核心数
     */
    @TableField(value = "monitor_sys_gen_server_metrics_cpu_cores")
    @ApiModelProperty(value = "CPU核心数")
    @Schema(description = "CPU核心数")
    private Integer monitorSysGenServerMetricsCpuCores;

    /**
     * CPU负载 (1分钟)
     */
    @TableField(value = "monitor_sys_gen_server_metrics_cpu_load_1m")
    @ApiModelProperty(value = "CPU负载1分钟")
    @Schema(description = "CPU负载1分钟")
    private BigDecimal monitorSysGenServerMetricsCpuLoad1m;

    /**
     * CPU负载 (5分钟)
     */
    @TableField(value = "monitor_sys_gen_server_metrics_cpu_load_5m")
    @ApiModelProperty(value = "CPU负载5分钟")
    @Schema(description = "CPU负载5分钟")
    private BigDecimal monitorSysGenServerMetricsCpuLoad5m;

    /**
     * CPU负载 (15分钟)
     */
    @TableField(value = "monitor_sys_gen_server_metrics_cpu_load_15m")
    @ApiModelProperty(value = "CPU负载15分钟")
    @Schema(description = "CPU负载15分钟")
    private BigDecimal monitorSysGenServerMetricsCpuLoad15m;

    /**
     * 内存总量 (MB)
     */
    @TableField(value = "monitor_sys_gen_server_metrics_memory_total")
    @ApiModelProperty(value = "内存总量")
    @Schema(description = "内存总量")
    private Long monitorSysGenServerMetricsMemoryTotal;

    /**
     * 内存使用量 (MB)
     */
    @TableField(value = "monitor_sys_gen_server_metrics_memory_used")
    @ApiModelProperty(value = "内存使用量")
    @Schema(description = "内存使用量")
    private Long monitorSysGenServerMetricsMemoryUsed;

    /**
     * 内存可用量 (MB)
     */
    @TableField(value = "monitor_sys_gen_server_metrics_memory_free")
    @ApiModelProperty(value = "内存可用量")
    @Schema(description = "内存可用量")
    private Long monitorSysGenServerMetricsMemoryFree;

    /**
     * 内存使用率 (百分比)
     */
    @TableField(value = "monitor_sys_gen_server_metrics_memory_usage")
    @ApiModelProperty(value = "内存使用率")
    @Schema(description = "内存使用率")
    private BigDecimal monitorSysGenServerMetricsMemoryUsage;

    /**
     * 磁盘总量 (GB)
     */
    @TableField(value = "monitor_sys_gen_server_metrics_disk_total")
    @ApiModelProperty(value = "磁盘总量")
    @Schema(description = "磁盘总量")
    private Long monitorSysGenServerMetricsDiskTotal;

    /**
     * 磁盘使用量 (GB)
     */
    @TableField(value = "monitor_sys_gen_server_metrics_disk_used")
    @ApiModelProperty(value = "磁盘使用量")
    @Schema(description = "磁盘使用量")
    private Long monitorSysGenServerMetricsDiskUsed;

    /**
     * 磁盘可用量 (GB)
     */
    @TableField(value = "monitor_sys_gen_server_metrics_disk_free")
    @ApiModelProperty(value = "磁盘可用量")
    @Schema(description = "磁盘可用量")
    private Long monitorSysGenServerMetricsDiskFree;

    /**
     * 磁盘使用率 (百分比)
     */
    @TableField(value = "monitor_sys_gen_server_metrics_disk_usage")
    @ApiModelProperty(value = "磁盘使用率")
    @Schema(description = "磁盘使用率")
    private BigDecimal monitorSysGenServerMetricsDiskUsage;

    /**
     * 网络入流量 (KB/s)
     */
    @TableField(value = "monitor_sys_gen_server_metrics_network_in")
    @ApiModelProperty(value = "网络入流量")
    @Schema(description = "网络入流量")
    private Long monitorSysGenServerMetricsNetworkIn;

    /**
     * 网络出流量 (KB/s)
     */
    @TableField(value = "monitor_sys_gen_server_metrics_network_out")
    @ApiModelProperty(value = "网络出流量")
    @Schema(description = "网络出流量")
    private Long monitorSysGenServerMetricsNetworkOut;

    /**
     * 系统运行时间 (秒)
     */
    @TableField(value = "monitor_sys_gen_server_metrics_uptime")
    @ApiModelProperty(value = "系统运行时间")
    @Schema(description = "系统运行时间")
    private Long monitorSysGenServerMetricsUptime;

    /**
     * 进程数量
     */
    @TableField(value = "monitor_sys_gen_server_metrics_process_count")
    @ApiModelProperty(value = "进程数量")
    @Schema(description = "进程数量")
    private Integer monitorSysGenServerMetricsProcessCount;

    /**
     * 连接状态 0:离线 1:在线
     */
    @TableField(value = "monitor_sys_gen_server_metrics_status")
    @ApiModelProperty(value = "连接状态")
    @Schema(description = "连接状态")
    private Integer monitorSysGenServerMetricsStatus;

    /**
     * 响应时间 (毫秒)
     */
    @TableField(value = "monitor_sys_gen_server_metrics_response_time")
    @ApiModelProperty(value = "响应时间")
    @Schema(description = "响应时间")
    private Long monitorSysGenServerMetricsResponseTime;

    /**
     * 操作系统信息
     */
    @TableField(value = "monitor_sys_gen_server_metrics_os_info")
    @ApiModelProperty(value = "操作系统信息")
    @Schema(description = "操作系统信息")
    private String monitorSysGenServerMetricsOsInfo;

    /**
     * 扩展信息 (JSON格式)
     */
    @TableField(value = "monitor_sys_gen_server_metrics_extra_info")
    @ApiModelProperty(value = "扩展信息")
    @Schema(description = "扩展信息")
    private String monitorSysGenServerMetricsExtraInfo;
}
