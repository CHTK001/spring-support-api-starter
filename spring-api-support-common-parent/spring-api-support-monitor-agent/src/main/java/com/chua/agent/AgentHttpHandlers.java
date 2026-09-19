package com.chua.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Agent HTTP 接口处理器。
 *
 * <p>以 {@code com.sun.net.httpserver} 实现各信息接口的统一响应封装与查询参数解析，
 * 统一返回 {@code application/json; charset=utf-8} 并允许跨域。</p>
 *
 * <p>全类保持 Java 8 兼容：不使用 {@code InputStream#readAllBytes}、{@code InputStream#transferTo}、
 * {@code Map.of} 等 Java 9+ API，因为被监控主机常见只有 JRE 8。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
public class AgentHttpHandlers {

    /**
     * JSON 序列化器（错误信息也走它，避免异常文本里的引号把响应体打断）
    */
    private static final ObjectMapper JSON = new ObjectMapper();

    /**
     * 分页默认条数
    */
    private static final int DEFAULT_PAGE_SIZE = 50;

    /**
     * 分页上限：防止前端传 pageSize=999999 把整台机器的进程表拖出来
    */
    private static final int MAX_PAGE_SIZE = 1000;

    /**
     * /exec 默认超时（秒）
    */
    private static final int EXEC_TIMEOUT_SECONDS = 30;

    /**
     * 流复制缓冲区大小
    */
    private static final int COPY_BUFFER_BYTES = 8192;

    /**
     * 输出 JSON 响应。
     *
     * @param ex   HTTP 交换对象
     * @param code HTTP 状态码
     * @param json 响应体
     * @throws IOException 响应写出失败
     */
    static void sendJson(HttpExchange ex, int code, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        // 画像页会周期性拉同一批端点，缓存下来只会看到过期数据
        ex.getResponseHeaders().set("Cache-Control", "no-store");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    /**
     * 输出错误响应。
     *
     * @param ex    HTTP 交换对象
     * @param code  HTTP 状态码
     * @param error 错误描述
     * @throws IOException 响应写出失败
     */
    static void sendError(HttpExchange ex, int code, String error) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", error == null ? "未知错误" : error);
        sendJson(ex, code, JSON.writeValueAsString(body));
    }

    /**
     * 解析 URL 查询串为键值对（值做 URL 解码，/exec 的命令带空格与符号）。
     *
     * @param query 查询串（可为空）
     * @return 参数映射；无参数时返回空 Map
     */
    static Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) {
            return map;
        }
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                map.put(kv[0], decode(kv[1]));
            }
        }
        return map;
    }

    /**
     * URL 解码，失败时回退原值。
     *
     * @param value 原值
     * @return 解码结果
     */
    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }

    /**
     * 取整数参数并夹到合法区间。
     *
     * @param query    参数表
     * @param key      键
     * @param fallback 缺省值
     * @param max      上限，0 表示不限
     * @return 整数
     */
    private static int intParam(Map<String, String> query, String key, int fallback, int max) {
        String raw = query.get(key);
        if (raw == null || raw.trim().isEmpty()) {
            return fallback;
        }
        try {
            int value = Integer.parseInt(raw.trim());
            if (value < 1) {
                return fallback;
            }
            return max > 0 && value > max ? max : value;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /**
     * 处理 /info：返回主机基础信息与 agent 身份。
     *
     * @param ex     HTTP 交换对象
     * @param si     系统信息采集器
     * @param config 运行配置
     * @throws IOException 响应写出失败
     */
    static void handleInfo(HttpExchange ex, AgentSystemInfo si, AgentConfig config) throws IOException {
        try {
            sendJson(ex, 200, si.info(config));
        } catch (Exception e) {
            sendError(ex, 500, e.getMessage());
        }
    }

    /**
     * 处理 /processes：分页返回进程列表。
     *
     * @param ex HTTP 交换对象（读取 page / pageSize 查询参数）
     * @param si 系统信息采集器
     * @throws IOException 响应写出失败
     */
    static void handleProcesses(HttpExchange ex, AgentSystemInfo si) throws IOException {
        Map<String, String> query = parseQuery(ex.getRequestURI().getQuery());
        int page = intParam(query, "page", 1, 0);
        int size = intParam(query, "pageSize", DEFAULT_PAGE_SIZE, MAX_PAGE_SIZE);
        try {
            sendJson(ex, 200, si.processes(page, size));
        } catch (Exception e) {
            sendError(ex, 500, e.getMessage());
        }
    }

    /**
     * 处理 /services：分页返回服务列表。
     *
     * @param ex HTTP 交换对象（读取 page / pageSize 查询参数）
     * @param si 系统信息采集器
     * @throws IOException 响应写出失败
     */
    static void handleServices(HttpExchange ex, AgentSystemInfo si) throws IOException {
        Map<String, String> query = parseQuery(ex.getRequestURI().getQuery());
        int page = intParam(query, "page", 1, 0);
        int size = intParam(query, "pageSize", DEFAULT_PAGE_SIZE, MAX_PAGE_SIZE);
        try {
            sendJson(ex, 200, si.services(page, size));
        } catch (Exception e) {
            sendError(ex, 500, e.getMessage());
        }
    }

    /**
     * 处理 /disks：返回磁盘列表。
     *
     * @param ex HTTP 交换对象
     * @param si 系统信息采集器
     * @throws IOException 响应写出失败
     */
    static void handleDisks(HttpExchange ex, AgentSystemInfo si) throws IOException {
        try {
            sendJson(ex, 200, si.disks());
        } catch (Exception e) {
            sendError(ex, 500, e.getMessage());
        }
    }

    /**
     * 处理 /metrics：返回实时指标（CPU/内存/磁盘使用率）。
     *
     * @param ex HTTP 交换对象
     * @param si 系统信息采集器
     * @throws IOException 响应写出失败
     */
    static void handleMetrics(HttpExchange ex, AgentSystemInfo si) throws IOException {
        try {
            sendJson(ex, 200, si.metrics());
        } catch (Exception e) {
            sendError(ex, 500, e.getMessage());
        }
    }

    /**
     * 处理 /network：返回网络接口列表。
     *
     * @param ex HTTP 交换对象
     * @param si 系统信息采集器
     * @throws IOException 响应写出失败
     */
    static void handleNetwork(HttpExchange ex, AgentSystemInfo si) throws IOException {
        try {
            sendJson(ex, 200, si.network());
        } catch (Exception e) {
            sendError(ex, 500, e.getMessage());
        }
    }

    /**
     * 处理 /ports：返回本机监听端口（TCP LISTEN + UDP 绑定）。
     *
     * @param ex HTTP 交换对象
     * @param si 系统信息采集器
     * @throws IOException 响应写出失败
     */
    static void handlePorts(HttpExchange ex, AgentSystemInfo si) throws IOException {
        try {
            sendJson(ex, 200, si.ports());
        } catch (Exception e) {
            sendError(ex, 500, e.getMessage());
        }
    }

    /**
     * 处理 /netproc：返回网卡累计收发计数。
     *
     * @param ex HTTP 交换对象
     * @param si 系统信息采集器
     * @throws IOException 响应写出失败
     */
    static void handleNetProc(HttpExchange ex, AgentSystemInfo si) throws IOException {
        try {
            sendJson(ex, 200, si.netProc());
        } catch (Exception e) {
            sendError(ex, 500, e.getMessage());
        }
    }

    /**
     * 处理 /files：列出指定目录下的文件条目（name/isDir/size/modified）。
     *
     * @param ex HTTP 交换对象（读取 path 查询参数，默认 /）
     * @throws IOException 响应写出失败
     */
    static void handleFiles(HttpExchange ex) throws IOException {
        Map<String, String> query = parseQuery(ex.getRequestURI().getQuery());
        String path = query.get("path");
        File dir = new File(path == null || path.trim().isEmpty() ? "/" : path.trim());
        List<Map<String, Object>> list = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("name", f.getName());
                item.put("isDir", f.isDirectory());
                item.put("size", f.length());
                item.put("modified", new Date(f.lastModified()).toString());
                list.add(item);
            }
        }
        sendJson(ex, 200, JSON.writeValueAsString(list));
    }

    /**
     * 处理 /exec：执行命令并返回输出。
     *
     * <p>配置了 {@code agent.token} 时必须在查询串带同值 token；不配置则保持原有的无校验行为。</p>
     *
     * @param ex     HTTP 交换对象（读取 cmd / timeout 查询参数）
     * @param config 运行配置
     * @throws IOException 响应写出失败
     */
    static void handleExec(HttpExchange ex, AgentConfig config) throws IOException {
        Map<String, String> query = parseQuery(ex.getRequestURI().getQuery());
        String required = config == null ? "" : config.token();
        if (!required.isEmpty() && !required.equals(query.get("token"))) {
            sendError(ex, 401, "缺少或错误的 token");
            return;
        }
        String cmd = query.get("cmd");
        if (cmd == null || cmd.trim().isEmpty()) {
            sendError(ex, 400, "cmd 不能为空");
            return;
        }
        int timeout = intParam(query, "timeout", EXEC_TIMEOUT_SECONDS, 300);
        Process process = null;
        try {
            process = new ProcessBuilder(splitCommand(cmd)).redirectErrorStream(true).start();
            String out = AgentMain.readFully(process.getInputStream());
            if (!process.waitFor(timeout, TimeUnit.SECONDS)) {
                process.destroy();
                sendError(ex, 504, "命令执行超时(" + timeout + "s)，已终止");
                return;
            }
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("exitCode", process.exitValue());
            body.put("output", out);
            sendJson(ex, 200, JSON.writeValueAsString(body));
        } catch (Exception e) {
            if (process != null) {
                process.destroy();
            }
            sendError(ex, 500, e.getMessage());
        }
    }

    /**
     * 按当前操作系统把命令行串拆成进程参数。
     *
     * @param cmd 命令行
     * @return 参数列表
     */
    private static List<String> splitCommand(String cmd) {
        List<String> commands = new ArrayList<>();
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        if (windows) {
            commands.add("cmd.exe");
            commands.add("/c");
        } else {
            commands.add("/bin/sh");
            commands.add("-c");
        }
        commands.add(cmd);
        return commands;
    }

    /**
     * 处理 /env：返回进程环境变量列表。
     *
     * @param ex HTTP 交换对象
     * @throws IOException 响应写出失败
     */
    static void handleEnv(HttpExchange ex) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", entry.getKey());
            item.put("value", entry.getValue());
            list.add(item);
        }
        sendJson(ex, 200, JSON.writeValueAsString(list));
    }

    /**
     * 处理 /upload：把请求体原样写入指定路径。
     *
     * @param ex HTTP 交换对象（读取 path 查询参数）
     * @throws IOException 响应写出失败
     */
    static void handleUpload(HttpExchange ex) throws IOException {
        Map<String, String> query = parseQuery(ex.getRequestURI().getQuery());
        String path = query.get("path");
        if (path == null || path.trim().isEmpty()) {
            sendError(ex, 400, "path 不能为空");
            return;
        }
        File target = new File(path.trim());
        File parent = target.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            sendError(ex, 500, "目录创建失败: " + parent);
            return;
        }
        long written = 0L;
        try (InputStream in = ex.getRequestBody();
             OutputStream out = new java.io.FileOutputStream(target)) {
            byte[] buf = new byte[COPY_BUFFER_BYTES];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
                written += len;
            }
        } catch (Exception e) {
            sendError(ex, 500, e.getMessage());
            return;
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", true);
        body.put("size", written);
        sendJson(ex, 200, JSON.writeValueAsString(body));
    }

    /**
     * 处理 /download：按路径回传文件流。
     *
     * @param ex HTTP 交换对象（读取 path 查询参数）
     * @throws IOException 响应写出失败
     */
    static void handleDownload(HttpExchange ex) throws IOException {
        Map<String, String> query = parseQuery(ex.getRequestURI().getQuery());
        String path = query.get("path");
        File file = path == null ? null : new File(path.trim());
        if (file == null || !file.isFile()) {
            sendError(ex, 404, "文件不存在");
            return;
        }
        ex.getResponseHeaders().set("Content-Type", "application/octet-stream");
        ex.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
        ex.sendResponseHeaders(200, file.length());
        try (InputStream in = new java.io.FileInputStream(file);
             OutputStream out = ex.getResponseBody()) {
            byte[] buf = new byte[COPY_BUFFER_BYTES];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        }
    }

    /**
     * 处理 /health：存活探针，同时回带版本与端口，供平台判定安装/升级是否真的生效。
     *
     * @param ex     HTTP 交换对象
     * @param config 运行配置
     * @throws IOException 响应写出失败
     */
    static void handleHealth(HttpExchange ex, AgentConfig config) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "up");
        body.put("version", config == null ? "" : config.version());
        body.put("buildTime", config == null ? "" : config.buildTime());
        body.put("port", config == null ? 0 : config.port());
        sendJson(ex, 200, JSON.writeValueAsString(body));
    }
}
