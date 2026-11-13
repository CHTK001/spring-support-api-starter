package com.chua.starter.tenant.server.support;

import com.chua.starter.datasource.support.DataSourceContextSupport;
import com.chua.starter.tenant.entity.SysTenant;
import com.chua.starter.tenant.properties.TenantProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * 租户数据库连接信息解析器。
 *
 * <p>唯一真源：{@link TenantJdbcUrlBuilder}、{@link TenantDataSourceRegistry}、
 * {@link TenantDataSourceFilter}、{@link TenantDatabaseProvisioner}、
 * {@link TenantDatabaseInitializer} 全部经这里取连接信息，不再各自读 {@code SysTenant} 字段。</p>
 *
 * <h2>三路优先级</h2>
 *
 * <ol>
 *   <li>{@code sys_tenant_datasource_id == }{@link #PLATFORM_SELF}——用平台自身实例：
 *       地址、端口、账号、口令都取 {@code datasource-routing-admin-url}，
 *       用于「同实例不同库」的租户，免去每个租户重填一遍实例信息。</li>
 *   <li>{@code sys_tenant_datasource_id > 0}——用数据源中心台账里的那条实例，
 *       经 {@link TenantDatasourceLookup} 取；<b>没有台账实现或查不到时返回 {@code null}</b>，
 *       绝不回退旧字段或主库（见 {@link TenantDatasourceLookup} 的说明）。</li>
 *   <li>其余情况（{@code null} / {@code 0}）——用租户记录自填字段，即存量行为，逐字节不变。</li>
 * </ol>
 *
 * <p><b>库名一律取 {@code sys_tenant_dbname}</b>：一个实例往往承载多个租户，
 * 实例级的库名只是模板，拿它路由会串库。</p>
 *
 * @since 4.0.0.52
 * @author CH
 */
@Slf4j
@Component
public class TenantDataSourceResolver {

    /**
     * 「使用平台自身实例」的哨兵值
     */
    public static final long PLATFORM_SELF = -1L;

    /**
     * 租户配置
     */
    private final TenantProperties tenantProperties;

    /**
     * 数据源台账查询口；未装 datasource-hub 的应用里为空
     */
    private final ObjectProvider<TenantDatasourceLookup> datasourceLookup;

    /**
     * 平台主数据源；用于「本机」租户：地址优先取配置，缺失时从主连接元数据解析，
     * 建库/建账号直接复用这条连接，不再要求 YAML 里另配一套管理连接。
     */
    private final ObjectProvider<DataSource> primaryDataSource;

    /**
     * 构造解析器。
     *
     * @param tenantProperties 租户配置
     * @param datasourceLookup 台账查询口
     * @param primaryDataSource 平台主数据源（可为空）
     */
    @Autowired
    public TenantDataSourceResolver(TenantProperties tenantProperties,
                                    ObjectProvider<TenantDatasourceLookup> datasourceLookup,
                                    ObjectProvider<DataSource> primaryDataSource) {
        this.tenantProperties = tenantProperties;
        this.datasourceLookup = datasourceLookup;
        this.primaryDataSource = primaryDataSource;
    }

    /**
     * 兼容两参构造：旧测试与只关心「租户字段 / 台账」的调用方使用。
     *
     * @param tenantProperties 租户配置
     * @param datasourceLookup 台账查询口
     */
    public TenantDataSourceResolver(TenantProperties tenantProperties,
                                    ObjectProvider<TenantDatasourceLookup> datasourceLookup) {
        this(tenantProperties, datasourceLookup, null);
    }

    /**
     * 解析租户的数据库连接信息。
     *
     * @param tenant 租户记录
     * @return 连接信息；未登记租户库或引用的台账不可用时返回 {@code null}
     */
    public TenantDatasourceInfo resolve(SysTenant tenant) {
        if (tenant == null) {
            return null;
        }
        Long datasourceId = tenant.getSysTenantDatasourceId();
        if (datasourceId != null && datasourceId == PLATFORM_SELF) {
            return resolvePlatformSelf(tenant);
        }
        if (datasourceId != null && datasourceId > 0) {
            return resolveFromHub(tenant, datasourceId);
        }
        return resolveFromTenantFields(tenant);
    }

    /**
     * 租户记录里是否登记了「独立库」。
     *
     * <p>判定与 {@link #resolve} 同源，避免过滤器与建池对「算不算已登记」看法不一致：
     * 过滤器认为未登记会走主库，建池认为已登记会去连——两者错位就会出现
     * 「请求静默读到平台数据」。</p>
     *
     * @param tenant 租户记录
     * @return 登记了独立库返回 true
     */
    public boolean isDedicated(SysTenant tenant) {
        return resolve(tenant) != null;
    }

    /**
     * 租户记录是否「声明」要使用独立实例（而不是自填字段）。
     *
     * <p>判定只看 id，不看能否解析出来：能解析是「可用」，解析失败是「坏了」，
     * 两者都必须走独立库分支，不能因为解析失败就当成「没配」而回退主库。</p>
     *
     * @param tenant 租户记录
     * @return 选了台账实例或平台自身实例返回 true
     */
    public boolean referencesInstance(SysTenant tenant) {
        if (tenant == null || tenant.getSysTenantDatasourceId() == null) {
            return false;
        }
        return tenant.getSysTenantDatasourceId() != 0L;
    }

    /**
     * 生成前端只读回显用的连接信息摘要。
     *
     * <p>解析失败但租户确实声明了独立库时<b>仍然返回一条记录</b>，带
     * {@code available=false} 与原因——否则页面只能显示「未登记」，运维分不清
     * 「没配」和「配错了」。</p>
     *
     * @param tenant 租户记录
     * @return 摘要；未声明独立库也未填库名（即共享主库的存量租户）时返回 {@code null}
     */
    public TenantDatasourceView describe(SysTenant tenant) {
        if (tenant == null) {
            return null;
        }
        TenantDatasourceInfo info = resolve(tenant);
        Long datasourceId = tenant.getSysTenantDatasourceId();
        String datasourceName = datasourceId != null && datasourceId > 0
                ? lookupName(datasourceId) : null;
        if (info != null) {
            // 地址与库名齐全只说明「已登记」；真能建池还必须有账号。
            // 少了账号时 getOrCreate 会直接返回 null（该租户请求全部 503），
            // 所以回显这里也要判不可用，不能给一个「看起来正常」的绿标。
            boolean usable = hasText(info.username());
            return new TenantDatasourceView(info.origin().name(), originLabel(info.origin()),
                    info.host(), info.port(), info.effectiveDbName(), info.username(),
                    datasourceName, usable,
                    usable ? null : "缺少数据库账号（自填型请补数据库账号，引用型请在数据源台账补账号）");
        }
        if (!referencesInstance(tenant) && !hasText(tenant.getSysTenantDbname())) {
            // 没选实例、也没填库名：仍是共享主库的存量租户，不是配置错误
            return null;
        }
        return new TenantDatasourceView(null, "配置不可用", null, null,
                tenant.getSysTenantDbname(), null, datasourceName, false, diagnose(tenant));
    }

    /**
     * 数据库来源下拉项：「自填」+「平台自身实例」+ 台账里可用的 jdbc 数据源。
     *
     * @return 可选项
     */
    public List<TenantDatasourceOption> options() {
        List<TenantDatasourceOption> options = new ArrayList<>();
        options.add(new TenantDatasourceOption(0L, "由租户自己填写实例信息", "manual", null, null, null, null));
        // 即使当前解析不到平台实例地址也要给出这一项：存量的 -1 租户在编辑时必须能匹配到
        // 自己的取值，否则下拉会显示裸的 "-1"，用户既看不懂也改不回来。
        AdminEndpoint admin = platformEndpoint();
        options.add(new TenantDatasourceOption(PLATFORM_SELF, "平台自身实例", "jdbc",
                admin == null ? null : admin.host(), admin == null ? null : admin.port(), null, null));
        TenantDatasourceLookup lookup = datasourceLookup.getIfAvailable();
        if (lookup != null) {
            options.addAll(lookup.options());
        }
        return options;
    }

    /**
     * 按主键取台账数据源名称（只读回显用）。
     *
     * @param datasourceId 台账主键
     * @return 名称；台账实现缺失、记录不存在或查询异常时返回 {@code null}
     */
    private String lookupName(long datasourceId) {
        TenantDatasourceLookup lookup = datasourceLookup.getIfAvailable();
        if (lookup == null) {
            return null;
        }
        try {
            return lookup.name(datasourceId);
        } catch (Exception e) {
            log.debug("[租户数据源] 查询数据源名称失败: id={}, error={}", datasourceId, e.getMessage());
            return null;
        }
    }

    /**
     * 给出「声明了独立库却解析不出来」的具体原因。
     *
     * <p>只用于前端回显与排障，不参与路由判定；措辞要让不懂内部实现的运维也能照着改。</p>
     *
     * @param tenant 租户记录
     * @return 原因说明
     */
    private String diagnose(SysTenant tenant) {
        Long datasourceId = tenant.getSysTenantDatasourceId();
        if (datasourceId != null && datasourceId > 0) {
            TenantDatasourceLookup lookup = datasourceLookup.getIfAvailable();
            if (lookup == null) {
                return "当前应用未安装数据源台账模块，无法读取所引用的实例";
            }
            try {
                if (lookup.find(datasourceId) == null) {
                    return "引用的数据源不存在或已停用";
                }
            } catch (Exception e) {
                return "查询数据源失败：" + e.getMessage();
            }
            return hasText(tenant.getSysTenantDbname()) ? "实例信息不完整" : "未填写库名";
        }
        if (datasourceId != null && datasourceId == PLATFORM_SELF) {
            if (!hasText(tenant.getSysTenantDbname())) {
                return "未填写库名";
            }
            return platformEndpoint() == null
                    ? "未配置平台实例地址（datasource-routing-admin-url）"
                    : "平台实例缺少可用的数据库账号";
        }
        return "独立库信息不完整：地址或库名缺失";
    }

    /**
     * 来源的中文名。
     *
     * @param origin 来源
     * @return 中文名
     */
    private static String originLabel(TenantDatasourceInfo.Origin origin) {
        return switch (origin) {
            case TENANT_FIELDS -> "租户自填";
            case DATASOURCE_HUB -> "数据源中心";
            case PLATFORM_SELF -> "平台自身实例";
        };
    }

    /**
     * 用租户自填字段构造连接信息（存量行为）。
     *
     * @param tenant 租户记录
     * @return 连接信息；地址或库名缺失时返回 {@code null}
     */
    private TenantDatasourceInfo resolveFromTenantFields(SysTenant tenant) {
        String host = hasText(tenant.getSysTenantHost())
                ? tenant.getSysTenantHost() : tenant.getSysTenantIp();
        TenantDatasourceInfo info = new TenantDatasourceInfo(
                host,
                tenant.getSysTenantPort(),
                tenant.getSysTenantDbname(),
                tenant.getSysTenantSqlUsername(),
                tenant.getSysTenantSqlPassword(),
                TenantDatasourceInfo.Origin.TENANT_FIELDS);
        return info.complete() ? info : null;
    }

    /**
     * 按台账主键取实例连接信息（实例自带账号）。
     *
     * @param datasourceId 台账主键
     * @param tenantId     租户标识，仅用于日志
     * @return 实例信息；台账模块缺失、记录不存在或已停用时返回 {@code null}
     */
    private TenantDatasourceInfo findHub(long datasourceId, String tenantId) {
        TenantDatasourceLookup lookup = datasourceLookup.getIfAvailable();
        if (lookup == null) {
            log.error("[租户数据源] 租户 {} 引用了数据源 {}，但当前应用未安装数据源台账模块，"
                    + "无法解析实例信息。已拒绝按库路由（不会回退主库，避免读到他租户数据）",
                    tenantId, datasourceId);
            return null;
        }
        try {
            TenantDatasourceInfo found = lookup.find(datasourceId);
            if (found == null) {
                log.error("[租户数据源] 租户 {} 引用的数据源 {} 不存在或已停用，已拒绝按库路由",
                        tenantId, datasourceId);
            }
            return found;
        } catch (Exception e) {
            log.error("[租户数据源] 租户 {} 查询数据源 {} 失败: {}", tenantId, datasourceId, e.getMessage());
            return null;
        }
    }

    /**
     * 用数据源台账里的实例构造「路由用」连接信息。
     *
     * <p>账号优先取租户自己的专用账号（{@code t_<code>}），没有时才回落实例账号——
     * 后者是存量租户的兼容路径，不能让它们因为升级而登不进去。</p>
     *
     * @param tenant       租户记录（提供库名与可能的专用账号）
     * @param datasourceId 台账主键
     * @return 连接信息；台账不可用或查不到时返回 {@code null}
     */
    private TenantDatasourceInfo resolveFromHub(SysTenant tenant, long datasourceId) {
        TenantDatasourceInfo found = findHub(datasourceId, tenant.getSysTenantId());
        if (found == null) {
            return null;
        }
        boolean ownCredentials = hasText(tenant.getSysTenantSqlUsername());
        // 库名强制取租户自己的：台账里的库名只是模板，同实例多租户时拿它路由会串库
        TenantDatasourceInfo info = new TenantDatasourceInfo(found.host(), found.port(),
                tenant.getSysTenantDbname(),
                ownCredentials ? tenant.getSysTenantSqlUsername() : found.username(),
                ownCredentials ? tenant.getSysTenantSqlPassword() : found.password(),
                TenantDatasourceInfo.Origin.DATASOURCE_HUB, ownCredentials);
        return info.complete() ? info : null;
    }

    /**
     * 解析「实例管理连接」：建库、建账号这类需要实例级权限的操作专用。
     *
     * <p>与 {@link #resolve} 的区别只在引用型租户：路由用租户的专用账号，而建账号时
     * 那个账号<b>还不存在</b>，必须用实例自带的账号。两者混用会表现为
     * 「建库第一步就连不上」，且错误信息看起来像网络问题。</p>
     *
     * @param tenant 租户记录
     * @return 管理用连接信息；自填型即租户自己的账号
     */
    public TenantDatasourceInfo resolveAdmin(SysTenant tenant) {
        if (tenant == null) {
            return null;
        }
        Long datasourceId = tenant.getSysTenantDatasourceId();
        if (datasourceId != null && datasourceId > 0) {
            TenantDatasourceInfo found = findHub(datasourceId, tenant.getSysTenantId());
            if (found == null) {
                return null;
            }
            return new TenantDatasourceInfo(found.host(), found.port(), null,
                    found.username(), found.password(), TenantDatasourceInfo.Origin.DATASOURCE_HUB, false);
        }
        if (datasourceId != null && datasourceId == PLATFORM_SELF) {
            return resolvePlatformSelf(tenant);
        }
        return resolveFromTenantFields(tenant);
    }

    /**
     * 用平台自身实例构造连接信息。
     *
     * @param tenant 租户记录（提供库名）
     * @return 连接信息；平台管理连接未配置或库名缺失时返回 {@code null}
     */
    private TenantDatasourceInfo resolvePlatformSelf(SysTenant tenant) {
        TenantProperties.ServerConfig server = tenantProperties.getServer();
        AdminEndpoint admin = platformEndpoint();
        if (admin == null) {
            log.error("[租户数据源] 租户 {} 选择使用平台自身实例，但既未配置 datasource-routing-admin-url，"
                    + "也无法从平台主数据源解析实例地址", tenant.getSysTenantId());
            return null;
        }
        // 账号优先用租户自己的库账号：可视化建租户时平台会为该租户生成专用账号，
        // 这样路由不再依赖 YAML 里的 admin 账号口令。
        String username = hasText(tenant.getSysTenantSqlUsername())
                ? tenant.getSysTenantSqlUsername() : server.getDatasourceRoutingAdminUsername();
        String password = hasText(tenant.getSysTenantSqlPassword())
                ? tenant.getSysTenantSqlPassword() : server.getDatasourceRoutingAdminPassword();
        TenantDatasourceInfo info = new TenantDatasourceInfo(admin.host(), admin.port(),
                tenant.getSysTenantDbname(), username, password, TenantDatasourceInfo.Origin.PLATFORM_SELF);
        return info.complete() ? info : null;
    }

    /**
     * 解析平台自身实例的地址与端口。
     *
     * <p>优先用显式配置 {@code datasource-routing-admin-url}（存量部署的行为不变）；
     * 未配置时从平台主数据源（动态数据源里名为 {@code master} 的那条）的连接元数据里取
     * 当前实例地址 —— 前端选「本机」时不需要再让运维在 YAML 里填一遍同样的实例信息。</p>
     *
     * <p>取 {@code master} 而不是「唯一 DataSource Bean」：装了数据源中心或读写分离的应用里
     * 容器内往往有多个 DataSource，{@code getIfUnique()} 会返回 {@code null}，
     * 表现为「平台自身实例」这一项时有时无。</p>
     *
     * @return 实例端点；两者都拿不到时返回 {@code null}
     */
    public AdminEndpoint platformEndpoint() {
        AdminEndpoint configured = parseAdminUrl(tenantProperties.getServer().getDatasourceRoutingAdminUrl());
        if (configured != null) {
            return configured;
        }
        DataSource dataSource = DataSourceContextSupport.getDatasource(DataSourceContextSupport.DEFAULT_DATASOURCE);
        if (dataSource == null && primaryDataSource != null) {
            // 少数部署没有把 master 注册进动态数据源表，退回「唯一 Bean」这条老路
            dataSource = primaryDataSource.getIfUnique();
        }
        if (dataSource == null) {
            return null;
        }
        try (Connection connection = dataSource.getConnection()) {
            return parseAdminUrl(connection.getMetaData().getURL());
        } catch (Exception e) {
            log.debug("[租户数据源] 从平台主数据源解析实例地址失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从管理连接地址里拆出主机与端口。
     *
     * <p>管理连接地址形如 {@code jdbc:mysql://127.0.0.1:3306/mysql}；这里只取 authority 段，
     * 参数串与库名都丢掉——库名由租户决定。</p>
     *
     * @param jdbcUrl 管理连接地址
     * @return 主机与端口；不可解析时返回 {@code null}
     */
    static AdminEndpoint parseAdminUrl(String jdbcUrl) {
        if (!hasText(jdbcUrl)) {
            return null;
        }
        String rest = jdbcUrl.trim();
        int scheme = rest.indexOf("//");
        if (scheme < 0) {
            return null;
        }
        rest = rest.substring(scheme + 2);
        int end = rest.length();
        for (char stop : new char[]{'/', '?', ';'}) {
            int index = rest.indexOf(stop);
            if (index >= 0 && index < end) {
                end = index;
            }
        }
        String authority = rest.substring(0, end);
        if (authority.isEmpty()) {
            return null;
        }
        String host = authority;
        Integer port = null;
        int colon = authority.lastIndexOf(':');
        if (colon > 0) {
            host = authority.substring(0, colon);
            try {
                port = Integer.valueOf(authority.substring(colon + 1));
            } catch (NumberFormatException e) {
                // 端口段不是数字（例如 IPv6 未加方括号）：整段当主机名，交给驱动默认端口
                host = authority;
                port = null;
            }
        }
        return hasText(host) ? new AdminEndpoint(host, port) : null;
    }

    /**
     * 是否包含文本。
     *
     * @param value 待判断值
     * @return 非空白返回 true
     */
    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    /**
     * 管理连接端点。
     *
     * @param host 主机
     * @param port 端口，可能为 {@code null}
     */
    public record AdminEndpoint(String host, Integer port) {
    }
}
