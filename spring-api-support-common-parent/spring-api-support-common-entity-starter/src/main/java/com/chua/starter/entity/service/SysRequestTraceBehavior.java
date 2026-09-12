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
* 请求追踪行为分析结果表。
*
* @since 2026/03/25
* @author CH
 */
@Schema(description = "请求追踪行为分析结果表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_request_trace_behavior")
public class SysRequestTraceBehavior extends SysBase implements Serializable {
    /** 串行版本UID */
    private static final long serialVersionUID = 1L;

    @TableId(value = "sys_request_trace_behavior_id", type = IdType.AUTO)
    @Schema(description = "行为记录ID")
    /** SYS请求跟踪行为id */
    private Long sysRequestTraceBehaviorId;

    @TableField(value = "sys_request_trace_request_time")
    @Schema(description = "请求时间")
    /** SYS请求跟踪请求时间 */
    private LocalDateTime sysRequestTraceRequestTime;

    @TableField(value = "sys_request_trace_analyzed_at")
    @Schema(description = "分析时间")
    /** SYS请求跟踪分析at */
    private LocalDateTime sysRequestTraceAnalyzedAt;

    @TableField(value = "sys_request_trace_trace_id")
    @Schema(description = "追踪ID")
    /** SYS请求跟踪跟踪标识 */
    private String sysRequestTraceTraceId;

    @TableField(value = "sys_request_trace_strategy_code")
    @Schema(description = "策略编码")
    /** SYS请求跟踪策略代码 */
    private String sysRequestTraceStrategyCode;

    @TableField(value = "sys_request_trace_route")
    @Schema(description = "路由")
    /** SYS请求跟踪route */
    private String sysRequestTraceRoute;

    @TableField(value = "sys_request_trace_url")
    @Schema(description = "请求地址")
    /** SYS请求跟踪URL */
    private String sysRequestTraceUrl;

    @TableField(value = "sys_request_trace_status")
    @Schema(description = "响应状态码")
    /** SYS请求跟踪状态 */
    private Integer sysRequestTraceStatus;

    @TableField(value = "sys_request_trace_duration_ms")
    @Schema(description = "耗时毫秒")
    /** SYS请求跟踪持续时间MS */
    private Long sysRequestTraceDurationMs;

    @TableField(value = "sys_request_trace_client_ip")
    @Schema(description = "客户端IP")
    /** SYS请求跟踪客户端IP */
    private String sysRequestTraceClientIp;

    @TableField(value = "sys_request_trace_browser")
    @Schema(description = "浏览器")
    /** SYS请求跟踪browser */
    private String sysRequestTraceBrowser;

    @TableField(value = "sys_request_trace_user_id")
    @Schema(description = "用户ID")
    /** SYS请求跟踪用户标识 */
    private String sysRequestTraceUserId;

    @TableField(value = "sys_request_trace_username")
    @Schema(description = "用户名")
    /** SYS请求跟踪用户名 */
    private String sysRequestTraceUsername;

    @TableField(value = "sys_request_trace_fingerprint")
    @Schema(description = "客户端指纹")
    /** SYS请求跟踪fingerprint */
    private String sysRequestTraceFingerprint;

    @TableField(value = "sys_request_trace_user_agent")
    @Schema(description = "浏览器标识")
    /** SYS请求跟踪用户智能体 */
    private String sysRequestTraceUserAgent;

    @TableField(value = "sys_request_trace_signals")
    @Schema(description = "命中信号")
    /** SYS请求跟踪signals */
    private String sysRequestTraceSignals;

    @TableField(value = "sys_request_trace_related_request_count")
    @Schema(description = "关联请求数量")
    /** SYS请求跟踪related请求数量 */
    private Long sysRequestTraceRelatedRequestCount;

    @TableField(value = "sys_request_trace_distinct_path_count")
    @Schema(description = "扫描路径数量")
    /** SYS请求跟踪去重路径数量 */
    private Long sysRequestTraceDistinctPathCount;

    @TableField(value = "sys_request_trace_risk_level")
    @Schema(description = "风险等级")
    /** SYS请求跟踪risk级别 */
    private String sysRequestTraceRiskLevel;

    @TableField(value = "sys_request_trace_summary")
    @Schema(description = "规则摘要")
    /** SYS请求跟踪summary */
    private String sysRequestTraceSummary;

    @TableField(value = "sys_request_trace_ai_summary")
    @Schema(description = "AI摘要")
    /** SYS请求跟踪aisummary */
    private String sysRequestTraceAiSummary;

    @TableField(value = "sys_request_trace_extra")
    @Schema(description = "扩展信息")
    /** SYS请求跟踪extra */
    private String sysRequestTraceExtra;
}
