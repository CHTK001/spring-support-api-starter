package com.chua.starter.monitor.starter.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 服务器文件上传能力评估实体类
 *
 * @author CH
 * @since 2024/12/20
 */
@ApiModel(description = "服务器文件上传能力评估")
@Schema(description = "服务器文件上传能力评估")
@Data
public class MonitorSysGenServerFileUploadCapacity implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 服务器类型
     */
    @ApiModelProperty(value = "服务器类型")
    @Schema(description = "服务器类型")
    private String serverType;

    /**
     * 评估时间
     */
    @ApiModelProperty(value = "评估时间")
    @Schema(description = "评估时间")
    private LocalDateTime assessmentTime;

    /**
     * 最大并发上传数
     */
    @ApiModelProperty(value = "最大并发上传数")
    @Schema(description = "最大并发上传数")
    private Integer maxConcurrentUploads;

    /**
     * 推荐并发上传数
     */
    @ApiModelProperty(value = "推荐并发上传数")
    @Schema(description = "推荐并发上传数")
    private Integer recommendedConcurrentUploads;

    /**
     * 最大文件大小(字节)
     */
    @ApiModelProperty(value = "最大文件大小(字节)")
    @Schema(description = "最大文件大小(字节)")
    private Long maxFileSize;

    /**
     * 平均上传速度(字节/秒)
     */
    @ApiModelProperty(value = "平均上传速度(字节/秒)")
    @Schema(description = "平均上传速度(字节/秒)")
    private Long avgUploadSpeed;

    /**
     * 峰值上传速度(字节/秒)
     */
    @ApiModelProperty(value = "峰值上传速度(字节/秒)")
    @Schema(description = "峰值上传速度(字节/秒)")
    private Long peakUploadSpeed;

    /**
     * 网络延迟(毫秒)
     */
    @ApiModelProperty(value = "网络延迟(毫秒)")
    @Schema(description = "网络延迟(毫秒)")
    private Long networkLatency;

    /**
     * 网络稳定性(%)
     */
    @ApiModelProperty(value = "网络稳定性(%)")
    @Schema(description = "网络稳定性(%)")
    private Double networkStability;

    /**
     * 磁盘可用空间(字节)
     */
    @ApiModelProperty(value = "磁盘可用空间(字节)")
    @Schema(description = "磁盘可用空间(字节)")
    private Long availableDiskSpace;

    /**
     * 磁盘写入速度(字节/秒)
     */
    @ApiModelProperty(value = "磁盘写入速度(字节/秒)")
    @Schema(description = "磁盘写入速度(字节/秒)")
    private Long diskWriteSpeed;

    /**
     * CPU使用率(%)
     */
    @ApiModelProperty(value = "CPU使用率(%)")
    @Schema(description = "CPU使用率(%)")
    private Double cpuUsage;

    /**
     * 内存使用率(%)
     */
    @ApiModelProperty(value = "内存使用率(%)")
    @Schema(description = "内存使用率(%)")
    private Double memoryUsage;

    /**
     * 成功率(%)
     */
    @ApiModelProperty(value = "成功率(%)")
    @Schema(description = "成功率(%)")
    private Double successRate;

    /**
     * 平均重试次数
     */
    @ApiModelProperty(value = "平均重试次数")
    @Schema(description = "平均重试次数")
    private Double avgRetryCount;

    /**
     * 超时率(%)
     */
    @ApiModelProperty(value = "超时率(%)")
    @Schema(description = "超时率(%)")
    private Double timeoutRate;

    /**
     * 综合评分(1-100)
     */
    @ApiModelProperty(value = "综合评分(1-100)")
    @Schema(description = "综合评分(1-100)")
    private Integer overallScore;

    /**
     * 性能等级
     */
    @ApiModelProperty(value = "性能等级")
    @Schema(description = "性能等级(EXCELLENT:优秀 GOOD:良好 FAIR:一般 POOR:较差)")
    private String performanceLevel;

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
     * 是否支持上传
     */
    @ApiModelProperty(value = "是否支持上传")
    @Schema(description = "是否支持上传")
    private Boolean uploadSupported;

    /**
     * 不支持原因
     */
    @ApiModelProperty(value = "不支持原因")
    @Schema(description = "不支持原因")
    private String unsupportedReason;

    /**
     * 历史任务数量
     */
    @ApiModelProperty(value = "历史任务数量")
    @Schema(description = "历史任务数量")
    private Long historicalTaskCount;

    /**
     * 评估数据来源天数
     */
    @ApiModelProperty(value = "评估数据来源天数")
    @Schema(description = "评估数据来源天数")
    private Integer assessmentDays;

    /**
     * 性能等级枚举
     */
    public enum PerformanceLevel {
        EXCELLENT("EXCELLENT", "优秀"),
        GOOD("GOOD", "良好"),
        FAIR("FAIR", "一般"),
        POOR("POOR", "较差");

        private final String code;
        private final String desc;

        PerformanceLevel(String code, String desc) {
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
     * 获取最大文件大小(MB)
     */
    public Double getMaxFileSizeMB() {
        return maxFileSize != null ? maxFileSize / (1024.0 * 1024.0) : 0.0;
    }

    /**
     * 获取平均上传速度(MB/s)
     */
    public Double getAvgUploadSpeedMBps() {
        return avgUploadSpeed != null ? avgUploadSpeed / (1024.0 * 1024.0) : 0.0;
    }

    /**
     * 获取峰值上传速度(MB/s)
     */
    public Double getPeakUploadSpeedMBps() {
        return peakUploadSpeed != null ? peakUploadSpeed / (1024.0 * 1024.0) : 0.0;
    }

    /**
     * 获取磁盘可用空间(GB)
     */
    public Double getAvailableDiskSpaceGB() {
        return availableDiskSpace != null ? availableDiskSpace / (1024.0 * 1024.0 * 1024.0) : 0.0;
    }

    /**
     * 获取磁盘写入速度(MB/s)
     */
    public Double getDiskWriteSpeedMBps() {
        return diskWriteSpeed != null ? diskWriteSpeed / (1024.0 * 1024.0) : 0.0;
    }

    /**
     * 是否性能优秀
     */
    public boolean isPerformanceExcellent() {
        return PerformanceLevel.EXCELLENT.getCode().equals(performanceLevel);
    }

    /**
     * 是否性能良好
     */
    public boolean isPerformanceGood() {
        return PerformanceLevel.GOOD.getCode().equals(performanceLevel) || 
               isPerformanceExcellent();
    }

    /**
     * 是否有足够磁盘空间
     */
    public boolean hasSufficientDiskSpace() {
        return availableDiskSpace != null && availableDiskSpace > 1024L * 1024L * 1024L; // 1GB
    }

    /**
     * 是否网络稳定
     */
    public boolean isNetworkStable() {
        return networkStability != null && networkStability >= 95.0;
    }

    /**
     * 计算性能等级
     */
    public void calculatePerformanceLevel() {
        if (overallScore != null) {
            if (overallScore >= 90) {
                this.performanceLevel = PerformanceLevel.EXCELLENT.getCode();
            } else if (overallScore >= 75) {
                this.performanceLevel = PerformanceLevel.GOOD.getCode();
            } else if (overallScore >= 60) {
                this.performanceLevel = PerformanceLevel.FAIR.getCode();
            } else {
                this.performanceLevel = PerformanceLevel.POOR.getCode();
            }
        }
    }
}
