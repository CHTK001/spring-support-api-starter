package com.chua.starter.tenant.server.support;

import com.chua.starter.tenant.entity.SysTenant;
import com.chua.starter.tenant.properties.TenantProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Locale;
import java.util.Set;

/**
 * 租户独立库自动开通器。
 *
 * <p>建租户时按 {@code sys_tenant_host/port/dbname} 自动执行
 * {@code CREATE DATABASE IF NOT EXISTS} + 建账号 + 授权，并校验租户账号真能连上。</p>
 *
 * <p><b>为什么不复用 {@code DataSourceScriptConfiguration}</b>：那套 Flyway-like 迁移器绑定
 * 在应用主数据源上，且带版本表/校验和语义；租户库是「新建空库」，复用要先绕开版本表。
 * 这里只用 {@link JdbcTemplate} 按顺序执行语句。业务表结构与种子数据由
 * {@link TenantDatabaseInitializer} 紧接着用同一批 {@code db/init} 脚本初始化，
 * 两者职责分开：本类只管「库和账号存在」，那边管「库里有表」。</p>
 *
 * <p><b>安全边界</b>：库名/账号只允许 {@code [A-Za-z0-9_]}（标识符白名单），
 * 口令无法参数化（DDL 不支持占位符），故单独做引号与反斜杠转义。
 * 管理连接缺失时整步跳过，不影响建租户。</p>
 *
 * <p><b>幂等</b>：库与账号都可能已存在，统一用 {@code IF NOT EXISTS}；
 * 口令用 {@code ALTER USER} 覆盖，保证平台记录与库内口令一致。</p>
 *
 * @since 4.0.0.51
 * @author CH
 */
@Slf4j
@Component
public class TenantDatabaseProvisioner {

    /**
     * 标识符白名单：库名/账号只允许字母数字下划线
     */
    private static final String SAFE_IDENTIFIER = "^[A-Za-z0-9_]{1,64}$";

    /**
     * MySQL 系统库：任何情况下都不允许作为租户库创建或删除
     */
    private static final Set<String> RESERVED_DATABASES = Set.of(
            "mysql", "information_schema", "performance_schema", "sys");

    /**
     * 租户配置
     */
    private final TenantProperties tenantProperties;

    /**
     * 租户库连接信息解析器；用于按可视化选择的实例来源取管理连接
     */
    private final TenantDataSourceResolver tenantDataSourceResolver;

    /**
     * 租户口令加解密；台账与平台自身实例的口令无需解密（无 ENC: 前缀时原样返回）
     */
    private final TenantPasswordCipher tenantPasswordCipher;

    /**
     * 平台主数据源；选「本机」时直接复用，不再要求 YAML 另配管理连接
     */
    @Autowired(required = false)
    private ObjectProvider<DataSource> primaryDataSourceProvider;

    /**
     * 兼容旧构造：没有解析器时只支持 YAML 管理连接（存量单元测试与外部直接 new 的场景）。
     *
     * @param tenantProperties 租户配置
     */
    public TenantDatabaseProvisioner(TenantProperties tenantProperties) {
        this(tenantProperties, null, null);
    }

    /**
     * 构造开通器。
     *
     * @param tenantProperties       租户配置
     * @param tenantDataSourceResolver 连接信息解析器
     * @param tenantPasswordCipher   口令加解密
     */
    @Autowired
    public TenantDatabaseProvisioner(TenantProperties tenantProperties,
                                     TenantDataSourceResolver tenantDataSourceResolver,
                                     TenantPasswordCipher tenantPasswordCipher) {
        this.tenantProperties = tenantProperties;
        this.tenantDataSourceResolver = tenantDataSourceResolver;
        this.tenantPasswordCipher = tenantPasswordCipher;
    }

    /**
     * 是否为「租户库自动开通」可用状态。
     *
     * @return 已启用且管理连接配置完整时返回 true
     */
    public boolean isAvailable() {
        TenantProperties.ServerConfig server = tenantProperties.getServer();
        if (!server.isDatasourceRoutingEnable() || !server.isDatasourceRoutingAutoProvision()) {
            return false;
        }
        return hasText(server.getDatasourceRoutingAdminUrl())
                && hasText(server.getDatasourceRoutingAdminUsername());
    }

