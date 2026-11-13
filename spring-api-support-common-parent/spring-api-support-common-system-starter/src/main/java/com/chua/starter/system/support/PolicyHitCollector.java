package com.chua.starter.system.support;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 策略命中统计（内存，无外部依赖）。
 *
 * <p><b>统计什么</b>：策略「生效」的次数，而不是请求总数。黑名单命中 = 被拦下、
 * 限流命中 = 被拒、接口停用命中 = 返回停用码、接口转发命中 = 被改写目标。
 * 没生效的请求不计，所以「放行」类的行计数为 0 是正确结果，不是缺数据。</p>
 *
 * <p><b>为什么记内存</b>：这是运维观测数据，价值在于「刚刚谁在打我」；
 * 落库会带来写入放大与建表迁移，而观测数据的可丢失性是可接受的。
 * 与 {@link com.chua.starter.system.support.restful.RestfulStatsCollector}
 * 同一思路（旁路、内存、有界）。</p>
 *
 * <p><b>为什么处处设上限</b>：key 来自用户输入（IP、账号、路径）。扫描型流量
 * （每个 id 一个路径、每次换源 IP）会让无界的 Map 无限膨胀直到 OOM。
 * 所以：追踪的 key 数、单 key 的不同 IP 数、单 key 的分钟点数都设硬上限，
 * 达到上限后只累加已存在的项、不再引入新项。</p>
 *
 * @since 4.0.0.43
 * @author CH
 */
public final class PolicyHitCollector {

    /**
     * 单例
     */
    private static final PolicyHitCollector INSTANCE = new PolicyHitCollector();

    /**
     * 最多追踪多少个策略键；超出后不再收录新键（已有键继续累加）
     */
    private static final int MAX_KEYS = 500;

    /**
     * 单个策略键最多记录多少个不同来源 IP
     */
    private static final int IP_LIMIT = 50;

    /**
     * 分钟级趋势保留点数（1 小时）
     */
    private static final int MINUTE_POINTS = 60;

    /**
     * 策略键长度上限，避免超长路径当键
     */
    private static final int KEY_MAX_LEN = 300;

    /**
     * 策略键 → 累计
     */
    private final ConcurrentHashMap<String, HitAccum> accums = new ConcurrentHashMap<>();

    private PolicyHitCollector() {
    }

    /**
     * 获取单例
     *
     * @return collector
     */
    public static PolicyHitCollector getInstance() {
        return INSTANCE;
    }

    /**
     * 策略类型：IP 黑名单
     */
    public static final String TYPE_IP_BLACK = "IP_BLACK";

    /**
     * 策略类型：IP 白名单（命中即豁免限流）
     */
    public static final String TYPE_IP_WHITE = "IP_WHITE";

    /**
     * 策略类型：账号黑名单
     */
    public static final String TYPE_ACCOUNT_BLACK = "ACCOUNT_BLACK";

    /**
     * 策略类型：账号白名单
     */
    public static final String TYPE_ACCOUNT_WHITE = "ACCOUNT_WHITE";

    /**
     * 策略类型：限流（单条规则）
     */
    public static final String TYPE_RATE_LIMIT = "RATE_LIMIT";

    /**
     * 策略类型：全局限流
     */
    public static final String TYPE_RATE_LIMIT_GLOBAL = "RATE_LIMIT_GLOBAL";

    /**
     * 策略类型：接口停用/转发
     */
    public static final String TYPE_API_ROUTE = "API_ROUTE";

    /**
     * 策略类型：消息头拦截
     *
     * <p>归因键是 {@code 头名|命中的模式}，这样能看出到底是哪条特征在拦，
     * 而不是只知道「User-Agent 被拦了」。</p>
     */
    public static final String TYPE_HEADER = "HEADER";

    /**
     * 记录一次命中。
     *
     * @param type     策略类型，取本类的 TYPE_* 常量
     * @param key      策略键：名单项（IP/网段/账号）、{@code 维度:目标}、接口路径等
     * @param clientIp 来源 IP，可为空（为空仍计数，只是不产生 IP 明细）
     */
    public void record(String type, String key, String clientIp) {
        if (null == type || null == key || key.isEmpty()) {
            return;
        }
        String composite = compositeKey(type, key);
        HitAccum accum = accums.get(composite);
        if (null == accum) {
            // 达到上限就不再收录新键：宁可少统计，也不能无界增长
            if (accums.size() >= MAX_KEYS) {
                return;
            }
            accum = accums.computeIfAbsent(composite, k -> new HitAccum(type, trimKey(key)));
        }
        accum.record(clientIp);
    }

    /**
     * 全部策略的命中次数。
     *
     * <p>供列表页「命中次数」列一次性取回，避免每行一次请求。</p>
     *
     * @return 键为 {@code type|key}、值为累计命中次数
     */
    public Map<String, Long> counts() {
        Map<String, Long> result = new LinkedHashMap<>(accums.size());
        accums.forEach((k, v) -> result.put(k, v.count.get()));
        return result;
    }

