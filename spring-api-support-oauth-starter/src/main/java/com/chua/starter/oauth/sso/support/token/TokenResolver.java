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

    /**
     * 解析 userCode（等价于uid）
     *
     * @param userCode 用户代码
     * @return 用户信息
     */
    ReturnResult<UserResume> resolveUserCode(String userCode);

    /**
     * 查询用户在线状态
     *
     * @param uid 用户唯一标识
     * @return 在线状态信息
     */
    default OnlineStatus getOnlineStatus(String uid) {
        return new OnlineStatus();
    }

    /**
     * 在线状态信息
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    class OnlineStatus {
        /**
         * 当前在线数量
         */
        private int onlineCount = 0;

        /**
         * 最大允许在线数量（-1表示不限制）
         */
        private int maxOnlineCount = -1;

        /**
         * 在线模式（SINGLE/MULTIPLE/LIMIT）
         */
        private String onlineMode = "MULTIPLE";

        /**
         * 是否已达上限
         */
        public boolean isReachedLimit() {
            return maxOnlineCount > 0 && onlineCount >= maxOnlineCount;
        }

        /**
         * 剩余可登录数量（-1表示不限制）
         */
        public int getRemainingSlots() {
            return maxOnlineCount > 0 ? Math.max(0, maxOnlineCount - onlineCount) : -1;
        }
    }

}
