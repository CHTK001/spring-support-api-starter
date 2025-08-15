package com.chua.starter.monitor.pojo;

import lombok.Data;

/**
 * 前端文件系统配置 DTO（与 /v1/filesystem/config 接口对齐）
 *
 * @author CH
 * @since 2025-04-05
 */
@Data
public class FileSystemConfigDto {

    /**
     * 分片大小(MB)，例如：5 表示每个分片为5MB
     */
    private Integer chunkSize;

    /**
     * 最大并发合并/上传数，例如：3 表示最多同时处理3个上传或合并任务
     */
    private Integer maxConcurrent;

    /**
     * 重试次数（后端暂未持久化），例如：3 表示失败时最多重试3次
     */
    private Integer retryCount;

    /**
     * 是否自动合并（映射为后端 manualMergeEnabled 的反义），
     * 例如：true 表示文件上传完成后自动合并分片
     */
    private Boolean autoMerge;

    /**
     * 是否启用HTTP访问，例如：true 表示可以通过HTTP协议访问上传的文件
     */
    private Boolean enableHttpAccess;

    /**
     * 允许的文件类型数组，例如 ["*", "jpg", "png"] 表示允许所有文件、jpg和png格式
     */
    private String[] allowedFileTypes;

    /**
     * 最大文件大小(MB)，例如：100 表示单个文件最大为100MB
     */
    private Integer maxFileSize;

    /**
     * 存储配额（后端暂未持久化），例如：1024 表示总存储空间为1024MB
     */
    private Integer storageQuota;

    /**
     * 文件存储根路径，例如："/data/uploads" 表示文件将存储在该目录下
     */
    private String storageRootPath;
}

