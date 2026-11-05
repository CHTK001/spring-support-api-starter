// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.tenant.server.support;

import com.chua.starter.tenant.entity.SysTenant;
import com.chua.starter.tenant.properties.TenantProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link TenantJdbcUrlBuilder} 单元测试。
 *
 * <p>覆盖「注册器建池」和「开通器连通性校验」共用的 URL 构造：
 * 两处必须拼出同一个串，否则会出现建库校验通过、路由却连不上的偏差。</p>
 *
 * @since 4.0.0.51
 * @author CH
 */
class TenantJdbcUrlBuilderTest {

    @Test
    @DisplayName("host/port/dbname 齐全时按模板拼接")
    void buildsUrlFromCompleteTenant() {
        SysTenant tenant = tenant("10.0.0.8", 3307, "tenant_a");
        String url = TenantJdbcUrlBuilder.build(tenant, properties());

        assertTrue(url.startsWith("jdbc:mysql://10.0.0.8:3307/tenant_a"), url);
    }

    @Test
    @DisplayName("端口为空时省略端口，交给驱动默认值")
    void omitsPortWhenAbsent() {
        SysTenant tenant = tenant("10.0.0.8", null, "tenant_a");
        String url = TenantJdbcUrlBuilder.build(tenant, properties());

        assertTrue(url.startsWith("jdbc:mysql://10.0.0.8/tenant_a"), url);
    }

    @Test
    @DisplayName("端口为 0 或负数时同样省略，不拼出非法端口")
    void omitsNonPositivePort() {
        assertTrue(TenantJdbcUrlBuilder.build(tenant("h", 0, "db"), properties())
                .startsWith("jdbc:mysql://h/db"));
        assertTrue(TenantJdbcUrlBuilder.build(tenant("h", -1, "db"), properties())
                .startsWith("jdbc:mysql://h/db"));
    }

    @Test
    @DisplayName("host 为空时回退 sys_tenant_ip")
    void fallsBackToIp() {
        SysTenant tenant = tenant(null, 3306, "tenant_a");
        tenant.setSysTenantIp("192.168.1.9");

        assertTrue(TenantJdbcUrlBuilder.build(tenant, properties())
                .startsWith("jdbc:mysql://192.168.1.9:3306/tenant_a"));
    }

    @Test
    @DisplayName("库名缺失视为未登记租户库")
    void returnsNullWhenDbNameMissing() {
        assertNull(TenantJdbcUrlBuilder.build(tenant("h", 3306, null), properties()));
        assertNull(TenantJdbcUrlBuilder.build(tenant("h", 3306, "   "), properties()));
    }

    @Test
    @DisplayName("地址与库名都缺失时返回 null")
    void returnsNullWhenHostMissing() {
        SysTenant tenant = tenant(null, 3306, "tenant_a");
        assertNull(TenantJdbcUrlBuilder.build(tenant, properties()));
    }

    @Test
    @DisplayName("地址被改过时保留模板里的其它参数")
    void keepsTemplateParameters() {
        TenantProperties properties = properties();
        // getServer() 返回副本，必须整体回写才生效（TenantProperties 的既有约定）
        TenantProperties.ServerConfig server = properties.getServer();
        server.setDatasourceRoutingUrlTemplate("jdbc:postgresql://{host}{port}/{dbname}?sslmode=disable");
        properties.setServer(server);

        String url = TenantJdbcUrlBuilder.build(tenant("pg", 5432, "t1"), properties);

        assertEquals("jdbc:postgresql://pg:5432/t1?sslmode=disable", url);
    }

    @Test
    @DisplayName("日志脱敏不泄漏口令参数")
    void masksPasswordInLog() {
        String masked = TenantJdbcUrlBuilder.mask("jdbc:mysql://h/db?password=secret&x=1");

        assertTrue(masked.contains("password=***"));
        assertTrue(!masked.contains("secret"));
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