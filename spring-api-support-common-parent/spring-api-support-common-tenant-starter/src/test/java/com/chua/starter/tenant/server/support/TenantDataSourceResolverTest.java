package com.chua.starter.tenant.server.support;

import com.chua.starter.tenant.entity.SysTenant;
import com.chua.starter.tenant.properties.TenantProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link TenantDataSourceResolver} 单元测试。
 *
 * <p>覆盖三路优先级与「引用型租户绝不静默回退」这条安全红线：</p>
 * <ol>
 *   <li>自填字段：存量行为，逐字节不变；</li>
 *   <li>数据源台账：库名强制取租户自己的，台账没有实现/查不到时返回 {@code null}；</li>
 *   <li>平台自身实例：地址与端口来自 {@code datasource-routing-admin-url}。</li>
 * </ol>
 *
 * <p>不使用 mockito（规约禁止），依赖用手写桩。</p>
 *
 * @since 4.0.0.52
 * @author CH
 */
class TenantDataSourceResolverTest {

    @Test
    @DisplayName("自填字段：host 优先于 ip，整段作为连接信息")
    void resolvesFromTenantFields() {
        SysTenant tenant = tenant("10.0.0.8", 3307, "tenant_a");
        tenant.setSysTenantIp("192.168.1.9");
        tenant.setSysTenantSqlUsername("u");
        tenant.setSysTenantSqlPassword("p");

        TenantDatasourceInfo info = resolver(null, properties()).resolve(tenant);

        assertNotNull(info);
        assertEquals(TenantDatasourceInfo.Origin.TENANT_FIELDS, info.origin());
        assertEquals("10.0.0.8", info.host());
        assertEquals(Integer.valueOf(3307), info.port());
        assertEquals("tenant_a", info.dbName());
        assertEquals("u", info.username());
    }

    @Test
    @DisplayName("自填字段：host 为空时回退 ip（与 TenantJdbcUrlBuilder 口径一致）")
    void fallsBackToIp() {
        SysTenant tenant = tenant(null, 3306, "tenant_a");
        tenant.setSysTenantIp("192.168.1.9");

        TenantDatasourceInfo info = resolver(null, properties()).resolve(tenant);

        assertNotNull(info);
        assertEquals("192.168.1.9", info.host());
    }

    @Test
    @DisplayName("自填字段：地址或库名缺失视为未登记")
    void tenantFieldsIncomplete() {
        TenantDataSourceResolver resolver = resolver(null, properties());

        assertNull(resolver.resolve(tenant("h", 3306, null)));
        assertNull(resolver.resolve(tenant(null, 3306, "db")));
        assertNull(resolver.resolve(null));
    }

    @Test
    @DisplayName("引用台账：地址端口账号口令取台账，库名取租户自己的")
    void resolvesFromHub() {
        SysTenant tenant = tenant(null, null, "tenant_b");
        tenant.setSysTenantDatasourceId(42L);
        TenantDatasourceInfo hub = new TenantDatasourceInfo("db.internal", 5432, "模板库",
                "hub_user", "hub_pwd", TenantDatasourceInfo.Origin.DATASOURCE_HUB);

        TenantDatasourceInfo info = resolver(hub, properties()).resolve(tenant);

        assertNotNull(info);
        assertEquals(TenantDatasourceInfo.Origin.DATASOURCE_HUB, info.origin());
        assertEquals("db.internal", info.host());
        assertEquals(Integer.valueOf(5432), info.port());
        assertEquals("tenant_b", info.dbName(), "库名必须取租户自己的，防止串库");
        assertEquals("hub_user", info.username());
    }

    @Test
    @DisplayName("引用台账但应用没装台账模块：返回 null，绝不回退旧字段")
    void hubMissingLookupDoesNotFallBack() {
        SysTenant tenant = tenant("10.0.0.8", 3306, "tenant_a");
        tenant.setSysTenantSqlUsername("u");
        tenant.setSysTenantDatasourceId(42L);

        assertNull(resolver(null, properties()).resolve(tenant));
        assertFalse(resolver(null, properties()).isDedicated(tenant));
    }

