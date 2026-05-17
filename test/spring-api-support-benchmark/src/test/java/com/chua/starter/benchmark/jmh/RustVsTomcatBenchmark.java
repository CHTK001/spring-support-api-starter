package com.chua.starter.benchmark.jmh;

import com.chua.starter.benchmark.AbstractBenchmarkBase;
import com.chua.starter.benchmark.BenchmarkRunner;

import java.net.http.HttpResponse;

/**
 * Rust vs Tomcat 全方位基准测试 — 输出 Markdown 表格。
 *
 * <p>使用前需确保两个服务器都已启动：
 * <ul>
 *   <li>Rust:  http://localhost:18081</li>
 *   <li>Tomcat: http://localhost:18082</li>
 * </ul>
 */
public class RustVsTomcatBenchmark extends AbstractBenchmarkBase {

    public static void main(String[] args) throws Exception {
        new RustVsTomcatBenchmark().runAll();
    }

    public void runAll() throws Exception {
        System.out.println("\n===== Rust vs Tomcat 基准测试 =====\n");

        for (int concurrency : new int[]{4, 8, 16, 32}) {
            System.out.printf("### 并发度: %d%n%n", concurrency);
            System.out.println("| 场景 | Rust (ops/s) | Rust p50(μs) | Rust p99(μs) | Tomcat (ops/s) | Tomcat p50(μs) | Tomcat p99(μs) | 倍率(R/T) |");
            System.out.println("|------|-------------|-------------|-------------|----------------|----------------|----------------|----------|");

            benchAndPrint("Health", "/api/health", concurrency);
            benchAndPrint("List Users", "/api/users", concurrency);
            benchAndPrint("Echo", "/api/echo?msg=hello", concurrency);
            benchAndPrint("Delay 100ms", "/api/delay/100", concurrency);
            benchAndPrint("Create User", "", concurrency);
            benchAndPrint("Get User", "/api/users/0", concurrency);
            System.out.println();
        }

        System.out.println("\n===== 完成. 可将以上表格复制到 BENCHMARK_RESULTS.md =====");
    }

    private void benchAndPrint(String label, String path, int concurrency) throws Exception {
        BenchmarkRunner runner = new BenchmarkRunner()
                .warmupIterations(2)
                .measureIterations(3)
                .concurrency(concurrency)
                .durationPerIteration(3000);

        BenchmarkRunner.Result rustResult = runner.run(() -> requestWithPath("rust", path));
        BenchmarkRunner.Result tomcatResult = runner.run(() -> requestWithPath("tomcat", path));

        double ratio = rustResult.throughput / tomcatResult.throughput;
        System.out.printf("| %s | %.0f | %d | %d | %.0f | %d | %d | %.2fx |%n",
                label,
                rustResult.throughput, rustResult.latencyP50, rustResult.latencyP99,
                tomcatResult.throughput, tomcatResult.latencyP50, tomcatResult.latencyP99,
                ratio);
    }

    private int requestWithPath(String server, String path) {
        try {
            HttpResponse<String> resp;
            if (path.isEmpty()) {
                String base = "rust".equals(server) ? RUST_BASE : TOMCAT_BASE;
                resp = post(base + "/api/users", "{\"name\":\"test\"}");
            } else {
                String url = "rust".equals(server) ? rustUrl(path) : tomcatUrl(path);
                resp = get(url);
            }
            return resp.statusCode();
        } catch (Exception e) {
            return 500;
        }
    }
}
