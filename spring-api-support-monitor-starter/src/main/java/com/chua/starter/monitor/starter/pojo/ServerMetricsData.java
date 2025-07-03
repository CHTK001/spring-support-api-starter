package com.chua.starter.monitor.starter.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 服务器指标数据传输对象
 * 用于 Prometheus 数据收集和传输
 *
 * @author CH
 * @since 2024/12/30
 */
@Data
public class ServerMetricsData {

    /**
     * 服务器ID
     */
    private Integer serverId;

    /**
     * 数据收集时间
     */
    private LocalDateTime collectTime;

    /**
     * CPU使用率 (百分比)
     */
    private Double cpuUsage;

    /**
     * CPU核心数
     */
    private Integer cpuCores;

    /**
     * CPU负载 (1分钟)
     */
    private Double load1m;

    /**
     * CPU负载 (5分钟)
     */
    private Double load5m;

    /**
     * CPU负载 (15分钟)
     */
    private Double load15m;

    /**
     * 内存总量 (字节)
     */
    private Long memoryTotal;

    /**
     * 内存使用量 (字节)
     */
    private Long memoryUsed;

    /**
     * 内存可用量 (字节)
     */
    private Long memoryFree;

    /**
     * 内存使用率 (百分比)
     */
    private Double memoryUsage;

    /**
     * 磁盘总量 (字节)
     */
    private Long diskTotal;

    /**
     * 磁盘使用量 (字节)
     */
    private Long diskUsed;

    /**
     * 磁盘可用量 (字节)
     */
    private Long diskFree;

    /**
     * 磁盘使用率 (百分比)
     */
    private Double diskUsage;

    /**
     * 网络入流量 (字节/秒)
     */
    private Double networkInBytes;

    /**
     * 网络出流量 (字节/秒)
     */
    private Double networkOutBytes;

    /**
     * 网络入包数 (包/秒)
     */
    private Double networkInPackets;

    /**
     * 网络出包数 (包/秒)
     */
    private Double networkOutPackets;

    /**
     * 系统运行时间 (秒)
     */
    private Long uptime;

    /**
     * 进程数量
     */
    private Integer processCount;

    /**
     * 服务器状态 (1=在线, 0=离线)
     */
    private Integer status;

    /**
     * 响应时间 (毫秒)
     */
    private Long responseTime;

    /**
     * 操作系统信息
     */
    private String osInfo;

    /**
     * 主机名
     */
    private String hostname;

    /**
     * 温度 (摄氏度)
     */
    private Double temperature;

    /**
     * 额外信息
     */
    private String extraInfo;

    /**
     * 创建默认的服务器指标数据
     *
     * @param serverId 服务器ID
     * @return 默认指标数据
     */
    public static ServerMetricsData createDefault(Integer serverId) {
        ServerMetricsData data = new ServerMetricsData();
        data.setServerId(serverId);
        data.setCollectTime(LocalDateTime.now());
        data.setStatus(0); // 默认离线
        data.setCpuUsage(0.0);
        data.setMemoryUsage(0.0);
        data.setDiskUsage(0.0);
        data.setNetworkInBytes(0.0);
        data.setNetworkOutBytes(0.0);
        data.setNetworkInPackets(0.0);
        data.setNetworkOutPackets(0.0);
        data.setUptime(0L);
        data.setProcessCount(0);
        data.setResponseTime(0L);
        return data;
    }

    /**
     * 检查数据是否有效
     *
     * @return 是否有效
     */
    public boolean isValid() {
        return serverId != null && collectTime != null;
    }

    /**
     * 获取格式化的运行时间
     *
     * @return 格式化的运行时间字符串
     */
    public String getFormattedUptime() {
        if (uptime == null || uptime <= 0) {
            return "0秒";
        }

        long seconds = uptime;
        long days = seconds / 86400;
        seconds %= 86400;
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;
        seconds %= 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("天");
        }
        if (hours > 0) {
            sb.append(hours).append("小时");
        }
        if (minutes > 0) {
            sb.append(minutes).append("分钟");
        }
        if (seconds > 0 || sb.length() == 0) {
            sb.append(seconds).append("秒");
        }

        return sb.toString();
    }

    /**
     * 获取内存使用情况描述
     *
     * @return 内存使用描述
     */
    public String getMemoryDescription() {
        if (memoryTotal == null || memoryUsed == null) {
            return "未知";
        }

        double totalGB = memoryTotal / (1024.0 * 1024.0 * 1024.0);
        double usedGB = memoryUsed / (1024.0 * 1024.0 * 1024.0);
        double usagePercent = memoryUsage != null ? memoryUsage : 0.0;

        return String.format("%.1fGB / %.1fGB (%.1f%%)", usedGB, totalGB, usagePercent);
    }

    /**
     * 获取磁盘使用情况描述
     *
     * @return 磁盘使用描述
     */
    public String getDiskDescription() {
        if (diskTotal == null || diskUsed == null) {
            return "未知";
        }

        double totalGB = diskTotal / (1024.0 * 1024.0 * 1024.0);
        double usedGB = diskUsed / (1024.0 * 1024.0 * 1024.0);
        double usagePercent = diskUsage != null ? diskUsage : 0.0;

        return String.format("%.1fGB / %.1fGB (%.1f%%)", usedGB, totalGB, usagePercent);
    }

    /**
     * 获取网络流量描述
     *
     * @return 网络流量描述
     */
    public String getNetworkDescription() {
        if (networkInBytes == null || networkOutBytes == null) {
            return "未知";
        }

        double inKB = networkInBytes / 1024.0;
        double outKB = networkOutBytes / 1024.0;

        return String.format("入: %.2fKB/s, 出: %.2fKB/s", inKB, outKB);
    }
}
