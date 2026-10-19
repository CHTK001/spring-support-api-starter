package com.chua.starter.system.pojo.monitor;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;

/**
 * Redis 监控信息
 *
 * @param version             Redis 版本
 * @param mode                运行模式: standalone/cluster
 * @param port                端口
 * @param connectedClients    已连接客户端数
 * @param uptimeDays          运行天数
 * @param usedMemory          已用内存(人类可读)
 * @param usedMemoryBytes     已用内存(字节)
 * @param maxMemory           最大内存(人类可读)
 * @param maxMemoryBytes      最大内存(字节)
 * @param usedMemoryRss       RSS 内存(人类可读)
 * @param usedMemoryRssBytes  RSS 内存(字节)
 * @param usedMemoryPeak      内存峰值(人类可读)
 * @param usedMemoryPeakBytes 内存峰值(字节)
 * @param memFragmentationRatio 内存碎片率
 * @param memAllocator        内存分配器
 * @param usedCpuPercent      CPU 占用百分比
 * @param aofEnabled          是否开启 AOF
 * @param rdbLastStatus       RDB 最后状态: ok/err
 * @param keyCount            key 总数
 * @param netInputKps         网络入站 Kps
 * @param netOutputKps        网络出站 Kps
 * @param commandStats        命令统计(按调用次数降序前15)
 * @param keyspaceDetails     各 db keyspace 详情
 * @since 4.0.0.50
 * @author CH
 */
@Schema(description = "Redis 监控信息")
public record RedisMonitorVO(
        @Schema(description = "Redis 版本") String version,
        @Schema(description = "运行模式: standalone/cluster") String mode,
        @Schema(description = "端口") String port,
        @Schema(description = "已连接客户端数") Integer connectedClients,
        @Schema(description = "运行天数") Long uptimeDays,
        @Schema(description = "已用内存(人类可读)") String usedMemory,
        @Schema(description = "已用内存(字节)") Long usedMemoryBytes,
        @Schema(description = "最大内存(人类可读)") String maxMemory,
        @Schema(description = "最大内存(字节)") Long maxMemoryBytes,
        @Schema(description = "RSS 内存(人类可读)") String usedMemoryRss,
        @Schema(description = "RSS 内存(字节)") Long usedMemoryRssBytes,
        @Schema(description = "内存峰值(人类可读)") String usedMemoryPeak,
        @Schema(description = "内存峰值(字节)") Long usedMemoryPeakBytes,
        @Schema(description = "内存碎片率") Double memFragmentationRatio,
        @Schema(description = "内存分配器") String memAllocator,
        @Schema(description = "CPU 占用百分比") Double usedCpuPercent,
        @Schema(description = "是否开启 AOF") Boolean aofEnabled,
        @Schema(description = "RDB 最后状态: ok/err") String rdbLastStatus,
        @Schema(description = "key 总数") Long keyCount,
        @Schema(description = "网络入站 Kps") Double netInputKps,
        @Schema(description = "网络出站 Kps") Double netOutputKps,
        @Schema(description = "命令统计(按调用次数降序前15)") List<CommandStat> commandStats,
        @Schema(description = "各 db keyspace 详情") List<KeyspaceDetail> keyspaceDetails) implements Serializable {

    /** 串行版本UID */
    private static final long serialVersionUID = 1L;

    /**
     * 命令调用统计
     *
     * @param command    命令名
     * @param calls      调用次数
     * @param percentage 占比百分比
     */
    @Schema(description = "命令调用统计")
    public record CommandStat(
            @Schema(description = "命令名") String command,
            @Schema(description = "调用次数") Long calls,
            @Schema(description = "占比百分比") Double percentage) implements Serializable {

        /** 串行版本UID */
        private static final long serialVersionUID = 1L;
    }

    /**
     * 单个 db 的 keyspace 详情
     *
     * @param db      db 名, 如 db0
     * @param keys    key 数量
     * @param expires 带过期时间的 key 数量
     * @param avgTtl  平均 TTL(秒)
     */
    @Schema(description = "keyspace 详情")
    public record KeyspaceDetail(
            @Schema(description = "db 名, 如 db0") String db,
            @Schema(description = "key 数量") Long keys,
            @Schema(description = "带过期时间的 key 数量") Long expires,
            @Schema(description = "平均 TTL(秒)") Long avgTtl) implements Serializable {

        /** 串行版本UID */
        private static final long serialVersionUID = 1L;
    }
}
