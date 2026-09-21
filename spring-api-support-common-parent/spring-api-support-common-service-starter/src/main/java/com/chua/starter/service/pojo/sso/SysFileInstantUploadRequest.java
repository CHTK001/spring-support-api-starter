package com.chua.starter.service.pojo.sso;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 秒传检查请求
 *
 * @param fileMd5 文件MD5
 * @param fileSize 文件大小（字节）
 * @param fileName 原始文件名
 *
 * @since 2024/12/20
 * @author CH
 */
@Schema(description = "秒传检查请求")
public record SysFileInstantUploadRequest(
        @Schema(description = "文件MD5") @NotBlank(message = "文件MD5不能为空") String fileMd5,
        @Schema(description = "文件大小（字节）") Long fileSize,
        @Schema(description = "原始文件名") String fileName
) {
}
