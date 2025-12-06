package com.chua.starter.monitor.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 文件操作响应
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileOperationResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 操作类型
     */
    private String operation;

    /**
     * 消息
     */
    private String message;

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 文件路径
     */
    private String path;

    /**
     * 文件列表
     */
    private List<Map<String, Object>> files;

    /**
     * 文件树
     */
    private Map<String, Object> tree;

    /**
     * 文件信息
     */
    private Map<String, Object> file;

    /**
     * 文件内容（Base64编码）
     */
    private String content;

    /**
     * 文件内容字节数组
     */
    private byte[] contentBytes;

    /**
     * 文件大小
     */
    private Long size;

    /**
     * 下载URL
     */
    private String downloadUrl;

    /**
     * 磁盘总空间
     */
    private Long totalSpace;

    /**
     * 磁盘可用空间
     */
    private Long freeSpace;

    /**
     * 磁盘可使用空间
     */
    private Long usableSpace;

    /**
     * 是否存在
     */
    private Boolean exists;

    /**
     * 旧路径
     */
    private String oldPath;

    /**
     * 新路径
     */
    private String newPath;

    /**
     * 源路径
     */
    private String sourcePath;

    /**
     * 目标路径
     */
    private String targetPath;

    /**
     * 总数量
     */
    private Integer totalCount;

    /**
     * 已处理数量
     */
    private Integer processedCount;

    /**
     * 附加数据
     */
    private Object data;

    /**
     * 媒体类型（用于预览）
     */
    private org.springframework.http.MediaType mediaType;

    /**
     * 完整文件内容字节数组（用于预览）
     */
    private byte[] all;

    /**
     * 文件信息列表
     */
    private List<FileInfo> fileInfos;

    /**
     * 文件树信息
     */
    private FileInfo treeInfo;

    /**
     * 执行耗时（毫秒）
     */
    private Long duration;

    /**
     * 创建成功响应
     *
     * @param operation 操作类型
     * @param message   消息
     * @return 响应对象
     */
    public static FileOperationResponse success(String operation, String message) {
        FileOperationResponse response = new FileOperationResponse();
        response.setSuccess(true);
        response.setOperation(operation);
        response.setMessage(message);
        return response;
    }

    /**
     * 创建成功响应（带文件列表）
     *
     * @param operation 操作类型
     * @param message   消息
     * @param files     文件列表
     * @return 响应对象
     */
    public static FileOperationResponse success(String operation, String message, List<FileInfo> files) {
        FileOperationResponse response = success(operation, message);
        response.setFileInfos(files);
        return response;
    }

    /**
     * 创建成功响应（带文件列表和路径）
     *
     * @param operation 操作类型
     * @param files     文件列表
     * @param path      路径
     * @return 响应对象
     */
    public static FileOperationResponse success(String operation, List<FileInfo> files, String path) {
        FileOperationResponse response = success(operation, "操作成功");
        response.setFileInfos(files);
        response.setPath(path);
        return response;
    }

    /**
     * 创建成功响应（带附加数据）
     *
     * @param operation 操作类型
     * @param message   消息
     * @param data      附加数据
     * @return 响应对象
     */
    public static FileOperationResponse success(String operation, String message, Object data) {
        FileOperationResponse response = success(operation, message);
        response.setData(data);
        return response;
    }

    /**
     * 创建成功响应（带文件树）
     *
     * @param operation 操作类型
     * @param message   消息
     * @param tree      文件树
     * @return 响应对象
     */
    public static FileOperationResponse successWithTree(String operation, String message, FileInfo tree) {
        FileOperationResponse response = success(operation, message);
        response.setTreeInfo(tree);
        return response;
    }

    /**
     * 创建失败响应
     *
     * @param operation 操作类型
     * @param message   错误消息
     * @return 响应对象
     */
    public static FileOperationResponse error(String operation, String message) {
        FileOperationResponse response = new FileOperationResponse();
        response.setSuccess(false);
        response.setOperation(operation);
        response.setMessage(message);
        return response;
    }

    /**
     * 设置执行耗时（链式调用）
     *
     * @param startTime 开始时间（毫秒）
     * @return 当前对象
     */
    public FileOperationResponse withDuration(long startTime) {
        this.duration = System.currentTimeMillis() - startTime;
        return this;
    }
}
