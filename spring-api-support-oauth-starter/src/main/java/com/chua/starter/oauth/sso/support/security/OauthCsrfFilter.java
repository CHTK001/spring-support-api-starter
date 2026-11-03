// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
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

    /**
     * CSRF_前缀
    */
    private static final String CSRF_PREFIX = "oauth:security:csrf:";

    /**
     * 「非 Cookie」服务凭据头
     *
     * <p>浏览器表单 / 跨站脚本无法设置这些自定义头，携带它们的请求天然不受 CSRF 约束。</p>
     */
    private static final String[] NON_COOKIE_CREDENTIAL_HEADERS = {
            "x-oauth-ak",
            "x-oauth-sign",
            "x-oauth-token",
            "x-oauth-user-code",
            "Authorization",
    };

    /**
     * 安全性属性
    */
    private final OauthSecurityProperties securityProperties;
    /**
     * 字符串Redis模板
    */
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    /**
     * 执行过滤
     * @param servletRequest servlet请求
     * @param servletResponse servlet响应
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
        if (requiresValidation(method, csrf) && !hasNonCookieCredential(request)) {
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

    /**
     * 是否Ignored
     *
     * <p><b>修复</b>：原实现用 {@code uri.contains(pattern)} 命中豁免，而
     * {@code getRequestURI()} 恒含上下文路径 {@code /oauth}，默认 ignorePaths 中的
     * {@code "/oauth"} 导致<b>所有</b>请求都被豁免，CSRF 校验实际从未生效。
     * 现改为剥离上下文路径后的锚定 Ant 匹配（见 {@link OauthUriMatcher}）。</p>
     *
     * @param request 请求
     * @param csrf csrf
     * @return 是否ignored的结果
     */
    private boolean isIgnored(HttpServletRequest request, OauthSecurityProperties.Csrf csrf) {
        if (!csrf.isEnabled()) {
            return true;
        }
        return OauthUriMatcher.matchesAny(OauthUriMatcher.logicalPath(request), csrf.getIgnorePaths());
    }

    /**
     * requires校验
     *
     * <p><b>修复</b>：本服务同时服务两类调用方——① 后端服务（oauth-client-starter），
     * 用 {@code x-oauth-ak}/{@code x-oauth-sign} 签名或 {@code x-oauth-token}/{@code Authorization}
     * 头认证；② 浏览器（第三方登录回调写入的 Cookie）。</p>
     * <p>CSRF 威胁模型只针对「浏览器自动携带 Cookie」这一种情形：攻击者的表单 / 跨站
     * 脚本无法设置自定义请求头，因此一旦请求带有上述任一凭据头，就不存在 CSRF 可利用面，
     * 对其强制要求 double-submit 只会让所有服务间调用 403。故此处只对<b>依赖 Cookie 认证</b>
     * 的请求校验 CSRF。</p>
     *
     * @param method 方法
     * @param csrf csrf
     * @return requires校验的结果
     */
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

    /**
     * 请求是否携带「非 Cookie」的服务凭据（这类请求不受 CSRF 威胁模型约束）
     *
     * @param request 请求
     * @return true 表示是机器调用方，CSRF 校验不适用
     */
    private boolean hasNonCookieCredential(HttpServletRequest request) {
        for (String header : NON_COOKIE_CREDENTIAL_HEADERS) {
            String value = request.getHeader(header);
            if (StringUtils.hasText(value) && !"unknown".equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 校验令牌
     *
     * @param request 请求
     * @param csrf csrf
     * @return 校验令牌的结果
     */
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

    /**
     * 解析请求令牌
     *
     * @param request 请求
     * @param csrf csrf
     * @return resolve请求令牌的结果
     */
    private String resolveRequestToken(HttpServletRequest request, OauthSecurityProperties.Csrf csrf) {
        String token = request.getHeader(csrf.getHeaderName());
        if (StringUtils.hasText(token)) {
            return token;
        }
        return request.getParameter(csrf.getParameterName());
    }

    /**
     * 解析Cookie令牌
     *
     * @param request 请求
     * @param cookieName Cookie名称
     * @return resolveCookie令牌的结果
     */
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
     * ensure令牌Cookie
     * @param request 请求
     * @param response 响应
     * @param csrf CSRF
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

    /**
     * 写入Forbidden
     *
     * @param response 响应
     */
    private void writeForbidden(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String body = "{\"code\":403,\"msg\":\"CSRF校验失败\",\"data\":null}";
        response.getWriter().write(body);
    }

    @Override
    /**
     * 获取订单
    */
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 15;
    }
}
