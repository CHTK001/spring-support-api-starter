package com.chua.starter.system.support.restful;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * RESTful 实时请求统计（内存环形缓冲，方案 A 旁路采集）。
 * <p>
 * 按秒聚合请求数 / 成功失败 / 耗时，保留最近 60 个点供监控大盘趋势图使用；
 * 同时维护 URL 级累计计数供 Top 接口列表展示。不落库、不依赖 report-client。
 * </p>
 *
 * @since 4.0.0.42
 * @author CH
 */
public final class RestfulStatsCollector {

    /**
     * 单例
     */
    private static final RestfulStatsCollector INSTANCE = new RestfulStatsCollector();

    /**
     * 趋势点保留秒数
     */
    private static final int MAX_POINTS = 60;

    /**
     * Top URL 数量上限
     */
    private static final int TOP_URL_LIMIT = 20;

    /**
     * IP 排行数量上限
     */
    private static final int IP_RANK_LIMIT = 20;

    /**
     * 单个 URL 最多记录的不同来源 IP 数
     *
     * <p>无界会让 {@code UrlAccum.ipCounts} 随扫描型接口（每个 id 一个 URL）无限膨胀，
     * 故设上限；已在上限内的 IP 继续累加，只是不再引入新 IP。</p>
     */
    private static final int URL_IP_LIMIT = 10;

    /**
     * 趋势点内单个秒点最多输出的 IP 明细数
     */
    private static final int TREND_IP_LIMIT = 8;

    /**
     * IP 累计统计 Key: 来源 IP
     */
    private final ConcurrentHashMap<String, IpAccum> ipAccums = new ConcurrentHashMap<>();

    /**
     * 秒级趋势点（队首最旧）
     */
    private final Deque<SecondPoint> points = new ArrayDeque<>();

    /**
     * URL 累计统计 Key: METHOD + " " + url
     */
    private final ConcurrentHashMap<String, UrlAccum> urlAccums = new ConcurrentHashMap<>();

    /**
     * 当前秒内累计
     */
    private volatile SecondPoint current = new SecondPoint(System.currentTimeMillis() / 1000L);

    /**
     * 进程内总请求数
     */
    private final AtomicLong totalRequests = new AtomicLong();

    /**
     * 进程内成功数
     */
    private final AtomicLong totalSuccess = new AtomicLong();

    /**
     * 进程内失败数
     */
    private final AtomicLong totalFail = new AtomicLong();

    /**
     * 进程内总耗时 ms
     */
    private final AtomicLong totalDurationMs = new AtomicLong();

    private RestfulStatsCollector() {
    }

    /**
     * 获取单例
     *
     * @return collector
     */
    public static RestfulStatsCollector getInstance() {
        return INSTANCE;
    }

    /**
     * 记录一次请求完成
     *
     * @param url      归一化 URL
     * @param method   HTTP 方法
     * @param duration 耗时 ms
     * @param success  是否成功
     */
    public void record(String url, String method, long duration, boolean success) {
        record(url, method, duration, success, null);
    }

    /**
     * 记录一次请求完成（带来源 IP）
     *
     * <p>{@code ip} 会同时进入三处统计：秒级点的本秒 IP 明细（供趋势图下钻）、
     * URL 维度的来源 IP 计数（受 {@link #URL_IP_LIMIT} 限制）、以及全局 IP 排行累计。
     * {@code ip} 为空时只统计前两者的 URL/总量，不产生排行数据。</p>
     *
     * @param url      归一化 URL
     * @param method   HTTP 方法
     * @param duration 耗时 ms
     * @param success  是否成功
     * @param ip       来源 IP，可为空
     */
    public void record(String url, String method, long duration, boolean success, String ip) {
        if (url == null || url.isEmpty()) {
            return;
        }
        String m = method == null || method.isEmpty() ? "GET" : method;
        String clientIp = ip == null || ip.isEmpty() ? null : ip;
        long second = System.currentTimeMillis() / 1000L;

        synchronized (this) {
            SecondPoint c = this.current;
            if (c.second != second) {
                points.addLast(c);
                while (points.size() > MAX_POINTS) {
                    points.removeFirst();
                }
                this.current = new SecondPoint(second);
                c = this.current;
            }
            c.count++;
            c.durationSum += Math.max(0, duration);
            if (success) {
                c.success++;
            } else {
                c.fail++;
            }
            if (clientIp != null) {
                c.recordIp(clientIp);
            }
        }

        totalRequests.incrementAndGet();
        totalDurationMs.addAndGet(Math.max(0, duration));
        if (success) {
            totalSuccess.incrementAndGet();
        } else {
            totalFail.incrementAndGet();
        }

        String key = m + " " + url;
        urlAccums.computeIfAbsent(key, k -> new UrlAccum(url, m)).record(duration, success, clientIp, URL_IP_LIMIT);
        if (clientIp != null) {
            ipAccums.computeIfAbsent(clientIp, IpAccum::new).record(url, duration, success);
        }
    }

