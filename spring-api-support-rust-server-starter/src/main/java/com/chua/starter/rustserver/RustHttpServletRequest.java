package com.chua.starter.rustserver;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * 轻量级 {@link HttpServletRequest} 实现。
 * 数据来源为 Rust 共享内存解析后的 {@link HttpRequestData}。
 */
public class RustHttpServletRequest implements HttpServletRequest {

    private final HttpRequestData data;
    private final ServletContext servletContext;
    private final SessionManager sessionManager;
    private final ServletConnection servletConnection;
    private final Map<String, Object> attributes = new HashMap<>();
    private final Map<String, String[]> parameters;
    private final ServletInputStream inputStream;
    private final BufferedReader reader;
    private final String scheme;
    private final String serverName;
    private final int serverPort;
    private String characterEncoding = StandardCharsets.UTF_8.name();

    // Session 跟踪
    private SimpleHttpSession session;
    private String requestedSessionId;
    private boolean requestedSessionIdParsed;
    private boolean requestedSessionIdFromCookie;

    public RustHttpServletRequest(HttpRequestData data, ServletContext servletContext, SessionManager sessionManager) {
        this.data = data;
        this.servletContext = servletContext;
        this.sessionManager = sessionManager;

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

        // 请求体流 (支持 Content-Encoding: gzip 自动解压)
        this.inputStream = new ServletInputStream() {
            private final InputStream in = createDecompressedStream(data);
            @Override public boolean isFinished() { try { return in.available() == 0; } catch (IOException e) { return true; } }
            @Override public boolean isReady() { return true; }
            @Override public void setReadListener(ReadListener listener) {}
            @Override public int read() throws IOException { return in.read(); }
            @Override public int read(byte[] b, int off, int len) throws IOException { return in.read(b, off, len); }
            @Override public void close() throws IOException { in.close(); }
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
        this.scheme = data.isSecure() ? "https" : "http";

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
    @Override public String getServletPath() { return data.getPath(); }
    @Override public String getContextPath() { return ""; }
    @Override public String getPathInfo() { return null; }
    @Override public String getPathTranslated() { return null; }
    @Override public String getRequestedSessionId() {
        parseRequestedSessionId();
        return requestedSessionId;
    }

    // ═══════════════════════════════════════════
    //  请求头
    // ═══════════════════════════════════════════

    @Override
    public String getHeader(String name) { return data.getHeader(name); }

    @Override
    public Enumeration<String> getHeaders(String name) {
        List<String> values = data.getHeaders().get(name);
        return values != null ? Collections.enumeration(values) : Collections.emptyEnumeration();
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
        try {
            return new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US)
                    .parse(val).getTime();
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot parse date header: " + name + " = " + val);
        }
    }

    // ═══════════════════════════════════════════
    //  参数
    // ═══════════════════════════════════════════

    @Override
    public String getParameter(String name) {
        String[] vals = parameters.get(name);
        return (vals != null && vals.length > 0) ? vals[0] : null;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(parameters.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        return parameters.get(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return Collections.unmodifiableMap(parameters);
    }

    @Override
    public String getContentType() { return data.getContentType(); }
    @Override public int getContentLength() { return data.getContentLength(); }
    @Override public long getContentLengthLong() { return data.getContentLength(); }
    @Override public String getCharacterEncoding() { return characterEncoding; }
    @Override public void setCharacterEncoding(String encoding) { this.characterEncoding = encoding; }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        if (value != null) attributes.put(name, value);
        else attributes.remove(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }

    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    @Override
    public ServletInputStream getInputStream() { return inputStream; }

    @Override
    public BufferedReader getReader() { return reader; }

    @Override
    public String getScheme() { return scheme; }

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

    private void parseRequestedSessionId() {
        if (requestedSessionIdParsed) return;
        requestedSessionIdParsed = true;

        Cookie[] cookies = getCookies();
        if (cookies != null && sessionManager != null) {
            String cookieName = sessionManager.getCookieConfig().getName();
            for (Cookie c : cookies) {
                if (cookieName.equals(c.getName())) {
                    requestedSessionId = c.getValue();
                    requestedSessionIdFromCookie = true;
                    return;
                }
            }
        }
    }

    @Override
    public HttpSession getSession(boolean create) {
        if (sessionManager == null) return null;

        if (session != null && session.isValid()) {
            session.setLastAccessedTime(System.currentTimeMillis());
            return session;
        }
        session = null;

        parseRequestedSessionId();
        if (requestedSessionId != null) {
            SimpleHttpSession existing = sessionManager.getSession(requestedSessionId);
            if (existing != null && existing.isValid()) {
                session = existing;
                session.setNew(false);
                session.setLastAccessedTime(System.currentTimeMillis());
                return session;
            }
        }

        if (!create) return null;

        session = sessionManager.createSession();
        session.setNew(true);
        return session;
    }

    @Override
    public HttpSession getSession() {
        return getSession(true);
    }

    @Override
    public String changeSessionId() {
        if (session == null) return null;
        String oldId = session.getId();
        sessionManager.removeSession(oldId);
        SimpleHttpSession newSession = sessionManager.createSession();
        Enumeration<String> names = session.getAttributeNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            newSession.setAttribute(name, session.getAttribute(name));
        }
        session = newSession;
        return session.getId();
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        parseRequestedSessionId();
        if (requestedSessionId == null) return false;
        SimpleHttpSession s = sessionManager != null ? sessionManager.getSession(requestedSessionId) : null;
        return s != null && s.isValid();
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        parseRequestedSessionId();
        return requestedSessionIdFromCookie;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        parseRequestedSessionId();
        return requestedSessionId != null && !requestedSessionIdFromCookie;
    }

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

    /**
     * 获取创建的 session（用于 response 写入 cookie）。
     */
    public SimpleHttpSession getCreatedSession() {
        return session;
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
    //  Multipart
    // ═══════════════════════════════════════════

    private Collection<Part> parts;

    @Override
    public Part getPart(String name) {
        if (parts == null) parts = parseMultipart();
        for (Part p : parts) {
            if (name.equals(p.getName())) return p;
        }
        return null;
    }

    @Override
    public Collection<Part> getParts() {
        if (parts == null) parts = parseMultipart();
        return parts;
    }

    private Collection<Part> parseMultipart() {
        String contentType = getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("multipart/")) {
            return Collections.emptyList();
        }

        String boundary = extractBoundary(contentType);
        if (boundary == null) return Collections.emptyList();

        byte[] body = data.getBody();
        if (body.length == 0) return Collections.emptyList();

        List<Part> result = new ArrayList<>();
        byte[] boundaryBytes = ("--" + boundary).getBytes(StandardCharsets.US_ASCII);

        int pos = 0;
        int start = indexOf(body, boundaryBytes, 0);
        if (start < 0) return Collections.emptyList();
        pos = start + boundaryBytes.length;

        if (pos + 2 <= body.length && body[pos] == '\r' && body[pos + 1] == '\n') pos += 2;

        while (pos < body.length) {
            int next = indexOf(body, boundaryBytes, pos);
            if (next < 0) break;

            int partEnd = next;
            if (partEnd - 2 >= pos && body[partEnd - 2] == '\r' && body[partEnd - 1] == '\n') partEnd -= 2;

            if (partEnd > pos) {
                byte[] partData = Arrays.copyOfRange(body, pos, partEnd);
                SimplePart part = parsePart(partData);
                if (part != null) result.add(part);
            }

            int afterBoundary = next + boundaryBytes.length;
            if (afterBoundary + 1 < body.length && body[afterBoundary] == '-' && body[afterBoundary + 1] == '-') {
                break;
            }

            pos = afterBoundary;
            if (pos + 2 <= body.length && body[pos] == '\r' && body[pos + 1] == '\n') pos += 2;
            else if (pos + 1 < body.length && (body[pos] == '\n' || body[pos] == '\r')) pos += 1;
        }

        return result;
    }

    private SimplePart parsePart(byte[] partData) {
        int headerEnd = -1;
        for (int i = 0; i < partData.length - 3; i++) {
            if (partData[i] == '\r' && partData[i + 1] == '\n' &&
                partData[i + 2] == '\r' && partData[i + 3] == '\n') {
                headerEnd = i;
                break;
            }
        }
        if (headerEnd < 0) return null;

        Map<String, List<String>> partHeaders = new LinkedHashMap<>();
        String headerSection = new String(partData, 0, headerEnd, StandardCharsets.US_ASCII);
        for (String line : headerSection.split("\r\n")) {
            int ci = line.indexOf(':');
            if (ci > 0) {
                String hname = line.substring(0, ci).trim().toLowerCase(Locale.ROOT);
                String hval = line.substring(ci + 1).trim();
                partHeaders.computeIfAbsent(hname, k -> new ArrayList<>()).add(hval);
            }
        }

        int bodyStart = headerEnd + 4;
        byte[] partBody = Arrays.copyOfRange(partData, bodyStart, partData.length);

        String name = "";
        String fileName = null;
        List<String> cd = partHeaders.get("content-disposition");
        if (cd != null && !cd.isEmpty()) {
            String disposition = cd.get(0);
            name = extractDispositionParam(disposition, "name");
            fileName = extractDispositionParam(disposition, "filename");
        }

        String contentType = null;
        List<String> ct = partHeaders.get("content-type");
        if (ct != null && !ct.isEmpty()) contentType = ct.get(0);

        return new SimplePart(name, fileName, contentType, partBody, partHeaders);
    }

    private static String extractDispositionParam(String disposition, String paramName) {
        String marker = paramName + "=";
        int ci = disposition.toLowerCase(Locale.ROOT).indexOf(marker);
        if (ci < 0) return "";
        int start = ci + marker.length();
        if (start >= disposition.length()) return "";
        String value;
        if (disposition.charAt(start) == '"') {
            int end = disposition.indexOf('"', start + 1);
            if (end < 0) return disposition.substring(start + 1);
            value = disposition.substring(start + 1, end);
        } else {
            int end = disposition.indexOf(';', start);
            value = end > start ? disposition.substring(start, end).trim() : disposition.substring(start).trim();
        }
        return value;
    }

    private static int indexOf(byte[] haystack, byte[] needle, int fromIndex) {
        for (int i = fromIndex; i <= haystack.length - needle.length; i++) {
            boolean found = true;
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) { found = false; break; }
            }
            if (found) return i;
        }
        return -1;
    }

