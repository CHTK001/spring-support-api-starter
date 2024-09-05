package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
 *
 * @since 2024/9/5
 * @author CH    
 */
/**
 * 模板分组
 */
@ApiModel(description="模板分组")
@Schema(description="模板分组")
@Data
@EqualsAndHashCode(callSuper=true)
@TableName(value = "sys_template_category")
public class SysTemplateCategory extends SysBase implements Serializable {
    @TableId(value = "sys_template_category_id", type = IdType.AUTO)
    @ApiModelProperty(value="")
    @Schema(description="")
    @NotNull(message = "不能为null")
    private Integer sysTemplateCategoryId;

    /**
     * 类别名称
     */
    @TableField(value = "sys_template_category_name")
    @ApiModelProperty(value="类别名称")
    @Schema(description="类别名称")
    @Size(max = 255,message = "类别名称最大长度要小于 255")
    private String sysTemplateCategoryName;

    /**
     * 类别唯一编码
     */
    @TableField(value = "sys_template_category_code")
    @ApiModelProperty(value="类别唯一编码")
    @Schema(description="类别唯一编码")
    @Size(max = 255,message = "类别唯一编码最大长度要小于 255")
    private String sysTemplateCategoryCode;

    /**
     * 是否删除;0:未删除
     */
    @TableField(value = "sys_template_category_delete")
    @ApiModelProperty(value="是否删除;0:未删除")
    @Schema(description="是否删除;0:未删除")
    private Integer sysTemplateCategoryDelete;

    /**
     * 类别优先级
     */
    @TableField(value = "sys_template_category_sort")
    @ApiModelProperty(value="类别优先级")
    @Schema(description="类别优先级")
    private Integer sysTemplateCategorySort;

    /**
     * 类别父ID
     */
    @TableField(value = "sys_template_category_pid")
    @ApiModelProperty(value="类别父ID")
    @Schema(description="类别父ID")
    private Integer sysTemplateCategoryPid;

    /**
     * 类别ID集合
     */
    @TableField(value = "sys_template_category_tree_id")
    @ApiModelProperty(value="类别ID集合")
    @Schema(description="类别ID集合")
    @Size(max = 255,message = "类别ID集合最大长度要小于 255")
    private String sysTemplateCategoryTreeId;
}