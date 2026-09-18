package com.chua.starter.oauth.sso.support.information;

import com.chua.protocol.support.network.protocol.request.ServletRequest;
import com.chua.protocol.support.network.protocol.request.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 协议服务端 协议认证信息适配器
 * <p>
 * 提供 协议服务端 协议的请求/响应适配，使其能够复用现有的认证逻辑
 * </p>
 *
 * @author CH
 * @版本 1.0.0
 * @since 2025-12-15
 */
public class ProtocolServerAuthInformation {

    /**
     * 协议服务端 请求适配器
     * <p>实现 HttpServletRequest 接口，用于适配 ProtocolServer 请求</p>
     * @author CH
     * @since 4.0.0
     */
    public static class ProtocolServerRequest implements HttpServletRequest {
        private final ServletRequest protocolRequest; // 协议请求
        private final Map<String, String> headers = new ConcurrentHashMap<>(); // 头部
        private final Map<String, Object> attributes = new ConcurrentHashMap<>(); // attributes
        private String remoteAddr; // 远程addr
        private int remotePort; // 远程端口

        /**
         * 协议服务端请求。
         * @param protocolRequest 协议请求
         */
        public ProtocolServerRequest(ServletRequest protocolRequest) {
            this.protocolRequest = protocolRequest;
 // 从 协议服务端 的 servlet请求 获取客户端地址
            if (protocolRequest != null) {
                this.remoteAddr = protocolRequest.getRemoteAddr();
                this.remotePort = protocolRequest.getRemotePort();
 // 从 协议服务端 的 servlet请求 获取 头部（统一转小写，HTTP 头部 大小写不敏感）
 // 优先使用 请求头部.foreach，同时也通过 获取头部名称 枚举兜底
                var requestHeaders = protocolRequest.getHeaders();
                if (requestHeaders != null) {
                    try {
                        requestHeaders.forEach((name, value) -> {
                            if (name != null && value != null) {
                                headers.put(name.toLowerCase(), value);
                            }
                        });
                    } catch (Exception ignored) {
 // foreach 失败时通过 获取头部名称 枚举
                    }
                }
 // 兜底：通过 获取头部名称 枚举（防止 foreach 未覆盖所有 头部）
                var headerNames = protocolRequest.getHeaderNames();
                if (headerNames != null) {
                    while (headerNames.hasMoreElements()) {
                        String name = headerNames.nextElement();
                        if (name != null) {
                            String value = protocolRequest.getHeader(name);
                            if (value != null) {
                                headers.put(name.toLowerCase(), value);
                            }
                        }
                    }
                }
            }
        }

        /**
         * 设置头部。
         * @param name 名称
         * @param value 值
         */
        public void setHeader(String name, String value) {
            headers.put(name != null ? name.toLowerCase() : name, value);
        }

        @Override
        public String getHeader(String name) {
 // 统一转小写查找，HTTP 头部 大小写不敏感
            return name != null ? headers.get(name.toLowerCase()) : null;
        }

        @Override
        public String getRemoteAddr() {
            return remoteAddr != null ? remoteAddr : "unknown";
        }

        @Override
        public int getRemotePort() {
            return remotePort != 0 ? remotePort : 0;
        }

        @Override
        public Object getAttribute(String name) {
            return attributes.get(name);
        }

        @Override
        public void setAttribute(String name, Object value) {
            attributes.put(name, value);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            return Collections.enumeration(headers.keySet());
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            String value = headers.get(name);
            if (value != null) {
                return Collections.enumeration(Collections.singletonList(value));
            }
            return Collections.emptyEnumeration();
        }

