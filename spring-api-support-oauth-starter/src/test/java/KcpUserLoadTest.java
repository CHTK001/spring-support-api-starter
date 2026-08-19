import com.chua.kcp.support.client.KcpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * KCP 并发用户承载测试
 * <p>模拟 N 个用户同时登录,统计成功/失败/延迟。
 * 判断 KCP 能支撑多少并发用户。</p>
 * <p>用法:KcpUserLoadTest <用户数> <每用户连接数(默认1)></p>
 * @author CH
 */
public class KcpUserLoadTest {

    /** Main */
    public static void main(String[] args) throws Exception {
        int users = args.length > 0 ? Integer.parseInt(args[0]) : 50;
        String serverUrl = "kcp://127.0.0.1:19186";
        String body = "{\"data\":\"{\\\"x-oauth-username\\\":\\\"admin\\\",\\\"x-oauth-password\\\":\\\"admin123\\\",\\\"x-oauth-auth-type\\\":\\\"WEB\\\",\\\"x-oauth-access-key\\\":\\\"\\\",\\\"x-oauth-secret-key\\\":\\\"\\\"}\"}";

        AtomicInteger success = new AtomicInteger();
        AtomicInteger failTimeout = new AtomicInteger();
        AtomicInteger failError = new AtomicInteger();
        List<Long> latencies = java.util.Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(users);
        long startTime = System.currentTimeMillis();

        // 模拟 users 个用户,每用户独立 KCP 连接,同时登录
        for (int u = 0; u < users; u++) {
            final int uid = u;
            Thread.ofVirtual().start(() -> {
                long t0 = System.nanoTime();
                KcpClient client = null;
                try {
                    client = new KcpClient("user-" + uid, serverUrl);
                    client.connect();
                    String resp = client.execute("login", body, 30000);
                    long ms = (System.nanoTime() - t0) / 1_000_000L;
                    latencies.add(ms);
                    if (resp != null && resp.contains("\"code\":\"00000\"")) {
                        success.incrementAndGet();
                    } else {
                        failError.incrementAndGet();
                    }
                } catch (Exception e) {
                    long ms = (System.nanoTime() - t0) / 1_000_000L;
                    latencies.add(ms);
                    String m = e.getMessage();
                    if (m != null && (m.contains("timeout") || m.contains("超时"))) {
                        failTimeout.incrementAndGet();
                    } else {
                        failError.incrementAndGet();
                    }
                } finally {
                    if (client != null) { try { client.disconnect(); } catch (Exception ignore) {} }
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

        System.out.println("========== KCP 并发用户承载测试 ==========");
        System.out.println("并发用户数: " + users);
        System.out.println("成功: " + success.get() + ", 超时失败: " + failTimeout.get() + ", 其他失败: " + failError.get());
        System.out.println("总耗时: " + elapsedMs + "ms");
        System.out.println("最慢: " + max + "ms, 最快: " + min + "ms, 平均: " + avg + "ms, P95: " + p95 + "ms");
        System.out.println("结论: " + (success.get() == users ? "ALL USERS OK" : (success.get() + "/" + users + " OK")));
    }
}
