// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.monitor.agent.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.chua.common.support.spi.annotations.Spi;
import com.chua.starter.monitor.agent.SystemMetricProperties;
import com.chua.starter.monitor.agent.SystemMetricProvider;
import com.chua.starter.monitor.agent.SystemMetricRequest;
import com.chua.starter.monitor.agent.SystemMetricResponse;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * {@code none} 数据源 —— 未检测到任何观测后端时的降级实现。
 *
 * <p><strong>为什么不是直接 404</strong>：前端页面（总览 / 链路 / 日志…）对每个接口
 * 都做 {@code .then()} 渲染，直接 404 会让 axios reject、页面报错。这里把常用接口
 * 返回「形状正确、数值为零」的空数据，页面自然渲染成空态，只有顶部 provider
 * 徽标提示「未连接观测后端」，比满屏报错更符合降级语义。</p>
 *
 * <p>接口返回一律 HTTP 200 + {@code {code:200, msg:"ok", data: ...}}，
 * 与 hotsport 后端契约一致，前端 {@code unwrap} 只取 {@code data}。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Spi(value = "none")
public class NoneSystemMetricProvider implements SystemMetricProvider {

    /** 返回数组空数据的路径片段（命中即 {@code data: []}） */
    private static final Set<String> ARRAY_PATHS = Set.of(
            "/agent/traces", "/agent/dependencies", "/services", "/endpoints",
            "/dependencies/aggregated", "/alerts", "/slow-calls", "/slow-sql",
            "/error-ranking", "/trend", "/log-stats", "/endpoint-stats",
            "/protocol-stats", "/thread-pools", "/connection-pools",
            "/agent/system-out", "/jvm/metrics", "/jvm/thread-dump",
            "/agent/trace-hotspots", "/agent/trace-phases", "/agent/skywalking/export",
            "/agent/handles"
    );

    @Override
    public String name() {
        return "none";
    }

    @Override
    public String description() {
        return "未启用观测后端（降级：接口返回空数据）";
    }

    @Override
    public boolean probe(SystemMetricProperties properties) {
        // none 是兜底数据源，恒可用
        return true;
    }

