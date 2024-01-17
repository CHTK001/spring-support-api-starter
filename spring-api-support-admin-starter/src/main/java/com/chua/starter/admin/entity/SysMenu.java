package com.chua.starter.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.admin.enums.MenuTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * 菜单实体对象
 */
@ApiModel(description = "菜单实体对象")
@Schema(description = "菜单实体对象")
@Data
@TableName(value = "sys_menu")
public class SysMenu {
    /**
     * 主键
     */
    @TableId(value = "menu_id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键")
    @Schema(description = "主键")
    @NotNull(message = "主键不能为null")
    private Long menuId;

    /**
     * 父菜单ID
     */
    @TableField(value = "menu_parent_id")
    @ApiModelProperty(value = "父菜单ID")
    @Schema(description = "父菜单ID")
    private Long menuParentId;

    /**
     * 菜单名称
     */
    @TableField(value = "menu_name")
    @ApiModelProperty(value = "菜单名称")
    @Schema(description = "菜单名称")
    @Size(max = 255, message = "菜单名称最大长度要小于 255")
    private String menuName;

    /**
     * 菜单类型(menu-菜单；link-外链；button-按钮权限)
     */
    @TableField(value = "menu_type")
    @ApiModelProperty(value = "菜单类型(menu-菜单；link-外链；button-按钮权限)")
    @Schema(description = "菜单类型(menu-菜单；link-外链；button-按钮权限)")
    @Size(max = 255, message = "菜单类型(menu-菜单；link-外链；button-按钮权限)最大长度要小于 255")
    private String menuType;

    /**
     * 路由路径(浏览器地址栏路径)
     */
    @TableField(value = "menu_path")
    @ApiModelProperty(value = "路由路径(浏览器地址栏路径)")
    @Schema(description = "路由路径(浏览器地址栏路径)")
    @Size(max = 255, message = "路由路径(浏览器地址栏路径)最大长度要小于 255")
    private String menuPath;

    /**
     * 组件路径(vue页面完整路径，省略.vue后缀)
     */
    @TableField(value = "menu_component")
    @ApiModelProperty(value = "组件路径(vue页面完整路径，省略.vue后缀)")
    @Schema(description = "组件路径(vue页面完整路径，省略.vue后缀)")
    @Size(max = 255, message = "组件路径(vue页面完整路径，省略.vue后缀)最大长度要小于 255")
    private String menuComponent;

    /**
     * 标签
     */
    @TableField(value = "menu_tag")
    @ApiModelProperty(value = "标签")
    @Schema(description = "标签")
    @Size(max = 255, message = "标签最大长度要小于 255")
    private String menuTag;

    /**
     * 颜色
     */
    @TableField(value = "menu_color")
    @ApiModelProperty(value = "颜色")
    @Schema(description = "颜色")
    @Size(max = 255, message = "颜色最大长度要小于 255")
    private String menuColor;

    /**
     * 权限标识
     */
    @TableField(value = "menu_perm")
    @ApiModelProperty(value = "权限标识")
    @Schema(description = "权限标识")
    @Size(max = 255, message = "权限标识最大长度要小于 255")
    private String menuPerm;

    /**
     * 显示状态(1:显示;0:隐藏), 任会返回给前端
     */
    @TableField(value = "menu_visible")
    @ApiModelProperty(value = "显示状态(1:显示;0:隐藏), 任会返回给前端")
    @Schema(description = "显示状态(1:显示;0:隐藏), 任会返回给前端")
    private Integer menuVisible;

    /**
     * 显示状态(1:显示;0:隐藏), 不返回给前端
     */
    @TableField(value = "menu_status")
    @ApiModelProperty(value = "显示状态(1:显示;0:隐藏), 不返回给前端")
    @Schema(description = "显示状态(1:显示;0:隐藏), 不返回给前端")
    private Integer menuStatus;

    /**
     * 排序
     */
    @TableField(value = "menu_sort")
    @ApiModelProperty(value = "排序")
    @Schema(description = "排序")
    private Integer menuSort;

    /**
     * 菜单图标
     */
    @TableField(value = "menu_icon")
    @ApiModelProperty(value = "菜单图标")
    @Schema(description = "菜单图标")
    @Size(max = 255, message = "菜单图标最大长度要小于 255")
    private String menuIcon;

    /**
     * 跳转路径
     */
    @TableField(value = "menu_redirect")
    @ApiModelProperty(value = "跳转路径")
    @Schema(description = "跳转路径")
    @Size(max = 255, message = "跳转路径最大长度要小于 255")
    private String menuRedirect;

    /**
     * 父节点路径，以英文逗号(,)分割
     */
    @TableField(value = "menu_tree_path")
    @ApiModelProperty(value = "父节点路径，以英文逗号(,)分割")
    @Schema(description = "父节点路径，以英文逗号(,)分割")
    @Size(max = 255, message = "父节点路径，以英文逗号(,)分割最大长度要小于 255")
    private String menuTreePath;

    @TableField(value = "menu_affix")
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Integer menuAffix;

    /**
     * 创建人
     */
    @TableField(value = "create_name")
    @ApiModelProperty(value = "创建人")
    @Schema(description = "创建人")
    @Size(max = 255, message = "创建人最大长度要小于 255")
    private String createName;

    /**
     * 创建人
     */
    @TableField(value = "create_by")
    @ApiModelProperty(value = "创建人")
    @Schema(description = "创建人")
    @Size(max = 255, message = "创建人最大长度要小于 255")
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    @ApiModelProperty(value = "创建时间")
    @Schema(description = "创建时间")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    @ApiModelProperty(value = "更新时间")
    @Schema(description = "更新时间")
    private Date updateTime;

    /**
     * 更新人
     */
    @TableField(value = "update_name")
    @ApiModelProperty(value = "更新人")
    @Schema(description = "更新人")
    @Size(max = 255, message = "更新人最大长度要小于 255")
    private String updateName;

    /**
     * 更新人
     */
    @TableField(value = "update_by")
    @ApiModelProperty(value = "更新人")
    @Schema(description = "更新人")
    @Size(max = 255, message = "更新人最大长度要小于 255")
    private String updateBy;

    /**
     * 租户ID
     */
    @TableField(value = "tenant_id")
    @ApiModelProperty(value = "租户ID")
    @Schema(description = "租户ID")
    private Integer tenantId;
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