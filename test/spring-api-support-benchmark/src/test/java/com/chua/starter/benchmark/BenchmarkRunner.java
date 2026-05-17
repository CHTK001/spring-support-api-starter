package com.chua.starter.benchmark;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * 轻量级基准测试运行器 — 测量指定操作的吞吐量和延迟分布。
 */
public class BenchmarkRunner {

    private int warmupIterations = 3;
    private int measureIterations = 5;
    private int concurrency = 4;
    private long durationPerIteration = 3000;

    public BenchmarkRunner warmupIterations(int n) { this.warmupIterations = n; return this; }
    public BenchmarkRunner measureIterations(int n) { this.measureIterations = n; return this; }
    public BenchmarkRunner concurrency(int n) { this.concurrency = n; return this; }
    public BenchmarkRunner durationPerIteration(long ms) { this.durationPerIteration = ms; return this; }

    public Result run(Supplier<Integer> task) throws InterruptedException {
        for (int i = 0; i < warmupIterations; i++) {
            executeRound(task, durationPerIteration);
        }
        List<RoundResult> rounds = new ArrayList<>();
        for (int i = 0; i < measureIterations; i++) {
            long start = System.nanoTime();
            RoundResult rr = executeRound(task, durationPerIteration);
            rr.elapsedNanos = System.nanoTime() - start;
            rounds.add(rr);
        }
        return aggregate(rounds);
    }

    private RoundResult executeRound(Supplier<Integer> task, long durationMs) throws InterruptedException {
        ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor();
        CountDownLatch latch = new CountDownLatch(1);
        ConcurrentLinkedQueue<Long> latencies = new ConcurrentLinkedQueue<>();
        long endTime = System.currentTimeMillis() + durationMs;
        AtomicInteger errors = new AtomicInteger(0);

        for (int i = 0; i < concurrency; i++) {
            pool.submit(() -> {
                try {
                    latch.await();
                    while (System.currentTimeMillis() < endTime) {
                        long t1 = System.nanoTime();
                        int code = task.get();
                        long t2 = System.nanoTime();
                        latencies.add((t2 - t1) / 1_000);
                        if (code < 200 || code >= 300) {
                            errors.incrementAndGet();
                        }
                    }
                } catch (Exception ignored) {
                }
            });
        }

        latch.countDown();
        pool.shutdown();
        pool.awaitTermination(durationMs + 5000, TimeUnit.MILLISECONDS);

        RoundResult rr = new RoundResult();
        rr.totalRequests = latencies.size();
        rr.errors = errors.get();
        rr.latencies = new ArrayList<>(latencies);
        rr.durationMs = durationMs;
        return rr;
    }

    private Result aggregate(List<RoundResult> rounds) {
        Result result = new Result();
        result.concurrency = concurrency;
        result.rounds = rounds.size();
        long totalRequests = 0;
        long totalErrors = 0;
        long totalDurationMs = 0;
        List<Long> allLatencies = new ArrayList<>();
        for (RoundResult rr : rounds) {
            totalRequests += rr.totalRequests;
            totalErrors += rr.errors;
            totalDurationMs += rr.durationMs;
            allLatencies.addAll(rr.latencies);
        }
        result.totalRequests = totalRequests;
        result.totalErrors = totalErrors;
        result.throughput = (double) totalRequests / totalDurationMs * 1000;
        allLatencies.sort(Long::compareTo);
        result.latencyAvg = allLatencies.stream().mapToLong(Long::longValue).average().orElse(0);
        result.latencyP50 = percentile(allLatencies, 50);
        result.latencyP90 = percentile(allLatencies, 90);
        result.latencyP99 = percentile(allLatencies, 99);
        result.latencyMax = allLatencies.isEmpty() ? 0 : allLatencies.get(allLatencies.size() - 1);
        return result;
    }

    private long percentile(List<Long> sorted, int pct) {
        if (sorted.isEmpty()) return 0;
        int idx = (int) Math.ceil(pct / 100.0 * sorted.size()) - 1;
        return sorted.get(Math.max(0, Math.min(idx, sorted.size() - 1)));
    }

    static class RoundResult {
        int totalRequests;
        int errors;
        long durationMs;
        long elapsedNanos;
        List<Long> latencies;
    }

    public static class Result {
        public int concurrency;
        public int rounds;
        public long totalRequests;
        public long totalErrors;
        public double throughput;
        public double latencyAvg;
        public long latencyP50;
        public long latencyP90;
        public long latencyP99;
        public long latencyMax;

        @Override
        public String toString() {
            return String.format("""
                            Benchmark Result (concurrency=%d, rounds=%d):
                              Requests: %d (errors: %d)
                              Throughput: %.0f ops/s
                              Latency (μs): avg=%.0f, p50=%d, p90=%d, p99=%d, max=%d""",
                    concurrency, rounds, totalRequests, totalErrors,
                    throughput, latencyAvg, latencyP50, latencyP90, latencyP99, latencyMax);
        }
    }
}
