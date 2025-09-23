package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 容器统计信息实体类
 *
 * @author CH
 * @since 2025/01/15
 */
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "容器统计信息")
@Schema(description = "容器统计信息")
@Data
@TableName(value = "system_soft_container_stats")
public class SystemSoftContainerStats extends SysBase {

    /**
     * 主键ID
     */
    @TableId(value = "system_soft_container_stats_id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID")
    @Schema(description = "主键ID")
    private Integer systemSoftContainerStatsId;

    /**
     * 容器ID
     */
    @TableField(value = "system_soft_container_stats_container_id")
    @ApiModelProperty(value = "容器ID")
    @Schema(description = "容器ID")
    private String systemSoftContainerStatsContainerId;

    /**
     * 容器名称
     */
    @TableField(value = "system_soft_container_stats_container_name")
    @ApiModelProperty(value = "容器名称")
    @Schema(description = "容器名称")
    private String systemSoftContainerStatsContainerName;

    /**
     * 状态
     */
    @TableField(value = "system_soft_container_stats_status")
    @ApiModelProperty(value = "状态")
    @Schema(description = "状态")
    private String systemSoftContainerStatsStatus;

    /**
     * 启动时间
     */
    @TableField(value = "system_soft_container_stats_started_at")
    @ApiModelProperty(value = "启动时间")
    @Schema(description = "启动时间")
    private String systemSoftContainerStatsStartedAt;

    /**
     * 镜像
     */
    @TableField(value = "system_soft_container_stats_image")
    @ApiModelProperty(value = "镜像")
    @Schema(description = "镜像")
    private String systemSoftContainerStatsImage;

    /**
     * 命令
     */
    @TableField(value = "system_soft_container_stats_command")
    @ApiModelProperty(value = "命令")
    @Schema(description = "命令")
    private String systemSoftContainerStatsCommand;

    /**
     * 创建时间
     */
    @TableField(value = "system_soft_container_stats_created")
    @ApiModelProperty(value = "创建时间")
    @Schema(description = "创建时间")
    private String systemSoftContainerStatsCreated;

    /**
     * 端口映射
     */
    @TableField(value = "system_soft_container_stats_ports")
    @ApiModelProperty(value = "端口映射")
    @Schema(description = "端口映射")
    private String systemSoftContainerStatsPorts;

    /**
     * 标签
     */
    @TableField(value = "system_soft_container_stats_labels")
    @ApiModelProperty(value = "标签")
    @Schema(description = "标签")
    private String systemSoftContainerStatsLabels;

    /**
     * 状态详情
     */
    @TableField(value = "system_soft_container_stats_status_detail")
    @ApiModelProperty(value = "状态详情")
    @Schema(description = "状态详情")
    private String systemSoftContainerStatsStatusDetail;

    /**
     * 记录创建时间
     */
    @TableField(value = "system_soft_container_stats_record_time")
    @ApiModelProperty(value = "记录创建时间")
    @Schema(description = "记录创建时间")
    private LocalDateTime systemSoftContainerStatsRecordTime;

    /**
     * 磁盘读取字节数
     */
    @TableField(value = "system_soft_container_stats_disk_read")
    @ApiModelProperty(value = "磁盘读取字节数")
    @Schema(description = "磁盘读取字节数")
    private Long systemSoftContainerStatsDiskRead;

    /**
     * 磁盘写入字节数
     */
    @TableField(value = "system_soft_container_stats_disk_write")
    @ApiModelProperty(value = "磁盘写入字节数")
    @Schema(description = "磁盘写入字节数")
    private Long systemSoftContainerStatsDiskWrite;

    /**
     * CPU使用率 (%)
     */
    @TableField(value = "system_soft_container_stats_cpu_percent")
    @ApiModelProperty(value = "CPU使用率 (%)")
    @Schema(description = "CPU使用率 (%)")
    private Double systemSoftContainerStatsCpuPercent;

    /**
     * 内存使用字节数
     */
    @TableField(value = "system_soft_container_stats_memory_usage")
    @ApiModelProperty(value = "内存使用字节数")
    @Schema(description = "内存使用字节数")
    private Long systemSoftContainerStatsMemoryUsage;

    /**
     * 内存限制字节数
     */
    @TableField(value = "system_soft_container_stats_memory_limit")
    @ApiModelProperty(value = "内存限制字节数")
    @Schema(description = "内存限制字节数")
    private Long systemSoftContainerStatsMemoryLimit;

    /**
     * 内存使用率 (%)
     */
    @TableField(value = "system_soft_container_stats_memory_percent")
    @ApiModelProperty(value = "内存使用率 (%)")
    @Schema(description = "内存使用率 (%)")
    private Double systemSoftContainerStatsMemoryPercent;

    /**
     * 网络接收字节数
     */
    @TableField(value = "system_soft_container_stats_network_rx_bytes")
    @ApiModelProperty(value = "网络接收字节数")
    @Schema(description = "网络接收字节数")
    private Long systemSoftContainerStatsNetworkRxBytes;

    /**
     * 网络发送字节数
     */
    @TableField(value = "system_soft_container_stats_network_tx_bytes")
    @ApiModelProperty(value = "网络发送字节数")
    @Schema(description = "网络发送字节数")
    private Long systemSoftContainerStatsNetworkTxBytes;

    /**
     * 容器关联ID
     */
    @TableField(value = "system_soft_container_id")
    @ApiModelProperty(value = "容器关联ID")
    @Schema(description = "容器关联ID")
    private Integer systemSoftContainerId;

    /**
     * 镜像关联ID
     */
    @TableField(value = "system_soft_image_id")
    @ApiModelProperty(value = "镜像关联ID")
    @Schema(description = "镜像关联ID")
    private Integer systemSoftImageId;
}