package com.chua.starter.monitor.starter.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 服务器文件上传WebSocket消息统计实体类
 *
 * @author CH
 * @since 2024/12/20
 */
@ApiModel(description = "服务器文件上传WebSocket消息统计")
@Schema(description = "服务器文件上传WebSocket消息统计")
@Data
public class MonitorSysGenServerFileUploadWebSocketMessageStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 统计时间
     */
    @ApiModelProperty(value = "统计时间")
    @Schema(description = "统计时间")
    private LocalDateTime statisticsTime;

    /**
     * 总发送消息数
     */
    @ApiModelProperty(value = "总发送消息数")
    @Schema(description = "总发送消息数")
    private Long totalSentMessages;

    /**
     * 总接收消息数
     */
    @ApiModelProperty(value = "总接收消息数")
    @Schema(description = "总接收消息数")
    private Long totalReceivedMessages;

    /**
     * 进度消息数
     */
    @ApiModelProperty(value = "进度消息数")
    @Schema(description = "进度消息数")
    private Long progressMessages;

    /**
     * 状态变更消息数
     */
    @ApiModelProperty(value = "状态变更消息数")
    @Schema(description = "状态变更消息数")
    private Long statusChangeMessages;

    /**
     * 错误消息数
     */
    @ApiModelProperty(value = "错误消息数")
    @Schema(description = "错误消息数")
    private Long errorMessages;

    /**
     * 告警消息数
     */
    @ApiModelProperty(value = "告警消息数")
    @Schema(description = "告警消息数")
    private Long alertMessages;

    /**
     * 统计消息数
     */
    @ApiModelProperty(value = "统计消息数")
    @Schema(description = "统计消息数")
    private Long statisticsMessages;

    /**
     * 系统消息数
     */
    @ApiModelProperty(value = "系统消息数")
    @Schema(description = "系统消息数")
    private Long systemMessages;

    /**
     * 私有消息数
     */
    @ApiModelProperty(value = "私有消息数")
    @Schema(description = "私有消息数")
    private Long privateMessages;

    /**
     * 广播消息数
     */
    @ApiModelProperty(value = "广播消息数")
    @Schema(description = "广播消息数")
    private Long broadcastMessages;

    /**
     * 失败消息数
     */
    @ApiModelProperty(value = "失败消息数")
    @Schema(description = "失败消息数")
    private Long failedMessages;

    /**
     * 平均消息大小(字节)
     */
    @ApiModelProperty(value = "平均消息大小(字节)")
    @Schema(description = "平均消息大小(字节)")
    private Long avgMessageSize;

    /**
     * 最大消息大小(字节)
     */
    @ApiModelProperty(value = "最大消息大小(字节)")
    @Schema(description = "最大消息大小(字节)")
    private Long maxMessageSize;

    /**
     * 平均发送延迟(毫秒)
     */
    @ApiModelProperty(value = "平均发送延迟(毫秒)")
    @Schema(description = "平均发送延迟(毫秒)")
    private Long avgSendLatency;

    /**
     * 最大发送延迟(毫秒)
     */
    @ApiModelProperty(value = "最大发送延迟(毫秒)")
    @Schema(description = "最大发送延迟(毫秒)")
    private Long maxSendLatency;

    /**
     * 消息发送成功率(%)
     */
    @ApiModelProperty(value = "消息发送成功率(%)")
    @Schema(description = "消息发送成功率(%)")
    private Double sendSuccessRate;

    /**
     * 每秒消息数(TPS)
     */
    @ApiModelProperty(value = "每秒消息数(TPS)")
    @Schema(description = "每秒消息数(TPS)")
    private Double messagesPerSecond;

    /**
     * 峰值TPS
     */
    @ApiModelProperty(value = "峰值TPS")
    @Schema(description = "峰值TPS")
    private Double peakTPS;

    /**
     * 在线会话数
     */
    @ApiModelProperty(value = "在线会话数")
    @Schema(description = "在线会话数")
    private Integer onlineSessionCount;

    /**
     * 活跃会话数
     */
    @ApiModelProperty(value = "活跃会话数")
    @Schema(description = "活跃会话数")
    private Integer activeSessionCount;

    /**
     * 总连接数
     */
    @ApiModelProperty(value = "总连接数")
    @Schema(description = "总连接数")
    private Long totalConnections;

    /**
     * 连接失败数
     */
    @ApiModelProperty(value = "连接失败数")
    @Schema(description = "连接失败数")
    private Long failedConnections;

    /**
     * 平均连接持续时间(毫秒)
     */
    @ApiModelProperty(value = "平均连接持续时间(毫秒)")
    @Schema(description = "平均连接持续时间(毫秒)")
    private Long avgConnectionDuration;

    /**
     * 内存使用量(字节)
     */
    @ApiModelProperty(value = "内存使用量(字节)")
    @Schema(description = "内存使用量(字节)")
    private Long memoryUsage;

    /**
     * CPU使用率(%)
     */
    @ApiModelProperty(value = "CPU使用率(%)")
    @Schema(description = "CPU使用率(%)")
    private Double cpuUsage;

    /**
     * 网络带宽使用(字节/秒)
     */
    @ApiModelProperty(value = "网络带宽使用(字节/秒)")
    @Schema(description = "网络带宽使用(字节/秒)")
    private Long networkBandwidthUsage;

    /**
     * 计算发送成功率
     */
    public void calculateSendSuccessRate() {
        if (totalSentMessages != null && totalSentMessages > 0) {
            long successMessages = totalSentMessages - (failedMessages != null ? failedMessages : 0);
            this.sendSuccessRate = successMessages * 100.0 / totalSentMessages;
        } else {
            this.sendSuccessRate = 0.0;
        }
    }

    /**
     * 获取平均消息大小(KB)
     */
    public Double getAvgMessageSizeKB() {
        return avgMessageSize != null ? avgMessageSize / 1024.0 : 0.0;
    }

    /**
     * 获取最大消息大小(KB)
     */
    public Double getMaxMessageSizeKB() {
        return maxMessageSize != null ? maxMessageSize / 1024.0 : 0.0;
    }

    /**
     * 获取内存使用量(MB)
     */
    public Double getMemoryUsageMB() {
        return memoryUsage != null ? memoryUsage / (1024.0 * 1024.0) : 0.0;
    }

    /**
     * 获取网络带宽使用(MB/s)
     */
    public Double getNetworkBandwidthUsageMBps() {
        return networkBandwidthUsage != null ? networkBandwidthUsage / (1024.0 * 1024.0) : 0.0;
    }

    /**
     * 获取平均连接持续时间(分钟)
     */
    public Double getAvgConnectionDurationMinutes() {
        return avgConnectionDuration != null ? avgConnectionDuration / (1000.0 * 60.0) : 0.0;
    }

    /**
     * 是否性能良好
     */
    public boolean isPerformanceGood() {
        return sendSuccessRate != null && sendSuccessRate >= 95.0 &&
               avgSendLatency != null && avgSendLatency <= 100 &&
               cpuUsage != null && cpuUsage <= 80.0;
    }

    /**
     * 是否需要优化
     */
    public boolean needsOptimization() {
        return !isPerformanceGood() ||
               (failedMessages != null && totalSentMessages != null &&
                failedMessages * 100.0 / totalSentMessages > 5.0);
    }
}