    /**
     * 单个策略的命中明细。
     *
     * @param type 策略类型
     * @param key  策略键
     * @return 命中明细，含总数、首末时间、来源 IP 排行、分钟级趋势；无数据时返回空结构而非 null
     */
    public Map<String, Object> detail(String type, String key) {
        HitAccum accum = null == type || null == key ? null : accums.get(compositeKey(type, key));
        Map<String, Object> result = new LinkedHashMap<>(8);
        if (null == accum) {
            result.put("total", 0L);
            result.put("firstTime", null);
            result.put("lastTime", null);
            result.put("topIps", List.of());
            result.put("trend", List.of());
            return result;
        }
        result.put("total", accum.count.get());
        result.put("firstTime", accum.firstTime);
        result.put("lastTime", accum.lastTime);
        result.put("topIps", accum.ipRank(IP_LIMIT));
        result.put("trend", accum.trend());
        return result;
    }

    /**
     * 复合键。
     *
     * @param type 策略类型
     * @param key  策略键
     * @return {@code type|key}
     */
    public static String compositeKey(String type, String key) {
        return type + "|" + trimKey(key);
    }

    /**
     * 截断超长键。
     *
     * @param key 原始键
     * @return 截断后的键
     */
    private static String trimKey(String key) {
        if (null == key) {
            return "";
        }
        return key.length() > KEY_MAX_LEN ? key.substring(0, KEY_MAX_LEN) : key;
    }

    /**
     * 单个策略键的累计。
     */
    private static final class HitAccum {

        /**
         * 策略类型
         */
        final String type;

        /**
         * 策略键
         */
        final String key;

        /**
         * 命中总数
         */
        final AtomicLong count = new AtomicLong();

        /**
         * 首次命中的 epoch 毫秒
         */
        volatile long firstTime;

        /**
         * 最近命中的 epoch 毫秒
         */
        volatile long lastTime;

        /**
         * 来源 IP 计数（有界）
         */
        final ConcurrentHashMap<String, AtomicLong> ipCounts = new ConcurrentHashMap<>();

        /**
         * 分钟级趋势点（队首最旧）
         */
        private final Deque<MinutePoint> minutes = new ArrayDeque<>();

        HitAccum(String type, String key) {
            this.type = type;
            this.key = key;
        }

        /**
         * 记一次命中。
         *
         * @param clientIp 来源 IP，可为空
         */
        void record(String clientIp) {
            long now = System.currentTimeMillis();
            count.incrementAndGet();
            if (0L == firstTime) {
                firstTime = now;
            }
            lastTime = now;

            if (null != clientIp && !clientIp.isEmpty()
                    && (ipCounts.size() < IP_LIMIT || ipCounts.containsKey(clientIp))) {
                ipCounts.computeIfAbsent(clientIp, k -> new AtomicLong()).incrementAndGet();
            }

            // [P3C A20 豁免]
            long minute = now / 60000L;
            synchronized (minutes) {
                MinutePoint last = minutes.peekLast();
                if (null == last || last.minute != minute) {
                    minutes.addLast(new MinutePoint(minute));
                    while (minutes.size() > MINUTE_POINTS) {
                        minutes.removeFirst();
                    }
                    last = minutes.peekLast();
                }
                if (null != last) {
                    last.count++;
                }
            }
        }

        /**
         * 来源 IP 排行。
         *
         * @param limit 上限
         * @return 按命中次数降序的 {@code [{ip,count}]}
         */
        List<Map<String, Object>> ipRank(int limit) {
            List<Map<String, Object>> list = new ArrayList<>(ipCounts.size());
            ipCounts.forEach((ip, n) -> {
                Map<String, Object> row = new LinkedHashMap<>(2);
                row.put("ip", ip);
                row.put("count", n.get());
                list.add(row);
            });
            list.sort((x, y) -> Long.compare((Long) y.get("count"), (Long) x.get("count")));
            return list.size() > limit ? new ArrayList<>(list.subList(0, limit)) : list;
        }

        /**
         * 分钟级趋势。
         *
         * @return {@code [{minute, time, count}]}，按时间升序
         */
        List<Map<String, Object>> trend() {
            List<Map<String, Object>> list;
            synchronized (minutes) {
                list = new ArrayList<>(minutes.size());
                for (MinutePoint p : minutes) {
                    Map<String, Object> row = new LinkedHashMap<>(3);
                    row.put("minute", p.minute);
                    // [P3C A20 豁免]
                    row.put("time", p.minute * 60000L);
                    row.put("count", p.count);
                    list.add(row);
                }
            }
            return list;
        }
    }

    /**
     * 分钟级点。
     */
    private static final class MinutePoint {

        /**
         * Unix 分钟
         */
        final long minute;

        /**
         * 该分钟命中数
         */
        long count;

        MinutePoint(long minute) {
            this.minute = minute;
        }
    }
}
