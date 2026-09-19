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
 * 请求统计记录表
 *
 * @since 2026/08/03
 * @author CH
 */
@Schema(description = "请求统计记录表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_request_statistics")
public class SysRequestStatistics extends SysBase implements Serializable {
    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 统计标识
     */
    @TableId(value = "sys_request_statistics_id", type = IdType.AUTO)
    @Schema(description = "统计ID")
    /**
     * SYS请求统计标识
    */
    private Long sysRequestStatisticsId;

    /**
     * 统计时间
     */
    @TableField(value = "sys_request_statistics_time")
    @Schema(description = "统计时间")
    /**
     * SYS请求统计时间
    */
    private LocalDateTime sysRequestStatisticsTime;

    /**
     * 链路追踪标识
     */
    @TableField(value = "sys_request_statistics_trace_id")
    @Schema(description = "链路追踪ID")
    /**
     * SYS请求统计跟踪标识
    */
    private String sysRequestStatisticsTraceId;

    /**
     * 路由信息
     */
    @TableField(value = "sys_request_statistics_route")
    @Schema(description = "路由信息")
    /**
     * SYS请求统计route
    */
    private String sysRequestStatisticsRoute;

    /**
     * 请求方式
     */
    @TableField(value = "sys_request_statistics_method")
    @Schema(description = "请求方式")
    /**
     * SYS请求统计方法
    */
    private String sysRequestStatisticsMethod;

    /**
     * 请求路径
     */
    @TableField(value = "sys_request_statistics_path")
    @Schema(description = "请求路径")
    /**
     * SYS请求统计路径
    */
    private String sysRequestStatisticsPath;

    /**
     * 请求状态
     */
    @TableField(value = "sys_request_statistics_status")
    @Schema(description = "请求状态")
    /**
     * SYS请求统计状态
    */
    private Integer sysRequestStatisticsStatus;

    /**
     * 请求耗时（毫秒）
     */
    @TableField(value = "sys_request_statistics_duration_ms")
    @Schema(description = "请求耗时（毫秒）")
    /**
     * SYS请求统计持续时间MS
    */
    private Long sysRequestStatisticsDurationMs;

    /**
     * 客户端IP
     */
    @TableField(value = "sys_request_statistics_client_ip")
    @Schema(description = "客户端IP")
    /**
     * SYS请求统计客户端IP
    */
    private String sysRequestStatisticsClientIp;

    /**
     * 用户标识
     */
    @TableField(value = "sys_request_statistics_user_id")
    @Schema(description = "用户ID")
    /**
     * SYS请求统计用户标识
    */
    private String sysRequestStatisticsUserId;

    /**
     * 用户名
     */
    @TableField(value = "sys_request_statistics_username")
    @Schema(description = "用户名")
    /**
     * SYS请求统计用户名
    */
    private String sysRequestStatisticsUsername;

    /**
     * 设备指纹
     */
    @TableField(value = "sys_request_statistics_fingerprint")
    @Schema(description = "设备指纹")
    /**
     * SYS请求统计fingerprint
    */
    private String sysRequestStatisticsFingerprint;

    /**
     * 令牌 哈希
     */
    @TableField(value = "sys_request_statistics_token_hash")
    @Schema(description = "Token 哈希")
    /**
     * SYS请求统计令牌哈希
    */
    private String sysRequestStatisticsTokenHash;

    /**
     * 浏览器信息
     */
    @TableField(value = "sys_request_statistics_browser")
    @Schema(description = "浏览器信息")
    /**
     * SYS请求统计browser
    */
    private String sysRequestStatisticsBrowser;

    /**
     * 应用名称
     */
    @TableField(value = "sys_request_statistics_app_name")
    @Schema(description = "应用名称")
    /**
     * SYS请求统计APP名称
    */
    private String sysRequestStatisticsAppName;

    /**
     * 请求完整 URL
     */
    @TableField(value = "sys_request_statistics_request_url")
    @Schema(description = "请求完整 URL")
    /**
     * SYS请求统计请求URL
    */
    private String sysRequestStatisticsRequestUrl;

    /**
     * 请求查询参数
     */
    @TableField(value = "sys_request_statistics_request_query")
    @Schema(description = "请求查询参数")
    /**
     * SYS请求统计请求查询
    */
    private String sysRequestStatisticsRequestQuery;

