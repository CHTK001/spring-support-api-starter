package com.chua.starter.datalake.service;

import com.chua.common.support.metric.MetricsReporter;
import com.chua.datalake.support.common.BackpressureBuffer;
import com.chua.datalake.support.engine.PipelineLifecycle;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 数据中台状态上报器 — 定时通过 {@link MetricsReporter} 上报运行时指标
 * <pre>
 *  上报指标:
 *    datalake.pipelines.active       — 活跃管线数 (gauge)
 *    datalake.pipelines.total        — 总管线数 (gauge)
 *    datalake.throughput             — 当前吞吐量 (gauge)
 *    datalake.queue.usage            — 队列使用量 (gauge)
 *    datalake.queue.usage.rate       — 队列使用率 (gauge)
 *    datalake.queue.dropped          — 队列丢弃总数 (counter)
 *    datalake.errors.total           — 今日错误总数 (counter)
 *    datalake.dlq.size               — 死信队列大小 (gauge)
 *    datalake.processed.total        — 今日处理总数 (counter)
 * </pre>
 *
 * @author CH
 * @since 2026/5/28
 */
@Slf4j
public class DatalakeReporter {

    private final MetricsReporter reporter;
    private final PipelineService pipelineService;
    private final DatalakeMonitorService monitorService;
    private final BackpressureBuffer buffer;
    private final int intervalSeconds;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private ScheduledExecutorService scheduler;

    private static final String[] EMPTY_TAGS = new String[0];

    public DatalakeReporter(MetricsReporter reporter,
                            PipelineService pipelineService,
                            DatalakeMonitorService monitorService,
                            BackpressureBuffer buffer,
                            int intervalSeconds) {
        this.reporter = reporter;
        this.pipelineService = pipelineService;
        this.monitorService = monitorService;
        this.buffer = buffer;
        this.intervalSeconds = intervalSeconds;
    }

    public DatalakeReporter(MetricsReporter reporter,
                            PipelineService pipelineService,
                            DatalakeMonitorService monitorService,
                            BackpressureBuffer buffer) {
        this(reporter, pipelineService, monitorService, buffer, 30);
    }

    public void start() {
        if (!started.compareAndSet(false, true)) return;
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "datalake-reporter");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleWithFixedDelay(this::report, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
        log.info("[DatalakeReporter] 启动上报，间隔 {}s", intervalSeconds);
    }

    public void stop() {
        if (!started.compareAndSet(true, false)) return;
        if (scheduler != null) {
            scheduler.shutdown();
            scheduler = null;
        }
        log.info("[DatalakeReporter] 已停止");
    }

    private void report() {
        try {
            List<?> allPipelines = pipelineService.list();
            long activeCount = allPipelines.stream()
                    .filter(p -> pipelineService.getLifecycle(
                            p instanceof com.chua.datalake.support.engine.ScGraph g ? g.getId() : p.toString()
                    ) == PipelineLifecycle.RUNNING)
                    .count();

            var stats = monitorService.stats();

            reporter.gauge("datalake.pipelines.active", EMPTY_TAGS, activeCount);
            reporter.gauge("datalake.pipelines.total", EMPTY_TAGS, allPipelines.size());
            reporter.gauge("datalake.throughput", EMPTY_TAGS, stats.getThroughput());
            reporter.gauge("datalake.queue.usage", EMPTY_TAGS, buffer.usage());
            reporter.gauge("datalake.queue.usage.rate", EMPTY_TAGS, buffer.usageRate());
            reporter.counter("datalake.queue.dropped", EMPTY_TAGS, buffer.droppedCount());
            reporter.counter("datalake.errors.total", EMPTY_TAGS, stats.getTodayErrors());
            reporter.gauge("datalake.dlq.size", EMPTY_TAGS, stats.getDlqSize());
            reporter.counter("datalake.processed.total", EMPTY_TAGS, stats.getTodayProcessed());

            log.debug("[DatalakeReporter] 上报完成: pipelines={}/{} throughput={} queue={}%",
                    activeCount, allPipelines.size(), stats.getThroughput(),
                    Math.round(buffer.usageRate() * 100));
        } catch (Exception e) {
            log.warn("[DatalakeReporter] 上报失败", e);
        }
    }
}
