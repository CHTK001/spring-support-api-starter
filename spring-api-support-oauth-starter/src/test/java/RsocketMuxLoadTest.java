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
 * RSocket 长连接多路复用压测
 * <p>模拟正常使用:建立 conns 条长连接并保持,每条连接上并发复用 requestsPerConn 个
 * request/response 流(同链多路复用,不经由连接的新建/销毁开销)。</p>
 * <p>用法:RsocketMuxLoadTest <host> <连接数> <每连接请求数></p>
 * @author CH
 */
public class RsocketMuxLoadTest {

    /** Main */
    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "localhost";
        int conns = args.length > 1 ? Integer.parseInt(args[1]) : 10;
        int perConn = args.length > 2 ? Integer.parseInt(args[2]) : 50;
        int totalRequests = conns * perConn;

        String body = "{\"data\":\"{\\\"x-oauth-username\\\":\\\"admin\\\",\\\"x-oauth-password\\\":\\\"admin123\\\",\\\"x-oauth-auth-type\\\":\\\"WEB\\\",\\\"x-oauth-access-key\\\":\\\"\\\",\\\"x-oauth-secret-key\\\":\\\"\\\"}\"}";
        String metadata = "{\"route\":\"login\",\"x-oauth-encode\":\"false\"}";

        AtomicInteger success = new AtomicInteger();
        AtomicInteger failTimeout = new AtomicInteger();
        AtomicInteger failError = new AtomicInteger();
        List<Long> latencies = java.util.Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(totalRequests);
        long startTime = System.currentTimeMillis();

        // 建立 conns 条长连接并保持
        List<io.rsocket.RSocket> sockets = new ArrayList<>();
        for (int c = 0; c < conns; c++) {
            var socket = RSocketConnector.create()
                    .metadataMimeType("message/x.rsocket.composite-metadata.v0")
                    .dataMimeType("application/json")
                    .connect(TcpClientTransport.create(host, 19185))
                    .block(Duration.ofSeconds(15));
            if (socket != null) {
                sockets.add(socket);
            } else {
                for (int i = 0; i < perConn; i++) {
                    failError.incrementAndGet();
                    latch.countDown();
                }
            }
        }
        int established = sockets.size();

        // 每条连接上并发复用 perConn 个请求(同链多路复用)
        for (int c = 0; c < established; c++) {
            final io.rsocket.RSocket socket = sockets.get(c);
            for (int r = 0; r < perConn; r++) {
                Thread.ofVirtual().start(() -> {
                    long t0 = System.nanoTime();
                    try {
                        Payload req = DefaultPayload.create(body, metadata);
                        Payload resp = socket.requestResponse(req).block(Duration.ofSeconds(30));
                        long ms = (System.nanoTime() - t0) / 1_000_000L;
                        latencies.add(ms);
                        String data = resp != null ? resp.getDataUtf8() : null;
                        if (data != null && data.contains("\"code\":\"00000\"")) {
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
        }
        latch.await();

        long elapsedMs = System.currentTimeMillis() - startTime;
        latencies.sort(Long::compareTo);
        long min = latencies.isEmpty() ? 0 : latencies.get(0);
        long max = latencies.isEmpty() ? 0 : latencies.get(latencies.size() - 1);
        long sum = latencies.stream().mapToLong(Long::longValue).sum();
        long avg = latencies.isEmpty() ? 0 : sum / latencies.size();
        long p95 = latencies.isEmpty() ? 0 : latencies.get((int) (latencies.size() * 0.95));
        long tps = elapsedMs > 0 ? totalRequests * 1000L / elapsedMs : 0;

        System.out.println("========== RSocket 长连接多路复用压测 ==========");
        System.out.println("长连接数: " + conns + ", 每连接请求数: " + perConn + ", 总请求: " + totalRequests);
        System.out.println("成功: " + success.get() + ", 超时失败: " + failTimeout.get() + ", 其他失败: " + failError.get());
        System.out.println("总耗时: " + elapsedMs + "ms, 吞吐: " + tps + " req/s");
        System.out.println("延迟: 最慢: " + max + "ms, 最快: " + min + "ms, 平均: " + avg + "ms, P95: " + p95 + "ms");
        System.out.println("结论: " + success.get() + "/" + totalRequests + " OK");
        sockets.forEach(io.rsocket.RSocket::dispose);
    }
}