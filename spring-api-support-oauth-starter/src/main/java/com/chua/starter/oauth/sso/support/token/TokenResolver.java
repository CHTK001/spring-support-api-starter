package com.chua.starter.oauth.sso.support.token;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.oauth.client.support.enums.LogoutType;
import com.chua.starter.oauth.client.support.user.LoginResult;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.client.support.user.UserResume;
import jakarta.servlet.http.Cookie;

/**
 * 令牌解释器
 *
 * @author CH
 */
public interface TokenResolver {

    /**
     * 创建令牌
     *
     * @param address    地址
     * @param userResult 用户信息
     * @param authType   登录方式
     * @return 登录结果
     */
    ReturnResult<LoginResult> createToken(String address, UserResult userResult, String authType);

    /**
     * 注销
     *
     * @param token 注销
     */
    void logout(String token);

    /**
     * 注销
     *
     * @param uid  账号ID
     * @param type 类型
     */
    void logout(String uid, LogoutType type);

    /**
     * 解析token
     *
     * @param cookie cookie
     * @param token   token
     * @return 登录信息
     */
    ReturnResult<UserResume> resolve(String cookie, String token);

    /**
     * 刷新token
     *
     * @param token        token
     * @param refreshToken
     * @return 登录信息
     */
    ReturnResult<UserResume> upgradeForTimestamp(String token, String refreshToken);

    /**
     * 刷新token内容
     *
     * @param token        token
     * @param refreshToken 刷新token
     * @return 登录信息
     */
    ReturnResult<UserResume> upgradeForVersion(String token, String refreshToken);

    /**
     * 刷新token
     *
     * @param token        token
     * @param refreshToken 刷新token
     * @return 登录信息
     */
    ReturnResult<UserResume> upgradeForRefresh(String token, String refreshToken);

    /**
     * 注销
     *
     * @param cookies      cookie
     * @param token        token
     * @param refreshToken 刷新token
     * @param cookieName   cookieName
     */
    void logout(Cookie[] cookies, String token, String refreshToken, String cookieName);

}