    /**
     * 请求体哈希
     */
    @TableField(value = "sys_request_statistics_request_body_hash")
    @Schema(description = "请求体哈希")
    /**
     * SYS请求统计请求请求体哈希
    */
    private String sysRequestStatisticsRequestBodyHash;

    /**
     * 响应体哈希
     */
    @TableField(value = "sys_request_statistics_response_body_hash")
    @Schema(description = "响应体哈希")
    /**
     * SYS请求统计响应请求体哈希
    */
    private String sysRequestStatisticsResponseBodyHash;

    /**
     * 输入 令牌 数
     */
    @TableField(value = "sys_request_statistics_input_tokens")
    @Schema(description = "输入 Token 数")
    /**
     * SYS请求统计输入令牌
    */
    private Long sysRequestStatisticsInputTokens;

    /**
     * 输出 令牌 数
     */
    @TableField(value = "sys_request_statistics_output_tokens")
    @Schema(description = "输出 Token 数")
    /**
     * SYS请求统计输出令牌
    */
    private Long sysRequestStatisticsOutputTokens;

    /**
     * 缓存 令牌 数
     */
    @TableField(value = "sys_request_statistics_cached_tokens")
    @Schema(description = "缓存 Token 数")
    /**
     * SYS请求统计cachedtokens
    */
    private Long sysRequestStatisticsCachedTokens;

    /**
     * 令牌 总数
     */
    @TableField(value = "sys_request_statistics_total_tokens")
    @Schema(description = "Token 总数")
    /**
     * SYS请求统计总数令牌
    */
    private Long sysRequestStatisticsTotalTokens;

    /**
     * 提示字符数
     */
    @TableField(value = "sys_request_statistics_prompt_characters")
    @Schema(description = "提示字符数")
    /**
     * SYS请求统计promptcharacters
    */
    private Long sysRequestStatisticsPromptCharacters;

    /**
     * 回复字符数
     */
    @TableField(value = "sys_request_statistics_completion_characters")
    @Schema(description = "回复字符数")
    /**
     * SYS请求统计completioncharacters
    */
    private Long sysRequestStatisticsCompletionCharacters;

    /**
     * 缓存命中次数
     */
    @TableField(value = "sys_request_statistics_cache_hits")
    @Schema(description = "缓存命中次数")
    /**
     * SYS请求统计缓存hits
    */
    private Long sysRequestStatisticsCacheHits;

    /**
     * 缓存未命中次数
     */
    @TableField(value = "sys_request_statistics_cache_misses")
    @Schema(description = "缓存未命中次数")
    /**
     * SYS请求统计缓存misses
    */
    private Long sysRequestStatisticsCacheMisses;

    /**
     * 消费金额（分）
     */
    @TableField(value = "sys_request_statistics_cost_cents")
    @Schema(description = "消费金额（分）")
    /**
     * SYS请求统计costcents
    */
    private Long sysRequestStatisticsCostCents;

    /**
     * 消费货币
     */
    @TableField(value = "sys_request_statistics_cost_currency")
    @Schema(description = "消费货币")
    /**
     * SYS请求统计costcurrency
    */
    private String sysRequestStatisticsCostCurrency;

    /**
     * 模型名称
     */
    @TableField(value = "sys_request_statistics_model")
    @Schema(description = "模型名称")
    /**
     * SYS请求统计模型
    */
    private String sysRequestStatisticsModel;

    /**
     * 服务提供商
     */
    @TableField(value = "sys_request_statistics_provider")
    @Schema(description = "服务提供商")
    /**
     * SYS请求统计提供者
    */
    private String sysRequestStatisticsProvider;

    /**
     * 请求通道
     */
    @TableField(value = "sys_request_statistics_channel")
    @Schema(description = "请求通道")
    /**
     * SYS请求统计通道
    */
    private String sysRequestStatisticsChannel;

    /**
     * 状态描述
     */
    @TableField(value = "sys_request_statistics_status_text")
    @Schema(description = "状态描述")
    /**
     * SYS请求统计状态文本
    */
    private String sysRequestStatisticsStatusText;

    /**
     * 扩展信息
     */
    @TableField(value = "sys_request_statistics_extra")
    @Schema(description = "扩展信息")
    /**
     * SYS请求统计extra
    */
    private String sysRequestStatisticsExtra;
}
