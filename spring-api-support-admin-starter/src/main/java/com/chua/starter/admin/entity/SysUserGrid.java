package com.chua.starter.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * 用户主页布局
 */
@ApiModel(description = "用户主页布局")
@Schema(description = "用户主页布局")
@Data
@TableName(value = "sys_user_grid")
public class SysUserGrid {
    /**
     * 主键
     */
    @TableId(value = "grid", type = IdType.AUTO)
    @ApiModelProperty(value = "主键")
    @Schema(description = "主键")
    @NotNull(message = "主键不能为null")
    private Long grid;

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    @ApiModelProperty(value = "用户ID")
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 布局
     */
    @TableField(value = "layout")
    @ApiModelProperty(value = "布局")
    @Schema(description = "布局")
    @Size(max = 255, message = "布局最大长度要小于 255")
    private String layout;

    /**
     * 组件
     */
    @TableField(value = "copms_list")
    @ApiModelProperty(value = "组件")
    @Schema(description = "组件")
    @Size(max = 255, message = "组件最大长度要小于 255")
    private String copmsList;

    @TableField(value = "tenant_id")
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Integer tenantId;

    @TableField(value = "create_name")
    @ApiModelProperty(value = "")
    @Schema(description = "")
    @Size(max = 255, message = "最大长度要小于 255")
    private String createName;

    @TableField(value = "create_by")
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Integer createBy;

    @TableField(value = "create_time")
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Date createTime;

    @TableField(value = "update_time")
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Date updateTime;

    @TableField(value = "update_name")
    @ApiModelProperty(value = "")
    @Schema(description = "")
    @Size(max = 255, message = "最大长度要小于 255")
    private String updateName;

    @TableField(value = "update_by")
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Integer updateBy;
}