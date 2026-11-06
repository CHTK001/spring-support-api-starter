package com.chua.starter.system.login;

/**
 * 租户独立库登录支持。
 *
 * <p>平台主库只登记租户（{@code sys_tenant}），租户自己的 {@code sys_user} 在租户库里。
 * 登录时用户名没有命中平台主库，就需要先按用户名找到它所属的租户，把数据源切到该租户库，
 * 再在租户库里查用户、验口令。这三步都跨模块（租户库注册在 tenant 模块），
 * 因此系统模块只声明扩展点，由 tenant 模块提供实现。</p>
 *
 * <p><b>为什么不放在系统模块直接查 {@code sys_tenant}</b>：系统模块是更底层的公共模块，
 * 反过来依赖租户模块会形成循环依赖；而且没有引入租户模块的工程（单库部署）根本不需要
 * 这套能力，扩展点为空即老行为。</p>
 *
 * <p><b>调用约定</b>：{@link #switchTo(String)} 成功后会改当前线程的数据源上下文，
 * 调用方必须在使用完毕后调 {@link #clear()}，否则线程复用会把后续请求也带到租户库。</p>
 *
 * @since 4.0.0.51
 * @author CH
 */
public interface TenantLoginSupport {

    /**
     * 租户解析结果。
     *
     * @param tenantId  租户标识（内部主键，切库与令牌都用它）
     * @param tenantCode 租户编码（用户输入的可读短码，回显与日志用）
     * @param dedicated 是否独立库：{@code true} 表示用户在自己的库里，需要切库；
     *                  {@code false} 表示仍在平台共享库，账号就在主库
     * @param enabled   租户是否处于启用状态
     */
    record TenantLoginTarget(String tenantId, String tenantCode, boolean dedicated, boolean enabled) {
    }

    /**
     * 按登录账号在平台主库解析其所属租户。
     *
     * <p>只返回「登记了独立库」的租户标识：仍在平台共享库的租户，其用户本来就在主库，
     * 不需要切库。调用时数据源上下文必须是主库（登录入口尚未切库）。</p>
     *
     * @param username 登录账号
     * @return 租户标识；账号不属于任何独立库租户时返回 {@code null}
     */
    String resolveTenantIdByUsername(String username);

    /**
     * 按用户输入的租户编码在平台主库解析租户。
     *
     * <p>与 {@link #resolveTenantIdByUsername(String)} 的区别：那里由账号反推租户，
     * 这里由用户在登录页显式选择的编码定位租户，因此<b>共享库租户也会返回</b>——
     * 调用方据此决定是查主库还是切库，并且要能告诉用户「编码不存在」或「租户已停用」。</p>
     *
     * @param tenantCode 租户编码，大小写不敏感
     * @return 解析结果；编码不存在时返回 {@code null}
     */
    TenantLoginTarget resolveByCode(String tenantCode);

    /**
     * 把当前线程的数据源切到指定租户库。
     *
     * @param tenantId 租户标识
     * @return 成功切库返回 true；租户不存在、未登记独立库或租户库不可用时返回 false
     */
    boolean switchTo(String tenantId);

    /**
     * 清空数据源上下文，恢复主库。
     */
    void clear();
}
