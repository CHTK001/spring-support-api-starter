package com.chua.starter.monitor.starter.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 网络速率计算服务
 * 用于计算网络流量的实时速率
 *
 * @author CH
 * @since 2024/12/30
 */
@Slf4j
@Service
public class NetworkSpeedCalculatorService {

    /**
     * 存储每个服务器的网络统计历史数据
     */
    private final Map<Integer, NetworkStats> networkStatsMap = new ConcurrentHashMap<>();

    /**
     * 网络统计数据
     */
    @Data
    public static class NetworkStats {
        private long lastInBytes;
        private long lastOutBytes;
        private long lastInPackets;
        private long lastOutPackets;
        private LocalDateTime lastUpdateTime;
        private double currentInSpeed;  // 字节/秒
        private double currentOutSpeed; // 字节/秒
        private double currentInPacketRate;  // 包/秒
        private double currentOutPacketRate; // 包/秒

        public NetworkStats() {
            this.lastUpdateTime = LocalDateTime.now();
        }
    }

    /**
     * 网络速率结果
     */
    @Data
    public static class NetworkSpeedResult {
        private double inSpeed;      // 入站速率 (字节/秒)
        private double outSpeed;     // 出站速率 (字节/秒)
        private double inPacketRate; // 入站包速率 (包/秒)
        private double outPacketRate;// 出站包速率 (包/秒)
        private long totalInBytes;   // 总接收字节数
        private long totalOutBytes;  // 总发送字节数
        private long totalInPackets; // 总接收包数
        private long totalOutPackets;// 总发送包数
    }

    /**
     * 计算网络速率
     *
     * @param serverId    服务器ID
     * @param inBytes     当前接收字节数
     * @param outBytes    当前发送字节数
     * @param inPackets   当前接收包数
     * @param outPackets  当前发送包数
     * @return 网络速率结果
     */
    public NetworkSpeedResult calculateSpeed(Integer serverId, long inBytes, long outBytes, 
                                           long inPackets, long outPackets) {
        NetworkStats stats = networkStatsMap.computeIfAbsent(serverId, k -> new NetworkStats());
        LocalDateTime now = LocalDateTime.now();
        
        NetworkSpeedResult result = new NetworkSpeedResult();
        result.setTotalInBytes(inBytes);
        result.setTotalOutBytes(outBytes);
        result.setTotalInPackets(inPackets);
        result.setTotalOutPackets(outPackets);

        // 如果是第一次记录，只保存数据不计算速率
        if (stats.getLastUpdateTime() == null) {
            stats.setLastInBytes(inBytes);
            stats.setLastOutBytes(outBytes);
            stats.setLastInPackets(inPackets);
            stats.setLastOutPackets(outPackets);
            stats.setLastUpdateTime(now);
            
            result.setInSpeed(0.0);
            result.setOutSpeed(0.0);
            result.setInPacketRate(0.0);
            result.setOutPacketRate(0.0);
            
            return result;
        }

        // 计算时间差（秒）
        long timeDiffSeconds = java.time.Duration.between(stats.getLastUpdateTime(), now).getSeconds();
        if (timeDiffSeconds <= 0) {
            timeDiffSeconds = 1; // 避免除零错误
        }

        // 计算字节差值
        long inBytesDiff = inBytes - stats.getLastInBytes();
        long outBytesDiff = outBytes - stats.getLastOutBytes();
        long inPacketsDiff = inPackets - stats.getLastInPackets();
        long outPacketsDiff = outPackets - stats.getLastOutPackets();

        // 处理计数器重置的情况（当前值小于上次值）
        if (inBytesDiff < 0) {
            inBytesDiff = inBytes; // 假设计数器重置，使用当前值
        }
        if (outBytesDiff < 0) {
            outBytesDiff = outBytes;
        }
        if (inPacketsDiff < 0) {
            inPacketsDiff = inPackets;
        }
        if (outPacketsDiff < 0) {
            outPacketsDiff = outPackets;
        }

        // 计算速率（字节/秒 和 包/秒）
        double inSpeed = (double) inBytesDiff / timeDiffSeconds;
        double outSpeed = (double) outBytesDiff / timeDiffSeconds;
        double inPacketRate = (double) inPacketsDiff / timeDiffSeconds;
        double outPacketRate = (double) outPacketsDiff / timeDiffSeconds;

        // 更新统计数据
        stats.setLastInBytes(inBytes);
        stats.setLastOutBytes(outBytes);
        stats.setLastInPackets(inPackets);
        stats.setLastOutPackets(outPackets);
        stats.setLastUpdateTime(now);
        stats.setCurrentInSpeed(inSpeed);
        stats.setCurrentOutSpeed(outSpeed);
        stats.setCurrentInPacketRate(inPacketRate);
        stats.setCurrentOutPacketRate(outPacketRate);

        // 设置结果
        result.setInSpeed(inSpeed);
        result.setOutSpeed(outSpeed);
        result.setInPacketRate(inPacketRate);
        result.setOutPacketRate(outPacketRate);

        log.debug("服务器 {} 网络速率计算: 入站{}B/s, 出站{}B/s, 时间间隔{}s", 
                serverId, Math.round(inSpeed), Math.round(outSpeed), timeDiffSeconds);

        return result;
    }

