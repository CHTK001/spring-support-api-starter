import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * HTTP 并发用户承载测试
 * <p>模拟 N 个用户同时登录,统计:成功数、失败数、最慢/最快/平均延迟。
 * 判断系统能支撑多少并发用户。</p>
 * <p>用法:HttpUserLoadTest <url> <用户数></p>
 * @author CH
 */
public class HttpUserLoadTest {

    public static void main(String[] args) throws Exception {
        String baseUrl = args.length > 0 ? args[0] : "http://localhost:19180";
        int users = args.length > 1 ? Integer.parseInt(args[1]) : 100;

        String body = "{\"data\":\"{\\\"x-oauth-username\\\":\\\"admin\\\",\\\"x-oauth-password\\\":\\\"admin123\\\",\\\"x-oauth-auth-type\\\":\\\"WEB\\\",\\\"x-oauth-access-key\\\":\\\"\\\",\\\"x-oauth-secret-key\\\":\\\"\\\"}\"}";
        String url = baseUrl + "/oauth/login";

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .executor(java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor())
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("x-oauth-ua", "Mozilla/5.0")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        AtomicInteger success = new AtomicInteger();
        AtomicInteger failTimeout = new AtomicInteger();
        AtomicInteger failError = new AtomicInteger();
        List<Long> latencies = java.util.Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(users);
        long startTime = System.currentTimeMillis();

        // 模拟 users 个用户同时登录(每人 1 次请求)
        for (int u = 0; u < users; u++) {
            Thread.ofVirtual().start(() -> {
                long t0 = System.nanoTime();
                try {
                    HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());
                    long ms = (System.nanoTime() - t0) / 1_000_000L;
                    latencies.add(ms);
                    if (resp.statusCode() == 200 && resp.body().contains("\"code\":\"00000\"")) {
                        success.incrementAndGet();
                    } else {
                        failError.incrementAndGet();
                    }
                } catch (Exception e) {
                    long ms = (System.nanoTime() - t0) / 1_000_000L;
                    latencies.add(ms);
                    String m = e.getMessage();
                    if (m != null && m.contains("timeout")) {
                        failTimeout.incrementAndGet();
                    } else {
                        failError.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        long elapsedMs = System.currentTimeMillis() - startTime;
        latencies.sort(Long::compareTo);
        long min = latencies.isEmpty() ? 0 : latencies.get(0);
        long max = latencies.isEmpty() ? 0 : latencies.get(latencies.size() - 1);
        long sum = latencies.stream().mapToLong(Long::longValue).sum();
        long avg = latencies.isEmpty() ? 0 : sum / latencies.size();
        long p95 = latencies.isEmpty() ? 0 : latencies.get((int)(latencies.size() * 0.95));

        System.out.println("========== 并发用户承载测试 (" + baseUrl + ") ==========");
        System.out.println("并发用户数: " + users);
        System.out.println("成功: " + success.get() + ", 超时失败: " + failTimeout.get() + ", 其他失败: " + failError.get());
        System.out.println("总耗时: " + elapsedMs + "ms");
        System.out.println("最慢: " + max + "ms, 最快: " + min + "ms, 平均: " + avg + "ms, P95: " + p95 + "ms");
        System.out.println("结论: " + (success.get() == users ? "ALL USERS OK" : (success.get() + "/" + users + " OK")));
    }
}
