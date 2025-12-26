package com.chua.starter.monitor.arthas.config;

import com.chua.starter.monitor.arthas.ws.ArthasWebSocketProxyHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * Arthas WebSocket配置
 * 仅用于Arthas控制台代理功能
 *
 * @author CH
 * @since 2024/12/25
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
@Slf4j
@ConditionalOnClass(ServerEndpointExporter.class)
public class ArthasWebSocketConfig implements WebSocketConfigurer {

    private final ArthasWebSocketProxyHandler arthasWebSocketProxyHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        try {
            // Arthas 控制台 WebSocket 代理端点
            registry.addHandler(arthasWebSocketProxyHandler, "/v1/arthas/console/ws")
                    .setAllowedOriginPatterns("*");

            log.info("Arthas WebSocket代理端点已注册: /v1/arthas/console/ws");
        } catch (Exception e) {
            log.warn("Arthas WebSocket配置失败: {}", e.getMessage());
        }
    }
}
