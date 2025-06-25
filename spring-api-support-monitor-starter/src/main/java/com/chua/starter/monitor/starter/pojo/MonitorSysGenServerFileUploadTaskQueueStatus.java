package com.chua.starter.monitor.starter.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 服务器文件上传任务队列状态实体类
 *
 * @author CH
 * @since 2024/12/20
 */
@ApiModel(description = "服务器文件上传任务队列状态")
@Schema(description = "服务器文件上传任务队列状态")
@Data
public class MonitorSysGenServerFileUploadTaskQueueStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 队列总长度
     */
    @ApiModelProperty(value = "队列总长度")
    @Schema(description = "队列总长度")
    private Long totalQueueLength;

    /**
     * 待处理任务数
     */
    @ApiModelProperty(value = "待处理任务数")
    @Schema(description = "待处理任务数")
    private Long pendingTasks;

    /**
     * 处理中任务数
     */
    @ApiModelProperty(value = "处理中任务数")
    @Schema(description = "处理中任务数")
    private Long processingTasks;

    /**
     * 已完成任务数
     */
    @ApiModelProperty(value = "已完成任务数")
    @Schema(description = "已完成任务数")
    private Long completedTasks;

    /**
     * 失败任务数
     */
    @ApiModelProperty(value = "失败任务数")
    @Schema(description = "失败任务数")
    private Long failedTasks;

    /**
     * 队列处理是否启用
     */
    @ApiModelProperty(value = "队列处理是否启用")
    @Schema(description = "队列处理是否启用")
    private Boolean queueProcessingEnabled;

    /**
     * 当前处理器数量
     */
    @ApiModelProperty(value = "当前处理器数量")
    @Schema(description = "当前处理器数量")
    private Integer activeProcessors;

    /**
     * 最大处理器数量
     */
    @ApiModelProperty(value = "最大处理器数量")
    @Schema(description = "最大处理器数量")
    private Integer maxProcessors;

    /**
     * 平均等待时间(毫秒)
     */
    @ApiModelProperty(value = "平均等待时间(毫秒)")
    @Schema(description = "平均等待时间(毫秒)")
    private Long avgWaitTime;

    /**
     * 平均处理时间(毫秒)
     */
    @ApiModelProperty(value = "平均处理时间(毫秒)")
    @Schema(description = "平均处理时间(毫秒)")
    private Long avgProcessTime;

    /**
     * 队列吞吐量(任务/小时)
     */
    @ApiModelProperty(value = "队列吞吐量(任务/小时)")
    @Schema(description = "队列吞吐量(任务/小时)")
    private Double throughputPerHour;

    /**
     * 成功率(%)
     */
    @ApiModelProperty(value = "成功率(%)")
    @Schema(description = "成功率(%)")
    private Double successRate;

    /**
     * 重试率(%)
     */
    @ApiModelProperty(value = "重试率(%)")
    @Schema(description = "重试率(%)")
    private Double retryRate;

    /**
     * 超时率(%)
     */
    @ApiModelProperty(value = "超时率(%)")
    @Schema(description = "超时率(%)")
    private Double timeoutRate;

    /**
     * 队列健康状态
     */
    @ApiModelProperty(value = "队列健康状态")
    @Schema(description = "队列健康状态(HEALTHY:健康 WARNING:警告 CRITICAL:严重)")
    private String healthStatus;

    /**
     * 队列负载水平
     */
    @ApiModelProperty(value = "队列负载水平")
    @Schema(description = "队列负载水平(LOW:低 MEDIUM:中 HIGH:高 OVERLOAD:过载)")
    private String loadLevel;

    /**
     * 预计完成时间
     */
    @ApiModelProperty(value = "预计完成时间")
    @Schema(description = "预计完成时间")
    private LocalDateTime estimatedCompletionTime;

    /**
     * 最后更新时间
     */
    @ApiModelProperty(value = "最后更新时间")
    @Schema(description = "最后更新时间")
    private LocalDateTime lastUpdateTime;

    /**
     * 瓶颈分析
     */
    @ApiModelProperty(value = "瓶颈分析")
    @Schema(description = "瓶颈分析")
    private String bottleneckAnalysis;

    /**
     * 优化建议
     */
    @ApiModelProperty(value = "优化建议")
    @Schema(description = "优化建议")
    private String optimizationSuggestions;

    /**
     * 健康状态枚举
     */
    public enum HealthStatus {
        HEALTHY("HEALTHY", "健康"),
        WARNING("WARNING", "警告"),
        CRITICAL("CRITICAL", "严重");

        private final String code;
        private final String desc;

        HealthStatus(String code, String desc) {
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
     * 负载水平枚举
     */
    public enum LoadLevel {
        LOW("LOW", "低"),
        MEDIUM("MEDIUM", "中"),
        HIGH("HIGH", "高"),
        OVERLOAD("OVERLOAD", "过载");

        private final String code;
        private final String desc;

        LoadLevel(String code, String desc) {
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
     * 计算总队列长度
     */
    public void calculateTotalQueueLength() {
        this.totalQueueLength = (pendingTasks != null ? pendingTasks : 0) +
                               (processingTasks != null ? processingTasks : 0) +
                               (completedTasks != null ? completedTasks : 0) +
                               (failedTasks != null ? failedTasks : 0);
    }

    /**
     * 计算成功率
     */
    public void calculateSuccessRate() {
        long total = (completedTasks != null ? completedTasks : 0) +
                    (failedTasks != null ? failedTasks : 0);
        if (total > 0) {
            this.successRate = (completedTasks != null ? completedTasks : 0) * 100.0 / total;
        } else {
            this.successRate = 0.0;
        }
    }

    /**
     * 获取平均等待时间(秒)
     */
    public Double getAvgWaitTimeSeconds() {
        return avgWaitTime != null ? avgWaitTime / 1000.0 : 0.0;
    }

    /**
     * 获取平均处理时间(秒)
     */
    public Double getAvgProcessTimeSeconds() {
        return avgProcessTime != null ? avgProcessTime / 1000.0 : 0.0;
    }

    /**
     * 是否队列健康
     */
    public boolean isQueueHealthy() {
        return HealthStatus.HEALTHY.getCode().equals(healthStatus);
    }

    /**
     * 是否队列过载
     */
    public boolean isQueueOverloaded() {
        return LoadLevel.OVERLOAD.getCode().equals(loadLevel);
    }

    /**
     * 是否需要优化
     */
    public boolean needsOptimization() {
        return !isQueueHealthy() || isQueueOverloaded() ||
               (successRate != null && successRate < 90.0);
    }

    /**
     * 获取处理器使用率(%)
     */
    public Double getProcessorUtilization() {
        if (maxProcessors != null && maxProcessors > 0 && activeProcessors != null) {
            return activeProcessors * 100.0 / maxProcessors;
        }
        return 0.0;
    }
}

