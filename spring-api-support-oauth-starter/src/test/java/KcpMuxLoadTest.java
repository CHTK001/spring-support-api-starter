import com.chua.kcp.support.client.KcpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

/**
 * KCP 单连接多路复用压测(真正发挥长连接优势)
 * <p>1 个 KCP 连接,多线程并发 executeAsync —— 类似 HTTP/2 多路复用。</p>
 * <p>用法:KcpMuxLoadTest <并发线程> <总请求数></p>
 * @author CH
 */
public class KcpMuxLoadTest {

    public static void main(String[] args) throws Exception {
        int concurrency = args.length > 0 ? Integer.parseInt(args[0]) : 32;
        int total = args.length > 1 ? Integer.parseInt(args[1]) : 500;

        String loginBody = "{\"data\":\"{\\\"x-oauth-username\\\":\\\"admin\\\",\\\"x-oauth-password\\\":\\\"admin123\\\",\\\"x-oauth-auth-type\\\":\\\"WEB\\\",\\\"x-oauth-access-key\\\":\\\"\\\",\\\"x-oauth-secret-key\\\":\\\"\\\"}\"}";
        String serverUrl = "kcp://127.0.0.1:19186";

        // 1 个 KCP 连接
        KcpClient client = new KcpClient("mux-client", serverUrl);
        client.connect();
        // 登录拿 token
        String loginResp = client.execute("login", loginBody, 10000);
        int tokenIdx = loginResp.indexOf("\"token\":\"");
        String token = tokenIdx > 0
                ? loginResp.substring(tokenIdx + 9, loginResp.indexOf("\"", tokenIdx + 9)) : "";
        if (token.isEmpty()) {
            System.out.println("登录失败,无 token: " + loginResp.substring(0, Math.min(100, loginResp.length())));
            return;
        }
        String body = "{\"data\":\"{\\\"x-oauth-token\\\":\\\"" + token + "\\\"}\"}";
        System.out.println("CONNECTED, 单连接并发线程: " + concurrency + ", token: " + token.substring(0, 16) + "...");

        AtomicLong success = new AtomicLong();
        AtomicLong failure = new AtomicLong();
        List<Long> latencies = java.util.Collections.synchronizedList(new ArrayList<>());
        long startTime = System.currentTimeMillis();

        // 用线程池并发调 executeAsync,共享同一连接(多路复用)
        int batch = Math.max(1, total / concurrency);
        CountDownLatch latch = new CountDownLatch(concurrency);
        for (int c = 0; c < concurrency; c++) {
            Thread.ofVirtual().start(() -> {
                try {
                    for (int i = 0; i < batch; i++) {
                        long t0 = System.nanoTime();
                        CompletableFuture<String> f = client.executeAsync("verify", body, 10000);
                        try {
                            String resp = f.get();
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
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        client.disconnect();

        long elapsedMs = System.currentTimeMillis() - startTime;
        long totalDone = success.get() + failure.get();
        double qps = totalDone * 1000.0 / Math.max(elapsedMs, 1);
        latencies.sort(Long::compareTo);
        long p50 = pct(latencies, 50);
        long p95 = pct(latencies, 95);
        long p99 = pct(latencies, 99);

        System.out.println("========== KCP verify (单连接多路复用) ==========");
        System.out.println("并发线程: " + concurrency + ", 单连接");
        System.out.println("total: " + totalDone + ", success: " + success.get() + ", failed: " + failure.get());
        System.out.println("success rate: " + (totalDone > 0 ? success.get() * 100.0 / totalDone : 0) + "%");
        System.out.println("QPS: " + String.format("%.1f", qps));
        System.out.println("elapsed: " + elapsedMs + "ms");
        System.out.println("P50: " + p50 + "ms, P95: " + p95 + "ms, P99: " + p99 + "ms");
    }

    private static long pct(List<Long> sorted, double p) {
        if (sorted.isEmpty()) return 0;
        int idx = (int) Math.ceil(p / 100.0 * sorted.size()) - 1;
        return sorted.get(Math.max(0, Math.min(idx, sorted.size() - 1)));
    }
}
