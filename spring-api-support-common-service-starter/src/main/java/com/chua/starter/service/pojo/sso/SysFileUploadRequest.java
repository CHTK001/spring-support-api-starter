package com.chua.starter.service.pojo.sso;

import com.chua.common.support.base.validator.group.UploadGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 文件上传请求
 * @author CH
 * @since 2024/12/4
 */
@Data
@Schema(description = "文件上传请求")
public class SysFileUploadRequest {

    /**
     * 文件类型
     */
    @Schema(description = "文件bucket")
    @NotNull(message = "文件bucket不能为空", groups = {UploadGroup.class})
    private String fileBucket;
    /**
     * 文件名
     */
    @Schema(description = "文件名")
    private String fileName;


    /**
     * OSS类型
     */
    @Schema(description = "OSS类型; 本地(FILESYSTEM: 默认), 七牛(QINIU)")
    private String ossType = "FILESYSTEM";
}
