package com.chua.starter.monitor.pojo;

import com.chua.starter.monitor.pojo.ClientHealthReport;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.Duration;

/**
 * 客户端健康状态存储对象
 * 
 * @author CH
 * @since 2024/12/13
 */
@ApiModel(description = "客户端健康状态存储对象")
@Schema(description = "客户端健康状态存储对象")
@Data
public class ClientHealthStatus implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String REDIS_KEY_PREFIX = "client:health:";

    /**
     * 客户端IP地址
     */
    @ApiModelProperty(value = "客户端IP地址")
    @Schema(description = "客户端IP地址")
    private String clientIp;

    /**
     * 客户端端口号
     */
    @ApiModelProperty(value = "客户端端口号")
    @Schema(description = "客户端端口号")
    private Integer clientPort;

    /**
     * 上报时间戳
     */
    @ApiModelProperty(value = "上报时间戳")
    @Schema(description = "上报时间戳")
    private LocalDateTime reportTime;

    /**
     * 存储时间戳
     */
    @ApiModelProperty(value = "存储时间戳")
    @Schema(description = "存储时间戳")
    private LocalDateTime storeTime;

    /**
     * 客户端版本号
     */
    @ApiModelProperty(value = "客户端版本号")
    @Schema(description = "客户端版本号")
    private String clientVersion;

    /**
     * 客户端主机名
     */
    @ApiModelProperty(value = "客户端主机名")
    @Schema(description = "客户端主机名")
    private String hostname;

    /**
     * 操作系统信息
     */
    @ApiModelProperty(value = "操作系统信息")
    @Schema(description = "操作系统信息")
    private String osInfo;

    /**
     * CPU使用率（百分比）
     */
    @ApiModelProperty(value = "CPU使用率")
    @Schema(description = "CPU使用率（百分比）")
    private Double cpuUsage;

    /**
     * 内存使用率（百分比）
     */
    @ApiModelProperty(value = "内存使用率")
    @Schema(description = "内存使用率（百分比）")
    private Double memoryUsage;

    /**
     * 磁盘使用率（百分比）
     */
    @ApiModelProperty(value = "磁盘使用率")
    @Schema(description = "磁盘使用率（百分比）")
    private Double diskUsage;

    /**
     * 网络延迟（毫秒）
     */
    @ApiModelProperty(value = "网络延迟")
    @Schema(description = "网络延迟（毫秒）")
    private Long networkLatency;

    /**
     * 额外信息（JSON格式）
     */
    @ApiModelProperty(value = "额外信息")
    @Schema(description = "额外信息（JSON格式）")
    private String extraInfo;

    /**
     * 获取客户端地址
     * 
     * @return 客户端地址（IP:端口）
     */
    public String getClientAddress() {
        return clientIp + ":" + clientPort;
    }

    /**
     * 获取Redis存储键
     * 
     * @return Redis键
     */
    public String getRedisKey() {
        return getRedisKey(clientIp, clientPort);
    }

    /**
     * 获取Redis存储键（静态方法）
     * 
     * @param clientIp 客户端IP
     * @param clientPort 客户端端口
     * @return Redis键
     */
    public static String getRedisKey(String clientIp, Integer clientPort) {
        return REDIS_KEY_PREFIX + clientIp + ":" + clientPort;
    }

    /**
     * 判断健康状态是否正常
     * 
     * @return true表示健康状态正常
     */
    public boolean isHealthy() {
        // 基本的健康检查逻辑
        if (cpuUsage != null && cpuUsage > 90) {
            return false;
        }
        if (memoryUsage != null && memoryUsage > 90) {
            return false;
        }
        if (diskUsage != null && diskUsage > 95) {
            return false;
        }
        if (networkLatency != null && networkLatency > 5000) {
            return false;
        }
        return true;
    }

    /**
     * 判断是否在线且有效
     * 
     * @return true表示在线且有效
     */
    public boolean isOnlineAndValid() {
        if (storeTime == null) {
            return false;
        }
        
        // 检查是否在有效期内（10秒内）
        Duration duration = Duration.between(storeTime, LocalDateTime.now());
        return duration.getSeconds() <= 10;
    }

    /**
     * 判断是否已过期
     * 
     * @return true表示已过期
     */
    public boolean isExpired() {
        return !isOnlineAndValid();
    }

    /**
     * 获取健康状态描述
     * 
     * @return 健康状态描述
     */
    public String getHealthStatus() {
        if (isHealthy()) {
            return "健康";
        }
        
        StringBuilder status = new StringBuilder();
        if (cpuUsage != null && cpuUsage > 90) {
            status.append("CPU使用率过高(").append(cpuUsage).append("%) ");
        }
        if (memoryUsage != null && memoryUsage > 90) {
            status.append("内存使用率过高(").append(memoryUsage).append("%) ");
        }
        if (diskUsage != null && diskUsage > 95) {
            status.append("磁盘使用率过高(").append(diskUsage).append("%) ");
        }
        if (networkLatency != null && networkLatency > 5000) {
            status.append("网络延迟过高(").append(networkLatency).append("ms) ");
        }
        
        return status.toString().trim();
    }

    /**
     * 获取在线状态描述
     * 
     * @return 在线状态描述
     */
    public String getOnlineStatus() {
        if (isOnlineAndValid()) {
            return "在线";
        } else {
            return "离线";
        }
    }

    /**
     * 从健康状态上报创建存储对象
     * 
     * @param report 健康状态上报
     * @return 健康状态存储对象
     */
    public static ClientHealthStatus fromReport(ClientHealthReport report) {
        ClientHealthStatus status = new ClientHealthStatus();
        status.setClientIp(report.getClientIp());
        status.setClientPort(report.getClientPort());
        status.setReportTime(report.getReportTime());
        status.setStoreTime(LocalDateTime.now());
        status.setClientVersion(report.getClientVersion());
        status.setHostname(report.getHostname());
        status.setOsInfo(report.getOsInfo());
        status.setCpuUsage(report.getCpuUsage());
        status.setMemoryUsage(report.getMemoryUsage());
        status.setDiskUsage(report.getDiskUsage());
        status.setNetworkLatency(report.getNetworkLatency());
        status.setExtraInfo(report.getExtraInfo());
        return status;
    }

    /**
     * 创建默认的健康状态
     * 
     * @param clientIp 客户端IP
     * @param clientPort 客户端端口
     * @return 健康状态对象
     */
    public static ClientHealthStatus createDefault(String clientIp, Integer clientPort) {
        ClientHealthStatus status = new ClientHealthStatus();
        status.setClientIp(clientIp);
        status.setClientPort(clientPort);
        status.setReportTime(LocalDateTime.now());
        status.setStoreTime(LocalDateTime.now());
        return status;
    }

    /**
     * 获取存活时间（秒）
     * 
     * @return 存活时间
     */
    public long getAliveSeconds() {
        if (storeTime == null) {
            return 0;
        }
        Duration duration = Duration.between(storeTime, LocalDateTime.now());
        return duration.getSeconds();
    }

    /**
     * 获取剩余存活时间（秒）
     * 
     * @return 剩余存活时间
     */
    public long getRemainingSeconds() {
        long aliveSeconds = getAliveSeconds();
        return Math.max(0, 10 - aliveSeconds);
    }

    /**
     * 判断是否即将过期（剩余时间少于3秒）
     * 
     * @return true表示即将过期
     */
    public boolean isAboutToExpire() {
        return getRemainingSeconds() <= 3;
    }

    @Override
    public String toString() {
        return String.format("ClientHealthStatus{address=%s, status=%s, online=%s, remaining=%ds}", 
                getClientAddress(), getHealthStatus(), getOnlineStatus(), getRemainingSeconds());
    }
}
