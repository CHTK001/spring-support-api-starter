package com.chua.starter.system.constant;

/**
 * 策略运行时缓存名常量。
 *
 * <p>策略的运行时投影统一由 Spring Cache 管理，业务代码不再直接拼接 Redis key。</p>
 *
 * @since 4.0.0.43
 * @author CH
 */
public final class PolicyCacheName {

    /**
     * 限流规则缓存（value = qps 字符串）
     */
    public static final String RATE_LIMIT = "sys:policy:rate-limit";

    /**
     * IP 黑名单缓存
     */
    public static final String IP_BLACKLIST = "sys:policy:ip-blacklist";

    /**
     * IP 白名单缓存
     */
    public static final String IP_WHITELIST = "sys:policy:ip-whitelist";

    /**
     * 账号黑名单缓存
     */
    public static final String ACCOUNT_BLACKLIST = "sys:policy:account-blacklist";

    /**
     * 账号白名单缓存
     */
    public static final String ACCOUNT_WHITELIST = "sys:policy:account-whitelist";

    /**
     * 接口停用 / 转发规则
     *
     * <p>key = {@code 去掉 context-path 的请求路径}，value = 规则 JSON
     * （action/status/message/target）。</p>
     */
    public static final String API_ROUTE = "sys:policy:api-route";

    /**
     * 消息头拦截规则
     *
     * <p>key = 消息头名（大小写不敏感，投影时统一转小写），value = 规则 JSON
     * （matchType/patterns/action/status/message）。</p>
     */
    public static final String HEADER = "sys:policy:header";

    /**
     * IP 缓存内部策略索引 key（value = 逗号分隔的精确 IP / CIDR）
     */
    public static final String IP_PATTERN_INDEX_KEY = "__pattern_index__";

    /**
     * 消息头缓存内部索引 key（value = 逗号分隔的、已启用规则的消息头名）
     *
     * <p>存在的意义：过滤器要决定「该检查哪些消息头」。若每请求回数据库或 Redis 扫全量，
     * 就不叫热重载了。有了这个索引，每请求只需按索引里的名字逐个读规则；
     * 索引随规则一起投影，保存即生效。</p>
     */
    public static final String HEADER_NAME_INDEX_KEY = "__header_name_index__";

    /**
     * 启用标记值
     */
    public static final String ENABLED_FLAG = "1";

    private PolicyCacheName() {
    }
}
