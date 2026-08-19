package com.chua.starter.service.server;

import com.chua.common.support.network.server.ServerSetting;
import com.chua.common.support.network.server.impl.JdkHttpServer;
import com.chua.starter.entity.service.SysFileSystem;
import com.chua.starter.entity.service.SysFileSystemSetting;
import com.chua.starter.service.SysFileSystemService;
import com.chua.starter.service.SysFileSystemSettingService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * 文件下载服务器 — 基于 {@link JdkHttpServer} 的轻量内嵌 HTTP 服务。
 *
 * <p>启动后对外提供：</p>
 * <ul>
 *   <li>{@code GET /file/{id}} — 通过数据库主键查找文件并下载</li>
 *   <li>{@code GET /raw/{path}} — 按相对路径(相对 tempPath)读取本地文件</li>
 *   <li>{@code GET /health} — 健康检查</li>
 * </ul>
 *
 * <p>使用 JDK 内置 HTTP Server + 虚拟线程池,零外部依赖。生命周期由 Service 显式管理。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@Slf4j
public class FileHttpServer {

    @Getter
    /** 设置 */
    private final ServerSetting setting;
    /** 服务器 */
    private final JdkHttpServer server;
    /** SYS文件system服务 */
    private final SysFileSystemService sysFileSystemService;
    /** SYS文件system设置服务 */
    private final SysFileSystemSettingService sysFileSystemSettingService;
    /** Temp路径 */
    private final String tempPath;
    @Getter
    private volatile boolean running;

    /**
     * 创建 FileHttpServer 实例
     * @param sysFileSystemService sysFileSystemService
     * @param sysFileSystemSettingService sysFileSystemSettingService
     * @param setting setting
     * @param tempPath tempPath
     */
    public FileHttpServer(SysFileSystemService sysFileSystemService,
                          SysFileSystemSettingService sysFileSystemSettingService,
                          ServerSetting setting,
                          String tempPath) {
        this.sysFileSystemService = sysFileSystemService;
        this.sysFileSystemSettingService = sysFileSystemSettingService;
        this.setting = setting;
        this.tempPath = tempPath;
        this.server = new JdkHttpServer(setting);
        registerRoutes();
    }

    private void registerRoutes() {
        // 解析 body 中的二进制流(由 JdkHttpServer 内部已 wrap 成 ServerResponse)
        this.server.registerMapping("/health", (req, res) -> {
            res.setContentType("text/plain;charset=UTF-8");
            res.setBody("ok");
            res.end();
        });
        this.server.registerMapping("/file/:id", (req, res) -> {
            String fullPath = req.getPath() != null ? req.getPath() : "";
            String idRaw = fullPath.startsWith("/file/") ? fullPath.substring("/file/".length()) : "";
            Integer fileId;
            try {
                fileId = Integer.parseInt(idRaw);
            } catch (NumberFormatException nfe) {
                res.setStatus(400);
                res.setBody("fileId 格式错误");
                res.end();
                return;
            }
            serveFileById(fileId, res);
        });
        this.server.registerMapping("/raw/**", (req, res) -> {
            String full = req.getPath() != null ? req.getPath() : "";
            String prefix = "/raw/";
            String encoded = full.startsWith(prefix) ? full.substring(prefix.length()) : full;
            String decoded = URLDecoder.decode(encoded, StandardCharsets.UTF_8);
            serveRawPath(decoded, res);
        });
    }

