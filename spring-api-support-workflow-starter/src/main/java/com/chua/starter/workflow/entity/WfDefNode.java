package com.chua.starter.workflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 工作流节点定义
 *
 * @author CH
 * @since 2026/5/28
 */
@Data
@Schema(description = "工作流节点定义")
@TableName("wf_def_node")
public class WfDefNode implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "wf_def_node_id", type = IdType.AUTO)
    @Schema(description = "节点ID")
    private Integer wfDefNodeId;

    @TableField("wf_def_id")
    @Schema(description = "流程定义ID")
    private Integer wfDefId;

    @TableField("node_name")
    @Schema(description = "节点名称")
    private String nodeName;

    @TableField("node_type")
    @Schema(description = "节点类型: START/END/APPROVAL/CC/CONDITION")
    private String nodeType;

    @TableField("node_config")
    @Schema(description = "节点动态配置(JSON)")
    private String nodeConfig;

    @TableField("assignee_type")
    @Schema(description = "分配策略: ROLE/DEPT/USER/DEPT_HEAD/UPLEVEL_DEPT_HEAD/APPLICANT")
    private String assigneeType;

    @TableField("assignee_value")
    @Schema(description = "分配策略值(JSON)")
    private String assigneeValue;

    @TableField("node_sort")
    @Schema(description = "排序")
    private Integer nodeSort;
}
