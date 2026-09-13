package com.chua.starter.system.pojo.jvm;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

/**
* JVM 信息
*
* @since 2024/12/18
* @author CH
 */
@Getter
@Schema(description = "JVM 信息")
public class JvmInfo implements Serializable {

    /** 串行版本UID */
    private static final long serialVersionUID = 1L;

    @Schema(description = "JVM 名称")
    /** JVM名称 */
    private String jvmName;

    @Schema(description = "JVM 版本")
    /** JVM版本 */
    private String jvmVersion;

    @Schema(description = "JVM 供应商")
    /** jvmvendor */
    private String jvmVendor;

    @Schema(description = "Java 版本")
    /** Java版本 */
    private String javaVersion;

    @Schema(description = "Java Home")
    /** Javahome */
    private String javaHome;

    @Schema(description = "JVM 启动时间")
    /** 开始时间 */
    private Long startTime;

    @Schema(description = "JVM 运行时长(毫秒)")
    /** Uptime */
    private Long uptime;

    @Schema(description = "JVM 输入参数")
    /** 输入参数 */
    private List<String> inputArguments;

    @Schema(description = "堆内存信息")
    /** Heapmemory */
    private MemoryInfo heapMemory;

    @Schema(description = "非堆内存信息")
    /** nonheapmemory */
    private MemoryInfo nonHeapMemory;

    @Schema(description = "内存池信息列表")
    /** Memorypools */
    private List<MemoryPoolInfo> memoryPools;

    @Schema(description = "线程信息")
    /** 线程信息 */
    private ThreadInfo threadInfo;

    @Schema(description = "线程详情列表(按CPU占用排序)")
    /** 顶部cputhreads */
    private List<ThreadDetail> topCpuThreads;

    @Schema(description = "类加载信息")
    /** Classloading信息 */
    private ClassLoadingInfo classLoadingInfo;

    @Schema(description = "GC 信息列表")
    /** Garbagecollectors */
    private List<GarbageCollectorInfo> garbageCollectors;

    @Schema(description = "操作系统信息")
    /** OS信息 */
    private OsInfo osInfo;

    @Schema(description = "CPU 信息")
    /** CPU信息 */
    private CpuInfo cpuInfo;

    @Schema(description = "编译器信息")
    /** Compilation信息 */
    private CompilationInfo compilationInfo;

    @Schema(description = "运行时信息")
    /** Runtime信息 */
    private RuntimeInfo runtimeInfo;

    @Schema(description = "缓冲池信息")
    /** 缓冲区游泳池 */
    private List<BufferPoolInfo> bufferPools;

    @Schema(description = "文件描述符信息")
    /** 文件descriptor信息 */
    private FileDescriptorInfo fileDescriptorInfo;

    @Schema(description = "线程状态分布")
    /** 线程状态distribution */
    private ThreadStateDistribution threadStateDistribution;

    @Schema(description = "JIT编译统计")
    /** jitcompilation统计 */
    private JitCompilationStats jitCompilationStats;

    @Schema(description = "代码缓存信息")
    /** 代码缓存信息 */
    private CodeCacheInfo codeCacheInfo;

    @Schema(description = "GC统计信息")
    /** GC统计 */
    private GcStats gcStats;

    @Schema(description = "元空间信息")
    /** Metaspace信息 */
    private MetaspaceInfo metaspaceInfo;

    @Schema(description = "直接内存信息")
    /** Directmemory信息 */
    private DirectMemoryInfo directMemoryInfo;

    @Schema(description = "内存分配速率")
    /** Memoryallocation比率 */
    private MemoryAllocationRate memoryAllocationRate;

    @Schema(description = "类加载详情")
    /** Classloadingdetail */
    private ClassLoadingDetail classLoadingDetail;

    @Schema(description = "安全管理器信息")
    /** 安全性信息 */
    private SecurityInfo securityInfo;

    /**
    * 内存信息
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "内存信息")
    public static class MemoryInfo implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "初始内存(字节)")
        /** 初始化 */
        private Long init;

        @Schema(description = "已使用内存(字节)")
        /** Used */
        private Long used;

        @Schema(description = "已提交内存(字节)")
        /** Committed */
        private Long committed;

        @Schema(description = "最大内存(字节)")
        /** 最大值 */
        private Long max;

        @Schema(description = "使用率(%)")
        /** Usage百分比 */
        private Double usagePercent;