        // 以下为必需但未实现的方法，返回默认值
        @Override public String getAuthType() { return null; }
        @Override public Cookie[] getCookies() { 
            return new Cookie[0]; 
        }
        @Override public long getDateHeader(String name) { return 0; }
        @Override public int getIntHeader(String name) { return 0; }
        @Override public String getMethod() { 
            return protocolRequest != null ? protocolRequest.getMethod() : "POST"; 
        }
        @Override public String getPathInfo() { return null; }
        @Override public String getPathTranslated() { return null; }
        @Override public String getContextPath() { return ""; }
        @Override public String getQueryString() { 
            return protocolRequest != null ? protocolRequest.getQueryString() : null; 
        }
        @Override public String getRemoteUser() { return null; }
        @Override public boolean isUserInRole(String role) { return false; }
        @Override public Principal getUserPrincipal() { return null; }
        @Override public String getRequestedSessionId() { return null; }
        @Override public String getRequestURI() { 
            return protocolRequest != null ? protocolRequest.getRequestURI() : "/"; 
        }
        @Override public StringBuffer getRequestURL() { 
            return new StringBuffer("http://localhost/"); 
        }
        @Override public String getServletPath() { return ""; }
        @Override public jakarta.servlet.http.HttpSession getSession(boolean create) { return null; }
        @Override public jakarta.servlet.http.HttpSession getSession() { return null; }
        @Override public String changeSessionId() { return null; }
        @Override public boolean isRequestedSessionIdValid() { return false; }
        @Override public boolean isRequestedSessionIdFromCookie() { return false; }
        @Override public boolean isRequestedSessionIdFromURL() { return false; }
        @Override public boolean authenticate(HttpServletResponse response) { return false; }
        @Override public void login(String username, String password) {}
        @Override public void logout() {}
        @Override public Collection<jakarta.servlet.http.Part> getParts() { return Collections.emptyList(); }
        @Override public jakarta.servlet.http.Part getPart(String name) { return null; }
        @Override public <T extends jakarta.servlet.http.HttpUpgradeHandler> T upgrade(Class<T> handlerClass) { return null; }
        @Override public Enumeration<String> getAttributeNames() { return Collections.enumeration(attributes.keySet()); }
        @Override public String getCharacterEncoding() { return "UTF-8"; }
        @Override public void setCharacterEncoding(String env) {}
        @Override public int getContentLength() { return 0; }
        @Override public long getContentLengthLong() { return 0; }
        @Override public String getContentType() { return "application/json"; }
        @Override public jakarta.servlet.ServletInputStream getInputStream() throws IOException { 
            return null; 
        }
        
