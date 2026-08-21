import com.chua.common.support.network.tcp.TcpClient;
import com.chua.common.support.network.tcp.JdkTcpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TCP 帧协议并发用户承载测试
 * <p>模拟 N 个用户同时通过 TCP 帧协议登录,统计成功/失败/延迟。
 * 帧协议:4 字节长度头 + body,无 topic/注册,一次请求-响应。</p>
 * <p>用法:TcpUserLoadTest <用户数> <host></p>
 * @author CH
 */
public class TcpUserLoadTest {

    static final int HEADER_SIZE = 4;

    /** Main */
    public static void main(String[] args) throws Exception {
        int users = args.length > 0 ? Integer.parseInt(args[0]) : 50;
        String host = args.length > 1 ? args[1] : "127.0.0.1";
        int port = args.length > 2 ? Integer.parseInt(args[2]) : 19187;
        String body = "{\"data\":\"{\\\"x-oauth-username\\\":\\\"admin\\\",\\\"x-oauth-password\\\":\\\"admin123\\\",\\\"x-oauth-auth-type\\\":\\\"WEB\\\",\\\"x-oauth-access-key\\\":\\\"\\\",\\\"x-oauth-secret-key\\\":\\\"\\\"}\"}";

        AtomicInteger success = new AtomicInteger();
        AtomicInteger failTimeout = new AtomicInteger();
        AtomicInteger failError = new AtomicInteger();
        List<Long> latencies = java.util.Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(users);
        TcpClient client = new JdkTcpClient(users, 5000, 30000);
        byte[] bodyBytes = body.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        long startTime = System.currentTimeMillis();

        for (int u = 0; u < users; u++) {
            Thread.ofVirtual().start(() -> {
                long t0 = System.nanoTime();
                try {
                    byte[] resp = client.call(host, port, bodyBytes);
                    long ms = (System.nanoTime() - t0) / 1_000_000L;
                    latencies.add(ms);
                    String s = new String(resp, java.nio.charset.StandardCharsets.UTF_8);
                    if (s.contains("\"code\":\"00000\"")) {
                        success.incrementAndGet();
                    } else {
                        failError.incrementAndGet();
                    }
                } catch (Exception e) {
                    long ms = (System.nanoTime() - t0) / 1_000_000L;
                    latencies.add(ms);
                    String m = e.getMessage();
                    if (m != null && (m.contains("timeout") || m.contains("Timeout"))) {
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
        latencies.sort(java.util.Comparator.naturalOrder());
        long min = latencies.isEmpty() ? 0 : latencies.get(0);
        long max = latencies.isEmpty() ? 0 : latencies.get(latencies.size() - 1);
        long sum = latencies.stream().mapToLong(Long::longValue).sum();
        long avg = latencies.isEmpty() ? 0 : sum / latencies.size();
        long p95 = latencies.isEmpty() ? 0 : latencies.get((int) (latencies.size() * 0.95));

        System.out.println("============= TCP 帧协议并发用户承载测试 =============");
        System.out.println("并发用户数: " + users);
        System.out.println("成功: " + success.get() + ", 超时失败: " + failTimeout.get() + ", 其他失败: " + failError.get());
        System.out.println("总耗时: " + elapsedMs + "ms");
        System.out.println("延迟: 最慢: " + max + "ms, 最快: " + min + "ms, 平均: " + avg + "ms, P95: " + p95 + "ms");
        System.out.println("结果: " + (success.get() == users ? "ALL USERS OK" : (success.get() + "/" + users + " OK")));
    }
}