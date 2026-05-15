package com.chua.starter.rustserver;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.*;

/**
 * 轻量级 {@link HttpServletRequest} 实现。
 * 数据来源为 Rust 共享内存解析后的 {@link HttpRequestData}。
 */
public class RustHttpServletRequest implements HttpServletRequest {

    private final HttpRequestData data;
    private final ServletContext servletContext;
    private final ServletConnection servletConnection;
    private final Map<String, Object> attributes = new HashMap<>();
    private final Map<String, String[]> parameters;
    private final ServletInputStream inputStream;
    private final BufferedReader reader;
    private final String scheme;
    private final String serverName;
    private final int serverPort;
    private String characterEncoding = StandardCharsets.UTF_8.name();

    public RustHttpServletRequest(HttpRequestData data, ServletContext servletContext) {
        this.data = data;
        this.servletContext = servletContext;

        // 查询参数
        this.parameters = parseQueryString(data.getQueryString());

        // URI 参数合并 (FORM POST)
        if (data.isFormPost() && data.getBody().length > 0) {
            String formStr = new String(data.getBody(), StandardCharsets.UTF_8);
            Map<String, String[]> formParams = parseQueryString(formStr);
            formParams.forEach((k, v) -> {
                if (!this.parameters.containsKey(k)) {
                    this.parameters.put(k, v);
                }
            });
        }

        // 请求体流
        this.inputStream = new ServletInputStream() {
            private final ByteArrayInputStream bin = new ByteArrayInputStream(data.getBody());
            @Override public boolean isFinished() { return bin.available() == 0; }
            @Override public boolean isReady() { return true; }
            @Override public void setReadListener(ReadListener listener) {}
            @Override public int read() { return bin.read(); }
            @Override public int read(byte[] b, int off, int len) { return bin.read(b, off, len); }
        };
        this.reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

        // 解析 Host
        String host = data.getHeader("Host");
        if (host != null) {
            int ci = host.lastIndexOf(':');
            if (ci >= 0 && host.indexOf(':') != ci) { // IPv6
                int ci2 = host.indexOf(']');
                if (ci2 >= 0 && ci > ci2) {
                    this.serverName = host.substring(0, ci).trim();
                    this.serverPort = Integer.parseInt(host.substring(ci + 1).trim());
                } else {
                    this.serverName = host;
                    this.serverPort = 80;
                }
            } else if (ci >= 0) {
                this.serverName = host.substring(0, ci).trim();
                this.serverPort = Integer.parseInt(host.substring(ci + 1).trim());
            } else {
                this.serverName = host;
                this.serverPort = 80;
            }
        } else {
            this.serverName = "localhost";
            this.serverPort = 8080;
        }
        this.scheme = (serverPort == 443) ? "https" : "http";

        // 连接信息
        this.servletConnection = new ServletConnection() {
            @Override public String getConnectionId() { return null; }
            @Override public String getProtocol() { return "HTTP/1.1"; }
            @Override public String getProtocolConnectionId() { return null; }
            @Override public boolean isSecure() { return "https".equals(scheme); }
        };
    }

    // ═══════════════════════════════════════════
    //  路径 / 方法
    // ═══════════════════════════════════════════

    @Override public String getMethod() { return data.getMethod(); }
    @Override public String getRequestURI() { return data.getPath(); }

    @Override
    public StringBuffer getRequestURL() {
        boolean defPort = (scheme.equals("https") && serverPort == 443)
                       || (scheme.equals("http") && serverPort == 80);
        StringBuilder sb = new StringBuilder();
        sb.append(scheme).append("://").append(serverName);
        if (!defPort) sb.append(':').append(serverPort);
        sb.append(data.getPath());
        return new StringBuffer(sb.toString());
    }

    @Override public String getQueryString() { return data.getQueryString(); }
    @Override public String getServletPath() { return ""; }
    @Override public String getContextPath() { return ""; }
    @Override public String getPathInfo() { return null; }
    @Override public String getPathTranslated() { return null; }
    @Override public String getRequestedSessionId() { return null; }

