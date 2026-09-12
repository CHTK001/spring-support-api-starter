package com.chua.starter.service.configuration.trace;

import lombok.Data;

import java.util.Locale;

/**
 * 请求追踪策略配置。
 *
 * @since 2026/03/25
 * @author CH
 */
@Data
public class RequestTraceStrategyConfig {

    /** 是否启用 */
    private Boolean enabled = Boolean.TRUE;

    /** RAW存储类型 */
    private String rawStorageType = "file";
    /** 行为存储类型 */
    private String behaviorStorageType = "database";
    /** 文件存储路径 */
    private String fileStoragePath = "logs/request-trace";

    /**
     * AI 分析开关与执行参数。
     */
    private Boolean aiSummaryEnabled = Boolean.TRUE;
    /** AI模型 */
    private String aiModel;
    /** AI比率限制是否启用 */
    private Boolean aiRateLimitEnabled = Boolean.TRUE;
    /** AI比率限制窗口秒 */
    private Integer aiRateLimitWindowSeconds = 60;
    /** AI比率限制最大值Requests */
    private Integer aiRateLimitMaxRequests = 30;
    /** AI超时MS */
    private Long aiTimeoutMs = 1500L;

    /** Retentionminutes */
    private Integer retentionMinutes = 30;
    /** 最大值事件 */
    private Integer maxEvents = 3000;
    /** 默认windowminutes */
    private Integer defaultWindowMinutes = 10;
    /** 最大值windowminutes */
    private Integer maxWindowMinutes = 60;
    /** 最大值顶部buckets */
    private Integer maxTopBuckets = 10;
    /** 最大值suspiciousevents */
    private Integer maxSuspiciousEvents = 20;

    /** Burst是否启用 */
    private Boolean burstEnabled = Boolean.TRUE;
    /** Burstwindow秒 */
    private Integer burstWindowSeconds = 60;
    /** Burst阈值 */
    private Integer burstThreshold = 25;

    /** Hotspot是否启用 */
    private Boolean hotspotEnabled = Boolean.TRUE;
    /** Hotspotwindowminutes */
    private Integer hotspotWindowMinutes = 5;
    /** Hotspot阈值 */
    private Integer hotspotThreshold = 50;

    /** Spam是否启用 */
    private Boolean spamEnabled = Boolean.TRUE;
    /** Spamwindow秒 */
    private Integer spamWindowSeconds = 30;
    /** Spam阈值 */
    private Integer spamThreshold = 12;

    /** 爬虫是否启用 */
    private Boolean crawlerEnabled = Boolean.TRUE;
    /** Crawlerwindowminutes */
    private Integer crawlerWindowMinutes = 5;
    /** 爬虫请求阈值 */
    private Integer crawlerRequestThreshold = 30;
    /** Crawlerdistinct路径阈值 */
    private Integer crawlerDistinctPathThreshold = 20;

    /** Slow请求是否启用 */
    private Boolean slowRequestEnabled = Boolean.TRUE;
    /** Slow请求阈值MS */
    private Long slowRequestThresholdMs = 1500L;

    /** 错误burst是否启用 */
    private Boolean errorBurstEnabled = Boolean.TRUE;
    /** 错误burstwindow秒 */
    private Integer errorBurstWindowSeconds = 60;
    /** 错误burst阈值 */
    private Integer errorBurstThreshold = 6;

    /** 令牌reuse是否启用 */
    private Boolean tokenReuseEnabled = Boolean.TRUE;
    /** 令牌reusewindowminutes */
    private Integer tokenReuseWindowMinutes = 10;
    /** 令牌reuse最小值事件 */
    private Integer tokenReuseMinEvents = 3;

    /** Fingerprintdrift是否启用 */
    private Boolean fingerprintDriftEnabled = Boolean.TRUE;
    /** Fingerprintdriftwindowminutes */
    private Integer fingerprintDriftWindowMinutes = 30;
    /** Fingerprintdrift最小值事件 */
    private Integer fingerprintDriftMinEvents = 3;

