package com.chua.starter.monitor.arthas.ws;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 简易 Holder，便于在 WebSocket 注册时安全获取 Bean
 */
@Component
public class ArthasWebSocketProxyHandlerHolder implements ApplicationContextAware {

    private static ApplicationContext CTX;
    private static ArthasWebSocketProxyHandler CACHED;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        CTX = applicationContext;
        try {
            CACHED = applicationContext.getBean(ArthasWebSocketProxyHandler.class);
        } catch (Exception ignored) {}
    }

    @Nullable
    public static ArthasWebSocketProxyHandler get() {
        if (CACHED != null) return CACHED;
        if (CTX == null) return null;
        try {
            return CTX.getBean(ArthasWebSocketProxyHandler.class);
        } catch (Exception e) {
            return null;
        }
    }
}

