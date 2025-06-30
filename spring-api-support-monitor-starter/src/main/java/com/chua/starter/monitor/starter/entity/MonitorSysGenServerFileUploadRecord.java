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

import java.time.LocalDateTime;

/**
 * 服务器文件上传记录实体类
 *
 * @author CH
 * @since 2024/12/20
 */
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "服务器文件上传记录")
@Schema(description = "服务器文件上传记录")
@Data
@TableName(value = "monitor_sys_gen_server_file_upload_records")
public class MonitorSysGenServerFileUploadRecord extends SysBase {

    @TableId(value = "monitor_sys_gen_server_file_upload_record_id", type = IdType.AUTO)
    @ApiModelProperty(value = "记录ID")
    @Schema(description = "记录ID")
    @NotNull(message = "不能为null")
    private Long monitorSysGenServerFileUploadRecordId;

    /**
     * 关联任务ID
     */
    @TableField(value = "monitor_sys_gen_server_file_upload_task_id")
    @ApiModelProperty(value = "关联任务ID")
    @Schema(description = "关联任务ID")
    @NotNull(message = "任务ID不能为空")
    private Long monitorSysGenServerFileUploadTaskId;

    /**
     * 目标服务器ID
     */
    @TableField(value = "monitor_sys_gen_server_id")
    @ApiModelProperty(value = "目标服务器ID")
    @Schema(description = "目标服务器ID")
    @NotNull(message = "服务器ID不能为空")
    private Integer monitorSysGenServerId;

    /**
     * 目标路径
     */
    @TableField(value = "monitor_sys_gen_file_upload_target_path")
    @ApiModelProperty(value = "目标路径")
    @Schema(description = "目标路径")
    @Size(max = 1000, message = "目标路径最大长度要小于 1000")
    private String monitorSysGenFileUploadTargetPath;

    /**
     * 上传状态
     */
    @TableField(value = "monitor_sys_gen_file_upload_status")
    @ApiModelProperty(value = "上传状态")
    @Schema(description = "上传状态(PENDING:待上传 UPLOADING:上传中 COMPLETED:已完成 FAILED:失败 CANCELLED:已取消)")
    @Size(max = 20, message = "上传状态最大长度要小于 20")
    private String monitorSysGenFileUploadStatus;

    /**
     * 上传进度(0-100)
     */
    @TableField(value = "monitor_sys_gen_file_upload_progress")
    @ApiModelProperty(value = "上传进度")
    @Schema(description = "上传进度(0-100)")
    private Integer monitorSysGenFileUploadProgress;

    /**
     * 上传速度(字节/秒)
     */
    @TableField(value = "monitor_sys_gen_file_upload_speed")
    @ApiModelProperty(value = "上传速度")
    @Schema(description = "上传速度(字节/秒)")
    private Long monitorSysGenFileUploadSpeed;

    /**
     * 已传输字节数
     */
    @TableField(value = "monitor_sys_gen_file_upload_transferred_bytes")
    @ApiModelProperty(value = "已传输字节数")
    @Schema(description = "已传输字节数")
    private Long monitorSysGenFileUploadTransferredBytes;

    /**
     * 总字节数
     */
    @TableField(value = "monitor_sys_gen_file_upload_total_bytes")
    @ApiModelProperty(value = "总字节数")
    @Schema(description = "总字节数")
    private Long monitorSysGenFileUploadTotalBytes;

    /**
     * 开始上传时间
     */
    @TableField(value = "monitor_sys_gen_file_upload_start_time")
    @ApiModelProperty(value = "开始上传时间")
    @Schema(description = "开始上传时间")
    private LocalDateTime monitorSysGenFileUploadStartTime;

    /**
     * 结束时间
     */
    @TableField(value = "monitor_sys_gen_file_upload_end_time")
    @ApiModelProperty(value = "结束时间")
    @Schema(description = "结束时间")
    private LocalDateTime monitorSysGenFileUploadEndTime;

    /**
     * 上传耗时(毫秒)
     */
    @TableField(value = "monitor_sys_gen_file_upload_duration_ms")
    @ApiModelProperty(value = "上传耗时(毫秒)")
    @Schema(description = "上传耗时(毫秒)")
    private Long monitorSysGenFileUploadDurationMs;