    /** 用户preferencedrift是否启用 */
    private Boolean userPreferenceDriftEnabled = Boolean.TRUE;
    /** 用户preferencewindowminutes */
    private Integer userPreferenceWindowMinutes = 30;
    /** 用户preferencebaseline数量 */
    private Integer userPreferenceBaselineCount = 5;
    /** 用户preferencedominant数量 */
    private Integer userPreferenceDominantCount = 4;

    /**
     * 复制
     *
     * @return 副本的结果
     */
    public RequestTraceStrategyConfig copy() {
        RequestTraceStrategyConfig target = new RequestTraceStrategyConfig();
        target.enabled = enabled;
        target.rawStorageType = rawStorageType;
        target.behaviorStorageType = behaviorStorageType;
        target.fileStoragePath = fileStoragePath;
        target.aiSummaryEnabled = aiSummaryEnabled;
        target.aiModel = aiModel;
        target.aiRateLimitEnabled = aiRateLimitEnabled;
        target.aiRateLimitWindowSeconds = aiRateLimitWindowSeconds;
        target.aiRateLimitMaxRequests = aiRateLimitMaxRequests;
        target.aiTimeoutMs = aiTimeoutMs;
        target.retentionMinutes = retentionMinutes;
        target.maxEvents = maxEvents;
        target.defaultWindowMinutes = defaultWindowMinutes;
        target.maxWindowMinutes = maxWindowMinutes;
        target.maxTopBuckets = maxTopBuckets;
        target.maxSuspiciousEvents = maxSuspiciousEvents;
        target.burstEnabled = burstEnabled;
        target.burstWindowSeconds = burstWindowSeconds;
        target.burstThreshold = burstThreshold;
        target.hotspotEnabled = hotspotEnabled;
        target.hotspotWindowMinutes = hotspotWindowMinutes;
        target.hotspotThreshold = hotspotThreshold;
        target.spamEnabled = spamEnabled;
        target.spamWindowSeconds = spamWindowSeconds;
        target.spamThreshold = spamThreshold;
        target.crawlerEnabled = crawlerEnabled;
        target.crawlerWindowMinutes = crawlerWindowMinutes;
        target.crawlerRequestThreshold = crawlerRequestThreshold;
        target.crawlerDistinctPathThreshold = crawlerDistinctPathThreshold;
        target.slowRequestEnabled = slowRequestEnabled;
        target.slowRequestThresholdMs = slowRequestThresholdMs;
        target.errorBurstEnabled = errorBurstEnabled;
        target.errorBurstWindowSeconds = errorBurstWindowSeconds;
        target.errorBurstThreshold = errorBurstThreshold;
        target.tokenReuseEnabled = tokenReuseEnabled;
        target.tokenReuseWindowMinutes = tokenReuseWindowMinutes;
        target.tokenReuseMinEvents = tokenReuseMinEvents;
        target.fingerprintDriftEnabled = fingerprintDriftEnabled;
        target.fingerprintDriftWindowMinutes = fingerprintDriftWindowMinutes;
        target.fingerprintDriftMinEvents = fingerprintDriftMinEvents;
        target.userPreferenceDriftEnabled = userPreferenceDriftEnabled;
        target.userPreferenceWindowMinutes = userPreferenceWindowMinutes;
        target.userPreferenceBaselineCount = userPreferenceBaselineCount;
        target.userPreferenceDominantCount = userPreferenceDominantCount;
        return target;
    }

