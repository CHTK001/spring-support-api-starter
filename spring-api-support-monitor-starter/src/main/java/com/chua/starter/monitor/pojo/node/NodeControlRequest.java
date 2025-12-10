package com.chua.starter.monitor.pojo.node;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 节点控制请求
 *
 * @author CH
 * @version 1.0.0
 * @since 2025/12/08
 */
@Data
@Schema(description = "节点控制请求")
public class NodeControlRequest {

    /**
     * 节点IP地址
     */
    @Schema(description = "节点IP地址")
    private String ipAddress;

    /**
     * 节点端口
     */
    @Schema(description = "节点端口")
    private Integer port;

    /**
     * 控制动作类型
     */
    @Schema(description = "控制动作类型: RESTART, LOG_LEVEL, LOG_VIEW, CONFIG_VIEW")
    private String action;

    /**
     * 日志器名称（用于日志配置）
     */
    @Schema(description = "日志器名称")
    private String loggerName;

    /**
     * 日志级别（用于日志配置）
     */
    @Schema(description = "日志级别: TRACE, DEBUG, INFO, WARN, ERROR, OFF")
    private String logLevel;

    /**
     * 额外参数
     */
    @Schema(description = "额外参数")
    private Map<String, Object> params;
}
