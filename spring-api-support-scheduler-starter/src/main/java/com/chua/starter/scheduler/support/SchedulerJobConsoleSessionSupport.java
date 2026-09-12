package com.chua.starter.scheduler.support;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
* 控制台登录会话工具。
*
* <p>提供控制台登录状态的写入、读取与清理能力,
* 会话信息保存在 Servlet 标准 http会话 中。</p>
*
* @since 4.0.0.42
* @author CH
 */
public final class SchedulerJobConsoleSessionSupport {

    /**
    * 会话认证状态属性名
     */
    public static final String SESSION_AUTHENTICATED = "scheduler.job.console.authenticated";

    /**
    * 会话登录用户名属性名
     */
    public static final String SESSION_USERNAME = "scheduler.job.console.username";

    /**
    * 私有构造器, 禁止实例化。
     */
    private SchedulerJobConsoleSessionSupport() {
    }

    /**
    * 判断请求对应的会话是否已认证。
    *
    * @param request HTTP 请求
    * @return 已认证返回 true, 否则返回 false
     */
    public static boolean isAuthenticated(HttpServletRequest request) {
        HttpSession session = request == null ? null : request.getSession(false);
        return isAuthenticated(session);
    }

    /**
    * 判断会话是否已认证。
    *
    * @param session HTTP 会话
    * @return 已认证返回 true, 否则返回 false
     */
    public static boolean isAuthenticated(HttpSession session) {
        return session != null && Boolean.TRUE.equals(session.getAttribute(SESSION_AUTHENTICATED));
    }

    /**
    * 标记会话为已认证状态。
    *
    * @param session  HTTP 会话
    * @param username 登录用户名
     */
    public static void markAuthenticated(HttpSession session, String username) {
        if (session == null) {
            return;
        }
        session.setAttribute(SESSION_AUTHENTICATED, Boolean.TRUE);
        session.setAttribute(SESSION_USERNAME, username);
    }

    /**
    * 清除会话中的认证信息。
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
    * 获取会话中的登录用户名。
    *
    * @param session HTTP 会话
    * @return 登录用户名, 未登录或会话为空时返回 空
     */
    public static String username(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(SESSION_USERNAME);
        return value == null ? null : String.valueOf(value);
    }
}
