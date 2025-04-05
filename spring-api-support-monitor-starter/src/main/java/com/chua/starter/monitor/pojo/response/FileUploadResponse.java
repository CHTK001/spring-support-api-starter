package com.chua.starter.monitor.pojo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 文件上传响应对象
 *
 * @author CH
 */
@Data
@Schema(description = "文件上传响应")
public class FileUploadResponse {

    /**
     * 上传结果，键为主机地址，值为上传结果
     */
    @Schema(description = "上传结果，键为主机地址，值为上传结果")
    private Map<String, String> results;

    /**
     * 是否全部成功
     */
    @Schema(description = "是否全部成功")
    private Boolean allSuccess;
} 