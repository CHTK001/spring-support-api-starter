package com.chua.starter.monitor.pojo.node;

import lombok.Data;

/**
 * 节点日志器配置 DTO
 * 
 * 用于封装从 Actuator 端点获取的日志器配置信息
 *
 * @author CH
 * @since 2025/01/17
 */
@Data
public class NodeLoggerConfigDTO {

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
     * 日志器名称
     */
    private String loggerName;

    /**
     * 配置的日志等级
     */
    private String configuredLevel;

    /**
     * 有效的日志等级
     */
    private String effectiveLevel;

    /**
     * 是否继承父级配置
     */
    private Boolean additive;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 日志器类型
     */
    private String loggerType;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 节点状态（在线/离线）
     */
    private String nodeStatus;

    /**
     * 最后检查时间
     */
    private String lastCheckTime;

    /**
     * 构造方法
     */
    public NodeLoggerConfigDTO() {
        this.enabled = true;
        this.nodeStatus = "UNKNOWN";
    }

    /**
     * 构造方法
     *
     * @param nodeUrl 节点URL
     * @param loggerName 日志器名称
     * @param configuredLevel 配置等级
     * @param effectiveLevel 有效等级
     */
    public NodeLoggerConfigDTO(String nodeUrl, String loggerName, String configuredLevel, String effectiveLevel) {
        this();
        this.nodeUrl = nodeUrl;
        this.loggerName = loggerName;
        this.configuredLevel = configuredLevel;
        this.effectiveLevel = effectiveLevel;
    }

    /**
     * 构造方法
     *
     * @param nodeUrl 节点URL
     * @param ipAddress IP地址
     * @param port 端口
     * @param applicationName 应用名称
     * @param loggerName 日志器名称
     * @param configuredLevel 配置等级
     * @param effectiveLevel 有效等级
     * @param additive 是否继承
     */
    public NodeLoggerConfigDTO(String nodeUrl, String ipAddress, Integer port, String applicationName,
                              String loggerName, String configuredLevel, String effectiveLevel, Boolean additive) {
        this();
        this.nodeUrl = nodeUrl;
        this.ipAddress = ipAddress;
        this.port = port;
        this.applicationName = applicationName;
        this.loggerName = loggerName;
        this.configuredLevel = configuredLevel;
        this.effectiveLevel = effectiveLevel;
        this.additive = additive;
    }

    /**
     * 检查是否有配置的日志等级
     *
     * @return true 如果有配置等级
     */
    public boolean hasConfiguredLevel() {
        return configuredLevel != null && !configuredLevel.trim().isEmpty();
    }

    /**
     * 检查是否继承父级配置
     *
     * @return true 如果继承父级配置
     */
    public boolean isInherited() {
        return !hasConfiguredLevel();
    }

    /**
     * 获取显示用的等级信息
     *
     * @return 显示等级
     */
    public String getDisplayLevel() {
        if (hasConfiguredLevel()) {
            return configuredLevel;
        } else {
            return effectiveLevel + " (继承)";
        }
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

    /**
     * 检查节点是否在线
     *
     * @return true 如果节点在线
     */
    public boolean isNodeOnline() {
        return "ONLINE".equals(nodeStatus);
    }

    @Override
    public String toString() {
        return "NodeLoggerConfigDTO{" +
                "nodeUrl='" + nodeUrl + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", loggerName='" + loggerName + '\'' +
                ", configuredLevel='" + configuredLevel + '\'' +
                ", effectiveLevel='" + effectiveLevel + '\'' +
                ", additive=" + additive +
                ", nodeStatus='" + nodeStatus + '\'' +
                '}';
    }
}
