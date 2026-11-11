package com.chua.starter.tenant.server.support;

import com.chua.starter.tenant.entity.SysTenant;
import com.chua.starter.tenant.properties.TenantProperties;

/**
 * 租户库 JDBC URL 构造器。
 *
 * <p>注册器（建连接池）和开通器（建库后做连通性校验）必须拼出一模一样的 URL，
 * 否则会出现「建库校验通过、路由却连不上」这种最难查的偏差。所以两处共用本类。</p>
 *
 * <p><b>为什么用模板而不是写死 MySQL</b>：平台主库可以是 MySQL / PostgreSQL / H2，
 * 租户库也可能与主库同类型。模板放配置里（{@code datasource-routing-url-template}），
 * 换库只改配置，不动代码。</p>
 *
 * @since 4.0.0.51
 * @author CH
 */
public final class TenantJdbcUrlBuilder {

    /**
     * 主机占位符
     */
    private static final String PLACEHOLDER_HOST = "{host}";

    /**
     * 端口占位符
     */
    private static final String PLACEHOLDER_PORT = "{port}";

    /**
     * 库名占位符
     */
    private static final String PLACEHOLDER_DBNAME = "{dbname}";

    private TenantJdbcUrlBuilder() {
    }

    /**
     * 按租户记录构造 JDBC URL。
     *
     * <p>只读租户自填字段，等价于 {@code build(解析自租户字段的连接信息, properties)}；
     * 新增的「引用数据源台账」路径必须走
     * {@link TenantDataSourceResolver} 解析后再调 {@link #build(TenantDatasourceInfo, TenantProperties)}。</p>
     *
     * @param tenant     租户记录
     * @param properties 租户配置（提供 URL 模板）
     * @return JDBC URL；信息不完整时返回 {@code null}
     */
    public static String build(SysTenant tenant, TenantProperties properties) {
        if (tenant == null) {
            return null;
        }
        return build(tenantFieldInfo(tenant), properties);
    }

    /**
     * 按连接信息构造 JDBC URL。
     *
     * <p>端口为空或非正数时整体省略（用驱动默认端口）；地址或库名缺失视为「未登记租户库」。</p>
     *
     * @param info       连接信息
     * @param properties 租户配置（提供 URL 模板）
     * @return JDBC URL；信息不完整时返回 {@code null}
     */
    public static String build(TenantDatasourceInfo info, TenantProperties properties) {
        if (info == null || properties == null) {
            return null;
        }
        String host = info.host();
        String dbName = info.dbName();
        if (!hasText(host) || !hasText(dbName)) {
            return null;
        }

        Integer port = info.port();
        String portPart = port == null || port <= 0 ? "" : ":" + port;
        String template = properties.getServer().getDatasourceRoutingUrlTemplate();
        if (!hasText(template)) {
            return null;
        }
        return template
                .replace(PLACEHOLDER_HOST, host.trim())
                .replace(PLACEHOLDER_PORT, portPart)
                .replace(PLACEHOLDER_DBNAME, dbName.trim());
    }

    /**
     * 构造「实例级」JDBC URL（库名留空），用于建库/建账号的管理连接。
     *
     * <p>建库必须连到实例而不是某个租户库：MySQL 的 {@code CREATE DATABASE} 不依赖当前库，
     * 但连到一个尚不存在的库会直接连接失败。这里把模板里的 {@code {dbname}} 替成空串，
     * 得到 {@code jdbc:mysql://host:3306/?...}，由驱动落到实例默认库。</p>
     *
     * @param info       连接信息（只需 host/port）
     * @param properties 租户配置（提供 URL 模板）
     * @return 实例级 JDBC URL；地址缺失时返回 {@code null}
     */
    public static String buildInstanceUrl(TenantDatasourceInfo info, TenantProperties properties) {
        if (info == null || properties == null || !hasText(info.host())) {
            return null;
        }
        Integer port = info.port();
        String portPart = port == null || port <= 0 ? "" : ":" + port;
        String template = properties.getServer().getDatasourceRoutingUrlTemplate();
        if (!hasText(template)) {
            return null;
        }
        return template
                .replace(PLACEHOLDER_HOST, info.host().trim())
                .replace(PLACEHOLDER_PORT, portPart)
                .replace(PLACEHOLDER_DBNAME, "");
    }

    /**
     * 把租户自填字段包装成连接信息。
     *
     * <p>地址优先 {@code sys_tenant_host}，为空回退 {@code sys_tenant_ip}。</p>
     *
     * @param tenant 租户记录
     * @return 连接信息（字段可能不完整，由 {@link #build(TenantDatasourceInfo, TenantProperties)} 判定）
     */
    private static TenantDatasourceInfo tenantFieldInfo(SysTenant tenant) {
        String host = hasText(tenant.getSysTenantHost()) ? tenant.getSysTenantHost() : tenant.getSysTenantIp();
        return new TenantDatasourceInfo(host, tenant.getSysTenantPort(), tenant.getSysTenantDbname(),
                tenant.getSysTenantSqlUsername(), tenant.getSysTenantSqlPassword(),
                TenantDatasourceInfo.Origin.TENANT_FIELDS);
    }

    /**
     * 日志里隐去口令。
     *
     * @param url JDBC URL
     * @return 脱敏后的 URL
     */
    public static String mask(String url) {
        return url == null ? null : url.replaceAll("(?i)(password|pwd)=[^&]*", "$1=***");
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
}
