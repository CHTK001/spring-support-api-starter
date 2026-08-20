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
import java.util.concurrent.atomic.AtomicLong;

/**
 * TCP 并发用户承载测试
 * <p>模拟 N 个用户同时通过 TCP 长连接登录,统计成功/失败/延迟。
 * 服务端通过 SyncServer 适配(topic 请求-响应协议)。</p>
 * <p>用法:TcpUserLoadTest <用户数> <host></p>
 *
 * @author CH
 */
public class TcpUserLoadTest {

    /** Main */
    public static void main(String[] args) throws Exception {
        int users = args.length > 0 ? Integer.parseInt(args[0]) : 50;
        String host = args.length > 1 ? args[1] : "127.0.0.1";
        int port = args.length > 2 ? Integer.parseInt(args[2]) : 19187;
        String serverUrl = "tcp://" + host + ":" + port;
        String body = "{\"data\":\"{\\\"x-oauth-username\\\":\\\"admin\\\",\\\"x-oauth-password\\\":\\\"admin123\\\",\\\"x-oauth-auth-type\\\":\\\"WEB\\\",\\\"x-oauth-access-key\\\":\\\"\\\",\\\"x-oauth-secret-key\\\":\\\"\\\"}\"}";

        AtomicInteger success = new AtomicInteger();
        AtomicInteger failTimeout = new AtomicInteger();
        AtomicInteger failError = new AtomicInteger();
        List<Long> latencies = java.util.Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(users);
        long startTime = System.currentTimeMillis();

        for (int u = 0; u < users; u++) {
            final int uid = u;
            Thread.ofVirtual().start(() -> {
                long t0 = System.nanoTime();
                SyncClient client = null;
                String reqId = "req" + uid;
                try {
                    client = new TcpSyncClient("tcp-user-" + uid, serverUrl);
                    CompletableFuture<String> future = new CompletableFuture<>();
                    SyncMessageHandler handler = (topic, message) -> {
                        String msg = String.valueOf(message);
                        int sep = msg.indexOf('|');
                        String gotId = sep > 0 ? msg.substring(0, sep) : "";
                        if (gotId.equals(reqId)) {
                            future.complete(sep > 0 ? msg.substring(sep + 1) : msg);
                        } else if (sep <= 0) {
                            future.complete(msg);
                        }
                    };
                    client.subscribe("resp/login", handler);
                    client.connect();
                    client.send("req/login", reqId + "|" + body);
                    String resp = future.get(30, TimeUnit.SECONDS);
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
                    if (client != null) {
                        client.disconnect();
                    }
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

        System.out.println("============= TCP 并发用户承载测试 =============");
        System.out.println("并发用户数: " + users);
        System.out.println("成功: " + success.get() + ", 超时失败: " + failTimeout.get() + ", 其他失败: " + failError.get());
        System.out.println("总耗时: " + elapsedMs + "ms");
        System.out.println("延迟: 最大: " + max + "ms, 最小: " + min + "ms, 平均: " + avg + "ms, P95: " + p95 + "ms");
        System.out.println("结果: " + success.get() + "/" + users + " OK");
    }
}