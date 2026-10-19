package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.system.pojo.monitor.ServerMonitorVO;
import com.chua.starter.system.support.restful.RestfulStatsCollector;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 服务监控接口
 *
 * <p>基于 OSHI 采集服务器硬件(CPU/内存/磁盘)与 JVM 运行信息。</p>
 *
 * @since 4.0.0.50
 * @author CH
 */
@Tag(name = "服务监控接口")
@RestController
@RequestMapping("/v2/server")
@RequiredArgsConstructor
@Slf4j
public class SysServerController {

    private final Environment environment;

    /**
     * 获取服务监控信息
     *
     * @return 服务监控信息
     */
    @Operation(summary = "获取服务监控信息")
    @GetMapping("/info")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<ServerMonitorVO> info() {
        try {
            return ReturnResult.ok(buildInfo());
        } catch (Exception e) {
            log.error("采集服务监控信息失败", e);
            return ReturnResult.illegal("获取服务监控信息失败: " + e.getMessage());
        }
    }

    /**
     * 服务监控信息 SSE 推送（每 1 秒一次）。
     *
     * @return SseEmitter
     */
    @Operation(summary = "服务监控信息流式推送")
    @PostMapping(value = "/info/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter infoStream() {
        SseEmitter emitter = new SseEmitter(0L);
        CompletableFuture.runAsync(() -> {
            try {
                while (true) {
                    emitter.send(SseEmitter.event().name("metrics").data(buildInfo(), MediaType.APPLICATION_JSON));
                    Thread.sleep(1000L);
                }
            } catch (Exception e) {
                emitter.complete();
            }
        });
        return emitter;
    }

    /**
     * RESTful 实时统计（方案 A 旁路：常规请求趋势 + QPS + Top URL）。
     *
     * @return 统计快照
     */
    @Operation(summary = "RESTful 实时统计")
    @GetMapping("/api/stats")
    public ReturnResult<Map<String, Object>> apiStats() {
        return ReturnResult.ok(RestfulStatsCollector.getInstance().snapshot());
    }

