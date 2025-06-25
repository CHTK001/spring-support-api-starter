package com.chua.starter.monitor.starter.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 服务器文件上传WebSocket配置实体类
 *
 * @author CH
 * @since 2024/12/20
 */
@ApiModel(description = "服务器文件上传WebSocket配置")
@Schema(description = "服务器文件上传WebSocket配置")
@Data
public class MonitorSysGenServerFileUploadWebSocketConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否启用WebSocket
     */
    @ApiModelProperty(value = "是否启用WebSocket")
    @Schema(description = "是否启用WebSocket")
    private Boolean enabled;

    /**
     * WebSocket端点路径
     */
    @ApiModelProperty(value = "WebSocket端点路径")
    @Schema(description = "WebSocket端点路径")
    private String endpointPath;

    /**
     * 最大连接数
     */
    @ApiModelProperty(value = "最大连接数")
    @Schema(description = "最大连接数")
    private Integer maxConnections;

    /**
     * 连接超时时间(秒)
     */
    @ApiModelProperty(value = "连接超时时间(秒)")
    @Schema(description = "连接超时时间(秒)")
    private Integer connectionTimeout;

    /**
     * 会话超时时间(秒)
     */
    @ApiModelProperty(value = "会话超时时间(秒)")
    @Schema(description = "会话超时时间(秒)")
    private Integer sessionTimeout;

    /**
     * 心跳间隔(秒)
     */
    @ApiModelProperty(value = "心跳间隔(秒)")
    @Schema(description = "心跳间隔(秒)")
    private Integer heartbeatInterval;

    /**
     * 最大消息大小(字节)
     */
    @ApiModelProperty(value = "最大消息大小(字节)")
    @Schema(description = "最大消息大小(字节)")
    private Long maxMessageSize;

    /**
     * 消息缓冲区大小
     */
    @ApiModelProperty(value = "消息缓冲区大小")
    @Schema(description = "消息缓冲区大小")
    private Integer messageBufferSize;

    /**
     * 是否启用消息压缩
     */
    @ApiModelProperty(value = "是否启用消息压缩")
    @Schema(description = "是否启用消息压缩")
    private Boolean compressionEnabled;

    /**
     * 是否启用SSL
     */
    @ApiModelProperty(value = "是否启用SSL")
    @Schema(description = "是否启用SSL")
    private Boolean sslEnabled;

    /**
     * 是否启用跨域
     */
    @ApiModelProperty(value = "是否启用跨域")
    @Schema(description = "是否启用跨域")
    private Boolean corsEnabled;

    /**
     * 允许的跨域来源
     */
    @ApiModelProperty(value = "允许的跨域来源")
    @Schema(description = "允许的跨域来源")
    private String allowedOrigins;

    /**
     * 是否启用进度推送
     */
    @ApiModelProperty(value = "是否启用进度推送")
    @Schema(description = "是否启用进度推送")
    private Boolean progressPushEnabled;

    /**
     * 进度推送间隔(毫秒)
     */
    @ApiModelProperty(value = "进度推送间隔(毫秒)")
    @Schema(description = "进度推送间隔(毫秒)")
    private Integer progressPushInterval;

    /**
     * 是否启用状态推送
     */
    @ApiModelProperty(value = "是否启用状态推送")
    @Schema(description = "是否启用状态推送")
    private Boolean statusPushEnabled;

    /**
     * 是否启用错误推送
     */
    @ApiModelProperty(value = "是否启用错误推送")
    @Schema(description = "是否启用错误推送")
    private Boolean errorPushEnabled;

    /**
     * 是否启用统计推送
     */
    @ApiModelProperty(value = "是否启用统计推送")
    @Schema(description = "是否启用统计推送")
    private Boolean statisticsPushEnabled;

    /**
     * 统计推送间隔(秒)
     */
    @ApiModelProperty(value = "统计推送间隔(秒)")
    @Schema(description = "统计推送间隔(秒)")
    private Integer statisticsPushInterval;

    /**
     * 是否启用告警推送
     */
    @ApiModelProperty(value = "是否启用告警推送")
    @Schema(description = "是否启用告警推送")
    private Boolean alertPushEnabled;

    /**
     * 是否启用队列状态推送
     */
    @ApiModelProperty(value = "是否启用队列状态推送")
    @Schema(description = "是否启用队列状态推送")
    private Boolean queueStatusPushEnabled;

    /**
     * 队列状态推送间隔(秒)
     */
    @ApiModelProperty(value = "队列状态推送间隔(秒)")
    @Schema(description = "队列状态推送间隔(秒)")
    private Integer queueStatusPushInterval;

    /**
     * 是否启用实时监控推送
     */
    @ApiModelProperty(value = "是否启用实时监控推送")
    @Schema(description = "是否启用实时监控推送")
    private Boolean realtimeMonitorPushEnabled;

    /**
     * 实时监控推送间隔(秒)
     */
    @ApiModelProperty(value = "实时监控推送间隔(秒)")
    @Schema(description = "实时监控推送间隔(秒)")
    private Integer realtimeMonitorPushInterval;

    /**
     * 消息重试次数
     */
    @ApiModelProperty(value = "消息重试次数")
    @Schema(description = "消息重试次数")
    private Integer messageRetryCount;

    /**
     * 消息重试间隔(毫秒)
     */
    @ApiModelProperty(value = "消息重试间隔(毫秒)")
    @Schema(description = "消息重试间隔(毫秒)")
    private Integer messageRetryInterval;

    /**
     * 是否启用消息持久化
     */
    @ApiModelProperty(value = "是否启用消息持久化")
    @Schema(description = "是否启用消息持久化")
    private Boolean messagePersistenceEnabled;

    /**
     * 消息保留天数
     */
    @ApiModelProperty(value = "消息保留天数")
    @Schema(description = "消息保留天数")
    private Integer messageRetentionDays;

    /**
     * 线程池大小
     */
    @ApiModelProperty(value = "线程池大小")
    @Schema(description = "线程池大小")
    private Integer threadPoolSize;

    /**
     * 是否启用性能监控
     */
    @ApiModelProperty(value = "是否启用性能监控")
    @Schema(description = "是否启用性能监控")
    private Boolean performanceMonitorEnabled;

    /**
     * 性能监控间隔(秒)
     */
    @ApiModelProperty(value = "性能监控间隔(秒)")
    @Schema(description = "性能监控间隔(秒)")
    private Integer performanceMonitorInterval;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 配置版本
     */
    @ApiModelProperty(value = "配置版本")
    @Schema(description = "配置版本")
    private String version;

    /**
     * 配置描述
     */
    @ApiModelProperty(value = "配置描述")
    @Schema(description = "配置描述")
    private String description;

    /**
     * 获取最大消息大小(KB)
     */
    public Double getMaxMessageSizeKB() {
        return maxMessageSize != null ? maxMessageSize / 1024.0 : 0.0;
    }

    /**
     * 获取最大消息大小(MB)
     */
    public Double getMaxMessageSizeMB() {
        return maxMessageSize != null ? maxMessageSize / (1024.0 * 1024.0) : 0.0;
    }

    /**
     * 是否启用推送功能
     */
    public boolean isPushEnabled() {
        return (progressPushEnabled != null && progressPushEnabled) ||
               (statusPushEnabled != null && statusPushEnabled) ||
               (errorPushEnabled != null && errorPushEnabled) ||
               (statisticsPushEnabled != null && statisticsPushEnabled) ||
               (alertPushEnabled != null && alertPushEnabled) ||
               (queueStatusPushEnabled != null && queueStatusPushEnabled) ||
               (realtimeMonitorPushEnabled != null && realtimeMonitorPushEnabled);
    }

    /**
     * 验证配置有效性
     */
    public boolean isConfigValid() {
        return enabled != null && enabled &&
               endpointPath != null && !endpointPath.trim().isEmpty() &&
               maxConnections != null && maxConnections > 0 &&
               connectionTimeout != null && connectionTimeout > 0 &&
               sessionTimeout != null && sessionTimeout > 0;
    }

    /**
     * 获取连接超时时间(毫秒)
     */
    public Long getConnectionTimeoutMs() {
        return connectionTimeout != null ? connectionTimeout * 1000L : 0L;
    }

    /**
     * 获取会话超时时间(毫秒)
     */
    public Long getSessionTimeoutMs() {
        return sessionTimeout != null ? sessionTimeout * 1000L : 0L;
    }
}
