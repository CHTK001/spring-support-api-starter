package com.chua.starter.benchmark.jmh;

import com.chua.starter.benchmark.AbstractBenchmarkBase;
import com.chua.starter.benchmark.BenchmarkRunner;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 响应缓冲区大小调优基准测试 — 输出 Markdown 表格。
 *
 * <p>逐一修改 plugin.server.rust.response-buffer 并重启服务器后运行。</p>
 */
public class ResponseBufferBenchmark extends AbstractBenchmarkBase {

    private static final int[] BUFFER_SIZES = {4096, 16384, 65536, 262144, 1048576};

    public static void main(String[] args) throws Exception {
        new ResponseBufferBenchmark().runAll();
    }

    public void runAll() throws Exception {
        System.out.println("\n===== 响应缓冲区大小调优 =====\n");
        System.out.println("测试端点: GET /api/users, 并发度: 16\n");

        List<BenchmarkRunner.Result> results = new ArrayList<>();
        for (int buf : BUFFER_SIZES) {
            System.out.println(">>> 测试 response-buffer=" + formatBytes(buf) + " (修改配置后重启服务器)...");
            BenchmarkRunner runner = new BenchmarkRunner()
                    .warmupIterations(2)
                    .measureIterations(3)
                    .concurrency(16)
                    .durationPerIteration(3000);

            BenchmarkRunner.Result result = runner.run(() -> {
                try {
                    return get(rustUrl("/api/users")).statusCode();
                } catch (Exception e) {
                    return 500;
                }
            });
            results.add(result);
        }

        System.out.println("\n| Buffer | Throughput (ops/s) | p50(μs) | p90(μs) | p99(μs) | Max(μs) |");
        System.out.println("|--------|-------------------|---------|---------|---------|---------|");
        for (int i = 0; i < BUFFER_SIZES.length; i++) {
            BenchmarkRunner.Result r = results.get(i);
            System.out.printf("| %s | %.0f | %d | %d | %d | %d |%n",
                    formatBytes(BUFFER_SIZES[i]), r.throughput,
                    r.latencyP50, r.latencyP90, r.latencyP99, r.latencyMax);
        }
        System.out.println();
    }

    private static String formatBytes(int bytes) {
        if (bytes < 1024) return bytes + "B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + "KB";
        return (bytes / (1024 * 1024)) + "MB";
    }
}
