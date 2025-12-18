package com.chua.starter.system.pojo.jvm;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * JVM 信息
 *
 * @author CH
 * @since 2024/12/18
 */
@Data
@Schema(description = "JVM 信息")
public class JvmInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "JVM 名称")
    private String jvmName;

    @Schema(description = "JVM 版本")
    private String jvmVersion;

    @Schema(description = "JVM 供应商")
    private String jvmVendor;

    @Schema(description = "Java 版本")
    private String javaVersion;

    @Schema(description = "Java Home")
    private String javaHome;

    @Schema(description = "JVM 启动时间")
    private Long startTime;

    @Schema(description = "JVM 运行时长(毫秒)")
    private Long uptime;

    @Schema(description = "JVM 输入参数")
    private List<String> inputArguments;

    @Schema(description = "堆内存信息")
    private MemoryInfo heapMemory;

    @Schema(description = "非堆内存信息")
    private MemoryInfo nonHeapMemory;

    @Schema(description = "内存池信息列表")
    private List<MemoryPoolInfo> memoryPools;

    @Schema(description = "线程信息")
    private ThreadInfo threadInfo;

    @Schema(description = "线程详情列表(按CPU占用排序)")
    private List<ThreadDetail> topCpuThreads;

    @Schema(description = "类加载信息")
    private ClassLoadingInfo classLoadingInfo;

    @Schema(description = "GC 信息列表")
    private List<GarbageCollectorInfo> garbageCollectors;

    @Schema(description = "操作系统信息")
    private OsInfo osInfo;

    @Schema(description = "CPU 信息")
    private CpuInfo cpuInfo;

    @Schema(description = "编译器信息")
    private CompilationInfo compilationInfo;

    @Schema(description = "运行时信息")
    private RuntimeInfo runtimeInfo;

    @Schema(description = "缓冲池信息")
    private List<BufferPoolInfo> bufferPools;

    @Schema(description = "文件描述符信息")
    private FileDescriptorInfo fileDescriptorInfo;

    @Schema(description = "线程状态分布")
    private ThreadStateDistribution threadStateDistribution;

    @Schema(description = "JIT编译统计")
    private JitCompilationStats jitCompilationStats;

    @Schema(description = "代码缓存信息")
    private CodeCacheInfo codeCacheInfo;

    @Schema(description = "GC统计信息")
    private GcStats gcStats;

    @Schema(description = "元空间信息")
    private MetaspaceInfo metaspaceInfo;

    @Schema(description = "直接内存信息")
    private DirectMemoryInfo directMemoryInfo;

    /**
     * 内存信息
     */
    @Data
    @Schema(description = "内存信息")
    public static class MemoryInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "初始内存(字节)")
        private Long init;

        @Schema(description = "已使用内存(字节)")
        private Long used;

        @Schema(description = "已提交内存(字节)")
        private Long committed;

        @Schema(description = "最大内存(字节)")
        private Long max;

        @Schema(description = "使用率(%)")
        private Double usagePercent;
    }

    /**
     * 内存池信息
     */
    @Data
    @Schema(description = "内存池信息")
    public static class MemoryPoolInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "内存池名称")
        private String name;

        @Schema(description = "内存池类型")
        private String type;

        @Schema(description = "内存使用信息")
        private MemoryInfo usage;

        @Schema(description = "内存池峰值使用")
        private MemoryInfo peakUsage;
    }

    /**
     * 线程信息
     */
    @Data
    @Schema(description = "线程信息")
    public static class ThreadInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "当前线程数")
        private Integer threadCount;

        @Schema(description = "峰值线程数")
        private Integer peakThreadCount;

        @Schema(description = "守护线程数")
        private Integer daemonThreadCount;

        @Schema(description = "总启动线程数")
        private Long totalStartedThreadCount;

        @Schema(description = "死锁线程数")
        private Integer deadlockedThreadCount;
    }

    /**
     * 线程详情
     */
    @Data
    @Schema(description = "线程详情")
    public static class ThreadDetail implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "线程ID")
        private Long threadId;

        @Schema(description = "线程名称")
        private String threadName;

        @Schema(description = "线程状态")
        private String state;

        @Schema(description = "CPU 时间(纳秒)")
        private Long cpuTime;

        @Schema(description = "用户时间(纳秒)")
        private Long userTime;

        @Schema(description = "CPU 使用率(%)")
        private Double cpuUsage;

        @Schema(description = "是否守护线程")
        private Boolean daemon;

        @Schema(description = "线程优先级")
        private Integer priority;

        @Schema(description = "阻塞次数")
        private Long blockedCount;

        @Schema(description = "阻塞时间(毫秒)")
        private Long blockedTime;

        @Schema(description = "等待次数")
        private Long waitedCount;

        @Schema(description = "等待时间(毫秒)")
        private Long waitedTime;

        @Schema(description = "锁名称")
        private String lockName;

        @Schema(description = "锁拥有者ID")
        private Long lockOwnerId;

        @Schema(description = "锁拥有者名称")
        private String lockOwnerName;

        @Schema(description = "堆栈跟踪")
        private String[] stackTrace;
    }

    /**
     * CPU 热点代码信息
     */
    @Data
    @Schema(description = "CPU 热点代码信息")
    public static class CpuHotspot implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "类名")
        private String className;

        @Schema(description = "方法名")
        private String methodName;

        @Schema(description = "文件名")
        private String fileName;

        @Schema(description = "行号")
        private Integer lineNumber;

        @Schema(description = "采样命中次数")
        private Integer hitCount;

        @Schema(description = "占比(%)")
        private Double percentage;

        @Schema(description = "完整方法签名")
        private String fullMethod;

        @Schema(description = "调用者列表")
        private List<String> callers;
    }

    /**
     * CPU 热点分析结果
     */
    @Data
    @Schema(description = "CPU 热点分析结果")
    public static class CpuHotspotAnalysis implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "采样次数")
        private Integer sampleCount;

        @Schema(description = "采样时长(毫秒)")
        private Long sampleDuration;

        @Schema(description = "采样间隔(毫秒)")
        private Integer sampleInterval;

        @Schema(description = "分析的线程数")
        private Integer threadCount;

        @Schema(description = "热点方法列表")
        private List<CpuHotspot> hotspots;

        @Schema(description = "热点类统计")
        private List<ClassHotspot> classHotspots;

        @Schema(description = "热点包统计")
        private List<PackageHotspot> packageHotspots;
    }

    /**
     * 类级别热点
     */
    @Data
    @Schema(description = "类级别热点")
    public static class ClassHotspot implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "类名")
        private String className;

        @Schema(description = "采样命中次数")
        private Integer hitCount;

        @Schema(description = "占比(%)")
        private Double percentage;

        @Schema(description = "方法数")
        private Integer methodCount;
    }

    /**
     * 包级别热点
     */
    @Data
    @Schema(description = "包级别热点")
    public static class PackageHotspot implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "包名")
        private String packageName;

        @Schema(description = "采样命中次数")
        private Integer hitCount;

        @Schema(description = "占比(%)")
        private Double percentage;

        @Schema(description = "类数")
        private Integer classCount;
    }

    /**
     * 类加载信息
     */
    @Data
    @Schema(description = "类加载信息")
    public static class ClassLoadingInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "已加载类数量")
        private Integer loadedClassCount;

        @Schema(description = "总加载类数量")
        private Long totalLoadedClassCount;

        @Schema(description = "已卸载类数量")
        private Long unloadedClassCount;
    }

    /**
     * 垃圾收集器信息
     */
    @Data
    @Schema(description = "垃圾收集器信息")
    public static class GarbageCollectorInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "收集器名称")
        private String name;

        @Schema(description = "收集次数")
        private Long collectionCount;

        @Schema(description = "收集时间(毫秒)")
        private Long collectionTime;

        @Schema(description = "内存池名称")
        private String[] memoryPoolNames;
    }

    /**
     * 操作系统信息
     */
    @Data
    @Schema(description = "操作系统信息")
    public static class OsInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "操作系统名称")
        private String name;

        @Schema(description = "操作系统版本")
        private String version;

        @Schema(description = "操作系统架构")
        private String arch;

        @Schema(description = "可用处理器数量")
        private Integer availableProcessors;

        @Schema(description = "系统负载平均值")
        private Double systemLoadAverage;

        @Schema(description = "总物理内存(字节)")
        private Long totalPhysicalMemory;

        @Schema(description = "空闲物理内存(字节)")
        private Long freePhysicalMemory;

        @Schema(description = "总交换空间(字节)")
        private Long totalSwapSpace;

        @Schema(description = "空闲交换空间(字节)")
        private Long freeSwapSpace;
    }

    /**
     * CPU 信息
     */
    @Data
    @Schema(description = "CPU 信息")
    public static class CpuInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "进程 CPU 使用率(%)")
        private Double processCpuLoad;

        @Schema(description = "系统 CPU 使用率(%)")
        private Double systemCpuLoad;

        @Schema(description = "进程 CPU 时间(纳秒)")
        private Long processCpuTime;
    }

    /**
     * 编译器信息
     */
    @Data
    @Schema(description = "编译器信息")
    public static class CompilationInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "编译器名称")
        private String name;

        @Schema(description = "总编译时间(毫秒)")
        private Long totalCompilationTime;

        @Schema(description = "是否支持编译时间监控")
        private Boolean compilationTimeMonitoringSupported;
    }

    /**
     * 运行时信息
     */
    @Data
    @Schema(description = "运行时信息")
    public static class RuntimeInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "进程ID")
        private String pid;

        @Schema(description = "启动类路径")
        private String bootClassPath;

        @Schema(description = "类路径")
        private String classPath;

        @Schema(description = "库路径")
        private String libraryPath;

        @Schema(description = "管理规范版本")
        private String managementSpecVersion;

        @Schema(description = "规范名称")
        private String specName;

        @Schema(description = "规范供应商")
        private String specVendor;

        @Schema(description = "规范版本")
        private String specVersion;
    }

    /**
     * 缓冲池信息
     */
    @Data
    @Schema(description = "缓冲池信息")
    public static class BufferPoolInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "缓冲池名称")
        private String name;

        @Schema(description = "缓冲区数量")
        private Long count;

        @Schema(description = "已使用内存(字节)")
        private Long memoryUsed;

        @Schema(description = "总容量(字节)")
        private Long totalCapacity;
    }

    /**
     * 文件描述符信息
     */
    @Data
    @Schema(description = "文件描述符信息")
    public static class FileDescriptorInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "已打开的文件描述符数")
        private Long openFileDescriptorCount;

        @Schema(description = "最大文件描述符数")
        private Long maxFileDescriptorCount;

        @Schema(description = "使用率(%)")
        private Double usagePercent;
    }

    /**
     * 内存对象信息
     */
    @Data
    @Schema(description = "内存对象信息")
    public static class HeapObjectInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "类名")
        private String className;

        @Schema(description = "实例数量")
        private Long instanceCount;

        @Schema(description = "占用内存(字节)")
        private Long bytes;

        @Schema(description = "内存占比(%)")
        private Double percentage;

        @Schema(description = "简化类名")
        private String simpleClassName;
    }

    /**
     * 内存分析结果
     */
    @Data
    @Schema(description = "内存分析结果")
    public static class HeapAnalysis implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "分析时间")
        private Long analysisTime;

        @Schema(description = "堆内存使用")
        private MemoryInfo heapUsage;

        @Schema(description = "对象总数")
        private Long totalObjects;

        @Schema(description = "占用内存总量(字节)")
        private Long totalBytes;

        @Schema(description = "类数量")
        private Integer classCount;

        @Schema(description = "对象列表(按内存排序)")
        private List<HeapObjectInfo> topByBytes;

        @Schema(description = "对象列表(按数量排序)")
        private List<HeapObjectInfo> topByCount;

        @Schema(description = "包级别内存统计")
        private List<PackageMemory> packageMemory;
    }

    /**
     * 包级别内存统计
     */
    @Data
    @Schema(description = "包级别内存统计")
    public static class PackageMemory implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "包名")
        private String packageName;

        @Schema(description = "占用内存(字节)")
        private Long bytes;

        @Schema(description = "内存占比(%)")
        private Double percentage;

        @Schema(description = "实例数量")
        private Long instanceCount;

        @Schema(description = "类数量")
        private Integer classCount;
    }

    /**
     * 业务代码归因分析结果
     */
    @Data
    @Schema(description = "业务代码归因分析结果")
    public static class BusinessCodeAnalysis implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "分析时间")
        private Long analysisTime;

        @Schema(description = "采样次数")
        private Integer sampleCount;

        @Schema(description = "采样间隔(毫秒)")
        private Integer sampleInterval;

        @Schema(description = "业务包前缀")
        private String businessPackage;

        @Schema(description = "CPU相关业务代码")
        private List<BusinessCodeImpact> cpuImpacts;

        @Schema(description = "内存相关业务代码")
        private List<BusinessCodeImpact> memoryImpacts;

        @Schema(description = "调用链分析")
        private List<CallChainAnalysis> callChains;

        @Schema(description = "业务入口点")
        private List<BusinessEntryPoint> entryPoints;
    }

    /**
     * 业务代码影响
     */
    @Data
    @Schema(description = "业务代码影响")
    public static class BusinessCodeImpact implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "业务方法")
        private String businessMethod;

        @Schema(description = "类名")
        private String className;

        @Schema(description = "方法名")
        private String methodName;

        @Schema(description = "文件名")
        private String fileName;

        @Schema(description = "行号")
        private Integer lineNumber;

        @Schema(description = "影响分数")
        private Integer impactScore;

        @Schema(description = "影响占比(%)")
        private Double impactPercentage;

        @Schema(description = "关联热点")
        private List<String> relatedHotspots;

        @Schema(description = "调用次数")
        private Integer callCount;

        @Schema(description = "影响类型: CPU/MEMORY/BOTH")
        private String impactType;

        @Schema(description = "描述")
        private String description;
    }

    /**
     * 调用链分析
     */
    @Data
    @Schema(description = "调用链分析")
    public static class CallChainAnalysis implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "热点方法")
        private String hotspotMethod;

        @Schema(description = "业务入口")
        private String businessEntry;

        @Schema(description = "调用链 (从业务入口到热点)")
        private List<String> callChain;

        @Schema(description = "出现次数")
        private Integer occurrenceCount;

        @Schema(description = "占比(%)")
        private Double percentage;
    }

    /**
     * 业务入口点
     */
    @Data
    @Schema(description = "业务入口点")
    public static class BusinessEntryPoint implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "入口方法")
        private String entryMethod;

        @Schema(description = "类名")
        private String className;

        @Schema(description = "方法名")
        private String methodName;

        @Schema(description = "入口类型: Controller/Service/Scheduler/MessageListener等")
        private String entryType;

        @Schema(description = "触发次数")
        private Integer triggerCount;

        @Schema(description = "关联热点数量")
        private Integer relatedHotspotCount;

        @Schema(description = "CPU影响分数")
        private Integer cpuImpactScore;

        @Schema(description = "描述")
        private String description;
    }

    /**
     * JVM 优化建议
     */
    @Data
    @Schema(description = "JVM 优化建议")
    public static class JvmOptimizationAdvice implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "分析时间")
        private Long analysisTime;

        @Schema(description = "当前JVM参数")
        private List<String> currentArgs;

        @Schema(description = "推荐JVM参数")
        private List<JvmParameter> recommendedParams;

        @Schema(description = "GC收集器建议")
        private GcAdvice gcAdvice;

        @Schema(description = "内存建议")
        private MemoryAdvice memoryAdvice;

        @Schema(description = "线程建议")
        private ThreadAdvice threadAdvice;

        @Schema(description = "性能建议")
        private List<PerformanceAdvice> performanceAdvices;

        @Schema(description = "完整启动命令")
        private String fullCommand;

        @Schema(description = "健康评分(0-100)")
        private Integer healthScore;

        @Schema(description = "诊断信息")
        private List<String> diagnostics;
    }

    /**
     * JVM 参数
     */
    @Data
    @Schema(description = "JVM 参数")
    public static class JvmParameter implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "参数名")
        private String name;

        @Schema(description = "当前值")
        private String currentValue;

        @Schema(description = "推荐值")
        private String recommendedValue;

        @Schema(description = "参数类型: MEMORY/GC/PERFORMANCE/DEBUG")
        private String type;

        @Schema(description = "优先级: HIGH/MEDIUM/LOW")
        private String priority;

        @Schema(description = "说明")
        private String description;

        @Schema(description = "推荐原因")
        private String reason;
    }

    /**
     * GC 收集器建议
     */
    @Data
    @Schema(description = "GC 收集器建议")
    public static class GcAdvice implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "当前GC收集器")
        private String currentGc;

        @Schema(description = "推荐GC收集器")
        private String recommendedGc;

        @Schema(description = "GC参数列表")
        private List<JvmParameter> gcParams;

        @Schema(description = "推荐原因")
        private String reason;

        @Schema(description = "GC频率评估")
        private String gcFrequencyAssessment;

        @Schema(description = "GC停顿时间评估")
        private String gcPauseAssessment;
    }

    /**
     * 内存建议
     */
    @Data
    @Schema(description = "内存建议")
    public static class MemoryAdvice implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "推荐堆内存大小")
        private String recommendedHeapSize;

        @Schema(description = "推荐初始堆大小")
        private String recommendedInitialHeap;

        @Schema(description = "推荐新生代大小")
        private String recommendedNewGenSize;

        @Schema(description = "推荐元空间大小")
        private String recommendedMetaspaceSize;

        @Schema(description = "内存参数列表")
        private List<JvmParameter> memoryParams;

        @Schema(description = "当前堆使用率")
        private Double currentHeapUsage;

        @Schema(description = "内存压力评估")
        private String memoryPressureAssessment;

        @Schema(description = "建议说明")
        private String advice;
    }

    /**
     * 线程建议
     */
    @Data
    @Schema(description = "线程建议")
    public static class ThreadAdvice implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "推荐线程栈大小")
        private String recommendedStackSize;

        @Schema(description = "当前线程数")
        private Integer currentThreadCount;

        @Schema(description = "线程数评估")
        private String threadCountAssessment;

        @Schema(description = "线程参数列表")
        private List<JvmParameter> threadParams;

        @Schema(description = "建议说明")
        private String advice;
    }

    /**
     * 性能建议
     */
    @Data
    @Schema(description = "性能建议")
    public static class PerformanceAdvice implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "类别: GC/MEMORY/THREAD/COMPILER/OTHER")
        private String category;

        @Schema(description = "级别: INFO/WARNING/CRITICAL")
        private String level;

        @Schema(description = "标题")
        private String title;

        @Schema(description = "详细描述")
        private String description;

        @Schema(description = "建议操作")
        private String action;

        @Schema(description = "影响")
        private String impact;
    }

    /**
     * 线程Dump信息
     */
    @Data
    @Schema(description = "线程Dump信息")
    public static class ThreadDump implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "生成时间")
        private Long timestamp;

        @Schema(description = "JVM名称")
        private String jvmName;

        @Schema(description = "JVM版本")
        private String jvmVersion;

        @Schema(description = "线程总数")
        private Integer threadCount;

        @Schema(description = "死锁线程数")
        private Integer deadlockedCount;

        @Schema(description = "死锁线程ID")
        private List<Long> deadlockedThreadIds;

        @Schema(description = "线程Dump内容")
        private String dumpContent;

        @Schema(description = "线程详情列表")
        private List<ThreadDumpDetail> threads;
    }

    /**
     * 线程Dump详情
     */
    @Data
    @Schema(description = "线程Dump详情")
    public static class ThreadDumpDetail implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "线程ID")
        private Long threadId;

        @Schema(description = "线程名称")
        private String threadName;

        @Schema(description = "线程状态")
        private String state;

        @Schema(description = "是否守护线程")
        private Boolean daemon;

        @Schema(description = "优先级")
        private Integer priority;

        @Schema(description = "锁信息")
        private String lockInfo;

        @Schema(description = "锁拥有者")
        private String lockOwner;

        @Schema(description = "堆栈跟踪")
        private List<String> stackTrace;

        @Schema(description = "持有的锁")
        private List<String> lockedMonitors;

        @Schema(description = "持有的同步器")
        private List<String> lockedSynchronizers;
    }

    /**
     * 内存泄漏检测结果
     */
    @Data
    @Schema(description = "内存泄漏检测结果")
    public static class MemoryLeakAnalysis implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "第一次快照时间")
        private Long firstSnapshotTime;

        @Schema(description = "第二次快照时间")
        private Long secondSnapshotTime;

        @Schema(description = "间隔时间(毫秒)")
        private Long intervalMs;

        @Schema(description = "第一次堆使用(bytes)")
        private Long firstHeapUsed;

        @Schema(description = "第二次堆使用(bytes)")
        private Long secondHeapUsed;

        @Schema(description = "堆内存增长(bytes)")
        private Long heapGrowth;

        @Schema(description = "堆内存增长率(%)")
        private Double heapGrowthPercent;

        @Schema(description = "是否可能存在泄漏")
        private Boolean possibleLeak;

        @Schema(description = "泄漏风险等级: LOW/MEDIUM/HIGH")
        private String leakRiskLevel;

        @Schema(description = "增长的对象列表")
        private List<ObjectGrowth> growingObjects;

        @Schema(description = "分析结论")
        private String conclusion;

        @Schema(description = "建议")
        private List<String> suggestions;
    }

    /**
     * 对象增长信息
     */
    @Data
    @Schema(description = "对象增长信息")
    public static class ObjectGrowth implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "类名")
        private String className;

        @Schema(description = "简化类名")
        private String simpleClassName;

        @Schema(description = "第一次实例数")
        private Long firstCount;

        @Schema(description = "第二次实例数")
        private Long secondCount;

        @Schema(description = "实例数增长")
        private Long countGrowth;

        @Schema(description = "增长率(%)")
        private Double growthPercent;

        @Schema(description = "第一次内存(bytes)")
        private Long firstBytes;

        @Schema(description = "第二次内存(bytes)")
        private Long secondBytes;

        @Schema(description = "内存增长(bytes)")
        private Long bytesGrowth;

        @Schema(description = "内存增长率(%)")
        private Double bytesGrowthPercent;
    }

    /**
     * JVM诊断信息
     */
    @Data
    @Schema(description = "JVM诊断信息")
    public static class JvmDiagnostic implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "诊断时间")
        private Long timestamp;

        @Schema(description = "VM Flags")
        private List<VmFlag> vmFlags;

        @Schema(description = "系统属性")
        private java.util.Map<String, String> systemProperties;

        @Schema(description = "环境变量")
        private java.util.Map<String, String> environmentVariables;

        @Schema(description = "类加载器信息")
        private List<ClassLoaderInfo> classLoaders;

        @Schema(description = "内存池详情")
        private List<MemoryPoolDetail> memoryPoolDetails;
    }

    /**
     * VM Flag
     */
    @Data
    @Schema(description = "VM Flag")
    public static class VmFlag implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "参数名")
        private String name;

        @Schema(description = "参数值")
        private String value;

        @Schema(description = "来源: COMMAND_LINE/ERGONOMIC/DEFAULT")
        private String origin;

        @Schema(description = "类型")
        private String type;
    }

    /**
     * 类加载器信息
     */
    @Data
    @Schema(description = "类加载器信息")
    public static class ClassLoaderInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "类加载器名称")
        private String name;

        @Schema(description = "类加载器类型")
        private String type;

        @Schema(description = "加载的类数量")
        private Integer loadedClassCount;

        @Schema(description = "父加载器")
        private String parent;
    }

    /**
     * 内存池详情
     */
    @Data
    @Schema(description = "内存池详情")
    public static class MemoryPoolDetail implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "内存池名称")
        private String name;

        @Schema(description = "类型")
        private String type;

        @Schema(description = "当前使用")
        private MemoryInfo usage;

        @Schema(description = "峰值使用")
        private MemoryInfo peakUsage;

        @Schema(description = "收集后使用")
        private MemoryInfo collectionUsage;

        @Schema(description = "使用率阈值")
        private Double usageThreshold;

        @Schema(description = "是否超过阈值")
        private Boolean usageThresholdExceeded;

        @Schema(description = "超过阈值次数")
        private Long usageThresholdCount;
    }

    /**
     * 线程状态分布
     */
    @Data
    @Schema(description = "线程状态分布")
    public static class ThreadStateDistribution implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "NEW状态线程数")
        private Integer newCount;

        @Schema(description = "RUNNABLE状态线程数")
        private Integer runnableCount;

        @Schema(description = "BLOCKED状态线程数")
        private Integer blockedCount;

        @Schema(description = "WAITING状态线程数")
        private Integer waitingCount;

        @Schema(description = "TIMED_WAITING状态线程数")
        private Integer timedWaitingCount;

        @Schema(description = "TERMINATED状态线程数")
        private Integer terminatedCount;

        @Schema(description = "总线程数")
        private Integer totalCount;
    }

    /**
     * JIT编译统计
     */
    @Data
    @Schema(description = "JIT编译统计")
    public static class JitCompilationStats implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "编译器名称")
        private String compilerName;

        @Schema(description = "总编译时间(毫秒)")
        private Long totalCompilationTime;

        @Schema(description = "是否支持编译时间监控")
        private Boolean compilationTimeMonitoringSupported;

        @Schema(description = "编译任务数(估计)")
        private Long compiledMethodCount;

        @Schema(description = "平均编译时间(毫秒)")
        private Double avgCompilationTime;
    }

    /**
     * 代码缓存信息
     */
    @Data
    @Schema(description = "代码缓存信息")
    public static class CodeCacheInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "代码缓存名称")
        private String name;

        @Schema(description = "已使用(字节)")
        private Long used;

        @Schema(description = "已提交(字节)")
        private Long committed;

        @Schema(description = "最大容量(字节)")
        private Long max;

        @Schema(description = "使用率(%)")
        private Double usagePercent;

        @Schema(description = "是否接近满")
        private Boolean nearFull;
    }

    /**
     * GC统计信息
     */
    @Data
    @Schema(description = "GC统计信息")
    public static class GcStats implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "总收集次数")
        private Long totalCollectionCount;

        @Schema(description = "总收集时间(毫秒)")
        private Long totalCollectionTime;

        @Schema(description = "平均收集时间(毫秒)")
        private Double avgCollectionTime;

        @Schema(description = "最近一次GC名称")
        private String lastGcName;

        @Schema(description = "每分钟GC次数")
        private Double gcPerMinute;

        @Schema(description = "GC时间占比(%)")
        private Double gcTimePercent;

        @Schema(description = "Young GC次数")
        private Long youngGcCount;

        @Schema(description = "Young GC时间(毫秒)")
        private Long youngGcTime;

        @Schema(description = "Old GC次数")
        private Long oldGcCount;

        @Schema(description = "Old GC时间(毫秒)")
        private Long oldGcTime;
    }

    /**
     * 元空间信息
     */
    @Data
    @Schema(description = "元空间信息")
    public static class MetaspaceInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "已使用(字节)")
        private Long used;

        @Schema(description = "已提交(字节)")
        private Long committed;

        @Schema(description = "最大容量(字节)")
        private Long max;

        @Schema(description = "使用率(%)")
        private Double usagePercent;

        @Schema(description = "压缩类空间已用(字节)")
        private Long compressedClassSpaceUsed;

        @Schema(description = "压缩类空间已提交(字节)")
        private Long compressedClassSpaceCommitted;

        @Schema(description = "压缩类空间最大(字节)")
        private Long compressedClassSpaceMax;

        @Schema(description = "压缩类空间使用率(%)")
        private Double compressedClassSpaceUsagePercent;
    }

    /**
     * 直接内存信息
     */
    @Data
    @Schema(description = "直接内存信息")
    public static class DirectMemoryInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "已使用(字节)")
        private Long used;

        @Schema(description = "总容量(字节)")
        private Long totalCapacity;

        @Schema(description = "缓冲区数量")
        private Long count;

        @Schema(description = "Mapped已用(字节)")
        private Long mappedUsed;

        @Schema(description = "Mapped容量(字节)")
        private Long mappedCapacity;

        @Schema(description = "Mapped缓冲区数量")
        private Long mappedCount;

        @Schema(description = "最大直接内存(字节,来自-XX:MaxDirectMemorySize)")
        private Long maxDirectMemory;

        @Schema(description = "使用率(%)")
        private Double usagePercent;
    }
}
