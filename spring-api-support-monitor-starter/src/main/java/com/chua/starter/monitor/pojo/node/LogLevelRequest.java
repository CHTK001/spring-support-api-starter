package com.chua.starter.monitor.pojo.node;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 日志等级设置请求
 *
 * @author CH
 * @version 1.0.0
 * @since 2025/12/08
 */
@Data
@Schema(description = "日志等级设置请求")
public class LogLevelRequest {

    /**
     * 客户端ID（通过SyncServer下发）
     */
    @Schema(description = "客户端ID")
    private String clientId;

    /**
     * 节点IP地址（与clientId二选一）
     */
    @Schema(description = "节点IP地址")
    private String ipAddress;

    /**
     * 节点端口（与clientId二选一）
     */
    @Schema(description = "节点端口")
    private Integer port;

    /**
     * 日志器名称
     * <p>例如：com.chua.xxx、ROOT</p>
     */
    @NotBlank(message = "日志器名称不能为空")
    @Schema(description = "日志器名称，例如：com.chua.xxx、ROOT", required = true)
    private String loggerName;

    /**
     * 日志级别
     * <p>支持：TRACE, DEBUG, INFO, WARN, ERROR, OFF</p>
     */
    @NotBlank(message = "日志级别不能为空")
    @Schema(description = "日志级别: TRACE, DEBUG, INFO, WARN, ERROR, OFF", required = true)
    private String logLevel;
}
