package com.chua.starter.monitor.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务器信息实体类
 *
 * @author CH
 * @since 2024/7/19
 */
@ApiModel(description = "服务器信息")
@Schema(description = "服务器信息")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerInfo {

    /**
     * SSH ID
     */
    @ApiModelProperty(value = "SSH ID")
    @Schema(description = "SSH ID")
    private String sshId;

    /**
     * 安装记录ID
     */
    @ApiModelProperty(value = "安装记录ID")
    @Schema(description = "安装记录ID")
    private Integer installId;

    /**
     * 安装状态：0-未安装，1-安装中，2-安装成功，3-安装失败
     */
    @ApiModelProperty(value = "安装状态")
    @Schema(description = "安装状态")
    private Integer installStatus;

    /**
     * 安装状态描述
     */
    @ApiModelProperty(value = "安装状态描述")
    @Schema(description = "安装状态描述")
    private String installStatusDesc;

    /**
     * 运行状态：0-未运行，1-运行中
     */
    @ApiModelProperty(value = "运行状态")
    @Schema(description = "运行状态")
    private Integer runStatus;

    /**
     * 运行状态描述
     */
    @ApiModelProperty(value = "运行状态描述")
    @Schema(description = "运行状态描述")
    private String runStatusDesc;

    /**
     * 安装时间
     */
    @ApiModelProperty(value = "安装时间")
    @Schema(description = "安装时间")
    private Long installTime;

    /**
     * 安装路径
     */
    @ApiModelProperty(value = "安装路径")
    @Schema(description = "安装路径")
    private String installPath;

    /**
     * 安装版本
     */
    @ApiModelProperty(value = "安装版本")
    @Schema(description = "安装版本")
    private String installVersion;

    /**
     * 服务器名称
     */
    @ApiModelProperty(value = "服务器名称")
    @Schema(description = "服务器名称")
    private String serverName;

    /**
     * 服务器主机地址
     */
    @ApiModelProperty(value = "服务器主机地址")
    @Schema(description = "服务器主机地址")
    private String serverHost;

    /**
     * 主机名
     */
    @ApiModelProperty(value = "主机名")
    @Schema(description = "主机名")
    private String hostName;

    /**
     * 操作系统
     */
    @ApiModelProperty(value = "操作系统")
    @Schema(description = "操作系统")
    private String osName;
} 