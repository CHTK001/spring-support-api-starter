package com.chua.starter.system.pojo.menu;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.chua.starter.system.entity.SysMenu;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * @author CH
 * @since 2024/8/12
 */
@Data
@Schema(description = "菜单")
public class SysMenuVO {
    @TableId(value = "sys_menu_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    @NotNull(message = "不能为null")
    private Integer sysMenuId;


    /**
     * 父菜单
     */
    @TableField(value = "sys_menu_pid")
    @ApiModelProperty(value = "父菜单")
    @Schema(description = "父菜单")
    private Integer sysMenuPid;
    /**
     * 菜单名称
     */
    @Schema(description = "菜单名称")
    private String name;

    /**
     * 菜单路径
     */
    @Schema(description = "菜单路径")
    private String path;

    /**
     * 菜单组件
     */
    @Schema(description = "菜单组件")
    private String component;


    /**
     * 菜单
     */
    @Schema(description = "菜单信息")
    private Meta meta;

    /**
     * 子菜单
     */
    @Schema(description = "子菜单")
    private List<SysMenu> children;


    @Schema(description = "菜单信息")
    @Data
    public static class Meta {

        /**
         * 菜单标题
         */
        @Schema(description = "菜单标题")
        private String title;

        /**
         * 菜单i18n
         */
        @Schema(description = "菜单i18n")
        private String i18nKey;

        /**
         * 菜单图标
         */
        @Schema(description = "菜单图标")
        private String icon;

    }
}
