package com.chua.starter.oauth.sso.support.security;

import com.chua.starter.oauth.sso.support.properties.OauthSecurityProperties;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;

/**
 * OAuth Session Fixation 防护过滤器
 *
 * <p>登录成功（POST 登录路径且响应 2xx）后调用 {@link HttpServletRequest#changeSessionId()}，
 * 防止攻击者固定会话标识。</p>
 *
 * @since 2025/01/01
 * @author CH
 */
@Slf4j
@RequiredArgsConstructor
public class OauthSessionFixationFilter implements Filter, Ordered {

    /** Security属性 */
    private final OauthSecurityProperties securityProperties;

    @Override
    /**
     * Do过滤
     * @param servletRequest servletRequest
     * @param servletResponse servletResponse
     */
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        boolean loginRequest = isLoginPost(request);
        chain.doFilter(request, response);

        if (!loginRequest) {
            return;
        }

        int status = response.getStatus();
        if (status < 200 || status >= 300) {
            return;
        }

        try {
            String newSessionId = request.changeSessionId();
            log.debug("Session Fixation防护: 已刷新会话ID, uri={}, newSessionId={}",
                    request.getRequestURI(), newSessionId);
        } catch (IllegalStateException e) {
            log.debug("Session Fixation防护跳过: 无可用会话, uri={}, reason={}",
                    request.getRequestURI(), e.getMessage());
        } catch (Exception e) {
            log.warn("Session Fixation防护异常: uri={}, error={}",
                    request.getRequestURI(), e.getMessage());
        }
    }

    private boolean isLoginPost(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String uri = request.getRequestURI();
        List<String> loginPaths = securityProperties.getSessionFixation().getLoginPaths();
        if (loginPaths == null || loginPaths.isEmpty()) {
            return uri.endsWith("/login");
        }
        for (String path : loginPaths) {
            if (!StringUtils.hasText(path)) {
                continue;
            }
            if (uri.equals(path) || uri.endsWith(path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 20;
    }
}
