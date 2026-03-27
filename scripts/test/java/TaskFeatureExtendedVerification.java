import com.chua.common.support.network.discovery.DefaultServiceDiscovery;
import com.chua.common.support.network.discovery.Discovery;
import com.chua.common.support.task.dispatcher.DispatcherFlow;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskFeatureExtendedVerification {

    public static void main(String[] args) throws Exception {
        run("chronicle", TaskFeatureExtendedVerification::verifyChronicle);
        run("discovery", TaskFeatureExtendedVerification::verifyDiscovery);
    }

    private static void run(String name, ThrowingRunnable runnable) {
        try {
            runnable.run();
        } catch (Throwable e) {
            System.out.println(name.toUpperCase() + "_ERROR:" + e.getClass().getSimpleName() + ":" + e.getMessage());
        }
    }

    private static void verifyChronicle() throws Exception {
        Path tempDir = Files.createTempDirectory("chronicle-dispatch-test");
        AtomicInteger counter = new AtomicInteger();
        DispatcherFlow flow = DispatcherFlow.createChronicle(tempDir.toString(), "node-test");
        try {
            flow.register(new CountingHandler(counter)).start();
            flow.publish(new CountingEvent("chronicle"));
            waitUntil(counter, 1, 3000);
            System.out.println("CHRONICLE_DISPATCH_OK:" + counter.get());
        } finally {
            try {
                flow.close();
            } catch (Throwable e) {
                System.out.println("CHRONICLE_CLOSE_DEFECT:" + e.getClass().getSimpleName() + ":" + e.getMessage());
            }
        }
    }

    private static void verifyDiscovery() throws Exception {
        DefaultServiceDiscovery discovery = new DefaultServiceDiscovery();
        discovery.registerService("/.dispatch", Discovery.builder()
                .id("dispatch-1")
                .serverId("dispatch-1")
                .protocol("guava")
                .host("127.0.0.1")
                .port(8080)
                .build());

        AtomicInteger counter = new AtomicInteger();
        DispatcherFlow flow = DispatcherFlow.createDiscovery(discovery);
        try {
            flow.register(new CountingHandler(counter)).start();
            Object result = flow.publish(new CountingEvent("discovery"));
            Thread.sleep(500);
            if (counter.get() == 0 && result == null) {
                System.out.println("DISCOVERY_DISPATCH_DEFECT:handler_not_registered_or_provider_not_started");
                return;
            }
            System.out.println("DISCOVERY_DISPATCH_OK:" + counter.get());
        } finally {
            flow.close();
        }
    }

    private static void waitUntil(AtomicInteger counter, int expected, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (counter.get() >= expected) {
                return;
            }
            Thread.sleep(50);
        }
        throw new IllegalStateException("timeout waiting for counter=" + expected + ", actual=" + counter.get());
    }

    public static class CountingEvent {
        private final String payload;

        public CountingEvent(String payload) {
            this.payload = payload;
        }

        public String getPayload() {
            return payload;
        }
    }

    public static class CountingHandler {
        private final AtomicInteger counter;

        public CountingHandler(AtomicInteger counter) {
            this.counter = counter;
        }

        @com.chua.common.support.core.annotation.Dispatcher("counting")
        public String handle(CountingEvent event) {
            counter.incrementAndGet();
            return event.getPayload();
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
