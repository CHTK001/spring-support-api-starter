import com.chua.common.support.network.sync.SyncClient;
import com.chua.common.support.network.sync.SyncMessageHandler;
import com.chua.common.support.network.sync.impl.TcpSyncClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TCP 长连接多路复用压测
 * <p>模拟正常使用:建立 conns 条长连接并保持,每条连接上并发复用请求(同链多路复用),
 * 与 RsocketMux 同口径,便于在公网下与 HTTP/RSocket 公平对比。</p>
 * <p>用法:TcpMuxLoadTest <host> <连接数> <每连接请求数> <端口></p>
 * @author CH
 */
public class TcpMuxLoadTest {

    /** Main */
    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "127.0.0.1";
        int conns = args.length > 1 ? Integer.parseInt(args[1]) : 10;
        int perConn = args.length > 2 ? Integer.parseInt(args[2]) : 50;
        int port = args.length > 3 ? Integer.parseInt(args[3]) : 19187;
        int totalRequests = conns * perConn;
        String serverUrl = "tcp://" + host + ":" + port;
        String body = "{\"data\":\"{\\\"x-oauth-username\\\":\\\"admin\\\",\\\"x-oauth-password\\\":\\\"admin123\\\",\\\"x-oauth-auth-type\\\":\\\"WEB\\\",\\\"x-oauth-access-key\\\":\\\"\\\",\\\"x-oauth-secret-key\\\":\\\"\\\"}\"}";

        AtomicInteger success = new AtomicInteger();
        AtomicInteger failTimeout = new AtomicInteger();
        AtomicInteger failError = new AtomicInteger();
        List<Long> latencies = java.util.Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(totalRequests);
        long startTime = System.currentTimeMillis();

        // 建立 conns 条长连接并保持
        List<SyncClient> clients = new ArrayList<>();
        List<CompletableFuture<String>> pending = new ArrayList<>();
        for (int c = 0; c < conns; c++) {
            try {
                SyncClient cli = new TcpSyncClient("tcp-mux-" + c + "-" + UUID.randomUUID(), serverUrl);
                cli.connect();
                clients.add(cli);
            } catch (Exception e) {
                for (int i = 0; i < perConn; i++) {
                    failError.incrementAndGet();
                    latch.countDown();
                }
            }
        }
        int established = clients.size();

        // 每条连接上并发复用 perConn 个请求(同链多路复用)
        for (int c = 0; c < established; c++) {
            final SyncClient cli = clients.get(c);
            for (int r = 0; r < perConn; r++) {
                final String reqId = "mq" + c + "-" + r;
                CompletableFuture<String> f = new CompletableFuture<>();
                pending.add(f);
                SyncMessageHandler handler = (topic, message) -> {
                    String msg = String.valueOf(message);
                    int sep = msg.indexOf('|');
                    String gotId = sep > 0 ? msg.substring(0, sep) : "";
                    if (gotId.equals(reqId)) {
                        f.complete(sep > 0 ? msg.substring(sep + 1) : msg);
                    } else if (sep <= 0) {
                        f.complete(msg);
                    }
                };
                cli.subscribe("resp/login", handler);
                Thread.ofVirtual().start(() -> {
                    long t0 = System.nanoTime();
                    try {
                        cli.send("req/login", reqId + "|" + body);
                        String resp = f.get(30, TimeUnit.SECONDS);
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
        latencies.sort(java.util.Comparator.naturalOrder());
        long min = latencies.isEmpty() ? 0 : latencies.get(0);
        long max = latencies.isEmpty() ? 0 : latencies.get(latencies.size() - 1);
        long sum = latencies.stream().mapToLong(Long::longValue).sum();
        long avg = latencies.isEmpty() ? 0 : sum / latencies.size();
        long p95 = latencies.isEmpty() ? 0 : latencies.get((int) (latencies.size() * 0.95));
        long tps = elapsedMs > 0 ? totalRequests * 1000L / elapsedMs : 0;

        System.out.println("========== TCP 长连接多路复用压测 ==========");
        System.out.println("长连接数: " + conns + ", 每连接请求数: " + perConn + ", 总请求: " + totalRequests);
        System.out.println("成功: " + success.get() + ", 超时失败: " + failTimeout.get() + ", 其他失败: " + failError.get());
        System.out.println("总耗时: " + elapsedMs + "ms, 吞吐: " + tps + " 次/秒");
        System.out.println("延迟: 最慢: " + max + "ms, 最快: " + min + "ms, 平均: " + avg + "ms, P95: " + p95 + "ms");
        System.out.println("结论: " + success.get() + "/" + totalRequests + " OK");
        clients.forEach(SyncClient::disconnect);
    }
}