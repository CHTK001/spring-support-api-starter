import com.chua.kcp.support.client.KcpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

/**
 * KCP 协议 verify(鉴权)压测程序
 * <p>先登录拿 token,多客户端并发 verify,统计 QPS/延迟。</p>
 * @author CH
 */
public class KcpVerifyLoadTest {

    /** Main */
    public static void main(String[] args) throws Exception {
        int clients = args.length > 0 ? Integer.parseInt(args[0]) : 16;
        int iterations = args.length > 1 ? Integer.parseInt(args[1]) : 20;

        String serverUrl = "kcp://127.0.0.1:19186";
        String loginBody = "{\"data\":\"{\\\"x-oauth-username\\\":\\\"admin\\\",\\\"x-oauth-password\\\":\\\"admin123\\\",\\\"x-oauth-auth-type\\\":\\\"WEB\\\",\\\"x-oauth-access-key\\\":\\\"\\\",\\\"x-oauth-secret-key\\\":\\\"\\\"}\"}";

        // 先登录拿 token
        KcpClient loginClient = new KcpClient("verify-login", serverUrl);
        loginClient.connect();
        String loginResp = loginClient.execute("login", loginBody, 10000);
        int tokenIdx = loginResp.indexOf("\"token\":\"");
        String token = tokenIdx > 0
                ? loginResp.substring(tokenIdx + 9, loginResp.indexOf("\"", tokenIdx + 9)) : "";
        loginClient.disconnect();
        if (token.isEmpty()) {
            System.out.println("登录失败,无 token: " + loginResp.substring(0, Math.min(100, loginResp.length())));
            return;
        }
        System.out.println("token 获取成功: " + token.substring(0, 20) + "...");

        String verifyBody = "{\"data\":\"{\\\"x-oauth-token\\\":\\\"" + token + "\\\"}\"}";

        AtomicLong success = new AtomicLong();
        AtomicLong failure = new AtomicLong();
        List<Long> latencies = java.util.Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(clients);
        long startTime = System.currentTimeMillis();

        for (int c = 0; c < clients; c++) {
            String clientId = "verify-" + c;
            Thread.ofVirtual().start(() -> {
                KcpClient client = null;
                try {
                    client = new KcpClient(clientId, serverUrl);
                    client.connect();
                    for (int i = 0; i < iterations; i++) {
                        long t0 = System.nanoTime();
                        try {
                            String resp = client.execute("verify", verifyBody, 10000);
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

        System.out.println("========== KCP verify 压测结果 ==========");
        System.out.println("并发连接: " + clients + ", 每连接请求: " + iterations);
        System.out.println("总请求: " + total + ", 成功: " + success.get() + ", 失败: " + failure.get());
        System.out.println("成功率: " + (total > 0 ? success.get() * 100.0 / total : 0) + "%");
        System.out.println("QPS: " + String.format("%.1f", qps));
        System.out.println("总耗时: " + elapsedMs + "ms");
        System.out.println("P50 延迟: " + p50 + "ms");
        System.out.println("P95 延迟: " + p95 + "ms");
        System.out.println("P99 延迟: " + p99 + "ms");
    }

    /** Pct */
    private static long pct(List<Long> sorted, double p) {
        if (sorted.isEmpty()) return 0;
        int idx = (int) Math.ceil(p / 100.0 * sorted.size()) - 1;
        return sorted.get(Math.max(0, Math.min(idx, sorted.size() - 1)));
    }
}
