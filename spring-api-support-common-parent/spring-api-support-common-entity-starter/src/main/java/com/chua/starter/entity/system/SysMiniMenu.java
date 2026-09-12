package com.chua.starter.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.base.validator.group.UpdateGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 小程序菜单卡片配置
 *
 * @since 2026/08/03
 * @author CH
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_mini_menu")
@Schema(description = "小程序菜单卡片配置")
public class SysMiniMenu extends SysBase implements Serializable {
    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
      * 小程序菜单标识
     */
    @TableId(value = "sys_mini_menu_id", type = IdType.AUTO)
    @Schema(description = "小程序菜单ID")
    @NotNull(message = "小程序菜单ID不能为空", groups = UpdateGroup.class)
    /** sysminimenuid */
    private Integer sysMiniMenuId;

    /**
     * 卡片标题
     */
    @TableField("sys_mini_menu_title")
    @Schema(description = "卡片标题")
    @NotBlank(message = "卡片标题不能为空")
    @Size(max = 255, message = "卡片标题最大长度需小于255")
    /** sysminimenu标题 */
    private String sysMiniMenuTitle;

    /**
     * 卡片副标题
     */
    @TableField("sys_mini_menu_subtitle")
    @Schema(description = "卡片副标题")
    @Size(max = 255, message = "卡片副标题最大长度需小于255")
    /** sysminimenusubtitle */
    private String sysMiniMenuSubtitle;

    /**
     * 卡片分组
     */
    @TableField("sys_mini_menu_category")
    @Schema(description = "卡片分组")
    @Size(max = 128, message = "卡片分组最大长度需小于128")
    /** sysminimenu分类 */
    private String sysMiniMenuCategory;

    /**
     * 小程序页面路径
     */
    @TableField("sys_mini_menu_path")
    @Schema(description = "小程序页面路径")
    @NotBlank(message = "页面路径不能为空")
    @Size(max = 255, message = "页面路径最大长度需小于255")
    /** sysminimenu路径 */
    private String sysMiniMenuPath;

    /**
      * 跳转模式; 0:普通页面 1:tabbar页面 2:webview
     */
    @TableField("sys_mini_menu_jump_mode")
    @Schema(description = "跳转模式; 0:普通页面 1:tabBar页面 2:webview")
    /** sysminimenujump模式 */
    private Integer sysMiniMenuJumpMode;

    /**
     * 卡片图标
     */
    @TableField("sys_mini_menu_icon")
    @Schema(description = "卡片图标")
    @Size(max = 255, message = "卡片图标最大长度需小于255")
    /** sysminimenuicon */
    private String sysMiniMenuIcon;

    /**
     * 卡片封面/背景
     */
    @TableField("sys_mini_menu_cover")
    @Schema(description = "卡片封面/背景")
    @Size(max = 255, message = "卡片封面最大长度需小于255")
    /** sysminimenucover */
    private String sysMiniMenuCover;

    /**
     * 角标文案
     */
    @TableField("sys_mini_menu_badge")
    @Schema(description = "角标文案")
    @Size(max = 64, message = "角标文案最大长度需小于64")
    /** sysminimenubadge */
    private String sysMiniMenuBadge;

    /**
     * 角标样式
     */
    @TableField("sys_mini_menu_badge_type")
    @Schema(description = "角标样式")
    @Size(max = 64, message = "角标样式最大长度需小于64")
    /** sysminimenubadge类型 */
    private String sysMiniMenuBadgeType;

    /**
     * 权限标识
     */
    @TableField("sys_mini_menu_perm")
    @Schema(description = "权限标识")
    @Size(max = 255, message = "权限标识最大长度需小于255")
    /** sysminimenuperm */
    private String sysMiniMenuPerm;

    /**
     * 允许访问角色编码，逗号分隔
     */
    @TableField("sys_mini_menu_role")
    @Schema(description = "允许访问角色编码，逗号分隔")
    @Size(max = 255, message = "角色编码最大长度需小于255")
    /** sysminimenurole */
    private String sysMiniMenuRole;

    /**
     * 排序
     */
    @TableField("sys_mini_menu_sort")
    @Schema(description = "排序")
    /** sysminimenusort */
    private Integer sysMiniMenuSort;

    /**
     * 是否缓存; 0:否 1:是
     */
    @TableField("sys_mini_menu_keep_alive")
    @Schema(description = "是否缓存; 0:否 1:是")
    /** sysminimenukeepalive */
    private Integer sysMiniMenuKeepAlive;

    /**
     * 是否隐藏; 0:显示 1:隐藏
     */
    @TableField("sys_mini_menu_hidden")
    @Schema(description = "是否隐藏; 0:显示 1:隐藏")
    /** sysminimenuhidden */
    private Integer sysMiniMenuHidden;

    /**
     * 卡片说明
     */
    @TableField("sys_mini_menu_description")
    @Schema(description = "卡片说明")
    @Size(max = 255, message = "卡片说明最大长度需小于255")
    /** sysminimenu描述 */
    private String sysMiniMenuDescription;
}
