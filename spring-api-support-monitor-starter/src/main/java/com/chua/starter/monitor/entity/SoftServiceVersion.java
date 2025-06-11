package com.chua.starter.monitor.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 软件服务版本信息
 *
 * @author CH
 * @since 2024/7/20
 */
@ApiModel(description = "软件服务版本信息")
@Schema(description = "软件服务版本信息")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SoftServiceVersion {

    /**
     * 软件服务ID
     */
    @ApiModelProperty(value = "软件服务ID")
    @Schema(description = "软件服务ID")
    private Integer softServiceId;

    /**
     * 版本号
     */
    @ApiModelProperty(value = "版本号")
    @Schema(description = "版本号")
    private String version;

    /**
     * 下载地址
     */
    @ApiModelProperty(value = "下载地址")
    @Schema(description = "下载地址")
    private String downloadUrl;

    /**
     * 发布时间
     */
    @ApiModelProperty(value = "发布时间")
    @Schema(description = "发布时间")
    private LocalDateTime releaseTime;

    /**
     * 版本描述
     */
    @ApiModelProperty(value = "版本描述")
    @Schema(description = "版本描述")
    private String versionDesc;

    /**
     * 是否为当前版本
     */
    @ApiModelProperty(value = "是否为当前版本")
    @Schema(description = "是否为当前版本")
    private Boolean isCurrent;

    /**
     * 是否可以安装
     * 根据安装脚本是否存在来决定
     */
    @ApiModelProperty(value = "是否可以安装")
    @Schema(description = "是否可以安装，根据安装脚本是否存在来决定")
    private Boolean isInstallable;

    /**
     * 安装命令
     * 用于判断是否可以安装，不会返回给前端
     */
    @ApiModelProperty(value = "安装命令", hidden = true)
    @Schema(description = "安装命令", hidden = true)
    private String installCommand;
} 