package com.chua.starter.rustserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.server.WebServerException;

import java.lang.foreign.MemorySegment;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.foreign.ValueLayout.*;

/**
 * Rust 驱动的 {@link WebServer} 实现。
 *
 * <p>通过 Panama FFI 启动 Rust (tokio+hyper) HTTP 服务器。
 * 请求到达时 Rust 同步回调 Java，解析 {@link HttpRequestData} 后分发给 {@link RequestHandler} 处理。</p>
 */
public class RustWebServer implements WebServer {

    private static final Logger log = LoggerFactory.getLogger(RustWebServer.class);

    /** 业务请求处理器 */
    @FunctionalInterface
    public interface RequestHandler {
        void handle(HttpRequestData request) throws Exception;
    }

    private final int port;
    private final int responseCapacity;
    private final int workerThreads;
    private final RequestHandler requestHandler;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final CountDownLatch keepAliveLatch = new CountDownLatch(1);
    private final int shutdownTimeoutMs;
    private final boolean sslEnabled;
    private final String sslCertPath;
    private final String sslKeyPath;
    private volatile NativeBridge nativeBridge;
    private NativeBridge.WsCallback wsCallback;

    /**
     * @param port             监听端口
     * @param responseCapacity 响应缓冲区大小
     * @param requestHandler   请求处理器
     */
    public RustWebServer(int port, int responseCapacity, RequestHandler requestHandler) {
        this(port, responseCapacity, requestHandler, 30000, 0, false, null, null);
    }

    /**
     * @param port             监听端口
     * @param responseCapacity 响应缓冲区大小
     * @param requestHandler   请求处理器
     * @param shutdownTimeoutMs 优雅关闭超时（毫秒），默认 30000
     */
    public RustWebServer(int port, int responseCapacity, RequestHandler requestHandler, int shutdownTimeoutMs) {
        this(port, responseCapacity, requestHandler, shutdownTimeoutMs, 0, false, null, null);
    }

    /**
     * @param port             监听端口
     * @param responseCapacity 响应缓冲区大小
     * @param requestHandler   请求处理器
     * @param shutdownTimeoutMs 优雅关闭超时
     * @param workerThreads    tokio worker 线程数（≤0 时自动 = CPU×2）
     * @param sslEnabled       是否启用 HTTPS
     * @param sslCertPath      PEM 证书路径
     * @param sslKeyPath       PEM 私钥路径
     */
    public RustWebServer(int port, int responseCapacity, RequestHandler requestHandler,
                         int shutdownTimeoutMs, int workerThreads, boolean sslEnabled,
                         String sslCertPath, String sslKeyPath) {
        this.port = port;
        this.responseCapacity = responseCapacity;
        this.workerThreads = workerThreads;
        this.requestHandler = requestHandler;
        this.shutdownTimeoutMs = shutdownTimeoutMs;
        this.sslEnabled = sslEnabled;
        this.sslCertPath = sslCertPath;
        this.sslKeyPath = sslKeyPath;
    }

    @Override
    public void start() throws WebServerException {
        if (running.compareAndSet(false, true)) {
            try {
                this.nativeBridge = new NativeBridge(port, responseCapacity, this::onRustRequest, workerThreads,
                        sslEnabled, sslCertPath, sslKeyPath);
                // 注册 WebSocket 回调
                if (wsCallback != null) {
                    nativeBridge.setWsCallback(wsCallback);
                }
                Thread keepAliveThread = new Thread(() -> {
                    try {
                        keepAliveLatch.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }, "rust-server-keepalive");
                keepAliveThread.setDaemon(false);
                keepAliveThread.start();
                log.info("[RustWebServer] Started on port {} (ssl={})", port, sslEnabled);
            } catch (Exception e) {
                running.set(false);
                throw new WebServerException("Failed to start RustWebServer", e);
            }
        }
    }

    @Override
    public void stop() throws WebServerException {
        if (running.compareAndSet(true, false)) {
            keepAliveLatch.countDown();
            try {
                if (nativeBridge != null) {
                    nativeBridge.stop(shutdownTimeoutMs);
                }
            } catch (Exception e) {
                log.warn("[RustWebServer] Error stopping: {}", e.getMessage());
            }
        }
    }

    @Override
    public int getPort() { return port; }

    /**
     * Rust 线程同步回调 — 解析请求数据并分发给业务处理器。
     */
    private void onRustRequest(
            long requestId,
            MemorySegment method, int methodLen,
            MemorySegment path, int pathLen,
            MemorySegment headers, int headersLen,
            MemorySegment body, int bodyLen,
            MemorySegment responseData, int responseCapacity) {

        try {
            HttpRequestData request = HttpRequestData.parse(
                    requestId,
                    method, methodLen,
                    path, pathLen,
                    headers, headersLen,
                    body, bodyLen,
                    responseData, responseCapacity);

            requestHandler.handle(request);
        } catch (Exception e) {
            log.error("[RustWebServer] Error processing request {}: {}", requestId, e.getMessage());
            writeErrorResponse(responseData, responseCapacity, 500, e.getMessage());
        }
    }

    static void writeErrorResponse(MemorySegment buf, int capacity, int status, String message) {
        if (capacity < 10) return;
        byte[] msgBytes = message != null
                ? message.getBytes(java.nio.charset.StandardCharsets.UTF_8)
                : new byte[0];
        int maxBody = Math.min(msgBytes.length, capacity - 10);
        if (maxBody < 0) maxBody = 0;

        buf.set(JAVA_SHORT_UNALIGNED, 0, (short) ((status >> 8) & 0xFF | (status & 0xFF) << 8));
        buf.set(JAVA_INT_UNALIGNED, 2, 0);
        buf.set(JAVA_INT_UNALIGNED, 6, maxBody);
        for (int i = 0; i < maxBody; i++) {
            buf.set(JAVA_BYTE, 10 + i, msgBytes[i]);
        }
    }

    public boolean isRunning() { return running.get(); }

    /**
     * 设置 WebSocket 事件回调。
     * <p>必须在 {@link #start()} 之前调用才生效。</p>
     */
    public void setWsCallback(NativeBridge.WsCallback callback) {
        this.wsCallback = callback;
    }
}
