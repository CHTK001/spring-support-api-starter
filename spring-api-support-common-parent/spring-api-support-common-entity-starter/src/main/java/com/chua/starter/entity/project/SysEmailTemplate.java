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
 *
 * @since 2026/08/03
 * @author CH
 */
@Schema(description="邮件模板")
@Data
@EqualsAndHashCode(callSuper=true)
@TableName(value = "sys_email_template")
public class SysEmailTemplate extends SysBase implements Serializable {
    /**
     * 模板主键ID
     */
    @TableId(value = "sys_email_template_id", type = IdType.AUTO)
    @Schema(description="")
    /** SYSemail模板ID */
    private Integer sysEmailTemplateId;

    /**
     * 项目ID
     */
    @TableField(value = "sys_project_id")
    @Schema(description="项目ID")
    /** SYSprojectID */
    private Integer sysProjectId;

    /**
     * 类别
     */
    @TableField(value = "sys_project_type")
    @Schema(description="类别(前端固定值)")
    /** SYSproject类型 */
    private Integer sysProjectType;

    /**
     * 模板优先级
     */
    @TableField(value = "sys_email_template_sort")
    @Schema(description="模板优先级")
    /** SYSemail模板sort */
    private Integer sysEmailTemplateSort;

    /**
     * 子类型
     */
    @TableField(value = "sys_email_template_category")
    @Schema(description="子类型(前端固定值)")
    /** SYSemail模板分类 */
    private Integer sysEmailTemplateCategory;

    /**
     * 名称
     */
    @TableField(value = "sys_email_template_name")
    @Schema(description="名称")
    /** SYSemail模板名称 */
    private String sysEmailTemplateName;


    /**
     * 回调地址
     */
    @TableField(value = "sys_email_template_callback_url")
    @Schema(description="回调地址")
    /** SYSemail模板callbackURL */
    private String sysEmailTemplateCallbackUrl;

    /**
     * 唯一编码
     */
    @TableField(value = "sys_email_template_code")
    @Schema(description="唯一编码")
    /** SYSemail模板代码 */
    private String sysEmailTemplateCode;

    /**
     * 模板内容
     */
    @TableField(value = "sys_email_template_content")
    @Schema(description="模板内容")
    /** SYSemail模板内容 */
    private String sysEmailTemplateContent;

    /**
     * 是否删除0:未删除
     */
    @TableField(value = "sys_email_template_delete")
    @Schema(description="是否删除0:未删除")
    /** SYSemail模板delete */
    private Integer sysEmailTemplateDelete;

    /**
     * 是否启用:0:未启用
     */
    @TableField(value = "sys_email_template_status")
    @Schema(description="是否启用:0:未启用")
    /** SYSemail模板状态 */
    private Integer sysEmailTemplateStatus;

    /**
     * 模板类型;0:系统;1:用户;2:其它
     */
    @TableField(value = "sys_email_template_type")
    @Schema(description="模板类型;0:系统;1:用户;2:其它")
    /** SYSemail模板类型 */
    private Integer sysEmailTemplateType;

    /**
     * 是否默认:0:未启用
     */
    @TableField(value = "sys_email_template_default")
    @Schema(description="是否默认:0:未启用")
    /** SYSemail模板默认 */
    private Integer sysEmailTemplateDefault;

    /**
     * 审批状态;0:未审批; 1: 审批中; 2:已通过; 3: 未通过
     */
    @TableField(value = "sys_email_template_approve")
    @Schema(description="审批状态;0:未审批; 1: 审批中; 2:已通过; 3: 未通过")
    /** SYSemail模板approve */
    private Integer sysEmailTemplateApprove;

    /**
     * 拒绝原因
     */
    @TableField(value = "sys_email_template_approve_reject")
    @Schema(description="拒绝原因")
    /** SYSemail模板approvereject */
    private String sysEmailTemplateApproveReject;

    /**
     * 审批人
     */
    @TableField(value = "sys_email_template_approve_create_by")
    @Schema(description="审批人")
    /** SYSemail模板approvecreateBY */
    private Integer sysEmailTemplateApproveCreateBy;

    /**
     * 模板编辑状态;0:可编辑; 1: 不可编辑;
     */
    @TableField(value = "sys_email_template_disabled")
    @Schema(description="模板编辑状态;0:可编辑; 1: 不可编辑;")
    /** SYSemail模板disabled */
    private Integer sysEmailTemplateDisabled;

    /**
     * 来源
     */
    @TableField(value = "sys_email_template_origin")
    @Schema(description="来源")
    /** SYSemail模板origin */
    private String sysEmailTemplateOrigin;

    /**
     * 来源uuid
     */
    @TableField(value = "sys_email_template_origin_uuid")
    @Schema(description="来源uuid")
    /** SYSemail模板originUUID */
    private String sysEmailTemplateOriginUuid;
}
