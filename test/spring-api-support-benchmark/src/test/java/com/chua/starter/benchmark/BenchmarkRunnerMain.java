package com.chua.starter.benchmark;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 快速压测入口 — 按指定并发度测试两个服务器。
 *
 * <p>用法: 先启动两个服务器，然后运行此 main。</p>
 */
public class BenchmarkRunnerMain {

    public static void main(String[] args) throws Exception {
        int[] concurrencyLevels = {100, 1000, 10000};
        String[] endpoints = {"/api/health", "/api/echo?msg=hello", "/api/users", "/api/delay/100"};

        var httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        System.out.println("# 基准测试结果\n");
        System.out.println("| 服务器 | 端点 | 并发 | 请求数 | 错误 | Throughput (ops/s) | Avg(μs) | p50(μs) | p90(μs) | p99(μs) | Max(μs) |");
        System.out.println("|--------|------|------|--------|------|-------------------|---------|---------|---------|---------|---------|");

        for (int concurrency : concurrencyLevels) {
            for (String endpoint : endpoints) {
                // 对 delay 端点跳过 10000 并发（会拖太久）
                if (endpoint.contains("delay/100") && concurrency >= 10000) {
                    continue;
                }
                testServer(httpClient, "Rust", AbstractBenchmarkBase.RUST_BASE + endpoint, concurrency);
                testServer(httpClient, "Tomcat", AbstractBenchmarkBase.TOMCAT_BASE + endpoint, concurrency);
            }
        }

        // 100000 并发单独说明
        System.out.println("| Rust | /api/health | 100000 | - | - | (单机端口不足, 需分布式压测) | - | - | - | - | - |");
        System.out.println("| Tomcat | /api/health | 100000 | - | - | (单机端口不足, 需分布式压测) | - | - | - | - | - |");
    }

    static void testServer(HttpClient httpClient, String label, String url, int concurrency) throws Exception {
        int warmup = concurrency >= 10000 ? 0 : 1;
        int measure = concurrency >= 10000 ? 1 : 2;
        long durationMs = concurrency >= 10000 ? 5000 : 3000;

        String path = url.substring(url.indexOf('/', 8));

        var runner = new BenchmarkRunner()
                .warmupIterations(warmup)
                .measureIterations(measure)
                .concurrency(concurrency)
                .durationPerIteration(durationMs);

        var result = runner.run(() -> {
            try {
                var req = HttpRequest.newBuilder()
                        .uri(java.net.URI.create(url))
                        .timeout(Duration.ofSeconds(60))
                        .GET()
                        .build();
                return httpClient.send(req, HttpResponse.BodyHandlers.ofString()).statusCode();
            } catch (Exception e) {
                return 500;
            }
        });

        System.out.printf("| %s | %s | %d | %d | %d | %.0f | %.0f | %d | %d | %d | %d |%n",
                label, path, concurrency,
                result.totalRequests, result.totalErrors,
                result.throughput, result.latencyAvg,
                result.latencyP50, result.latencyP90, result.latencyP99, result.latencyMax);
    }
}
