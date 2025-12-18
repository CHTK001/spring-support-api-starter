package com.chua.starter.monitor.pojo.skywalking;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * SkyWalking 拓扑信息
 *
 * @author CH
 * @since 2024-12-18
 */
@Data
@ApiModel(description = "SkyWalking 拓扑信息")
@Schema(description = "SkyWalking 拓扑信息")
public class SkywalkingTopology {

    @ApiModelProperty("节点列表")
    @Schema(description = "节点列表")
    private List<Node> nodes;

    @ApiModelProperty("调用关系列表")
    @Schema(description = "调用关系列表")
    private List<Call> calls;

    /**
     * 拓扑节点
     */
    @Data
    @ApiModel(description = "拓扑节点")
    @Schema(description = "拓扑节点")
    public static class Node {
        @ApiModelProperty("节点ID")
        @Schema(description = "节点ID")
        private String id;

        @ApiModelProperty("节点名称")
        @Schema(description = "节点名称")
        private String name;

        @ApiModelProperty("节点类型")
        @Schema(description = "节点类型")
        private String type;

        @ApiModelProperty("是否真实节点")
        @Schema(description = "是否真实节点")
        private Boolean isReal;

        @ApiModelProperty("层")
        @Schema(description = "层")
        private String layer;
    }

    /**
     * 拓扑调用关系
     */
    @Data
    @ApiModel(description = "拓扑调用关系")
    @Schema(description = "拓扑调用关系")
    public static class Call {
        @ApiModelProperty("调用ID")
        @Schema(description = "调用ID")
        private String id;

        @ApiModelProperty("源节点ID")
        @Schema(description = "源节点ID")
        private String source;

        @ApiModelProperty("源节点名称")
        @Schema(description = "源节点名称")
        private String sourceName;

        @ApiModelProperty("目标节点ID")
        @Schema(description = "目标节点ID")
        private String target;

        @ApiModelProperty("目标节点名称")
        @Schema(description = "目标节点名称")
        private String targetName;

        @ApiModelProperty("检测点列表")
        @Schema(description = "检测点列表")
        private List<String> detectPoints;
    }
}
