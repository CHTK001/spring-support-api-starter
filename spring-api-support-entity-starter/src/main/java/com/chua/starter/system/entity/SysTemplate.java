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
     * 厂家ID
     */
    @TableField(value = "sys_dict_item_id1")
    @ApiModelProperty(value = "适用于厂家类型, 字典(dictId = 1)子项")
    @Schema(description = "适用于厂家类型, 字典(dictId = 1)子项")
    private Integer sysDictItem1;
    /**
     * 厂家名称
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "适用于厂家名称, 字典(dictId = 1)子项")
    @Schema(description = "适用于厂家名称,字典(dictId = 1)子项")
    private String sysDictItem1Name;

    /**
     * 模板类型
     */
    @TableField(value = "sys_dict_item_id2")
    @ApiModelProperty(value = "类别字典表(dictId=2)")
    @Schema(description = "类别字典表(dictId=2)")
    private Integer sysDictItem2;
    /**
     * 模板类型
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "类别")
    @Schema(description = "类别")
    private String sysDictItem2Name;
    /**
     * 模板类型
     */
    @TableField(value = "sys_dict_item_id3")
    @ApiModelProperty(value = "子类别字典表(dictId=3)")
    @Schema(description = "子类别字典表(dictId=3)")
    private Integer sysDictItem3;
    /**
     * 模板类型
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "类别")
    @Schema(description = "类别")
    private String sysDictItem3Name;
    /**
     * 模板类型
     */
    @TableField(value = "sys_secret_id")
    @ApiModelProperty(value = "密钥ID")
    @Schema(description = "密钥ID")
    private Integer sysSecretId;
    /**
     * 模板优先级
     */
    @TableField(value = "sys_template_sort")
    @ApiModelProperty(value = "模板优先级")
    @Schema(description = "模板优先级")
    private Integer sysTemplateSort;

    /**
     * 模板类型
     */
    @TableField(value = "sys_template_type")
    @ApiModelProperty(value = "模板类型;0:系统;1:用户;2:其它")
    @Schema(description = "模板类型;0:系统;1:用户;2:其它;")
    private Integer sysTemplateType;

    /**
     * 模板编辑状态
     */
    @TableField(value = "sys_template_disabled")
    @ApiModelProperty(value = "模板编辑状态;0:可编辑; 1: 不可编辑;")
    @Schema(description = "模板编辑状态;0:可编辑; 1: 不可编辑;")
    private Integer sysTemplateDisabled;
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
     * origin
     */
    @TableField(value = "sys_template_origin")
    @ApiModelProperty(value = "来源")
    @Schema(description = "来源")
    @Size(max = 255, message = "来源最大长度要小于 255")
    private String sysTemplateOrigin;
    /**
     * origin uuid
     */
    @TableField(value = "sys_template_origin_uuid")
    @ApiModelProperty(value = "来源uuid")
    @Schema(description = "来源uuid")
    @Size(max = 255, message = "来源uid最大长度要小于 255")
    private String sysTemplateOriginUuid;
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