package com.chua.starter.service.pojo.sso;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 秒传检查响应
 *
 * @author CH
 * @since 2024/12/20
 */
@Data
@Schema(description = "秒传检查响应")
public class SysFileInstantUploadResponse {

    /**
     * 是否可以秒传
     */
    @Schema(description = "是否可以秒传")
    private Boolean instantUpload;

    /**
     * 已存在的文件ID
     */
    @Schema(description = "已存在的文件ID")
    private Integer existingFileId;

    /**
     * 已存在的文件访问URL
     */
    @Schema(description = "已存在的文件访问URL")
    private String existingFileUrl;

    /**
     * 已存在的文件HTTP访问URL
     */
    @Schema(description = "已存在的文件HTTP访问URL")
    private String existingFileHttpUrl;

    /**
     * 已存在的文件名
     */
    @Schema(description = "已存在的文件名")
    private String existingFileName;

    /**
     * 创建不支持秒传的响应
     *
     * @return 响应对象
     */
    public static SysFileInstantUploadResponse notSupport() {
        SysFileInstantUploadResponse response = new SysFileInstantUploadResponse();
        response.setInstantUpload(false);
        return response;
    }

    /**
     * 创建支持秒传的响应
     *
     * @param fileId      文件ID
     * @param fileUrl     文件URL
     * @param fileHttpUrl 文件HTTP URL
     * @param fileName    文件名
     * @return 响应对象
     */
    public static SysFileInstantUploadResponse support(Integer fileId, String fileUrl, String fileHttpUrl, String fileName) {
        SysFileInstantUploadResponse response = new SysFileInstantUploadResponse();
        response.setInstantUpload(true);
        response.setExistingFileId(fileId);
        response.setExistingFileUrl(fileUrl);
        response.setExistingFileHttpUrl(fileHttpUrl);
        response.setExistingFileName(fileName);
        return response;
    }
}
