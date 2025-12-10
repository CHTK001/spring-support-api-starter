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

    /**
     * 排序字段
     */
    private String sortBy;

    /**
     * 排序顺序（asc/desc）
     */
    private String sortOrder;

    /**
     * 文件权限（八进制格式，如：755）
     */
    private String permissions;

    /**
     * 最大文件大小（字节）
     */
    private Long maxSize;

    /**
     * 文件编码
     */
    private String encoding;

    /**
     * 搜索模式
     */
    private String pattern;

    /**
     * 是否包含内容搜索
     */
    private Boolean includeContent;

    /**
     * 最大结果数
     */
    private Integer maxResults;

    /**
     * 压缩包路径
     */
    private String archivePath;

    /**
     * 压缩格式（zip/tar/gz等）
     */
    private String format;

    /**
     * 解压路径
     */
    private String extractPath;

    /**
     * 验证请求参数
     */
    public void validate() {
        if (operation == null || operation.isEmpty()) {
            throw new IllegalArgumentException("操作类型不能为空");
        }
    }

    // ==================== 带默认值的 getter 方法 ====================

    /**
     * 获取是否包含隐藏文件（默认 false）
     *
     * @return 是否包含隐藏文件
     */
    public Boolean getIncludeHiddenOrDefault() {
        return includeHidden != null ? includeHidden : false;
    }

    /**
     * 获取排序字段（默认 name）
     *
     * @return 排序字段
     */
    public String getSortByOrDefault() {
        return sortBy != null ? sortBy : "name";
    }

    /**
     * 获取排序顺序（默认 asc）
     *
     * @return 排序顺序
     */
    public String getSortOrderOrDefault() {
        return sortOrder != null ? sortOrder : "asc";
    }

    /**
     * 获取最大深度（默认 3）
     *
     * @return 最大深度
     */
    public Integer getMaxDepthOrDefault() {
        return maxDepth != null ? maxDepth : 3;
    }

    /**
     * 获取是否递归（默认 false）
     *
     * @return 是否递归
     */
    public Boolean getRecursiveOrDefault() {
        return recursive != null ? recursive : false;
    }

    /**
     * 获取是否覆盖（默认 false）
     *
     * @return 是否覆盖
     */
    public Boolean getOverwriteOrDefault() {
        return overwrite != null ? overwrite : false;
    }

    /**
     * 获取是否包含内容搜索（默认 false）
     *
     * @return 是否包含内容搜索
     */
    public Boolean getIncludeContentOrDefault() {
        return includeContent != null ? includeContent : false;
    }

    /**
     * 获取最大结果数（默认 100）
     *
     * @return 最大结果数
     */
    public Integer getMaxResultsOrDefault() {
        return maxResults != null ? maxResults : 100;
    }

    /**
     * 操作类型枚举
     */
    public enum OperationType {
        /**
         * 列出文件
         */
        LIST("list"),
        /**
         * 获取文件树
         */
        TREE("tree"),
        /**
         * 获取文件信息
         */
        INFO("info"),
        /**
         * 读取文件内容
         */
        READ("read"),
        /**
         * 写入文件内容
         */
        WRITE("write"),
        /**
         * 删除文件
         */
        DELETE("delete"),
        /**
         * 创建目录
         */
        MKDIR("mkdir"),
        /**
         * 重命名
         */
        RENAME("rename"),
        /**
         * 复制
         */
        COPY("copy"),
        /**
         * 移动
         */
        MOVE("move"),
        /**
         * 检查是否存在
         */
        EXISTS("exists"),
        /**
         * 磁盘使用情况
         */
        DISK_USAGE("diskUsage"),
        /**
         * 预览文件
         */
        PREVIEW("preview"),
        /**
         * 下载文件
         */
        DOWNLOAD("download"),
        /**
         * 上传文件
         */
        UPLOAD("upload"),
        /**
         * 修改权限
         */
        CHMOD("chmod"),
        /**
         * 搜索文件
         */
        SEARCH("search"),
        /**
         * 压缩文件
         */
        COMPRESS("compress"),
        /**
         * 解压文件
         */
        EXTRACT("extract"),
        /**
         * 未知操作
         */
        UNKNOWN("unknown");

        private final String code;

        OperationType(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        /**
         * 从操作码获取操作类型
         *
         * @param code 操作码
         * @return 操作类型
         */
        public static OperationType fromCode(String code) {
            if (code == null || code.isEmpty()) {
                return UNKNOWN;
            }
            String upperCode = code.toUpperCase();
            for (OperationType type : values()) {
                if (type.code.equalsIgnoreCase(code) || type.name().equals(upperCode)) {
                    return type;
                }
            }
            return UNKNOWN;
        }
    }
}
