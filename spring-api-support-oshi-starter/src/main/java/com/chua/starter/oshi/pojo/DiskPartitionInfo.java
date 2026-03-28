package com.chua.starter.oshi.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 磁盘分区信息。
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

    private String name;
    private String mount;
    private String type;
    private Long totalSpace;
    private Long freeSpace;
    private Long usedSpace;
    private Double usagePercent;
    private Boolean isPrimary;
    private String deviceName;

    public String getFormattedTotalSpace() {
        return formatBytes(totalSpace);
    }

    public String getFormattedFreeSpace() {
        return formatBytes(freeSpace);
    }

    public String getFormattedUsedSpace() {
        return formatBytes(usedSpace);
    }

    public String getUsageStatus() {
        if (usagePercent == null) {
            return "unknown";
        }
        if (usagePercent < 60) {
            return "normal";
        }
        if (usagePercent < 80) {
            return "warning";
        }
        return "danger";
    }

    public boolean isHighUsage() {
        return usagePercent != null && usagePercent > 80;
    }

    public boolean isDangerousUsage() {
        return usagePercent != null && usagePercent > 90;
    }

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
}
