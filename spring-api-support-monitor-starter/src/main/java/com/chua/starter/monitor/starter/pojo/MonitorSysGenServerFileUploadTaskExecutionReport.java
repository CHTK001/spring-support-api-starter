package com.chua.starter.monitor.starter.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 服务器文件上传任务执行报告实体类
 *
 * @author CH
 * @since 2024/12/20
 */
@ApiModel(description = "服务器文件上传任务执行报告")
@Schema(description = "服务器文件上传任务执行报告")
@Data
public class MonitorSysGenServerFileUploadTaskExecutionReport implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    @ApiModelProperty(value = "任务ID")
    @Schema(description = "任务ID")
    private Long taskId;

    /**
     * 任务名称
     */
    @ApiModelProperty(value = "任务名称")
    @Schema(description = "任务名称")
    private String taskName;

    /**
     * 服务器ID
     */
    @ApiModelProperty(value = "服务器ID")
    @Schema(description = "服务器ID")
    private Integer serverId;

    /**
     * 服务器名称
     */
    @ApiModelProperty(value = "服务器名称")
    @Schema(description = "服务器名称")
    private String serverName;

    /**
     * 文件名称
     */
    @ApiModelProperty(value = "文件名称")
    @Schema(description = "文件名称")
    private String fileName;

    /**
     * 文件大小(字节)
     */
    @ApiModelProperty(value = "文件大小(字节)")
    @Schema(description = "文件大小(字节)")
    private Long fileSize;

    /**
     * 目标路径
     */
    @ApiModelProperty(value = "目标路径")
    @Schema(description = "目标路径")
    private String targetPath;

    /**
     * 任务状态
     */
    @ApiModelProperty(value = "任务状态")
    @Schema(description = "任务状态")
    private String status;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 开始时间
     */
    @ApiModelProperty(value = "开始时间")
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    /**
     * 完成时间
     */
    @ApiModelProperty(value = "完成时间")
    @Schema(description = "完成时间")
    private LocalDateTime completeTime;

    /**
     * 执行耗时(毫秒)
     */
    @ApiModelProperty(value = "执行耗时(毫秒)")
    @Schema(description = "执行耗时(毫秒)")
    private Long executionDuration;

    /**
     * 上传速度(字节/秒)
     */
    @ApiModelProperty(value = "上传速度(字节/秒)")
    @Schema(description = "上传速度(字节/秒)")
    private Long uploadSpeed;

    /**
     * 重试次数
     */
    @ApiModelProperty(value = "重试次数")
    @Schema(description = "重试次数")
    private Integer retryCount;

    /**
     * 错误信息
     */
    @ApiModelProperty(value = "错误信息")
    @Schema(description = "错误信息")
    private String errorMessage;

    /**
     * 队列等待时间(毫秒)
     */
    @ApiModelProperty(value = "队列等待时间(毫秒)")
    @Schema(description = "队列等待时间(毫秒)")
    private Long queueWaitDuration;

    /**
     * 网络延迟(毫秒)
     */
    @ApiModelProperty(value = "网络延迟(毫秒)")
    @Schema(description = "网络延迟(毫秒)")
    private Long networkLatency;

    /**
     * CPU使用率(%)
     */
    @ApiModelProperty(value = "CPU使用率(%)")
    @Schema(description = "CPU使用率(%)")
    private Double cpuUsage;

    /**
     * 内存使用率(%)
     */
    @ApiModelProperty(value = "内存使用率(%)")
    @Schema(description = "内存使用率(%)")
    private Double memoryUsage;

    /**
     * 磁盘使用率(%)
     */
    @ApiModelProperty(value = "磁盘使用率(%)")
    @Schema(description = "磁盘使用率(%)")
    private Double diskUsage;

    /**
     * 网络带宽使用率(%)
     */
    @ApiModelProperty(value = "网络带宽使用率(%)")
    @Schema(description = "网络带宽使用率(%)")
    private Double networkUsage;

    /**
     * 是否成功
     */
    @ApiModelProperty(value = "是否成功")
    @Schema(description = "是否成功")
    private Boolean success;

    /**
     * 性能评分(1-100)
     */
    @ApiModelProperty(value = "性能评分(1-100)")
    @Schema(description = "性能评分(1-100)")
    private Integer performanceScore;

    /**
     * 建议优化项
     */
    @ApiModelProperty(value = "建议优化项")
    @Schema(description = "建议优化项")
    private String optimizationSuggestions;

    /**
     * 计算执行耗时
     */
    public void calculateExecutionDuration() {
        if (startTime != null && completeTime != null) {
            this.executionDuration = java.time.Duration.between(startTime, completeTime).toMillis();
        }
    }

    /**
     * 计算上传速度
     */
    public void calculateUploadSpeed() {
        if (fileSize != null && executionDuration != null && executionDuration > 0) {
            this.uploadSpeed = fileSize * 1000 / executionDuration; // 字节/秒
        }
    }

    /**
     * 获取文件大小(MB)
     */
    public Double getFileSizeMB() {
        return fileSize != null ? fileSize / (1024.0 * 1024.0) : 0.0;
    }

    /**
     * 获取上传速度(MB/s)
     */
    public Double getUploadSpeedMBps() {
        return uploadSpeed != null ? uploadSpeed / (1024.0 * 1024.0) : 0.0;
    }

    /**
     * 获取执行耗时(秒)
     */
    public Double getExecutionDurationSeconds() {
        return executionDuration != null ? executionDuration / 1000.0 : 0.0;
    }

    /**
     * 是否执行成功
     */
    public boolean isExecutionSuccessful() {
        return success != null && success;
    }

    /**
     * 是否有重试
     */
    public boolean hasRetries() {
        return retryCount != null && retryCount > 0;
    }

    /**
     * 是否性能良好
     */
    public boolean isPerformanceGood() {
        return performanceScore != null && performanceScore >= 80;
    }
}
