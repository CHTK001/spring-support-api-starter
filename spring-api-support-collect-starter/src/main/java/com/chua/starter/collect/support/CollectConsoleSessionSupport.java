package com.chua.starter.collect.support;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * 采集控制台会话工具。
 *
 * <p>登录状态写在 Servlet 会话里，控制台页面与 {@code /v2/collect/**} 接口共用，
 * 因此接口调用方登录后带 Cookie 即可，不需要平台的访问令牌。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
public final class CollectConsoleSessionSupport {

    /**
     * 会话认证标记属性名。
     */
    public static final String SESSION_AUTHENTICATED = "collect.console.authenticated";

    /**
     * 会话登录账号属性名。
     */
    public static final String SESSION_USERNAME = "collect.console.username";

    /**
     * 工具类禁止实例化。
     */
    private CollectConsoleSessionSupport() {
    }

    /**
     * 请求对应的会话是否已登录。
     *
     * @param request HTTP 请求
     * @return 已登录返回 true
     */
    public static boolean isAuthenticated(HttpServletRequest request) {
        return isAuthenticated(request == null ? null : request.getSession(false));
    }

    /**
     * 会话是否已登录。
     *
     * @param session HTTP 会话
     * @return 已登录返回 true
     */
    public static boolean isAuthenticated(HttpSession session) {
        return session != null && Boolean.TRUE.equals(session.getAttribute(SESSION_AUTHENTICATED));
    }

    /**
     * 标记会话已登录并设置有效期。
     *
     * @param session         HTTP 会话
     * @param username        登录账号
     * @param timeoutSeconds  会话有效期秒
     */
    public static void markAuthenticated(HttpSession session, String username, int timeoutSeconds) {
        if (session == null) {
            return;
        }
        session.setAttribute(SESSION_AUTHENTICATED, Boolean.TRUE);
        session.setAttribute(SESSION_USERNAME, username);
        if (timeoutSeconds > 0) {
            session.setMaxInactiveInterval(timeoutSeconds);
        }
    }

    /**
     * 清除会话登录状态。
     *
     * @param session HTTP 会话
     */
    public static void clear(HttpSession session) {
        if (session == null) {
            return;
        }
        session.removeAttribute(SESSION_AUTHENTICATED);
        session.removeAttribute(SESSION_USERNAME);
    }

    /**
     * 取会话里的登录账号。
     *
     * @param session HTTP 会话
     * @return 登录账号，未登录返回 null
     */
    public static String username(HttpSession session) {
        Object value = session == null ? null : session.getAttribute(SESSION_USERNAME);
        return value == null ? null : String.valueOf(value);
    }
}
