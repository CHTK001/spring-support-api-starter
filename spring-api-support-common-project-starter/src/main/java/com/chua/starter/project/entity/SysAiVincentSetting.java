package com.chua.starter.project.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 文生图模型参数
 */
@ApiModel(description = "文生图模型参数")
@Schema(description = "文生图模型参数")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_ai_vincent_setting")
public class SysAiVincentSetting extends SysBase {
    @TableId(value = "sys_ai_vincent_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Integer sysAiVincentId;

    /**
     * 所属模块模型ID
     */
    @TableField(value = "sys_ai_module_id")
    @ApiModelProperty(value = "所属模块模型ID")
    @Schema(description = "所属模块模型ID")
    @NotNull(message = "所属模块模型ID不能为空", groups = {AddGroup.class})
    private Integer sysAiModuleId;

    /**
     * 支持的尺寸
     */
    @TableField(value = "sys_ai_vincent_supported_size")
    @ApiModelProperty(value = "支持的尺寸")
    @Schema(description = "支持的尺寸")
    private String sysAiVincentSupportedSize;
    /**
     * 支持的最大尺寸
     */
    @TableField(value = "sys_ai_vincent_supported_max_size")
    @ApiModelProperty(value = "支持的最大尺寸")
    @Schema(description = "支持的最大尺寸")
    private Integer sysAiVincentSupportedMaxSize;

    /**
     * 支持的风格
     */
    @TableField(value = "sys_ai_vincent_supported_style")
    @ApiModelProperty(value = "支持的风格")
    @Schema(description = "支持的风格")
    private String sysAiVincentSupportedStyle;

    /**
     * 支持输出张数
     */
    @TableField(value = "sys_ai_vincent_supported_number")
    @ApiModelProperty(value = "支持输出张数")
    @Schema(description = "支持输出张数")
    private Integer sysAiVincentSupportedNumber;
    /**
     * 是否支持异步任务;0:不支持
     */
    @TableField(value = "sys_ai_vincent_support_async")
    @ApiModelProperty(value = "是否支持异步任务;0:不支持")
    @Schema(description = "是否支持异步任务;0:不支持")
    private Integer sysAiVincentSupportAsync;
    /**
     * 是否支持音频;0:不支持
     */
    @TableField(value = "sys_ai_vincent_support_audio")
    @ApiModelProperty(value = "是否支持AI音效;0:不支持")
    @Schema(description = "是否支持AI音效;0:不支持")
    private Integer sysAiVincentSupportAudio;

    /**
     * 支持的清晰度
     */
    @TableField(value = "sys_ai_vincent_supported_quality")
    @ApiModelProperty(value = "支持的清晰度")
    @Schema(description = "支持的清晰度")
    private String sysAiVincentSupportedQuality;
    /**
     * 支持的fps
     */
    @TableField(value = "sys_ai_vincent_supported_fps")
    @ApiModelProperty(value = "支持的fps")
    @Schema(description = "支持的fps")
    private String sysAiVincentSupportedFps;
    /**
     * 是否支持参考图片;0:不支持
     */
    @TableField(value = "sys_ai_vincent_support_ref_image")
    @ApiModelProperty(value = "是否支持参考图片;0:不支持")
    @Schema(description = "是否支持参考图片;0:不支持")
    private Integer sysAiVincentSupportRefImage;


    /**
     * 支持的风格
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "支持的风格")
    @Schema(description = "支持的风格")
    private List<SysAiVincentStyle> styles;
}