package com.chua.starter.monitor.configuration;

import com.chua.starter.monitor.starter.websocket.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Guacamole WebSocket配置
 *
 * @author CH
 * @since 2024/12/18
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class GuacamoleWebSocketConfig implements WebSocketConfigurer {

    private final RdpWebSocketHandler rdpWebSocketHandler;
    private final VncWebSocketHandler vncWebSocketHandler;
    private final TestWebSocketHandler testWebSocketHandler;
    private final GuacamoleWebSocketInterceptor webSocketInterceptor;
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

        // RDP WebSocket端点 (原有实现，保留用于对比测试)
        registry.addHandler(rdpWebSocketHandler, "/websocket/rdp")
                .addInterceptors(webSocketInterceptor)
                .setAllowedOrigins("*");

        // VNC WebSocket端点 (原有实现，保留用于对比测试)
        registry.addHandler(vncWebSocketHandler, "/websocket/vnc")
                .addInterceptors(webSocketInterceptor)
                .setAllowedOrigins("*");

        // 测试 WebSocket端点
        registry.addHandler(testWebSocketHandler, "/websocket/test")
                .addInterceptors(webSocketInterceptor)
                .setAllowedOrigins("*");
    }
}
