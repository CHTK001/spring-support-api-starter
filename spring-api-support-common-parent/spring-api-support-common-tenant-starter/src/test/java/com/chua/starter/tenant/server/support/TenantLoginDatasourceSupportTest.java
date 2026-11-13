package com.chua.starter.tenant.server.support;

import com.chua.starter.system.properties.SystemSecurityProperties;
import com.chua.starter.tenant.entity.SysTenant;
import com.chua.starter.tenant.properties.TenantProperties;
import com.chua.starter.tenant.server.mapper.SysTenantMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link TenantLoginDatasourceSupport} 单元测试。
 *
 * <p>守的是一条越权边界：租户<b>声明了</b>独立实例、但实例解析不出来时，
 * 不能因为 {@code isDedicated=false} 就把它当成共享库去平台主库认证——
 * 那等于让填了租户编码的人用平台账号登录并读到平台数据。正确行为是按
 * 「独立库不可用」拒绝（由 {@code switchTo} 返回 false，登录层给 503）。</p>
 *
 * @since 4.0.0.52
 * @author CH
 */
class TenantLoginDatasourceSupportTest {

    /**
     * 租户配置
     */
    private final TenantProperties properties = new TenantProperties();

    @Test
    @DisplayName("引用台账却解析失败：必须按独立库处理，交给 switchTo 拒绝")
    void brokenReferenceIsTreatedAsDedicated() {
        SysTenant tenant = tenant("tenant_b");
        tenant.setSysTenantDbname("tenant_b");
        // [P3C A20 豁免]
        tenant.setSysTenantDatasourceId(42L);

        TenantLoginDatasourceSupport support = support(tenant);
        TenantLoginDatasourceSupport.TenantLoginTarget target = support.resolveByCode("tenant_b");

        assertNotNull(target);
        assertTrue(target.dedicated(), "解析失败不能降级成共享库，否则会用平台账号认证");
        assertTrue(target.enabled());
    }

    @Test
    @DisplayName("平台自身实例但地址未配置：同样按独立库不可用处理")
    void platformSelfWithoutEndpointIsTreatedAsDedicated() {
        SysTenant tenant = tenant("tenant_c");
        tenant.setSysTenantDbname("tenant_c");
        tenant.setSysTenantDatasourceId(TenantDataSourceResolver.PLATFORM_SELF);

        TenantLoginDatasourceSupport.TenantLoginTarget target = support(tenant).resolveByCode("tenant_c");

        assertNotNull(target);
        assertTrue(target.dedicated());
    }

    @Test
    @DisplayName("自填字段完整的租户：按独立库处理")
    void completeSelfFieldsIsDedicated() {
        SysTenant tenant = tenant("tenant_a");
        tenant.setSysTenantHost("127.0.0.1");
        tenant.setSysTenantDbname("tenant_a");

        TenantLoginDatasourceSupport.TenantLoginTarget target = support(tenant).resolveByCode("tenant_a");

        assertNotNull(target);
        assertTrue(target.dedicated());
    }

    @Test
    @DisplayName("真·共享库租户：不算独立库，走主库认证")
    void sharedTenantIsNotDedicated() {
        SysTenant tenant = tenant("tenant_shared");

        TenantLoginDatasourceSupport.TenantLoginTarget target = support(tenant).resolveByCode("tenant_shared");

        assertNotNull(target);
        assertFalse(target.dedicated(), "既没选实例也没填库名，才是共享库");
    }

    @Test
    @DisplayName("按账号找租户：引用了坏实例时仍返回租户，避免落到主库认证")
    void usernameLookupKeepsBrokenReference() {
        SysTenant broken = tenant("tenant_b");
        broken.setSysTenantUsername("tenant_b_user");
        // [P3C A20 豁免]
        broken.setSysTenantDatasourceId(42L);

        assertEquals(broken.getSysTenantId(), support(broken).resolveTenantIdByUsername("tenant_b_user"));

        SysTenant shared = tenant("tenant_shared");
        shared.setSysTenantUsername("shared_user");
        assertNull(support(shared).resolveTenantIdByUsername("shared_user"),
                "共享库租户的账号本来就在主库，这里应回 null");
    }

    /**
     * 构造被测对象。
     *
     * @param tenant 查询桩固定返回的租户
     * @return 被测对象
     */
    private TenantLoginDatasourceSupport support(SysTenant tenant) {
        TenantDataSourceResolver resolver = new TenantDataSourceResolver(properties,
                new StaticListableBeanFactory().getBeanProvider(TenantDatasourceLookup.class));
        TenantDataSourceRegistry registry = new TenantDataSourceRegistry(properties,
                new TenantPasswordCipher(new SystemSecurityProperties()));
        return new TenantLoginDatasourceSupport(mapperStub(tenant), registry, resolver);
    }

    /**
     * 构造一个租户记录。
     *
     * @param code 租户编码
     * @return 租户记录
     */
    private static SysTenant tenant(String code) {
        SysTenant tenant = new SysTenant();
        tenant.setSysTenantId("1950000000000000009");
        tenant.setSysTenantCode(code);
        tenant.setSysTenantStatus(0);
        return tenant;
    }

    /**
     * 查询桩：{@code selectOne}/{@code selectById} 都返回固定租户。
     *
     * @param tenant 固定租户
     * @return 桩
     */
    private static SysTenantMapper mapperStub(SysTenant tenant) {
        return (SysTenantMapper) Proxy.newProxyInstance(
                SysTenantMapper.class.getClassLoader(),
                new Class<?>[]{SysTenantMapper.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "selectOne", "selectById" -> tenant;
                    case "toString" -> "StubTenantMapper";
                    default -> null;
                });
    }
}
