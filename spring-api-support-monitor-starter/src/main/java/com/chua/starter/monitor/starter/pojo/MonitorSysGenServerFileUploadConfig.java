package com.chua.starter.monitor.starter.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 服务器文件上传配置信息实体类
 *
 * @author CH
 * @since 2024/12/20
 */
@ApiModel(description = "服务器文件上传配置信息")
@Schema(description = "服务器文件上传配置信息")
@Data
public class MonitorSysGenServerFileUploadConfig implements Serializable {

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
     * 最大并发上传数
     */
    @ApiModelProperty(value = "最大并发上传数")
    @Schema(description = "最大并发上传数")
    private Integer maxConcurrentUploads;

    /**
     * 最大文件大小(字节)
     */
    @ApiModelProperty(value = "最大文件大小(字节)")
    @Schema(description = "最大文件大小(字节)")
    private Long maxFileSize;

    /**
     * 允许的文件类型
     */
    @ApiModelProperty(value = "允许的文件类型")
    @Schema(description = "允许的文件类型(逗号分隔)")
    private String allowedFileTypes;

    /**
     * 禁止的文件类型
     */
    @ApiModelProperty(value = "禁止的文件类型")
    @Schema(description = "禁止的文件类型(逗号分隔)")
    private String forbiddenFileTypes;

    /**
     * 默认目标路径
     */
    @ApiModelProperty(value = "默认目标路径")
    @Schema(description = "默认目标路径")
    private String defaultTargetPath;

    /**
     * 超时时间(秒)
     */
    @ApiModelProperty(value = "超时时间(秒)")
    @Schema(description = "超时时间(秒)")
    private Integer timeoutSeconds;

    /**
     * 重试次数
     */
    @ApiModelProperty(value = "重试次数")
    @Schema(description = "重试次数")
    private Integer maxRetries;

    /**
     * 重试间隔(秒)
     */
    @ApiModelProperty(value = "重试间隔(秒)")
    @Schema(description = "重试间隔(秒)")
    private Integer retryInterval;

    /**
     * 是否启用压缩
     */
    @ApiModelProperty(value = "是否启用压缩")
    @Schema(description = "是否启用压缩")
    private Boolean compressionEnabled;

    /**
     * 是否启用加密
     */
    @ApiModelProperty(value = "是否启用加密")
    @Schema(description = "是否启用加密")
    private Boolean encryptionEnabled;

    /**
     * 是否启用校验
     */
    @ApiModelProperty(value = "是否启用校验")
    @Schema(description = "是否启用校验")
    private Boolean verificationEnabled;

    /**
     * 是否启用备份
     */
    @ApiModelProperty(value = "是否启用备份")
    @Schema(description = "是否启用备份")
    private Boolean backupEnabled;

    /**
     * 备份路径
     */
    @ApiModelProperty(value = "备份路径")
    @Schema(description = "备份路径")
    private String backupPath;

    /**
     * 是否覆盖已存在文件
     */
    @ApiModelProperty(value = "是否覆盖已存在文件")
    @Schema(description = "是否覆盖已存在文件")
    private Boolean overwriteEnabled;

    /**
     * 分片大小(字节)
     */
    @ApiModelProperty(value = "分片大小(字节)")
    @Schema(description = "分片大小(字节)")
    private Long chunkSize;

    /**
     * 缓冲区大小(字节)
     */
    @ApiModelProperty(value = "缓冲区大小(字节)")
    @Schema(description = "缓冲区大小(字节)")
    private Integer bufferSize;

    /**
     * 连接超时时间(秒)
     */
    @ApiModelProperty(value = "连接超时时间(秒)")
    @Schema(description = "连接超时时间(秒)")
    private Integer connectionTimeout;

    /**
     * 读取超时时间(秒)
     */
    @ApiModelProperty(value = "读取超时时间(秒)")
    @Schema(description = "读取超时时间(秒)")
    private Integer readTimeout;

    /**
     * 写入超时时间(秒)
     */
    @ApiModelProperty(value = "写入超时时间(秒)")
    @Schema(description = "写入超时时间(秒)")
    private Integer writeTimeout;

    /**
     * 是否启用断点续传
     */
    @ApiModelProperty(value = "是否启用断点续传")
    @Schema(description = "是否启用断点续传")
    private Boolean resumeEnabled;

    /**
     * 是否启用进度回调
     */
    @ApiModelProperty(value = "是否启用进度回调")
    @Schema(description = "是否启用进度回调")
    private Boolean progressCallbackEnabled;

    /**
     * 进度回调间隔(毫秒)
     */
    @ApiModelProperty(value = "进度回调间隔(毫秒)")
    @Schema(description = "进度回调间隔(毫秒)")
    private Integer progressCallbackInterval;

    /**
     * 是否启用自动清理
     */
    @ApiModelProperty(value = "是否启用自动清理")
    @Schema(description = "是否启用自动清理")
    private Boolean autoCleanupEnabled;

    /**
     * 清理保留天数
     */
    @ApiModelProperty(value = "清理保留天数")
    @Schema(description = "清理保留天数")
    private Integer cleanupRetentionDays;

    /**
     * 队列优先级
     */
    @ApiModelProperty(value = "队列优先级")
    @Schema(description = "队列优先级(1-10，数字越小优先级越高)")
    private Integer queuePriority;

    /**
     * 是否启用配置
     */
    @ApiModelProperty(value = "是否启用配置")
    @Schema(description = "是否启用配置")
    private Boolean enabled;

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
     * 获取最大文件大小(MB)
     */
    public Double getMaxFileSizeMB() {
        return maxFileSize != null ? maxFileSize / (1024.0 * 1024.0) : 0.0;
    }

    /**
     * 获取分片大小(MB)
     */
    public Double getChunkSizeMB() {
        return chunkSize != null ? chunkSize / (1024.0 * 1024.0) : 0.0;
    }

    /**
     * 获取缓冲区大小(KB)
     */
    public Double getBufferSizeKB() {
        return bufferSize != null ? bufferSize / 1024.0 : 0.0;
    }

    /**
     * 是否允许文件类型
     */
    public boolean isFileTypeAllowed(String fileType) {
        if (allowedFileTypes == null || allowedFileTypes.trim().isEmpty()) {
            return true; // 如果没有限制，则允许所有类型
        }

        String[] allowed = allowedFileTypes.split(",");
        for (String type : allowed) {
            if (type.trim().equalsIgnoreCase(fileType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否禁止文件类型
     */
    public boolean isFileTypeForbidden(String fileType) {
        if (forbiddenFileTypes == null || forbiddenFileTypes.trim().isEmpty()) {
            return false; // 如果没有禁止列表，则不禁止
        }

        String[] forbidden = forbiddenFileTypes.split(",");
        for (String type : forbidden) {
            if (type.trim().equalsIgnoreCase(fileType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 验证文件大小
     */
    public boolean isFileSizeValid(Long fileSize) {
        return maxFileSize == null || fileSize == null || fileSize <= maxFileSize;
    }
}

