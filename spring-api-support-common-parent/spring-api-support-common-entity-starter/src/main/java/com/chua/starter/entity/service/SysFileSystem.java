package com.chua.starter.entity.service;

import com.baomidou.mybatisplus.annotation.*;
import com.chua.starter.mybatis.pojo.SysBase;
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
 * @since 2024/12/10
 * @版本 1.0.0
 * @author CH
 */
@Schema(description = "文件系统管理表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_file_system")
public class SysFileSystem extends SysBase implements Serializable {

    @Serial
    /** 串行版本UID */
    private static final long serialVersionUID = 1L;

    /**
    * 主键标识
     */
    @TableId(value = "sys_file_system_id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    /** SYS文件系统id */
    private Integer sysFileSystemId;

    /**
    * 任务唯一标识（ULID）
     */
    @TableField(value = "sys_file_system_tid")
    @Schema(description = "任务唯一标识（ULID）")
    @Size(max = 64, message = "任务唯一标识最大长度要小于 64")
    /** SYS文件系统tid */
    private String sysFileSystemTid;

    /**
    * 存储桶/存储位置
     */
    @TableField(value = "sys_file_system_bucket")
    @Schema(description = "存储桶/存储位置")
    @Size(max = 255, message = "存储桶最大长度要小于 255")
    /** SYS文件系统存储桶 */
    private String sysFileSystemBucket;

    /**
    * 文件名称
     */
    @TableField(value = "sys_file_system_name")
    @Schema(description = "文件名称")
    @Size(max = 255, message = "文件名称最大长度要小于 255")
    /** SYS文件系统名称 */
    private String sysFileSystemName;

    /**
    * 文件原始名称
     */
    @TableField(value = "sys_file_system_original_name")
    @Schema(description = "文件原始名称")
    @Size(max = 255, message = "文件原始名称最大长度要小于 255")
    /** SYS文件systemoriginal名称 */
    private String sysFileSystemOriginalName;

    /**
    * 存储类型：本地-本地存储, 对象存储-阿里云对象存储, COS-腾讯云COS等
     */
    @TableField(value = "sys_file_system_type")
    @Schema(description = "存储类型：local-本地存储, oss-阿里云OSS, cos-腾讯云COS等")
    @Size(max = 50, message = "存储类型最大长度要小于 50")
    /** SYS文件系统类型 */
    private String sysFileSystemType;

    /**
    * 文件扩展名
     */
    @TableField(value = "sys_file_system_extension")
    @Schema(description = "文件扩展名")
    @Size(max = 20, message = "文件扩展名最大长度要小于 20")
    /** SYS文件systemextension */
    private String sysFileSystemExtension;

    /**
    * 文件路径
     */
    @TableField(value = "sys_file_system_path")
    @Schema(description = "文件路径")
    @Size(max = 500, message = "文件路径最大长度要小于 500")
    /** SYS文件系统路径 */
    private String sysFileSystemPath;

    /**
    * 文件大小(字节)
     */
    @TableField(value = "sys_file_system_size")
    @Schema(description = "文件大小(字节)")
    /** SYS文件系统尺寸 */
    private Long sysFileSystemSize;

    /**
    * 文件MD5值
     */
    @TableField(value = "sys_file_system_md5")
    @Schema(description = "文件MD5值")
    @Size(max = 32, message = "文件MD5值最大长度要小于 32")
    /** SYS文件系统md5 */
    private String sysFileSystemMd5;

    /**
    * 分片数量
     */
    @TableField(value = "sys_file_system_chunk_total")
    @Schema(description = "分片数量")
    /** SYS文件systemchunk总数 */
    private Integer sysFileSystemChunkTotal;

    /**
    * 已上传分片数
     */
    @TableField(value = "sys_file_system_chunk_uploaded")
    @Schema(description = "已上传分片数")
    /** SYS文件systemchunkuploaded */
    private Integer sysFileSystemChunkUploaded;

    /**
    * 分片大小(字节)
     */
    @TableField(value = "sys_file_system_chunk_size")
    @Schema(description = "分片大小(字节)")
    /** SYS文件systemchunk尺寸 */
    private Long sysFileSystemChunkSize;

    /**
    * 版本号(乐观锁)
     */
    @TableField(value = "sys_file_system_version")
    @Schema(description = "版本号(乐观锁)")
    @Version
    /** SYS文件系统版本 */
    private Integer sysFileSystemVersion;

    /**
    * 状态: 0-待上传, 1-上传中, 2-待合并, 3-合并中, 4-已完成, -1-处理异常
     */
    @TableField(value = "sys_file_system_status")
    @Schema(description = "状态: 0-待上传, 1-上传中, 2-待合并, 3-合并中, 4-已完成, -1-处理异常")
    /** SYS文件系统状态 */
    private Integer sysFileSystemStatus;

    /**
    * 是否启用HTTP访问
     */
    @TableField(value = "sys_file_system_http_enabled")
    @Schema(description = "是否启用HTTP访问")
    /** SYS文件系统http是否启用 */
    private Boolean sysFileSystemHttpEnabled;

    /**
    * HTTP访问URL
     */
    @TableField(value = "sys_file_system_http_url")
    @Schema(description = "HTTP访问URL")
    @Size(max = 500, message = "HTTP访问URL最大长度要小于 500")
    /** SYS文件系统httpurl */
    private String sysFileSystemHttpUrl;

    /**
    * 绑定的服务标识
     */
    @TableField(value = "sys_file_system_service_id")
    @Schema(description = "绑定的服务ID")
    /** SYS文件系统服务标识 */
    private Integer sysFileSystemServiceId;

    /**
    * 文件分组标识
     */
    @TableField(value = "sys_file_system_group_id")
    @Schema(description = "文件分组ID")
    /** SYS文件系统分组标识 */
    private Integer sysFileSystemGroupId;

    /**
    * 上传开始时间
     */
    @TableField(value = "sys_file_system_upload_start_time")
    @Schema(description = "上传开始时间")
    /** SYS文件系统上传开始时间 */
    private LocalDateTime sysFileSystemUploadStartTime;

    /**
    * 上传完成时间
     */
    @TableField(value = "sys_file_system_upload_complete_time")
    @Schema(description = "上传完成时间")
    /** SYS文件系统上传完成时间 */
    private LocalDateTime sysFileSystemUploadCompleteTime;

    /**
    * 本地临时文件路径
     */
    @TableField(value = "sys_file_system_local_path")
    @Schema(description = "本地临时文件路径")
    @Size(max = 500, message = "本地临时文件路径最大长度要小于 500")
    /** SYS文件系统本地路径 */
    private String sysFileSystemLocalPath;

    /**
    * 描述/摘要
     */
    @TableField(value = "sys_file_system_summary")
    @Schema(description = "描述/摘要")
    @Size(max = 500, message = "描述最大长度要小于 500")
    /** SYS文件systemsummary */
    private String sysFileSystemSummary;

    /**
    * 文件分组名称（冗余字段，便于查询显示）
     */
    @TableField(exist = false)
    @Schema(description = "文件分组名称")
    /** SYS文件系统分组名称 */
    private String sysFileSystemGroupName;

    /**
    * 文件状态枚举
    * @author CH
    * @since 4.0.0
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
