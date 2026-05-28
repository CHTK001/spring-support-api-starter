package com.chua.starter.workflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 工作流边/连线定义
 *
 * @author CH
 * @since 2026/5/28
 */
@Data
@Schema(description = "工作流边/连线定义")
@TableName("wf_def_edge")
public class WfDefEdge implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "wf_def_edge_id", type = IdType.AUTO)
    @Schema(description = "连线ID")
    private Integer wfDefEdgeId;

    @TableField("wf_def_id")
    @Schema(description = "流程定义ID")
    private Integer wfDefId;

    @TableField("edge_name")
    @Schema(description = "连线名称")
    private String edgeName;

    @TableField("source_node_id")
    @Schema(description = "源节点ID(WfDefNode表)")
    private Integer sourceNodeId;

    @TableField("target_node_id")
    @Schema(description = "目标节点ID(WfDefNode表)")
    private Integer targetNodeId;

    @TableField("edge_condition")
    @Schema(description = "条件表达式(动态JSON)")
    private String edgeCondition;
}
