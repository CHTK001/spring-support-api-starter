package com.chua.starter.monitor.starter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 脚本执行历史实体
 *
 * @author CH
 * @since 2024/12/19
 */
@Schema(description = "脚本执行历史实体")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("monitor_sys_gen_script_execution")
public class MonitorSysGenScriptExecution extends SysBase {

    /**
     * 执行记录ID
     */
    @Schema(description = "执行记录ID", example = "1")
    @TableId(value = "monitor_sys_gen_script_execution_id", type = IdType.AUTO)
    private Long monitorSysGenScriptExecutionId;

    /**
     * 脚本ID
     */
    @Schema(description = "脚本ID", example = "1", required = true)
    @TableField("monitor_sys_gen_script_id")
    private Integer monitorSysGenScriptId;

    /**
     * 执行参数
     */
    @Schema(description = "执行参数", example = "--verbose --output /tmp/result.log")
    @TableField("monitor_sys_gen_script_execution_parameters")
    private String monitorSysGenScriptExecutionParameters;

    /**
     * 执行状态
     */
    @Schema(description = "执行状态", implementation = ExecutionStatus.class)
    @TableField("monitor_sys_gen_script_execution_status")
    private ExecutionStatus monitorSysGenScriptExecutionStatus;

    /**
     * 开始执行时间
     */
    @Schema(description = "开始执行时间", example = "2024-01-01T12:00:00")
    @TableField("monitor_sys_gen_script_execution_start_time")
    private LocalDateTime monitorSysGenScriptExecutionStartTime;

    /**
     * 结束执行时间
     */
    @Schema(description = "结束执行时间", example = "2024-01-01T12:05:00")
    @TableField("monitor_sys_gen_script_execution_end_time")
    private LocalDateTime monitorSysGenScriptExecutionEndTime;

    /**
     * 执行耗时(毫秒)
     */
    @Schema(description = "执行耗时(毫秒)", example = "300000", minimum = "0")
    @TableField("monitor_sys_gen_script_execution_duration")
    private Long monitorSysGenScriptExecutionDuration;

    /**
     * 退出码
     */
    @Schema(description = "退出码", example = "0")
    @TableField("monitor_sys_gen_script_execution_exit_code")
    private Integer monitorSysGenScriptExecutionExitCode;

    /**
     * 标准输出
     */
    @Schema(description = "标准输出", example = "Script executed successfully")
    @TableField("monitor_sys_gen_script_execution_stdout")
    private String monitorSysGenScriptExecutionStdout;

    /**
     * 错误输出
     */
    @Schema(description = "错误输出", example = "Warning: deprecated function used")
    @TableField("monitor_sys_gen_script_execution_stderr")
    private String monitorSysGenScriptExecutionStderr;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息", example = "Connection timeout")
    @TableField("monitor_sys_gen_script_execution_error_message")
    private String monitorSysGenScriptExecutionErrorMessage;

    /**
     * 进程ID
     */
    @Schema(description = "进程ID", example = "12345")
    @TableField("monitor_sys_gen_script_execution_process_id")
    private Long monitorSysGenScriptExecutionProcessId;

    /**
     * 执行服务器ID
     */
    @Schema(description = "执行服务器ID", example = "1")
    @TableField("monitor_sys_gen_script_execution_server_id")
    private Integer monitorSysGenScriptExecutionServerId;

    /**
     * 触发类型
     */
    @Schema(description = "触发类型", implementation = TriggerType.class)
    @TableField("monitor_sys_gen_script_execution_trigger_type")
    private TriggerType monitorSysGenScriptExecutionTriggerType;

    /**
     * 执行状态枚举
     */
    @Schema(description = "执行状态枚举")
    public enum ExecutionStatus {
        @Schema(description = "运行中")
        RUNNING("RUNNING", "运行中"), @Schema(description = "执行成功")
        SUCCESS("SUCCESS", "执行成功"), @Schema(description = "执行失败")
        FAILED("FAILED", "执行失败"), @Schema(description = "执行超时")
        TIMEOUT("TIMEOUT", "执行超时"), @Schema(description = "已取消")
        CANCELLED("CANCELLED", "已取消");

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
            if (code == null) {
                return null;
            }
            for (ExecutionStatus status : values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            return null;
        }
    }

