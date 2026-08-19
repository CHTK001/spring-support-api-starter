package com.chua.starter.system.pojo.jvm;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * JVM 信息
 *
 * @since 2024/12/18
 * @author CH
 */
@Data
@Schema(description = "JVM 信息")
public class JvmInfo implements Serializable {

    /** Serial版本UID */
    private static final long serialVersionUID = 1L;

    @Schema(description = "JVM 名称")
    /** JVM名称 */
    private String jvmName;

    @Schema(description = "JVM 版本")
    /** JVM版本 */
    private String jvmVersion;

    @Schema(description = "JVM 供应商")
    /** JVMvendor */
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
    /** 输入arguments */
    private List<String> inputArguments;

    @Schema(description = "堆内存信息")
    /** Heapmemory */
    private MemoryInfo heapMemory;

    @Schema(description = "非堆内存信息")
    /** NONheapmemory */
    private MemoryInfo nonHeapMemory;

    @Schema(description = "内存池信息列表")
    /** Memorypools */
    private List<MemoryPoolInfo> memoryPools;

    @Schema(description = "线程信息")
    /** 线程信息 */
    private ThreadInfo threadInfo;

    @Schema(description = "线程详情列表(按CPU占用排序)")
    /** 顶部CPUthreads */
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
    /** 缓冲区pools */
    private List<BufferPoolInfo> bufferPools;

    @Schema(description = "文件描述符信息")
    /** 文件descriptor信息 */
    private FileDescriptorInfo fileDescriptorInfo;

    @Schema(description = "线程状态分布")
    /** 线程状态distribution */
    private ThreadStateDistribution threadStateDistribution;

    @Schema(description = "JIT编译统计")
    /** JITcompilation统计 */
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
    /** Security信息 */
    private SecurityInfo securityInfo;

    /**
     * 内存信息
     */
    @Data
    @Schema(description = "内存信息")
    public static class MemoryInfo implements Serializable {
        /** Serial版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "初始内存(字节)")
        /** Init */
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
    }

    /**
     * 内存池信息
     */
    @Data
    @Schema(description = "内存池信息")
    public static class MemoryPoolInfo implements Serializable {
        /** Serial版本UID */
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
    }

    /**
     * 线程信息
     */
    @Data
    @Schema(description = "线程信息")
    public static class ThreadInfo implements Serializable {
        /** Serial版本UID */
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
        /** 总数started线程数量 */
        private Long totalStartedThreadCount;

        @Schema(description = "死锁线程数")
        /** Deadlocked线程数量 */
        private Integer deadlockedThreadCount;
    }

    /**
     * 线程详情
     */
    @Data
    @Schema(description = "线程详情")
    public static class ThreadDetail implements Serializable {
        /** Serial版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "线程ID")
        /** 线程ID */
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
        /** CPUusage */
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
        /** 锁ownerID */
        private Long lockOwnerId;

        @Schema(description = "锁拥有者名称")
        /** 锁owner名称 */
        private String lockOwnerName;

        @Schema(description = "堆栈跟踪")
        /** 栈跟踪 */
        private String[] stackTrace;
    }

    /**
     * CPU 热点代码信息
     */
    @Data
    @Schema(description = "CPU 热点代码信息")
    public static class CpuHotspot implements Serializable {
        /** Serial版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "类名")
        /** Class名称 */
        private String className;

        @Schema(description = "方法名")
        /** Method名称 */
        private String methodName;

        @Schema(description = "文件名")
        /** 文件名称 */
        private String fileName;

        @Schema(description = "行号")
        /** Line数字 */
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
    }

    /**
     * CPU 热点分析结果
     */
    @Data
    @Schema(description = "CPU 热点分析结果")
    public static class CpuHotspotAnalysis implements Serializable {
        /** Serial版本UID */
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
    }

    /**
     * 类级别热点
     */
    @Data
    @Schema(description = "类级别热点")
    public static class ClassHotspot implements Serializable {
        /** Serial版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "类名")
        /** Class名称 */
        private String className;

        @Schema(description = "采样命中次数")
        /** HIT数量 */
        private Integer hitCount;

        @Schema(description = "占比(%)")
        /** Percentage */
        private Double percentage;

        @Schema(description = "方法数")
        /** Method数量 */
        private Integer methodCount;
    }

