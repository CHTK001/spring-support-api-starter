package com.chua.report.client.starter.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 设备指标数据实体类
 *
 * @author CH
 * @since 2024/12/28
 */
@Data
public class DeviceMetrics implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 主机名
     */
    private String hostname;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 操作系统名称
     */
    private String osName;

    /**
     * 操作系统版本
     */
    private String osVersion;

    /**
     * 操作系统架构
     */
    private String osArch;

    /**
     * CPU使用率 (百分比)
     */
    private Double cpuUsage;

    /**
     * CPU核心数
     */
    private Integer cpuCores;

    /**
     * CPU频率 (Hz)
     */
    private Long cpuFrequency;

    /**
     * 内存使用率 (百分比)
     */
    private Double memoryUsage;

    /**
     * 总内存 (字节)
     */
    private Long totalMemory;

    /**
     * 已用内存 (字节)
     */
    private Long usedMemory;

    /**
     * 可用内存 (字节)
     */
    private Long availableMemory;

    /**
     * 磁盘使用率 (百分比)
     */
    private Double diskUsage;

    /**
     * 总磁盘空间 (字节)
     */
    private Long totalDisk;

    /**
     * 已用磁盘空间 (字节)
     */
    private Long usedDisk;

    /**
     * 可用磁盘空间 (字节)
     */
    private Long availableDisk;

    /**
     * 磁盘信息列表
     */
    private List<Map<String, Object>> diskInfo;

    /**
     * 网络接收字节数
     */
    private Long networkInBytes;

    /**
     * 网络发送字节数
     */
    private Long networkOutBytes;

    /**
     * 网络接收包数
     */
    private Long networkInPackets;

    /**
     * 网络发送包数
     */
    private Long networkOutPackets;

    /**
     * 系统负载
     */
    private String loadAverage;

    /**
     * 温度 (摄氏度)
     */
    private Double temperature;

    /**
     * 设备是否在线
     */
    private Boolean online;

    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdateTime;

    /**
     * 数据收集时间
     */
    private LocalDateTime collectTime;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendedProperties;

    /**
     * 构造函数
     */
    public DeviceMetrics() {
        this.collectTime = LocalDateTime.now();
        this.lastUpdateTime = LocalDateTime.now();
        this.online = true;
    }

    /**
     * 设置基本设备信息
     */
    public void setBasicInfo(String deviceId, String deviceName, String hostname) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.hostname = hostname;
    }

    /**
     * 设置CPU信息
     */
    public void setCpuInfo(Double usage, Integer cores, Long frequency) {
        this.cpuUsage = usage;
        this.cpuCores = cores;
        this.cpuFrequency = frequency;
    }

    /**
     * 设置内存信息
     */
    public void setMemoryInfo(Double usage, Long total, Long used, Long available) {
        this.memoryUsage = usage;
        this.totalMemory = total;
        this.usedMemory = used;
        this.availableMemory = available;
    }

    /**
     * 设置磁盘信息
     */
    public void setDiskInfo(Double usage, Long total, Long used, Long available) {
        this.diskUsage = usage;
        this.totalDisk = total;
        this.usedDisk = used;
        this.availableDisk = available;
    }

    /**
     * 设置网络信息
     */
    public void setNetworkInfo(Long inBytes, Long outBytes, Long inPackets, Long outPackets) {
        this.networkInBytes = inBytes;
        this.networkOutBytes = outBytes;
        this.networkInPackets = inPackets;
        this.networkOutPackets = outPackets;
    }

    /**
     * 更新时间戳
     */
    public void updateTimestamp() {
        this.lastUpdateTime = LocalDateTime.now();
    }

    /**
     * 检查设备是否健康
     */
    public boolean isHealthy() {
        // 简单的健康检查逻辑
        if (cpuUsage != null && cpuUsage > 95) return false;
        if (memoryUsage != null && memoryUsage > 95) return false;
        if (diskUsage != null && diskUsage > 95) return false;
        return online != null && online;
    }

    /**
     * 获取设备状态摘要
     */
    public String getStatusSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Device: ").append(deviceName != null ? deviceName : "Unknown");
        if (cpuUsage != null) {
            summary.append(", CPU: ").append(String.format("%.1f%%", cpuUsage));
        }
        if (memoryUsage != null) {
            summary.append(", Memory: ").append(String.format("%.1f%%", memoryUsage));
        }
        if (diskUsage != null) {
            summary.append(", Disk: ").append(String.format("%.1f%%", diskUsage));
        }
        summary.append(", Status: ").append(online != null && online ? "Online" : "Offline");
        return summary.toString();
    }
}
