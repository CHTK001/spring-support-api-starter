import com.chua.kcp.support.client.KcpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

/**
 * KCP verify 压测(长连接复用模式,与 HTTP 公平对比)
 * <p>用法:KcpConnReuseVerify <客户端数> <每客户端请求数></p>
 * @author CH
 */
public class KcpConnReuseVerify {

    public static void main(String[] args) throws Exception {
        int clients = args.length > 0 ? Integer.parseInt(args[0]) : 4;
        int iterations = args.length > 1 ? Integer.parseInt(args[1]) : 100;

        String serverUrl = "kcp://127.0.0.1:19186";
        String loginBody = "{\"data\":\"{\\\"x-oauth-username\\\":\\\"admin\\\",\\\"x-oauth-password\\\":\\\"admin123\\\",\\\"x-oauth-auth-type\\\":\\\"WEB\\\",\\\"x-oauth-access-key\\\":\\\"\\\",\\\"x-oauth-secret-key\\\":\\\"\\\"}\"}";

        // 登录拿 token(复用连接)
        KcpClient loginClient = new KcpClient("verify-login", serverUrl);
        loginClient.connect();
        String loginResp = loginClient.execute("login", loginBody, 10000);
        int tokenIdx = loginResp.indexOf("\"token\":\"");
        String token = tokenIdx > 0
                ? loginResp.substring(tokenIdx + 9, loginResp.indexOf("\"", tokenIdx + 9)) : "";
        if (token.isEmpty()) {
            System.out.println("登录失败,无 token");
            return;
        }
        System.out.println("token 获取成功: " + token.substring(0, 20) + "...");
        // 使用同一个连接发 verify,对比 HTTP(连接池复用)
        String verifyBody = "{\"data\":\"{\\\"x-oauth-token\\\":\\\"" + token + "\\\"}\"}";

        AtomicLong success = new AtomicLong();
        AtomicLong failure = new AtomicLong();
        List<Long> latencies = java.util.Collections.synchronizedList(new ArrayList<>());
        long startTime = System.currentTimeMillis();

        // 复用同一个 KCP 连接连续发 verify
        for (int i = 0; i < iterations; i++) {
            long t0 = System.nanoTime();
            try {
                String resp = loginClient.execute("verify", verifyBody, 10000);
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
        loginClient.disconnect();

        long elapsedMs = System.currentTimeMillis() - startTime;
        long total = success.get() + failure.get();
        double qps = total * 1000.0 / Math.max(elapsedMs, 1);
        latencies.sort(Long::compareTo);
        long p50 = pct(latencies, 50);
        long p95 = pct(latencies, 95);
        long p99 = pct(latencies, 99);

        System.out.println("========== KCP verify (single conn reuse) ==========");
        System.out.println("KCP connections: 1, requests: " + iterations);
        System.out.println("total: " + total + ", success: " + success.get() + ", failed: " + failure.get());
        System.out.println("success rate: " + (total > 0 ? success.get() * 100.0 / total : 0) + "%");
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
