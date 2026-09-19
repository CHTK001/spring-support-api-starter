package com.chua.starter.service.configuration.trace;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 请求追踪策略配置编解码。
 *
 * @since 2026/03/25
 * @author CH
 */
public final class RequestTraceStrategyConfigCodec {

    /**
     * 创建 请求追踪strategy配置codec 实例
    */
    private RequestTraceStrategyConfigCodec() {
    }

    /**
     * 转为映射
     *
     * @param config 配置
     * @return 转为映射的结果
     */
    public static Map<String, String> toMap(RequestTraceStrategyConfig config) {
        RequestTraceStrategyConfig normalized = normalize(config);
        Map<String, String> values = new LinkedHashMap<>();
        values.put("enabled", String.valueOf(normalized.getEnabled()));
        values.put("rawStorageType", normalized.getRawStorageType());
        values.put("behaviorStorageType", normalized.getBehaviorStorageType());
        values.put("fileStoragePath", normalized.getFileStoragePath());
        values.put("aiSummaryEnabled", String.valueOf(normalized.getAiSummaryEnabled()));
        values.put("aiModel", normalized.getAiModel());
        values.put("aiRateLimitEnabled", String.valueOf(normalized.getAiRateLimitEnabled()));
        values.put("aiRateLimitWindowSeconds", String.valueOf(normalized.getAiRateLimitWindowSeconds()));
        values.put("aiRateLimitMaxRequests", String.valueOf(normalized.getAiRateLimitMaxRequests()));
        values.put("aiTimeoutMs", String.valueOf(normalized.getAiTimeoutMs()));
        values.put("retentionMinutes", String.valueOf(normalized.getRetentionMinutes()));
        values.put("maxEvents", String.valueOf(normalized.getMaxEvents()));
        values.put("defaultWindowMinutes", String.valueOf(normalized.getDefaultWindowMinutes()));
        values.put("maxWindowMinutes", String.valueOf(normalized.getMaxWindowMinutes()));
        values.put("maxTopBuckets", String.valueOf(normalized.getMaxTopBuckets()));
        values.put("maxSuspiciousEvents", String.valueOf(normalized.getMaxSuspiciousEvents()));
        values.put("burstEnabled", String.valueOf(normalized.getBurstEnabled()));
        values.put("burstWindowSeconds", String.valueOf(normalized.getBurstWindowSeconds()));
        values.put("burstThreshold", String.valueOf(normalized.getBurstThreshold()));
        values.put("hotspotEnabled", String.valueOf(normalized.getHotspotEnabled()));
        values.put("hotspotWindowMinutes", String.valueOf(normalized.getHotspotWindowMinutes()));
        values.put("hotspotThreshold", String.valueOf(normalized.getHotspotThreshold()));
        values.put("spamEnabled", String.valueOf(normalized.getSpamEnabled()));
        values.put("spamWindowSeconds", String.valueOf(normalized.getSpamWindowSeconds()));
        values.put("spamThreshold", String.valueOf(normalized.getSpamThreshold()));
        values.put("crawlerEnabled", String.valueOf(normalized.getCrawlerEnabled()));
        values.put("crawlerWindowMinutes", String.valueOf(normalized.getCrawlerWindowMinutes()));
        values.put("crawlerRequestThreshold", String.valueOf(normalized.getCrawlerRequestThreshold()));
        values.put("crawlerDistinctPathThreshold", String.valueOf(normalized.getCrawlerDistinctPathThreshold()));
        values.put("slowRequestEnabled", String.valueOf(normalized.getSlowRequestEnabled()));
        values.put("slowRequestThresholdMs", String.valueOf(normalized.getSlowRequestThresholdMs()));
        values.put("errorBurstEnabled", String.valueOf(normalized.getErrorBurstEnabled()));
        values.put("errorBurstWindowSeconds", String.valueOf(normalized.getErrorBurstWindowSeconds()));
        values.put("errorBurstThreshold", String.valueOf(normalized.getErrorBurstThreshold()));
        values.put("tokenReuseEnabled", String.valueOf(normalized.getTokenReuseEnabled()));
        values.put("tokenReuseWindowMinutes", String.valueOf(normalized.getTokenReuseWindowMinutes()));
        values.put("tokenReuseMinEvents", String.valueOf(normalized.getTokenReuseMinEvents()));
        values.put("fingerprintDriftEnabled", String.valueOf(normalized.getFingerprintDriftEnabled()));
        values.put("fingerprintDriftWindowMinutes", String.valueOf(normalized.getFingerprintDriftWindowMinutes()));
        values.put("fingerprintDriftMinEvents", String.valueOf(normalized.getFingerprintDriftMinEvents()));
        values.put("userPreferenceDriftEnabled", String.valueOf(normalized.getUserPreferenceDriftEnabled()));
        values.put("userPreferenceWindowMinutes", String.valueOf(normalized.getUserPreferenceWindowMinutes()));
        values.put("userPreferenceBaselineCount", String.valueOf(normalized.getUserPreferenceBaselineCount()));
        values.put("userPreferenceDominantCount", String.valueOf(normalized.getUserPreferenceDominantCount()));
        values.entrySet().removeIf(entry -> entry.getValue() == null);
        return values;
    }

