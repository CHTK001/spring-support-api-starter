package com.chua.starter.tenant.properties;

import com.chua.starter.tenant.constant.TenantDbConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashSet;
import java.util.Set;

/**
 * 租户配置属性
 *
 * @version 1.0.0
 * @since 2024/12/06
 * @author CH
 */
@Data
@ConfigurationProperties(prefix = TenantProperties.PRE, ignoreInvalidFields = true)
public class TenantProperties {

    /** PRE */
    public static final String PRE = "plugin.mybatis-plus.tenant";

    /**
     * 忽略的表（不添加租户条件，通用配置）
     */
    private Set<String> ignoreTable = new HashSet<>();

    /**
     * 服务端配置
     */
    private ServerConfig server = new ServerConfig();

    /**
     * 客户端配置
     */
    private ClientConfig client = new ClientConfig();

    public Set<String> getIgnoreTable() {
        return new HashSet<>(ignoreTable);
    }

    public void setIgnoreTable(Set<String> ignoreTable) {
        this.ignoreTable = ignoreTable == null ? new HashSet<>() : new HashSet<>(ignoreTable);
    }

    public ServerConfig getServer() {
        return server == null ? new ServerConfig() : server.copy();
    }

    public void setServer(ServerConfig server) {
        this.server = server == null ? new ServerConfig() : server.copy();
    }

    public ClientConfig getClient() {
        return client == null ? new ClientConfig() : client.copy();
    }

    public void setClient(ClientConfig client) {
        this.client = client == null ? new ClientConfig() : client.copy();
    }

    /**
     * 判断是否为服务端模式
     *
     * @return 服务端启用时返回 true
     */
    public boolean isServerMode() {
        return server != null && server.enable;
    }

    /**
     * 判断是否为客户端模式
     *
     * @return 客户端启用时返回 true
     */
    public boolean isClientMode() {
        return client != null && client.enable;
    }

    /**
     * 判断服务端租户同步是否启用消息签名。
     *
     * @return 启用时返回 true
     */
    public boolean isServerSyncSignEnabled() {
        return server != null && hasText(server.syncSignSecret);
    }

    /**
     * 判断客户端租户同步是否启用消息签名。
     *
     * @return 启用时返回 true
     */
    public boolean isClientSyncSignEnabled() {
        return client != null && hasText(client.syncSignSecret);
    }

    /**
     * 判断服务端租户同步是否启用业务 ACK。
     *
     * @return 启用时返回 true
     */
    public boolean isServerSyncAckEnabled() {
        return server != null && server.syncAckEnabled;
    }

    /**
     * 判断客户端租户同步是否启用业务 ACK。
     *
     * @return 启用时返回 true
     */
    public boolean isClientSyncAckEnabled() {
        return client != null && client.syncAckEnabled;
    }

    /**
     * 获取租户ID字段名
     *
     * @return 租户ID字段名
     */
    public String getEffectiveTenantIdColumn() {
        return TenantDbConstants.TENANT_ID_COLUMN;
    }

