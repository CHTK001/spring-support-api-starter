package com.chua.starter.workflow.dto;

import com.chua.starter.workflow.entity.WfDef;
import com.chua.starter.workflow.entity.WfDefEdge;
import com.chua.starter.workflow.entity.WfDefNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 流程定义视图(含节点和边)
 *
 * @author CH
 * @since 2026/5/28
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "流程定义视图")
public class WfDefVO extends WfDef {

    @Schema(description = "节点列表")
    private List<WfDefNode> nodes;

    @Schema(description = "边列表")
    private List<WfDefEdge> edges;
}
