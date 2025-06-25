package com.chua.starter.monitor.starter.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 服务器文件上传重置结果实体类
 *
 * @author CH
 * @since 2024/12/20
 */
@ApiModel(description = "服务器文件上传重置结果")
@Schema(description = "服务器文件上传重置结果")
@Data
public class MonitorSysGenServerFileUploadResetResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 重置类型
     */
    @ApiModelProperty(value = "重置类型")
    @Schema(description = "重置类型(TIMEOUT:超时任务 FAILED:失败任务 PROCESSING:处理中任务)")
    private String resetType;

    /**
     * 重置数量
     */
    @ApiModelProperty(value = "重置数量")
    @Schema(description = "重置数量")
    private Long resetCount;

    /**
     * 超时分钟数
     */
    @ApiModelProperty(value = "超时分钟数")
    @Schema(description = "超时分钟数")
    private Integer timeoutMinutes;

    /**
     * 超时时间点
     */
    @ApiModelProperty(value = "超时时间点")
    @Schema(description = "超时时间点")
    private LocalDateTime timeoutTime;

    /**
     * 重置开始时间
     */
    @ApiModelProperty(value = "重置开始时间")
    @Schema(description = "重置开始时间")
    private LocalDateTime startTime;

    /**
     * 重置结束时间
     */
    @ApiModelProperty(value = "重置结束时间")
    @Schema(description = "重置结束时间")
    private LocalDateTime endTime;

    /**
     * 重置耗时(毫秒)
     */
    @ApiModelProperty(value = "重置耗时(毫秒)")
    @Schema(description = "重置耗时(毫秒)")
    private Long durationMs;

    /**
     * 是否成功
     */
    @ApiModelProperty(value = "是否成功")
    @Schema(description = "是否成功")
    private Boolean success;

    /**
     * 错误信息
     */
    @ApiModelProperty(value = "错误信息")
    @Schema(description = "错误信息")
    private String errorMessage;

    /**
     * 重置详情
     */
    @ApiModelProperty(value = "重置详情")
    @Schema(description = "重置详情")
    private String details;

    /**
     * 重置前状态统计
     */
    @ApiModelProperty(value = "重置前状态统计")
    @Schema(description = "重置前状态统计")
    private String beforeStatus;

    /**
     * 重置后状态统计
     */
    @ApiModelProperty(value = "重置后状态统计")
    @Schema(description = "重置后状态统计")
    private String afterStatus;

    /**
     * 重置类型枚举
     */
    public enum ResetType {
        TIMEOUT("TIMEOUT", "超时任务"),
        FAILED("FAILED", "失败任务"),
        PROCESSING("PROCESSING", "处理中任务");

        private final String code;
        private final String desc;

        ResetType(String code, String desc) {
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
     * 计算重置耗时
     */
    public void calculateDuration() {
        if (startTime != null && endTime != null) {
            this.durationMs = java.time.Duration.between(startTime, endTime).toMillis();
        }
    }

    /**
     * 获取重置效率(条/秒)
     */
    public Double getResetRate() {
        if (durationMs != null && durationMs > 0 && resetCount != null) {
            return resetCount * 1000.0 / durationMs;
        }
        return 0.0;
    }

    /**
     * 是否有数据被重置
     */
    public boolean hasDataReset() {
        return resetCount != null && resetCount > 0;
    }
}

