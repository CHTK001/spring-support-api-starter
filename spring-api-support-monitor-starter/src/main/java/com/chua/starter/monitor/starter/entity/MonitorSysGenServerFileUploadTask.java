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
 * 服务器文件上传任务实体类
 *
 * @author CH
 * @since 2024/12/20
 */
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "服务器文件上传任务")
@Schema(description = "服务器文件上传任务")
@Data
@TableName(value = "monitor_sys_gen_server_file_upload_tasks")
public class MonitorSysGenServerFileUploadTask extends SysBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "monitor_sys_gen_server_file_upload_task_id", type = IdType.AUTO)
    @ApiModelProperty(value = "任务ID")
    @Schema(description = "任务ID")
    @NotNull(message = "不能为null")
    private Long monitorSysGenServerFileUploadTaskId;

    /**
     * 任务名称
     */
    @TableField(value = "monitor_sys_gen_server_file_upload_task_name")
    @ApiModelProperty(value = "任务名称")
    @Schema(description = "任务名称")
    @NotNull(message = "任务名称不能为空")
    @Size(max = 255, message = "任务名称最大长度要小于 255")
    private String monitorSysGenServerFileUploadTaskName;

    /**
     * 文件名称
     */
    @TableField(value = "monitor_sys_gen_server_file_upload_task_file_name")
    @ApiModelProperty(value = "文件名称")
    @Schema(description = "文件名称")
    @NotNull(message = "文件名称不能为空")
    @Size(max = 255, message = "文件名称最大长度要小于 255")
    private String monitorSysGenServerFileUploadTaskFileName;

    /**
     * 文件路径
     */
    @TableField(value = "monitor_sys_gen_server_file_upload_task_file_path")
    @ApiModelProperty(value = "文件路径")
    @Schema(description = "文件路径")
    @NotNull(message = "文件路径不能为空")
    @Size(max = 1000, message = "文件路径最大长度要小于 1000")
    private String monitorSysGenServerFileUploadTaskFilePath;

    /**
     * 文件大小(字节)
     */
    @TableField(value = "monitor_sys_gen_server_file_upload_task_file_size")
    @ApiModelProperty(value = "文件大小(字节)")
    @Schema(description = "文件大小(字节)")
    private Long monitorSysGenServerFileUploadTaskFileSize;

    /**
     * 文件MD5值
     */
    @TableField(value = "monitor_sys_gen_server_file_upload_task_file_md5")
    @ApiModelProperty(value = "文件MD5值")
    @Schema(description = "文件MD5值")
    @Size(max = 32, message = "文件MD5值最大长度要小于 32")
    private String monitorSysGenServerFileUploadTaskFileMd5;

    /**
     * 目标服务器ID列表(JSON格式)
     */
    @TableField(value = "monitor_sys_gen_file_upload_target_server_ids")
    @ApiModelProperty(value = "目标服务器ID列表")
    @Schema(description = "目标服务器ID列表(JSON格式)")
    private String monitorSysGenFileUploadTargetServerIds;

    /**
     * 目标路径列表(JSON格式)
     */
    @TableField(value = "monitor_sys_gen_file_upload_target_paths")
    @ApiModelProperty(value = "目标路径列表")
    @Schema(description = "目标路径列表(JSON格式)")
    private String monitorSysGenFileUploadTargetPaths;

    /**
     * 上传模式
     */
    @TableField(value = "monitor_sys_gen_file_upload_mode")
    @ApiModelProperty(value = "上传模式")
    @Schema(description = "上传模式(REALTIME:实时 SCHEDULED:定时)")
    @Size(max = 20, message = "上传模式最大长度要小于 20")
    private String monitorSysGenFileUploadMode;

    /**
     * 上传类型
     */
    @TableField(value = "monitor_sys_gen_file_upload_type")
    @ApiModelProperty(value = "上传类型")
    @Schema(description = "上传类型(SINGLE:单文件 BATCH:批量)")
    @Size(max = 20, message = "上传类型最大长度要小于 20")
    private String monitorSysGenFileUploadType;

    /**
     * 任务状态
     */
    @TableField(value = "monitor_sys_gen_file_upload_status")
    @ApiModelProperty(value = "任务状态")
    @Schema(description = "任务状态(PENDING:待处理 PROCESSING:处理中 COMPLETED:已完成 FAILED:失败 CANCELLED:已取消)")
    @Size(max = 20, message = "任务状态最大长度要小于 20")
    private String monitorSysGenFileUploadStatus;

    /**
     * 定时执行时间
     */
    @TableField(value = "monitor_sys_gen_file_upload_scheduled_time")
    @ApiModelProperty(value = "定时执行时间")
    @Schema(description = "定时执行时间")
    private LocalDateTime monitorSysGenFileUploadScheduledTime;

    /**
     * 优先级
     */
    @TableField(value = "monitor_sys_gen_file_upload_priority")
    @ApiModelProperty(value = "优先级")
    @Schema(description = "优先级(1-10，数字越小优先级越高)")
    private Integer monitorSysGenFileUploadPriority;

    /**
     * 重试次数
     */
    @TableField(value = "monitor_sys_gen_file_upload_retry_count")
    @ApiModelProperty(value = "重试次数")
    @Schema(description = "重试次数")
    private Integer monitorSysGenFileUploadRetryCount;

    /**
     * 最大重试次数
     */
    @TableField(value = "monitor_sys_gen_file_upload_max_retry")
    @ApiModelProperty(value = "最大重试次数")
    @Schema(description = "最大重试次数")
    private Integer monitorSysGenFileUploadMaxRetry;

    /**
     * 超时时间(秒)
     */
    @TableField(value = "monitor_sys_gen_file_upload_timeout_seconds")
    @ApiModelProperty(value = "超时时间(秒)")
    @Schema(description = "超时时间(秒)")
    private Integer monitorSysGenFileUploadTimeoutSeconds;

    /**
     * 是否覆盖已存在文件
     */
    @TableField(value = "monitor_sys_gen_file_upload_overwrite_enabled")
    @ApiModelProperty(value = "是否覆盖已存在文件")
    @Schema(description = "是否覆盖已存在文件(0:否 1:是)")
    private Integer monitorSysGenFileUploadOverwriteEnabled;

    /**
     * 是否备份原文件
     */
    @TableField(value = "monitor_sys_gen_file_upload_backup_enabled")
    @ApiModelProperty(value = "是否备份原文件")
    @Schema(description = "是否备份原文件(0:否 1:是)")
    private Integer monitorSysGenFileUploadBackupEnabled;

    /**
     * 是否压缩传输
     */
    @TableField(value = "monitor_sys_gen_file_upload_compress_enabled")
    @ApiModelProperty(value = "是否压缩传输")
    @Schema(description = "是否压缩传输(0:否 1:是)")
    private Integer monitorSysGenFileUploadCompressEnabled;

    /**
     * 是否验证文件完整性
     */
    @TableField(value = "monitor_sys_gen_file_upload_verify_enabled")
    @ApiModelProperty(value = "是否验证文件完整性")
    @Schema(description = "是否验证文件完整性(0:否 1:是)")
    private Integer monitorSysGenFileUploadVerifyEnabled;

    /**
     * 任务描述
     */
    @TableField(value = "monitor_sys_gen_file_upload_description")
    @ApiModelProperty(value = "任务描述")
    @Schema(description = "任务描述")
    private String monitorSysGenFileUploadDescription;

    /**
     * 标签(JSON格式)
     */
    @TableField(value = "monitor_sys_gen_file_upload_tags")
    @ApiModelProperty(value = "标签")
    @Schema(description = "标签(JSON格式)")
    @Size(max = 500, message = "标签最大长度要小于 500")
    private String monitorSysGenFileUploadTags;

    /**
     * 创建用户
     */
    @TableField(value = "monitor_sys_gen_file_upload_create_user")
    @ApiModelProperty(value = "创建用户")
    @Schema(description = "创建用户")
    @Size(max = 100, message = "创建用户最大长度要小于 100")
    private String monitorSysGenFileUploadCreateUser;

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
     * 开始执行时间
     */
    @TableField(value = "monitor_sys_gen_file_upload_start_time")
    @ApiModelProperty(value = "开始执行时间")
    @Schema(description = "开始执行时间")
    private LocalDateTime monitorSysGenFileUploadStartTime;

    /**
     * 结束时间
     */
    @TableField(value = "monitor_sys_gen_file_upload_end_time")
    @ApiModelProperty(value = "结束时间")
    @Schema(description = "结束时间")
    private LocalDateTime monitorSysGenFileUploadEndTime;

    /**
     * 记录状态
     */
    @TableField(value = "monitor_sys_gen_file_upload_task_status")
    @ApiModelProperty(value = "记录状态")
    @Schema(description = "记录状态(0:删除 1:正常)")
    private Integer monitorSysGenFileUploadTaskStatus;

    /**
     * 上传模式枚举
     */
    public enum UploadMode {
        REALTIME("REALTIME", "实时上传"),
        SCHEDULED("SCHEDULED", "定时上传");

        private final String code;
        private final String desc;

        UploadMode(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static UploadMode fromCode(String code) {
            for (UploadMode mode : values()) {
                if (mode.code.equals(code)) {
                    return mode;
                }
            }
            return REALTIME;
        }
    }

    /**
     * 上传类型枚举
     */
    public enum UploadType {
        SINGLE("SINGLE", "单文件上传"),
        BATCH("BATCH", "批量上传");

        private final String code;
        private final String desc;

        UploadType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static UploadType fromCode(String code) {
            for (UploadType type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            return SINGLE;
        }
    }

    /**
     * 任务状态枚举
     */
    public enum TaskStatus {
        PENDING("PENDING", "待处理"),
        PROCESSING("PROCESSING", "处理中"),
        COMPLETED("COMPLETED", "已完成"),
        FAILED("FAILED", "失败"),
        CANCELLED("CANCELLED", "已取消");

        private final String code;
        private final String desc;

        TaskStatus(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static TaskStatus fromCode(String code) {
            for (TaskStatus status : values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            return PENDING;
        }
    }

    /**
     * 获取上传模式描述
     */
    public String getUploadModeDesc() {
        return UploadMode.fromCode(this.monitorSysGenFileUploadMode).getDesc();
    }

    /**
     * 获取上传类型描述
     */
    public String getUploadTypeDesc() {
        return UploadType.fromCode(this.monitorSysGenFileUploadType).getDesc();
    }

    /**
     * 获取任务状态描述
     */
    public String getTaskStatusDesc() {
        return TaskStatus.fromCode(this.monitorSysGenFileUploadStatus).getDesc();
    }

    /**
     * 是否实时上传
     */
    public boolean isRealTimeUpload() {
        return UploadMode.REALTIME.getCode().equals(this.monitorSysGenFileUploadMode);
    }

    /**
     * 是否定时上传
     */
    public boolean isScheduledUpload() {
        return UploadMode.SCHEDULED.getCode().equals(this.monitorSysGenFileUploadMode);
    }

    /**
     * 是否单文件上传
     */
    public boolean isSingleUpload() {
        return UploadType.SINGLE.getCode().equals(this.monitorSysGenFileUploadType);
    }

    /**
     * 是否批量上传
     */
    public boolean isBatchUpload() {
        return UploadType.BATCH.getCode().equals(this.monitorSysGenFileUploadType);
    }

    /**
     * 是否已完成
     */
    public boolean isCompleted() {
        return TaskStatus.COMPLETED.getCode().equals(this.monitorSysGenFileUploadStatus);
    }

    /**
     * 是否失败
     */
    public boolean isFailed() {
        return TaskStatus.FAILED.getCode().equals(this.monitorSysGenFileUploadStatus);
    }

    /**
     * 是否处理中
     */
    public boolean isProcessing() {
        return TaskStatus.PROCESSING.getCode().equals(this.monitorSysGenFileUploadStatus);
    }

    /**
     * 是否可以重试
     */
    public boolean canRetry() {
        return this.monitorSysGenFileUploadRetryCount != null && 
               this.monitorSysGenFileUploadMaxRetry != null &&
               this.monitorSysGenFileUploadRetryCount < this.monitorSysGenFileUploadMaxRetry &&
               (isFailed() || TaskStatus.PENDING.getCode().equals(this.monitorSysGenFileUploadStatus));
    }

    /**
     * 获取文件大小(MB)
     */
    public Double getFileSizeMB() {
        if (this.monitorSysGenServerFileUploadTaskFileSize == null) {
            return 0.0;
        }
        return this.monitorSysGenServerFileUploadTaskFileSize / (1024.0 * 1024.0);
    }

    /**
     * 获取文件大小(GB)
     */
    public Double getFileSizeGB() {
        if (this.monitorSysGenServerFileUploadTaskFileSize == null) {
            return 0.0;
        }
        return this.monitorSysGenServerFileUploadTaskFileSize / (1024.0 * 1024.0 * 1024.0);
    }

    /**
     * 是否启用覆盖
     */
    public boolean isOverwriteEnabled() {
        return this.monitorSysGenFileUploadOverwriteEnabled != null && this.monitorSysGenFileUploadOverwriteEnabled == 1;
    }

    /**
     * 是否启用备份
     */
    public boolean isBackupEnabled() {
        return this.monitorSysGenFileUploadBackupEnabled != null && this.monitorSysGenFileUploadBackupEnabled == 1;
    }

    /**
     * 是否启用压缩
     */
    public boolean isCompressEnabled() {
        return this.monitorSysGenFileUploadCompressEnabled != null && this.monitorSysGenFileUploadCompressEnabled == 1;
    }

    /**
     * 是否启用验证
     */
    public boolean isVerifyEnabled() {
        return this.monitorSysGenFileUploadVerifyEnabled != null && this.monitorSysGenFileUploadVerifyEnabled == 1;
    }
}