    /**
     * 从映射
     *
     * @param values 值
     * @return 从映射的结果
     */
    public static RequestTraceStrategyConfig fromMap(Map<String, String> values) {
        RequestTraceStrategyConfig config = new RequestTraceStrategyConfig();
        if (values == null || values.isEmpty()) {
            return config.normalize();
        }
        config.setEnabled(parseBoolean(values.get("enabled"), config.getEnabled()));
        config.setRawStorageType(values.get("rawStorageType"));
        config.setBehaviorStorageType(values.get("behaviorStorageType"));
        config.setFileStoragePath(values.get("fileStoragePath"));
        config.setAiSummaryEnabled(parseBoolean(values.get("aiSummaryEnabled"), config.getAiSummaryEnabled()));
        config.setAiModel(values.get("aiModel"));
        config.setAiRateLimitEnabled(parseBoolean(values.get("aiRateLimitEnabled"), config.getAiRateLimitEnabled()));
        config.setAiRateLimitWindowSeconds(parseInteger(values.get("aiRateLimitWindowSeconds"), config.getAiRateLimitWindowSeconds()));
        config.setAiRateLimitMaxRequests(parseInteger(values.get("aiRateLimitMaxRequests"), config.getAiRateLimitMaxRequests()));
        config.setAiTimeoutMs(parseLong(values.get("aiTimeoutMs"), config.getAiTimeoutMs()));
        config.setRetentionMinutes(parseInteger(values.get("retentionMinutes"), config.getRetentionMinutes()));
        config.setMaxEvents(parseInteger(values.get("maxEvents"), config.getMaxEvents()));
        config.setDefaultWindowMinutes(parseInteger(values.get("defaultWindowMinutes"), config.getDefaultWindowMinutes()));
        config.setMaxWindowMinutes(parseInteger(values.get("maxWindowMinutes"), config.getMaxWindowMinutes()));
        config.setMaxTopBuckets(parseInteger(values.get("maxTopBuckets"), config.getMaxTopBuckets()));
        config.setMaxSuspiciousEvents(parseInteger(values.get("maxSuspiciousEvents"), config.getMaxSuspiciousEvents()));
        config.setBurstEnabled(parseBoolean(values.get("burstEnabled"), config.getBurstEnabled()));
        config.setBurstWindowSeconds(parseInteger(values.get("burstWindowSeconds"), config.getBurstWindowSeconds()));
        config.setBurstThreshold(parseInteger(values.get("burstThreshold"), config.getBurstThreshold()));
        config.setHotspotEnabled(parseBoolean(values.get("hotspotEnabled"), config.getHotspotEnabled()));
        config.setHotspotWindowMinutes(parseInteger(values.get("hotspotWindowMinutes"), config.getHotspotWindowMinutes()));
        config.setHotspotThreshold(parseInteger(values.get("hotspotThreshold"), config.getHotspotThreshold()));
        config.setSpamEnabled(parseBoolean(values.get("spamEnabled"), config.getSpamEnabled()));
        config.setSpamWindowSeconds(parseInteger(values.get("spamWindowSeconds"), config.getSpamWindowSeconds()));
        config.setSpamThreshold(parseInteger(values.get("spamThreshold"), config.getSpamThreshold()));
        config.setCrawlerEnabled(parseBoolean(values.get("crawlerEnabled"), config.getCrawlerEnabled()));
        config.setCrawlerWindowMinutes(parseInteger(values.get("crawlerWindowMinutes"), config.getCrawlerWindowMinutes()));
        config.setCrawlerRequestThreshold(parseInteger(values.get("crawlerRequestThreshold"), config.getCrawlerRequestThreshold()));
        config.setCrawlerDistinctPathThreshold(parseInteger(values.get("crawlerDistinctPathThreshold"), config.getCrawlerDistinctPathThreshold()));
        config.setSlowRequestEnabled(parseBoolean(values.get("slowRequestEnabled"), config.getSlowRequestEnabled()));
        config.setSlowRequestThresholdMs(parseLong(values.get("slowRequestThresholdMs"), config.getSlowRequestThresholdMs()));
        config.setErrorBurstEnabled(parseBoolean(values.get("errorBurstEnabled"), config.getErrorBurstEnabled()));
        config.setErrorBurstWindowSeconds(parseInteger(values.get("errorBurstWindowSeconds"), config.getErrorBurstWindowSeconds()));
        config.setErrorBurstThreshold(parseInteger(values.get("errorBurstThreshold"), config.getErrorBurstThreshold()));
        config.setTokenReuseEnabled(parseBoolean(values.get("tokenReuseEnabled"), config.getTokenReuseEnabled()));
        config.setTokenReuseWindowMinutes(parseInteger(values.get("tokenReuseWindowMinutes"), config.getTokenReuseWindowMinutes()));
        config.setTokenReuseMinEvents(parseInteger(values.get("tokenReuseMinEvents"), config.getTokenReuseMinEvents()));
        config.setFingerprintDriftEnabled(parseBoolean(values.get("fingerprintDriftEnabled"), config.getFingerprintDriftEnabled()));
        config.setFingerprintDriftWindowMinutes(parseInteger(values.get("fingerprintDriftWindowMinutes"), config.getFingerprintDriftWindowMinutes()));
        config.setFingerprintDriftMinEvents(parseInteger(values.get("fingerprintDriftMinEvents"), config.getFingerprintDriftMinEvents()));
        config.setUserPreferenceDriftEnabled(parseBoolean(values.get("userPreferenceDriftEnabled"), config.getUserPreferenceDriftEnabled()));
        config.setUserPreferenceWindowMinutes(parseInteger(values.get("userPreferenceWindowMinutes"), config.getUserPreferenceWindowMinutes()));
        config.setUserPreferenceBaselineCount(parseInteger(values.get("userPreferenceBaselineCount"), config.getUserPreferenceBaselineCount()));
        config.setUserPreferenceDominantCount(parseInteger(values.get("userPreferenceDominantCount"), config.getUserPreferenceDominantCount()));
        return config.normalize();
    }

    /**
     * Normalize
     *
     * @param config 配置
     * @return normalize的结果
     */
    public static RequestTraceStrategyConfig normalize(RequestTraceStrategyConfig config) {
        return config == null ? new RequestTraceStrategyConfig().normalize() : config.copy().normalize();
    }

    /**
     * 解析布尔值
     *
     * @param value 值
     * @param defaultValue 默认值
     * @return 解析布尔值的结果
     */
    private static Boolean parseBoolean(String value, Boolean defaultValue) {
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }

    /**
     * 解析Integer
     *
     * @param value 值
     * @param defaultValue 默认值
     * @return 解析integer的结果
     */
    private static Integer parseInteger(String value, Integer defaultValue) {
        try {
            return value == null ? defaultValue : Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    /**
     * 解析Long
     *
     * @param value 值
     * @param defaultValue 默认值
     * @return 解析long的结果
     */
    private static Long parseLong(String value, Long defaultValue) {
        try {
            return value == null ? defaultValue : Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}
