import com.chua.kcp.support.client.KcpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * KCP 协议登录压测程序
 * <p>多客户端并发执行 login,统计 QPS/成功率/P50/P95 延迟。</p>
 */
public class KcpLoadTest {

    public static void main(String[] args) throws Exception {
        int clients = args.length > 0 ? Integer.parseInt(args[0]) : 8;
        int iterations = args.length > 1 ? Integer.parseInt(args[1]) : 5;

        String body = "{\"data\":\"{\\\"x-oauth-username\\\":\\\"admin\\\",\\\"x-oauth-password\\\":\\\"admin123\\\",\\\"x-oauth-auth-type\\\":\\\"WEB\\\",\\\"x-oauth-access-key\\\":\\\"\\\",\\\"x-oauth-secret-key\\\":\\\"\\\"}\"}";
        String serverUrl = "kcp://127.0.0.1:19186";

        AtomicLong success = new AtomicLong();
        AtomicLong failure = new AtomicLong();
        List<Long> latencies = java.util.Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(clients);
        long startTime = System.currentTimeMillis();

        for (int c = 0; c < clients; c++) {
            String clientId = "load-" + c;
            Thread.ofVirtual().start(() -> {
                KcpClient client = null;
                try {
                    client = new KcpClient(clientId, serverUrl);
                    client.connect();
                    for (int i = 0; i < iterations; i++) {
                        long t0 = System.nanoTime();
                        try {
                            String resp = client.execute("login", body, 10000);
                            long ms = (System.nanoTime() - t0) / 1_000_000L;
                            latencies.add(ms);
                            if (resp != null && resp.contains("\"code\":\"00000\"")) {
                                success.incrementAndGet();
                            } else {
                                failure.incrementAndGet();
                            }
                        } catch (Exception e) {
                            failure.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    System.out.println("client " + clientId + " connect err: " + e.getMessage());
                    failure.incrementAndGet();
                } finally {
                    if (client != null) { try { client.disconnect(); } catch (Exception ignore) {} }
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

        System.out.println("========== KCP 压测结果 ==========");
        System.out.println("并发连接: " + clients + ", 每连接请求: " + iterations);
        System.out.println("总请求: " + total + ", 成功: " + success.get() + ", 失败: " + failure.get());
        System.out.println("成功率: " + (total > 0 ? success.get() * 100.0 / total : 0) + "%");
        System.out.println("QPS: " + String.format("%.1f", qps));
        System.out.println("总耗时: " + elapsedMs + "ms");
        System.out.println("P50 延迟: " + p50 + "ms");
        System.out.println("P95 延迟: " + p95 + "ms");
        System.out.println("P99 延迟: " + p99 + "ms");
    }

    private static long pct(List<Long> sorted, double p) {
        if (sorted.isEmpty()) return 0;
        int idx = (int) Math.ceil(p / 100.0 * sorted.size()) - 1;
        return sorted.get(Math.max(0, Math.min(idx, sorted.size() - 1)));
    }
}