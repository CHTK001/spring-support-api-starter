package com.chua.starter.monitor.starter.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 服务器文件上传进度信息实体类
 *
 * @author CH
 * @since 2024/12/20
 */
@ApiModel(description = "服务器文件上传进度信息")
@Schema(description = "服务器文件上传进度信息")
@Data
public class MonitorSysGenServerFileUploadProgress implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    @ApiModelProperty(value = "任务ID")
    @Schema(description = "任务ID")
    private Long taskId;

    /**
     * 记录ID
     */
    @ApiModelProperty(value = "记录ID")
    @Schema(description = "记录ID")
    private Long recordId;

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
     * 文件总大小(字节)
     */
    @ApiModelProperty(value = "文件总大小(字节)")
    @Schema(description = "文件总大小(字节)")
    private Long totalSize;

    /**
     * 已上传大小(字节)
     */
    @ApiModelProperty(value = "已上传大小(字节)")
    @Schema(description = "已上传大小(字节)")
    private Long uploadedSize;

    /**
     * 上传进度(0-100)
     */
    @ApiModelProperty(value = "上传进度(0-100)")
    @Schema(description = "上传进度(0-100)")
    private Integer progress;

    /**
     * 上传速度(字节/秒)
     */
    @ApiModelProperty(value = "上传速度(字节/秒)")
    @Schema(description = "上传速度(字节/秒)")
    private Long uploadSpeed;

    /**
     * 剩余时间(秒)
     */
    @ApiModelProperty(value = "剩余时间(秒)")
    @Schema(description = "剩余时间(秒)")
    private Long remainingTime;

    /**
     * 上传状态
     */
    @ApiModelProperty(value = "上传状态")
    @Schema(description = "上传状态(PENDING:待上传 UPLOADING:上传中 COMPLETED:已完成 FAILED:失败 PAUSED:已暂停)")
    private String status;

    /**
     * 开始时间
     */
    @ApiModelProperty(value = "开始时间")
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 完成时间
     */
    @ApiModelProperty(value = "完成时间")
    @Schema(description = "完成时间")
    private LocalDateTime completeTime;

    /**
     * 错误信息
     */
    @ApiModelProperty(value = "错误信息")
    @Schema(description = "错误信息")
    private String errorMessage;

    /**
     * 目标路径
     */
    @ApiModelProperty(value = "目标路径")
    @Schema(description = "目标路径")
    private String targetPath;

    /**
     * 是否可以暂停
     */
    @ApiModelProperty(value = "是否可以暂停")
    @Schema(description = "是否可以暂停")
    private Boolean pausable;

    /**
     * 是否可以取消
     */
    @ApiModelProperty(value = "是否可以取消")
    @Schema(description = "是否可以取消")
    private Boolean cancellable;

    /**
     * 是否可以重试
     */
    @ApiModelProperty(value = "是否可以重试")
    @Schema(description = "是否可以重试")
    private Boolean retryable;

    /**
     * 计算上传进度
     */
    public void calculateProgress() {
        if (totalSize != null && totalSize > 0 && uploadedSize != null) {
            this.progress = (int) ((uploadedSize * 100) / totalSize);
        } else {
            this.progress = 0;
        }
    }

    /**
     * 计算剩余时间
     */
    public void calculateRemainingTime() {
        if (uploadSpeed != null && uploadSpeed > 0 && totalSize != null && uploadedSize != null) {
            long remainingBytes = totalSize - uploadedSize;
            this.remainingTime = remainingBytes / uploadSpeed;
        } else {
            this.remainingTime = null;
        }
    }

    /**
     * 获取文件大小(MB)
     */
    public Double getTotalSizeMB() {
        return totalSize != null ? totalSize / (1024.0 * 1024.0) : 0.0;
    }

    /**
     * 获取已上传大小(MB)
     */
    public Double getUploadedSizeMB() {
        return uploadedSize != null ? uploadedSize / (1024.0 * 1024.0) : 0.0;
    }

    /**
     * 获取上传速度(MB/s)
     */
    public Double getUploadSpeedMBps() {
        return uploadSpeed != null ? uploadSpeed / (1024.0 * 1024.0) : 0.0;
    }

    /**
     * 是否已完成
     */
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    /**
     * 是否失败
     */
    public boolean isFailed() {
        return "FAILED".equals(status);
    }

    /**
     * 是否正在上传
     */
    public boolean isUploading() {
        return "UPLOADING".equals(status);
    }

    /**
     * 是否已暂停
     */
    public boolean isPaused() {
        return "PAUSED".equals(status);
    }
}

