package com.chua.starter.entity.service;

import com.baomidou.mybatisplus.annotation.*;
import com.chua.common.support.datasource.annotation.ColumnDesc;
import com.chua.common.support.utils.FileUtils;
import com.chua.common.support.utils.IoUtils;
import com.chua.starter.mybatis.pojo.SysBase;
import com.google.common.io.Files;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件系统分片表
 * 用于管理文件分片上传的详细信息
 *
 * @author CH
 * @since 2024/12/10
 * @version 1.0.0
 */
@ApiModel(description = "文件系统分片表")
@Schema(description = "文件系统分片表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_file_system_part")
public class SysFileSystemPart extends SysBase implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 分片ID
     */
    @TableId(value = "sys_file_system_part_id", type = IdType.AUTO)
    @ApiModelProperty(value = "分片ID")
    @Schema(description = "分片ID")
    private Integer sysFileSystemPartId;

    /**
     * 文件ID
     */
    @ColumnDesc("文件ID")
    @TableField(value = "sys_file_system_part_file_id")
    @ApiModelProperty(value = "文件ID")
    @Schema(description = "文件ID")
    @NotNull(message = "文件ID不能为null")
    private Integer sysFileSystemPartFileId;

    /**
     * 分片序号(从1开始)
     */
    @ColumnDesc("分片序号")
    @TableField(value = "sys_file_system_part_seq")
    @ApiModelProperty(value = "分片序号")
    @Schema(description = "分片序号(从1开始)")
    @NotNull(message = "分片序号不能为null")
    private Integer sysFileSystemPartSeq;

    /**
     * 分片大小(字节)
     */
    @ColumnDesc("分片大小(字节)")
    @TableField(value = "sys_file_system_part_size")
    @ApiModelProperty(value = "分片大小(字节)")
    @Schema(description = "分片大小(字节)")
    private Long sysFileSystemPartSize;

    /**
     * 分片MD5值
     */
    @ColumnDesc("分片MD5值")
    @TableField(value = "sys_file_system_part_md5")
    @ApiModelProperty(value = "分片MD5值")
    @Schema(description = "分片MD5值")
    @Size(max = 32, message = "分片MD5值最大长度要小于 32")
    private String sysFileSystemPartMd5;

    /**
     * 分片本地存储路径
     */
    @ColumnDesc("分片本地存储路径")
    @TableField(value = "sys_file_system_part_local_path")
    @ApiModelProperty(value = "分片本地存储路径")
    @Schema(description = "分片本地存储路径")
    @Size(max = 500, message = "分片本地存储路径最大长度要小于 500")
    private String sysFileSystemPartLocalPath;

    /**
     * 版本号(乐观锁)
     */
    @ColumnDesc("版本号")
    @TableField(value = "sys_file_system_part_version")
    @ApiModelProperty(value = "版本号")
    @Schema(description = "版本号(乐观锁)")
    @Version
    private Integer sysFileSystemPartVersion;

    /**
     * 状态: 0-待上传, 1-上传中, 2-已完成, -1-处理异常
     */
    @ColumnDesc("状态")
    @TableField(value = "sys_file_system_part_status")
    @ApiModelProperty(value = "状态")
    @Schema(description = "状态: 0-待上传, 1-上传中, 2-已完成, -1-处理异常")
    private Integer sysFileSystemPartStatus;

    /**
     * 上传时间
     */
    @ColumnDesc("上传时间")
    @TableField(value = "sys_file_system_part_upload_time")
    @ApiModelProperty(value = "上传时间")
    @Schema(description = "上传时间")
    private LocalDateTime sysFileSystemPartUploadTime;

    /**
     * 处理时间
     */
    @ColumnDesc("处理时间")
    @TableField(value = "sys_file_system_part_process_time")
    @ApiModelProperty(value = "处理时间")
    @Schema(description = "处理时间")
    private LocalDateTime sysFileSystemPartProcessTime;

    /**
     * 错误信息
     */
    @ColumnDesc("错误信息")
    @TableField(value = "sys_file_system_part_error_message")
    @ApiModelProperty(value = "错误信息")
    @Schema(description = "错误信息")
    @Size(max = 1000, message = "错误信息最大长度要小于 1000")
    private String sysFileSystemPartErrorMessage;

    /**
     * 重试次数
     */
    @ColumnDesc("重试次数")
    @TableField(value = "sys_file_system_part_retry_count")
    @ApiModelProperty(value = "重试次数")
    @Schema(description = "重试次数")
    private Integer sysFileSystemPartRetryCount;

    /**
     * 从上传文件传输到本地路径
     *
     * @param multipartFile 上传文件
     * @throws Exception 传输异常
     */
    public void transferFrom(MultipartFile multipartFile) throws Exception {
        File file = new File(sysFileSystemPartLocalPath);
        FileUtils.forceMkdirParent(file);
        try (InputStream inputStream = multipartFile.getInputStream();
             FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            IoUtils.copy(inputStream, fileOutputStream);
        }
    }

    /**
     * 拷贝文件字节数组
     *
     * @return 文件字节数组
     * @throws IOException 读取异常
     */
    public byte[] copyByteArray() throws IOException {
        return Files.toByteArray(new File(sysFileSystemPartLocalPath));
    }

    /**
     * 分片状态枚举
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
         * 已完成
         */
        public static final int COMPLETED = 2;
        /**
         * 处理异常
         */
        public static final int ERROR = -1;
    }
}
