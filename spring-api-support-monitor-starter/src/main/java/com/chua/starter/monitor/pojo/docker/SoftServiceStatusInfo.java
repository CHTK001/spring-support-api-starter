package com.chua.starter.monitor.pojo.docker;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 软件服务状态信息
 *
 * @author CH
 * @since 2025/6/10
 */
@ApiModel(description = "软件服务状态信息")
@Schema(description = "软件服务状态信息")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SoftServiceStatusInfo implements Serializable {

    /**
     * 安装记录ID
     */
    @ApiModelProperty(value = "安装记录ID")
    @Schema(description = "安装记录ID")
    private Integer installId;

    /**
     * 软件服务ID
     */
    @ApiModelProperty(value = "软件服务ID")
    @Schema(description = "软件服务ID")
    private Integer softServiceId;

    /**
     * 软件服务名称
     */
    @ApiModelProperty(value = "软件服务名称")
    @Schema(description = "软件服务名称")
    private String softServiceName;

    /**
     * 安装状态：0-未安装，1-安装中，2-安装成功，3-安装失败
     */
    @ApiModelProperty(value = "安装状态：0-未安装，1-安装中，2-安装成功，3-安装失败")
    @Schema(description = "安装状态：0-未安装，1-安装中，2-安装成功，3-安装失败")
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
    @ApiModelProperty(value = "运行状态：0-未运行，1-运行中")
    @Schema(description = "运行状态：0-未运行，1-运行中")
    private Integer runStatus;

    /**
     * 运行状态描述
     */
    @ApiModelProperty(value = "运行状态描述")
    @Schema(description = "运行状态描述")
    private String runStatusDesc;

    /**
     * 进程ID
     */
    @ApiModelProperty(value = "进程ID")
    @Schema(description = "进程ID")
    private String pid;

    /**
     * 内存占用（MB）
     */
    @ApiModelProperty(value = "内存占用（MB）")
    @Schema(description = "内存占用（MB）")
    private Double memoryUsage;

    /**
     * CPU占用（%）
     */
    @ApiModelProperty(value = "CPU占用（%）")
    @Schema(description = "CPU占用（%）")
    private Double cpuUsage;

    /**
     * 运行时长（秒）
     */
    @ApiModelProperty(value = "运行时长（秒）")
    @Schema(description = "运行时长（秒）")
    private Long runningTime;

    /**
     * 安装路径
     */
    @ApiModelProperty(value = "安装路径")
    @Schema(description = "安装路径")
    private String installPath;

    /**
     * 是否存在
     */
    @ApiModelProperty(value = "是否存在")
    @Schema(description = "是否存在")
    private Boolean exists;

    /**
     * 状态检测时间
     */
    @ApiModelProperty(value = "状态检测时间")
    @Schema(description = "状态检测时间")
    private Long checkTime;

    /**
     * 状态消息
     */
    @ApiModelProperty(value = "状态消息")
    @Schema(description = "状态消息")
    private String message;
} 