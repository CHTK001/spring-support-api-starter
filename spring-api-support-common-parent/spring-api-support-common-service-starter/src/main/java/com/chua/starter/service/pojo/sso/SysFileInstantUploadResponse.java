package com.chua.starter.service.pojo.sso;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 秒传检查响应
 *
 * @since 2024/12/20
 * @author CH
 */
@Schema(description = "秒传检查响应")
public record SysFileInstantUploadResponse(
        /**
         * 是否可以秒传
         */
        @Schema(description = "是否可以秒传")
        /**
         * Instant上传
         */
        Boolean instantUpload,
        /**
         * 已存在的文件标识
         */
        @Schema(description = "已存在的文件ID")
        /**
         * Existing文件标识
         */
        Integer existingFileId,
        /**
         * 已存在的文件访问URL
         */
        @Schema(description = "已存在的文件访问URL")
        /**
         * Existing文件URL
         */
        String existingFileUrl,
        /**
         * 已存在的文件HTTP访问URL
         */
        @Schema(description = "已存在的文件HTTP访问URL")
        /**
         * Existing文件HTTPURL
         */
        String existingFileHttpUrl,
        /**
         * 已存在的文件名
         */
        @Schema(description = "已存在的文件名")
        /**
         * Existing文件名称
         */
        String existingFileName
) {

    /**
     * 创建不支持秒传的响应
     *
     * @return 响应对象
     */
    public static SysFileInstantUploadResponse notSupport() {
        return new SysFileInstantUploadResponse(false, null, null, null, null);
    }

    /**
     * 创建支持秒传的响应
     *
     * @param fileId      文件标识
     * @param fileUrl     文件URL
     * @param fileHttpUrl 文件HTTP URL
     * @param fileName    文件名
     * @return 响应对象
     */
    public static SysFileInstantUploadResponse support(Integer fileId, String fileUrl, String fileHttpUrl, String fileName) {
        return new SysFileInstantUploadResponse(true, fileId, fileUrl, fileHttpUrl, fileName);
    }
}
