package com.chua.starter.system.pojo.menu;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Collections;
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
     * 重定向
     */
    @Schema(description = "重定向")
    private String redirect;


    /**
     * 进场动画
     */
    @Schema(description = "进场动画")
    private String enterTransition;

    /**
     * 离场动画
     */
    @Schema(description = "离场动画")
    private String leaveTransition;

    /**
     * 菜单
     */
    @Schema(description = "菜单信息")
    private Meta meta;

    /**
     * 子菜单
     */
    @Schema(description = "子菜单")
    private List<SysMenuVO> children = Collections.emptyList();


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
         * iframe 链接地址
         */
        @Schema(description = "iframe 链接地址")
        private String frameSrc;

        /**
         * 激活路径
         */
        @Schema(description = "激活路径")
        private String activePath;

        /**
         * 权限
         */
        @Schema(description = "权限")
        private String[] auths;
        /**
         * 是否显示链接
         */
        @Schema(description = "是否显示链接")
        private Boolean showLink;

        /**
         * 菜单图标
         */
        @Schema(description = "菜单图标")
        private String icon;

        /**
         * 是否首页
         */
        @Schema(description = "是否首页")
        private Boolean home;

        /**
         * 额外图标
         */
        @Schema(description = "额外图标")
        private String extraIcon;

        /**
         * 是否显示父菜单
         */
        @Schema(description = "是否显示父菜单")
        private boolean showParent = true;

        /**
         * 是否缓存
         */
        @Schema(description = "是否缓存")
        private boolean keepAlive;

        /**
         * 是否隐藏
         */
        @Schema(description = "是否隐藏")
        private boolean hiddenTag;
        /**
         * 是否固定
         */
        @Schema(description = "是否固定")
        private boolean fixedTag;

        /**
         * 排序
         */
        @Schema(description = "排序")
        private Integer rank;


        /**
         * 过渡动画
         */
        @Schema(description = "过渡动画")
        private Transition transition;
        /**
         * 过渡动画
         */
        @Data
        public static class Transition {

            @Schema(description = "离场动画")
            private String leaveTransition;
            @Schema(description = "进场动画")
            private String enterTransition;
        }
    }

}
