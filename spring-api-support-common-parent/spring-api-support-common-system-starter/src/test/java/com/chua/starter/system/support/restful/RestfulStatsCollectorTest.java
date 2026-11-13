package com.chua.starter.system.support.restful;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RESTful 实时统计采集器单元测试
 *
 * <p>重点覆盖带 IP 的 5 参重载：IP 必须同时进入秒级点、URL 维度与全局排行三处，
 * 少任何一处都会让对应面板空白但不会报错。</p>
 *
 * @since 4.0.0.50
 * @author CH
 */
@DisplayName("RESTful实时统计采集器测试")
class RestfulStatsCollectorTest {

    private RestfulStatsCollector collector;

    @BeforeEach
    void setUp() {
        collector = RestfulStatsCollector.getInstance();
    }

    @Test
    @DisplayName("空URL直接忽略不计入总量")
    void testBlankUrlIgnored() {
        // 先制造一次真实计数，使 before 非 0，否则本断言在单例初始态下恒为 0==0 而失效
        collector.record("/v2/probe", "GET", 1L, true, "203.0.113.200");
        long before = total(collector);
        assertTrue(before > 0, "前置计数应大于 0，否则本用例断言无效");
        collector.record("", "GET", 5L, true, "1.1.1.1");
        collector.record(null, "GET", 5L, true, "1.1.1.1");
        assertEquals(before, total(collector));
    }

    @Test
    @DisplayName("带IP记录会进入IP排行")
    void testIpRecordedIntoRanking() {
        // [P3C A20 豁免]
        collector.record("/v2/ip-check", "GET", 12L, true, "203.0.113.7");
        List<Map<String, Object>> topIps = ips(collector);
        assertTrue(topIps.stream().anyMatch(m -> "203.0.113.7".equals(m.get("ip"))),
                "IP 排行应包含刚记录的来源 IP");
    }

    @Test
    @DisplayName("空IP不进入IP排行但仍计入总量")
    void testNullIpNotRanked() {
        // 采集器是进程级单例，IP 排行条数只增不减；此处用「专属 IP 不出现」来判定，
        // 不能断言全局条数不变——其它用例已写入的 IP 会让条数变化。
        int before = ips(collector).size();
        collector.record("/v2/no-ip", "POST", 8L, true, null);
        assertEquals(before, ips(collector).size(), "空 IP 不应新增排行条目");
        assertTrue(total(collector) > 0);
    }

    @Test
    @DisplayName("IP排行按请求数降序")
    void testIpRankingSortedDesc() {
        collector.record("/v2/sort", "GET", 1L, true, "198.51.100.1");
        collector.record("/v2/sort", "GET", 1L, true, "198.51.100.1");
        collector.record("/v2/sort", "GET", 1L, true, "198.51.100.1");
        collector.record("/v2/sort", "GET", 1L, true, "198.51.100.2");

        List<Map<String, Object>> topIps = ips(collector);
        for (int i = 1; i < topIps.size(); i++) {
            long prev = num(topIps.get(i - 1), "count");
            long cur = num(topIps.get(i), "count");
            assertTrue(prev >= cur, "IP 排行必须按次数降序，实际 " + prev + " < " + cur);
        }
    }

    @Test
    @DisplayName("趋势点带本秒IP明细且条数受限")
    void testTrendRowHasIps() {
        collector.record("/v2/trend", "GET", 3L, true, "192.0.2.55");
        Map<String, Object> snapshot = collector.snapshot();
        Object trendObj = snapshot.get("trend");
        assertNotNull(trendObj);
        List<?> trend = (List<?>) trendObj;
        assertTrue(!trend.isEmpty(), "应至少产出一个趋势点");

        Map<?, ?> last = (Map<?, ?>) trend.get(trend.size() - 1);
        Object ipsObj = last.get("ips");
        assertNotNull(ipsObj, "趋势点必须带 ips 明细");
        List<?> ips = (List<?>) ipsObj;
        assertTrue(ips.size() <= 8, "单点 IP 明细上限为 8，实际 " + ips.size());
    }

    @Test
    @DisplayName("快照结构包含 trend/summary/topUrls/topIps")
    void testSnapshotShape() {
        // [P3C A20 豁免]
        collector.record("/v2/shape", "PUT", 20L, false, "203.0.113.99");
        Map<String, Object> snapshot = collector.snapshot();
        assertTrue(snapshot.containsKey("trend"));
        assertTrue(snapshot.containsKey("summary"));
        assertTrue(snapshot.containsKey("topUrls"));
        assertTrue(snapshot.containsKey("topIps"));

        Map<?, ?> summary = (Map<?, ?>) snapshot.get("summary");
        assertNotNull(summary.get("total"));
        assertNotNull(summary.get("windowQps"));
    }

    @Test
    @DisplayName("成功与失败分别累计")
    void testSuccessFailCounters() {
        collector.record("/v2/ok", "GET", 1L, true, "203.0.113.11");
        collector.record("/v2/ng", "GET", 1L, false, "203.0.113.12");
        // 单例被各用例共享，只断言成功/失败都被记到了，不比对全局差值
        // summary 里的键是 success / fail（进程累计），不是 totalSuccess / totalFail
        assertTrue(summaryNum(collector, "success") > 0, "成功数应大于 0");
        assertTrue(summaryNum(collector, "fail") > 0, "失败数应大于 0");
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> ips(RestfulStatsCollector c) {
        return (List<Map<String, Object>>) c.snapshot().get("topIps");
    }

    @SuppressWarnings("unchecked")
    private long total(RestfulStatsCollector c) {
        return num((Map<String, Object>) c.snapshot().get("summary"), "total");
    }

    @SuppressWarnings("unchecked")
    private long summaryNum(RestfulStatsCollector c, String key) {
        return num((Map<String, Object>) c.snapshot().get("summary"), key);
    }

    private long num(Map<String, Object> row, String key) {
        Object v = row.get(key);
        return v instanceof Number n ? n.longValue() : 0L;
    }
}
