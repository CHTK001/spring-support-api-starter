package com.chua.starter.rustserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.foreign.ValueLayout.*;

/**
 * Panama FFI 绑定层 — 异步 FFI + 虚拟线程 + JDK 25 兼容。
 *
 * <p>Rust 端不阻塞 tokio worker，Java 回调在虚拟线程池中执行。
 * 请求完成时通过 {@code rust_send_response} 通知 Rust。</p>
 */
public class NativeBridge implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(NativeBridge.class);
    private static final Linker LINKER = Linker.nativeLinker();
    private static final int JDK_VERSION = Runtime.version().feature();

    /** Rust server 句柄 */
    private final MemorySegment serverHandle;
    /** 共享 Arena */
    private final Arena arena;
    /** rust_server_stop MethodHandle */
    private final MethodHandle rustServerStop;
    /** rust_server_stop_graceful */
    private final MethodHandle rustServerStopGraceful;
    /** rust_server_set_ws_callback */
    private final MethodHandle rustServerSetWsCallback;
    /** rust_ws_send */
    private final MethodHandle rustWsSend;
    /** rust_ws_close */
    private final MethodHandle rustWsClose;
    /** rust_send_response — 通知 Rust 响应已就绪 */
    private final MethodHandle rustServerSendResponse;
    /** 虚拟线程池 — 用于异步处理请求，不阻塞 tokio worker */
    private final ExecutorService requestExecutor = Executors.newVirtualThreadPerTaskExecutor();

    /**
     * 构造并启动 Rust 服务器（兼容 3-arg 调用，workerThreads 自动 = CPU×2）。
     */
    public NativeBridge(int port, int responseCapacity, RequestCallback callback) {
        this(port, responseCapacity, callback, 0);
    }

    /**
     * 构造并启动 Rust 服务器（HTTP）。
     *
     * @param port             监听端口
     * @param responseCapacity 响应缓冲区大小
     * @param callback         Java 回调
     * @param workerThreads    tokio worker 线程数（≤0 时自动 = CPU×2）
     */
    public NativeBridge(int port, int responseCapacity, RequestCallback callback, int workerThreads) {
        this(port, responseCapacity, callback, workerThreads, false, null, null);
    }

    /**
     * 构造并启动 Rust 服务器（支持 HTTP/TLS）。
     *
     * @param port             监听端口
     * @param responseCapacity 响应缓冲区大小
     * @param callback         Java 回调
     * @param workerThreads    tokio worker 线程数（≤0 时自动 = CPU×2）
     * @param sslEnabled       是否启用 TLS
     * @param sslCertPath      PEM 证书路径
     * @param sslKeyPath       PEM 私钥路径
     */
    public NativeBridge(int port, int responseCapacity, RequestCallback callback, int workerThreads,
                        boolean sslEnabled, String sslCertPath, String sslKeyPath) {
        checkJdkVersion();
        this.arena = Arena.ofShared();

        try {
            SymbolLookup lookup = loadLibrary();

            // ── rust_send_response (先查找，因为 upcall 中需要) ──
            MemorySegment sendRespFunc = lookup.find("rust_send_response")
                    .orElseThrow(() -> new UnsatisfiedLinkError("Cannot find rust_send_response"));
            this.rustServerSendResponse = LINKER.downcallHandle(
                    sendRespFunc,
                    FunctionDescriptor.ofVoid(JAVA_LONG)
            );

            // ── safeCallback: 异步包装，不阻塞 Rust tokio 线程 ──
            RequestCallback safeCallback = (rid,
                    method, methodLen,
                    path, pathLen,
                    headers, headersLen,
                    body, bodyLen,
                    responseData, resCapacity) -> {

                // 在 upcall 上下文中：拷贝所有数据
                byte[] methodCopy = methodLen > 0 ? method.reinterpret(methodLen).toArray(JAVA_BYTE) : new byte[0];
                byte[] pathCopy = pathLen > 0 ? path.reinterpret(pathLen).toArray(JAVA_BYTE) : new byte[0];
                byte[] headerCopy = headersLen > 0 ? headers.reinterpret(headersLen).toArray(JAVA_BYTE) : new byte[0];
                byte[] bodyCopy = bodyLen > 0 ? body.reinterpret(bodyLen).toArray(JAVA_BYTE) : new byte[0];

                // 创建全局作用域的响应段（upcall 返回后仍然有效）
                final MemorySegment respSeg;
                if (resCapacity > 0 && responseData.address() != 0) {
                    respSeg = MemorySegment.ofAddress(responseData.address()).reinterpret(resCapacity);
                } else {
                    respSeg = responseData;
                }
                final int rCap = resCapacity;

                // 提交到虚拟线程池处理
                requestExecutor.submit(() -> {
                    try {
                        MemorySegment methodSeg2 = MemorySegment.ofArray(methodCopy);
                        MemorySegment pathSeg2 = MemorySegment.ofArray(pathCopy);
                        MemorySegment headersSeg2 = headerCopy.length > 0
                                ? MemorySegment.ofArray(headerCopy) : MemorySegment.NULL;
                        MemorySegment bodySeg2 = bodyCopy.length > 0
                                ? MemorySegment.ofArray(bodyCopy) : MemorySegment.NULL;

                        callback.onRequest(rid,
                                methodSeg2, methodCopy.length,
                                pathSeg2, pathCopy.length,
                                headersSeg2, headerCopy.length,
                                bodySeg2, bodyCopy.length,
                                respSeg, rCap);

                        // 通知 Rust 响应已就绪
                        rustServerSendResponse.invoke(rid);
                    } catch (Throwable e) {
                        log.error("[RustServer] Async callback error: {}", e.getMessage(), e);
                        try {
                            if (respSeg != null && rCap >= 10) {
                                writeErrorResponse(respSeg, rCap, 500, e.getMessage());
                            }
                        } catch (Throwable ignored) {}
                        try {
                            rustServerSendResponse.invoke(rid);
                        } catch (Throwable ignored) {}
                    }
                });
            };

            // ── Upcall Stub (safeCallback 作为 FFI 回调) ──
            MethodHandle callbackHandle = MethodHandles.lookup()
                    .findVirtual(RequestCallback.class, "onRequest",
                            MethodType.methodType(void.class,
                                    long.class,
                                    MemorySegment.class, int.class,
                                    MemorySegment.class, int.class,
                                    MemorySegment.class, int.class,
                                    MemorySegment.class, int.class,
                                    MemorySegment.class, int.class))
                    .bindTo(safeCallback);

            MemorySegment callbackStub = LINKER.upcallStub(
                    callbackHandle,
                    FunctionDescriptor.ofVoid(
                            JAVA_LONG,
                            ADDRESS, JAVA_INT,
                            ADDRESS, JAVA_INT,
                            ADDRESS, JAVA_INT,
                            ADDRESS, JAVA_INT,
                            ADDRESS, JAVA_INT
                    ),
                    arena
            );

            // ── rust_server_start / rust_server_start_tls ──
            log.info("[RustServer] Starting on port {} (responseBuffer={}, workerThreads={}, jdk={}, ssl={})",
                    port, responseCapacity, workerThreads, JDK_VERSION, sslEnabled);

            if (sslEnabled) {
                byte[] certBytes = loadSslFile(sslCertPath, "certificate");
                byte[] keyBytes = loadSslFile(sslKeyPath, "private key");

                MemorySegment tlsStartFunc = lookup.find("rust_server_start_tls")
                        .orElseThrow(() -> new UnsatisfiedLinkError("Cannot find rust_server_start_tls"));

                MethodHandle rustServerStartTls = LINKER.downcallHandle(
                        tlsStartFunc,
                        FunctionDescriptor.of(ADDRESS, JAVA_INT, JAVA_INT, ADDRESS,
                                JAVA_INT, ADDRESS, JAVA_INT, ADDRESS, JAVA_INT)
                );

                MemorySegment certSeg = MemorySegment.ofArray(certBytes);
                MemorySegment keySeg = MemorySegment.ofArray(keyBytes);
                this.serverHandle = (MemorySegment) rustServerStartTls.invoke(
                        port, responseCapacity, callbackStub,
                        workerThreads,
                        certSeg, certBytes.length,
                        keySeg, keyBytes.length);
            } else {
                MemorySegment startFunc = lookup.find("rust_server_start")
                        .orElseThrow(() -> new UnsatisfiedLinkError("Cannot find rust_server_start"));

                MethodHandle rustServerStart = LINKER.downcallHandle(
                        startFunc,
                        FunctionDescriptor.of(ADDRESS, JAVA_INT, JAVA_INT, ADDRESS, JAVA_INT)
                );
                this.serverHandle = (MemorySegment) rustServerStart.invoke(port, responseCapacity, callbackStub, workerThreads);
            }

            if (serverHandle.address() == 0) {
                throw new RuntimeException("rust_server_start returned null");
            }

            // ── rust_server_stop ──
            MemorySegment stopFunc = lookup.find("rust_server_stop")
                    .orElseThrow(() -> new UnsatisfiedLinkError("Cannot find rust_server_stop"));
            this.rustServerStop = LINKER.downcallHandle(
                    stopFunc,
                    FunctionDescriptor.ofVoid(ADDRESS)
            );

            // ── rust_server_stop_graceful ──
            MemorySegment stopGracefulFunc = lookup.find("rust_server_stop_graceful")
                    .orElseThrow(() -> new UnsatisfiedLinkError("Cannot find rust_server_stop_graceful"));
            this.rustServerStopGraceful = LINKER.downcallHandle(
                    stopGracefulFunc,
                    FunctionDescriptor.ofVoid(ADDRESS, JAVA_INT)
            );

            log.info("[RustServer] Started successfully (ssl={})", sslEnabled);

            // JVM 退出时清理 arena
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try { arena.close(); } catch (Throwable ignored) {}
            }));

            // ── rust_server_set_ws_callback ──
            MemorySegment setWsCbFunc = lookup.find("rust_server_set_ws_callback")
                    .orElseThrow(() -> new UnsatisfiedLinkError("Cannot find rust_server_set_ws_callback"));
            this.rustServerSetWsCallback = LINKER.downcallHandle(
                    setWsCbFunc,
                    FunctionDescriptor.ofVoid(ADDRESS)
            );

            // ── rust_ws_send ──
            MemorySegment wsSendFunc = lookup.find("rust_ws_send")
                    .orElseThrow(() -> new UnsatisfiedLinkError("Cannot find rust_ws_send"));
            this.rustWsSend = LINKER.downcallHandle(
                    wsSendFunc,
                    FunctionDescriptor.of(JAVA_INT, JAVA_LONG, ADDRESS, JAVA_INT, JAVA_INT)
            );

            // ── rust_ws_close ──
            MemorySegment wsCloseFunc = lookup.find("rust_ws_close")
                    .orElseThrow(() -> new UnsatisfiedLinkError("Cannot find rust_ws_close"));
            this.rustWsClose = LINKER.downcallHandle(
                    wsCloseFunc,
                    FunctionDescriptor.ofVoid(JAVA_LONG)
            );

        } catch (Throwable e) {
            arena.close();
            throw new RuntimeException("Failed to initialize Rust native bridge", e);
        }
    }

    /** 立即停止服务器 */
    public void stop() {
        stop(0);
    }

    /**
     * 停止服务器。
     *
     * @param timeoutMs 等待 in-flight 请求完成的超时时间（毫秒），0 表示立即停止
     */
    public void stop(int timeoutMs) {
        try {
            // 1. 先关闭虚拟线程池
            requestExecutor.shutdown();
            if (timeoutMs > 0) {
                try {
                    requestExecutor.awaitTermination(timeoutMs, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // 2. 通知 Rust 停止
            if (serverHandle != null && serverHandle.address() != 0) {
                if (timeoutMs > 0) {
                    rustServerStopGraceful.invoke(serverHandle, timeoutMs);
                    log.info("[RustServer] Graceful stop initiated (timeout={}ms)", timeoutMs);
                } else {
                    rustServerStop.invoke(serverHandle);
                    log.info("[RustServer] Stopped");
                }
            }
        } catch (Throwable e) {
            log.warn("[RustServer] Stop error: {}", e.getMessage());
        }
    }

    @Override
    public void close() { stop(); }

    // ═════════════════════════════════════════════════════
    //  WebSocket 支持
    // ═════════════════════════════════════════════════════

    /**
     * 注册 WebSocket 事件回调。
     */
    public void setWsCallback(WsCallback callback) {
        try {
            WsCallback safeCallback = (connId, data, dataLen, msgType) -> {
                try {
                    MemorySegment safeData = (dataLen > 0 && data != null && data.address() != 0)
                            ? data.reinterpret(dataLen)
                            : data;
                    callback.onEvent(connId, safeData, dataLen, msgType);
                } catch (Exception e) {
                    log.error("[RustServer] WS callback error: {}", e.getMessage(), e);
                }
            };

            MethodHandle cbHandle = MethodHandles.lookup()
                    .findVirtual(WsCallback.class, "onEvent",
                            MethodType.methodType(void.class,
                                    long.class, MemorySegment.class, int.class, int.class))
                    .bindTo(safeCallback);

            MemorySegment cbStub = LINKER.upcallStub(
                    cbHandle,
                    FunctionDescriptor.ofVoid(JAVA_LONG, ADDRESS, JAVA_INT, JAVA_INT),
                    arena
            );

            rustServerSetWsCallback.invoke(cbStub);
            log.info("[RustServer] WebSocket callback registered");
        } catch (Throwable e) {
            throw new RuntimeException("Failed to register WS callback", e);
        }
    }

    /**
     * 发送 WebSocket 消息。
     *
     * @param connId  连接 ID
     * @param data    消息数据
     * @param msgType 消息类型（1=TEXT, 2=BINARY）
     * @return 0=成功, -1=连接不存在
     */
    public int wsSend(long connId, byte[] data, int msgType) {
        try {
            MemorySegment dataSeg = MemorySegment.ofArray(data);
            return (int) rustWsSend.invoke(connId, dataSeg, data.length, msgType);
        } catch (Throwable e) {
            log.error("[RustServer] WS send error: {}", e.getMessage());
            return -1;
        }
    }

    /**
     * 关闭 WebSocket 连接。
     *
     * @param connId 连接 ID
     */
    public void wsClose(long connId) {
        try {
            rustWsClose.invoke(connId);
        } catch (Throwable e) {
            log.error("[RustServer] WS close error: {}", e.getMessage());
        }
    }

    // ═════════════════════════════════════════════════════
    //  写入错误响应到共享缓冲区
    // ═════════════════════════════════════════════════════

    private static void writeErrorResponse(MemorySegment buf, int capacity, int status, String msg) {
        if (buf == null || capacity < 10) return;
        byte[] msgBytes = msg != null ? msg.getBytes(java.nio.charset.StandardCharsets.UTF_8) : new byte[0];
        int totalLen = 10 + msgBytes.length;
        if (totalLen > capacity) {
            int maxBody = capacity - 10;
            if (maxBody < 0) maxBody = 0;
            msgBytes = java.util.Arrays.copyOf(msgBytes, maxBody);
        }
        buf.set(JAVA_SHORT_UNALIGNED, 0, (short) ((status >> 8) & 0xFF | (status & 0xFF) << 8));
        buf.set(JAVA_INT_UNALIGNED, 2, 0);
        buf.set(JAVA_INT_UNALIGNED, 6, msgBytes.length);
        if (msgBytes.length > 0) {
            MemorySegment.copy(MemorySegment.ofArray(msgBytes), 0, buf, 10, msgBytes.length);
        }
    }

    // ═════════════════════════════════════════════════════
    //  JDK 版本检测
    // ═════════════════════════════════════════════════════

    private static void checkJdkVersion() {
        if (JDK_VERSION < 21) {
            throw new UnsupportedOperationException(
                    "JDK 21+ required for Panama FFI. Current: " + JDK_VERSION);
        }
        if (JDK_VERSION == 21) {
            log.warn("[RustServer] JDK 21 detected: Panama FFI is preview, add --enable-preview");
        }
    }

    // ═════════════════════════════════════════════════════
    //  native 库加载
    // ═════════════════════════════════════════════════════

    private SymbolLookup loadLibrary() {
        String libName = System.mapLibraryName("rust_server");
        log.debug("[RustServer] Looking for native library: {}", libName);

        Path libPath = findLibrary(libName);
        if (libPath == null) {
            throw new UnsatisfiedLinkError(
                    "Cannot find native library: " + libName + "\n" +
                    "Build it: cd src/main/rust && cargo build --release\n" +
                    "Or: mvn compile -Pbuild-rust");
        }

        try {
            if (JDK_VERSION >= 22) {
                Method m = SymbolLookup.class.getMethod("libraryLookup", Path.class, Arena.class);
                return (SymbolLookup) m.invoke(null, libPath, arena);
            }

            Method m = SymbolLookup.class.getMethod("libraryLookup", Path.class);
            SymbolLookup lookup = (SymbolLookup) m.invoke(null, libPath);
            return lookup;

        } catch (NoSuchMethodException e) {
            try {
                Method m = SymbolLookup.class.getMethod("libraryLookup", String.class);
                return (SymbolLookup) m.invoke(null, libPath.toString());
            } catch (Exception ex) {
                throw new RuntimeException("Cannot load native library via reflection", ex);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load native library: " + libPath, e);
        }
    }

    private Path findLibrary(String libName) {
        String libPath = System.getProperty("java.library.path");
        if (libPath != null) {
            for (String dir : libPath.split(System.getProperty("path.separator"))) {
                Path p = Path.of(dir, libName);
                if (Files.exists(p)) return p;
            }
        }

        var libUrl = getClass().getClassLoader().getResource("native/" + libName);
        if (libUrl != null && "file".equals(libUrl.getProtocol())) {
            Path p = urlToPath(libUrl);
            if (Files.exists(p)) return p;
        }

        Path udLib = Path.of(System.getProperty("user.dir"), "native", libName);
        if (Files.exists(udLib)) return udLib;

        Path cargoLib = Path.of("src/main/rust/target/release").resolve(libName);
        if (Files.exists(cargoLib)) return cargoLib;

        return null;
    }

    private static Path urlToPath(java.net.URL url) {
        String path = url.getPath();
        int slashEnd = 0;
        while (slashEnd < path.length() && path.charAt(slashEnd) == '/') slashEnd++;
        if (slashEnd > 0 && slashEnd + 1 < path.length()
                && Character.isLetter(path.charAt(slashEnd))
                && path.charAt(slashEnd + 1) == ':') {
            path = path.substring(slashEnd);
        }
        return Path.of(path);
    }

    /**
     * 加载 SSL 证书/私钥文件。
     * 支持 classpath: 和 file: 前缀。
     */
    private byte[] loadSslFile(String path, String description) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("SSL " + description + " path is required");
        }
        try {
            if (path.startsWith("classpath:")) {
                String classpathPath = path.substring("classpath:".length());
                if (classpathPath.startsWith("/")) classpathPath = classpathPath.substring(1);
                try (var in = getClass().getClassLoader().getResourceAsStream(classpathPath)) {
                    if (in == null) throw new RuntimeException("SSL " + description + " not found: " + path);
                    return in.readAllBytes();
                }
            } else {
                String filePath = path;
                if (filePath.startsWith("file:")) filePath = filePath.substring("file:".length());
                return java.nio.file.Files.readAllBytes(Path.of(filePath));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SSL " + description + ": " + path, e);
        }
    }

    @FunctionalInterface
    public interface RequestCallback {
        void onRequest(
                long requestId,
                MemorySegment method, int methodLen,
                MemorySegment path, int pathLen,
                MemorySegment headers, int headersLen,
                MemorySegment body, int bodyLen,
                MemorySegment responseData, int responseCapacity
        );
    }

    /**
     * WebSocket 事件回调接口。
     *
     * <p>msgType 取值：</p>
     * <ul>
     *   <li>0 (WS_OPEN) — 新连接建立，data=null</li>
     *   <li>1 (WS_TEXT) — 文本消息</li>
     *   <li>2 (WS_BINARY) — 二进制消息</li>
     *   <li>3 (WS_CLOSE) — 连接关闭，data=null</li>
     *   <li>4 (WS_ERROR) — 连接错误，data=null</li>
     * </ul>
     */
    @FunctionalInterface
    public interface WsCallback {
        void onEvent(long connId, MemorySegment data, int dataLen, int msgType);
    }
}
