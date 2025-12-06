package com.chua.starter.project.pojo.vincent;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 超分辨率任务查询
 *
 * @author CH
 */
@Data
@Schema(description = "超分辨率任务查询")
public class ImageResolutionSearchTaskV2Request {

    /**
     * 项目ID
     */
    @Schema(description = "项目ID")
    private Integer sysProjectId;
    /**
     * 任务ID
     */
    @Schema(description = "任务ID")
    private String taskId;
    /**
     * 模型
     */
    @Schema(description = "模型")
    private String model;
    /**
     * 地域
     */
    @Schema(description = "地域")
    private String region;
}
