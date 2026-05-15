package com.chua.starter.rustserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RustServerProvider} 默认实现。
 *
 * <p>检查 JDK 版本（需 ≥21 以支持 Panama FFI）和 native 库文件是否存在。
 * 多版本 JDK 兼容：JDK 8~20 返回不可用，JDK 21 起检测库文件。</p>
 */
public class NativeRustServerProvider implements RustServerProvider {

    private static final Logger log = LoggerFactory.getLogger(NativeRustServerProvider.class);
    private static final String VERSION = "0.1.0 (tokio+hyper+panama)";
    private static volatile Boolean available;

    @Override
    public boolean isAvailable() {
        if (available == null) {
            // 1) JDK 版本检测 — Panama FFI 需要 ≥21
            if (!isJdk21Plus()) {
                log.debug("[RustServer] JDK {} < 21, not available", System.getProperty("java.version"));
                available = false;
                return false;
            }

            // 2) Native 库文件检测
            try {
                String libName = System.mapLibraryName("rust_server");
                available = findLibrary(libName);
            } catch (Exception e) {
                log.debug("[RustServer] Not available: {}", e.getMessage());
                available = false;
            }
        }
        return available;
    }

    @Override
    public String version() {
        return VERSION;
    }

    /**
     * 检查当前 JDK 版本是否 ≥21。
     * 兼容 JDK 8~25：通过 {@link Runtime#version()} 或回退 {@code System.getProperty("java.version")} 解析。
     */
    private static boolean isJdk21Plus() {
        try {
            // JDK 9+ 优先使用 Runtime.version()
            return Runtime.version().feature() >= 21;
        } catch (Throwable ignored) {
            // JDK 8 及以下：Runtime.version() 不存在，回退解析 java.version
            try {
                String ver = System.getProperty("java.version");
                if (ver == null) return false;
                if (ver.startsWith("1.")) {
                    // "1.8.0_xxx" → JDK 8
                    return false;
                }
                int dot = ver.indexOf('.');
                int major = dot > 0 ? Integer.parseInt(ver.substring(0, dot)) : Integer.parseInt(ver);
                return major >= 21;
            } catch (Exception e) {
                return false;
            }
        }
    }

    private static boolean findLibrary(String libName) {
        // java.library.path
        String libPath = System.getProperty("java.library.path");
        if (libPath != null) {
            for (String dir : libPath.split(System.getProperty("path.separator"))) {
                if (java.nio.file.Files.exists(java.nio.file.Path.of(dir, libName))) {
                    return true;
                }
            }
        }
        // user.dir/native/
        if (java.nio.file.Files.exists(
                java.nio.file.Path.of(System.getProperty("user.dir"), "native", libName))) {
            return true;
        }
        // cargo 产物
        if (java.nio.file.Files.exists(
                java.nio.file.Path.of("src/main/rust/target/release").resolve(libName))) {
            return true;
        }
        return false;
    }
}
