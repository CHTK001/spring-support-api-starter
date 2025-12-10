package com.chua.starter.monitor.pojo.node;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 节点监控指标数据传输对象
 *
 * @author CH
 * @version 1.0.0
 * @since 2025/12/08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "节点监控指标")
public class NodeMetricsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 节点标识
     */
    @Schema(description = "节点标识")
    private String nodeId;

    /**
     * 节点IP地址
     */
    @Schema(description = "节点IP地址")
    private String ipAddress;

    /**
     * 节点端口
     */
    @Schema(description = "节点端口")
    private Integer port;

    /**
     * 应用名称
     */
    @Schema(description = "应用名称")
    private String applicationName;

    /**
     * 采集时间戳
     */
    @Schema(description = "采集时间戳")
    private Long timestamp;

    /**
     * CPU指标
     */
    @Schema(description = "CPU指标")
    private CpuMetrics cpu;

    /**
     * 内存指标
     */
    @Schema(description = "内存指标")
    private MemoryMetrics memory;

    /**
     * 磁盘指标
     */
    @Schema(description = "磁盘指标")
    private DiskMetrics disk;

    /**
     * 网络指标
     */
    @Schema(description = "网络指标")
    private NetworkMetrics network;

    /**
     * JVM指标
     */
    @Schema(description = "JVM指标")
    private JvmMetrics jvm;

    /**
     * 线程指标
     */
    @Schema(description = "线程指标")
    private ThreadMetrics thread;

    /**
     * GC指标
     */
    @Schema(description = "GC指标")
    private GcMetrics gc;

    /**
     * CPU指标
     *
     * @author CH
     * @since 2025/12/08
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "CPU指标")
    public static class CpuMetrics implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * CPU使用率（百分比）
         */
        @Schema(description = "CPU使用率（百分比）")
        private Double usage;

        /**
         * 系统CPU使用率
         */
        @Schema(description = "系统CPU使用率")
        private Double systemUsage;

        /**
         * 进程CPU使用率
         */
        @Schema(description = "进程CPU使用率")
        private Double processUsage;

        /**
         * CPU核心数
         */
        @Schema(description = "CPU核心数")
        private Integer cores;

        /**
         * 系统负载
         */
        @Schema(description = "系统负载")
        private Double loadAverage;
    }

    /**
     * 内存指标
     *
     * @author CH
     * @since 2025/12/08
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "内存指标")
    public static class MemoryMetrics implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 总内存（字节）
         */
        @Schema(description = "总内存（字节）")
        private Long total;

        /**
         * 已用内存（字节）
         */
        @Schema(description = "已用内存（字节）")
        private Long used;

        /**
         * 空闲内存（字节）
         */
        @Schema(description = "空闲内存（字节）")
        private Long free;

        /**
         * 内存使用率（百分比）
         */
        @Schema(description = "内存使用率（百分比）")
        private Double usage;
    }

    /**
     * 磁盘指标
     *
     * @author CH
     * @since 2025/12/08
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "磁盘指标")
    public static class DiskMetrics implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 总磁盘空间（字节）
         */
        @Schema(description = "总磁盘空间（字节）")
        private Long total;

        /**
         * 已用磁盘空间（字节）
         */
        @Schema(description = "已用磁盘空间（字节）")
        private Long used;

        /**
         * 空闲磁盘空间（字节）
         */
        @Schema(description = "空闲磁盘空间（字节）")
        private Long free;

        /**
         * 磁盘使用率（百分比）
         */
        @Schema(description = "磁盘使用率（百分比）")
        private Double usage;

        /**
         * 各分区信息
         */
        @Schema(description = "各分区信息")
        private List<DiskPartition> partitions;
    }

    /**
     * 磁盘分区信息
     *
     * @author CH
     * @since 2025/12/08
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "磁盘分区信息")
    public static class DiskPartition implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 挂载点
         */
        @Schema(description = "挂载点")
        private String mountPoint;

        /**
         * 总空间
         */
        @Schema(description = "总空间")
        private Long total;

        /**
         * 已用空间
         */
        @Schema(description = "已用空间")
        private Long used;

        /**
         * 空闲空间
         */
        @Schema(description = "空闲空间")
        private Long free;

        /**
         * 使用率
         */
        @Schema(description = "使用率")
        private Double usage;
    }

    /**
     * 网络指标
     *
     * @author CH
     * @since 2025/12/08
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "网络指标")
    public static class NetworkMetrics implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 接收字节数
         */
        @Schema(description = "接收字节数")
        private Long bytesReceived;

        /**
         * 发送字节数
         */
        @Schema(description = "发送字节数")
        private Long bytesSent;

        /**
         * 接收速率（字节/秒）
         */
        @Schema(description = "接收速率（字节/秒）")
        private Long receiveRate;

        /**
         * 发送速率（字节/秒）
         */
        @Schema(description = "发送速率（字节/秒）")
        private Long sendRate;

        /**
         * TCP连接数
         */
        @Schema(description = "TCP连接数")
        private Integer tcpConnections;
    }

    /**
     * JVM指标
     *
     * @author CH
     * @since 2025/12/08
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "JVM指标")
    public static class JvmMetrics implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 堆内存使用（字节）
         */
        @Schema(description = "堆内存使用（字节）")
        private Long heapUsed;

        /**
         * 堆内存最大值（字节）
         */
        @Schema(description = "堆内存最大值（字节）")
        private Long heapMax;

        /**
         * 堆内存已提交（字节）
         */
        @Schema(description = "堆内存已提交（字节）")
        private Long heapCommitted;

        /**
         * 非堆内存使用（字节）
         */
        @Schema(description = "非堆内存使用（字节）")
        private Long nonHeapUsed;

        /**
         * 非堆内存最大值（字节）
         */
        @Schema(description = "非堆内存最大值（字节）")
        private Long nonHeapMax;

        /**
         * JVM运行时长（毫秒）
         */
        @Schema(description = "JVM运行时长（毫秒）")
        private Long uptime;

        /**
         * 启动时间戳
         */
        @Schema(description = "启动时间戳")
        private Long startTime;

        /**
         * JVM版本
         */
        @Schema(description = "JVM版本")
        private String version;

        /**
         * JVM名称
         */
        @Schema(description = "JVM名称")
        private String name;

        /**
         * 内存池信息
         */
        @Schema(description = "内存池信息")
        private Map<String, MemoryPoolInfo> memoryPools;
    }

    /**
     * 内存池信息
     *
     * @author CH
     * @since 2025/12/08
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "内存池信息")
    public static class MemoryPoolInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 池名称
         */
        @Schema(description = "池名称")
        private String name;

        /**
         * 已使用
         */
        @Schema(description = "已使用")
        private Long used;

        /**
         * 最大值
         */
        @Schema(description = "最大值")
        private Long max;

        /**
         * 已提交
         */
        @Schema(description = "已提交")
        private Long committed;
    }

    /**
     * 线程指标
     *
     * @author CH
     * @since 2025/12/08
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "线程指标")
    public static class ThreadMetrics implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 活动线程数
         */
        @Schema(description = "活动线程数")
        private Integer live;

        /**
         * 守护线程数
         */
        @Schema(description = "守护线程数")
        private Integer daemon;

        /**
         * 峰值线程数
         */
        @Schema(description = "峰值线程数")
        private Integer peak;

        /**
         * 总启动线程数
         */
        @Schema(description = "总启动线程数")
        private Long totalStarted;

        /**
         * 各状态线程数
         */
        @Schema(description = "各状态线程数")
        private Map<String, Integer> stateCount;
    }

    /**
     * GC指标
     *
     * @author CH
     * @since 2025/12/08
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "GC指标")
    public static class GcMetrics implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * GC次数
         */
        @Schema(description = "GC次数")
        private Long count;

        /**
         * GC总耗时（毫秒）
         */
        @Schema(description = "GC总耗时（毫秒）")
        private Long time;

        /**
         * 各GC收集器信息
         */
        @Schema(description = "各GC收集器信息")
        private Map<String, GcCollectorInfo> collectors;
    }

    /**
     * GC收集器信息
     *
     * @author CH
     * @since 2025/12/08
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "GC收集器信息")
    public static class GcCollectorInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 收集器名称
         */
        @Schema(description = "收集器名称")
        private String name;

        /**
         * 收集次数
         */
        @Schema(description = "收集次数")
        private Long count;

        /**
         * 收集耗时（毫秒）
         */
        @Schema(description = "收集耗时（毫秒）")
        private Long time;
    }
}
