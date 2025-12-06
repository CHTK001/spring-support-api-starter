package com.chua.starter.monitor.pojo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件信息
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/06
 */
@Data
public class FileInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件名
     */
    private String name;

    /**
     * 文件完整路径
     */
    private String path;

    /**
     * 父目录路径
     */
    private String parentPath;

    /**
     * 文件大小（字节）
     */
    private Long size;

    /**
     * 是否是目录
     */
    private Boolean directory;

    /**
     * 是否是文件
     */
    private Boolean file;

    /**
     * 是否隐藏
     */
    private Boolean hidden;

    /**
     * 是否可读
     */
    private Boolean readable;

    /**
     * 是否可写
     */
    private Boolean writable;

    /**
     * 是否可执行
     */
    private Boolean executable;

    /**
     * 文件扩展名
     */
    private String extension;

    /**
     * MIME类型
     */
    private String mimeType;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 修改时间
     */
    private LocalDateTime modifiedTime;

    /**
     * 访问时间
     */
    private LocalDateTime accessTime;

    /**
     * 文件所有者
     */
    private String owner;

    /**
     * 文件所属组
     */
    private String group;

    /**
     * 文件权限（Unix格式，如：rwxr-xr-x）
     */
    private String permissions;

    /**
     * 文件权限（八进制格式，如：755）
     */
    private String permissionsOctal;

    /**
     * 是否为符号链接
     */
    private Boolean symbolicLink;

    /**
     * 符号链接目标路径
     */
    private String linkTarget;

    /**
     * 文件MD5摘要
     */
    private String md5;

    /**
     * 文件SHA256摘要
     */
    private String sha256;

    /**
     * 子文件数量（目录时有效）
     */
    private Integer childCount;

    /**
     * 附加属性
     */
    private java.util.Map<String, Object> attributes;
}
