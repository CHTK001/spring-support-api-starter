package com.chua.report.client.starter.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 文件操作响应
 * @author CH
 * @since 2024/12/19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileOperationResponse {

    /**
     * 操作是否成功
     */
    private Boolean success;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 操作类型
     */
    private String operation;

    /**
     * 文件信息列表
     */
    private List<FileInfo> files;

    /**
     * 单个文件信息
     */
    private FileInfo file;

    /**
     * 文件内容（用于预览等操作）
     */
    private String content;

    /**
     * 文件内容字节数组（用于下载等操作）
     */
    private byte[] data;

    /**
     * 文件大小
     */
    private Long size;

    /**
     * 文件数量
     */
    private Integer count;

    /**
     * 操作时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operationTime;

    /**
     * 额外属性
     */
    private Map<String, Object> properties;

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 错误详情
     */
    private String errorDetail;

    /**
     * 处理耗时（毫秒）
     */
    private Long duration;

    /**
     * 文件系统信息
     */
    private FileSystemInfo fileSystemInfo;

    /**
     * 创建成功响应
     */
    public static FileOperationResponse success(String operation, String message) {
        return FileOperationResponse.builder()
                .success(true)
                .operation(operation)
                .message(message)
                .operationTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建成功响应（带文件列表）
     */
    public static FileOperationResponse success(String operation, List<FileInfo> files) {
        return FileOperationResponse.builder()
                .success(true)
                .operation(operation)
                .files(files)
                .count(files != null ? files.size() : 0)
                .operationTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建成功响应（带单个文件）
     */
    public static FileOperationResponse success(String operation, FileInfo file) {
        return FileOperationResponse.builder()
                .success(true)
                .operation(operation)
                .file(file)
                .operationTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建成功响应（带内容）
     */
    public static FileOperationResponse success(String operation, String content) {
        return FileOperationResponse.builder()
                .success(true)
                .operation(operation)
                .content(content)
                .operationTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建失败响应
     */
    public static FileOperationResponse error(String operation, String message) {
        return FileOperationResponse.builder()
                .success(false)
                .operation(operation)
                .message(message)
                .operationTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建失败响应（带错误代码）
     */
    public static FileOperationResponse error(String operation, String message, String errorCode) {
        return FileOperationResponse.builder()
                .success(false)
                .operation(operation)
                .message(message)
                .errorCode(errorCode)
                .operationTime(LocalDateTime.now())
                .build();
    }

    /**
     * 文件系统信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileSystemInfo {
        /**
         * 总空间（字节）
         */
        private Long totalSpace;

        /**
         * 可用空间（字节）
         */
        private Long freeSpace;

        /**
         * 已用空间（字节）
         */
        private Long usedSpace;

        /**
         * 使用率（百分比）
         */
        private Double usagePercentage;

        /**
         * 文件系统类型
         */
        private String fileSystemType;

        /**
         * 根路径
         */
        private String rootPath;
    }
}
