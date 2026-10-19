package com.chua.starter.system.pojo.monitor;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 服务(硬件/JVM)监控信息
 *
 * @param cpu                  CPU 信息
 * @param systemMemory         系统内存(bytes)
 * @param jvmMemory            JVM 内存(bytes)
 * @param diskPeak             磁盘整体峰值
 * @param disks                各挂载点磁盘
 * @param server               服务器信息
 * @param jvm                  JVM 信息
 * @param systemUptime         系统运行时长(秒)
 * @param processUptime        JVM进程运行时长(秒)
 * @param processCount         OS进程总数
 * @param threadCount          当前线程数
 * @param peakThreadCount      峰值线程数
 * @param daemonThreadCount    守护线程数
 * @param systemLoadAverage    系统1分钟负载均值, 取不到为null
 * @param coreUsages           每个逻辑核心使用率(0-100)
 * @param memoryPools          JVM内存池
 * @param gcCollectors         GC 收集器
 * @param loadedClassCount     已加载类数量
 * @param totalLoadedClassCount 累计加载类数量
 * @param unloadedClassCount   已卸载类数量
 * @param threadStates         线程状态分布
 * @param deadlockedThreadCount 死锁线程数
 * @param topThreads           CPU时间最高的线程(前10)
 * @param networkInterfaces    网络接口(累计流量)
 * @param springConfig         Spring 核心参数
 * @since 4.0.0.50
 * @author CH
 */
@Schema(description = "服务监控信息")
public record ServerMonitorVO(
        @Schema(description = "CPU 信息") CpuInfo cpu,
        @Schema(description = "系统内存(bytes)") MemInfo systemMemory,
        @Schema(description = "JVM 内存(bytes)") MemInfo jvmMemory,
        @Schema(description = "磁盘整体峰值") DiskPeak diskPeak,
        @Schema(description = "各挂载点磁盘") List<DiskInfo> disks,
        @Schema(description = "服务器信息") ServerInfo server,
        @Schema(description = "JVM 信息") JvmInfo jvm,
        @Schema(description = "系统运行时长(秒)") Long systemUptime,
        @Schema(description = "JVM进程运行时长(秒)") Long processUptime,
        @Schema(description = "OS进程总数") Integer processCount,
        @Schema(description = "当前线程数") Integer threadCount,
        @Schema(description = "峰值线程数") Integer peakThreadCount,
        @Schema(description = "守护线程数") Integer daemonThreadCount,
        @Schema(description = "系统1分钟负载均值, 取不到为null") Double systemLoadAverage,
        @Schema(description = "每个逻辑核心使用率(0-100)") List<Double> coreUsages,
        @Schema(description = "JVM内存池") List<MemoryPoolInfo> memoryPools,
        @Schema(description = "GC 收集器") List<GcInfo> gcCollectors,
        @Schema(description = "已加载类数量") Integer loadedClassCount,
        @Schema(description = "累计加载类数量") Long totalLoadedClassCount,
        @Schema(description = "已卸载类数量") Long unloadedClassCount,
        @Schema(description = "线程状态分布") Map<String, Integer> threadStates,
        @Schema(description = "死锁线程数") Integer deadlockedThreadCount,
        @Schema(description = "CPU时间最高的线程(前10)") List<ThreadDetail> topThreads,
        @Schema(description = "网络接口(累计流量)") List<NetworkInfo> networkInterfaces,
        @Schema(description = "Spring 核心参数") Map<String, String> springConfig) implements Serializable {

    /** 串行版本UID */
    private static final long serialVersionUID = 1L;

    // ---- 嵌套 record ----

    @Schema(description = "CPU")
    public record CpuInfo(
            @Schema(description = "逻辑核心数") int logicalCores,
            @Schema(description = "用户态占用百分比") double userPercent,
            @Schema(description = "系统态占用百分比") double systemPercent,
            @Schema(description = "空闲百分比") double idlePercent,
            @Schema(description = "总使用率百分比") double usagePercent) implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    @Schema(description = "内存")
    public record MemInfo(
            @Schema(description = "总量(bytes)") long total,
            @Schema(description = "已用(bytes)") long used,
            @Schema(description = "空闲(bytes)") long free,
            @Schema(description = "使用率百分比") double usagePercent) implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    @Schema(description = "磁盘峰值")
    public record DiskPeak(
            @Schema(description = "最大使用率百分比") double usagePercent,
            @Schema(description = "分区数量") int partitionCount) implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    @Schema(description = "磁盘")
    public record DiskInfo(
            @Schema(description = "挂载点/路径") String path,
            @Schema(description = "使用率百分比") double usagePercent,
            @Schema(description = "文件系统类型") String fsType,
            @Schema(description = "总量(bytes)") long total,
            @Schema(description = "已用(bytes)") long used,
            @Schema(description = "可用(bytes)") long available) implements Serializable {
        private static final long serialVersionUID = 1L;

        /** 兼容旧前端：剩余空间 = available */
        public long free() {
            return available;
        }
    }

    @Schema(description = "服务器")
    public record ServerInfo(
            @Schema(description = "主机名") String hostname,
            @Schema(description = "操作系统名") String osName,
            @Schema(description = "操作系统版本") String osVersion,
            @Schema(description = "IP(首个非回环IPv4)") String ip,
            @Schema(description = "系统架构") String arch) implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    @Schema(description = "JVM")
    public record JvmInfo(
            @Schema(description = "JVM 名称") String name,
            @Schema(description = "JVM 版本") String version,
            @Schema(description = "启动时间(ms)") long startTime,
            @Schema(description = "运行时长(ms)") long uptime,
            @Schema(description = "JAVA_HOME") String javaHome,
            @Schema(description = "项目工作目录") String projectPath,
            @Schema(description = "JVM 输入参数") List<String> inputArgs) implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    @Schema(description = "JVM内存池")
    public record MemoryPoolInfo(
            @Schema(description = "名称") String name,
            @Schema(description = "类型: heap/non-heap") String type,
            @Schema(description = "初始化(bytes)") Long init,
            @Schema(description = "已用(bytes)") Long used,
            @Schema(description = "已提交(bytes)") Long committed,
            @Schema(description = "最大(bytes)") Long max) implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    @Schema(description = "GC收集器")
    public record GcInfo(
            @Schema(description = "名称") String name,
            @Schema(description = "累计回收次数") Long collectionCount,
            @Schema(description = "累计耗时(ms)") Long collectionTime) implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    @Schema(description = "线程明细")
    public record ThreadDetail(
            @Schema(description = "线程名") String name,
            @Schema(description = "线程状态") String state,
            @Schema(description = "CPU时间(ms), 不支持则为null") Long cpuTime,
            @Schema(description = "用户态时间(ms), 不支持则为null") Long userTime) implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    @Schema(description = "网络接口")
    public record NetworkInfo(
            @Schema(description = "接口名") String name,
            @Schema(description = "显示名") String displayName,
            @Schema(description = "累计接收字节") long bytesRecv,
            @Schema(description = "累计发送字节") long bytesSent,
            @Schema(description = "累计接收包数") long packetsRecv,
            @Schema(description = "累计发送包数") long packetsSent,
            @Schema(description = "网卡速率(bps), 未知为0") long speed,
            @Schema(description = "IP 地址列表") List<String> addresses,
            @Schema(description = "接收丢包数") long dropRecv,
            @Schema(description = "发送丢包数") long dropSent) implements Serializable {
        private static final long serialVersionUID = 1L;
    }
}
