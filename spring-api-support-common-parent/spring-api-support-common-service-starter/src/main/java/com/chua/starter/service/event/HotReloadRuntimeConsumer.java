package com.chua.starter.service.event;

public interface HotReloadRuntimeConsumer {

    String consumerId();

    void onHotReloadChanged(HotReloadChangedEvent event);
}
