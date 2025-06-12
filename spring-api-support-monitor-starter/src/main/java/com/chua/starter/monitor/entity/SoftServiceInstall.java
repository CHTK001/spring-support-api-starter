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
     * 安装日志路径
     */
    @TableField(value = "install_log_path")
    @ApiModelProperty(value = "安装日志路径")
    @Schema(description = "安装日志路径")
    private String installLogPath;

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
     * 最后一次状态检查时间
     */
    @TableField(value = "install_last_check_time")
    @ApiModelProperty(value = "最后一次状态检查时间")
    @Schema(description = "最后一次状态检查时间")
    private Long installLastCheckTime;

    /**
     * SSH主机地址
     * 非数据库字段，用于前端展示
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "SSH主机地址")
    @Schema(description = "SSH主机地址")
    private String sshHost;
    
    /**
     * SSH端口
     * 非数据库字段，用于前端展示
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "SSH端口")
    @Schema(description = "SSH端口")
    private Integer sshPort;
    
    /**
     * SSH服务器名称
     * 非数据库字段，用于前端展示
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "SSH服务器名称")
    @Schema(description = "SSH服务器名称")
    private String sshName;
    
    /**
     * 软件服务名称
     * 非数据库字段，用于前端展示
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "软件服务名称")
    @Schema(description = "软件服务名称")
    private String softServiceName;

    /**
     * 安装命令
     * 非数据库字段，用于缓存SoftService命令数据
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "安装命令")
    @Schema(description = "安装命令")
    private String softServiceInstallCommand;

    /**
     * 安装命令类型（0:单行命令，1:脚本）
     * 非数据库字段，用于缓存SoftService命令数据
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "安装命令类型（0:单行命令，1:脚本）")
    @Schema(description = "安装命令类型（0:单行命令，1:脚本）")
    private Integer softServiceInstallCommandType;

    /**
     * 卸载命令
     * 非数据库字段，用于缓存SoftService命令数据
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "卸载命令")
    @Schema(description = "卸载命令")
    private String softServiceUninstallCommand;

    /**
     * 卸载命令类型（0:单行命令，1:脚本）
     * 非数据库字段，用于缓存SoftService命令数据
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "卸载命令类型（0:单行命令，1:脚本）")
    @Schema(description = "卸载命令类型（0:单行命令，1:脚本）")
    private Integer softServiceUninstallCommandType;

    /**
     * 启动命令
     * 非数据库字段，用于缓存SoftService命令数据
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "启动命令")
    @Schema(description = "启动命令")
    private String softServiceStartCommand;

    /**
     * 启动命令类型（0:单行命令，1:脚本）
     * 非数据库字段，用于缓存SoftService命令数据
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "启动命令类型（0:单行命令，1:脚本）")
    @Schema(description = "启动命令类型（0:单行命令，1:脚本）")
    private Integer softServiceStartCommandType;

    /**
     * 停止命令
     * 非数据库字段，用于缓存SoftService命令数据
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "停止命令")
    @Schema(description = "停止命令")
    private String softServiceStopCommand;

    /**
     * 停止命令类型（0:单行命令，1:脚本）
     * 非数据库字段，用于缓存SoftService命令数据
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "停止命令类型（0:单行命令，1:脚本）")
    @Schema(description = "停止命令类型（0:单行命令，1:脚本）")
    private Integer softServiceStopCommandType;

    /**
     * 重启命令
     * 非数据库字段，用于缓存SoftService命令数据
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "重启命令")
    @Schema(description = "重启命令")
    private String softServiceRestartCommand;

    /**
     * 重启命令类型（0:单行命令，1:脚本）
     * 非数据库字段，用于缓存SoftService命令数据
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "重启命令类型（0:单行命令，1:脚本）")
    @Schema(description = "重启命令类型（0:单行命令，1:脚本）")
    private Integer softServiceRestartCommandType;

    /**
     * 服务状态检查命令
     * 非数据库字段，用于缓存SoftService命令数据
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "服务状态检查命令")
    @Schema(description = "服务状态检查命令")
    private String softServiceStatusCheckCommand;

    /**
     * 服务状态检查命令类型（0:单行命令，1:脚本）
     * 非数据库字段，用于缓存SoftService命令数据
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "服务状态检查命令类型（0:单行命令，1:脚本）")
    @Schema(description = "服务状态检查命令类型（0:单行命令，1:脚本）")
    private Integer softServiceStatusCheckCommandType;

    /**
     * 服务状态检查成功标识
     * 命令执行结果中包含此字符串则表示服务正常运行，多个标识用逗号分隔，支持通配符
     */
    @TableField(value = "soft_service_status_check_success_flag")
    @ApiModelProperty(value = "服务状态检查成功标识")
    @Schema(description = "服务状态检查成功标识，多个标识用逗号分隔，支持通配符")
    private String softServiceStatusCheckSuccessFlag;

    /**
     * 服务状态检查失败标识
     * 命令执行结果中包含此字符串则表示服务未正常运行，多个标识用逗号分隔，支持通配符，优先级高于成功标识
     */
    @TableField(value = "soft_service_status_check_failure_flag")
    @ApiModelProperty(value = "服务状态检查失败标识")
    @Schema(description = "服务状态检查失败标识，多个标识用逗号分隔，支持通配符，优先级高于成功标识")
    private String softServiceStatusCheckFailureFlag;

    /**
     * 软件服务安装命令成功标识
     * 命令执行结果中包含此字符串则表示安装成功，多个标识用逗号分隔，支持通配符
     */
    @TableField(value = "soft_service_install_success_flag")
    @ApiModelProperty(value = "软件服务安装命令成功标识")
    @Schema(description = "软件服务安装命令成功标识，多个标识用逗号分隔，支持通配符")
    private String softServiceInstallSuccessFlag;

    /**
     * 软件服务安装命令失败标识
     * 命令执行结果中包含此字符串则表示安装失败，多个标识用逗号分隔，支持通配符，优先级高于成功标识
     */
    @TableField(value = "soft_service_install_failure_flag")
    @ApiModelProperty(value = "软件服务安装命令失败标识")
    @Schema(description = "软件服务安装命令失败标识，多个标识用逗号分隔，支持通配符，优先级高于成功标识")
    private String softServiceInstallFailureFlag;

    /**
     * 软件服务卸载命令成功标识
     * 命令执行结果中包含此字符串则表示卸载成功，多个标识用逗号分隔，支持通配符
     */
    @TableField(value = "soft_service_uninstall_success_flag")
    @ApiModelProperty(value = "软件服务卸载命令成功标识")
    @Schema(description = "软件服务卸载命令成功标识，多个标识用逗号分隔，支持通配符")
    private String softServiceUninstallSuccessFlag;

    /**
     * 软件服务卸载命令失败标识
     * 命令执行结果中包含此字符串则表示卸载失败，多个标识用逗号分隔，支持通配符，优先级高于成功标识
     */
    @TableField(value = "soft_service_uninstall_failure_flag")
    @ApiModelProperty(value = "软件服务卸载命令失败标识")
    @Schema(description = "软件服务卸载命令失败标识，多个标识用逗号分隔，支持通配符，优先级高于成功标识")
    private String softServiceUninstallFailureFlag;

    /**
     * 软件服务启动命令成功标识
     * 命令执行结果中包含此字符串则表示启动成功，多个标识用逗号分隔，支持通配符
     */
    @TableField(value = "soft_service_start_success_flag")
    @ApiModelProperty(value = "软件服务启动命令成功标识")
    @Schema(description = "软件服务启动命令成功标识，多个标识用逗号分隔，支持通配符")
    private String softServiceStartSuccessFlag;

    /**
     * 软件服务启动命令失败标识
     * 命令执行结果中包含此字符串则表示启动失败，多个标识用逗号分隔，支持通配符，优先级高于成功标识
     */
    @TableField(value = "soft_service_start_failure_flag")
    @ApiModelProperty(value = "软件服务启动命令失败标识")
    @Schema(description = "软件服务启动命令失败标识，多个标识用逗号分隔，支持通配符，优先级高于成功标识")
    private String softServiceStartFailureFlag;

    /**
     * 软件服务停止命令成功标识
     * 命令执行结果中包含此字符串则表示停止成功，多个标识用逗号分隔，支持通配符
     */
    @TableField(value = "soft_service_stop_success_flag")
    @ApiModelProperty(value = "软件服务停止命令成功标识")
    @Schema(description = "软件服务停止命令成功标识，多个标识用逗号分隔，支持通配符")
    private String softServiceStopSuccessFlag;

    /**
     * 软件服务停止命令失败标识
     * 命令执行结果中包含此字符串则表示停止失败，多个标识用逗号分隔，支持通配符，优先级高于成功标识
     */
    @TableField(value = "soft_service_stop_failure_flag")
    @ApiModelProperty(value = "软件服务停止命令失败标识")
    @Schema(description = "软件服务停止命令失败标识，多个标识用逗号分隔，支持通配符，优先级高于成功标识")
    private String softServiceStopFailureFlag;

    /**
     * 软件服务重启命令成功标识
     * 命令执行结果中包含此字符串则表示重启成功，多个标识用逗号分隔，支持通配符
     */
    @TableField(value = "soft_service_restart_success_flag")
    @ApiModelProperty(value = "软件服务重启命令成功标识")
    @Schema(description = "软件服务重启命令成功标识，多个标识用逗号分隔，支持通配符")
    private String softServiceRestartSuccessFlag;

    /**
     * 软件服务重启命令失败标识
     * 命令执行结果中包含此字符串则表示重启失败，多个标识用逗号分隔，支持通配符，优先级高于成功标识
     */
    @TableField(value = "soft_service_restart_failure_flag")
    @ApiModelProperty(value = "软件服务重启命令失败标识")
    @Schema(description = "软件服务重启命令失败标识，多个标识用逗号分隔，支持通配符，优先级高于成功标识")
    private String softServiceRestartFailureFlag;
} 