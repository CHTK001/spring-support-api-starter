package com.chua.starter.monitor.pojo;

import lombok.Data;

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
}
