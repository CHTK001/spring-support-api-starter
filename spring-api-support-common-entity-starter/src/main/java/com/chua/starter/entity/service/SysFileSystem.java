package com.chua.starter.entity.service;

import com.baomidou.mybatisplus.annotation.*;
import com.chua.common.support.datasource.annotation.ColumnDesc;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件系统管理表
 * 用于管理分片上传的文件信息
 *
 * @author CH
 * @since 2024/12/10
 * @version 1.0.0
 */
@ApiModel(description = "文件系统管理表")
@Schema(description = "文件系统管理表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_file_system")
public class SysFileSystem extends SysBase implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "sys_file_system_id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID")
    @Schema(description = "主键ID")
    private Integer sysFileSystemId;

    /**
     * 任务唯一标识（ULID）
     */
    @ColumnDesc("任务唯一标识")
    @TableField(value = "sys_file_system_tid")
    @ApiModelProperty(value = "任务唯一标识")
    @Schema(description = "任务唯一标识（ULID）")
    @Size(max = 64, message = "任务唯一标识最大长度要小于 64")
    private String sysFileSystemTid;

    /**
     * 存储桶/存储位置
     */
    @ColumnDesc("存储桶")
    @TableField(value = "sys_file_system_bucket")
    @ApiModelProperty(value = "存储桶")
    @Schema(description = "存储桶/存储位置")
    @Size(max = 255, message = "存储桶最大长度要小于 255")
    private String sysFileSystemBucket;

    /**
     * 文件名称
     */
    @ColumnDesc("文件名称")
    @TableField(value = "sys_file_system_name")
    @ApiModelProperty(value = "文件名称")
    @Schema(description = "文件名称")
    @Size(max = 255, message = "文件名称最大长度要小于 255")
    private String sysFileSystemName;

    /**
     * 文件原始名称
     */
    @ColumnDesc("文件原始名称")
    @TableField(value = "sys_file_system_original_name")
    @ApiModelProperty(value = "文件原始名称")
    @Schema(description = "文件原始名称")
    @Size(max = 255, message = "文件原始名称最大长度要小于 255")
    private String sysFileSystemOriginalName;

    /**
     * 存储类型：local-本地存储, oss-阿里云OSS, cos-腾讯云COS等
     */
    @ColumnDesc("存储类型")
    @TableField(value = "sys_file_system_type")
    @ApiModelProperty(value = "存储类型")
    @Schema(description = "存储类型：local-本地存储, oss-阿里云OSS, cos-腾讯云COS等")
    @Size(max = 50, message = "存储类型最大长度要小于 50")
    private String sysFileSystemType;

    /**
     * 文件扩展名
     */
    @ColumnDesc("文件扩展名")
    @TableField(value = "sys_file_system_extension")
    @ApiModelProperty(value = "文件扩展名")
    @Schema(description = "文件扩展名")
    @Size(max = 20, message = "文件扩展名最大长度要小于 20")
    private String sysFileSystemExtension;

    /**
     * 文件路径
     */
    @ColumnDesc("文件路径")
    @TableField(value = "sys_file_system_path")
    @ApiModelProperty(value = "文件路径")
    @Schema(description = "文件路径")
    @Size(max = 500, message = "文件路径最大长度要小于 500")
    private String sysFileSystemPath;

    /**
     * 文件大小(字节)
     */
    @ColumnDesc("文件大小(字节)")
    @TableField(value = "sys_file_system_size")
    @ApiModelProperty(value = "文件大小(字节)")
    @Schema(description = "文件大小(字节)")
    private Long sysFileSystemSize;

    /**
     * 文件MD5值
     */
    @ColumnDesc("文件MD5值")
    @TableField(value = "sys_file_system_md5")
    @ApiModelProperty(value = "文件MD5值")
    @Schema(description = "文件MD5值")
    @Size(max = 32, message = "文件MD5值最大长度要小于 32")
    private String sysFileSystemMd5;

    /**
     * 分片数量
     */
    @ColumnDesc("分片数量")
    @TableField(value = "sys_file_system_chunk_total")
    @ApiModelProperty(value = "分片数量")
    @Schema(description = "分片数量")
    private Integer sysFileSystemChunkTotal;

    /**
     * 已上传分片数
     */
    @ColumnDesc("已上传分片数")
    @TableField(value = "sys_file_system_chunk_uploaded")
    @ApiModelProperty(value = "已上传分片数")
    @Schema(description = "已上传分片数")
    private Integer sysFileSystemChunkUploaded;

    /**
     * 分片大小(字节)
     */
    @ColumnDesc("分片大小(字节)")
    @TableField(value = "sys_file_system_chunk_size")
    @ApiModelProperty(value = "分片大小(字节)")
    @Schema(description = "分片大小(字节)")
    private Long sysFileSystemChunkSize;

    /**
     * 版本号(乐观锁)
     */
    @ColumnDesc("版本号")
    @TableField(value = "sys_file_system_version")
    @ApiModelProperty(value = "版本号")
    @Schema(description = "版本号(乐观锁)")
    @Version
    private Integer sysFileSystemVersion;

    /**
     * 状态: 0-待上传, 1-上传中, 2-待合并, 3-合并中, 4-已完成, -1-处理异常
     */
    @ColumnDesc("状态")
    @TableField(value = "sys_file_system_status")
    @ApiModelProperty(value = "状态")
    @Schema(description = "状态: 0-待上传, 1-上传中, 2-待合并, 3-合并中, 4-已完成, -1-处理异常")
    private Integer sysFileSystemStatus;

    /**
     * 是否启用HTTP访问
     */
    @ColumnDesc("是否启用HTTP访问")
    @TableField(value = "sys_file_system_http_enabled")
    @ApiModelProperty(value = "是否启用HTTP访问")
    @Schema(description = "是否启用HTTP访问")
    private Boolean sysFileSystemHttpEnabled;

    /**
     * HTTP访问URL
     */
    @ColumnDesc("HTTP访问URL")
    @TableField(value = "sys_file_system_http_url")
    @ApiModelProperty(value = "HTTP访问URL")
    @Schema(description = "HTTP访问URL")
    @Size(max = 500, message = "HTTP访问URL最大长度要小于 500")
    private String sysFileSystemHttpUrl;

    /**
     * 绑定的服务ID
     */
    @ColumnDesc("绑定的服务ID")
    @TableField(value = "sys_file_system_service_id")
    @ApiModelProperty(value = "绑定的服务ID")
    @Schema(description = "绑定的服务ID")
    private Integer sysFileSystemServiceId;

    /**
     * 文件分组ID
     */
    @ColumnDesc("文件分组ID")
    @TableField(value = "sys_file_system_group_id")
    @ApiModelProperty(value = "文件分组ID")
    @Schema(description = "文件分组ID")
    private Integer sysFileSystemGroupId;

    /**
     * 上传开始时间
     */
    @ColumnDesc("上传开始时间")
    @TableField(value = "sys_file_system_upload_start_time")
    @ApiModelProperty(value = "上传开始时间")
    @Schema(description = "上传开始时间")
    private LocalDateTime sysFileSystemUploadStartTime;

    /**
     * 上传完成时间
     */
    @ColumnDesc("上传完成时间")
    @TableField(value = "sys_file_system_upload_complete_time")
    @ApiModelProperty(value = "上传完成时间")
    @Schema(description = "上传完成时间")
    private LocalDateTime sysFileSystemUploadCompleteTime;

    /**
     * 本地临时文件路径
     */
    @ColumnDesc("本地临时文件路径")
    @TableField(value = "sys_file_system_local_path")
    @ApiModelProperty(value = "本地临时文件路径")
    @Schema(description = "本地临时文件路径")
    @Size(max = 500, message = "本地临时文件路径最大长度要小于 500")
    private String sysFileSystemLocalPath;

    /**
     * 描述/摘要
     */
    @ColumnDesc("描述")
    @TableField(value = "sys_file_system_summary")
    @ApiModelProperty(value = "描述")
    @Schema(description = "描述/摘要")
    @Size(max = 500, message = "描述最大长度要小于 500")
    private String sysFileSystemSummary;

    /**
     * 文件分组名称（冗余字段，便于查询显示）
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "文件分组名称")
    @Schema(description = "文件分组名称")
    private String sysFileSystemGroupName;

    /**
     * 文件状态枚举
     */
    public static class Status {
        /**
         * 待上传
         */
        public static final int PENDING = 0;
        /**
         * 上传中
         */
        public static final int UPLOADING = 1;
        /**
         * 待合并
         */
        public static final int PENDING_MERGE = 2;
        /**
         * 合并中
         */
        public static final int MERGING = 3;
        /**
         * 已完成
         */
        public static final int COMPLETED = 4;
        /**
         * 处理异常
         */
        public static final int ERROR = -1;
    }
}
