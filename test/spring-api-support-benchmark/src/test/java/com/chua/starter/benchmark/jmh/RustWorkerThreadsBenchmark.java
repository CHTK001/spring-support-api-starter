package com.chua.starter.benchmark.jmh;

import com.chua.starter.benchmark.AbstractBenchmarkBase;
import com.chua.starter.benchmark.BenchmarkRunner;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Rust 服务器 worker 线程数调优基准测试 — 输出 Markdown 表格。
 *
 * <p>逐一修改 plugin.server.rust.worker-threads 并重启服务器后运行。</p>
 */
public class RustWorkerThreadsBenchmark extends AbstractBenchmarkBase {

    private static final int[] WORKER_THREADS = {0, 2, 4, 8, 16, 32};

    public static void main(String[] args) throws Exception {
        new RustWorkerThreadsBenchmark().runAll();
    }

    public void runAll() throws Exception {
        System.out.println("\n===== Rust Worker 线程数调优 =====\n");
        System.out.println("测试端点: GET /api/health, 并发度: 16\n");

        List<BenchmarkRunner.Result> results = new ArrayList<>();
        for (int wt : WORKER_THREADS) {
            System.out.println(">>> 测试 workerThreads=" + wt + " (修改配置后重启服务器)...");
            BenchmarkRunner runner = new BenchmarkRunner()
                    .warmupIterations(2)
                    .measureIterations(3)
                    .concurrency(16)
                    .durationPerIteration(3000);

            BenchmarkRunner.Result result = runner.run(() -> {
                try {
                    return get(rustUrl("/api/health")).statusCode();
                } catch (Exception e) {
                    return 500;
                }
            });
            results.add(result);
        }

        System.out.println("\n| workerThreads | Throughput (ops/s) | p50(μs) | p90(μs) | p99(μs) | Max(μs) |");
        System.out.println("|--------------|-------------------|---------|---------|---------|---------|");
        for (int i = 0; i < WORKER_THREADS.length; i++) {
            BenchmarkRunner.Result r = results.get(i);
            System.out.printf("| %d | %.0f | %d | %d | %d | %d |%n",
                    WORKER_THREADS[i], r.throughput,
                    r.latencyP50, r.latencyP90, r.latencyP99, r.latencyMax);
        }
        System.out.println();
    }
}
