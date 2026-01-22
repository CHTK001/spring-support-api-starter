package com.chua.starter.oauth.sso.support.parser.handler;

import com.chua.common.support.core.annotation.Spi;
import com.chua.common.support.text.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.core.spi.ServiceProvider;
import com.chua.common.support.core.utils.MapUtils;
import com.chua.common.support.core.utils.StringUtils;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
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

            // 查询AppKey是否有效（从数据库获取）
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


            // 指纹检测：仅记录日志，不做任何处理（指纹验证在客户端进行）
            // 指纹配置已从数据库迁移到配置文件，不再从数据库读取
            String fingerprint = authInformation.getRequestHeaderValue("x-oauth-fingerprint");
            if (StringUtils.isBlank(fingerprint)) {
                fingerprint = MapUtils.getString(authInformation.getBodyData(), "x-oauth-fingerprint");
            }
            if (StringUtils.isNotBlank(fingerprint)) {
                log.debug("客户端已提供指纹: appKey={}, fingerprint={}", maskAppKey(appKey), fingerprint);
            }

            // 更新使用统计
            try {
                // 更新AppKey使用信息（通过appKey值）
                appKeyService.updateUseInfoByAppKey(appKey);
            } catch (Exception e) {
                log.warn("更新AppKey使用信息失败", e);
            }
            
            // 参考默认实现，通过 UserInfoService 获取完整用户信息
            // appId认证必须要有code（用户唯一编码），否则返回用户信息不存在
            String code = MapUtils.getString(authInformation.getBodyData(), "code");
            if (StringUtils.isBlank(code)) {
                // 如果请求体中没有，尝试从请求头获取
                code = authInformation.getRequestHeaderValue("code");
            }
            if (StringUtils.isBlank(code)) {
                log.warn("AppId认证失败: code参数（用户唯一编码）不能为空, appKey={}", maskAppKey(appKey));
                return ReturnResult.error("用户信息不存在: code参数（用户唯一编码）不能为空");
            }
            
            // 创建 UserResult 对象，设置基本信息
            UserResult userResult = new UserResult();
            userResult.setUserId(String.valueOf(dbAppKey.getSysUserId()));
            userResult.setUsername(dbAppKey.getSysAppKeyName());
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
                log.warn("获取用户信息失败: userId={}", dbAppKey.getSysUserId());
                return ReturnResult.error("获取用户信息失败");
            }
            
            // 将 appKey 存储到 ext 中
            if (userResume.getExt() == null) {
                userResume.setExt(new java.util.HashMap<>());
            }
            userResume.getExt().put("appKey", appKey);
            
            String json = Json.toJson(userResume);
            log.info("AppKey认证成功: appKey={}, userId={}", 
                    maskAppKey(appKey), dbAppKey.getSysUserId());
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
