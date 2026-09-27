package com.chua.starter.service.hotreload;

import com.chua.starter.service.event.HotReloadChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 热重载事件桥接器。
 *
 * <p>监听事务提交后发布的配置变更事件，清理 {@link HotReloadConfigService} 的 Redis 缓存，
 * 使下一次业务调用立即读取到新值（lazy reload）。</p>
 *
 * @since 4.0.0.43
 * @author CH
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HotReloadConfigEventBridge {

    private final HotReloadConfigService hotReloadConfigService;

    /**
     * 配置保存后清理缓存。
     *
     * @param event 热重载事件
     */
    @EventListener
    public void onHotReloadChanged(HotReloadChangedEvent event) {
        if (null != event.getCode() && !event.getCode().isBlank()) {
            hotReloadConfigService.evict(event.getCode());
        } else {
            hotReloadConfigService.evictAll();
        }
    }
}
