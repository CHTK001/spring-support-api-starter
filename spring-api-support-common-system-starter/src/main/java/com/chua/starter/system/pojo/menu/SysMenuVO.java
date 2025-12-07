package com.chua.starter.system.pojo.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 菜单VO
 *
 * @author CH
 * @since 2024/8/20
 */
@Data
@Schema(description = "菜单VO")
public class SysMenuVO {

    /**
     * 菜单ID
     */
    @Schema(description = "菜单ID")
    private Integer sysMenuId;

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
     * 重定向地址
     */
    @Schema(description = "重定向地址")
    private String redirect;

    /**
     * 离开过渡动画
     */
    @Schema(description = "离开过渡动画")
    private String leaveTransition;

    /**
     * 进入过渡动画
     */
    @Schema(description = "进入过渡动画")
    private String enterTransition;

    /**
     * 元数据
     */
    @Schema(description = "元数据")
    private Meta meta;

    /**
     * 菜单名称（旧字段）
     */
    @Schema(description = "菜单名称")
    private String sysMenuName;

    /**
     * 菜单标题
     */
    @Schema(description = "菜单标题")
    private String sysMenuTitle;

    /**
     * 父菜单ID
     */
    @Schema(description = "父菜单ID")
    private Integer sysMenuPid;

    /**
     * 菜单图标
     */
    @Schema(description = "菜单图标")
    private String sysMenuIcon;

    /**
     * 右侧图标
     */
    @Schema(description = "右侧图标")
    private String sysMenuExtraIcon;

    /**
     * 菜单类型; 0:菜单; 1:iframe; 2:外链; 3:按钮
     */
    @Schema(description = "菜单类型; 0:菜单; 1:iframe; 2:外链; 3:按钮")
    private Integer sysMenuType;

    /**
     * 优先级别
     */
    @Schema(description = "优先级别")
    private Integer sysMenuSort;

    /**
     * 菜单链路
     */
    @Schema(description = "菜单链路")
    private String sysMenuTreeId;

    /**
     * 权限标识
     */
    @Schema(description = "权限标识")
    private String sysMenuPerm;

    /**
     * 是否隐藏; 0:显示
     */
    @Schema(description = "是否隐藏; 0:显示")
    private Integer sysMenuHidden;

    /**
     * 是否缓存; 0:否
     */
    @Schema(description = "是否缓存; 0:否")
    private Integer sysMenuKeepAlive;

    /**
     * 重定向地址
     */
    @Schema(description = "重定向地址")
    private String sysMenuRedirect;

    /**
     * 子菜单
     */
    @Schema(description = "子菜单")
    private List<SysMenuVO> children;

    /**
     * 元数据内部类
     */
    @Data
    @Schema(description = "菜单元数据")
    public static class Meta {
        /**
         * 标题
         */
        @Schema(description = "标题")
        private String title;

        /**
         * 图标
         */
        @Schema(description = "图标")
        private String icon;

        /**
         * 额外图标
         */
        @Schema(description = "额外图标")
        private String extraIcon;

        /**
         * 国际化key
         */
        @Schema(description = "国际化key")
        private String i18nKey;

        /**
         * iframe地址
         */
        @Schema(description = "iframe地址")
        private String frameSrc;

        /**
         * 激活路径
         */
        @Schema(description = "激活路径")
        private String activePath;

        /**
         * 权限列表
         */
        @Schema(description = "权限列表")
        private Iterable<String> auths;

        /**
         * 是否显示链接
         */
        @Schema(description = "是否显示链接")
        private Boolean showLink;

        /**
         * 是否显示父级
         */
        @Schema(description = "是否显示父级")
        private Boolean showParent;

        /**
         * 是否缓存
         */
        @Schema(description = "是否缓存")
        private Boolean keepAlive;

        /**
         * 是否隐藏标签
         */
        @Schema(description = "是否隐藏标签")
        private Boolean hiddenTag;

        /**
         * 是否固定标签
         */
        @Schema(description = "是否固定标签")
        private Boolean fixedTag;

        /**
         * 排序
         */
        @Schema(description = "排序")
        private Integer rank;

        /**
         * 是否首页
         */
        @Schema(description = "是否首页")
        private Boolean home;

        /**
         * 过渡动画
         */
        @Schema(description = "过渡动画")
        private Transition transition;

        /**
         * 过渡动画内部类
         */
        @Data
        @Schema(description = "过渡动画")
        public static class Transition {
            /**
             * 离开过渡
             */
            @Schema(description = "离开过渡")
            private String leaveTransition;

            /**
             * 进入过渡
             */
            @Schema(description = "进入过渡")
            private String enterTransition;
        }
    }
}