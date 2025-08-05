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
 * 服务器日志实体类
 *
 * @author CH
 * @since 2024/12/20
 */
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "服务器日志")
@Schema(description = "服务器日志")
@Data
@TableName(value = "monitor_sys_gen_server_logs")
public class MonitorSysGenServerLog extends SysBase {

    @TableId(value = "monitor_sys_gen_server_log_id", type = IdType.AUTO)
    @ApiModelProperty(value = "日志ID")
    @Schema(description = "日志ID")
    @NotNull(message = "不能为null")
    private Long monitorSysGenServerLogId;

    /**
     * 关联服务器ID
     */
    @TableField(value = "monitor_sys_gen_server_id")
    @ApiModelProperty(value = "关联服务器ID")
    @Schema(description = "关联服务器ID")
    @NotNull(message = "服务器ID不能为空")
    private Integer monitorSysGenServerId;

    /**
     * 日志内容
     */
    @TableField(value = "monitor_sys_gen_server_log_content")
    @ApiModelProperty(value = "日志内容")
    @Schema(description = "日志内容")
    private String monitorSysGenServerLogContent;

    /**
     * 日志级别
     */
    @TableField(value = "monitor_sys_gen_server_log_level")
    @ApiModelProperty(value = "日志级别")
    @Schema(description = "日志级别(DEBUG,INFO,WARN,ERROR,FATAL)")
    @Size(max = 20, message = "日志级别最大长度要小于 20")
    private String monitorSysGenServerLogLevel;

    /**
     * 日志来源
     */
    @TableField(value = "monitor_sys_gen_server_log_source")
    @ApiModelProperty(value = "日志来源")
    @Schema(description = "日志来源(SYSTEM,APPLICATION,SECURITY,ACCESS)")
    @Size(max = 50, message = "日志来源最大长度要小于 50")
    private String monitorSysGenServerLogSource;

    /**
     * 日志分类
     */
    @TableField(value = "monitor_sys_gen_server_log_category")
    @ApiModelProperty(value = "日志分类")
    @Schema(description = "日志分类")
    @Size(max = 50, message = "日志分类最大长度要小于 50")
    private String monitorSysGenServerLogCategory;

    /**
     * 线程名称
     */
    @TableField(value = "monitor_sys_gen_server_log_thread")
    @ApiModelProperty(value = "线程名称")
    @Schema(description = "线程名称")
    @Size(max = 100, message = "线程名称最大长度要小于 100")
    private String monitorSysGenServerLogThread;

    /**
     * 日志记录器名称
     */
    @TableField(value = "monitor_sys_gen_server_log_logger")
    @ApiModelProperty(value = "日志记录器名称")
    @Schema(description = "日志记录器名称")
    @Size(max = 200, message = "日志记录器名称最大长度要小于 200")
    private String monitorSysGenServerLogLogger;

    /**
     * 日志文件路径
     */
    @TableField(value = "monitor_sys_gen_server_log_file_path")
    @ApiModelProperty(value = "日志文件路径")
    @Schema(description = "日志文件路径")
    @Size(max = 500, message = "日志文件路径最大长度要小于 500")
    private String monitorSysGenServerLogFilePath;

    /**
     * 日志行号
     */
    @TableField(value = "monitor_sys_gen_server_log_line_number")
    @ApiModelProperty(value = "日志行号")
    @Schema(description = "日志行号")
    private Integer monitorSysGenServerLogLineNumber;

    /**
     * 异常堆栈信息
     */
    @TableField(value = "monitor_sys_gen_server_log_exception_stack")
    @ApiModelProperty(value = "异常堆栈信息")
    @Schema(description = "异常堆栈信息")
    private String monitorSysGenServerLogExceptionStack;

    /**
     * 创建时间
     */
    @TableField(value = "monitor_sys_gen_server_log_create_time")
    @ApiModelProperty(value = "创建时间")
    @Schema(description = "创建时间")
    private LocalDateTime monitorSysGenServerLogCreateTime;

    /**
     * 服务器时间
     */
    @TableField(value = "monitor_sys_gen_server_log_server_time")
    @ApiModelProperty(value = "服务器时间")
    @Schema(description = "服务器时间")
    private LocalDateTime monitorSysGenServerLogServerTime;

