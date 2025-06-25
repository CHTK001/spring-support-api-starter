package com.chua.starter.monitor.starter.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 服务器文件上传WebSocket会话信息实体类
 *
 * @author CH
 * @since 2024/12/20
 */
@ApiModel(description = "服务器文件上传WebSocket会话信息")
@Schema(description = "服务器文件上传WebSocket会话信息")
@Data
public class MonitorSysGenServerFileUploadWebSocketSessionInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    @ApiModelProperty(value = "会话ID")
    @Schema(description = "会话ID")
    private String sessionId;

    /**
     * 用户ID
     */
    @ApiModelProperty(value = "用户ID")
    @Schema(description = "用户ID")
    private String userId;

    /**
     * 用户名
     */
    @ApiModelProperty(value = "用户名")
    @Schema(description = "用户名")
    private String username;

    /**
     * 客户端IP
     */
    @ApiModelProperty(value = "客户端IP")
    @Schema(description = "客户端IP")
    private String clientIp;

    /**
     * 用户代理
     */
    @ApiModelProperty(value = "用户代理")
    @Schema(description = "用户代理")
    private String userAgent;

    /**
     * 连接时间
     */
    @ApiModelProperty(value = "连接时间")
    @Schema(description = "连接时间")
    private LocalDateTime connectTime;

    /**
     * 最后活跃时间
     */
    @ApiModelProperty(value = "最后活跃时间")
    @Schema(description = "最后活跃时间")
    private LocalDateTime lastActiveTime;

    /**
     * 连接状态
     */
    @ApiModelProperty(value = "连接状态")
    @Schema(description = "连接状态(CONNECTED:已连接 DISCONNECTED:已断开 ERROR:错误)")
    private String connectionStatus;

    /**
     * 订阅的任务ID列表
     */
    @ApiModelProperty(value = "订阅的任务ID列表")
    @Schema(description = "订阅的任务ID列表")
    private List<Long> subscribedTaskIds;

    /**
     * 订阅的服务器ID列表
     */
    @ApiModelProperty(value = "订阅的服务器ID列表")
    @Schema(description = "订阅的服务器ID列表")
    private List<Integer> subscribedServerIds;

    /**
     * 是否订阅队列状态
     */
    @ApiModelProperty(value = "是否订阅队列状态")
    @Schema(description = "是否订阅队列状态")
    private Boolean subscribedQueueStatus;

    /**
     * 是否订阅统计信息
     */
    @ApiModelProperty(value = "是否订阅统计信息")
    @Schema(description = "是否订阅统计信息")
    private Boolean subscribedStatistics;

    /**
     * 是否订阅系统告警
     */
    @ApiModelProperty(value = "是否订阅系统告警")
    @Schema(description = "是否订阅系统告警")
    private Boolean subscribedAlerts;

    /**
     * 发送消息数量
     */
    @ApiModelProperty(value = "发送消息数量")
    @Schema(description = "发送消息数量")
    private Long sentMessageCount;

    /**
     * 接收消息数量
     */
    @ApiModelProperty(value = "接收消息数量")
    @Schema(description = "接收消息数量")
    private Long receivedMessageCount;

    /**
     * 错误消息数量
     */
    @ApiModelProperty(value = "错误消息数量")
    @Schema(description = "错误消息数量")
    private Long errorMessageCount;

    /**
     * 连接持续时间(毫秒)
     */
    @ApiModelProperty(value = "连接持续时间(毫秒)")
    @Schema(description = "连接持续时间(毫秒)")
    private Long connectionDuration;

    /**
     * 平均响应时间(毫秒)
     */
    @ApiModelProperty(value = "平均响应时间(毫秒)")
    @Schema(description = "平均响应时间(毫秒)")
    private Long avgResponseTime;

    /**
     * 最后错误信息
     */
    @ApiModelProperty(value = "最后错误信息")
    @Schema(description = "最后错误信息")
    private String lastErrorMessage;

    /**
     * 连接状态枚举
     */
    public enum ConnectionStatus {
        CONNECTED("CONNECTED", "已连接"),
        DISCONNECTED("DISCONNECTED", "已断开"),
        ERROR("ERROR", "错误");

        private final String code;
        private final String desc;

        ConnectionStatus(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 计算连接持续时间
     */
    public void calculateConnectionDuration() {
        if (connectTime != null) {
            LocalDateTime endTime = lastActiveTime != null ? lastActiveTime : LocalDateTime.now();
            this.connectionDuration = java.time.Duration.between(connectTime, endTime).toMillis();
        }
    }

    /**
     * 是否连接中
     */
    public boolean isConnected() {
        return ConnectionStatus.CONNECTED.getCode().equals(connectionStatus);
    }

    /**
     * 是否有订阅
     */
    public boolean hasSubscriptions() {
        return (subscribedTaskIds != null && !subscribedTaskIds.isEmpty()) ||
               (subscribedServerIds != null && !subscribedServerIds.isEmpty()) ||
               (subscribedQueueStatus != null && subscribedQueueStatus) ||
               (subscribedStatistics != null && subscribedStatistics) ||
               (subscribedAlerts != null && subscribedAlerts);
    }

    /**
     * 获取连接持续时间(秒)
     */
    public Double getConnectionDurationSeconds() {
        return connectionDuration != null ? connectionDuration / 1000.0 : 0.0;
    }

    /**
     * 获取消息发送率(消息/分钟)
     */
    public Double getMessageSendRate() {
        if (connectionDuration != null && connectionDuration > 0 && sentMessageCount != null) {
            return sentMessageCount * 60000.0 / connectionDuration;
        }
        return 0.0;
    }

    /**
     * 是否活跃会话
     */
    public boolean isActiveSession() {
        if (lastActiveTime == null) {
            return false;
        }
        return java.time.Duration.between(lastActiveTime, LocalDateTime.now()).toMinutes() < 5;
    }
}