    /**
     * 包级别热点
     */
    @Data
    @Schema(description = "包级别热点")
    public static class PackageHotspot implements Serializable {
        /** Serial版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "包名")
        /** Package名称 */
        private String packageName;

        @Schema(description = "采样命中次数")
        /** HIT数量 */
        private Integer hitCount;

        @Schema(description = "占比(%)")
        /** Percentage */
        private Double percentage;

        @Schema(description = "类数")
        /** Class数量 */
        private Integer classCount;
    }

    /**
     * 类加载信息
     */
    @Data
    @Schema(description = "类加载信息")
    public static class ClassLoadingInfo implements Serializable {
        /** Serial版本UID */
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
    }

    /**
     * 垃圾收集器信息
     */
    @Data
    @Schema(description = "垃圾收集器信息")
    public static class GarbageCollectorInfo implements Serializable {
        /** Serial版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "收集器名称")
        /** 名称 */
        private String name;

        @Schema(description = "收集次数")
        /** Collection数量 */
        private Long collectionCount;

        @Schema(description = "收集时间(毫秒)")
        /** Collection时间 */
        private Long collectionTime;

        @Schema(description = "内存池名称")
        /** Memory池names */
        private String[] memoryPoolNames;
    }

    /**
     * 操作系统信息
     */
    @Data
    @Schema(description = "操作系统信息")
    public static class OsInfo implements Serializable {
        /** Serial版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "操作系统名称")
        /** 名称 */
        private String name;

        @Schema(description = "操作系统版本")
        /** 版本 */
        private String version;

        @Schema(description = "操作系统架构")
        /** Arch */
        private String arch;

        @Schema(description = "可用处理器数量")
        /** 可用processors */
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
    }

    /**
     * CPU 信息
     */
    @Data
    @Schema(description = "CPU 信息")
    public static class CpuInfo implements Serializable {
        /** Serial版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "进程 CPU 使用率(%)")
        /** ProcessCPUload */
        private Double processCpuLoad;

        @Schema(description = "系统 CPU 使用率(%)")
        /** SystemCPUload */
        private Double systemCpuLoad;

        @Schema(description = "进程 CPU 时间(纳秒)")
        /** ProcessCPU时间 */
        private Long processCpuTime;
    }

    /**
     * 编译器信息
     */
    @Data
    @Schema(description = "编译器信息")
    public static class CompilationInfo implements Serializable {
        /** Serial版本UID */
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
    }

    /**
     * 运行时信息
     */
    @Data
    @Schema(description = "运行时信息")
    public static class RuntimeInfo implements Serializable {
        /** Serial版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "进程ID")
        /** PID */
        private String pid;

        @Schema(description = "启动类路径")
        /** Bootclass路径 */
        private String bootClassPath;

        @Schema(description = "类路径")
        /** Class路径 */
        private String classPath;

        @Schema(description = "库路径")
        /** Library路径 */
        private String libraryPath;

        @Schema(description = "管理规范版本")
        /** Management规范版本 */
        private String managementSpecVersion;

        @Schema(description = "规范名称")
        /** 规范名称 */
        private String specName;

        @Schema(description = "规范供应商")
        /** 规范vendor */
        private String specVendor;

        @Schema(description = "规范版本")
        /** 规范版本 */
        private String specVersion;
    }

    /**
     * 缓冲池信息
     */
    @Data
    @Schema(description = "缓冲池信息")
    public static class BufferPoolInfo implements Serializable {
        /** Serial版本UID */
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
    }

    /**
     * 文件描述符信息
     */
    @Data
    @Schema(description = "文件描述符信息")
    public static class FileDescriptorInfo implements Serializable {
        /** Serial版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "已打开的文件描述符数")
        /** Open文件descriptor数量 */
        private Long openFileDescriptorCount;

        @Schema(description = "最大文件描述符数")
        /** 最大值文件descriptor数量 */
        private Long maxFileDescriptorCount;

        @Schema(description = "使用率(%)")
        /** Usage百分比 */
        private Double usagePercent;
    }

    /**
     * 内存对象信息
     */
    @Data
    @Schema(description = "内存对象信息")
    public static class HeapObjectInfo implements Serializable {
        /** Serial版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "类名")
        /** Class名称 */
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
    }

