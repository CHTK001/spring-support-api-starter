package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.system.pojo.jvm.JvmInfo;
import com.sun.management.OperatingSystemMXBean;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.RequestParam;

import java.lang.management.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JVM 信息控制器
 *
 * @author CH
 * @since 2024/12/18
 */
@Slf4j
@RestController
@RequestMapping("/v2/jvm")
@Tag(name = "JVM 信息管理")
public class SysJvmController {

    /**
     * 获取 JVM 信息
     *
     * @return JVM 信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取 JVM 信息")
    public ReturnResult<JvmInfo> getJvmInfo() {
        JvmInfo jvmInfo = new JvmInfo();

        // 运行时信息
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        jvmInfo.setJvmName(runtimeMXBean.getVmName());
        jvmInfo.setJvmVersion(runtimeMXBean.getVmVersion());
        jvmInfo.setJvmVendor(runtimeMXBean.getVmVendor());
        jvmInfo.setJavaVersion(System.getProperty("java.version"));
        jvmInfo.setJavaHome(System.getProperty("java.home"));
        jvmInfo.setStartTime(runtimeMXBean.getStartTime());
        jvmInfo.setUptime(runtimeMXBean.getUptime());
        jvmInfo.setInputArguments(runtimeMXBean.getInputArguments());

        // 内存信息
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        jvmInfo.setHeapMemory(convertMemoryUsage(memoryMXBean.getHeapMemoryUsage()));
        jvmInfo.setNonHeapMemory(convertMemoryUsage(memoryMXBean.getNonHeapMemoryUsage()));

        // 内存池信息
        List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
        List<JvmInfo.MemoryPoolInfo> memoryPools = new ArrayList<>();
        for (MemoryPoolMXBean pool : memoryPoolMXBeans) {
            JvmInfo.MemoryPoolInfo poolInfo = new JvmInfo.MemoryPoolInfo();
            poolInfo.setName(pool.getName());
            poolInfo.setType(pool.getType().name());
            poolInfo.setUsage(convertMemoryUsage(pool.getUsage()));
            poolInfo.setPeakUsage(convertMemoryUsage(pool.getPeakUsage()));
            memoryPools.add(poolInfo);
        }
        jvmInfo.setMemoryPools(memoryPools);

        // 线程信息
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        JvmInfo.ThreadInfo threadInfo = new JvmInfo.ThreadInfo();
        threadInfo.setThreadCount(threadMXBean.getThreadCount());
        threadInfo.setPeakThreadCount(threadMXBean.getPeakThreadCount());
        threadInfo.setDaemonThreadCount(threadMXBean.getDaemonThreadCount());
        threadInfo.setTotalStartedThreadCount(threadMXBean.getTotalStartedThreadCount());
        long[] deadlockedThreads = threadMXBean.findDeadlockedThreads();
        threadInfo.setDeadlockedThreadCount(deadlockedThreads != null ? deadlockedThreads.length : 0);
        jvmInfo.setThreadInfo(threadInfo);

        // 线程 CPU 分析
        jvmInfo.setTopCpuThreads(getTopCpuThreads(threadMXBean, 20));

        // 类加载信息
        ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
        JvmInfo.ClassLoadingInfo classLoadingInfo = new JvmInfo.ClassLoadingInfo();
        classLoadingInfo.setLoadedClassCount(classLoadingMXBean.getLoadedClassCount());
        classLoadingInfo.setTotalLoadedClassCount(classLoadingMXBean.getTotalLoadedClassCount());
        classLoadingInfo.setUnloadedClassCount(classLoadingMXBean.getUnloadedClassCount());
        jvmInfo.setClassLoadingInfo(classLoadingInfo);

        // GC 信息
        List<GarbageCollectorMXBean> gcMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
        List<JvmInfo.GarbageCollectorInfo> garbageCollectors = new ArrayList<>();
        for (GarbageCollectorMXBean gc : gcMXBeans) {
            JvmInfo.GarbageCollectorInfo gcInfo = new JvmInfo.GarbageCollectorInfo();
            gcInfo.setName(gc.getName());
            gcInfo.setCollectionCount(gc.getCollectionCount());
            gcInfo.setCollectionTime(gc.getCollectionTime());
            gcInfo.setMemoryPoolNames(gc.getMemoryPoolNames());
            garbageCollectors.add(gcInfo);
        }
        jvmInfo.setGarbageCollectors(garbageCollectors);

        // 操作系统信息
        java.lang.management.OperatingSystemMXBean osMXBean = ManagementFactory.getOperatingSystemMXBean();
        JvmInfo.OsInfo osInfo = new JvmInfo.OsInfo();
        osInfo.setName(osMXBean.getName());
        osInfo.setVersion(osMXBean.getVersion());
        osInfo.setArch(osMXBean.getArch());
        osInfo.setAvailableProcessors(osMXBean.getAvailableProcessors());
        osInfo.setSystemLoadAverage(osMXBean.getSystemLoadAverage());

        if (osMXBean instanceof OperatingSystemMXBean) {
            OperatingSystemMXBean sunOsMXBean = (OperatingSystemMXBean) osMXBean;
            osInfo.setTotalPhysicalMemory(sunOsMXBean.getTotalMemorySize());
            osInfo.setFreePhysicalMemory(sunOsMXBean.getFreeMemorySize());
            osInfo.setTotalSwapSpace(sunOsMXBean.getTotalSwapSpaceSize());
            osInfo.setFreeSwapSpace(sunOsMXBean.getFreeSwapSpaceSize());

            // CPU 信息
            JvmInfo.CpuInfo cpuInfo = new JvmInfo.CpuInfo();
            cpuInfo.setProcessCpuLoad(sunOsMXBean.getProcessCpuLoad() * 100);
            cpuInfo.setSystemCpuLoad(sunOsMXBean.getCpuLoad() * 100);
            cpuInfo.setProcessCpuTime(sunOsMXBean.getProcessCpuTime());
            jvmInfo.setCpuInfo(cpuInfo);
            
            // 文件描述符信息 (Unix系统)
            try {
                if (sunOsMXBean instanceof com.sun.management.UnixOperatingSystemMXBean) {
                    com.sun.management.UnixOperatingSystemMXBean unixOsMXBean = 
                        (com.sun.management.UnixOperatingSystemMXBean) sunOsMXBean;
                    JvmInfo.FileDescriptorInfo fdInfo = new JvmInfo.FileDescriptorInfo();
                    fdInfo.setOpenFileDescriptorCount(unixOsMXBean.getOpenFileDescriptorCount());
                    fdInfo.setMaxFileDescriptorCount(unixOsMXBean.getMaxFileDescriptorCount());
                    if (unixOsMXBean.getMaxFileDescriptorCount() > 0) {
                        fdInfo.setUsagePercent((double) unixOsMXBean.getOpenFileDescriptorCount() 
                            / unixOsMXBean.getMaxFileDescriptorCount() * 100);
                    }
                    jvmInfo.setFileDescriptorInfo(fdInfo);
                }
            } catch (Exception e) {
                // Windows系统不支持
            }
        }
        jvmInfo.setOsInfo(osInfo);
        
        // 编译器信息
        CompilationMXBean compilationMXBean = ManagementFactory.getCompilationMXBean();
        if (compilationMXBean != null) {
            JvmInfo.CompilationInfo compilationInfo = new JvmInfo.CompilationInfo();
            compilationInfo.setName(compilationMXBean.getName());
            compilationInfo.setCompilationTimeMonitoringSupported(compilationMXBean.isCompilationTimeMonitoringSupported());
            if (compilationMXBean.isCompilationTimeMonitoringSupported()) {
                compilationInfo.setTotalCompilationTime(compilationMXBean.getTotalCompilationTime());
            }
            jvmInfo.setCompilationInfo(compilationInfo);
        }
        
        // 运行时信息
        JvmInfo.RuntimeInfo runtimeInfo = new JvmInfo.RuntimeInfo();
        runtimeInfo.setPid(String.valueOf(ProcessHandle.current().pid()));
        runtimeInfo.setClassPath(runtimeMXBean.getClassPath());
        runtimeInfo.setLibraryPath(runtimeMXBean.getLibraryPath());
        runtimeInfo.setManagementSpecVersion(runtimeMXBean.getManagementSpecVersion());
        runtimeInfo.setSpecName(runtimeMXBean.getSpecName());
        runtimeInfo.setSpecVendor(runtimeMXBean.getSpecVendor());
        runtimeInfo.setSpecVersion(runtimeMXBean.getSpecVersion());
        try {
            runtimeInfo.setBootClassPath(runtimeMXBean.getBootClassPath());
        } catch (UnsupportedOperationException e) {
            // Java 9+ 可能不支持
        }
        jvmInfo.setRuntimeInfo(runtimeInfo);
        
        // 缓冲池信息
        try {
            List<java.lang.management.BufferPoolMXBean> bufferPoolMXBeans = 
                ManagementFactory.getPlatformMXBeans(java.lang.management.BufferPoolMXBean.class);
            List<JvmInfo.BufferPoolInfo> bufferPools = new ArrayList<>();
            for (java.lang.management.BufferPoolMXBean pool : bufferPoolMXBeans) {
                JvmInfo.BufferPoolInfo poolInfo = new JvmInfo.BufferPoolInfo();
                poolInfo.setName(pool.getName());
                poolInfo.setCount(pool.getCount());
                poolInfo.setMemoryUsed(pool.getMemoryUsed());
                poolInfo.setTotalCapacity(pool.getTotalCapacity());
                bufferPools.add(poolInfo);
            }
            jvmInfo.setBufferPools(bufferPools);
        } catch (Exception e) {
            log.warn("获取缓冲池信息失败: {}", e.getMessage());
        }
        
        // 线程状态分布
        JvmInfo.ThreadStateDistribution threadStateDist = new JvmInfo.ThreadStateDistribution();
        int newCount = 0, runnableCount = 0, blockedCount = 0, waitingCount = 0, timedWaitingCount = 0, terminatedCount = 0;
        long[] allThreadIds = threadMXBean.getAllThreadIds();
        java.lang.management.ThreadInfo[] allThreadInfos = threadMXBean.getThreadInfo(allThreadIds);
        for (java.lang.management.ThreadInfo ti : allThreadInfos) {
            if (ti != null) {
                switch (ti.getThreadState()) {
                    case NEW: newCount++; break;
                    case RUNNABLE: runnableCount++; break;
                    case BLOCKED: blockedCount++; break;
                    case WAITING: waitingCount++; break;
                    case TIMED_WAITING: timedWaitingCount++; break;
                    case TERMINATED: terminatedCount++; break;
                }
            }
        }
        threadStateDist.setNewCount(newCount);
        threadStateDist.setRunnableCount(runnableCount);
        threadStateDist.setBlockedCount(blockedCount);
        threadStateDist.setWaitingCount(waitingCount);
        threadStateDist.setTimedWaitingCount(timedWaitingCount);
        threadStateDist.setTerminatedCount(terminatedCount);
        threadStateDist.setTotalCount(allThreadInfos.length);
        jvmInfo.setThreadStateDistribution(threadStateDist);
        
        // JIT编译统计
        if (compilationMXBean != null) {
            JvmInfo.JitCompilationStats jitStats = new JvmInfo.JitCompilationStats();
            jitStats.setCompilerName(compilationMXBean.getName());
            jitStats.setCompilationTimeMonitoringSupported(compilationMXBean.isCompilationTimeMonitoringSupported());
            if (compilationMXBean.isCompilationTimeMonitoringSupported()) {
                long totalTime = compilationMXBean.getTotalCompilationTime();
                jitStats.setTotalCompilationTime(totalTime);
                // 估计编译方法数(根据加载的类数和编译时间估算)
                long loadedClasses = classLoadingMXBean.getLoadedClassCount();
                // 假设平均每个类有5个方法，约有30%被编译
                long estimatedMethods = (long) (loadedClasses * 5 * 0.3);
                jitStats.setCompiledMethodCount(estimatedMethods);
                if (estimatedMethods > 0) {
                    jitStats.setAvgCompilationTime((double) totalTime / estimatedMethods);
                }
            }
            jvmInfo.setJitCompilationStats(jitStats);
        }
        
        // 代码缓存信息
        for (MemoryPoolMXBean pool : memoryPoolMXBeans) {
            String poolName = pool.getName().toLowerCase();
            if (poolName.contains("code") && poolName.contains("cache")) {
                JvmInfo.CodeCacheInfo codeCacheInfo = new JvmInfo.CodeCacheInfo();
                codeCacheInfo.setName(pool.getName());
                MemoryUsage usage = pool.getUsage();
                codeCacheInfo.setUsed(usage.getUsed());
                codeCacheInfo.setCommitted(usage.getCommitted());
                codeCacheInfo.setMax(usage.getMax());
                if (usage.getMax() > 0) {
                    double usagePercent = (double) usage.getUsed() / usage.getMax() * 100;
                    codeCacheInfo.setUsagePercent(usagePercent);
                    codeCacheInfo.setNearFull(usagePercent > 80);
                } else {
                    codeCacheInfo.setNearFull(false);
                }
                jvmInfo.setCodeCacheInfo(codeCacheInfo);
                break;
            }
        }
        
        // GC统计信息
        JvmInfo.GcStats gcStats = new JvmInfo.GcStats();
        long totalGcCount = 0;
        long totalGcTime = 0;
        long youngGcCount = 0, youngGcTime = 0;
        long oldGcCount = 0, oldGcTime = 0;
        String lastGcName = null;
        for (GarbageCollectorMXBean gc : gcMXBeans) {
            String gcName = gc.getName().toLowerCase();
            long count = gc.getCollectionCount();
            long time = gc.getCollectionTime();
            totalGcCount += count;
            totalGcTime += time;
            lastGcName = gc.getName();
            // 区分Young/Old GC
            if (gcName.contains("young") || gcName.contains("minor") || gcName.contains("scavenge") 
                || gcName.contains("parnew") || gcName.contains("copy") || gcName.contains("g1 young")) {
                youngGcCount += count;
                youngGcTime += time;
            } else if (gcName.contains("old") || gcName.contains("major") || gcName.contains("marksweep") 
                || gcName.contains("cms") || gcName.contains("g1 old") || gcName.contains("g1 mixed")) {
                oldGcCount += count;
                oldGcTime += time;
            }
        }
        gcStats.setTotalCollectionCount(totalGcCount);
        gcStats.setTotalCollectionTime(totalGcTime);
        gcStats.setYoungGcCount(youngGcCount);
        gcStats.setYoungGcTime(youngGcTime);
        gcStats.setOldGcCount(oldGcCount);
        gcStats.setOldGcTime(oldGcTime);
        gcStats.setLastGcName(lastGcName);
        if (totalGcCount > 0) {
            gcStats.setAvgCollectionTime((double) totalGcTime / totalGcCount);
        }
        long uptimeMs = runtimeMXBean.getUptime();
        if (uptimeMs > 0) {
            gcStats.setGcPerMinute((double) totalGcCount / (uptimeMs / 60000.0));
            gcStats.setGcTimePercent((double) totalGcTime / uptimeMs * 100);
        }
        jvmInfo.setGcStats(gcStats);
        
        // 元空间信息
        for (MemoryPoolMXBean pool : memoryPoolMXBeans) {
            String poolName = pool.getName().toLowerCase();
            if (poolName.contains("metaspace")) {
                JvmInfo.MetaspaceInfo metaspaceInfo = new JvmInfo.MetaspaceInfo();
                MemoryUsage usage = pool.getUsage();
                metaspaceInfo.setUsed(usage.getUsed());
                metaspaceInfo.setCommitted(usage.getCommitted());
                metaspaceInfo.setMax(usage.getMax());
                if (usage.getMax() > 0) {
                    metaspaceInfo.setUsagePercent((double) usage.getUsed() / usage.getMax() * 100);
                } else if (usage.getCommitted() > 0) {
                    metaspaceInfo.setUsagePercent((double) usage.getUsed() / usage.getCommitted() * 100);
                }
                jvmInfo.setMetaspaceInfo(metaspaceInfo);
            } else if (poolName.contains("compressed class space")) {
                JvmInfo.MetaspaceInfo metaspaceInfo = jvmInfo.getMetaspaceInfo();
                if (metaspaceInfo == null) {
                    metaspaceInfo = new JvmInfo.MetaspaceInfo();
                    jvmInfo.setMetaspaceInfo(metaspaceInfo);
                }
                MemoryUsage usage = pool.getUsage();
                metaspaceInfo.setCompressedClassSpaceUsed(usage.getUsed());
                metaspaceInfo.setCompressedClassSpaceCommitted(usage.getCommitted());
                metaspaceInfo.setCompressedClassSpaceMax(usage.getMax());
                if (usage.getMax() > 0) {
                    metaspaceInfo.setCompressedClassSpaceUsagePercent((double) usage.getUsed() / usage.getMax() * 100);
                }
            }
        }
        
        // 直接内存信息
        JvmInfo.DirectMemoryInfo directMemoryInfo = new JvmInfo.DirectMemoryInfo();
        try {
            List<java.lang.management.BufferPoolMXBean> bufferPoolMXBeans2 = 
                ManagementFactory.getPlatformMXBeans(java.lang.management.BufferPoolMXBean.class);
            for (java.lang.management.BufferPoolMXBean pool : bufferPoolMXBeans2) {
                String poolName = pool.getName().toLowerCase();
                if (poolName.contains("direct")) {
                    directMemoryInfo.setUsed(pool.getMemoryUsed());
                    directMemoryInfo.setTotalCapacity(pool.getTotalCapacity());
                    directMemoryInfo.setCount(pool.getCount());
                } else if (poolName.contains("mapped")) {
                    directMemoryInfo.setMappedUsed(pool.getMemoryUsed());
                    directMemoryInfo.setMappedCapacity(pool.getTotalCapacity());
                    directMemoryInfo.setMappedCount(pool.getCount());
                }
            }
            // 尝试获取MaxDirectMemorySize
            try {
                Class<?> vmClass = Class.forName("sun.misc.VM");
                java.lang.reflect.Method method = vmClass.getDeclaredMethod("maxDirectMemory");
                long maxDirect = (Long) method.invoke(null);
                directMemoryInfo.setMaxDirectMemory(maxDirect);
                if (maxDirect > 0 && directMemoryInfo.getUsed() != null) {
                    directMemoryInfo.setUsagePercent((double) directMemoryInfo.getUsed() / maxDirect * 100);
                }
            } catch (Exception e) {
                // 忽略
            }
        } catch (Exception e) {
            log.warn("获取直接内存信息失败: {}", e.getMessage());
        }
        jvmInfo.setDirectMemoryInfo(directMemoryInfo);
        
        // 内存分配速率
        JvmInfo.MemoryAllocationRate memoryAllocationRate = new JvmInfo.MemoryAllocationRate();
        for (MemoryPoolMXBean pool : memoryPoolMXBeans) {
            String poolName = pool.getName().toLowerCase();
            MemoryUsage usage = pool.getUsage();
            if (poolName.contains("eden")) {
                memoryAllocationRate.setEdenUsed(usage.getUsed());
                memoryAllocationRate.setEdenMax(usage.getMax() > 0 ? usage.getMax() : usage.getCommitted());
                if (memoryAllocationRate.getEdenMax() > 0) {
                    memoryAllocationRate.setEdenUsagePercent((double) usage.getUsed() / memoryAllocationRate.getEdenMax() * 100);
                }
            } else if (poolName.contains("survivor")) {
                Long currentSurvivor = memoryAllocationRate.getSurvivorUsed();
                memoryAllocationRate.setSurvivorUsed((currentSurvivor != null ? currentSurvivor : 0) + usage.getUsed());
                Long currentMax = memoryAllocationRate.getSurvivorMax();
                long maxVal = usage.getMax() > 0 ? usage.getMax() : usage.getCommitted();
                memoryAllocationRate.setSurvivorMax((currentMax != null ? currentMax : 0) + maxVal);
            } else if (poolName.contains("old") || poolName.contains("tenured")) {
                memoryAllocationRate.setOldUsed(usage.getUsed());
                memoryAllocationRate.setOldMax(usage.getMax() > 0 ? usage.getMax() : usage.getCommitted());
                if (memoryAllocationRate.getOldMax() > 0) {
                    memoryAllocationRate.setOldUsagePercent((double) usage.getUsed() / memoryAllocationRate.getOldMax() * 100);
                }
            }
        }
        // 估算分配速率(基于运行时间和Eden使用量)
        long uptimeSeconds = runtimeMXBean.getUptime() / 1000;
        if (uptimeSeconds > 0 && memoryAllocationRate.getEdenUsed() != null) {
            memoryAllocationRate.setAllocationRatePerSecond(memoryAllocationRate.getEdenUsed() / uptimeSeconds);
        }
        if (uptimeSeconds > 0 && memoryAllocationRate.getOldUsed() != null) {
            memoryAllocationRate.setPromotionRatePerSecond(memoryAllocationRate.getOldUsed() / uptimeSeconds);
        }
        jvmInfo.setMemoryAllocationRate(memoryAllocationRate);
        
        // 类加载详情
        ClassLoadingMXBean classLoadingMXBean1 = ManagementFactory.getClassLoadingMXBean();
        JvmInfo.ClassLoadingDetail classLoadingDetail = new JvmInfo.ClassLoadingDetail();
        classLoadingDetail.setCurrentLoadedCount(classLoadingMXBean1.getLoadedClassCount());
        classLoadingDetail.setTotalLoadedCount(classLoadingMXBean1.getTotalLoadedClassCount());
        classLoadingDetail.setUnloadedCount(classLoadingMXBean1.getUnloadedClassCount());
        classLoadingDetail.setVerbose(classLoadingMXBean1.isVerbose());
        // 计算每分钟加载/卸载速率
        double uptimeMinutes = runtimeMXBean.getUptime() / 60000.0;
        if (uptimeMinutes > 0) {
            classLoadingDetail.setLoadRatePerMinute(classLoadingMXBean1.getTotalLoadedClassCount() / uptimeMinutes);
            classLoadingDetail.setUnloadRatePerMinute(classLoadingMXBean1.getUnloadedClassCount() / uptimeMinutes);
        }
        jvmInfo.setClassLoadingDetail(classLoadingDetail);
        
        // 安全管理器信息 (SecurityManager在Java 17开始弃用，Java 21已移除)
        JvmInfo.SecurityInfo securityInfo = new JvmInfo.SecurityInfo();
        securityInfo.setSecurityManagerEnabled(false);
        securityInfo.setSecurityManagerClass(null);
        String policyFile = System.getProperty("java.security.policy");
        securityInfo.setPolicyFile(policyFile);
        jvmInfo.setSecurityInfo(securityInfo);

        return ReturnResult.ok(jvmInfo);
    }

    /**
     * 手动执行 GC
     *
     * @return 执行结果
     */
    @PostMapping("/gc")
    @Operation(summary = "手动执行 GC")
    public ReturnResult<String> triggerGc() {
        log.info("手动触发 GC");
        long beforeMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        System.gc();
        long afterMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long freedMemory = beforeMemory - afterMemory;
        String message = String.format("GC 执行完成，释放内存: %s MB", freedMemory / (1024 * 1024));
        log.info(message);
        return ReturnResult.ok(message);
    }