    /**
     * Normalize
     *
     * @return normalize的结果
     */
    public RequestTraceStrategyConfig normalize() {
        enabled = defaultBoolean(enabled, true);
        rawStorageType = lowerCaseText(defaultText(rawStorageType, "file"));
        behaviorStorageType = lowerCaseText(defaultText(behaviorStorageType, "database"));
        fileStoragePath = defaultText(fileStoragePath, "logs/request-trace");
        aiSummaryEnabled = defaultBoolean(aiSummaryEnabled, true);
        aiModel = lowerCaseText(normalizeText(aiModel));
        aiRateLimitEnabled = defaultBoolean(aiRateLimitEnabled, true);
        aiRateLimitWindowSeconds = positive(aiRateLimitWindowSeconds, 60);
        aiRateLimitMaxRequests = positive(aiRateLimitMaxRequests, 30);
        aiTimeoutMs = positive(aiTimeoutMs, 1500L);

        retentionMinutes = positive(retentionMinutes, 30);
        maxEvents = positive(maxEvents, 3000);
        defaultWindowMinutes = positive(defaultWindowMinutes, 10);
        maxWindowMinutes = positive(maxWindowMinutes, 60);
        maxTopBuckets = positive(maxTopBuckets, 10);
        maxSuspiciousEvents = positive(maxSuspiciousEvents, 20);

        burstEnabled = defaultBoolean(burstEnabled, true);
        burstWindowSeconds = positive(burstWindowSeconds, 60);
        burstThreshold = positive(burstThreshold, 25);

        hotspotEnabled = defaultBoolean(hotspotEnabled, true);
        hotspotWindowMinutes = positive(hotspotWindowMinutes, 5);
        hotspotThreshold = positive(hotspotThreshold, 50);

        spamEnabled = defaultBoolean(spamEnabled, true);
        spamWindowSeconds = positive(spamWindowSeconds, 30);
        spamThreshold = positive(spamThreshold, 12);

        crawlerEnabled = defaultBoolean(crawlerEnabled, true);
        crawlerWindowMinutes = positive(crawlerWindowMinutes, 5);
        crawlerRequestThreshold = positive(crawlerRequestThreshold, 30);
        crawlerDistinctPathThreshold = positive(crawlerDistinctPathThreshold, 20);

        slowRequestEnabled = defaultBoolean(slowRequestEnabled, true);
        slowRequestThresholdMs = positive(slowRequestThresholdMs, 1500L);

        errorBurstEnabled = defaultBoolean(errorBurstEnabled, true);
        errorBurstWindowSeconds = positive(errorBurstWindowSeconds, 60);
        errorBurstThreshold = positive(errorBurstThreshold, 6);

        tokenReuseEnabled = defaultBoolean(tokenReuseEnabled, true);
        tokenReuseWindowMinutes = positive(tokenReuseWindowMinutes, 10);
        tokenReuseMinEvents = positive(tokenReuseMinEvents, 3);

        fingerprintDriftEnabled = defaultBoolean(fingerprintDriftEnabled, true);
        fingerprintDriftWindowMinutes = positive(fingerprintDriftWindowMinutes, 30);
        fingerprintDriftMinEvents = positive(fingerprintDriftMinEvents, 3);

        userPreferenceDriftEnabled = defaultBoolean(userPreferenceDriftEnabled, true);
        userPreferenceWindowMinutes = positive(userPreferenceWindowMinutes, 30);
        userPreferenceBaselineCount = positive(userPreferenceBaselineCount, 5);
        userPreferenceDominantCount = positive(userPreferenceDominantCount, 4);
        return this;
    }

    /**
     * 默认布尔值
     *
     * @param value 值
     * @param defaultValue 默认值
     * @return 默认布尔值的结果
     */
    private Boolean defaultBoolean(Boolean value, boolean defaultValue) {
        return value == null ? defaultValue : value;
    }

    /**
     * Positive
     *
     * @param value 值
     * @param defaultValue 默认值
     * @return positive的结果
     */
    private Integer positive(Integer value, int defaultValue) {
        return value == null || value <= 0 ? defaultValue : value;
    }

    /**
     * Positive
     *
     * @param value 值
     * @param defaultValue 默认值
     * @return positive的结果
     */
    private Long positive(Long value, long defaultValue) {
        return value == null || value <= 0 ? defaultValue : value;
    }

    /**
     * 默认文本
     *
     * @param value 值
     * @param defaultValue 默认值
     * @return 默认文本的结果
     */
    private String defaultText(String value, String defaultValue) {
        String normalized = normalizeText(value);
        return normalized == null ? defaultValue : normalized;
    }

    /**
     * normalize文本
     *
     * @param value 值
     * @return normalize文本的结果
     */
    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String text = value.trim();
        return text.isEmpty() ? null : text;
    }

    /**
     * 降低大小写文本
     *
     * @param value 值
     * @return 降低大小写文本的结果
     */
    private String lowerCaseText(String value) {
        String text = normalizeText(value);
        return text == null ? null : text.toLowerCase(Locale.ROOT);
    }
}