    /**
     * 内存分析结果
     */
    @Data
    @Schema(description = "内存分析结果")
    public static class HeapAnalysis implements Serializable {
        /** Serial版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "分析时间")
        /** Analysis时间 */
        private Long analysisTime;

        @Schema(description = "堆内存使用")
        /** Heapusage */
        private MemoryInfo heapUsage;

        @Schema(description = "对象总数")
        /** 总数objects */
        private Long totalObjects;

        @Schema(description = "占用内存总量(字节)")
        /** 总数bytes */
        private Long totalBytes;

        @Schema(description = "类数量")
        /** Class数量 */
        private Integer classCount;

        @Schema(description = "对象列表(按内存排序)")
        /** 顶部BYbytes */
        private List<HeapObjectInfo> topByBytes;

        @Schema(description = "对象列表(按数量排序)")
        /** 顶部BY数量 */
        private List<HeapObjectInfo> topByCount;

        @Schema(description = "包级别内存统计")
        /** Packagememory */
        private List<PackageMemory> packageMemory;
    }

    /**
     * 包级别内存统计
     */
    @Data
    @Schema(description = "包级别内存统计")
    public static class PackageMemory implements Serializable {
        /** Serial版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "包名")
        /** Package名称 */
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
        /** Class数量 */
        private Integer classCount;
    }

    /**
     * 业务代码归因分析结果
     */
    @Data
    @Schema(description = "业务代码归因分析结果")
    public static class BusinessCodeAnalysis implements Serializable {
        /** Serial版本UID */
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
        /** CPUimpacts */
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
    }

    /**
     * 业务代码影响
     */
    @Data
    @Schema(description = "业务代码影响")
    public static class BusinessCodeImpact implements Serializable {
        /** Serial版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "业务方法")
        /** Businessmethod */
        private String businessMethod;

        @Schema(description = "类名")
        /** Class名称 */
        private String className;

        @Schema(description = "方法名")
        /** Method名称 */
        private String methodName;

        @Schema(description = "文件名")
        /** 文件名称 */
        private String fileName;

        @Schema(description = "行号")
        /** Line数字 */
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
    }

    /**
     * 调用链分析
     */
    @Data
    @Schema(description = "调用链分析")
    public static class CallChainAnalysis implements Serializable {
        /** Serial版本UID */
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
    }

    /**
     * 业务入口点
     */
    @Data
    @Schema(description = "业务入口点")
    public static class BusinessEntryPoint implements Serializable {
        /** Serial版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "入口方法")
        /** Entrymethod */
        private String entryMethod;

        @Schema(description = "类名")
        /** Class名称 */
        private String className;

        @Schema(description = "方法名")
        /** Method名称 */
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
        /** CPUimpact分数 */
        private Integer cpuImpactScore;

        @Schema(description = "描述")
        /** 描述 */
        private String description;
    }

    /**
     * JVM 优化建议
     */
    @Data
    @Schema(description = "JVM 优化建议")
    public static class JvmOptimizationAdvice implements Serializable {
        /** Serial版本UID */
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
        /** GCadvice */
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
        /** Full命令 */
        private String fullCommand;

        @Schema(description = "健康评分(0-100)")
        /** Health分数 */
        private Integer healthScore;

        @Schema(description = "诊断信息")
        /** Diagnostics */
        private List<String> diagnostics;
    }

    /**
     * JVM 参数
     */
    @Data
    @Schema(description = "JVM 参数")
    public static class JvmParameter implements Serializable {
        /** Serial版本UID */
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
        /** Reason */
        private String reason;
    }

    /**
     * GC 收集器建议
     */
    @Data
    @Schema(description = "GC 收集器建议")
    public static class GcAdvice implements Serializable {
        /** Serial版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "当前GC收集器")
        /** 当前GC */
        private String currentGc;

        @Schema(description = "推荐GC收集器")
        /** RecommendedGC */
        private String recommendedGc;

        @Schema(description = "GC参数列表")
        /** GC参数 */
        private List<JvmParameter> gcParams;

        @Schema(description = "推荐原因")
        /** Reason */
        private String reason;

        @Schema(description = "GC频率评估")
        /** GCfrequencyassessment */
        private String gcFrequencyAssessment;