    /**
     * 错误信息
     */
    @TableField(value = "monitor_sys_gen_file_upload_error_message")
    @ApiModelProperty(value = "错误信息")
    @Schema(description = "错误信息")
    private String monitorSysGenFileUploadErrorMessage;

    /**
     * 错误代码
     */
    @TableField(value = "monitor_sys_gen_file_upload_error_code")
    @ApiModelProperty(value = "错误代码")
    @Schema(description = "错误代码")
    @Size(max = 50, message = "错误代码最大长度要小于 50")
    private String monitorSysGenFileUploadErrorCode;

    /**
     * 重试次数
     */
    @TableField(value = "monitor_sys_gen_file_upload_retry_count")
    @ApiModelProperty(value = "重试次数")
    @Schema(description = "重试次数")
    private Integer monitorSysGenFileUploadRetryCount;

    /**
     * 远程文件MD5值
     */
    @TableField(value = "monitor_sys_gen_file_upload_file_md5_remote")
    @ApiModelProperty(value = "远程文件MD5值")
    @Schema(description = "远程文件MD5值")
    @Size(max = 32, message = "远程文件MD5值最大长度要小于 32")
    private String monitorSysGenFileUploadFileMd5Remote;

    /**
     * 验证状态
     */
    @TableField(value = "monitor_sys_gen_file_upload_verification_status")
    @ApiModelProperty(value = "验证状态")
    @Schema(description = "验证状态(PENDING:待验证 PASSED:通过 FAILED:失败)")
    @Size(max = 20, message = "验证状态最大长度要小于 20")
    private String monitorSysGenFileUploadVerificationStatus;

    /**
     * 备份文件路径
     */
    @TableField(value = "monitor_sys_gen_file_upload_backup_path")
    @ApiModelProperty(value = "备份文件路径")
    @Schema(description = "备份文件路径")
    @Size(max = 1000, message = "备份文件路径最大长度要小于 1000")
    private String monitorSysGenFileUploadBackupPath;

    /**
     * 连接信息(JSON格式)
     */
    @TableField(value = "monitor_sys_gen_file_upload_connection_info")
    @ApiModelProperty(value = "连接信息")
    @Schema(description = "连接信息(JSON格式)")
    private String monitorSysGenFileUploadConnectionInfo;

    /**
     * 额外数据(JSON格式)
     */
    @TableField(value = "monitor_sys_gen_file_upload_extra_data")
    @ApiModelProperty(value = "额外数据")
    @Schema(description = "额外数据(JSON格式)")
    private String monitorSysGenFileUploadExtraData;

    /**
     * 创建时间
     */
    @TableField(value = "monitor_sys_gen_file_upload_create_time")
    @ApiModelProperty(value = "创建时间")
    @Schema(description = "创建时间")
    private LocalDateTime monitorSysGenFileUploadCreateTime;

    /**
     * 更新时间
     */
    @TableField(value = "monitor_sys_gen_file_upload_update_time")
    @ApiModelProperty(value = "更新时间")
    @Schema(description = "更新时间")
    private LocalDateTime monitorSysGenFileUploadUpdateTime;

    /**
     * 记录状态
     */
    @TableField(value = "monitor_sys_gen_file_upload_record_status")
    @ApiModelProperty(value = "记录状态")
    @Schema(description = "记录状态(0:删除 1:正常)")
    private Integer monitorSysGenFileUploadRecordStatus;

    /**
     * 上传状态枚举
     */
    public enum UploadStatus {
        PENDING("PENDING", "待上传"),
        UPLOADING("UPLOADING", "上传中"),
        COMPLETED("COMPLETED", "已完成"),
        FAILED("FAILED", "失败"),
        CANCELLED("CANCELLED", "已取消");

        private final String code;
        private final String desc;

