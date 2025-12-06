package com.chua.starter.tenant.server.notify;

import com.chua.starter.tenant.entity.SysTenant;
import com.chua.starter.tenant.entity.SysTenantService;

import java.util.List;

/**
 * 租户通知服务接口
 * <p>
 * 用于发送租户相关的通知，包括服务到期提醒、服务禁用通知等
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/06
 */
public interface TenantNotifyService {

    /**
     * 发送服务即将到期通知
     *
     * @param tenant          租户信息
     * @param services        即将到期的服务列表
     * @param daysUntilExpire 距离到期的天数
     */
    void notifyExpiringSoon(SysTenant tenant, List<SysTenantService> services, int daysUntilExpire);

    /**
     * 发送服务已到期通知
     *
     * @param tenant      租户信息
     * @param services    已到期的服务列表
     * @param daysExpired 已过期天数
     */
    void notifyExpired(SysTenant tenant, List<SysTenantService> services, int daysExpired);

    /**
     * 发送服务已禁用通知
     *
     * @param tenant   租户信息
     * @param services 被禁用的服务列表
     */
    void notifyServiceDisabled(SysTenant tenant, List<SysTenantService> services);

    /**
     * 发送管理员账号信息通知
     *
     * @param tenant 租户信息
     */
    void notifyAdminAccountCreated(SysTenant tenant);

    /**
     * 发送密码重置通知
     *
     * @param tenant      租户信息
     * @param newPassword 新密码
     */
    void notifyPasswordReset(SysTenant tenant, String newPassword);
}
