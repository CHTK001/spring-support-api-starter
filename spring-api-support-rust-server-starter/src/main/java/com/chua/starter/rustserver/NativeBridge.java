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
import java.util.function.Consumer;

import static java.lang.foreign.ValueLayout.*;

/**
 * Panama FFI 绑定层 — 兼容 JDK 21 (预览) / JDK 22+ (正式)。
 *
 * <p>通过反射调用 {@link SymbolLookup#libraryLookup}，隔离 JDK 21 与 22+ 的 API 签名差异。
 * JDK 21 需添加 JVM 参数 {@code --enable-preview}。</p>
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

    /**
     * 构造并启动 Rust 服务器。
     *
     * @param port             监听端口
     * @param responseCapacity 响应缓冲区大小
     * @param callback         Java 回调
     */
    public NativeBridge(int port, int responseCapacity, RequestCallback callback) {
        checkJdkVersion();
        this.arena = Arena.ofShared();

        try {
            SymbolLookup lookup = loadLibrary();

            // ── Upcall Stub (Java 回调 → Rust 调用) ──
            MethodHandle callbackHandle = MethodHandles.lookup()
                    .findVirtual(RequestCallback.class, "onRequest",
                            MethodType.methodType(void.class,
                                    long.class,
                                    MemorySegment.class, int.class,
                                    MemorySegment.class, int.class,
                                    MemorySegment.class, int.class,
                                    MemorySegment.class, int.class,
                                    MemorySegment.class, int.class))
                    .bindTo(callback);

            MemorySegment callbackStub = LINKER.upcallStub(
                    callbackHandle,
                    FunctionDescriptor.ofVoid(
                            JAVA_LONG,      // request_id
                            ADDRESS,        // method
                            JAVA_INT,       // method_len
                            ADDRESS,        // path
                            JAVA_INT,       // path_len
                            ADDRESS,        // headers
                            JAVA_INT,       // headers_len
                            ADDRESS,        // body
                            JAVA_INT,       // body_len
                            ADDRESS,        // response_ptr
                            JAVA_INT        // response_capacity
                    ),
                    arena
            );

            // ── rust_server_start ──
            MemorySegment startFunc = lookup.find("rust_server_start")
                    .orElseThrow(() -> new UnsatisfiedLinkError("Cannot find rust_server_start"));

            MethodHandle rustServerStart = LINKER.downcallHandle(
                    startFunc,
                    FunctionDescriptor.of(ADDRESS, JAVA_INT, JAVA_INT, ADDRESS)
            );

            // ── rust_server_stop ──
            MemorySegment stopFunc = lookup.find("rust_server_stop")
                    .orElseThrow(() -> new UnsatisfiedLinkError("Cannot find rust_server_stop"));

            this.rustServerStop = LINKER.downcallHandle(
                    stopFunc,
                    FunctionDescriptor.ofVoid(ADDRESS)
            );

            // ── 启动 ──
            log.info("[RustServer] Starting on port {} (responseBuffer={}, jdk={})",
                    port, responseCapacity, JDK_VERSION);

            this.serverHandle = (MemorySegment) rustServerStart.invoke(port, responseCapacity, callbackStub);

            if (serverHandle.address() == 0) {
                throw new RuntimeException("rust_server_start returned null");
            }
            log.info("[RustServer] Started successfully");

        } catch (Throwable e) {
            arena.close();
            throw new RuntimeException("Failed to initialize Rust native bridge", e);
        }
    }

    /** 停止服务器 */
    public void stop() {
        try {
            if (serverHandle != null && serverHandle.address() != 0) {
                rustServerStop.invoke(serverHandle);
                log.info("[RustServer] Stopped");
            }
        } catch (Throwable e) {
            log.warn("[RustServer] Stop error: {}", e.getMessage());
        } finally {
            arena.close();
        }
    }

    @Override
    public void close() { stop(); }

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
            // 尝试检测是否启用了预览
            try {
                Class.forName("java.lang.foreign.Linker");
            } catch (ClassNotFoundException e) {
                // 实际上在 JDK 21 中 Linker 是存在的，即使没有 --enable-preview
                // 但 API 的使用会在运行时报错
            }
        }
    }

    // ═════════════════════════════════════════════════════
    //  native 库加载 (兼容 JDK 21 / 22+)
    // ═════════════════════════════════════════════════════

    /**
     * 加载 librust_server 动态库，兼容 JDK 21 与 JDK 22+ 的 API 差异。
     *
     * <p>JDK 21: {@code SymbolLookup.libraryLookup(String)}<br>
     * JDK 22+: {@code SymbolLookup.libraryLookup(String, Arena)}</p>
     */
    private SymbolLookup loadLibrary() {
        String libName = System.mapLibraryName("rust_server");
        log.debug("[RustServer] Looking for native library: {}", libName);

        // 找文件
        Path libPath = findLibrary(libName);
        if (libPath == null) {
            throw new UnsatisfiedLinkError(
                    "Cannot find native library: " + libName + "\n" +
                    "Build it: cd src/main/rust && cargo build --release\n" +
                    "Or: mvn compile -Pbuild-rust");
        }

        try {
            // JDK 22+: libraryLookup(Path, Arena) — 最优先
            if (JDK_VERSION >= 22) {
                Method m = SymbolLookup.class.getMethod("libraryLookup", Path.class, Arena.class);
                return (SymbolLookup) m.invoke(null, libPath, arena);
            }

            // JDK 21: libraryLookup(Path)
            Method m = SymbolLookup.class.getMethod("libraryLookup", Path.class);
            SymbolLookup lookup = (SymbolLookup) m.invoke(null, libPath);

            // JDK 21 返回的 lookup 需要手动管理生命周期，但 Arena 不适用
            // 返回后正常使用，JVM 退出时自动释放
            return lookup;

        } catch (NoSuchMethodException e) {
            // 最后的回退：libraryLookup(String) — 最老的 API
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

    /**
     * 按优先级搜索 native 库。
     * 1. java.library.path
     * 2. classpath native/
     * 3. user.dir/native/
     * 4. cargo 构建产物
     */
    private Path findLibrary(String libName) {
        // 1) java.library.path
        String libPath = System.getProperty("java.library.path");
        if (libPath != null) {
            for (String dir : libPath.split(System.getProperty("path.separator"))) {
                Path p = Path.of(dir, libName);
                if (Files.exists(p)) return p;
            }
        }

        // 2) classpath native/ — 转成文件路径
        var libUrl = getClass().getClassLoader().getResource("native/" + libName);
        if (libUrl != null && "file".equals(libUrl.getProtocol())) {
            Path p = Path.of(libUrl.getPath());
            if (Files.exists(p)) return p;
        }

        // 3) user.dir/native/
        Path udLib = Path.of(System.getProperty("user.dir"), "native", libName);
        if (Files.exists(udLib)) return udLib;

        // 4) cargo 产物 (target/release/)
        Path cargoLib = Path.of("src/main/rust/target/release").resolve(libName);
        if (Files.exists(cargoLib)) return cargoLib;

        return null;
    }

    // ═════════════════════════════════════════════════════
    //  回调接口
    // ═════════════════════════════════════════════════════

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
}
