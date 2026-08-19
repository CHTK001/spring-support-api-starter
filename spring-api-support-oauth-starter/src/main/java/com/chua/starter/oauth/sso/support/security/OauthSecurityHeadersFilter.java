package com.chua.starter.oauth.sso.support.security;

import com.chua.starter.oauth.sso.support.properties.OauthSecurityProperties;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * OAuth安全响应头过滤器
 *
 * <p>为所有响应追加安全相关 HTTP 头，降低点击劫持、MIME 嗅探、XSS 等风险。</p>
 *
 * @since 2025/01/01
 * @author CH
 */
@Slf4j
@RequiredArgsConstructor
public class OauthSecurityHeadersFilter implements Filter, Ordered {

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
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        OauthSecurityProperties.Headers headers = securityProperties.getHeaders();

        setHeaderIfPresent(response, "X-Content-Type-Options", headers.getContentTypeOptions());
        setHeaderIfPresent(response, "X-Frame-Options", headers.getFrameOptions());
        setHeaderIfPresent(response, "X-XSS-Protection", headers.getXssProtection());
        setHeaderIfPresent(response, "Referrer-Policy", headers.getReferrerPolicy());
        setHeaderIfPresent(response, "Content-Security-Policy", headers.getContentSecurityPolicy());
        setHeaderIfPresent(response, "Strict-Transport-Security", headers.getStrictTransportSecurity());
        setHeaderIfPresent(response, "Permissions-Policy", headers.getPermissionsPolicy());
        setHeaderIfPresent(response, "Cache-Control", headers.getCacheControl());

        if (StringUtils.hasText(headers.getCacheControl())) {
            response.setHeader("Pragma", "no-cache");
        }

        chain.doFilter(servletRequest, response);
    }

    private void setHeaderIfPresent(HttpServletResponse response, String name, String value) {
        if (StringUtils.hasText(value)) {
            response.setHeader(name, value);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
