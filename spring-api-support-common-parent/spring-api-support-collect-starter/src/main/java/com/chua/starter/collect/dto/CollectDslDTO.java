package com.chua.starter.collect.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 流程图 DSL。
 *
 * <p>序列化后存入 collect_flow.collect_flow_dsl，是可视化设计器与执行引擎之间的唯一契约。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Data
@Schema(description = "流程图DSL")
public class CollectDslDTO {

    /**
     * 流程名称，与流程表冗余一致，以 DSL 为准展示在设计器标题。
     */
    @Schema(description = "流程名称")
    private String name;

    /**
     * 流程内并行线程数，0 或空取平台默认。
     */
    @Schema(description = "并行线程数")
    private Integer threadCount;

    /**
     * 全局请求头，所有抓取节点共享。
     */
    @Schema(description = "全局请求头")
    private List<CollectParamDTO> headers = new ArrayList<>();

    /**
     * 全局 Cookie，所有抓取节点共享。
     */
    @Schema(description = "全局Cookie")
    private List<CollectParamDTO> cookies = new ArrayList<>();

    /**
     * 全局变量，执行前注入变量上下文。
     */
    @Schema(description = "全局变量")
    private List<CollectParamDTO> variables = new ArrayList<>();

    /**
     * 节点集合。
     */
    @Schema(description = "节点集合")
    private List<CollectNodeDTO> nodes = new ArrayList<>();

    /**
     * 连线集合。
     */
    @Schema(description = "连线集合")
    private List<CollectEdgeDTO> edges = new ArrayList<>();
}