    /**
     * 获取线程详情列表
     *
     * @param topN 返回前 N 个线程
     * @return 线程详情列表
     */
    @GetMapping("/threads")
    @Operation(summary = "获取线程详情列表")
    public ReturnResult<List<JvmInfo.ThreadDetail>> getThreadDetails(
            @RequestParam(value = "topN", defaultValue = "50") Integer topN,
            @RequestParam(value = "includeStack", defaultValue = "false") Boolean includeStack) {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        List<JvmInfo.ThreadDetail> threads = getTopCpuThreads(threadMXBean, topN);
        
        // 如果需要堆栈信息
        if (includeStack) {
            long[] threadIds = threads.stream().mapToLong(JvmInfo.ThreadDetail::getThreadId).toArray();
            java.lang.management.ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(threadIds, 50);
            Map<Long, java.lang.management.ThreadInfo> threadInfoMap = new HashMap<>();
            for (java.lang.management.ThreadInfo info : threadInfos) {
                if (info != null) {
                    threadInfoMap.put(info.getThreadId(), info);
                }
            }
            for (JvmInfo.ThreadDetail detail : threads) {
                java.lang.management.ThreadInfo info = threadInfoMap.get(detail.getThreadId());
                if (info != null) {
                    StackTraceElement[] stackTrace = info.getStackTrace();
                    String[] stackTraceStrings = new String[stackTrace.length];
                    for (int i = 0; i < stackTrace.length; i++) {
                        stackTraceStrings[i] = stackTrace[i].toString();
                    }
                    detail.setStackTrace(stackTraceStrings);
                }
            }
        }
        return ReturnResult.ok(threads);
    }

    /**
     * 获取单个线程详细信息(包含堆栈)
     *
     * @param threadId 线程ID
     * @return 线程详情
     */
    @GetMapping("/thread")
    @Operation(summary = "获取单个线程详细信息")
    public ReturnResult<JvmInfo.ThreadDetail> getThreadDetail(@RequestParam("threadId") Long threadId) {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        java.lang.management.ThreadInfo info = threadMXBean.getThreadInfo(threadId, 100);
        if (info == null) {
            return ReturnResult.error("线程不存在或已结束");
        }
        
        JvmInfo.ThreadDetail detail = new JvmInfo.ThreadDetail();
        detail.setThreadId(info.getThreadId());
        detail.setThreadName(info.getThreadName());
        detail.setState(info.getThreadState().name());
        detail.setBlockedCount(info.getBlockedCount());
        detail.setBlockedTime(info.getBlockedTime());
        detail.setWaitedCount(info.getWaitedCount());
        detail.setWaitedTime(info.getWaitedTime());
        detail.setLockName(info.getLockName());
        detail.setLockOwnerId(info.getLockOwnerId());
        detail.setLockOwnerName(info.getLockOwnerName());
        
        // CPU 时间
        if (threadMXBean.isThreadCpuTimeSupported() && threadMXBean.isThreadCpuTimeEnabled()) {
            detail.setCpuTime(threadMXBean.getThreadCpuTime(threadId));
            detail.setUserTime(threadMXBean.getThreadUserTime(threadId));
        }
        
        // 线程属性
        Thread thread = findThreadById(threadId);
        if (thread != null) {
            detail.setDaemon(thread.isDaemon());
            detail.setPriority(thread.getPriority());
        }
        
        // 堆栈信息
        StackTraceElement[] stackTrace = info.getStackTrace();
        String[] stackTraceStrings = new String[stackTrace.length];
        for (int i = 0; i < stackTrace.length; i++) {
            stackTraceStrings[i] = stackTrace[i].toString();
        }
        detail.setStackTrace(stackTraceStrings);
        
        return ReturnResult.ok(detail);
    }

