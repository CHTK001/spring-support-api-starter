package com.chua.starter.oauth.sso.support.parser.handler;

import com.chua.common.support.annotations.Spi;
import com.chua.common.support.annotations.SpiDefault;
import com.chua.common.support.crypto.digest.HMac;
import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.oauth.client.support.entity.AppKeySecret;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.sso.service.SysAppKeyService;
import com.chua.starter.oauth.sso.support.information.OauthAuthInformation;
import com.chua.starter.oauth.sso.support.token.TokenResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

/**
 * 默认认证处理器（AppKey签名认证）
 * <p>
 * 处理基于AppID/AppSecret签名的认证请求
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/11
 */
@Slf4j
@Spi("appkey")
@SpiDefault
public class DefaultAuthorizationHandler implements AuthorizationHandler {

    @Override
    public ReturnResult<String> authenticate(OauthAuthInformation authInformation, 
                                            String token, 
                                            String cookie, 
                                            String userCode) {
        log.info("使用AppKey签名认证方式");
        
        ApplicationContext applicationContext = authInformation.getApplicationContext();
        
        try {
            // 1. 解析AppKey参数
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

            // 2. 参数验证
            if (StringUtils.isBlank(appId) || StringUtils.isBlank(xTime) || 
                StringUtils.isBlank(xRandom) || StringUtils.isBlank(xSign)) {
                return ReturnResult.error("必需参数缺失");
            }

            // 3. 时间戳验证（防重放攻击，5分钟内有效）
            long timestamp = Long.parseLong(xTime);
            long currentTime = System.currentTimeMillis() / 1000;
            long timeDiff = Math.abs(currentTime - timestamp);
            if (timeDiff > 300) {
                log.warn("时间戳已过期: timestamp={}, current={}, diff={}秒", 
                        timestamp, currentTime, timeDiff);
                return ReturnResult.error("时间戳已过期，请求可能被重放");
            }

            // 4. 查询AppKey
            SysAppKeyService appKeyService = applicationContext.getBean(SysAppKeyService.class);
            AppKeySecret dbAppKey = appKeyService.getByAppId(appId);
            if (dbAppKey == null) {
                log.warn("AppKey不存在: appId={}", appId);
                return ReturnResult.error("AppKey不存在");
            }

            // 5. 签名验证
            String signContent = appId + xTime + xRandom + body;
            String expectedSign = HMac.sha256(signContent, dbAppKey.getAppSecret());
            
            log.debug("签名内容: {}", signContent);
            log.debug("期望签名: {}", expectedSign);
            log.debug("实际签名: {}", xSign);
            
            if (!expectedSign.equals(xSign)) {
                log.warn("签名验证失败: appId={}", appId);
                return ReturnResult.error("签名验证失败");
            }

            // 6. 构建用户信息
            UserResume userResume = new UserResume();
            userResume.setUserId(dbAppKey.getUserId());
            userResume.setUsername(dbAppKey.getAppName());
            userResume.setAppId(appId);
            
            // 7. 生成Token
            TokenResolver tokenResolver = ServiceProvider.of(TokenResolver.class)
                    .getNewExtension("redis");
            String newToken = tokenResolver.createToken(userResume);
            
            log.info("AppKey认证成功: appId={}, userId={}", appId, dbAppKey.getUserId());
            return ReturnResult.ok(newToken);
            
        } catch (NumberFormatException e) {
            log.error("时间戳格式错误", e);
            return ReturnResult.error("时间戳格式错误");
        } catch (Exception e) {
            log.error("AppKey认证失败", e);
            return ReturnResult.error("认证失败: " + e.getMessage());
        }
    }

    @Override
    public boolean supports(String subProtocol, String token, String userCode) {
        // AppKey认证：有userCode参数
        return StringUtils.isNotBlank(userCode);
    }
}
