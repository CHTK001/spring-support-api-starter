package com.chua.starter.tenant.properties;

import com.chua.starter.tenant.constant.TenantDbConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashSet;
import java.util.Set;

/**
 * 租户配置属性
 *
   * @版本 1.0.0
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

    /**
     * 获取ignoretable
     *
     * @return 获取ignoretable的结果
     */
    public Set<String> getIgnoreTable() {
        return new HashSet<>(ignoreTable);
    }

    /**
     * 设置ignoretable
     *
     * @param ignoreTable ignoreTable
     */
    public void setIgnoreTable(Set<String> ignoreTable) {
        this.ignoreTable = ignoreTable == null ? new HashSet<>() : new HashSet<>(ignoreTable);
    }

    /**
     * 获取服务端
     *
     * @return 获取服务端的结果
     */
    public ServerConfig getServer() {
        return server == null ? new ServerConfig() : server.copy();
    }

    /**
     * 设置服务端
     *
     * @param server 服务端
     */
    public void setServer(ServerConfig server) {
        this.server = server == null ? new ServerConfig() : server.copy();
    }

    /**
     * 获取客户端
     *
     * @return 获取客户端的结果
     */
    public ClientConfig getClient() {
        return client == null ? new ClientConfig() : client.copy();
    }

    /**
     * 设置客户端
     *
     * @param client 客户端
     */
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
      * 获取租户标识字段名
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

    /**
     * 是否拥有文本
     *
     * @param value 值
     * @return 是否包含文本的结果
     */
    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    /**
     * 服务端配置
     * @author CH
     * @since 4.0.0
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
          * 通信协议: rsocket, WebSocket
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

        /**
         * 是否启用
         *
         * @return 是否enable的结果
         */
        public boolean isEnable() {
            return enable;
        }

        /**
         * 是否同步启用
         *
         * @return 是否同步enable的结果
         */
        public boolean isSyncEnable() {
            return syncEnable;
        }

        /**
         * 是否同步完整推送On注册
         *
         * @return 是否同步完整pushon注册的结果
         */
        public boolean isSyncFullPushOnRegister() {
            return syncFullPushOnRegister;
        }

        /**
         * 获取同步完整推送间隔seconds
         *
         * @return 获取同步完整push间隔seconds的结果
         */
        public long getSyncFullPushIntervalSeconds() {
            return syncFullPushIntervalSeconds;
        }

        /**
         * 是否同步broadcast降级
         *
         * @return 是否同步broadcast降级的结果
         */
        public boolean isSyncBroadcastFallback() {
            return syncBroadcastFallback;
        }

        /**
         * 是否同步跳过unchanged完整推送
         *
         * @return 是否同步跳过unchanged完整push的结果
         */
        public boolean isSyncSkipUnchangedFullPush() {
            return syncSkipUnchangedFullPush;
        }

        /**
         * 获取同步标志secret
         *
         * @return 获取同步标志secret的结果
         */
        public String getSyncSignSecret() {
            return syncSignSecret;
        }

        /**
         * 获取同步标志allowedclockskewseconds
         *
         * @return 获取同步标志allowedclockskewseconds的结果
         */
        public long getSyncSignAllowedClockSkewSeconds() {
            return syncSignAllowedClockSkewSeconds;
        }

        /**
         * 是否同步ACK已启用
         *
         * @return 是否同步ACK已启用的结果
         */
        public boolean isSyncAckEnabled() {
            return syncAckEnabled;
        }

        /**
         * 获取同步ACK超时seconds
         *
         * @return 获取同步ACK超时seconds的结果
         */
        public long getSyncAckTimeoutSeconds() {
            return syncAckTimeoutSeconds;
        }

        /**
         * 获取同步ACK校验间隔seconds
         *
         * @return 获取同步ACK检查间隔seconds的结果
         */
        public long getSyncAckCheckIntervalSeconds() {
            return syncAckCheckIntervalSeconds;
        }

        /**
         * 获取同步ACK报酬cooldownseconds
         *
         * @return 获取同步ACK报酬cooldownseconds的结果
         */
        public long getSyncAckCompensationCooldownSeconds() {
            return syncAckCompensationCooldownSeconds;
        }

        /**
         * 获取同步ACK最大值Pending
         *
         * @return 获取同步ACK最大pending的结果
         */
        public int getSyncAckMaxPending() {
            return syncAckMaxPending;
        }

        /**
         * 获取ignoretable
         *
         * @return 获取ignoretable的结果
         */
        public Set<String> getIgnoreTable() {
            return ignoreTable;
        }

        /**
         * 获取协议
         *
         * @return 获取协议的结果
         */
        public String getProtocol() {
            return protocol;
        }

        /**
         * 获取主机
         *
         * @return 获取主机的结果
         */
        public String getHost() {
            return host;
        }

        /**
         * 获取端口
         *
         * @return 获取端口的结果
         */
        public int getPort() {
            return port;
        }

        /**
         * 复制
         *
         * @return 副本的结果
         */
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
     * @author CH
     * @since 4.0.0
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
          * 当前租户标识
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
          * 是否自动向 同步客户端 注册租户同步主题
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
          * 通信协议: rsocket, WebSocket
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

        /**
         * 是否启用
         *
         * @return 是否enable的结果
         */
        public boolean isEnable() {
            return enable;
        }

        /**
         * 是否同步启用
         *
         * @return 是否同步enable的结果
         */
        public boolean isSyncEnable() {
            return syncEnable;
        }

        /**
         * 获取tenantid
         *
         * @return 获取tenantid的结果
         */
        public String getTenantId() {
            return tenantId;
        }

        /**
         * 获取同步标志secret
         *
         * @return 获取同步标志secret的结果
         */
        public String getSyncSignSecret() {
            return syncSignSecret;
        }

        /**
         * 获取同步标志allowedclockskewseconds
         *
         * @return 获取同步标志allowedclockskewseconds的结果
         */
        public long getSyncSignAllowedClockSkewSeconds() {
            return syncSignAllowedClockSkewSeconds;
        }

        /**
         * 是否同步ACK已启用
         *
         * @return 是否同步ACK已启用的结果
         */
        public boolean isSyncAckEnabled() {
            return syncAckEnabled;
        }

        /**
         * 是否同步deduplicate消息
         *
         * @return 是否同步deduplicate消息的结果
         */
        public boolean isSyncDeduplicateMessages() {
            return syncDeduplicateMessages;
        }

        /**
         * 获取同步deduplicate窗口seconds
         *
         * @return 获取同步deduplicate窗口seconds的结果
         */
        public long getSyncDeduplicateWindowSeconds() {
            return syncDeduplicateWindowSeconds;
        }

        /**
         * 获取同步deduplicate最大值获取大小
         *
         * @return 获取同步deduplicate最大大小的结果
         */
        public int getSyncDeduplicateMaxSize() {
            return syncDeduplicateMaxSize;
        }

        /**
         * 是否同步auto订阅Topics
         *
         * @return 是否同步auto订阅topics的结果
         */
        public boolean isSyncAutoSubscribeTopics() {
            return syncAutoSubscribeTopics;
        }

        /**
         * 是否同步请求完整推送On连接
         *
         * @return 是否同步请求完整pushon连接的结果
         */
        public boolean isSyncRequestFullPushOnConnect() {
            return syncRequestFullPushOnConnect;
        }

        /**
         * 获取同步请求完整推送延迟millis
         *
         * @return 获取同步请求完整push延迟millis的结果
         */
        public long getSyncRequestFullPushDelayMillis() {
            return syncRequestFullPushDelayMillis;
        }

        /**
         * 是否同步ignore出的订单消息
         *
         * @return 是否同步ignore出的订单消息的结果
         */
        public boolean isSyncIgnoreOutOfOrderMessages() {
            return syncIgnoreOutOfOrderMessages;
        }

        /**
         * 获取ignoretable
         *
         * @return 获取ignoretable的结果
         */
        public Set<String> getIgnoreTable() {
            return ignoreTable;
        }

        /**
         * 是否Auto添加Column
         *
         * @return 是否auto添加column的结果
         */
        public boolean isAutoAddColumn() {
            return autoAddColumn;
        }

        /**
         * 获取协议
         *
         * @return 获取协议的结果
         */
        public String getProtocol() {
            return protocol;
        }

        /**
         * 获取服务端主机
         *
         * @return 获取服务端主机的结果
         */
        public String getServerHost() {
            return serverHost;
        }

        /**
         * 获取服务端端口
         *
         * @return 获取服务端端口的结果
         */
        public int getServerPort() {
            return serverPort;
        }

        /**
         * 复制
         *
         * @return 副本的结果
         */
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
