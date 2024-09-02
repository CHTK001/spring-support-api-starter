package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author CH
 * @since 2024/9/2
 */

/**
 * 模板管理
 */
@ApiModel(description = "模板管理")
@Schema(description = "模板管理")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_template")
public class SysTemplate extends SysBase implements Serializable {
    @TableId(value = "sys_template_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    @NotNull(message = "不能为null")
    private Integer sysTemplateId;

    /**
     * 分组ID
     */
    @TableField(value = "sys_template_group_id")
    @ApiModelProperty(value = "分组ID")
    @Schema(description = "分组ID")
    private Integer sysTemplateGroupId;
    /**
     * 厂家ID
     */
    @TableField(value = "sys_dict_item_id")
    @ApiModelProperty(value = "厂家ID, 字典(dictId = 2)子项")
    @Schema(description = "厂家ID,字典(dictId = 2)子项")
    private Integer sysDictItemId;

    /**
     * 模板优先级
     */
    @TableField(value = "sys_template_sort")
    @ApiModelProperty(value = "模板优先级")
    @Schema(description = "模板优先级")
    private Integer sysTemplateSort;
    /**
     * 分组名称
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "分组名称")
    @Schema(description = "分组名称")
    private String sysTemplateGroupName;
    /**
     * 分组名称
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "分组名称")
    @Schema(description = "分组名称")
    private String sysTemplateGroupCode;

    /**
     * 模板类型
     */
    @TableField(value = "sys_template_type")
    @ApiModelProperty(value = "模板类型;0:系统;1:用户;2:其它")
    @Schema(description = "模板类型;0:系统;1:用户;2:其它")
    private Integer sysTemplateType;
    /**
     * 审批状态;0:未审批; 1: 审批中; 2:已通过; 3: 未通过
     */
    @TableField(value = "sys_template_approve")
    @ApiModelProperty(value = "审批状态;0:未审批; 1: 审批中; 2:已通过; 3: 未通过")
    @Schema(description = "审批状态;0:未审批; 1: 审批中; 2:已通过; 3: 未通过")
    private Integer sysTemplateApprove;
    /**
     * 审批人
     */
    @TableField(value = "sys_template_approve_create_by")
    @ApiModelProperty(value = "审批人")
    @Schema(description = "审批人")
    private Integer sysTemplateApproveCreateBy;
    /**
     * 拒绝原因
     */
    @TableField(value = "sys_template_approve_reject")
    @ApiModelProperty(value = "拒绝原因")
    @Schema(description = "拒绝原因")
    @Size(max = 255, message = "拒绝原因最大长度要小于 255")
    private String sysTemplateApproveReject;

    /**
     * 名称
     */
    @TableField(value = "sys_template_name")
    @ApiModelProperty(value = "名称")
    @Schema(description = "名称")
    @Size(max = 255, message = "名称最大长度要小于 255")
    private String sysTemplateName;

    /**
     * 唯一编码
     */
    @TableField(value = "sys_template_code")
    @ApiModelProperty(value = "唯一编码")
    @Schema(description = "唯一编码")
    @Size(max = 255, message = "唯一编码最大长度要小于 255")
    private String sysTemplateCode;

    /**
     * 模板内容
     */
    @TableField(value = "sys_template_content")
    @ApiModelProperty(value = "模板内容")
    @Schema(description = "模板内容")
    private String sysTemplateContent;

    /**
     * 是否删除0:未删除
     */
    @TableField(value = "sys_template_delete")
    @ApiModelProperty(value = "是否删除0:未删除")
    @Schema(description = "是否删除0:未删除")
    @TableLogic(value = "0", delval = "1")
    private Integer sysTemplateDelete;

    /**
     * 是否启用:0:未启用
     */
    @TableField(value = "sys_template_status")
    @ApiModelProperty(value = "是否启用:0:未启用")
    @Schema(description = "是否启用:0:未启用")
    private Integer sysTemplateStatus;

    /**
     * 是否启用:0:未启用
     */
    @TableField(value = "sys_template_default")
    @ApiModelProperty(value = "是否默认:0:未启用")
    @Schema(description = "是否默认:0:未启用")
    private Integer sysTemplateDefault;
}