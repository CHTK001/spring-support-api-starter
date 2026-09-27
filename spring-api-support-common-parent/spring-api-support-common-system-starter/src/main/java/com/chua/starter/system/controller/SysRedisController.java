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

        // connection.info() 返回 Properties, key 为指标名, value 为指标值
        Map<String, String> flat = new LinkedHashMap<>();
        for (String name : info.stringPropertyNames()) {
            flat.put(name, info.getProperty(name, "").trim());
        }

        // INFO 默认不返回 commandstats 段（旧版 / Windows Redis），单独请求一次补齐命令统计
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

        RedisMonitorVO vo = new RedisMonitorVO();
        vo.setVersion(flat.get("redis_version"));
        vo.setMode(flat.get("redis_mode"));
        vo.setPort(flat.get("tcp_port"));
        vo.setConnectedClients(toInt(flat.get("connected_clients")));
        vo.setUptimeDays(toLong(flat.get("uptime_in_days")));

        long usedBytes = toLong(flat.get("used_memory"));
        long maxBytes = toLong(flat.get("maxmemory"));
        vo.setUsedMemoryBytes(usedBytes);
        vo.setUsedMemory(humanReadable(usedBytes));
        vo.setMaxMemoryBytes(maxBytes);
        vo.setMaxMemory(maxBytes > 0 ? humanReadable(maxBytes) : "无限制");

        // 内存 RSS / 峰值 / 碎片率 / 分配器
        long rssBytes = toLong(flat.get("used_memory_rss"));
        long peakBytes = toLong(flat.get("used_memory_peak"));
        vo.setUsedMemoryRssBytes(rssBytes);
        vo.setUsedMemoryRss(flat.getOrDefault("used_memory_rss_human", humanReadable(rssBytes)));
        vo.setUsedMemoryPeakBytes(peakBytes);
        vo.setUsedMemoryPeak(flat.getOrDefault("used_memory_peak_human", humanReadable(peakBytes)));
        vo.setMemFragmentationRatio(toDoubleOrNull(flat.get("mem_fragmentation_ratio")));
        vo.setMemAllocator(flat.get("mem_allocator"));

        // CPU: used_cpu_sys + used_cpu_user
        double usedCpuSys = toDouble(flat.get("used_cpu_sys"));
        double usedCpuUser = toDouble(flat.get("used_cpu_user"));
        long uptimeSec = Math.max(1, toLong(flat.get("uptime_in_seconds")));
        vo.setUsedCpuPercent(round2((usedCpuSys + usedCpuUser) / uptimeSec * 100));

        vo.setAofEnabled("yes".equalsIgnoreCase(flat.get("aof_enabled")));
        vo.setRdbLastStatus(flat.get("rdb_last_bgsave_status"));
        vo.setNetInputKps(toDouble(flat.get("total_net_input_bytes")) / 1024.0 / Math.max(1, toLong(flat.get("uptime_in_seconds"))));
        vo.setNetOutputKps(toDouble(flat.get("total_net_output_bytes")) / 1024.0 / Math.max(1, toLong(flat.get("uptime_in_seconds"))));

        // keyspace: db0:keys=10,expires=2,avg_ttl=3600
        List<RedisMonitorVO.KeyspaceDetail> keyspaceDetails = new ArrayList<>();
        long totalKeys = 0L;
        for (Map.Entry<String, String> e : flat.entrySet()) {
            if (!e.getKey().startsWith("db")) {
                continue;
            }
            RedisMonitorVO.KeyspaceDetail detail = new RedisMonitorVO.KeyspaceDetail();
            detail.setDb(e.getKey());
            long keys = 0L;
            for (String kv : e.getValue().split(",")) {
                int eq = kv.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String k = kv.substring(0, eq);
                long v = toLong(kv.substring(eq + 1));
                switch (k) {
                    case "keys" -> {
                        keys = v;
                        detail.setKeys(v);
                    }
                    case "expires" -> detail.setExpires(v);
                    case "avg_ttl" -> detail.setAvgTtl(v);
                    default -> { }
                }
            }
            totalKeys += keys;
            keyspaceDetails.add(detail);
        }
        vo.setKeyCount(totalKeys);
        vo.setKeyspaceDetails(keyspaceDetails);

        // commandstats: cmdstat_get:calls=N,usec=M,usec_per_call=...
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
            RedisMonitorVO.CommandStat stat = new RedisMonitorVO.CommandStat();
            stat.setCommand(e.getKey().substring("cmdstat_".length()));
            stat.setCalls(calls);
            totalCalls += calls;
            commandStats.add(stat);
        }
        commandStats.sort((a, b) -> Long.compare(b.getCalls() == null ? 0 : b.getCalls(), a.getCalls() == null ? 0 : a.getCalls()));
        if (commandStats.size() > 15) {
            commandStats = new ArrayList<>(commandStats.subList(0, 15));
        }
        for (RedisMonitorVO.CommandStat stat : commandStats) {
            stat.setPercentage(totalCalls > 0 ? round2((stat.getCalls() == null ? 0 : stat.getCalls()) * 100.0 / totalCalls) : 0.0);
        }
        vo.setCommandStats(commandStats);

        return vo;
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