        UploadStatus(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static UploadStatus fromCode(String code) {
            for (UploadStatus status : values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            return PENDING;
        }
    }

    /**
     * 验证状态枚举
     */
    public enum VerificationStatus {
        PENDING("PENDING", "待验证"),
        PASSED("PASSED", "通过"),
        FAILED("FAILED", "失败");

        private final String code;
        private final String desc;

        VerificationStatus(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static VerificationStatus fromCode(String code) {
            for (VerificationStatus status : values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            return PENDING;
        }
    }

    /**
     * 获取上传状态描述
     */
    public String getUploadStatusDesc() {
        return UploadStatus.fromCode(this.monitorSysGenFileUploadStatus).getDesc();
    }

    /**
     * 获取验证状态描述
     */
    public String getVerificationStatusDesc() {
        return VerificationStatus.fromCode(this.monitorSysGenFileUploadVerificationStatus).getDesc();
    }

    /**
     * 是否上传中
     */
    public boolean isUploading() {
        return UploadStatus.UPLOADING.getCode().equals(this.monitorSysGenFileUploadStatus);
    }

    /**
     * 是否已完成
     */
    public boolean isCompleted() {
        return UploadStatus.COMPLETED.getCode().equals(this.monitorSysGenFileUploadStatus);
    }

    /**
     * 是否失败
     */
    public boolean isFailed() {
        return UploadStatus.FAILED.getCode().equals(this.monitorSysGenFileUploadStatus);
    }

    /**
     * 是否已取消
     */
    public boolean isCancelled() {
        return UploadStatus.CANCELLED.getCode().equals(this.monitorSysGenFileUploadStatus);
    }

    /**
     * 是否验证通过
     */
    public boolean isVerificationPassed() {
        return VerificationStatus.PASSED.getCode().equals(this.monitorSysGenFileUploadVerificationStatus);
    }

    /**
     * 是否验证失败
     */
    public boolean isVerificationFailed() {
        return VerificationStatus.FAILED.getCode().equals(this.monitorSysGenFileUploadVerificationStatus);
    }

    /**
     * 获取上传速度(KB/s)
     */
    public Double getUploadSpeedKBps() {
        if (this.monitorSysGenFileUploadSpeed == null) {
            return 0.0;
        }
        return this.monitorSysGenFileUploadSpeed / 1024.0;
    }

    /**
     * 获取上传速度(MB/s)
     */
    public Double getUploadSpeedMBps() {
        if (this.monitorSysGenFileUploadSpeed == null) {
            return 0.0;
        }
        return this.monitorSysGenFileUploadSpeed / (1024.0 * 1024.0);
    }

    /**
     * 获取已传输大小(MB)
     */
    public Double getTransferredSizeMB() {
        if (this.monitorSysGenFileUploadTransferredBytes == null) {
            return 0.0;
        }
        return this.monitorSysGenFileUploadTransferredBytes / (1024.0 * 1024.0);
    }

    /**
     * 获取总大小(MB)
     */
    public Double getTotalSizeMB() {
        if (this.monitorSysGenFileUploadTotalBytes == null) {
            return 0.0;
        }
        return this.monitorSysGenFileUploadTotalBytes / (1024.0 * 1024.0);
    }

    /**
     * 获取上传耗时(秒)
     */
    public Double getDurationSeconds() {
        if (this.monitorSysGenFileUploadDurationMs == null) {
            return 0.0;
        }
        return this.monitorSysGenFileUploadDurationMs / 1000.0;
    }

    /**
     * 获取剩余时间估算(秒)
     */
    public Long getEstimatedRemainingSeconds() {
        if (this.monitorSysGenFileUploadSpeed == null || this.monitorSysGenFileUploadSpeed <= 0 ||
            this.monitorSysGenFileUploadTransferredBytes == null || this.monitorSysGenFileUploadTotalBytes == null) {
            return null;
        }
        
        long remainingBytes = this.monitorSysGenFileUploadTotalBytes - this.monitorSysGenFileUploadTransferredBytes;
        if (remainingBytes <= 0) {
            return 0L;
        }
        
        return remainingBytes / this.monitorSysGenFileUploadSpeed;
    }

    /**
     * 计算实际进度百分比
     */
    public Integer calculateProgress() {
        if (this.monitorSysGenFileUploadTransferredBytes == null || this.monitorSysGenFileUploadTotalBytes == null ||
            this.monitorSysGenFileUploadTotalBytes <= 0) {
            return 0;
        }
        
        return (int) ((this.monitorSysGenFileUploadTransferredBytes * 100) / this.monitorSysGenFileUploadTotalBytes);
    }

    /**
     * 是否有错误
     */
    public boolean hasError() {
        return this.monitorSysGenFileUploadErrorMessage != null && !this.monitorSysGenFileUploadErrorMessage.trim().isEmpty();
    }

    /**
     * 是否有备份
     */
    public boolean hasBackup() {
        return this.monitorSysGenFileUploadBackupPath != null && !this.monitorSysGenFileUploadBackupPath.trim().isEmpty();
    }
}
