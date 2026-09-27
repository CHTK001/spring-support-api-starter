package com.chua.starter.collect.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 流程连线。
 *
 * <p>条件与异常流向挂在边上而非节点上，与 spider-flow 的
 * condition/exception-flow/transmit-variable 三件套语义一致。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Data
@Schema(description = "流程连线")
public class CollectEdgeDTO {

    /**
     * 连线ID。
     */
    @Schema(description = "连线ID")
    private String id;

    /**
     * 源节点ID。
     */
    @Schema(description = "源节点ID")
    private String from;

    /**
     * 目标节点ID。
     */
    @Schema(description = "目标节点ID")
    private String to;

    /**
     * 流转条件表达式，空表示无条件流转。
     */
    @Schema(description = "流转条件表达式")
    private String condition;

    /**
     * 异常流向：0 总是流转、1 仅上游异常时流转、2 仅上游正常时流转。
     *
     * <p>取值语义以 {@link com.chua.starter.collect.engine.model.CollectEdge} 的
     * {@code FLOW_ALWAYS / FLOW_EXCEPTION / FLOW_NORMAL} 常量为准；留空按 0 处理。</p>
     */
    @Schema(description = "异常流向 0总是流转 1仅异常时流转 2仅正常时流转")
    private String exceptionFlow;

    /**
     * 是否把上游变量传递给下游。
     */
    @Schema(description = "是否传递上游变量")
    private Boolean transmitVariable;
}
