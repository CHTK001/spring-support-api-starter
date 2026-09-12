package com.chua.starter.service.pojo.sso;

import com.chua.common.support.base.validator.group.UploadGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
* 文件上传请求
* @since 2024/12/4
* @author CH
 */
@Data
@Schema(description = "文件上传请求")
public class SysFileUploadRequest {

    /**
    * 文件类型
     */
    @Schema(description = "文件bucket")
    @NotNull(message = "文件bucket不能为空", groups = {UploadGroup.class})
    /** 文件存储桶 */
    private String fileBucket;
    /**
    * 文件名
     */
    @Schema(description = "文件名")
    /** 文件名称 */
    private String fileName;


    /**
    * 对象存储类型
     */
    @Schema(description = "OSS类型; 本地(FILESYSTEM: 默认), 七牛(QINIU)")
    /** 对象存储类型 */
    private String ossType = "FILESYSTEM";
}
