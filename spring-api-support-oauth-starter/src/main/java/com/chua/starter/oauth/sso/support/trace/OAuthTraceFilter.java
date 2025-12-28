package com.chua.starter.oauth.sso.support.trace;

import com.chua.starter.oauth.sso.support.entity.SysOAuthTraceLog;
import com.chua.starter.oauth.sso.support.service.OAuthTraceLogService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;

import java.io.IOException;
import java.util.Set;

/**
 * OAuth追踪过滤器
 * 接收客户端传递的追踪头，创建追踪上下文，并在请求结束后保存追踪日志
 *
 * @author CH
 * @since 2025/01/01
 */
@Slf4j
@RequiredArgsConstructor
public class OAuthTraceFilter implements Filter, Ordered {

    private final OAuthTraceLogService traceLogService;

    /**
     * 需要追踪的路径
     */
    private static final Set<String> TRACE_PATHS = Set.of(
            "/login", "/logout", "/oauth", "/upgrade", "/temporary-token", "/online/list", "/online/status"
    );

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        
        String requestUri = request.getRequestURI();
        
        // 只追踪OAuth相关路径
        boolean shouldTrace = TRACE_PATHS.stream().anyMatch(requestUri::endsWith);
        
        if (!shouldTrace) {
            chain.doFilter(request, response);
            return;
        }

        // 从请求头获取追踪信息
        String traceId = request.getHeader("x-trace-id");
        String traceTimestamp = request.getHeader("x-trace-timestamp");
        String clientAppName = request.getHeader("x-trace-app-name");
        String clientIp = request.getHeader("x-trace-client-ip");
        
        // 如果没有客户端IP，使用请求的远程地址
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = getClientIp(request);
        }

        // 创建追踪上下文
        ServerTraceContext traceContext = ServerTraceContext.create(traceId, traceTimestamp, clientAppName, clientIp);
        
        // 开始接收请求的Span
        traceContext.startSpan("server_receive");
        
        SysOAuthTraceLog traceLog = null;
        try {
            // 构建追踪日志
            traceLog = traceLogService.buildTraceLog(
                    traceContext.getTraceId(),
                    determineOperationType(requestUri),
                    clientAppName,
                    clientIp,
                    requestUri
            );
            
            // 记录AccessKey
            String accessKey = request.getHeader("x-oauth-ak");
            if (accessKey != null && !accessKey.isEmpty()) {
                traceLog.setSysAccessKey(accessKey);
            }

            traceContext.endSpan(); // 结束 server_receive span
            
            // 执行后续过滤器链
            chain.doFilter(request, response);
            
            // 请求成功
            traceLog.setSysSuccess(response.getStatus() < 400);
            traceLog.setSysResponseCode(response.getStatus());
            
        } catch (Exception e) {
            // 请求异常
            if (traceLog != null) {
                traceLog.setSysSuccess(false);
                traceLog.setSysResponseCode(500);
                traceLog.setSysErrorMessage(e.getMessage());
            }
            throw e;
        } finally {
            // 结束所有未完成的Span
            traceContext.endSpan();
            
            // 设置追踪详情
            if (traceLog != null) {
                traceLog.setSysTotalCostMs(traceContext.getTotalCostMs());
                traceLog.setSysSpansDetail(traceContext.getSpansJson());
                
                // 异步保存日志
                traceLogService.saveAsync(traceLog);
                
                log.debug("[OAuth追踪]请求完成 - traceId: {}, uri: {}, 服务端耗时: {}ms, 总耗时: {}ms, spans: {}",
                        traceContext.getTraceId(), requestUri, 
                        traceContext.getServerCostMs(), traceContext.getTotalCostMs(),
                        traceContext.getSpansSummary());
            }
            
            // 清理追踪上下文
            ServerTraceContext.clear();
        }
    }

    /**
     * 根据请求URI确定操作类型
     */
    private String determineOperationType(String uri) {
        if (uri.endsWith("/login")) {
            return "LOGIN";
        } else if (uri.endsWith("/logout")) {
            return "LOGOUT";
        } else if (uri.endsWith("/oauth")) {
            return "AUTH";
        } else if (uri.endsWith("/upgrade")) {
            return "UPGRADE";
        } else if (uri.endsWith("/temporary-token")) {
            return "TEMP_TOKEN";
        } else if (uri.contains("/online")) {
            return "ONLINE_QUERY";
        }
        return "UNKNOWN";
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理时取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}
