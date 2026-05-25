package com.chua.starter.datalake.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Pipeline 创建请求
 */
@Data
@Schema(description = "Pipeline 创建请求")
public class PipelineCreateRequest {

    @Schema(description = "Pipeline ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "版本")
    private String version = "1.0.0";

    @Schema(description = "描述")
    private String description;

    @Schema(description = "节点定义", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Map<String, Object>> nodes;

    @Schema(description = "Sink 配置")
    private Map<String, Map<String, Object>> sinks;
}
