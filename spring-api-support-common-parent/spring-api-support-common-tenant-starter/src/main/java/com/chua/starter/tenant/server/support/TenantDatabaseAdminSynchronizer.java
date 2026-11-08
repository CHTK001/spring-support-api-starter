package com.chua.starter.tenant.server.support;

import com.chua.common.support.utils.IdUtils;
import com.chua.starter.system.util.PasswordUtils;
import com.chua.starter.tenant.entity.SysTenant;
import com.chua.starter.tenant.properties.TenantProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.List;

/**
 * 租户库管理员账号同步器。
 *
 * <p><b>解决什么问题</b>：{@link TenantDatabaseProvisioner} 只建「库 + 数据库账号」，
 * {@link TenantDatabaseInitializer} 只建「表 + 种子」。两者都做完之后，租户库里能登录的
 * 仍然只有种子里的 {@code sa/admin/ops}（口令 {@code admin123}）——
 * <b>租户自己的账号在租户库里根本不存在</b>。于是「租户建了、库建了、租户却登不进自己的库」
 * 这种最难解释的状态会一直留着，只能靠人工进库补一条 {@code sys_user}。</p>
 *
 * <p>本类补上这一环：把平台租户账号（{@code sys_tenant_username} / {@code sys_tenant_password}）
 * 作为<b>租户库管理员</b>写进租户库的 {@code sys_user}，并绑定 {@code SUPER_ADMIN} 角色。
 * 平台侧重置租户口令时同步改这条记录，保证「平台改完密码，租户端立刻能用新密码登进自己的库」。</p>
 *
 * <p><b>为什么不是给库账号（{@code sys_tenant_username_sql}）改密</b>：那是 MySQL 层面的
 * 连接账号，由 {@link TenantDatabaseProvisioner} 用 {@code ALTER USER} 维护；本类处理的是
 * 应用层面的登录账号（{@code sys_user}），两者不是一回事，改错一个都不会有人报错，
 * 只会表现为「密码改了但登不上」。</p>
 *
 * <p><b>幂等</b>：账号存在则覆盖口令/盐并重新启用，不存在则插入；角色绑定先查后插。
 * 因此可以反复调用（建租户、{@code PUT /v2/tenant/init-database}、重置口令都会走这里）。</p>
 *
 * <p><b>失败语义</b>：建库/建表已经成功、租户记录已经落库，同步管理员失败不能让建租户接口
 * 报错，只回结构化结果由调用方记 WARN；需要补做时重新调用即可。</p>
 *
 * @since 4.0.0.51
 * @author CH
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantDatabaseAdminSynchronizer {

    /**
     * 租户库管理员的角色编码（{@code db/init} 种子里 {@code sys_role_id=1} 的那一条）
     */
    private static final String SUPER_ADMIN_ROLE_CODE = "SUPER_ADMIN";

    /**
     * 租户库默认部门（{@code db/init} 种子里 {@code sys_dept_id=1 总部}）
     */
    private static final int DEFAULT_DEPT_ID = 1;

    /**
     * 盐值长度，与种子账号（{@code '123456'}）保持同一量级，避免两种长度的盐混用
     */
    private static final int SALT_LENGTH = 6;

    /**
     * 租户配置
     */
    private final TenantProperties tenantProperties;

    /**
     * 租户口令加解密（{@code sys_tenant_password} 落库是密文，同步前必须还原明文）
     */
    private final TenantPasswordCipher tenantPasswordCipher;

    /**
     * 租户库连接信息解析器（自填字段 / 数据源台账 / 平台自身实例）
     */
    private final TenantDataSourceResolver tenantDataSourceResolver;

    /**
     * 是否具备「同步租户库管理员」的前提条件。
     *
     * <p>只要求：开了按库路由总开关 + 租户记录里登记了库地址与账号口令。
     * <b>不要求</b> {@code auto-provision}：手工登记好库地址的存量租户同样要能同步管理员。</p>
     *
     * @param tenant 租户记录
     * @return 可以同步返回 true
     */
    public boolean isAvailable(SysTenant tenant) {
        if (tenant == null) {
            return false;
        }
        if (!tenantProperties.getServer().isDatasourceRoutingEnable()) {
            return false;
        }
        return separateDb(tenant) && hasText(tenant.getSysTenantUsername());
    }

    /**
     * 租户是否配置了「独立库」。
     *
     * <p>自填型看库名 + 地址；引用型（台账 / 平台自身实例）看库名 + 解析出的实例。</p>
     *
     * @param tenant 租户记录
     * @return 配置了独立库返回 true
     */
    private boolean separateDb(SysTenant tenant) {
        TenantDatasourceInfo info = tenantDataSourceResolver.resolve(tenant);
        return info != null && hasText(info.dbName());
    }

    /**
     * 同步租户库管理员（口令取库里落库的密文并还原）。
     *
     * @param tenant 租户记录
     * @return 同步结果
     */
    public Result sync(SysTenant tenant) {
        if (!isAvailable(tenant)) {
            return Result.skipped("租户未登记独立库或缺少租户账号，跳过租户库管理员同步");
        }
        String plainPassword = tenantPasswordCipher.decrypt(tenant.getSysTenantPassword());
        if (!hasText(plainPassword)) {
            return Result.skipped("租户口令为空，跳过租户库管理员同步");
        }
        return sync(tenant, plainPassword);
    }

    /**
     * 同步租户库管理员（显式给定明文口令）。
     *
     * <p>重置口令时用这个重载：此时库里存的还是旧密文（或本次已更新但调用方手里就有明文），
     * 直接拿明文同步最可靠，少一次解密。</p>
     *
     * @param tenant        租户记录
     * @param plainPassword 租户明文口令
     * @return 同步结果
     */
    public Result sync(SysTenant tenant, String plainPassword) {
        if (tenant == null) {
            return Result.failed("租户记录为空");
        }
        if (!tenantProperties.getServer().isDatasourceRoutingEnable()) {
            return Result.skipped("未开启按租户切库（datasource-routing-enable=false）");
        }
        TenantDatasourceInfo info = tenantDataSourceResolver.resolve(tenant);
        String jdbcUrl = TenantJdbcUrlBuilder.build(info, tenantProperties);
        if (jdbcUrl == null) {
            return Result.skipped("租户未登记库地址或库名，共享主库租户无需同步租户库管理员");
        }
        if (!hasText(tenant.getSysTenantUsername())) {
            return Result.skipped("租户账号为空，无法创建租户库管理员");
        }
        if (!hasText(plainPassword)) {
            return Result.skipped("租户口令为空，无法创建租户库管理员");
        }
        String plainSqlPassword = tenantPasswordCipher.decrypt(info.password());
        if (!hasText(plainSqlPassword)) {
            return Result.skipped("租户库口令为空，无法连接租户库同步管理员");
        }

        String username = tenant.getSysTenantUsername().trim();
        try {
            JdbcTemplate jdbc = new JdbcTemplate(buildDataSource(jdbcUrl,
                    info.username(), plainSqlPassword));
            List<java.util.Map<String, Object>> rows = jdbc.queryForList(
                    "SELECT sys_user_id, sys_user_salt FROM sys_user "
                            + "WHERE sys_user_username = ? AND sys_user_delete = 0", username);
            if (rows.isEmpty()) {
                int userId = insertAdmin(jdbc, tenant, username, plainPassword);
                bindSuperAdmin(jdbc, userId);
                log.info("[租户库管理员] 已创建: tenantId={}, username={}, userId={}",
                        tenant.getSysTenantId(), username, userId);
                return Result.created(userId, "租户库管理员已创建: " + username);
            }

            Object idValue = rows.get(0).get("sys_user_id");
            Object saltValue = rows.get(0).get("sys_user_salt");
            int userId = idValue instanceof Number number ? number.intValue() : Integer.parseInt(String.valueOf(idValue));
            String salt = saltValue == null || String.valueOf(saltValue).isBlank()
                    ? newSalt()
                    : String.valueOf(saltValue);
            jdbc.update("UPDATE sys_user SET sys_user_password = ?, sys_user_salt = ?, "
                            + "sys_user_status = 1, sys_user_delete = 0 WHERE sys_user_id = ?",
                    PasswordUtils.encrypt(plainPassword, salt), salt, userId);
            bindSuperAdmin(jdbc, userId);
            log.info("[租户库管理员] 已同步口令: tenantId={}, username={}, userId={}",
                    tenant.getSysTenantId(), username, userId);
            return Result.updated(userId, "租户库管理员口令已同步: " + username);
        } catch (Exception e) {
            // 库/表可能还没就绪（脚本异步通道未跑完）：只记日志，不让调用方 500
            log.warn("[租户库管理员] 同步失败 tenantId={}, username={}: {}",
                    tenant.getSysTenantId(), username, e.getMessage());
            return Result.failed("同步租户库管理员失败: " + e.getMessage());
        }
    }

    /**
     * 插入租户库管理员账号。
     *
     * @param jdbc        租户库 JdbcTemplate
     * @param tenant      租户记录
     * @param username    管理员账号
     * @param plainPassword 明文口令
     * @return 新账号主键
     */
    private int insertAdmin(JdbcTemplate jdbc, SysTenant tenant, String username, String plainPassword) {
        String salt = newSalt();
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Integer deptId = existingDeptId(jdbc);
        jdbc.update("INSERT INTO sys_user (sys_user_username, sys_user_nickname, sys_user_password, "
                        + "sys_user_salt, sys_user_delete, sys_user_status, sys_user_in_system, sys_dept_id, "
                        + "sys_user_phone, sys_user_email, sys_client_id, create_name, create_time, "
                        + "sys_user_must_change_password) "
                        + "VALUES (?, ?, ?, ?, 0, 1, 1, ?, ?, ?, NULL, ?, ?, 0)",
                username,
                hasText(tenant.getSysTenantName()) ? tenant.getSysTenantName() : username,
                PasswordUtils.encrypt(plainPassword, salt),
                salt,
                deptId,
                tenant.getSysTenantPhone(),
                tenant.getSysTenantEmail(),
                "tenant-provision",
                now);
        Integer id = jdbc.queryForObject(
                "SELECT sys_user_id FROM sys_user WHERE sys_user_username = ? AND sys_user_delete = 0",
                Integer.class, username);
        if (id == null) {
            throw new IllegalStateException("租户库管理员插入后未查到主键: " + username);
        }
        return id;
    }

    /**
     * 绑定超级管理员角色（角色种子缺失时只告警，不打断账号创建）。
     *
     * @param jdbc   租户库 JdbcTemplate
     * @param userId 账号主键
     */
    private void bindSuperAdmin(JdbcTemplate jdbc, int userId) {
        try {
            List<Integer> roleIds = jdbc.queryForList(
                    "SELECT sys_role_id FROM sys_role WHERE sys_role_code = ?",
                    Integer.class, SUPER_ADMIN_ROLE_CODE);
            if (roleIds.isEmpty()) {
                // 种子（initdata 通道）可能还没跑完：账号已经能登录，角色可以下次补
                log.warn("[租户库管理员] 租户库缺少 {} 角色种子，暂不绑定角色（可再次同步补做）",
                        SUPER_ADMIN_ROLE_CODE);
                return;
            }
            int roleId = roleIds.get(0);
            List<Integer> bound = jdbc.queryForList(
                    "SELECT sys_user_role_id FROM sys_user_role WHERE sys_user_id = ? AND sys_role_id = ?",
                    Integer.class, userId, roleId);
            if (!bound.isEmpty()) {
                return;
            }
            jdbc.update("INSERT INTO sys_user_role (sys_user_id, sys_role_id) VALUES (?, ?)", userId, roleId);
            log.info("[租户库管理员] 已绑定角色: userId={}, roleId={}", userId, roleId);
        } catch (Exception e) {
            log.warn("[租户库管理员] 绑定角色失败（不影响账号本身）: {}", e.getMessage());
        }
    }

    /**
     * 取种子里的默认部门，缺失时返回 {@code null}（部门非必填，硬写 1 会造出孤儿外键）。
     *
     * @param jdbc 租户库 JdbcTemplate
     * @return 部门ID
     */
    private Integer existingDeptId(JdbcTemplate jdbc) {
        try {
            List<Integer> deptIds = jdbc.queryForList(
                    "SELECT sys_dept_id FROM sys_dept WHERE sys_dept_id = ?",
                    Integer.class, DEFAULT_DEPT_ID);
            return deptIds.isEmpty() ? null : deptIds.get(0);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 生成口令盐值。
     *
     * @return 盐值
     */
    private String newSalt() {
        String uuid = IdUtils.createSimpleUuid();
        return uuid.length() <= SALT_LENGTH ? uuid : uuid.substring(0, SALT_LENGTH);
    }

    /**
     * 构造连到租户库的临时数据源。
     *
     * <p>用 {@link DriverManagerDataSource} 而不是连接池：同步是一次性动作，
     * 建池会长期占住连接。</p>
     *
     * @param jdbcUrl  租户库 URL
     * @param username 租户库账号
     * @param password 租户库明文口令
     * @return 数据源
     */
    private DataSource buildDataSource(String jdbcUrl, String username, String password) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        String driverClassName = tenantProperties.getServer().getDatasourceRoutingDriverClassName();
        if (hasText(driverClassName)) {
            dataSource.setDriverClassName(driverClassName);
        }
        return dataSource;
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

    /**
     * 同步结果。
     *
     * @param status  结果状态
     * @param userId  租户库里的账号主键（跳过/失败时为 0）
     * @param message 说明
     */
    public record Result(Status status, int userId, String message) {

        /**
         * 结果状态。
         */
        public enum Status {
            /**
             * 新建了账号
             */
            CREATED,
            /**
             * 账号已存在，口令已覆盖
             */
            UPDATED,
            /**
             * 前置条件不满足，主动跳过
             */
            SKIPPED,
            /**
             * 连接或 SQL 失败
             */
            FAILED
        }

        /**
         * 是否真的落到租户库（新建或更新）。
         *
         * @return 落库成功返回 true
         */
        public boolean isSynced() {
            return status == Status.CREATED || status == Status.UPDATED;
        }

        /**
         * 构造新建结果。
         *
         * @param userId  账号主键
         * @param message 说明
         * @return 结果
         */
        static Result created(int userId, String message) {
            return new Result(Status.CREATED, userId, message);
        }

        /**
         * 构造更新结果。
         *
         * @param userId  账号主键
         * @param message 说明
         * @return 结果
         */
        static Result updated(int userId, String message) {
            return new Result(Status.UPDATED, userId, message);
        }

        /**
         * 构造跳过结果。
         *
         * @param message 说明
         * @return 结果
         */
        static Result skipped(String message) {
            return new Result(Status.SKIPPED, 0, message);
        }

        /**
         * 构造失败结果。
         *
         * @param message 说明
         * @return 结果
         */
        static Result failed(String message) {
            return new Result(Status.FAILED, 0, message);
        }
    }
}
