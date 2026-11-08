package com.chua.starter.tenant.server.support;

/**
 * 租户库连接信息摘要（下发前端用的只读投影）。
 *
 * <p>前端只需要知道「这个租户现在连的是哪个实例的哪个库」，用于在租户表单里只读回显。
 * 口令永不进来，地址/账号来自实例侧，避免页面把平台内部地址当作可编辑项暴露出去。</p>
 *
 * @param origin        来源：{@code TENANT_FIELDS} / {@code DATASOURCE_HUB} / {@code PLATFORM_SELF}
 * @param originLabel   来源中文名，前端直接展示
 * @param host          实例地址
 * @param port          实例端口，可能为空
 * @param dbName        库名
 * @param username      连接账号
 * @param datasourceName 引用的台账数据源名称；非台账来源时为空
 * @since 4.0.0.52
 * @author CH
 */
public record TenantDatasourceView(String origin,
                                   String originLabel,
                                   String host,
                                   Integer port,
                                   String dbName,
                                   String username,
                                   String datasourceName) {
}
