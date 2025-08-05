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
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 服务器脚本执行记录实体类
 *
 * @author CH
 * @since 2024/12/20
 */
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "服务器脚本执行记录")
@Schema(description = "服务器脚本执行记录")
@Data
@TableName(value = "monitor_sys_gen_server_script_executions")
public class MonitorSysGenServerScriptExecution extends SysBase {

    @TableId(value = "monitor_sys_gen_server_script_execution_id", type = IdType.AUTO)
    @ApiModelProperty(value = "执行记录ID")
    @Schema(description = "执行记录ID")
    @NotNull(message = "不能为null")
    private Long monitorSysGenServerScriptExecutionId;

    /**
     * 脚本ID
     */
    @TableField(value = "monitor_sys_gen_server_script_id")
    @ApiModelProperty(value = "脚本ID")
    @Schema(description = "脚本ID")
    @NotNull(message = "脚本ID不能为空")
    private Long monitorSysGenServerScriptId;

    /**
     * 服务器ID
     */
    @TableField(value = "monitor_sys_gen_server_id")
    @ApiModelProperty(value = "服务器ID")
    @Schema(description = "服务器ID")
    @NotNull(message = "服务器ID不能为空")
    private Integer monitorSysGenServerId;

    /**
     * 执行名称
     */
    @TableField(value = "monitor_sys_gen_server_script_execution_name")
    @ApiModelProperty(value = "执行名称")
    @Schema(description = "执行名称")
    @Size(max = 255, message = "执行名称最大长度要小于 255")
    private String monitorSysGenServerScriptExecutionName;

    /**
     * 执行参数(JSON格式)
     */
    @TableField(value = "monitor_sys_gen_server_script_execution_parameters")
    @ApiModelProperty(value = "执行参数")
    @Schema(description = "执行参数(JSON格式)")
    private String monitorSysGenServerScriptExecutionParameters;

    /**
     * 执行状态
     */
    @TableField(value = "monitor_sys_gen_server_script_execution_status")
    @ApiModelProperty(value = "执行状态")
    @Schema(description = "执行状态(PENDING:待执行 RUNNING:执行中 COMPLETED:已完成 FAILED:失败 CANCELLED:已取消 TIMEOUT:超时)")
    @Size(max = 20, message = "执行状态最大长度要小于 20")
    private String monitorSysGenServerScriptExecutionStatus;

    /**
     * 执行模式
     */
    @TableField(value = "monitor_sys_gen_server_script_execution_mode")
    @ApiModelProperty(value = "执行模式")
    @Schema(description = "执行模式(SYNC:同步 ASYNC:异步)")
    @Size(max = 20, message = "执行模式最大长度要小于 20")
    private String monitorSysGenServerScriptExecutionMode;

    /**
     * 执行优先级
     */
    @TableField(value = "monitor_sys_gen_server_script_execution_priority")
    @ApiModelProperty(value = "执行优先级")
    @Schema(description = "执行优先级(1-10，数字越小优先级越高)")
    private Integer monitorSysGenServerScriptExecutionPriority;

    /**
     * 开始执行时间
     */
    @TableField(value = "monitor_sys_gen_server_script_execution_start_time")
    @ApiModelProperty(value = "开始执行时间")
    @Schema(description = "开始执行时间")
    private LocalDateTime monitorSysGenServerScriptExecutionStartTime;

    /**
     * 结束时间
     */
    @TableField(value = "monitor_sys_gen_server_script_execution_end_time")
    @ApiModelProperty(value = "结束时间")
    @Schema(description = "结束时间")
    private LocalDateTime monitorSysGenServerScriptExecutionEndTime;

    /**
     * 执行耗时(毫秒)
     */
    @TableField(value = "monitor_sys_gen_server_script_execution_duration_ms")
    @ApiModelProperty(value = "执行耗时(毫秒)")
    @Schema(description = "执行耗时(毫秒)")
    private Long monitorSysGenServerScriptExecutionDurationMs;

