package com.chua.agent;

import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.util.*;

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
        } catch (Exception ignored) {}
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
        } catch (Exception ignored) {}
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
        CentralProcessor cpu = hal.getProcessor();
        double cpuPct = cpu.getSystemCpuLoadBetweenTicks(cpu.getSystemCpuLoadTicks()) * 100;
        m.put("cpuPercent", Math.round(cpuPct * 10) / 10.0);
        
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

        m.put("processCount", os.getProcessCount());
        m.put("threadCount", os.getThreadCount());
        m.put("uptimeSec", os.getSystemUptime());
        m.put("collectTime", nowText());

        return json.writeValueAsString(m);
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
        List<OSService> services = os.getServices();
        List<Map<String, Object>> list = new ArrayList<>();
        int from = (page - 1) * pageSize;
        int to = Math.min(from + pageSize, services.size());
        for (int i = from; i < to; i++) {
            OSService s = services.get(i);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", s.getName());
            m.put("status", s.getState().name());
            m.put("pid", s.getProcessID());
            list.add(m);
        }
        Map<String, Object> page2 = new LinkedHashMap<>();
        page2.put("list", list);
        page2.put("total", services.size());
        return json.writeValueAsString(page2);
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
            m.put("mac", nif.getMacaddr());
            m.put("ipv4", String.join(",", nif.getIPv4addr()));
            m.put("ipv6", String.join(",", nif.getIPv6addr()));
            m.put("mtu", nif.getMTU());
            list.add(m);
        }
        return json.writeValueAsString(list);
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
     * 网卡累计收发计数（rxBytes/txBytes），注意这不是端口表。
     *
     * @return 结果字符串
     * @throws Exception 当执行过程不满足前置条件时
     */
    public String netProc() throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        for (NetworkIF nif : hal.getNetworkIFs()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("ifname", nif.getName());
            m.put("rxBytes", nif.getBytesRecv());
            m.put("txBytes", nif.getBytesSent());
            list.add(m);
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
