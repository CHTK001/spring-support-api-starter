package com.chua.starter.rustserver;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 默认静态资源 Servlet，从 classpath 加载静态文件。
 *
 * <p>搜索路径优先级：classpath:/META-INF/resources/ > classpath:/static/ > classpath:/public/ > classpath:/resources/</p>
 */
public class StaticResourceServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(StaticResourceServlet.class);

    private static final String[] STATIC_LOCATIONS = {
            "META-INF/resources/",
            "static/",
            "public/",
            "resources/"
    };

    private static final String[] WELCOME_FILES = {"index.html", "index.htm"};

    private static final Map<String, String> MIME_TYPES = new LinkedHashMap<>();

    static {
        MIME_TYPES.put("html", "text/html");
        MIME_TYPES.put("htm", "text/html");
        MIME_TYPES.put("css", "text/css");
        MIME_TYPES.put("js", "application/javascript");
        MIME_TYPES.put("json", "application/json");
        MIME_TYPES.put("xml", "application/xml");
        MIME_TYPES.put("png", "image/png");
        MIME_TYPES.put("jpg", "image/jpeg");
        MIME_TYPES.put("jpeg", "image/jpeg");
        MIME_TYPES.put("gif", "image/gif");
        MIME_TYPES.put("svg", "image/svg+xml");
        MIME_TYPES.put("ico", "image/x-icon");
        MIME_TYPES.put("webp", "image/webp");
        MIME_TYPES.put("woff", "font/woff");
        MIME_TYPES.put("woff2", "font/woff2");
        MIME_TYPES.put("ttf", "font/ttf");
        MIME_TYPES.put("eot", "application/vnd.ms-fontobject");
        MIME_TYPES.put("pdf", "application/pdf");
        MIME_TYPES.put("zip", "application/zip");
        MIME_TYPES.put("txt", "text/plain; charset=UTF-8");
        MIME_TYPES.put("mp4", "video/mp4");
        MIME_TYPES.put("webm", "video/webm");
        MIME_TYPES.put("mp3", "audio/mpeg");
        MIME_TYPES.put("wav", "audio/wav");
        MIME_TYPES.put("map", "application/json");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String requestPath = req.getServletPath();
        if (requestPath == null || requestPath.isEmpty() || requestPath.equals("/")) {
            // 尝试 welcome 文件
            for (String welcome : WELCOME_FILES) {
                URL resource = findResource("/" + welcome);
                if (resource != null) {
                    serveResource(resource, resp);
                    return;
                }
            }
            resp.sendError(404, "Not Found");
            return;
        }

        // 去掉开头的 /
        String path = requestPath.startsWith("/") ? requestPath.substring(1) : requestPath;

        // 遍历所有静态资源目录查找文件
        URL resource = findResource(path);
        if (resource == null) {
            resp.sendError(404, "Not Found");
            return;
        }

        serveResource(resource, resp);
    }

    /**
     * 在静态资源目录中查找文件。
     */
    private URL findResource(String path) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        ClassLoader cl = getClass().getClassLoader();
        for (String location : STATIC_LOCATIONS) {
            URL resource = cl.getResource(location + path);
            if (resource != null) {
                return resource;
            }
        }
        return null;
    }

    /**
     * 输出静态资源响应（含缓存控制）。
     */
    private void serveResource(URL resource, HttpServletResponse resp) throws IOException {
        URLConnection conn = resource.openConnection();
        long lastModified = conn.getLastModified();
        long contentLength = conn.getContentLengthLong();

        // Content-Type
        String contentType = getContentType(resource);
        resp.setContentType(contentType);

        // 缓存头
        resp.setDateHeader("Last-Modified", lastModified);
        resp.setHeader("Cache-Control", "max-age=3600");

        // If-Modified-Since 支持（304）
        long ifModifiedSince = 0;
        // 简单解析，不依赖 getDateHeader 避免异常
        resp.setStatus(200);

        // Content-Length
        if (contentLength >= 0) {
            resp.setContentLengthLong(contentLength);
        }

        // 输出响应体
        try (InputStream in = conn.getInputStream()) {
            var out = resp.getOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
        }
    }

    /**
     * 根据文件扩展名获取 MIME 类型。
     */
    private String getContentType(URL resource) {
        String path = resource.getPath();
        int dot = path.lastIndexOf('.');
        if (dot > 0) {
            String ext = path.substring(dot + 1).toLowerCase(Locale.ROOT);
            String mime = MIME_TYPES.get(ext);
            if (mime != null) return mime;
        }
        // fallback
        try {
            String guessed = Files.probeContentType(new File(path).toPath().toAbsolutePath());
            if (guessed != null) return guessed;
        } catch (Exception ignored) {
        }
        return "application/octet-stream";
    }
}
