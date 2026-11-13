package com.chua.starter.tenant.server.support;

import com.chua.starter.system.properties.SystemSecurityProperties;
import com.chua.starter.tenant.entity.SysTenant;
import com.chua.starter.tenant.properties.TenantProperties;
import com.chua.starter.tenant.server.mapper.SysTenantMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link TenantDedicatedAccountBackfill} 单元测试。
 *
 * <p>核心约束只有一条：<b>账号必须在实例上创建成功之后才允许写回记录</b>。
 * 顺序反了会让租户指向一个不存在的账号，该租户所有请求直接 503，而且从记录上看不出来。</p>
 *
 * @since 4.0.0.52
 * @author CH
 */
class TenantDedicatedAccountBackfillTest {

    /**
     * 候选租户
     */
    private final List<SysTenant> rows = new ArrayList<>();

    /**
     * 建库/建账号器桩
     */
    private StubProvisioner provisioner;

    /**
     * 注册器桩
     */
    private StubRegistry registry;

    /**
     * Mapper 桩记录的最后一次更新对象
     */
    private SysTenant lastUpdated;

    /**
     * 被测对象
     */
    private TenantDedicatedAccountBackfill backfill;

    @BeforeEach
    void setUp() {
        rows.clear();
        lastUpdated = null;
        provisioner = new StubProvisioner();
        registry = new StubRegistry();
        backfill = new TenantDedicatedAccountBackfill(
                mapperStub(), provisioner,
                new TenantPasswordCipher(new SystemSecurityProperties()), registry);
    }

    @Test
    @DisplayName("演练：只给出计划，不建账号也不改记录")
    void dryRunOnlyPlans() {
        rows.add(tenant());

        // [P3C A20 豁免]
        TenantDedicatedAccountBackfill.Result result = backfill.run(true, 20);

        assertEquals(1, result.items().size());
        assertEquals("PLANNED", result.items().get(0).status());
        assertEquals("t_acme", result.items().get(0).username());
        assertEquals(0, provisioner.provisionCalls, "演练不能碰实例");
        assertNull(lastUpdated, "演练不能改记录");
    }

    @Test
    @DisplayName("执行：先建账号成功，再把记录指向它，并释放旧连接池")
    void createsAccountBeforePersisting() {
        rows.add(tenant());

        // [P3C A20 豁免]
        TenantDedicatedAccountBackfill.Result result = backfill.run(false, 20);

        assertEquals("CREATED", result.items().get(0).status());
        assertEquals(1, provisioner.provisionCalls);
        assertNotNull(lastUpdated);
        assertEquals("t_acme", lastUpdated.getSysTenantSqlUsername());
        assertTrue(lastUpdated.getSysTenantSqlPassword().startsWith("ENC:"), "落库必须是密文");
        assertEquals(rows.get(0).getSysTenantId(), registry.evicted, "换账号后必须重建连接池");
    }

    @Test
    @DisplayName("建账号失败：记录保持原样，继续用实例账号路由")
    void failedProvisionLeavesRecordUntouched() {
        rows.add(tenant());
        provisioner.provisionResult = false;

        // [P3C A20 豁免]
        TenantDedicatedAccountBackfill.Result result = backfill.run(false, 20);

        assertEquals("FAILED", result.items().get(0).status());
        assertNull(lastUpdated, "账号没建出来就不能写记录");
        assertNull(registry.evicted);
    }

    @Test
    @DisplayName("非 MySQL 实例：跳过，不生成专用账号")
    void skipsNonMysqlInstance() {
        rows.add(tenant());
        provisioner.supported = false;

        // [P3C A20 豁免]
        TenantDedicatedAccountBackfill.Result result = backfill.run(false, 20);

        assertEquals("SKIPPED", result.items().get(0).status());
        assertEquals(0, provisioner.provisionCalls);
        assertNull(lastUpdated);
    }

    @Test
    @DisplayName("管理连接不可用：跳过而不是硬建")
    void skipsWhenManagerUnavailable() {
        rows.add(tenant());
        provisioner.available = false;

        // [P3C A20 豁免]
        TenantDedicatedAccountBackfill.Result result = backfill.run(false, 20);

        assertEquals("SKIPPED", result.items().get(0).status());
        assertEquals(0, provisioner.provisionCalls);
    }

    /**
     * 构造候选租户。
     *
     * @return 引用平台自身实例、没有 sql 账号的租户
     */
    private static SysTenant tenant() {
        SysTenant tenant = new SysTenant();
        tenant.setSysTenantId("1950000000000000123");
        tenant.setSysTenantCode("acme");
        tenant.setSysTenantDbname("tenant_acme");
        tenant.setSysTenantDatasourceId(TenantDataSourceResolver.PLATFORM_SELF);
        return tenant;
    }

    /**
     * 台账 Mapper 桩。
     *
     * @return 桩
     */
    private SysTenantMapper mapperStub() {
        return (SysTenantMapper) Proxy.newProxyInstance(
                SysTenantMapper.class.getClassLoader(),
                new Class<?>[]{SysTenantMapper.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "selectList" -> new ArrayList<>(rows);
                    case "selectCount" -> (long) rows.size();
                    case "updateById" -> {
                        lastUpdated = (SysTenant) args[0];
                        yield 1;
                    }
                    case "toString" -> "StubTenantMapper";
                    default -> null;
                });
    }

    /**
     * 建库/建账号器桩：不连库，只记录行为。
     */
    private static final class StubProvisioner extends TenantDatabaseProvisioner {

        /**
         * 是否支持专用账号
         */
        private boolean supported = true;

        /**
         * 管理连接是否可用
         */
        private boolean available = true;

        /**
         * 建账号结果
         */
        private boolean provisionResult = true;

        /**
         * provision 调用次数
         */
        private int provisionCalls;

        private StubProvisioner() {
            super(new TenantProperties());
        }

        @Override
        public boolean supportsDedicatedAccount(SysTenant tenant) {
            return supported;
        }

        @Override
        public boolean isAvailable(SysTenant tenant) {
            return available;
        }

        @Override
        public boolean provision(SysTenant tenant, String plainSqlPassword) {
            provisionCalls++;
            return provisionResult;
        }
    }

    /**
     * 注册器桩：只记录 evict。
     */
    private static final class StubRegistry extends TenantDataSourceRegistry {

        /**
         * 最后一次释放的租户标识
         */
        private String evicted;

        private StubRegistry() {
            super(new TenantProperties(), new TenantPasswordCipher(new SystemSecurityProperties()));
        }

        @Override
        public void evict(String tenantId) {
            this.evicted = tenantId;
        }
    }
}