    /**
     * 退出代码
     */
    @TableField(value = "monitor_sys_gen_server_script_execution_exit_code")
    @ApiModelProperty(value = "退出代码")
    @Schema(description = "退出代码")
    private Integer monitorSysGenServerScriptExecutionExitCode;

    /**
     * 执行输出
     */
    @TableField(value = "monitor_sys_gen_server_script_execution_output")
    @ApiModelProperty(value = "执行输出")
    @Schema(description = "执行输出")
    private String monitorSysGenServerScriptExecutionOutput;

    /**
     * 错误输出
     */
    @TableField(value = "monitor_sys_gen_server_script_execution_error_output")
    @ApiModelProperty(value = "错误输出")
    @Schema(description = "错误输出")
    private String monitorSysGenServerScriptExecutionErrorOutput;

    /**
     * 工作目录
     */
    @TableField(value = "monitor_sys_gen_server_script_execution_working_directory")
    @ApiModelProperty(value = "工作目录")
    @Schema(description = "工作目录")
    @Size(max = 500, message = "工作目录最大长度要小于 500")
    private String monitorSysGenServerScriptExecutionWorkingDirectory;

    /**
     * 环境变量(JSON格式)
     */
    @TableField(value = "monitor_sys_gen_server_script_execution_environment_vars")
    @ApiModelProperty(value = "环境变量")
    @Schema(description = "环境变量(JSON格式)")
    private String monitorSysGenServerScriptExecutionEnvironmentVars;

    /**
     * 进程ID
     */
    @TableField(value = "monitor_sys_gen_server_script_execution_process_id")
    @ApiModelProperty(value = "进程ID")
    @Schema(description = "进程ID")
    @Size(max = 50, message = "进程ID最大长度要小于 50")
    private String monitorSysGenServerScriptExecutionProcessId;

    /**
     * 触发类型
     */
    @TableField(value = "monitor_sys_gen_server_script_execution_trigger_type")
    @ApiModelProperty(value = "触发类型")
    @Schema(description = "触发类型(MANUAL:手动 SCHEDULED:定时 API:API调用 EVENT:事件触发)")
    @Size(max = 50, message = "触发类型最大长度要小于 50")
    private String monitorSysGenServerScriptExecutionTriggerType;

    /**
     * 触发用户
     */
    @TableField(value = "monitor_sys_gen_server_script_execution_trigger_user")
    @ApiModelProperty(value = "触发用户")
    @Schema(description = "触发用户")
    @Size(max = 100, message = "触发用户最大长度要小于 100")
    private String monitorSysGenServerScriptExecutionTriggerUser;

    /**
     * 重试次数
     */
    @TableField(value = "monitor_sys_gen_server_script_execution_retry_count")
    @ApiModelProperty(value = "重试次数")
    @Schema(description = "重试次数")
    private Integer monitorSysGenServerScriptExecutionRetryCount;

    /**
     * 最大重试次数
     */
    @TableField(value = "monitor_sys_gen_server_script_execution_max_retry")
    @ApiModelProperty(value = "最大重试次数")
    @Schema(description = "最大重试次数")
    private Integer monitorSysGenServerScriptExecutionMaxRetry;

    /**
     * 父执行记录ID
     */
    @TableField(value = "monitor_sys_gen_server_script_execution_parent_id")
    @ApiModelProperty(value = "父执行记录ID")
    @Schema(description = "父执行记录ID(重试时使用)")
    private Long monitorSysGenServerScriptExecutionParentId;

    /**
     * 计划执行时间
     */
    @TableField(value = "monitor_sys_gen_server_script_execution_scheduled_time")
    @ApiModelProperty(value = "计划执行时间")
    @Schema(description = "计划执行时间")
    private LocalDateTime monitorSysGenServerScriptExecutionScheduledTime;

    /**
     * 超时时间(秒)
     */
    @TableField(value = "monitor_sys_gen_server_script_execution_timeout_seconds")
    @ApiModelProperty(value = "超时时间(秒)")
    @Schema(description = "超时时间(秒)")
    private Integer monitorSysGenServerScriptExecutionTimeoutSeconds;