    /**
     * CPU 热点分析 - 通过采样堆栈分析哪些代码占用 CPU 最多
     *
     * @param sampleCount 采样次数
     * @param interval 采样间隔(毫秒)
     * @param topN 返回前 N 个热点
     * @return CPU 热点分析结果
     */
    @GetMapping("/hotspots")
    @Operation(summary = "CPU 热点分析")
    public ReturnResult<JvmInfo.CpuHotspotAnalysis> analyzeCpuHotspots(
            @RequestParam(value = "sampleCount", defaultValue = "20") Integer sampleCount,
            @RequestParam(value = "interval", defaultValue = "50") Integer interval,
            @RequestParam(value = "topN", defaultValue = "30") Integer topN,
            @RequestParam(value = "excludeIdle", defaultValue = "true") Boolean excludeIdle,
            @RequestParam(value = "packageFilter", required = false) String packageFilter) {
        
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        long startTime = System.currentTimeMillis();
        
        // 方法命中统计: key = "类名#方法名"
        Map<String, MethodHitInfo> methodHits = new HashMap<>();
        // 类命中统计
        Map<String, Integer> classHits = new HashMap<>();
        // 包命中统计
        Map<String, Set<String>> packageClasses = new HashMap<>();
        
        int totalSamples = 0;
        Set<Long> sampledThreads = new HashSet<>();
        
        // 多次采样
        for (int i = 0; i < sampleCount; i++) {
            long[] threadIds = threadMXBean.getAllThreadIds();
            java.lang.management.ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(threadIds, 50);
            
            for (java.lang.management.ThreadInfo info : threadInfos) {
                if (info == null) continue;
                
                // 排除空闲线程
                if (excludeIdle && isIdleThread(info)) {
                    continue;
                }
                
                sampledThreads.add(info.getThreadId());
                StackTraceElement[] stackTrace = info.getStackTrace();
                
                if (stackTrace.length == 0) continue;
                
                // 统计栈顶方法(正在执行的代码)
                for (int j = 0; j < Math.min(stackTrace.length, 10); j++) {
                    StackTraceElement element = stackTrace[j];
                    String className = element.getClassName();
                    
                    // 包过滤
                    if (packageFilter != null && !packageFilter.isEmpty()) {
                        if (!className.startsWith(packageFilter)) {
                            continue;
                        }
                    }
                    
                    String methodKey = className + "#" + element.getMethodName();
                    
                    // 方法统计
                    MethodHitInfo hitInfo = methodHits.computeIfAbsent(methodKey, k -> {
                        MethodHitInfo info2 = new MethodHitInfo();
                        info2.className = className;
                        info2.methodName = element.getMethodName();
                        info2.fileName = element.getFileName();
                        info2.lineNumber = element.getLineNumber();
                        return info2;
                    });
                    
                    // 栈顶方法权重更高
                    int weight = (j == 0) ? 3 : (j < 3) ? 2 : 1;
                    hitInfo.hitCount += weight;
                    
                    // 记录行号
                    if (element.getLineNumber() > 0) {
                        hitInfo.lineNumbers.merge(element.getLineNumber(), weight, Integer::sum);
                    }
                    
                    // 记录调用者
                    if (j < stackTrace.length - 1) {
                        String caller = stackTrace[j + 1].getClassName() + "#" + stackTrace[j + 1].getMethodName();
                        hitInfo.callers.merge(caller, 1, Integer::sum);
                    }
                    
                    // 类统计
                    classHits.merge(className, weight, Integer::sum);
                    
                    // 包统计
                    String packageName = getPackageName(className);
                    packageClasses.computeIfAbsent(packageName, k -> new HashSet<>()).add(className);
                }
                
                totalSamples++;
            }
            
            // 采样间隔
            if (i < sampleCount - 1) {
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        long endTime = System.currentTimeMillis();
        final int finalTotalSamples = totalSamples;
        
        // 构建结果
        JvmInfo.CpuHotspotAnalysis analysis = new JvmInfo.CpuHotspotAnalysis();
        analysis.setSampleCount(sampleCount);
        analysis.setSampleDuration(endTime - startTime);
        analysis.setSampleInterval(interval);
        analysis.setThreadCount(sampledThreads.size());
        
        // 计算总命中数
        int totalHits = methodHits.values().stream().mapToInt(h -> h.hitCount).sum();
        
        // 热点方法列表
        List<JvmInfo.CpuHotspot> hotspots = methodHits.values().stream()
                .sorted(Comparator.comparing(h -> -h.hitCount))
                .limit(topN)
                .map(h -> {
                    JvmInfo.CpuHotspot hotspot = new JvmInfo.CpuHotspot();
                    hotspot.setClassName(h.className);
                    hotspot.setMethodName(h.methodName);
                    hotspot.setFileName(h.fileName);
                    // 取最常见的行号
                    hotspot.setLineNumber(h.lineNumbers.entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse(h.lineNumber));
                    hotspot.setHitCount(h.hitCount);
                    hotspot.setPercentage(totalHits > 0 ? (double) h.hitCount / totalHits * 100 : 0);
                    hotspot.setFullMethod(h.className + "." + h.methodName + "()" + 
                            (h.fileName != null ? " (" + h.fileName + ":" + hotspot.getLineNumber() + ")" : ""));
                    // 调用者列表
                    hotspot.setCallers(h.callers.entrySet().stream()
                            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                            .limit(5)
                            .map(e -> e.getKey() + " (" + e.getValue() + "次)")
                            .collect(Collectors.toList()));
                    return hotspot;
                })
                .collect(Collectors.toList());
        analysis.setHotspots(hotspots);
        
        // 热点类统计
        int totalClassHits = classHits.values().stream().mapToInt(Integer::intValue).sum();
        Map<String, Long> classMethodCounts = methodHits.values().stream()
                .collect(Collectors.groupingBy(h -> h.className, Collectors.counting()));
        
        List<JvmInfo.ClassHotspot> classHotspots = classHits.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(20)
                .map(e -> {
                    JvmInfo.ClassHotspot ch = new JvmInfo.ClassHotspot();
                    ch.setClassName(e.getKey());
                    ch.setHitCount(e.getValue());
                    ch.setPercentage(totalClassHits > 0 ? (double) e.getValue() / totalClassHits * 100 : 0);
                    ch.setMethodCount(classMethodCounts.getOrDefault(e.getKey(), 0L).intValue());
                    return ch;
                })
                .collect(Collectors.toList());
        analysis.setClassHotspots(classHotspots);
        
        // 热点包统计
        Map<String, Integer> packageHits = new HashMap<>();
        for (Map.Entry<String, Integer> entry : classHits.entrySet()) {
            String pkg = getPackageName(entry.getKey());
            packageHits.merge(pkg, entry.getValue(), Integer::sum);
        }
        int totalPackageHits = packageHits.values().stream().mapToInt(Integer::intValue).sum();
        
        List<JvmInfo.PackageHotspot> packageHotspots = packageHits.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(15)
                .map(e -> {
                    JvmInfo.PackageHotspot ph = new JvmInfo.PackageHotspot();
                    ph.setPackageName(e.getKey());
                    ph.setHitCount(e.getValue());
                    ph.setPercentage(totalPackageHits > 0 ? (double) e.getValue() / totalPackageHits * 100 : 0);
                    ph.setClassCount(packageClasses.getOrDefault(e.getKey(), Collections.emptySet()).size());
                    return ph;
                })
                .collect(Collectors.toList());
        analysis.setPackageHotspots(packageHotspots);
        
        return ReturnResult.ok(analysis);
    }
    
    /**
     * 堆内存分析 - 分析哪些类占用内存最多
     *
     * @param topN 返回前 N 个
     * @param packageFilter 包名过滤
     * @return 堆内存分析结果
     */
    @GetMapping("/heap")
    @Operation(summary = "堆内存分析")
    public ReturnResult<JvmInfo.HeapAnalysis> analyzeHeap(
            @RequestParam(value = "topN", defaultValue = "50") Integer topN,
            @RequestParam(value = "packageFilter", required = false) String packageFilter) {
        
        JvmInfo.HeapAnalysis analysis = new JvmInfo.HeapAnalysis();
        analysis.setAnalysisTime(System.currentTimeMillis());
        
        // 获取堆内存使用情况
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        analysis.setHeapUsage(convertMemoryUsage(memoryMXBean.getHeapMemoryUsage()));
        
        // 尝试获取堆直方图
        try {
            // 使用 DiagnosticCommandMBean 执行 GC.class_histogram
            javax.management.MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            javax.management.ObjectName diagCmdName = new javax.management.ObjectName("com.sun.management:type=DiagnosticCommand");
            
            // 执行 gcClassHistogram 命令
            String[] emptyArgs = new String[0];
            String histogram = (String) server.invoke(diagCmdName, "gcClassHistogram", 
                    new Object[]{emptyArgs}, new String[]{String[].class.getName()});
            
            // 解析直方图输出
            parseHistogram(analysis, histogram, topN, packageFilter);
            
        } catch (Exception e) {
            log.warn("无法获取堆直方图: {}", e.getMessage());
            // 回退方案：使用基本内存信息
            analysis.setTopByBytes(new ArrayList<>());
            analysis.setTopByCount(new ArrayList<>());
            analysis.setPackageMemory(new ArrayList<>());
        }
        
        return ReturnResult.ok(analysis);
    }
    
    /**
     * 解析堆直方图输出
     */
    private void parseHistogram(JvmInfo.HeapAnalysis analysis, String histogram, int topN, String packageFilter) {
        List<HeapHistogramEntry> entries = new ArrayList<>();
        long totalBytes = 0;
        long totalInstances = 0;
        
        String[] lines = histogram.split("\n");
        boolean dataStarted = false;
        
        for (String line : lines) {
            line = line.trim();
            
            // 跳过头部
            if (line.startsWith("num") || line.startsWith("---")) {
                dataStarted = true;
                continue;
            }
            
            if (!dataStarted || line.isEmpty()) {
                continue;
            }
            
            // 解析数据行: "num     #instances         #bytes  class name"
            // 例如: "   1:        123456        12345678  [B"
            if (line.startsWith("Total")) {
                // 总计行
                continue;
            }
            
            try {
                String[] parts = line.split("\\s+");
                if (parts.length >= 4) {
                    int idx = 0;
                    // 跳过序号(如 "1:")
                    if (parts[idx].endsWith(":")) {
                        idx++;
                    }
                    
                    long instances = Long.parseLong(parts[idx++]);
                    long bytes = Long.parseLong(parts[idx++]);
                    String className = parts[idx];
                    
                    // 包过滤
                    if (packageFilter != null && !packageFilter.isEmpty()) {
                        String normalizedClass = normalizeClassName(className);
                        if (!normalizedClass.startsWith(packageFilter)) {
                            continue;
                        }
                    }
                    
                    HeapHistogramEntry entry = new HeapHistogramEntry();
                    entry.className = className;
                    entry.instanceCount = instances;
                    entry.bytes = bytes;
                    entries.add(entry);
                    
                    totalBytes += bytes;
                    totalInstances += instances;
                }
            } catch (NumberFormatException e) {
                // 跳过无法解析的行
            }
        }
        
        analysis.setTotalBytes(totalBytes);
        analysis.setTotalObjects(totalInstances);
        analysis.setClassCount(entries.size());
        
        final long finalTotalBytes = totalBytes;
        
        // 按内存排序
        List<JvmInfo.HeapObjectInfo> topByBytes = entries.stream()
                .sorted(Comparator.comparing(e -> -e.bytes))
                .limit(topN)
                .map(e -> {
                    JvmInfo.HeapObjectInfo info = new JvmInfo.HeapObjectInfo();
                    info.setClassName(e.className);
                    info.setSimpleClassName(normalizeClassName(e.className));
                    info.setInstanceCount(e.instanceCount);
                    info.setBytes(e.bytes);
                    info.setPercentage(finalTotalBytes > 0 ? (double) e.bytes / finalTotalBytes * 100 : 0);
                    return info;
                })
                .collect(Collectors.toList());
        analysis.setTopByBytes(topByBytes);
        
        // 按数量排序
        List<JvmInfo.HeapObjectInfo> topByCount = entries.stream()
                .sorted(Comparator.comparing(e -> -e.instanceCount))
                .limit(topN)
                .map(e -> {
                    JvmInfo.HeapObjectInfo info = new JvmInfo.HeapObjectInfo();
                    info.setClassName(e.className);
                    info.setSimpleClassName(normalizeClassName(e.className));
                    info.setInstanceCount(e.instanceCount);
                    info.setBytes(e.bytes);
                    info.setPercentage(finalTotalBytes > 0 ? (double) e.bytes / finalTotalBytes * 100 : 0);
                    return info;
                })
                .collect(Collectors.toList());
        analysis.setTopByCount(topByCount);
        
        // 包级别统计
        Map<String, PackageStats> packageStats = new HashMap<>();
        for (HeapHistogramEntry entry : entries) {
            String normalizedClass = normalizeClassName(entry.className);
            String pkg = getPackageName(normalizedClass);
            PackageStats stats = packageStats.computeIfAbsent(pkg, k -> new PackageStats());
            stats.bytes += entry.bytes;
            stats.instanceCount += entry.instanceCount;
            stats.classCount++;
        }
        
        List<JvmInfo.PackageMemory> packageMemory = packageStats.entrySet().stream()
                .sorted(Comparator.comparing(e -> -e.getValue().bytes))
                .limit(20)
                .map(e -> {
                    JvmInfo.PackageMemory pm = new JvmInfo.PackageMemory();
                    pm.setPackageName(e.getKey());
                    pm.setBytes(e.getValue().bytes);
                    pm.setInstanceCount(e.getValue().instanceCount);
                    pm.setClassCount(e.getValue().classCount);
                    pm.setPercentage(finalTotalBytes > 0 ? (double) e.getValue().bytes / finalTotalBytes * 100 : 0);
                    return pm;
                })
                .collect(Collectors.toList());
        analysis.setPackageMemory(packageMemory);
    }
    
    /**
     * 规范化类名 (处理数组类型等)
     */
    private String normalizeClassName(String className) {
        if (className == null) return "";
        
        // 处理数组类型
        if (className.startsWith("[")) {
            int dims = 0;
            int i = 0;
            while (i < className.length() && className.charAt(i) == '[') {
                dims++;
                i++;
            }
            
            String suffix = "[]".repeat(dims);
            
            if (i < className.length()) {
                char typeChar = className.charAt(i);
                switch (typeChar) {
                    case 'Z': return "boolean" + suffix;
                    case 'B': return "byte" + suffix;
                    case 'C': return "char" + suffix;
                    case 'D': return "double" + suffix;
                    case 'F': return "float" + suffix;
                    case 'I': return "int" + suffix;
                    case 'J': return "long" + suffix;
                    case 'S': return "short" + suffix;
                    case 'L':
                        // 对象数组 [Ljava.lang.String;
                        String objClass = className.substring(i + 1);
                        if (objClass.endsWith(";")) {
                            objClass = objClass.substring(0, objClass.length() - 1);
                        }
                        return objClass + suffix;
                    default:
                        return className;
                }
            }
        }
        
        return className;
    }
    
    /**
     * 堆直方图条目
     */
    private static class HeapHistogramEntry {
        String className;
        long instanceCount;
        long bytes;
    }
    
    /**
     * 包统计信息
     */
    private static class PackageStats {
        long bytes;
        long instanceCount;
        int classCount;
    }
    
    /**
     * 方法命中信息
     */
    private static class MethodHitInfo {
        String className;
        String methodName;
        String fileName;
        int lineNumber;
        int hitCount;
        Map<Integer, Integer> lineNumbers = new HashMap<>();
        Map<String, Integer> callers = new HashMap<>();
    }
    
    /**
     * 判断是否为空闲线程
     */
    private boolean isIdleThread(java.lang.management.ThreadInfo info) {
        Thread.State state = info.getThreadState();
        if (state == Thread.State.WAITING || state == Thread.State.TIMED_WAITING) {
            StackTraceElement[] stack = info.getStackTrace();
            if (stack.length > 0) {
                String topMethod = stack[0].getClassName() + "." + stack[0].getMethodName();
                // 常见的空闲等待方法
                return topMethod.contains("park") || 
                       topMethod.contains("wait") ||
                       topMethod.contains("sleep") ||
                       topMethod.contains("take") ||
                       topMethod.contains("poll") ||
                       topMethod.contains("accept") ||
                       topMethod.contains("read") ||
                       topMethod.contains("select");
            }
        }
        return false;
    }
    
    /**
     * 获取包名
     */
    private String getPackageName(String className) {
        int lastDot = className.lastIndexOf('.');
        return lastDot > 0 ? className.substring(0, lastDot) : "(default)";
    }

    /**
     * 获取 CPU 占用最高的线程列表
     */
    private List<JvmInfo.ThreadDetail> getTopCpuThreads(ThreadMXBean threadMXBean, int topN) {
        List<JvmInfo.ThreadDetail> threadDetails = new ArrayList<>();
        
        if (!threadMXBean.isThreadCpuTimeSupported() || !threadMXBean.isThreadCpuTimeEnabled()) {
            return threadDetails;
        }
        
        long[] threadIds = threadMXBean.getAllThreadIds();
        java.lang.management.ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(threadIds);
        
        // 获取进程总 CPU 时间用于计算百分比
        long totalCpuTime = 0;
        Map<Long, Long> cpuTimeMap = new HashMap<>();
        for (long threadId : threadIds) {
            long cpuTime = threadMXBean.getThreadCpuTime(threadId);
            if (cpuTime > 0) {
                cpuTimeMap.put(threadId, cpuTime);
                totalCpuTime += cpuTime;
            }
        }
        
        final long finalTotalCpuTime = totalCpuTime;
        
        // 构建线程详情列表
        Map<Long, Thread> threadMap = getThreadMap();
        
        for (int i = 0; i < threadInfos.length; i++) {
            java.lang.management.ThreadInfo info = threadInfos[i];
            if (info == null) {
                continue;
            }
            
            JvmInfo.ThreadDetail detail = new JvmInfo.ThreadDetail();
            detail.setThreadId(info.getThreadId());
            detail.setThreadName(info.getThreadName());
            detail.setState(info.getThreadState().name());
            detail.setBlockedCount(info.getBlockedCount());
            detail.setBlockedTime(info.getBlockedTime());
            detail.setWaitedCount(info.getWaitedCount());
            detail.setWaitedTime(info.getWaitedTime());
            detail.setLockName(info.getLockName());
            detail.setLockOwnerId(info.getLockOwnerId());
            detail.setLockOwnerName(info.getLockOwnerName());
            
            // CPU 时间
            Long cpuTime = cpuTimeMap.get(info.getThreadId());
            if (cpuTime != null) {
                detail.setCpuTime(cpuTime);
                detail.setUserTime(threadMXBean.getThreadUserTime(info.getThreadId()));
                // 计算 CPU 使用率
                if (finalTotalCpuTime > 0) {
                    detail.setCpuUsage((double) cpuTime / finalTotalCpuTime * 100);
                }
            } else {
                detail.setCpuTime(0L);
                detail.setUserTime(0L);
                detail.setCpuUsage(0.0);
            }
            
            // 线程属性
            Thread thread = threadMap.get(info.getThreadId());
            if (thread != null) {
                detail.setDaemon(thread.isDaemon());
                detail.setPriority(thread.getPriority());
            }
            
            threadDetails.add(detail);
        }
        
        // 按 CPU 时间降序排序，取前 N 个
        return threadDetails.stream()
                .sorted(Comparator.comparing(JvmInfo.ThreadDetail::getCpuTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(topN)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取所有线程的 Map
     */
    private Map<Long, Thread> getThreadMap() {
        Map<Long, Thread> threadMap = new HashMap<>();
        ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
        while (rootGroup.getParent() != null) {
            rootGroup = rootGroup.getParent();
        }
        Thread[] threads = new Thread[rootGroup.activeCount() * 2];
        int count = rootGroup.enumerate(threads, true);
        for (int i = 0; i < count; i++) {
            threadMap.put(threads[i].getId(), threads[i]);
        }
        return threadMap;
    }
    
    /**
     * 根据 ID 查找线程
     */
    private Thread findThreadById(long threadId) {
        return getThreadMap().get(threadId);
    }

    /**
     * 业务代码归因分析 - 分析哪些业务代码导致了CPU/内存问题
     *
     * @param businessPackage 业务包前缀(如 com.chua)
     * @param sampleCount 采样次数
     * @param interval 采样间隔(毫秒)
     * @param topN 返回前N个结果
     * @return 业务代码归因分析结果
     */
    @GetMapping("/business-analysis")
    @Operation(summary = "业务代码归因分析")
    public ReturnResult<JvmInfo.BusinessCodeAnalysis> analyzeBusinessCode(
            @RequestParam(value = "businessPackage", defaultValue = "com.chua") String businessPackage,
            @RequestParam(value = "sampleCount", defaultValue = "30") Integer sampleCount,
            @RequestParam(value = "interval", defaultValue = "50") Integer interval,
            @RequestParam(value = "topN", defaultValue = "20") Integer topN) {
        
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        long startTime = System.currentTimeMillis();
        
        // 业务方法影响统计
        Map<String, BusinessMethodStats> businessMethodStats = new HashMap<>();
        // 调用链统计: key = "业务入口 -> 热点方法"
        Map<String, CallChainStats> callChainStats = new HashMap<>();
        // 业务入口点统计
        Map<String, EntryPointStats> entryPointStats = new HashMap<>();
        
        int totalSamples = 0;
        
        // 多次采样收集调用栈
        for (int i = 0; i < sampleCount; i++) {
            long[] threadIds = threadMXBean.getAllThreadIds();
            java.lang.management.ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(threadIds, 100);
            
            for (java.lang.management.ThreadInfo info : threadInfos) {
                if (info == null) continue;
                
                // 排除空闲线程
                if (isIdleThread(info)) continue;
                
                StackTraceElement[] stackTrace = info.getStackTrace();
                if (stackTrace.length == 0) continue;
                
                totalSamples++;
                
                // 分析这个调用栈
                analyzeStackTrace(stackTrace, businessPackage, businessMethodStats, callChainStats, entryPointStats);
            }
            
            // 采样间隔
            if (i < sampleCount - 1) {
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        long endTime = System.currentTimeMillis();
        
        // 构建结果
        JvmInfo.BusinessCodeAnalysis analysis = new JvmInfo.BusinessCodeAnalysis();
        analysis.setAnalysisTime(endTime - startTime);
        analysis.setSampleCount(sampleCount);
        analysis.setSampleInterval(interval);
        analysis.setBusinessPackage(businessPackage);
        
        // 计算总影响分数
        int totalImpactScore = businessMethodStats.values().stream()
                .mapToInt(s -> s.cpuScore).sum();
        
        // CPU影响的业务代码
        List<JvmInfo.BusinessCodeImpact> cpuImpacts = businessMethodStats.values().stream()
                .filter(s -> s.cpuScore > 0)
                .sorted(Comparator.comparing(s -> -s.cpuScore))
                .limit(topN)
                .map(s -> {
                    JvmInfo.BusinessCodeImpact impact = new JvmInfo.BusinessCodeImpact();
                    impact.setBusinessMethod(s.className + "." + s.methodName + "()");
                    impact.setClassName(s.className);
                    impact.setMethodName(s.methodName);
                    impact.setFileName(s.fileName);
                    impact.setLineNumber(s.lineNumber);
                    impact.setImpactScore(s.cpuScore);
                    impact.setImpactPercentage(totalImpactScore > 0 ? 
                            (double) s.cpuScore / totalImpactScore * 100 : 0);
                    impact.setCallCount(s.callCount);
                    impact.setImpactType("CPU");
                    impact.setRelatedHotspots(s.relatedHotspots.entrySet().stream()
                            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                            .limit(5)
                            .map(e -> e.getKey() + " (" + e.getValue() + "次)")
                            .collect(Collectors.toList()));
                    impact.setDescription(generateImpactDescription(s, "CPU"));
                    return impact;
                })
                .collect(Collectors.toList());
        analysis.setCpuImpacts(cpuImpacts);
        
        // 内存影响的业务代码(通过检测可能创建大量对象的方法)
        List<JvmInfo.BusinessCodeImpact> memoryImpacts = businessMethodStats.values().stream()
                .filter(s -> s.memoryScore > 0)
                .sorted(Comparator.comparing(s -> -s.memoryScore))
                .limit(topN)
                .map(s -> {
                    JvmInfo.BusinessCodeImpact impact = new JvmInfo.BusinessCodeImpact();
                    impact.setBusinessMethod(s.className + "." + s.methodName + "()");
                    impact.setClassName(s.className);
                    impact.setMethodName(s.methodName);
                    impact.setFileName(s.fileName);
                    impact.setLineNumber(s.lineNumber);
                    impact.setImpactScore(s.memoryScore);
                    int totalMemScore = businessMethodStats.values().stream()
                            .mapToInt(st -> st.memoryScore).sum();
                    impact.setImpactPercentage(totalMemScore > 0 ? 
                            (double) s.memoryScore / totalMemScore * 100 : 0);
                    impact.setCallCount(s.callCount);
                    impact.setImpactType("MEMORY");
                    impact.setRelatedHotspots(s.memoryRelatedMethods.entrySet().stream()
                            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                            .limit(5)
                            .map(e -> e.getKey() + " (" + e.getValue() + "次)")
                            .collect(Collectors.toList()));
                    impact.setDescription(generateImpactDescription(s, "MEMORY"));
                    return impact;
                })
                .collect(Collectors.toList());
        analysis.setMemoryImpacts(memoryImpacts);
        
        // 调用链分析
        int totalChainCount = callChainStats.values().stream()
                .mapToInt(s -> s.count).sum();
        List<JvmInfo.CallChainAnalysis> callChains = callChainStats.values().stream()
                .sorted(Comparator.comparing(s -> -s.count))
                .limit(topN)
                .map(s -> {
                    JvmInfo.CallChainAnalysis chain = new JvmInfo.CallChainAnalysis();
                    chain.setHotspotMethod(s.hotspotMethod);
                    chain.setBusinessEntry(s.businessEntry);
                    chain.setCallChain(s.callChain);
                    chain.setOccurrenceCount(s.count);
                    chain.setPercentage(totalChainCount > 0 ? 
                            (double) s.count / totalChainCount * 100 : 0);
                    return chain;
                })
                .collect(Collectors.toList());
        analysis.setCallChains(callChains);
        
        // 业务入口点
        List<JvmInfo.BusinessEntryPoint> entryPoints = entryPointStats.values().stream()
                .sorted(Comparator.comparing(s -> -s.cpuImpactScore))
                .limit(topN)
                .map(s -> {
                    JvmInfo.BusinessEntryPoint entry = new JvmInfo.BusinessEntryPoint();
                    entry.setEntryMethod(s.className + "." + s.methodName + "()");
                    entry.setClassName(s.className);
                    entry.setMethodName(s.methodName);
                    entry.setEntryType(s.entryType);
                    entry.setTriggerCount(s.triggerCount);
                    entry.setRelatedHotspotCount(s.relatedHotspots.size());
                    entry.setCpuImpactScore(s.cpuImpactScore);
                    entry.setDescription(generateEntryPointDescription(s));
                    return entry;
                })
                .collect(Collectors.toList());
        analysis.setEntryPoints(entryPoints);
        
        return ReturnResult.ok(analysis);
    }
    
    /**
     * 分析单个调用栈
     */
    private void analyzeStackTrace(StackTraceElement[] stackTrace, String businessPackage,
            Map<String, BusinessMethodStats> businessMethodStats,
            Map<String, CallChainStats> callChainStats,
            Map<String, EntryPointStats> entryPointStats) {
        
        // 找出栈中的业务方法和热点方法
        List<StackTraceElement> businessMethods = new ArrayList<>();
        List<StackTraceElement> hotspotMethods = new ArrayList<>();
        StackTraceElement businessEntry = null;
        String businessEntryType = null;
        
        for (int i = 0; i < stackTrace.length; i++) {
            StackTraceElement element = stackTrace[i];
            String className = element.getClassName();
            
            // 判断是否为业务代码
            if (className.startsWith(businessPackage)) {
                businessMethods.add(element);
                // 最后一个业务方法作为入口点
                businessEntry = element;
                businessEntryType = detectEntryType(className, element.getMethodName());
            }
            
            // 栈顶方法是热点方法
            if (i < 5) {
                hotspotMethods.add(element);
            }
        }
        
        // 如果没有业务方法，跳过
        if (businessMethods.isEmpty()) {
            return;
        }
        
        // 更新业务方法统计
        for (int i = 0; i < businessMethods.size(); i++) {
            StackTraceElement bm = businessMethods.get(i);
            String key = bm.getClassName() + "#" + bm.getMethodName();
            
            BusinessMethodStats stats = businessMethodStats.computeIfAbsent(key, k -> {
                BusinessMethodStats s = new BusinessMethodStats();
                s.className = bm.getClassName();
                s.methodName = bm.getMethodName();
                s.fileName = bm.getFileName();
                s.lineNumber = bm.getLineNumber();
                return s;
            });
            
            stats.callCount++;
            
            // CPU 影响分数 - 越靠近栈顶影响越大
            int cpuWeight = (i == 0) ? 5 : (i < 3) ? 3 : 1;
            stats.cpuScore += cpuWeight;
            
            // 记录关联的热点方法
            for (StackTraceElement hp : hotspotMethods) {
                String hpKey = hp.getClassName() + "." + hp.getMethodName() + "()";
                stats.relatedHotspots.merge(hpKey, 1, Integer::sum);
            }
            
            // 检测可能的内存分配
            detectMemoryAllocation(stackTrace, i, stats);
        }
        
        // 更新入口点统计
        if (businessEntry != null) {
            String entryKey = businessEntry.getClassName() + "#" + businessEntry.getMethodName();
            StackTraceElement finalBusinessEntry = businessEntry;
            String finalBusinessEntryType = businessEntryType;
            EntryPointStats entryStats = entryPointStats.computeIfAbsent(entryKey, k -> {
                EntryPointStats s = new EntryPointStats();
                s.className = finalBusinessEntry.getClassName();
                s.methodName = finalBusinessEntry.getMethodName();
                s.entryType = finalBusinessEntryType;
                return s;
            });
            entryStats.triggerCount++;
            
            // 累计CPU影响
            for (StackTraceElement hp : hotspotMethods) {
                entryStats.relatedHotspots.add(hp.getClassName() + "." + hp.getMethodName());
                entryStats.cpuImpactScore++;
            }
            
            // 记录调用链
            if (!hotspotMethods.isEmpty()) {
                StackTraceElement topHotspot = hotspotMethods.get(0);
                String chainKey = businessEntry.getClassName() + "#" + businessEntry.getMethodName() + 
                        " -> " + topHotspot.getClassName() + "#" + topHotspot.getMethodName();
                
                CallChainStats chainStats = callChainStats.computeIfAbsent(chainKey, k -> {
                    CallChainStats s = new CallChainStats();
                    s.businessEntry = businessEntry.getClassName() + "." + businessEntry.getMethodName() + "()";
                    s.hotspotMethod = topHotspot.getClassName() + "." + topHotspot.getMethodName() + "()";
                    // 构建简化的调用链
                    s.callChain = buildSimplifiedCallChain(stackTrace, businessPackage);
                    return s;
                });
                chainStats.count++;
            }
        }
    }
    
    /**
     * 检测入口类型
     */
    private String detectEntryType(String className, String methodName) {
        String lowerClassName = className.toLowerCase();
        String lowerMethodName = methodName.toLowerCase();
        
        if (lowerClassName.contains("controller")) {
            return "Controller";
        } else if (lowerClassName.contains("service")) {
            return "Service";
        } else if (lowerClassName.contains("scheduler") || lowerClassName.contains("job") ||
                   lowerClassName.contains("task") || lowerMethodName.contains("schedule")) {
            return "Scheduler";
        } else if (lowerClassName.contains("listener") || lowerClassName.contains("consumer") ||
                   lowerClassName.contains("handler")) {
            return "MessageListener";
        } else if (lowerClassName.contains("filter")) {
            return "Filter";
        } else if (lowerClassName.contains("interceptor")) {
            return "Interceptor";
        } else if (lowerClassName.contains("aspect")) {
            return "Aspect";
        } else if (lowerClassName.contains("repository") || lowerClassName.contains("dao") ||
                   lowerClassName.contains("mapper")) {
            return "Repository";
        }
        return "Other";
    }
    
    /**
     * 检测可能的内存分配操作
     */
    private void detectMemoryAllocation(StackTraceElement[] stackTrace, int businessMethodIndex,
            BusinessMethodStats stats) {
        // 检查业务方法调用的下游方法是否涉及大量对象创建
        for (int i = 0; i < businessMethodIndex && i < stackTrace.length; i++) {
            StackTraceElement element = stackTrace[i];
            String methodName = element.getMethodName().toLowerCase();
            String className = element.getClassName().toLowerCase();
            
            // 检测可能创建大量对象的方法
            boolean isMemoryIntensive = false;
            String memoryPattern = null;
            
            if (className.contains("arraylist") || className.contains("hashmap") ||
                className.contains("hashset") || className.contains("linkedlist")) {
                if (methodName.equals("add") || methodName.equals("put") || 
                    methodName.equals("addall") || methodName.equals("putall")) {
                    isMemoryIntensive = true;
                    memoryPattern = "集合添加";
                }
            } else if (methodName.equals("new") || methodName.equals("<init>") ||
                       methodName.equals("newinstance") || methodName.equals("create")) {
                isMemoryIntensive = true;
                memoryPattern = "对象创建";
            } else if (className.contains("string") && 
                       (methodName.equals("concat") || methodName.equals("format") ||
                        methodName.equals("+") || methodName.equals("valueof"))) {
                isMemoryIntensive = true;
                memoryPattern = "字符串操作";
            } else if (className.contains("stream") || methodName.equals("collect") ||
                       methodName.equals("tolist") || methodName.equals("toarray")) {
                isMemoryIntensive = true;
                memoryPattern = "Stream收集";
            } else if (className.contains("json") || className.contains("jackson") ||
                       className.contains("gson") || className.contains("fastjson")) {
                isMemoryIntensive = true;
                memoryPattern = "JSON序列化";
            } else if (className.contains("buffer") || className.contains("builder")) {
                isMemoryIntensive = true;
                memoryPattern = "缓冲区操作";
            } else if (methodName.equals("clone") || methodName.equals("copy") ||
                       methodName.equals("deepcopy")) {
                isMemoryIntensive = true;
                memoryPattern = "对象复制";
            }
            
            if (isMemoryIntensive) {
                stats.memoryScore += (i < 3) ? 3 : 1;
                String memKey = element.getClassName() + "." + element.getMethodName() + 
                        "() [" + memoryPattern + "]";
                stats.memoryRelatedMethods.merge(memKey, 1, Integer::sum);
            }
        }
    }
    
    /**
     * 构建简化的调用链
     */
    private List<String> buildSimplifiedCallChain(StackTraceElement[] stackTrace, String businessPackage) {
        List<String> chain = new ArrayList<>();
        boolean inFramework = false;
        int frameworkCount = 0;
        
        for (int i = stackTrace.length - 1; i >= 0; i--) {
            StackTraceElement element = stackTrace[i];
            String className = element.getClassName();
            
            if (className.startsWith(businessPackage)) {
                // 业务代码，添加到链中
                if (inFramework && frameworkCount > 0) {
                    chain.add("... (" + frameworkCount + "个框架调用)");
                    frameworkCount = 0;
                }
                chain.add(getSimpleClassName(className) + "." + element.getMethodName() + "()");
                inFramework = false;
            } else {
                // 框架代码
                if (!inFramework) {
                    inFramework = true;
                }
                frameworkCount++;
            }
        }
        
        // 最多保留10个元素
        if (chain.size() > 10) {
            List<String> simplified = new ArrayList<>();
            simplified.addAll(chain.subList(0, 4));
            simplified.add("... (省略" + (chain.size() - 8) + "个调用)");
            simplified.addAll(chain.subList(chain.size() - 4, chain.size()));
            return simplified;
        }
        
        return chain;
    }
    
    /**
     * 获取简化类名
     */
    private String getSimpleClassName(String fullClassName) {
        int lastDot = fullClassName.lastIndexOf('.');
        return lastDot > 0 ? fullClassName.substring(lastDot + 1) : fullClassName;
    }
    
    /**
     * 生成影响描述
     */
    private String generateImpactDescription(BusinessMethodStats stats, String type) {
        StringBuilder desc = new StringBuilder();
        if ("CPU".equals(type)) {
            desc.append("此方法在采样中出现").append(stats.callCount).append("次");
            if (!stats.relatedHotspots.isEmpty()) {
                String topHotspot = stats.relatedHotspots.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse("");
                if (!topHotspot.isEmpty()) {
                    desc.append("，主要热点: ").append(getSimpleClassName(topHotspot));
                }
            }
        } else {
            desc.append("此方法可能导致内存分配");
            if (!stats.memoryRelatedMethods.isEmpty()) {
                String topMemory = stats.memoryRelatedMethods.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse("");
                if (!topMemory.isEmpty()) {
                    // 提取模式
                    int bracketStart = topMemory.indexOf('[');
                    if (bracketStart > 0) {
                        desc.append("，原因: ").append(topMemory.substring(bracketStart));
                    }
                }
            }
        }
        return desc.toString();
    }
    
    /**
     * 生成入口点描述
     */
    private String generateEntryPointDescription(EntryPointStats stats) {
        StringBuilder desc = new StringBuilder();
        desc.append(stats.entryType).append("入口，触发").append(stats.triggerCount).append("次");
        if (!stats.relatedHotspots.isEmpty()) {
            desc.append("，关联").append(stats.relatedHotspots.size()).append("个热点方法");
        }
        return desc.toString();
    }
    
    /**
     * 业务方法统计
     */
    private static class BusinessMethodStats {
        String className;
        String methodName;
        String fileName;
        int lineNumber;
        int callCount = 0;
        int cpuScore = 0;
        int memoryScore = 0;
        Map<String, Integer> relatedHotspots = new HashMap<>();
        Map<String, Integer> memoryRelatedMethods = new HashMap<>();
    }
    
    /**
     * 调用链统计
     */
    private static class CallChainStats {
        String businessEntry;
        String hotspotMethod;
        List<String> callChain;
        int count = 0;
    }
    
    /**
     * 入口点统计
     */
    private static class EntryPointStats {
        String className;
        String methodName;
        String entryType;
        int triggerCount = 0;
        int cpuImpactScore = 0;
        Set<String> relatedHotspots = new HashSet<>();
    }

    /**
     * JVM 优化建议 - 根据当前状态给出最优JVM参数建议
     *
     * @param targetLatency 目标响应时间(ms)，用于选择GC策略
     * @param targetThroughput 目标吞吐量(%)
     * @return JVM 优化建议
     */
    @GetMapping("/optimization")
    @Operation(summary = "JVM 优化建议")
    public ReturnResult<JvmInfo.JvmOptimizationAdvice> getOptimizationAdvice(
            @RequestParam(value = "targetLatency", defaultValue = "200") Integer targetLatency,
            @RequestParam(value = "targetThroughput", defaultValue = "95") Integer targetThroughput) {
        
        JvmInfo.JvmOptimizationAdvice advice = new JvmInfo.JvmOptimizationAdvice();
        advice.setAnalysisTime(System.currentTimeMillis());
        
        // 获取当前JVM信息
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        java.lang.management.OperatingSystemMXBean osMXBean = ManagementFactory.getOperatingSystemMXBean();
        
        // 当前参数
        List<String> currentArgs = runtimeMXBean.getInputArguments();
        advice.setCurrentArgs(currentArgs);
        
        // 收集当前状态
        MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryMXBean.getNonHeapMemoryUsage();
        int threadCount = threadMXBean.getThreadCount();
        int processors = osMXBean.getAvailableProcessors();
        
        long maxMemory = heapUsage.getMax();
        long usedMemory = heapUsage.getUsed();
        double heapUsagePercent = maxMemory > 0 ? (double) usedMemory / maxMemory * 100 : 0;
        
        // 获取系统内存
        long totalPhysicalMemory = 0;
        if (osMXBean instanceof com.sun.management.OperatingSystemMXBean) {
            totalPhysicalMemory = ((com.sun.management.OperatingSystemMXBean) osMXBean).getTotalMemorySize();
        }
        
        // 获取GC信息
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        long totalGcTime = 0;
        long totalGcCount = 0;
        String currentGcName = "";
        for (GarbageCollectorMXBean gc : gcBeans) {
            totalGcTime += gc.getCollectionTime();
            totalGcCount += gc.getCollectionCount();
            if (gc.getName().contains("Old") || gc.getName().contains("Major") || 
                gc.getName().contains("MarkSweep") || gc.getName().contains("G1")) {
                currentGcName = gc.getName();
            }
        }
        
        // 诊断信息列表
        List<String> diagnostics = new ArrayList<>();
        List<JvmInfo.PerformanceAdvice> performanceAdvices = new ArrayList<>();
        List<JvmInfo.JvmParameter> recommendedParams = new ArrayList<>();
        int healthScore = 100;
        
        // ============ 内存分析和建议 ============
        JvmInfo.MemoryAdvice memoryAdvice = new JvmInfo.MemoryAdvice();
        memoryAdvice.setCurrentHeapUsage(heapUsagePercent);
        List<JvmInfo.JvmParameter> memoryParams = new ArrayList<>();
        
        // 计算推荐堆大小
        long recommendedMaxHeap;
        long recommendedInitHeap;
        
        if (totalPhysicalMemory > 0) {
            // 基于物理内存计算
            if (totalPhysicalMemory > 32L * 1024 * 1024 * 1024) {
                // 大于32GB，建议使用物理内存的50%
                recommendedMaxHeap = (long) (totalPhysicalMemory * 0.5);
            } else if (totalPhysicalMemory > 8L * 1024 * 1024 * 1024) {
                // 8-32GB，建议使用物理内存的60%
                recommendedMaxHeap = (long) (totalPhysicalMemory * 0.6);
            } else {
                // 小于8GB，建议使用物理内存的70%
                recommendedMaxHeap = (long) (totalPhysicalMemory * 0.7);
            }
            // 上限为31GB(压缩指针边界)
            recommendedMaxHeap = Math.min(recommendedMaxHeap, 31L * 1024 * 1024 * 1024);
        } else {
            // 无法获取物理内存，基于当前使用情况估算
            recommendedMaxHeap = (long) (usedMemory * 2.5);
        }
        
        // 初始堆建议与最大堆相同，避免扩容开销
        recommendedInitHeap = recommendedMaxHeap;
        
        memoryAdvice.setRecommendedHeapSize(formatBytesCompact(recommendedMaxHeap));
        memoryAdvice.setRecommendedInitialHeap(formatBytesCompact(recommendedInitHeap));
        
        // -Xmx
        JvmInfo.JvmParameter xmxParam = new JvmInfo.JvmParameter();
        xmxParam.setName("-Xmx");
        xmxParam.setCurrentValue(formatBytesCompact(maxMemory));
        xmxParam.setRecommendedValue(formatBytesCompact(recommendedMaxHeap));
        xmxParam.setType("MEMORY");
        xmxParam.setPriority("HIGH");
        xmxParam.setDescription("最大堆内存");
        xmxParam.setReason("基于系统可用内存和当前使用情况计算");
        memoryParams.add(xmxParam);
        recommendedParams.add(xmxParam);
        
        // -Xms
        JvmInfo.JvmParameter xmsParam = new JvmInfo.JvmParameter();
        xmsParam.setName("-Xms");
        xmsParam.setCurrentValue(formatBytesCompact(heapUsage.getInit()));
        xmsParam.setRecommendedValue(formatBytesCompact(recommendedInitHeap));
        xmsParam.setType("MEMORY");
        xmsParam.setPriority("HIGH");
        xmsParam.setDescription("初始堆内存");
        xmsParam.setReason("设置与-Xmx相同，避免堆扩容开销");
        memoryParams.add(xmsParam);
        recommendedParams.add(xmsParam);
        
        // 新生代大小
        long recommendedNewGenSize = recommendedMaxHeap / 3;
        memoryAdvice.setRecommendedNewGenSize(formatBytesCompact(recommendedNewGenSize));
        
        // 元空间
        long metaspaceUsed = nonHeapUsage.getUsed();
        long recommendedMetaspace = Math.max(256 * 1024 * 1024, (long) (metaspaceUsed * 1.5));
        memoryAdvice.setRecommendedMetaspaceSize(formatBytesCompact(recommendedMetaspace));
        
        JvmInfo.JvmParameter metaspaceParam = new JvmInfo.JvmParameter();
        metaspaceParam.setName("-XX:MetaspaceSize");
        metaspaceParam.setCurrentValue("default");
        metaspaceParam.setRecommendedValue(formatBytesCompact(recommendedMetaspace));
        metaspaceParam.setType("MEMORY");
        metaspaceParam.setPriority("MEDIUM");
        metaspaceParam.setDescription("元空间初始大小");
        metaspaceParam.setReason("避免元空间扩容引起的Full GC");
        memoryParams.add(metaspaceParam);
        recommendedParams.add(metaspaceParam);
        
        JvmInfo.JvmParameter maxMetaspaceParam = new JvmInfo.JvmParameter();
        maxMetaspaceParam.setName("-XX:MaxMetaspaceSize");
        maxMetaspaceParam.setCurrentValue("unlimited");
        maxMetaspaceParam.setRecommendedValue(formatBytesCompact(recommendedMetaspace * 2));
        maxMetaspaceParam.setType("MEMORY");
        maxMetaspaceParam.setPriority("MEDIUM");
        maxMetaspaceParam.setDescription("元空间最大大小");
        maxMetaspaceParam.setReason("限制元空间增长，防止内存泄漏");
        memoryParams.add(maxMetaspaceParam);
        recommendedParams.add(maxMetaspaceParam);
        
        // 内存压力评估
        if (heapUsagePercent > 85) {
            memoryAdvice.setMemoryPressureAssessment("严重 - 堆内存使用率超过85%");
            healthScore -= 20;
            diagnostics.add("⚠️ 内存压力过大，堆使用率达" + String.format("%.1f", heapUsagePercent) + "%");
            
            JvmInfo.PerformanceAdvice memPerfAdvice = new JvmInfo.PerformanceAdvice();
            memPerfAdvice.setCategory("MEMORY");
            memPerfAdvice.setLevel("CRITICAL");
            memPerfAdvice.setTitle("内存压力过大");
            memPerfAdvice.setDescription("堆内存使用率超过85%，可能导致频繁GC或OOM");
            memPerfAdvice.setAction("增加堆内存或优化内存使用");
            memPerfAdvice.setImpact("可能导致应用响应变慢或崩溃");
            performanceAdvices.add(memPerfAdvice);
        } else if (heapUsagePercent > 70) {
            memoryAdvice.setMemoryPressureAssessment("较高 - 堆内存使用率超过70%");
            healthScore -= 10;
            diagnostics.add("ℹ️ 内存使用较高，堆使用率达" + String.format("%.1f", heapUsagePercent) + "%");
        } else {
            memoryAdvice.setMemoryPressureAssessment("正常 - 堆内存使用率在合理范围");
        }
        
        memoryAdvice.setMemoryParams(memoryParams);
        memoryAdvice.setAdvice(generateMemoryAdviceText(heapUsagePercent, maxMemory, recommendedMaxHeap));
        advice.setMemoryAdvice(memoryAdvice);
        
        // ============ GC 分析和建议 ============
        JvmInfo.GcAdvice gcAdvice = new JvmInfo.GcAdvice();
        gcAdvice.setCurrentGc(currentGcName);
        List<JvmInfo.JvmParameter> gcParams = new ArrayList<>();
        
        // 确定推荐的GC收集器
        String recommendedGc;
        String gcReason;
        
        if (recommendedMaxHeap > 8L * 1024 * 1024 * 1024) {
            // 大堆 (>8GB) - 推荐 G1 或 ZGC
            if (targetLatency < 100) {
                recommendedGc = "ZGC";
                gcReason = "大堆内存且低延迟要求，ZGC可保证停顿时间在毫秒级";
                
                JvmInfo.JvmParameter zgcParam = new JvmInfo.JvmParameter();
                zgcParam.setName("-XX:+UseZGC");
                zgcParam.setCurrentValue("未启用");
                zgcParam.setRecommendedValue("启用");
                zgcParam.setType("GC");
                zgcParam.setPriority("HIGH");
                zgcParam.setDescription("启用ZGC收集器");
                zgcParam.setReason("低延迟大堆收集器，停顿时间<10ms");
                gcParams.add(zgcParam);
                recommendedParams.add(zgcParam);
            } else {
                recommendedGc = "G1";
                gcReason = "大堆内存，G1可提供较好的吞吐量和可预测的停顿时间";
                
                JvmInfo.JvmParameter g1Param = new JvmInfo.JvmParameter();
                g1Param.setName("-XX:+UseG1GC");
                g1Param.setCurrentValue(currentGcName.contains("G1") ? "已启用" : "未启用");
                g1Param.setRecommendedValue("启用");
                g1Param.setType("GC");
                g1Param.setPriority("HIGH");
                g1Param.setDescription("启用G1收集器");
                g1Param.setReason("大堆内存首选，平衡吞吐量与停顿时间");
                gcParams.add(g1Param);
                recommendedParams.add(g1Param);
                
                // G1 相关参数
                JvmInfo.JvmParameter pauseTarget = new JvmInfo.JvmParameter();
                pauseTarget.setName("-XX:MaxGCPauseMillis");
                pauseTarget.setCurrentValue("200");
                pauseTarget.setRecommendedValue(String.valueOf(targetLatency));
                pauseTarget.setType("GC");
                pauseTarget.setPriority("MEDIUM");
                pauseTarget.setDescription("GC最大停顿时间目标");
                pauseTarget.setReason("根据目标响应时间设置");
                gcParams.add(pauseTarget);
                recommendedParams.add(pauseTarget);
            }
        } else if (recommendedMaxHeap > 4L * 1024 * 1024 * 1024) {
            // 中等堆 (4-8GB) - 推荐 G1
            recommendedGc = "G1";
            gcReason = "中等堆内存，G1提供最佳性价比";
            
            JvmInfo.JvmParameter g1Param = new JvmInfo.JvmParameter();
            g1Param.setName("-XX:+UseG1GC");
            g1Param.setCurrentValue(currentGcName.contains("G1") ? "已启用" : "未启用");
            g1Param.setRecommendedValue("启用");
            g1Param.setType("GC");
            g1Param.setPriority("HIGH");
            g1Param.setDescription("启用G1收集器");
            g1Param.setReason("中等堆内存最佳选择");
            gcParams.add(g1Param);
            recommendedParams.add(g1Param);
        } else {
            // 小堆 (<4GB)
            if (targetThroughput > 95) {
                recommendedGc = "Parallel";
                gcReason = "小堆内存且高吞吐量要求，并行GC最适合";
                
                JvmInfo.JvmParameter parallelParam = new JvmInfo.JvmParameter();
                parallelParam.setName("-XX:+UseParallelGC");
                parallelParam.setCurrentValue("检测中");
                parallelParam.setRecommendedValue("启用");
                parallelParam.setType("GC");
                parallelParam.setPriority("HIGH");
                parallelParam.setDescription("启用并行GC");
                parallelParam.setReason("高吞吐量场景的最佳选择");
                gcParams.add(parallelParam);
                recommendedParams.add(parallelParam);
            } else {
                recommendedGc = "G1";
                gcReason = "小堆内存，G1提供较好的通用性能";
                
                JvmInfo.JvmParameter g1Param = new JvmInfo.JvmParameter();
                g1Param.setName("-XX:+UseG1GC");
                g1Param.setCurrentValue(currentGcName.contains("G1") ? "已启用" : "未启用");
                g1Param.setRecommendedValue("启用");
                g1Param.setType("GC");
                g1Param.setPriority("HIGH");
                g1Param.setDescription("启用G1收集器");
                g1Param.setReason("JDK9+默认收集器，通用性好");
                gcParams.add(g1Param);
                recommendedParams.add(g1Param);
            }
        }
        
        gcAdvice.setRecommendedGc(recommendedGc);
        gcAdvice.setReason(gcReason);
        
        // GC 频率评估
        long uptime = runtimeMXBean.getUptime();
        double gcFrequency = uptime > 0 ? (double) totalGcCount / (uptime / 1000.0 / 60.0) : 0; // 每分钟GC次数
        if (gcFrequency > 10) {
            gcAdvice.setGcFrequencyAssessment("过高 - 每分钟GC " + String.format("%.1f", gcFrequency) + " 次");
            healthScore -= 15;
            diagnostics.add("⚠️ GC频率过高: " + String.format("%.1f", gcFrequency) + " 次/分钟");
            
            JvmInfo.PerformanceAdvice gcPerfAdvice = new JvmInfo.PerformanceAdvice();
            gcPerfAdvice.setCategory("GC");
            gcPerfAdvice.setLevel("WARNING");
            gcPerfAdvice.setTitle("GC频率过高");
            gcPerfAdvice.setDescription("当前GC频率为" + String.format("%.1f", gcFrequency) + "次/分钟，可能影响应用性能");
            gcPerfAdvice.setAction("检查是否存在内存泄漏，考虑增加堆内存");
            gcPerfAdvice.setImpact("导致应用响应时间增加");
            performanceAdvices.add(gcPerfAdvice);
        } else if (gcFrequency > 5) {
            gcAdvice.setGcFrequencyAssessment("较高 - 每分钟GC " + String.format("%.1f", gcFrequency) + " 次");
            diagnostics.add("ℹ️ GC频率较高: " + String.format("%.1f", gcFrequency) + " 次/分钟");
        } else {
            gcAdvice.setGcFrequencyAssessment("正常 - 每分钟GC " + String.format("%.1f", gcFrequency) + " 次");
        }
        
        // GC 停顿时间评估
        double avgGcPause = totalGcCount > 0 ? (double) totalGcTime / totalGcCount : 0;
        if (avgGcPause > 500) {
            gcAdvice.setGcPauseAssessment("过长 - 平均停顿 " + String.format("%.0f", avgGcPause) + "ms");
            healthScore -= 15;
            diagnostics.add("⚠️ GC停顿时间过长: 平均" + String.format("%.0f", avgGcPause) + "ms");
        } else if (avgGcPause > 200) {
            gcAdvice.setGcPauseAssessment("较长 - 平均停顿 " + String.format("%.0f", avgGcPause) + "ms");
            diagnostics.add("ℹ️ GC平均停顿时间: " + String.format("%.0f", avgGcPause) + "ms");
        } else {
            gcAdvice.setGcPauseAssessment("正常 - 平均停顿 " + String.format("%.0f", avgGcPause) + "ms");
        }
        
        gcAdvice.setGcParams(gcParams);
        advice.setGcAdvice(gcAdvice);
        
        // ============ 线程分析和建议 ============
        JvmInfo.ThreadAdvice threadAdvice = new JvmInfo.ThreadAdvice();
        threadAdvice.setCurrentThreadCount(threadCount);
        List<JvmInfo.JvmParameter> threadParams = new ArrayList<>();
        
        // 线程栈大小建议
        String recommendedStackSize;
        if (threadCount > 500) {
            recommendedStackSize = "512k";
            threadAdvice.setThreadCountAssessment("较高 - 建议减小线程栈");
        } else if (threadCount > 200) {
            recommendedStackSize = "768k";
            threadAdvice.setThreadCountAssessment("中等 - 线程数量在合理范围");
        } else {
            recommendedStackSize = "1m";
            threadAdvice.setThreadCountAssessment("正常 - 线程数量健康");
        }
        threadAdvice.setRecommendedStackSize(recommendedStackSize);
        
        JvmInfo.JvmParameter xssParam = new JvmInfo.JvmParameter();
        xssParam.setName("-Xss");
        xssParam.setCurrentValue("default (1m)");
        xssParam.setRecommendedValue(recommendedStackSize);
        xssParam.setType("MEMORY");
        xssParam.setPriority(threadCount > 200 ? "MEDIUM" : "LOW");
        xssParam.setDescription("线程栈大小");
        xssParam.setReason("根据线程数量(" + threadCount + ")调整");
        threadParams.add(xssParam);
        recommendedParams.add(xssParam);
        
        // 检查死锁
        long[] deadlockedThreads = threadMXBean.findDeadlockedThreads();
        if (deadlockedThreads != null && deadlockedThreads.length > 0) {
            healthScore -= 25;
            diagnostics.add("❌ 检测到死锁线程: " + deadlockedThreads.length + " 个");
            
            JvmInfo.PerformanceAdvice deadlockAdvice = new JvmInfo.PerformanceAdvice();
            deadlockAdvice.setCategory("THREAD");
            deadlockAdvice.setLevel("CRITICAL");
            deadlockAdvice.setTitle("检测到死锁");
            deadlockAdvice.setDescription("发现" + deadlockedThreads.length + "个死锁线程，需要立即处理");
            deadlockAdvice.setAction("检查代码中的锁顺序，避免循环等待");
            deadlockAdvice.setImpact("导致线程永久阻塞，应用功能异常");
            performanceAdvices.add(deadlockAdvice);
        }
        
        threadAdvice.setThreadParams(threadParams);
        threadAdvice.setAdvice(generateThreadAdviceText(threadCount, processors));
        advice.setThreadAdvice(threadAdvice);
        
        // ============ 其他性能参数 ============
        
        // 编译器参数
        JvmInfo.JvmParameter tieredCompilation = new JvmInfo.JvmParameter();
        tieredCompilation.setName("-XX:+TieredCompilation");
        tieredCompilation.setCurrentValue("默认启用");
        tieredCompilation.setRecommendedValue("启用");
        tieredCompilation.setType("PERFORMANCE");
        tieredCompilation.setPriority("LOW");
        tieredCompilation.setDescription("分层编译");
        tieredCompilation.setReason("提高启动速度和峰值性能");
        recommendedParams.add(tieredCompilation);
        
        // 字符串去重
        JvmInfo.JvmParameter stringDedup = new JvmInfo.JvmParameter();
        stringDedup.setName("-XX:+UseStringDeduplication");
        stringDedup.setCurrentValue("未启用");
        stringDedup.setRecommendedValue("启用(G1)");
        stringDedup.setType("MEMORY");
        stringDedup.setPriority("LOW");
        stringDedup.setDescription("字符串去重");
        stringDedup.setReason("减少重复字符串的内存占用");
        recommendedParams.add(stringDedup);
        
        // 设置结果
        advice.setRecommendedParams(recommendedParams);
        advice.setPerformanceAdvices(performanceAdvices);
        advice.setDiagnostics(diagnostics);
        advice.setHealthScore(Math.max(0, healthScore));
        
        // 生成完整启动命令
        advice.setFullCommand(generateFullCommand(recommendedParams, recommendedGc));
        
        return ReturnResult.ok(advice);
    }
    
    /**
     * 格式化字节数为紧凑格式
     */
    private String formatBytesCompact(long bytes) {
        if (bytes < 1024) return bytes + "";
        if (bytes < 1024 * 1024) return (bytes / 1024) + "k";
        if (bytes < 1024L * 1024 * 1024) return (bytes / (1024 * 1024)) + "m";
        return (bytes / (1024L * 1024 * 1024)) + "g";
    }
    
    /**
     * 生成内存建议文本
     */
    private String generateMemoryAdviceText(double heapUsagePercent, long currentMax, long recommendedMax) {
        StringBuilder sb = new StringBuilder();
        if (heapUsagePercent > 85) {
            sb.append("内存压力较大，建议将堆内存从 ").append(formatBytesCompact(currentMax))
              .append(" 增加到 ").append(formatBytesCompact(recommendedMax)).append("。");
        } else if (currentMax < recommendedMax * 0.8) {
            sb.append("当前堆内存配置偏小，建议设置为 ").append(formatBytesCompact(recommendedMax)).append("。");
        } else {
            sb.append("内存配置合理，当前使用率 ").append(String.format("%.1f", heapUsagePercent)).append("%。");
        }
        sb.append("建议将-Xms和-Xmx设置为相同值，避免运行时堆扩容。");
        return sb.toString();
    }
    
    /**
     * 生成线程建议文本
     */
    private String generateThreadAdviceText(int threadCount, int processors) {
        StringBuilder sb = new StringBuilder();
        sb.append("当前线程数: ").append(threadCount).append("，CPU核心数: ").append(processors).append("。");
        if (threadCount > processors * 100) {
            sb.append("线程数偏高，建议检查是否存在线程泄漏或使用线程池。");
        } else if (threadCount > processors * 50) {
            sb.append("线程数较高，确保使用了线程池管理。");
        } else {
            sb.append("线程数在合理范围内。");
        }
        return sb.toString();
    }
    
    /**
     * 生成完整启动命令
     */
    private String generateFullCommand(List<JvmInfo.JvmParameter> params, String gc) {
        StringBuilder cmd = new StringBuilder("java");
        
        // 内存参数
        for (JvmInfo.JvmParameter param : params) {
            if ("MEMORY".equals(param.getType()) && "HIGH".equals(param.getPriority())) {
                if (param.getName().startsWith("-X")) {
                    cmd.append(" ").append(param.getName()).append(param.getRecommendedValue());
                } else {
                    cmd.append(" ").append(param.getName()).append("=").append(param.getRecommendedValue());
                }
            }
        }
        
        // GC参数
        switch (gc) {
            case "G1":
                cmd.append(" -XX:+UseG1GC");
                break;
            case "ZGC":
                cmd.append(" -XX:+UseZGC");
                break;
            case "Parallel":
                cmd.append(" -XX:+UseParallelGC");
                break;
        }
        
        // 元空间
        for (JvmInfo.JvmParameter param : params) {
            if (param.getName().contains("Metaspace") && "MEDIUM".equals(param.getPriority())) {
                cmd.append(" ").append(param.getName()).append("=").append(param.getRecommendedValue());
            }
        }
        
        // 调试参数
        cmd.append(" -XX:+HeapDumpOnOutOfMemoryError");
        cmd.append(" -XX:HeapDumpPath=./heapdump.hprof");
        
        cmd.append(" -jar your-app.jar");
        return cmd.toString();
    }

    /**
     * 获取线程Dump
     *
     * @param format 输出格式: json/text
     * @return 线程Dump信息
     */
    @GetMapping("/thread-dump")
    @Operation(summary = "获取线程Dump")
    public ReturnResult<JvmInfo.ThreadDump> getThreadDump(
            @RequestParam(value = "format", defaultValue = "json") String format) {
        
        JvmInfo.ThreadDump dump = new JvmInfo.ThreadDump();
        dump.setTimestamp(System.currentTimeMillis());
        
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        dump.setJvmName(runtimeMXBean.getVmName());
        dump.setJvmVersion(runtimeMXBean.getVmVersion());
        
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        long[] threadIds = threadMXBean.getAllThreadIds();
        dump.setThreadCount(threadIds.length);
        
        // 检测死锁
        long[] deadlockedThreads = threadMXBean.findDeadlockedThreads();
        if (deadlockedThreads != null) {
            dump.setDeadlockedCount(deadlockedThreads.length);
            List<Long> deadlockedList = new ArrayList<>();
            for (long id : deadlockedThreads) {
                deadlockedList.add(id);
            }
            dump.setDeadlockedThreadIds(deadlockedList);
        } else {
            dump.setDeadlockedCount(0);
            dump.setDeadlockedThreadIds(new ArrayList<>());
        }
        
        // 获取所有线程信息
        java.lang.management.ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(true, true);
        List<JvmInfo.ThreadDumpDetail> threads = new ArrayList<>();
        StringBuilder dumpContent = new StringBuilder();
        
        // 生成文本格式的Dump
        dumpContent.append("Full thread dump ").append(dump.getJvmName())
                   .append(" (").append(dump.getJvmVersion()).append("):\n\n");
        
        Map<Long, Thread> threadMap = getThreadMap();
        
        for (java.lang.management.ThreadInfo info : threadInfos) {
            if (info == null) continue;
            
            JvmInfo.ThreadDumpDetail detail = new JvmInfo.ThreadDumpDetail();
            detail.setThreadId(info.getThreadId());
            detail.setThreadName(info.getThreadName());
            detail.setState(info.getThreadState().name());
            
            Thread thread = threadMap.get(info.getThreadId());
            if (thread != null) {
                detail.setDaemon(thread.isDaemon());
                detail.setPriority(thread.getPriority());
            }
            
            detail.setLockInfo(info.getLockName());
            if (info.getLockOwnerName() != null) {
                detail.setLockOwner(info.getLockOwnerName() + " (" + info.getLockOwnerId() + ")");
            }
            
            // 堆栈跟踪
            StackTraceElement[] stackTrace = info.getStackTrace();
            List<String> stackList = new ArrayList<>();
            for (StackTraceElement element : stackTrace) {
                stackList.add(element.toString());
            }
            detail.setStackTrace(stackList);
            
            // 持有的锁
            java.lang.management.MonitorInfo[] monitors = info.getLockedMonitors();
            List<String> monitorList = new ArrayList<>();
            for (java.lang.management.MonitorInfo monitor : monitors) {
                monitorList.add(monitor.toString());
            }
            detail.setLockedMonitors(monitorList);
            
            // 持有的同步器
            java.lang.management.LockInfo[] synchronizers = info.getLockedSynchronizers();
            List<String> syncList = new ArrayList<>();
            for (java.lang.management.LockInfo sync : synchronizers) {
                syncList.add(sync.toString());
            }
            detail.setLockedSynchronizers(syncList);
            
            threads.add(detail);
            
            // 生成文本格式
            dumpContent.append("\"").append(info.getThreadName()).append("\"");
            if (detail.getDaemon() != null && detail.getDaemon()) {
                dumpContent.append(" daemon");
            }
            dumpContent.append(" prio=").append(detail.getPriority() != null ? detail.getPriority() : 5);
            dumpContent.append(" tid=").append(info.getThreadId());
            dumpContent.append(" ").append(info.getThreadState());
            if (info.getLockName() != null) {
                dumpContent.append(" on ").append(info.getLockName());
            }
            dumpContent.append("\n");
            
            for (int i = 0; i < stackTrace.length; i++) {
                dumpContent.append("\tat ").append(stackTrace[i]).append("\n");
                // 显示锁信息
                for (java.lang.management.MonitorInfo monitor : monitors) {
                    if (monitor.getLockedStackDepth() == i) {
                        dumpContent.append("\t- locked ").append(monitor).append("\n");
                    }
                }
            }
            
            if (synchronizers.length > 0) {
                dumpContent.append("\n\tNumber of locked synchronizers = ").append(synchronizers.length).append("\n");
                for (java.lang.management.LockInfo sync : synchronizers) {
                    dumpContent.append("\t- ").append(sync).append("\n");
                }
            }
            
            dumpContent.append("\n");
        }
        
        dump.setThreads(threads);
        dump.setDumpContent(dumpContent.toString());
        
        return ReturnResult.ok(dump);
    }
    
    // 用于存储上一次堆快照的缓存
    private static volatile Map<String, Long[]> lastHeapSnapshot = null;
    private static volatile long lastSnapshotTime = 0;
    
    /**
     * 内存泄漏检测 - 对比两次堆快照
     *
     * @param intervalSeconds 两次快照间隔(秒)
     * @param topN 返回前N个增长的对象
     * @return 内存泄漏分析结果
     */
    @GetMapping("/memory-leak")
    @Operation(summary = "内存泄漏检测")
    public ReturnResult<JvmInfo.MemoryLeakAnalysis> analyzeMemoryLeak(
            @RequestParam(value = "intervalSeconds", defaultValue = "5") Integer intervalSeconds,
            @RequestParam(value = "topN", defaultValue = "20") Integer topN) {
        
        JvmInfo.MemoryLeakAnalysis analysis = new JvmInfo.MemoryLeakAnalysis();
        
        try {
            // 第一次快照
            MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
            long firstHeapUsed = memoryMXBean.getHeapMemoryUsage().getUsed();
            Map<String, Long[]> firstSnapshot = getHeapHistogramSnapshot();
            long firstTime = System.currentTimeMillis();
            
            analysis.setFirstSnapshotTime(firstTime);
            analysis.setFirstHeapUsed(firstHeapUsed);
            
            // 等待指定时间
            Thread.sleep(intervalSeconds * 1000L);
            
            // 第二次快照
            long secondHeapUsed = memoryMXBean.getHeapMemoryUsage().getUsed();
            Map<String, Long[]> secondSnapshot = getHeapHistogramSnapshot();
            long secondTime = System.currentTimeMillis();
            
            analysis.setSecondSnapshotTime(secondTime);
            analysis.setSecondHeapUsed(secondHeapUsed);
            analysis.setIntervalMs(secondTime - firstTime);
            
            // 计算堆内存增长
            long heapGrowth = secondHeapUsed - firstHeapUsed;
            analysis.setHeapGrowth(heapGrowth);
            analysis.setHeapGrowthPercent(firstHeapUsed > 0 ? (double) heapGrowth / firstHeapUsed * 100 : 0);
            
            // 比较两次快照，找出增长的对象
            List<JvmInfo.ObjectGrowth> growingObjects = new ArrayList<>();
            
            for (Map.Entry<String, Long[]> entry : secondSnapshot.entrySet()) {
                String className = entry.getKey();
                Long[] secondData = entry.getValue(); // [count, bytes]
                Long[] firstData = firstSnapshot.get(className);
                
                long firstCount = firstData != null ? firstData[0] : 0;
                long firstBytes = firstData != null ? firstData[1] : 0;
                long secondCount = secondData[0];
                long secondBytes = secondData[1];
                
                long countGrowth = secondCount - firstCount;
                long bytesGrowth = secondBytes - firstBytes;
                
                // 只关注有增长的对象
                if (bytesGrowth > 0 && countGrowth > 0) {
                    JvmInfo.ObjectGrowth growth = new JvmInfo.ObjectGrowth();
                    growth.setClassName(className);
                    growth.setSimpleClassName(normalizeClassName(className));
                    growth.setFirstCount(firstCount);
                    growth.setSecondCount(secondCount);
                    growth.setCountGrowth(countGrowth);
                    growth.setGrowthPercent(firstCount > 0 ? (double) countGrowth / firstCount * 100 : 100);
                    growth.setFirstBytes(firstBytes);
                    growth.setSecondBytes(secondBytes);
                    growth.setBytesGrowth(bytesGrowth);
                    growth.setBytesGrowthPercent(firstBytes > 0 ? (double) bytesGrowth / firstBytes * 100 : 100);
                    growingObjects.add(growth);
                }
            }
            
            // 按内存增长排序
            growingObjects.sort(Comparator.comparing(JvmInfo.ObjectGrowth::getBytesGrowth).reversed());
            analysis.setGrowingObjects(growingObjects.stream().limit(topN).collect(Collectors.toList()));
            
            // 分析结论
            List<String> suggestions = new ArrayList<>();
            String leakRiskLevel;
            boolean possibleLeak;
            String conclusion;
            
            double growthRate = analysis.getHeapGrowthPercent();
            if (growthRate > 20) {
                leakRiskLevel = "HIGH";
                possibleLeak = true;
                conclusion = "检测到显著的内存增长(增长率" + String.format("%.1f", growthRate) + 
                            "%)，可能存在内存泄漏";
                suggestions.add("检查增长最快的对象类型，分析其创建来源");
                suggestions.add("使用MAT或VisualVM进行深入分析");
                suggestions.add("检查是否有未关闭的资源或缓存无限增长");
            } else if (growthRate > 10) {
                leakRiskLevel = "MEDIUM";
                possibleLeak = true;
                conclusion = "检测到较明显的内存增长(增长率" + String.format("%.1f", growthRate) + 
                            "%)，建议持续观察";
                suggestions.add("延长检测间隔再次分析");
                suggestions.add("检查增长的对象是否为正常业务行为");
            } else if (growthRate > 5) {
                leakRiskLevel = "LOW";
                possibleLeak = false;
                conclusion = "内存有少量增长(增长率" + String.format("%.1f", growthRate) + 
                            "%)，可能是正常业务波动";
                suggestions.add("可能是正常的内存波动，继续监控");
            } else {
                leakRiskLevel = "LOW";
                possibleLeak = false;
                conclusion = "内存状态稳定，未发现明显泄漏迹象";
                suggestions.add("内存状态良好");
            }
            
            analysis.setLeakRiskLevel(leakRiskLevel);
            analysis.setPossibleLeak(possibleLeak);
            analysis.setConclusion(conclusion);
            analysis.setSuggestions(suggestions);
            
        } catch (Exception e) {
            log.error("内存泄漏检测失败", e);
            return ReturnResult.error("内存泄漏检测失败: " + e.getMessage());
        }
        
        return ReturnResult.ok(analysis);
    }
    
    /**
     * 获取堆直方图快照
     */
    private Map<String, Long[]> getHeapHistogramSnapshot() {
        Map<String, Long[]> snapshot = new HashMap<>();
        
        try {
            javax.management.MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            javax.management.ObjectName diagCmdName = new javax.management.ObjectName("com.sun.management:type=DiagnosticCommand");
            
            String[] emptyArgs = new String[0];
            String histogram = (String) server.invoke(diagCmdName, "gcClassHistogram", 
                    new Object[]{emptyArgs}, new String[]{String[].class.getName()});
            
            String[] lines = histogram.split("\n");
            boolean dataStarted = false;
            
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("num") || line.startsWith("---")) {
                    dataStarted = true;
                    continue;
                }
                if (!dataStarted || line.isEmpty() || line.startsWith("Total")) {
                    continue;
                }
                
                try {
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 4) {
                        int idx = 0;
                        if (parts[idx].endsWith(":")) idx++;
                        long instances = Long.parseLong(parts[idx++]);
                        long bytes = Long.parseLong(parts[idx++]);
                        String className = parts[idx];
                        snapshot.put(className, new Long[]{instances, bytes});
                    }
                } catch (NumberFormatException e) {
                    // 跳过无法解析的行
                }
            }
        } catch (Exception e) {
            log.warn("无法获取堆直方图: {}", e.getMessage());
        }
        
        return snapshot;
    }
    
    /**
     * JVM诊断信息
     *
     * @param includeEnv 是否包含环境变量
     * @return JVM诊断信息
     */
    @GetMapping("/diagnostic")
    @Operation(summary = "JVM诊断信息")
    public ReturnResult<JvmInfo.JvmDiagnostic> getDiagnostic(
            @RequestParam(value = "includeEnv", defaultValue = "false") Boolean includeEnv) {
        
        JvmInfo.JvmDiagnostic diagnostic = new JvmInfo.JvmDiagnostic();
        diagnostic.setTimestamp(System.currentTimeMillis());
        
        // VM Flags
        List<JvmInfo.VmFlag> vmFlags = new ArrayList<>();
        try {
            javax.management.MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            javax.management.ObjectName diagCmdName = new javax.management.ObjectName("com.sun.management:type=DiagnosticCommand");
            
            String[] emptyArgs = new String[0];
            String flagsOutput = (String) server.invoke(diagCmdName, "vmFlags", 
                    new Object[]{emptyArgs}, new String[]{String[].class.getName()});
            
            // 解析VM flags输出
            String[] lines = flagsOutput.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || !line.contains("=")) continue;
                
                JvmInfo.VmFlag flag = new JvmInfo.VmFlag();
                
                // 解析格式: -XX:xxx=yyy {origin}
                if (line.startsWith("-XX:")) {
                    int eqIdx = line.indexOf('=');
                    if (eqIdx > 0) {
                        flag.setName(line.substring(0, eqIdx));
                        String rest = line.substring(eqIdx + 1);
                        int braceIdx = rest.indexOf('{');
                        if (braceIdx > 0) {
                            flag.setValue(rest.substring(0, braceIdx).trim());
                            String origin = rest.substring(braceIdx + 1, rest.length() - 1).trim();
                            flag.setOrigin(origin);
                        } else {
                            flag.setValue(rest.trim());
                            flag.setOrigin("DEFAULT");
                        }
                        vmFlags.add(flag);
                    }
                } else if (line.contains(" ")) {
                    // 另一种格式
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 2) {
                        flag.setName(parts[0]);
                        flag.setValue(parts[1]);
                        flag.setOrigin(parts.length > 2 ? parts[2] : "DEFAULT");
                        vmFlags.add(flag);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("无法获取VM flags: {}", e.getMessage());
        }
        diagnostic.setVmFlags(vmFlags);
        
        // 系统属性
        Map<String, String> sysProps = new LinkedHashMap<>();
        Properties properties = System.getProperties();
        // 只包含重要的属性
        String[] importantProps = {
            "java.version", "java.vendor", "java.home", "java.class.path",
            "java.library.path", "os.name", "os.arch", "os.version",
            "user.name", "user.home", "user.dir", "file.encoding",
            "sun.jnu.encoding", "java.io.tmpdir", "java.vm.name", "java.vm.version"
        };
        for (String prop : importantProps) {
            String value = properties.getProperty(prop);
            if (value != null) {
                sysProps.put(prop, value);
            }
        }
        diagnostic.setSystemProperties(sysProps);
        
        // 环境变量(可选)
        if (includeEnv) {
            Map<String, String> envVars = new LinkedHashMap<>();
            Map<String, String> env = System.getenv();
            // 只包含java相关的环境变量
            for (Map.Entry<String, String> entry : env.entrySet()) {
                String key = entry.getKey().toUpperCase();
                if (key.contains("JAVA") || key.contains("JVM") || key.contains("CLASSPATH") ||
                    key.equals("PATH") || key.equals("HOME") || key.equals("USER")) {
                    envVars.put(entry.getKey(), entry.getValue());
                }
            }
            diagnostic.setEnvironmentVariables(envVars);
        }
        
        // 类加载器信息
        List<JvmInfo.ClassLoaderInfo> classLoaders = new ArrayList<>();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Set<ClassLoader> visited = new HashSet<>();
        while (cl != null && !visited.contains(cl)) {
            visited.add(cl);
            JvmInfo.ClassLoaderInfo clInfo = new JvmInfo.ClassLoaderInfo();
            clInfo.setName(cl.getName() != null ? cl.getName() : cl.getClass().getSimpleName());
            clInfo.setType(cl.getClass().getName());
            clInfo.setParent(cl.getParent() != null ? cl.getParent().getClass().getName() : null);
            classLoaders.add(clInfo);
            cl = cl.getParent();
        }
        diagnostic.setClassLoaders(classLoaders);
        
        // 内存池详情
        List<JvmInfo.MemoryPoolDetail> poolDetails = new ArrayList<>();
        List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean pool : pools) {
            JvmInfo.MemoryPoolDetail detail = new JvmInfo.MemoryPoolDetail();
            detail.setName(pool.getName());
            detail.setType(pool.getType().name());
            detail.setUsage(convertMemoryUsage(pool.getUsage()));
            detail.setPeakUsage(convertMemoryUsage(pool.getPeakUsage()));
            if (pool.getCollectionUsage() != null) {
                detail.setCollectionUsage(convertMemoryUsage(pool.getCollectionUsage()));
            }
            if (pool.isUsageThresholdSupported()) {
                detail.setUsageThreshold((double) pool.getUsageThreshold());
                detail.setUsageThresholdExceeded(pool.isUsageThresholdExceeded());
                detail.setUsageThresholdCount(pool.getUsageThresholdCount());
            }
            poolDetails.add(detail);
        }
        diagnostic.setMemoryPoolDetails(poolDetails);
        
        return ReturnResult.ok(diagnostic);
    }
    
    /**
     * 执行VM诊断命令
     *
     * @param command 命令名称
     * @return 命令输出
     */
    @GetMapping("/vm-command")
    @Operation(summary = "执行VM诊断命令")
    public ReturnResult<String> executeVmCommand(
            @RequestParam("command") String command) {
        
        // 只允许执行安全的只读命令
        Set<String> allowedCommands = new HashSet<>(Arrays.asList(
            "vmFlags", "vmInfo", "vmVersion", "vmCommandLine",
            "gcClassHistogram", "gcHeapInfo", "vmSystemProperties",
            "vmUptime", "threadPrint"
        ));
        
        if (!allowedCommands.contains(command)) {
            return ReturnResult.error("不允许执行该命令: " + command + "。允许的命令: " + allowedCommands);
        }
        
        try {
            javax.management.MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            javax.management.ObjectName diagCmdName = new javax.management.ObjectName("com.sun.management:type=DiagnosticCommand");
            
            String[] emptyArgs = new String[0];
            String result = (String) server.invoke(diagCmdName, command, 
                    new Object[]{emptyArgs}, new String[]{String[].class.getName()});
            
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("执行VM命令失败: {}", command, e);
            return ReturnResult.error("执行命令失败: " + e.getMessage());
        }
    }

    /**
     * 转换内存使用信息
     */
    private JvmInfo.MemoryInfo convertMemoryUsage(MemoryUsage memoryUsage) {
        if (memoryUsage == null) {
            return null;
        }
        JvmInfo.MemoryInfo memoryInfo = new JvmInfo.MemoryInfo();
        memoryInfo.setInit(memoryUsage.getInit());
        memoryInfo.setUsed(memoryUsage.getUsed());
        memoryInfo.setCommitted(memoryUsage.getCommitted());
        memoryInfo.setMax(memoryUsage.getMax());
        if (memoryUsage.getMax() > 0) {
            memoryInfo.setUsagePercent((double) memoryUsage.getUsed() / memoryUsage.getMax() * 100);
        } else {
            memoryInfo.setUsagePercent(0.0);
        }
        return memoryInfo;
    }
}
