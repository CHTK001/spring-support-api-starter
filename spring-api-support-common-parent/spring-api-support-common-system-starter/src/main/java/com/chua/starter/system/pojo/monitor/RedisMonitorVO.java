package com.chua.starter.system.pojo.monitor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Redis 监控信息
 *
 * @since 4.0.0.50
 * @author CH
 */
@Data
@Schema(description = "Redis 监控信息")
public class RedisMonitorVO implements Serializable {

    /**
     * 串行版本UID
     */
    private static final long serialVersionUID = 1L;

    @Schema(description = "Redis 版本")
    private String version;

    @Schema(description = "运行模式: standalone/cluster")
    private String mode;

    @Schema(description = "端口")
    private String port;

    @Schema(description = "已连接客户端数")
    private Integer connectedClients;

    @Schema(description = "运行天数")
    private Long uptimeDays;

    @Schema(description = "已用内存(人类可读)")
    private String usedMemory;

    @Schema(description = "已用内存(字节)")
    private Long usedMemoryBytes;

    @Schema(description = "最大内存(人类可读)")
    private String maxMemory;

    @Schema(description = "最大内存(字节)")
    private Long maxMemoryBytes;

    @Schema(description = "RSS 内存(人类可读)")
    private String usedMemoryRss;

    @Schema(description = "RSS 内存(字节)")
    private Long usedMemoryRssBytes;

    @Schema(description = "内存峰值(人类可读)")
    private String usedMemoryPeak;

    @Schema(description = "内存峰值(字节)")
    private Long usedMemoryPeakBytes;

    @Schema(description = "内存碎片率")
    private Double memFragmentationRatio;

    @Schema(description = "内存分配器")
    private String memAllocator;

    @Schema(description = "CPU 占用百分比")
    private Double usedCpuPercent;

    @Schema(description = "是否开启 AOF")
    private Boolean aofEnabled;

    @Schema(description = "RDB 最后状态: ok/err")
    private String rdbLastStatus;

    @Schema(description = "key 总数")
    private Long keyCount;

    @Schema(description = "网络入站 Kps")
    private Double netInputKps;

    @Schema(description = "网络出站 Kps")
    private Double netOutputKps;

    @Schema(description = "命令统计(按调用次数降序前15)")
    private List<CommandStat> commandStats;

    @Schema(description = "各 db keyspace 详情")
    private List<KeyspaceDetail> keyspaceDetails;

    /**
     * 命令调用统计
     */
    @Data
    @Schema(description = "命令调用统计")
    public static class CommandStat implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "命令名")
        private String command;

        @Schema(description = "调用次数")
        private Long calls;

        @Schema(description = "占比百分比")
        private Double percentage;
    }

    /**
     * 单个 db 的 keyspace 详情
     */
    @Data
    @Schema(description = "keyspace 详情")
    public static class KeyspaceDetail implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "db 名, 如 db0")
        private String db;

        @Schema(description = "key 数量")
        private Long keys;

        @Schema(description = "带过期时间的 key 数量")
        private Long expires;

        @Schema(description = "平均 TTL(秒)")
        private Long avgTtl;
    }
}
