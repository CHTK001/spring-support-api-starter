package com.chua.starter.tenant.server.support;

import com.chua.starter.tenant.entity.SysTenant;
import com.chua.starter.tenant.properties.TenantProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * 租户独立库自动开通器。
 *
 * <p>建租户时按 {@code sys_tenant_host/port/dbname} 自动执行
 * {@code CREATE DATABASE IF NOT EXISTS} + 建账号 + 授权，并校验租户账号真能连上。</p>
 *
 * <p><b>为什么不复用 {@code DataSourceScriptConfiguration}</b>：那套 Flyway-like 迁移器绑定
 * 在应用主数据源上，且带版本表/校验和语义；租户库是「新建空库」，复用要先绕开版本表。
 * 这里只用 {@link JdbcTemplate} 按顺序执行语句。业务表结构与种子数据由
 * {@link TenantDatabaseInitializer} 紧接着用同一批 {@code db/init} 脚本初始化，
 * 两者职责分开：本类只管「库和账号存在」，那边管「库里有表」。</p>
 *
 * <p><b>安全边界</b>：库名/账号只允许 {@code [A-Za-z0-9_]}（标识符白名单），
 * 口令无法参数化（DDL 不支持占位符），故单独做引号与反斜杠转义。
 * 管理连接缺失时整步跳过，不影响建租户。</p>
 *
 * <p><b>幂等</b>：库与账号都可能已存在，统一用 {@code IF NOT EXISTS}；
 * 口令用 {@code ALTER USER} 覆盖，保证平台记录与库内口令一致。</p>
 *
 * @since 4.0.0.51
 * @author CH
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantDatabaseProvisioner {

    /**
     * 标识符白名单：库名/账号只允许字母数字下划线
     */
    private static final String SAFE_IDENTIFIER = "^[A-Za-z0-9_]{1,64}$";

    /**
     * 租户配置
     */
    private final TenantProperties tenantProperties;

    /**
     * 是否为「租户库自动开通」可用状态。
     *
     * @return 已启用且管理连接配置完整时返回 true
     */
    public boolean isAvailable() {
        TenantProperties.ServerConfig server = tenantProperties.getServer();
        if (!server.isDatasourceRoutingEnable() || !server.isDatasourceRoutingAutoProvision()) {
            return false;
        }
        return hasText(server.getDatasourceRoutingAdminUrl())
                && hasText(server.getDatasourceRoutingAdminUsername());
    }

    /**
     * 开通租户库。
     *
     * <p>失败只记录错误并返回 {@code false}，不抛异常打断建租户——租户记录已落库，
     * 建库失败应该由运维补做，而不是让接口 500。</p>
     *
     * @param tenant           租户记录
     * @param plainSqlPassword 明文库口令（落库版本是密文，建账号必须用明文）
     * @return 库已就绪返回 true；未启用或失败返回 false
     */
    public boolean provision(SysTenant tenant, String plainSqlPassword) {
        if (!isAvailable() || tenant == null) {
            return false;
        }
        String dbName = tenant.getSysTenantDbname();
        String username = tenant.getSysTenantSqlUsername();
        if (!hasText(dbName) || !hasText(username)) {
            log.warn("[租户建库] 租户 {} 未填写库名或数据库账号，跳过自动建库", tenant.getSysTenantId());
            return false;
        }
        if (!dbName.matches(SAFE_IDENTIFIER) || !username.matches(SAFE_IDENTIFIER)) {
            log.error("[租户建库] 库名/账号含非法字符，已拒绝: db={}, user={}", dbName, username);
            return false;
        }
        if (!hasText(plainSqlPassword)) {
            log.warn("[租户建库] 租户 {} 未填写数据库口令，跳过自动建库", tenant.getSysTenantId());
            return false;
        }
        // 下面的 DDL 是 MySQL 语法（CREATE USER ...@'%' / GRANT ... ON db.*）。
        // 主库或租户库换成 PostgreSQL 之类必须先实现对应方言，否则会在这里静默失败，
        // 表现为「租户建了、库没建」，故显式挡在入口。
        if (!isMysqlAdmin(tenantProperties.getServer().getDatasourceRoutingAdminUrl())) {
            log.warn("[租户建库] 管理连接不是 MySQL，自动建库仅支持 MySQL，已跳过: {}",
                    tenantProperties.getServer().getDatasourceRoutingAdminUrl());
            return false;
        }

        try {
            JdbcTemplate admin = new JdbcTemplate(adminDataSource());

            admin.execute("CREATE DATABASE IF NOT EXISTS `" + dbName + "` "
                    + "DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci");
            log.info("[租户建库] 数据库就绪: {}", dbName);

            String literal = escapeLiteral(plainSqlPassword);
            admin.execute("CREATE USER IF NOT EXISTS '" + username + "'@'%' IDENTIFIED BY '" + literal + "'");
            // 已存在时 CREATE USER 不生效，口令可能与平台记录不一致，必须显式覆盖
            admin.execute("ALTER USER '" + username + "'@'%' IDENTIFIED BY '" + literal + "'");
            admin.execute("GRANT ALL PRIVILEGES ON `" + dbName + "`.* TO '" + username + "'@'%'");
            admin.execute("FLUSH PRIVILEGES");
            log.info("[租户建库] 账号与授权就绪: user={}, db={}", username, dbName);

            verifyConnectivity(tenant, dbName, plainSqlPassword);
            return true;
        } catch (Exception e) {
            log.error("[租户建库] 租户 {} 自动建库失败: {}", tenant.getSysTenantId(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 用租户自己的账号连一次，确认授权真的生效。
     *
     * <p>这是「建库成功」和「租户库可用」之间最容易漏掉的一环：账号建了但 host 不匹配、
     * 授权没生效，都会在这里暴露出来。校验失败不改返回值，只告警，因为库本身已经可用。</p>
     *
     * @param tenant           租户记录
     * @param dbName           库名
     * @param plainSqlPassword 明文库口令
     */
    private void verifyConnectivity(SysTenant tenant, String dbName, String plainSqlPassword) {
        String url = TenantJdbcUrlBuilder.build(tenant, tenantProperties);
        if (url == null) {
            return;
        }
        DriverManagerDataSource probe = new DriverManagerDataSource(url,
                tenant.getSysTenantSqlUsername(), plainSqlPassword);
        try (var ignored = probe.getConnection()) {
            log.info("[租户建库] 连通性校验通过: {}", dbName);
        } catch (Exception e) {
            log.warn("[租户建库] 连通性校验失败（库已建好，请检查网络/授权）: {}", e.getMessage());
        }
    }

    /**
     * 管理连接是否为 MySQL。
     *
     * @param adminUrl 管理连接 URL
     * @return 是 MySQL 协议返回 true
     */
    private boolean isMysqlAdmin(String adminUrl) {
        return adminUrl != null && adminUrl.trim().toLowerCase(java.util.Locale.ROOT).startsWith("jdbc:mysql:");
    }

    /**
     * 构造管理连接。
     *
     * @return 管理数据源
     */
    private DataSource adminDataSource() {
        TenantProperties.ServerConfig server = tenantProperties.getServer();
        return new DriverManagerDataSource(
                server.getDatasourceRoutingAdminUrl(),
                server.getDatasourceRoutingAdminUsername(),
                server.getDatasourceRoutingAdminPassword()
        );
    }

    /**
     * SQL 字符串字面量转义（只用于 {@code IDENTIFIED BY '...'}）。
     *
     * @param value 原始口令
     * @return 转义后的口令
     */
    private String escapeLiteral(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("'", "''");
    }

    /**
     * 是否包含文本。
     *
     * @param value 待判断值
     * @return 非空白返回 true
     */
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
