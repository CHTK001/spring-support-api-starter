package com.chua.starter.monitor.pojo.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    /**
     * 是否为叶子节点（文件为叶子节点，目录不是叶子节点）
     */
    private Boolean leaf;

    /**
     * 子文件列表（用于文件树）
     */
    private java.util.List<FileInfo> children;

    /**
     * 额外属性JSON字符串
     */
    private String extraProperties;

    // ==================== 兼容旧字段名的别名方法 ====================

    /**
     * 设置是否是目录（兼容旧字段名）
     * 
     * @param isDirectory 是否是目录
     */
    public void setIsDirectory(Boolean isDirectory) {
        this.directory = isDirectory;
    }

    /**
     * 获取是否是目录（兼容旧字段名）
     * 
     * @return 是否是目录
     */
    public Boolean getIsDirectory() {
        return this.directory;
    }

    /**
     * 设置是否隐藏（兼容旧字段名）
     * 
     * @param isHidden 是否隐藏
     */
    public void setIsHidden(Boolean isHidden) {
        this.hidden = isHidden;
    }

    /**
     * 获取是否隐藏（兼容旧字段名）
     * 
     * @return 是否隐藏
     */
    public Boolean getIsHidden() {
        return this.hidden;
    }

    /**
     * 设置修改时间（兼容旧字段名）
     * 
     * @param modifyTime 修改时间
     */
    public void setModifyTime(LocalDateTime modifyTime) {
        this.modifiedTime = modifyTime;
    }

    /**
     * 获取修改时间（兼容旧字段名）
     * 
     * @return 修改时间
     */
    public LocalDateTime getModifyTime() {
        return this.modifiedTime;
    }

    /**
     * 设置创建时间（兼容旧字段名）
     * 
     * @param createTime 创建时间
     */
    public void setCreateTime(LocalDateTime createTime) {
        this.createdTime = createTime;
    }

    /**
     * 获取创建时间（兼容旧字段名）
     * 
     * @return 创建时间
     */
    public LocalDateTime getCreateTime() {
        return this.createdTime;
    }
}