        @Schema(description = "GC停顿时间评估")
        /** GCpauseassessment */
        private String gcPauseAssessment;
    }

    /**
     * 内存建议
     */
    @Data
    @Schema(description = "内存建议")
    public static class MemoryAdvice implements Serializable {
        /** Serial版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "推荐堆内存大小")
        /** Recommendedheap尺寸 */
        private String recommendedHeapSize;

        @Schema(description = "推荐初始堆大小")
        /** Recommendedinitialheap */
        private String recommendedInitialHeap;

        @Schema(description = "推荐新生代大小")
        /** RecommendedNEWGEN尺寸 */
        private String recommendedNewGenSize;

        @Schema(description = "推荐元空间大小")
        /** Recommendedmetaspace尺寸 */
        private String recommendedMetaspaceSize;

        @Schema(description = "内存参数列表")
        /** Memory参数 */
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
    }

    /**
     * 线程建议
     */
    @Data
    @Schema(description = "线程建议")
    public static class ThreadAdvice implements Serializable {
        /** Serial版本UID */
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
    }

    /**
     * 性能建议
     */
    @Data
    @Schema(description = "性能建议")
    public static class PerformanceAdvice implements Serializable {
        /** Serial版本UID */
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
    }

    /**
     * 线程Dump信息
     */
    @Data
    @Schema(description = "线程Dump信息")
    public static class ThreadDump implements Serializable {
        /** Serial版本UID */
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
        /** Deadlocked线程IDS */
        private List<Long> deadlockedThreadIds;

        @Schema(description = "线程Dump内容")
        /** Dump内容 */
        private String dumpContent;

        @Schema(description = "线程详情列表")
        /** Threads */
        private List<ThreadDumpDetail> threads;
    }

    /**
     * 线程Dump详情
     */
    @Data
    @Schema(description = "线程Dump详情")
    public static class ThreadDumpDetail implements Serializable {
        /** Serial版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "线程ID")
        /** 线程ID */
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
    }

    /**
     * 内存泄漏检测结果
     */
    @Data
    @Schema(description = "内存泄漏检测结果")
    public static class MemoryLeakAnalysis implements Serializable {
        /** Serial版本UID */
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
    }

    /**
     * 对象增长信息
     */
    @Data
    @Schema(description = "对象增长信息")
    public static class ObjectGrowth implements Serializable {
        /** Serial版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "类名")
        /** Class名称 */
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
        /** 数量growth */
        private Long countGrowth;

        @Schema(description = "增长率(%)")
        /** Growth百分比 */
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
    }

    /**
     * JVM诊断信息
     */
    @Data
    @Schema(description = "JVM诊断信息")
    public static class JvmDiagnostic implements Serializable {
        /** Serial版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "诊断时间")
        /** 时间戳 */
        private Long timestamp;

        @Schema(description = "VM Flags")
        /** VMflags */
        private List<VmFlag> vmFlags;

        @Schema(description = "系统属性")
        private java.util.Map<String, String> systemProperties;

        @Schema(description = "环境变量")
        private java.util.Map<String, String> environmentVariables;

        @Schema(description = "类加载器信息")
        /** Classloaders */
        private List<ClassLoaderInfo> classLoaders;

        @Schema(description = "内存池详情")
        /** Memory池details */
        private List<MemoryPoolDetail> memoryPoolDetails;
    }

    /**
     * VM Flag
     */
    @Data
    @Schema(description = "VM Flag")
    public static class VmFlag implements Serializable {
        /** Serial版本UID */
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
    }

    /**
     * 类加载器信息
     */
    @Data
    @Schema(description = "类加载器信息")
    public static class ClassLoaderInfo implements Serializable {
        /** Serial版本UID */
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
    }

    /**
     * 内存池详情
     */
    @Data
    @Schema(description = "内存池详情")
    public static class MemoryPoolDetail implements Serializable {
        /** Serial版本UID */
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
    }

    /**
     * 线程状态分布
     */
    @Data
    @Schema(description = "线程状态分布")
    public static class ThreadStateDistribution implements Serializable {
        /** Serial版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "NEW状态线程数")
        /** NEW数量 */
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
    }

