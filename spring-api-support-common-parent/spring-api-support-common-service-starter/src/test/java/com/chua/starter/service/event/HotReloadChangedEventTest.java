package com.chua.starter.service.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link HotReloadChangedEvent} 的契约测试。
 *
 * <p>锁定事件的对外约定：运行时消费者集合的显式携带与空值归一化、
 * 以及 {@code isForConsumer} 的匹配口径。</p>
 *
 * @author CH
 * @since 4.0.0.51
 */
@DisplayName("热更新事件契约测试")
class HotReloadChangedEventTest {

    @Test
    @DisplayName("事件显式携带运行时消费者")
    void shouldExposeRuntimeConsumers() {
        HotReloadChangedEvent event = new HotReloadChangedEvent(this, 1, "enabled", "runtime.enabled",
                "true", "boolean", 1, Set.of("runtime"));

        assertEquals("hot_reload", event.getGroup());
        assertTrue(event.isForConsumer("runtime"));
        assertFalse(event.isForConsumer("cache"));
    }
}
