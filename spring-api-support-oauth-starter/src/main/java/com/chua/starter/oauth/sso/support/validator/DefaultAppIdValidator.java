package com.chua.starter.oauth.sso.support.validator;

import com.chua.common.support.annotations.SpiDefault;
import com.chua.common.support.objects.annotation.AutoInject;
import com.chua.starter.entity.system.SysAppKey;
import com.chua.starter.entity.system.SysAppId;
import com.chua.starter.system.service.SysAppIdService;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认 AppId 验证器
 * <p>
 * 默认实现：从app_id表查询appId并比较是否匹配
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2025-01-05
 */
@Slf4j
@SpiDefault
public class DefaultAppIdValidator implements AppIdValidator {

    @AutoInject
    private SysAppIdService sysAppIdService;

    @Override
    public ValidationResult validate(String processedAppId, SysAppKey appKey) {
        if (appKey == null) {
            return ValidationResult.failure("AppKey 不存在");
        }

        // 从app_id表查询appId
        SysAppId sysAppId = sysAppIdService.getByAppIdValue(processedAppId);
        if (sysAppId == null) {
            log.warn("AppId 不存在: processedAppId={}", maskAppId(processedAppId));
            return ValidationResult.failure("AppId 不存在");
        }

        // appKey和appId是并列关系，不需要验证关联关系

        // 检查appId状态
        if (sysAppId.getAppIdStatus() == null || sysAppId.getAppIdStatus() != 1) {
            log.warn("AppId 已禁用: processedAppId={}", maskAppId(processedAppId));
            return ValidationResult.failure("AppId 已禁用");
        }

        // 检查过期时间
        if (sysAppId.getAppIdExpireTime() != null) {
            long currentTime = System.currentTimeMillis();
            if (currentTime > sysAppId.getAppIdExpireTime()) {
                log.warn("AppId 已过期: processedAppId={}, expireTime={}", 
                        maskAppId(processedAppId), sysAppId.getAppIdExpireTime());
                return ValidationResult.failure("AppId 已过期");
            }
        }

        return ValidationResult.success();
    }

    /**
     * 脱敏 AppId
     */
    private String maskAppId(String appId) {
        if (appId == null || appId.length() < 8) {
            return "***";
        }
        return appId.substring(0, 4) + "..." + appId.substring(appId.length() - 4);
    }
}

