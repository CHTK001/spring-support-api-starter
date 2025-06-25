package com.chua.starter.monitor.starter.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 服务器文件上传任务统计信息实体类
 *
 * @author CH
 * @since 2024/12/20
 */
@ApiModel(description = "服务器文件上传任务统计信息")
@Schema(description = "服务器文件上传任务统计信息")
@Data
public class MonitorSysGenServerFileUploadTaskStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 统计开始时间
     */
    @ApiModelProperty(value = "统计开始时间")
    @Schema(description = "统计开始时间")
    private LocalDateTime startTime;

    /**
     * 统计结束时间
     */
    @ApiModelProperty(value = "统计结束时间")
    @Schema(description = "统计结束时间")
    private LocalDateTime endTime;

    /**
     * 总任务数
     */
    @ApiModelProperty(value = "总任务数")
    @Schema(description = "总任务数")
    private Long totalTasks;

    /**
     * 成功任务数
     */
    @ApiModelProperty(value = "成功任务数")
    @Schema(description = "成功任务数")
    private Long successTasks;

    /**
     * 失败任务数
     */
    @ApiModelProperty(value = "失败任务数")
    @Schema(description = "失败任务数")
    private Long failedTasks;

    /**
     * 进行中任务数
     */
    @ApiModelProperty(value = "进行中任务数")
    @Schema(description = "进行中任务数")
    private Long processingTasks;

    /**
     * 待处理任务数
     */
    @ApiModelProperty(value = "待处理任务数")
    @Schema(description = "待处理任务数")
    private Long pendingTasks;

    /**
     * 总文件大小(字节)
     */
    @ApiModelProperty(value = "总文件大小(字节)")
    @Schema(description = "总文件大小(字节)")
    private Long totalFileSize;

    /**
     * 成功上传文件大小(字节)
     */
    @ApiModelProperty(value = "成功上传文件大小(字节)")
    @Schema(description = "成功上传文件大小(字节)")
    private Long successFileSize;

    /**
     * 平均上传速度(字节/秒)
     */
    @ApiModelProperty(value = "平均上传速度(字节/秒)")
    @Schema(description = "平均上传速度(字节/秒)")
    private Double avgUploadSpeed;

    /**
     * 平均任务执行时间(毫秒)
     */
    @ApiModelProperty(value = "平均任务执行时间(毫秒)")
    @Schema(description = "平均任务执行时间(毫秒)")
    private Double avgExecutionTime;

    /**
     * 成功率(百分比)
     */
    @ApiModelProperty(value = "成功率(百分比)")
    @Schema(description = "成功率(百分比)")
    private Double successRate;

    /**
     * 失败率(百分比)
     */
    @ApiModelProperty(value = "失败率(百分比)")
    @Schema(description = "失败率(百分比)")
    private Double failureRate;

    /**
     * 重试次数
     */
    @ApiModelProperty(value = "重试次数")
    @Schema(description = "重试次数")
    private Long totalRetries;

    /**
     * 最大文件大小(字节)
     */
    @ApiModelProperty(value = "最大文件大小(字节)")
    @Schema(description = "最大文件大小(字节)")
    private Long maxFileSize;

    /**
     * 最小文件大小(字节)
     */
    @ApiModelProperty(value = "最小文件大小(字节)")
    @Schema(description = "最小文件大小(字节)")
    private Long minFileSize;

    /**
     * 计算成功率和失败率
     */
    public void calculateRates() {
        if (totalTasks != null && totalTasks > 0) {
            this.successRate = (successTasks != null ? successTasks : 0) * 100.0 / totalTasks;
            this.failureRate = (failedTasks != null ? failedTasks : 0) * 100.0 / totalTasks;
        } else {
            this.successRate = 0.0;
            this.failureRate = 0.0;
        }
    }

    /**
     * 获取总文件大小(MB)
     */
    public Double getTotalFileSizeMB() {
        return totalFileSize != null ? totalFileSize / (1024.0 * 1024.0) : 0.0;
    }

    /**
     * 获取成功上传文件大小(MB)
     */
    public Double getSuccessFileSizeMB() {
        return successFileSize != null ? successFileSize / (1024.0 * 1024.0) : 0.0;
    }
}

