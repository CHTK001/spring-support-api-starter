package com.chua.starter.rustserver;

import jakarta.servlet.*;
import jakarta.servlet.descriptor.JspConfigDescriptor;
import jakarta.servlet.http.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;

import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于 Rust (tokio+hyper) + Panama FFI 的 {@link ServletWebServerFactory} 实现。
 *
 * <p>Rust 管理 TCP 连接和 HTTP 协议解析，Java 侧专注业务处理。
 * 请求到达时同步回调 Java，通过简易 {@link ServletContext} 分发给 Spring 的 {@code DispatcherServlet}。</p>
 *
 * <h3>使用方式</h3>
 * <pre>
 * server:
 *   rust:
 *     enabled: true
 *     response-buffer: 524288   # 默认 512KB
 * </pre>
 */
@Slf4j
public class RustServletWebServerFactory implements ServletWebServerFactory {

    private int port = 8080;
    private int responseCapacity = 512 * 1024;

    @Override
    public WebServer getWebServer(ServletContextInitializer... initializers) {
        // 1. 初始化简易 ServletContext
        SimpleServletContext servletContext = new SimpleServletContext();
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

        // 2. 创建 Rust WebServer，提供完整请求处理逻辑
        return new RustWebServer(port, responseCapacity, request -> {
            // 创建 Servlet 请求/响应包装
            RustHttpServletRequest servletRequest =
                    new RustHttpServletRequest(request, servletContext);
            RustHttpServletResponse servletResponse =
                    new RustHttpServletResponse();

            // 查找匹配的 Servlet
            Servlet servlet = findServlet(servletContext, request.getPath());
            if (servlet == null) {
                log.warn("[RustServer] No servlet found for path: {}", request.getPath());
                servletResponse.sendError(404, "Not Found");
            } else {
                try {
                    servlet.service(servletRequest, servletResponse);
                } catch (Exception e) {
                    log.error("[RustServer] Servlet error: {} {}", request.getPath(), e.getMessage(), e);
                    servletResponse = new RustHttpServletResponse();
                    servletResponse.sendError(500, e.getMessage());
                }
            }

            // 序列化响应回 Rust 缓冲区
            servletResponse.writeTo(request.getResponseData(), request.getResponseCapacity());
        });
    }

    // ── Servlet 路由 ──

    /**
     * 根据请求路径查找匹配的 Servlet。
     * 优先级：精确匹配 > 前缀匹配 ("/path/*") > 通配匹配 ("/*")
     */
    static Servlet findServlet(SimpleServletContext ctx, String path) {
        if (path == null) path = "/";

        ServletRegistrationImpl wildcardReg = null;
        ServletRegistrationImpl defaultReg = null;

        for (ServletRegistrationImpl reg : ctx.registrations.values()) {
            for (String mapping : reg.mappings) {
                if (mapping == null) continue;

                if (mapping.equals(path)) {
                    // 精确匹配 —— 直接返回
                    initServletIfNeeded(reg, ctx);
                    return reg.servlet;
                }

                if (mapping.equals("/*")) {
                    defaultReg = reg;
                    continue;
                }

                if (mapping.endsWith("/*")) {
                    String prefix = mapping.substring(0, mapping.length() - 2);
                    if (path.startsWith(prefix)) {
                        wildcardReg = reg;
                        // 继续找更精确的
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

        // 没有精确/前缀匹配，用 /* 兜底
        ServletRegistrationImpl result = wildcardReg != null ? wildcardReg : defaultReg;
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

    // ── getter/setter ──

    public void setPort(int port) { this.port = port; }
    public void setResponseCapacity(int responseCapacity) { this.responseCapacity = responseCapacity; }

    // ═══════════════════════════════════════════
    //  ServletContext 简易实现
    // ═══════════════════════════════════════════

    static class SimpleServletContext implements ServletContext {
        private static final org.slf4j.Logger innerLog = org.slf4j.LoggerFactory.getLogger(SimpleServletContext.class);
        final Map<String, Object> attributes = new ConcurrentHashMap<>();
        final Map<String, String> initParameters = new ConcurrentHashMap<>();
        final Map<String, ServletRegistrationImpl> registrations = new ConcurrentHashMap<>();

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

        @Override
        public jakarta.servlet.FilterRegistration.Dynamic addFilter(String s, String s1) { return null; }
        @Override public jakarta.servlet.FilterRegistration.Dynamic addFilter(String s, Filter filter) { return null; }
        @Override public jakarta.servlet.FilterRegistration.Dynamic addFilter(String s, Class<? extends Filter> aClass) { return null; }
        @Override public <T extends Filter> T createFilter(Class<T> aClass) { return null; }
        @Override public FilterRegistration getFilterRegistration(String s) { return null; }
        @Override public Map<String, ? extends FilterRegistration> getFilterRegistrations() { return Collections.emptyMap(); }

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
            if (file.endsWith(".html")||file.endsWith(".htm")) return "text/html";
            if (file.endsWith(".json")) return "application/json";
            if (file.endsWith(".js"))   return "application/javascript";
            if (file.endsWith(".css"))  return "text/css";
            if (file.endsWith(".xml"))  return "application/xml";
            if (file.endsWith(".png"))  return "image/png";
            if (file.endsWith(".jpg")||file.endsWith(".jpeg")) return "image/jpeg";
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

        @Override public SessionCookieConfig getSessionCookieConfig() { return null; }
        @Override public void setSessionTrackingModes(Set<SessionTrackingMode> set) {}
        @Override public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
            return EnumSet.of(SessionTrackingMode.COOKIE);
        }
        @Override public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
            return EnumSet.of(SessionTrackingMode.COOKIE);
        }
        @Override public int getSessionTimeout() { return 30; }
        @Override public void setSessionTimeout(int i) {}
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
}
