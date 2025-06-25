package com.chua.starter.monitor.starter.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 服务器文件上传错误信息实体类
 *
 * @author CH
 * @since 2024/12/20
 */
@ApiModel(description = "服务器文件上传错误信息")
@Schema(description = "服务器文件上传错误信息")
@Data
public class MonitorSysGenServerFileUploadErrorInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    @ApiModelProperty(value = "任务ID")
    @Schema(description = "任务ID")
    private Long taskId;

    /**
     * 记录ID
     */
    @ApiModelProperty(value = "记录ID")
    @Schema(description = "记录ID")
    private Long recordId;

    /**
     * 服务器ID
     */
    @ApiModelProperty(value = "服务器ID")
    @Schema(description = "服务器ID")
    private Integer serverId;

    /**
     * 错误类型
     */
    @ApiModelProperty(value = "错误类型")
    @Schema(description = "错误类型(NETWORK:网络错误 AUTH:认证错误 PERMISSION:权限错误 DISK_SPACE:磁盘空间不足 FILE_NOT_FOUND:文件未找到 TIMEOUT:超时 OTHER:其他)")
    private String errorType;

    /**
     * 错误代码
     */
    @ApiModelProperty(value = "错误代码")
    @Schema(description = "错误代码")
    private String errorCode;

    /**
     * 错误消息
     */
    @ApiModelProperty(value = "错误消息")
    @Schema(description = "错误消息")
    private String errorMessage;

    /**
     * 详细错误信息
     */
    @ApiModelProperty(value = "详细错误信息")
    @Schema(description = "详细错误信息")
    private String errorDetails;

    /**
     * 错误堆栈信息
     */
    @ApiModelProperty(value = "错误堆栈信息")
    @Schema(description = "错误堆栈信息")
    private String stackTrace;

    /**
     * 发生时间
     */
    @ApiModelProperty(value = "发生时间")
    @Schema(description = "发生时间")
    private LocalDateTime occurTime;

    /**
     * 重试次数
     */
    @ApiModelProperty(value = "重试次数")
    @Schema(description = "重试次数")
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    @ApiModelProperty(value = "最大重试次数")
    @Schema(description = "最大重试次数")
    private Integer maxRetries;

    /**
     * 是否可重试
     */
    @ApiModelProperty(value = "是否可重试")
    @Schema(description = "是否可重试")
    private Boolean retryable;

    /**
     * 是否已解决
     */
    @ApiModelProperty(value = "是否已解决")
    @Schema(description = "是否已解决")
    private Boolean resolved;

    /**
     * 解决方案
     */
    @ApiModelProperty(value = "解决方案")
    @Schema(description = "解决方案")
    private String solution;

    /**
     * 解决时间
     */
    @ApiModelProperty(value = "解决时间")
    @Schema(description = "解决时间")
    private LocalDateTime resolveTime;

    /**
     * 相关文件路径
     */
    @ApiModelProperty(value = "相关文件路径")
    @Schema(description = "相关文件路径")
    private String filePath;

    /**
     * 文件大小
     */
    @ApiModelProperty(value = "文件大小")
    @Schema(description = "文件大小")
    private Long fileSize;

    /**
     * 目标路径
     */
    @ApiModelProperty(value = "目标路径")
    @Schema(description = "目标路径")
    private String targetPath;

    /**
     * 网络信息
     */
    @ApiModelProperty(value = "网络信息")
    @Schema(description = "网络信息")
    private String networkInfo;

    /**
     * 系统信息
     */
    @ApiModelProperty(value = "系统信息")
    @Schema(description = "系统信息")
    private String systemInfo;

    /**
     * 错误严重级别
     */
    @ApiModelProperty(value = "错误严重级别")
    @Schema(description = "错误严重级别(LOW:低 MEDIUM:中 HIGH:高 CRITICAL:严重)")
    private String severity;

    /**
     * 影响范围
     */
    @ApiModelProperty(value = "影响范围")
    @Schema(description = "影响范围(SINGLE:单个任务 SERVER:整个服务器 SYSTEM:系统级别)")
    private String impact;

    /**
     * 建议操作
     */
    @ApiModelProperty(value = "建议操作")
    @Schema(description = "建议操作")
    private List<String> suggestedActions;

    /**
     * 错误类型枚举
     */
    public enum ErrorType {
        NETWORK("NETWORK", "网络错误"),
        AUTH("AUTH", "认证错误"),
        PERMISSION("PERMISSION", "权限错误"),
        DISK_SPACE("DISK_SPACE", "磁盘空间不足"),
        FILE_NOT_FOUND("FILE_NOT_FOUND", "文件未找到"),
        TIMEOUT("TIMEOUT", "超时"),
        OTHER("OTHER", "其他");

        private final String code;
        private final String desc;

        ErrorType(String code, String desc) {
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
     * 严重级别枚举
     */
    public enum Severity {
        LOW("LOW", "低"),
        MEDIUM("MEDIUM", "中"),
        HIGH("HIGH", "高"),
        CRITICAL("CRITICAL", "严重");

        private final String code;
        private final String desc;

        Severity(String code, String desc) {
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
     * 影响范围枚举
     */
    public enum Impact {
        SINGLE("SINGLE", "单个任务"),
        SERVER("SERVER", "整个服务器"),
        SYSTEM("SYSTEM", "系统级别");

        private final String code;
        private final String desc;

        Impact(String code, String desc) {
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
     * 是否可以重试
     */
    public boolean canRetry() {
        return retryable != null && retryable &&
               retryCount != null && maxRetries != null &&
               retryCount < maxRetries;
    }

    /**
     * 是否是严重错误
     */
    public boolean isCritical() {
        return Severity.CRITICAL.getCode().equals(severity) ||
               Severity.HIGH.getCode().equals(severity);
    }

    /**
     * 是否影响系统
     */
    public boolean isSystemImpact() {
        return Impact.SYSTEM.getCode().equals(impact);
    }
}

