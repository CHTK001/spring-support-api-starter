package com.chua.starter.service.pojo.sso;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.Objects;

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

    /**
     * 规范构造器：文件MD5 空值敌对。
     *
     * <p>value class 前置条件——语义必填的引用组件不允许为 null。{@code fileMd5} 已由
     * {@link NotBlank} 声明为必填，此处校验是其子集；{@code fileSize} 与
     * {@code fileName} 未声明必填，可能缺省，故不做校验。</p>
     *
     * @param fileMd5  文件MD5
     * @param fileSize 文件大小（字节）
     * @param fileName 原始文件名
     */
    public SysFileInstantUploadRequest {
        fileMd5 = Objects.requireNonNull(fileMd5, "fileMd5 不能为 null");
    }
}
