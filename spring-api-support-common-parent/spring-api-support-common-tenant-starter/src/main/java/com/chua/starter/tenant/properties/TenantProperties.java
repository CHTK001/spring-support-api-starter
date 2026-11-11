package com.chua.starter.tenant.properties;

import com.chua.starter.tenant.constant.TenantDbConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashSet;
import java.util.Set;

/**
 * 租户配置属性
 *
 * @since 2024/12/06
 * @author CH
 */
@Data
@ConfigurationProperties(prefix = TenantProperties.PRE, ignoreInvalidFields = true)
public class TenantProperties {

    /**
     * PRE
    */
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
         * 是否启用「按租户切换数据源」。
         * <p>
         * 默认开启：租户是否使用独立库由前端可视化配置（实例来源 + 库名）决定，
         * 未登记库的租户仍然走主库，行为不变。显式设置为 {@code false} 可整体关掉切库
         * （仅建议在排障时临时使用）。
         * </p>
         */
        private boolean datasourceRoutingEnable = true;

        /**
         * 租户数据源在 {@link com.chua.starter.datasource.support.DataSourceContextSupport}
         * 中的注册前缀，实际注册名为 {@code prefix + tenantId}。
         *
         * <p>默认用 {@code tenant-} 而不是 {@code tenant_}：{@code DataSourceGroupRegistry}
         * 会把「{@code 前缀_数字}」当成负载均衡分组（{@code slave_1}/{@code slave_2} → {@code slave}），
         * 而租户标识恰好是雪花数字，用下划线会让所有租户被收进同一个 {@code tenant} 组，
         * 导致 {@code @DS("tenant")} 随机串到别的租户库，且删除租户后组里仍留着已关闭的池。</p>
         */
        private String datasourceRoutingPrefix = "tenant-";

        /**
         * 租户数据源连接池最大连接数。
         */
        private int datasourceRoutingMaxPoolSize = 10;

        /**
         * 租户数据源连接池最小空闲连接数。
         */
        private int datasourceRoutingMinIdle = 1;

        /**
         * 租户数据源连接超时（毫秒）。
         */
        private long datasourceRoutingConnectionTimeoutMillis = 30000L;

        /**
         * 建池失败后的冷却时长（毫秒）。
         * <p>租户库宕机期间，每个请求都会尝试建池并等满连接超时，把请求线程逐个拖住。
         * 失败后进入冷却期，冷却期内直接回退主库，冷却结束才重试。</p>
         */
        private long datasourceRoutingFailureCooldownMillis = 30000L;

        /**
         * 租户数据源 JDBC 驱动类名；留空则按租户库 URL 交给驱动管理器自动识别。
         */
        private String datasourceRoutingDriverClassName;

        /**
         * 租户库 JDBC URL 模板。
         *
         * <p>占位符：{@code {host}} / {@code {port}} / {@code {dbname}}，端口为空时 {@code {port}}
         * 会被替换成空串。默认值是 MySQL 模板；主库是 PostgreSQL/H2 等其它库时改这里即可，
         * 无需改代码。</p>
         */
        private String datasourceRoutingUrlTemplate =
                "jdbc:mysql://{host}{port}/{dbname}"
                        + "?useUnicode=true&characterEncoding=utf8&useSSL=false"
                        + "&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai";

        /**
         * 租户标识的可信来源，决定「按哪个值切库」。
         *
         * <ul>
         *   <li>{@code session}（默认）——只认登录会话里的 {@code tenantId}。
         *       {@code X-Tenant-Id} 请求头<b>完全忽略</b>，因为请求头由客户端控制，
         *       改一个头就能读到别的租户库，等于没隔离。</li>
         *   <li>{@code header}——只认 {@code X-Tenant-Id} 请求头。仅供「平台管理员跨租户查询」
         *       这类场景使用，<b>必须</b>同时在接口层做租户授权校验，否则任何人都能切库。</li>
         *   <li>{@code session-or-header}——优先会话，会话无租户时才用请求头。</li>
         * </ul>
         */
        private String datasourceRoutingTenantSource = "session";

        /**
         * 有租户身份但拿不到该租户库时，是否拒绝请求（失败关闭）。
         *
         * <p><b>为什么默认开启</b>：租户库未登记、连不上、口令错时如果静默回退主库，
         * 租户用户的查询会读到平台共享库的数据，写操作更会直接落到主库——
         * 这是数据越权访问，而不是可用性降级，且全程没有任何报错。开启后这类请求返回
         * HTTP 503，故障只在受影响的租户身上可见。</p>
         *
         * <p>关闭（{@code false}）恢复旧行为：回退主库并打 WARN。仅适用于
         * 「部分租户已切库、存量租户仍在共享库」的迁移期，<b>不能</b>在已完成切库的环境里使用。</p>
         */
        private boolean datasourceRoutingFailClosed = true;