    @Test
    @DisplayName("引用台账但查不到记录：返回 null")
    void hubRecordMissing() {
        SysTenant tenant = tenant(null, null, "tenant_b");
        tenant.setSysTenantDatasourceId(7L);

        assertNull(resolver(null, properties()).resolve(tenant));
    }

    @Test
    @DisplayName("引用台账但租户没填库名：视为未登记")
    void hubWithoutDbName() {
        SysTenant tenant = tenant(null, null, "  ");
        tenant.setSysTenantDatasourceId(42L);
        TenantDatasourceInfo hub = new TenantDatasourceInfo("db.internal", 3306, null,
                "u", "p", TenantDatasourceInfo.Origin.DATASOURCE_HUB);

        assertNull(resolver(hub, properties()).resolve(tenant));
    }

    @Test
    @DisplayName("-1 表示平台自身实例：地址端口取管理连接，库名取租户自己的")
    void resolvesPlatformSelf() {
        TenantProperties properties = properties();
        TenantProperties.ServerConfig server = properties.getServer();
        server.setDatasourceRoutingAdminUrl("jdbc:mysql://127.0.0.1:3306/mysql?useSSL=false");
        server.setDatasourceRoutingAdminUsername("root");
        server.setDatasourceRoutingAdminPassword("root");
        properties.setServer(server);

        SysTenant tenant = tenant("10.9.9.9", 1234, "tenant_c");
        tenant.setSysTenantDatasourceId(TenantDataSourceResolver.PLATFORM_SELF);

        TenantDatasourceInfo info = resolver(null, properties).resolve(tenant);

        assertNotNull(info);
        assertEquals(TenantDatasourceInfo.Origin.PLATFORM_SELF, info.origin());
        assertEquals("127.0.0.1", info.host());
        assertEquals(Integer.valueOf(3306), info.port());
        assertEquals("tenant_c", info.dbName());
        assertEquals("root", info.username());
    }

    @Test
    @DisplayName("-1 但管理连接未配置：返回 null")
    void platformSelfWithoutAdminUrl() {
        SysTenant tenant = tenant(null, null, "tenant_c");
        tenant.setSysTenantDatasourceId(TenantDataSourceResolver.PLATFORM_SELF);

        assertNull(resolver(null, properties()).resolve(tenant));
    }

    @Test
    @DisplayName("管理连接地址解析：端口可缺省，参数串与库名被丢弃")
    void parsesAdminUrl() {
        assertEquals(new TenantDataSourceResolver.AdminEndpoint("h", 3306),
                TenantDataSourceResolver.parseAdminUrl("jdbc:mysql://h:3306/db?x=1"));
        assertEquals(new TenantDataSourceResolver.AdminEndpoint("h", null),
                TenantDataSourceResolver.parseAdminUrl("jdbc:mysql://h/db"));
        assertEquals(new TenantDataSourceResolver.AdminEndpoint("h", null),
                TenantDataSourceResolver.parseAdminUrl("jdbc:postgresql://h/db;sslmode=disable"));
        assertNull(TenantDataSourceResolver.parseAdminUrl("h:3306/db"));
        assertNull(TenantDataSourceResolver.parseAdminUrl(null));
    }

    @Test
    @DisplayName("isDedicated 与 resolve 同源：只要 resolve 得出连接信息就算已登记")
    void isDedicatedMatchesResolve() {
        TenantDataSourceResolver resolver = resolver(null, properties());

        assertTrue(resolver.isDedicated(tenant("h", 3306, "db")));
        assertFalse(resolver.isDedicated(tenant("h", 3306, null)));
        assertFalse(resolver.isDedicated(tenant("h", 3306, "  ")));
    }

