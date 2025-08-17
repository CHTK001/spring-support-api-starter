package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.datasource.annotation.ColumnDesc;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件系统管理
 *
 * @author CH
 * @since 2024/12/30
 */
@ApiModel(description = "文件系统管理")
@Schema(description = "文件系统管理")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "file_system")
public class FileSystem extends SysBase implements Serializable {

    /**
     * 文件ID
     */
    @TableId(value = "file_system_id", type = IdType.AUTO)
    @ApiModelProperty(value = "文件ID")
    @Schema(description = "文件ID")
    @NotNull(message = "文件ID不能为null")
    private Integer fileSystemId;

    /**
     * 文件名称
     */
    @ColumnDesc("文件名称")
    @TableField(value = "file_system_name")
    @ApiModelProperty(value = "文件名称")
    @Schema(description = "文件名称")
    @Size(max = 255, message = "文件名称最大长度要小于 255")
    private String fileSystemName;

    /**
     * 文件原始名称
     */
    @ColumnDesc("文件原始名称")
    @TableField(value = "file_system_original_name")
    @ApiModelProperty(value = "文件原始名称")
    @Schema(description = "文件原始名称")
    @Size(max = 255, message = "文件原始名称最大长度要小于 255")
    private String fileSystemOriginalName;

    /**
     * 文件路径
     */
    @ColumnDesc("文件路径")
    @TableField(value = "file_system_path")
    @ApiModelProperty(value = "文件路径")
    @Schema(description = "文件路径")
    @Size(max = 500, message = "文件路径最大长度要小于 500")
    private String fileSystemPath;

    /**
     * 文件大小(字节)
     */
    @ColumnDesc("文件大小(字节)")
    @TableField(value = "file_system_size")
    @ApiModelProperty(value = "文件大小(字节)")
    @Schema(description = "文件大小(字节)")
    private Long fileSystemSize;

    /**
     * 文件MD5值
     */
    @ColumnDesc("文件MD5值")
    @TableField(value = "file_system_md5")
    @ApiModelProperty(value = "文件MD5值")
    @Schema(description = "文件MD5值")
    @Size(max = 32, message = "文件MD5值最大长度要小于 32")
    private String fileSystemMd5;

    /**
     * 文件类型
     */
    @ColumnDesc("文件类型")
    @TableField(value = "file_system_type")
    @ApiModelProperty(value = "文件类型")
    @Schema(description = "文件类型")
    @Size(max = 100, message = "文件类型最大长度要小于 100")
    private String fileSystemType;

    /**
     * 文件扩展名
     */
    @ColumnDesc("文件扩展名")
    @TableField(value = "file_system_extension")
    @ApiModelProperty(value = "文件扩展名")
    @Schema(description = "文件扩展名")
    @Size(max = 10, message = "文件扩展名最大长度要小于 10")
    private String fileSystemExtension;

    /**
     * 分片总数
     */
    @ColumnDesc("分片总数")
    @TableField(value = "file_system_chunk_total")
    @ApiModelProperty(value = "分片总数")
    @Schema(description = "分片总数")
    private Integer fileSystemChunkTotal;

    /**
     * 已上传分片数
     */
    @ColumnDesc("已上传分片数")
    @TableField(value = "file_system_chunk_uploaded")
    @ApiModelProperty(value = "已上传分片数")
    @Schema(description = "已上传分片数")
    private Integer fileSystemChunkUploaded;

    /**
     * 文件状态：0-待合并，1-合并中，2-已完成，3-合并失败
     */
    @ColumnDesc("文件状态：0-待合并，1-合并中，2-已完成，3-合并失败")
    @TableField(value = "file_system_status")
    @ApiModelProperty(value = "文件状态：0-待合并，1-合并中，2-已完成，3-合并失败")
    @Schema(description = "文件状态：0-待合并，1-合并中，2-已完成，3-合并失败")
    private Integer fileSystemStatus;

    /**
     * 是否启用HTTP访问
     */
    @ColumnDesc("是否启用HTTP访问")
    @TableField(value = "file_system_http_access_enabled")
    @ApiModelProperty(value = "是否启用HTTP访问")
    @Schema(description = "是否启用HTTP访问")
    private Boolean fileSystemHttpAccessEnabled;

    /**
     * HTTP访问URL
     */
    @ColumnDesc("HTTP访问URL")
    @TableField(value = "file_system_http_access_url")
    @ApiModelProperty(value = "HTTP访问URL")
    @Schema(description = "HTTP访问URL")
    @Size(max = 500, message = "HTTP访问URL最大长度要小于 500")
    private String fileSystemHttpAccessUrl;

    /**
     * 绑定的服务ID（Service Management）
     */
    @ColumnDesc("绑定的服务ID")
    @TableField(value = "file_system_http_service_id")
    @ApiModelProperty(value = "绑定的服务ID")
    @Schema(description = "绑定的服务ID")
    private Integer fileSystemHttpServiceId;

    /**
     * 上传开始时间
     */
    @ColumnDesc("上传开始时间")
    @TableField(value = "file_system_upload_start_time")
    @ApiModelProperty(value = "上传开始时间")
    @Schema(description = "上传开始时间")
    private LocalDateTime fileSystemUploadStartTime;

    /**
     * 上传完成时间
     */
    @ColumnDesc("上传完成时间")
    @TableField(value = "file_system_upload_complete_time")
    @ApiModelProperty(value = "上传完成时间")
    @Schema(description = "上传完成时间")
    private LocalDateTime fileSystemUploadCompleteTime;

    /**
     * 备注
     */
    @ColumnDesc("备注")
    @TableField(value = "file_system_remark")
    @ApiModelProperty(value = "备注")
    @Schema(description = "备注")
    @Size(max = 500, message = "备注最大长度要小于 500")
    private String fileSystemRemark;

    /**
     * 文件分组ID
     */
    @ColumnDesc("文件分组ID")
    @TableField(value = "file_system_group_id")
    @ApiModelProperty(value = "文件分组ID")
    @Schema(description = "文件分组ID")
    private Integer fileSystemGroupId;

    /**
     * 文件分组名称（冗余字段，便于查询显示）
     */
    @ColumnDesc("文件分组名称")
    @TableField(value = "file_system_group_name", exist = false)
    @ApiModelProperty(value = "文件分组名称")
    @Schema(description = "文件分组名称")
    private String fileSystemGroupName;

    private static final long serialVersionUID = 1L;

    /**
     * 文件状态枚举
     */
    public static class FileStatus {
        public static final int PENDING_MERGE = 0; // 待合并
        public static final int MERGING = 1; // 合并中
        public static final int COMPLETED = 2; // 已完成
        public static final int MERGE_FAILED = 3; // 合并失败
    }
}
