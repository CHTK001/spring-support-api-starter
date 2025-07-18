package com.chua.starter.monitor.starter.pojo;

import lombok.Data;

/**
 * 节点日志器配置统计 DTO
 * 
 * 用于封装节点日志器配置的统计信息
 *
 * @author CH
 * @since 2025/01/17
 */
@Data
public class NodeLoggerStatsDTO {

    /**
     * 节点URL（Base64编码）
     */
    private String nodeUrl;

    /**
     * 节点IP地址
     */
    private String ipAddress;

    /**
     * 节点端口
     */
    private Integer port;

    /**
     * 应用名称
     */
    private String applicationName;

    /**
     * 总日志器数量
     */
    private Integer totalLoggers;

    /**
     * 已配置等级的日志器数量
     */
    private Integer configuredLoggers;

    /**
     * 继承父级配置的日志器数量
     */
    private Integer inheritedLoggers;

    /**
     * 节点在线状态
     */
    private String nodeStatus;

    /**
     * 统计时间
     */
    private String statsTime;

    /**
     * 各等级日志器数量统计
     */
    private LevelStatsDTO levelStats;

    /**
     * 日志等级统计内部类
     */
    @Data
    public static class LevelStatsDTO {
        private Integer traceCount = 0;
        private Integer debugCount = 0;
        private Integer infoCount = 0;
        private Integer warnCount = 0;
        private Integer errorCount = 0;
        private Integer offCount = 0;
    }

    /**
     * 构造方法
     */
    public NodeLoggerStatsDTO() {
        this.totalLoggers = 0;
        this.configuredLoggers = 0;
        this.inheritedLoggers = 0;
        this.nodeStatus = "UNKNOWN";
        this.levelStats = new LevelStatsDTO();
    }

    /**
     * 构造方法
     *
     * @param nodeUrl 节点URL
     * @param totalLoggers 总日志器数量
     * @param configuredLoggers 已配置日志器数量
     * @param inheritedLoggers 继承日志器数量
     */
    public NodeLoggerStatsDTO(String nodeUrl, Integer totalLoggers, Integer configuredLoggers, Integer inheritedLoggers) {
        this();
        this.nodeUrl = nodeUrl;
        this.totalLoggers = totalLoggers;
        this.configuredLoggers = configuredLoggers;
        this.inheritedLoggers = inheritedLoggers;
    }

    /**
     * 获取配置率百分比
     *
     * @return 配置率（0-100）
     */
    public Double getConfigurationRate() {
        if (totalLoggers == null || totalLoggers == 0) {
            return 0.0;
        }
        return (configuredLoggers.doubleValue() / totalLoggers.doubleValue()) * 100;
    }

    /**
     * 获取继承率百分比
     *
     * @return 继承率（0-100）
     */
    public Double getInheritanceRate() {
        if (totalLoggers == null || totalLoggers == 0) {
            return 0.0;
        }
        return (inheritedLoggers.doubleValue() / totalLoggers.doubleValue()) * 100;
    }

    /**
     * 检查节点是否在线
     *
     * @return true 如果节点在线
     */
    public boolean isNodeOnline() {
        return "ONLINE".equals(nodeStatus);
    }

    /**
     * 获取节点地址信息
     *
     * @return IP:Port 格式的地址
     */
    public String getNodeAddress() {
        if (ipAddress != null && port != null) {
            return ipAddress + ":" + port;
        }
        return "未知";
    }

    @Override
    public String toString() {
        return "NodeLoggerStatsDTO{" +
                "nodeUrl='" + nodeUrl + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", totalLoggers=" + totalLoggers +
                ", configuredLoggers=" + configuredLoggers +
                ", inheritedLoggers=" + inheritedLoggers +
                ", nodeStatus='" + nodeStatus + '\'' +
                ", configurationRate=" + String.format("%.2f", getConfigurationRate()) + "%" +
                '}';
    }
}
