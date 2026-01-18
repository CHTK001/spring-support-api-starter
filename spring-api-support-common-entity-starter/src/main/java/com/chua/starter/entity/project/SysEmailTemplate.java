package com.chua.starter.entity.project;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 邮件模板
 */
@Schema(description="邮件模板")
@Data
@EqualsAndHashCode(callSuper=true)
@TableName(value = "sys_email_template")
public class SysEmailTemplate extends SysBase implements Serializable {
    @TableId(value = "sys_email_template_id", type = IdType.AUTO)
    @Schema(description="")
    private Integer sysEmailTemplateId;

    /**
     * 项目ID
     */
    @TableField(value = "sys_project_id")
    @Schema(description="项目ID")
    private Integer sysProjectId;

    /**
     * 类别字典表(dictId=6)
     */
    @TableField(value = "sys_project_type")
    @Schema(description="类别字典表(dictId=6)")
    private Integer sysProjectType;

    /**
     * 模板优先级
     */
    @TableField(value = "sys_email_template_sort")
    @Schema(description="模板优先级")
    private Integer sysEmailTemplateSort;

    /**
     * 子类型; (dictId = 2)
     */
    @TableField(value = "sys_email_template_category")
    @Schema(description="子类型; (dictId = 2)")
    private Integer sysEmailTemplateCategory;
    /**
     * 模板类别
     */
    @TableField(exist = false)
    @Schema(description="模板类别")
    private String sysEmailTemplateCategoryLabel;

    /**
     * 名称
     */
    @TableField(value = "sys_email_template_name")
    @Schema(description="名称")
    private String sysEmailTemplateName;


    /**
     * 回调地址
     */
    @TableField(value = "sys_email_template_callback_url")
    @Schema(description="回调地址")
    private String sysEmailTemplateCallbackUrl;

    /**
     * 唯一编码
     */
    @TableField(value = "sys_email_template_code")
    @Schema(description="唯一编码")
    private String sysEmailTemplateCode;

    /**
     * 模板内容
     */
    @TableField(value = "sys_email_template_content")
    @Schema(description="模板内容")
    private String sysEmailTemplateContent;

    /**
     * 是否删除0:未删除
     */
    @TableField(value = "sys_email_template_delete")
    @Schema(description="是否删除0:未删除")
    private Integer sysEmailTemplateDelete;

    /**
     * 是否启用:0:未启用
     */
    @TableField(value = "sys_email_template_status")
    @Schema(description="是否启用:0:未启用")
    private Integer sysEmailTemplateStatus;

    /**
     * 模板类型;0:系统;1:用户;2:其它
     */
    @TableField(value = "sys_email_template_type")
    @Schema(description="模板类型;0:系统;1:用户;2:其它")
    private Integer sysEmailTemplateType;

    /**
     * 是否默认:0:未启用
     */
    @TableField(value = "sys_email_template_default")
    @Schema(description="是否默认:0:未启用")
    private Integer sysEmailTemplateDefault;

    /**
     * 审批状态;0:未审批; 1: 审批中; 2:已通过; 3: 未通过
     */
    @TableField(value = "sys_email_template_approve")
    @Schema(description="审批状态;0:未审批; 1: 审批中; 2:已通过; 3: 未通过")
    private Integer sysEmailTemplateApprove;

    /**
     * 拒绝原因
     */
    @TableField(value = "sys_email_template_approve_reject")
    @Schema(description="拒绝原因")
    private String sysEmailTemplateApproveReject;

    /**
     * 审批人
     */
    @TableField(value = "sys_email_template_approve_create_by")
    @Schema(description="审批人")
    private Integer sysEmailTemplateApproveCreateBy;

    /**
     * 模板编辑状态;0:可编辑; 1: 不可编辑;
     */
    @TableField(value = "sys_email_template_disabled")
    @Schema(description="模板编辑状态;0:可编辑; 1: 不可编辑;")
    private Integer sysEmailTemplateDisabled;

    /**
     * 来源
     */
    @TableField(value = "sys_email_template_origin")
    @Schema(description="来源")
    private String sysEmailTemplateOrigin;

    /**
     * 来源uuid
     */
    @TableField(value = "sys_email_template_origin_uuid")
    @Schema(description="来源uuid")
    private String sysEmailTemplateOriginUuid;
}
