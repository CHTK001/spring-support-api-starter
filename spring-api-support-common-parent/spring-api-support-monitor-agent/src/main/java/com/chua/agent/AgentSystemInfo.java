package com.chua.agent;

import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Agent 侧基于 OSHI 的系统信息采集器。
 *
 * <p>采集结果统一序列化为 JSON 字符串，供 Agent HTTP 接口与指标上报线程使用。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
public class AgentSystemInfo {

    /**
     * 日志
     */
    private static final java.util.logging.Logger LOG =
            java.util.logging.Logger.getLogger(AgentSystemInfo.class.getName());

    /**
     * OSHI 入口
    */
    private final SystemInfo si = new SystemInfo();
    /**
     * 操作系统信息
    */
    private final OperatingSystem os = si.getOperatingSystem();
    /**
     * 硬件抽象层
    */
    private final HardwareAbstractionLayer hal = si.getHardware();
    /**
     * JSON 序列化器
    */
    private final ObjectMapper json = new ObjectMapper();

    /**
     * CPU 采样最小窗口（毫秒）：窗口短于此值增量无意义
     */
    private static final long CPU_WINDOW_MS = 500L;

    /**
     * 上次 CPU ticks，使用率需两次采样的增量
     */
    private long[] cpuTicks;

    /**
     * 上次 CPU 采样时刻
     */
    private long cpuSampleAt;

    /**
     * 上次算出的 CPU 使用率，窗口不足时沿用
     */
    private double cpuCache;

    /**
     * 上次磁盘 IO 累计字节与采样时刻：[readBytes, writeBytes, epochMs]，首帧为 null
     */
    private long[] diskIoPrev;

    /**
     * PowerShell 输出的 Base64 负载
    */
    private static final Pattern BASE64_PAYLOAD = Pattern.compile("[A-Za-z0-9+/]{40,}={0,2}");

