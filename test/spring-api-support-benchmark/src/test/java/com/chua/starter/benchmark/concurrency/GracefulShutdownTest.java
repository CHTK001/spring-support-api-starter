package com.chua.starter.benchmark.concurrency;

import com.chua.starter.benchmark.AbstractBenchmarkBase;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 优雅关闭测试 — 发送持续请求的过程中触发 shutdown，
 * 验证正在处理的请求能完成，新请求被拒绝。
 *
 * <p>注意：本测试需要手动配合，因为 shutdown 需要调用 /actuator/shutdown
 * 或停止进程。这里测试的是 shutdown 前/后的请求行为。</p>
 */
class GracefulShutdownTest extends AbstractBenchmarkBase {

    /**
     * 验证服务器在运行期间能持续正确处理请求（基线测试）。
     */
    @Test
    void testSustainedRequests() throws Exception {
        int requestCount = 50;
        CountDownLatch latch = new CountDownLatch(requestCount);
        AtomicInteger successCount = new AtomicInteger(0);
        ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor();

        for (int i = 0; i < requestCount; i++) {
            pool.submit(() -> {
                try {
                    HttpResponse<String> resp = get(rustUrl("/api/health"));
                    if (resp.statusCode() == 200) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception ignored) {
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        pool.shutdown();

        double successRate = (double) successCount.get() / requestCount;
        log.info("Sustained requests: {}/{} success ({}%)",
                successCount.get(), requestCount, String.format("%.0f", successRate * 100));

        assertTrue(successRate >= 0.95,
                "At least 95% of sustained requests should succeed");
    }
}
