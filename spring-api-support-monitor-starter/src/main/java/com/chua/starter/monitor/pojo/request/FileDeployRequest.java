package com.chua.starter.monitor.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文件部署请求对象
 *
 * @author CH
 */
@Data
@Schema(description = "文件部署请求")
public class FileDeployRequest {

    /**
     * 文件ID
     */
    @Schema(description = "文件ID")
    private Long maintenanceFileId;

    /**
     * 维护组ID
     */
    @Schema(description = "维护组ID")
    private Long maintenanceGroupId;
    
    /**
     * 部署路径
     */
    @Schema(description = "部署路径")
    private String deployPath;
    
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
    
    /**
     * 部署后是否执行
     */
    @Schema(description = "部署后是否执行")
    private Boolean executeAfterDeploy;
    
    /**
     * 执行参数
     */
    @Schema(description = "执行参数")
    private String executeParams;
} 