    /**
     * 启动 HTTP 服务器。
     */
    public synchronized void start() {
        if (running) {
            return;
        }
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException("FileHttpServer 启动失败 port=" + setting.getPort(), e);
        }
        running = true;
        log.info("FileHttpServer started on {}:{}{}",
                setting.getHost(), setting.getPort(), setting.getContextPath());
    }

    /**
     * 关闭 HTTP 服务器。
     */
    public synchronized void stop() {
        if (!running) {
            return;
        }
        try {
            server.close();
        } catch (Exception e) {
            log.warn("FileHttpServer 关闭失败", e);
        }
        running = false;
        log.info("FileHttpServer stopped on {}:{}", setting.getHost(), setting.getPort());
    }

    private void serveFileById(Integer fileId, com.chua.common.support.network.server.response.ServerResponse res) {
        SysFileSystem file = sysFileSystemService.getById(fileId);
        if (file == null) {
            res.setStatus(404);
            res.setBody("文件不存在");
            res.end();
            return;
        }
        if (!Boolean.TRUE.equals(file.getSysFileSystemHttpEnabled())) {
            res.setStatus(403);
            res.setBody("文件已禁用");
            res.end();
            return;
        }
        if (!Boolean.TRUE.equals(getCurrentDownloadEnabled())) {
            res.setStatus(403);
            res.setBody("下载功能未启用");
            res.end();
            return;
        }
        Path localPath = resolveLocalPath(file);
        if (localPath == null || !Files.exists(localPath)) {
            res.setStatus(404);
            res.setBody("本地文件不存在");
            res.end();
            return;
        }
        writeFile(res, localPath, file.getSysFileSystemOriginalName());
    }

    private void serveRawPath(String decoded, com.chua.common.support.network.server.response.ServerResponse res) {
        if (decoded.contains("..")) {
            res.setStatus(400);
            res.setBody("非法的路径");
            res.end();
            return;
        }
        Path base = Paths.get(tempPath).toAbsolutePath().normalize();
        Path target = base.resolve(decoded).normalize();
        if (!target.startsWith(base)) {
            res.setStatus(400);
            res.setBody("非法的路径");
            res.end();
            return;
        }
        if (!Files.exists(target)) {
            res.setStatus(404);
            res.setBody("文件不存在");
            res.end();
            return;
        }
        writeFile(res, target, target.getFileName().toString());
    }

    private Path resolveLocalPath(SysFileSystem file) {
        String local = file.getSysFileSystemLocalPath();
        if (local == null || local.isEmpty()) {
            local = file.getSysFileSystemPath();
        }
        if (local == null || local.isEmpty()) {
            return null;
        }
        return Paths.get(local);
    }

    private Boolean getCurrentDownloadEnabled() {
        try {
            SysFileSystemSetting enabled = sysFileSystemSettingService.getEnabledSettingForFileSystem();
            return enabled == null || enabled.getSysFileSystemSettingDownloadEnabled() == null
                    || enabled.getSysFileSystemSettingDownloadEnabled();
        } catch (Exception ignored) {
            return true;
        }
    }

    private void writeFile(com.chua.common.support.network.server.response.ServerResponse res, Path file, String name) {
        try {
            long len = Files.size(file);
            res.setContentType(guessContentType(name));
            res.setHeader("Content-Length", String.valueOf(len));
            res.setHeader("Content-Disposition", "inline; filename=\"" + encode(name) + "\"");
            res.setStatus(200);
            byte[] bytes = Files.readAllBytes(file);
            res.setBody(bytes);
            res.end();
        } catch (Exception e) {
            log.warn("发送文件失败 name={} err={}", name, e.getMessage());
            res.setStatus(500);
            res.setBody("发送文件失败");
            res.end();
        }
    }

    private static String guessContentType(String name) {
        String lower = name.toLowerCase();
        if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html;charset=UTF-8";
        if (lower.endsWith(".json")) return "application/json;charset=UTF-8";
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".txt")) return "text/plain;charset=UTF-8";
        if (lower.endsWith(".zip")) return "application/zip";
        return "application/octet-stream";
    }

    private static String encode(String name) {
        StringBuilder sb = new StringBuilder();
        for (char c : name.toCharArray()) {
            if (c < 0x80) {
                sb.append(c);
            } else {
                for (byte b : String.valueOf(c).getBytes(StandardCharsets.UTF_8)) {
                    sb.append(String.format("%%%02X", b & 0xff));
                }
            }
        }
        return sb.toString();
    }

    /**
     * 创建默认 server 设置。
     */
    public static ServerSetting defaultSetting(String host, int port, String contextPath) {
        return ServerSetting.builder()
                .host(host)
                .port(port)
                .contextPath(contextPath)
                .protocol("http")
                .backlog(128)
                .build();
    }
}
