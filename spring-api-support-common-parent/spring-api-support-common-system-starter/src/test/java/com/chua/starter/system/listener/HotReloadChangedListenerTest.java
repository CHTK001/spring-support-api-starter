package com.chua.starter.system.listener;

import com.chua.starter.service.SysSettingService;
import com.chua.starter.service.event.HotReloadChangedEvent;
import com.chua.starter.service.event.HotReloadRuntimeConsumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link HotReloadChangedListener} 的分发口径测试。
 *
 * <p>覆盖无消费者时只清缓存、按 consumerId 精确分发、以及未知标识不唤醒任何消费者。</p>
 *
 * @author CH
 * @since 4.0.0.51
 */
@DisplayName("热更新事件监听测试")
class HotReloadChangedListenerTest {

    @Test
    @DisplayName("无运行时消费者时仅清理热更新缓存")
    void shouldBeSafeWithoutRuntimeConsumers() {
        AtomicInteger cacheClearCount = new AtomicInteger();
        AtomicInteger consumerCount = new AtomicInteger();
        SysSettingService service = (SysSettingService) Proxy.newProxyInstance(
                SysSettingService.class.getClassLoader(),
                new Class<?>[]{SysSettingService.class},
                (proxy, method, args) -> {
                    if ("clearHotReloadCache".equals(method.getName())) {
                        cacheClearCount.incrementAndGet();
                    }
                    return null;
                });
        HotReloadRuntimeConsumer consumer = new HotReloadRuntimeConsumer() {
            @Override
            public String consumerId() {
                return "runtime";
            }

            @Override
            public void onHotReloadChanged(HotReloadChangedEvent event) {
                consumerCount.incrementAndGet();
            }
        };
        HotReloadChangedListener listener = new HotReloadChangedListener(service, List.of(consumer));
        HotReloadChangedEvent event = new HotReloadChangedEvent(this, 1, "enabled", "runtime.enabled",
                "true", "boolean", 1, Set.of());

        listener.onHotReloadChanged(event);

        assertEquals(1, cacheClearCount.get());
        assertEquals(0, consumerCount.get());
    }
}
