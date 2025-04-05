package com.chua.starter.monitor.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文件上传请求对象
 *
 * @author CH
 */
@Data
@Schema(description = "文件上传请求")
public class FileUploadRequest {

    /**
     * 维护组ID
     */
    @Schema(description = "维护组ID")
    private Long maintenanceGroupId;

    /**
     * 目标路径
     */
    @Schema(description = "目标路径")
    private String targetPath;

    /**
     * 是否覆盖
     */
    @Schema(description = "是否覆盖")
    private Boolean overwrite;

    /**
     * 如果是压缩包，是否解压
     */
    @Schema(description = "如果是压缩包，是否解压")
    private Boolean extract;
} 