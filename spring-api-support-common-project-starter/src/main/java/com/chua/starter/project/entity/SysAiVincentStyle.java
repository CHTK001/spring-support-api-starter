package com.chua.starter.project.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.validator.group.AddGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 文生图风�?
 */
@ApiModel(description = "文生图风�?)
@Schema(description = "文生图风�?)
@Data
@TableName(value = "sys_ai_vincent_style")
public class SysAiVincentStyle {
    @TableId(value = "sys_ai_vincent_style_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Integer sysAiVincentStyleId;
    /**
     * 所属模块模型ID
     */
    @TableField(value = "sys_ai_module_id")
    @ApiModelProperty(value = "所属模块模型ID")
    @Schema(description = "所属模块模型ID")
    @NotNull(message = "所属模块模型ID不能为空", groups = {AddGroup.class})
    private Integer sysAiModuleId;
    /**
     * 风格名称
     */
    @TableField(value = "sys_ai_vincent_style_name")
    @ApiModelProperty(value = "风格名称")
    @Schema(description = "风格名称")
    private String sysAiVincentStyleName;

    /**
     * 风格编码
     */
    @TableField(value = "sys_ai_vincent_style_code")
    @ApiModelProperty(value = "风格编码")
    @Schema(description = "风格编码")
    private String sysAiVincentStyleCode;

    /**
     * 风格地址
     */
    @TableField(value = "sys_ai_vincent_style_image")
    @ApiModelProperty(value = "风格地址")
    @Schema(description = "风格地址")
    private String sysAiVincentStyleImage;
}
