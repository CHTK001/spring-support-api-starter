package com.chua.starter.protocol.test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * HTTP 文件服务器 - 基于 JDK 内置 HttpServer
 */
public class StartHttpFileServer {
    public static void main(String[] args) throws Exception {
        int port = 8080;
        String baseDir = new File(".").getAbsoluteFile().getParent();

        // 确保目录存在
        new File(baseDir).mkdirs();

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", new FileHandler(baseDir));

        server.setExecutor(java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor());
        server.start();

        System.out.println("========================================");
        System.out.println("HTTP 文件服务器已启动!");
        System.out.println("地址: http://127.0.0.1:" + port);
        System.out.println("根目录: " + baseDir);
        System.out.println("========================================");
        System.out.println("浏览器打开 http://127.0.0.1:" + port + " 即可浏览文件");
        System.out.println("按 Ctrl+C 停止服务器");
        System.out.println("========================================");
    }

    static class FileHandler implements HttpHandler {
        private final String baseDir;

        FileHandler(String baseDir) {
            this.baseDir = baseDir;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestPath = exchange.getRequestURI().getPath();
            if (requestPath == null || requestPath.isEmpty()) {
                requestPath = "/";
            }
            requestPath = URLDecoder.decode(requestPath, StandardCharsets.UTF_8);

            File file = new File(baseDir, requestPath).getCanonicalFile();

            // 安全检查：防止路径穿越
            if (!file.getPath().startsWith(new File(baseDir).getCanonicalPath())) {
                sendResponse(exchange, 403, "Forbidden");
                return;
            }

            if (requestPath.equals("/")) {
                // 目录列表
                StringBuilder html = new StringBuilder();
                html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
                html.append("<title>文件服务器 - ").append(baseDir).append("</title>");
                html.append("<style>body{font-family:sans-serif;margin:20px}");
                html.append("table{border-collapse:collapse;width:100%}");
                html.append("th,td{text-align:left;padding:8px;border-bottom:1px solid #ddd}");
                html.append("tr:hover{background:#f5f5f5}");
                html.append("a{text-decoration:none;color:#0366d6}</style></head><body>");
                html.append("<h2>📁 ").append(baseDir).append("</h2><table>");
                html.append("<tr><th>名称</th><th>大小</th><th>类型</th></tr>");

                File[] files = file.listFiles();
                if (files != null) {
                    for (File f : files) {
                        String name = f.getName();
                        String size = f.isDirectory() ? "-" : formatSize(f.length());
                        String type = f.isDirectory() ? "📁 目录" : "📄 文件";
                        String link = "/" + name + (f.isDirectory() ? "/" : "");
                        html.append("<tr><td><a href=\"").append(link).append("\">").append(name).append("</a></td>");
                        html.append("<td>").append(size).append("</td><td>").append(type).append("</td></tr>");
                    }
                }
                html.append("</table></body></html>");
                sendResponse(exchange, 200, html.toString());
                return;
            }

            if (!file.exists()) {
                sendResponse(exchange, 404, "Not Found: " + requestPath);
                return;
            }

            if (file.isDirectory()) {
                // 目录重定向
                exchange.getResponseHeaders().set("Location", requestPath + "/");
                sendResponse(exchange, 302, "Redirect");
                return;
            }

            // 根据扩展名设置 Content-Type
            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.getResponseHeaders().set("Content-Length", String.valueOf(file.length()));
            exchange.sendResponseHeaders(200, file.length());

            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = exchange.getResponseBody()) {
                fis.transferTo(os);
            }
        }

        private void sendResponse(HttpExchange exchange, int code, String body) throws IOException {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(code, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private String formatSize(long size) {
            if (size < 1024) return size + " B";
            if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
            if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024));
            return String.format("%.1f GB", size / (1024.0 * 1024 * 1024));
        }
    }
}
