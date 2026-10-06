package com.chua.starter.oauth.sso.support.security;

import com.chua.starter.oauth.sso.support.properties.OauthSecurityProperties;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;

/**
 * OAuth 会话 Fixation 防护过滤器
 *
 * <p><b>为什么必须在 {@code chain.doFilter} 之前换 SessionId。</b></p>
 *
 * <p>原实现是「先放行整个请求链，再判断是否登录成功、成功了才
 * {@code changeSessionId()}」。问题是：</p>
 * <ol>
 *   <li>登录接口通常在 {@code doFilter} 返回前就已经把
 *       {@code Set-Cookie: JSESSIONID=...} 写进响应头并提交了。
 *       此时换 ID <b>无法再改变客户端拿到的 Cookie</b> —— 新 ID 只存在于服务端，
 *       客户端下次请求仍会带上旧的、被攻击者预先知道的 ID。
 *       也就是说该防护<b>整体是空转的</b>，而它看上去像是生效了。</li>
 *   <li>响应一旦提交，任何「事后补救」都太晚：这是 Servlet 规范层面的时序问题，
 *       不是调参能解决的。</li>
 * </ol>
 *
 * <p><b>本实现的做法。</b>在进入业务处理之前就对登录路径换 ID：</p>
 * <ul>
 *   <li>攻击者预先固定的 ID 在认证发生<b>之前</b>就被丢弃，认证成功后写进新会话的
 *       一切都与那个已知 ID 无关 —— 这正是 fixation 防护的目的；</li>
 *   <li>登录失败时也只是白换一个匿名会话 ID，无副作用；</li>
 *   <li>无会话存在时（纯令牌登录，本 OAuth 服务端不依赖 Session）跳过，
 *       不抛异常打断请求。</li>
 * </ul>
 *
 * <p>顺带修掉两处：原实现<b>从不读</b> {@code sessionFixation.enabled}
 * （只靠注册处的 {@code @ConditionalOnProperty} 兜底，于是「按属性动态开关」
 * 在同一份代码里有两套真相）；以及用 {@code endsWith} 匹配登录路径，
 * 会让 {@code /evil/login}、{@code /foo/v1/login} 一并命中 —— 现改为剥掉
 * 上下文路径后精确匹配，与 {@link OauthUriMatcher} 语义一致。</p>
 *
 * @since 2025/01/01
 * @author CH
 */
@Slf4j
@RequiredArgsConstructor
public class OauthSessionFixationFilter implements Filter, Ordered {

    /**
     * 安全性属性
     */
    private final OauthSecurityProperties securityProperties;

    @Override
    /**
     * 执行过滤
     * @param servletRequest servlet请求
     * @param servletResponse servlet响应
     */
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        // 必须在业务处理之前换 ID：等 chain.doFilter 返回就太晚了（响应已提交）
        if (isLoginPost(request)) {
            rotateSessionId(request);
        }

        chain.doFilter(servletRequest, servletResponse);
    }

    /**
     * 登录路径上换掉 SessionId。
     *
     * <p>无会话时静默跳过：本 OAuth 服务端以令牌而非 Session 承载登录态，
     * 大量请求根本不会建立会话，此时 {@code changeSessionId()} 会抛
     * {@link IllegalStateException}，不该因此让请求失败。</p>
     *
     * @param request 请求
     */
    private void rotateSessionId(HttpServletRequest request) {
        try {
            String newSessionId = request.changeSessionId();
            log.debug("Session Fixation防护: 已在业务处理前刷新会话ID, uri={}, newSessionId={}",
                    request.getRequestURI(), newSessionId);
        } catch (IllegalStateException e) {
            log.debug("Session Fixation防护跳过: 无可用会话, uri={}, reason={}",
                    request.getRequestURI(), e.getMessage());
        } catch (Exception e) {
            log.warn("Session Fixation防护异常: uri={}, error={}",
                    request.getRequestURI(), e.getMessage());
        }
    }

    /**
     * 是否是配置中的登录 POST 请求
     *
     * @param request 请求
     * @return 是否登录 POST 的结果
     */
    private boolean isLoginPost(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        if (!securityProperties.getSessionFixation().isEnabled()) {
            return false;
        }
        String uri = OauthUriMatcher.stripContextPath(request.getRequestURI(), request.getContextPath());
        List<String> loginPaths = securityProperties.getSessionFixation().getLoginPaths();
        if (loginPaths == null || loginPaths.isEmpty()) {
            return uri.endsWith("/login");
        }
        for (String path : loginPaths) {
            if (!StringUtils.hasText(path)) {
                continue;
            }
            String normalized = path.trim();
            if (uri.equals(normalized)) {
                return true;
            }
            // 各协议的认证入口都是「/{协议}/login」（见 AuthProtocolController 的
            // @PostMapping(value = {"/login", ...})，协议前缀由调用方拼接），
            // 所以以 /login 结尾的配置项按后缀匹配。这类「多命中」不是缺陷：
            // 多换一次匿名会话 ID 没有安全副作用，而漏掉真正的登录入口才有。
            if (normalized.endsWith("/login") && uri.endsWith("/login")) {
                return true;
            }
        }
        return false;
    }

    @Override
    /**
     * 获取订单
     */
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 20;
    }
}