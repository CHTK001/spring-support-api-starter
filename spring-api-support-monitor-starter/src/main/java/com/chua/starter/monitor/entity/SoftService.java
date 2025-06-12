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

import java.util.List;

/**
 *
 * @author CH
 * @since 2025/6/9 15:34
 */

/**
 * 软件服务
 */
@ApiModel(description = "软件服务")
@Schema(description = "软件服务")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "soft_service")
public class SoftService extends SysBase {
    @TableId(value = "soft_service_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Integer softServiceId;

    /**
     * 名称
     */
    @TableField(value = "soft_service_name")
    @ApiModelProperty(value = "名称")
    @Schema(description = "名称")
    private String softServiceName;

    /**
     * logo
     */
    @TableField(value = "soft_service_logo")
    @ApiModelProperty(value = "logo")
    @Schema(description = "logo")
    private String softServiceLogo;

    /**
     * 分类
     */
    @TableField(value = "soft_service_category")
    @ApiModelProperty(value = "分类")
    @Schema(description = "分类")
    private String softServiceCategory;

    /**
     * 操作系统
     */
    @TableField(value = "soft_service_os")
    @ApiModelProperty(value = "操作系统")
    @Schema(description = "操作系统")
    private String softServiceOs;

    /**
     * 安装命令
     */
    @TableField(value = "soft_service_install_command")
    @ApiModelProperty(value = "安装命令")
    @Schema(description = "安装命令")
    private String softServiceInstallCommand;

    /**
     * 安装命令类型（0:单行命令，1:脚本）
     */
    @TableField(value = "soft_service_install_command_type")
    @ApiModelProperty(value = "安装命令类型（0:单行命令，1:脚本）")
    @Schema(description = "安装命令类型（0:单行命令，1:脚本）")
    private Integer softServiceInstallCommandType;

    /**
     * 安装命令成功标识
     * 命令执行结果中包含此字符串则表示安装成功，多个标识用逗号分隔，支持通配符
     */
    @TableField(value = "soft_service_install_success_flag")
    @ApiModelProperty(value = "安装命令成功标识")
    @Schema(description = "安装命令成功标识，多个标识用逗号分隔，支持通配符")
    private String softServiceInstallSuccessFlag;

    /**
     * 安装命令失败标识
     * 命令执行结果中包含此字符串则表示安装失败，多个标识用逗号分隔，支持通配符，优先级高于成功标识
     */
    @TableField(value = "soft_service_install_failure_flag")
    @ApiModelProperty(value = "安装命令失败标识")
    @Schema(description = "安装命令失败标识，多个标识用逗号分隔，支持通配符，优先级高于成功标识")
    private String softServiceInstallFailureFlag;

    /**
     * 卸载命令
     */
    @TableField(value = "soft_service_uninstall_command")
    @ApiModelProperty(value = "卸载命令")
    @Schema(description = "卸载命令")
    private String softServiceUninstallCommand;

    /**
     * 卸载命令类型（0:单行命令，1:脚本）
     */
    @TableField(value = "soft_service_uninstall_command_type")
    @ApiModelProperty(value = "卸载命令类型（0:单行命令，1:脚本）")
    @Schema(description = "卸载命令类型（0:单行命令，1:脚本）")
    private Integer softServiceUninstallCommandType;

    /**
     * 卸载命令成功标识
     * 命令执行结果中包含此字符串则表示卸载成功，多个标识用逗号分隔，支持通配符
     */
    @TableField(value = "soft_service_uninstall_success_flag")
    @ApiModelProperty(value = "卸载命令成功标识")
    @Schema(description = "卸载命令成功标识，多个标识用逗号分隔，支持通配符")
    private String softServiceUninstallSuccessFlag;

    /**
     * 卸载命令失败标识
     * 命令执行结果中包含此字符串则表示卸载失败，多个标识用逗号分隔，支持通配符，优先级高于成功标识
     */
    @TableField(value = "soft_service_uninstall_failure_flag")
    @ApiModelProperty(value = "卸载命令失败标识")
    @Schema(description = "卸载命令失败标识，多个标识用逗号分隔，支持通配符，优先级高于成功标识")
    private String softServiceUninstallFailureFlag;

    /**
     * 启动命令
     */
    @TableField(value = "soft_service_start_command")
    @ApiModelProperty(value = "启动命令")
    @Schema(description = "启动命令")
    private String softServiceStartCommand;

    /**
     * 启动命令类型（0:单行命令，1:脚本）
     */
    @TableField(value = "soft_service_start_command_type")
    @ApiModelProperty(value = "启动命令类型（0:单行命令，1:脚本）")
    @Schema(description = "启动命令类型（0:单行命令，1:脚本）")
    private Integer softServiceStartCommandType;

    /**
     * 启动命令成功标识
     * 命令执行结果中包含此字符串则表示启动成功，多个标识用逗号分隔，支持通配符
     */
    @TableField(value = "soft_service_start_success_flag")
    @ApiModelProperty(value = "启动命令成功标识")
    @Schema(description = "启动命令成功标识，多个标识用逗号分隔，支持通配符")
    private String softServiceStartSuccessFlag;

    /**
     * 启动命令失败标识
     * 命令执行结果中包含此字符串则表示启动失败，多个标识用逗号分隔，支持通配符，优先级高于成功标识
     */
    @TableField(value = "soft_service_start_failure_flag")
    @ApiModelProperty(value = "启动命令失败标识")
    @Schema(description = "启动命令失败标识，多个标识用逗号分隔，支持通配符，优先级高于成功标识")
    private String softServiceStartFailureFlag;

    /**
     * 停止命令
     */
    @TableField(value = "soft_service_stop_command")
    @ApiModelProperty(value = "停止命令")
    @Schema(description = "停止命令")
    private String softServiceStopCommand;

    /**
     * 停止命令类型（0:单行命令，1:脚本）
     */
    @TableField(value = "soft_service_stop_command_type")
    @ApiModelProperty(value = "停止命令类型（0:单行命令，1:脚本）")
    @Schema(description = "停止命令类型（0:单行命令，1:脚本）")
    private Integer softServiceStopCommandType;

    /**
     * 停止命令成功标识
     * 命令执行结果中包含此字符串则表示停止成功，多个标识用逗号分隔，支持通配符
     */
    @TableField(value = "soft_service_stop_success_flag")
    @ApiModelProperty(value = "停止命令成功标识")
    @Schema(description = "停止命令成功标识，多个标识用逗号分隔，支持通配符")
    private String softServiceStopSuccessFlag;

    /**
     * 停止命令失败标识
     * 命令执行结果中包含此字符串则表示停止失败，多个标识用逗号分隔，支持通配符，优先级高于成功标识
     */
    @TableField(value = "soft_service_stop_failure_flag")
    @ApiModelProperty(value = "停止命令失败标识")
    @Schema(description = "停止命令失败标识，多个标识用逗号分隔，支持通配符，优先级高于成功标识")
    private String softServiceStopFailureFlag;

    /**
     * 重启命令
     */
    @TableField(value = "soft_service_restart_command")
    @ApiModelProperty(value = "重启命令")
    @Schema(description = "重启命令")
    private String softServiceRestartCommand;

    /**
     * 重启命令类型（0:单行命令，1:脚本）
     */
    @TableField(value = "soft_service_restart_command_type")
    @ApiModelProperty(value = "重启命令类型（0:单行命令，1:脚本）")
    @Schema(description = "重启命令类型（0:单行命令，1:脚本）")
    private Integer softServiceRestartCommandType;

    /**
     * 重启命令成功标识
     * 命令执行结果中包含此字符串则表示重启成功，多个标识用逗号分隔，支持通配符
     */
    @TableField(value = "soft_service_restart_success_flag")
    @ApiModelProperty(value = "重启命令成功标识")
    @Schema(description = "重启命令成功标识，多个标识用逗号分隔，支持通配符")
    private String softServiceRestartSuccessFlag;

    /**
     * 重启命令失败标识
     * 命令执行结果中包含此字符串则表示重启失败，多个标识用逗号分隔，支持通配符，优先级高于成功标识
     */
    @TableField(value = "soft_service_restart_failure_flag")
    @ApiModelProperty(value = "重启命令失败标识")
    @Schema(description = "重启命令失败标识，多个标识用逗号分隔，支持通配符，优先级高于成功标识")
    private String softServiceRestartFailureFlag;

    /**
     * 服务状态检查命令
     * 用于检查服务是否正常运行的命令，如：ps -ef | grep xxx
     */
    @TableField(value = "soft_service_status_check_command")
    @ApiModelProperty(value = "服务状态检查命令")
    @Schema(description = "服务状态检查命令")
    private String softServiceStatusCheckCommand;

    /**
     * 服务状态检查命令类型（0:单行命令，1:脚本）
     */
    @TableField(value = "soft_service_status_check_command_type")
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
     * 是否安装
     */
    @TableField(value = "soft_service_installed")
    @ApiModelProperty(value = "是否安装")
    @Schema(description = "是否安装")
    private Boolean softServiceInstalled;

    /**
     * 描述
     */
    @TableField(value = "soft_service_remark")
    @ApiModelProperty(value = "描述")
    @Schema(description = "描述")
    private String softServiceRemark;

    /**
     * 版本号
     */
    @TableField(value = "soft_service_version")
    @ApiModelProperty(value = "版本号")
    @Schema(description = "版本号")
    private String softServiceVersion;

    /**
     * 下载地址
     */
    @TableField(value = "soft_service_download_url")
    @ApiModelProperty(value = "下载地址")
    @Schema(description = "下载地址")
    private String softServiceDownloadUrl;

    /**
     * 标签
     */
    @TableField(value = "soft_service_tags")
    @ApiModelProperty(value = "标签")
    @Schema(description = "标签")
    private String softServiceTags;
    /**
     * sshId
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "sshId")
    @Schema(description = "sshId")
    private String sshId;
    /**
     * 是否安装
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "是否安装")
    @Schema(description = "是否安装")
    private Boolean isInstalled;

    /**
     * 安装次数
     */
    @TableField(value = "install_count")
    @ApiModelProperty(value = "安装次数")
    @Schema(description = "安装次数")
    private Integer installCount;

    /**
     * 收藏次数
     */
    @TableField(value = "favorite_count")
    @ApiModelProperty(value = "收藏次数")
    @Schema(description = "收藏次数")
    private Integer favoriteCount;

    /**
     * 已安装的服务器列表
     * 包含服务器ID、名称和安装状态等信息
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "已安装的服务器列表")
    @Schema(description = "已安装的服务器列表")
    private List<ServerInfo> installedServers;

    /**
     * 软件版本列表
     * 包含不同版本的软件服务信息
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "软件版本列表")
    @Schema(description = "软件版本列表")
    private List<SoftServiceVersion> versions;

    /**
     * 是否为分组头
     * 用于前端展示，标识是否为同名软件的分组头
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "是否为分组头")
    @Schema(description = "是否为分组头")
    private Boolean isGroupHeader;
}