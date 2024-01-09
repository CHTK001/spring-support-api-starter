package com.chua.starter.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.datasource.annotation.Column;
import com.chua.common.support.datasource.annotation.Table;
import com.chua.starter.mybatis.pojo.SysBase;
import com.chua.starter.admin.enums.MenuTypeEnum;
import com.chua.starter.mybatis.pojo.SysTenantBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单实体对象
 *
 * @author haoxr
 * @since 2023/3/6
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_menu")
@Data
@Table(comment = "菜单实体对象")
public class SysMenu extends SysTenantBase {
    /**
     * 菜单ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Long menuId;

    /**
     * 父菜单ID
     */
    @Schema(description = "父菜单ID")
    private Long menuParentId;

    /**
     * 菜单名称
     */
    @Schema(description = "菜单名称")
    private String menuName;

    /**
     * menu-菜单；link-外链；button-按钮权限
     */
    @Schema(description = "菜单类型(menu-菜单；link-外链；button-按钮权限)")
    private String menuType;

    /**
     * 路由路径(浏览器地址栏路径)
     */
    @Schema(description = "路由路径(浏览器地址栏路径)")
    private String menuPath;

    /**
     * 组件路径(vue页面完整路径，省略.vue后缀)
     */
    @Schema(description = "组件路径(vue页面完整路径，省略.vue后缀)")
    private String menuComponent;

    /**
     * 标签
     */
    @Schema(description = "标签")
    private String menuTag;

    /**
     * 颜色
     */
    @Schema(description = "颜色")
    private String menuColor;

    /**
     * 权限标识
     */
    @Schema(description = "权限标识")
    private String menuPerm;

    /**
     * 显示状态(1:显示;0:隐藏)
     */
    @Schema(description = "显示状态(1:显示;0:隐藏), 任会返回给前端")
    @Column(defaultValue = "1", comment = "显示状态(1:显示;0:隐藏), 任会返回给前端")
    private Integer menuVisible;

    /**
     * 显示状态(1:显示;0:隐藏)
     */
    @Schema(description = "显示状态(1:显示;0:隐藏), 不返回给前端")
    @Column(defaultValue = "1", comment = "显示状态(1:显示;0:隐藏), 不返回给前端")
    private Integer menuStatus;

    /**
     * 排序
     */
    @Schema(description = "排序")
    private Integer menuSort;

    /**
     * 菜单图标
     */
    @Schema(description = "菜单图标")
    private String menuIcon;

    /**
     * 跳转路径
     */
    @Schema(description = "跳转路径")
    private String menuRedirect;

    /**
     * 父节点路径，以英文逗号(,)分割
     */
    @Schema(description = "父节点路径，以英文逗号(,)分割")
    private String menuTreePath;

    /**
     * 是否是固定菜单; (1:固定;0:非固定)
     */
    @Schema(description = "是否是固定菜单; (1:固定;0:非固定)")
    @Column(defaultValue = "0")
    private Integer menuAffix;
    /**
     * 角色列表
     */
    @TableField(exist = false)
    private String role;
    /**
     * 菜单类型
     */
    @TableField(exist = false)
    private MenuTypeEnum menuTypeEnum;

}