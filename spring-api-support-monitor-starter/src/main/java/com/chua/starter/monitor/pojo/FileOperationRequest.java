package com.chua.starter.monitor.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 文件操作请求
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/06
 */
@Data
public class FileOperationRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 操作类型
     * list: 列出文件
     * tree: 获取文件树
     * info: 获取文件信息
     * read: 读取文件内容
     * write: 写入文件内容
     * delete: 删除文件
     * mkdir: 创建目录
     * rename: 重命名
     * copy: 复制
     * move: 移动
     * exists: 检查是否存在
     * diskUsage: 磁盘使用情况
     */
    private String operation;

    /**
     * 文件路径
     */
    private String path;

    /**
     * 源文件路径（用于复制、移动操作）
     */
    private String sourcePath;

    /**
     * 目标文件路径（用于复制、移动操作）
     */
    private String targetPath;

    /**
     * 新文件名（用于重命名操作）
     */
    private String newName;

    /**
     * 文件内容（Base64编码，用于写入操作）
     */
    private String content;

    /**
     * 是否递归操作
     */
    private Boolean recursive;

    /**
     * 是否覆盖已存在的文件
     */
    private Boolean overwrite;

    /**
     * 是否追加（用于写入操作）
     */
    private Boolean append;

    /**
     * 是否包含隐藏文件
     */
    private Boolean includeHidden;

    /**
     * 最大深度（用于树操作）
     */
    private Integer maxDepth;

    /**
     * 读取偏移量
     */
    private Long offset;

    /**
     * 读取长度
     */
    private Integer length;

    /**
     * 批量操作的路径列表
     */
    private List<String> paths;

    /**
     * 附加参数
     */
    private Map<String, Object> extra;

    /**
     * 请求ID（用于异步响应匹配）
     */
    private String requestId;
}
