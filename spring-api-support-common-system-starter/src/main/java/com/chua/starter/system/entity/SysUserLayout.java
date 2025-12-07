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
 * 用户布局
 *
 * @author CH
 * @since 2024/8/30
 */
@ApiModel(description = "用户布局")
@Schema(description = "用户布局")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user_layout")
public class SysUserLayout extends SysBase implements Serializable {

    @TableId(value = "sys_layout_id", type = IdType.AUTO)
    @ApiModelProperty(value = "布局ID")
    @Schema(description = "布局ID")
    @NotNull(message = "不能为null")
    private Integer sysLayoutId;

    /**
     * 布局
     */
    @TableField(value = "sys_layout_layout")
    @ApiModelProperty(value = "布局")
    @Schema(description = "布局")
    @Size(max = 255, message = "布局最大长度要小于 255")
    private String sysLayoutLayout;

    /**
     * 布局网格
     */
    @TableField(value = "sys_layout_grid")
    @ApiModelProperty(value = "布局网格")
    @Schema(description = "布局网格")
    @Size(max = 255, message = "布局网格最大长度要小于 255")
    private String sysLayoutGrid;

    /**
     * 组件
     */
    @TableField(value = "sys_layout_component")
    @ApiModelProperty(value = "组件")
    @Schema(description = "组件")
    @Size(max = 255, message = "组件最大长度要小于 255")
    private String sysLayoutComponent;

    /**
     * 所属用户
     */
    @TableField(value = "sys_layout_user_id")
    @ApiModelProperty(value = "所属用户")
    @Schema(description = "所属用户")
    private Integer sysLayoutUserId;
}