    @Test
    @DisplayName("下拉项：自填在最前，平台自身实例其次，台账项随后且不含口令")
    void optionsForForm() {
        TenantProperties properties = properties();
        TenantProperties.ServerConfig server = properties.getServer();
        server.setDatasourceRoutingAdminUrl("jdbc:mysql://127.0.0.1:3306/mysql");
        properties.setServer(server);
        TenantDatasourceLookup lookup = new TenantDatasourceLookup() {
            @Override
            public TenantDatasourceInfo find(long datasourceId) {
                return null;
            }

            @Override
            public java.util.List<TenantDatasourceOption> options() {
                return java.util.List.of(new TenantDatasourceOption(42L, "生产库", "jdbc",
                        "10.0.0.8", 3306, "template", Boolean.TRUE));
            }
        };
        StaticListableBeanFactory factory = new StaticListableBeanFactory();
        factory.addBean("lookup", lookup);
        TenantDataSourceResolver resolver = new TenantDataSourceResolver(properties,
                factory.getBeanProvider(TenantDatasourceLookup.class));

        java.util.List<TenantDatasourceOption> options = resolver.options();

        assertEquals(3, options.size());
        assertEquals(0L, options.get(0).id());
        assertNull(options.get(0).host(), "自填项没有实例地址");
        assertEquals(TenantDataSourceResolver.PLATFORM_SELF, options.get(1).id());
        assertEquals("127.0.0.1", options.get(1).host());
        assertEquals(Integer.valueOf(3306), options.get(1).port());
        assertEquals(42L, options.get(2).id());
        assertEquals("10.0.0.8", options.get(2).host());
    }

    @Test
    @DisplayName("下拉项：未装台账模块时只有自填与平台自身实例")
    void optionsWithoutHub() {
        TenantProperties properties = properties();
        TenantProperties.ServerConfig server = properties.getServer();
        server.setDatasourceRoutingAdminUrl("jdbc:mysql://127.0.0.1:3306/mysql");
        properties.setServer(server);

        java.util.List<TenantDatasourceOption> options = resolver(null, properties).options();

        assertEquals(2, options.size());
    }

    @Test
    @DisplayName("只读回显：台账来源带数据源名称，租户自填不带")
    void describeResult() {
        SysTenant tenant = tenant(null, null, "tenant_b");
        tenant.setSysTenantDatasourceId(42L);
        TenantDatasourceInfo hub = new TenantDatasourceInfo("db.internal", 3306, null,
                "hub_user", "hub_pwd", TenantDatasourceInfo.Origin.DATASOURCE_HUB);

        TenantDatasourceView view = resolver(hub, properties()).describe(tenant);

        assertNotNull(view);
        assertEquals(TenantDatasourceInfo.Origin.DATASOURCE_HUB.name(), view.origin());
        assertEquals("数据源中心", view.originLabel());
        assertEquals("db.internal", view.host());
        assertEquals("tenant_b", view.dbName());
        assertEquals("hub_user", view.username());
        assertNull(view.datasourceName(), "台账桩没实现 options()，名称可为空");
    }

    /**
     * 构造解析器。
     *
     * @param hub        台账返回的连接信息，{@code null} 表示应用未装台账模块
     * @param properties 租户配置
     * @return 解析器
     */
    private TenantDataSourceResolver resolver(TenantDatasourceInfo hub, TenantProperties properties) {
        StaticListableBeanFactory factory = new StaticListableBeanFactory();
        if (hub != null) {
            factory.addBean("lookup", (TenantDatasourceLookup) id -> hub);
        }
        return new TenantDataSourceResolver(properties, factory.getBeanProvider(TenantDatasourceLookup.class));
    }

    /**
     * 构造一个带地址的租户。
     *
     * @param host   库地址
     * @param port   端口
     * @param dbName 库名
     * @return 租户记录
     */
    private SysTenant tenant(String host, Integer port, String dbName) {
        SysTenant tenant = new SysTenant();
        tenant.setSysTenantId("1950000000000000001");
        tenant.setSysTenantHost(host);
        tenant.setSysTenantPort(port);
        tenant.setSysTenantDbname(dbName);
        return tenant;
    }

    /**
     * 默认配置。
     *
     * @return 租户配置
     */
    private TenantProperties properties() {
        return new TenantProperties();
    }
}
