// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.system.pojo.monitor.RedisMonitorVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Redis 监控接口
 *
 * <p>通过 INFO 命令采集 Redis 运行指标，供监控大盘展示。</p>
 *
 * @since 4.0.0.50
 * @author CH
 */
@Tag(name = "Redis 监控接口")
@RestController
@RequestMapping("/v2/redis")
@RequiredArgsConstructor
@Slf4j
public class SysRedisController {

    /**
     * stringRedisTemplate
     */
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 获取 Redis 监控信息
     *
     * @return Redis 监控信息
     */
    @Operation(summary = "获取 Redis 监控信息")
    @GetMapping("/info")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<RedisMonitorVO> info() {
        try {
            return ReturnResult.ok(buildInfo());
        } catch (Exception e) {
            log.error("获取 Redis INFO 失败", e);
            return ReturnResult.illegal("获取 Redis 信息失败: " + e.getMessage());
        }
    }

    /**
     * 采集 Redis 监控信息。
     *
     * @return Redis 监控信息
     * @throws Exception 采集失败
     */
    private RedisMonitorVO buildInfo() throws Exception {
        Properties info = stringRedisTemplate.execute(
                (org.springframework.data.redis.core.RedisCallback<Properties>) connection -> connection.info());
        if (null == info || info.isEmpty()) {
            throw new IllegalStateException("Redis INFO 返回为空");
        }

        Map<String, String> flat = new LinkedHashMap<>();
        for (String name : info.stringPropertyNames()) {
            flat.put(name, info.getProperty(name, "").trim());
        }

        try {
            Properties commandInfo = stringRedisTemplate.execute(
                    (org.springframework.data.redis.core.RedisCallback<Properties>) connection -> connection.info("commandstats"));
            if (null != commandInfo && !commandInfo.isEmpty()) {
                for (String name : commandInfo.stringPropertyNames()) {
                    if (name.startsWith("cmdstat_")) {
                        flat.put(name, commandInfo.getProperty(name, "").trim());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("获取 Redis commandstats 失败: {}", e.getMessage());
        }

        String version = flat.get("redis_version");
        String mode = flat.get("redis_mode");
        String port = flat.get("tcp_port");
        Integer connectedClients = toInt(flat.get("connected_clients"));
        Long uptimeDays = toLong(flat.get("uptime_in_days"));

        long usedBytes = toLong(flat.get("used_memory"));
        long maxBytes = toLong(flat.get("maxmemory"));
        long rssBytes = toLong(flat.get("used_memory_rss"));
        long peakBytes = toLong(flat.get("used_memory_peak"));

        String usedMemoryRss = flat.getOrDefault("used_memory_rss_human", humanReadable(rssBytes));
        String usedMemoryPeak = flat.getOrDefault("used_memory_peak_human", humanReadable(peakBytes));
        long uptimeSec = Math.max(1, toLong(flat.get("uptime_in_seconds")));
        double usedCpuSys = toDouble(flat.get("used_cpu_sys"));
        double usedCpuUser = toDouble(flat.get("used_cpu_user"));

        // keyspace
        List<RedisMonitorVO.KeyspaceDetail> keyspaceDetails = new ArrayList<>();
        long totalKeys = 0L;
        for (Map.Entry<String, String> e : flat.entrySet()) {
            if (!e.getKey().startsWith("db")) {
                continue;
            }
            long keys = 0L, expires = 0L, avgTtl = 0L;
            for (String kv : e.getValue().split(",")) {
                int eq = kv.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String k = kv.substring(0, eq);
                long v = toLong(kv.substring(eq + 1));
                switch (k) {
                    case "keys" -> keys = v;
                    case "expires" -> expires = v;
                    case "avg_ttl" -> avgTtl = v;
                    default -> { }
                }
            }
            totalKeys += keys;
            keyspaceDetails.add(new RedisMonitorVO.KeyspaceDetail(e.getKey(), keys, expires, avgTtl));
        }

        // commandstats
        List<RedisMonitorVO.CommandStat> commandStats = new ArrayList<>();
        long totalCalls = 0L;
        for (Map.Entry<String, String> e : flat.entrySet()) {
            if (!e.getKey().startsWith("cmdstat_")) {
                continue;
            }
            long calls = 0L;
            for (String kv : e.getValue().split(",")) {
                int eq = kv.indexOf('=');
                if (eq > 0 && "calls".equals(kv.substring(0, eq))) {
                    calls = toLong(kv.substring(eq + 1));
                    break;
                }
            }
            totalCalls += calls;
            commandStats.add(new RedisMonitorVO.CommandStat(
                    e.getKey().substring("cmdstat_".length()), calls, null));
        }
        commandStats.sort((a, b) -> Long.compare(b.calls() == null ? 0 : b.calls(), a.calls() == null ? 0 : a.calls()));
        List<RedisMonitorVO.CommandStat> top15 = commandStats.size() > 15
                ? new ArrayList<>(commandStats.subList(0, 15)) : commandStats;
        long finalTotalCalls = totalCalls;
        List<RedisMonitorVO.CommandStat> withPct = top15.stream()
                .map(cs -> new RedisMonitorVO.CommandStat(
                        cs.command(), cs.calls(),
                        finalTotalCalls > 0 ? round2((cs.calls() == null ? 0 : cs.calls()) * 100.0 / finalTotalCalls) : 0.0))
                .toList();

        return new RedisMonitorVO(
                version, mode, port, connectedClients, uptimeDays,
                humanReadable(usedBytes), usedBytes,
                maxBytes > 0 ? humanReadable(maxBytes) : "无限制", maxBytes,
                usedMemoryRss, rssBytes,
                usedMemoryPeak, peakBytes,
                toDoubleOrNull(flat.get("mem_fragmentation_ratio")),
                flat.get("mem_allocator"),
                round2((usedCpuSys + usedCpuUser) / uptimeSec * 100),
                "yes".equalsIgnoreCase(flat.get("aof_enabled")),
                flat.get("rdb_last_bgsave_status"),
                totalKeys,
                toDouble(flat.get("total_net_input_bytes")) / 1024.0 / Math.max(1, toLong(flat.get("uptime_in_seconds"))),
                toDouble(flat.get("total_net_output_bytes")) / 1024.0 / Math.max(1, toLong(flat.get("uptime_in_seconds"))),
                withPct,
                keyspaceDetails);
    }

    /**
     * 字节数转人类可读
     *
     * @param bytes 字节数
     * @return 人类可读字符串
     */
    private String humanReadable(long bytes) {
        if (bytes <= 0) {
            return "0 B";
        }
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        double size = bytes;
        int idx = 0;
        while (size >= 1024 && idx < units.length - 1) {
            size /= 1024.0;
            idx++;
        }
        return String.format("%.2f %s", size, units[idx]);
    }

    /**
     * toInt
     *
     * @param s 字符串
     * @return int
     */
    private Integer toInt(String s) {
        try {
            return s == null ? 0 : (int) Double.parseDouble(s);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * toLong
     *
     * @param s 字符串
     * @return long
     */
    private long toLong(String s) {
        try {
            return s == null ? 0L : (long) Double.parseDouble(s);
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * toDouble
     *
     * @param s 字符串
     * @return double
     */
    private double toDouble(String s) {
        try {
            return s == null ? 0d : Double.parseDouble(s);
        } catch (Exception e) {
            return 0d;
        }
    }

    /**
     * toDoubleOrNull
     *
     * @param s 字符串
     * @return Double, 解析失败为 null
     */
    private Double toDoubleOrNull(String s) {
        try {
            return s == null || s.isBlank() ? null : Double.parseDouble(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 保留两位小数
     *
     * @param v 原值
     * @return 保留两位
     */
    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}