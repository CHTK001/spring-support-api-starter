package com.chua.starter.monitor.configuration;

import com.chua.starter.monitor.arthas.ws.ArthasWebSocketProxyHandlerHolder;
import com.chua.starter.monitor.websocket.GuacamoleWebSocketInterceptor;
import com.chua.starter.monitor.websocket.RdpWebSocketHandler;
import com.chua.starter.monitor.websocket.TestWebSocketHandler;
import com.chua.starter.monitor.websocket.VncWebSocketHandler;
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

                // RDP WebSocket端点（支持带/不带 /monitor/api 前缀，便于前端代理/网关转发）
                registry.addHandler(rdpWebSocketHandler,
                                "/websocket/rdp",
                                "/monitor/api/websocket/rdp")
                        .addInterceptors(webSocketInterceptor)
                        .setAllowedOriginPatterns("*");

                // VNC WebSocket端点（同上）
                registry.addHandler(vncWebSocketHandler,
                                "/websocket/vnc",
                                "/monitor/api/websocket/vnc")
                        .addInterceptors(webSocketInterceptor)
                        .setAllowedOriginPatterns("*");

                // 测试 WebSocket端点（同上）
                registry.addHandler(testWebSocketHandler,
                                "/websocket/test",
                                "/monitor/api/websocket/test")
                        .addInterceptors(webSocketInterceptor)
                        .setAllowedOriginPatterns("*");

                // Arthas 控制台 WebSocket 代理端点
                // 路径模式：/v1/arthas/console/{nodeId}/ws
                try {
                        var arthasHandler = ArthasWebSocketProxyHandlerHolder.get();
                        if (arthasHandler != null) {
                                registry.addHandler(arthasHandler,
                                                "/v1/arthas/console/ws",
                                                "/monitor/api/v1/arthas/console/ws")
                                        .addInterceptors(webSocketInterceptor)
                                        .setAllowedOriginPatterns("*");
                        }
                } catch (Throwable ignored) {
                }

        }
}
