package com.chua.starter.tenant.server.notify.impl;

import com.chua.starter.tenant.entity.SysTenant;
import com.chua.starter.tenant.entity.SysTenantService;
import com.chua.starter.tenant.server.notify.TenantNotifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 默认租户通知服务实现
 * <p>
 * 默认实现仅记录日志，实际项目中可以扩展实现邮件、短信、站内信等通知方式
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/06
 */
@Slf4j
@Service
public class DefaultTenantNotifyService implements TenantNotifyService {

    @Override
    public void notifyExpiringSoon(SysTenant tenant, List<SysTenantService> services, int daysUntilExpire) {
        log.info("[租户通知] 租户 {} ({}) 有 {} 个服务将在 {} 天后到期",
                tenant.getSysTenantName(),
                tenant.getSysTenantCode(),
                services.size(),
                daysUntilExpire);

        // 如果租户有邮箱，可以发送邮件通知
        if (tenant.getSysTenantEmail() != null && !tenant.getSysTenantEmail().isEmpty()) {
            sendEmail(
                    tenant.getSysTenantEmail(),
                    "服务即将到期提醒",
                    buildExpiringSoonEmailContent(tenant, services, daysUntilExpire)
            );
        }

        // 如果租户有手机号，可以发送短信通知
        if (tenant.getSysTenantPhone() != null && !tenant.getSysTenantPhone().isEmpty()) {
            sendSms(
                    tenant.getSysTenantPhone(),
                    String.format("【服务提醒】您的 %d 个服务将在 %d 天后到期，请及时续费。",
                            services.size(), daysUntilExpire)
            );
        }
    }

    @Override
    public void notifyExpired(SysTenant tenant, List<SysTenantService> services, int daysExpired) {
        log.warn("[租户通知] 租户 {} ({}) 有 {} 个服务已到期 {} 天",
                tenant.getSysTenantName(),
                tenant.getSysTenantCode(),
                services.size(),
                daysExpired);

        if (tenant.getSysTenantEmail() != null && !tenant.getSysTenantEmail().isEmpty()) {
            sendEmail(
                    tenant.getSysTenantEmail(),
                    "服务已到期通知",
                    buildExpiredEmailContent(tenant, services, daysExpired)
            );
        }

        if (tenant.getSysTenantPhone() != null && !tenant.getSysTenantPhone().isEmpty()) {
            sendSms(
                    tenant.getSysTenantPhone(),
                    String.format("【服务提醒】您的 %d 个服务已到期 %d 天，请尽快续费以免影响使用。",
                            services.size(), daysExpired)
            );
        }
    }

    @Override
    public void notifyServiceDisabled(SysTenant tenant, List<SysTenantService> services) {
        log.warn("[租户通知] 租户 {} ({}) 的 {} 个服务已被禁用",
                tenant.getSysTenantName(),
                tenant.getSysTenantCode(),
                services.size());

        if (tenant.getSysTenantEmail() != null && !tenant.getSysTenantEmail().isEmpty()) {
            sendEmail(
                    tenant.getSysTenantEmail(),
                    "服务已禁用通知",
                    buildServiceDisabledEmailContent(tenant, services)
            );
        }
    }

    @Override
    public void notifyAdminAccountCreated(SysTenant tenant) {
        log.info("[租户通知] 租户 {} ({}) 管理员账号已创建",
                tenant.getSysTenantName(),
                tenant.getSysTenantCode());

        if (tenant.getSysTenantEmail() != null && !tenant.getSysTenantEmail().isEmpty()) {
            sendEmail(
                    tenant.getSysTenantEmail(),
                    "管理员账号创建通知",
                    buildAdminAccountEmailContent(tenant)
            );
        }
    }

    @Override
    public void notifyPasswordReset(SysTenant tenant, String newPassword) {
        log.info("[租户通知] 租户 {} ({}) 密码已重置",
                tenant.getSysTenantName(),
                tenant.getSysTenantCode());

        if (tenant.getSysTenantEmail() != null && !tenant.getSysTenantEmail().isEmpty()) {
            sendEmail(
                    tenant.getSysTenantEmail(),
                    "密码重置通知",
                    buildPasswordResetEmailContent(tenant, newPassword)
            );
        }
    }

    /**
     * 发送邮件（子类可覆盖实现具体逻辑）
     *
     * @param to      收件人
     * @param subject 主题
     * @param content 内容
     */
    protected void sendEmail(String to, String subject, String content) {
        log.debug("[租户通知] 发送邮件: to={}, subject={}", to, subject);
        // 默认实现不发送邮件，子类可以注入邮件服务并实现
    }

    /**
     * 发送短信（子类可覆盖实现具体逻辑）
     *
     * @param phone   手机号
     * @param content 内容
     */
    protected void sendSms(String phone, String content) {
        log.debug("[租户通知] 发送短信: phone={}, content={}", phone, content);
        // 默认实现不发送短信，子类可以注入短信服务并实现
    }

    /**
     * 构建即将到期邮件内容
     */
    private String buildExpiringSoonEmailContent(SysTenant tenant, List<SysTenantService> services, int daysUntilExpire) {
        return String.format("""
                尊敬的 %s：
                
                您好！
                
                您的租户账号 %s 有 %d 个服务将在 %d 天后到期。
                
                请及时续费以确保服务不中断。
                
                如有疑问，请联系客服。
                """,
                tenant.getSysTenantName(),
                tenant.getSysTenantCode(),
                services.size(),
                daysUntilExpire);
    }

    /**
     * 构建已到期邮件内容
     */
    private String buildExpiredEmailContent(SysTenant tenant, List<SysTenantService> services, int daysExpired) {
        return String.format("""
                尊敬的 %s：
                
                您好！
                
                您的租户账号 %s 有 %d 个服务已到期 %d 天。
                
                请尽快续费以恢复服务。超过宽限期后，服务将被禁用。
                
                如有疑问，请联系客服。
                """,
                tenant.getSysTenantName(),
                tenant.getSysTenantCode(),
                services.size(),
                daysExpired);
    }

    /**
     * 构建服务禁用邮件内容
     */
    private String buildServiceDisabledEmailContent(SysTenant tenant, List<SysTenantService> services) {
        return String.format("""
                尊敬的 %s：
                
                您好！
                
                由于服务到期未续费，您的租户账号 %s 的 %d 个服务已被禁用。
                
                如需恢复服务，请联系客服办理续费。
                """,
                tenant.getSysTenantName(),
                tenant.getSysTenantCode(),
                services.size());
    }

    /**
     * 构建管理员账号邮件内容
     */
    private String buildAdminAccountEmailContent(SysTenant tenant) {
        return String.format("""
                尊敬的 %s：
                
                您好！
                
                您的租户管理员账号已创建成功。
                
                账号：%s
                
                请使用上述账号登录系统。首次登录后，请及时修改密码。
                
                如有疑问，请联系客服。
                """,
                tenant.getSysTenantName(),
                tenant.getSysTenantUsername());
    }

    /**
     * 构建密码重置邮件内容
     */
    private String buildPasswordResetEmailContent(SysTenant tenant, String newPassword) {
        return String.format("""
                尊敬的 %s：
                
                您好！
                
                您的租户管理员账号密码已重置。
                
                账号：%s
                新密码：%s
                
                请使用新密码登录系统，登录后请及时修改密码。
                
                如非本人操作，请联系客服。
                """,
                tenant.getSysTenantName(),
                tenant.getSysTenantUsername(),
                newPassword);
    }
}