    /**
     * 执行标签(JSON格式)
     */
    @TableField(value = "monitor_sys_gen_server_script_execution_tags")
    @ApiModelProperty(value = "执行标签")
    @Schema(description = "执行标签(JSON格式)")
    private String monitorSysGenServerScriptExecutionTags;

    /**
     * 执行元数据(JSON格式)
     */
    @TableField(value = "monitor_sys_gen_server_script_execution_metadata")
    @ApiModelProperty(value = "执行元数据")
    @Schema(description = "执行元数据(JSON格式)")
    private String monitorSysGenServerScriptExecutionMetadata;

    /**
     * 创建时间
     */
    @TableField(value = "monitor_sys_gen_server_script_execution_create_time")
    @ApiModelProperty(value = "创建时间")
    @Schema(description = "创建时间")
    private LocalDateTime monitorSysGenServerScriptExecutionCreateTime;

    /**
     * 更新时间
     */
    @TableField(value = "monitor_sys_gen_server_script_execution_update_time")
    @ApiModelProperty(value = "更新时间")
    @Schema(description = "更新时间")
    private LocalDateTime monitorSysGenServerScriptExecutionUpdateTime;

    /**
     * 记录状态
     */
    @TableField(value = "monitor_sys_gen_server_script_execution_record_status")
    @ApiModelProperty(value = "记录状态")
    @Schema(description = "记录状态(0:删除 1:正常)")
    private Integer monitorSysGenServerScriptExecutionRecordStatus;

    /**
     * 执行状态枚举
     */
    public enum ExecutionStatus {
        PENDING("PENDING", "待执行"),
        RUNNING("RUNNING", "执行中"),
        COMPLETED("COMPLETED", "已完成"),
        FAILED("FAILED", "失败"),
        CANCELLED("CANCELLED", "已取消"),
        TIMEOUT("TIMEOUT", "超时");

        private final String code;
        private final String desc;

