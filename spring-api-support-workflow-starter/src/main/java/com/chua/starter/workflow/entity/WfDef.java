package com.chua.starter.workflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工作流定义
 *
 * @author CH
 * @since 2026/5/28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "工作流定义")
@TableName("wf_def")
public class WfDef extends SysBase {

    private static final long serialVersionUID = 1L;

    @TableId(value = "wf_def_id", type = IdType.AUTO)
    @Schema(description = "流程定义ID")
    private Integer wfDefId;

    @TableField("wf_def_name")
    @Schema(description = "流程名称")
    private String wfDefName;

    @TableField("wf_def_code")
    @Schema(description = "流程编码(唯一)")
    private String wfDefCode;

    @TableField("wf_def_category")
    @Schema(description = "分类")
    private String wfDefCategory;

    @TableField("wf_def_version")
    @Schema(description = "版本号")
    private Integer wfDefVersion;

    @TableField("wf_def_status")
    @Schema(description = "状态: 0草稿 1发布 2停用")
    private Integer wfDefStatus;

    @TableField("wf_def_form_config")
    @Schema(description = "表单配置(动态JSON)")
    private String wfDefFormConfig;

    @TableField("wf_def_remark")
    @Schema(description = "备注")
    private String wfDefRemark;
}
