package com.chua.starter.tenant.server.support;

/**
 * 租户服务端地址解析结果。
 *
 * <p>同一个租户可能同时存在两类地址，按可信度排序返回：</p>
 * <ol>
 *   <li>{@code sync}——租户端进程通过同步通道注册上来的地址，说明该实例此刻真的活着；</li>
 *   <li>{@code config}——{@code sys_tenant_home_url} 里人工登记的地址，租户端未连上来时用它兜底。</li>
 * </ol>
 *
 * @param tenantId        租户标识
 * @param url             可访问的基地址，已去掉末尾斜杠（拼接路径时直接 {@code url + "/v2/xxx"}）
 * @param source          地址来源：{@code sync} / {@code config}
 * @param online          是否实时在线。{@code sync} 来源是注册表里的真实在线状态；{@code config}
 *                        来源恒为 {@code false}——地址是人工填的，没有经过连接验证
 * @param clientId        同步会话标识；{@code config} 来源为 {@code null}
 * @param applicationName 租户端应用名；{@code config} 来源为 {@code null}
 * @since 4.0.0.52
 * @author CH
 */
public record TenantEndpoint(String tenantId,
                             String url,
                             String source,
                             boolean online,
                             String clientId,
                             String applicationName) {
}
