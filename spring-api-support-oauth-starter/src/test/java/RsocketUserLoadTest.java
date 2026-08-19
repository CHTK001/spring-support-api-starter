import io.rsocket.core.RSocketConnector;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import io.rsocket.Payload;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * RSocket 并发用户承载测试
 * <p>模拟 N 个用户同时登录,统计成功/失败/延迟。</p>
 * <p>用法:RsocketUserLoadTest <用户数></p>
 * @author CH
 */
public class RsocketUserLoadTest {

    /** Main */
    public static void main(String[] args) throws Exception {
        int users = args.length > 0 ? Integer.parseInt(args[0].trim()) : 50;
        String body = "{\"data\":\"{\\\"x-oauth-username\\\":\\\"admin\\\",\\\"x-oauth-password\\\":\\\"admin123\\\",\\\"x-oauth-auth-type\\\":\\\"WEB\\\",\\\"x-oauth-access-key\\\":\\\"\\\",\\\"x-oauth-secret-key\\\":\\\"\\\"}\"}";
        String metadata = "{\"route\":\"login\",\"x-oauth-encode\":\"false\"}";

        AtomicInteger success = new AtomicInteger();
        AtomicInteger failTimeout = new AtomicInteger();
        AtomicInteger failError = new AtomicInteger();
        List<Long> latencies = java.util.Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(users);
        long startTime = System.currentTimeMillis();

        for (int u = 0; u < users; u++) {
            Thread.ofVirtual().start(() -> {
                long t0 = System.nanoTime();
                try {
                    var socket = RSocketConnector.create()
                            .connect(TcpClientTransport.create("localhost", 19185))
                            .block(Duration.ofSeconds(10));
                    if (socket == null) {
                        failError.incrementAndGet();
                        return;
                    }
                    Payload req = DefaultPayload.create(body, metadata);
                    Payload resp = socket.requestResponse(req).block(Duration.ofSeconds(30));
                    long ms = (System.nanoTime() - t0) / 1_000_000L;
                    latencies.add(ms);
                    if (resp != null && resp.getDataUtf8().contains("\"code\":\"00000\"")) {
                        success.incrementAndGet();
                    } else {
                        failError.incrementAndGet();
                    }
                    if (resp != null) resp.release();
                    req.release();
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
        latencies.sort(Long::compareTo);
        long min = latencies.isEmpty() ? 0 : latencies.get(0);
        long max = latencies.isEmpty() ? 0 : latencies.get(latencies.size() - 1);
        long sum = latencies.stream().mapToLong(Long::longValue).sum();
        long avg = latencies.isEmpty() ? 0 : sum / latencies.size();
        long p95 = latencies.isEmpty() ? 0 : latencies.get((int)(latencies.size() * 0.95));

        System.out.println("========== RSocket 并发用户承载测试 ==========");
        System.out.println("并发用户数: " + users);
        System.out.println("成功: " + success.get() + ", 超时失败: " + failTimeout.get() + ", 其他失败: " + failError.get());
        System.out.println("总耗时: " + elapsedMs + "ms");
        System.out.println("最慢: " + max + "ms, 最快: " + min + "ms, 平均: " + avg + "ms, P95: " + p95 + "ms");
        System.out.println("结论: " + (success.get() == users ? "ALL USERS OK" : (success.get() + "/" + users + " OK")));
    }
}
