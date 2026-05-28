package com.chua.starter.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 流程定义查询
 *
 * @author CH
 * @since 2026/5/28
 */
@Data
@Schema(name = "流程定义查询")
public class WfDefQuery {

    @Schema(description = "流程名称(模糊)")
    private String wfDefName;

    @Schema(description = "流程分类")
    private String wfDefCategory;

    @Schema(description = "状态(0=draft 1=published 2=disabled)")
    private Integer wfDefStatus;
}
