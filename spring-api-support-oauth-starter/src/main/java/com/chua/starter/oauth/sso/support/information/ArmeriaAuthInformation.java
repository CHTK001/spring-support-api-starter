package com.chua.starter.oauth.sso.support.information;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Armeria 协议认证信息适配器
 * <p>
 * 提供 Armeria 协议的请求/响应适配，使其能够复用现有的认证逻辑
 * </p>
 *
   * @版本 1.0.0
 * @since 2025-12-07
 * @author CH
 */
public class ArmeriaAuthInformation {

    /**
     * Armeria 请求适配器
     * <p>实现 HttpServletRequest 接口，用于适配 Armeria 请求</p>
     * @author CH
     * @since 4.0.0
     */
    public static class ArmeriaRequest implements HttpServletRequest {
        /** 客户端地址 */
        private final InetSocketAddress clientAddress;
        private final Map<String, String> headers = new ConcurrentHashMap<>(); // 头部
        private final Map<String, Object> attributes = new ConcurrentHashMap<>(); // attributes

        /**
        * 创建 armeria请求 实例
        * @param clientAddress 客户端地址
         */
        public ArmeriaRequest(InetSocketAddress clientAddress) {
            this.clientAddress = clientAddress;
        }

        /**
         * 设置头部
         *
         * @param name 名称
         * @param value 值
         */
        public void setHeader(String name, String value) {
            headers.put(name, value);
        }

        @Override
        /** 获取头部 */
        public String getHeader(String name) {
            return headers.get(name);
        }

        @Override
        /** 获取远程addr */
        public String getRemoteAddr() {
            return clientAddress != null ? clientAddress.getAddress().getHostAddress() : "unknown";
        }

        @Override
        /** 获取远程端口 */
        public int getRemotePort() {
            return clientAddress != null ? clientAddress.getPort() : 0;
        }

        @Override
        /** 获取Attribute */
        public Object getAttribute(String name) {
            return attributes.get(name);
        }

        @Override
        /** 设置Attribute */
        public void setAttribute(String name, Object value) {
            attributes.put(name, value);
        }

        @Override
        /** 获取头部名称 */
        public Enumeration<String> getHeaderNames() {
            return Collections.enumeration(headers.keySet());
        }

        @Override
        /** 获取头部 */
        public Enumeration<String> getHeaders(String name) {
            String value = headers.get(name);
            return value != null ? Collections.enumeration(Collections.singletonList(value)) : Collections.emptyEnumeration();
        }

        // 以下为必需但未实现的方法
        @Override public String getAuthType() { return null; }
        @Override public Cookie[] getCookies() { return new Cookie[0]; }
        @Override public long getDateHeader(String name) { return 0; }
        @Override public int getIntHeader(String name) { return 0; }
        @Override public String getMethod() { return "POST"; }
        @Override public String getPathInfo() { return null; }
        @Override public String getPathTranslated() { return null; }
        @Override public String getContextPath() { return ""; }
        @Override public String getQueryString() { return null; }
        @Override public String getRemoteUser() { return null; }
        @Override public boolean isUserInRole(String role) { return false; }
        @Override public Principal getUserPrincipal() { return null; }
        @Override public String getRequestedSessionId() { return null; }
        @Override public String getRequestURI() { return "/"; }
        @Override public StringBuffer getRequestURL() { return new StringBuffer("http://localhost/"); }
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
        @Override public jakarta.servlet.ServletInputStream getInputStream() { return null; }
        @Override public String getParameter(String name) { return null; }
        @Override public Enumeration<String> getParameterNames() { return Collections.emptyEnumeration(); }
        @Override public String[] getParameterValues(String name) { return new String[0]; }
        @Override public Map<String, String[]> getParameterMap() { return Collections.emptyMap(); }
        @Override public String getProtocol() { return "HTTP/1.1"; }
        @Override public String getScheme() { return "http"; }
        @Override public String getServerName() { return "localhost"; }
        @Override public int getServerPort() { return 80; }
        @Override public BufferedReader getReader() { return new BufferedReader(new StringReader("")); }
        @Override public String getRemoteHost() { return getRemoteAddr(); }
        @Override public void removeAttribute(String name) { attributes.remove(name); }
        @Override public Locale getLocale() { return Locale.getDefault(); }
        @Override public Enumeration<Locale> getLocales() { return Collections.enumeration(Collections.singletonList(Locale.getDefault())); }
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
     * Armeria 响应适配器
     * <p>实现 HttpServletResponse 接口，用于适配 Armeria 响应</p>
     * @author CH
     * @since 4.0.0
     */
    public static class ArmeriaResponse implements HttpServletResponse {
        private final Map<String, String> headers = new ConcurrentHashMap<>(); // 头部
        /** 状态 */
        private int status = 200;

        @Override
        /** 设置头部 */
        public void setHeader(String name, String value) {
            headers.put(name, value);
        }

        @Override
        /** 添加头部 */
        public void addHeader(String name, String value) {
            headers.put(name, value);
        }

        @Override
        /** 获取头部 */
        public String getHeader(String name) {
            return headers.get(name);
        }

        @Override
        /** 获取头部 */
        public Collection<String> getHeaders(String name) {
            String value = headers.get(name);
            return value != null ? Collections.singletonList(value) : Collections.emptyList();
        }

        @Override
        /** 获取头部名称 */
        public Collection<String> getHeaderNames() {
            return headers.keySet();
        }

        /**
        * 获取全部头部
        *
        * @return 获取全部头部的结果
         */
        public Map<String, String> getAllHeaders() {
            return new HashMap<>(headers);
        }

        @Override
        /** 设置状态 */
        public void setStatus(int sc) {
            this.status = sc;
        }

        @Override
        /** 获取状态 */
        public int getStatus() {
            return status;
        }

        // 以下为必需但未实现的方法
        @Override public void addCookie(Cookie cookie) {}
        @Override public boolean containsHeader(String name) { return headers.containsKey(name); }
        @Override public String encodeURL(String url) { return url; }
        @Override public String encodeRedirectURL(String url) { return url; }
        @Override public void sendError(int sc, String msg) {}
        @Override public void sendError(int sc) {}
        @Override public void sendRedirect(String location) {}
        @Override public void sendRedirect(String location, int sc, boolean clearBuffer) {}
        @Override public void setDateHeader(String name, long date) {}
        @Override public void addDateHeader(String name, long date) {}
        @Override public void setIntHeader(String name, int value) { headers.put(name, String.valueOf(value)); }
        @Override public void addIntHeader(String name, int value) { headers.put(name, String.valueOf(value)); }
        @Override public String getCharacterEncoding() { return "UTF-8"; }
        @Override public String getContentType() { return "application/json"; }
        @Override public jakarta.servlet.ServletOutputStream getOutputStream() { return null; }
        @Override public PrintWriter getWriter() { return null; }
        @Override public void setCharacterEncoding(String charset) {}
        @Override public void setContentLength(int len) {}
        @Override public void setContentLengthLong(long len) {}
        @Override public void setContentType(String type) {}
        @Override public void setBufferSize(int size) {}
        @Override public int getBufferSize() { return 0; }
        @Override public void flushBuffer() {}
        @Override public void resetBuffer() {}
        @Override public boolean isCommitted() { return false; }
        @Override public void reset() {}
        @Override public void setLocale(Locale loc) {}
        @Override public Locale getLocale() { return Locale.getDefault(); }
    }
}
