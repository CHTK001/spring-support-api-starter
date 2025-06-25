package com.chua.starter.monitor.starter.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 服务器文件上传清理结果实体类
 *
 * @author CH
 * @since 2024/12/20
 */
@ApiModel(description = "服务器文件上传清理")
@Schema(description = "服务器文件上传清理")
@Data
public class MonitorSysGenServerFileUploadCleanupResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 清理类型
     */
    @ApiModelProperty(value = "清理类型")
    @Schema(description = "清理类型(COMPLETED:已完成任务 TIMEOUT: 超时任务 FAILED: 失败任务 ALL:全部)")
    private String cleanupType;

    /**
     * 清理数量
     */
    @ApiModelProperty(value = "清理数量")
    @Schema(description = "清理数量")
    private Long cleanupCount;

    /**
     * 保留天数
     */
    @ApiModelProperty(value = "保留天数")
    @Schema(description = "保留天数")
    private Integer retentionDays;

    /**
     * 超时分钟
     */
    @ApiModelProperty(value = "超时分钟")
    @Schema(description = "超时分钟")
    private Integer timeoutMinutes;

    /**
     * 截止时间
     */
    @ApiModelProperty(value = "截止时间")
    @Schema(description = "截止时间")
    private LocalDateTime cutoffTime;

    /**
     * 清理开始时间
     */
    @ApiModelProperty(value = "清理开始时间")
    @Schema(description = "清理开始时间")
    private LocalDateTime startTime;

    /**
     * 清理结束时间
     */
    @ApiModelProperty(value = "清理结束时间")
    @Schema(description = "清理结束时间")
    private LocalDateTime endTime;

    /**
     * 清理耗时(毫秒)
     */
    @ApiModelProperty(value = "清理耗时(毫秒)")
    @Schema(description = "清理耗时(毫秒)")
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
     * 清理详情
     */
    @ApiModelProperty(value = "清理详情")
    @Schema(description = "清理详情")
    private String details;

    /**
     * 清理前总数
     */
    @ApiModelProperty(value = "清理前总数")
    @Schema(description = "清理前总数")
    private Long beforeCount;

    /**
     * 清理后总数
     */
    @ApiModelProperty(value = "清理后总数")
    @Schema(description = "清理后总数")
    private Long afterCount;

    /**
     * 释放的存储空间(字节)
     */
    @ApiModelProperty(value = "释放的存储空间(字节)")
    @Schema(description = "释放的存储空间(字节)")
    private Long freedSpace;

    /**
     * 清理类型枚举
     */
    public enum CleanupType {
        COMPLETED("COMPLETED", "已完成任务"),
        TIMEOUT("TIMEOUT", "超时任务"),
        FAILED("FAILED", "失败任务"),
        ALL("ALL", "全部");

        private final String code;
        private final String desc;

        CleanupType(String code, String desc) {
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
     * 计算清理耗时
     */
    public void calculateDuration() {
        if (startTime != null && endTime != null) {
            this.durationMs = java.time.Duration.between(startTime, endTime).toMillis();
        }
    }

    /**
     * 获取释放的存储空间(MB)
     */
    public Double getFreedSpaceMB() {
        return freedSpace != null ? freedSpace / (1024.0 * 1024.0) : 0.0;
    }

    /**
     * 获取释放的存储空间(GB)
     */
    public Double getFreedSpaceGB() {
        return freedSpace != null ? freedSpace / (1024.0 * 1024.0 * 1024.0) : 0.0;
    }

    /**
     * 获取清理效率(条/秒)
     */
    public Double getCleanupRate() {
        if (durationMs != null && durationMs > 0 && cleanupCount != null) {
            return cleanupCount * 1000.0 / durationMs;
        }
        return 0.0;
    }

    /**
     * 是否有数据被清理
     */
    public boolean hasDataCleaned() {
        return cleanupCount != null && cleanupCount > 0;
    }
}

