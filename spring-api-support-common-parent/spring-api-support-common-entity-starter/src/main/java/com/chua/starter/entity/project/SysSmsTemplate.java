package com.chua.starter.entity.project;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.base.validator.group.AddGroup;
import com.chua.common.support.base.validator.group.SelectGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
    private static final long serialVersionUID = 1L;

/**
 * 模板管理
 *
 * @since 2026/08/03
 * @author CH
 */
@Schema(description="模板管理")
@Data
@EqualsAndHashCode(callSuper=true)
@TableName(value = "sys_sms_template")
public class SysSmsTemplate extends SysBase implements Serializable {
    /**
     * 模板主键ID
     */
    @TableId(value = "sys_sms_template_id", type = IdType.AUTO)
    @Schema(description="")
    /** SYSSMS模板ID */
    private Integer sysSmsTemplateId;

    /**
     * 项目ID
     */
    @TableField(value = "sys_project_id")
    @Schema(description="项目ID")
    @NotNull(message = "项目ID不能为空", groups = {SelectGroup.class, AddGroup.class})
    /** SYSprojectID */
    private Integer sysProjectId;

    /**
     * 类别
     */
    @TableField(value = "sys_project_type")
    @Schema(description="类别(前端固定值)")
    @NotNull(message = "类别不能为空", groups = {AddGroup.class})
    /** SYSproject类型 */
    private Integer sysProjectType;

    /**
     * 模板优先级
     */
    @TableField(value = "sys_sms_template_sort")
    @Schema(description="模板优先级")
    /** SYSSMS模板sort */
    private Integer sysSmsTemplateSort;
    /**
     * 模板类别
     */
    @TableField(value = "sys_sms_template_category")
    @Schema(description="模板类别")
    /** SYSSMS模板分类 */
    private Integer sysSmsTemplateCategory;

    /**
     * 名称
     */
    @TableField(value = "sys_sms_template_name")
    @Schema(description="名称")
    /** SYSSMS模板名称 */
    private String sysSmsTemplateName;


    /**
     * 唯一编码
     */
    @TableField(value = "sys_sms_template_code")
    @Schema(description="唯一编码")
    /** SYSSMS模板代码 */
    private String sysSmsTemplateCode;

    /**
     * 模板内容
     */
    @TableField(value = "sys_sms_template_content")
    @Schema(description="模板内容")
    /** SYSSMS模板内容 */
    private String sysSmsTemplateContent;

    /**
     * 是否删除0:未删除
     */
    @TableField(value = "sys_sms_template_delete")
    @Schema(description="是否删除0:未删除")
    /** SYSSMS模板delete */
    private Integer sysSmsTemplateDelete;

    /**
     * 是否启用:0:未启用
     */
    @TableField(value = "sys_sms_template_status")
    @Schema(description="是否启用:0:未启用")
    /** SYSSMS模板状态 */
    private Integer sysSmsTemplateStatus;

    /**
     * 模板类型;0:系统;1:用户;2:其它
     */
    @TableField(value = "sys_sms_template_type")
    @Schema(description="模板类型;0:系统;1:用户;2:其它")
    /** SYSSMS模板类型 */
    private Integer sysSmsTemplateType;

    /**
     * 是否默认:0:未启用
     */
    @TableField(value = "sys_sms_template_default")
    @Schema(description="是否默认:0:未启用")
    /** SYSSMS模板默认 */
    private Integer sysSmsTemplateDefault;

    /**
     * 审批状态;0:未审批; 1: 审批中; 2:已通过; 3: 未通过
     */
    @TableField(value = "sys_sms_template_approve")
    @Schema(description="审批状态;0:未审批; 1: 审批中; 2:已通过; 3: 未通过")
    /** SYSSMS模板approve */
    private Integer sysSmsTemplateApprove;

    /**
     * 拒绝原因
     */
    @TableField(value = "sys_sms_template_approve_reject")
    @Schema(description="拒绝原因")
    /** SYSSMS模板approvereject */
    private String sysSmsTemplateApproveReject;

    /**
     * 审批人
     */
    @TableField(value = "sys_sms_template_approve_create_by")
    @Schema(description="审批人")
    /** SYSSMS模板approvecreateBY */
    private Integer sysSmsTemplateApproveCreateBy;

    /**
     * 模板编辑状态;0:可编辑; 1: 不可编辑;
     */
    @TableField(value = "sys_sms_template_disabled")
    @Schema(description="模板编辑状态;0:可编辑; 1: 不可编辑;")
    /** SYSSMS模板disabled */
    private Integer sysSmsTemplateDisabled;

    /**
     * 来源
     */
    @TableField(value = "sys_sms_template_origin")
    @Schema(description="来源")
    /** SYSSMS模板origin */
    private String sysSmsTemplateOrigin;

    /**
     * 来源uuid
     */
    @TableField(value = "sys_sms_template_origin_uuid")
    @Schema(description="来源uuid")
    /** SYSSMS模板originUUID */
    private String sysSmsTemplateOriginUuid;
}
