package com.chua.starter.system.filter;

import com.chua.common.support.utils.StringUtils;
import com.chua.starter.system.support.ApiRouteExecutor;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 接口停用 / 转发过滤器。
 *
 * <p>必须排在 {@code DispatcherServlet} 之前：转发靠改写 {@code getRequestURI()} 影响
 * 后续路由匹配，如果放在 {@code HandlerInterceptor} 里就晚了。</p>
 *
 * <p>整段兜住异常，任何未预期错误都按放行处理 —— 策略层不该成为把服务打挂的单点。</p>
 *
 * @since 4.0.0.43
 * @author CH
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class ApiRouteFilter extends OncePerRequestFilter {

    /**
     * 转发标记头，便于下游与链路排查识别这次请求被改写过
     */
    public static final String FORWARDED_HEADER = "X-Api-Forwarded-To";

    /**
     * 决策器
     */
    private final ApiRouteExecutor apiRouteExecutor;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        ApiRouteExecutor.Decision decision;
        try {
            decision = apiRouteExecutor.resolve(request);
        } catch (Exception e) {
            log.warn("[ApiRoute] 策略求值异常，放行: uri={}", request.getRequestURI(), e);
            chain.doFilter(request, response);
            return;
        }
        if (decision.passThrough()) {
            chain.doFilter(request, response);
            return;
        }
        if (decision.disabled()) {
            writeDisabled(response, decision.status(), decision.message());
            return;
        }
        chain.doFilter(new ForwardedRequestWrapper(request, decision.targetPath()), response);
    }

    private void writeDisabled(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        String body = "{\"code\":\"" + status + "\",\"msg\":\"" + safe(message) + "\",\"ok\":false}";
        response.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
        response.getOutputStream().flush();
    }

    private String safe(String message) {
        if (StringUtils.isBlank(message)) {
            return ApiRouteRuleMessage.DEFAULT;
        }
        return message.replace("\"", "'");
    }

    /**
     * 只改写请求路径的包装器。
     *
     * <p>刻意不改动 method、参数、请求体、其它请求头：转发语义是「换一个处理方法」，
     * 任何额外改动都可能让鉴权/签名校验失效。</p>
     *
     * <p><b>{@code getRequestURI()} 必须带 context-path</b>：策略里存的是去掉 context-path 的
     * 应用内路径（{@code /v2/user/me}），但 servlet 规范要求 requestURI 以 contextPath 开头，
     * 框架会校验 {@code Invalid contextPath '...' : must match the start of requestPath}。
     * 只改 URI 不改 contextPath（或反过来）都会抛这个异常。</p>
     */
    private static final class ForwardedRequestWrapper extends HttpServletRequestWrapper {

        /**
         * 带 context-path 的目标 URI
         */
        private final String targetUri;

        private ForwardedRequestWrapper(HttpServletRequest request, String targetPath) {
            super(request);
            String contextPath = null == request.getContextPath() ? "" : request.getContextPath();
            this.targetUri = contextPath + targetPath;
        }

        @Override
        public String getRequestURI() {
            return targetUri;
        }

        @Override
        public String getServletPath() {
            return targetUri;
        }
    }

    /**
     * 默认提示常量
     */
    static final class ApiRouteRuleMessage {
        /**
         * 默认提示
         */
        static final String DEFAULT = "接口已停用，请稍后再试";

        private ApiRouteRuleMessage() {
        }
    }
}
