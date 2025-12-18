package com.chua.starter.monitor.pojo.skywalking;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * SkyWalking 服务信息
 *
 * @author CH
 * @since 2024-12-18
 */
@Data
@ApiModel(description = "SkyWalking 服务信息")
@Schema(description = "SkyWalking 服务信息")
public class SkywalkingServiceInfo {

    @ApiModelProperty("服务ID")
    @Schema(description = "服务ID")
    private String id;

    @ApiModelProperty("服务名称")
    @Schema(description = "服务名称")
    private String name;

    @ApiModelProperty("服务组")
    @Schema(description = "服务组")
    private String group;

    @ApiModelProperty("服务短名称")
    @Schema(description = "服务短名称")
    private String shortName;

    @ApiModelProperty("层")
    @Schema(description = "层")
    private String layer;

    @ApiModelProperty("是否正常服务")
    @Schema(description = "是否正常服务")
    private Boolean normal;

    /**
     * 服务实例信息
     */
    @Data
    @ApiModel(description = "服务实例信息")
    @Schema(description = "服务实例信息")
    public static class Instance {
        @ApiModelProperty("实例ID")
        @Schema(description = "实例ID")
        private String id;

        @ApiModelProperty("实例名称")
        @Schema(description = "实例名称")
        private String name;

        @ApiModelProperty("语言")
        @Schema(description = "语言")
        private String language;

        @ApiModelProperty("实例属性")
        @Schema(description = "实例属性")
        private List<Attribute> attributes;
    }

    /**
     * 端点信息
     */
    @Data
    @ApiModel(description = "端点信息")
    @Schema(description = "端点信息")
    public static class Endpoint {
        @ApiModelProperty("端点ID")
        @Schema(description = "端点ID")
        private String id;

        @ApiModelProperty("端点名称")
        @Schema(description = "端点名称")
        private String name;
    }

    /**
     * 属性信息
     */
    @Data
    @ApiModel(description = "属性信息")
    @Schema(description = "属性信息")
    public static class Attribute {
        @ApiModelProperty("属性名称")
        @Schema(description = "属性名称")
        private String name;

        @ApiModelProperty("属性值")
        @Schema(description = "属性值")
        private String value;
    }
}
