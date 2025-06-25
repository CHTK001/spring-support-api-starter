package com.chua.starter.monitor.starter.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 服务器文件上传队列优化结果实体类
 *
 * @author CH
 * @since 2024/12/20
 */
@ApiModel(description = "服务器文件上传队列优化结果")
@Schema(description = "服务器文件上传队列优化结果")
@Data
public class MonitorSysGenServerFileUploadQueueOptimizationResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 优化类型
     */
    @ApiModelProperty(value = "优化类型")
    @Schema(description = "优化类型(PRIORITY:优先级调整 CLEANUP:清理 REBALANCE:重新平衡 CONFIG:配置优化)")
    private String optimizationType;

    /**
     * 优化开始时间
     */
    @ApiModelProperty(value = "优化开始时间")
    @Schema(description = "优化开始时间")
    private LocalDateTime startTime;

    /**
     * 优化结束时间
     */
    @ApiModelProperty(value = "优化结束时间")
    @Schema(description = "优化结束时间")
    private LocalDateTime endTime;

    /**
     * 优化耗时(毫秒)
     */
    @ApiModelProperty(value = "优化耗时(毫秒)")
    @Schema(description = "优化耗时(毫秒)")
    private Long optimizationDuration;

    /**
     * 是否成功
     */
    @ApiModelProperty(value = "是否成功")
    @Schema(description = "是否成功")
    private Boolean success;

    /**
     * 优化前队列长度
     */
    @ApiModelProperty(value = "优化前队列长度")
    @Schema(description = "优化前队列长度")
    private Long beforeQueueLength;

    /**
     * 优化后队列长度
     */
    @ApiModelProperty(value = "优化后队列长度")
    @Schema(description = "优化后队列长度")
    private Long afterQueueLength;

    /**
     * 优化前平均等待时间(毫秒)
     */
    @ApiModelProperty(value = "优化前平均等待时间(毫秒)")
    @Schema(description = "优化前平均等待时间(毫秒)")
    private Long beforeAvgWaitTime;

    /**
     * 优化后平均等待时间(毫秒)
     */
    @ApiModelProperty(value = "优化后平均等待时间(毫秒)")
    @Schema(description = "优化后平均等待时间(毫秒)")
    private Long afterAvgWaitTime;

    /**
     * 优化前吞吐量(任务/小时)
     */
    @ApiModelProperty(value = "优化前吞吐量(任务/小时)")
    @Schema(description = "优化前吞吐量(任务/小时)")
    private Double beforeThroughput;

    /**
     * 优化后吞吐量(任务/小时)
     */
    @ApiModelProperty(value = "优化后吞吐量(任务/小时)")
    @Schema(description = "优化后吞吐量(任务/小时)")
    private Double afterThroughput;

    /**
     * 清理的任务数量
     */
    @ApiModelProperty(value = "清理的任务数量")
    @Schema(description = "清理的任务数量")
    private Long cleanedTaskCount;

    /**
     * 调整优先级的任务数量
     */
    @ApiModelProperty(value = "调整优先级的任务数量")
    @Schema(description = "调整优先级的任务数量")
    private Long priorityAdjustedCount;

    /**
     * 重新分配的任务数量
     */
    @ApiModelProperty(value = "重新分配的任务数量")
    @Schema(description = "重新分配的任务数量")
    private Long rebalancedTaskCount;

    /**
     * 性能提升百分比
     */
    @ApiModelProperty(value = "性能提升百分比")
    @Schema(description = "性能提升百分比")
    private Double performanceImprovement;

    /**
     * 等待时间减少百分比
     */
    @ApiModelProperty(value = "等待时间减少百分比")
    @Schema(description = "等待时间减少百分比")
    private Double waitTimeReduction;

    /**
     * 吞吐量提升百分比
     */
    @ApiModelProperty(value = "吞吐量提升百分比")
    @Schema(description = "吞吐量提升百分比")
    private Double throughputImprovement;

    /**
     * 执行的优化操作列表
     */
    @ApiModelProperty(value = "执行的优化操作列表")
    @Schema(description = "执行的优化操作列表")
    private List<String> optimizationActions;

    /**
     * 优化详情
     */
    @ApiModelProperty(value = "优化详情")
    @Schema(description = "优化详情")
    private String details;

    /**
     * 错误信息
     */
    @ApiModelProperty(value = "错误信息")
    @Schema(description = "错误信息")
    private String errorMessage;

    /**
     * 后续建议
     */
    @ApiModelProperty(value = "后续建议")
    @Schema(description = "后续建议")
    private String followUpSuggestions;

    /**
     * 下次优化建议时间
     */
    @ApiModelProperty(value = "下次优化建议时间")
    @Schema(description = "下次优化建议时间")
    private LocalDateTime nextOptimizationTime;

    /**
     * 优化类型枚举
     */
    public enum OptimizationType {
        PRIORITY("PRIORITY", "优先级调整"),
        CLEANUP("CLEANUP", "清理"),
        REBALANCE("REBALANCE", "重新平衡"),
        CONFIG("CONFIG", "配置优化");

        private final String code;
        private final String desc;

        OptimizationType(String code, String desc) {
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
     * 计算优化耗时
     */
    public void calculateOptimizationDuration() {
        if (startTime != null && endTime != null) {
            this.optimizationDuration = java.time.Duration.between(startTime, endTime).toMillis();
        }
    }

    /**
     * 计算性能提升
     */
    public void calculatePerformanceImprovement() {
        if (beforeThroughput != null && afterThroughput != null && beforeThroughput > 0) {
            this.throughputImprovement = ((afterThroughput - beforeThroughput) / beforeThroughput) * 100.0;
        }

        if (beforeAvgWaitTime != null && afterAvgWaitTime != null && beforeAvgWaitTime > 0) {
            this.waitTimeReduction = ((beforeAvgWaitTime - afterAvgWaitTime) / (double) beforeAvgWaitTime) * 100.0;
        }

        // 综合性能提升 = (吞吐量提升 + 等待时间减少) / 2
        if (throughputImprovement != null && waitTimeReduction != null) {
            this.performanceImprovement = (throughputImprovement + waitTimeReduction) / 2.0;
        } else if (throughputImprovement != null) {
            this.performanceImprovement = throughputImprovement;
        } else if (waitTimeReduction != null) {
            this.performanceImprovement = waitTimeReduction;
        }
    }

    /**
     * 获取优化耗时(秒)
     */
    public Double getOptimizationDurationSeconds() {
        return optimizationDuration != null ? optimizationDuration / 1000.0 : 0.0;
    }

    /**
     * 是否有显著改善
     */
    public boolean hasSignificantImprovement() {
        return performanceImprovement != null && performanceImprovement > 10.0;
    }

    /**
     * 是否需要进一步优化
     */
    public boolean needsFurtherOptimization() {
        return performanceImprovement == null || performanceImprovement < 5.0;
    }

    /**
     * 获取队列长度减少数量
     */
    public Long getQueueLengthReduction() {
        if (beforeQueueLength != null && afterQueueLength != null) {
            return beforeQueueLength - afterQueueLength;
        }
        return 0L;
    }

    /**
     * 获取队列长度减少百分比
     */
    public Double getQueueLengthReductionPercentage() {
        if (beforeQueueLength != null && afterQueueLength != null && beforeQueueLength > 0) {
            return ((beforeQueueLength - afterQueueLength) / (double) beforeQueueLength) * 100.0;
        }
        return 0.0;
    }
}

