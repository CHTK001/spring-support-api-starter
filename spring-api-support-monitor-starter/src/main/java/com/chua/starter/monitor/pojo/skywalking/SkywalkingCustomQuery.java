package com.chua.starter.monitor.pojo.skywalking;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * SkyWalking 自定义 GraphQL 查询请求
 *
 * @author CH
 * @since 2024-12-28
 */
@Data
@ApiModel(description = "SkyWalking 自定义 GraphQL 查询请求")
@Schema(description = "SkyWalking 自定义 GraphQL 查询请求")
public class SkywalkingCustomQuery {

    @ApiModelProperty(value = "SkyWalking 配置ID", required = true)
    @Schema(description = "SkyWalking 配置ID", required = true)
    private Integer configId;

    @ApiModelProperty(value = "GraphQL 查询语句", required = true, example = "{ listLayers }")
    @Schema(description = "GraphQL 查询语句", required = true, example = "{ listLayers }")
    private String query;

    @ApiModelProperty(value = "GraphQL 变量", example = "{\"serviceId\": \"xxx\"}")
    @Schema(description = "GraphQL 变量")
    private Map<String, Object> variables;

    @ApiModelProperty(value = "查询名称（可选，用于日志记录）")
    @Schema(description = "查询名称（可选，用于日志记录）")
    private String queryName;
}