        /**
         * 建租户时是否自动建库/建账号。
         * <p>默认开启：管理连接按租户所选的实例来源解析（自填 URL / 平台自身实例 / 数据源台账），
         * 不再要求额外配置 {@code datasource-routing-admin-url}。显式设为 {@code false} 可关闭
         * 自动建库（库与账号由 DBA 预先准备好）。</p>
         */
        private boolean datasourceRoutingAutoProvision = true;

        /**
         * 建库用的管理连接 URL（连到实例而非某个租户库，例如
         * {@code jdbc:mysql://127.0.0.1:3306/mysql}）。
         */
        private String datasourceRoutingAdminUrl;

        /**
         * 建库用的管理连接账号。
         */
        private String datasourceRoutingAdminUsername;

        /**
         * 建库用的管理连接口令（支持 {@code ENC:} 前缀密文）。
         */
        private String datasourceRoutingAdminPassword;

        /**
         * 建库后是否自动初始化业务表结构。
         *
         * <p>只建库建账号得到的是一张空库：切过去之后第一条业务 SQL 就会报表不存在。
         * 开启后会用租户自己的账号连上该库，执行与平台主库同一批
         * {@code db/init} 迁移脚本（{@code Flyway} SPI，版本记录写进租户库的
         * {@code sys_database_version}），因此重复执行幂等、后续增量脚本也能补上。</p>
         *
         * <p>需要 {@link #datasourceRoutingAutoProvision} 一起开启；关闭时只建库不建表，
         * 由租户应用自行迁移。默认开启，避免「库建好了但没人建表」这种静默半成品。</p>
         */
        private boolean datasourceRoutingAutoInitScripts = true;

        /**
         * 租户库初始化脚本位置。
         *
         * <p>默认与平台自身的 {@code plugin.datasource.script.script-path} 保持一致 ——
         * 租户库和主库的表结构本来就是同一套，分叉只会让以后每次改表漏掉一边。
         * 支持逗号、分号、换行分隔的多个位置。</p>
         */
        private String datasourceRoutingInitScriptLocations =
                "classpath*:db/init/*.sql,classpath*:db/migration/*.sql";

        /**
         * 租户库初始化时是否同时执行种子数据脚本（{@code __initdata_}）。
         *
         * <p>默认开启：没有 {@code sys_role}/{@code sys_user} 这类基础数据的租户库
         * 连登录都做不到，比空库更难排查。已有数据的库由
         * {@code INSERT IGNORE} 与版本记录共同保证不重复插入。</p>
         */
        private boolean datasourceRoutingInitDataEnable = true;


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
         * 是否启用按租户切换数据源
         *
         * @return 启用时返回 true
         */
        public boolean isDatasourceRoutingEnable() {
            return datasourceRoutingEnable;
        }

        /**
         * 获取租户数据源注册前缀
         *
         * @return 注册前缀
         */
        public String getDatasourceRoutingPrefix() {
            return datasourceRoutingPrefix;
        }

        /**
         * 获取租户数据源连接池最大连接数
         *
         * @return 最大连接数
         */
        public int getDatasourceRoutingMaxPoolSize() {
            return datasourceRoutingMaxPoolSize;
        }

        /**
         * 获取租户数据源连接池最小空闲连接数
         *
         * @return 最小空闲连接数
         */
        public int getDatasourceRoutingMinIdle() {
            return datasourceRoutingMinIdle;
        }

        /**
         * 获取租户数据源连接超时（毫秒）
         *
         * @return 连接超时毫秒数
         */
        public long getDatasourceRoutingConnectionTimeoutMillis() {
            return datasourceRoutingConnectionTimeoutMillis;
        }

        /**
         * 获取建池失败冷却时长（毫秒）
         *
         * @return 冷却毫秒数
         */
        public long getDatasourceRoutingFailureCooldownMillis() {
            return datasourceRoutingFailureCooldownMillis;
        }

        /**
         * 获取租户数据源 JDBC 驱动类名
         *
         * @return 驱动类名，未配置时为 null
         */
        public String getDatasourceRoutingDriverClassName() {
            return datasourceRoutingDriverClassName;
        }

