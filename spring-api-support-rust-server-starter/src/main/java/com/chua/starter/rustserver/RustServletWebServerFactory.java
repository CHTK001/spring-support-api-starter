package com.chua.starter.rustserver;

import jakarta.servlet.*;
import jakarta.servlet.descriptor.JspConfigDescriptor;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPOutputStream;

/**
 * 基于 Rust (tokio+hyper) + Panama FFI 的 {@link ServletWebServerFactory} 实现。
 *
 * <p>Rust 管理 TCP 连接和 HTTP 协议解析，Java 侧专注业务处理。
 * 请求到达时同步回调 Java，通过简易 {@link ServletContext} 分发给 Spring 的 {@code DispatcherServlet}。</p>
 */
public class RustServletWebServerFactory implements ServletWebServerFactory {

    private static final Logger log = LoggerFactory.getLogger(RustServletWebServerFactory.class);
    private int port = 8080;
    private int responseCapacity = 64 * 1024;
    private int workerThreads = 0;
    private int sessionTimeout = 1800;
    private String sessionCookieName = "JSESSIONID";
    private boolean accessLogEnabled = true;
    private int shutdownTimeout = 30;
    private boolean sslEnabled;
    private String sslCertPath;
    private String sslKeyPath;

    @Override
    public WebServer getWebServer(ServletContextInitializer... initializers) {
        // 1. 初始化简易 ServletContext
        SimpleServletContext servletContext = new SimpleServletContext();
        servletContext.sessionManager = new SessionManager(servletContext);
        servletContext.sessionManager.setDefaultMaxInactiveInterval(sessionTimeout);
        servletContext.sessionManager.getCookieConfig().setName(sessionCookieName);

        for (ServletContextInitializer initializer : initializers) {
            try {
                initializer.onStartup(servletContext);
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize servlet context", e);
            }
        }

        // 打印已注册的 Servlet
        servletContext.registrations.forEach((name, reg) ->
                log.info("[RustServer] Servlet '{}' = {} (mappings: {})",
                        name, reg.servlet.getClass().getSimpleName(), reg.mappings));

        // 注册静态资源 Servlet 作为 fallback
        boolean hasDefault = servletContext.registrations.values().stream()
                .anyMatch(reg -> reg.mappings.contains("/*"));
        if (!hasDefault) {
            StaticResourceServlet staticServlet = new StaticResourceServlet();
            servletContext.addServlet("staticResourceServlet", staticServlet).addMapping("/*");
        }

        // 2. 创建 Rust WebServer，提供完整请求处理逻辑
        int shutdownTimeoutMs = shutdownTimeout * 1000;
        return new RustWebServer(port, responseCapacity, request -> {
            long startTime = System.nanoTime();

            // 创建 Servlet 请求/响应包装
            RustHttpServletRequest servletRequest =
                    new RustHttpServletRequest(request, servletContext, servletContext.sessionManager);
            RustHttpServletResponse servletResponse =
                    new RustHttpServletResponse(servletContext.sessionManager);
            servletResponse.setRequest(servletRequest);

            // 查找匹配的 Servlet
            Servlet servlet = findServlet(servletContext, request.getPath());
            if (servlet == null) {
                log.warn("[RustServer] No servlet found for path: {}", request.getPath());
                servletResponse.sendError(404, "Not Found");
            } else {
                try {
                    // 查找匹配的 Filter，构建 FilterChain
                    List<Filter> filters = findFilters(servletContext, request.getPath());
                    if (filters.isEmpty()) {
                        servlet.service(servletRequest, servletResponse);
                    } else {
                        FilterChain chain = new SimpleFilterChain(filters, servlet,
                                servletRequest, servletResponse);
                        chain.doFilter(servletRequest, servletResponse);
                    }
                } catch (Exception e) {
                    log.error("[RustServer] Servlet error: {} {}", request.getPath(), e.getMessage(), e);
                    servletResponse = new RustHttpServletResponse(servletContext.sessionManager);
                    servletResponse.sendError(500, e.getMessage());
                }
            }

            // 写入 session cookie
            if (servletResponse != null) {
                servletResponse.flushSessionCookieIfNeeded();
            }

            // 序列化响应回 Rust 缓冲区
            if ("HEAD".equalsIgnoreCase(request.getMethod())) {
                try { if (servletResponse != null) servletResponse.resetBuffer(); } catch (IllegalStateException ignored) {}
                if (servletResponse != null) {
                    servletResponse.setContentLength(0);
                    servletResponse.setHeader("Content-Length", "0");
                }
            } else {
                if (servletResponse != null) {
                    tryCompressResponse(request, servletResponse);
                }
            }

            if (servletResponse != null) {
                servletResponse.writeTo(request.getResponseData(), request.getResponseCapacity());
            }

            // 访问日志
            if (accessLogEnabled) {
                long durationMs = (System.nanoTime() - startTime) / 1_000_000;
                log.info("[RustAccess] {} {} -> {} ({}ms) [{}]",
                        request.getMethod(), request.getPath(),
                        servletResponse != null ? servletResponse.getStatus() : 500,
                        durationMs, "127.0.0.1");
            }
        }, shutdownTimeoutMs, workerThreads, sslEnabled, sslCertPath, sslKeyPath);
    }

