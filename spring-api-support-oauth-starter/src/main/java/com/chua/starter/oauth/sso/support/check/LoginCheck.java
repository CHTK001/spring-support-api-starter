package com.chua.starter.oauth.sso.support.check;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.starter.oauth.client.support.annotation.LoginType;
import com.chua.starter.oauth.client.support.user.LoginResult;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import com.chua.starter.oauth.sso.support.service.UserInfoService;
import com.chua.starter.oauth.sso.support.token.TokenResolver;
import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ClassUtils;

import java.util.Map;

import static com.chua.common.support.lang.code.ReturnCode.OK;
import static com.chua.starter.oauth.client.support.execute.AuthClientExecute.createUid;

/**
 * 登录检测
 *
 * @author CH
 */
@RequiredArgsConstructor
@Slf4j
public class LoginCheck {

    final AuthServerProperties authServerProperties;

    final ApplicationContext applicationContext;

    /**
     * 是否匹配类型
     *
     * @param userInfoService 服务
     * @param authType        类型
     * @return 是否匹配类型
     */
    private boolean isMatch(UserInfoService userInfoService, String authType) {
        Class<?> aClass = ClassUtils.getUserClass(userInfoService.getClass());
        LoginType userLoginType = aClass.getDeclaredAnnotation(LoginType.class);
        if (null == userLoginType) {
            return false;
        }
        String value = userLoginType.value();
        return null != value && value.equalsIgnoreCase(authType);
    }


    /**
     * 登录验证
     * <p>
     * 根据登录类型选择合适的UserInfoService实现，验证用户凭证，并生成登录令牌。
     * 不同登录类型的参数含义可能不同，如用户名密码登录、令牌登录或第三方登录。
     * </p>
     *
     * @param address    客户端地址
     * @param principal  用户标识（用户名/手机号/邮箱/令牌等）
     * @param credential 用户凭证（密码/令牌/证书等）
     * @param authType   认证类型，与UserInfoService实现的@LoginType注解值匹配
     * @param ext        扩展参数，传递给UserInfoService实现
     * @return 登录结果，包含令牌和用户信息
     */
    public ReturnResult<LoginResult> doLogin(String address, String principal, String credential, String authType, Object ext) {
        UserResult userResult = null;
        Map<String, UserInfoService> beansOfType = applicationContext.getBeansOfType(UserInfoService.class);
        for (Map.Entry<String, UserInfoService> entry : beansOfType.entrySet()) {
            UserInfoService userInfoService = entry.getValue();
            if (!isMatch(userInfoService, authType)) {
                continue;
            }

            try {
                userResult = userInfoService.checkLogin(principal, credential, address, ext);
            } catch (Exception e) {
                log.error("用户认证异常: {}, 类型: {}", principal, authType, e);
                continue;
            }
            if (null != userResult && Strings.isNullOrEmpty(userResult.getMessage())) {
                userResult.setAuthType(authType);
                userResult.setUid(createUid(principal, authType));
                break;
            }
        }

        if (null == userResult) {
            return ReturnResult.noAuth();
        }

        if (!Strings.isNullOrEmpty(userResult.getMessage())) {
            return ReturnResult.noAuth(userResult.getMessage());
        }

        TokenResolver tokenManagement = ServiceProvider.of(TokenResolver.class).getExtension(authServerProperties.getTokenManagement());
        ReturnResult<LoginResult> token = tokenManagement.createToken(address, userResult, authType);
        String code = token.getCode();
        if (OK.getCode().equals(code)) {
            LoginResult loginResult = token.getData();
            loginResult.setUserResult(userResult);
        }
        return token;
    }

    /**
     * 获取用户信息
     *
     * @param userResult 用户信息
     * @return 用户信息
     */
    public UserResult getUserInfo(UserResult userResult) {
        try {
            Map<String, UserInfoService> beansOfType = applicationContext.getBeansOfType(UserInfoService.class);
            UserInfoService userInfoService = null;
            for (Map.Entry<String, UserInfoService> entry : beansOfType.entrySet()) {
                if (isMatch(entry.getValue(), userResult.getAuthType())) {
                    userInfoService = entry.getValue();
                    break;
                }
            }
            if (null == userInfoService) {
                return null;
            }

            return userInfoService.upgrade(userResult);
        } catch (Exception e) {
            return null;
        }
    }
}
