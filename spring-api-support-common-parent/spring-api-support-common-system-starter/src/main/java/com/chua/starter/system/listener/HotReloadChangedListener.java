package com.chua.starter.system.listener;

import com.chua.starter.service.SysSettingService;
import com.chua.starter.service.event.HotReloadChangedEvent;
import com.chua.starter.service.event.HotReloadRuntimeConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

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
