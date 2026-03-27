import com.chua.common.support.core.annotation.Dispatcher;
import com.chua.common.support.task.debounce.DebounceFlow;
import com.chua.common.support.task.dispatcher.DispatcherFlow;
import com.chua.common.support.task.layer.DefaultLayerManager;
import com.chua.common.support.task.layer.LayerContext;
import com.chua.common.support.task.layer.container.DefaultContainerLayer;
import com.chua.common.support.task.layer.data.DefaultDataLayer;
import com.chua.common.support.task.layer.decision.DefaultDecisionLayer;
import com.chua.common.support.task.layer.network.AbstractNetworkLayer;

import java.util.concurrent.atomic.AtomicInteger;

public class TaskFeatureVerification {

    public static void main(String[] args) throws Exception {
        verifyDebounce();
        verifyDispatcher();
        verifyLayer();
        System.out.println("TASK_FEATURE_VERIFICATION_OK");
    }

    private static void verifyDebounce() {
        DebounceFlow.clearCache();
        AtomicInteger counter = new AtomicInteger();

        String first = DebounceFlow.with(() -> "run-" + counter.incrementAndGet())
                .key("task-feature-debounce")
                .duration(300)
                .fallbackValue("debounced")
                .execute();

        String second = DebounceFlow.with(() -> "run-" + counter.incrementAndGet())
                .key("task-feature-debounce")
                .duration(300)
                .fallbackValue("debounced")
                .execute();

        sleep(350);

        String third = DebounceFlow.with(() -> "run-" + counter.incrementAndGet())
                .key("task-feature-debounce")
                .duration(300)
                .fallbackValue("debounced")
                .execute();

        assertEquals("run-1", first, "debounce first result");
        assertEquals("debounced", second, "debounce second result");
        assertEquals("run-2", third, "debounce third result");
        assertEquals(2, counter.get(), "debounce execution count");
        DebounceFlow.clearCache();
    }

    private static void verifyDispatcher() {
        DispatcherFlow flow = DispatcherFlow.createLocal();
        try {
            flow.register(new SampleHandler()).start();
            Object result = flow.publish(new SampleEvent("ping"));
            assertEquals("handled-ping", result, "dispatcher result");
        } finally {
            flow.close();
        }
    }

    private static void verifyLayer() throws Exception {
        DefaultDecisionLayer decisionLayer = new DefaultDecisionLayer("decision");
        DefaultContainerLayer containerLayer = new DefaultContainerLayer("container");
        StubNetworkLayer networkLayer = new StubNetworkLayer("network");
        DefaultDataLayer dataLayer = new DefaultDataLayer("data");

        dataLayer.start();
        dataLayer.setMaskEnabled(true);
        dataLayer.addMaskRule("phone", "phone");
        LayerContext maskContext = new LayerContext("http").setRequest("13812345678");
        Object masked = dataLayer.maskRequest(maskContext);
        assertEquals("138****5678", masked, "data layer mask result");

        dataLayer.setCacheEnabled(true);
        LayerContext cacheContext = new LayerContext("http").setAttribute("path", "/demo");
        dataLayer.setCache(cacheContext, "cached-value", 3);
        dataLayer.process(cacheContext);
        assertEquals("cached-value", cacheContext.getResponse(), "data layer cache result");
        assertEquals(Boolean.TRUE, cacheContext.getAttribute("fromCache"), "data layer cache marker");
        dataLayer.stop();

        containerLayer.registerNetwork(networkLayer);
        decisionLayer.registerContainer("http", containerLayer);

        DefaultLayerManager layerManager = new DefaultLayerManager();
        layerManager.registerProtocolHandler("http", decisionLayer);
        layerManager.registerLayer(containerLayer);
        layerManager.registerLayer(networkLayer);
        layerManager.start();
        try {
            LayerContext context = new LayerContext("http")
                    .setClientIp("127.0.0.1")
                    .setRequest("payload");
            layerManager.process(context);

            assertEquals("forwarded:payload", context.getResponse(), "layer manager response");
            assertEquals("container", context.getSelectedContainer().getName(), "selected container");
            assertEquals("network", context.getSelectedNetwork().getName(), "selected network");
            assertEquals(false, context.isTerminated(), "layer terminated flag");
        } finally {
            layerManager.stop();
        }
    }

    private static void assertEquals(Object expected, Object actual, String label) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new IllegalStateException(label + " expected=" + expected + " actual=" + actual);
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public static class SampleEvent {
        private final String payload;

        public SampleEvent(String payload) {
            this.payload = payload;
        }

        public String getPayload() {
            return payload;
        }
    }

    public static class SampleHandler {
        @Dispatcher("sample")
        public String handle(SampleEvent event) {
            return "handled-" + event.getPayload();
        }
    }

    public static class StubNetworkLayer extends AbstractNetworkLayer {

        public StubNetworkLayer(String name) {
            setName(name);
            setProtocol("http");
            setAddress("127.0.0.1:8080");
        }

        @Override
        public void forward(LayerContext context) {
            context.setSelectedNetwork(this);
            context.setResponse("forwarded:" + context.getRequest());
        }
    }
}
