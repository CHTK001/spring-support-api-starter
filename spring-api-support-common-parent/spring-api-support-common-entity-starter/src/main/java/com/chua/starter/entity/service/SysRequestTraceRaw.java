package com.chua.starter.entity.service;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 请求追踪原始记录表。
 *
 * @author CH
 * @since 2026/03/25
 */
@Schema(description = "请求追踪原始记录表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_request_trace_raw")
public class SysRequestTraceRaw extends SysBase implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "sys_request_trace_raw_id", type = IdType.AUTO)
    @Schema(description = "原始记录ID")
    private Long sysRequestTraceRawId;

    @TableField(value = "sys_request_trace_time")
    @Schema(description = "请求时间")
    private LocalDateTime sysRequestTraceTime;

    @TableField(value = "sys_request_trace_trace_id")
    @Schema(description = "追踪ID")
    private String sysRequestTraceTraceId;

    @TableField(value = "sys_request_trace_strategy_code")
    @Schema(description = "策略编码")
    private String sysRequestTraceStrategyCode;

    @TableField(value = "sys_request_trace_method")
    @Schema(description = "请求方法")
    private String sysRequestTraceMethod;

    @TableField(value = "sys_request_trace_path")
    @Schema(description = "请求路径")
    private String sysRequestTracePath;

    @TableField(value = "sys_request_trace_route")
    @Schema(description = "路由模板")
    private String sysRequestTraceRoute;

    @TableField(value = "sys_request_trace_query")
    @Schema(description = "查询参数")
    private String sysRequestTraceQuery;

    @TableField(value = "sys_request_trace_url")
    @Schema(description = "请求地址")
    private String sysRequestTraceUrl;

    @TableField(value = "sys_request_trace_status")
    @Schema(description = "响应状态码")
    private Integer sysRequestTraceStatus;

    @TableField(value = "sys_request_trace_duration_ms")
    @Schema(description = "耗时毫秒")
    private Long sysRequestTraceDurationMs;

    @TableField(value = "sys_request_trace_client_ip")
    @Schema(description = "客户端IP")
    private String sysRequestTraceClientIp;

    @TableField(value = "sys_request_trace_browser")
    @Schema(description = "浏览器")
    private String sysRequestTraceBrowser;

    @TableField(value = "sys_request_trace_user_agent")
    @Schema(description = "浏览器标识")
    private String sysRequestTraceUserAgent;

    @TableField(value = "sys_request_trace_app_name")
    @Schema(description = "应用名称")
    private String sysRequestTraceAppName;

    @TableField(value = "sys_request_trace_user_id")
    @Schema(description = "用户ID")
    private String sysRequestTraceUserId;

    @TableField(value = "sys_request_trace_username")
    @Schema(description = "用户名")
    private String sysRequestTraceUsername;

    @TableField(value = "sys_request_trace_fingerprint")
    @Schema(description = "客户端指纹")
    private String sysRequestTraceFingerprint;

    @TableField(value = "sys_request_trace_token_hash")
    @Schema(description = "令牌摘要")
    private String sysRequestTraceTokenHash;

    @TableField(value = "sys_request_trace_signals")
    @Schema(description = "命中信号")
    private String sysRequestTraceSignals;
}