        /**
         * 获取租户库 JDBC URL 模板
         *
         * @return URL 模板，含 {host}/{port}/{dbname} 占位符
         */
        public String getDatasourceRoutingUrlTemplate() {
            return datasourceRoutingUrlTemplate;
        }

        /**
         * 获取租户标识来源
         *
         * @return session / header / session-or-header
         */
        public String getDatasourceRoutingTenantSource() {
            return datasourceRoutingTenantSource;
        }

        /**
         * 有租户身份但拿不到租户库时是否拒绝请求（失败关闭）
         *
         * @return 拒绝时返回 true
         */
        public boolean isDatasourceRoutingFailClosed() {
            return datasourceRoutingFailClosed;
        }

        /**
         * 建租户时是否自动建库/建账号
         *
         * @return 启用时返回 true
         */
        public boolean isDatasourceRoutingAutoProvision() {
            return datasourceRoutingAutoProvision;
        }

        /**
         * 获取建库管理连接 URL
         *
         * @return 管理连接 URL
         */
        public String getDatasourceRoutingAdminUrl() {
            return datasourceRoutingAdminUrl;
        }

        /**
         * 获取建库管理连接账号
         *
         * @return 管理连接账号
         */
        public String getDatasourceRoutingAdminUsername() {
            return datasourceRoutingAdminUsername;
        }

        /**
         * 获取建库管理连接口令
         *
         * @return 管理连接口令
         */
        public String getDatasourceRoutingAdminPassword() {
            return datasourceRoutingAdminPassword;
        }

        /**
         * 建库后是否自动初始化业务表结构
         *
         * @return 启用时返回 true
         */
        public boolean isDatasourceRoutingAutoInitScripts() {
            return datasourceRoutingAutoInitScripts;
        }

        /**
         * 获取租户库初始化脚本位置
         *
         * @return 逗号分隔的脚本位置
         */
        public String getDatasourceRoutingInitScriptLocations() {
            return datasourceRoutingInitScriptLocations;
        }

        /**
         * 租户库初始化时是否同时执行种子数据脚本
         *
         * @return 启用时返回 true
         */
        public boolean isDatasourceRoutingInitDataEnable() {
            return datasourceRoutingInitDataEnable;
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
            copy.datasourceRoutingEnable = this.datasourceRoutingEnable;
            copy.datasourceRoutingPrefix = this.datasourceRoutingPrefix;
            copy.datasourceRoutingMaxPoolSize = this.datasourceRoutingMaxPoolSize;
            copy.datasourceRoutingMinIdle = this.datasourceRoutingMinIdle;
            copy.datasourceRoutingConnectionTimeoutMillis = this.datasourceRoutingConnectionTimeoutMillis;
            copy.datasourceRoutingFailureCooldownMillis = this.datasourceRoutingFailureCooldownMillis;
            copy.datasourceRoutingDriverClassName = this.datasourceRoutingDriverClassName;
            copy.datasourceRoutingUrlTemplate = this.datasourceRoutingUrlTemplate;
            copy.datasourceRoutingTenantSource = this.datasourceRoutingTenantSource;
            copy.datasourceRoutingFailClosed = this.datasourceRoutingFailClosed;
            copy.datasourceRoutingAutoProvision = this.datasourceRoutingAutoProvision;
            copy.datasourceRoutingAdminUrl = this.datasourceRoutingAdminUrl;
            copy.datasourceRoutingAdminUsername = this.datasourceRoutingAdminUsername;
            copy.datasourceRoutingAdminPassword = this.datasourceRoutingAdminPassword;
            copy.datasourceRoutingAutoInitScripts = this.datasourceRoutingAutoInitScripts;
            copy.datasourceRoutingInitScriptLocations = this.datasourceRoutingInitScriptLocations;
            copy.datasourceRoutingInitDataEnable = this.datasourceRoutingInitDataEnable;
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
         * 租户端对外访问基地址（平台侧解析「该租户在哪」时使用）。
         *
         * <p>例如 {@code https://t1.example.com} 或 {@code http://10.0.0.8:8080/api}。
         * 留空时按「本机首选 IPv4 + server.port + context-path」自动推导，
         * 这在容器或反向代理后面通常是错的，生产环境建议显式配置。</p>
         */
        private String tenantBaseUrl;

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
         * 获取租户端对外访问基地址
         *
         * @return 基地址，未配置时为 null
         */
        public String getTenantBaseUrl() {
            return tenantBaseUrl;
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
            copy.tenantBaseUrl = this.tenantBaseUrl;
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
