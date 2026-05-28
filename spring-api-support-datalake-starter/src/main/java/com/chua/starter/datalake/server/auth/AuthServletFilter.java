package com.chua.starter.datalake.server.auth;

import com.chua.common.support.network.protocol.ProtocolType;
import com.chua.common.support.network.protocol.filter.ServletFilter;
import com.chua.common.support.network.protocol.request.ServletRequest;
import com.chua.common.support.network.protocol.request.ServletResponse;
import com.chua.common.support.network.protocol.server.AbstractServletFilter;
import com.chua.common.support.network.protocol.server.ServletFilterChain;
import com.chua.datalake.support.spi.auth.RateLimiter;
import com.chua.datalake.support.spi.auth.TokenService;

import java.nio.charset.StandardCharsets;

/**
 * Token 鉴权过滤器 — 验证采集/订阅 API 的 Bearer Token
 * <p>优先级 90，在 MappingServletFilter（200）之前运行。
 * 请求必须携带 {@code Authorization: Bearer <token>} 头部，
 * 否则返回 401。自动跳过 OPTIONS 预检请求、静态资源文件路径、
 * 以及令牌管理端点 ({@code /auth/token})。</p>
 *
 * @author CH
 */
public class AuthServletFilter extends AbstractServletFilter implements ServletFilter {

    private static final String FILTER_NAME = "AuthServletFilter";
    private static final int PRIORITY = 90;

    private final TokenService tokenService;
    private final RateLimiter rateLimiter;

    /** 不参与鉴权的路径前缀 */
    private static final String[] SKIP_PREFIXES = {
            "/auth/token",
    };

    /** 不参与鉴权的文件扩展名（静态资源） */
    private static final String[] SKIP_EXTENSIONS = {
            ".html", ".htm", ".js", ".mjs", ".css", ".png", ".jpg",
            ".jpeg", ".gif", ".svg", ".ico", ".woff", ".woff2",
            ".ttf", ".eot", ".json", ".map", ".txt", ".xml", ".pdf",
    };

    /** 系统属性: datalake.auth.enabled — 是否启用令牌鉴权，默认 true */
    private static final boolean AUTH_ENABLED = isAuthEnabled();

    public AuthServletFilter(TokenService tokenService, RateLimiter rateLimiter) {
        this.tokenService = tokenService;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public boolean isEnabled() {
        return AUTH_ENABLED;
    }

    private static boolean isAuthEnabled() {
        String prop = System.getProperty("datalake.auth.enabled");
        return prop == null || Boolean.parseBoolean(prop);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, ServletFilterChain chain) throws Exception {
        // ── 跳过已提交响应或已有状态码的请求 ──
        if (response.isCommitted() || response.getStatusCode() != 0) {
            chain.doFilter(request, response);
            return;
        }

        String method = request.getMethod();
        // ── 跳过 OPTIONS 预检请求 ──
        if ("OPTIONS".equalsIgnoreCase(method)) {
            chain.doFilter(request, response);
            return;
        }

        String path = request.getPath();
        // ── 跳过令牌管理端点 ──
        for (String prefix : SKIP_PREFIXES) {
            if (path != null && path.contains(prefix)) {
                chain.doFilter(request, response);
                return;
            }
        }

        // ── 跳过静态资源文件路径 ──
        if (path != null) {
            for (String ext : SKIP_EXTENSIONS) {
                if (path.endsWith(ext)) {
                    chain.doFilter(request, response);
                    return;
                }
            }
        }

        // ── 提取 Authorization 头部 ──
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeUnauthorized(response, "缺少 Authorization 头部，格式: Bearer <token>");
            return;
        }

        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            writeUnauthorized(response, "Token 不能为空");
            return;
        }

        // ── 校验 Token ──
        if (!tokenService.validateToken(token)) {
            writeUnauthorized(response, "Token 无效或已过期");
            return;
        }

        // ── 限流检查 ──
        if (!rateLimiter.tryAcquire(token)) {
            response.setStatusCode(429);
            response.setContentType("application/json;charset=UTF-8");
            response.setBodyString("{\"code\":429,\"message\":\"请求过于频繁，请稍后重试\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    /**
     * 写入 401 未授权 JSON 响应
     */
    private void writeUnauthorized(ServletResponse response, String message) {
        response.setStatusCode(401);
        response.setContentType("application/json;charset=UTF-8");
        response.setBodyString("{\"code\":401,\"message\":\"" + message + "\"}");
    }

    @Override
    public String getFilterName() {
        return FILTER_NAME;
    }

    @Override
    public int getOrder() {
        return PRIORITY;
    }

    @Override
    public ProtocolType[] getSupportedProtocolTypes() {
        return new ProtocolType[]{ProtocolType.HTTP, ProtocolType.HTTPS};
    }
}
