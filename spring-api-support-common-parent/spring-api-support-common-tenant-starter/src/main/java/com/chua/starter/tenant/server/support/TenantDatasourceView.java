package com.chua.starter.tenant.server.support;

/**
 * 租户库连接信息摘要（下发前端用的只读投影）。
 *
 * <p>前端只需要知道「这个租户现在连的是哪个实例的哪个库」，用于在租户表单里只读回显。
 * 口令永不进来，地址/账号来自实例侧，避免页面把平台内部地址当作可编辑项暴露出去。</p>
 *
 * <p><b>{@code available=false} 也要下发</b>：租户明明选了实例、但台账记录被删/停用、
 * 或漏填库名时，若这里直接返回 {@code null}，页面只能显示「未登记」，
 * 运维看不出是「没配」还是「配错了」。带上 {@code problem} 才能一眼定位。</p>
 *
 * @param origin        来源：{@code TENANT_FIELDS} / {@code DATASOURCE_HUB} / {@code PLATFORM_SELF}；解析失败时为空
 * @param originLabel   来源中文名，前端直接展示
 * @param host          实例地址
 * @param port          实例端口，可能为空
 * @param dbName        库名
 * @param username      连接账号
 * @param datasourceName 引用的台账数据源名称；非台账来源时为空
 * @param available     连接信息是否可用（可用才能真的建池路由）
 * @param problem       不可用原因；可用时为 {@code null}
 * @since 4.0.0.52
 * @author CH
 */
public record TenantDatasourceView(String origin,
                                   String originLabel,
                                   String host,
                                   Integer port,
                                   String dbName,
                                   String username,
                                   String datasourceName,
                                   boolean available,
                                   String problem) {
}
