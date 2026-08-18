import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

/**
 * HTTP/Armeria verify 压测(统一环境,与 KCP 公平对比)
 * <p>用法:java HttpVerifyLoadTest <url> <并发> <每连接请求数></p>
 */
public class HttpVerifyLoadTest {

    public static void main(String[] args) throws Exception {
        String baseUrl = args.length > 0 ? args[0] : "http://localhost:19180";
        int clients = args.length > 1 ? Integer.parseInt(args[1]) : 16;
        int iterations = args.length > 2 ? Integer.parseInt(args[2]) : 20;

        // 先登录拿 token
        String loginBody = "{\"data\":\"{\\\"x-oauth-username\\\":\\\"admin\\\",\\\"x-oauth-password\\\":\\\"admin123\\\",\\\"x-oauth-auth-type\\\":\\\"WEB\\\",\\\"x-oauth-access-key\\\":\\\"\\\",\\\"x-oauth-secret-key\\\":\\\"\\\"}\"}";
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .executor(java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor())
                .build();
        HttpRequest loginReq = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/oauth/login"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .header("x-oauth-ua", "Mozilla/5.0")
                .POST(HttpRequest.BodyPublishers.ofString(loginBody))
                .build();
        HttpResponse<String> loginResp = client.send(loginReq, HttpResponse.BodyHandlers.ofString());
        String loginBodyStr = loginResp.body();
        int tokenIdx = loginBodyStr.indexOf("\"token\":\"");
        String token = tokenIdx > 0
                ? loginBodyStr.substring(tokenIdx + 9, loginBodyStr.indexOf("\"", tokenIdx + 9)) : "";
        if (token.isEmpty()) {
            System.out.println("登录失败,无 token");
            return;
        }
        System.out.println("token 获取成功: " + token.substring(0, Math.min(20, token.length())) + "...");

        String verifyBody = "{\"data\":\"{\\\"x-oauth-token\\\":\\\"" + token + "\\\"}\"}";
        HttpRequest verifyReq = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/oauth/verify"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .header("x-oauth-ua", "Mozilla/5.0")
                .POST(HttpRequest.BodyPublishers.ofString(verifyBody))
                .build();

        AtomicLong success = new AtomicLong();
        AtomicLong failure = new AtomicLong();
        List<Long> latencies = java.util.Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(clients);
        long startTime = System.currentTimeMillis();

        for (int c = 0; c < clients; c++) {
            Thread.ofVirtual().start(() -> {
                try {
                    for (int i = 0; i < iterations; i++) {
                        long t0 = System.nanoTime();
                        try {
                            HttpResponse<String> resp = client.send(verifyReq, HttpResponse.BodyHandlers.ofString());
                            long ms = (System.nanoTime() - t0) / 1_000_000L;
                            latencies.add(ms);
                            if (resp.statusCode() == 200 && resp.body().contains("\"code\":\"00000\"")) {
                                success.incrementAndGet();
                            } else {
                                failure.incrementAndGet();
                            }
                        } catch (Exception e) {
                            failure.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        long elapsedMs = System.currentTimeMillis() - startTime;
        long total = success.get() + failure.get();
        double qps = total * 1000.0 / Math.max(elapsedMs, 1);
        latencies.sort(Long::compareTo);
        long p50 = pct(latencies, 50);
        long p95 = pct(latencies, 95);
        long p99 = pct(latencies, 99);

        System.out.println("========== HTTP verify 压测 (" + baseUrl + ") ==========");
        System.out.println("并发连接: " + clients + ", 每连接请求: " + iterations);
        System.out.println("总请求: " + total + ", 成功: " + success.get() + ", 失败: " + failure.get());
        System.out.println("成功率: " + (total > 0 ? success.get() * 100.0 / total : 0) + "%");
        System.out.println("QPS: " + String.format("%.1f", qps));
        System.out.println("总耗时: " + elapsedMs + "ms");
        System.out.println("P50: " + p50 + "ms, P95: " + p95 + "ms, P99: " + p99 + "ms");
    }

    private static long pct(List<Long> sorted, double p) {
        if (sorted.isEmpty()) return 0;
        int idx = (int) Math.ceil(p / 100.0 * sorted.size()) - 1;
        return sorted.get(Math.max(0, Math.min(idx, sorted.size() - 1)));
    }
}