package com.chua.starter.oauth.sso.support.parser.handler;

import com.chua.common.support.annotations.Spi;
import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.MapUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.system.service.SysAppIdService;
import com.chua.starter.entity.system.SysAppId;
import com.chua.starter.oauth.sso.support.information.OauthAuthInformation;
import com.chua.starter.oauth.sso.support.validator.AppIdValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

/**
 * AppId认证处理器
 * <p>
 * 独立处理AppId认证，验证AppId合法性并返回关联用户信息
 * AppId和AppKey是并列关系，各自独立处理
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2025/01/05
 */
@Slf4j
@Spi("appid")
public class AppIdAuthorizationHandler implements AuthorizationHandler {

    @Override
    public ReturnResult<String> authenticate(OauthAuthInformation authInformation, 
                                            String token, 
                                            String cookie, 
                                            String userCode) {
        log.info("使用AppId认证方式");
        
        ApplicationContext applicationContext = authInformation.getApplicationContext();
        
        try {
            // 从请求中提取 appId
            String appId = MapUtils.getString(authInformation.getBodyData(), "x-oauth-app-id");
            if (StringUtils.isBlank(appId)) {
                appId = authInformation.getRequestHeaderValue("x-oauth-app-id");
            }
            
            if (StringUtils.isBlank(appId)) {
                return ReturnResult.error("AppId不能为空");
            }
            
            log.info("AppId认证开始: appId={}", maskAppId(appId));

            // 查询AppId是否有效（从数据库获取）
            SysAppIdService appIdService = applicationContext.getBean(SysAppIdService.class);
            SysAppId dbAppId = appIdService.getByAppIdValue(appId);
            if (dbAppId == null) {
                log.warn("AppId不存在: appId={}", maskAppId(appId));
                return ReturnResult.error("AppId不存在或无效");
            }

            // 检查状态
            if (dbAppId.getAppIdStatus() == null || dbAppId.getAppIdStatus() != 1) {
                log.warn("AppId已禁用: appId={}", maskAppId(appId));
                return ReturnResult.error("AppId已禁用");
            }

            // 检查过期时间
            if (dbAppId.getAppIdExpireTime() != null) {
                long currentTime = System.currentTimeMillis();
                if (currentTime > dbAppId.getAppIdExpireTime()) {
                    log.warn("AppId已过期: appId={}", maskAppId(appId));
                    return ReturnResult.error("AppId已过期");
                }
            }

            // 验证 appId（使用默认验证器）
            // AppId 认证使用简单的验证方式，不需要签名验证（签名验证用于 AppKey+AppId 组合场景）
            AppIdValidator validator = ServiceProvider.of(AppIdValidator.class)
                    .getNewExtension("default");
            
            if (validator != null && validator.supports(appId)) {
                AppIdValidator.ValidationResult validationResult = validator.validate(appId, null);
                if (!validationResult.isValid()) {
                    log.warn("AppId验证失败: appId={}, reason={}", 
                            maskAppId(appId), validationResult.getMessage());
                    return ReturnResult.error("AppId验证失败: " + validationResult.getMessage());
                }
                log.debug("AppId验证成功: appId={}", maskAppId(appId));
            }

            // 指纹检测：仅记录日志，不做任何处理（指纹验证在客户端进行）
            String fingerprint = authInformation.getRequestHeaderValue("x-oauth-fingerprint");
            if (StringUtils.isBlank(fingerprint)) {
                fingerprint = MapUtils.getString(authInformation.getBodyData(), "x-oauth-fingerprint");
            }
            if (StringUtils.isNotBlank(fingerprint)) {
                log.debug("客户端已提供指纹: appId={}, fingerprint={}", maskAppId(appId), fingerprint);
            }

            // 更新使用统计
            try {
                appIdService.updateUseInfo(appId);
            } catch (Exception e) {
                log.warn("更新AppId使用信息失败", e);
            }
            
            // 获取用户信息
            // AppId认证必须要有code（用户唯一编码），否则返回用户信息不存在
            String code = MapUtils.getString(authInformation.getBodyData(), "code");
            if (StringUtils.isBlank(code)) {
                // 如果请求体中没有，尝试从请求头获取
                code = authInformation.getRequestHeaderValue("code");
            }
            if (StringUtils.isBlank(code)) {
                log.warn("AppId认证失败: code参数（用户唯一编码）不能为空, appId={}", maskAppId(appId));
                return ReturnResult.error("用户信息不存在: code参数（用户唯一编码）不能为空");
            }
            
            // 创建 UserResult 对象，设置基本信息
            UserResult userResult = new UserResult();
            userResult.setUserId(String.valueOf(dbAppId.getUserId()));
            userResult.setUsername(dbAppId.getAppIdName());
            // 使用 WEB 登录类型，通过 UserLoginService 获取完整用户信息（包括角色、权限、部门等）
            userResult.setLoginType("WEB");
            
            // 将 code 设置到 UserResult 的 ext 中，以便传递给用户信息获取逻辑
            if (userResult.getExt() == null) {
                userResult.setExt(new java.util.HashMap<>());
            }
            userResult.getExt().put("code", code);
            log.debug("从请求中获取到 code 参数: code={}", code);
            
            // 通过 LoginCheck 获取完整的用户信息（包括角色、权限、部门等）
            LoginCheck loginCheck = authInformation.getLoginCheck();
            UserResume userResume = loginCheck.getUserInfo(userResult);
            
            if (userResume == null || userResume.getUserId() == null) {
                log.warn("获取用户信息失败: userId={}", dbAppId.getUserId());
                return ReturnResult.error("获取用户信息失败");
            }
            
            // 设置 appId 信息
            userResume.setAppId(appId);
            
            String json = Json.toJson(userResume);
            log.info("AppId认证成功: appId={}, userId={}", 
                    maskAppId(appId), dbAppId.getUserId());
            return ReturnResult.ok(authInformation.encodeHex(json));
            
        } catch (Exception e) {
            log.error("AppId认证失败", e);
            return ReturnResult.error("认证失败: " + e.getMessage());
        }
    }
    
    /**
     * 脱敏AppId
     */
    private String maskAppId(String appId) {
        if (appId == null || appId.length() < 8) {
            return "***";
        }
        return appId.substring(0, 4) + "..." + appId.substring(appId.length() - 4);
    }

    @Override
    public boolean supports(String subProtocol, String token, String userCode) {
        // AppId认证：没有userCode（appKey），且没有token（避免与DefaultAuthorizationHandler冲突）
        // 注意：实际的appId验证在authenticate方法中进行
        return StringUtils.isBlank(userCode) && StringUtils.isBlank(token);
    }
}

