package com.chua.starter.monitor.pojo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 文件部署响应对象
 *
 * @author CH
 */
@Data
@Schema(description = "文件部署响应")
public class FileDeployResponse {

    /**
     * 任务ID
     */
    @Schema(description = "任务ID")
    private String taskId;
    
    /**
     * 部署状态
     */
    @Schema(description = "部署状态")
    private String status;
    
    /**
     * 成功部署主机数
     */
    @Schema(description = "成功部署主机数")
    private Integer successCount;
    
    /**
     * 失败部署主机数
     */
    @Schema(description = "失败部署主机数")
    private Integer failCount;
    
    /**
     * 总主机数
     */
    @Schema(description = "总主机数")
    private Integer totalCount;
    
    /**
     * 部署结果详情，键为主机地址，值为部署结果
     */
    @Schema(description = "部署结果详情，键为主机地址，值为部署结果")
    private Map<String, String> details;
    
    /**
     * 详细信息
     */
    @Schema(description = "详细信息")
    private String message;
} 