        ExecutionStatus(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static ExecutionStatus fromCode(String code) {
            for (ExecutionStatus status : values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            return PENDING;
        }
    }

    /**
     * 触发类型枚举
     */
    public enum TriggerType {
        MANUAL("MANUAL", "手动触发"),
        SCHEDULED("SCHEDULED", "定时触发"),
        API("API", "API调用"),
        EVENT("EVENT", "事件触发");

        private final String code;
        private final String desc;

        TriggerType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static TriggerType fromCode(String code) {
            for (TriggerType type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            return MANUAL;
        }
    }

    /**
     * 执行模式枚举
     */
    public enum ExecutionMode {
        SYNC("SYNC", "同步执行"),
        ASYNC("ASYNC", "异步执行");

        private final String code;
        private final String desc;

        ExecutionMode(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static ExecutionMode fromCode(String code) {
            for (ExecutionMode mode : values()) {
                if (mode.code.equals(code)) {
                    return mode;
                }
            }
            return SYNC;
        }
    }

    /**
     * 获取执行状态描述
     */
    public String getExecutionStatusDesc() {
        return ExecutionStatus.fromCode(this.monitorSysGenServerScriptExecutionStatus).getDesc();
    }

    /**
     * 获取触发类型描述
     */
    public String getTriggerTypeDesc() {
        return TriggerType.fromCode(this.monitorSysGenServerScriptExecutionTriggerType).getDesc();
    }

    /**
     * 获取执行模式描述
     */
    public String getExecutionModeDesc() {
        return ExecutionMode.fromCode(this.monitorSysGenServerScriptExecutionMode).getDesc();
    }

    /**
     * 是否正在执行
     */
    public boolean isRunning() {
        return ExecutionStatus.RUNNING.getCode().equals(this.monitorSysGenServerScriptExecutionStatus);
    }

    /**
     * 是否已完成
     */
    public boolean isCompleted() {
        return ExecutionStatus.COMPLETED.getCode().equals(this.monitorSysGenServerScriptExecutionStatus);
    }

    /**
     * 是否失败
     */
    public boolean isFailed() {
        return ExecutionStatus.FAILED.getCode().equals(this.monitorSysGenServerScriptExecutionStatus);
    }

    /**
     * 是否已取消
     */
    public boolean isCancelled() {
        return ExecutionStatus.CANCELLED.getCode().equals(this.monitorSysGenServerScriptExecutionStatus);
    }

    /**
     * 是否超时
     */
    public boolean isTimeout() {
        return ExecutionStatus.TIMEOUT.getCode().equals(this.monitorSysGenServerScriptExecutionStatus);
    }

    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return isCompleted() && (this.monitorSysGenServerScriptExecutionExitCode == null || this.monitorSysGenServerScriptExecutionExitCode == 0);
    }

    /**
     * 是否可以重试
     */
    public boolean canRetry() {
        return (isFailed() || isTimeout()) && 
               this.monitorSysGenServerScriptExecutionRetryCount != null && 
               this.monitorSysGenServerScriptExecutionMaxRetry != null &&
               this.monitorSysGenServerScriptExecutionRetryCount < this.monitorSysGenServerScriptExecutionMaxRetry;
    }

    /**
     * 获取执行耗时(秒)
     */
    public Double getDurationSeconds() {
        if (this.monitorSysGenServerScriptExecutionDurationMs == null) {
            return 0.0;
        }
        return this.monitorSysGenServerScriptExecutionDurationMs / 1000.0;
    }

    /**
     * 计算执行耗时
     */
    public Long calculateDuration() {
        if (this.monitorSysGenServerScriptExecutionStartTime == null) {
            return null;
        }
        
        LocalDateTime endTime = this.monitorSysGenServerScriptExecutionEndTime;
        if (endTime == null && isRunning()) {
            endTime = LocalDateTime.now();
        }
        
        if (endTime == null) {
            return null;
        }
        
        return java.time.Duration.between(this.monitorSysGenServerScriptExecutionStartTime, endTime).toMillis();
    }

    /**
     * 是否有输出
     */
    public boolean hasOutput() {
        return this.monitorSysGenServerScriptExecutionOutput != null && !this.monitorSysGenServerScriptExecutionOutput.trim().isEmpty();
    }

    /**
     * 是否有错误输出
     */
    public boolean hasErrorOutput() {
        return this.monitorSysGenServerScriptExecutionErrorOutput != null && !this.monitorSysGenServerScriptExecutionErrorOutput.trim().isEmpty();
    }

    /**
     * 设置默认值
     */
    public void setDefaultValues() {
        if (this.monitorSysGenServerScriptExecutionStatus == null) {
            this.monitorSysGenServerScriptExecutionStatus = ExecutionStatus.PENDING.getCode();
        }
        if (this.monitorSysGenServerScriptExecutionMode == null) {
            this.monitorSysGenServerScriptExecutionMode = ExecutionMode.SYNC.getCode();
        }
        if (this.monitorSysGenServerScriptExecutionPriority == null) {
            this.monitorSysGenServerScriptExecutionPriority = 5;
        }
        if (this.monitorSysGenServerScriptExecutionTriggerType == null) {
            this.monitorSysGenServerScriptExecutionTriggerType = TriggerType.MANUAL.getCode();
        }
        if (this.monitorSysGenServerScriptExecutionRetryCount == null) {
            this.monitorSysGenServerScriptExecutionRetryCount = 0;
        }
        if (this.monitorSysGenServerScriptExecutionMaxRetry == null) {
            this.monitorSysGenServerScriptExecutionMaxRetry = 0;
        }
        if (this.monitorSysGenServerScriptExecutionTimeoutSeconds == null) {
            this.monitorSysGenServerScriptExecutionTimeoutSeconds = 300;
        }
        if (this.monitorSysGenServerScriptExecutionRecordStatus == null) {
            this.monitorSysGenServerScriptExecutionRecordStatus = 1;
        }
    }
}