    // === Manually generated setters ===
    public void setInit(Long init) { this.init = init; }
    public void setUsed(Long used) { this.used = used; }
    public void setCommitted(Long committed) { this.committed = committed; }
    public void setMax(Long max) { this.max = max; }
    public void setUsagePercent(Double usagePercent) { this.usagePercent = usagePercent; }
    }

    /**
    * 内存池信息
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "内存池信息")
    public static class MemoryPoolInfo implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "内存池名称")
        /** 名称 */
        private String name;

        @Schema(description = "内存池类型")
        /** 类型 */
        private String type;

        @Schema(description = "内存使用信息")
        /** Usage */
        private MemoryInfo usage;

        @Schema(description = "内存池峰值使用")
        /** Peakusage */
        private MemoryInfo peakUsage;

    // === Manually generated setters ===
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setUsage(MemoryInfo usage) { this.usage = usage; }
    public void setPeakUsage(MemoryInfo peakUsage) { this.peakUsage = peakUsage; }
    }

    /**
    * 线程信息
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "线程信息")
    public static class ThreadInfo implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "当前线程数")
        /** 线程数量 */
        private Integer threadCount;

        @Schema(description = "峰值线程数")
        /** Peak线程数量 */
        private Integer peakThreadCount;

        @Schema(description = "守护线程数")
        /** Daemon线程数量 */
        private Integer daemonThreadCount;

        @Schema(description = "总启动线程数")
        /** 总数启动线程数量 */
        private Long totalStartedThreadCount;

        @Schema(description = "死锁线程数")
        /** Deadlocked线程数量 */
        private Integer deadlockedThreadCount;

    // === Manually generated setters ===
    public void setThreadCount(Integer threadCount) { this.threadCount = threadCount; }
    public void setPeakThreadCount(Integer peakThreadCount) { this.peakThreadCount = peakThreadCount; }
    public void setDaemonThreadCount(Integer daemonThreadCount) { this.daemonThreadCount = daemonThreadCount; }
    public void setTotalStartedThreadCount(Long totalStartedThreadCount) { this.totalStartedThreadCount = totalStartedThreadCount; }
    public void setDeadlockedThreadCount(Integer deadlockedThreadCount) { this.deadlockedThreadCount = deadlockedThreadCount; }
    }

    /**
    * 线程详情
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "线程详情")
    public static class ThreadDetail implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "线程ID")
        /** 线程标识 */
        private Long threadId;

        @Schema(description = "线程名称")
        /** 线程名称 */
        private String threadName;

        @Schema(description = "线程状态")
        /** 状态 */
        private String state;

        @Schema(description = "CPU 时间(纳秒)")
        /** CPU时间 */
        private Long cpuTime;

        @Schema(description = "用户时间(纳秒)")
        /** 用户时间 */
        private Long userTime;

        @Schema(description = "CPU 使用率(%)")
        /** cpuusage */
        private Double cpuUsage;

        @Schema(description = "是否守护线程")
        /** Daemon */
        private Boolean daemon;

        @Schema(description = "线程优先级")
        /** 优先级 */
        private Integer priority;

        @Schema(description = "阻塞次数")
        /** Blocked数量 */
        private Long blockedCount;

        @Schema(description = "阻塞时间(毫秒)")
        /** Blocked时间 */
        private Long blockedTime;

        @Schema(description = "等待次数")
        /** Waited数量 */
        private Long waitedCount;

        @Schema(description = "等待时间(毫秒)")
        /** Waited时间 */
        private Long waitedTime;

        @Schema(description = "锁名称")
        /** 锁名称 */
        private String lockName;

        @Schema(description = "锁拥有者ID")
        /** 锁ownerid */
        private Long lockOwnerId;

        @Schema(description = "锁拥有者名称")
        /** 锁owner名称 */
        private String lockOwnerName;

        @Schema(description = "堆栈跟踪")
        /** 栈跟踪 */
        private String[] stackTrace;

    // === Manually generated setters ===
    public void setThreadId(Long threadId) { this.threadId = threadId; }
    public void setThreadName(String threadName) { this.threadName = threadName; }
    public void setState(String state) { this.state = state; }
    public void setCpuTime(Long cpuTime) { this.cpuTime = cpuTime; }
    public void setUserTime(Long userTime) { this.userTime = userTime; }
    public void setCpuUsage(Double cpuUsage) { this.cpuUsage = cpuUsage; }
    public void setDaemon(Boolean daemon) { this.daemon = daemon; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public void setBlockedCount(Long blockedCount) { this.blockedCount = blockedCount; }
    public void setBlockedTime(Long blockedTime) { this.blockedTime = blockedTime; }
    public void setWaitedCount(Long waitedCount) { this.waitedCount = waitedCount; }
    public void setWaitedTime(Long waitedTime) { this.waitedTime = waitedTime; }
    public void setLockName(String lockName) { this.lockName = lockName; }
    public void setLockOwnerId(Long lockOwnerId) { this.lockOwnerId = lockOwnerId; }
    public void setLockOwnerName(String lockOwnerName) { this.lockOwnerName = lockOwnerName; }
    public void setStackTrace(String[] stackTrace) { this.stackTrace = stackTrace; }
    }

    /**
    * CPU 热点代码信息
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "CPU 热点代码信息")
    public static class CpuHotspot implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "类名")
        /** 类名称 */
        private String className;

        @Schema(description = "方法名")
        /** 方法名称 */
        private String methodName;

        @Schema(description = "文件名")
        /** 文件名称 */
        private String fileName;

        @Schema(description = "行号")
        /** 线数字 */
        private Integer lineNumber;

        @Schema(description = "采样命中次数")
        /** HIT数量 */
        private Integer hitCount;

        @Schema(description = "占比(%)")
        /** Percentage */
        private Double percentage;

        @Schema(description = "完整方法签名")
        /** Fullmethod */
        private String fullMethod;

        @Schema(description = "调用者列表")
        /** Callers */
        private List<String> callers;

    // === Manually generated setters ===
    public void setClassName(String className) { this.className = className; }
    public void setMethodName(String methodName) { this.methodName = methodName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setLineNumber(Integer lineNumber) { this.lineNumber = lineNumber; }
    public void setHitCount(Integer hitCount) { this.hitCount = hitCount; }
    public void setPercentage(Double percentage) { this.percentage = percentage; }
    public void setFullMethod(String fullMethod) { this.fullMethod = fullMethod; }
    public void setCallers(List<String> callers) { this.callers = callers; }
    }

    /**
    * CPU 热点分析结果
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "CPU 热点分析结果")
    public static class CpuHotspotAnalysis implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "采样次数")
        /** 示例数量 */
        private Integer sampleCount;

        @Schema(description = "采样时长(毫秒)")
        /** 示例持续时间 */
        private Long sampleDuration;

        @Schema(description = "采样间隔(毫秒)")
        /** 示例间隔 */
        private Integer sampleInterval;

        @Schema(description = "分析的线程数")
        /** 线程数量 */
        private Integer threadCount;

        @Schema(description = "热点方法列表")
        /** Hotspots */
        private List<CpuHotspot> hotspots;

        @Schema(description = "热点类统计")
        /** Classhotspots */
        private List<ClassHotspot> classHotspots;

        @Schema(description = "热点包统计")
        /** Packagehotspots */
        private List<PackageHotspot> packageHotspots;

    // === Manually generated setters ===
    public void setSampleCount(Integer sampleCount) { this.sampleCount = sampleCount; }
    public void setSampleDuration(Long sampleDuration) { this.sampleDuration = sampleDuration; }
    public void setSampleInterval(Integer sampleInterval) { this.sampleInterval = sampleInterval; }
    public void setThreadCount(Integer threadCount) { this.threadCount = threadCount; }
    public void setHotspots(List<CpuHotspot> hotspots) { this.hotspots = hotspots; }
    public void setClassHotspots(List<ClassHotspot> classHotspots) { this.classHotspots = classHotspots; }
    public void setPackageHotspots(List<PackageHotspot> packageHotspots) { this.packageHotspots = packageHotspots; }
    }

    /**
    * 类级别热点
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "类级别热点")
    public static class ClassHotspot implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "类名")
        /** 类名称 */
        private String className;

        @Schema(description = "采样命中次数")
        /** HIT数量 */
        private Integer hitCount;

        @Schema(description = "占比(%)")
        /** Percentage */
        private Double percentage;

        @Schema(description = "方法数")
        /** 方法数量 */
        private Integer methodCount;

    // === Manually generated setters ===
    public void setClassName(String className) { this.className = className; }
    public void setHitCount(Integer hitCount) { this.hitCount = hitCount; }
    public void setPercentage(Double percentage) { this.percentage = percentage; }
    public void setMethodCount(Integer methodCount) { this.methodCount = methodCount; }
    }

    /**
    * 包级别热点
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "包级别热点")
    public static class PackageHotspot implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "包名")
        /** 包名称 */
        private String packageName;

        @Schema(description = "采样命中次数")
        /** HIT数量 */
        private Integer hitCount;

        @Schema(description = "占比(%)")
        /** Percentage */
        private Double percentage;

        @Schema(description = "类数")
        /** 类数量 */
        private Integer classCount;

    // === Manually generated setters ===
    public void setPackageName(String packageName) { this.packageName = packageName; }
    public void setHitCount(Integer hitCount) { this.hitCount = hitCount; }
    public void setPercentage(Double percentage) { this.percentage = percentage; }
    public void setClassCount(Integer classCount) { this.classCount = classCount; }
    }

    /**
    * 类加载信息
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "类加载信息")
    public static class ClassLoadingInfo implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "已加载类数量")
        /** Loadedclass数量 */
        private Integer loadedClassCount;

        @Schema(description = "总加载类数量")
        /** 总数loadedclass数量 */
        private Long totalLoadedClassCount;

        @Schema(description = "已卸载类数量")
        /** Unloadedclass数量 */
        private Long unloadedClassCount;

    // === Manually generated setters ===
    public void setLoadedClassCount(Integer loadedClassCount) { this.loadedClassCount = loadedClassCount; }
    public void setTotalLoadedClassCount(Long totalLoadedClassCount) { this.totalLoadedClassCount = totalLoadedClassCount; }
    public void setUnloadedClassCount(Long unloadedClassCount) { this.unloadedClassCount = unloadedClassCount; }
    }

    /**
    * 垃圾收集器信息
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "垃圾收集器信息")
    public static class GarbageCollectorInfo implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "收集器名称")
        /** 名称 */
        private String name;

        @Schema(description = "收集次数")
        /** 集合数量 */
        private Long collectionCount;

        @Schema(description = "收集时间(毫秒)")
        /** 集合时间 */
        private Long collectionTime;

        @Schema(description = "内存池名称")
        /** 内存池名称 */
        private String[] memoryPoolNames;

    // === Manually generated setters ===
    public void setName(String name) { this.name = name; }
    public void setCollectionCount(Long collectionCount) { this.collectionCount = collectionCount; }
    public void setCollectionTime(Long collectionTime) { this.collectionTime = collectionTime; }
    public void setMemoryPoolNames(String[] memoryPoolNames) { this.memoryPoolNames = memoryPoolNames; }
    }

    /**
    * 操作系统信息
    * @author CH
    * @since 4.0.0
     */
    @lombok.Getter
    @lombok.Setter
    @Schema(description = "操作系统信息")
    public static class OsInfo implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "操作系统名称")
        /** 名称 */
        private String name;

        @Schema(description = "操作系统版本")
        /** 版本 */
        private String version;

        @Schema(description = "操作系统架构")
        /** Arch Linux */
        private String arch;

        @Schema(description = "可用处理器数量")
        /** 可用处理器 */
        private Integer availableProcessors;

        @Schema(description = "系统负载平均值")
        /** Systemloadaverage */
        private Double systemLoadAverage;

        @Schema(description = "总物理内存(字节)")
        /** 总数physicalmemory */
        private Long totalPhysicalMemory;

        @Schema(description = "空闲物理内存(字节)")
        /** Freephysicalmemory */
        private Long freePhysicalMemory;

        @Schema(description = "总交换空间(字节)")
        /** 总数swapspace */
        private Long totalSwapSpace;

        @Schema(description = "空闲交换空间(字节)")
        /** Freeswapspace */
        private Long freeSwapSpace;

    // === Manually generated setters ===
    public void setName(String name) { this.name = name; }
    public void setVersion(String version) { this.version = version; }
    public void setArch(String arch) { this.arch = arch; }
    public void setAvailableProcessors(Integer availableProcessors) { this.availableProcessors = availableProcessors; }
    public void setSystemLoadAverage(Double systemLoadAverage) { this.systemLoadAverage = systemLoadAverage; }
    public void setTotalPhysicalMemory(Long totalPhysicalMemory) { this.totalPhysicalMemory = totalPhysicalMemory; }
    public void setFreePhysicalMemory(Long freePhysicalMemory) { this.freePhysicalMemory = freePhysicalMemory; }
    public void setTotalSwapSpace(Long totalSwapSpace) { this.totalSwapSpace = totalSwapSpace; }
    public void setFreeSwapSpace(Long freeSwapSpace) { this.freeSwapSpace = freeSwapSpace; }
    }

    /**
    * CPU 信息
    * @author CH
    * @since 4.0.0
     */
    @lombok.Getter
    @lombok.Setter
    @Schema(description = "CPU 信息")
    public static class CpuInfo implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "进程 CPU 使用率(%)")
        /** 处理cpuload */
        private Double processCpuLoad;

        @Schema(description = "系统 CPU 使用率(%)")
        /** 系统cpuload */
        private Double systemCpuLoad;

        @Schema(description = "进程 CPU 时间(纳秒)")
        /** 处理cpu时间 */
        private Long processCpuTime;

    // === Manually generated setters ===
    public void setProcessCpuLoad(Double processCpuLoad) { this.processCpuLoad = processCpuLoad; }
    public void setSystemCpuLoad(Double systemCpuLoad) { this.systemCpuLoad = systemCpuLoad; }
    public void setProcessCpuTime(Long processCpuTime) { this.processCpuTime = processCpuTime; }
    }

    /**
    * 编译器信息
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "编译器信息")
    public static class CompilationInfo implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "编译器名称")
        /** 名称 */
        private String name;

        @Schema(description = "总编译时间(毫秒)")
        /** 总数compilation时间 */
        private Long totalCompilationTime;

        @Schema(description = "是否支持编译时间监控")
        /** Compilation时间monitoringsupported */
        private Boolean compilationTimeMonitoringSupported;

    // === Manually generated setters ===
    public void setName(String name) { this.name = name; }
    public void setTotalCompilationTime(Long totalCompilationTime) { this.totalCompilationTime = totalCompilationTime; }
    public void setCompilationTimeMonitoringSupported(Boolean compilationTimeMonitoringSupported) { this.compilationTimeMonitoringSupported = compilationTimeMonitoringSupported; }
    }

    /**
    * 运行时信息
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "运行时信息")
    public static class RuntimeInfo implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "进程ID")
        /** PID */
        private String pid;

        @Schema(description = "启动类路径")
        /** Bootclass路径 */
        private String bootClassPath;

        @Schema(description = "类路径")
        /** 类路径 */
        private String classPath;

        @Schema(description = "库路径")
        /** 图书馆路径 */
        private String libraryPath;

        @Schema(description = "管理规范版本")
        /** Management规范版本 */
        private String managementSpecVersion;

        @Schema(description = "规范名称")
        /** 规范名称 */
        private String specName;

        @Schema(description = "规范供应商")
        /** 规范供应商 */
        private String specVendor;

        @Schema(description = "规范版本")
        /** 规范版本 */
        private String specVersion;

    // === Manually generated setters ===
    public void setPid(String pid) { this.pid = pid; }
    public void setBootClassPath(String bootClassPath) { this.bootClassPath = bootClassPath; }
    public void setClassPath(String classPath) { this.classPath = classPath; }
    public void setLibraryPath(String libraryPath) { this.libraryPath = libraryPath; }
    public void setManagementSpecVersion(String managementSpecVersion) { this.managementSpecVersion = managementSpecVersion; }
    public void setSpecName(String specName) { this.specName = specName; }
    public void setSpecVendor(String specVendor) { this.specVendor = specVendor; }
    public void setSpecVersion(String specVersion) { this.specVersion = specVersion; }
    }

    /**
    * 缓冲池信息
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "缓冲池信息")
    public static class BufferPoolInfo implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "缓冲池名称")
        /** 名称 */
        private String name;

        @Schema(description = "缓冲区数量")
        /** 数量 */
        private Long count;

        @Schema(description = "已使用内存(字节)")
        /** Memoryused */
        private Long memoryUsed;

        @Schema(description = "总容量(字节)")
        /** 总数容量 */
        private Long totalCapacity;

    // === Manually generated setters ===
    public void setName(String name) { this.name = name; }
    public void setCount(Long count) { this.count = count; }
    public void setMemoryUsed(Long memoryUsed) { this.memoryUsed = memoryUsed; }
    public void setTotalCapacity(Long totalCapacity) { this.totalCapacity = totalCapacity; }
    }

    /**
    * 文件描述符信息
    * @author CH
    * @since 4.0.0
     */
    @lombok.Getter
    @lombok.Setter
    @Schema(description = "文件描述符信息")
    public static class FileDescriptorInfo implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "已打开的文件描述符数")
        /** 打开文件descriptor数量 */
        private Long openFileDescriptorCount;

        @Schema(description = "最大文件描述符数")
        /** 最大值文件descriptor数量 */
        private Long maxFileDescriptorCount;

        @Schema(description = "使用率(%)")
        /** Usage百分比 */
        private Double usagePercent;

    // === Manually generated setters ===
    public void setOpenFileDescriptorCount(Long openFileDescriptorCount) { this.openFileDescriptorCount = openFileDescriptorCount; }
    public void setMaxFileDescriptorCount(Long maxFileDescriptorCount) { this.maxFileDescriptorCount = maxFileDescriptorCount; }
    public void setUsagePercent(Double usagePercent) { this.usagePercent = usagePercent; }
    }

    /**
    * 内存对象信息
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "内存对象信息")
    public static class HeapObjectInfo implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "类名")
        /** 类名称 */
        private String className;

        @Schema(description = "实例数量")
        /** 实例数量 */
        private Long instanceCount;

        @Schema(description = "占用内存(字节)")
        /** Bytes */
        private Long bytes;

        @Schema(description = "内存占比(%)")
        /** Percentage */
        private Double percentage;

        @Schema(description = "简化类名")
        /** Simpleclass名称 */
        private String simpleClassName;

    // === Manually generated setters ===
    public void setClassName(String className) { this.className = className; }
    public void setInstanceCount(Long instanceCount) { this.instanceCount = instanceCount; }
    public void setBytes(Long bytes) { this.bytes = bytes; }
    public void setPercentage(Double percentage) { this.percentage = percentage; }
    public void setSimpleClassName(String simpleClassName) { this.simpleClassName = simpleClassName; }
    }

    /**
    * 内存分析结果
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "内存分析结果")
    public static class HeapAnalysis implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "分析时间")
        /** Analysis时间 */
        private Long analysisTime;

        @Schema(description = "堆内存使用")
        /** Heapusage */
        private MemoryInfo heapUsage;

        @Schema(description = "对象总数")
        /** 总数对象 */
        private Long totalObjects;

        @Schema(description = "占用内存总量(字节)")
        /** 总数bytes */
        private Long totalBytes;

        @Schema(description = "类数量")
        /** 类数量 */
        private Integer classCount;

        @Schema(description = "对象列表(按内存排序)")
        /** 顶部bybytes */
        private List<HeapObjectInfo> topByBytes;

        @Schema(description = "对象列表(按数量排序)")
        /** 顶部BY数量 */
        private List<HeapObjectInfo> topByCount;

        @Schema(description = "包级别内存统计")
        /** Packagememory */
        private List<PackageMemory> packageMemory;

    // === Manually generated setters ===
    public void setAnalysisTime(Long analysisTime) { this.analysisTime = analysisTime; }
    public void setHeapUsage(MemoryInfo heapUsage) { this.heapUsage = heapUsage; }
    public void setTotalObjects(Long totalObjects) { this.totalObjects = totalObjects; }
    public void setTotalBytes(Long totalBytes) { this.totalBytes = totalBytes; }
    public void setClassCount(Integer classCount) { this.classCount = classCount; }
    public void setTopByBytes(List<HeapObjectInfo> topByBytes) { this.topByBytes = topByBytes; }
    public void setTopByCount(List<HeapObjectInfo> topByCount) { this.topByCount = topByCount; }
    public void setPackageMemory(List<PackageMemory> packageMemory) { this.packageMemory = packageMemory; }
    }

    /**
    * 包级别内存统计
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "包级别内存统计")
    public static class PackageMemory implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "包名")
        /** 包名称 */
        private String packageName;

        @Schema(description = "占用内存(字节)")
        /** Bytes */
        private Long bytes;

        @Schema(description = "内存占比(%)")
        /** Percentage */
        private Double percentage;

        @Schema(description = "实例数量")
        /** 实例数量 */
        private Long instanceCount;

        @Schema(description = "类数量")
        /** 类数量 */
        private Integer classCount;

    // === Manually generated setters ===
    public void setPackageName(String packageName) { this.packageName = packageName; }
    public void setBytes(Long bytes) { this.bytes = bytes; }
    public void setPercentage(Double percentage) { this.percentage = percentage; }
    public void setInstanceCount(Long instanceCount) { this.instanceCount = instanceCount; }
    public void setClassCount(Integer classCount) { this.classCount = classCount; }
    }

    /**
    * 业务代码归因分析结果
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "业务代码归因分析结果")
    public static class BusinessCodeAnalysis implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "分析时间")
        /** Analysis时间 */
        private Long analysisTime;

        @Schema(description = "采样次数")
        /** 示例数量 */
        private Integer sampleCount;

        @Schema(description = "采样间隔(毫秒)")
        /** 示例间隔 */
        private Integer sampleInterval;

        @Schema(description = "业务包前缀")
        /** Businesspackage */
        private String businessPackage;

        @Schema(description = "CPU相关业务代码")
        /** cpuimpacts */
        private List<BusinessCodeImpact> cpuImpacts;

        @Schema(description = "内存相关业务代码")
        /** Memoryimpacts */
        private List<BusinessCodeImpact> memoryImpacts;

        @Schema(description = "调用链分析")
        /** Callchains */
        private List<CallChainAnalysis> callChains;

        @Schema(description = "业务入口点")
        /** Entrypoints */
        private List<BusinessEntryPoint> entryPoints;

    // === Manually generated setters ===
    public void setAnalysisTime(Long analysisTime) { this.analysisTime = analysisTime; }
    public void setSampleCount(Integer sampleCount) { this.sampleCount = sampleCount; }
    public void setSampleInterval(Integer sampleInterval) { this.sampleInterval = sampleInterval; }
    public void setBusinessPackage(String businessPackage) { this.businessPackage = businessPackage; }
    public void setCpuImpacts(List<BusinessCodeImpact> cpuImpacts) { this.cpuImpacts = cpuImpacts; }
    public void setMemoryImpacts(List<BusinessCodeImpact> memoryImpacts) { this.memoryImpacts = memoryImpacts; }
    public void setCallChains(List<CallChainAnalysis> callChains) { this.callChains = callChains; }
    public void setEntryPoints(List<BusinessEntryPoint> entryPoints) { this.entryPoints = entryPoints; }
    }

    /**
    * 业务代码影响
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "业务代码影响")
    public static class BusinessCodeImpact implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "业务方法")
        /** Businessmethod */
        private String businessMethod;

        @Schema(description = "类名")
        /** 类名称 */
        private String className;

        @Schema(description = "方法名")
        /** 方法名称 */
        private String methodName;

        @Schema(description = "文件名")
        /** 文件名称 */
        private String fileName;

        @Schema(description = "行号")
        /** 线数字 */
        private Integer lineNumber;

        @Schema(description = "影响分数")
        /** Impact分数 */
        private Integer impactScore;

        @Schema(description = "影响占比(%)")
        /** Impactpercentage */
        private Double impactPercentage;

        @Schema(description = "关联热点")
        /** Relatedhotspots */
        private List<String> relatedHotspots;

        @Schema(description = "调用次数")
        /** Call数量 */
        private Integer callCount;

        @Schema(description = "影响类型: CPU/MEMORY/BOTH")
        /** Impact类型 */
        private String impactType;

        @Schema(description = "描述")
        /** 描述 */
        private String description;

    // === Manually generated setters ===
    public void setBusinessMethod(String businessMethod) { this.businessMethod = businessMethod; }
    public void setClassName(String className) { this.className = className; }
    public void setMethodName(String methodName) { this.methodName = methodName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setLineNumber(Integer lineNumber) { this.lineNumber = lineNumber; }
    public void setImpactScore(Integer impactScore) { this.impactScore = impactScore; }
    public void setImpactPercentage(Double impactPercentage) { this.impactPercentage = impactPercentage; }
    public void setRelatedHotspots(List<String> relatedHotspots) { this.relatedHotspots = relatedHotspots; }
    public void setCallCount(Integer callCount) { this.callCount = callCount; }
    public void setImpactType(String impactType) { this.impactType = impactType; }
    public void setDescription(String description) { this.description = description; }
    }

    /**
    * 调用链分析
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "调用链分析")
    public static class CallChainAnalysis implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "热点方法")
        /** Hotspotmethod */
        private String hotspotMethod;

        @Schema(description = "业务入口")
        /** Businessentry */
        private String businessEntry;

        @Schema(description = "调用链 (从业务入口到热点)")
        /** Callchain */
        private List<String> callChain;

        @Schema(description = "出现次数")
        /** Occurrence数量 */
        private Integer occurrenceCount;

        @Schema(description = "占比(%)")
        /** Percentage */
        private Double percentage;

    // === Manually generated setters ===
    public void setHotspotMethod(String hotspotMethod) { this.hotspotMethod = hotspotMethod; }
    public void setBusinessEntry(String businessEntry) { this.businessEntry = businessEntry; }
    public void setCallChain(List<String> callChain) { this.callChain = callChain; }
    public void setOccurrenceCount(Integer occurrenceCount) { this.occurrenceCount = occurrenceCount; }
    public void setPercentage(Double percentage) { this.percentage = percentage; }
    }

    /**
    * 业务入口点
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "业务入口点")
    public static class BusinessEntryPoint implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "入口方法")
        /** Entrymethod */
        private String entryMethod;

        @Schema(description = "类名")
        /** 类名称 */
        private String className;

        @Schema(description = "方法名")
        /** 方法名称 */
        private String methodName;

        @Schema(description = "入口类型: Controller/Service/Scheduler/MessageListener等")
        /** Entry类型 */
        private String entryType;

        @Schema(description = "触发次数")
        /** Trigger数量 */
        private Integer triggerCount;

        @Schema(description = "关联热点数量")
        /** Relatedhotspot数量 */
        private Integer relatedHotspotCount;

        @Schema(description = "CPU影响分数")
        /** cpuimpact分数 */
        private Integer cpuImpactScore;

        @Schema(description = "描述")
        /** 描述 */
        private String description;

    // === Manually generated setters ===
    public void setEntryMethod(String entryMethod) { this.entryMethod = entryMethod; }
    public void setClassName(String className) { this.className = className; }
    public void setMethodName(String methodName) { this.methodName = methodName; }
    public void setEntryType(String entryType) { this.entryType = entryType; }
    public void setTriggerCount(Integer triggerCount) { this.triggerCount = triggerCount; }
    public void setRelatedHotspotCount(Integer relatedHotspotCount) { this.relatedHotspotCount = relatedHotspotCount; }
    public void setCpuImpactScore(Integer cpuImpactScore) { this.cpuImpactScore = cpuImpactScore; }
    public void setDescription(String description) { this.description = description; }
    }

    /**
    * JVM 优化建议
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "JVM 优化建议")
    public static class JvmOptimizationAdvice implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "分析时间")
        /** Analysis时间 */
        private Long analysisTime;

        @Schema(description = "当前JVM参数")
        /** 当前参数 */
        private List<String> currentArgs;

        @Schema(description = "推荐JVM参数")
        /** Recommended参数 */
        private List<JvmParameter> recommendedParams;

        @Schema(description = "GC收集器建议")
        /** gcadvice */
        private GcAdvice gcAdvice;

        @Schema(description = "内存建议")
        /** Memoryadvice */
        private MemoryAdvice memoryAdvice;

        @Schema(description = "线程建议")
        /** 线程advice */
        private ThreadAdvice threadAdvice;

        @Schema(description = "性能建议")
        /** Performanceadvices */
        private List<PerformanceAdvice> performanceAdvices;

        @Schema(description = "完整启动命令")
        /** 完整命令 */
        private String fullCommand;

        @Schema(description = "健康评分(0-100)")
        /** 健康分数 */
        private Integer healthScore;

        @Schema(description = "诊断信息")
        /** Diagnostics */
        private List<String> diagnostics;

    // === Manually generated setters ===
    public void setAnalysisTime(Long analysisTime) { this.analysisTime = analysisTime; }
    public void setCurrentArgs(List<String> currentArgs) { this.currentArgs = currentArgs; }
    public void setRecommendedParams(List<JvmParameter> recommendedParams) { this.recommendedParams = recommendedParams; }
    public void setGcAdvice(GcAdvice gcAdvice) { this.gcAdvice = gcAdvice; }
    public void setMemoryAdvice(MemoryAdvice memoryAdvice) { this.memoryAdvice = memoryAdvice; }
    public void setThreadAdvice(ThreadAdvice threadAdvice) { this.threadAdvice = threadAdvice; }
    public void setPerformanceAdvices(List<PerformanceAdvice> performanceAdvices) { this.performanceAdvices = performanceAdvices; }
    public void setFullCommand(String fullCommand) { this.fullCommand = fullCommand; }
    public void setHealthScore(Integer healthScore) { this.healthScore = healthScore; }
    public void setDiagnostics(List<String> diagnostics) { this.diagnostics = diagnostics; }
    }

    /**
    * JVM 参数
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "JVM 参数")
    public static class JvmParameter implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "参数名")
        /** 名称 */
        private String name;

        @Schema(description = "当前值")
        /** 当前值 */
        private String currentValue;

        @Schema(description = "推荐值")
        /** Recommended值 */
        private String recommendedValue;

        @Schema(description = "参数类型: MEMORY/GC/PERFORMANCE/DEBUG")
        /** 类型 */
        private String type;

        @Schema(description = "优先级: HIGH/MEDIUM/LOW")
        /** 优先级 */
        private String priority;

        @Schema(description = "说明")
        /** 描述 */
        private String description;

        @Schema(description = "推荐原因")
        /** ReasonML */
        private String reason;

    // === Manually generated setters ===
    public void setName(String name) { this.name = name; }
    public void setCurrentValue(String currentValue) { this.currentValue = currentValue; }
    public void setRecommendedValue(String recommendedValue) { this.recommendedValue = recommendedValue; }
    public void setType(String type) { this.type = type; }
    public void setPriority(String priority) { this.priority = priority; }
    public void setDescription(String description) { this.description = description; }
    public void setReason(String reason) { this.reason = reason; }
    }

    /**
    * GC 收集器建议
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "GC 收集器建议")
    public static class GcAdvice implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "当前GC收集器")
        /** 当前GC */
        private String currentGc;

        @Schema(description = "推荐GC收集器")
        /** recommendedgc */
        private String recommendedGc;

        @Schema(description = "GC参数列表")
        /** GC参数 */
        private List<JvmParameter> gcParams;

        @Schema(description = "推荐原因")
        /** ReasonML */
        private String reason;

        @Schema(description = "GC频率评估")
        /** gcfrequencyassessment */
        private String gcFrequencyAssessment;

        @Schema(description = "GC停顿时间评估")
        /** gcpauseassessment */
        private String gcPauseAssessment;

    // === Manually generated setters ===
    public void setCurrentGc(String currentGc) { this.currentGc = currentGc; }
    public void setRecommendedGc(String recommendedGc) { this.recommendedGc = recommendedGc; }
    public void setGcParams(List<JvmParameter> gcParams) { this.gcParams = gcParams; }
    public void setReason(String reason) { this.reason = reason; }
    public void setGcFrequencyAssessment(String gcFrequencyAssessment) { this.gcFrequencyAssessment = gcFrequencyAssessment; }
    public void setGcPauseAssessment(String gcPauseAssessment) { this.gcPauseAssessment = gcPauseAssessment; }
    }

    /**
    * 内存建议
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "内存建议")
    public static class MemoryAdvice implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "推荐堆内存大小")
        /** Recommendedheap尺寸 */
        private String recommendedHeapSize;

        @Schema(description = "推荐初始堆大小")
        /** Recommendedinitialheap */
        private String recommendedInitialHeap;

        @Schema(description = "推荐新生代大小")
        /** recommendednewgen尺寸 */
        private String recommendedNewGenSize;

        @Schema(description = "推荐元空间大小")
        /** Recommendedmetaspace尺寸 */
        private String recommendedMetaspaceSize;

        @Schema(description = "内存参数列表")
        /** 内存参数 */
        private List<JvmParameter> memoryParams;

        @Schema(description = "当前堆使用率")
        /** 当前heapusage */
        private Double currentHeapUsage;

        @Schema(description = "内存压力评估")
        /** Memorypressureassessment */
        private String memoryPressureAssessment;

        @Schema(description = "建议说明")
        /** Advice */
        private String advice;

    // === Manually generated setters ===
    public void setRecommendedHeapSize(String recommendedHeapSize) { this.recommendedHeapSize = recommendedHeapSize; }
    public void setRecommendedInitialHeap(String recommendedInitialHeap) { this.recommendedInitialHeap = recommendedInitialHeap; }
    public void setRecommendedNewGenSize(String recommendedNewGenSize) { this.recommendedNewGenSize = recommendedNewGenSize; }
    public void setRecommendedMetaspaceSize(String recommendedMetaspaceSize) { this.recommendedMetaspaceSize = recommendedMetaspaceSize; }
    public void setMemoryParams(List<JvmParameter> memoryParams) { this.memoryParams = memoryParams; }
    public void setCurrentHeapUsage(Double currentHeapUsage) { this.currentHeapUsage = currentHeapUsage; }
    public void setMemoryPressureAssessment(String memoryPressureAssessment) { this.memoryPressureAssessment = memoryPressureAssessment; }
    public void setAdvice(String advice) { this.advice = advice; }
    }

    /**
    * 线程建议
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "线程建议")
    public static class ThreadAdvice implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "推荐线程栈大小")
        /** Recommended栈尺寸 */
        private String recommendedStackSize;

        @Schema(description = "当前线程数")
        /** 当前线程数量 */
        private Integer currentThreadCount;

        @Schema(description = "线程数评估")
        /** 线程数量assessment */
        private String threadCountAssessment;

        @Schema(description = "线程参数列表")
        /** 线程参数 */
        private List<JvmParameter> threadParams;

        @Schema(description = "建议说明")
        /** Advice */
        private String advice;

    // === Manually generated setters ===
    public void setRecommendedStackSize(String recommendedStackSize) { this.recommendedStackSize = recommendedStackSize; }
    public void setCurrentThreadCount(Integer currentThreadCount) { this.currentThreadCount = currentThreadCount; }
    public void setThreadCountAssessment(String threadCountAssessment) { this.threadCountAssessment = threadCountAssessment; }
    public void setThreadParams(List<JvmParameter> threadParams) { this.threadParams = threadParams; }
    public void setAdvice(String advice) { this.advice = advice; }
    }

    /**
    * 性能建议
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "性能建议")
    public static class PerformanceAdvice implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "类别: GC/MEMORY/THREAD/COMPILER/OTHER")
        /** 分类 */
        private String category;

        @Schema(description = "级别: INFO/WARNING/CRITICAL")
        /** 级别 */
        private String level;

        @Schema(description = "标题")
        /** 标题 */
        private String title;

        @Schema(description = "详细描述")
        /** 描述 */
        private String description;

        @Schema(description = "建议操作")
        /** 动作 */
        private String action;

        @Schema(description = "影响")
        /** Impact */
        private String impact;

    // === Manually generated setters ===
    public void setCategory(String category) { this.category = category; }
    public void setLevel(String level) { this.level = level; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setAction(String action) { this.action = action; }
    public void setImpact(String impact) { this.impact = impact; }
    }

    /**
    * 线程Dump信息
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "线程Dump信息")
    public static class ThreadDump implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "生成时间")
        /** 时间戳 */
        private Long timestamp;

        @Schema(description = "JVM名称")
        /** JVM名称 */
        private String jvmName;

        @Schema(description = "JVM版本")
        /** JVM版本 */
        private String jvmVersion;

        @Schema(description = "线程总数")
        /** 线程数量 */
        private Integer threadCount;

        @Schema(description = "死锁线程数")
        /** Deadlocked数量 */
        private Integer deadlockedCount;

        @Schema(description = "死锁线程ID")
        /** Deadlocked线程标识 */
        private List<Long> deadlockedThreadIds;

        @Schema(description = "线程Dump内容")
        /** Dump内容 */
        private String dumpContent;

        @Schema(description = "线程详情列表")
        /** Threads */
        private List<ThreadDumpDetail> threads;

    // === Manually generated setters ===
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
    public void setJvmName(String jvmName) { this.jvmName = jvmName; }
    public void setJvmVersion(String jvmVersion) { this.jvmVersion = jvmVersion; }
    public void setThreadCount(Integer threadCount) { this.threadCount = threadCount; }
    public void setDeadlockedCount(Integer deadlockedCount) { this.deadlockedCount = deadlockedCount; }
    public void setDeadlockedThreadIds(List<Long> deadlockedThreadIds) { this.deadlockedThreadIds = deadlockedThreadIds; }
    public void setDumpContent(String dumpContent) { this.dumpContent = dumpContent; }
    public void setThreads(List<ThreadDumpDetail> threads) { this.threads = threads; }
    }

    /**
    * 线程Dump详情
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "线程Dump详情")
    public static class ThreadDumpDetail implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "线程ID")
        /** 线程标识 */
        private Long threadId;

        @Schema(description = "线程名称")
        /** 线程名称 */
        private String threadName;

        @Schema(description = "线程状态")
        /** 状态 */
        private String state;

        @Schema(description = "是否守护线程")
        /** Daemon */
        private Boolean daemon;

        @Schema(description = "优先级")
        /** 优先级 */
        private Integer priority;

        @Schema(description = "锁信息")
        /** 锁信息 */
        private String lockInfo;

        @Schema(description = "锁拥有者")
        /** 锁owner */
        private String lockOwner;

        @Schema(description = "堆栈跟踪")
        /** 栈跟踪 */
        private List<String> stackTrace;

        @Schema(description = "持有的锁")
        /** Lockedmonitors */
        private List<String> lockedMonitors;

        @Schema(description = "持有的同步器")
        /** Lockedsynchronizers */
        private List<String> lockedSynchronizers;

    // === Manually generated setters ===
    public void setThreadId(Long threadId) { this.threadId = threadId; }
    public void setThreadName(String threadName) { this.threadName = threadName; }
    public void setState(String state) { this.state = state; }
    public void setDaemon(Boolean daemon) { this.daemon = daemon; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public void setLockInfo(String lockInfo) { this.lockInfo = lockInfo; }
    public void setLockOwner(String lockOwner) { this.lockOwner = lockOwner; }
    public void setStackTrace(List<String> stackTrace) { this.stackTrace = stackTrace; }
    public void setLockedMonitors(List<String> lockedMonitors) { this.lockedMonitors = lockedMonitors; }
    public void setLockedSynchronizers(List<String> lockedSynchronizers) { this.lockedSynchronizers = lockedSynchronizers; }
    }

    /**
    * 内存泄漏检测结果
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "内存泄漏检测结果")
    public static class MemoryLeakAnalysis implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "第一次快照时间")
        /** 首个snapshot时间 */
        private Long firstSnapshotTime;

        @Schema(description = "第二次快照时间")
        /** Secondsnapshot时间 */
        private Long secondSnapshotTime;

        @Schema(description = "间隔时间(毫秒)")
        /** 间隔MS */
        private Long intervalMs;

        @Schema(description = "第一次堆使用(bytes)")
        /** 首个heapused */
        private Long firstHeapUsed;

        @Schema(description = "第二次堆使用(bytes)")
        /** Secondheapused */
        private Long secondHeapUsed;

        @Schema(description = "堆内存增长(bytes)")
        /** Heapgrowth */
        private Long heapGrowth;

        @Schema(description = "堆内存增长率(%)")
        /** Heapgrowth百分比 */
        private Double heapGrowthPercent;

        @Schema(description = "是否可能存在泄漏")
        /** Possibleleak */
        private Boolean possibleLeak;

        @Schema(description = "泄漏风险等级: LOW/MEDIUM/HIGH")
        /** Leakrisk级别 */
        private String leakRiskLevel;

        @Schema(description = "增长的对象列表")
        /** Growingobjects */
        private List<ObjectGrowth> growingObjects;

        @Schema(description = "分析结论")
        /** Conclusion */
        private String conclusion;

        @Schema(description = "建议")
        /** Suggestions */
        private List<String> suggestions;

    // === Manually generated setters ===
    public void setFirstSnapshotTime(Long firstSnapshotTime) { this.firstSnapshotTime = firstSnapshotTime; }
    public void setSecondSnapshotTime(Long secondSnapshotTime) { this.secondSnapshotTime = secondSnapshotTime; }
    public void setIntervalMs(Long intervalMs) { this.intervalMs = intervalMs; }
    public void setFirstHeapUsed(Long firstHeapUsed) { this.firstHeapUsed = firstHeapUsed; }
    public void setSecondHeapUsed(Long secondHeapUsed) { this.secondHeapUsed = secondHeapUsed; }
    public void setHeapGrowth(Long heapGrowth) { this.heapGrowth = heapGrowth; }
    public void setHeapGrowthPercent(Double heapGrowthPercent) { this.heapGrowthPercent = heapGrowthPercent; }
    public void setPossibleLeak(Boolean possibleLeak) { this.possibleLeak = possibleLeak; }
    public void setLeakRiskLevel(String leakRiskLevel) { this.leakRiskLevel = leakRiskLevel; }
    public void setGrowingObjects(List<ObjectGrowth> growingObjects) { this.growingObjects = growingObjects; }
    public void setConclusion(String conclusion) { this.conclusion = conclusion; }
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }
    }

    /**
    * 对象增长信息
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "对象增长信息")
    public static class ObjectGrowth implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "类名")
        /** 类名称 */
        private String className;

        @Schema(description = "简化类名")
        /** Simpleclass名称 */
        private String simpleClassName;

        @Schema(description = "第一次实例数")
        /** 首个数量 */
        private Long firstCount;

        @Schema(description = "第二次实例数")
        /** Second数量 */
        private Long secondCount;

        @Schema(description = "实例数增长")
        /** 数量增长 */
        private Long countGrowth;

        @Schema(description = "增长率(%)")
        /** 增长百分比 */
        private Double growthPercent;

        @Schema(description = "第一次内存(bytes)")
        /** 首个bytes */
        private Long firstBytes;

        @Schema(description = "第二次内存(bytes)")
        /** Secondbytes */
        private Long secondBytes;

        @Schema(description = "内存增长(bytes)")
        /** Bytesgrowth */
        private Long bytesGrowth;

        @Schema(description = "内存增长率(%)")
        /** Bytesgrowth百分比 */
        private Double bytesGrowthPercent;

    // === Manually generated setters ===
    public void setClassName(String className) { this.className = className; }
    public void setSimpleClassName(String simpleClassName) { this.simpleClassName = simpleClassName; }
    public void setFirstCount(Long firstCount) { this.firstCount = firstCount; }
    public void setSecondCount(Long secondCount) { this.secondCount = secondCount; }
    public void setCountGrowth(Long countGrowth) { this.countGrowth = countGrowth; }
    public void setGrowthPercent(Double growthPercent) { this.growthPercent = growthPercent; }
    public void setFirstBytes(Long firstBytes) { this.firstBytes = firstBytes; }
    public void setSecondBytes(Long secondBytes) { this.secondBytes = secondBytes; }
    public void setBytesGrowth(Long bytesGrowth) { this.bytesGrowth = bytesGrowth; }
    public void setBytesGrowthPercent(Double bytesGrowthPercent) { this.bytesGrowthPercent = bytesGrowthPercent; }
    }

    /**
    * JVM诊断信息
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "JVM诊断信息")
    public static class JvmDiagnostic implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "诊断时间")
        /** 时间戳 */
        private Long timestamp;

        @Schema(description = "VM Flags")
        /** vmflags */
        private List<VmFlag> vmFlags;

        @Schema(description = "系统属性")
        private java.util.Map<String, String> systemProperties; // 系统属性

        @Schema(description = "环境变量")
        private java.util.Map<String, String> environmentVariables; // 环境变量

        @Schema(description = "类加载器信息")
        /** Classloaders */
        private List<ClassLoaderInfo> classLoaders;

        @Schema(description = "内存池详情")
        /** 内存池details */
        private List<MemoryPoolDetail> memoryPoolDetails;

    // === Manually generated setters ===
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
    public void setVmFlags(List<VmFlag> vmFlags) { this.vmFlags = vmFlags; }
    public void setClassLoaders(List<ClassLoaderInfo> classLoaders) { this.classLoaders = classLoaders; }
    public void setMemoryPoolDetails(List<MemoryPoolDetail> memoryPoolDetails) { this.memoryPoolDetails = memoryPoolDetails; }
    public void setSystemProperties(java.util.Map<String, String> systemProperties) { this.systemProperties = systemProperties; }
    public void setEnvironmentVariables(java.util.Map<String, String> environmentVariables) { this.environmentVariables = environmentVariables; }
    }

    /**
    * VM Flag
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "VM Flag")
    public static class VmFlag implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "参数名")
        /** 名称 */
        private String name;

        @Schema(description = "参数值")
        /** 值 */
        private String value;

        @Schema(description = "来源: COMMAND_LINE/ERGONOMIC/DEFAULT")
        /** Origin */
        private String origin;

        @Schema(description = "类型")
        /** 类型 */
        private String type;

    // === Manually generated setters ===
    public void setName(String name) { this.name = name; }
    public void setValue(String value) { this.value = value; }
    public void setOrigin(String origin) { this.origin = origin; }
    public void setType(String type) { this.type = type; }
    }

    /**
    * 类加载器信息
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "类加载器信息")
    public static class ClassLoaderInfo implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "类加载器名称")
        /** 名称 */
        private String name;

        @Schema(description = "类加载器类型")
        /** 类型 */
        private String type;

        @Schema(description = "加载的类数量")
        /** Loadedclass数量 */
        private Integer loadedClassCount;

        @Schema(description = "父加载器")
        /** 父级 */
        private String parent;

    // === Manually generated setters ===
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setLoadedClassCount(Integer loadedClassCount) { this.loadedClassCount = loadedClassCount; }
    public void setParent(String parent) { this.parent = parent; }
    }

    /**
    * 内存池详情
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "内存池详情")
    public static class MemoryPoolDetail implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "内存池名称")
        /** 名称 */
        private String name;

        @Schema(description = "类型")
        /** 类型 */
        private String type;

        @Schema(description = "当前使用")
        /** Usage */
        private MemoryInfo usage;

        @Schema(description = "峰值使用")
        /** Peakusage */
        private MemoryInfo peakUsage;

        @Schema(description = "收集后使用")
        /** Collectionusage */
        private MemoryInfo collectionUsage;

        @Schema(description = "使用率阈值")
        /** Usage阈值 */
        private Double usageThreshold;

        @Schema(description = "是否超过阈值")
        /** Usage阈值exceeded */
        private Boolean usageThresholdExceeded;

        @Schema(description = "超过阈值次数")
        /** Usage阈值数量 */
        private Long usageThresholdCount;

    // === Manually generated setters ===
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setUsage(MemoryInfo usage) { this.usage = usage; }
    public void setPeakUsage(MemoryInfo peakUsage) { this.peakUsage = peakUsage; }
    public void setCollectionUsage(MemoryInfo collectionUsage) { this.collectionUsage = collectionUsage; }
    public void setUsageThreshold(Double usageThreshold) { this.usageThreshold = usageThreshold; }
    public void setUsageThresholdExceeded(Boolean usageThresholdExceeded) { this.usageThresholdExceeded = usageThresholdExceeded; }
    public void setUsageThresholdCount(Long usageThresholdCount) { this.usageThresholdCount = usageThresholdCount; }
    }

    /**
    * 线程状态分布
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "线程状态分布")
    public static class ThreadStateDistribution implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "NEW状态线程数")
        /** 新数量 */
        private Integer newCount;

        @Schema(description = "RUNNABLE状态线程数")
        /** Runnable数量 */
        private Integer runnableCount;

        @Schema(description = "BLOCKED状态线程数")
        /** Blocked数量 */
        private Integer blockedCount;

        @Schema(description = "WAITING状态线程数")
        /** Waiting数量 */
        private Integer waitingCount;

        @Schema(description = "TIMED_WAITING状态线程数")
        /** Timedwaiting数量 */
        private Integer timedWaitingCount;

        @Schema(description = "TERMINATED状态线程数")
        /** Terminated数量 */
        private Integer terminatedCount;

        @Schema(description = "总线程数")
        /** 总数数量 */
        private Integer totalCount;

    // === Manually generated setters ===
    public void setNewCount(Integer newCount) { this.newCount = newCount; }
    public void setRunnableCount(Integer runnableCount) { this.runnableCount = runnableCount; }
    public void setBlockedCount(Integer blockedCount) { this.blockedCount = blockedCount; }
    public void setWaitingCount(Integer waitingCount) { this.waitingCount = waitingCount; }
    public void setTimedWaitingCount(Integer timedWaitingCount) { this.timedWaitingCount = timedWaitingCount; }
    public void setTerminatedCount(Integer terminatedCount) { this.terminatedCount = terminatedCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
    }

    /**
    * JIT编译统计
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "JIT编译统计")
    public static class JitCompilationStats implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "编译器名称")
        /** Compiler名称 */
        private String compilerName;

        @Schema(description = "总编译时间(毫秒)")
        /** 总数compilation时间 */
        private Long totalCompilationTime;

        @Schema(description = "是否支持编译时间监控")
        /** Compilation时间monitoringsupported */
        private Boolean compilationTimeMonitoringSupported;

        @Schema(description = "编译任务数(估计)")
        /** Compiledmethod数量 */
        private Long compiledMethodCount;

        @Schema(description = "平均编译时间(毫秒)")
        /** avgcompilation时间 */
        private Double avgCompilationTime;

    // === Manually generated setters ===
    public void setCompilerName(String compilerName) { this.compilerName = compilerName; }
    public void setTotalCompilationTime(Long totalCompilationTime) { this.totalCompilationTime = totalCompilationTime; }
    public void setCompilationTimeMonitoringSupported(Boolean compilationTimeMonitoringSupported) { this.compilationTimeMonitoringSupported = compilationTimeMonitoringSupported; }
    public void setCompiledMethodCount(Long compiledMethodCount) { this.compiledMethodCount = compiledMethodCount; }
    public void setAvgCompilationTime(Double avgCompilationTime) { this.avgCompilationTime = avgCompilationTime; }
    }

    /**
    * 代码缓存信息
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "代码缓存信息")
    public static class CodeCacheInfo implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "代码缓存名称")
        /** 名称 */
        private String name;

        @Schema(description = "已使用(字节)")
        /** Used */
        private Long used;

        @Schema(description = "已提交(字节)")
        /** Committed */
        private Long committed;

        @Schema(description = "最大容量(字节)")
        /** 最大值 */
        private Long max;

        @Schema(description = "使用率(%)")
        /** Usage百分比 */
        private Double usagePercent;

        @Schema(description = "是否接近满")
        /** Nearfull */
        private Boolean nearFull;

    // === Manually generated setters ===
    public void setName(String name) { this.name = name; }
    public void setUsed(Long used) { this.used = used; }
    public void setCommitted(Long committed) { this.committed = committed; }
    public void setMax(Long max) { this.max = max; }
    public void setUsagePercent(Double usagePercent) { this.usagePercent = usagePercent; }
    public void setNearFull(Boolean nearFull) { this.nearFull = nearFull; }
    }

    /**
    * GC统计信息
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "GC统计信息")
    public static class GcStats implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "总收集次数")
        /** 总数集合数量 */
        private Long totalCollectionCount;

        @Schema(description = "总收集时间(毫秒)")
        /** 总数集合时间 */
        private Long totalCollectionTime;

        @Schema(description = "平均收集时间(毫秒)")
        /** avgcollection时间 */
        private Double avgCollectionTime;

        @Schema(description = "最近一次GC名称")
        /** 最后GC名称 */
        private String lastGcName;

        @Schema(description = "每分钟GC次数")
        /** gcperminute */
        private Double gcPerMinute;

        @Schema(description = "GC时间占比(%)")
        /** GC时间百分比 */
        private Double gcTimePercent;

        @Schema(description = "Young GC次数")
        /** younggc数量 */
        private Long youngGcCount;

        @Schema(description = "Young GC时间(毫秒)")
        /** younggc时间 */
        private Long youngGcTime;

        @Schema(description = "Old GC次数")
        /** OLDGC数量 */
        private Long oldGcCount;

        @Schema(description = "Old GC时间(毫秒)")
        /** OLDGC时间 */
        private Long oldGcTime;

    // === Manually generated setters ===
    public void setTotalCollectionCount(Long totalCollectionCount) { this.totalCollectionCount = totalCollectionCount; }
    public void setTotalCollectionTime(Long totalCollectionTime) { this.totalCollectionTime = totalCollectionTime; }
    public void setAvgCollectionTime(Double avgCollectionTime) { this.avgCollectionTime = avgCollectionTime; }
    public void setLastGcName(String lastGcName) { this.lastGcName = lastGcName; }
    public void setGcPerMinute(Double gcPerMinute) { this.gcPerMinute = gcPerMinute; }
    public void setGcTimePercent(Double gcTimePercent) { this.gcTimePercent = gcTimePercent; }
    public void setYoungGcCount(Long youngGcCount) { this.youngGcCount = youngGcCount; }
    public void setYoungGcTime(Long youngGcTime) { this.youngGcTime = youngGcTime; }
    public void setOldGcCount(Long oldGcCount) { this.oldGcCount = oldGcCount; }
    public void setOldGcTime(Long oldGcTime) { this.oldGcTime = oldGcTime; }
    }

    /**
    * 元空间信息
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "元空间信息")
    public static class MetaspaceInfo implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "已使用(字节)")
        /** Used */
        private Long used;

        @Schema(description = "已提交(字节)")
        /** Committed */
        private Long committed;

        @Schema(description = "最大容量(字节)")
        /** 最大值 */
        private Long max;

        @Schema(description = "使用率(%)")
        /** Usage百分比 */
        private Double usagePercent;

        @Schema(description = "压缩类空间已用(字节)")
        /** Compressedclassspaceused */
        private Long compressedClassSpaceUsed;

        @Schema(description = "压缩类空间已提交(字节)")
        /** Compressedclassspacecommitted */
        private Long compressedClassSpaceCommitted;

        @Schema(description = "压缩类空间最大(字节)")
        /** Compressedclassspace最大值 */
        private Long compressedClassSpaceMax;

        @Schema(description = "压缩类空间使用率(%)")
        /** Compressedclassspaceusage百分比 */
        private Double compressedClassSpaceUsagePercent;

    // === Manually generated setters ===
    public void setUsed(Long used) { this.used = used; }
    public void setCommitted(Long committed) { this.committed = committed; }
    public void setMax(Long max) { this.max = max; }
    public void setUsagePercent(Double usagePercent) { this.usagePercent = usagePercent; }
    public void setCompressedClassSpaceUsed(Long compressedClassSpaceUsed) { this.compressedClassSpaceUsed = compressedClassSpaceUsed; }
    public void setCompressedClassSpaceCommitted(Long compressedClassSpaceCommitted) { this.compressedClassSpaceCommitted = compressedClassSpaceCommitted; }
    public void setCompressedClassSpaceMax(Long compressedClassSpaceMax) { this.compressedClassSpaceMax = compressedClassSpaceMax; }
    public void setCompressedClassSpaceUsagePercent(Double compressedClassSpaceUsagePercent) { this.compressedClassSpaceUsagePercent = compressedClassSpaceUsagePercent; }
    }

    /**
    * 直接内存信息
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "直接内存信息")
    public static class DirectMemoryInfo implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "已使用(字节)")
        /** Used */
        private Long used;

        @Schema(description = "总容量(字节)")
        /** 总数容量 */
        private Long totalCapacity;

        @Schema(description = "缓冲区数量")
        /** 数量 */
        private Long count;

        @Schema(description = "Mapped已用(字节)")
        /** Mappedused */
        private Long mappedUsed;

        @Schema(description = "Mapped容量(字节)")
        /** Mapped容量 */
        private Long mappedCapacity;

        @Schema(description = "Mapped缓冲区数量")
        /** Mapped数量 */
        private Long mappedCount;

        @Schema(description = "最大直接内存(字节,来自-XX:MaxDirectMemorySize)")
        /** 最大值directmemory */
        private Long maxDirectMemory;

        @Schema(description = "使用率(%)")
        /** Usage百分比 */
        private Double usagePercent;

    // === Manually generated setters ===
    public void setUsed(Long used) { this.used = used; }
    public void setTotalCapacity(Long totalCapacity) { this.totalCapacity = totalCapacity; }
    public void setCount(Long count) { this.count = count; }
    public void setMappedUsed(Long mappedUsed) { this.mappedUsed = mappedUsed; }
    public void setMappedCapacity(Long mappedCapacity) { this.mappedCapacity = mappedCapacity; }
    public void setMappedCount(Long mappedCount) { this.mappedCount = mappedCount; }
    public void setMaxDirectMemory(Long maxDirectMemory) { this.maxDirectMemory = maxDirectMemory; }
    public void setUsagePercent(Double usagePercent) { this.usagePercent = usagePercent; }
    }

    /**
    * 内存分配速率
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "内存分配速率")
    public static class MemoryAllocationRate implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "Eden区当前使用(字节)")
        /** Edenused */
        private Long edenUsed;

        @Schema(description = "Eden区最大(字节)")
        /** Eden最大值 */
        private Long edenMax;

        @Schema(description = "Eden区使用率(%)")
        /** Edenusage百分比 */
        private Double edenUsagePercent;

        @Schema(description = "Survivor区当前使用(字节)")
        /** Survivorused */
        private Long survivorUsed;

        @Schema(description = "Survivor区最大(字节)")
        /** Survivor最大值 */
        private Long survivorMax;

        @Schema(description = "Old区当前使用(字节)")
        /** oldused */
        private Long oldUsed;

        @Schema(description = "Old区最大(字节)")
        /** 旧最大值 */
        private Long oldMax;

        @Schema(description = "Old区使用率(%)")
        /** oldusage百分比 */
        private Double oldUsagePercent;

        @Schema(description = "估计分配速率(字节/秒)")
        /** Allocation比率persecond */
        private Long allocationRatePerSecond;

        @Schema(description = "估计晒升速率(字节/秒)")
        /** 晋升比率persecond */
        private Long promotionRatePerSecond;

    // === Manually generated setters ===
    public void setEdenUsed(Long edenUsed) { this.edenUsed = edenUsed; }
    public void setEdenMax(Long edenMax) { this.edenMax = edenMax; }
    public void setEdenUsagePercent(Double edenUsagePercent) { this.edenUsagePercent = edenUsagePercent; }
    public void setSurvivorUsed(Long survivorUsed) { this.survivorUsed = survivorUsed; }
    public void setSurvivorMax(Long survivorMax) { this.survivorMax = survivorMax; }
    public void setOldUsed(Long oldUsed) { this.oldUsed = oldUsed; }
    public void setOldMax(Long oldMax) { this.oldMax = oldMax; }
    public void setOldUsagePercent(Double oldUsagePercent) { this.oldUsagePercent = oldUsagePercent; }
    public void setAllocationRatePerSecond(Long allocationRatePerSecond) { this.allocationRatePerSecond = allocationRatePerSecond; }
    public void setPromotionRatePerSecond(Long promotionRatePerSecond) { this.promotionRatePerSecond = promotionRatePerSecond; }
    }

    /**
    * 类加载详情
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "类加载详情")
    public static class ClassLoadingDetail implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "当前加载类数")
        /** 当前加载数量 */
        private Integer currentLoadedCount;

        @Schema(description = "总加载类数")
        /** 总数加载数量 */
        private Long totalLoadedCount;

        @Schema(description = "卸载类数")
        /** Unloaded数量 */
        private Long unloadedCount;

        @Schema(description = "每分钟加载类数")
        /** 加载比率perminute */
        private Double loadRatePerMinute;

        @Schema(description = "每分钟卸载类数")
        /** Unload比率perminute */
        private Double unloadRatePerMinute;

        @Schema(description = "是否启用详细输出")
        /** Verbose */
        private Boolean verbose;

    // === Manually generated setters ===
    public void setCurrentLoadedCount(Integer currentLoadedCount) { this.currentLoadedCount = currentLoadedCount; }
    public void setTotalLoadedCount(Long totalLoadedCount) { this.totalLoadedCount = totalLoadedCount; }
    public void setUnloadedCount(Long unloadedCount) { this.unloadedCount = unloadedCount; }
    public void setLoadRatePerMinute(Double loadRatePerMinute) { this.loadRatePerMinute = loadRatePerMinute; }
    public void setUnloadRatePerMinute(Double unloadRatePerMinute) { this.unloadRatePerMinute = unloadRatePerMinute; }
    public void setVerbose(Boolean verbose) { this.verbose = verbose; }
    }

    /**
    * 安全管理器信息
    * @author CH
    * @since 4.0.0
     */
    @Getter
    @Schema(description = "安全管理器信息")
    public static class SecurityInfo implements Serializable {
        /** 串行版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "是否启用安全管理器")
        /** 安全性管理器是否启用 */
        private Boolean securityManagerEnabled;

        @Schema(description = "安全管理器类名")
        /** 安全性管理器类 */
        private String securityManagerClass;

        @Schema(description = "安全策略文件")
        /** Policy文件 */
        private String policyFile;

    // === Manually generated setters ===
    public void setSecurityManagerEnabled(Boolean securityManagerEnabled) { this.securityManagerEnabled = securityManagerEnabled; }
    public void setSecurityManagerClass(String securityManagerClass) { this.securityManagerClass = securityManagerClass; }
    public void setPolicyFile(String policyFile) { this.policyFile = policyFile; }
    }

    // === Manually generated setters ===
    public void setJvmName(String jvmName) { this.jvmName = jvmName; }
    public void setJvmVersion(String jvmVersion) { this.jvmVersion = jvmVersion; }
    public void setJvmVendor(String jvmVendor) { this.jvmVendor = jvmVendor; }
    public void setJavaVersion(String javaVersion) { this.javaVersion = javaVersion; }
    public void setJavaHome(String javaHome) { this.javaHome = javaHome; }
    public void setStartTime(Long startTime) { this.startTime = startTime; }
    public void setUptime(Long uptime) { this.uptime = uptime; }
    public void setInputArguments(List<String> inputArguments) { this.inputArguments = inputArguments; }
    public void setHeapMemory(MemoryInfo heapMemory) { this.heapMemory = heapMemory; }
    public void setNonHeapMemory(MemoryInfo nonHeapMemory) { this.nonHeapMemory = nonHeapMemory; }
    public void setMemoryPools(List<MemoryPoolInfo> memoryPools) { this.memoryPools = memoryPools; }
    public void setThreadInfo(ThreadInfo threadInfo) { this.threadInfo = threadInfo; }
    public void setTopCpuThreads(List<ThreadDetail> topCpuThreads) { this.topCpuThreads = topCpuThreads; }
    public void setClassLoadingInfo(ClassLoadingInfo classLoadingInfo) { this.classLoadingInfo = classLoadingInfo; }
    public void setGarbageCollectors(List<GarbageCollectorInfo> garbageCollectors) { this.garbageCollectors = garbageCollectors; }
    public void setOsInfo(OsInfo osInfo) { this.osInfo = osInfo; }
    public void setCpuInfo(CpuInfo cpuInfo) { this.cpuInfo = cpuInfo; }
    public void setCompilationInfo(CompilationInfo compilationInfo) { this.compilationInfo = compilationInfo; }
    public void setRuntimeInfo(RuntimeInfo runtimeInfo) { this.runtimeInfo = runtimeInfo; }
    public void setBufferPools(List<BufferPoolInfo> bufferPools) { this.bufferPools = bufferPools; }
    public void setFileDescriptorInfo(FileDescriptorInfo fileDescriptorInfo) { this.fileDescriptorInfo = fileDescriptorInfo; }
    public void setThreadStateDistribution(ThreadStateDistribution threadStateDistribution) { this.threadStateDistribution = threadStateDistribution; }
    public void setJitCompilationStats(JitCompilationStats jitCompilationStats) { this.jitCompilationStats = jitCompilationStats; }
    public void setCodeCacheInfo(CodeCacheInfo codeCacheInfo) { this.codeCacheInfo = codeCacheInfo; }
    public void setGcStats(GcStats gcStats) { this.gcStats = gcStats; }
    public void setMetaspaceInfo(MetaspaceInfo metaspaceInfo) { this.metaspaceInfo = metaspaceInfo; }
    public void setDirectMemoryInfo(DirectMemoryInfo directMemoryInfo) { this.directMemoryInfo = directMemoryInfo; }
    public void setMemoryAllocationRate(MemoryAllocationRate memoryAllocationRate) { this.memoryAllocationRate = memoryAllocationRate; }
    public void setClassLoadingDetail(ClassLoadingDetail classLoadingDetail) { this.classLoadingDetail = classLoadingDetail; }
    public void setSecurityInfo(SecurityInfo securityInfo) { this.securityInfo = securityInfo; }
}

