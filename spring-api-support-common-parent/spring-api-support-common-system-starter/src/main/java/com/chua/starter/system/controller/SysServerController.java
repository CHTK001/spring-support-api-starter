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
import org.springframework.core.env.Environment;

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
import java.util.HashMap;
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
        ServerMonitorVO vo = new ServerMonitorVO();
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hw = si.getHardware();
        OperatingSystem os = si.getOperatingSystem();

        // ---- CPU: 两次采样间隔 500ms ----
        CentralProcessor cpu = hw.getProcessor();
        long[] ticks1 = cpu.getSystemCpuLoadTicks();
        long[][] procTicks1 = cpu.getProcessorCpuLoadTicks();
        sleepQuietly(500L);
        long[] ticks2 = cpu.getSystemCpuLoadTicks();
        ServerMonitorVO.CpuInfo cpuInfo = new ServerMonitorVO.CpuInfo();
        cpuInfo.setLogicalCores(cpu.getLogicalProcessorCount());
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
        cpuInfo.setUserPercent(user);
        cpuInfo.setSystemPercent(system);
        cpuInfo.setIdlePercent(idle);
        cpuInfo.setUsagePercent(round2(100d - idle));
        vo.setCpu(cpuInfo);

        // ---- 每核使用率(复用同一次 500ms 采样) + 系统负载 ----
        try {
            double[] perCore = cpu.getProcessorCpuLoadBetweenTicks(procTicks1);
            if (null != perCore) {
                List<Double> coreUsages = new ArrayList<>(perCore.length);
                for (double c : perCore) {
                    coreUsages.add(Double.isNaN(c) ? 0d : round2(c * 100d));
                }
                vo.setCoreUsages(coreUsages);
            }
        } catch (Exception e) {
            log.warn("采集每核 CPU 使用率失败", e);
        }
        try {
            double[] loadAvg = cpu.getSystemLoadAverage(1);
            if (null != loadAvg && loadAvg.length > 0 && loadAvg[0] >= 0) {
                vo.setSystemLoadAverage(round2(loadAvg[0]));
            }
        } catch (Exception e) {
            log.warn("采集系统负载失败", e);
        }

        // ---- 系统内存 ----
        GlobalMemory memory = hw.getMemory();
        long memTotal = memory.getTotal();
        long memAvail = memory.getAvailable();
        long memUsed = memTotal - memAvail;
        ServerMonitorVO.MemInfo sysMem = new ServerMonitorVO.MemInfo();
        sysMem.setTotal(memTotal);
        sysMem.setUsed(memUsed);
        sysMem.setFree(memAvail);
        sysMem.setUsagePercent(memTotal > 0 ? round2(memUsed * 100.0 / memTotal) : 0);
        vo.setSystemMemory(sysMem);

        // ---- JVM 内存 ----
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = memoryMXBean.getHeapMemoryUsage();
        long jvmTotal = heap.getCommitted();
        long jvmUsed = heap.getUsed();
        long jvmFree = jvmTotal - jvmUsed;
        ServerMonitorVO.MemInfo jvmMem = new ServerMonitorVO.MemInfo();
        jvmMem.setTotal(jvmTotal);
        jvmMem.setUsed(jvmUsed);
        jvmMem.setFree(jvmFree);
        jvmMem.setUsagePercent(jvmTotal > 0 ? round2(jvmUsed * 100.0 / jvmTotal) : 0);
        vo.setJvmMemory(jvmMem);

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
            ServerMonitorVO.DiskInfo disk = new ServerMonitorVO.DiskInfo();
            disk.setPath(store.getMount());
            disk.setFsType(store.getType());
            disk.setTotal(totalSpace);
            disk.setUsed(usedSpace);
            disk.setAvailable(usable);
            disk.setUsagePercent(usage);
            disks.add(disk);
            if (usage > peak) {
                peak = usage;
            }
        }
        vo.setDisks(disks);
        ServerMonitorVO.DiskPeak diskPeak = new ServerMonitorVO.DiskPeak();
        diskPeak.setUsagePercent(peak);
        diskPeak.setPartitionCount(disks.size());
        vo.setDiskPeak(diskPeak);

        // ---- 服务器信息 ----
        ServerMonitorVO.ServerInfo server = new ServerMonitorVO.ServerInfo();
        server.setHostname(os.getNetworkParams().getHostName());
        server.setOsName(os.getFamily());
        server.setOsVersion(System.getProperty("os.version"));
        server.setArch(System.getProperty("os.arch"));
        server.setIp(resolveLocalIp());
        vo.setServer(server);

        // ---- JVM 信息 ----
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        ServerMonitorVO.JvmInfo jvm = new ServerMonitorVO.JvmInfo();
        jvm.setName(runtimeMXBean.getVmName());
        jvm.setVersion(System.getProperty("java.version"));
        jvm.setStartTime(runtimeMXBean.getStartTime());
        jvm.setUptime(runtimeMXBean.getUptime());
        jvm.setJavaHome(System.getProperty("java.home"));
        jvm.setProjectPath(System.getProperty("user.dir"));
        jvm.setInputArgs(new ArrayList<>(runtimeMXBean.getInputArguments()));
        vo.setJvm(jvm);

        // ---- 系统/进程运行信息 ----
        collectRuntimeInfo(vo, os, cpu, runtimeMXBean);

        // ---- JVM 内存池 ----
        collectMemoryPools(vo);

        // ---- GC ----
        collectGc(vo);

        // ---- 类加载 ----
        collectClassLoading(vo);

        // ---- 线程状态分析 ----
        collectThreads(vo);

        // ---- 网络接口 ----
        collectNetwork(vo, hw);

        // ---- Spring 核心参数 ----
        collectSpringConfig(vo);

        return vo;
    }

    /**
     * 采集系统/进程运行信息
     */
    private void collectRuntimeInfo(ServerMonitorVO vo, OperatingSystem os, CentralProcessor cpu,
                                    RuntimeMXBean runtimeMXBean) {
        try {
            long uptimeSec = os.getSystemUptime();
            vo.setSystemUptime(uptimeSec);
        } catch (Exception e) {
            log.warn("采集系统运行时长失败", e);
        }
        try {
            vo.setProcessUptime(runtimeMXBean.getUptime() / 1000L);
        } catch (Exception e) {
            log.warn("采集进程运行时长失败", e);
        }
        ThreadMXBean tmb = ManagementFactory.getThreadMXBean();
        try {
            vo.setProcessCount(os.getProcessCount());
        } catch (Exception e) {
            log.warn("采集进程数失败", e);
        }
        try {
            vo.setThreadCount(tmb.getThreadCount());
            vo.setPeakThreadCount(tmb.getPeakThreadCount());
            vo.setDaemonThreadCount(tmb.getDaemonThreadCount());
        } catch (Exception e) {
            log.warn("采集线程数失败", e);
        }
    }

    /**
     * 采集 JVM 内存池
     */
    private void collectMemoryPools(ServerMonitorVO vo) {
        try {
            List<ServerMonitorVO.MemoryPoolInfo> pools = new ArrayList<>();
            for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
                String name = pool.getName();
                if (null == name || name.contains("Shenandoah")) {
                    continue;
                }
                ServerMonitorVO.MemoryPoolInfo p = new ServerMonitorVO.MemoryPoolInfo();
                p.setName(name);
                p.setType(pool.getType().name().equalsIgnoreCase("HEAP") ? "heap" : "non-heap");
                MemoryUsage u = pool.getUsage();
                if (null != u) {
                    p.setInit(u.getInit());
                    p.setUsed(u.getUsed());
                    p.setCommitted(u.getCommitted());
                    p.setMax(u.getMax());
                }
                pools.add(p);
            }
            vo.setMemoryPools(pools);
        } catch (Exception e) {
            log.warn("采集内存池失败", e);
        }
    }

    /**
     * 采集 GC 收集器
     */
    private void collectGc(ServerMonitorVO vo) {
        try {
            List<ServerMonitorVO.GcInfo> gcs = new ArrayList<>();
            for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
                ServerMonitorVO.GcInfo g = new ServerMonitorVO.GcInfo();
                g.setName(gc.getName());
                long count = gc.getCollectionCount();
                long time = gc.getCollectionTime();
                g.setCollectionCount(count < 0 ? null : count);
                g.setCollectionTime(time < 0 ? null : time);
                gcs.add(g);
            }
            vo.setGcCollectors(gcs);
        } catch (Exception e) {
            log.warn("采集 GC 信息失败", e);
        }
    }

    /**
     * 采集类加载
     */
    private void collectClassLoading(ServerMonitorVO vo) {
        try {
            ClassLoadingMXBean cl = ManagementFactory.getClassLoadingMXBean();
            vo.setLoadedClassCount(cl.getLoadedClassCount());
            vo.setTotalLoadedClassCount(cl.getTotalLoadedClassCount());
            vo.setUnloadedClassCount(cl.getUnloadedClassCount());
        } catch (Exception e) {
            log.warn("采集类加载信息失败", e);
        }
    }

    /**
     * 采集线程状态分析
     */
    private void collectThreads(ServerMonitorVO vo) {
        try {
            ThreadMXBean tmb = ManagementFactory.getThreadMXBean();
            // 状态分布
            Map<String, Integer> states = new LinkedHashMap<>();
            boolean cpuSupported = tmb.isThreadCpuTimeSupported();
            int dumpCount = 0;
            // 候选线程: (cpuTime, detail)
            List<Map.Entry<Long, ServerMonitorVO.ThreadDetail>> top = new ArrayList<>();
            ThreadInfo[] infos = tmb.dumpAllThreads(false, false);
            for (ThreadInfo info : infos) {
                if (null == info) {
                    continue;
                }
                Thread.State st = info.getThreadState();
                if (null != st) {
                    states.merge(st.name(), 1, Integer::sum);
                }
                if (dumpCount++ >= 200) {
                    continue;
                }
                ServerMonitorVO.ThreadDetail d = new ServerMonitorVO.ThreadDetail();
                d.setName(info.getThreadName());
                d.setState(null == st ? null : st.name());
                if (cpuSupported) {
                    long tid = info.getThreadId();
                    long cpuNanos = tmb.getThreadCpuTime(tid);
                    long userNanos = tmb.getThreadUserTime(tid);
                    d.setCpuTime(cpuNanos < 0 ? null : cpuNanos / 1_000_000L);
                    d.setUserTime(userNanos < 0 ? null : userNanos / 1_000_000L);
                    long cpuMs = cpuNanos < 0 ? 0L : cpuNanos / 1_000_000L;
                    top.add(Map.entry(cpuMs, d));
                }
            }
            vo.setThreadStates(states);
            // 死锁
            try {
                long[] dead = tmb.findDeadlockedThreads();
                vo.setDeadlockedThreadCount(null == dead ? 0 : dead.length);
            } catch (Exception e) {
                vo.setDeadlockedThreadCount(0);
            }
            // 前10 CPU 时间
            top.sort((a, b) -> Long.compare(b.getKey(), a.getKey()));
            List<ServerMonitorVO.ThreadDetail> topList = new ArrayList<>();
            for (int i = 0; i < Math.min(10, top.size()); i++) {
                topList.add(top.get(i).getValue());
            }
            vo.setTopThreads(topList);
        } catch (Exception e) {
            log.warn("采集线程分析失败", e);
        }
    }

    /**
     * 采集网络接口
     */
    private void collectNetwork(ServerMonitorVO vo, HardwareAbstractionLayer hw) {
        try {
            List<ServerMonitorVO.NetworkInfo> nics = new ArrayList<>();
            List<NetworkIF> ifs = hw.getNetworkIFs();
            for (NetworkIF nif : ifs) {
                long recv = nif.getBytesRecv();
                long sent = nif.getBytesSent();
                if (recv == 0 && sent == 0) {
                    continue;
                }
                ServerMonitorVO.NetworkInfo n = new ServerMonitorVO.NetworkInfo();
                n.setName(nif.getName());
                n.setDisplayName(nif.getDisplayName());
                n.setBytesRecv(recv);
                n.setBytesSent(sent);
                n.setPacketsRecv(nif.getPacketsRecv());
                n.setPacketsSent(nif.getPacketsSent());
                n.setSpeed(nif.getSpeed());
                n.setDropRecv(nif.getInErrors() + nif.getInDrops());
                n.setDropSent(nif.getOutErrors());
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
                } catch (Exception ignore) {
                    // ignore
                }
                n.setAddresses(addrs);
                nics.add(n);
                if (nics.size() >= 5) {
                    break;
                }
            }
            vo.setNetworkInterfaces(nics);
        } catch (Exception e) {
            log.warn("采集网络接口失败", e);
        }
    }

    /**
     * 采集 Spring 核心参数（白名单前缀 + 常用键）。
     */
    private void collectSpringConfig(ServerMonitorVO vo) {
        try {
            Map<String, String> cfg = new LinkedHashMap<>();
            String[] active = environment.getActiveProfiles();
            if (null != active && active.length > 0) {
                cfg.put("spring.profiles.active", String.join(",", active));
            }
            String[] defaultProfiles = environment.getDefaultProfiles();
            if (null != defaultProfiles && defaultProfiles.length > 0) {
                cfg.put("spring.profiles.default", String.join(",", defaultProfiles));
            }
            // 常用核心键
            String[] keys = {
                    "spring.application.name",
                    "spring.application.index",
                    "server.port",
                    "server.address",
                    "server.servlet.context-path",
                    "server.tomcat.max-threads",
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
                    cfg.put(key, v);
                }
            }
            vo.setSpringConfig(cfg);
        } catch (Exception e) {
            log.warn("采集 Spring 核心参数失败", e);
        }
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
