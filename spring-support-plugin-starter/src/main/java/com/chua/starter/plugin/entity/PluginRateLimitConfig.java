package com.chua.starter.plugin.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 限流配置实体（兼容类，实际使用 PluginSecurityConfig）
 * 
 * @author CH
 * @since 2025/1/16
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PluginRateLimitConfig {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 限流类型：IP、API
     */
    private LimitType limitType;

    /**
     * 限流键（IP地址或API路径）
     */
    private String limitKey;

    /**
     * 每秒允许的请求数
     */
    private Integer qps;

    /**
     * 突发容量（令牌桶算法使用）
     */
    private Integer burstCapacity;

    /**
     * 限流算法类型
     */
    private AlgorithmType algorithmType = AlgorithmType.TOKEN_BUCKET;

    /**
     * 超出限制时的处理策略
     */
    private OverflowStrategy overflowStrategy = OverflowStrategy.REJECT;

    /**
     * 时间窗口大小（秒）
     */
    private Integer windowSizeSeconds = 60;

    /**
     * 是否启用
     */
    private Boolean enabled = true;

    /**
     * 描述
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 创建者
     */
    private String createdBy;

    /**
     * 更新者
     */
    private String updatedBy;

    /**
     * 限流类型枚举
     */
    public enum LimitType {
        /**
         * IP限流
         */
        IP,
        /**
         * API路径限流
         */
        API
    }

    /**
     * 限流算法类型枚举
     */
    public enum AlgorithmType {
        /**
         * 令牌桶算法
         */
        TOKEN_BUCKET,
        /**
         * 漏桶算法
         */
        LEAKY_BUCKET,
        /**
         * 固定窗口算法
         */
        FIXED_WINDOW,
        /**
         * 滑动窗口算法
         */
        SLIDING_WINDOW
    }

    /**
     * 超出限制时的处理策略枚举
     */
    public enum OverflowStrategy {
        /**
         * 拒绝请求
         */
        REJECT,
        /**
         * 排队等待
         */
        QUEUE,
        /**
         * 降级处理
         */
        FALLBACK
    }

    /**
     * 创建时调用
     */
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdTime = now;
        updatedTime = now;
    }

    /**
     * 更新时调用
     */
    public void onUpdate() {
        updatedTime = LocalDateTime.now();
    }

    /**
     * 获取唯一键
     * 
     * @return 唯一键
     */
    public String getUniqueKey() {
        return limitType + ":" + limitKey;
    }

    /**
     * 检查配置是否有效
     * 
     * @return 是否有效
     */
    public boolean isValid() {
        return limitType != null && limitKey != null && !limitKey.trim().isEmpty() 
               && qps != null && qps > 0 && enabled != null;
    }

    /**
     * 创建IP限流配置
     * 
     * @param ipAddress IP地址
     * @param qps       每秒请求数
     * @return 限流配置
     */
    public static PluginRateLimitConfig createIpConfig(String ipAddress, Integer qps) {
        PluginRateLimitConfig config = new PluginRateLimitConfig();
        config.setLimitType(LimitType.IP);
        config.setLimitKey(ipAddress);
        config.setQps(qps);
        config.setBurstCapacity(qps * 2);
        config.setDescription("IP限流 - " + ipAddress);
        config.onCreate();
        return config;
    }

    /**
     * 创建API限流配置
     * 
     * @param apiPath API路径
     * @param qps     每秒请求数
     * @return 限流配置
     */
    public static PluginRateLimitConfig createApiConfig(String apiPath, Integer qps) {
        PluginRateLimitConfig config = new PluginRateLimitConfig();
        config.setLimitType(LimitType.API);
        config.setLimitKey(apiPath);
        config.setQps(qps);
        config.setBurstCapacity(qps * 2);
        config.setDescription("API限流 - " + apiPath);
        config.onCreate();
        return config;
    }

    /**
     * 从 RateLimit 注解的 LimitType 转换为实体的 LimitType
     */
    public static LimitType fromAnnotationLimitType(com.chua.starter.plugin.annotation.RateLimit.LimitType annotationLimitType) {
        switch (annotationLimitType) {
            case IP:
                return LimitType.IP;
            case API:
                return LimitType.API;
            default:
                return LimitType.API;
        }
    }

    /**
     * 从 RateLimit 注解的 AlgorithmType 转换为实体的 AlgorithmType
     */
    public static AlgorithmType fromAnnotationAlgorithmType(com.chua.starter.plugin.annotation.RateLimit.AlgorithmType annotationAlgorithmType) {
        switch (annotationAlgorithmType) {
            case TOKEN_BUCKET:
                return AlgorithmType.TOKEN_BUCKET;
            case LEAKY_BUCKET:
                return AlgorithmType.LEAKY_BUCKET;
            case FIXED_WINDOW:
                return AlgorithmType.FIXED_WINDOW;
            case SLIDING_WINDOW:
                return AlgorithmType.SLIDING_WINDOW;
            default:
                return AlgorithmType.TOKEN_BUCKET;
        }
    }

    /**
     * 从 RateLimit 注解的 OverflowStrategy 转换为实体的 OverflowStrategy
     */
    public static OverflowStrategy fromAnnotationOverflowStrategy(com.chua.starter.plugin.annotation.RateLimit.OverflowStrategy annotationOverflowStrategy) {
        switch (annotationOverflowStrategy) {
            case REJECT:
                return OverflowStrategy.REJECT;
            case QUEUE:
                return OverflowStrategy.QUEUE;
            case FALLBACK:
                return OverflowStrategy.FALLBACK;
            default:
                return OverflowStrategy.REJECT;
        }
    }
}
