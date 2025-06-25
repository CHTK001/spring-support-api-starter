package com.chua.starter.monitor.starter.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 服务器文件上传队列统计信息实体类
 *
 * @author CH
 * @since 2024/12/20
 */
@ApiModel(description = "服务器文件上传队列统计信息")
@Schema(description = "服务器文件上传队列统计信息")
@Data
public class MonitorSysGenServerFileUploadQueueStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 待处理任务数量
     */
    @ApiModelProperty(value = "待处理任务数量")
    @Schema(description = "待处理任务数量")
    private Long pendingCount;

    /**
     * 处理中任务数量
     */
    @ApiModelProperty(value = "处理中任务数量")
    @Schema(description = "处理中任务数量")
    private Long processingCount;

    /**
     * 已完成任务数量
     */
    @ApiModelProperty(value = "已完成任务数量")
    @Schema(description = "已完成任务数量")
    private Long completedCount;

    /**
     * 失败任务数量
     */
    @ApiModelProperty(value = "失败任务数量")
    @Schema(description = "失败任务数量")
    private Long failedCount;

    /**
     * 总任务数量
     */
    @ApiModelProperty(value = "总任务数量")
    @Schema(description = "总任务数量")
    private Long totalCount;

    /**
     * 队列处理是否启用
     */
    @ApiModelProperty(value = "队列处理是否启用")
    @Schema(description = "队列处理是否启用")
    private Boolean queueProcessingEnabled;

    /**
     * 平均等待时间(毫秒)
     */
    @ApiModelProperty(value = "平均等待时间(毫秒)")
    @Schema(description = "平均等待时间(毫秒)")
    private Double avgWaitDurationMs;

    /**
     * 平均处理时间(毫秒)
     */
    @ApiModelProperty(value = "平均处理时间(毫秒)")
    @Schema(description = "平均处理时间(毫秒)")
    private Double avgProcessDurationMs;

    /**
     * 成功率(百分比)
     */
    @ApiModelProperty(value = "成功率(百分比)")
    @Schema(description = "成功率(百分比)")
    private Double successRate;

    /**
     * 失败率(百分比)
     */
    @ApiModelProperty(value = "失败率(百分比)")
    @Schema(description = "失败率(百分比)")
    private Double failureRate;

    /**
     * 队列吞吐量(任务/小时)
     */
    @ApiModelProperty(value = "队列吞吐量(任务/小时)")
    @Schema(description = "队列吞吐量(任务/小时)")
    private Double throughputPerHour;

    /**
     * 计算成功率
     */
    public void calculateSuccessRate() {
        if (totalCount != null && totalCount > 0) {
            this.successRate = (completedCount != null ? completedCount : 0) * 100.0 / totalCount;
            this.failureRate = (failedCount != null ? failedCount : 0) * 100.0 / totalCount;
        } else {
            this.successRate = 0.0;
            this.failureRate = 0.0;
        }
    }

    /**
     * 计算总数
     */
    public void calculateTotalCount() {
        this.totalCount = (pendingCount != null ? pendingCount : 0) +
                         (processingCount != null ? processingCount : 0) +
                         (completedCount != null ? completedCount : 0) +
                         (failedCount != null ? failedCount : 0);
    }
}

