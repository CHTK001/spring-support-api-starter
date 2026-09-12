package com.chua.starter.service.pojo.sso;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 秒传检查请求
 *
 * @since 2024/12/20
 * @author CH
 */
@Data
@Schema(description = "秒传检查请求")
public class SysFileInstantUploadRequest {

    /**
     * 文件MD5
     */
    @Schema(description = "文件MD5")
    @NotBlank(message = "文件MD5不能为空")
    /** 文件MD5 */
    private String fileMd5;

    /**
    * 文件大小（字节）
     */
    @Schema(description = "文件大小（字节）")
    /** 文件尺寸 */
    private Long fileSize;

    /**
    * 原始文件名
     */
    @Schema(description = "原始文件名")
    /** 文件名称 */
    private String fileName;
}
