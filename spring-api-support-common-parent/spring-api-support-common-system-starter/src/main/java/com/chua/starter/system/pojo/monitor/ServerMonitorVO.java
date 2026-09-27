package com.chua.starter.system.pojo.monitor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 服务(硬件/JVM)监控信息
 *
 * @since 4.0.0.50
 * @author CH
 */
@Data
@Schema(description = "服务监控信息")
public class ServerMonitorVO implements Serializable {

    /**
     * 串行版本UID
     */
    private static final long serialVersionUID = 1L;

    @Schema(description = "CPU 信息")
    private CpuInfo cpu;

    @Schema(description = "系统内存(bytes)")
    private MemInfo systemMemory;

    @Schema(description = "JVM 内存(bytes)")
    private MemInfo jvmMemory;

    @Schema(description = "磁盘整体峰值")
    private DiskPeak diskPeak;

    @Schema(description = "各挂载点磁盘")
    private List<DiskInfo> disks;

    @Schema(description = "服务器信息")
    private ServerInfo server;

    @Schema(description = "JVM 信息")
    private JvmInfo jvm;

    @Schema(description = "系统运行时长(秒)")
    private Long systemUptime;

    @Schema(description = "JVM进程运行时长(秒)")
    private Long processUptime;

    @Schema(description = "OS进程总数")
    private Integer processCount;

    @Schema(description = "当前线程数")
    private Integer threadCount;

    @Schema(description = "峰值线程数")
    private Integer peakThreadCount;

    @Schema(description = "守护线程数")
    private Integer daemonThreadCount;

    @Schema(description = "系统1分钟负载均值, 取不到为null")
    private Double systemLoadAverage;

    @Schema(description = "每个逻辑核心使用率(0-100)")
    private List<Double> coreUsages;

    @Schema(description = "JVM内存池")
    private List<MemoryPoolInfo> memoryPools;

    @Schema(description = "GC 收集器")
    private List<GcInfo> gcCollectors;

    @Schema(description = "已加载类数量")
    private Integer loadedClassCount;

    @Schema(description = "累计加载类数量")
    private Long totalLoadedClassCount;

    @Schema(description = "已卸载类数量")
    private Long unloadedClassCount;

    @Schema(description = "线程状态分布")
    private Map<String, Integer> threadStates;

    @Schema(description = "死锁线程数")
    private Integer deadlockedThreadCount;

    @Schema(description = "CPU时间最高的线程(前10)")
    private List<ThreadDetail> topThreads;

    @Schema(description = "网络接口(累计流量)")
    private List<NetworkInfo> networkInterfaces;

    @Schema(description = "Spring 核心参数")
    private Map<String, String> springConfig;

    /**
     * JVM 内存池
     */
    @Data
    @Schema(description = "JVM内存池")
    public static class MemoryPoolInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "名称")
        private String name;

        @Schema(description = "类型: heap/non-heap")
        private String type;

        @Schema(description = "初始化(bytes)")
        private Long init;

        @Schema(description = "已用(bytes)")
        private Long used;

        @Schema(description = "已提交(bytes)")
        private Long committed;

        @Schema(description = "最大(bytes)")
        private Long max;
    }

    /**
     * GC 收集器
     */
    @Data
    @Schema(description = "GC收集器")
    public static class GcInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "名称")
        private String name;

        @Schema(description = "累计回收次数")
        private Long collectionCount;

        @Schema(description = "累计耗时(ms)")
        private Long collectionTime;
    }

    /**
     * 线程明细
     */
    @Data
    @Schema(description = "线程明细")
    public static class ThreadDetail implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "线程名")
        private String name;

        @Schema(description = "线程状态")
        private String state;

        @Schema(description = "CPU时间(ms), 不支持则为null")
        private Long cpuTime;

        @Schema(description = "用户态时间(ms), 不支持则为null")
        private Long userTime;
    }

    /**
     * 网络接口
     */
    @Data
    @Schema(description = "网络接口")
    public static class NetworkInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "接口名")
        private String name;

        @Schema(description = "显示名")
        private String displayName;

        @Schema(description = "累计接收字节")
        private Long bytesRecv;

        @Schema(description = "累计发送字节")
        private Long bytesSent;

        @Schema(description = "累计接收包数")
        private Long packetsRecv;

        @Schema(description = "累计发送包数")
        private Long packetsSent;

        @Schema(description = "网卡速率(bps), 未知为0")
        private Long speed;

        @Schema(description = "IP 地址列表")
        private List<String> addresses;

        @Schema(description = "接收丢包数")
        private Long dropRecv;

        @Schema(description = "发送丢包数")
        private Long dropSent;
    }

    /**
     * CPU
     */
    @Data
    @Schema(description = "CPU")
    public static class CpuInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "逻辑核心数")
        private int logicalCores;

        @Schema(description = "用户态占用百分比")
        private double userPercent;

        @Schema(description = "系统态占用百分比")
        private double systemPercent;

        @Schema(description = "空闲百分比")
        private double idlePercent;

        @Schema(description = "总使用率百分比")
        private double usagePercent;
    }

    /**
     * 内存
     */
    @Data
    @Schema(description = "内存")
    public static class MemInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "总量(bytes)")
        private long total;

        @Schema(description = "已用(bytes)")
        private long used;

        @Schema(description = "空闲(bytes)")
        private long free;

        @Schema(description = "使用率百分比")
        private double usagePercent;
    }

    /**
     * 磁盘峰值
     */
    @Data
    @Schema(description = "磁盘峰值")
    public static class DiskPeak implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "最大使用率百分比")
        private double usagePercent;

        @Schema(description = "分区数量")
        private int partitionCount;
    }

    /**
     * 单个挂载点磁盘
     */
    @Data
    @Schema(description = "磁盘")
    public static class DiskInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "挂载点/路径")
        private String path;

        @Schema(description = "使用率百分比")
        private double usagePercent;

        @Schema(description = "文件系统类型")
        private String fsType;

        @Schema(description = "总量(bytes)")
        private long total;

        @Schema(description = "已用(bytes)")
        private long used;

        @Schema(description = "可用(bytes)")
        private long available;

        /** 兼容旧前端：剩余空间 = available */
        public long getFree() {
            return available;
        }
    }

    /**
     * 服务器
     */
    @Data
    @Schema(description = "服务器")
    public static class ServerInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "主机名")
        private String hostname;

        @Schema(description = "操作系统名")
        private String osName;

        @Schema(description = "操作系统版本")
        private String osVersion;

        @Schema(description = "IP(首个非回环IPv4)")
        private String ip;

        @Schema(description = "系统架构")
        private String arch;
    }

    /**
     * JVM
     */
    @Data
    @Schema(description = "JVM")
    public static class JvmInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "JVM 名称")
        private String name;

        @Schema(description = "JVM 版本")
        private String version;

        @Schema(description = "启动时间(ms)")
        private long startTime;

        @Schema(description = "运行时长(ms)")
        private long uptime;

        @Schema(description = "JAVA_HOME")
        private String javaHome;

        @Schema(description = "项目工作目录")
        private String projectPath;

        @Schema(description = "JVM 输入参数")
        private List<String> inputArgs;
    }
}
