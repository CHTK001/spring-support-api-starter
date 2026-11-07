// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.service.hotreload;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * 热重载配置切面值转换测试。
 *
 * @author CH
 * @since 4.0.0.51
 */
class HotReloadConfigAspectTest {

    @Test
    void shouldConvertScalarTypes() {
        assertEquals("true", HotReloadConfigAspect.convertValue("true", String.class));
        assertEquals(Boolean.TRUE, HotReloadConfigAspect.convertValue("true", Boolean.class));
        assertEquals(120, HotReloadConfigAspect.convertValue("120", Integer.class));
        assertEquals(120L, HotReloadConfigAspect.convertValue("120", Long.class));
        assertEquals(1.5D, HotReloadConfigAspect.convertValue("1.5", Double.class));
    }

    @Test
    void shouldReturnPrimitiveDefaultsWhenConfigMissing() {
        assertEquals(Boolean.FALSE, HotReloadConfigAspect.defaultValue(boolean.class));
        assertEquals(0, HotReloadConfigAspect.defaultValue(int.class));
        assertEquals(0L, HotReloadConfigAspect.defaultValue(long.class));
        org.junit.jupiter.api.Assertions.assertNull(HotReloadConfigAspect.defaultValue(String.class));
    }

    @Test
    void shouldConvertJsonTypes() {
        Object list = HotReloadConfigAspect.convertValue("[\"a\",\"b\"]", List.class);
        assertInstanceOf(List.class, list);
        assertEquals(2, ((List<?>) list).size());

        Object map = HotReloadConfigAspect.convertValue("{\"pageSize\":20}", Map.class);
        assertInstanceOf(Map.class, map);
        assertEquals(20, ((Map<?, ?>) map).get("pageSize"));
    }
}