    /**
     * 采集主机基础信息（主机名 / 系统 / CPU / 内存 / IP / MAC）。
     *
     * @return JSON 字符串
     * @throws Exception 序列化失败
     */
    public String info(AgentConfig config) throws Exception {
        OperatingSystem.OSVersionInfo version = null;
        try {
            version = os.getVersionInfo();
        } catch (Exception ignored) {
            // 部分 Windows 版本取 build 会抛异常，概览信息不应因此整体失败
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("hostname", os.getNetworkParams().getHostName());
        m.put("os", os.getFamily());
        m.put("osVersion", version == null ? "" : nullToEmpty(version.getVersion()));
        m.put("osBuild", version == null ? "" : nullToEmpty(version.getCodeName()));
        m.put("arch", System.getProperty("os.arch"));
        m.put("cpuCores", hal.getProcessor().getLogicalProcessorCount());
        m.put("memoryMb", hal.getMemory().getTotal() / 1024 / 1024);
        m.put("diskTotalGb", round1(totalDiskBytes() / 1024.0 / 1024 / 1024));
        m.put("publicIp", "");
        m.put("privateIp", getPrimaryIp());
        m.put("mac", getPrimaryMac());
        m.put("javaVersion", System.getProperty("java.version"));
        m.put("agentVersion", config == null ? "" : config.version());
        m.put("agentPort", config == null ? 0 : config.port());
        m.put("pid", os.getProcessId());
        m.put("uptimeSec", os.getSystemUptime());
        m.put("collectTime", nowText());
        return json.writeValueAsString(m);
    }

    /**
     * 采集全部文件系统的容量总和（字节）。
     *
     * @return 总容量，取不到时为 0
     */
    private long totalDiskBytes() {
        long total = 0L;
        try {
            for (OSFileStore store : os.getFileSystem().getFileStores()) {
                total += store.getTotalSpace();
            }
        } catch (Exception ignored) {
            // 只读挂载点异常不影响其余字段
        }
        return total;
    }

    /**
     * 空串归一。
     *
     * @param text 文本
     * @return 非 null 文本
     */
    private String nullToEmpty(String text) {
        return text == null ? "" : text;
    }

    /**
     * 保留一位小数。
     *
     * @param value 数值
     * @return 结果
     */
    private double round1(double value) {
        return Math.round(value * 10) / 10.0;
    }

    /**
     * 采集时间戳（平台侧据此判断画像数据新鲜度）。
     *
     * @return yyyy-MM-dd HH:mm:ss
     */
    private String nowText() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    /**
     * 获取PrimaryIP。
     *
     * @return 结果字符串
     */
    private String getPrimaryIp() {
        try {
            for (NetworkIF nif : hal.getNetworkIFs()) {
                if (!nif.getName().startsWith("lo") && nif.getIPv4addr().length > 0) {
                    return nif.getIPv4addr()[0];
                }
            }
        } catch (Exception e) {
            LOG.log(java.util.logging.Level.WARNING, "[AgentSystemInfo] Failed to get primary IP", e);
        }
        return "127.0.0.1";
    }

    /**
     * 获取PrimaryMac。
     *
     * @return 结果字符串
     */
    private String getPrimaryMac() {
        try {
            for (NetworkIF nif : hal.getNetworkIFs()) {
                if (!nif.getName().startsWith("lo") && nif.getMacaddr() != null && !nif.getMacaddr().isEmpty()) {
                    return nif.getMacaddr();
                }
            }
        } catch (Exception e) {
            LOG.log(java.util.logging.Level.WARNING, "[AgentSystemInfo] Failed to get primary MAC", e);
        }
        return "";
    }

    /**
     * 采集实时指标（CPU / 内存 / 磁盘使用率）。
     *
     * @return JSON 字符串
     * @throws Exception 序列化失败
     */
    public String metrics() throws Exception {
        Map<String, Object> m = new LinkedHashMap<>();
        
        // CPU 使用率
        m.put("cpuPercent", cpuPercent());
        
        // 内存使用率
        GlobalMemory mem = hal.getMemory();
        long memTotal = mem.getTotal();
        long memUsed = memTotal - mem.getAvailable();
        double memPct = memTotal > 0 ? (memUsed * 100.0 / memTotal) : 0;
        m.put("memoryTotalMb", memTotal / 1024 / 1024);
        m.put("memoryUsedMb", memUsed / 1024 / 1024);
        m.put("memoryPercent", Math.round(memPct * 10) / 10.0);
        
        // 磁盘使用率
        long diskTotal = 0;
        long diskUsed = 0;
        List<OSFileStore> fsList = os.getFileSystem().getFileStores();
        for (OSFileStore fs : fsList) {
            diskTotal += fs.getTotalSpace();
            diskUsed += fs.getTotalSpace() - fs.getUsableSpace();
        }
        double diskPct = diskTotal > 0 ? (diskUsed * 100.0 / diskTotal) : 0;
        m.put("diskTotalGb", round1(diskTotal / 1024.0 / 1024 / 1024));
        m.put("diskUsedGb", round1(diskUsed / 1024.0 / 1024 / 1024));
        m.put("diskPercent", Math.round(diskPct * 10) / 10.0);

        // 磁盘读写速率（KB/s，跨调用两次采样）
        double[] dio = diskIoKbs();
        m.put("diskReadKbs", dio[0]);
        m.put("diskWriteKbs", dio[1]);

        m.put("processCount", os.getProcessCount());
        m.put("threadCount", os.getThreadCount());
        m.put("uptimeSec", os.getSystemUptime());
        m.put("collectTime", nowText());

        return json.writeValueAsString(m);
    }

    /**
     * 计算 CPU 使用率：取上次采样到此刻的 ticks 增量。
     *
     * <p>OSHI 的 {@code getSystemCpuLoadBetweenTicks} 需要真正的历史基线，用同一次采样当基线算出的
     * 增量恒为 0，因此这里跨调用保存 ticks。首次调用阻塞一个最小窗口，避免上报 0。</p>
     *
     * @return 百分比，保留一位小数
     */
    private double cpuPercent() {
        CentralProcessor processor = hal.getProcessor();
        synchronized (this) {
            long now = System.currentTimeMillis();
            if (cpuTicks == null) {
                cpuTicks = processor.getSystemCpuLoadTicks();
                cpuSampleAt = now;
                try {
                    TimeUnit.MILLISECONDS.sleep(CPU_WINDOW_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                now = System.currentTimeMillis();
            }
            if (now - cpuSampleAt < CPU_WINDOW_MS) {
                return cpuCache;
            }
            double load = processor.getSystemCpuLoadBetweenTicks(cpuTicks);
            cpuTicks = processor.getSystemCpuLoadTicks();
            cpuSampleAt = System.currentTimeMillis();
            if (load < 0 || Double.isNaN(load)) {
                return cpuCache;
            }
            cpuCache = Math.round(load * 1000) / 10.0;
            return cpuCache;
        }
    }

    /**
     * 计算磁盘读写速率（KB/s）：HWDiskStore 累计字节两次采样增量。
     *
     * @return 长度 2 数组 [读 KB/s, 写 KB/s]，首帧为 0
     */
    private double[] diskIoKbs() {
        double[] z = {0.0, 0.0};
        long readBytes = 0L, writeBytes = 0L;
        for (HWDiskStore disk : hal.getDiskStores()) {
            if (disk.updateAttributes()) {
                readBytes += disk.getReadBytes();
                writeBytes += disk.getWriteBytes();
            }
        }
        long now = System.currentTimeMillis();
        if (diskIoPrev != null) {
            double dtSec = (now - diskIoPrev[2]) / 1000.0;
            if (dtSec > 0) {
                z[0] = Math.round(Math.max(0, readBytes - diskIoPrev[0]) / 1024.0 / dtSec * 10) / 10.0;
                z[1] = Math.round(Math.max(0, writeBytes - diskIoPrev[1]) / 1024.0 / dtSec * 10) / 10.0;
            }
        }
        diskIoPrev = new long[]{readBytes, writeBytes, now};
        return z;
    }

    /**
     * 分页采集进程列表（按 CPU 占用倒序）。
     *
     * @param page     页码
     * @param pageSize 每页条数
     * @return JSON 字符串
     * @throws Exception 序列化失败
     */
    public String processes(int page, int pageSize) throws Exception {
        List<OSProcess> procs = os.getProcesses(null, OperatingSystem.ProcessSorting.CPU_DESC, Integer.MAX_VALUE);
        List<Map<String, Object>> list = new ArrayList<>();
        java.text.SimpleDateFormat start = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int from = (page - 1) * pageSize;
        int to = Math.min(from + pageSize, procs.size());
        for (int i = from; i < to; i++) {
            OSProcess p = procs.get(i);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("pid", p.getProcessID());
            m.put("ppid", p.getParentProcessID());
            m.put("cpu", String.format("%.1f", p.getProcessCpuLoadCumulative() * 100));
            m.put("mem", String.format("%.1f", (double) p.getResidentSetSize() / hal.getMemory().getTotal() * 100));
            m.put("rss", p.getResidentSetSize() / 1024);
            m.put("name", p.getName());
            m.put("user", nullToEmpty(p.getUser()));
            m.put("threads", p.getThreadCount());
            m.put("path", nullToEmpty(p.getPath()));
            m.put("startTime", p.getStartTime() > 0 ? start.format(new Date(p.getStartTime())) : "");
            m.put("args", p.getCommandLine());
            list.add(m);
        }
        Map<String, Object> page1 = new LinkedHashMap<>();
        page1.put("list", list);
        page1.put("total", procs.size());
        return json.writeValueAsString(page1);
    }

    /**
     * services。
     *
     * @param page 页，不允许为 null
     * @param pageSize 页大小，不允许为 null
     * @return 结果字符串
     * @throws Exception 当执行过程不满足前置条件时
     */
    public String services(int page, int pageSize) throws Exception {
        List<Map<String, Object>> rows = serviceRows();
        int from = Math.max(0, (page - 1) * pageSize);
        int to = Math.min(from + pageSize, rows.size());
        List<Map<String, Object>> list = from >= to ? new ArrayList<>() : new ArrayList<>(rows.subList(from, to));
        Map<String, Object> page2 = new LinkedHashMap<>();
        page2.put("list", list);
        page2.put("total", rows.size());
        return json.writeValueAsString(page2);
    }

    /**
     * 全量服务行：Windows 取 Win32_Service 的真实服务键，其余平台回落 OSHI。
     *
     * @return 每行含 name/displayName/state/startType/pid
     */
    private List<Map<String, Object>> serviceRows() {
        List<Map<String, Object>> windows = windowsServiceRows();
        if (windows != null) {
            return windows;
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (OSService s : os.getServices()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", s.getName());
            m.put("displayName", s.getName());
            m.put("state", s.getState().name());
            m.put("startType", "");
            m.put("pid", s.getProcessID());
            out.add(m);
        }
        return out;
    }

    /**
     * 用 Win32_Service 枚举 Windows 服务（制表符分隔的原始行）。
     *
     * @return 成功返回行列表；非 Windows 或执行失败返回 null，由调用方回落 OSHI
     */
    private List<Map<String, Object>> windowsServiceRows() {
        if (!"Windows".equalsIgnoreCase(os.getFamily())) {
            return null;
        }
        String ps = "$t=[char]9; $rows=@(Get-CimInstance -ClassName Win32_Service | ForEach-Object { "
                + "$_.Name + $t + $_.DisplayName + $t + $_.State + $t + $_.StartMode + $t + $_.ProcessId }); "
                + "[Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes(($rows -join \"`n\")))";
        Process process = null;
        try {
            process = new ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive",
                    "-EncodedCommand", AgentMain.encodePowerShell(ps))
                    .redirectErrorStream(true)
                    .start();
            ByteArrayOutputStream sink = new ByteArrayOutputStream();
            Thread pump = AgentMain.pumpOutput(process.getInputStream(), sink);
            if (!process.waitFor(60, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                pump.join(AgentMain.PUMP_DRAIN_MILLIS);
                System.err.println("[Agent] Win32_Service 枚举超时，回落 OSHI");
                return null;
            }
            pump.join(AgentMain.PUMP_DRAIN_MILLIS);
            String out = AgentMain.decodeOutput(AgentMain.snapshot(sink));
            String payload = longestBase64Line(out);
            if (payload == null) {
                System.err.println("[Agent] Win32_Service 枚举失败 exit=" + process.exitValue()
                        + " out=" + abbreviate(out) + "，回落 OSHI");
                return null;
            }
            String text = new String(Base64.getDecoder().decode(payload), StandardCharsets.UTF_8);
            List<Map<String, Object>> rows = new ArrayList<>();
            for (String line : text.split("\n")) {
                String[] p = line.split("\t", -1);
                if (p.length < 5 || p[0].trim().isEmpty()) {
                    continue;
                }
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("name", p[0].trim());
                m.put("displayName", p[1].trim());
                m.put("state", p[2].trim().toUpperCase(Locale.ROOT));
                m.put("startType", p[3].trim());
                m.put("pid", parseLong(p[4]));
                rows.add(m);
            }
            return rows.isEmpty() ? null : rows;
        } catch (Exception e) {
            System.err.println("[Agent] Win32_Service 枚举异常，回落 OSHI: " + e);
            return null;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    /**
     * 从合并了 stderr 的输出里取出最长的纯 Base64 串（脚本负载）。
     *
     * @param out 子进程输出
     * @return Base64 负载，不存在返回 null
     */
    private String longestBase64Line(String out) {
        if (out == null || out.isEmpty()) {
            return null;
        }
        String best = null;
        Matcher matcher = BASE64_PAYLOAD.matcher(out);
        while (matcher.find()) {
            String candidate = matcher.group();
            if (best == null || candidate.length() > best.length()) {
                best = candidate;
            }
        }
        return best;
    }

    /**
     * 截断日志文本。
     *
     * @param text 方法入参 text
     * @return 最长 300 字符的文本
     */
    private String abbreviate(String text) {
        if (text == null) {
            return "";
        }
        return text.length() <= 300 ? text : text.substring(0, 300);
    }

    /**
     * 解析数字文本。
     *
     * @param text 方法入参 text
     * @return long 结果
     */
    private long parseLong(String text) {
        try {
            return Long.parseLong(text.trim());
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * disks。
     *
     * @return 结果字符串
     * @throws Exception 当执行过程不满足前置条件时
     */
    public String disks() throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        for (OSFileStore store : os.getFileSystem().getFileStores()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("mount", store.getMount());
            m.put("name", store.getName());
            long total = store.getTotalSpace();
            long free = store.getUsableSpace();
            m.put("size", formatBytes(total));
            m.put("used", formatBytes(total - free));
            m.put("avail", formatBytes(free));
            m.put("use%", total > 0 ? (int)((total - free) * 100 / total) + "%" : "0%");
            list.add(m);
        }
        return json.writeValueAsString(list);
    }

    /**
     * network。
     *
     * @return 结果字符串
     * @throws Exception 当执行过程不满足前置条件时
     */
    public String network() throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        for (NetworkIF nif : hal.getNetworkIFs()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("ifname", nif.getName());
            m.put("displayName", nif.getDisplayName());
            m.put("status", operStatus(nif));
            m.put("mac", nif.getMacaddr());
            m.put("ipv4", String.join(";", nif.getIPv4addr()));
            m.put("ipv6", String.join(";", nif.getIPv6addr()));
            m.put("mtu", nif.getMTU());
            m.put("speed", nif.getSpeed());
            list.add(m);
        }
        return json.writeValueAsString(list);
    }

    /**
     * 网卡操作态归一：确证在线才报 UP，确证不可用才报 DOWN，其余留给 UNKNOWN。
     *
     * @param nif 网卡
     * @return {@code UP}、{@code DOWN} 或 {@code UNKNOWN}
     */
    private static String operStatus(NetworkIF nif) {
        NetworkIF.IfOperStatus op = nif.getIfOperStatus();
        if (op == null) {
            return "UNKNOWN";
        }
        switch (op) {
            case UP:
                return "UP";
            case DOWN:
            case NOT_PRESENT:
            case LOWER_LAYER_DOWN:
                return "DOWN";
            default:
                return "UNKNOWN";
        }
    }

    /**
     * 采集监听端口表（TCP LISTEN + UDP 绑定），供主机画像「端口」页使用。
     *
     * @return JSON 字符串（proto/local/foreign/state/port/pid/pidName）
     * @throws Exception 序列化失败
     */
    public String ports() throws Exception {
        Map<Integer, String> nameCache = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            for (InternetProtocolStats.IPConnection c : os.getInternetProtocolStats().getConnections()) {
                String type = nullToEmpty(c.getType()).toUpperCase();
                boolean tcp = type.startsWith("TCP");
                InternetProtocolStats.TcpState state = c.getState();
                // 只看本机监听面：TCP 收 LISTEN，UDP 无连接状态全收
                if (tcp && state != InternetProtocolStats.TcpState.LISTEN) {
                    continue;
                }
                if (c.getLocalPort() <= 0) {
                    continue;
                }
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("proto", tcp ? "tcp" : "udp");
                m.put("local", addressText(c.getLocalAddress()));
                m.put("port", c.getLocalPort());
                m.put("state", tcp && state != null ? state.name() : "UNCONNECTED");
                int pid = c.getowningProcessId();
                m.put("pid", pid);
                m.put("pidName", processName(pid, nameCache));
                list.add(m);
            }
        } catch (Exception e) {
            // 无权限或平台不支持时返回空表而不是 500，前端据此显示"不可用"
            return "[]";
        }
        return json.writeValueAsString(list);
    }

    /**
     * byte[] 地址转可读 IP（OSHI 6.8 直接给字节数组）。
     *
     * @param bytes 地址字节
     * @return IP 文本，异常时返回空串
     */
    private String addressText(byte[] bytes) {
        if (bytes == null || (bytes.length != 4 && bytes.length != 16)) {
            return "";
        }
        try {
            return java.net.InetAddress.getByAddress(bytes).getHostAddress();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 取端口归属进程名（同一次采集内按 pid 复用查询结果）。
     *
     * @param pid    进程号
     * @param cached 进程名缓存
     * @return 进程名，取不到时为空串
     */
    private String processName(int pid, Map<Integer, String> cached) {
        if (pid <= 0) {
            return "";
        }
        String hit = cached.get(pid);
        if (hit != null) {
            return hit;
        }
        String name = "";
        try {
            OSProcess process = os.getProcess(pid);
            if (process != null && process.getName() != null) {
                name = process.getName();
            }
        } catch (Exception ignored) {
            // 进程可能刚好退出
        }
        cached.put(pid, name);
        return name;
    }

    /**
     * 全量网络连接与占用进程（管控端 /ms/system/netproc 的数据源）。
     *
     * @return 结果字符串
     * @throws Exception 当执行过程不满足前置条件时
     */
    public String netProc() throws Exception {
        Map<Integer, String> nameCache = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            for (InternetProtocolStats.IPConnection c : os.getInternetProtocolStats().getConnections()) {
                String proto = nullToEmpty(c.getType());
                String localAddr = addressText(c.getLocalAddress());
                String remoteAddr = addressText(c.getForeignAddress());
                int localPort = c.getLocalPort();
                int remotePort = c.getForeignPort();
                InternetProtocolStats.TcpState state = c.getState();
                boolean tcp = proto.toUpperCase().startsWith("TCP");
                String stateText = tcp && state != null ? state.name() : "UNCONN";
                int pid = c.getowningProcessId();
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("protocol", proto);
                m.put("type", proto);
                m.put("local", localAddr + ":" + localPort);
                m.put("localAddr", localAddr);
                m.put("localPort", localPort);
                m.put("remote", remoteAddr + ":" + remotePort);
                m.put("remoteAddr", remoteAddr);
                m.put("remotePort", remotePort);
                m.put("port", localPort);
                m.put("state", stateText);
                m.put("pid", pid);
                m.put("process", processName(pid, nameCache));
                list.add(m);
            }
        } catch (Exception e) {
            // 无权限或平台不支持时返回空表而不是 500，前端据此显示"不可用"
            return "[]";
        }
        return json.writeValueAsString(list);
    }


    /**
     * 格式化字节数组。
     *
     * @param b 方法入参 b
     * @return 结果字符串
     */
    private String formatBytes(long b) {
        if (b < 1024) {
            return b + " B";
        }
        if (b < 1048576) {
            return String.format("%.1f K", b / 1024.0);
        }
        if (b < 1073741824) {
            return String.format("%.1f M", b / 1048576.0);
        }
        return String.format("%.1f G", b / 1073741824.0);
    }
}
