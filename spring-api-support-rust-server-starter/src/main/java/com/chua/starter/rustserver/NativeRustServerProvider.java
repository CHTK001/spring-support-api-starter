package com.chua.starter.rustserver;

import lombok.extern.slf4j.Slf4j;

/**
 * {@link RustServerProvider} 默认实现。
 *
 * <p>检查 native 库是否可加载，提供模块版本信息。</p>
 */
@Slf4j
public class NativeRustServerProvider implements RustServerProvider {

    private static final String VERSION = "0.1.0 (tokio+hyper+panama)";
    private static volatile Boolean available;

    @Override
    public boolean isAvailable() {
        if (available == null) {
            try {
                String libName = System.mapLibraryName("rust_server");
                // 尝试查找但不加载 (如果加载失败会有 UnsatisfiedLinkError)
                boolean found = findLibrary(libName);
                available = found;
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
