package com.chua.starter.monitor.starter.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件信息
 * 
 * @author CH
 * @since 2024/12/13
 */
@ApiModel(description = "文件信息")
@Schema(description = "文件信息")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件名
     */
    @ApiModelProperty(value = "文件名")
    @Schema(description = "文件名")
    private String name;

    /**
     * 文件路径
     */
    @ApiModelProperty(value = "文件路径")
    @Schema(description = "文件路径")
    private String path;

    /**
     * 文件大小（字节）
     */
    @ApiModelProperty(value = "文件大小（字节）")
    @Schema(description = "文件大小（字节）")
    private Long size;

    /**
     * 是否为目录
     */
    @ApiModelProperty(value = "是否为目录")
    @Schema(description = "是否为目录")
    private Boolean isDirectory;

    /**
     * 是否为隐藏文件
     */
    @ApiModelProperty(value = "是否为隐藏文件")
    @Schema(description = "是否为隐藏文件")
    private Boolean isHidden;

    /**
     * 文件权限
     */
    @ApiModelProperty(value = "文件权限")
    @Schema(description = "文件权限")
    private String permissions;

    /**
     * 文件所有者
     */
    @ApiModelProperty(value = "文件所有者")
    @Schema(description = "文件所有者")
    private String owner;

    /**
     * 文件组
     */
    @ApiModelProperty(value = "文件组")
    @Schema(description = "文件组")
    private String group;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @ApiModelProperty(value = "修改时间")
    @Schema(description = "修改时间")
    private LocalDateTime modifyTime;

    /**
     * 访问时间
     */
    @ApiModelProperty(value = "访问时间")
    @Schema(description = "访问时间")
    private LocalDateTime accessTime;

    /**
     * 文件类型
     */
    @ApiModelProperty(value = "文件类型")
    @Schema(description = "文件类型")
    private String fileType;

    /**
     * MIME类型
     */
    @ApiModelProperty(value = "MIME类型")
    @Schema(description = "MIME类型")
    private String mimeType;

    /**
     * 文件扩展名
     */
    @ApiModelProperty(value = "文件扩展名")
    @Schema(description = "文件扩展名")
    private String extension;

    /**
     * 是否可读
     */
    @ApiModelProperty(value = "是否可读")
    @Schema(description = "是否可读")
    private Boolean readable;

    /**
     * 是否可写
     */
    @ApiModelProperty(value = "是否可写")
    @Schema(description = "是否可写")
    private Boolean writable;

    /**
     * 是否可执行
     */
    @ApiModelProperty(value = "是否可执行")
    @Schema(description = "是否可执行")
    private Boolean executable;

    /**
     * 子文件列表（目录时使用）
     */
    @ApiModelProperty(value = "子文件列表")
    @Schema(description = "子文件列表（目录时使用）")
    private List<FileInfo> children;

    /**
     * 父目录路径
     */
    @ApiModelProperty(value = "父目录路径")
    @Schema(description = "父目录路径")
    private String parentPath;

    /**
     * 文件深度（在树结构中的层级）
     */
    @ApiModelProperty(value = "文件深度")
    @Schema(description = "文件深度（在树结构中的层级）")
    private Integer depth;

    /**
     * 额外属性
     */
    @ApiModelProperty(value = "额外属性")
    @Schema(description = "额外属性（JSON格式）")
    private String extraProperties;

    /**
     * 是否为叶子节点（用于Element Plus el-tree组件）
     */
    @ApiModelProperty(value = "是否为叶子节点")
    @Schema(description = "是否为叶子节点（用于Element Plus el-tree组件）")
    private Boolean leaf;

    /**
     * 获取格式化的文件大小
     */
    public String getFormattedSize() {
        if (size == null || size == 0) {
            return "0 B";
        }

        String[] units = { "B", "KB", "MB", "GB", "TB" };
        int unitIndex = 0;
        double fileSize = size.doubleValue();

        while (fileSize >= 1024 && unitIndex < units.length - 1) {
            fileSize /= 1024;
            unitIndex++;
        }

        return String.format("%.2f %s", fileSize, units[unitIndex]);
    }

    /**
     * 获取文件图标类型
     */
    public String getIconType() {
        if (isDirectory != null && isDirectory) {
            return "folder";
        }

        if (extension == null || extension.isEmpty()) {
            return "file";
        }

        String ext = extension.toLowerCase();
        switch (ext) {
        case "txt":
        case "log":
        case "md":
            return "file-text";
        case "jpg":
        case "jpeg":
        case "png":
        case "gif":
        case "bmp":
            return "file-image";
        case "mp4":
        case "avi":
        case "mov":
        case "wmv":
            return "file-video";
        case "mp3":
        case "wav":
        case "flac":
            return "file-audio";
        case "pdf":
            return "file-pdf";
        case "doc":
        case "docx":
            return "file-word";
        case "xls":
        case "xlsx":
            return "file-excel";
        case "ppt":
        case "pptx":
            return "file-powerpoint";
        case "zip":
        case "rar":
        case "7z":
        case "tar":
        case "gz":
            return "file-archive";
        case "java":
        case "js":
        case "py":
        case "cpp":
        case "c":
        case "php":
        case "html":
        case "css":
            return "file-code";
        default:
            return "file";
        }
    }

    /**
     * 判断是否为文本文件
     */
    public boolean isTextFile() {
        if (extension == null || extension.isEmpty()) {
            return false;
        }

        String ext = extension.toLowerCase();
        String[] textExtensions = { "txt", "log", "md", "json", "xml", "yml", "yaml", "properties", "java", "js", "py",
                "cpp", "c", "h", "php", "html", "css", "sql" };

        for (String textExt : textExtensions) {
            if (textExt.equals(ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否为图片文件
     */
    public boolean isImageFile() {
        if (extension == null || extension.isEmpty()) {
            return false;
        }

        String ext = extension.toLowerCase();
        String[] imageExtensions = { "jpg", "jpeg", "png", "gif", "bmp", "svg", "webp" };

        for (String imageExt : imageExtensions) {
            if (imageExt.equals(ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否为压缩文件
     */
    public boolean isArchiveFile() {
        if (extension == null || extension.isEmpty()) {
            return false;
        }

        String ext = extension.toLowerCase();
        String[] archiveExtensions = { "zip", "rar", "7z", "tar", "gz", "bz2", "xz" };

        for (String archiveExt : archiveExtensions) {
            if (archiveExt.equals(ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取权限描述
     */
    public String getPermissionDescription() {
        if (permissions == null || permissions.isEmpty()) {
            return "未知";
        }

        StringBuilder desc = new StringBuilder();
        if (readable != null && readable) {
            desc.append("读 ");
        }
        if (writable != null && writable) {
            desc.append("写 ");
        }
        if (executable != null && executable) {
            desc.append("执行 ");
        }

        return desc.toString().trim();
    }

    /**
     * 获取完整路径
     */
    public String getFullPath() {
        if (path == null) {
            return name;
        }
        if (path.endsWith("/")) {
            return path + name;
        } else {
            return path + "/" + name;
        }
    }

    /**
     * 判断是否为空目录
     */
    public boolean isEmptyDirectory() {
        return isDirectory != null && isDirectory && (children == null || children.isEmpty());
    }

    /**
     * 获取子文件数量
     */
    public int getChildrenCount() {
        return children != null ? children.size() : 0;
    }

    /**
     * 判断是否有子目录
     */
    public boolean hasSubDirectories() {
        if (children == null || children.isEmpty()) {
            return false;
        }

        return children.stream().anyMatch(child -> child.getIsDirectory() != null && child.getIsDirectory());
    }

    /**
     * 创建简单文件信息
     */
    public static FileInfo createSimple(String name, String path, boolean isDirectory) {
        return FileInfo.builder().name(name).path(path).isDirectory(isDirectory).build();
    }

    /**
     * 创建目录信息
     */
    public static FileInfo createDirectory(String name, String path) {
        return FileInfo.builder().name(name).path(path).isDirectory(true).size(0L).build();
    }

    /**
     * 创建文件信息
     */
    public static FileInfo createFile(String name, String path, long size) {
        return FileInfo.builder().name(name).path(path).isDirectory(false).size(size).build();
    }

    @Override
    public String toString() {
        return String.format("FileInfo{name='%s', path='%s', isDirectory=%s, size=%s}", name, path, isDirectory,
                getFormattedSize());
    }
}
