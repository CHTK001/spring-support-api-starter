package com.chua.starter.tenant.server.support;

import com.chua.starter.tenant.entity.SysTenant;
import com.chua.starter.tenant.properties.TenantProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

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
     * 构造解析器。
     *
     * @param tenantProperties 租户配置
     * @param datasourceLookup 台账查询口
     */
    public TenantDataSourceResolver(TenantProperties tenantProperties,
                                    ObjectProvider<TenantDatasourceLookup> datasourceLookup) {
        this.tenantProperties = tenantProperties;
        this.datasourceLookup = datasourceLookup;
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
     * 生成前端只读回显用的连接信息摘要。
     *
     * @param tenant 租户记录
     * @return 摘要；未登记独立库时返回 {@code null}
     */
    public TenantDatasourceView describe(SysTenant tenant) {
        TenantDatasourceInfo info = resolve(tenant);
        if (info == null) {
            return null;
        }
        String datasourceName = null;
        Long datasourceId = tenant == null ? null : tenant.getSysTenantDatasourceId();
        if (datasourceId != null && datasourceId > 0) {
            TenantDatasourceLookup lookup = datasourceLookup.getIfAvailable();
            if (lookup != null) {
                for (TenantDatasourceOption option : lookup.options()) {
                    if (option.id() == datasourceId) {
                        datasourceName = option.name();
                        break;
                    }
                }
            }
        }
        return new TenantDatasourceView(info.origin().name(), originLabel(info.origin()),
                info.host(), info.port(), info.effectiveDbName(), info.username(), datasourceName);
    }

    /**
     * 数据库来源下拉项：「自填」+「平台自身实例」+ 台账里可用的 jdbc 数据源。
     *
     * @return 可选项
     */
    public List<TenantDatasourceOption> options() {
        List<TenantDatasourceOption> options = new ArrayList<>();
        options.add(new TenantDatasourceOption(0L, "由租户自己填写实例信息", "manual", null, null, null, null));
        TenantProperties.ServerConfig server = tenantProperties.getServer();
        TenantDataSourceResolver.AdminEndpoint admin = parseAdminUrl(server.getDatasourceRoutingAdminUrl());
        if (admin != null) {
            options.add(new TenantDatasourceOption(PLATFORM_SELF, "平台自身实例", "jdbc", admin.host(),
                    admin.port(), null, null));
        }
        TenantDatasourceLookup lookup = datasourceLookup.getIfAvailable();
        if (lookup != null) {
            options.addAll(lookup.options());
        }
        return options;
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
     * 用数据源台账里的实例构造连接信息。
     *
     * @param tenant       租户记录（提供库名）
     * @param datasourceId 台账主键
     * @return 连接信息；台账不可用或查不到时返回 {@code null}
     */
    private TenantDatasourceInfo resolveFromHub(SysTenant tenant, long datasourceId) {
        TenantDatasourceLookup lookup = datasourceLookup.getIfAvailable();
        if (lookup == null) {
            log.error("[租户数据源] 租户 {} 引用了数据源 {}，但当前应用未安装数据源台账模块，"
                    + "无法解析实例信息。已拒绝按库路由（不会回退主库，避免读到他租户数据）",
                    tenant.getSysTenantId(), datasourceId);
            return null;
        }
        TenantDatasourceInfo found;
        try {
            found = lookup.find(datasourceId);
        } catch (Exception e) {
            log.error("[租户数据源] 租户 {} 查询数据源 {} 失败: {}",
                    tenant.getSysTenantId(), datasourceId, e.getMessage());
            return null;
        }
        if (found == null) {
            log.error("[租户数据源] 租户 {} 引用的数据源 {} 不存在或已停用，已拒绝按库路由",
                    tenant.getSysTenantId(), datasourceId);
            return null;
        }
        // 库名强制取租户自己的：台账里的库名只是模板，同实例多租户时拿它路由会串库
        TenantDatasourceInfo info = new TenantDatasourceInfo(found.host(), found.port(),
                tenant.getSysTenantDbname(), found.username(), found.password(),
                TenantDatasourceInfo.Origin.DATASOURCE_HUB);
        return info.complete() ? info : null;
    }

    /**
     * 用平台自身实例构造连接信息。
     *
     * @param tenant 租户记录（提供库名）
     * @return 连接信息；平台管理连接未配置或库名缺失时返回 {@code null}
     */
    private TenantDatasourceInfo resolvePlatformSelf(SysTenant tenant) {
        TenantProperties.ServerConfig server = tenantProperties.getServer();
        AdminEndpoint admin = parseAdminUrl(server.getDatasourceRoutingAdminUrl());
        if (admin == null) {
            log.error("[租户数据源] 租户 {} 选择使用平台自身实例，但 datasource-routing-admin-url 未配置或不可解析",
                    tenant.getSysTenantId());
            return null;
        }
        TenantDatasourceInfo info = new TenantDatasourceInfo(admin.host(), admin.port(),
                tenant.getSysTenantDbname(), server.getDatasourceRoutingAdminUsername(),
                server.getDatasourceRoutingAdminPassword(), TenantDatasourceInfo.Origin.PLATFORM_SELF);
        return info.complete() ? info : null;
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
