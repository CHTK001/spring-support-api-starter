package com.chua.starter.service.pojo.sso;

import com.chua.common.support.base.validator.group.UploadGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
* 任务创建响应
* @since 2024/12/4
* @author CH
 */
@Data
@Schema(description = "任务创建请求")
public class SysFileUploadTaskPartRequest {


    /**
    * 任务编号
     */
    @Schema(description = "任务ID")
    @NotNull(message = "任务ID不能为空", groups = {UploadGroup.class})
    /** 任务标识 */
    private String taskId;
    /**
    * 文件md5
     */
    @Schema(description = "文件md5")
    /** 文件MD5 */
    private String fileMd5;

    /**
    * 任务摘要
     */
    @Schema(description = "任务编号")
    @NotNull(message = "任务编号不能为空", groups = {UploadGroup.class})
    /** 任务SEQ */
    private Integer taskSeq;
    /**
    * 文件大小
     */
    @Schema(description = "文件大小")
    @NotNull(message = "文件大小不能为空", groups = {UploadGroup.class})
    /** 文件尺寸 */
    private long fileSize;
}
