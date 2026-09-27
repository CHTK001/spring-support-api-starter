package com.chua.starter.system.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明式菜单注解
 *
 * <p>标注在控制器或方法上，启动期由
 * {@code com.chua.starter.system.support.MenuAnnotationRegistrar} 扫描并
 * <b>upsert 进 {@code sys_menu}</b>。注解只提供「默认值」，落库后管理员仍可在菜单管理里
 * 正常编辑、排序、开关、授权角色；再次启动时按 {@link #name()} 幂等更新，不会覆盖人工改动过的
 * 排序与角色。</p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * @Menu(name = "JobConsole", title = "调度控制台", path = "/job/console",
 *       component = "views/job/console/index.vue", icon = "Timer", sort = 30)
 * @RestController
 * public class JobConsoleController {
 * }
 *
 * // 仅在调度模块的类存在时显示该菜单
 * @Menu(name = "JobConsole", title = "调度控制台", path = "/job/console",
 *       showType = MenuShowType.CLASS, showValue = "com.chua.starter.scheduler.SchedulerApplication")
 * }</pre>
 *
 * @since 4.0.0.50
 * @author CH
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Menu {

    /**
     * 菜单唯一名称（路由名），幂等键
     *
     * @return 菜单名称
     */
    String name();

    /**
     * 菜单显示标题
     *
     * @return 标题
     */
    String title() default "";

    /**
     * 前端路由路径
     *
     * @return 路径
     */
    String path() default "";

    /**
     * 前端组件路径
     *
     * @return 组件
     */
    String component() default "";

    /**
     * 菜单图标
     *
     * @return 图标
     */
    String icon() default "";

    /**
     * 按钮权限标识
     *
     * @return 权限标识
     */
    String perm() default "";

    /**
     * 父菜单名称；留空表示顶级（pid=0）
     *
     * @return 父菜单名称
     */
    String parent() default "";

    /**
     * 排序值，越小越靠前
     *
     * @return 排序
     */
    int sort() default 0;

    /**
     * 动态显示条件类型名，取值见 {@code MenuShowType}；留空等同 ALWAYS
     *
     * @return 条件类型
     */
    String showType() default "";

    /**
     * 动态显示条件值，含义随 {@link #showType()} 变化
     *
     * @return 条件值
     */
    String showValue() default "";
}
