// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
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
     * 超时文案里附带多少字符的已捕获输出
    */
    private static final int ERROR_TAIL_CHARS = 500;

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
        Map<String, String> query = parseQuery(ex.getRequestURI().getRawQuery());
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
        Map<String, String> query = parseQuery(ex.getRequestURI().getRawQuery());
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
     * 处理 /netproc：返回全量网络连接及其占用进程。
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
        Map<String, String> query = parseQuery(ex.getRequestURI().getRawQuery());
        String path = query.get("path");
        File dir = new File(path == null || path.trim().isEmpty() ? "/" : path.trim());
        // 空目录与「路径不存在/不是目录」是两回事：回空列表会让写错的路径在页面上显示成「空目录」
        if (!dir.isDirectory()) {
            sendError(ex, 404, "目录不存在或不是目录: " + dir);
            return;
        }
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
     * 处理 /files/mkdir：创建目录（含父目录）。
     *
     * @param ex HTTP 交换对象（读取 path 查询参数）
     * @throws IOException 响应写出失败
     */
    static void handleFileMkdir(HttpExchange ex) throws IOException {
        Map<String, String> query = parseQuery(ex.getRequestURI().getRawQuery());
        String path = query.get("path");
        if (path == null || path.trim().isEmpty()) {
            sendError(ex, 400, "path 不能为空");
            return;
        }
        File dir = new File(path.trim());
        boolean ok = dir.exists() ? dir.isDirectory() : dir.mkdirs();
        if (ok) {
            okJson(ex);
        } else {
            sendError(ex, 500, "目录创建失败: " + path);
        }
    }

    /**
     * 处理 /files/delete：删除文件或递归删除目录，目标不存在按幂等成功处理。
     *
     * @param ex HTTP 交换对象（读取 path 查询参数）
     * @throws IOException 响应写出失败
     */
    static void handleFileDelete(HttpExchange ex) throws IOException {
        Map<String, String> query = parseQuery(ex.getRequestURI().getRawQuery());
        String path = query.get("path");
        if (path == null || path.trim().isEmpty()) {
            sendError(ex, 400, "path 不能为空");
            return;
        }
        File f = new File(path.trim());
        // 幂等：不存在视为已删除
        boolean ok = !f.exists() || (f.isDirectory() ? deleteRecursively(f) : f.delete());
        if (ok) {
            okJson(ex);
        } else {
            sendError(ex, 500, "删除失败: " + path);
        }
    }

    /**
     * 处理 /files/rename：重命名 / 移动，目标父目录必须已存在，覆盖已存在目标。
     *
     * @param ex HTTP 交换对象（读取 from / to 查询参数）
     * @throws IOException 响应写出失败
     */
    static void handleFileRename(HttpExchange ex) throws IOException {
        Map<String, String> query = parseQuery(ex.getRequestURI().getRawQuery());
        String from = query.get("from");
        String to = query.get("to");
        if (from == null || from.trim().isEmpty() || to == null || to.trim().isEmpty()) {
            sendError(ex, 400, "from/to 不能为空");
            return;
        }
        File src = new File(from.trim());
        File target = new File(to.trim());
        File parent = target.getParentFile();
        // 与上传同口径：移动到不存在的目录不是「成功顺带建目录」，而是失败；建目录请走 /files/mkdir
        if (parent != null && !parent.isDirectory()) {
            sendError(ex, 400, "目标目录不存在: " + parent);
            return;
        }
        // renameTo 跨平台覆盖语义不一致；目标已存在先递归删除
        if (target.exists() && !deleteQuietly(target)) {
            sendError(ex, 500, "目标已存在且无法覆盖: " + to);
            return;
        }
        if (src.renameTo(target)) {
            okJson(ex);
        } else {
            sendError(ex, 500, "重命名失败: " + from + " -> " + to);
        }
    }

    /**
     * 回传统一的成功报文 {@code {"ok":true}}。
     *
     * @param ex HTTP 交换对象
     * @throws IOException 响应写出失败
     */
    private static void okJson(HttpExchange ex) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", true);
        sendJson(ex, 200, JSON.writeValueAsString(body));
    }

    /**
     * 递归删除目录及其全部子项。
     *
     * @param f 目录
     * @return 全部删除成功返回 true
     */
    private static boolean deleteRecursively(File f) {
        File[] children = f.listFiles();
        if (children != null) {
            for (int i = 0; i < children.length; i++) {
                File c = children[i];
                boolean r = c.isDirectory() ? deleteRecursively(c) : c.delete();
                if (!r) {
                    return false;
                }
            }
        }
        return f.delete();
    }

    /**
     * 删除文件或目录（不抛异常）。
     *
     * @param f 目标
     * @return 成功返回 true
     */
    private static boolean deleteQuietly(File f) {
        return f.isDirectory() ? deleteRecursively(f) : f.delete();
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
        Map<String, String> query = parseQuery(ex.getRequestURI().getRawQuery());
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
            ByteArrayOutputStream sink = new ByteArrayOutputStream();
            Thread pump = AgentMain.pumpOutput(process.getInputStream(), sink);
            if (!process.waitFor(timeout, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                // 强杀后流会关闭，给抽取线程一点收尾时间，拿到的部分输出仍有排查价值
                pump.join(AgentMain.PUMP_DRAIN_MILLIS);
                sendError(ex, 504, "命令执行超时(" + timeout + "s)，已终止" + tail(sink));
                return;
            }
            // 命令自身已退出；被 start/nohup 派生的子孙仍可能握着输出句柄，只等一小段排空
            pump.join(AgentMain.PUMP_DRAIN_MILLIS);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("exitCode", process.exitValue());
            body.put("output", AgentMain.decodeOutput(AgentMain.snapshot(sink)));
            sendJson(ex, 200, JSON.writeValueAsString(body));
        } catch (Exception e) {
            if (process != null) {
                process.destroyForcibly();
            }
            sendError(ex, 500, e.getMessage());
        }
    }

    /**
     * 把超时前已捕获的输出缀到错误文案里。
     *
     * @param sink 累积缓冲区
     * @return 形如「；已捕获输出: ...」的后缀，无输出时为空串
     */
    private static String tail(ByteArrayOutputStream sink) {
        String text = AgentMain.decodeOutput(AgentMain.snapshot(sink)).trim();
        if (text.isEmpty()) {
            return "";
        }
        int from = Math.max(0, text.length() - ERROR_TAIL_CHARS);
        return "；已捕获输出: " + text.substring(from).replaceAll("\\s+", " ");
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
            commands.add(cmd);
            return commands;
        }
        commands.add("/bin/sh");
        commands.add("-c");
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
        Map<String, String> query = parseQuery(ex.getRequestURI().getRawQuery());
        String path = query.get("path");
        if (path == null || path.trim().isEmpty()) {
            sendError(ex, 400, "path 不能为空");
            return;
        }
        File target = new File(path.trim());
        // 不再自动补齐父目录：写进不存在的路径也算成功，会让用户在页面上凭空造出目录，
        // 与本机/SSH/WinRM 三套通道的口径相反；建目录请走 /files/mkdir
        File parent = target.getParentFile();
        if (parent != null && !parent.isDirectory()) {
            sendError(ex, 400, "目录不存在: " + parent);
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
        Map<String, String> query = parseQuery(ex.getRequestURI().getRawQuery());
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

    /**
     * 单次分析最多扫描的目录数（防止整盘递归无限放大）。
     */
    private static final int USAGE_MAX_DIRS = 100000;

    /**
     * 每个目录中拆成独立矩形块的最大文件数（按大小取 Top N，其余小文件合并为一个块）。
     */
    private static final int USAGE_TOP_FILES = 12;

    /**
     * 处理 /files/usage：在被监控主机本地递归统计目录大小，一次 HTTP 返回整棵 WizTree 风格占用树。
     *
     * <p>若由平台侧逐目录经 HTTP 递归，整盘会放大成数千次串行往返、长时间不返回；agent 直接遍历
     * 本地文件系统（无网络往返），并在根节点回填卷总容量 / 可用容量。</p>
     *
     * @param ex HTTP 交换对象（读取 path / depth 查询参数）
     * @throws IOException 响应写出失败
     */
    static void handleFileUsage(HttpExchange ex) throws IOException {
        Map<String, String> query = parseQuery(ex.getRequestURI().getRawQuery());
        String rawPath = query.get("path");
        String path = rawPath == null || rawPath.trim().isEmpty() ? "/" : rawPath.trim();
        int depth = intParam(query, "depth", 4, 8);
        File root = new File(path);
        if (!root.isDirectory()) {
            sendError(ex, 404, "目录不存在或不是目录: " + root);
            return;
        }
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        char sep = windows ? '\\' : '/';
        long[] scanned = {0L};
        // Windows 盘符根：优先直读 MFT（WizTree 原理，能看到 ACL 隐藏文件），失败再回退普通递归
        Map<String, Object> tree = null;
        if (windows && path.matches("[A-Za-z]:[\\\\/]*")) {
            try {
                tree = NtfsMftReader.scan(path);
            } catch (Throwable ignored) {
                tree = null;
            }
        }
        if (tree == null) {
            tree = walkUsageLocal(root, path, depth, sep, scanned);
        }
        tree.put("totalSpace", root.getTotalSpace());
        tree.put("freeSpace", root.getFreeSpace());
        sendJson(ex, 200, JSON.writeValueAsString(tree));
    }

    /**
     * 本地递归遍历并构建占用节点（镜像平台侧 walkUsage 的分块规则）。
     *
     * @param dir     当前目录
     * @param path    当前目录完整路径
     * @param remain  剩余递归深度
     * @param sep     路径分隔符
     * @param scanned 已扫描目录计数（可变）
     * @return 节点 Map
     */
    private static Map<String, Object> walkUsageLocal(File dir, String path, int remain, char sep, long[] scanned) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("directory", true);
        node.put("path", path);
        node.put("name", basenameLocal(path, sep));
        File[] files = dir.listFiles();
        scanned[0]++;
        long total = 0L;
        long fileCount = 0L;
        long dirCount = 0L;
        String latest = null;
        List<Map<String, Object>> children = new ArrayList<>();
        List<File> loose = new ArrayList<>();
        if (files != null) {
            for (File f : files) {
                long len = f.length();
                String mod = new Date(f.lastModified()).toString();
                if (latest == null || mod.compareTo(latest) > 0) {
                    latest = mod;
                }
                total += len;
                String childPath = joinUsageLocal(path, f.getName(), sep);
                if (f.isDirectory()) {
                    dirCount++;
                    if (remain > 1 && scanned[0] < USAGE_MAX_DIRS) {
                        Map<String, Object> child = walkUsageLocal(f, childPath, remain - 1, sep, scanned);
                        children.add(child);
                        long childSize = ((Number) child.get("size")).longValue();
                        total += Math.max(0L, childSize - len);
                        fileCount += ((Number) child.get("fileCount")).longValue();
                        dirCount += ((Number) child.get("dirCount")).longValue();
                    } else {
                        Map<String, Object> leaf = new LinkedHashMap<>();
                        leaf.put("directory", true);
                        leaf.put("name", f.getName());
                        leaf.put("path", childPath);
                        leaf.put("size", len);
                        leaf.put("modified", mod);
                        leaf.put("fileCount", 0L);
                        leaf.put("dirCount", 1L);
                        children.add(leaf);
                    }
                } else {
                    fileCount++;
                    loose.add(f);
                }
            }
        }
        loose.sort(new Comparator<File>() {
            @Override
            public int compare(File a, File b) {
                return Long.compare(b.length(), a.length());
            }
        });
        long restSize = 0L;
        long restCount = 0L;
        for (int i = 0; i < loose.size(); i++) {
            File f = loose.get(i);
            if (i < USAGE_TOP_FILES) {
                Map<String, Object> leaf = new LinkedHashMap<>();
                leaf.put("directory", false);
                leaf.put("name", f.getName());
                leaf.put("path", joinUsageLocal(path, f.getName(), sep));
                leaf.put("size", f.length());
                leaf.put("modified", new Date(f.lastModified()).toString());
                leaf.put("fileCount", 1L);
                leaf.put("dirCount", 0L);
                children.add(leaf);
            } else {
                restSize += f.length();
                restCount++;
            }
        }
        if (restCount > 0) {
            Map<String, Object> rest = new LinkedHashMap<>();
            rest.put("directory", false);
            rest.put("name", "__files__");
            rest.put("path", path);
            rest.put("size", restSize);
            rest.put("fileCount", restCount);
            rest.put("dirCount", 0L);
            children.add(rest);
        }
        children.sort(new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> a, Map<String, Object> b) {
                return Long.compare(((Number) b.get("size")).longValue(), ((Number) a.get("size")).longValue());
            }
        });
        if (!children.isEmpty()) {
            node.put("children", children);
        }
        node.put("size", total);
        node.put("fileCount", fileCount);
        node.put("dirCount", dirCount);
        node.put("modified", latest == null ? "" : latest);
        return node;
    }

    /**
     * 拼接父路径与子名称。
     *
     * @param parent 父路径
     * @param name   子名称
     * @param sep    分隔符
     * @return 完整路径
     */
    private static String joinUsageLocal(String parent, String name, char sep) {
        if (parent == null || parent.isEmpty()) {
            return name;
        }
        return parent.endsWith(String.valueOf(sep)) ? parent + name : parent + sep + name;
    }

    /**
     * 取路径最后一段作为名称。
     *
     * @param path 完整路径
     * @param sep  分隔符
     * @return 名称
     */
    private static String basenameLocal(String path, char sep) {
        int idx = path.lastIndexOf(sep);
        String tail = idx >= 0 ? path.substring(idx + 1) : path;
        return tail.isEmpty() ? path : tail;
    }
}