    private static String extractBoundary(String contentType) {
        String lower = contentType.toLowerCase(Locale.ROOT);
        int bi = lower.indexOf("boundary=");
        if (bi < 0) return null;
        int start = bi + 9;
        if (start >= contentType.length()) return null;
        StringBuilder boundary = new StringBuilder();
        boolean quoted = contentType.charAt(start) == '"';
        if (quoted) start++;
        for (int i = start; i < contentType.length(); i++) {
            char c = contentType.charAt(i);
            if (quoted && c == '"') break;
            if (!quoted && (c == ';' || c == ' ' || c == '\t')) break;
            boundary.append(c);
        }
        return boundary.toString();
    }

    static class SimplePart implements Part {
        private final String name;
        private final String submittedFileName;
        private final String contentType;
        private final byte[] body;
        private final Map<String, List<String>> headers;

        SimplePart(String name, String submittedFileName, String contentType,
                   byte[] body, Map<String, List<String>> headers) {
            this.name = name != null ? name : "";
            this.submittedFileName = submittedFileName;
            this.contentType = contentType;
            this.body = body;
            this.headers = headers;
        }

        @Override public InputStream getInputStream() { return new ByteArrayInputStream(body); }
        @Override public String getContentType() { return contentType; }
        @Override public String getName() { return name; }
        @Override public String getSubmittedFileName() { return submittedFileName; }
        @Override public long getSize() { return body.length; }

