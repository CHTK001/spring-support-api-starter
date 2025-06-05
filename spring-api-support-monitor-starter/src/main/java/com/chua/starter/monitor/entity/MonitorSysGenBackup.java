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
 * @author CH
 * @since 2025/6/5 11:16
 */

/**
 * 数据源备份
 */
@ApiModel(description = "数据源备份")
@Schema(description = "数据源备份")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "monitor_sys_gen_backup")
public class MonitorSysGenBackup extends SysBase {
    @TableId(value = "monitor_sys_gen_backup_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Integer monitorSysGenBackupId;

    /**
     * 多少天备份一次
     */
    @TableField(value = "monitor_sys_gen_backup_day")
    @ApiModelProperty(value = "多少天备份一次")
    @Schema(description = "多少天备份一次")
    private Integer monitorSysGenBackupDay;

    /**
     * 备份地址
     */
    @TableField(value = "monitor_sys_gen_backup_path")
    @ApiModelProperty(value = "备份地址")
    @Schema(description = "备份地址")
    private String monitorSysGenBackupPath;

    /**
     * 备份结果
     */
    @TableField(value = "monitor_sys_gen_backup_status")
    @ApiModelProperty(value = "备份结果")
    @Schema(description = "备份结果")
    private String monitorSysGenBackupStatus;

    /**
     * 失败后重试次数
     */
    @TableField(value = "monitor_sys_gen_backup_retry")
    @ApiModelProperty(value = "失败后重试次数")
    @Schema(description = "失败后重试次数")
    private Integer monitorSysGenBackupRetry;

    /**
     * 是否开启
     */
    @TableField(value = "monitor_sys_gen_backup_enable")
    @ApiModelProperty(value = "是否开启")
    @Schema(description = "是否开启")
    private Boolean monitorSysGenBackupEnable;

    /**
     * 超时时间(min); 默认30分钟
     */
    @TableField(value = "monitor_sys_gen_backup_timeout")
    @ApiModelProperty(value = "超时时间(min); 默认30分钟")
    @Schema(description = "超时时间(min); 默认30分钟")
    private Integer monitorSysGenBackupTimeout;

    /**
     * 保存天数(day); 默认90天
     */
    @TableField(value = "monitor_sys_gen_backup_retention_days")
    @ApiModelProperty(value = "保存天数(day); 默认90天")
    @Schema(description = "保存天数(day); 默认90天")
    private Integer monitorSysGenBackupRetentionDays;

    /**
     * 数据源ID
     */
    @TableField(value = "monitor_sys_gen_id")
    @ApiModelProperty(value = "数据源ID")
    @Schema(description = "数据源ID")
    private Integer monitorSysGenId;
}