    /**
     * 采集服务监控信息(REST 与 SSE 共用)。
     *
     * @return 服务监控信息
     */
    private ServerMonitorVO buildInfo() {
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hw = si.getHardware();
        OperatingSystem os = si.getOperatingSystem();
        CentralProcessor cpu = hw.getProcessor();
        GlobalMemory memory = hw.getMemory();

        // ---- CPU: 两次采样间隔 500ms ----
        long[] ticks1 = cpu.getSystemCpuLoadTicks();
        long[][] procTicks1 = cpu.getProcessorCpuLoadTicks();
        sleepQuietly(500L);
        long[] ticks2 = cpu.getSystemCpuLoadTicks();
        long[] delta = new long[ticks2.length];
        long total = 0L;
        for (int i = 0; i < ticks2.length; i++) {
            delta[i] = ticks2[i] - ticks1[i];
            if (delta[i] < 0) {
                delta[i] = 0;
            }
            total += delta[i];
        }
        double user = pct(delta, CentralProcessor.TickType.USER, total);
        double system = pct(delta, CentralProcessor.TickType.SYSTEM, total);
        double idle = pct(delta, CentralProcessor.TickType.IDLE, total);
        ServerMonitorVO.CpuInfo cpuInfo = new ServerMonitorVO.CpuInfo(
                cpu.getLogicalProcessorCount(), user, system, idle, round2(100d - idle));

        // ---- 每核使用率 ----
        List<Double> coreUsages = null;
        try {
            double[] perCore = cpu.getProcessorCpuLoadBetweenTicks(procTicks1);
            if (null != perCore) {
                coreUsages = new ArrayList<>(perCore.length);
                for (double c : perCore) {
                    coreUsages.add(Double.isNaN(c) ? 0d : round2(c * 100d));
                }
            }
        } catch (Exception e) {
            log.warn("采集每核 CPU 使用率失败", e);
        }

        // ---- 系统负载 ----
        Double systemLoadAverage = null;
        try {
            double[] loadAvg = cpu.getSystemLoadAverage(1);
            if (null != loadAvg && loadAvg.length > 0 && loadAvg[0] >= 0) {
                systemLoadAverage = round2(loadAvg[0]);
            }
        } catch (Exception e) {
            log.warn("采集系统负载失败", e);
        }

        // ---- 系统内存 ----
        long memTotal = memory.getTotal();
        long memAvail = memory.getAvailable();
        long memUsed = memTotal - memAvail;
        ServerMonitorVO.MemInfo sysMem = new ServerMonitorVO.MemInfo(
                memTotal, memUsed, memAvail, memTotal > 0 ? round2(memUsed * 100.0 / memTotal) : 0);

        // ---- JVM 内存 ----
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = memoryMXBean.getHeapMemoryUsage();
        long jvmTotal = heap.getCommitted();
        long jvmUsed = heap.getUsed();
        ServerMonitorVO.MemInfo jvmMem = new ServerMonitorVO.MemInfo(
                jvmTotal, jvmUsed, jvmTotal - jvmUsed, jvmTotal > 0 ? round2(jvmUsed * 100.0 / jvmTotal) : 0);

        // ---- 磁盘 ----
        List<ServerMonitorVO.DiskInfo> disks = new ArrayList<>();
        double peak = 0d;
        List<OSFileStore> stores = os.getFileSystem().getFileStores();
        for (OSFileStore store : stores) {
            long totalSpace = store.getTotalSpace();
            if (totalSpace <= 0) {
                continue;
            }
            long usable = store.getUsableSpace();
            long usedSpace = totalSpace - usable;
            double usage = round2(usedSpace * 100.0 / totalSpace);
            disks.add(new ServerMonitorVO.DiskInfo(store.getMount(), usage, store.getType(),
                    totalSpace, usedSpace, usable));
            if (usage > peak) {
                peak = usage;
            }
        }
        ServerMonitorVO.DiskPeak diskPeak = new ServerMonitorVO.DiskPeak(peak, disks.size());

        // ---- 服务器信息 ----
        ServerMonitorVO.ServerInfo server = new ServerMonitorVO.ServerInfo(
                os.getNetworkParams().getHostName(),
                os.getFamily(),
                System.getProperty("os.version"),
                resolveLocalIp(),
                System.getProperty("os.arch"));

        // ---- JVM 信息 ----
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        ServerMonitorVO.JvmInfo jvm = new ServerMonitorVO.JvmInfo(
                runtimeMXBean.getVmName(),
                System.getProperty("java.version"),
                runtimeMXBean.getStartTime(),
                runtimeMXBean.getUptime(),
                System.getProperty("java.home"),
                System.getProperty("user.dir"),
                new ArrayList<>(runtimeMXBean.getInputArguments()));

        // ---- 运行时信息 ----
        Long systemUptime = null;
        Long processUptime = null;
        Integer processCount = null;
        Integer threadCount = null;
        Integer peakThreadCount = null;
        Integer daemonThreadCount = null;
        try {
            systemUptime = os.getSystemUptime();
        } catch (Exception e) {
            log.warn("采集系统运行时长失败", e);
        }
        try {
            processUptime = runtimeMXBean.getUptime() / 1000L;
        } catch (Exception e) {
            log.warn("采集进程运行时长失败", e);
        }
        try {
            processCount = os.getProcessCount();
        } catch (Exception e) {
            log.warn("采集进程数失败", e);
        }
        ThreadMXBean tmb = ManagementFactory.getThreadMXBean();
        try {
            threadCount = tmb.getThreadCount();
            peakThreadCount = tmb.getPeakThreadCount();
            daemonThreadCount = tmb.getDaemonThreadCount();
        } catch (Exception e) {
            log.warn("采集线程数失败", e);
        }

        // ---- JVM 内存池 ----
        List<ServerMonitorVO.MemoryPoolInfo> memoryPools = new ArrayList<>();
        try {
            for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
                String name = pool.getName();
                if (null == name || name.contains("Shenandoah")) {
                    continue;
                }
                MemoryUsage u = pool.getUsage();
                memoryPools.add(new ServerMonitorVO.MemoryPoolInfo(
                        name,
                        pool.getType().name().equalsIgnoreCase("HEAP") ? "heap" : "non-heap",
                        u == null ? null : u.getInit(),
                        u == null ? null : u.getUsed(),
                        u == null ? null : u.getCommitted(),
                        u == null ? null : u.getMax()));
            }
        } catch (Exception e) {
            log.warn("采集内存池失败", e);
        }

