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

@Schema(description = "请求统计记录表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_request_statistics")
public class SysRequestStatistics extends SysBase implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "sys_request_statistics_id", type = IdType.AUTO)
    @Schema(description = "统计ID")
    private Long sysRequestStatisticsId;

    @TableField(value = "sys_request_statistics_time")
    @Schema(description = "统计时间")
    private LocalDateTime sysRequestStatisticsTime;

    @TableField(value = "sys_request_statistics_trace_id")
    private String sysRequestStatisticsTraceId;

    @TableField(value = "sys_request_statistics_route")
    private String sysRequestStatisticsRoute;

    @TableField(value = "sys_request_statistics_method")
    private String sysRequestStatisticsMethod;

    @TableField(value = "sys_request_statistics_path")
    private String sysRequestStatisticsPath;

    @TableField(value = "sys_request_statistics_status")
    private Integer sysRequestStatisticsStatus;

    @TableField(value = "sys_request_statistics_duration_ms")
    private Long sysRequestStatisticsDurationMs;

    @TableField(value = "sys_request_statistics_client_ip")
    private String sysRequestStatisticsClientIp;

    @TableField(value = "sys_request_statistics_user_id")
    private String sysRequestStatisticsUserId;

    @TableField(value = "sys_request_statistics_username")
    private String sysRequestStatisticsUsername;

    @TableField(value = "sys_request_statistics_fingerprint")
    private String sysRequestStatisticsFingerprint;

    @TableField(value = "sys_request_statistics_token_hash")
    private String sysRequestStatisticsTokenHash;

    @TableField(value = "sys_request_statistics_browser")
    private String sysRequestStatisticsBrowser;

    @TableField(value = "sys_request_statistics_app_name")
    private String sysRequestStatisticsAppName;

    @TableField(value = "sys_request_statistics_request_url")
    private String sysRequestStatisticsRequestUrl;

    @TableField(value = "sys_request_statistics_request_query")
    private String sysRequestStatisticsRequestQuery;

    @TableField(value = "sys_request_statistics_request_body_hash")
    private String sysRequestStatisticsRequestBodyHash;

    @TableField(value = "sys_request_statistics_response_body_hash")
    private String sysRequestStatisticsResponseBodyHash;

    @TableField(value = "sys_request_statistics_input_tokens")
    private Long sysRequestStatisticsInputTokens;

    @TableField(value = "sys_request_statistics_output_tokens")
    private Long sysRequestStatisticsOutputTokens;

    @TableField(value = "sys_request_statistics_cached_tokens")
    private Long sysRequestStatisticsCachedTokens;

    @TableField(value = "sys_request_statistics_total_tokens")
    private Long sysRequestStatisticsTotalTokens;

    @TableField(value = "sys_request_statistics_prompt_characters")
    private Long sysRequestStatisticsPromptCharacters;

    @TableField(value = "sys_request_statistics_completion_characters")
    private Long sysRequestStatisticsCompletionCharacters;

    @TableField(value = "sys_request_statistics_cache_hits")
    private Long sysRequestStatisticsCacheHits;

    @TableField(value = "sys_request_statistics_cache_misses")
    private Long sysRequestStatisticsCacheMisses;

    @TableField(value = "sys_request_statistics_cost_cents")
    private Long sysRequestStatisticsCostCents;

    @TableField(value = "sys_request_statistics_cost_currency")
    private String sysRequestStatisticsCostCurrency;

    @TableField(value = "sys_request_statistics_model")
    private String sysRequestStatisticsModel;

    @TableField(value = "sys_request_statistics_provider")
    private String sysRequestStatisticsProvider;

    @TableField(value = "sys_request_statistics_channel")
    private String sysRequestStatisticsChannel;

    @TableField(value = "sys_request_statistics_status_text")
    private String sysRequestStatisticsStatusText;

    @TableField(value = "sys_request_statistics_extra")
    private String sysRequestStatisticsExtra;
}
