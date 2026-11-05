// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
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
 * @since 4.0.0.42
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
     * @param uid  账号标识
     * @param type 类型
     */
    void logout(String uid, LogoutType type);

    /**
     * 解析令牌
     *
     * @param cookie Cookie
     * @param token   令牌
     * @return 登录信息
     */
    ReturnResult<UserResume> resolve(String cookie, String token);

    /**
     * 刷新令牌
     *
     * @param token        令牌
     * @param refreshToken
     * @return 登录信息
     */
    ReturnResult<UserResume> upgradeForTimestamp(String token, String refreshToken);

    /**
     * 刷新令牌内容
     *
     * @param token        令牌
     * @param refreshToken 刷新令牌
     * @return 登录信息
     */
    ReturnResult<UserResume> upgradeForVersion(String token, String refreshToken);

    /**
     * 刷新令牌
     *
     * @param token        令牌
     * @param refreshToken 刷新令牌
     * @return 登录信息
     */
    ReturnResult<UserResume> upgradeForRefresh(String token, String refreshToken);

    /**
     * 注销
     *
     * @param cookies      Cookie
     * @param token        令牌
     * @param refreshToken 刷新令牌
     * @param cookieName   Cookie名称
     */
    void logout(Cookie[] cookies, String token, String refreshToken, String cookieName);

    /**
     * 解析 用户编码（等价于uid）
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
     * @param token 用户令牌
     * @return 是否成功
     */
    default boolean kickUser(String token) {
        return false;
    }

    /**
     * 在线状态信息
     *
     * @since 4.0.0.42
     * @author CH
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
         * 在线模式（单个/多个/限制）
         */
        private String onlineMode = "MULTIPLE";

        /**
         * 是否已达上限
         * @return 是否reached限制的结果
         */
        public boolean isReachedLimit() {
            return maxOnlineCount > 0 && onlineCount >= maxOnlineCount;
        }

        /**
         * 剩余可登录数量（-1表示不限制）
         * @return 获取remainingslots的结果
         */
        public int getRemainingSlots() {
            return maxOnlineCount > 0 ? Math.max(0, maxOnlineCount - onlineCount) : -1;
        }
    }

    /**
     * 在线用户查询参数
     * @author CH
     * @since 4.0.0
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
     * @author CH
     * @since 4.0.0
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

        /**
         * 空
         *
         * @return 空的结果
         */
        public static OnlineUserResult empty() {
            return new OnlineUserResult(java.util.Collections.emptyList(), 0, 1, 20);
        }
    }

    /**
     * 在线用户信息
     * @author CH
     * @since 4.0.0
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    class OnlineUserInfo {
        /**
         * 用户标识
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
         * 令牌
         */
        private String token;
        /**
         * 登录类型
         */
        private String loginType;
    }

}