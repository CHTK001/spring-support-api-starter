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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Locale;

/**
 * OAuth 管理控制台鉴权过滤器
 *
 * <p><b>修复一个严重缺陷</b>：{@code AdminController} 提供的 {@code /oauth/admin/**}
 * 此前<b>完全无鉴权</b>。本服务作为认证服务端，通过 {@code SsoServer} 标记类 +
 * {@code @ConditionalOnMissingClass} 主动关闭了客户端侧的 {@code AuthFilter}，
 * 同时未引入 Spring Security，因此没有兜底的认证层。结果：</p>
 * <ul>
 *   <li>{@code POST /oauth/admin/sign-verify?enabled=false} → 关闭全局请求签名校验；</li>
 *   <li>{@code POST /oauth/admin/expire?expire=9223372036854775807} → 事实上的永久令牌；</li>
 *   <li>{@code POST /oauth/admin/online-mode?mode=MULTIPLE} → 解除单点登录约束；</li>
 *   <li>{@code POST /oauth/admin/rate-limit} → 任意覆盖限流参数。</li>
 * </ul>
 * <p>本过滤器要求携带 {@code plugin.oauth.security.admin.token} 配置的管理令牌
 * （请求头，默认 {@code X-OAuth-Admin-Token}，或 {@code Authorization: Bearer}）。
 * 关键安全性质：</p>
 * <ol>
 *   <li><b>fail-closed</b>：令牌未配置时拒绝一切管理请求，绝不退化为「无令牌即放行」；</li>
 *   <li><b>常量时间比较</b>：用 {@link MessageDigest#isEqual}，避免按字节提前返回导致时序侧信道；</li>
 *   <li><b>可选 IP 收敛</b>：配置 {@code ipWhitelist} 后管理面只对内网/跳板机开放。</li>
 * </ol>
 *
 * @since 4.0.0.51
 * @author CH
 */
@Slf4j
@RequiredArgsConstructor
public class OauthAdminAuthFilter implements Filter, Ordered {

    /**
     * 安全配置
     */
    private final OauthSecurityProperties securityProperties;

    @Override
    /**
     * 执行过滤
     *
     * @param servletRequest  servlet请求
     * @param servletResponse servlet响应
     * @param chain           过滤器链
     * @throws IOException      写响应异常
     * @throws ServletException 过滤器异常
     */
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        OauthSecurityProperties.Admin admin = securityProperties.getAdmin();
        if (admin == null || !admin.isEnabled()) {
            // 显式关闭（仅限本地调试）。此处打 WARN，避免生产误关而无感知。
            log.warn("[AdminAuth] 管理控制台鉴权已被显式关闭，/admin/** 处于无鉴权状态！");
            chain.doFilter(request, response);
            return;
        }

        String logicalPath = OauthUriMatcher.logicalPath(request);
        if (!OauthUriMatcher.matchesAny(logicalPath, admin.getPaths())) {
            chain.doFilter(request, response);
            return;
        }

        String expected = admin.getToken();
        if (!StringUtils.hasText(expected)) {
            // fail-closed：未配置令牌 = 拒绝，而不是放行
            log.error("[AdminAuth] 管理令牌未配置(plugin.oauth.security.admin.token)，拒绝管理请求: uri={}",
                    logicalPath);
            writeUnauthorized(response, "管理控制台未配置访问令牌");
            return;
        }

        if (!isIpAllowed(request, admin.getIpWhitelist())) {
            log.warn("[AdminAuth] 来源 IP 不在白名单内，拒绝: uri={}, ip={}", logicalPath,
                    request.getRemoteAddr());
            writeForbidden(response);
            return;
        }

        String provided = resolveToken(request, admin.getHeaderName());
        if (!StringUtils.hasText(provided) || !constantTimeEquals(expected.trim(), provided.trim())) {
            log.warn("[AdminAuth] 管理令牌校验失败: uri={}, ip={}", logicalPath, request.getRemoteAddr());
            writeUnauthorized(response, "管理令牌无效");
            return;
        }

