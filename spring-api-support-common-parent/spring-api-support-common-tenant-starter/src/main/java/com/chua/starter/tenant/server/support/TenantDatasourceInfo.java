package com.chua.starter.tenant.server.support;

/**
 * 租户数据库连接信息。
 *
 * <p>把「连哪个库、用什么账号」从 {@code SysTenant} 里剥出来，是因为它有三个来源：
 * 租户记录自填字段、数据源中心台账（{@code ms_datasource}）、平台自身实例。
 * 下游的 URL 构造、建池、开通、初始化都只认这一个结构，避免三处各写一套判定
 * 而出现「校验通过、路由却连不上」的偏差。</p>
 *
 * <p><b>库名永远是租户自己的</b>：台账只提供实例级信息（地址/端口/账号/口令），
 * 库名由租户记录的 {@code sys_tenant_dbname} 决定。若允许从台账取库名，
 * 「一个实例承载多个租户」的场景会直接串库。</p>
 *
 * @param host     主机地址
 * @param port     端口；为空或非正数时由驱动默认端口兜底
 * @param dbName   库名
 * @param username 连接账号
 * @param password 连接口令（明文；调用方按需解密后再传入）
 * @param origin   来源，仅用于日志与排错
 * @since 4.0.0.52
 * @author CH
 */
public record TenantDatasourceInfo(String host,
                                   Integer port,
                                   String dbName,
                                   String username,
                                   String password,
                                   Origin origin) {

    /**
     * 连接信息来源
     */
    public enum Origin {

        /**
         * 租户记录自填的 host/port/username_sql/password_sql 字段（存量行为）
         */
        TENANT_FIELDS,

        /**
         * 数据源中心台账（{@code ms_datasource}）
         */
        DATASOURCE_HUB,

        /**
         * 平台自身实例（{@code datasource-routing-admin-url}）
         */
        PLATFORM_SELF
    }

    /**
     * 连接信息是否完整。
     *
     * <p>地址与库名齐全就算「已登记」——与 {@link TenantJdbcUrlBuilder#build}
     * 的判定口径一致；账号口令的缺失由建池阶段单独告警，不在这里吞掉。</p>
     *
     * @return 地址与库名都非空返回 true
     */
    public boolean complete() {
        return hasText(host) && hasText(dbName);
    }

    /**
     * 库名去掉首尾空白。
     *
     * <p>判定与使用同一份文本：{@code complete()} 用 {@code isBlank} 判空，若使用方拿到的仍是
     * 带空白的原值，拼 URL 时会拼出 {@code jdbc:mysql://h/db } 这类连不上的串。</p>
     *
     * @return 本次解析实际生效的库名
     */
    public String effectiveDbName() {
        return hasText(dbName) ? dbName.trim() : null;
    }

    /**
     * 是否包含文本。
     *
     * @param value 待判断值
     * @return 非空白返回 true
     */
    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
