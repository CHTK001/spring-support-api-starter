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

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author CH
 * @since 2025/6/5 11:16
 */

/**
 * 备份日志
 */
@ApiModel(description = "备份日志")
@Schema(description = "备份日志")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "monitor_sys_gen_backup_log")
public class MonitorSysGenBackupLog extends SysBase {
    @TableId(value = "monitor_sys_gen_backup_log_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Integer monitorSysGenBackupLogId;

    /**
     * 备份时间
     */
    @TableField(value = "monitor_sys_gen_backup_log_time")
    @ApiModelProperty(value = "备份时间")
    @Schema(description = "备份时间")
    private LocalDateTime monitorSysGenBackupLogTime;

    /**
     * 备份状态
     */
    @TableField(value = "monitor_sys_gen_backup_log_status")
    @ApiModelProperty(value = "备份状态")
    @Schema(description = "备份状态")
    private String monitorSysGenBackupLogStatus;

    /**
     * 备份日期
     */
    @TableField(value = "monitor_sys_gen_backup_log_day")
    @ApiModelProperty(value = "备份日期")
    @Schema(description = "备份日期")
    private LocalDate monitorSysGenBackupLogDay;

    /**
     * 错误原因
     */
    @TableField(value = "monitor_sys_gen_backup_log_error_message")
    @ApiModelProperty(value = "错误原因")
    @Schema(description = "错误原因")
    private String monitorSysGenBackupLogErrorMessage;

    /**
     * 备份ID
     */
    @TableField(value = "monitor_sys_gen_backup_id")
    @ApiModelProperty(value = "备份ID")
    @Schema(description = "备份ID")
    private Integer monitorSysGenBackupId;

    /**
     * 数据源ID
     */
    @TableField(value = "monitor_sys_gen_id")
    @ApiModelProperty(value = "数据源ID")
    @Schema(description = "数据源ID")
    private Integer monitorSysGenId;
}