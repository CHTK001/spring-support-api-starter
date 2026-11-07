package com.chua.starter.system.listener;

import com.chua.starter.service.SysSettingService;
import com.chua.starter.service.event.HotReloadChangedEvent;
import com.chua.starter.service.event.HotReloadRuntimeConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 热更新配置变更监听器。
 *
 * <p>接收 {@link HotReloadChangedEvent}，按事件声明的消费者标识分发给对应的
 * {@link HotReloadRuntimeConsumer}，并清理配置缓存，使下一次读取拿到新值。</p>
 *
 * @author CH
 * @since 4.0.0.51
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HotReloadChangedListener {

    private final SysSettingService sysSettingService;
    private final List<HotReloadRuntimeConsumer> runtimeConsumers;

    @EventListener
    public void onHotReloadChanged(HotReloadChangedEvent event) {
        try {
            sysSettingService.clearHotReloadCache();
        } catch (Exception e) {
            log.warn("热更新配置缓存清理失败: code={}, error={}", event.getCode(), e.getMessage());
        }
        for (HotReloadRuntimeConsumer consumer : runtimeConsumers) {
            if (!event.isForConsumer(consumer.consumerId())) {
                continue;
            }
            try {
                consumer.onHotReloadChanged(event);
            } catch (Exception e) {
                log.warn("热更新配置运行时消费者处理失败: consumer={}, code={}, error={}",
                        consumer.consumerId(), event.getCode(), e.getMessage());
            }
        }
    }
}
