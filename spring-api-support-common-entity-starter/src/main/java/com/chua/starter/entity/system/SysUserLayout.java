package com.chua.starter.entity.system;

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
 * 用户布局配置表
 *
 * @author CH
 * @since 2024/8/30
 */
@ApiModel(description = "用户布局配置表")
@Schema(description = "用户布局配置表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user_layout")
public class SysUserLayout extends SysBase implements Serializable {

    /**
     * 布局ID
     */
    @TableId(value = "sys_layout_id", type = IdType.AUTO)
    @ApiModelProperty(value = "布局ID")
    @Schema(description = "布局ID")
    @NotNull(message = "布局ID不能为空")
    private Integer sysLayoutId;

    /**
     * 布局类型
     */
    @TableField(value = "sys_layout_layout")
    @ApiModelProperty(value = "布局类型")
    @Schema(description = "布局类型")
    @Size(max = 255, message = "布局类型最大长度需小于255")
    private String sysLayoutLayout;

    /**
     * 布局网格配置
     */
    @TableField(value = "sys_layout_grid")
    @ApiModelProperty(value = "布局网格配置")
    @Schema(description = "布局网格配置")
    @Size(max = 255, message = "布局网格配置最大长度需小于255")
    private String sysLayoutGrid;

    /**
     * 组件配置
     */
    @TableField(value = "sys_layout_component")
    @ApiModelProperty(value = "组件配置")
    @Schema(description = "组件配置")
    @Size(max = 255, message = "组件配置最大长度需小于255")
    private String sysLayoutComponent;

    /**
     * 所属用户ID
     */
    @TableField(value = "sys_layout_user_id")
    @ApiModelProperty(value = "所属用户ID")
    @Schema(description = "所属用户ID")
    private Integer sysLayoutUserId;
}
