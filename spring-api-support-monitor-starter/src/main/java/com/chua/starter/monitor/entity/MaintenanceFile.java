package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 维护文件实体类
 * 定义维护管理中的文件信息
 *
 * @author CH
 */
@Data
@TableName("maintenance_file")
@Schema(description = "维护文件")
public class MaintenanceFile {

    /**
     * 文件ID，主键
     */
    @TableId(value = "maintenance_file_id", type = IdType.AUTO)
    @Schema(description = "文件ID")
    private Long maintenanceFileId;

    /**
     * 维护组ID
     */
    @TableField("maintenance_group_id")
    @Schema(description = "维护组ID")
    private Long maintenanceGroupId;

    /**
     * 文件名称
     */
    @TableField("maintenance_file_name")
    @Schema(description = "文件名称")
    private String maintenanceFileName;

    /**
     * 文件路径
     */
    @TableField("maintenance_file_path")
    @Schema(description = "文件路径")
    private String maintenanceFilePath;

    /**
     * 文件大小（字节）
     */
    @TableField("maintenance_file_size")
    @Schema(description = "文件大小")
    private Long maintenanceFileSize;

    /**
     * 是否是压缩文件
     */
    @TableField("maintenance_file_is_compressed")
    @Schema(description = "是否压缩;0:否;1:是")
    private Boolean maintenanceFileIsCompressed;

    /**
     * 文件类型（扩展名）
     */
    @TableField("maintenance_file_type")
    @Schema(description = "文件类型")
    private String maintenanceFileType;

    /**
     * 是否解压：0-否，1-是
     */
    @TableField("maintenance_file_is_extract")
    @Schema(description = "是否解压;0:否;1:是")
    private Integer maintenanceFileIsExtract;

    /**
     * 是否覆盖：0-否，1-是
     */
    @TableField("maintenance_file_is_override")
    @Schema(description = "是否覆盖;0:否;1:是")
    private Integer maintenanceFileIsOverride;

    /**
     * 文件状态：0-失效，1-有效
     */
    @TableField("maintenance_file_status")
    @Schema(description = "状态;0:停用;1:启用")
    private Integer maintenanceFileStatus;

    /**
     * 创建时间
     */
    @TableField("maintenance_file_create_time")
    @Schema(description = "创建时间")
    private LocalDateTime maintenanceFileCreateTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 创建者
     */
    @TableField("create_by")
    @Schema(description = "创建者")
    private String createBy;

    /**
     * 更新者
     */
    @TableField("update_by")
    @Schema(description = "更新者")
    private String updateBy;
}