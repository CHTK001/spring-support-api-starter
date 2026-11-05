// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.tenant.server.support;

import com.chua.starter.tenant.entity.SysTenant;
import com.chua.starter.tenant.properties.TenantProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link TenantDatabaseInitializer} 单元测试。
 *
 * <p>真正的迁移执行需要一个可达的 MySQL 实例，不适合放进单元测试；
 * 这里覆盖的是「开关、前置条件、脚本位置解析」这一层 —— 判断错了会直接表现为
 * 「建完库一张表都没有」或「开关关掉了却还是去连库」。</p>
 *
 * @since 4.0.0.51
 * @author CH
 */
class TenantDatabaseInitializerTest {

    /**
     * 租户配置
     */
    private TenantProperties properties;

    /**
     * 被测对象
     */
    private TenantDatabaseInitializer initializer;

    @BeforeEach
    void setUp() {
        properties = new TenantProperties();
        initializer = new TenantDatabaseInitializer(properties, new TenantPasswordCipherStub());
    }

    @Test
    @DisplayName("默认开启自动初始化，避免只建空库")
    void autoInitScriptsEnabledByDefault() {
        assertTrue(properties.getServer().isDatasourceRoutingAutoInitScripts());
    }

    @Test
    @DisplayName("默认同时执行种子脚本——没有 sys_user 的租户库连登录都做不到")
    void initDataEnabledByDefault() {
        assertTrue(properties.getServer().isDatasourceRoutingInitDataEnable());
    }

    @Test
    @DisplayName("默认脚本位置与平台主库一致，保证两边表结构同源")
    void defaultLocationsMatchPlatform() {
        List<String> locations = initializer.resolveLocations();

        assertEquals(List.of("classpath*:db/init/*.sql", "classpath*:db/migration/*.sql"), locations);
    }

    @Test
    @DisplayName("脚本位置支持逗号、分号、换行混用，并去空白去重")
    void locationsAcceptMixedSeparators() {
        updateServer(server -> server.setDatasourceRoutingInitScriptLocations(
                " classpath*:db/init/*.sql , classpath*:db/migration/*.sql;\nclasspath*:db/init/*.sql "));

        List<String> locations = initializer.resolveLocations();

        assertEquals(2, locations.size());
        assertEquals("classpath*:db/init/*.sql", locations.get(0));
        assertEquals("classpath*:db/migration/*.sql", locations.get(1));
    }

    @Test
    @DisplayName("脚本位置配空时不猜默认值，直接返回空列表由调用方报错")
    void blankLocationsYieldEmptyList() {
        updateServer(server -> server.setDatasourceRoutingInitScriptLocations("   "));

        assertTrue(initializer.resolveLocations().isEmpty());
    }

    @Test
    @DisplayName("开关关闭时直接跳过，不会尝试连接租户库")
    void skipsWhenAutoInitDisabled() {
        updateServer(server -> server.setDatasourceRoutingAutoInitScripts(false));

        TenantDatabaseInitializer.InitResult result = initializer.initialize(tenantWithDatabase(), "plain");

        assertEquals(TenantDatabaseInitializer.InitResult.Status.SKIPPED, result.status());
        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("租户未登记库地址时失败，但不抛异常（租户记录已落库，不能 500）")
    void failsWhenDatabaseNotRegistered() {
        SysTenant tenant = new SysTenant();
        tenant.setSysTenantId("1");
        tenant.setSysTenantSqlUsername("u1");

        TenantDatabaseInitializer.InitResult result = initializer.initialize(tenant, "plain");

        assertEquals(TenantDatabaseInitializer.InitResult.Status.FAILED, result.status());
        assertTrue(result.message().contains("库地址"));
    }

    @Test
    @DisplayName("租户未登记数据库账号时失败，避免用空账号去连库")
    void failsWithoutUsername() {
        SysTenant tenant = tenantWithDatabase();
        tenant.setSysTenantSqlUsername(null);

        TenantDatabaseInitializer.InitResult result = initializer.initialize(tenant, "plain");

        assertEquals(TenantDatabaseInitializer.InitResult.Status.FAILED, result.status());
        assertTrue(result.message().contains("账号"));
    }

    @Test
    @DisplayName("没有口令时失败，不做无口令连接尝试")
    void failsWithoutPassword() {
        SysTenant tenant = tenantWithDatabase();
        tenant.setSysTenantSqlPassword(null);

        TenantDatabaseInitializer.InitResult result = initializer.initialize(tenant, null);

        assertEquals(TenantDatabaseInitializer.InitResult.Status.FAILED, result.status());
        assertTrue(result.message().contains("口令"));
    }

    @Test
    @DisplayName("前置条件齐备但库连不上时返回失败而不是抛出，让建租户流程继续")
    void unreachableDatabaseReturnsFailure() {
        // 指向一个必然连不上的地址：这里验证的是异常被收敛成 InitResult，
        // 而不是把它抛给建租户接口变成 500
        updateServer(server -> server.setDatasourceRoutingInitScriptLocations("classpath*:db/init/*.sql"));
        SysTenant tenant = tenantWithDatabase();
        tenant.setSysTenantHost("127.0.0.1");
        tenant.setSysTenantPort(1);

        TenantDatabaseInitializer.InitResult result =
                initializer.initialize(tenant, "plain");

        assertEquals(TenantDatabaseInitializer.InitResult.Status.FAILED, result.status());
    }

    /**
     * 构造一个「库地址与账号口令都齐备」的租户记录。
     *
     * @return 租户记录
     */
    private SysTenant tenantWithDatabase() {
        SysTenant tenant = new SysTenant();
        tenant.setSysTenantId("1");
        tenant.setSysTenantHost("127.0.0.1");
        tenant.setSysTenantPort(3306);
        tenant.setSysTenantDbname("tenant_1");
        tenant.setSysTenantSqlUsername("t1");
        tenant.setSysTenantSqlPassword("plain");
        return tenant;
    }

    /**
     * 修改服务端配置。
     *
     * <p>{@link TenantProperties#getServer()} 返回副本，必须改完整体回写。</p>
     *
     * @param mutator 修改动作
     */
    private void updateServer(java.util.function.Consumer<TenantProperties.ServerConfig> mutator) {
        TenantProperties.ServerConfig server = properties.getServer();
        mutator.accept(server);
        properties.setServer(server);
    }

    /**
     * 口令加解密桩：单元测试不加载 AES 密钥配置，明文原样返回。
     */
    private static class TenantPasswordCipherStub extends TenantPasswordCipher {

        /**
         * 无密钥配置的桩，绕过 {@code SystemSecurityProperties} 依赖。
         */
        TenantPasswordCipherStub() {
            super(null);
        }

        @Override
        public String decrypt(String stored) {
            return stored;
        }
    }
}