package com.chua.starter.oauth.sso.support.security;

import com.chua.starter.oauth.sso.support.properties.OauthSecurityProperties;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * OAuth CSRF 防护过滤器
 *
 * <p>对状态变更请求校验 CSRF 令牌；安全方法（GET 等）自动下发令牌 Cookie。</p>
 * <p>令牌存储于 Redis，键前缀 {@code oauth:security:csrf:}。</p>
 *
 * @since 2025/01/01
 * @author CH
 */
@Slf4j
@RequiredArgsConstructor
public class OauthCsrfFilter implements Filter, Ordered {

    /** Csrf_prefix */
    private static final String CSRF_PREFIX = "oauth:security:csrf:";

    /** Security属性 */
    private final OauthSecurityProperties securityProperties;
    /** 字符串redis模板 */
    private final StringRedisTemplate stringRedisTemplate;

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
        OauthSecurityProperties.Csrf csrf = securityProperties.getCsrf();

        if (isIgnored(request, csrf)) {
            chain.doFilter(request, response);
            return;
        }

        String method = request.getMethod();
        if (requiresValidation(method, csrf)) {
            if (!validateToken(request, csrf)) {
                log.warn("CSRF校验失败: method={}, uri={}, ip={}",
                        method, request.getRequestURI(), request.getRemoteAddr());
                writeForbidden(response);
                return;
            }
        } else {
            ensureTokenCookie(request, response, csrf);
        }

        chain.doFilter(request, response);
    }

    private boolean isIgnored(HttpServletRequest request, OauthSecurityProperties.Csrf csrf) {
        String uri = request.getRequestURI();
        List<String> ignorePaths = csrf.getIgnorePaths();
        if (ignorePaths == null || ignorePaths.isEmpty()) {
            return false;
        }
        for (String pattern : ignorePaths) {
            if (!StringUtils.hasText(pattern)) {
                continue;
            }
            if (uri.equals(pattern) || uri.endsWith(pattern) || uri.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    private boolean requiresValidation(String method, OauthSecurityProperties.Csrf csrf) {
        List<String> methods = csrf.getMethods();
        if (methods == null || methods.isEmpty()) {
            return false;
        }
        for (String m : methods) {
            if (m != null && m.equalsIgnoreCase(method)) {
                return true;
            }
        }
        return false;
    }

    private boolean validateToken(HttpServletRequest request, OauthSecurityProperties.Csrf csrf) {
        String requestToken = resolveRequestToken(request, csrf);
        if (!StringUtils.hasText(requestToken)) {
            return false;
        }

        String cookieToken = resolveCookieToken(request, csrf.getCookieName());
        if (!StringUtils.hasText(cookieToken)) {
            return false;
        }

        if (!requestToken.equals(cookieToken)) {
            return false;
        }

        try {
            String redisValue = stringRedisTemplate.opsForValue().get(CSRF_PREFIX + cookieToken);
            return StringUtils.hasText(redisValue);
        } catch (Exception e) {
            log.error("CSRF Redis校验异常: error={}", e.getMessage());
            return false;
        }
    }

    private String resolveRequestToken(HttpServletRequest request, OauthSecurityProperties.Csrf csrf) {
        String token = request.getHeader(csrf.getHeaderName());
        if (StringUtils.hasText(token)) {
            return token;
        }
        return request.getParameter(csrf.getParameterName());
    }

    private String resolveCookieToken(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    /**
     * EnsureTokenCookie
     * @param request request
     * @param response response
     * @param csrf csrf
     */
    private void ensureTokenCookie(HttpServletRequest request, HttpServletResponse response,
                                   OauthSecurityProperties.Csrf csrf) {
        String existing = resolveCookieToken(request, csrf.getCookieName());
        if (StringUtils.hasText(existing)) {
            try {
                Boolean exists = stringRedisTemplate.hasKey(CSRF_PREFIX + existing);
                if (Boolean.TRUE.equals(exists)) {
                    return;
                }
            } catch (Exception e) {
                log.debug("CSRF令牌存在性检查失败: {}", e.getMessage());
            }
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        try {
            stringRedisTemplate.opsForValue().set(
                    CSRF_PREFIX + token,
                    "1",
                    Duration.ofSeconds(csrf.getTokenTtlSeconds())
            );
        } catch (Exception e) {
            log.error("CSRF令牌写入Redis失败: error={}", e.getMessage());
            return;
        }

        Cookie cookie = new Cookie(csrf.getCookieName(), token);
        cookie.setPath("/");
        cookie.setHttpOnly(false);
        cookie.setSecure(request.isSecure());
        cookie.setMaxAge((int) Math.min(csrf.getTokenTtlSeconds(), Integer.MAX_VALUE));
        response.addCookie(cookie);
        response.setHeader(csrf.getHeaderName(), token);
    }

    private void writeForbidden(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String body = "{\"code\":403,\"msg\":\"CSRF校验失败\",\"data\":null}";
        response.getWriter().write(body);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 15;
    }
}
