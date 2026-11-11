package com.chua.starter.tenant.server.support;

import com.chua.starter.tenant.entity.SysTenant;
import com.chua.starter.tenant.properties.TenantProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link TenantDatabaseProvisioner} 单元测试。
 *
 * <p>只验证「开关与前置条件」这一层：真正的 DDL 需要一个带
 * CREATE DATABASE 权限的 MySQL 实例，不适合放进单元测试。
 * 但开关判断错了会直接导致「以为没开启自动建库」或「未配置管理连接却真去连」，
 * 这两类事故都发生在这里，必须有测试兜住。</p>
 *
 * @since 4.0.0.51
 * @author CH
 */
class TenantDatabaseProvisionerTest {

    /**
     * 租户配置
     */
    private TenantProperties properties;

    /**
     * 被测对象
     */
    private TenantDatabaseProvisioner provisioner;

    @BeforeEach
    void setUp() {
        properties = new TenantProperties();
        provisioner = new TenantDatabaseProvisioner(properties);
    }

    @Test
    @DisplayName("默认配置下没有管理连接：按租户逐个开通时不可用（默认已开路由与自动建库）")
    void unavailableByDefault() {
        assertFalse(provisioner.isAvailable());
    }

    @Test
    @DisplayName("只开路由开关、不开自动建库时仍不可用")
    void unavailableWhenAutoProvisionOff() {
        enableRouting();

        assertFalse(provisioner.isAvailable());
    }

    @Test
    @DisplayName("开了自动建库但没配管理连接时不可用")
    void unavailableWithoutAdminConnection() {
        updateServer(server -> {
            server.setDatasourceRoutingEnable(true);
            server.setDatasourceRoutingAutoProvision(true);
        });

        assertFalse(provisioner.isAvailable());
    }

    @Test
    @DisplayName("管理连接缺口令时仍可用——口令可以为空，但账号 URL 必须齐")
    void availableWithAdminUrlAndUsername() {
        enableAvailable();

        assertTrue(provisioner.isAvailable());
    }

    @Test
    @DisplayName("路由总开关显式关闭时，即使自动建库和连接都配好也不可用")
    void unavailableWhenRoutingDisabled() {
        updateServer(server -> {
            server.setDatasourceRoutingEnable(false);
            server.setDatasourceRoutingAutoProvision(true);
            server.setDatasourceRoutingAdminUrl("jdbc:mysql://127.0.0.1:3306/mysql");
            server.setDatasourceRoutingAdminUsername("root");
        });

        assertFalse(provisioner.isAvailable());
    }

    @Test
    @DisplayName("不可用时 provision 直接返回 false，不会尝试连接")
    void provisionSkipsWhenUnavailable() {
        SysTenant tenant = new SysTenant();
        tenant.setSysTenantId("1");
        tenant.setSysTenantDbname("t1");
        tenant.setSysTenantSqlUsername("u1");

        assertFalse(provisioner.provision(tenant, "plain"));
    }

    @Test
    @DisplayName("库名为空时跳过，不执行任何 DDL")
    void provisionSkipsWithoutDbName() {
        enableAvailable();
        SysTenant tenant = new SysTenant();
        tenant.setSysTenantId("1");
        tenant.setSysTenantSqlUsername("u1");

        assertFalse(provisioner.provision(tenant, "plain"));
    }

    @Test
    @DisplayName("库名含反引号等非法字符时拒绝，防止 SQL 注入拼接到 DDL")
    void provisionRejectsUnsafeIdentifier() {
        enableAvailable();
        SysTenant tenant = new SysTenant();
        tenant.setSysTenantId("1");
        tenant.setSysTenantDbname("t1`; DROP DATABASE x; --");
        tenant.setSysTenantSqlUsername("u1");

        assertFalse(provisioner.provision(tenant, "plain"));
    }

    @Test
    @DisplayName("非 MySQL 管理连接时跳过，不执行 MySQL 专有 DDL")
    void provisionSkipsNonMysqlAdmin() {
        enableAvailable();
        updateServer(server -> server.setDatasourceRoutingAdminUrl("jdbc:postgresql://127.0.0.1:5432/postgres"));
        SysTenant tenant = new SysTenant();
        tenant.setSysTenantId("1");
        tenant.setSysTenantDbname("t1");
        tenant.setSysTenantSqlUsername("u1");

        assertFalse(provisioner.provision(tenant, "plain"));
    }

    /**
     * 打开路由与自动建库开关。
     */
    private void enableRouting() {
        updateServer(server -> server.setDatasourceRoutingEnable(true));
    }

    /**
     * 配成「全部前置条件满足」的状态。
     */
    private void enableAvailable() {
        updateServer(server -> {
            server.setDatasourceRoutingEnable(true);
            server.setDatasourceRoutingAutoProvision(true);
            server.setDatasourceRoutingAdminUrl("jdbc:mysql://127.0.0.1:3306/mysql");
            server.setDatasourceRoutingAdminUsername("root");
        });
    }

    /**
     * 修改服务端配置。
     *
     * <p>{@link TenantProperties#getServer()} 返回的是副本（防外部改到内部状态），
     * 必须改完整体回写，否则修改会被丢弃。</p>
     *
     * @param mutator 修改动作
     */
    private void updateServer(java.util.function.Consumer<TenantProperties.ServerConfig> mutator) {
        TenantProperties.ServerConfig server = properties.getServer();
        mutator.accept(server);
        properties.setServer(server);
    }
}