    /**
     * 按租户记录判断「这个租户能不能自动建库」。
     *
     * <p>前端已把实例来源做成可视化配置（自填 URL / 平台自身实例 / 数据源台账），
     * 因此是否建库只取决于租户记录本身：登记了库名、能解析出实例账号，就应当建库。
     * YAML 里的 {@code datasource-routing-admin-url} 只在存量部署里作为首选管理连接，
     * <b>不再作为建库的前置开关</b>。</p>
     *
     * @param tenant 租户记录
     * @return 可以自动建库返回 true
     */
    public boolean isAvailable(SysTenant tenant) {
        if (tenant == null || !tenantProperties.getServer().isDatasourceRoutingAutoProvision()) {
            return false;
        }
        if (!hasText(tenant.getSysTenantDbname())) {
            return false;
        }
        TenantDatasourceInfo info = resolveInfo(tenant);
        if (info == null || !hasText(info.username())) {
            // 没有解析器（存量构造）或实例账号缺失时，退回 YAML 管理连接口径
            return isAvailable() && hasText(tenant.getSysTenantSqlUsername());
        }
        return true;
    }

    /**
     * 开通租户库。
     *
     * <p>失败只记录错误并返回 {@code false}，不抛异常打断建租户——租户记录已落库，
     * 建库失败应该由运维补做，而不是让接口 500。</p>
     *
     * @param tenant           租户记录
     * @param plainSqlPassword 明文库口令（落库版本是密文，建账号必须用明文）
     * @return 库已就绪返回 true；未启用或失败返回 false
     */
    public boolean provision(SysTenant tenant, String plainSqlPassword) {
        if (!isAvailable(tenant)) {
            return false;
        }
        String dbName = tenant.getSysTenantDbname();
        String username = tenant.getSysTenantSqlUsername();
        // 台账实例自带账号：只需要建库，不需要也不应该再建一个库账号
        boolean sharedInstance = tenant.getSysTenantDatasourceId() != null
                && tenant.getSysTenantDatasourceId() > 0;
        if (!hasText(dbName)) {
            log.warn("[租户建库] 租户 {} 未填写库名，跳过自动建库", tenant.getSysTenantId());
            return false;
        }
        if (!dbName.matches(SAFE_IDENTIFIER)) {
            log.error("[租户建库] 库名含非法字符，已拒绝: db={}", dbName);
            return false;
        }
        if (!sharedInstance && !hasText(username)) {
            log.warn("[租户建库] 租户 {} 未填写数据库账号，跳过自动建库", tenant.getSysTenantId());
            return false;
        }
        if (hasText(username) && !username.matches(SAFE_IDENTIFIER)) {
            log.error("[租户建库] 数据库账号含非法字符，已拒绝: user={}", username);
            return false;
        }
        if (!sharedInstance && !hasText(plainSqlPassword)) {
            log.warn("[租户建库] 租户 {} 未填写数据库口令，跳过自动建库", tenant.getSysTenantId());
            return false;
        }
        // 下面的 DDL 是 MySQL 语法（CREATE USER ...@'%' / GRANT ... ON db.*）。
        // 主库或租户库换成 PostgreSQL 之类必须先实现对应方言，否则会在这里静默失败，
        // 表现为「租户建了、库没建」，故显式挡在入口。
        if (!isMysqlAdmin(tenant)) {
            log.warn("[租户建库] 管理连接不是 MySQL，自动建库仅支持 MySQL，已跳过: tenantId={}",
                    tenant.getSysTenantId());
            return false;
        }

        try {
            createDatabase(tenant, dbName);
            if (sharedInstance) {
                log.info("[租户建库] 使用数据源台账实例账号，跳过建账号: db={}", dbName);
            } else {
                createAccount(tenant, dbName, plainSqlPassword);
                verifyConnectivity(tenant, dbName, plainSqlPassword);
            }
            return true;
        } catch (Exception e) {
            log.error("[租户建库] 租户 {} 自动建库失败: {}", tenant.getSysTenantId(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 只建库（{@code CREATE DATABASE IF NOT EXISTS}）。
     *
     * <p>拆出来是给「创建进度」用的：建库与建账号是两个可观察的步骤，进度弹窗要分别显示
     * 「正在建库」「正在建账号授权」，合并成一步会让人以为卡住。</p>
     *
     * @param dbName 库名（调用方已按白名单校验）
     * @throws Exception 执行失败
     */
    public void createDatabase(String dbName) throws Exception {
        refuseProtectedDatabase(dbName);
        JdbcTemplate admin = new JdbcTemplate(adminDataSource());
        admin.execute("CREATE DATABASE IF NOT EXISTS `" + dbName + "` "
                + "DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci");
        log.info("[租户建库] 数据库就绪: {}", dbName);
    }

    /**
     * 只建库（按租户所选实例的管理连接）。
     *
     * @param tenant 租户记录（提供实例来源）
     * @param dbName 库名（调用方已按白名单校验）
     * @throws Exception 执行失败
     */
    public void createDatabase(SysTenant tenant, String dbName) throws Exception {
        refuseProtectedDatabase(dbName);
        JdbcTemplate admin = requireAdmin(tenant, null);
        admin.execute("CREATE DATABASE IF NOT EXISTS `" + dbName + "` "
                + "DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci");
        log.info("[租户建库] 数据库就绪: {}", dbName);
    }

    /**
     * 只建账号并授权（{@code CREATE USER / ALTER USER / GRANT}）。
     *
     * @param tenant           租户记录
     * @param dbName           库名（调用方已按白名单校验）
     * @param plainSqlPassword 明文库口令
     * @throws Exception 执行失败
     */
    public void createAccount(SysTenant tenant, String dbName, String plainSqlPassword) throws Exception {
        String username = tenant.getSysTenantSqlUsername();
        // 共享实例（台账）沿用实例自己的账号，不需要也不应该再建一个库账号：
        // 之前这里没判空，会在「只填库名」的租户上拼出 CREATE USER 'null'@'%'。
        if (!hasText(username)) {
            log.warn("[租户建库] 租户 {} 未登记数据库账号，跳过建账号（沿用所选实例账号）",
                    tenant.getSysTenantId());
            return;
        }
        JdbcTemplate admin = requireAdmin(tenant, plainSqlPassword);
        String literal = escapeLiteral(plainSqlPassword);
        admin.execute("CREATE USER IF NOT EXISTS '" + username + "'@'%' IDENTIFIED BY '" + literal + "'");
        // 已存在时 CREATE USER 不生效，口令可能与平台记录不一致，必须显式覆盖
        admin.execute("ALTER USER '" + username + "'@'%' IDENTIFIED BY '" + literal + "'");
        admin.execute("GRANT ALL PRIVILEGES ON `" + dbName + "`.* TO '" + username + "'@'%'");
        admin.execute("FLUSH PRIVILEGES");
        log.info("[租户建库] 账号与授权就绪: user={}, db={}", username, dbName);
    }

    /**
     * 删除租户库并回收数据库账号。
     *
     * <p>租户删除是逻辑删除（{@code sys_tenant_delete=1}），库是物理对象，必须显式清理，
     * 否则平台会持续占着一堆没人用的库和账号。放在事务提交之后调用：DDL 在 MySQL 里隐式提交，
     * 混进业务事务会出现「库删了、记录回滚了」的错位。</p>
     *
     * <p>失败只记日志，不向调用方抛——租户记录已经删掉，不应因为一个落单的库把删除接口变成 500；
     * 但日志必须留下库名，运维按它手工清理。</p>
     *
     * @param tenant 租户记录
     * @return 库已删除（或本来就没有独立库）返回 true
     */
    public boolean destroy(SysTenant tenant) {
        if (tenant == null) {
            return false;
        }
        String dbName = tenant.getSysTenantDbname();
        if (!hasText(dbName)) {
            return true;
        }
        if (!dbName.matches(SAFE_IDENTIFIER)) {
            log.error("[租户建库] 库名含非法字符，已拒绝删除: db={}", dbName);
            return false;
        }
        if (isProtectedDatabase(dbName)) {
            log.error("[租户建库] 库名是平台主库或系统库，已拒绝删除: db={}", dbName);
            return false;
        }
        if (!isMysqlAdmin(tenant)) {
            log.warn("[租户建库] 管理连接不是 MySQL，自动删库仅支持 MySQL，已跳过: {}", dbName);
            return false;
        }
        String username = tenant.getSysTenantSqlUsername();
        JdbcTemplate admin = adminJdbc(tenant, null);
        if (admin == null) {
            log.warn("[租户建库] 无法解析管理连接，跳过删除租户库: db={}", dbName);
            return false;
        }
        try {
            admin.execute("DROP DATABASE IF EXISTS `" + dbName + "`");
            log.info("[租户建库] 租户库已删除: db={}", dbName);
        } catch (Exception e) {
            log.error("[租户建库] 删除租户库失败，需人工清理: db={}, {}", dbName, e.getMessage(), e);
            return false;
        }
        if (hasText(username) && username.matches(SAFE_IDENTIFIER)) {
            // 只回收平台生成（t_ 前缀）的专用账号。自填/台账账号很可能被其它库或
            // 其它租户共用，DROP USER 会直接断掉别人的连接。
            if (!username.startsWith("t_")) {
                log.info("[租户建库] 账号 {} 不是平台生成，保留不删（可能被其它库共用）", username);
                return true;
            }
            try {
                admin.execute("DROP USER IF EXISTS '" + username + "'@'%'");
                log.info("[租户建库] 数据库账号已删除: user={}", username);
            } catch (Exception e) {
                // 账号删除失败不影响库已删的事实，留日志即可
                log.warn("[租户建库] 删除数据库账号失败，需人工清理: user={}, {}", username, e.getMessage());
            }
        }
        return true;
    }

    /**
     * 校验库名/账号是否满足标识符白名单。
     *
     * @param value 待校验值
     * @return 合法返回 true
     */
    public boolean isSafeIdentifier(String value) {
        return hasText(value) && value.matches(SAFE_IDENTIFIER);
    }

    /**
     * 是否是平台主库或 MySQL 系统库。
     *
     * <p>建库与删库都必须先过这一关：租户库名是人工填写的，一旦填成平台主库名，
     * {@code CREATE DATABASE IF NOT EXISTS} 会把平台库当成租户库复用，
     * {@code DROP DATABASE} 则会直接删掉平台数据。</p>
     *
     * @param dbName 库名
     * @return 平台主库或系统库返回 true
     */
    public boolean isProtectedDatabase(String dbName) {
        if (!hasText(dbName)) {
            return false;
        }
        String normalized = dbName.trim().toLowerCase(Locale.ROOT);
        if (RESERVED_DATABASES.contains(normalized)) {
            return true;
        }
        String platform = platformDatabaseName();
        return platform != null && platform.toLowerCase(Locale.ROOT).equals(normalized);
    }

    /**
     * 取平台主库名：优先从主数据源连接元数据读，取不到再解析 YAML 管理连接 URL。
     *
     * @return 平台主库名；无法确定时返回 {@code null}
     */
    private String platformDatabaseName() {
        DataSource primary = primaryDataSource();
        if (primary != null) {
            try (Connection connection = primary.getConnection()) {
                String catalog = connection.getCatalog();
                if (hasText(catalog)) {
                    return catalog;
                }
            } catch (Exception e) {
                log.debug("[租户建库] 读取平台主库名失败，尝试解析管理连接 URL: {}", e.getMessage());
            }
        }
        return databaseFromUrl(tenantProperties.getServer().getDatasourceRoutingAdminUrl());
    }

    /**
     * 从 JDBC URL 中取库名。
     *
     * @param url JDBC URL
     * @return 库名；URL 未带库名时返回 {@code null}
     */
    private String databaseFromUrl(String url) {
        if (!hasText(url)) {
            return null;
        }
        String clean = url.trim();
        int query = clean.indexOf('?');
        if (query >= 0) {
            clean = clean.substring(0, query);
        }
        int slash = clean.lastIndexOf('/');
        if (slash < 0 || slash == clean.length() - 1) {
            return null;
        }
        String database = clean.substring(slash + 1).trim();
        return database.isEmpty() ? null : database;
    }

    /**
     * 拒绝把平台主库/系统库当成租户库。
     *
     * @param dbName 库名
     */
    private void refuseProtectedDatabase(String dbName) {
        if (isProtectedDatabase(dbName)) {
            throw new IllegalStateException("库名「" + dbName + "」是平台主库或系统库，已拒绝执行");
        }
    }

    /**
     * 建库是否可用（同 {@link #isAvailable()} 的语义，供进度步骤判断）。
     *
     * @return 可用返回 true
     */
    public boolean isProvisionEnabled() {
        return isAvailable();
    }

    /**
     * 连通性校验（供进度步骤在建账号后单独调用）。
     *
     * @param tenant           租户记录
     * @param plainSqlPassword 明文库口令
     */
    public void verify(SysTenant tenant, String plainSqlPassword) {
        verifyConnectivity(tenant, tenant.getSysTenantDbname(), plainSqlPassword);
    }

    /**
     * 用租户自己的账号连一次，确认授权真的生效。
     *
     * <p>这是「建库成功」和「租户库可用」之间最容易漏掉的一环：账号建了但 host 不匹配、
     * 授权没生效，都会在这里暴露出来。校验失败不改返回值，只告警，因为库本身已经可用。</p>
     *
     * @param tenant           租户记录
     * @param dbName           库名
     * @param plainSqlPassword 明文库口令
     */
    private void verifyConnectivity(SysTenant tenant, String dbName, String plainSqlPassword) {
        // 与建池同源：台账租户的地址在台账里，只读租户自填字段会直接跳过校验
        TenantDatasourceInfo info = resolveInfo(tenant);
        String url = TenantJdbcUrlBuilder.build(info, tenantProperties);
        if (url == null) {
            url = TenantJdbcUrlBuilder.build(tenant, tenantProperties);
        }
        if (url == null) {
            return;
        }
        String username = info != null && hasText(info.username())
                ? info.username() : tenant.getSysTenantSqlUsername();
        DriverManagerDataSource probe = new DriverManagerDataSource(url, username, plainSqlPassword);
        try (var ignored = probe.getConnection()) {
            log.info("[租户建库] 连通性校验通过: {}", dbName);
        } catch (Exception e) {
            log.warn("[租户建库] 连通性校验失败（库已建好，请检查网络/授权）: {}", e.getMessage());
        }
    }

    /**
     * 管理连接是否为 MySQL。
     *
     * <p>按「这个租户实际会用的管理连接」判断：显式配置优先；否则看可视化选择的实例；
     * 选「本机」且无配置时看平台主数据源的连接元数据。</p>
     *
     * @param tenant 租户记录
     * @return 管理连接是 MySQL 返回 true
     */
    private boolean isMysqlAdmin(SysTenant tenant) {
        TenantDatasourceInfo info = resolveInfo(tenant);
        if (info != null) {
            if (info.origin() == TenantDatasourceInfo.Origin.PLATFORM_SELF) {
                DataSource primary = primaryDataSource();
                if (primary != null) {
                    try (Connection connection = primary.getConnection()) {
                        return isMysqlAdmin(connection.getMetaData().getURL());
                    } catch (Exception e) {
                        log.debug("[租户建库] 读取平台主数据源元数据失败: {}", e.getMessage());
                        return false;
                    }
                }
            }
            return isMysqlAdmin(TenantJdbcUrlBuilder.buildInstanceUrl(info, tenantProperties));
        }
        TenantProperties.ServerConfig server = tenantProperties.getServer();
        return hasYamlAdmin() && isMysqlAdmin(server.getDatasourceRoutingAdminUrl());
    }

    /**
     * 管理连接是否为 MySQL。
     *
     * @param adminUrl 管理连接 URL
     * @return 是 MySQL 协议返回 true
     */
    private boolean isMysqlAdmin(String adminUrl) {
        return adminUrl != null && adminUrl.trim().toLowerCase(java.util.Locale.ROOT).startsWith("jdbc:mysql:");
    }

    /**
     * 取管理连接；取不到时抛异常，由调用方转成可读的失败原因。
     *
     * @param tenant           租户记录
     * @param plainSqlPassword 租户库明文口令（台账来源可为空）
     * @return 管理 JdbcTemplate
     */
    private JdbcTemplate requireAdmin(SysTenant tenant, String plainSqlPassword) {
        JdbcTemplate admin = adminJdbc(tenant, plainSqlPassword);
        if (admin == null) {
            throw new IllegalStateException("无法解析到这个租户的管理连接：请检查实例来源、库名与数据库账号");
        }
        return admin;
    }

    /**
     * 按租户所选实例构造管理连接。
     *
     * <p>三种来源对应三条连接路径：</p>
     * <ul>
     *   <li><b>显式管理连接</b>（{@code datasource-routing-admin-url}）——存量部署不变，优先使用；</li>
     *   <li><b>平台自身实例</b>——直接复用平台主数据源，不需要 YAML 再配一套口令；</li>
     *   <li><b>台账 / 自填</b>——用该实例自己的账号连到实例级 URL（库名留空），再建库建账号。</li>
     * </ul>
     *
     * @param tenant           租户记录
     * @param plainSqlPassword 租户库明文口令；台账来源忽略它
     * @return 管理 JdbcTemplate；无法解析时返回 {@code null}
     */
    private JdbcTemplate adminJdbc(SysTenant tenant, String plainSqlPassword) {
        TenantDatasourceInfo info = resolveInfo(tenant);
        // 租户显式选择的实例优先：可视化配置是唯一真源，
        // YAML 管理连接只在租户没有可解析实例（存量部署）时兜底
        if (info != null) {
            if (info.origin() == TenantDatasourceInfo.Origin.PLATFORM_SELF) {
                DataSource primary = primaryDataSource();
                if (primary != null) {
                    return new JdbcTemplate(primary);
                }
            }
            if (!hasText(info.username())) {
                return null;
            }
            String url = TenantJdbcUrlBuilder.buildInstanceUrl(info, tenantProperties);
            if (url == null) {
                return null;
            }
            String password;
            if (info.origin() == TenantDatasourceInfo.Origin.DATASOURCE_HUB) {
                // 台账自带实例口令；租户自己的口令不是这个账号的，用错会认证失败
                password = decrypt(info.password());
            } else {
                password = hasText(plainSqlPassword) ? plainSqlPassword : decrypt(info.password());
            }
            return new JdbcTemplate(new DriverManagerDataSource(url, info.username(), password));
        }
        if (hasYamlAdmin()) {
            return new JdbcTemplate(adminDataSource());
        }
        return null;
    }

    /**
     * 是否配置了 YAML 管理连接（存量部署口径）。
     *
     * @return 配置完整返回 true
     */
    private boolean hasYamlAdmin() {
        TenantProperties.ServerConfig server = tenantProperties.getServer();
        return hasText(server.getDatasourceRoutingAdminUrl())
                && hasText(server.getDatasourceRoutingAdminUsername());
    }

    /**
     * 解析租户的连接信息。
     *
     * @param tenant 租户记录
     * @return 连接信息；无解析器或未登记时返回 {@code null}
     */
    private TenantDatasourceInfo resolveInfo(SysTenant tenant) {
        return tenantDataSourceResolver == null ? null : tenantDataSourceResolver.resolve(tenant);
    }

    /**
     * 平台主数据源；容器里有多个候选时返回 {@code null}，避免注入歧义。
     *
     * @return 平台主数据源
     */
    private DataSource primaryDataSource() {
        return primaryDataSourceProvider == null ? null : primaryDataSourceProvider.getIfUnique();
    }

    /**
     * 还原口令（{@code ENC:} 前缀才是密文，历史明文原样返回）。
     *
     * @param stored 库中或台账里的口令
     * @return 明文口令
     */
    private String decrypt(String stored) {
        return tenantPasswordCipher == null ? stored : tenantPasswordCipher.decrypt(stored);
    }

    /**
     * 构造 YAML 管理连接（存量口径）。
     *
     * @return 管理数据源
     */
    private DataSource adminDataSource() {
        TenantProperties.ServerConfig server = tenantProperties.getServer();
        return new DriverManagerDataSource(
                server.getDatasourceRoutingAdminUrl(),
                server.getDatasourceRoutingAdminUsername(),
                server.getDatasourceRoutingAdminPassword()
        );
    }

    /**
     * SQL 字符串字面量转义（只用于 {@code IDENTIFIED BY '...'}）。
     *
     * @param value 原始口令
     * @return 转义后的口令
     */
    private String escapeLiteral(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("'", "''");
    }

    /**
     * 是否包含文本。
     *
     * @param value 待判断值
     * @return 非空白返回 true
     */
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
