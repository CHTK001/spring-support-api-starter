package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 默认功能 <---->对象的项目; e.g. 短信默认采用那个项目配置
 */
@ApiModel(description = "默认功能 <---->对象的项目; e.g. 短信默认采用那个项目配置")
@Schema(description = "默认功能 <---->对象的项目; e.g. 短信默认采用那个项目配置")
@Data
@TableName(value = "sys_project_default")
public class SysProjectDefault {
    @TableId(value = "sys_project_default_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Integer sysProjectDefaultId;

    /**
     * 类型； 字典表6;
     */
    @TableField(value = "sys_project_default_type")
    @ApiModelProperty(value = "类型； 字典表6;")
    @Schema(description = "类型； 字典表6;")
    private String sysProjectDefaultType;

    /**
     * 项目ID
     */
    @TableField(value = "sys_project_id")
    @ApiModelProperty(value = "项目ID")
    @Schema(description = "项目ID")
    private Integer sysProjectId;
}