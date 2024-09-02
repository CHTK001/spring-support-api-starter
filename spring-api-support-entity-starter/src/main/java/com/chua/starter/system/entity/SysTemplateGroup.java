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
 *
 * @since 2024/9/2
 * @author CH    
 */

/**
 * 模板分组
 */
@ApiModel(description = "模板分组")
@Schema(description = "模板分组")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_template_group")
public class SysTemplateGroup extends SysBase implements Serializable {
    @TableId(value = "sys_template_group_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    @NotNull(message = "不能为null")
    private Integer sysTemplateGroupId;

    /**
     * 模板名称
     */
    @TableField(value = "sys_template_group_name")
    @ApiModelProperty(value = "模板名称")
    @Schema(description = "模板名称")
    @Size(max = 255, message = "模板名称最大长度要小于 255")
    private String sysTemplateGroupName;

    /**
     * 模板优先级
     */
    @TableField(value = "sys_template_group_sort")
    @ApiModelProperty(value = "模板优先级")
    @Schema(description = "模板优先级")
    private Integer sysTemplateGroupSort;
    /**
     * 模板唯一编码
     */
    @TableField(value = "sys_template_group_code")
    @ApiModelProperty(value = "模板唯一编码")
    @Schema(description = "模板唯一编码")
    @Size(max = 255, message = "模板唯一编码最大长度要小于 255")
    private String sysTemplateGroupCode;

    /**
     * 是否删除;0:未删除
     */
    @TableField(value = "sys_template_group_delete")
    @ApiModelProperty(value = "是否删除;0:未删除")
    @Schema(description = "是否删除;0:未删除")
    @TableLogic(value = "0", delval = "1")
    private Integer sysTemplateGroupDelete;
}