package com.chua.starter.entity.project;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.SelectGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 模板管理
 */
@ApiModel(description="模板管理")
@Schema(description="模板管理")
@Data
@EqualsAndHashCode(callSuper=true)
@TableName(value = "sys_sms_template")
public class SysSmsTemplate extends SysBase implements Serializable {
    @TableId(value = "sys_sms_template_id", type = IdType.AUTO)
    @ApiModelProperty(value="")
    @Schema(description="")
    private Integer sysSmsTemplateId;

    /**
     * 项目ID
     */
    @TableField(value = "sys_project_id")
    @ApiModelProperty(value="项目ID")
    @Schema(description="项目ID")
    @NotNull(message = "项目ID不能为空", groups = {SelectGroup.class, AddGroup.class})
    private Integer sysProjectId;

    /**
     * 子类别字典表(dictId=6)
     */
    @TableField(value = "sys_project_type")
    @ApiModelProperty(value="类别字典表(dictId=6)")
    @Schema(description="类别字典表(dictId=6)")
    @NotNull(message = "类别不能为空", groups = {AddGroup.class})
    private Integer sysProjectType;

    /**
     * 模板优先级
     */
    @TableField(value = "sys_sms_template_sort")
    @ApiModelProperty(value="模板优先级")
    @Schema(description="模板优先级")
    private Integer sysSmsTemplateSort;
    /**
     * 模板类别
     */
    @TableField(value = "sys_sms_template_category")
    @ApiModelProperty(value="模板类别")
    @Schema(description="模板类别")
    private Integer sysSmsTemplateCategory;
    /**
     * 模板类别
     */
    @TableField(exist = false)
    @ApiModelProperty(value="模板类别")
    @Schema(description="模板类别")
    private String sysSmsTemplateCategoryLabel;

    /**
     * 名称
     */
    @TableField(value = "sys_sms_template_name")
    @ApiModelProperty(value="名称")
    @Schema(description="名称")
    private String sysSmsTemplateName;


    /**
     * 唯一编码
     */
    @TableField(value = "sys_sms_template_code")
    @ApiModelProperty(value="唯一编码")
    @Schema(description="唯一编码")
    private String sysSmsTemplateCode;

    /**
     * 模板内容
     */
    @TableField(value = "sys_sms_template_content")
    @ApiModelProperty(value="模板内容")
    @Schema(description="模板内容")
    private String sysSmsTemplateContent;

    /**
     * 是否删除0:未删除
     */
    @TableField(value = "sys_sms_template_delete")
    @ApiModelProperty(value="是否删除0:未删除")
    @Schema(description="是否删除0:未删除")
    private Integer sysSmsTemplateDelete;

    /**
     * 是否启用:0:未启用
     */
    @TableField(value = "sys_sms_template_status")
    @ApiModelProperty(value="是否启用:0:未启用")
    @Schema(description="是否启用:0:未启用")
    private Integer sysSmsTemplateStatus;

    /**
     * 模板类型;0:系统;1:用户;2:其它
     */
    @TableField(value = "sys_sms_template_type")
    @ApiModelProperty(value="模板类型;0:系统;1:用户;2:其它")
    @Schema(description="模板类型;0:系统;1:用户;2:其它")
    private Integer sysSmsTemplateType;

    /**
     * 是否默认:0:未启用
     */
    @TableField(value = "sys_sms_template_default")
    @ApiModelProperty(value="是否默认:0:未启用")
    @Schema(description="是否默认:0:未启用")
    private Integer sysSmsTemplateDefault;

    /**
     * 审批状态;0:未审批; 1: 审批中; 2:已通过; 3: 未通过
     */
    @TableField(value = "sys_sms_template_approve")
    @ApiModelProperty(value="审批状态;0:未审批; 1: 审批中; 2:已通过; 3: 未通过")
    @Schema(description="审批状态;0:未审批; 1: 审批中; 2:已通过; 3: 未通过")
    private Integer sysSmsTemplateApprove;

    /**
     * 拒绝原因
     */
    @TableField(value = "sys_sms_template_approve_reject")
    @ApiModelProperty(value="拒绝原因")
    @Schema(description="拒绝原因")
    private String sysSmsTemplateApproveReject;

    /**
     * 审批人
     */
    @TableField(value = "sys_sms_template_approve_create_by")
    @ApiModelProperty(value="审批人")
    @Schema(description="审批人")
    private Integer sysSmsTemplateApproveCreateBy;

    /**
     * 模板编辑状态;0:可编辑; 1: 不可编辑;
     */
    @TableField(value = "sys_sms_template_disabled")
    @ApiModelProperty(value="模板编辑状态;0:可编辑; 1: 不可编辑;")
    @Schema(description="模板编辑状态;0:可编辑; 1: 不可编辑;")
    private Integer sysSmsTemplateDisabled;

    /**
     * 来源
     */
    @TableField(value = "sys_sms_template_origin")
    @ApiModelProperty(value="来源")
    @Schema(description="来源")
    private String sysSmsTemplateOrigin;

    /**
     * 来源uuid
     */
    @TableField(value = "sys_sms_template_origin_uuid")
    @ApiModelProperty(value="来源uuid")
    @Schema(description="来源uuid")
    private String sysSmsTemplateOriginUuid;
}