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
}