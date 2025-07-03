package com.chua.starter.monitor.starter.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 磁盘分区信息
 *
 * @author CH
 * @since 2024/12/30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiskPartitionInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分区名称
     */
    private String name;

    /**
     * 挂载点
     */
    private String mount;

    /**
     * 文件系统类型
     */
    private String type;

    /**
     * 总空间 (字节)
     */
    private Long totalSpace;

    /**
     * 可用空间 (字节)
     */
    private Long freeSpace;

    /**
     * 已用空间 (字节)
     */
    private Long usedSpace;

    /**
     * 使用率 (百分比)
     */
    private Double usagePercent;

    /**
     * 是否为主分区
     */
    private Boolean isPrimary;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 获取格式化的总空间
     */
    public String getFormattedTotalSpace() {
        return formatBytes(totalSpace);
    }

    /**
     * 获取格式化的可用空间
     */
    public String getFormattedFreeSpace() {
        return formatBytes(freeSpace);
    }

    /**
     * 获取格式化的已用空间
     */
    public String getFormattedUsedSpace() {
        return formatBytes(usedSpace);
    }

    /**
     * 格式化字节数
     */
    private String formatBytes(Long bytes) {
        if (bytes == null || bytes == 0) {
            return "0 B";
        }

        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = bytes.doubleValue();

        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        return String.format("%.1f %s", size, units[unitIndex]);
    }

    /**
     * 获取使用率状态
     */
    public String getUsageStatus() {
        if (usagePercent == null) {
            return "unknown";
        }
        
        if (usagePercent < 60) {
            return "normal";
        } else if (usagePercent < 80) {
            return "warning";
        } else {
            return "danger";
        }
    }

    /**
     * 是否使用率过高
     */
    public boolean isHighUsage() {
        return usagePercent != null && usagePercent > 80;
    }

    /**
     * 是否使用率危险
     */
    public boolean isDangerousUsage() {
        return usagePercent != null && usagePercent > 90;
    }
}
