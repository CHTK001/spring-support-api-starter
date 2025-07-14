package com.chua.starter.monitor.starter.entity;

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
 * 文件系统分片管理
 *
 * @author CH
 * @since 2024/12/30
 */
@ApiModel(description = "文件系统分片管理")
@Schema(description = "文件系统分片管理")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "file_system_part")
public class FileSystemPart extends SysBase implements Serializable {

    /**
     * 分片ID
     */
    @TableId(value = "file_system_part_id", type = IdType.AUTO)
    @ApiModelProperty(value = "分片ID")
    @Schema(description = "分片ID")
    @NotNull(message = "分片ID不能为null")
    private Integer fileSystemPartId;

    /**
     * 文件ID
     */
    @ColumnDesc("文件ID")
    @TableField(value = "file_system_part_file_id")
    @ApiModelProperty(value = "文件ID")
    @Schema(description = "文件ID")
    @NotNull(message = "文件ID不能为null")
    private Integer fileSystemPartFileId;

    /**
     * 分片序号(从0开始)
     */
    @ColumnDesc("分片序号(从0开始)")
    @TableField(value = "file_system_part_chunk_number")
    @ApiModelProperty(value = "分片序号(从0开始)")
    @Schema(description = "分片序号(从0开始)")
    @NotNull(message = "分片序号不能为null")
    private Integer fileSystemPartChunkNumber;

    /**
     * 分片大小(字节)
     */
    @ColumnDesc("分片大小(字节)")
    @TableField(value = "file_system_part_chunk_size")
    @ApiModelProperty(value = "分片大小(字节)")
    @Schema(description = "分片大小(字节)")
    private Long fileSystemPartChunkSize;

    /**
     * 分片MD5值
     */
    @ColumnDesc("分片MD5值")
    @TableField(value = "file_system_part_chunk_md5")
    @ApiModelProperty(value = "分片MD5值")
    @Schema(description = "分片MD5值")
    @Size(max = 32, message = "分片MD5值最大长度要小于 32")
    private String fileSystemPartChunkMd5;

    /**
     * 分片文件路径
     */
    @ColumnDesc("分片文件路径")
    @TableField(value = "file_system_part_chunk_path")
    @ApiModelProperty(value = "分片文件路径")
    @Schema(description = "分片文件路径")
    @Size(max = 500, message = "分片文件路径最大长度要小于 500")
    private String fileSystemPartChunkPath;

    /**
     * 分片状态：0-待处理，1-处理中，2-已完成，3-处理失败
     */
    @ColumnDesc("分片状态：0-待处理，1-处理中，2-已完成，3-处理失败")
    @TableField(value = "file_system_part_chunk_status")
    @ApiModelProperty(value = "分片状态：0-待处理，1-处理中，2-已完成，3-处理失败")
    @Schema(description = "分片状态：0-待处理，1-处理中，2-已完成，3-处理失败")
    private Integer fileSystemPartChunkStatus;

    /**
     * 上传时间
     */
    @ColumnDesc("上传时间")
    @TableField(value = "file_system_part_upload_time")
    @ApiModelProperty(value = "上传时间")
    @Schema(description = "上传时间")
    private LocalDateTime fileSystemPartUploadTime;

    /**
     * 处理时间
     */
    @ColumnDesc("处理时间")
    @TableField(value = "file_system_part_process_time")
    @ApiModelProperty(value = "处理时间")
    @Schema(description = "处理时间")
    private LocalDateTime fileSystemPartProcessTime;

    /**
     * 错误信息
     */
    @ColumnDesc("错误信息")
    @TableField(value = "file_system_part_error_message")
    @ApiModelProperty(value = "错误信息")
    @Schema(description = "错误信息")
    @Size(max = 1000, message = "错误信息最大长度要小于 1000")
    private String fileSystemPartErrorMessage;

    /**
     * 重试次数
     */
    @ColumnDesc("重试次数")
    @TableField(value = "file_system_part_retry_count")
    @ApiModelProperty(value = "重试次数")
    @Schema(description = "重试次数")
    private Integer fileSystemPartRetryCount;

    private static final long serialVersionUID = 1L;

    /**
     * 分片状态枚举
     */
    public static class ChunkStatus {
        public static final int PENDING = 0; // 待处理
        public static final int PROCESSING = 1; // 处理中
        public static final int COMPLETED = 2; // 已完成
        public static final int FAILED = 3; // 处理失败
    }
}
