package com.chua.starter.datalake.server;

import com.chua.common.support.network.protocol.ProtocolType;
import com.chua.common.support.network.protocol.filter.ServletFilter;
import com.chua.common.support.network.protocol.request.ServletRequest;
import com.chua.common.support.network.protocol.request.ServletResponse;
import com.chua.common.support.network.protocol.server.AbstractServletFilter;
import com.chua.common.support.network.protocol.server.ServletFilterChain;
import com.chua.common.support.network.protocol.server.ServletFilterConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据中台静态文件 ServletFilter
 * <p>
 * 在 MappingServletFilter 之后运行，为未匹配的请求提供 SPA 静态文件服务。
 * 优先级 250（MappingServletFilter 的优先级为 200）。
 * </p>
 *
 * @author CH
 */
public class DatalakeStaticFileFilter extends AbstractServletFilter implements ServletFilter {

    private static final String FILTER_NAME = "DatalakeStaticFileFilter";
    private static final int PRIORITY = 250;

    /** MIME 类型映射表，根据文件扩展名确定 Content-Type */
    private static final Map<String, String> MIME_TYPES = new ConcurrentHashMap<>();

    static {
        MIME_TYPES.put("html", "text/html; charset=utf-8");
        MIME_TYPES.put("css", "text/css; charset=utf-8");
        MIME_TYPES.put("js", "application/javascript; charset=utf-8");
        MIME_TYPES.put("mjs", "application/javascript; charset=utf-8");
        MIME_TYPES.put("json", "application/json");
        MIME_TYPES.put("png", "image/png");
        MIME_TYPES.put("jpg", "image/jpeg");
        MIME_TYPES.put("jpeg", "image/jpeg");
        MIME_TYPES.put("gif", "image/gif");
        MIME_TYPES.put("svg", "image/svg+xml");
        MIME_TYPES.put("ico", "image/x-icon");
        MIME_TYPES.put("woff", "font/woff");
        MIME_TYPES.put("woff2", "font/woff2");
        MIME_TYPES.put("ttf", "font/ttf");
        MIME_TYPES.put("eot", "application/vnd.ms-fontobject");
        MIME_TYPES.put("map", "application/json");
        MIME_TYPES.put("txt", "text/plain; charset=utf-8");
        MIME_TYPES.put("xml", "application/xml");
        MIME_TYPES.put("pdf", "application/pdf");
    }

    /** 静态文件根目录（自动探测或通过 init 参数指定） */
    private File baseDir;

    /** API 上下文路径（需剥离的前缀，例如 /v2/datalake） */
    private String contextPath = "";

    /** SPA fallback 入口文件名，默认为 index.html */
    private String indexFileName = "index.html";

    public DatalakeStaticFileFilter() {
    }

    /**
     * @param baseDir     静态文件根目录
     * @param contextPath API 上下文路径（将被剥离以匹配文件路径）
     */
    public DatalakeStaticFileFilter(File baseDir, String contextPath) {
        this.baseDir = baseDir;
        this.contextPath = contextPath != null ? contextPath : "";
    }

    @Override
    public void init(ServletFilterConfig config) {
        if (config != null) {
            String baseDirStr = config.getInitParameter("baseDir");
            if (baseDirStr != null) {
                this.baseDir = new File(baseDirStr);
            }
            String ctxPath = config.getInitParameter("contextPath");
            if (ctxPath != null) {
                this.contextPath = ctxPath;
            }
            String index = config.getInitParameter("indexFile");
            if (index != null) {
                this.indexFileName = index;
            }
        }

        if (baseDir == null) {
            // 自动探测: 尝试多个可能的前端构建输出路径
            baseDir = autoDetectBaseDir();
        }

        if (baseDir != null && baseDir.exists()) {
            System.out.println("[DatalakeStaticFileFilter] 静态文件目录: " + baseDir.getAbsolutePath());
        } else {
            System.out.println("[DatalakeStaticFileFilter] 静态文件目录不存在，静态文件服务跳过: " +
                    (baseDir != null ? baseDir.getAbsolutePath() : "null"));
        }
    }