        @Override
        public void write(String fileName) throws IOException {
            java.nio.file.Files.write(java.nio.file.Paths.get(fileName), body);
        }

        @Override
        public void delete() throws IOException {}

        @Override
        public String getHeader(String name) {
            List<String> vals = headers.get(name.toLowerCase(Locale.ROOT));
            return (vals != null && !vals.isEmpty()) ? vals.get(0) : null;
        }

        @Override
        public Collection<String> getHeaders(String name) {
            return headers.getOrDefault(name.toLowerCase(Locale.ROOT), Collections.emptyList());
        }

        @Override
        public Collection<String> getHeaderNames() {
            return headers.keySet();
        }
    }

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

    private static InputStream createDecompressedStream(HttpRequestData data) {
        byte[] body = data.getBody();
        if (body.length == 0) return new ByteArrayInputStream(body);
        String contentEncoding = data.getHeader("Content-Encoding");
        if (contentEncoding == null || contentEncoding.isEmpty()) return new ByteArrayInputStream(body);
        String enc = contentEncoding.toLowerCase(Locale.ROOT).trim();
        try {
            if (enc.contains("gzip") || enc.equals("x-gzip")) {
                GZIPInputStream gzipIn = new GZIPInputStream(new ByteArrayInputStream(body));
                ByteArrayOutputStream out = new ByteArrayOutputStream(body.length * 2);
                byte[] buf = new byte[8192];
                int n;
                while ((n = gzipIn.read(buf)) != -1) out.write(buf, 0, n);
                gzipIn.close();
                return new ByteArrayInputStream(out.toByteArray());
            }
            if (enc.contains("deflate")) {
                InflaterInputStream inflaterIn = new InflaterInputStream(new ByteArrayInputStream(body));
                ByteArrayOutputStream out = new ByteArrayOutputStream(body.length * 2);
                byte[] buf = new byte[8192];
                int n;
                while ((n = inflaterIn.read(buf)) != -1) out.write(buf, 0, n);
                inflaterIn.close();
                return new ByteArrayInputStream(out.toByteArray());
            }
        } catch (Exception e) {
        }
        return new ByteArrayInputStream(body);
    }

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
