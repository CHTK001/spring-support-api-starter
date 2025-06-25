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
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 服务器文件上传任务队列实体类
 *
 * @author CH
 * @since 2024/12/20
 */
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "服务器文件上传任务队列")
@Schema(description = "服务器文件上传任务队列")
@Data
@TableName(value = "monitor_sys_gen_server_file_upload_queue")
public class MonitorSysGenServerFileUploadQueue extends SysBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "monitor_sys_gen_server_file_upload_queue_id", type = IdType.AUTO)
    @ApiModelProperty(value = "队列ID")
    @Schema(description = "队列ID")
    @NotNull(message = "不能为null")
    private Long monitorSysGenServerFileUploadQueueId;

    /**
     * 任务ID
     */
    @TableField(value = "monitor_sys_gen_server_file_upload_task_id")
    @ApiModelProperty(value = "任务ID")
    @Schema(description = "任务ID")
    @NotNull(message = "任务ID不能为空")
    private Long monitorSysGenServerFileUploadTaskId;

    /**
     * 服务器ID
     */
    @TableField(value = "monitor_sys_gen_server_id")
    @ApiModelProperty(value = "服务器ID")
    @Schema(description = "服务器ID")
    @NotNull(message = "服务器ID不能为空")
    private Integer monitorSysGenServerId;

    /**
     * 队列优先级
     */
    @TableField(value = "monitor_sys_gen_server_file_upload_queue_priority")
    @ApiModelProperty(value = "队列优先级")
    @Schema(description = "队列优先级(1-10，数字越小优先级越高)")
    private Integer monitorSysGenServerFileUploadQueuePriority;

    /**
     * 队列状态
     */
    @TableField(value = "monitor_sys_gen_server_file_upload_queue_status")
    @ApiModelProperty(value = "队列状态")
    @Schema(description = "队列状态(PENDING:待处理 PROCESSING:处理中 COMPLETED:已完成 FAILED:失败)")
    private String monitorSysGenServerFileUploadQueueStatus;

    /**
     * 重试次数
     */
    @TableField(value = "monitor_sys_gen_server_file_upload_queue_retry_count")
    @ApiModelProperty(value = "重试次数")
    @Schema(description = "重试次数")
    private Integer monitorSysGenServerFileUploadQueueRetryCount;

    /**
     * 入队时间
     */
    @TableField(value = "monitor_sys_gen_server_file_upload_queue_create_time")
    @ApiModelProperty(value = "入队时间")
    @Schema(description = "入队时间")
    private LocalDateTime monitorSysGenServerFileUploadQueueCreateTime;

    /**
     * 开始处理时间
     */
    @TableField(value = "monitor_sys_gen_server_file_upload_queue_process_time")
    @ApiModelProperty(value = "开始处理时间")
    @Schema(description = "开始处理时间")
    private LocalDateTime monitorSysGenServerFileUploadQueueProcessTime;

    /**
     * 完成时间
     */
    @TableField(value = "monitor_sys_gen_server_file_upload_queue_complete_time")
    @ApiModelProperty(value = "完成时间")
    @Schema(description = "完成时间")
    private LocalDateTime monitorSysGenServerFileUploadQueueCompleteTime;

    /**
     * 错误信息
     */
    @TableField(value = "monitor_sys_gen_server_file_upload_queue_error_message")
    @ApiModelProperty(value = "错误信息")
    @Schema(description = "错误信息")
    private String monitorSysGenServerFileUploadQueueErrorMessage;

    /**
     * 队列状态枚举
     */
    public enum QueueStatus {
        PENDING("PENDING", "待处理"),
        PROCESSING("PROCESSING", "处理中"),
        COMPLETED("COMPLETED", "已完成"),
        FAILED("FAILED", "失败");

        private final String code;
        private final String desc;

        QueueStatus(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static QueueStatus fromCode(String code) {
            for (QueueStatus status : values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            return PENDING;
        }
    }

    /**
     * 获取队列状态描述
     */
    public String getQueueStatusDesc() {
        return QueueStatus.fromCode(this.monitorSysGenServerFileUploadQueueStatus).getDesc();
    }

    /**
     * 是否待处理
     */
    public boolean isPending() {
        return QueueStatus.PENDING.getCode().equals(this.monitorSysGenServerFileUploadQueueStatus);
    }

    /**
     * 是否处理中
     */
    public boolean isProcessing() {
        return QueueStatus.PROCESSING.getCode().equals(this.monitorSysGenServerFileUploadQueueStatus);
    }

    /**
     * 是否已完成
     */
    public boolean isCompleted() {
        return QueueStatus.COMPLETED.getCode().equals(this.monitorSysGenServerFileUploadQueueStatus);
    }

    /**
     * 是否失败
     */
    public boolean isFailed() {
        return QueueStatus.FAILED.getCode().equals(this.monitorSysGenServerFileUploadQueueStatus);
    }

    /**
     * 获取处理耗时(毫秒)
     */
    public Long getProcessDurationMs() {
        if (this.monitorSysGenServerFileUploadQueueProcessTime == null || 
            this.monitorSysGenServerFileUploadQueueCompleteTime == null) {
            return null;
        }
        
        return java.time.Duration.between(
            this.monitorSysGenServerFileUploadQueueProcessTime,
            this.monitorSysGenServerFileUploadQueueCompleteTime
        ).toMillis();
    }

    /**
     * 获取等待时间(毫秒)
     */
    public Long getWaitDurationMs() {
        if (this.monitorSysGenServerFileUploadQueueCreateTime == null || 
            this.monitorSysGenServerFileUploadQueueProcessTime == null) {
            return null;
        }
        
        return java.time.Duration.between(
            this.monitorSysGenServerFileUploadQueueCreateTime,
            this.monitorSysGenServerFileUploadQueueProcessTime
        ).toMillis();
    }

    /**
     * 是否有错误
     */
    public boolean hasError() {
        return this.monitorSysGenServerFileUploadQueueErrorMessage != null && 
               !this.monitorSysGenServerFileUploadQueueErrorMessage.trim().isEmpty();
    }

    /**
     * 是否可以重试
     */
    public boolean canRetry() {
        return isFailed() && (this.monitorSysGenServerFileUploadQueueRetryCount == null || 
                             this.monitorSysGenServerFileUploadQueueRetryCount < 3);
    }
}