    /**
     * 触发类型枚举
     */
    @Schema(description = "触发类型枚举")
    public enum TriggerType {
        @Schema(description = "手动执行")
        MANUAL("MANUAL", "手动执行"), @Schema(description = "定时执行")
        SCHEDULED("SCHEDULED", "定时执行"), @Schema(description = "接口调用")
        API("API", "接口调用");

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
            if (code == null) {
                return TriggerType.MANUAL;
            }
            for (TriggerType type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            return TriggerType.MANUAL;
        }
    }

    /**
     * 检查执行是否成功
     */
    public boolean isSuccess() {
        return ExecutionStatus.SUCCESS.equals(monitorSysGenScriptExecutionStatus);
    }

    /**
     * 检查执行是否失败
     */
    public boolean isFailure() {
        return ExecutionStatus.FAILED.equals(monitorSysGenScriptExecutionStatus)
                || ExecutionStatus.TIMEOUT.equals(monitorSysGenScriptExecutionStatus);
    }

    /**
     * 检查执行是否正在运行
     */
    public boolean isRunning() {
        return ExecutionStatus.RUNNING.equals(monitorSysGenScriptExecutionStatus);
    }

    /**
     * 获取格式化的执行耗时
     */
    public String getFormattedDuration() {
        if (monitorSysGenScriptExecutionDuration == null) {
            return "未知";
        }

        if (monitorSysGenScriptExecutionDuration < 1000) {
            return monitorSysGenScriptExecutionDuration + "ms";
        } else if (monitorSysGenScriptExecutionDuration < 60000) {
            return String.format("%.2fs", monitorSysGenScriptExecutionDuration / 1000.0);
        } else {
            long minutes = monitorSysGenScriptExecutionDuration / 60000;
            long seconds = (monitorSysGenScriptExecutionDuration % 60000) / 1000;
            return String.format("%dm%ds", minutes, seconds);
        }
    }

    /**
     * 计算执行耗时
     */
    public void calculateDuration() {
        if (monitorSysGenScriptExecutionStartTime != null && monitorSysGenScriptExecutionEndTime != null) {
            this.monitorSysGenScriptExecutionDuration = java.time.Duration
                    .between(monitorSysGenScriptExecutionStartTime, monitorSysGenScriptExecutionEndTime).toMillis();
        }
    }

    /**
     * 设置执行完成
     */
    public void setExecutionCompleted(ExecutionStatus status, Integer exitCode, String stdout, String stderr) {
        this.monitorSysGenScriptExecutionStatus = status;
        this.monitorSysGenScriptExecutionEndTime = LocalDateTime.now();
        this.monitorSysGenScriptExecutionExitCode = exitCode;
        this.monitorSysGenScriptExecutionStdout = stdout;
        this.monitorSysGenScriptExecutionStderr = stderr;
        calculateDuration();
    }

    /**
     * 设置执行失败
     */
    public void setExecutionFailed(String errorMessage) {
        this.monitorSysGenScriptExecutionStatus = ExecutionStatus.FAILED;
        this.monitorSysGenScriptExecutionEndTime = LocalDateTime.now();
        this.monitorSysGenScriptExecutionErrorMessage = errorMessage;
        calculateDuration();
    }

    /**
     * 获取执行结果摘要
     */
    public String getExecutionSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("状态: ").append(
                monitorSysGenScriptExecutionStatus != null ? monitorSysGenScriptExecutionStatus.getDesc() : "未知");
        summary.append(", 耗时: ").append(getFormattedDuration());
        if (monitorSysGenScriptExecutionExitCode != null) {
            summary.append(", 退出码: ").append(monitorSysGenScriptExecutionExitCode);
        }
        return summary.toString();
    }
}
