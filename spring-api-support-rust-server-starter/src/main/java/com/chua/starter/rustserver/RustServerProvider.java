package com.chua.starter.rustserver;

import com.chua.common.support.core.annotation.Spi;

/**
 * Rust Server SPI 接口 — 用于 utils-common SPI 机制发现 Rust 服务器模块。
 *
 * <p>通过 {@code ServiceProvider.of(RustServerProvider.class)} 检测是否可用。</p>
 */
@Spi("rust-server")
public interface RustServerProvider {

    /** 模块是否可用（native 库是否存在） */
    boolean isAvailable();

    /** 返回模块版本信息 */
    String version();
}
