package com.chua.starter.tenant.server.support;

import com.chua.starter.datasource.support.DataSourceContextSupport;
import com.chua.starter.tenant.entity.SysTenant;
import com.chua.starter.tenant.properties.TenantProperties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 租户数据源注册器。
 *
 * <p>把 {@code sys_tenant} 中登记的租户独立库地址（{@code sys_tenant_host/port/ip/dbname/
 * sql_username/sql_password}）转换成 {@link HikariDataSource}，并注册到
 * {@link DataSourceContextSupport} 供 {@code DynamicDataSource} 按名路由。</p>
 *
 * <p><b>口令来源</b>：{@code sys_tenant_password_sql} 落库是 {@code ENC:} 前缀密文，
 * 这里经 {@link TenantPasswordCipher#decrypt(String)} 还原成明文再交给连接池；
 * 存量明文没有前缀，解密器原样返回，因此不需要数据迁移。</p>
 *
 * <p><b>为什么不用 {@code @DS} 注解</b>：注解只能在编译期写死数据源名，租户是运行期新增的，
 * 必须在请求到达时按 header 动态切换，故走 {@link DataSourceContextSupport#setDbType(String)}。</p>
 *
 * <p><b>失败语义</b>：任何一步失败（地址缺失、连不上、驱动不匹配）都只记录告警并返回
 * {@code null}，由调用方回退主库——绝不能因为某个租户库不可用而让平台整体 500。</p>
 *
 * <p><b>失败退避</b>：建池会做一次连接探测，租户库宕机时这一步要等满
 * {@code connection-timeout}。若不记住失败，租户库长时间不可用期间每个请求都会再等一次，
 * 请求线程被逐个拖住。故失败后进入冷却期，冷却期内直接返回 {@code null} 走主库，
 * 冷却结束才重新尝试。</p>
 *
 * <p><b>并发</b>：按租户维度串行建池（{@code tenantLocks}），而不是全表锁——
 * 某个租户库慢不能阻塞其它租户的请求。</p>
 *
 * @since 4.0.0.51
 * @author CH
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantDataSourceRegistry implements DisposableBean {

    /**
     * 已注册的租户数据源：租户标识 → 连接池
     */
    private final Map<String, HikariDataSource> registered = new ConcurrentHashMap<>();

    /**
     * 建池失败冷却期截止时间：租户标识 → 时间戳（毫秒）
     */
    private final Map<String, Long> failureCooldownUntil = new ConcurrentHashMap<>();

    /**
     * 按租户的建池锁：租户标识 → 锁对象
     */
    private final Map<String, Object> tenantLocks = new ConcurrentHashMap<>();

    /**
     * 租户配置
     */
    private final TenantProperties tenantProperties;

    /**
     * 租户数据库口令加解密
     */
    private final TenantPasswordCipher tenantPasswordCipher;

    /**
     * 获取（必要时创建）租户数据源。
     *
     * @param tenant 租户记录，必须带 {@code sysTenantId}
     * @return 租户数据源；租户未登记库地址、建池失败或处于失败冷却期时返回 {@code null}
     */
    public DataSource getOrCreate(SysTenant tenant) {
        if (tenant == null || !hasText(tenant.getSysTenantId())) {
            return null;
        }
        String tenantId = tenant.getSysTenantId();
        HikariDataSource existing = registered.get(tenantId);
        if (existing != null && !existing.isClosed()) {
            return existing;
        }

        String jdbcUrl = TenantJdbcUrlBuilder.build(tenant, tenantProperties);
        if (jdbcUrl == null) {
            log.debug("[租户数据源] 租户 {} 未登记库地址，跳过按库路由", tenantId);
            return null;
        }
        if (inCooldown(tenantId)) {
            log.debug("[租户数据源] 租户 {} 处于失败冷却期，本轮走主库", tenantId);
            return null;
        }

        Object lock = tenantLocks.computeIfAbsent(tenantId, key -> new Object());
        synchronized (lock) {
            HikariDataSource current = registered.get(tenantId);
            if (current != null && !current.isClosed()) {
                return current;
            }
            // 双检：等锁期间别的线程可能已失败并进入冷却期
            if (inCooldown(tenantId)) {
                return null;
            }
            HikariDataSource created = createDataSource(tenantId, jdbcUrl, tenant);
            if (created == null) {
                markFailure(tenantId);
                return null;
            }
            registered.put(tenantId, created);
            DataSourceContextSupport.addDatasource(dataSourceName(tenantId), created);
            failureCooldownUntil.remove(tenantId);
            log.info("[租户数据源] 已注册: tenantId={}, name={}, url={}",
                    tenantId, dataSourceName(tenantId), TenantJdbcUrlBuilder.mask(jdbcUrl));
            return created;
        }
    }

    /**
     * 按租户标识查找已注册的数据源名。
     *
     * @param tenantId 租户标识
     * @return 已注册的数据源名；未注册时返回 {@code null}
     */
    public String findRegisteredName(String tenantId) {
        if (!hasText(tenantId)) {
            return null;
        }
        HikariDataSource dataSource = registered.get(tenantId);
        if (dataSource == null || dataSource.isClosed()) {
            return null;
        }
        return dataSourceName(tenantId);
    }

    /**
     * 租户库地址变更、口令变更或租户删除时释放连接池。
     *
     * @param tenantId 租户标识
     */
    public void evict(String tenantId) {
        if (!hasText(tenantId)) {
            return;
        }
        // 先摘路由再关池：顺序反了会出现「池已关但路由还在」，
        // 窗口期内到达的请求会拿到 ClosedConnection，而不是回退主库
        DataSourceContextSupport.DATA_SOURCE_MAP.remove(dataSourceName(tenantId));
        failureCooldownUntil.remove(tenantId);
        HikariDataSource removed = registered.remove(tenantId);
        if (removed != null) {
            closeQuietly(removed);
            log.info("[租户数据源] 已释放: tenantId={}", tenantId);
        }
    }

    /**
     * 应用关闭时释放所有租户连接池。
     */
    @Override
    public void destroy() {
        for (Map.Entry<String, HikariDataSource> entry : registered.entrySet()) {
            DataSourceContextSupport.DATA_SOURCE_MAP.remove(dataSourceName(entry.getKey()));
            closeQuietly(entry.getValue());
        }
        registered.clear();
        failureCooldownUntil.clear();
        log.info("[租户数据源] 已随应用关闭释放全部租户数据源");
    }

    /**
     * 构造数据源注册名。
     *
     * @param tenantId 租户标识
     * @return 注册名
     */
    public String dataSourceName(String tenantId) {
        return tenantProperties.getServer().getDatasourceRoutingPrefix() + tenantId;
    }

    /**
     * 创建租户连接池。
     *
     * @param tenantId 租户标识
     * @param jdbcUrl  JDBC URL
     * @param tenant   租户记录
     * @return 连接池；创建失败时返回 {@code null}
     */
    private HikariDataSource createDataSource(String tenantId, String jdbcUrl, SysTenant tenant) {
        TenantProperties.ServerConfig server = tenantProperties.getServer();
        if (!hasText(tenant.getSysTenantSqlUsername())) {
            log.warn("[租户数据源] 租户 {} 未配置数据库账号，跳过按库路由", tenantId);
            return null;
        }

        HikariConfig config = new HikariConfig();
        config.setPoolName("tenant-" + tenantId);
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(tenant.getSysTenantSqlUsername());
        config.setPassword(tenantPasswordCipher.decrypt(tenant.getSysTenantSqlPassword()));
        config.setMaximumPoolSize(Math.max(1, server.getDatasourceRoutingMaxPoolSize()));
        config.setMinimumIdle(Math.max(0, server.getDatasourceRoutingMinIdle()));
        config.setConnectionTimeout(Math.max(1000L, server.getDatasourceRoutingConnectionTimeoutMillis()));
        config.setAutoCommit(true);
        if (hasText(server.getDatasourceRoutingDriverClassName())) {
            config.setDriverClassName(server.getDatasourceRoutingDriverClassName());
        }

        try {
            HikariDataSource dataSource = new HikariDataSource(config);
            // 主动探测一次：地址写错时在这里就失败，而不是等到第一条业务 SQL
            // 在半开事务里报错，那种报错跟"代码 bug"看起来一模一样
            try (var ignored = dataSource.getConnection()) {
                return dataSource;
            } catch (Exception probeError) {
                closeQuietly(dataSource);
                log.warn("[租户数据源] 租户 {} 连接探测失败，回退主库: {}",
                        tenantId, probeError.getMessage());
                return null;
            }
        } catch (Exception e) {
            log.warn("[租户数据源] 租户 {} 建池失败，回退主库: {}", tenantId, e.getMessage());
            return null;
        }
    }

    /**
     * 是否处于失败冷却期。
     *
     * @param tenantId 租户标识
     * @return 冷却中返回 true
     */
    private boolean inCooldown(String tenantId) {
        Long until = failureCooldownUntil.get(tenantId);
        if (until == null) {
            return false;
        }
        if (System.currentTimeMillis() >= until) {
            failureCooldownUntil.remove(tenantId);
            return false;
        }
        return true;
    }

    /**
     * 记录一次建池失败，进入冷却期。
     *
     * @param tenantId 租户标识
     */
    private void markFailure(String tenantId) {
        long cooldownMillis = Math.max(1000L,
                tenantProperties.getServer().getDatasourceRoutingFailureCooldownMillis());
        failureCooldownUntil.put(tenantId, System.currentTimeMillis() + cooldownMillis);
        log.warn("[租户数据源] 租户 {} 建池失败，{} ms 内不再尝试，期间走主库", tenantId, cooldownMillis);
    }

    /**
     * 静默关闭连接池。
     *
     * @param dataSource 连接池
     */
    private void closeQuietly(HikariDataSource dataSource) {
        try {
            dataSource.close();
        } catch (Exception e) {
            log.debug("[租户数据源] 关闭连接池失败，已忽略: {}", e.getMessage());
        }
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
