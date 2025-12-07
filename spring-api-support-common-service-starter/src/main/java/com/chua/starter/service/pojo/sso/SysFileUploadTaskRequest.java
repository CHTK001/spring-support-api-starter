package com.chua.starter.service.pojo.sso;

import com.chua.common.support.validator.group.UploadGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 任务创建响应
 * @author CH
 * @since 2024/12/4
 */
@Data
@Schema(description = "任务创建请求")
public class SysFileUploadTaskRequest {


    /**
     * 文件名称
     */
    @Schema(description = "文件名称")
    private String fileName;
    /**
     * 文件md5
     */
    @Schema(description = "文件md5")
    @NotNull(message = "文件md5不能为空", groups = {UploadGroup.class})
    private String fileMd5;

    /**
     * 文件类型
     */
    @Schema(description = "文件类型")
    @NotNull(message = "文件类型不能为空", groups = {UploadGroup.class})
    private String fileType;

    /**
     * 任务摘要
     */
    @Schema(description = "任务摘要")
    private String fileSummary;
    /**
     * 存储桶
     */
    @Schema(description = "存储桶")
    @NotNull(message = "存储桶不能为空", groups = {UploadGroup.class})
    private String fileBucket;
    /**
     * sso类型
     */
    @Schema(description = "OSS类型; 本地(FILESYSTEM: 默认), 七牛(QINIU)")
    private String ssoType = "FILESYSTEM";
    /**
     * 文件大小
     */
    @Schema(description = "文件大小")
    @NotNull(message = "文件大小不能为空", groups = {UploadGroup.class})
    private long fileSize;
}
