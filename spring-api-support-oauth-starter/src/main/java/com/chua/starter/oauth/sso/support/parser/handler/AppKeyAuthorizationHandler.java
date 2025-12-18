package com.chua.starter.oauth.sso.support.parser.handler;

import com.chua.common.support.annotations.Spi;
import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.system.service.SysAppKeyService;
import com.chua.starter.entity.system.SysAppKey;
import com.chua.starter.oauth.sso.support.information.OauthAuthInformation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

/**
 * AppKey认证处理器
 * <p>
 * 类似大模型的认证方式，只验证AppKey合法性，返回关联用户信息
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/11
 */
@Slf4j
@Spi("appkey")
public class AppKeyAuthorizationHandler implements AuthorizationHandler {

    @Override
    public ReturnResult<String> authenticate(OauthAuthInformation authInformation, 
                                            String token, 
                                            String cookie, 
                                            String userCode) {
        log.info("使用AppKey认证方式");
        
        ApplicationContext applicationContext = authInformation.getApplicationContext();
        
        try {
            // userCode 即为 appKey
            String appKey = userCode;
            if (StringUtils.isBlank(appKey)) {
                return ReturnResult.error("AppKey不能为空");
            }
            
            log.info("AppKey认证开始: appKey={}", maskAppKey(appKey));

            // 查询AppKey是否有效
            SysAppKeyService appKeyService = applicationContext.getBean(SysAppKeyService.class);
            SysAppKey dbAppKey = appKeyService.getByAppKey(appKey);
            if (dbAppKey == null) {
                log.warn("AppKey不存在: appKey={}", maskAppKey(appKey));
                return ReturnResult.error("AppKey不存在或无效");
            }

            // 检查状态
            if (dbAppKey.getSysAppKeyStatus() != null && dbAppKey.getSysAppKeyStatus() != 0) {
                log.warn("AppKey已禁用: appKey={}", maskAppKey(appKey));
                return ReturnResult.error("AppKey已禁用");
            }

            // 检查过期时间
            if (dbAppKey.getSysAppKeyExpireTime() != null) {
                long currentTime = System.currentTimeMillis() / 1000;
                if (currentTime > dbAppKey.getSysAppKeyExpireTime()) {
                    log.warn("AppKey已过期: appKey={}", maskAppKey(appKey));
                    return ReturnResult.error("AppKey已过期");
                }
            }

            // 构建用户信息（只返回关联用户信息，不创建token）
            UserResume userResume = new UserResume();
            userResume.setUserId(String.valueOf(dbAppKey.getSysUserId()));
            userResume.setUsername(dbAppKey.getSysAppKeyName());
            userResume.setAppId(dbAppKey.getSysAppKeyAppId());
            
            // 更新使用统计
            try {
                appKeyService.updateUseInfo(dbAppKey.getSysAppKeyAppId());
            } catch (Exception e) {
                log.warn("更新AppKey使用信息失败", e);
            }
            
            String json = Json.toJson(userResume);
            log.info("AppKey认证成功: appKey={}, userId={}", maskAppKey(appKey), dbAppKey.getSysUserId());
            return ReturnResult.ok(authInformation.encodeHex(json));
            
        } catch (Exception e) {
            log.error("AppKey认证失败", e);
            return ReturnResult.error("认证失败: " + e.getMessage());
        }
    }
    
    /**
     * 脱敏AppKey
     */
    private String maskAppKey(String appKey) {
        if (appKey == null || appKey.length() < 8) {
            return "***";
        }
        return appKey.substring(0, 4) + "..." + appKey.substring(appKey.length() - 4);
    }

    @Override
    public boolean supports(String subProtocol, String token, String userCode) {
        // AppKey认证：有userCode参数
        return StringUtils.isNotBlank(userCode);
    }
}