    // ── Servlet 路由 ──

    static Servlet findServlet(SimpleServletContext ctx, String path) {
        if (path == null) path = "/";

        ServletRegistrationImpl wildcardReg = null;
        ServletRegistrationImpl defaultReg = null;
        ServletRegistrationImpl rootReg = null;

        for (ServletRegistrationImpl reg : ctx.registrations.values()) {
            for (String mapping : reg.mappings) {
                if (mapping == null) continue;

                if (mapping.equals(path)) {
                    initServletIfNeeded(reg, ctx);
                    return reg.servlet;
                }

                if (mapping.equals("/*")) {
                    defaultReg = reg;
                    continue;
                }

                if (mapping.equals("/")) {
                    rootReg = reg;
                    continue;
                }

                if (mapping.endsWith("/*")) {
                    String prefix = mapping.substring(0, mapping.length() - 2);
                    if (path.startsWith(prefix)) {
                        wildcardReg = reg;
                    }
                    continue;
                }

                if (mapping.startsWith("*.")) {
                    String ext = mapping.substring(1);
                    if (path.endsWith(ext)) {
                        wildcardReg = reg;
                    }
                }
            }
        }

        ServletRegistrationImpl result = wildcardReg != null ? wildcardReg :
                (rootReg != null ? rootReg : defaultReg);
        if (result != null) {
            initServletIfNeeded(result, ctx);
        }
        return result != null ? result.servlet : null;
    }

    private static void initServletIfNeeded(ServletRegistrationImpl reg, ServletContext ctx) {
        if (!reg.initialized) {
            try {
                reg.servlet.init(new SimpleServletConfig(reg, ctx));
                reg.initialized = true;
            } catch (ServletException e) {
                log.error("[RustServer] Failed to init servlet '{}': {}", reg.name, e.getMessage());
            }
        }
    }

    // ── 响应压缩 ──

    private static void tryCompressResponse(HttpRequestData request, RustHttpServletResponse response) {
        int status = response.getStatus();
        if (status == 204 || status == 304) return;
        if (response.getBodySize() < 256) return;
        String acceptEncoding = request.getHeader("Accept-Encoding");
        if (acceptEncoding == null || !acceptEncoding.toLowerCase(Locale.ROOT).contains("gzip")) {
            return;
        }
        response.compressBody();
    }

    // ── getter/setter ──

    public void setPort(int port) { this.port = port; }
    public void setResponseCapacity(int responseCapacity) { this.responseCapacity = responseCapacity; }
    public void setWorkerThreads(int workerThreads) { this.workerThreads = workerThreads; }
    public void setSessionTimeout(int sessionTimeout) { this.sessionTimeout = sessionTimeout; }
    public void setSessionCookieName(String sessionCookieName) { this.sessionCookieName = sessionCookieName; }
    public void setAccessLogEnabled(boolean accessLogEnabled) { this.accessLogEnabled = accessLogEnabled; }
    public void setShutdownTimeout(int shutdownTimeout) { this.shutdownTimeout = shutdownTimeout; }
    public void setSslEnabled(boolean sslEnabled) { this.sslEnabled = sslEnabled; }
    public void setSslCertPath(String sslCertPath) { this.sslCertPath = sslCertPath; }
    public void setSslKeyPath(String sslKeyPath) { this.sslKeyPath = sslKeyPath; }

    // ═══════════════════════════════════════════
    //  ServletContext 简易实现
    // ═══════════════════════════════════════════

    static class SimpleServletContext implements ServletContext {
        private static final org.slf4j.Logger innerLog = org.slf4j.LoggerFactory.getLogger(SimpleServletContext.class);
        final Map<String, Object> attributes = new ConcurrentHashMap<>();
        final Map<String, String> initParameters = new ConcurrentHashMap<>();
        final Map<String, ServletRegistrationImpl> registrations = new ConcurrentHashMap<>();
        final Map<String, FilterRegistrationImpl> filterRegistrations = new ConcurrentHashMap<>();

