package com.chua.starter.service.configuration.trace;

import lombok.Data;

import java.util.Locale;

/**
 * 请求追踪策略配置。
 *
 * @author CH
 * @since 2026/03/25
 */
@Data
public class RequestTraceStrategyConfig {

    private Boolean enabled = Boolean.TRUE;

    private String rawStorageType = "file";
    private String behaviorStorageType = "database";
    private String fileStoragePath = "logs/request-trace";

    /**
     * AI 分析开关与执行参数。
     */
    private Boolean aiSummaryEnabled = Boolean.TRUE;
    private String aiModel;
    private Boolean aiRateLimitEnabled = Boolean.TRUE;
    private Integer aiRateLimitWindowSeconds = 60;
    private Integer aiRateLimitMaxRequests = 30;
    private Long aiTimeoutMs = 1500L;

    private Integer retentionMinutes = 30;
    private Integer maxEvents = 3000;
    private Integer defaultWindowMinutes = 10;
    private Integer maxWindowMinutes = 60;
    private Integer maxTopBuckets = 10;
    private Integer maxSuspiciousEvents = 20;

    private Boolean burstEnabled = Boolean.TRUE;
    private Integer burstWindowSeconds = 60;
    private Integer burstThreshold = 25;

    private Boolean hotspotEnabled = Boolean.TRUE;
    private Integer hotspotWindowMinutes = 5;
    private Integer hotspotThreshold = 50;

    private Boolean spamEnabled = Boolean.TRUE;
    private Integer spamWindowSeconds = 30;
    private Integer spamThreshold = 12;

    private Boolean crawlerEnabled = Boolean.TRUE;
    private Integer crawlerWindowMinutes = 5;
    private Integer crawlerRequestThreshold = 30;
    private Integer crawlerDistinctPathThreshold = 20;

    private Boolean slowRequestEnabled = Boolean.TRUE;
    private Long slowRequestThresholdMs = 1500L;

    private Boolean errorBurstEnabled = Boolean.TRUE;
    private Integer errorBurstWindowSeconds = 60;
    private Integer errorBurstThreshold = 6;

    private Boolean tokenReuseEnabled = Boolean.TRUE;
    private Integer tokenReuseWindowMinutes = 10;
    private Integer tokenReuseMinEvents = 3;

    private Boolean fingerprintDriftEnabled = Boolean.TRUE;
    private Integer fingerprintDriftWindowMinutes = 30;
    private Integer fingerprintDriftMinEvents = 3;

    private Boolean userPreferenceDriftEnabled = Boolean.TRUE;
    private Integer userPreferenceWindowMinutes = 30;
    private Integer userPreferenceBaselineCount = 5;
    private Integer userPreferenceDominantCount = 4;

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

    private Boolean defaultBoolean(Boolean value, boolean defaultValue) {
        return value == null ? defaultValue : value;
    }

    private Integer positive(Integer value, int defaultValue) {
        return value == null || value <= 0 ? defaultValue : value;
    }

    private Long positive(Long value, long defaultValue) {
        return value == null || value <= 0 ? defaultValue : value;
    }

    private String defaultText(String value, String defaultValue) {
        String normalized = normalizeText(value);
        return normalized == null ? defaultValue : normalized;
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String text = value.trim();
        return text.isEmpty() ? null : text;
    }

    private String lowerCaseText(String value) {
        String text = normalizeText(value);
        return text == null ? null : text.toLowerCase(Locale.ROOT);
    }
}
