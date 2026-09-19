package com.chua.starter.entity.service;

import com.baomidou.mybatisplus.annotation.*;
import com.chua.common.support.utils.IoUtils;
import com.chua.starter.mybatis.pojo.SysBase;
import com.google.common.io.Files;
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
 * @since 2024/12/10
 * @author CH
 */
@Schema(description = "文件系统分片表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_file_system_part")
public class SysFileSystemPart extends SysBase implements Serializable {

    @Serial
    /** 串行版本UID */
    private static final long serialVersionUID = 1L;

    /**
     * 分片标识
     */
    @TableId(value = "sys_file_system_part_id", type = IdType.AUTO)
    @Schema(description = "分片ID")
    /** SYS文件systempartid */
    private Integer sysFileSystemPartId;

    /**
     * 文件标识
     */
    @TableField(value = "sys_file_system_part_file_id")
    @Schema(description = "文件ID")
    @NotNull(message = "文件ID不能为null")
    /** SYS文件systempart文件标识 */
    private Integer sysFileSystemPartFileId;

    /**
     * 分片序号(从1开始)
     */
    @TableField(value = "sys_file_system_part_seq")
    @Schema(description = "分片序号(从1开始)")
    @NotNull(message = "分片序号不能为null")
    /** SYS文件systempartseq */
    private Integer sysFileSystemPartSeq;

    /**
     * 分片大小(字节)
     */
    @TableField(value = "sys_file_system_part_size")
    @Schema(description = "分片大小(字节)")
    /** SYS文件systempart尺寸 */
    private Long sysFileSystemPartSize;

    /**
     * 分片MD5值
     */
    @TableField(value = "sys_file_system_part_md5")
    @Schema(description = "分片MD5值")
    @Size(max = 32, message = "分片MD5值最大长度要小于 32")
    /** SYS文件systempartmd5 */
    private String sysFileSystemPartMd5;

    /**
     * 分片本地存储路径
     */
    @TableField(value = "sys_file_system_part_local_path")
    @Schema(description = "分片本地存储路径")
    @Size(max = 500, message = "分片本地存储路径最大长度要小于 500")
    /** SYS文件systempart本地路径 */
    private String sysFileSystemPartLocalPath;

    /**
     * 版本号(乐观锁)
     */
    @TableField(value = "sys_file_system_part_version")
    @Schema(description = "版本号(乐观锁)")
    @Version
    /** SYS文件systempart版本 */
    private Integer sysFileSystemPartVersion;

    /**
     * 状态: 0-待上传, 1-上传中, 2-已完成, -1-处理异常
     */
    @TableField(value = "sys_file_system_part_status")
    @Schema(description = "状态: 0-待上传, 1-上传中, 2-已完成, -1-处理异常")
    /** SYS文件systempart状态 */
    private Integer sysFileSystemPartStatus;

    /**
     * 上传时间
     */
    @TableField(value = "sys_file_system_part_upload_time")
    @Schema(description = "上传时间")
    /** SYS文件systempart上传时间 */
    private LocalDateTime sysFileSystemPartUploadTime;

    /**
     * 处理时间
     */
    @TableField(value = "sys_file_system_part_process_time")
    @Schema(description = "处理时间")
    /** SYS文件systempartprocess时间 */
    private LocalDateTime sysFileSystemPartProcessTime;

    /**
     * 错误信息
     */
    @TableField(value = "sys_file_system_part_error_message")
    @Schema(description = "错误信息")
    @Size(max = 1000, message = "错误信息最大长度要小于 1000")
    /** SYS文件systempart错误消息 */
    private String sysFileSystemPartErrorMessage;

    /**
     * 重试次数
     */
    @TableField(value = "sys_file_system_part_retry_count")
    @Schema(description = "重试次数")
    /** SYS文件systempart重试数量 */
    private Integer sysFileSystemPartRetryCount;

    /**
     * 从上传文件传输到本地路径
     *
     * @param multipartFile 上传文件
     * @throws Exception 传输异常
     */
    public void transferFrom(MultipartFile multipartFile) throws Exception {
        File file = new File(sysFileSystemPartLocalPath);
        file.getParentFile().mkdirs();
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
         * 已完成
         */
        public static final int COMPLETED = 2;
        /**
         * 处理异常
         */
        public static final int ERROR = -1;
    }
}
