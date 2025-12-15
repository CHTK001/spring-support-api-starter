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
     * 获取在线用户列表
     *
     * @param query 查询参数
     * @return 在线用户列表结果
     */
    default OnlineUserResult getOnlineUsers(OnlineUserQuery query) {
        return OnlineUserResult.empty();
    }

    /**
     * 强制踢出用户
     *
     * @param token 用户token
     * @return 是否成功
     */
    default boolean kickUser(String token) {
        return false;
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

    /**
     * 在线用户查询参数
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    class OnlineUserQuery {
        /**
         * 用户名（模糊匹配）
         */
        private String username;
        /**
         * IP地址（模糊匹配）
         */
        private String ip;
        /**
         * 页码（从1开始）
         */
        private int page = 1;
        /**
         * 每页大小
         */
        private int size = 20;
    }

    /**
     * 在线用户列表返回结果
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    class OnlineUserResult {
        /**
         * 在线用户列表
         */
        private java.util.List<OnlineUserInfo> users;
        /**
         * 总数量
         */
        private int total;
        /**
         * 当前页码
         */
        private int page;
        /**
         * 每页大小
         */
        private int size;

        public static OnlineUserResult empty() {
            return new OnlineUserResult(java.util.Collections.emptyList(), 0, 1, 20);
        }
    }

    /**
     * 在线用户信息
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    class OnlineUserInfo {
        /**
         * 用户ID
         */
        private String userId;
        /**
         * 用户名
         */
        private String username;
        /**
         * 昵称
         */
        private String nickname;
        /**
         * 登录IP
         */
        private String loginIp;
        /**
         * 登录地址
         */
        private String loginAddress;
        /**
         * 浏览器
         */
        private String browser;
        /**
         * 操作系统
         */
        private String os;
        /**
         * 登录时间（时间戳）
         */
        private Long loginTime;
        /**
         * Token
         */
        private String token;
        /**
         * 登录类型
         */
        private String loginType;
    }

}
