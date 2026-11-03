package com.chua.agent;

import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;

/**
 * 监控 Agent 启动入口（独立运行于被监控主机）。
 *
 * <p>只对外提供内置 HTTP 服务（默认 19876），平台侧按需拉取主机画像各项数据；
 * Agent 不反向连平台，重装/升级/卸载都以平台侧脚本为准。</p>
 *
 * <p>本类必须保持 Java 8 兼容：被监控主机常见只有 JRE 8。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
public class AgentMain {

    /**
     * HTTP 默认端口
    */
    private static final int DEFAULT_PORT = 19876;

    /**
     * 默认监听地址
    */
    private static final String DEFAULT_BIND = "0.0.0.0";

    /**
     * 本机传统编码缓存，首次使用时解析
    */
    private static volatile Charset NATIVE_CHARSET;

    /**
     * HTTP 线程数：主机画像一次刷新会并发拉 /info、/metrics、/ports 等多个端点
    */
    private static final int HTTP_THREADS = 4;

    /**
     * 启动 Agent：读取配置 → 注册 HTTP 接口 → 写 pid 文件。
     *
     * @param args 可选 {@code -c <配置文件>}；缺省依次找 config/agent.yml、agent.yml
     * @throws Exception 配置读取失败或端口占用
     */
    public static void main(String[] args) throws Exception {
        Path cfgPath = resolveConfigPath(args);
        final AgentConfig config = AgentConfig.load(cfgPath, DEFAULT_PORT, DEFAULT_BIND);
        final AgentSystemInfo sysInfo = new AgentSystemInfo();

        HttpServer server = HttpServer.create(new InetSocketAddress(config.bind(), config.port()), 0);
        server.createContext("/info", ex -> AgentHttpHandlers.handleInfo(ex, sysInfo, config));
        server.createContext("/processes", ex -> AgentHttpHandlers.handleProcesses(ex, sysInfo));
        server.createContext("/services", ex -> AgentHttpHandlers.handleServices(ex, sysInfo));
        server.createContext("/disks", ex -> AgentHttpHandlers.handleDisks(ex, sysInfo));
        server.createContext("/metrics", ex -> AgentHttpHandlers.handleMetrics(ex, sysInfo));
        server.createContext("/network", ex -> AgentHttpHandlers.handleNetwork(ex, sysInfo));
        server.createContext("/ports", ex -> AgentHttpHandlers.handlePorts(ex, sysInfo));
        server.createContext("/netproc", ex -> AgentHttpHandlers.handleNetProc(ex, sysInfo));
        server.createContext("/files", ex -> AgentHttpHandlers.handleFiles(ex));
        server.createContext("/files/usage", ex -> AgentHttpHandlers.handleFileUsage(ex));
        server.createContext("/files/mkdir", ex -> AgentHttpHandlers.handleFileMkdir(ex));
        server.createContext("/files/delete", ex -> AgentHttpHandlers.handleFileDelete(ex));
        server.createContext("/files/rename", ex -> AgentHttpHandlers.handleFileRename(ex));
        server.createContext("/upload", ex -> AgentHttpHandlers.handleUpload(ex));
        server.createContext("/download", ex -> AgentHttpHandlers.handleDownload(ex));
        server.createContext("/exec", ex -> AgentHttpHandlers.handleExec(ex, config));
        server.createContext("/env", ex -> AgentHttpHandlers.handleEnv(ex));
        server.createContext("/health", ex -> AgentHttpHandlers.handleHealth(ex, config));
        server.setExecutor(Executors.newFixedThreadPool(HTTP_THREADS));
        server.start();

        writePidFile();
        // [P3C E1 豁免] CLI 入口：Agent 启动横幅，非业务输出
        System.out.println("[Agent] v" + config.version() + " HTTP listening on " + config.bind() + ":" + config.port() // [P3C E1 豁免]
                + (cfgPath == null ? " (未找到配置文件，使用默认值)" : " (config=" + cfgPath.toAbsolutePath() + ")"));
    }

    /**
     * 定位配置文件：命令行 -c 优先，其次工作目录 config/agent.yml，最后 agent.yml。
     *
     * @param args 命令行参数
     * @return 存在的配置文件路径，全都不存在时返回 null（走内置默认值）
     */
    private static Path resolveConfigPath(String[] args) {
        if (args != null) {
            for (int i = 0; i + 1 < args.length; i++) {
                if ("-c".equals(args[i]) || "--config".equals(args[i])) {
                    Path given = Paths.get(args[i + 1]);
                    return Files.isReadable(given) ? given : null;
                }
            }
        }
        Path nested = Paths.get("config", "agent.yml");
        if (Files.isReadable(nested)) {
            return nested;
        }
        Path flat = Paths.get("agent.yml");
        return Files.isReadable(flat) ? flat : null;
    }

    /**
     * 写 agent.pid：卸载与升级要靠它精确停掉进程，不能按进程名猜。
     */
    private static void writePidFile() {
        try {
            long pid = currentPid();
            if (pid > 0) {
                Files.write(Paths.get("agent.pid"), String.valueOf(pid).getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            // [P3C E1 豁免] CLI 入口：运行时错误回显，非业务输出
            System.err.println("[Agent] pid 文件写入失败: " + e.getMessage()); // [P3C E1 豁免]
        }
    }

    /**
     * 取当前进程号：Java 8 无 ProcessHandle，用 RuntimeMXBean 的 name（形如 pid@host）解析。
     *
     * @return 进程号，取不到时返回 -1
     */
    private static long currentPid() {
        String name = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
        int at = name.indexOf('@');
        try {
            return Long.parseLong(at > 0 ? name.substring(0, at) : name);
        } catch (NumberFormatException e) {
            return -1L;
        }
    }

    /**
     * 读流到文本：Java 8 没有 {@code InputStream#readAllBytes}，统一走这里。
     *
     * @param in 输入流
     * @return 文本内容
     */
    static String readFully(InputStream in) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            char[] buf = new char[4096];
            int len;
            while ((len = reader.read(buf)) > 0) {
                sb.append(buf, 0, len);
            }
        } catch (Exception e) {
            // [P3C E1 豁免] CLI 入口：运行时错误回显，非业务输出
            System.err.println("[Agent] 读取流失败: " + e.getMessage()); // [P3C E1 豁免]
        }
        return sb.toString();
    }

    /**
     * 进程退出或被强杀后，等待抽取线程收尾的时间（毫秒）：拿到的部分输出仍有排查价值，
     * 但不能让 HTTP 线程无限等一个被子孙进程握住的管道。
     */
    static final long PUMP_DRAIN_MILLIS = 300L;

    /**
     * 用守护线程把子进程输出抽进缓冲区：读取阻塞在流上，只有句柄全部关闭才会 EOF，
     * 放到独立线程后调用方才能用自己的超时决定是否继续等待。
     *
     * @param in   子进程输出流
     * @param sink 累积缓冲区
     * @return 已启动的抽取线程
     */
    static Thread pumpOutput(InputStream in, final ByteArrayOutputStream sink) {
// [P3C C1 豁免] 守护/后台线程：有独立生命周期，不适合交给通用执行器
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buf = new byte[8192];
                try (InputStream stream = in) {
                    int len;
                    while ((len = stream.read(buf)) > 0) {
                        synchronized (sink) {
                            sink.write(buf, 0, len);
                        }
                    }
                } catch (Exception e) {
                    // 进程被强杀时流会抛异常，已读到的部分依然有效
                }
            }
        }, "agent-output-pump");
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    /**
     * 线程安全地取抽取缓冲区当前已读到的字节。
     *
     * @param sink 累积缓冲区
     * @return 字节快照，编码判定交给 {@link #decodeOutput(byte[])}
     */
    static byte[] snapshot(ByteArrayOutputStream sink) {
        synchronized (sink) {
            return sink.toByteArray();
        }
    }

    /**
     * 本机传统编码（系统 ANSI/OEM 码页）对应的字符集，仅解析一次并缓存。
     *
     * <p>JDK 18 起默认字符集是 UTF-8，还会给子进程设置 UTF-8 代码页，但 cmd 的内部消息
     * （ver、找不到命令等）仍按系统 ANSI 码页输出。{@code native.encoding} 记录的正是后者，
     * Java 8 没有该属性时 {@code file.encoding} 同义。</p>
     *
     * @return 本机传统字符集
     */
    static Charset nativeCharset() {
        Charset cached = NATIVE_CHARSET;
        if (cached != null) {
            return cached;
        }
        Charset resolved = StandardCharsets.UTF_8;
        String hint = System.getProperty("native.encoding");
        if (hint == null || hint.trim().isEmpty()) {
            hint = System.getProperty("file.encoding");
        }
        try {
            if (hint != null && !hint.trim().isEmpty()) {
                resolved = Charset.forName(hint.trim());
            }
        } catch (Exception e) {
            resolved = Charset.defaultCharset();
        }
        NATIVE_CHARSET = resolved;
        return resolved;
    }

    /**
     * 解析外部进程输出：能按 UTF-8 完整解码的按 UTF-8，否则按本机传统编码。
     *
     * @param bytes 进程输出字节
     * @return 解码后的文本
     */
    static String decodeOutput(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();
        } catch (CharacterCodingException e) {
            return new String(bytes, nativeCharset());
        }
    }

    /**
     * 生成 PowerShell 的 -EncodedCommand 参数（Base64 of UTF-16LE）。
     *
     * <p>PowerShell 5.1 会对 -Command 的命令行做二次解析，Java 传入的内层引号被吞后脚本报语法错误、
     * 进程以 1 退出且无输出；传字节再解码则与引号无关。</p>
     *
     * @param script PowerShell 脚本
     * @return -EncodedCommand 参数值
     */
    static String encodePowerShell(String script) {
        return Base64.getEncoder().encodeToString(script.getBytes(StandardCharsets.UTF_16LE));
    }

    /**
     * 读取配置文件的原始行（供 AgentConfig 解析）。
     *
     * @param cfgPath 配置文件路径
     * @return 文本行，读取失败返回空集合
     */
    static List<String> readLines(Path cfgPath) {
        try {
            List<String> lines = Files.readAllLines(cfgPath, StandardCharsets.UTF_8);
            // Windows 编辑器与 PowerShell 默认写 UTF-8 BOM，首行会多出 \ufeff
            // 导致命名空间键变成 "agent"，端口/令牌全部静默回落到默认值
            if (!lines.isEmpty()) {
                String first = lines.get(0);
                if (first != null && !first.isEmpty() && first.charAt(0) == 0xFEFF) {
                    lines.set(0, first.substring(1));
                }
            }
            return lines;
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }

    /**
     * 极简 YAML 解析，忽略注释、空行、列表行与绝对路径行。
     *
     * <p>必须按缩进拼出层级键：平台下发的 agent.yml 写的是 {@code agent:} 换行两空格 {@code port: 19876}，
     * 早先只按第一个冒号切分会得到裸键 {@code port}，读 {@code agent.port} 永远取不到值，
     * 于是端口、token、agentKey 全部静默落回默认值。</p>
     *
     * @param cfgPath 配置文件路径，可为 null
     * @return 扁平化后的配置项（键形如 {@code agent.port}）
     */
    static Properties loadYamlLike(Path cfgPath) {
        Properties props = new Properties();
        if (cfgPath == null) {
            return props;
        }
        // 缩进 → 该缩进层级当前的命名空间键
        Map<Integer, String> stack = new java.util.LinkedHashMap<>();
        for (String raw : readLines(cfgPath)) {
            if (raw.trim().isEmpty() || raw.trim().startsWith("#") || raw.trim().startsWith("-") || raw.trim().startsWith("/")) {
                continue;
            }
            int indent = 0;
            while (indent < raw.length() && raw.charAt(indent) == ' ') {
                indent++;
            }
            String line = raw.trim();
            int idx = line.indexOf(':');
            if (idx <= 0) {
                continue;
            }
            String key = line.substring(0, idx).trim();
            String value = line.substring(idx + 1).trim().replace("\"", "");
            // 比当前缩进更深的命名空间已经失效
            java.util.Iterator<Map.Entry<Integer, String>> it = stack.entrySet().iterator();
            while (it.hasNext()) {
                if (it.next().getKey() >= indent) {
                    it.remove();
                }
            }
            String qualified = prefix(stack) + key;
            if (value.isEmpty()) {
                stack.put(indent, qualified);
            } else {
                props.setProperty(qualified, value);
            }
        }
        return props;
    }

    /**
     * 由命名空间栈拼出前缀（形如 {@code agent.}）。
     *
     * @param stack 缩进栈
     * @return 前缀，无命名空间时返回空串
     */
    private static String prefix(Map<Integer, String> stack) {
        StringBuilder sb = new StringBuilder();
        for (String key : stack.values()) {
            sb.append(key).append('.');
        }
        return sb.toString();
    }
}
