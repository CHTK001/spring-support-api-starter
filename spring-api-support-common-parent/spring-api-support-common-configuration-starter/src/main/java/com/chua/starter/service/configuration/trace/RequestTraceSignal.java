package com.chua.starter.service.configuration.trace;

/**
 * 请求追踪信号
 *
 * @since 2026/03/25
 * @author CH
 */
public enum RequestTraceSignal {

    BURST("burst", "同一来源在短时间内高频请求"),
    HOTSPOT("hotspot", "某一路由在窗口期内成为访问热点"),
    SPAM("spam", "同一主体在短时间内重复刷同一路由"),
    CRAWLER("crawler", "同一主体在窗口期内高频扫描多个路径"),
    SLOW_REQUEST("slow_request", "请求耗时超过阈值"),
    ERROR_BURST("error_burst", "同一主体在短时间内连续触发异常状态"),
    TOKEN_REUSE("token_reuse", "同一令牌在多个终端上下文中复用"),
    FINGERPRINT_DRIFT("fingerprint_drift", "同一主体在短时间内切换指纹"),
    USER_PREFERENCE_DRIFT("user_preference_drift", "当前访问行为偏离近期常用模式");

    /** 代码 */
    private final String code;
    /** 描述 */
    private final String description;

    RequestTraceSignal(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
    * 获取编码
    *
    * @return 获取编码的结果
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取Description
     *
     * @return 获取description的结果
     */
    public String getDescription() {
        return description;
    }
}