    /**
     * 服务器IP
     */
    @TableField(value = "monitor_sys_gen_server_log_ip")
    @ApiModelProperty(value = "服务器IP")
    @Schema(description = "服务器IP")
    @Size(max = 50, message = "服务器IP最大长度要小于 50")
    private String monitorSysGenServerLogIp;

    /**
     * 主机名
     */
    @TableField(value = "monitor_sys_gen_server_log_hostname")
    @ApiModelProperty(value = "主机名")
    @Schema(description = "主机名")
    @Size(max = 100, message = "主机名最大长度要小于 100")
    private String monitorSysGenServerLogHostname;

    /**
     * 进程ID
     */
    @TableField(value = "monitor_sys_gen_server_log_process_id")
    @ApiModelProperty(value = "进程ID")
    @Schema(description = "进程ID")
    @Size(max = 20, message = "进程ID最大长度要小于 20")
    private String monitorSysGenServerLogProcessId;

    /**
     * 日志标签(JSON格式)
     */
    @TableField(value = "monitor_sys_gen_server_log_tags")
    @ApiModelProperty(value = "日志标签")
    @Schema(description = "日志标签(JSON格式)")
    @Size(max = 500, message = "日志标签最大长度要小于 500")
    private String monitorSysGenServerLogTags;

    /**
     * 额外数据(JSON格式)
     */
    @TableField(value = "monitor_sys_gen_server_log_extra_data")
    @ApiModelProperty(value = "额外数据")
    @Schema(description = "额外数据(JSON格式)")
    private String monitorSysGenServerLogExtraData;

    /**
     * 状态(0:删除 1:正常)
     */
    @TableField(value = "monitor_sys_gen_server_log_status")
    @ApiModelProperty(value = "状态")
    @Schema(description = "状态(0:删除 1:正常)")
    private Integer monitorSysGenServerLogStatus;

    /**
     * 日志级别枚举
     */
    public enum LogLevel {
        DEBUG("DEBUG", "调试"),
        INFO("INFO", "信息"),
        WARN("WARN", "警告"),
        ERROR("ERROR", "错误"),
        FATAL("FATAL", "致命");

        private final String code;
        private final String desc;

        LogLevel(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static LogLevel fromCode(String code) {
            for (LogLevel level : values()) {
                if (level.code.equals(code)) {
                    return level;
                }
            }
            return INFO;
        }
    }

    /**
     * 日志来源枚举
     */
    public enum LogSource {
        SYSTEM("SYSTEM", "系统日志"),
        APPLICATION("APPLICATION", "应用日志"),
        SECURITY("SECURITY", "安全日志"),
        ACCESS("ACCESS", "访问日志");

        private final String code;
        private final String desc;

        LogSource(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static LogSource fromCode(String code) {
            for (LogSource source : values()) {
                if (source.code.equals(code)) {
                    return source;
                }
            }
            return APPLICATION;
        }
    }

    /**
     * 获取日志级别描述
     */
    public String getLogLevelDesc() {
        return LogLevel.fromCode(this.monitorSysGenServerLogLevel).getDesc();
    }

    /**
     * 获取日志来源描述
     */
    public String getLogSourceDesc() {
        return LogSource.fromCode(this.monitorSysGenServerLogSource).getDesc();
    }

    /**
     * 是否为错误级别日志
     */
    public boolean isErrorLevel() {
        return LogLevel.ERROR.getCode().equals(this.monitorSysGenServerLogLevel) ||
               LogLevel.FATAL.getCode().equals(this.monitorSysGenServerLogLevel);
    }

    /**
     * 是否为警告级别日志
     */
    public boolean isWarnLevel() {
        return LogLevel.WARN.getCode().equals(this.monitorSysGenServerLogLevel);
    }

    /**
     * 是否包含异常信息
     */
    public boolean hasException() {
        return this.monitorSysGenServerLogExceptionStack != null && 
               !this.monitorSysGenServerLogExceptionStack.trim().isEmpty();
    }
}