    /**
     * 获取忽略的表
     * 合并通用配置和模式特定配置
     *
     * @return 忽略的表集合
     */
    public Set<String> getEffectiveIgnoreTable() {
        Set<String> result = new HashSet<>(ignoreTable);
        if (isClientMode()) {
            result.addAll(client.ignoreTable);
        }
        if (isServerMode()) {
            result.addAll(server.ignoreTable);
        }
        return result;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    /**
     * 服务端配置
     */
    @Data
    public static class ServerConfig {

        /**
         * 是否启用服务端模式
         */
        private boolean enable = false;

        /**
         * 是否启用同步下发
         */
        private boolean syncEnable = false;

        /**
         * 客户端注册后是否立即下发全量租户数据
         */
        private boolean syncFullPushOnRegister = true;

        /**
         * 全量同步校正间隔（秒），小于等于 0 表示关闭
         */
        private long syncFullPushIntervalSeconds = 300L;

        /**
         * 无法定向到租户客户端时是否回退为广播。
         * <p>
         * 默认关闭，避免将某个租户的完整数据误发给其它租户客户端。
         * </p>
         */
        private boolean syncBroadcastFallback = false;

        /**
         * 定时全量同步时，若租户数据未变化则跳过下发。
         */
        private boolean syncSkipUnchangedFullPush = true;

        /**
         * 租户同步消息签名密钥。
         * <p>
         * 配置后平台端会为租户同步消息签名（并在校验），租户端需要同步配置并校验签名。
         * </p>
         */
        private String syncSignSecret;

        /**
         * 租户同步签名允许的时钟偏移秒数。
         */
        private long syncSignAllowedClockSkewSeconds = 300L;

        /**
         * 是否启用租户同步业务 ACK 跟踪。
         */
        private boolean syncAckEnabled = true;

        /**
         * 等待租户客户端 ACK 的超时秒数。
         */
        private long syncAckTimeoutSeconds = 45L;

        /**
         * ACK 超时检查周期（秒）。
         */
        private long syncAckCheckIntervalSeconds = 15L;

        /**
         * 同一租户会话发生 ACK 超时后，触发全量补偿的冷却时间（秒）。
         */
        private long syncAckCompensationCooldownSeconds = 60L;

        /**
         * ACK 跟踪缓存上限。
         */
        private int syncAckMaxPending = 8192;

        /**
         * 忽略的表（不添加租户条件，追加到通用配置）
         */
        private Set<String> ignoreTable = new HashSet<>();

        /**
         * 通信协议: rsocket, websocket
         */
        private String protocol = "rsocket-sync";

        /**
         * 绑定地址
         */
        private String host = "0.0.0.0";

        /**
         * 监听端口
         */
        private int port = 19380;

        public boolean isEnable() {
            return enable;
        }

        public boolean isSyncEnable() {
            return syncEnable;
        }

        public boolean isSyncFullPushOnRegister() {
            return syncFullPushOnRegister;
        }

        public long getSyncFullPushIntervalSeconds() {
            return syncFullPushIntervalSeconds;
        }

        public boolean isSyncBroadcastFallback() {
            return syncBroadcastFallback;
        }

        public boolean isSyncSkipUnchangedFullPush() {
            return syncSkipUnchangedFullPush;
        }

        public String getSyncSignSecret() {
            return syncSignSecret;
        }

        public long getSyncSignAllowedClockSkewSeconds() {
            return syncSignAllowedClockSkewSeconds;
        }

        public boolean isSyncAckEnabled() {
            return syncAckEnabled;
        }

        public long getSyncAckTimeoutSeconds() {
            return syncAckTimeoutSeconds;
        }

        public long getSyncAckCheckIntervalSeconds() {
            return syncAckCheckIntervalSeconds;
        }

        public long getSyncAckCompensationCooldownSeconds() {
            return syncAckCompensationCooldownSeconds;
        }

        public int getSyncAckMaxPending() {
            return syncAckMaxPending;
        }

        public Set<String> getIgnoreTable() {
            return ignoreTable;
        }

        public String getProtocol() {
            return protocol;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public ServerConfig copy() {
            ServerConfig copy = new ServerConfig();
            copy.enable = this.enable;
            copy.syncEnable = this.syncEnable;
            copy.syncFullPushOnRegister = this.syncFullPushOnRegister;
            copy.syncFullPushIntervalSeconds = this.syncFullPushIntervalSeconds;
            copy.syncBroadcastFallback = this.syncBroadcastFallback;
            copy.syncSkipUnchangedFullPush = this.syncSkipUnchangedFullPush;
            copy.syncSignSecret = this.syncSignSecret;
            copy.syncSignAllowedClockSkewSeconds = this.syncSignAllowedClockSkewSeconds;
            copy.syncAckEnabled = this.syncAckEnabled;
            copy.syncAckTimeoutSeconds = this.syncAckTimeoutSeconds;
            copy.syncAckCheckIntervalSeconds = this.syncAckCheckIntervalSeconds;
            copy.syncAckCompensationCooldownSeconds = this.syncAckCompensationCooldownSeconds;
            copy.syncAckMaxPending = this.syncAckMaxPending;
            copy.ignoreTable = new HashSet<>(this.ignoreTable);
            copy.protocol = this.protocol;
            copy.host = this.host;
            copy.port = this.port;
            return copy;
        }
    }

    /**
     * 客户端配置
     */
    @Data
    public static class ClientConfig {

        /**
         * 是否启用客户端模式
         */
        private boolean enable = false;

        /**
         * 是否启用同步接收
         */
        private boolean syncEnable = false;

        /**
         * 当前租户ID
         */
        private String tenantId;

        /**
         * 租户同步消息签名密钥。
         * <p>
         * 配置后租户端会校验平台端下发的消息签名，为可选能力。
         * </p>
         */
        private String syncSignSecret;

        /**
         * 租户同步签名允许的时钟偏移秒数。
         */
        private long syncSignAllowedClockSkewSeconds = 300L;

        /**
         * 是否启用租户同步业务 ACK 回执。
         */
        private boolean syncAckEnabled = true;

        /**
         * 是否启用消息去重
         */
        private boolean syncDeduplicateMessages = true;

        /**
         * 消息去重窗口（秒）
         */
        private long syncDeduplicateWindowSeconds = 3600L;

        /**
         * 消息去重缓存上限
         */
        private int syncDeduplicateMaxSize = 4096;

        /**
         * 是否自动向 SyncClient 注册租户同步主题
         */
        private boolean syncAutoSubscribeTopics = true;

        /**
         * 连接建立后是否主动请求一次全量同步补偿。
         * <p>
         * 默认关闭，避免与服务端“注册即全量下发”重复。
         * 如服务端关闭注册全量推送，可按需开启。
         * </p>
         */
        private boolean syncRequestFullPushOnConnect = false;

        /**
         * 连接后主动全量同步补偿延迟（毫秒）。
         * <p>
         * 用于给服务端注册后的首轮推送留出时间，避免重复补偿。
         * </p>
         */
        private long syncRequestFullPushDelayMillis = 3000L;

        /**
         * 是否忽略乱序旧消息
         */
        private boolean syncIgnoreOutOfOrderMessages = true;

        /**
         * 忽略的表（不添加租户条件，追加到通用配置）
         */
        private Set<String> ignoreTable = new HashSet<>();

        /**
         * 是否自动添加租户字段到数据库表
         */
        private boolean autoAddColumn = false;

        /**
         * 通信协议: rsocket, websocket
         */
        private String protocol = "rsocket-sync";

        /**
         * 服务端地址
         */
        private String serverHost = "localhost";

        /**
         * 服务端端口
         */
        private int serverPort = 19380;

        public boolean isEnable() {
            return enable;
        }

        public boolean isSyncEnable() {
            return syncEnable;
        }

        public String getTenantId() {
            return tenantId;
        }

        public String getSyncSignSecret() {
            return syncSignSecret;
        }

        public long getSyncSignAllowedClockSkewSeconds() {
            return syncSignAllowedClockSkewSeconds;
        }

        public boolean isSyncAckEnabled() {
            return syncAckEnabled;
        }

        public boolean isSyncDeduplicateMessages() {
            return syncDeduplicateMessages;
        }

        public long getSyncDeduplicateWindowSeconds() {
            return syncDeduplicateWindowSeconds;
        }

        public int getSyncDeduplicateMaxSize() {
            return syncDeduplicateMaxSize;
        }

        public boolean isSyncAutoSubscribeTopics() {
            return syncAutoSubscribeTopics;
        }

        public boolean isSyncRequestFullPushOnConnect() {
            return syncRequestFullPushOnConnect;
        }

        public long getSyncRequestFullPushDelayMillis() {
            return syncRequestFullPushDelayMillis;
        }

        public boolean isSyncIgnoreOutOfOrderMessages() {
            return syncIgnoreOutOfOrderMessages;
        }

        public Set<String> getIgnoreTable() {
            return ignoreTable;
        }

        public boolean isAutoAddColumn() {
            return autoAddColumn;
        }

        public String getProtocol() {
            return protocol;
        }

        public String getServerHost() {
            return serverHost;
        }

        public int getServerPort() {
            return serverPort;
        }

        public ClientConfig copy() {
            ClientConfig copy = new ClientConfig();
            copy.enable = this.enable;
            copy.syncEnable = this.syncEnable;
            copy.tenantId = this.tenantId;
            copy.syncSignSecret = this.syncSignSecret;
            copy.syncSignAllowedClockSkewSeconds = this.syncSignAllowedClockSkewSeconds;
            copy.syncAckEnabled = this.syncAckEnabled;
            copy.syncDeduplicateMessages = this.syncDeduplicateMessages;
            copy.syncDeduplicateWindowSeconds = this.syncDeduplicateWindowSeconds;
            copy.syncDeduplicateMaxSize = this.syncDeduplicateMaxSize;
            copy.syncAutoSubscribeTopics = this.syncAutoSubscribeTopics;
            copy.syncRequestFullPushOnConnect = this.syncRequestFullPushOnConnect;
            copy.syncRequestFullPushDelayMillis = this.syncRequestFullPushDelayMillis;
            copy.syncIgnoreOutOfOrderMessages = this.syncIgnoreOutOfOrderMessages;
            copy.ignoreTable = new HashSet<>(this.ignoreTable);
            copy.autoAddColumn = this.autoAddColumn;
            copy.protocol = this.protocol;
            copy.serverHost = this.serverHost;
            copy.serverPort = this.serverPort;
            return copy;
        }
    }
}
