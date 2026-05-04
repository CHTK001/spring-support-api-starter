package com.chua.starter.monitor.configuration;

import com.chua.socket.support.session.SocketSession;
import com.chua.socket.support.session.SocketSessionTemplate;
import com.chua.socket.support.session.SocketUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.Collections;
import java.util.List;

/**
 * SocketSessionTemplate 降级配置
 * <p>当 plugin.socket.enable=false 时提供无操作实现。</p>
 */
@Slf4j
@Configuration
@ConditionalOnClass(SocketSessionTemplate.class)
@ConditionalOnMissingBean(SocketSessionTemplate.class)
public class SocketSessionTemplateFallbackConfiguration {

    @Bean
    @Primary
    public SocketSessionTemplate noopSocketSessionTemplate() {
        log.warn("[SocketFallback] SocketSessionTemplate 不可用 - 使用无操作实现");
        return new SocketSessionTemplate() {
            @Override
            public SocketSession save(String s, SocketSession socketSession) { return socketSession; }

            @Override
            public void remove(String s, SocketSession socketSession) {}

            @Override
            public SocketSession getSession(String s) { return null; }

            @Override
            public void send(String s, String s1, String s2) {}

            @Override
            public void broadcast(String s, String s1) {}

            @Override
            public void sendToUser(String s, String s1, String s2) {}

            @Override
            public List<SocketSession> getOnlineSessions() { return Collections.emptyList(); }

            @Override
            public List<SocketSession> getOnlineSession(String s, String s1) { return Collections.emptyList(); }

            @Override
            public List<SocketUser> getOnlineUsers(String s) { return Collections.emptyList(); }

            @Override
            public int getOnlineCount() { return 0; }

            @Override
            public void start() { log.info("[SocketFallback] 无操作 SocketSessionTemplate 已就绪"); }

            @Override
            public void stop() {}
        };
    }
}
