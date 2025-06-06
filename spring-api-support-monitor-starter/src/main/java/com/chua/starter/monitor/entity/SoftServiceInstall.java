package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 软件服务安装记录
 *
 * @author CH
 * @since 2024/7/18
 */
@ApiModel(description = "软件服务安装记录")
@Schema(description = "软件服务安装记录")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "soft_service_install")
public class SoftServiceInstall extends SysBase {

    /**
     * 主键ID
     */
    @TableId(value = "install_id", type = IdType.AUTO)
    @ApiModelProperty(value = "安装记录ID")
    @Schema(description = "安装记录ID")
    @NotNull(message = "安装记录ID不能为空", groups = {UpdateGroup.class})
    private Integer installId;

    /**
     * SSH ID，关联MachineSshModel的ID
     */
    @TableField(value = "ssh_id")
    @ApiModelProperty(value = "SSH ID，关联MachineSshModel的ID")
    @Schema(description = "SSH ID，关联MachineSshModel的ID")
    @NotBlank(message = "SSH ID不能为空", groups = {AddGroup.class})
    private String sshId;

    /**
     * 软件服务ID
     */
    @TableField(value = "soft_service_id")
    @ApiModelProperty(value = "软件服务ID")
    @Schema(description = "软件服务ID")
    @NotNull(message = "软件服务ID不能为空", groups = {AddGroup.class})
    private Integer softServiceId;

    /**
     * 安装状态：0-未安装，1-安装中，2-安装成功，3-安装失败
     */
    @TableField(value = "install_status")
    @ApiModelProperty(value = "安装状态：0-未安装，1-安装中，2-安装成功，3-安装失败")
    @Schema(description = "安装状态：0-未安装，1-安装中，2-安装成功，3-安装失败")
    private Integer installStatus;

    /**
     * 安装路径
     */
    @TableField(value = "install_path")
    @ApiModelProperty(value = "安装路径")
    @Schema(description = "安装路径")
    private String installPath;

    /**
     * 安装日志
     */
    @TableField(value = "install_log")
    @ApiModelProperty(value = "安装日志")
    @Schema(description = "安装日志")
    private String installLog;

    /**
     * 安装版本
     */
    @TableField(value = "install_version")
    @ApiModelProperty(value = "安装版本")
    @Schema(description = "安装版本")
    private String installVersion;

    /**
     * 安装时间
     */
    @TableField(value = "install_time")
    @ApiModelProperty(value = "安装时间")
    @Schema(description = "安装时间")
    private Long installTime;

    /**
     * 最后一次启动时间
     */
    @TableField(value = "install_last_start_time")
    @ApiModelProperty(value = "最后一次启动时间")
    @Schema(description = "最后一次启动时间")
    private Long installLastStartTime;

    /**
     * 最后一次停止时间
     */
    @TableField(value = "install_last_stop_time")
    @ApiModelProperty(value = "最后一次停止时间")
    @Schema(description = "最后一次停止时间")
    private Long installLastStopTime;

    /**
     * 运行状态：0-未运行，1-运行中
     */
    @TableField(value = "install_run_status")
    @ApiModelProperty(value = "运行状态：0-未运行，1-运行中")
    @Schema(description = "运行状态：0-未运行，1-运行中")
    private Integer installRunStatus;

    /**
     * 配置文件目录
     */
    @TableField(value = "install_config_path")
    @ApiModelProperty(value = "配置文件目录")
    @Schema(description = "配置文件目录")
    private String installConfigPath;

    /**
     * 启动命令
     */
    @TableField(value = "install_start_command")
    @ApiModelProperty(value = "启动命令")
    @Schema(description = "启动命令")
    private String installStartCommand;

    /**
     * 重启命令
     */
    @TableField(value = "install_restart_command")
    @ApiModelProperty(value = "重启命令")
    @Schema(description = "重启命令")
    private String installRestartCommand;

    /**
     * 关闭命令
     */
    @TableField(value = "install_stop_command")
    @ApiModelProperty(value = "关闭命令")
    @Schema(description = "关闭命令")
    private String installStopCommand;

    /**
     * 日志目录
     */
    @TableField(value = "install_log_path")
    @ApiModelProperty(value = "日志目录")
    @Schema(description = "日志目录")
    private String installLogPath;

    /**
     * 卸载命令
     */
    @TableField(value = "install_uninstall_command")
    @ApiModelProperty(value = "卸载命令")
    @Schema(description = "卸载命令")
    private String installUninstallCommand;
} 