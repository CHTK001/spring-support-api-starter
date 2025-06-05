package com.chua.starter.monitor.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * websocket服务配置
 *
 * @author CH
 * @since 2025/6/5 11:45
 */
//@Configuration
//@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class MonitorWebSocketMessageBrokerConfigurer implements WebSocketMessageBrokerConfigurer {

    final MonitorHandshakeInterceptor serverInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/monitor-command")
                .addInterceptors(serverInterceptor)
                .withSockJS();
    }
}
