package com.chua.starter.monitor.starter.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 服务器文件上传批量操作结果实体类
 *
 * @author CH
 * @since 2024/12/20
 */
@ApiModel(description = "服务器文件上传批量操作结果")
@Schema(description = "服务器文件上传批量操作结果")
@Data
public class MonitorSysGenServerFileUploadBatchResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 操作类型
     */
    @ApiModelProperty(value = "操作类型")
    @Schema(description = "操作类型(CANCEL:取消 RETRY:重试 DELETE:删除)")
    private String operationType;

    /**
     * 总数量
     */
    @ApiModelProperty(value = "总数量")
    @Schema(description = "总数量")
    private Integer totalCount;

    /**
     * 成功数量
     */
    @ApiModelProperty(value = "成功数量")
    @Schema(description = "成功数量")
    private Integer successCount;

    /**
     * 失败数量
     */
    @ApiModelProperty(value = "失败数量")
    @Schema(description = "失败数量")
    private Integer failureCount;

    /**
     * 跳过数量
     */
    @ApiModelProperty(value = "跳过数量")
    @Schema(description = "跳过数量")
    private Integer skippedCount;

    /**
     * 成功的任务ID列表
     */
    @ApiModelProperty(value = "成功的任务ID列表")
    @Schema(description = "成功的任务ID列表")
    private List<Long> successTaskIds;

    /**
     * 失败的任务ID列表
     */
    @ApiModelProperty(value = "失败的任务ID列表")
    @Schema(description = "失败的任务ID列表")
    private List<Long> failureTaskIds;

    /**
     * 跳过的任务ID列表
     */
    @ApiModelProperty(value = "跳过的任务ID列表")
    @Schema(description = "跳过的任务ID列表")
    private List<Long> skippedTaskIds;

    /**
     * 失败原因列表
     */
    @ApiModelProperty(value = "失败原因列表")
    @Schema(description = "失败原因列表")
    private List<String> failureReasons;

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
     * 执行耗时(毫秒)
     */
    @ApiModelProperty(value = "执行耗时(毫秒)")
    @Schema(description = "执行耗时(毫秒)")
    private Long executionDuration;

    /**
     * 是否全部成功
     */
    @ApiModelProperty(value = "是否全部成功")
    @Schema(description = "是否全部成功")
    private Boolean allSuccess;

    /**
     * 成功率(%)
     */
    @ApiModelProperty(value = "成功率(%)")
    @Schema(description = "成功率(%)")
    private Double successRate;

    /**
     * 操作详情
     */
    @ApiModelProperty(value = "操作详情")
    @Schema(description = "操作详情")
    private String details;

    /**
     * 错误信息
     */
    @ApiModelProperty(value = "错误信息")
    @Schema(description = "错误信息")
    private String errorMessage;

    /**
     * 操作类型枚举
     */
    public enum OperationType {
        CANCEL("CANCEL", "取消"),
        RETRY("RETRY", "重试"),
        DELETE("DELETE", "删除");

        private final String code;
        private final String desc;

        OperationType(String code, String desc) {
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
     * 计算执行耗时
     */
    public void calculateExecutionDuration() {
        if (startTime != null && endTime != null) {
            this.executionDuration = java.time.Duration.between(startTime, endTime).toMillis();
        }
    }

    /**
     * 计算成功率
     */
    public void calculateSuccessRate() {
        if (totalCount != null && totalCount > 0) {
            this.successRate = (successCount != null ? successCount : 0) * 100.0 / totalCount;
        } else {
            this.successRate = 0.0;
        }
    }

    /**
     * 计算是否全部成功
     */
    public void calculateAllSuccess() {
        this.allSuccess = totalCount != null && successCount != null && 
                         totalCount.equals(successCount) && 
                         (failureCount == null || failureCount == 0);
    }

    /**
     * 获取执行耗时(秒)
     */
    public Double getExecutionDurationSeconds() {
        return executionDuration != null ? executionDuration / 1000.0 : 0.0;
    }

    /**
     * 是否有失败
     */
    public boolean hasFailures() {
        return failureCount != null && failureCount > 0;
    }

    /**
     * 是否有跳过
     */
    public boolean hasSkipped() {
        return skippedCount != null && skippedCount > 0;
    }

    /**
     * 获取处理速度(任务/秒)
     */
    public Double getProcessingRate() {
        if (executionDuration != null && executionDuration > 0 && totalCount != null) {
            return totalCount * 1000.0 / executionDuration;
        }
        return 0.0;
    }
}
