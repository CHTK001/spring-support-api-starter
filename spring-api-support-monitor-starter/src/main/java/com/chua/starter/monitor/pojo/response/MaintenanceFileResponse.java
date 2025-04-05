package com.chua.starter.monitor.pojo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 维护文件响应对象
 *
 * @author CH
 */
@Data
@Schema(description = "维护文件响应")
public class MaintenanceFileResponse {

    /**
     * 文件ID
     */
    @Schema(description = "文件ID")
    private Long maintenanceFileId;

    /**
     * 维护组ID
     */
    @Schema(description = "维护组ID")
    private Long maintenanceGroupId;

    /**
     * 文件名称
     */
    @Schema(description = "文件名称")
    private String maintenanceFileName;

    /**
     * 文件路径
     */
    @Schema(description = "文件路径")
    private String maintenanceFilePath;

    /**
     * 文件大小（字节）
     */
    @Schema(description = "文件大小")
    private Long maintenanceFileSize;

    /**
     * 是否是压缩文件
     */
    @Schema(description = "是否压缩;0:否;1:是")
    private Boolean maintenanceFileIsCompressed;

    /**
     * 文件类型（扩展名）
     */
    @Schema(description = "文件类型")
    private String maintenanceFileType;

    /**
     * 是否解压：0-否，1-是
     */
    @Schema(description = "是否解压;0:否;1:是")
    private Integer maintenanceFileIsExtract;

    /**
     * 是否覆盖：0-否，1-是
     */
    @Schema(description = "是否覆盖;0:否;1:是")
    private Integer maintenanceFileIsOverride;

    /**
     * 文件状态：0-失效，1-有效
     */
    @Schema(description = "状态;0:停用;1:启用")
    private Integer maintenanceFileStatus;

    /**
     * 解压路径
     */
    @Schema(description = "解压路径")
    private String maintenanceFileExtractPath;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime maintenanceFileCreateTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
} 