    /**
     * 快照：最近趋势点 + 汇总 + Top URL（不重置累计）
     *
     * @return RestfulStatsVO 视图 Map
     */
    public Map<String, Object> snapshot() {
        List<Map<String, Object>> trend;
        long windowCount;
        long windowSuccess;
        long windowFail;
        long windowDuration;

        synchronized (this) {
            trend = new ArrayList<>(points.size() + 1);
            for (SecondPoint p : points) {
                if (p.count <= 0) {
                    continue;
                }
                trend.add(toTrendRow(p));
            }
            SecondPoint c = this.current;
            if (c.count > 0) {
                trend.add(toTrendRow(c));
            }

            long wc = 0, ws = 0, wf = 0, wd = 0;
            for (SecondPoint p : points) {
                wc += p.count;
                ws += p.success;
                wf += p.fail;
                wd += p.durationSum;
            }
            wc += c.count;
            ws += c.success;
            wf += c.fail;
            wd += c.durationSum;
            windowCount = wc;
            windowSuccess = ws;
            windowFail = wf;
            windowDuration = wd;
        }

        List<Map<String, Object>> topUrls = new ArrayList<>();
        for (Map.Entry<String, UrlAccum> e : urlAccums.entrySet()) {
            UrlAccum a = e.getValue();
            Map<String, Object> row = new LinkedHashMap<>(10);
            row.put("key", e.getKey());
            row.put("url", a.url);
            row.put("method", a.method);
            row.put("count", a.count.get());
            row.put("success", a.success.get());
            row.put("fail", a.fail.get());
            row.put("avgMs", a.count.get() > 0 ? a.durationMs.get() / a.count.get() : 0);
            row.put("ips", topIps(a.ipCounts, URL_IP_LIMIT));
            topUrls.add(row);
        }
        topUrls.sort((x, y) -> Long.compare(((Number) y.get("count")).longValue(), ((Number) x.get("count")).longValue()));
        if (topUrls.size() > TOP_URL_LIMIT) {
            topUrls = topUrls.subList(0, TOP_URL_LIMIT);
        }

        // IP 排行（按请求数）
        List<Map<String, Object>> topIps = new ArrayList<>();
        for (IpAccum a : ipAccums.values()) {
            Map<String, Object> row = new LinkedHashMap<>(10);
            row.put("ip", a.ip);
            row.put("count", a.count.get());
            row.put("success", a.success.get());
            row.put("fail", a.fail.get());
            row.put("avgMs", a.count.get() > 0 ? a.durationMs.get() / a.count.get() : 0);
            row.put("lastTime", a.lastTime);
            PendingRow first = topOne(a.urls);
            row.put("topUrl", first == null ? "" : first.key);
            topIps.add(row);
        }
        topIps.sort((x, y) -> Long.compare(((Number) y.get("count")).longValue(), ((Number) x.get("count")).longValue()));
        if (topIps.size() > IP_RANK_LIMIT) {
            topIps = topIps.subList(0, IP_RANK_LIMIT);
        }

        Map<String, Object> summary = new LinkedHashMap<>(8);
        summary.put("total", totalRequests.get());
        summary.put("success", totalSuccess.get());
        summary.put("fail", totalFail.get());
        summary.put("avgMs", totalRequests.get() > 0 ? totalDurationMs.get() / totalRequests.get() : 0);
        summary.put("windowCount", windowCount);
        summary.put("windowSuccess", windowSuccess);
        summary.put("windowFail", windowFail);
        summary.put("windowAvgMs", windowCount > 0 ? windowDuration / windowCount : 0);
        summary.put("windowQps", windowCount);

        Map<String, Object> result = new LinkedHashMap<>(8);
        result.put("trend", trend);
        result.put("summary", summary);
        result.put("topUrls", topUrls);
        result.put("topIps", topIps);
        return result;
    }

    /** 秒级点 → 趋势行（含本秒各 IP 请求数） */
    private Map<String, Object> toTrendRow(SecondPoint p) {
        Map<String, Object> row = new LinkedHashMap<>(10);
        row.put("second", p.second);
        row.put("count", p.count);
        row.put("success", p.success);
        row.put("fail", p.fail);
        row.put("avgMs", p.count > 0 ? p.durationSum / p.count : 0);
        row.put("qps", p.count);
        // 本秒 IP 明细（按次数降序，最多 TREND_IP_LIMIT 个）
        // 不用 lambda 收集：ips 后面还要做截断赋值，一旦被 lambda 捕获就不是 effectively final
        List<Map<String, Object>> ipRows = new ArrayList<>(p.ipCounts.size());
        for (Map.Entry<String, Integer> e : p.ipCounts.entrySet()) {
            Map<String, Object> m = new LinkedHashMap<>(2);
            m.put("ip", e.getKey());
            m.put("count", e.getValue());
            ipRows.add(m);
        }
        ipRows.sort((x, y) -> Integer.compare((Integer) y.get("count"), (Integer) x.get("count")));
        row.put("ips", ipRows.size() > TREND_IP_LIMIT ? ipRows.subList(0, TREND_IP_LIMIT) : ipRows);
        return row;
    }