    /**
     * 获取服务器当前网络统计
     *
     * @param serverId 服务器ID
     * @return 网络统计数据，如果不存在返回null
     */
    public NetworkStats getCurrentStats(Integer serverId) {
        return networkStatsMap.get(serverId);
    }

    /**
     * 清除服务器的网络统计数据
     *
     * @param serverId 服务器ID
     */
    public void clearStats(Integer serverId) {
        networkStatsMap.remove(serverId);
        log.debug("清除服务器 {} 的网络统计数据", serverId);
    }

    /**
     * 清除所有网络统计数据
     */
    public void clearAllStats() {
        networkStatsMap.clear();
        log.debug("清除所有网络统计数据");
    }

    /**
     * 格式化网络速率为可读字符串
     *
     * @param bytesPerSecond 字节/秒
     * @return 格式化的速率字符串
     */
    public static String formatSpeed(double bytesPerSecond) {
        if (bytesPerSecond < 1024) {
            return String.format("%.1f B/s", bytesPerSecond);
        } else if (bytesPerSecond < 1024 * 1024) {
            return String.format("%.1f KB/s", bytesPerSecond / 1024);
        } else if (bytesPerSecond < 1024 * 1024 * 1024) {
            return String.format("%.1f MB/s", bytesPerSecond / (1024 * 1024));
        } else {
            return String.format("%.1f GB/s", bytesPerSecond / (1024 * 1024 * 1024));
        }
    }

    /**
     * 格式化包速率为可读字符串
     *
     * @param packetsPerSecond 包/秒
     * @return 格式化的包速率字符串
     */
    public static String formatPacketRate(double packetsPerSecond) {
        if (packetsPerSecond < 1000) {
            return String.format("%.1f pps", packetsPerSecond);
        } else if (packetsPerSecond < 1000000) {
            return String.format("%.1f Kpps", packetsPerSecond / 1000);
        } else {
            return String.format("%.1f Mpps", packetsPerSecond / 1000000);
        }
    }

    /**
     * 获取所有服务器的网络统计概览
     *
     * @return 统计概览
     */
    public Map<String, Object> getStatsOverview() {
        Map<String, Object> overview = new ConcurrentHashMap<>();
        overview.put("totalServers", networkStatsMap.size());
        
        double totalInSpeed = 0;
        double totalOutSpeed = 0;
        int activeServers = 0;
        
        for (NetworkStats stats : networkStatsMap.values()) {
            if (stats.getLastUpdateTime() != null && 
                java.time.Duration.between(stats.getLastUpdateTime(), LocalDateTime.now()).getSeconds() < 300) {
                totalInSpeed += stats.getCurrentInSpeed();
                totalOutSpeed += stats.getCurrentOutSpeed();
                activeServers++;
            }
        }
        
        overview.put("activeServers", activeServers);
        overview.put("totalInSpeed", totalInSpeed);
        overview.put("totalOutSpeed", totalOutSpeed);
        overview.put("totalInSpeedFormatted", formatSpeed(totalInSpeed));
        overview.put("totalOutSpeedFormatted", formatSpeed(totalOutSpeed));
        
        return overview;
    }
}
