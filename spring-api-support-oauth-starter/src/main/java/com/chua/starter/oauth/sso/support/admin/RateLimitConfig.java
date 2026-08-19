package com.chua.starter.oauth.sso.support.admin;

import lombok.Data;

/**
 * 限流配置
 * @author CH
 */
@Data
public class RateLimitConfig {
    /** 是否启用限流 */
    /** 是否启用 */
    private boolean enabled = false;
    /** 每秒允许的请求数（QPS） */
    /** QPS */
    private int qps = 100;
    /** 突发容量（令牌桶最大积累量） */
    /** Burst尺寸 */
    private int burstSize = 200;
    /** 限流维度：global / ip / user */
    /** Dimension */
    private String dimension = "global";
}