    /** 计数器 Map → Top N 列表（按次数降序） */
    private List<Map<String, Object>> topIps(ConcurrentHashMap<String, AtomicLong> src, int limit) {
        List<Map<String, Object>> list = new ArrayList<>(src.size());
        src.forEach((ip, n) -> {
            Map<String, Object> m = new LinkedHashMap<>(2);
            m.put("ip", ip);
            m.put("count", n.get());
            list.add(m);
        });
        list.sort((x, y) -> Long.compare(((Number) y.get("count")).longValue(), ((Number) x.get("count")).longValue()));
        return list.size() > limit ? list.subList(0, limit) : list;
    }

    /** 取计数器中次数最大的项 */
    private PendingRow topOne(ConcurrentHashMap<String, AtomicLong> src) {
        PendingRow best = null;
        for (Map.Entry<String, AtomicLong> e : src.entrySet()) {
            if (best == null || e.getValue().get() > best.count) {
                best = new PendingRow(e.getKey(), e.getValue().get());
            }
        }
        return best;
    }

    /** 计数项 */
    private static final class PendingRow {
        final String key;
        final long count;

        PendingRow(String key, long count) {
            this.key = key;
            this.count = count;
        }
    }

    /**
     * 秒级点
     */
    private static final class SecondPoint {
        /**
         * Unix 秒
         */
        final long second;
        /**
         * 请求数
         */
        long count;
        /**
         * 成功
         */
        long success;
        /**
         * 失败
         */
        long fail;
        /**
         * 耗时合计
         */
        long durationSum;
        /**
         * 本秒内各来源 IP 请求数（有界，用于 IP 趋势）
         */
        final Map<String, Integer> ipCounts = new java.util.HashMap<>(4);

        SecondPoint(long second) {
            this.second = second;
        }

        void recordIp(String ip) {
            if (ipCounts.size() < 32 || ipCounts.containsKey(ip)) {
                ipCounts.merge(ip, 1, Integer::sum);
            }
        }
    }

    /**
     * URL 累计
     */
    private static final class UrlAccum {
        /**
         * URL
         */
        final String url;
        /**
         * Method
         */
        final String method;
        /**
         * 次数
         */
        final AtomicLong count = new AtomicLong();
        /**
         * 成功
         */
        final AtomicLong success = new AtomicLong();
        /**
         * 失败
         */
        final AtomicLong fail = new AtomicLong();
        /**
         * 耗时合计
         */
        final AtomicLong durationMs = new AtomicLong();
        /**
         * 来源 IP 请求数（有界，用于 URL→IP 明细）
         */
        final ConcurrentHashMap<String, AtomicLong> ipCounts = new ConcurrentHashMap<>();

        UrlAccum(String url, String method) {
            this.url = url;
            this.method = method;
        }

        void record(long duration, boolean ok) {
            record(duration, ok, null, 0);
        }

        void record(long duration, boolean ok, String ip, int ipLimit) {
            count.incrementAndGet();
            durationMs.addAndGet(Math.max(0, duration));
            if (ok) {
                success.incrementAndGet();
            } else {
                fail.incrementAndGet();
            }
            if (ip != null && !ip.isEmpty() && (ipCounts.size() < ipLimit || ipCounts.containsKey(ip))) {
                ipCounts.computeIfAbsent(ip, k -> new AtomicLong()).incrementAndGet();
            }
        }
    }

    /**
     * IP 累计
     */
    private static final class IpAccum {
        /**
         * IP
         */
        final String ip;
        /**
         * 该 IP 总请求数
         */
        final AtomicLong count = new AtomicLong();
        /**
         * 成功
         */
        final AtomicLong success = new AtomicLong();
        /**
         * 失败
         */
        final AtomicLong fail = new AtomicLong();
        /**
         * 耗时合计
         */
        final AtomicLong durationMs = new AtomicLong();
        /**
         * 最近访问 URL（有界）
         */
        final ConcurrentHashMap<String, AtomicLong> urls = new ConcurrentHashMap<>();
        /**
         * 最近访问时间
         */
        volatile long lastTime;

        IpAccum(String ip) {
            this.ip = ip;
        }

        void record(String url, long duration, boolean ok) {
            count.incrementAndGet();
            durationMs.addAndGet(Math.max(0, duration));
            if (ok) {
                success.incrementAndGet();
            } else {
                fail.incrementAndGet();
            }
            lastTime = System.currentTimeMillis();
            if (url != null && !url.isEmpty() && (urls.size() < 10 || urls.containsKey(url))) {
                urls.computeIfAbsent(url, k -> new AtomicLong()).incrementAndGet();
            }
        }
    }
}
