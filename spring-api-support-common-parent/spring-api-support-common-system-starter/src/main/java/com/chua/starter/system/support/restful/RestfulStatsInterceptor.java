package com.chua.starter.system.support.restful;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * RESTful 请求旁路统计拦截器（方案 A）。
 * <p>
 * 在请求完成后将 method/url/duration/status 写入 {@link RestfulStatsCollector}，
 * 不改动 sys_log 审计链路，也不依赖 report-client 的 UrlQps 上报。
 * </p>
 *
 * @since 4.0.0.50
 * @author CH
 */
public class RestfulStatsInterceptor implements HandlerInterceptor {

    /**
     * Attr_启动_时间
     */
    private static final String ATTR_START_TIME = "restfulStats_startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(ATTR_START_TIME, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        try {
            Long startTime = (Long) request.getAttribute(ATTR_START_TIME);
            if (startTime == null) {
                return;
            }
            String path = request.getRequestURI();
            String contextPath = request.getContextPath();
            if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
                path = path.substring(contextPath.length());
            }
            path = path.replaceAll("/\\d+", "/{id}");
            path = path.replaceAll("/[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}", "/{uuid}");

            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();
            boolean success = status >= 200 && status < 400;
            RestfulStatsCollector.getInstance().record(path, request.getMethod(), duration, success, resolveClientIp(request));
        } catch (Exception ignored) {
            // 统计失败不影响业务
        }
    }

    /**
     * 解析客户端真实 IP：优先代理头，其次远端地址。
     *
     * @param request 请求
     * @return 客户端 IP
     */
    private String resolveClientIp(HttpServletRequest request) {
        String[] headers = {"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP"};
        for (String h : headers) {
            String v = request.getHeader(h);
            if (v != null && !v.isBlank() && !"unknown".equalsIgnoreCase(v)) {
                int comma = v.indexOf(',');
                return comma > 0 ? v.substring(0, comma).trim() : v.trim();
            }
        }
        String remote = request.getRemoteAddr();
        if ("0:0:0:0:0:0:0:1".equals(remote) || "::1".equals(remote)) {
            return "127.0.0.1";
        }
        return remote;
    }
}
