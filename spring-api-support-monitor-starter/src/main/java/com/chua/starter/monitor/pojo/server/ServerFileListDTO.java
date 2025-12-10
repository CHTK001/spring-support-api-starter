package com.chua.starter.monitor.pojo;

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
 * 服务器文件列表DTO
 *
 * @author CH
 * @since 2024/12/30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "ServerFileListDTO", description = "服务器文件列表DTO")
@Schema(description = "服务器文件列表DTO")
public class ServerFileListDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前路径
     */
    @ApiModelProperty(value = "当前路径")
    @Schema(description = "当前路径")
    private String currentPath;

    /**
     * 父路径
     */
    @ApiModelProperty(value = "父路径")
    @Schema(description = "父路径")
    private String parentPath;

    /**
     * 文件列表
     */
    @ApiModelProperty(value = "文件列表")
    @Schema(description = "文件列表")
    private List<FileInfoDTO> files;

    /**
     * 目录列表
     */
    @ApiModelProperty(value = "目录列表")
    @Schema(description = "目录列表")
    private List<FileInfoDTO> directories;

    /**
     * 总文件数
     */
    @ApiModelProperty(value = "总文件数")
    @Schema(description = "总文件数")
    private Integer totalFiles;

    /**
     * 总目录数
     */
    @ApiModelProperty(value = "总目录数")
    @Schema(description = "总目录数")
    private Integer totalDirectories;

    /**
     * 文件信息内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel(value = "FileInfoDTO", description = "文件信息DTO")
    @Schema(description = "文件信息DTO")
    public static class FileInfoDTO implements Serializable {

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
         * 修改时间
         */
        @ApiModelProperty(value = "修改时间")
        @Schema(description = "修改时间")
        private LocalDateTime lastModified;

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
         * 获取格式化的文件大小
         */
        public String getFormattedSize() {
            if (size == null || isDirectory) {
                return "-";
            }
            
            if (size < 1024) {
                return size + " B";
            } else if (size < 1024 * 1024) {
                return String.format("%.1f KB", size / 1024.0);
            } else if (size < 1024 * 1024 * 1024) {
                return String.format("%.1f MB", size / (1024.0 * 1024.0));
            } else {
                return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
            }
        }

        /**
         * 获取文件类型
         */
        public String getFileType() {
            if (isDirectory != null && isDirectory) {
                return "目录";
            }
            
            if (name == null) {
                return "未知";
            }
            
            int lastDot = name.lastIndexOf('.');
            if (lastDot > 0 && lastDot < name.length() - 1) {
                return name.substring(lastDot + 1).toUpperCase();
            }
            
            return "文件";
        }
    }
}
