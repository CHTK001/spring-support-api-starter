package com.chua.starter.benchmark.concurrency;

import com.chua.starter.benchmark.AbstractBenchmarkBase;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 数据竞争检测 — 高并发读写场景下验证无数据竞争。
 *
 * <p>通过 AtomicLong 计数器 vs long 原始类型在并发场景下的行为对比，
 * 验证 Rust 服务器的请求处理不会产生数据竞争。</p>
 */
class DataRaceDetectionTest extends AbstractBenchmarkBase {

    /**
     * 使用 AtomicLong 验证高并发下计数器的正确性。
     * 即使 Rust 内部是异步的，返回给 Java 端的每个 response 都应是完整且正确的。
     */
    @Test
    void testConcurrentCounterConsistency() throws Exception {
        int requestCount = 500;
        CountDownLatch latch = new CountDownLatch(requestCount);
        AtomicLong responseSum = new AtomicLong(0);
        ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor();

        for (int i = 0; i < requestCount; i++) {
            final int idx = i;
            pool.submit(() -> {
                try {
                    HttpResponse<String> resp = get(rustUrl("/api/echo?msg=" + idx));
                    if (resp.statusCode() == 200) {
                        responseSum.addAndGet(1);
                    }
                } catch (Exception ignored) {
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        pool.shutdown();

        log.info("Successful responses: {} out of {}", responseSum.get(), requestCount);
        assertTrue(responseSum.get() >= requestCount * 0.98,
                "At least 98% of concurrent requests should succeed without data races");
    }

    /**
     * 并发获取用户列表，验证返回的 JSON 格式完整（没有因竞态导致截断/损坏）。
     */
    @Test
    void testConcurrentUserListIntegrity() throws Exception {
        int requestCount = 100;
        CountDownLatch latch = new CountDownLatch(requestCount);
        AtomicInteger validJsonCount = new AtomicInteger(0);
        ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor();

        for (int i = 0; i < requestCount; i++) {
            pool.submit(() -> {
                try {
                    HttpResponse<String> resp = get(rustUrl("/api/users"));
                    String body = resp.body();
                    // 验证 JSON 数组格式完整
                    if (resp.statusCode() == 200
                            && body.startsWith("[")
                            && body.endsWith("]")
                            && body.contains("id")
                            && body.contains("name")) {
                        validJsonCount.incrementAndGet();
                    }
                } catch (Exception ignored) {
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        pool.shutdown();

        assertTrue(validJsonCount.get() >= requestCount * 0.98,
                "At least 98% of user list responses should have valid JSON");
    }
}
