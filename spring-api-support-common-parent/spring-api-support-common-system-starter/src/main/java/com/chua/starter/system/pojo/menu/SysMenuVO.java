package com.chua.starter.system.pojo.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 菜单VO
 *
 * @since 2024/8/20
 * @author CH
 */
@Data
@Schema(description = "菜单VO")
public class SysMenuVO {

    /**
      * 菜单标识
     */
    @Schema(description = "菜单ID")
    /** sysmenuid */
    private Integer sysMenuId;

    /**
     * 菜单名称
     */
    @Schema(description = "菜单名称")
    /** 名称 */
    private String name;

    /**
     * 菜单路径
     */
    @Schema(description = "菜单路径")
    /** 路径 */
    private String path;

    /**
     * 菜单组件
     */
    @Schema(description = "菜单组件")
    /** 组件 */
    private String component;

    /**
     * 重定向地址
     */
    @Schema(description = "重定向地址")
    /** Redirect */
    private String redirect;

    /**
     * 离开过渡动画
     */
    @Schema(description = "离开过渡动画")
    /** Leavetransition */
    private String leaveTransition;

    /**
     * 进入过渡动画
     */
    @Schema(description = "进入过渡动画")
    /** Entertransition */
    private String enterTransition;

    /**
     * 元数据
     */
    @Schema(description = "元数据")
    /** Meta */
    private Meta meta;

    /**
      * 父菜单标识（koi 前端契约字段）
     */
    @Schema(description = "父菜单ID")
    /** 父级标识 */
    private Integer parentId;

    /**
      * 菜单标识（koi 前端契约字段）
     */
    @Schema(description = "菜单ID")
    /** menuid */
    private Integer menuId;

    /**
     * 菜单标题（koi 前端契约字段，用于侧边栏/面包屑展示）
     */
    @Schema(description = "菜单标题")
    /** Menu名称 */
    private String menuName;

    /**
     * 菜单类型（koi 前端契约字段）; 1:目录 0:页面
     */
    @Schema(description = "菜单类型; 1:目录 0:页面")
    /** Menu类型 */
    private String menuType;

    /**
     * 是否显示（koi 前端契约字段）; 1:显示 0:隐藏
     */
    @Schema(description = "是否显示; 1:显示 0:隐藏")
    /** isvisible */
    private String isVisible;

    /**
     * 是否缓存（koi 前端契约字段）; 1:缓存 0:不缓存
     */
    @Schema(description = "是否缓存; 1:缓存 0:不缓存")
    /** iskeepalive */
    private String isKeepAlive;

    /**
     * 菜单图标（koi 前端契约字段）
     */
    @Schema(description = "菜单图标")
    /** Icon */
    private String icon;

    /**
     * 菜单名称（旧字段）
     */
    @Schema(description = "菜单名称")
    /** sysmenu名称 */
    private String sysMenuName;

    /**
     * 菜单标题
     */
    @Schema(description = "菜单标题")
    /** sysmenu标题 */
    private String sysMenuTitle;

    /**
      * 父菜单标识
     */
    @Schema(description = "父菜单ID")
    /** sysmenupid */
    private Integer sysMenuPid;

    /**
     * 菜单图标
     */
    @Schema(description = "菜单图标")
    /** sysmenuicon */
    private String sysMenuIcon;

    /**
     * 右侧图标
     */
    @Schema(description = "右侧图标")
    /** sysmenuextraicon */
    private String sysMenuExtraIcon;

    /**
     * 菜单类型; 0:菜单; 1:iframe; 2:外链; 3:按钮
     */
    @Schema(description = "菜单类型; 0:菜单; 1:iframe; 2:外链; 3:按钮")
    /** sysmenu类型 */
    private Integer sysMenuType;

    /**
     * 优先级别
     */
    @Schema(description = "优先级别")
    /** sysmenusort */
    private Integer sysMenuSort;

    /**
     * 菜单链路
     */
    @Schema(description = "菜单链路")
    /** sysmenutreeid */
    private String sysMenuTreeId;

    /**
     * 权限标识
     */
    @Schema(description = "权限标识")
    /** sysmenuperm */
    private String sysMenuPerm;

    /**
     * 是否隐藏; 0:显示
     */
    @Schema(description = "是否隐藏; 0:显示")
    /** sysmenuhidden */
    private Integer sysMenuHidden;

    /**
     * 是否缓存; 0:否
     */
    @Schema(description = "是否缓存; 0:否")
    /** sysmenukeepalive */
    private Integer sysMenuKeepAlive;

    /**
     * 重定向地址
     */
    @Schema(description = "重定向地址")
    /** sysmenuredirect */
    private String sysMenuRedirect;

    @Schema(description = "菜单端类型; 0:PC 1:小程序")
    /** sysmenu引擎 */
    private Integer sysMenuEngine;

    /**
     * 子菜单
     */
    @Schema(description = "子菜单")
    /** Children */
    private List<SysMenuVO> children;

    /**
     * 元数据内部类
     * @author CH
     * @since 4.0.0
     */
    @Data
    @Schema(description = "菜单元数据")
    public static class Meta {
        /**
         * 标题
         */
        @Schema(description = "标题")
        /** 标题 */
        private String title;

        /**
         * 图标
         */
        @Schema(description = "图标")
        /** Icon */
        private String icon;

        /**
         * 额外图标
         */
        @Schema(description = "额外图标")
        /** Extraicon */
        private String extraIcon;

        /**
          * 国际化键
         */
        @Schema(description = "国际化key")
        /** I18n密钥 */
        private String i18nKey;

        /**
         * iframe地址
         */
        @Schema(description = "iframe地址")
        /** 帧src */
        private String frameSrc;

        /**
         * 激活路径
         */
        @Schema(description = "激活路径")
        /** 是否激活路径 */
        private String activePath;

        /**
         * 权限列表
         */
        @Schema(description = "权限列表")
        /** 认证 */
        private Iterable<String> auths;

        /**
         * 是否显示链接
         */
        @Schema(description = "是否显示链接")
        /** Showlink */
        private Boolean showLink;

        /**
         * 是否显示父级
         */
        @Schema(description = "是否显示父级")
        /** Show父级 */
        private Boolean showParent;

        /**
         * 是否缓存
         */
        @Schema(description = "是否缓存")
        /** Keepalive */
        private Boolean keepAlive;

        /**
         * 是否隐藏标签
         */
        @Schema(description = "是否隐藏标签")
        /** Hidden标签 */
        private Boolean hiddenTag;

        /**
         * 是否固定标签
         */
        @Schema(description = "是否固定标签")
        /** Fixed标签 */
        private Boolean fixedTag;

        /**
         * 排序
         */
        @Schema(description = "排序")
        /** 排名 */
        private Integer rank;

        /**
         * 是否首页
         */
        @Schema(description = "是否首页")
        /** Home */
        private Boolean home;

        /**
         * 过渡动画
         */
        @Schema(description = "过渡动画")
        /** 过渡 */
        private Transition transition;

        /**
         * 过渡动画内部类
         * @author CH
         * @since 4.0.0
         */
        @Data
        @Schema(description = "过渡动画")
        public static class Transition {
            /**
             * 离开过渡
             */
            @Schema(description = "离开过渡")
            /** Leavetransition */
            private String leaveTransition;

            /**
             * 进入过渡
             */
            @Schema(description = "进入过渡")
            /** Entertransition */
            private String enterTransition;
        }
    }
}
