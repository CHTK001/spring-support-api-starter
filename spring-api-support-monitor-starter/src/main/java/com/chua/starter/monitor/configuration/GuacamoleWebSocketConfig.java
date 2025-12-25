package com.chua.starter.monitor.configuration;

import com.chua.starter.monitor.websocket.GuacamoleWebSocketInterceptor;
import com.chua.starter.monitor.websocket.TestWebSocketHandler;
import com.chua.starter.monitor.websocket.connection.RdpWebSocketHandler;
import com.chua.starter.monitor.websocket.connection.SshWebSocketHandler;
import com.chua.starter.monitor.websocket.connection.VncWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

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
        private final SshWebSocketHandler sshWebSocketHandler;
        private final TestWebSocketHandler testWebSocketHandler;
        private final GuacamoleWebSocketInterceptor webSocketInterceptor;

        @Bean
        public ServerEndpointExporter serverEndpointExporter() {
                return new ServerEndpointExporter();
        }

        @Override
        public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

                // RDP WebSocket端点（支持带/不带 /monitor/api 前缀，便于前端代理/网关转发）
                registry.addHandler(rdpWebSocketHandler,
                                "/websocket/rdp")
                        .addInterceptors(webSocketInterceptor)
                        .setAllowedOriginPatterns("*");

                // VNC WebSocket端点（同上）
                registry.addHandler(vncWebSocketHandler,
                                "/websocket/vnc")
                        .addInterceptors(webSocketInterceptor)
                        .setAllowedOriginPatterns("*");

                // 测试 WebSocket端点（同上）
                registry.addHandler(testWebSocketHandler,
                                "/websocket/test")
                        .addInterceptors(webSocketInterceptor)
                        .setAllowedOriginPatterns("*");

                // SSH WebSocket端点
                registry.addHandler(sshWebSocketHandler,
                                "/websocket/ssh")
                        .addInterceptors(webSocketInterceptor)
                        .setAllowedOriginPatterns("*");

//                // Arthas 控制台 WebSocket 代理端点
//                // 路径模式：/v1/arthas/console/{nodeId}/ws
//                try {
//                        var arthasHandler = ArthasWebSocketProxyHandlerHolder.get();
//                        if (arthasHandler != null) {
//                                registry.addHandler(arthasHandler,
//                                                "/v1/arthas/console/ws")
//                                        .addInterceptors(webSocketInterceptor)
//                                        .setAllowedOriginPatterns("*");
//                        }
//                } catch (Throwable ignored) {
//                }

        }
}
