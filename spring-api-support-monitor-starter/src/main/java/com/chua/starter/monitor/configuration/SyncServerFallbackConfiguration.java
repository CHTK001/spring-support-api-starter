package com.chua.starter.monitor.configuration;

import com.chua.starter.sync.support.pojo.ClientInfo;
import com.chua.starter.sync.support.server.SyncServer;
import com.chua.starter.sync.support.server.SyncServerInstance;
import com.chua.starter.sync.support.spi.SyncMessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * SyncServer 降级配置
 * <p>当 SyncAutoConfiguration 因配置原因（如 plugin.sync.server.enable=false）未创建 SyncServer Bean 时，
 * 提供一个无操作实现，避免依赖 SyncServer 的组件启动失败。</p>
 *
 * @author CH
 * @since 2024/12/05
 */
@Slf4j
@Configuration
@ConditionalOnClass(SyncServer.class)
@ConditionalOnMissingBean(SyncServer.class)
public class SyncServerFallbackConfiguration {

    @Bean
    public SyncServer noopSyncServer() {
        log.warn("[SyncServerFallback] 创建 SyncServer 无操作实现 - 实际 SyncServer 未启动");
        return new NoopSyncServer();
    }

    /**
     * SyncServer 无操作实现
     */
    static class NoopSyncServer extends SyncServer {
        private final Map<String, ClientInfo> emptyClients = Collections.emptyMap();

        NoopSyncServer() {
            super(new com.chua.starter.sync.support.properties.SyncProperties());
        }

        @Override
        public void afterPropertiesSet() {
            log.info("[SyncServerFallback] 无操作 SyncServer 已就绪");
        }

        @Override
        public Map<String, ClientInfo> getAllClients() {
            return emptyClients;
        }

        @Override
        public Map<String, ClientInfo> getAllClientsRaw() {
            return emptyClients;
        }

        @Override
        public void registerHandler(String topic, SyncMessageHandler handler) {
            log.debug("[SyncServerFallback] 忽略处理器注册: topic={}, handler={}", topic, handler.getName());
        }

        @Override
        public void addConnectListener(BiConsumer<String, ClientInfo> listener) {
        }

        @Override
        public void addDisconnectListener(BiConsumer<String, ClientInfo> listener) {
        }

        @Override
        public void send(String clientId, String topic, Object data) {
            log.debug("[SyncServerFallback] 忽略发送: clientId={}, topic={}", clientId, topic);
        }

        @Override
        public void broadcast(String topic, Object data) {
        }

        @Override
        public void broadcast(String sessionId, String topic, Object data) {
        }

        @Override
        public int getConnectionCount() {
            return 0;
        }

        @Override
        public Set<String> getConnectedSessions() {
            return Collections.emptySet();
        }

        @Override
        public SyncServerInstance getInstance(String name) {
            return null;
        }

        @Override
        public SyncServerInstance getDefaultInstance() {
            return null;
        }

        @Override
        public int publishByAppName(String appName, String topic, Object data) {
            return 0;
        }

        @Override
        public boolean publish(String clientId, int port, String topic, Object data) {
            return false;
        }

        @Override
        public boolean publish(String clientId, int port, String sessionId, String topic, Object data) {
            return false;
        }

        @Override
        public String findClientId(String host, int port, String appName) {
            return null;
        }

        @Override
        public List<String> findClientIdsByAppName(String appName) {
            return Collections.emptyList();
        }

        @Override
        public List<String> findClientIdsByHost(String host) {
            return Collections.emptyList();
        }

        @Override
        public List<String> findClientIdsByMetadata(String key, Object value) {
            return Collections.emptyList();
        }

        @Override
        public List<String> findClientIds(Predicate<ClientInfo> predicate) {
            return Collections.emptyList();
        }

        @Override
        public int publishByMetadata(String key, Object value, String topic, Object data) {
            return 0;
        }

        @Override
        public int publishToClients(Predicate<ClientInfo> predicate, String topic, Object data) {
            return 0;
        }
    }
}
