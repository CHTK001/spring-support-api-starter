package com.chua.starter.workflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 工作流流转历史
 *
 * @author CH
 * @since 2026/5/28
 */
@Data
@Schema(description = "工作流流转历史")
@TableName("wf_task_history")
public class WfTaskHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "wf_task_history_id", type = IdType.AUTO)
    @Schema(description = "历史ID")
    private Integer wfTaskHistoryId;

    @TableField("wf_inst_id")
    @Schema(description = "流程实例ID")
    private Integer wfInstId;

    @TableField("wf_task_id")
    @Schema(description = "任务ID")
    private Integer wfTaskId;

    @TableField("operator_id")
    @Schema(description = "操作人ID")
    private Integer operatorId;

    @TableField("operator_name")
    @Schema(description = "操作人姓名")
    private String operatorName;

    @TableField("operation")
    @Schema(description = "操作: SUBMIT/APPROVE/REJECT/RETURN/WITHDRAW/TRANSFER")
    private String operation;

    @TableField("comment")
    @Schema(description = "操作意见")
    private String comment;

    @TableField("duration_seconds")
    @Schema(description = "耗时(秒)")
    private Long durationSeconds;

    @TableField("create_time")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
