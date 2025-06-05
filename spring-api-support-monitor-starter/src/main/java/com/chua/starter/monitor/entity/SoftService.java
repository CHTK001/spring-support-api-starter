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
 * @since 2025/6/5 9:19
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
     * 安装命令
     */
    @TableField(value = "soft_service_install")
    @ApiModelProperty(value = "安装命令")
    @Schema(description = "安装命令")
    private String softServiceInstall;

    /**
     * 卸载命令
     */
    @TableField(value = "soft_service_uninstall")
    @ApiModelProperty(value = "卸载命令")
    @Schema(description = "卸载命令")
    private String softServiceUninstall;

    /**
     * 启动命令
     */
    @TableField(value = "soft_service_start")
    @ApiModelProperty(value = "启动命令")
    @Schema(description = "启动命令")
    private String softServiceStart;

    /**
     * 停止命令
     */
    @TableField(value = "soft_service_stop")
    @ApiModelProperty(value = "停止命令")
    @Schema(description = "停止命令")
    private String softServiceStop;

    /**
     * 重启命令
     */
    @TableField(value = "soft_service_restart")
    @ApiModelProperty(value = "重启命令")
    @Schema(description = "重启命令")
    private String softServiceRestart;

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
}