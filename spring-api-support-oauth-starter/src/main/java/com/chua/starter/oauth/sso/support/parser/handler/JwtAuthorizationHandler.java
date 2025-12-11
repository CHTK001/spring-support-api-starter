package com.chua.starter.oauth.sso.support.parser.handler;

import com.chua.common.support.annotations.Spi;
import com.chua.common.support.annotations.SpiDefault;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.information.OauthAuthInformation;
import com.chua.starter.oauth.sso.support.token.TokenResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

/**
 * JWT Token认证处理器（默认实现）
 * <p>
 * 处理基于JWT Token的认证请求
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/11
 */
@Slf4j
@Spi("jwt")
@SpiDefault
public class JwtAuthorizationHandler implements AuthorizationHandler {

    @Override
    public ReturnResult<String> authenticate(OauthAuthInformation authInformation, 
                                            String token, 
                                            String cookie, 
                                            String userCode) {
        log.info("使用JWT认证方式");
        
        ApplicationContext applicationContext = authInformation.getApplicationContext();
        LoginCheck loginCheck = authInformation.getLoginCheck();
        
        // 1. 优先使用Token
        String authToken = StringUtils.isNotBlank(token) ? token : cookie;
        if (StringUtils.isBlank(authToken)) {
            log.warn("Token和Cookie都为空");
            return ReturnResult.error("认证信息缺失");
        }
        
        // 2. 解析Token获取用户信息
        TokenResolver tokenResolver = ServiceProvider.of(TokenResolver.class)
                .getNewExtension("redis");
        
        UserResume userResume = tokenResolver.解析Token(authToken);
        if (userResume == null) {
            log.warn("Token解析失败或已过期: {}", authToken);
            return ReturnResult.error("Token无效或已过期");
        }
        
        // 3. 登录检查（黑名单、状态等）
        ReturnResult<String> checkResult = loginCheck.check(userResume);
        if (!checkResult.isOk()) {
            log.warn("登录检查失败: userId={}, reason={}", 
                    userResume.getUserId(), checkResult.getMsg());
            return checkResult;
        }
        
        // 4. 刷新Token（可选）
        String newToken = tokenResolver.refreshToken(authToken, userResume);
        
        log.info("JWT认证成功: userId={}", userResume.getUserId());
        return ReturnResult.ok(StringUtils.defaultString(newToken, authToken));
    }

    @Override
    public boolean supports(String subProtocol, String token, String userCode) {
        // JWT认证：有token或cookie，且没有userCode
        return StringUtils.isBlank(userCode) && 
               (StringUtils.isNotBlank(token) || StringUtils.isNotBlank(subProtocol));
    }
}