    /**
     * JIT编译统计
     */
    @Data
    @Schema(description = "JIT编译统计")
    public static class JitCompilationStats implements Serializable {
        /** Serial版本UID */
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
        /** AVGcompilation时间 */
        private Double avgCompilationTime;
    }

    /**
     * 代码缓存信息
     */
    @Data
    @Schema(description = "代码缓存信息")
    public static class CodeCacheInfo implements Serializable {
        /** Serial版本UID */
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
    }

    /**
     * GC统计信息
     */
    @Data
    @Schema(description = "GC统计信息")
    public static class GcStats implements Serializable {
        /** Serial版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "总收集次数")
        /** 总数collection数量 */
        private Long totalCollectionCount;

        @Schema(description = "总收集时间(毫秒)")
        /** 总数collection时间 */
        private Long totalCollectionTime;

        @Schema(description = "平均收集时间(毫秒)")
        /** AVGcollection时间 */
        private Double avgCollectionTime;

        @Schema(description = "最近一次GC名称")
        /** 最后GC名称 */
        private String lastGcName;

        @Schema(description = "每分钟GC次数")
        /** GCPERminute */
        private Double gcPerMinute;

        @Schema(description = "GC时间占比(%)")
        /** GC时间百分比 */
        private Double gcTimePercent;

        @Schema(description = "Young GC次数")
        /** YoungGC数量 */
        private Long youngGcCount;

        @Schema(description = "Young GC时间(毫秒)")
        /** YoungGC时间 */
        private Long youngGcTime;

        @Schema(description = "Old GC次数")
        /** OLDGC数量 */
        private Long oldGcCount;

        @Schema(description = "Old GC时间(毫秒)")
        /** OLDGC时间 */
        private Long oldGcTime;
    }

    /**
     * 元空间信息
     */
    @Data
    @Schema(description = "元空间信息")
    public static class MetaspaceInfo implements Serializable {
        /** Serial版本UID */
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
    }

    /**
     * 直接内存信息
     */
    @Data
    @Schema(description = "直接内存信息")
    public static class DirectMemoryInfo implements Serializable {
        /** Serial版本UID */
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
    }

    /**
     * 内存分配速率
     */
    @Data
    @Schema(description = "内存分配速率")
    public static class MemoryAllocationRate implements Serializable {
        /** Serial版本UID */
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
        /** OLDused */
        private Long oldUsed;

        @Schema(description = "Old区最大(字节)")
        /** OLD最大值 */
        private Long oldMax;

        @Schema(description = "Old区使用率(%)")
        /** OLDusage百分比 */
        private Double oldUsagePercent;

        @Schema(description = "估计分配速率(字节/秒)")
        /** Allocation比率PERsecond */
        private Long allocationRatePerSecond;

        @Schema(description = "估计晒升速率(字节/秒)")
        /** Promotion比率PERsecond */
        private Long promotionRatePerSecond;
    }

    /**
     * 类加载详情
     */
    @Data
    @Schema(description = "类加载详情")
    public static class ClassLoadingDetail implements Serializable {
        /** Serial版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "当前加载类数")
        /** 当前loaded数量 */
        private Integer currentLoadedCount;

        @Schema(description = "总加载类数")
        /** 总数loaded数量 */
        private Long totalLoadedCount;

        @Schema(description = "卸载类数")
        /** Unloaded数量 */
        private Long unloadedCount;

        @Schema(description = "每分钟加载类数")
        /** Load比率PERminute */
        private Double loadRatePerMinute;

        @Schema(description = "每分钟卸载类数")
        /** Unload比率PERminute */
        private Double unloadRatePerMinute;

        @Schema(description = "是否启用详细输出")
        /** Verbose */
        private Boolean verbose;
    }

    /**
     * 安全管理器信息
     */
    @Data
    @Schema(description = "安全管理器信息")
    public static class SecurityInfo implements Serializable {
        /** Serial版本UID */
        private static final long serialVersionUID = 1L;

        @Schema(description = "是否启用安全管理器")
        /** Security管理器是否启用 */
        private Boolean securityManagerEnabled;

        @Schema(description = "安全管理器类名")
        /** Security管理器class */
        private String securityManagerClass;

        @Schema(description = "安全策略文件")
        /** Policy文件 */
        private String policyFile;
    }
}
