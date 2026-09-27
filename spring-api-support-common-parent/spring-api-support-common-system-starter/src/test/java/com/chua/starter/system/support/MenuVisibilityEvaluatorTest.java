package com.chua.starter.system.support;

import com.chua.starter.entity.system.SysMenu;
import com.chua.starter.system.constant.MenuShowType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 菜单动态显示条件求值器单元测试
 *
 * <p>重点验证两条设计取向：</p>
 * <ol>
 *   <li>条件<b>明确为假</b>才隐藏（类不存在、开关关着、属性 false）</li>
 *   <li>条件<b>求值出错</b>一律放行，绝不让一条脏配置把菜单整片干掉</li>
 * </ol>
 *
 * <p>不启用 mockito（P3C40）；Mapper 传 null 走「查不到开关行」分支，
 * Environment 用 Spring 自带的 {@link StandardEnvironment} 加 {@link MapPropertySource}。</p>
 *
 * @since 4.0.0.50
 * @author CH
 */
@DisplayName("菜单动态显示条件求值器测试")
class MenuVisibilityEvaluatorTest {

    private MenuVisibilityEvaluator evaluator(Map<String, Object> props) {
        StandardEnvironment env = new StandardEnvironment();
        if (null != props && !props.isEmpty()) {
            env.getPropertySources().addFirst(new MapPropertySource("test", new HashMap<>(props)));
        }
        return new MenuVisibilityEvaluator(null, env);
    }

    private MenuVisibilityEvaluator evaluator() {
        return evaluator(null);
    }

    private SysMenu menu(String type, String value) {
        SysMenu m = new SysMenu();
        m.setSysMenuName("Test");
        m.setSysMenuShowType(type);
        m.setSysMenuShowValue(value);
        return m;
    }

    @Test
    @DisplayName("null菜单不可见")
    void testNullMenu() {
        assertFalse(evaluator().visible(null));
    }

    @Test
    @DisplayName("未配置条件类型时恒显示")
    void testAlwaysWhenBlankType() {
        MenuVisibilityEvaluator e = evaluator();
        assertTrue(e.visible(menu(null, null)));
        assertTrue(e.visible(menu("", null)));
        assertTrue(e.visible(menu("   ", null)));
    }

    @Test
    @DisplayName("未识别的条件类型退化为ALWAYS而不是隐藏")
    void testUnknownTypeFallsBackToAlways() {
        assertTrue(evaluator().visible(menu("NOT_A_TYPE", "whatever")));
    }

    @Test
    @DisplayName("类型解析大小写不敏感")
    void testTypeParsingCaseInsensitive() {
        assertEquals(MenuShowType.CLASS, MenuShowType.of("class"));
        assertEquals(MenuShowType.SPEL, MenuShowType.of(" Spel "));
        assertEquals(MenuShowType.ALWAYS, MenuShowType.of(null));
    }

    @Test
    @DisplayName("CLASS-类存在则显示")
    void testClassPresent() {
        assertTrue(evaluator().visible(menu("CLASS", "java.lang.String")));
    }

    @Test
    @DisplayName("CLASS-类不存在则隐藏")
    void testClassAbsent() {
        assertFalse(evaluator().visible(menu("CLASS", "com.not.exist.Nope")));
    }

    @Test
    @DisplayName("CLASS-条件值为空按放行处理")
    void testClassBlankValue() {
        assertTrue(evaluator().visible(menu("CLASS", "")));
    }

    @Test
    @DisplayName("SPEL-表达式为真则显示")
    void testSpelTrue() {
        assertTrue(evaluator().visible(menu("SPEL", "1 == 1")));
    }

    @Test
    @DisplayName("SPEL-表达式为假则隐藏")
    void testSpelFalse() {
        assertFalse(evaluator().visible(menu("SPEL", "1 == 2")));
    }

    @Test
    @DisplayName("SPEL-可访问env变量")
    void testSpelEnvAccess() {
        MenuVisibilityEvaluator e = evaluator(Map.of("demo.on", "true"));
        assertTrue(e.visible(menu("SPEL", "#env.getProperty('demo.on') == 'true'")));
    }

    @Test
    @DisplayName("SPEL-语法错误按放行处理而不是隐藏")
    void testSpelBrokenFallsBackToVisible() {
        assertTrue(evaluator().visible(menu("SPEL", "1 ==== (((")));
    }

    @Test
    @DisplayName("PROPERTY-属性为true显示，为false隐藏")
    void testProperty() {
        MenuVisibilityEvaluator e = evaluator(Map.of("demo.yes", "true", "demo.no", "false"));
        assertTrue(e.visible(menu("PROPERTY", "demo.yes")));
        assertFalse(e.visible(menu("PROPERTY", "demo.no")));
    }

    @Test
    @DisplayName("PROPERTY-属性缺失按放行处理")
    void testPropertyMissing() {
        assertTrue(evaluator().visible(menu("PROPERTY", "demo.absent")));
    }

    @Test
    @DisplayName("SWITCH-未配联动键时用手工开关值")
    void testSwitchManualValue() {
        MenuVisibilityEvaluator e = evaluator();
        SysMenu on = menu("SWITCH", null);
        on.setSysMenuShowSwitch(1);
        SysMenu off = menu("SWITCH", null);
        off.setSysMenuShowSwitch(0);
        assertTrue(e.visible(on));
        assertFalse(e.visible(off));
    }

    @Test
    @DisplayName("SWITCH-未配开关值默认为放行")
    void testSwitchNullValue() {
        assertTrue(evaluator().visible(menu("SWITCH", null)));
    }

    @Test
    @DisplayName("SWITCH-配了联动键但Mapper为null时按手工开关兜底")
    void testSwitchFeatureWithoutMapper() {
        SysMenu m = menu("SWITCH", null);
        m.setSysMenuShowKey("some-feature");
        m.setSysMenuShowSwitch(1);
        assertTrue(evaluator().visible(m));
    }
}
