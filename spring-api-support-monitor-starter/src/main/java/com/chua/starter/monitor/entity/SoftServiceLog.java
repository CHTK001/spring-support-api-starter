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

/**
 * 软件服务日志
 *
 * @author CH
 * @since 2025/6/5 9:19
 */
@ApiModel(description = "软件服务日志")
@Schema(description = "软件服务日志")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "soft_service_log")
public class SoftServiceLog extends SysBase {
    /**
     * 日志ID
     */
    @TableId(value = "log_id", type = IdType.AUTO)
    @ApiModelProperty(value = "日志ID")
    @Schema(description = "日志ID")
    private Integer softServiceLogId;

    /**
     * 安装ID
     */
    @TableField(value = "install_id")
    @ApiModelProperty(value = "安装ID")
    @Schema(description = "安装ID")
    private Integer softServiceLogInstallId;

    /**
     * 软件服务ID
     */
    @TableField(value = "soft_service_id")
    @ApiModelProperty(value = "软件服务ID")
    @Schema(description = "软件服务ID")
    private Integer softServiceLogServiceId;

    /**
     * SSH ID
     */
    @TableField(value = "ssh_id")
    @ApiModelProperty(value = "SSH ID")
    @Schema(description = "SSH ID")
    private String softServiceLogSshId;

    /**
     * 日志类型：1-安装，2-卸载，3-启动，4-停止，5-重启，6-状态检查
     */
    @TableField(value = "log_type")
    @ApiModelProperty(value = "日志类型：1-安装，2-卸载，3-启动，4-停止，5-重启，6-状态检查")
    @Schema(description = "日志类型：1-安装，2-卸载，3-启动，4-停止，5-重启，6-状态检查")
    private Integer softServiceLogType;

    /**
     * 日志内容
     */
    @TableField(value = "log_content")
    @ApiModelProperty(value = "日志内容")
    @Schema(description = "日志内容")
    private String softServiceLogContent;

    /**
     * 日志状态：0-进行中，1-成功，2-失败
     */
    @TableField(value = "log_status")
    @ApiModelProperty(value = "日志状态：0-进行中，1-成功，2-失败")
    @Schema(description = "日志状态：0-进行中，1-成功，2-失败")
    private Integer softServiceLogStatus;

    /**
     * 日志时间
     */
    @TableField(value = "log_time")
    @ApiModelProperty(value = "日志时间")
    @Schema(description = "日志时间")
    private Long softServiceLogTime;

    /**
     * 日志文件路径
     */
    @TableField(value = "log_file_path")
    @ApiModelProperty(value = "日志文件路径")
    @Schema(description = "日志文件路径")
    private String softServiceLogFilePath;

    /**
     * 日志操作类型（字符串形式）：install, uninstall, start, stop, restart, status
     */
    @TableField(value = "log_operation_type")
    @ApiModelProperty(value = "日志操作类型：install, uninstall, start, stop, restart, status")
    @Schema(description = "日志操作类型：install, uninstall, start, stop, restart, status")
    private String softServiceLogOperationType;

    /**
     * 软件服务名称
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "软件服务名称")
    @Schema(description = "软件服务名称")
    private String softServiceLogServiceName;

    /**
     * SSH主机名称
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "SSH主机名称")
    @Schema(description = "SSH主机名称")
    private String softServiceLogSshName;

    /**
     * SSH主机地址
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "SSH主机地址")
    @Schema(description = "SSH主机地址")
    private String softServiceLogSshHost;

    /**
     * 日志文件大小（字节）
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "日志文件大小（字节）")
    @Schema(description = "日志文件大小（字节）")
    private Long softServiceLogFileSize;

    /**
     * 日志执行耗时（毫秒）
     */
    @TableField(value = "log_duration")
    @ApiModelProperty(value = "日志执行耗时（毫秒）")
    @Schema(description = "日志执行耗时（毫秒）")
    private Long softServiceLogDuration;

    /**
     * 日志执行命令
     */
    @TableField(value = "log_command")
    @ApiModelProperty(value = "日志执行命令")
    @Schema(description = "日志执行命令")
    private String softServiceLogCommand;

    /**
     * 日志执行错误信息
     */
    @TableField(value = "log_error")
    @ApiModelProperty(value = "日志执行错误信息")
    @Schema(description = "日志执行错误信息")
    private String softServiceLogError;

    /**
     * 日志结束时间
     */
    @TableField(value = "log_end_time")
    @ApiModelProperty(value = "日志结束时间")
    @Schema(description = "日志结束时间")
    private Long softServiceLogEndTime;
}