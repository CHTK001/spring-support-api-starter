package com.chua.starter.oauth.sso.support.parser;

import com.chua.common.support.crypto.digest.HMac;
import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.DigestUtils;
import com.chua.common.support.utils.HexUtils;
import com.chua.common.support.utils.RandomUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.oauth.client.support.entity.AppKeySecret;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.sso.service.SysAppKeyService;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.information.OauthAuthInformation;
import com.chua.starter.oauth.sso.support.token.TokenResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.nio.charset.StandardCharsets;

/**
 * 鉴权信息
 *
 * @author CH
 */
@Slf4j
public final class OauthAuthorization implements Authorization {
    private final String token;
    private final String cookie;
    private final String subProtocol;
    private final String userCode;
    final ApplicationContext applicationContext;
    final LoginCheck loginCheck;
    private final OauthAuthInformation authInformation;

    public OauthAuthorization(OauthAuthInformation authInformation,
                              String token,
                              String cookie,
                              String subProtocol,
                              String userCode,
                              ApplicationContext applicationContext,
                              LoginCheck loginCheck) {
        this.authInformation = authInformation;
        this.token = token;
        this.cookie = cookie;
        this.subProtocol = subProtocol;
        this.userCode = userCode;
        this.applicationContext = applicationContext;
        this.loginCheck = loginCheck;
    }

    @Override
    public ReturnResult<String> authentication() {
        if(StringUtils.isNotBlank(userCode)) {
            return parseAuthInformationUserCode();
        }
        return parseAuthInformation();
    }

    /**
     * 解析用户信息（基于 AppID/AppSecret 签名认证）
     * @return 用户信息
     */
    private ReturnResult<String> parseAuthInformationUserCode() {
        try {
            // 1. 解析请求参数
            AppKeySecret appKeySecret = Json.fromJson(userCode, AppKeySecret.class);
            if (appKeySecret == null) {
                return ReturnResult.error("请求参数格式错误");
            }

            String appId = appKeySecret.getAppId();
            String xTime = appKeySecret.getXTime();
            String xRandom = appKeySecret.getXRandom();
            String xSign = appKeySecret.getXSign();
            String body = StringUtils.defaultString(appKeySecret.getBody(), "");

            log.info("=== AppKey认证开始 ===");
            log.info("AppID: {}", appId);
            log.info("时间戳: {}", xTime);
            log.info("随机数: {}", xRandom);
            log.info("签名: {}", xSign);

            // 2. 参数验证
            if (StringUtils.isBlank(appId) || StringUtils.isBlank(xTime) || 
                StringUtils.isBlank(xRandom) || StringUtils.isBlank(xSign)) {
                return ReturnResult.error("必需参数缺失");
            }

            // 3. 时间戳验证（防重放攻击，5分钟内有效）
            try {
                long timestamp = Long.parseLong(xTime);
                long currentTime = System.currentTimeMillis() / 1000;
                long timeDiff = Math.abs(currentTime - timestamp);
                if (timeDiff > 300) {
                    log.warn("时间戳已过期: timestamp={}, current={}, diff={}秒", 
                            timestamp, currentTime, timeDiff);
                    return ReturnResult.error("时间戳已过期");
                }
            } catch (NumberFormatException e) {
                return ReturnResult.error("时间戳格式错误");
            }

            // 4. 从数据库获取 AppSecret
            var sysAppKeyService =
                    applicationContext.getBean(SysAppKeyService.class);

            var appKey = sysAppKeyService.getByAppId(appId);
            if (appKey == null) {
                log.warn("AppKey不存在: appId={}", appId);
                return ReturnResult.error("AppKey不存在");
            }

            // 5. 检查AppKey状态
            if (appKey.getSysAppKeyStatus() != 0) {
                log.warn("AppKey已禁用: appId={}", appId);
                return ReturnResult.error("AppKey已禁用");
            }

            // 6. 检查过期时间
            if (appKey.getSysAppKeyExpireTime() != null) {
                long currentTime = System.currentTimeMillis() / 1000;
                if (currentTime > appKey.getSysAppKeyExpireTime()) {
                    log.warn("AppKey已过期: appId={}", appId);
                    return ReturnResult.error("AppKey已过期");
                }
            }

            // 7. 构建待签名字符串
            String xBody = HexUtils.encodeHexString(DigestUtils.md5(body.getBytes(StandardCharsets.UTF_8)));
            String signStr = String.format("x-body=%s&x-random=%s&x-time=%s", xBody, xRandom, xTime);
            log.info("待签名字符串: {}", signStr);
            log.info("请求体摘要: {}", xBody);

            // 8. 使用 AppSecret 进行签名
            HMac hmac = new HMac("HmacSHA256", appKey.getSysAppKeySecret().getBytes(StandardCharsets.UTF_8));
            String expectedSign = hmac.digestHex(signStr);
            log.info("期望签名: {}", expectedSign);

            // 9. 验证签名
            if (!expectedSign.equalsIgnoreCase(xSign)) {
                log.warn("签名验证失败: appId={}, expected={}, actual={}", appId, expectedSign, xSign);
                return ReturnResult.error("签名验证失败");
            }

            log.info("签名验证成功: appId={}", appId);

            // 10. 更新使用统计
            try {
                sysAppKeyService.updateUseInfo(appId);
            } catch (Exception e) {
                log.error("更新AppKey使用信息失败", e);
            }

            // 11. 获取用户信息
            String userCodeValue = appKeySecret.getUserCode();
            if (StringUtils.isBlank(userCodeValue)) {
                return ReturnResult.error("用户代码不能为空");
            }

            String tokenManagement = authInformation.getAuthServerProperties().getTokenManagement();
            TokenResolver tokenResolver = ServiceProvider.of(TokenResolver.class).getExtension(tokenManagement);
            if (null == tokenResolver) {
                return ReturnResult.error("认证服务器无法认证");
            }

            ReturnResult<UserResume> resolve = tokenResolver.resolveUserCode(userCodeValue);
            if (!resolve.isSuccess()) {
                log.warn("获取用户信息失败: userCode={}, error={}", userCodeValue, resolve.getMsg());
                return ReturnResult.error(resolve.getMsg());
            }

            UserResume userResult = resolve.getData();
            String json = Json.toJson(userResult);
            log.info("=== AppKey认证成功 === userId={}", userResult.getUserId());
            
            return ReturnResult.ok(authInformation.encodeHex(json));

        } catch (Exception e) {
            log.error("AppKey认证异常", e);
            return ReturnResult.error("认证失败: " + e.getMessage());
        }
    }

    /**
     * 解析用户信息
     * @return 用户信息
     */
    private ReturnResult<String> parseAuthInformation() {
        String tokenManagement = authInformation.getAuthServerProperties().getTokenManagement();
        TokenResolver tokenResolver = ServiceProvider.of(TokenResolver.class).getExtension(tokenManagement);
        if (null == tokenResolver) {
            return ReturnResult.error("认证服务器无法认证");
        }
        ReturnResult<UserResume> resolve = tokenResolver.resolve(cookie, token);
        if (!resolve.isSuccess()) {
            return ReturnResult.error( resolve.getMsg());
        }

        UserResume userResult = resolve.getData();
        String json = Json.toJson(userResult);
        return ReturnResult.ok(authInformation.encodeHex(json));
    }

}
