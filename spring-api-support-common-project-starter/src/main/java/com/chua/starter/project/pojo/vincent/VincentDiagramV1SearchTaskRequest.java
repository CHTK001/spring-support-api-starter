package com.chua.starter.project.pojo.vincent;

import com.chua.common.support.validator.group.SelectGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 查询任务请求
 *
 * @author CH
 */
@Data
@Schema(description = "请求查询任务")
public class VincentDiagramV1SearchTaskRequest {

    /**
     * 项目ID
     */
    @Schema(description = "项目ID")
    @NotNull(message = "项目ID不能为空", groups = {SelectGroup.class})
    private Integer sysProjectId;
    /**
     * 任务ID
     */
    @Schema(description = "任务ID")
    @NotBlank(message = "任务ID不能为空", groups = {SelectGroup.class})
    private String taskId;

    /**
     * 模型
     */
    private String model;
    /**
     * 地域
     */
    private String region;
}