    private File autoDetectBaseDir() {
        String[] candidates = {
                "datalake-ui",
                "test/datalake/dist",
                "../vue-support-parent-starter/test/datalake/dist",
                "../vue-support-parent-starter/pages/datalake/dist",
        };
        File cwd = new File(System.getProperty("user.dir"));
        for (String candidate : candidates) {
            File dir = new File(cwd, candidate);
            if (dir.exists() && dir.isDirectory()) {
                return dir;
            }
            // 尝试从模块目录查找
            File fromModule = new File(cwd, "spring-api-support-datalake-starter/" + candidate);
            if (fromModule.exists() && fromModule.isDirectory()) {
                return fromModule;
            }
        }
        return null;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, ServletFilterChain chain) throws Exception {
        // 如果响应已提交或已有状态码，跳过
        if (response.isCommitted() || response.getStatusCode() != 0) {
            chain.doFilter(request, response);
            return;
        }

        // 静态文件目录不可用时跳过
        if (baseDir == null || !baseDir.exists()) {
            chain.doFilter(request, response);
            return;
        }

        // 只处理 GET 请求
        String method = request.getMethod();
        if (!"GET".equalsIgnoreCase(method) && !"HEAD".equalsIgnoreCase(method)) {
            chain.doFilter(request, response);
            return;
        }

        String requestPath = request.getPath();
        // 剥离上下文路径
        if (!contextPath.isEmpty() && requestPath.startsWith(contextPath)) {
            requestPath = requestPath.substring(contextPath.length());
        }
        // 去掉尾部 /
        if (requestPath.endsWith("/") && requestPath.length() > 1) {
            requestPath = requestPath.substring(0, requestPath.length() - 1);
        }
        if (requestPath.isEmpty()) {
            requestPath = "/";
        }

        // 尝试查找文件
        File file = resolveFile(requestPath);
        if (file != null && file.isFile()) {
            serveFile(file, request, response);
            return;
        }

        // SPA fallback: 非文件路径 → 返回 index.html
        if (!requestPath.contains(".")) {
            File indexFile = new File(baseDir, indexFileName);
            if (indexFile.isFile()) {
                serveFile(indexFile, request, response);
                return;
            }
        }

        // 无匹配，继续过滤器链（最终走默认 200 JSON 响应）
        chain.doFilter(request, response);
    }

    /**
     * 解析请求路径为实际文件，带路径穿越安全校验
     *
     * @param requestPath HTTP 请求路径
     * @return 匹配的文件对象，不存在或路径穿越时返回 null
     */
    private File resolveFile(String requestPath) {
        if ("/".equals(requestPath)) {
            return new File(baseDir, indexFileName);
        }

        // 去除前导 /
        String relativePath = requestPath.startsWith("/") ? requestPath.substring(1) : requestPath;

        // 安全校验: 防止路径穿越
        File file = new File(baseDir, relativePath);
        try {
            String canonicalPath = file.getCanonicalPath();
            String baseCanonical = baseDir.getCanonicalPath();
            if (!canonicalPath.startsWith(baseCanonical + File.separator) && !canonicalPath.equals(baseCanonical)) {
                return null;
            }
        } catch (IOException e) {
            return null;
        }

        return file.exists() ? file : null;
    }

    /**
     * 读取文件内容并写入 HTTP 响应
     *
     * @param file     静态文件
     * @param request  HTTP 请求
     * @param response HTTP 响应
     */
    private void serveFile(File file, ServletRequest request, ServletResponse response) throws IOException {
        String extension = getExtension(file.getName());
        String contentType = MIME_TYPES.getOrDefault(extension, "application/octet-stream");

        byte[] content = Files.readAllBytes(file.toPath());

        response.setStatusCode(200);
        response.setContentType(contentType);
        response.setBody(content);
        response.addHeader("Content-Length", String.valueOf(content.length));
        response.addHeader("Cache-Control", isCacheable(extension) ? "public, max-age=3600" : "no-cache");
    }

    /**
     * 获取文件扩展名
     *
     * @param name 文件名
     * @return 小写扩展名，无扩展名时返回空字符串
     */
    private String getExtension(String name) {
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(dot + 1).toLowerCase() : "";
    }

    /**
     * 判断文件扩展名是否可缓存（静态资源如 JS/CSS/图片/字体）
     *
     * @param ext 文件扩展名
     * @return 是否可缓存
     */
    private boolean isCacheable(String ext) {
        return "js".equals(ext) || "css".equals(ext) || "png".equals(ext)
                || "jpg".equals(ext) || "jpeg".equals(ext) || "svg".equals(ext)
                || "ico".equals(ext) || "woff".equals(ext) || "woff2".equals(ext)
                || "ttf".equals(ext) || "eot".equals(ext);
    }

    @Override
    public String getFilterName() {
        return FILTER_NAME;
    }

    @Override
    public int getOrder() {
        return PRIORITY;
    }

    @Override
    public ProtocolType[] getSupportedProtocolTypes() {
        return new ProtocolType[]{ProtocolType.HTTP, ProtocolType.HTTPS};
    }
}
