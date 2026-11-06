package com.chua.starter.system.support;

import com.chua.starter.datasource.support.DataSourceContextSupport;

/**
 * 缓存键的租户作用域。
 *
 * <p>同一个应用可能同时服务平台库和多个租户库，而 Redis 是共享的。凡是缓存键里带
 * 自增主键（如 {@code #userId}）或干脆是常量（如 {@code 'cache:role'}）的地方，
 * 平台与租户之间、租户与租户之间都会互相命中：租户库里 {@code sys_user_id=4}
 * 与另一个租户库里的 4 是两个人，却共用一个缓存条目，读出来是别人库里的数据。</p>
 *
 * <p>缓存键拼上当前数据源名即可把作用域隔开：平台请求上下文为空，统一记
 * {@code master}，租户请求是 {@code tenant-<id>}。缓存失效注解也要用同一个表达式，
 * 否则写入的是带作用域的键、删的是不带作用域的键，改了数据还会读到旧值。</p>
 *
 * @since 4.0.0.51
 * @author CH
 */
public final class TenantCacheScope {

    /**
     * 平台主库的作用域名
     */
    private static final String PLATFORM_SCOPE = "master";

    private TenantCacheScope() {
    }

    /**
     * 当前缓存作用域。
     *
     * @return 当前数据源名；平台主库或无上下文时返回 {@code master}
     */
    public static String scope() {
        String dbType = DataSourceContextSupport.getDbType();
        return (dbType == null || dbType.isBlank()) ? PLATFORM_SCOPE : dbType;
    }
}