        // ---- GC ----
        List<ServerMonitorVO.GcInfo> gcCollectors = new ArrayList<>();
        try {
            for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
                long count = gc.getCollectionCount();
                long time = gc.getCollectionTime();
                gcCollectors.add(new ServerMonitorVO.GcInfo(
                        gc.getName(),
                        count < 0 ? null : count,
                        time < 0 ? null : time));
            }
        } catch (Exception e) {
            log.warn("采集 GC 信息失败", e);
        }

        // ---- 类加载 ----
        Integer loadedClassCount = null;
        Long totalLoadedClassCount = null;
        Long unloadedClassCount = null;
        try {
            ClassLoadingMXBean cl = ManagementFactory.getClassLoadingMXBean();
            loadedClassCount = cl.getLoadedClassCount();
            totalLoadedClassCount = cl.getTotalLoadedClassCount();
            unloadedClassCount = cl.getUnloadedClassCount();
        } catch (Exception e) {
            log.warn("采集类加载信息失败", e);
        }

        // ---- 线程状态分析 ----
        Map<String, Integer> threadStates = new LinkedHashMap<>();
        int deadlockedThreadCount = 0;
        List<ServerMonitorVO.ThreadDetail> topThreads = new ArrayList<>();
        try {
            boolean cpuSupported = tmb.isThreadCpuTimeSupported();
            int dumpCount = 0;
            List<Map.Entry<Long, ServerMonitorVO.ThreadDetail>> top = new ArrayList<>();
            ThreadInfo[] infos = tmb.dumpAllThreads(false, false);
            for (ThreadInfo info : infos) {
                if (null == info) {
                    continue;
                }
                Thread.State st = info.getThreadState();
                if (null != st) {
                    threadStates.merge(st.name(), 1, Integer::sum);
                }
                if (dumpCount++ >= 200) {
                    continue;
                }
                long cpuNanos = -1, userNanos = -1;
                if (cpuSupported) {
                    long tid = info.getThreadId();
                    cpuNanos = tmb.getThreadCpuTime(tid);
                    userNanos = tmb.getThreadUserTime(tid);
                }
                long cpuMs = cpuNanos < 0 ? 0L : cpuNanos / 1_000_000L;
                top.add(Map.entry(cpuMs,
                        new ServerMonitorVO.ThreadDetail(info.getThreadName(),
                                null == st ? null : st.name(),
                                cpuNanos < 0 ? null : cpuNanos / 1_000_000L,
                                userNanos < 0 ? null : userNanos / 1_000_000L)));
            }
            try {
                long[] dead = tmb.findDeadlockedThreads();
                deadlockedThreadCount = (null == dead || dead.length == 0) ? 0 : dead.length;
            } catch (Exception ignored) {
                // ignore
            }
            top.sort((a, b) -> Long.compare(b.getKey(), a.getKey()));
            for (int i = 0; i < Math.min(10, top.size()); i++) {
                topThreads.add(top.get(i).getValue());
            }
        } catch (Exception e) {
            log.warn("采集线程分析失败", e);
        }

        // ---- 网络接口 ----
        List<ServerMonitorVO.NetworkInfo> networkInterfaces = new ArrayList<>();
        try {
            List<NetworkIF> ifs = hw.getNetworkIFs();
            for (NetworkIF nif : ifs) {
                long recv = nif.getBytesRecv();
                long sent = nif.getBytesSent();
                if (recv == 0 && sent == 0) {
                    continue;
                }
                List<String> addrs = new ArrayList<>();
                try {
                    String[] v4 = nif.getIPv4addr();
                    if (null != v4) {
                        Collections.addAll(addrs, v4);
                    }
                    String[] v6 = nif.getIPv6addr();
                    if (null != v6) {
                        for (String s : v6) {
                            if (addrs.size() >= 5) {
                                break;
                            }
                            addrs.add(s);
                        }
                    }
                } catch (Exception ignored) {
                    // ignore
                }
                networkInterfaces.add(new ServerMonitorVO.NetworkInfo(
                        nif.getName(), nif.getDisplayName(),
                        recv, sent,
                        nif.getPacketsRecv(), nif.getPacketsSent(),
                        nif.getSpeed(),
                        addrs,
                        nif.getInErrors() + nif.getInDrops(),
                        nif.getOutErrors()));
                if (networkInterfaces.size() >= 5) {
                    break;
                }
            }
        } catch (Exception e) {
            log.warn("采集网络接口失败", e);
        }

        // ---- Spring 核心参数 ----
        Map<String, String> springConfig = new LinkedHashMap<>();
        try {
            String[] active = environment.getActiveProfiles();
            if (null != active && active.length > 0) {
                springConfig.put("spring.profiles.active", String.join(",", active));
            }
            String[] defaultProfiles = environment.getDefaultProfiles();
            if (null != defaultProfiles && defaultProfiles.length > 0) {
                springConfig.put("spring.profiles.default", String.join(",", defaultProfiles));
            }
            String[] keys = {
                    "spring.application.name",
                    "server.port",
                    "spring.datasource.druid.enabled",
                    "spring.redis.host",
                    "spring.jpa.open-in-view",
                    "server.tomcat.min-spare-threads",
                    "management.endpoints.web.exposure.include",
                    "management.endpoint.health.show-details",
                    "management.server.port",
                    "logging.level.root",
                    "logging.file.path",
                    "spring.main.web-application-type",
                    "spring.jmx.enabled",
                    "spring.task.scheduling.pool.size",
                    "spring.task.execution.pool.core-size",
                    "spring.datasource.url",
                    "spring.datasource.driver-class-name",
                    "spring.data.redis.host",
                    "spring.data.redis.port",
                    "spring.data.redis.database",
                    "spring.servlet.multipart.max-file-size",
                    "spring.servlet.multipart.max-request-size",
                    "spring.jackson.default-property-inclusion",
                    "springdoc.swagger-ui.path",
                    "mybatis.configuration.map-underscore-to-camel-case",
            };
            for (String key : keys) {
                String v = environment.getProperty(key);
                if (null != v && !v.isEmpty()) {
                    springConfig.put(key, v);
                }
            }
        } catch (Exception e) {
            log.warn("采集 Spring 核心参数失败", e);
        }

        return new ServerMonitorVO(
                cpuInfo, sysMem, jvmMem, diskPeak, disks, server, jvm,
                systemUptime, processUptime, processCount, threadCount, peakThreadCount, daemonThreadCount,
                systemLoadAverage, coreUsages,
                memoryPools, gcCollectors,
                loadedClassCount, totalLoadedClassCount, unloadedClassCount,
                threadStates, deadlockedThreadCount, topThreads,
                networkInterfaces, springConfig);
    }

    /**
     * 计算某类 tick 占比
     *
     * @param delta 差值数组
     * @param type  tick 类型
     * @param total 总和
     * @return 百分比
     */
    private double pct(long[] delta, CentralProcessor.TickType type, long total) {
        if (total <= 0) {
            return 0d;
        }
        return round2(delta[type.getIndex()] * 100.0 / total);
    }

    /**
     * 取首个非回环 IPv4
     *
     * @return ip
     */
    private String resolveLocalIp() {
        try {
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            if (null != nis) {
                while (nis.hasMoreElements()) {
                    NetworkInterface ni = nis.nextElement();
                    if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) {
                        continue;
                    }
                    Enumeration<InetAddress> addrs = ni.getInetAddresses();
                    while (addrs.hasMoreElements()) {
                        InetAddress addr = addrs.nextElement();
                        String host = addr.getHostAddress();
                        if (!addr.isLoopbackAddress() && host.indexOf(':') < 0) {
                            return host;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("解析本机 IP 失败", e);
        }
        return "127.0.0.1";
    }

    /**
     * 静默 sleep
     *
     * @param ms 毫秒
     */
    private void sleepQuietly(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 保留两位小数
     *
     * @param v 原值
     * @return 保留两位
     */
    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
