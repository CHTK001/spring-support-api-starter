package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文生图模板
 */
@ApiModel(description = "文生图模板")
@Schema(description = "文生图模板")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_ai_vincent_template")
public class SysAiVincentTemplate extends SysBase {
    @TableId(value = "sys_ai_vincent_template_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Integer sysAiVincentTemplateId;
    /**
     * 所属模块模型ID
     */
    @TableField(value = "sys_ai_module_id")
    @ApiModelProperty(value = "所属模块模型ID")
    @Schema(description = "所属模块模型ID")
    @NotNull(message = "所属模块模型ID不能为空", groups = {AddGroup.class})
    private Integer sysAiModuleId;
    /**
     * 排序
     */
    @TableField(value = "sys_ai_vincent_template_sort")
    @ApiModelProperty(value = "排序")
    @Schema(description = "排序")
    private Integer sysAiVincentTemplateSort;
    /**
     * 名称
     */
    @TableField(value = "sys_ai_vincent_template_name")
    @ApiModelProperty(value = "名称")
    @Schema(description = "名称")
    @NotNull(message = "所属模板名称不能为空", groups = {AddGroup.class})
    private String sysAiVincentTemplateName;

    /**
     * 类型
     */
    @TableField(value = "sys_ai_vincent_template_type")
    @ApiModelProperty(value = "类型")
    @Schema(description = "类型")
    private String sysAiVincentTemplateType;

    /**
     * 地址
     */
    @TableField(value = "sys_ai_vincent_template_address")
    @ApiModelProperty(value = "地址")
    @Schema(description = "地址")
    private String sysAiVincentTemplateAddress;

    /**
     * 描述
     */
    @TableField(value = "sys_ai_vincent_template_remark")
    @ApiModelProperty(value = "描述")
    @Schema(description = "描述")
    @NotBlank(message = "所属模板描述不能为空", groups = {AddGroup.class})
    private String sysAiVincentTemplateRemark;

    /**
     * 模型ID
     */
    @TableField(value = "sys_ai_vincent_template_model_id")
    @ApiModelProperty(value = "模型ID")
    @Schema(description = "模型ID")
    private String sysAiVincentTemplateModelId;

    /**
     * 模型名称
     */
    @TableField(value = "sys_ai_vincent_template_model_name")
    @ApiModelProperty(value = "模型名称")
    @Schema(description = "模型名称")
    private String sysAiVincentTemplateModelName;

    /**
     * 分类
     */
    @TableField(value = "sys_ai_vincent_template_category")
    @ApiModelProperty(value = "分类")
    @Schema(description = "分类")
    @NotBlank(message = "所属模板分类不能为空", groups = {AddGroup.class})
    private String sysAiVincentTemplateCategory;

    /**
     * 训练模型
     */
    @TableField(value = "sys_ai_vincent_template_train_model")
    @ApiModelProperty(value = "训练模型")
    @Schema(description = "训练模型")
    private String sysAiVincentTemplateTrainModel;

    /**
     * 正向关键词
     */
    @TableField(value = "sys_ai_vincent_template_recommend_prompt")
    @ApiModelProperty(value = "正向关键词")
    @Schema(description = "正向关键词")
    @NotBlank(message = "正向关键词不能为空", groups = {AddGroup.class})
    private String sysAiVincentTemplateRecommendPrompt;

    /**
     * 分辨率
     */
    @TableField(value = "sys_ai_vincent_template_resolutions")
    @ApiModelProperty(value = "分辨率")
    @Schema(description = "分辨率")
    private String sysAiVincentTemplateResolutions;

    /**
     * 关键词
     */
    @TableField(value = "sys_ai_vincent_template_word")
    @ApiModelProperty(value = "关键词")
    @Schema(description = "关键词")
    @NotBlank(message = "关键词不能为空", groups = {AddGroup.class})
    private String sysAiVincentTemplateWord;
}