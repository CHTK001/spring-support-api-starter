package com.chua.starter.collect.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 流程节点。
 *
 * <p>坐标 x/y 仅服务于可视化布局，执行引擎不读取。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Data
@Schema(description = "流程节点")
public class CollectNodeDTO {

    /**
     * 节点ID，流程内唯一。
     */
    @Schema(description = "节点ID")
    private String id;

    /**
     * 节点类型，对应执行器 supportShape。
     */
    @Schema(description = "节点类型")
    private String shape;

    /**
     * 节点显示名称。
     */
    @Schema(description = "节点名称")
    private String name;

    /**
     * 画布横坐标。
     */
    @Schema(description = "画布横坐标")
    private Integer x;

    /**
     * 画布纵坐标。
     */
    @Schema(description = "画布纵坐标")
    private Integer y;

    /**
     * 节点参数。
     */
    @Schema(description = "节点参数")
    private List<CollectParamDTO> params = new ArrayList<>();

    /**
     * 抽取字段声明，仅抽取节点使用。
     */
    @Schema(description = "抽取字段声明")
    private List<CollectExtractFieldDTO> fields = new ArrayList<>();
}
