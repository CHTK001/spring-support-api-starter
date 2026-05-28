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
 * 流程实例
 *
 * @author CH
 * @since 2026/5/28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "流程实例")
@TableName("wf_inst")
public class WfInst extends SysBase {

    private static final long serialVersionUID = 1L;

    @TableId(value = "wf_inst_id", type = IdType.AUTO)
    @Schema(description = "实例ID")
    private Integer wfInstId;

    @TableField("wf_def_id")
    @Schema(description = "流程定义ID")
    private Integer wfDefId;

    @TableField("business_type")
    @Schema(description = "业务类型")
    private String businessType;

    @TableField("business_id")
    @Schema(description = "业务ID")
    private String businessId;

    @TableField("inst_title")
    @Schema(description = "流程标题")
    private String instTitle;

    @TableField("form_data")
    @Schema(description = "表单数据(动态JSON)")
    private String formData;

    @TableField("starter_id")
    @Schema(description = "发起人ID")
    private Integer starterId;

    @TableField("dept_id")
    @Schema(description = "发起人部门ID")
    private Integer deptId;

    @TableField("inst_status")
    @Schema(description = "状态: DRAFT/PENDING/APPROVED/REJECTED/WITHDRAWN")
    private String instStatus;

    @TableField("idempotent_key")
    @Schema(description = "幂等键(唯一)")
    private String idempotentKey;

    @TableField("current_node_id")
    @Schema(description = "当前节点ID(运行时)")
    private Integer currentNodeId;

    @TableField("expire_time")
    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    @TableField("finish_time")
    @Schema(description = "完成时间")
    private LocalDateTime finishTime;
}