        chain.doFilter(request, response);
    }

    /**
     * 解析请求携带的管理令牌
     *
     * @param request    请求
     * @param headerName 头名称
     * @return 令牌，取不到返回 null
     */
    private String resolveToken(HttpServletRequest request, String headerName) {
        String token = request.getHeader(headerName);
        if (StringUtils.hasText(token)) {
            return token;
        }
        String authorization = request.getHeader("Authorization");
        if (StringUtils.hasText(authorization)) {
            String prefix = "Bearer ";
            if (authorization.regionMatches(true, 0, prefix, 0, prefix.length())) {
                return authorization.substring(prefix.length());
            }
            return authorization;
        }
        return null;
    }

    /**
     * 常量时间字符串比较
     *
     * <p>刻意不用 {@code String#equals}：后者在首个不等字节处短路返回，
     * 理论上可被时序侧信道逐字节猜测令牌。</p>
     *
     * @param a 期望值
     * @param b 实际值
     * @return 相等返回 true
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 判断来源 IP 是否在白名单内（白名单为空表示不限制）
     *
     * @param request  请求
     * @param allowed  白名单
     * @return 允许返回 true
     */
    private boolean isIpAllowed(HttpServletRequest request, List<String> allowed) {
        if (allowed == null || allowed.isEmpty()) {
            return true;
        }
        String ip = request.getRemoteAddr();
        if (!StringUtils.hasText(ip)) {
            return false;
        }
        for (String entry : allowed) {
            if (!StringUtils.hasText(entry)) {
                continue;
            }
            String candidate = entry.trim();
            if (candidate.equalsIgnoreCase(ip)) {
                return true;
            }
            int slash = candidate.indexOf('/');
            if (slash > 0 && cidrMatches(ip, candidate.substring(0, slash),
                    parsePrefixLength(candidate.substring(slash + 1)))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 解析 CIDR 前缀长度，非法返回 -1
     *
     * @param raw 前缀字符串
     * @return 前缀长度，非法返回 -1
     */
    private int parsePrefixLength(String raw) {
        if (!StringUtils.hasText(raw)) {
            return -1;
        }
        try {
            int bits = Integer.parseInt(raw.trim());
            return (bits >= 0 && bits <= 128) ? bits : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * 判断 ip 是否落在给定网段内
     *
     * @param ip      待判断 ip
     * @param network 网段地址
     * @param prefix  前缀长度
     * @return 命中返回 true
     */
    private boolean cidrMatches(String ip, String network, int prefix) {
        if (prefix < 0) {
            return false;
        }
        try {
            byte[] addr = InetAddress.getByName(ip).getAddress();
            byte[] net = InetAddress.getByName(network).getAddress();
            if (addr.length != net.length) {
                return false;
            }
            int fullBytes = prefix / 8;
            int remainingBits = prefix % 8;
            for (int i = 0; i < fullBytes; i++) {
                if (addr[i] != net[i]) {
                    return false;
                }
            }
            if (remainingBits == 0) {
                return true;
            }
            int mask = (0xFF << (8 - remainingBits)) & 0xFF;
            return (addr[fullBytes] & mask) == (net[fullBytes] & mask);
        } catch (UnknownHostException e) {
            return false;
        }
    }

    /**
     * 写入 Unauthorized
     *
     * @param response 响应
     * @param message  提示
     * @throws IOException 写响应异常
     */
    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, message);
    }

    /**
     * 写入 Forbidden
     *
     * @param response 响应
     * @throws IOException 写响应异常
     */
    private void writeForbidden(HttpServletResponse response) throws IOException {
        writeJson(response, HttpServletResponse.SC_FORBIDDEN, "来源地址不允许访问管理控制台");
    }

    /**
     * 写 json 响应
     *
     * @param response 响应
     * @param status   状态码
     * @param message  提示
     * @throws IOException 写响应异常
     */
    private void writeJson(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        String safe = message == null ? "" : message.replace("\"", "'");
        String body = String.format(Locale.ROOT,
                "{\"code\":%d,\"msg\":\"%s\",\"data\":null}", status, safe);
        response.getWriter().write(body);
    }

    @Override
    /**
     * 获取订单
     *
     * @return 过滤器顺序
     */
    public int getOrder() {
        // 排在限流(+5)之后：既让管理面也吃 IP 限流（抗令牌爆破），又早于 CSRF(+15)
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}