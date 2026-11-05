// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.tenant.server.support;

import com.chua.common.support.lang.datasource.flyway.Flyway;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.starter.tenant.entity.SysTenant;
import com.chua.starter.tenant.properties.TenantProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 租户库业务表初始化器。
 *
 * <p>{@link TenantDatabaseProvisioner} 只负责 {@code CREATE DATABASE} + 建账号 + 授权，
 * 结果的库是<b>空的</b>：路由过去之后第一条业务 SQL 就会报表不存在。本类补上这一环 ——
 * 用租户自己的账号连上刚建好的库，按仓库既有约定执行 {@code db/init} 下的迁移脚本。</p>
 *
 * <p><b>为什么不另写一套建表逻辑</b>：仓库里的建表脚本本来就已经统一收敛在
 * {@code classpath*:db/init/V{版本}__init_all_{模块}.sql}，并由 {@code Flyway} SPI
 * （{@code utils-support-flyway-starter} 的 {@code DataSourceFlyway}）执行、把版本记录写进
 * {@code sys_database_version}。租户库与平台主库的<b>表结构本来就该是同一套</b>，
 * 另起炉灶只会在以后每次改表时漏掉一边。这里直接复用同一个 SPI 与同一批脚本，
 * 得到三个直接好处：</p>
 *
 * <ul>
 *   <li><b>幂等</b>：{@code sys_database_version} 是租户库自己的，重复调用不会重复执行；</li>
 *   <li><b>增量</b>：以后新增的 {@code V1.1__alter_xxx.sql} 对存量租户库同样生效（手动重跑一次即可）；</li>
 *   <li><b>可排查</b>：失败脚本在租户库里记为 {@code success=false}，下一次调用会重试。</li>
 * </ul>
 *
 * <p><b>表结构 vs 种子数据</b>：结构脚本（{@code __init_}）默认执行；
 * 种子脚本（{@code __initdata_}）默认<b>也执行</b> —— 没有 {@code sys_role}/{@code sys_user}
 * 这类基础数据的租户库是连登录都做不到的，比"空库"更难排查。两者都可以在配置里关掉。</p>
 *
 * <p><b>失败语义</b>：建库已经成功、租户记录已经落库，脚本失败不能让建租户接口 500，
 * 因此本类只返回结构化结果、不抛异常，由调用方决定是 WARN 还是回给运维。
 * 需要补做时调 {@code PUT /v2/tenant/init-database} 重跑即可。</p>
 *
 * @since 4.0.0.51
 * @author CH
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantDatabaseInitializer {

    /**
     * Flyway 脚本版本与描述之间的分隔符，与仓库脚本命名（{@code V1.0__init_...}）一致
     */
    private static final String FLYWAY_SEPARATOR = "__";

    /**
     * 租户配置
     */
    private final TenantProperties tenantProperties;
    /**
     * 租户数据库口令加解密
     */
    private final TenantPasswordCipher tenantPasswordCipher;

    /**
     * 用落库口令初始化租户库。
     *
     * @param tenant 租户记录（{@code sqlPassword} 为库中密文）
     * @return 初始化结果
     */
    public InitResult initialize(SysTenant tenant) {
        return initialize(tenant, null);
    }

    /**
     * 初始化租户库。
     *
     * @param tenant           租户记录
     * @param plainSqlPassword 明文库口令；为 {@code null} 时从租户记录解密还原
     * @return 初始化结果
     */
    public InitResult initialize(SysTenant tenant, String plainSqlPassword) {
        TenantProperties.ServerConfig server = tenantProperties.getServer();
        if (!server.isDatasourceRoutingAutoInitScripts()) {
            log.info("[租户建表] datasource-routing-auto-init-scripts=false，跳过租户库业务表初始化");
            return InitResult.skipped("已关闭自动初始化脚本（datasource-routing-auto-init-scripts）");
        }
        if (tenant == null) {
            return InitResult.failed("租户记录为空");
        }
        String tenantId = tenant.getSysTenantId();
        String jdbcUrl = TenantJdbcUrlBuilder.build(tenant, tenantProperties);
        if (jdbcUrl == null) {
            log.debug("[租户建表] 租户 {} 未登记库地址或库名，跳过初始化", tenantId);
            return InitResult.failed("租户未登记库地址或库名，无法初始化业务表");
        }
        if (!hasText(tenant.getSysTenantSqlUsername())) {
            log.warn("[租户建表] 租户 {} 未登记数据库账号，跳过初始化", tenantId);
            return InitResult.failed("租户未登记数据库账号，无法初始化业务表");
        }
        String password = hasText(plainSqlPassword)
                ? plainSqlPassword
                : tenantPasswordCipher.decrypt(tenant.getSysTenantSqlPassword());
        if (!hasText(password)) {
            log.warn("[租户建表] 租户 {} 未登记数据库口令，跳过初始化", tenantId);
            return InitResult.failed("租户未登记数据库口令，无法初始化业务表");
        }

        List<String> locations = resolveLocations();
        if (locations.isEmpty()) {
            return InitResult.failed("未配置初始化脚本位置（datasource-routing-init-script-locations）");
        }

        DataSource dataSource = buildDataSource(jdbcUrl, tenant.getSysTenantSqlUsername(), password);
        String protocol = resolveProtocol(jdbcUrl);
        boolean withData = server.isDatasourceRoutingInitDataEnable();
        try {
            List<Flyway> extensions = ServiceProvider.of(Flyway.class)
                    .getNewExtensions(Flyway.SPI_NAME, dataSource);
            if (extensions.isEmpty()) {
                log.error("[租户建表] 未找到 Flyway SPI 实现，租户库业务表无法初始化。"
                        + "请确认依赖 utils-support-flyway-starter（spring-support-datasource-starter 已传递引入）");
                return InitResult.failed("未找到 Flyway SPI 实现，无法初始化业务表");
            }
            int schemaScripts = 0;
            int dataScripts = 0;
            boolean executed = false;
            Set<String> failedStatements = new LinkedHashSet<>();
            for (Flyway flyway : extensions) {
                if (flyway == null) {
                    // 构造参数不匹配时 SPI 会产生 null 实例（例如回落到只接受 Engine 的默认实现）
                    continue;
                }
                for (String location : locations) {
                    flyway.location(location);
                }
                flyway.separator(FLYWAY_SEPARATOR);
                if (protocol != null) {
                    flyway.protocol(protocol);
                }
                try {
                    schemaScripts = flyway.migrateInit();
                    if (withData) {
                        dataScripts = flyway.migrateInitData();
                    }
                } finally {
                    failedStatements.addAll(flyway.getFailedStatements());
                }
                executed = true;
                log.info("[租户建表] 租户 {} 初始化完成: 结构脚本 {} 个, 数据脚本 {} 个, 脚本位置 {}",
                        tenantId, schemaScripts, dataScripts, locations);
                break;
            }
            if (!executed) {
                // 不能因为「SPI 返回了非空列表」就认为跑过：列表里可能全是构造失败的 null
                log.error("[租户建表] 未取得可用的 Flyway 实例（SPI 实现构造失败），租户库 {} 未初始化", tenantId);
                return InitResult.failed("未取得可用的 Flyway 实例，无法初始化业务表");
            }
            if (!failedStatements.isEmpty()) {
                // 与平台主库同语义：单条方言差异语句失败不中断整体，但必须让人看得见，
                // 否则「脚本跑完了」的日志会掩盖掉真正没建出来的那张表
                log.warn("[租户建表] 租户 {} 有 {} 条语句被容错跳过（可在租户库 sys_database_version 查 success=false）：",
                        tenantId, failedStatements.size());
                int printed = 0;
                for (String statement : failedStatements) {
                    log.warn("[租户建表]   跳过语句: {}", statement);
                    if (++printed >= 10) {
                        log.warn("[租户建表]   其余 {} 条略", failedStatements.size() - printed);
                        break;
                    }
                }
            }
            return InitResult.ok(schemaScripts, dataScripts);
        } catch (Exception e) {
            // 建库已成功、租户已落库：初始化失败不能变成建租户接口 500
            log.error("[租户建表] 租户 {} 初始化业务表失败: {}", tenantId, e.getMessage(), e);
            return InitResult.failed("初始化业务表失败: " + e.getMessage());
        }
    }

    /**
     * 解析初始化脚本位置列表。
     *
     * <p>支持英文逗号、分号、换行分隔，去空白去重；默认值与平台自身的
     * {@code plugin.datasource.script.script-path} 保持一致，保证租户库与主库同源。</p>
     *
     * @return 脚本位置列表
     */
    List<String> resolveLocations() {
        String configured = tenantProperties.getServer().getDatasourceRoutingInitScriptLocations();
        if (!hasText(configured)) {
            return List.of();
        }
        Set<String> locations = new LinkedHashSet<>();
        for (String part : configured.split("[,;\\n]")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                locations.add(trimmed);
            }
        }
        return new ArrayList<>(locations);
    }

    /**
     * 构造连到租户库的临时数据源。
     *
     * <p>用 {@link DriverManagerDataSource} 而不是 Hikari：这是一次性迁移，
     * 建池反而会长期占住连接；迁移结束对象即被回收。</p>
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
     * 从 JDBC URL 推断 {@code ScriptConverter} 协议名。
     *
     * <p>不取连接元数据是为了在「连都没连上」时也能给出正确的转换协议，
     * 且少一次连接往返。识别不出时返回 {@code null}，脚本按原样执行。</p>
     *
     * @param jdbcUrl JDBC URL
     * @return 协议名；无法识别时返回 {@code null}
     */
    private String resolveProtocol(String jdbcUrl) {
        if (jdbcUrl == null) {
            return null;
        }
        String url = jdbcUrl.toLowerCase(Locale.ROOT);
        if (url.contains("mariadb:")) {
            return "mariadb";
        }
        if (url.contains("mysql:")) {
            return "mysql";
        }
        if (url.contains("h2:")) {
            return "h2";
        }
        if (url.contains("postgresql:")) {
            return "postgresql";
        }
        if (url.contains("sqlite:")) {
            return "sqlite";
        }
        if (url.contains("sqlserver:") || url.contains("mssql:")) {
            return "sqlserver";
        }
        if (url.contains("oracle:")) {
            return "oracle";
        }
        return null;
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
     * 租户库初始化结果。
     *
     * @param status        结果状态
     * @param schemaScripts 本次执行的结构脚本数
     * @param dataScripts   本次执行的数据脚本数
     * @param message       说明（失败/跳过原因）
     */
    public record InitResult(Status status, int schemaScripts, int dataScripts, String message) {

        /**
         * 结果状态。
         */
        public enum Status {
            /**
             * 初始化完成
             */
            SUCCESS,
            /**
             * 配置开关关闭等原因主动跳过
             */
            SKIPPED,
            /**
             * 前置条件缺失或执行失败
             */
            FAILED
        }

        /**
         * 是否成功。
         *
         * @return 成功返回 true
         */
        public boolean isSuccess() {
            return status == Status.SUCCESS;
        }

        /**
         * 构造成功结果。
         *
         * @param schemaScripts 结构脚本数
         * @param dataScripts   数据脚本数
         * @return 成功结果
         */
        static InitResult ok(int schemaScripts, int dataScripts) {
            return new InitResult(Status.SUCCESS, schemaScripts, dataScripts,
                    "结构脚本 " + schemaScripts + " 个, 数据脚本 " + dataScripts + " 个");
        }

        /**
         * 构造跳过结果。
         *
         * @param reason 跳过原因
         * @return 跳过结果
         */
        static InitResult skipped(String reason) {
            return new InitResult(Status.SKIPPED, 0, 0, reason);
        }

        /**
         * 构造失败结果。
         *
         * @param reason 失败原因
         * @return 失败结果
         */
        static InitResult failed(String reason) {
            return new InitResult(Status.FAILED, 0, 0, reason);
        }
    }
}