    // ═══════════════════════════════════════════
    //  请求头
    // ═══════════════════════════════════════════

    @Override public String getHeader(String name) { return data.getHeader(name); }

    @Override
    public Enumeration<String> getHeaders(String name) {
        String val = data.getHeader(name);
        return Collections.enumeration(val != null ? Collections.singletonList(val) : Collections.emptyList());
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(data.getHeaders().keySet());
    }

    @Override
    public int getIntHeader(String name) {
        String val = getHeader(name);
        return val != null ? Integer.parseInt(val) : -1;
    }

    @Override
    public long getDateHeader(String name) {
        String val = getHeader(name);
        if (val == null) return -1;
        // 简化的日期解析——实际应使用完整 HTTP 日期格式
        try {
            return new java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US).parse(val).getTime();
        } catch (Exception e) {
            return -1;
        }
    }

    // ═══════════════════════════════════════════
    //  参数
    // ═══════════════════════════════════════════

    @Override public String getParameter(String name) {
        String[] vals = parameters.get(name);
        return (vals != null && vals.length > 0) ? vals[0] : null;
    }

    @Override public Enumeration<String> getParameterNames() {
        return Collections.enumeration(parameters.keySet());
    }

    @Override public String[] getParameterValues(String name) { return parameters.get(name); }

    @Override public Map<String, String[]> getParameterMap() {
        return Collections.unmodifiableMap(parameters);
    }

    // ═══════════════════════════════════════════
    //  属性
    // ═══════════════════════════════════════════

    @Override public Object getAttribute(String name) { return attributes.get(name); }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }

    @Override
    public void setAttribute(String name, Object value) {
        if (value != null) attributes.put(name, value);
        else attributes.remove(name);
    }

    @Override public void removeAttribute(String name) { attributes.remove(name); }

    // ═══════════════════════════════════════════
    //  内容信息
    // ═══════════════════════════════════════════

    @Override
    public String getContentType() {
        String ct = data.getContentType();
        return ct != null ? ct : getHeader("Content-Type");
    }

    @Override
    public int getContentLength() {
        String cl = getHeader("Content-Length");
        return cl != null ? Integer.parseInt(cl) : (data.getBody().length > 0 ? data.getBody().length : -1);
    }

    @Override public long getContentLengthLong() { return getContentLength(); }
    @Override public String getCharacterEncoding() { return characterEncoding; }

    @Override
    public void setCharacterEncoding(String enc) throws UnsupportedEncodingException {
        this.characterEncoding = enc;
    }

    @Override public ServletInputStream getInputStream() { return inputStream; }
    @Override public BufferedReader getReader() { return reader; }

    // ═══════════════════════════════════════════
    //  协议 / 地址
    // ═══════════════════════════════════════════

    @Override public String getScheme() { return scheme; }
    @Override public String getServerName() { return serverName; }
    @Override public int getServerPort() { return serverPort; }
    @Override public String getProtocol() { return "HTTP/1.1"; }
    @Override public String getRequestId() { return String.valueOf(data.getRequestId()); }
    @Override public String getProtocolRequestId() { return null; }
    @Override public ServletConnection getServletConnection() { return servletConnection; }
    @Override public String getRemoteAddr() { return "127.0.0.1"; }
    @Override public String getRemoteHost() { return "127.0.0.1"; }
    @Override public int getRemotePort() { return 0; }
    @Override public String getLocalAddr() { return "0.0.0.0"; }
    @Override public String getLocalName() { return serverName; }
    @Override public int getLocalPort() { return serverPort; }
    @Override public boolean isSecure() { return "https".equals(scheme); }

    // ═══════════════════════════════════════════
    //  Session
    // ═══════════════════════════════════════════

    @Override public HttpSession getSession(boolean create) { return null; }
    @Override public HttpSession getSession() { return null; }
    @Override public String changeSessionId() { return null; }
    @Override public boolean isRequestedSessionIdValid() { return false; }
    @Override public boolean isRequestedSessionIdFromCookie() { return false; }
    @Override public boolean isRequestedSessionIdFromURL() { return false; }

    // ═══════════════════════════════════════════
    //  Cookie
    // ═══════════════════════════════════════════

    @Override
    public Cookie[] getCookies() {
        String cookieHeader = getHeader("Cookie");
        if (cookieHeader == null || cookieHeader.isEmpty()) return null;
        List<Cookie> cookies = new ArrayList<>();
        for (String part : cookieHeader.split(";")) {
            part = part.trim();
            int eq = part.indexOf('=');
            if (eq > 0) {
                cookies.add(new Cookie(part.substring(0, eq).trim(), part.substring(eq + 1).trim()));
            }
        }
        return cookies.toArray(new Cookie[0]);
    }

    // ═══════════════════════════════════════════
    //  安全 / 认证
    // ═══════════════════════════════════════════

    @Override public Principal getUserPrincipal() { return null; }
    @Override public boolean isUserInRole(String role) { return false; }
    @Override public String getRemoteUser() { return null; }
    @Override public String getAuthType() { return null; }
    @Override public boolean authenticate(HttpServletResponse response) { return false; }
    @Override public void login(String username, String password) {}
    @Override public void logout() {}

    // ═══════════════════════════════════════════
    //  Dispatcher / Forward / Include
    // ═══════════════════════════════════════════

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return servletContext.getRequestDispatcher(path);
    }

    @Override public DispatcherType getDispatcherType() { return DispatcherType.REQUEST; }
    @Override public ServletContext getServletContext() { return servletContext; }
    @Override public AsyncContext startAsync() { throw new UnsupportedOperationException("Async not supported"); }
    @Override public AsyncContext startAsync(ServletRequest req, ServletResponse resp) {
        throw new UnsupportedOperationException("Async not supported");
    }
    @Override public boolean isAsyncSupported() { return false; }
    @Override public boolean isAsyncStarted() { return false; }
    @Override public AsyncContext getAsyncContext() { return null; }

    // ═══════════════════════════════════════════
    //  Locale
    // ═══════════════════════════════════════════

    @Override
    public Locale getLocale() {
        String lang = getHeader("Accept-Language");
        if (lang != null) {
            String[] parts = lang.split(",");
            if (parts.length > 0) {
                String[] lp = parts[0].split(";")[0].split("-");
                if (lp.length == 2) return new Locale(lp[0], lp[1]);
                if (lp.length == 1) return new Locale(lp[0]);
            }
        }
        return Locale.getDefault();
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return Collections.enumeration(Collections.singletonList(getLocale()));
    }

    // ═══════════════════════════════════════════
    //  Multipart (未实现)
    // ═══════════════════════════════════════════

    @Override public Part getPart(String name) { return null; }
    @Override public Collection<Part> getParts() { return Collections.emptyList(); }

    // ═══════════════════════════════════════════
    //  Upgrade / Trailer
    // ═══════════════════════════════════════════

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) {
        throw new UnsupportedOperationException("Upgrade not supported");
    }

    @Override public Map<String, String> getTrailerFields() { return Collections.emptyMap(); }
    @Override public boolean isTrailerFieldsReady() { return false; }

    // ═══════════════════════════════════════════
    //  工具
    // ═══════════════════════════════════════════

    static Map<String, String[]> parseQueryString(String qs) {
        Map<String, String[]> result = new LinkedHashMap<>();
        if (qs == null || qs.isEmpty()) return result;
        for (String pair : qs.split("&")) {
            int eq = pair.indexOf('=');
            String key, val;
            if (eq >= 0) {
                key = urlDecode(pair.substring(0, eq));
                val = urlDecode(pair.substring(eq + 1));
            } else {
                key = urlDecode(pair);
                val = "";
            }
            result.compute(key, (k, existing) -> {
                if (existing == null) return new String[]{val};
                String[] nv = Arrays.copyOf(existing, existing.length + 1);
                nv[existing.length] = val;
                return nv;
            });
        }
        return result;
    }

    private static String urlDecode(String s) {
        try {
            return URLDecoder.decode(s, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return s;
        }
    }
}
