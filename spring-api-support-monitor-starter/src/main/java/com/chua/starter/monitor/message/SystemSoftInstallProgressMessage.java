package com.chua.starter.monitor.message;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统软件安装卸载进度消息
 *
 * @author CH
 * @version 1.0
 * @since 2025/01/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "系统软件安装卸载进度消息")
@Schema(description = "系统软件安装卸载进度消息")
public class SystemSoftInstallProgressMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息类型
     */
    @ApiModelProperty(value = "消息类型")
    @Schema(description = "消息类型")
    private String messageType;

    /**
     * 任务ID
     */
    @ApiModelProperty(value = "任务ID")
    @Schema(description = "任务ID")
    private String taskId;

    /**
     * 服务器ID
     */
    @ApiModelProperty(value = "服务器ID")
    @Schema(description = "服务器ID")
    private Integer serverId;

    /**
     * 服务器名称
     */
    @ApiModelProperty(value = "服务器名称")
    @Schema(description = "服务器名称")
    private String serverName;

    /**
     * 软件ID
     */
    @ApiModelProperty(value = "软件ID")
    @Schema(description = "软件ID")
    private Integer softId;

    /**
     * 软件名称
     */
    @ApiModelProperty(value = "软件名称")
    @Schema(description = "软件名称")
    private String softName;

    /**
     * 软件版本ID
     */
    @ApiModelProperty(value = "软件版本ID")
    @Schema(description = "软件版本ID")
    private Integer softVersionId;

    /**
     * 软件版本名称
     */
    @ApiModelProperty(value = "软件版本名称")
    @Schema(description = "软件版本名称")
    private String softVersionName;

    /**
     * 操作类型：INSTALL-安装，UNINSTALL-卸载
     */
    @ApiModelProperty(value = "操作类型")
    @Schema(description = "操作类型：INSTALL-安装，UNINSTALL-卸载")
    private String operationType;

    /**
     * 当前步骤
     */
    @ApiModelProperty(value = "当前步骤")
    @Schema(description = "当前步骤")
    private String currentStep;

    /**
     * 进度百分比（0-100）
     */
    @ApiModelProperty(value = "进度百分比")
    @Schema(description = "进度百分比（0-100）")
    private Integer progress;

    /**
     * 状态：PENDING-等待中，RUNNING-进行中，SUCCESS-成功，FAILED-失败
     */
    @ApiModelProperty(value = "状态")
    @Schema(description = "状态：PENDING-等待中，RUNNING-进行中，SUCCESS-成功，FAILED-失败")
    private String status;

    /**
     * 状态消息
     */
    @ApiModelProperty(value = "状态消息")
    @Schema(description = "状态消息")
    private String message;

    /**
     * 错误信息
     */
    @ApiModelProperty(value = "错误信息")
    @Schema(description = "错误信息")
    private String errorMessage;

    /**
     * 容器ID（Docker安装时）
     */
    @ApiModelProperty(value = "容器ID")
    @Schema(description = "容器ID（Docker安装时）")
    private String containerId;

    /**
     * 开始时间
     */
    @ApiModelProperty(value = "开始时间")
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @ApiModelProperty(value = "结束时间")
    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    /**
     * 消息时间戳
     */
    @ApiModelProperty(value = "消息时间戳")
    @Schema(description = "消息时间戳")
    private Long timestamp;

    /**
     * 扩展数据
     */
    @ApiModelProperty(value = "扩展数据")
    @Schema(description = "扩展数据")
    private Object data;

    /**
     * 创建开始消息
     */
    public static SystemSoftInstallProgressMessage createStartMessage(String taskId, Integer serverId, String serverName,
                                                                      Integer softId, String softName,
                                                                      Integer softVersionId, String softVersionName,
                                                                      String operationType) {
        return SystemSoftInstallProgressMessage.builder()
                .messageType("system_soft_install_progress")
                .taskId(taskId)
                .serverId(serverId)
                .serverName(serverName)
                .softId(softId)
                .softName(softName)
                .softVersionId(softVersionId)
                .softVersionName(softVersionName)
                .operationType(operationType)
                .currentStep("开始" + ("INSTALL".equals(operationType) ? "安装" : "卸载"))
                .progress(0)
                .status("RUNNING")
                .message("开始" + ("INSTALL".equals(operationType) ? "安装" : "卸载") + "软件")
                .startTime(LocalDateTime.now())
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建进度消息
     */
    public static SystemSoftInstallProgressMessage createProgressMessage(String taskId, String currentStep,
                                                                         Integer progress, String message) {
        return SystemSoftInstallProgressMessage.builder()
                .messageType("system_soft_install_progress")
                .taskId(taskId)
                .currentStep(currentStep)
                .progress(progress)
                .status("RUNNING")
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建成功消息
     */
    public static SystemSoftInstallProgressMessage createSuccessMessage(String taskId, String operationType,
                                                                        String containerId) {
        return SystemSoftInstallProgressMessage.builder()
                .messageType("system_soft_install_progress")
                .taskId(taskId)
                .currentStep(("INSTALL".equals(operationType) ? "安装" : "卸载") + "完成")
                .progress(100)
                .status("SUCCESS")
                .message(("INSTALL".equals(operationType) ? "安装" : "卸载") + "成功")
                .containerId(containerId)
                .endTime(LocalDateTime.now())
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建失败消息
     */
    public static SystemSoftInstallProgressMessage createFailedMessage(String taskId, String operationType,
                                                                       String errorMessage) {
        return SystemSoftInstallProgressMessage.builder()
                .messageType("system_soft_install_progress")
                .taskId(taskId)
                .currentStep(("INSTALL".equals(operationType) ? "安装" : "卸载") + "失败")
                .progress(0)
                .status("FAILED")
                .message(("INSTALL".equals(operationType) ? "安装" : "卸载") + "失败")
                .errorMessage(errorMessage)
                .endTime(LocalDateTime.now())
                .timestamp(System.currentTimeMillis())
                .build();
    }
}