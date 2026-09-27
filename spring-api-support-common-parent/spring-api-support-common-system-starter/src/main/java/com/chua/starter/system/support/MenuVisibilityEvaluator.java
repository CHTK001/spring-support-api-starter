package com.chua.starter.system.support;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.entity.system.SysMenu;
import com.chua.starter.entity.system.SysPolicyConfig;
import com.chua.starter.system.constant.MenuShowType;
import com.chua.starter.system.mapper.SysPolicyConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 菜单动态显示条件求值器
 *
 * <p>按 {@code sys_menu_show_type} 判定一条菜单是否出现在当前用户的菜单树里，
 * 支持的条件类型见 {@link MenuShowType}。</p>
 *
 * <p><b>设计取向：宁可多显示，不要整棵树消失。</b>
 * 条件求值本身出错（SpEL 语法错、配置键缺失、功能键查不到）一律按「放行」并记 warn；
 * 只有条件<b>明确为假</b>时（类确实不在 classpath、开关确实关着、属性确实为 false）才隐藏。
 * 一个手滑的 SpEL 表达式不应该让管理员排查半天才发现菜单整片消失。</p>
 *
 * @since 4.0.0.50
 * @author CH
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MenuVisibilityEvaluator {

    /**
     * 统一策略配置映射器，用于读取 FEATURE 开关行
     */
    private final SysPolicyConfigMapper sysPolicyConfigMapper;

    /**
     * Spring 环境，用于 PROPERTY 条件
     */
    private final Environment environment;

    /**
     * SpEL 解析器
     */
    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * SpEL 表达式缓存，避免每次菜单查询都重新解析
     */
    private final Map<String, Object> expressionCache = new ConcurrentHashMap<>();

    /**
     * 判定单条菜单是否可见。
     *
     * @param menu 菜单
     * @return 是否可见
     */
    public boolean visible(SysMenu menu) {
        if (null == menu) {
            return false;
        }
        MenuShowType type = MenuShowType.of(menu.getSysMenuShowType());
        if (MenuShowType.ALWAYS == type) {
            return true;
        }
        return switch (type) {
            case CLASS -> visibleByClass(menu.getSysMenuShowValue());
            case SPEL -> visibleBySpel(menu.getSysMenuShowValue());
            case SWITCH -> visibleBySwitch(menu);
            case PROPERTY -> visibleByProperty(menu.getSysMenuShowValue());
            default -> true;
        };
    }

    /**
     * CLASS 条件：类是否在 classpath。
     *
     * @param className 类全名
     * @return 是否存在
     */
    private boolean visibleByClass(String className) {
        if (StringUtils.isBlank(className)) {
            return true;
        }
        try {
            return ClassUtils.isPresent(className.trim(), null == getClass() ? null : getClass().getClassLoader());
        } catch (Exception e) {
            log.warn("菜单 CLASS 条件求值失败，按放行处理: class={}", className, e);
            return true;
        }
    }

    /**
     * SPEL 条件：表达式求值为真才显示。
     *
     * <p>上下文暴露 {@code env}（Spring Environment），因此可写
     * {@code #env.getProperty('plugin.job.enable')} 这类表达式。</p>
     *
     * @param expression 表达式
     * @return 是否为真
     */
    private boolean visibleBySpel(String expression) {
        if (StringUtils.isBlank(expression)) {
            return true;
        }
        try {
            Object value = expressionCache.computeIfAbsent(expression.trim(), key -> {
                try {
                    return parser.parseExpression(key);
                } catch (Exception e) {
                    log.warn("菜单 SPEL 条件解析失败，将按放行处理: expression={}", key, e);
                    return Boolean.TRUE;
                }
            });
            if (value instanceof Boolean b) {
                return b;
            }
            if (value instanceof org.springframework.expression.Expression e) {
                StandardEvaluationContext ctx = new StandardEvaluationContext();
                ctx.setVariable("env", environment);
                ctx.setRootObject(environment);
                Object result = e.getValue(ctx);
                return !(result instanceof Boolean b) || b;
            }
            return true;
        } catch (Exception e) {
            log.warn("菜单 SPEL 条件求值失败，按放行处理: expression={}", expression, e);
            return true;
        }
    }

    /**
     * SWITCH 条件：与统一策略配置表里的功能开关行联动。
     *
     * <p>取值优先级：</p>
     * <ol>
     *   <li>若 {@code sys_menu_show_key} 非空，取该功能键对应行的 status</li>
     *   <li>否则取 {@code sys_menu_show_switch}：1 放行 / 0 隐藏（纯手动开关）</li>
     * </ol>
     *
     * @param menu 菜单
     * @return 是否可见
     */
    private boolean visibleBySwitch(SysMenu menu) {
        String featureKey = StringUtils.isBlank(menu.getSysMenuShowKey()) ? null : menu.getSysMenuShowKey().trim();
        if (null == featureKey) {
            return null == menu.getSysMenuShowSwitch() || menu.getSysMenuShowSwitch() == 1;
        }
        try {
            SysPolicyConfig row = sysPolicyConfigMapper.selectOne(
                    Wrappers.<SysPolicyConfig>lambdaQuery()
                            .eq(SysPolicyConfig::getType, SysPolicyConfig.TYPE_FEATURE)
                            .eq(SysPolicyConfig::getBizKey, featureKey)
                            .last("limit 1"));
            if (null == row) {
                // 库里有开关行才算联动；没有就退回菜单自身的开关值
                return null == menu.getSysMenuShowSwitch() || menu.getSysMenuShowSwitch() == 1;
            }
            return null != row.getStatus() && row.getStatus() == SysPolicyConfig.STATUS_ENABLED;
        } catch (Exception e) {
            log.warn("菜单 SWITCH 条件求值失败，按放行处理: feature={}", featureKey, e);
            return true;
        }
    }

    /**
     * PROPERTY 条件：Spring 配置项为真才显示。
     *
     * @param propertyKey 配置键
     * @return 是否为真
     */
    private boolean visibleByProperty(String propertyKey) {
        if (StringUtils.isBlank(propertyKey)) {
            return true;
        }
        try {
            String value = environment.getProperty(propertyKey.trim());
            return !"false".equalsIgnoreCase(value);
        } catch (Exception e) {
            log.warn("菜单 PROPERTY 条件求值失败，按放行处理: key={}", propertyKey, e);
            return true;
        }
    }
}