        // 解析查询参数
        /**
         * 解析查询参数。
         * @return 解析查询参数的结果
         */
        private Map<String, String> parseQueryParams() {
            Map<String, String> params = new HashMap<>();
            String queryString = getQueryString();
            if (queryString != null && !queryString.isEmpty()) {
                String[] pairs = queryString.split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=", 2);
                    if (keyValue.length == 2) {
                        try {
                            params.put(keyValue[0], URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8));
                        } catch (Exception e) {
                            params.put(keyValue[0], keyValue[1]);
                        }
                    } else if (keyValue.length == 1) {
                        params.put(keyValue[0], "");
                    }
                }
            }
            return params;
        }
        
        @Override public String getParameter(String name) { 
            return parseQueryParams().get(name);
        }
        @Override public Enumeration<String> getParameterNames() { 
            return Collections.enumeration(parseQueryParams().keySet()); 
        }
        @Override public String[] getParameterValues(String name) { 
            String value = parseQueryParams().get(name);
            return value != null ? new String[]{value} : new String[0];
        }
        @Override public Map<String, String[]> getParameterMap() { 
            Map<String, String[]> result = new HashMap<>();
            parseQueryParams().forEach((key, value) -> result.put(key, new String[]{value}));
            return result;
        }
        @Override public String getProtocol() { return "HTTP/1.1"; }
        @Override public String getScheme() { return "http"; }
        @Override public String getServerName() { return "localhost"; }
        @Override public int getServerPort() { return 80; }
        @Override public BufferedReader getReader() throws IOException { 
            return new BufferedReader(new StringReader("")); 
        }
        @Override public String getRemoteHost() { return getRemoteAddr(); }
        @Override public void removeAttribute(String name) { 
            attributes.remove(name);
        }
        @Override public Locale getLocale() { return Locale.getDefault(); }
        @Override public Enumeration<Locale> getLocales() { 
            return Collections.enumeration(Collections.singletonList(Locale.getDefault())); 
        }
        @Override public boolean isSecure() { return false; }
        @Override public jakarta.servlet.RequestDispatcher getRequestDispatcher(String path) { return null; }
        @Override public int getLocalPort() { return 0; }
        @Override public String getLocalName() { return "localhost"; }
        @Override public String getLocalAddr() { return "127.0.0.1"; }
        @Override public jakarta.servlet.ServletContext getServletContext() { return null; }
        @Override public jakarta.servlet.AsyncContext startAsync() { return null; }
        @Override public jakarta.servlet.AsyncContext startAsync(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res) { return null; }
        @Override public boolean isAsyncStarted() { return false; }
        @Override public boolean isAsyncSupported() { return false; }
        @Override public jakarta.servlet.AsyncContext getAsyncContext() { return null; }
        @Override public jakarta.servlet.DispatcherType getDispatcherType() { return jakarta.servlet.DispatcherType.REQUEST; }
        @Override public String getRequestId() { return UUID.randomUUID().toString(); }
        @Override public String getProtocolRequestId() { return ""; }
        @Override public jakarta.servlet.ServletConnection getServletConnection() { return null; }
    }

    /**
     * 协议服务端 响应适配器
     * <p>实现 HttpServletResponse 接口，用于适配 ProtocolServer 响应</p>
     * @author CH
     * @since 4.0.0
     * @return 获取全部头部的结果
     * @param name 名称
     * @param value 值
     */
    public static class ProtocolServerResponse implements HttpServletResponse {
        private final ServletResponse protocolResponse; // 协议响应
        private final Map<String, String> headers = new ConcurrentHashMap<>(); // 头部
        /**
         * 协议服务端响应。
         * @param protocolResponse 协议响应
         */
        private int status = 200;
        private String bodyString; // 主体字符串
        
        public ProtocolServerResponse(ServletResponse protocolResponse) {
            /**
             * 设置主体字符串。
             * @param body 主体
             */
            this.protocolResponse = protocolResponse;
        }
        
        public void setBodyString(String body) {
            /**
             * 获取主体字符串。
             * @return 获取主体字符串的结果
             */
            this.bodyString = body;
        }
        
        public String getBodyString() {
            return bodyString;
        /**
         * 设置头部。
         * @param name 名称
         * @param value 值
         */
        }

        @Override
        public void setHeader(String name, String value) {
            headers.put(name, value);
            if (protocolResponse != null) {
                protocolResponse.addHeader(name, value);
            }
        }

        @Override
        public void addHeader(String name, String value) {
            headers.put(name, value);
            if (protocolResponse != null) {
                protocolResponse.addHeader(name, value);
            }
        }

        @Override
        public String getHeader(String name) {
            return headers.get(name);
        }

        @Override
        public Collection<String> getHeaders(String name) {
            String value = headers.get(name);
            if (value != null) {
                return Collections.singletonList(value);
            }
            return Collections.emptyList();
        }

        @Override
        public Collection<String> getHeaderNames() {
            return headers.keySet();
        }

        public Map<String, String> getAllHeaders() {
            return new HashMap<>(headers);
        }

        @Override
        public void setStatus(int sc) {
            this.status = sc;
            if (protocolResponse != null) {
                protocolResponse.setStatus(sc);
            }
        }

        @Override
        public int getStatus() {
            return status;
        }

        // 以下为必需但未实现的方法
        @Override public void addCookie(Cookie cookie) {
 // 协议服务端 的 servlet响应 可能不支持 Cookie
        }
        @Override public boolean containsHeader(String name) { return headers.containsKey(name); }
        @Override public String encodeURL(String url) { return url; }
        @Override public String encodeRedirectURL(String url) { return url; }
        @Override public void sendError(int sc, String msg) throws IOException {
            setStatus(sc);
        }
        @Override public void sendError(int sc) throws IOException {
            setStatus(sc);
        }
        @Override public void sendRedirect(String location) throws IOException {
            sendRedirect(location, 302, false);
        }
        @Override public void sendRedirect(String location, int statusCode, boolean isSameContext) throws IOException {
            setStatus(statusCode);
            setHeader("Location", location);
        }
        @Override public void setDateHeader(String name, long date) {
            headers.put(name, String.valueOf(date));
        }
        @Override public void addDateHeader(String name, long date) {
            headers.put(name, String.valueOf(date));
        }
        @Override public void setIntHeader(String name, int value) { 
            headers.put(name, String.valueOf(value));
        }
        @Override public void addIntHeader(String name, int value) { 
            headers.put(name, String.valueOf(value));
        }
        @Override public String getCharacterEncoding() { return "UTF-8"; }
        @Override public String getContentType() { return "application/json"; }
        @Override public jakarta.servlet.ServletOutputStream getOutputStream() throws IOException { 
            return null; 
        }
        @Override public PrintWriter getWriter() throws IOException { 
            return null; 
        }
        @Override public void setCharacterEncoding(String charset) {
 // 协议服务端 的 servlet响应 可能不支持设置字符编码
        }
        @Override public void setContentLength(int len) {
 // 协议服务端 的 servlet响应 可能不支持设置内容长度
        }
        @Override public void setContentLengthLong(long len) {
 // 协议服务端 的 servlet响应 可能不支持设置内容长度
        }
        @Override public void setContentType(String type) {
            headers.put("Content-Type", type);
            if (protocolResponse != null) {
                protocolResponse.setContentType(type);
            }
        }
        @Override public void setBufferSize(int size) {
 // 协议服务端 的 servlet响应 可能不支持设置缓冲区大小
        }
        @Override public int getBufferSize() { return 0; }
        @Override public void flushBuffer() throws IOException {
 // 协议服务端 的 servlet响应 可能不支持刷新缓冲区
        }
        @Override public void resetBuffer() {
 // 协议服务端 的 servlet响应 可能不支持重置缓冲区
        }
        @Override public boolean isCommitted() { 
            return false; 
        }
        @Override public void reset() {
 // 协议服务端 的 servlet响应 可能不支持重置
        }
        @Override public void setLocale(Locale loc) {
 // 协议服务端 的 servlet响应 可能不支持设置 区域
        }
        @Override public Locale getLocale() { return Locale.getDefault(); }
    }
}

