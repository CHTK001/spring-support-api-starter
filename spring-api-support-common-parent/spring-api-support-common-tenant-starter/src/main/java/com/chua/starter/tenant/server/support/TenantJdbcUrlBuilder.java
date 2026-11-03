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
     * <p>地址优先 {@code sys_tenant_host}，为空回退 {@code sys_tenant_ip}；
     * 端口为空时整体省略（用驱动默认端口）。地址或库名缺失视为「未登记租户库」。</p>
     *
     * @param tenant     租户记录
     * @param properties 租户配置（提供 URL 模板）
     * @return JDBC URL；信息不完整时返回 {@code null}
     */
    public static String build(SysTenant tenant, TenantProperties properties) {
        if (tenant == null || properties == null) {
            return null;
        }
        String host = hasText(tenant.getSysTenantHost()) ? tenant.getSysTenantHost() : tenant.getSysTenantIp();
        String dbName = tenant.getSysTenantDbname();
        if (!hasText(host) || !hasText(dbName)) {
            return null;
        }

        Integer port = tenant.getSysTenantPort();
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
