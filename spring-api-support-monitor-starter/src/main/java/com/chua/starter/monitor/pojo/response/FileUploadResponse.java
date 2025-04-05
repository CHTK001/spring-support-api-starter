package com.chua.starter.monitor.pojo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文件上传响应对象
 *
 * @author CH
 */
@Data
@Schema(description = "文件上传响应")
public class FileUploadResponse {

    /**
     * 任务ID
     */
    @Schema(description = "任务ID")
    private String taskId;
    
    /**
     * 上传状态
     */
    @Schema(description = "上传状态")
    private String status;

    /**
     * 成功上传文件数
     */
    @Schema(description = "成功上传文件数")
    private Integer successCount;
    
    /**
     * 失败上传文件数
     */
    @Schema(description = "失败上传文件数")
    private Integer failCount;
    
    /**
     * 总文件数
     */
    @Schema(description = "总文件数")
    private Integer totalCount;
    
    /**
     * 详细信息
     */
    @Schema(description = "详细信息")
    private String message;
} 