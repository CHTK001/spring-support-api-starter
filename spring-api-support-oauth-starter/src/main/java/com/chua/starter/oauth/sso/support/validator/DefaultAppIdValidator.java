package com.chua.starter.oauth.sso.support.validator;

import com.chua.common.support.spi.annotations.SpiDefault;
import com.chua.common.support.objects.annotation.AutoInject;
import com.chua.starter.entity.system.SysAppKey;
import com.chua.starter.entity.system.SysAppId;
import com.chua.starter.system.service.SysAppIdService;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认 appid 验证器
 * <p>
 * 默认实现：从app_标识表查询appid并比较是否匹配
 * </p>
 *
 * @版本 1.0.0
 * @since 2025-01-05
 * @author CH
 */
@Slf4j
@SpiDefault
public class DefaultAppIdValidator implements AppIdValidator {

    /**
     * appid 服务
     */
    @AutoInject
    /** SYSAPPID服务 */
    private SysAppIdService sysAppIdService;

    /**
     * 验证 appid, 从 app_标识 表查询并校验状态与过期时间。
     *
     * @param processedAppId 客户端算法处理后的 appid
     * @param appKey         数据库中的 app键 信息
     * @return 验证结果
     */
    @Override
    public ValidationResult validate(String processedAppId, SysAppKey appKey) {
        if (appKey == null) {
            return ValidationResult.failure("AppKey 不存在");
        }

 // 从app_标识表查询appid
        SysAppId sysAppId = sysAppIdService.getByAppIdValue(processedAppId);
        if (sysAppId == null) {
            log.warn("AppId 不存在: processedAppId={}", maskAppId(processedAppId));
            return ValidationResult.failure("AppId 不存在");
        }

 // app键和appid是并列关系，不需要验证关联关系

 // 检查appid状态
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
     * 脱敏 appid
     * @param appId appId
     * @return maskAppId的结果
     */
    private String maskAppId(String appId) {
        if (appId == null || appId.length() < 8) {
            return "***";
        }
        return appId.substring(0, 4) + "..." + appId.substring(appId.length() - 4);
    }
}

