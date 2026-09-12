package com.chua.starter.oauth.sso.support.admin;

import lombok.Data;

/**
* 限流配置
* @author CH
* @since 4.0.0
 */
@Data
public class RateLimitConfig {
    /** 是否启用限流 */
    private boolean enabled = false;
    /** 每秒允许的请求数（QPS） */
    private int qps = 100;
    /** 突发容量（令牌桶最大积累量） */
    private int burstSize = 200;
    /** 限流维度：全局 / ip / 用户 */
    private String dimension = "global";
}

