package com.chua.starter.monitor.starter.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * Guacamole WebSocket拦截器
 *
 * @author CH
 * @since 2024/12/18
 */
@Component
@Slf4j
public class GuacamoleWebSocketInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        
        String uri = request.getURI().toString();
        log.info("WebSocket握手开始: uri={}, remoteAddress={}", uri, request.getRemoteAddress());
        
        // 可以在这里添加认证逻辑
        // 例如检查token、session等
        
        // 将请求信息存储到attributes中，供WebSocketHandler使用
        attributes.put("requestUri", uri);
        attributes.put("remoteAddress", request.getRemoteAddress());
        attributes.put("headers", request.getHeaders());
        
        return true; // 允许握手
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        
        String uri = request.getURI().toString();
        if (exception != null) {
            log.error("WebSocket握手失败: uri={}, error={}", uri, exception.getMessage(), exception);
        } else {
            log.info("WebSocket握手成功: uri={}", uri);
        }
    }
}
