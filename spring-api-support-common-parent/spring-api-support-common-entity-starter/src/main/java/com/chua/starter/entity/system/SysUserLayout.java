package com.chua.starter.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
    private static final long serialVersionUID = 1L;

/**
 * 用户布局配置表
 *
 * @since 2024/8/30
 * @author CH
 */
@Schema(description = "用户布局配置表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user_layout")
public class SysUserLayout extends SysBase implements Serializable {

    /**
     * 布局ID
     */
    @TableId(value = "sys_layout_id", type = IdType.AUTO)
    @Schema(description = "布局ID")
    @NotNull(message = "布局ID不能为空")
    /** SYSlayoutID */
    private Integer sysLayoutId;

    /**
     * 布局类型
     */
    @TableField(value = "sys_layout_layout")
    @Schema(description = "布局类型")
    @Size(max = 255, message = "布局类型最大长度需小于255")
    /** SYSlayoutlayout */
    private String sysLayoutLayout;

    /**
     * 布局网格配置
     */
    @TableField(value = "sys_layout_grid")
    @Schema(description = "布局网格配置")
    @Size(max = 255, message = "布局网格配置最大长度需小于255")
    /** SYSlayoutgrid */
    private String sysLayoutGrid;

    /**
     * 组件配置
     */
    @TableField(value = "sys_layout_component")
    @Schema(description = "组件配置")
    @Size(max = 255, message = "组件配置最大长度需小于255")
    /** SYSlayoutcomponent */
    private String sysLayoutComponent;

    /**
     * 所属用户ID
     */
    @TableField(value = "sys_layout_user_id")
    @Schema(description = "所属用户ID")
    /** SYSlayout用户ID */
    private Integer sysLayoutUserId;

    /**
     * 用户偏好配置（lay-setting 配置，JSON格式）
     */
    @TableField(value = "sys_layout_preference")
    @Schema(description = "用户偏好配置")
    /** SYSlayoutpreference */
    private String sysLayoutPreference;
}