    @Override
    public SystemMetricResponse forward(SystemMetricRequest request, SystemMetricProperties properties) {
        String path = request.getPath();
        if (path == null) {
            path = "/";
        }
        // 去掉尾部斜杠，忽略查询串影响
        while (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        // SSE 实时流：给一帧零值 stats 后正常关闭，EventSource 按 retry 重连
        if ("/stream".equals(path)) {
            return SystemMetricResponse.stream("text/event-stream;charset=UTF-8", this::writeEmptyStream);
        }

        Object data = emptyData(path);
        return json(data);
    }

    /**
     * 按路径选择合理的空数据形状。
     *
     * @param path 接口路径
     * @return data 值
     */
    private Object emptyData(String path) {
        // 结构化对象：给完整零值，避免视图解引用 NPE
        if ("/stats".equals(path)) {
            return zeroStats();
        }
        if ("/health".equals(path)) {
            return zeroHealth();
        }
        if ("/sla".equals(path)) {
            return zeroSla();
        }
        if ("/system/topology".equals(path)) {
            return zeroTopology();
        }
        if ("/jvm/snapshot".equals(path)) {
            return zeroJvmSnapshot();
        }
        if ("/jvm/memory/pools".equals(path)) {
            return zeroMemoryPools();
        }
        if ("/jvm/class-loading".equals(path)) {
            return zeroClassLoading();
        }

        // 分页结构
        if ("/transmissions".equals(path) || "/leaks".equals(path) || "/logs".equals(path)) {
            JSONObject paged = new JSONObject(new LinkedHashMap<>());
            paged.set("total", 0);
            paged.set("offset", 0);
            paged.set("limit", 20);
            paged.set("items", JSONUtil.createArray());
            return paged;
        }
        if ("/dependencies".equals(path)) {
            JSONObject dep = new JSONObject(new LinkedHashMap<>());
            dep.set("total", 0);
            dep.set("items", JSONUtil.createArray());
            return dep;
        }
        if ("/agent/enabled".equals(path)) {
            JSONObject enabled = new JSONObject(new LinkedHashMap<>());
            enabled.set("enabled", false);
            return enabled;
        }

        // 其余命中数组片段 → 空数组，如 /alerts、/trend、/jvm/metrics 等列表接口
        for (String fragment : ARRAY_PATHS) {
            if (path.startsWith(fragment)) {
                return JSONUtil.createArray();
            }
        }

        // 详情类接口无数据时返回 Java null，JSON 序列化为 {"data":null}，
        // 前端按「无详情」降级；不要返回 404，否则 axios reject 会打断页面。
        return null;
    }

    /** 零值概览统计 */
    private JSONObject zeroStats() {
        JSONObject stats = new JSONObject(new LinkedHashMap<>());
        stats.set("agentActive", false);
        stats.set("agentEnabled", false);
        stats.set("transmissionCount", 0);
        stats.set("leakCount", 0);
        stats.set("errorCount", 0);
        stats.set("traceCount", 0);
        stats.set("dependencyCount", 0);
        stats.set("health", "DEGRADED");
        stats.set("qps", 0);
        stats.set("errorRate", 0);
        stats.set("activeLeaks", 0);
        stats.set("recentAlerts", JSONUtil.createArray());
        return stats;
    }

    /** 零值健康状态 */
    private JSONObject zeroHealth() {
        JSONObject health = new JSONObject(new LinkedHashMap<>());
        health.set("health", "DEGRADED");
        health.set("qps", 0);
        health.set("errorRate", 0);
        health.set("activeLeaks", 0);
        health.set("windowSeconds", 0);
        return health;
    }

    /** 零值 SLA 统计 */
    private JSONObject zeroSla() {
        JSONObject sla = new JSONObject(new LinkedHashMap<>());
        sla.set("totalCount", 0);
        sla.set("errorCount", 0);
        sla.set("successCount", 0);
        sla.set("slaPercent", 100);
        sla.set("uptimeMs", 0);
        sla.set("qps", 0);
        return sla;
    }

    /** 零值系统拓扑 */
    private JSONObject zeroTopology() {
        JSONObject topology = new JSONObject(new LinkedHashMap<>());
        topology.set("center", "");
        topology.set("jvmPid", 0);
        topology.set("modules", JSONUtil.createArray());
        topology.set("ports", JSONUtil.createArray());
        topology.set("connections", JSONUtil.createArray());
        topology.set("nodes", JSONUtil.createArray());
        topology.set("edges", JSONUtil.createArray());
        return topology;
    }

    /** 零值 JVM 快照 */
    private JSONObject zeroJvmSnapshot() {
        JSONObject snapshot = new JSONObject(new LinkedHashMap<>());
        snapshot.set("ts", 0);
        JSONObject memory = new JSONObject(new LinkedHashMap<>());
        memory.set("heapUsed", 0);
        memory.set("heapMax", 0);
        memory.set("heapCommitted", 0);
        memory.set("heapUsedPercent", 0);
        memory.set("nonHeapUsed", 0);
        snapshot.set("memory", memory);
        JSONObject gc = new JSONObject(new LinkedHashMap<>());
        gc.set("collectors", JSONUtil.createArray());
        gc.set("gcCount", 0);
        gc.set("gcTimeMs", 0);
        snapshot.set("gc", gc);
        JSONObject cpu = new JSONObject(new LinkedHashMap<>());
        cpu.set("cpuCores", 0);
        cpu.set("systemLoadAverage", 0);
        cpu.set("loadPerCore", 0);
        cpu.set("processCpuLoad", 0);
        snapshot.set("cpu", cpu);
        JSONObject threads = new JSONObject(new LinkedHashMap<>());
        threads.set("totalThreads", 0);
        threads.set("peakThreads", 0);
        threads.set("daemonThreads", 0);
        threads.set("liveThreads", 0);
        snapshot.set("threads", threads);
        snapshot.set("network", new JSONObject(new LinkedHashMap<>()));
        snapshot.set("process", new JSONObject(new LinkedHashMap<>()));
        return snapshot;
    }

    /** 零值 JVM 内存池快照 */
    private JSONObject zeroMemoryPools() {
        JSONObject snapshot = new JSONObject(new LinkedHashMap<>());
        snapshot.set("ts", 0);
        JSONObject heap = new JSONObject(new LinkedHashMap<>());
        heap.set("name", "HEAP");
        heap.set("used", 0);
        heap.set("committed", 0);
        heap.set("max", 0);
        heap.set("usedPercent", 0);
        snapshot.set("heap", heap);
        JSONObject nonHeap = new JSONObject(new LinkedHashMap<>());
        nonHeap.set("name", "NON_HEAP");
        nonHeap.set("used", 0);
        nonHeap.set("committed", 0);
        nonHeap.set("max", 0);
        nonHeap.set("usedPercent", 0);
        snapshot.set("nonHeap", nonHeap);
        snapshot.set("pools", JSONUtil.createArray());
        return snapshot;
    }

    /** 零值类加载统计 */
    private JSONObject zeroClassLoading() {
        JSONObject loading = new JSONObject(new LinkedHashMap<>());
        loading.set("loadedClassCount", 0);
        loading.set("totalLoadedClassCount", 0);
        loading.set("unloadedClassCount", 0);
        loading.set("verbose", false);
        loading.set("ts", 0);
        return loading;
    }

    /** 写一帧空 SSE after 立即关闭 */
    private void writeEmptyStream(OutputStream out) {
        try {
            JSONObject frame = new JSONObject(new LinkedHashMap<>());
            frame.set("ts", System.currentTimeMillis());
            frame.set("data", zeroStats());
            String line = "data: " + frame.toString() + "\n\n";
            out.write(line.getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (Exception ignored) {
            // 客户端断开时静默结束
        }
    }

    /** 组装统一 JSON 响应 */
    private SystemMetricResponse json(Object data) {
        JSONObject wrapper = new JSONObject(new LinkedHashMap<>());
        wrapper.set("code", 200);
        wrapper.set("msg", "ok");
        wrapper.set("data", data);
        byte[] bytes = wrapper.toString().getBytes(StandardCharsets.UTF_8);
        return SystemMetricResponse.of(200, "application/json;charset=UTF-8", bytes);
    }
}