        SessionManager sessionManager;

        @Override
        public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
            ServletRegistrationImpl reg = new ServletRegistrationImpl(servletName, servlet);
            registrations.put(servletName, reg);
            return reg;
        }

        @Override
        public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
            try {
                return addServlet(servletName, servletClass.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                throw new RuntimeException("Cannot instantiate servlet: " + servletClass, e);
            }
        }

        @Override
        public ServletRegistration.Dynamic addServlet(String servletName, String className) {
            log.warn("addServlet by className not implemented: {}", className);
            return null;
        }

        @Override
        public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
            try { return clazz.getDeclaredConstructor().newInstance(); }
            catch (Exception e) { throw new ServletException(e); }
        }

        @Override
        public ServletRegistration getServletRegistration(String servletName) {
            return registrations.get(servletName);
        }

        @Override
        public Map<String, ? extends ServletRegistration> getServletRegistrations() {
            return Collections.unmodifiableMap(registrations);
        }

        // ── Filter 支持 ──

        @Override
        public jakarta.servlet.FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
            FilterRegistrationImpl reg = new FilterRegistrationImpl(filterName, filter);
            filterRegistrations.put(filterName, reg);
            return reg;
        }

        @Override
        public jakarta.servlet.FilterRegistration.Dynamic addFilter(String filterName, String className) {
            log.warn("addFilter by className not implemented: {}", className);
            return null;
        }

        @Override
        public jakarta.servlet.FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
            try {
                return addFilter(filterName, filterClass.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                throw new RuntimeException("Cannot instantiate filter: " + filterClass, e);
            }
        }

        @Override
        public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
            try { return clazz.getDeclaredConstructor().newInstance(); }
            catch (Exception e) { throw new ServletException(e); }
        }

        @Override
        public FilterRegistration getFilterRegistration(String filterName) {
            return filterRegistrations.get(filterName);
        }

        @Override
        public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
            return Collections.unmodifiableMap(filterRegistrations);
        }

        @Override public String getContextPath() { return ""; }
        @Override public ServletContext getContext(String s) { return this; }
        @Override public int getMajorVersion() { return 6; }
        @Override public int getMinorVersion() { return 0; }
        @Override public int getEffectiveMajorVersion() { return 6; }
        @Override public int getEffectiveMinorVersion() { return 0; }
        @Override public String getServerInfo() { return "RustServer/1.0 (tokio+hyper)"; }
        @Override public String getServletContextName() { return "RustServer"; }
        @Override public String getVirtualServerName() { return "RustServer/1.0"; }

        @Override
        public boolean setInitParameter(String name, String value) {
            return initParameters.put(name, value) == null;
        }

        @Override
        public String getInitParameter(String name) { return initParameters.get(name); }

        @Override
        public Enumeration<String> getInitParameterNames() {
            return Collections.enumeration(initParameters.keySet());
        }

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

        @Override
        public void removeAttribute(String name) { attributes.remove(name); }

        @Override
        public String getMimeType(String file) {
            if (file == null) return null;
            String lower = file.toLowerCase(Locale.ROOT);
            if (lower.endsWith(".html")||lower.endsWith(".htm")) return "text/html";
            if (lower.endsWith(".json")) return "application/json";
            if (lower.endsWith(".js"))   return "application/javascript";
            if (lower.endsWith(".css"))  return "text/css";
            if (lower.endsWith(".xml"))  return "application/xml";
            if (lower.endsWith(".png"))  return "image/png";
            if (lower.endsWith(".jpg")||lower.endsWith(".jpeg")) return "image/jpeg";
            if (lower.endsWith(".gif"))  return "image/gif";
            if (lower.endsWith(".svg")||lower.endsWith(".svgz")) return "image/svg+xml";
            if (lower.endsWith(".ico"))  return "image/x-icon";
            if (lower.endsWith(".webp")) return "image/webp";
            if (lower.endsWith(".woff")) return "font/woff";
            if (lower.endsWith(".woff2")) return "font/woff2";
            if (lower.endsWith(".ttf"))  return "font/ttf";
            if (lower.endsWith(".eot"))  return "application/vnd.ms-fontobject";
            if (lower.endsWith(".pdf"))  return "application/pdf";
            if (lower.endsWith(".zip"))  return "application/zip";
            if (lower.endsWith(".txt"))  return "text/plain";
            if (lower.endsWith(".mp4"))  return "video/mp4";
            if (lower.endsWith(".webm")) return "video/webm";
            if (lower.endsWith(".mp3"))  return "audio/mpeg";
            return "application/octet-stream";
        }

        @Override public URL getResource(String path) throws MalformedURLException { return null; }
        @Override public InputStream getResourceAsStream(String path) { return null; }
        @Override public Set<String> getResourcePaths(String path) { return null; }
        @Override public String getRealPath(String path) { return null; }
        @Override public RequestDispatcher getRequestDispatcher(String path) { return null; }
        @Override public RequestDispatcher getNamedDispatcher(String name) { return null; }
        @Override public void log(String msg) { innerLog.debug("[ServletContext] {}", msg); }
        @Override public void log(String msg, Throwable t) { innerLog.debug("[ServletContext] {}", msg, t); }

        @Override public SessionCookieConfig getSessionCookieConfig() {
            return sessionManager != null ? sessionManager.getCookieConfig() : null;
        }
        @Override public void setSessionTrackingModes(Set<SessionTrackingMode> set) {}
        @Override public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
            return EnumSet.of(SessionTrackingMode.COOKIE);
        }
        @Override public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
            return EnumSet.of(SessionTrackingMode.COOKIE);
        }
        @Override public int getSessionTimeout() {
            return sessionManager != null ? sessionManager.getCookieConfig().getMaxAge() : 30;
        }
        @Override public void setSessionTimeout(int i) {
            if (sessionManager != null) {
                sessionManager.setDefaultMaxInactiveInterval(i * 60);
            }
        }
        @Override public JspConfigDescriptor getJspConfigDescriptor() { return null; }
        @Override public ServletRegistration.Dynamic addJspFile(String s, String s1) { return null; }

        @Override public String getRequestCharacterEncoding() { return "UTF-8"; }
        @Override public void setRequestCharacterEncoding(String s) {}
        @Override public String getResponseCharacterEncoding() { return "UTF-8"; }
        @Override public void setResponseCharacterEncoding(String s) {}
        @Override public ClassLoader getClassLoader() { return getClass().getClassLoader(); }

        @Override public void addListener(Class<? extends EventListener> aClass) {}
        @Override public void addListener(String s) {}
        @Override public <T extends EventListener> void addListener(T t) {}
        @Override public <T extends EventListener> T createListener(Class<T> aClass) { return null; }
        @Override public void declareRoles(String... roles) {}
    }

    // ═══════════════════════════════════════════
    //  ServletConfig 简易实现
    // ═══════════════════════════════════════════

    static class SimpleServletConfig implements ServletConfig {
        private final ServletRegistrationImpl reg;
        private final ServletContext context;

        SimpleServletConfig(ServletRegistrationImpl reg, ServletContext context) {
            this.reg = reg;
            this.context = context;
        }

        @Override public String getServletName() { return reg.name; }
        @Override public ServletContext getServletContext() { return context; }

        @Override
        public String getInitParameter(String name) {
            return reg.initParameters.get(name);
        }

        @Override
        public Enumeration<String> getInitParameterNames() {
            return Collections.enumeration(reg.initParameters.keySet());
        }
    }

    // ═══════════════════════════════════════════
    //  ServletRegistration.Dynamic 实现
    // ═══════════════════════════════════════════

    static class ServletRegistrationImpl implements ServletRegistration.Dynamic {
        final String name;
        final Servlet servlet;
        final Set<String> mappings = new LinkedHashSet<>();
        final Map<String, String> initParameters = new HashMap<>();
        boolean asyncSupported = false;
        boolean initialized = false;

        ServletRegistrationImpl(String name, Servlet servlet) {
            this.name = name;
            this.servlet = servlet;
        }

        @Override
        public Set<String> addMapping(String... urlPatterns) {
            Collections.addAll(mappings, urlPatterns);
            return Collections.unmodifiableSet(mappings);
        }

        @Override
        public Collection<String> getMappings() { return Collections.unmodifiableCollection(mappings); }
        @Override public String getRunAsRole() { return null; }
        @Override public String getName() { return name; }
        @Override public String getClassName() { return servlet.getClass().getName(); }

        @Override
        public boolean setInitParameter(String name, String value) {
            return initParameters.put(name, value) == null;
        }

        @Override public String getInitParameter(String name) { return initParameters.get(name); }

        @Override
        public Set<String> setInitParameters(Map<String, String> params) {
            initParameters.putAll(params);
            return Collections.emptySet();
        }

        @Override public Map<String, String> getInitParameters() {
            return Collections.unmodifiableMap(initParameters);
        }

        @Override public void setLoadOnStartup(int i) {}
        @Override public void setMultipartConfig(MultipartConfigElement e) {}
        @Override public void setRunAsRole(String s) {}
        @Override public void setAsyncSupported(boolean b) { this.asyncSupported = b; }

        @Override
        public Set<String> setServletSecurity(ServletSecurityElement e) {
            return Collections.emptySet();
        }
    }

    // ═══════════════════════════════════════════
    //  FilterRegistration 实现
    // ═══════════════════════════════════════════

    static class FilterRegistrationImpl implements FilterRegistration.Dynamic {
        final String name;
        final Filter filter;
        final Set<String> urlPatterns = new LinkedHashSet<>();
        final Map<String, String> initParameters = new HashMap<>();
        boolean asyncSupported = false;
        boolean initialized = false;

        FilterRegistrationImpl(String name, Filter filter) {
            this.name = name;
            this.filter = filter;
        }

        @Override public String getName() { return name; }
        @Override public String getClassName() { return filter.getClass().getName(); }

        @Override
        public void addMappingForServletNames(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... servletNames) {}

        @Override
        public void addMappingForUrlPatterns(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... urlPatterns) {
            Collections.addAll(this.urlPatterns, urlPatterns);
        }

        @Override
        public Collection<String> getServletNameMappings() { return Collections.emptyList(); }
        @Override public Collection<String> getUrlPatternMappings() { return Collections.unmodifiableCollection(urlPatterns); }

        @Override
        public boolean setInitParameter(String name, String value) {
            return initParameters.put(name, value) == null;
        }

        @Override public String getInitParameter(String name) { return initParameters.get(name); }

        @Override
        public Set<String> setInitParameters(Map<String, String> params) {
            initParameters.putAll(params);
            return Collections.emptySet();
        }

        @Override public Map<String, String> getInitParameters() {
            return Collections.unmodifiableMap(initParameters);
        }

        @Override public void setAsyncSupported(boolean isAsyncSupported) { this.asyncSupported = isAsyncSupported; }
    }

    // ═══════════════════════════════════════════
    //  FilterChain 实现
    // ═══════════════════════════════════════════

    static class SimpleFilterChain implements FilterChain {
        private final List<Filter> filters;
        private int index = 0;
        private final Servlet servlet;
        private final HttpServletRequest request;
        private final HttpServletResponse response;

        SimpleFilterChain(List<Filter> filters, Servlet servlet,
                          HttpServletRequest request, HttpServletResponse response) {
            this.filters = filters;
            this.servlet = servlet;
            this.request = request;
            this.response = response;
        }

        @Override
        public void doFilter(ServletRequest req, ServletResponse res) throws IOException, ServletException {
            if (index < filters.size()) {
                filters.get(index++).doFilter(req, res, this);
            } else {
                servlet.service(req, res);
            }
        }
    }

    static List<Filter> findFilters(SimpleServletContext ctx, String path) {
        List<Filter> result = new ArrayList<>();
        for (FilterRegistrationImpl reg : ctx.filterRegistrations.values()) {
            for (String pattern : reg.urlPatterns) {
                if (matchFilterPattern(pattern, path)) {
                    initFilterIfNeeded(reg, ctx);
                    result.add(reg.filter);
                    break;
                }
            }
        }
        return result;
    }

    private static boolean matchFilterPattern(String pattern, String path) {
        if ("/*".equals(pattern)) return true;
        if (pattern.endsWith("/*")) {
            return path.startsWith(pattern.substring(0, pattern.length() - 1));
        }
        if (pattern.startsWith("*.")) {
            return path.endsWith(pattern.substring(1));
        }
        return pattern.equals(path);
    }

    private static void initFilterIfNeeded(FilterRegistrationImpl reg, ServletContext ctx) {
        if (!reg.initialized) {
            try {
                reg.filter.init(new SimpleFilterConfig(reg, ctx));
                reg.initialized = true;
            } catch (ServletException e) {
                log.error("[RustServer] Failed to init filter '{}': {}", reg.name, e.getMessage());
            }
        }
    }

    static class SimpleFilterConfig implements FilterConfig {
        private final FilterRegistrationImpl reg;
        private final ServletContext context;

        SimpleFilterConfig(FilterRegistrationImpl reg, ServletContext context) {
            this.reg = reg;
            this.context = context;
        }

        @Override public String getFilterName() { return reg.name; }
        @Override public ServletContext getServletContext() { return context; }

        @Override
        public String getInitParameter(String name) {
            return reg.initParameters.get(name);
        }

        @Override
        public Enumeration<String> getInitParameterNames() {
            return Collections.enumeration(reg.initParameters.keySet());
        }
    }
}
