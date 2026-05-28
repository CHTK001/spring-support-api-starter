package com.chua.starter.workflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 工作流任务(待办)
 *
 * @author CH
 * @since 2026/5/28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "工作流任务/待办")
@TableName("wf_task")
public class WfTask extends SysBase {

    private static final long serialVersionUID = 1L;

    @TableId(value = "wf_task_id", type = IdType.AUTO)
    @Schema(description = "任务ID")
    private Integer wfTaskId;

    @TableField("wf_inst_id")
    @Schema(description = "流程实例ID")
    private Integer wfInstId;

    @TableField("wf_def_node_id")
    @Schema(description = "节点定义ID")
    private Integer wfDefNodeId;

    @TableField("node_name")
    @Schema(description = "节点名称")
    private String nodeName;

    @TableField("assignee_id")
    @Schema(description = "审批人ID")
    private Integer assigneeId;

    @TableField("assignee_name")
    @Schema(description = "审批人姓名")
    private String assigneeName;

    @TableField("task_status")
    @Schema(description = "状态: PENDING/APPROVED/REJECTED/RETURNED/TRANSFERRED")
    private String taskStatus;

    @TableField("comment")
    @Schema(description = "审批意见")
    private String comment;

    @TableField("attachment")
    @Schema(description = "附件(JSON)")
    private String attachment;

    @TableField("deadline")
    @Schema(description = "截止时间")
    private LocalDateTime deadline;

    @TableField("form_data")
    @Schema(description = "节点表单数据(JSON)")
    private String formData;

    @TableField("sort")
    @Schema(description = "排序")
    private Integer sort;
}
