package com.chua.starter.system.constant;

import com.chua.common.support.utils.StringUtils;

/**
 * 菜单动态显示条件类型
 *
 * <p>决定 {@code sys_menu} 一条菜单是否出现在当前用户的菜单树里。
 * 求值器见 {@code MenuVisibilityEvaluator}。</p>
 *
 * @since 4.0.0.50
 * @author CH
 */
public enum MenuShowType {

    /**
     * 始终显示：不看任何条件
     */
    ALWAYS,

    /**
     * 类存在才显示：条件值填类全名，用 ClassUtils 判定类是否在 classpath
     */
    CLASS,

    /**
     * SpEL 表达式为真才显示：条件值填表达式，可引用 environment
     */
    SPEL,

    /**
     * 功能开关闭联：条件值填 {@code sys_policy_config} 里 {@code type='FEATURE'} 的业务键，
     * 该行启用则显示
     */
    SWITCH,

    /**
     * 配置属性为真才显示：条件值填 Spring 配置键，如 {@code plugin.job.console.enable}
     */
    PROPERTY;

    /**
     * 按名称解析，空值或未匹配一律退化为 {@link #ALWAYS}。
     *
     * <p>退化成 ALWAYS 而不是隐藏，是为了让「条件字段填错」表现为「菜单照常显示」，
     * 不会因为一个拼写错误把整棵菜单树干掉。</p>
     *
     * @param name 类型名
     * @return 条件类型
     */
    public static MenuShowType of(String name) {
        if (StringUtils.isBlank(name)) {
            return ALWAYS;
        }
        for (MenuShowType type : values()) {
            if (type.name().equalsIgnoreCase(name.trim())) {
                return type;
            }
        }
        return ALWAYS;
    }
}
